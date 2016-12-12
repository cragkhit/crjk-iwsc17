package org.apache.catalina.tribes.group.interceptors;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.group.InterceptorPayload;
import org.apache.catalina.tribes.ChannelMessage;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.catalina.tribes.Member;
import java.util.HashMap;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
public class OrderInterceptor extends ChannelInterceptorBase {
    protected static final StringManager sm;
    private final HashMap<Member, Counter> outcounter;
    private final HashMap<Member, Counter> incounter;
    private final HashMap<Member, MessageOrder> incoming;
    private long expire;
    private boolean forwardExpired;
    private int maxQueue;
    final ReentrantReadWriteLock inLock;
    final ReentrantReadWriteLock outLock;
    public OrderInterceptor() {
        this.outcounter = new HashMap<Member, Counter>();
        this.incounter = new HashMap<Member, Counter>();
        this.incoming = new HashMap<Member, MessageOrder>();
        this.expire = 3000L;
        this.forwardExpired = true;
        this.maxQueue = Integer.MAX_VALUE;
        this.inLock = new ReentrantReadWriteLock ( true );
        this.outLock = new ReentrantReadWriteLock ( true );
    }
    @Override
    public void sendMessage ( final Member[] destination, final ChannelMessage msg, final InterceptorPayload payload ) throws ChannelException {
        if ( !this.okToProcess ( msg.getOptions() ) ) {
            super.sendMessage ( destination, msg, payload );
            return;
        }
        ChannelException cx = null;
        for ( int i = 0; i < destination.length; ++i ) {
            try {
                int nr = 0;
                this.outLock.writeLock().lock();
                try {
                    nr = this.incCounter ( destination[i] );
                } finally {
                    this.outLock.writeLock().unlock();
                }
                msg.getMessage().append ( nr );
                try {
                    this.getNext().sendMessage ( new Member[] { destination[i] }, msg, payload );
                } finally {
                    msg.getMessage().trim ( 4 );
                }
            } catch ( ChannelException x ) {
                if ( cx == null ) {
                    cx = x;
                }
                cx.addFaultyMember ( x.getFaultyMembers() );
            }
        }
        if ( cx != null ) {
            throw cx;
        }
    }
    @Override
    public void messageReceived ( final ChannelMessage msg ) {
        if ( !this.okToProcess ( msg.getOptions() ) ) {
            super.messageReceived ( msg );
            return;
        }
        final int msgnr = XByteBuffer.toInt ( msg.getMessage().getBytesDirect(), msg.getMessage().getLength() - 4 );
        msg.getMessage().trim ( 4 );
        final MessageOrder order = new MessageOrder ( msgnr, ( ChannelMessage ) msg.deepclone() );
        this.inLock.writeLock().lock();
        try {
            if ( this.processIncoming ( order ) ) {
                this.processLeftOvers ( msg.getAddress(), false );
            }
        } finally {
            this.inLock.writeLock().unlock();
        }
    }
    protected void processLeftOvers ( final Member member, final boolean force ) {
        final MessageOrder tmp = this.incoming.get ( member );
        if ( force ) {
            final Counter cnt = this.getInCounter ( member );
            cnt.setCounter ( Integer.MAX_VALUE );
        }
        if ( tmp != null ) {
            this.processIncoming ( tmp );
        }
    }
    protected boolean processIncoming ( MessageOrder order ) {
        boolean result = false;
        final Member member = order.getMessage().getAddress();
        final Counter cnt = this.getInCounter ( member );
        MessageOrder tmp = this.incoming.get ( member );
        if ( tmp != null ) {
            order = MessageOrder.add ( tmp, order );
        }
        while ( order != null && order.getMsgNr() <= cnt.getCounter() ) {
            if ( order.getMsgNr() == cnt.getCounter() ) {
                cnt.inc();
            } else if ( order.getMsgNr() > cnt.getCounter() ) {
                cnt.setCounter ( order.getMsgNr() );
            }
            super.messageReceived ( order.getMessage() );
            order.setMessage ( null );
            order = order.next;
        }
        MessageOrder head = order;
        MessageOrder prev = null;
        final boolean empty = ( tmp = order ) != null && order.getCount() >= this.maxQueue;
        while ( tmp != null ) {
            if ( tmp.isExpired ( this.expire ) || empty ) {
                if ( tmp == head ) {
                    head = tmp.next;
                }
                cnt.setCounter ( tmp.getMsgNr() + 1 );
                if ( this.getForwardExpired() ) {
                    super.messageReceived ( tmp.getMessage() );
                }
                tmp.setMessage ( null );
                tmp = tmp.next;
                if ( prev != null ) {
                    prev.next = tmp;
                }
                result = true;
            } else {
                prev = tmp;
                tmp = tmp.next;
            }
        }
        if ( head == null ) {
            this.incoming.remove ( member );
        } else {
            this.incoming.put ( member, head );
        }
        return result;
    }
    @Override
    public void memberAdded ( final Member member ) {
        super.memberAdded ( member );
    }
    @Override
    public void memberDisappeared ( final Member member ) {
        this.incounter.remove ( member );
        this.outcounter.remove ( member );
        this.processLeftOvers ( member, true );
        super.memberDisappeared ( member );
    }
    protected int incCounter ( final Member mbr ) {
        final Counter cnt = this.getOutCounter ( mbr );
        return cnt.inc();
    }
    protected Counter getInCounter ( final Member mbr ) {
        Counter cnt = this.incounter.get ( mbr );
        if ( cnt == null ) {
            cnt = new Counter();
            cnt.inc();
            this.incounter.put ( mbr, cnt );
        }
        return cnt;
    }
    protected Counter getOutCounter ( final Member mbr ) {
        Counter cnt = this.outcounter.get ( mbr );
        if ( cnt == null ) {
            cnt = new Counter();
            this.outcounter.put ( mbr, cnt );
        }
        return cnt;
    }
    public void setExpire ( final long expire ) {
        this.expire = expire;
    }
    public void setForwardExpired ( final boolean forwardExpired ) {
        this.forwardExpired = forwardExpired;
    }
    public void setMaxQueue ( final int maxQueue ) {
        this.maxQueue = maxQueue;
    }
    public long getExpire() {
        return this.expire;
    }
    public boolean getForwardExpired() {
        return this.forwardExpired;
    }
    public int getMaxQueue() {
        return this.maxQueue;
    }
    static {
        sm = StringManager.getManager ( OrderInterceptor.class );
    }
    protected static class Counter {
        private final AtomicInteger value;
        protected Counter() {
            this.value = new AtomicInteger ( 0 );
        }
        public int getCounter() {
            return this.value.get();
        }
        public void setCounter ( final int counter ) {
            this.value.set ( counter );
        }
        public int inc() {
            return this.value.addAndGet ( 1 );
        }
    }
    protected static class MessageOrder {
        private final long received;
        private MessageOrder next;
        private final int msgNr;
        private ChannelMessage msg;
        public MessageOrder ( final int msgNr, final ChannelMessage msg ) {
            this.received = System.currentTimeMillis();
            this.msg = null;
            this.msgNr = msgNr;
            this.msg = msg;
        }
        public boolean isExpired ( final long expireTime ) {
            return System.currentTimeMillis() - this.received > expireTime;
        }
        public ChannelMessage getMessage() {
            return this.msg;
        }
        public void setMessage ( final ChannelMessage msg ) {
            this.msg = msg;
        }
        public void setNext ( final MessageOrder order ) {
            this.next = order;
        }
        public MessageOrder getNext() {
            return this.next;
        }
        public int getCount() {
            int counter = 1;
            for ( MessageOrder tmp = this.next; tmp != null; tmp = tmp.next ) {
                ++counter;
            }
            return counter;
        }
        public static MessageOrder add ( final MessageOrder head, final MessageOrder add ) {
            if ( head == null ) {
                return add;
            }
            if ( add == null ) {
                return head;
            }
            if ( head == add ) {
                return add;
            }
            if ( head.getMsgNr() > add.getMsgNr() ) {
                add.next = head;
                return add;
            }
            MessageOrder iter = head;
            MessageOrder prev = null;
            while ( iter.getMsgNr() < add.getMsgNr() && iter.next != null ) {
                prev = iter;
                iter = iter.next;
            }
            if ( iter.getMsgNr() < add.getMsgNr() ) {
                add.next = iter.next;
                iter.next = add;
            } else {
                if ( iter.getMsgNr() <= add.getMsgNr() ) {
                    throw new ArithmeticException ( OrderInterceptor.sm.getString ( "orderInterceptor.messageAdded.sameCounter" ) );
                }
                prev.next = add;
                add.next = iter;
            }
            return head;
        }
        public int getMsgNr() {
            return this.msgNr;
        }
    }
}
