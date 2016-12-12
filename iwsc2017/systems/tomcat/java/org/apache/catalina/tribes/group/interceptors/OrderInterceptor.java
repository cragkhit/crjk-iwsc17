package org.apache.catalina.tribes.group.interceptors;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
import org.apache.catalina.tribes.group.InterceptorPayload;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.util.StringManager;
public class OrderInterceptor extends ChannelInterceptorBase {
    protected static final StringManager sm = StringManager.getManager ( OrderInterceptor.class );
    private final HashMap<Member, Counter> outcounter = new HashMap<>();
    private final HashMap<Member, Counter> incounter = new HashMap<>();
    private final HashMap<Member, MessageOrder> incoming = new HashMap<>();
    private long expire = 3000;
    private boolean forwardExpired = true;
    private int maxQueue = Integer.MAX_VALUE;
    final ReentrantReadWriteLock inLock = new ReentrantReadWriteLock ( true );
    final ReentrantReadWriteLock outLock = new ReentrantReadWriteLock ( true );
    @Override
    public void sendMessage ( Member[] destination, ChannelMessage msg, InterceptorPayload payload ) throws ChannelException {
        if ( !okToProcess ( msg.getOptions() ) ) {
            super.sendMessage ( destination, msg, payload );
            return;
        }
        ChannelException cx = null;
        for ( int i = 0; i < destination.length; i++ ) {
            try {
                int nr = 0;
                outLock.writeLock().lock();
                try {
                    nr = incCounter ( destination[i] );
                } finally {
                    outLock.writeLock().unlock();
                }
                msg.getMessage().append ( nr );
                try {
                    getNext().sendMessage ( new Member[] {destination[i]}, msg, payload );
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
    public void messageReceived ( ChannelMessage msg ) {
        if ( !okToProcess ( msg.getOptions() ) ) {
            super.messageReceived ( msg );
            return;
        }
        int msgnr = XByteBuffer.toInt ( msg.getMessage().getBytesDirect(), msg.getMessage().getLength() - 4 );
        msg.getMessage().trim ( 4 );
        MessageOrder order = new MessageOrder ( msgnr, ( ChannelMessage ) msg.deepclone() );
        inLock.writeLock().lock();
        try {
            if ( processIncoming ( order ) ) {
                processLeftOvers ( msg.getAddress(), false );
            }
        } finally {
            inLock.writeLock().unlock();
        }
    }
    protected void processLeftOvers ( Member member, boolean force ) {
        MessageOrder tmp = incoming.get ( member );
        if ( force ) {
            Counter cnt = getInCounter ( member );
            cnt.setCounter ( Integer.MAX_VALUE );
        }
        if ( tmp != null ) {
            processIncoming ( tmp );
        }
    }
    protected boolean processIncoming ( MessageOrder order ) {
        boolean result = false;
        Member member = order.getMessage().getAddress();
        Counter cnt = getInCounter ( member );
        MessageOrder tmp = incoming.get ( member );
        if ( tmp != null ) {
            order = MessageOrder.add ( tmp, order );
        }
        while ( ( order != null ) && ( order.getMsgNr() <= cnt.getCounter() ) ) {
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
        tmp = order;
        boolean empty = order != null ? order.getCount() >= maxQueue : false;
        while ( tmp != null ) {
            if ( tmp.isExpired ( expire ) || empty ) {
                if ( tmp == head ) {
                    head = tmp.next;
                }
                cnt.setCounter ( tmp.getMsgNr() + 1 );
                if ( getForwardExpired() ) {
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
            incoming.remove ( member );
        } else {
            incoming.put ( member, head );
        }
        return result;
    }
    @Override
    public void memberAdded ( Member member ) {
        super.memberAdded ( member );
    }
    @Override
    public void memberDisappeared ( Member member ) {
        incounter.remove ( member );
        outcounter.remove ( member );
        processLeftOvers ( member, true );
        super.memberDisappeared ( member );
    }
    protected int incCounter ( Member mbr ) {
        Counter cnt = getOutCounter ( mbr );
        return cnt.inc();
    }
    protected Counter getInCounter ( Member mbr ) {
        Counter cnt = incounter.get ( mbr );
        if ( cnt == null ) {
            cnt = new Counter();
            cnt.inc();
            incounter.put ( mbr, cnt );
        }
        return cnt;
    }
    protected Counter getOutCounter ( Member mbr ) {
        Counter cnt = outcounter.get ( mbr );
        if ( cnt == null ) {
            cnt = new Counter();
            outcounter.put ( mbr, cnt );
        }
        return cnt;
    }
    protected static class Counter {
        private final AtomicInteger value = new AtomicInteger ( 0 );
        public int getCounter() {
            return value.get();
        }
        public void setCounter ( int counter ) {
            this.value.set ( counter );
        }
        public int inc() {
            return value.addAndGet ( 1 );
        }
    }
    protected static class MessageOrder {
        private final long received = System.currentTimeMillis();
        private MessageOrder next;
        private final int msgNr;
        private ChannelMessage msg = null;
        public MessageOrder ( int msgNr, ChannelMessage msg ) {
            this.msgNr = msgNr;
            this.msg = msg;
        }
        public boolean isExpired ( long expireTime ) {
            return ( System.currentTimeMillis() - received ) > expireTime;
        }
        public ChannelMessage getMessage() {
            return msg;
        }
        public void setMessage ( ChannelMessage msg ) {
            this.msg = msg;
        }
        public void setNext ( MessageOrder order ) {
            this.next = order;
        }
        public MessageOrder getNext() {
            return next;
        }
        public int getCount() {
            int counter = 1;
            MessageOrder tmp = next;
            while ( tmp != null ) {
                counter++;
                tmp = tmp.next;
            }
            return counter;
        }
        @SuppressWarnings ( "null" )
        public static MessageOrder add ( MessageOrder head, MessageOrder add ) {
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
            while ( iter.getMsgNr() < add.getMsgNr() && ( iter.next != null ) ) {
                prev = iter;
                iter = iter.next;
            }
            if ( iter.getMsgNr() < add.getMsgNr() ) {
                add.next = iter.next;
                iter.next = add;
            } else if ( iter.getMsgNr() > add.getMsgNr() ) {
                prev.next = add;
                add.next = iter;
            } else {
                throw new ArithmeticException ( sm.getString ( "orderInterceptor.messageAdded.sameCounter" ) );
            }
            return head;
        }
        public int getMsgNr() {
            return msgNr;
        }
    }
    public void setExpire ( long expire ) {
        this.expire = expire;
    }
    public void setForwardExpired ( boolean forwardExpired ) {
        this.forwardExpired = forwardExpired;
    }
    public void setMaxQueue ( int maxQueue ) {
        this.maxQueue = maxQueue;
    }
    public long getExpire() {
        return expire;
    }
    public boolean getForwardExpired() {
        return forwardExpired;
    }
    public int getMaxQueue() {
        return maxQueue;
    }
}
