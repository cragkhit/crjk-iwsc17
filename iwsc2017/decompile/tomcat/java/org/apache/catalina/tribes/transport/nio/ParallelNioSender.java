package org.apache.catalina.tribes.transport.nio;
import org.apache.juli.logging.LogFactory;
import java.util.Map;
import java.net.UnknownHostException;
import java.util.Iterator;
import org.apache.catalina.tribes.transport.SenderState;
import java.sql.Timestamp;
import org.apache.catalina.tribes.UniqueId;
import org.apache.catalina.tribes.util.Logs;
import java.nio.channels.SelectionKey;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.io.ChannelData;
import org.apache.catalina.tribes.ChannelMessage;
import java.io.IOException;
import org.apache.catalina.tribes.Member;
import java.util.HashMap;
import java.nio.channels.Selector;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.tribes.transport.MultiPointSender;
import org.apache.catalina.tribes.transport.AbstractSender;
public class ParallelNioSender extends AbstractSender implements MultiPointSender {
    private static final Log log;
    protected static final StringManager sm;
    protected final long selectTimeout = 5000L;
    protected final Selector selector;
    protected final HashMap<Member, NioSender> nioSenders;
    public ParallelNioSender() throws IOException {
        this.nioSenders = new HashMap<Member, NioSender>();
        this.selector = Selector.open();
        this.setConnected ( true );
    }
    @Override
    public synchronized void sendMessage ( final Member[] destination, final ChannelMessage msg ) throws ChannelException {
        final long start = System.currentTimeMillis();
        this.setUdpBased ( ( msg.getOptions() & 0x20 ) == 0x20 );
        final byte[] data = XByteBuffer.createDataPackage ( ( ChannelData ) msg );
        final NioSender[] senders = this.setupForSend ( destination );
        this.connect ( senders );
        this.setData ( senders, data );
        int remaining = senders.length;
        ChannelException cx = null;
        try {
            long delta = System.currentTimeMillis() - start;
            final boolean waitForAck = ( 0x2 & msg.getOptions() ) == 0x2;
            while ( remaining > 0 && delta < this.getTimeout() ) {
                try {
                    remaining -= this.doLoop ( 5000L, this.getMaxRetryAttempts(), waitForAck, msg );
                } catch ( Exception x ) {
                    if ( ParallelNioSender.log.isTraceEnabled() ) {
                        ParallelNioSender.log.trace ( "Error sending message", x );
                    }
                    final int faulty = ( cx == null ) ? 0 : cx.getFaultyMembers().length;
                    if ( cx == null ) {
                        if ( x instanceof ChannelException ) {
                            cx = ( ChannelException ) x;
                        } else {
                            cx = new ChannelException ( ParallelNioSender.sm.getString ( "parallelNioSender.send.failed" ), x );
                        }
                    } else if ( x instanceof ChannelException ) {
                        cx.addFaultyMember ( ( ( ChannelException ) x ).getFaultyMembers() );
                    }
                    if ( faulty < cx.getFaultyMembers().length ) {
                        remaining -= cx.getFaultyMembers().length - faulty;
                    }
                }
                if ( cx != null && cx.getFaultyMembers().length == remaining ) {
                    throw cx;
                }
                delta = System.currentTimeMillis() - start;
            }
            if ( remaining > 0 ) {
                final ChannelException cxtimeout = new ChannelException ( ParallelNioSender.sm.getString ( "parallelNioSender.operation.timedout", Long.toString ( this.getTimeout() ) ) );
                if ( cx == null ) {
                    cx = new ChannelException ( ParallelNioSender.sm.getString ( "parallelNioSender.operation.timedout", Long.toString ( this.getTimeout() ) ) );
                }
                for ( int i = 0; i < senders.length; ++i ) {
                    if ( !senders[i].isComplete() ) {
                        cx.addFaultyMember ( senders[i].getDestination(), cxtimeout );
                    }
                }
                throw cx;
            }
            if ( cx != null ) {
                throw cx;
            }
        } catch ( Exception x2 ) {
            try {
                this.disconnect();
            } catch ( Exception ex ) {}
            if ( x2 instanceof ChannelException ) {
                throw ( ChannelException ) x2;
            }
            throw new ChannelException ( x2 );
        }
    }
    private int doLoop ( final long selectTimeOut, final int maxAttempts, final boolean waitForAck, final ChannelMessage msg ) throws IOException, ChannelException {
        int completed = 0;
        final int selectedKeys = this.selector.select ( selectTimeOut );
        if ( selectedKeys == 0 ) {
            return 0;
        }
        final Iterator<SelectionKey> it = this.selector.selectedKeys().iterator();
        while ( it.hasNext() ) {
            final SelectionKey sk = it.next();
            it.remove();
            final int readyOps = sk.readyOps();
            sk.interestOps ( sk.interestOps() & ~readyOps );
            final NioSender sender = ( NioSender ) sk.attachment();
            try {
                if ( !sender.process ( sk, waitForAck ) ) {
                    continue;
                }
                ++completed;
                sender.setComplete ( true );
                if ( Logs.MESSAGES.isTraceEnabled() ) {
                    Logs.MESSAGES.trace ( "ParallelNioSender - Sent msg:" + new UniqueId ( msg.getUniqueId() ) + " at " + new Timestamp ( System.currentTimeMillis() ) + " to " + sender.getDestination().getName() );
                }
                SenderState.getSenderState ( sender.getDestination() ).setReady();
            } catch ( Exception x ) {
                if ( ParallelNioSender.log.isTraceEnabled() ) {
                    ParallelNioSender.log.trace ( "Error while processing send to " + sender.getDestination().getName(), x );
                }
                final SenderState state = SenderState.getSenderState ( sender.getDestination() );
                final int attempt = sender.getAttempt() + 1;
                final boolean retry = sender.getAttempt() <= maxAttempts && maxAttempts > 0;
                synchronized ( state ) {
                    if ( state.isSuspect() ) {
                        state.setFailing();
                    }
                    if ( state.isReady() ) {
                        state.setSuspect();
                        if ( retry ) {
                            ParallelNioSender.log.warn ( ParallelNioSender.sm.getString ( "parallelNioSender.send.fail.retrying", sender.getDestination().getName() ) );
                        } else {
                            ParallelNioSender.log.warn ( ParallelNioSender.sm.getString ( "parallelNioSender.send.fail", sender.getDestination().getName() ), x );
                        }
                    }
                }
                if ( !this.isConnected() ) {
                    ParallelNioSender.log.warn ( ParallelNioSender.sm.getString ( "parallelNioSender.sender.disconnected.notRetry", sender.getDestination().getName() ) );
                    final ChannelException cx = new ChannelException ( ParallelNioSender.sm.getString ( "parallelNioSender.sender.disconnected.sendFailed" ), x );
                    cx.addFaultyMember ( sender.getDestination(), x );
                    throw cx;
                }
                final byte[] data = sender.getMessage();
                if ( retry ) {
                    try {
                        sender.disconnect();
                        sender.connect();
                        sender.setAttempt ( attempt );
                        sender.setMessage ( data );
                    } catch ( Exception ignore ) {
                        state.setFailing();
                    }
                } else {
                    final ChannelException cx2 = new ChannelException ( ParallelNioSender.sm.getString ( "parallelNioSender.sendFailed.attempt", Integer.toString ( sender.getAttempt() ), Integer.toString ( maxAttempts ) ), x );
                    cx2.addFaultyMember ( sender.getDestination(), x );
                }
            }
        }
        return completed;
    }
    private void connect ( final NioSender[] senders ) throws ChannelException {
        ChannelException x = null;
        for ( int i = 0; i < senders.length; ++i ) {
            try {
                senders[i].connect();
            } catch ( IOException io ) {
                if ( x == null ) {
                    x = new ChannelException ( io );
                }
                x.addFaultyMember ( senders[i].getDestination(), io );
            }
        }
        if ( x != null ) {
            throw x;
        }
    }
    private void setData ( final NioSender[] senders, final byte[] data ) throws ChannelException {
        ChannelException x = null;
        for ( int i = 0; i < senders.length; ++i ) {
            try {
                senders[i].setMessage ( data );
            } catch ( IOException io ) {
                if ( x == null ) {
                    x = new ChannelException ( io );
                }
                x.addFaultyMember ( senders[i].getDestination(), io );
            }
        }
        if ( x != null ) {
            throw x;
        }
    }
    private NioSender[] setupForSend ( final Member[] destination ) throws ChannelException {
        ChannelException cx = null;
        final NioSender[] result = new NioSender[destination.length];
        for ( int i = 0; i < destination.length; ++i ) {
            NioSender sender = this.nioSenders.get ( destination[i] );
            try {
                if ( sender == null ) {
                    sender = new NioSender();
                    AbstractSender.transferProperties ( this, sender );
                    this.nioSenders.put ( destination[i], sender );
                }
                sender.reset();
                sender.setDestination ( destination[i] );
                sender.setSelector ( this.selector );
                sender.setUdpBased ( this.isUdpBased() );
                result[i] = sender;
            } catch ( UnknownHostException x ) {
                if ( cx == null ) {
                    cx = new ChannelException ( ParallelNioSender.sm.getString ( "parallelNioSender.unable.setup.NioSender" ), x );
                }
                cx.addFaultyMember ( destination[i], x );
            }
        }
        if ( cx != null ) {
            throw cx;
        }
        return result;
    }
    @Override
    public void connect() {
        this.setConnected ( true );
    }
    private synchronized void close() throws ChannelException {
        ChannelException x = null;
        final Object[] members = this.nioSenders.keySet().toArray();
        for ( int i = 0; i < members.length; ++i ) {
            final Member mbr = ( Member ) members[i];
            try {
                final NioSender sender = this.nioSenders.get ( mbr );
                sender.disconnect();
            } catch ( Exception e ) {
                if ( x == null ) {
                    x = new ChannelException ( e );
                }
                x.addFaultyMember ( mbr, e );
            }
            this.nioSenders.remove ( mbr );
        }
        if ( x != null ) {
            throw x;
        }
    }
    @Override
    public void add ( final Member member ) {
    }
    @Override
    public void remove ( final Member member ) {
        final NioSender sender = this.nioSenders.remove ( member );
        if ( sender != null ) {
            sender.disconnect();
        }
    }
    @Override
    public synchronized void disconnect() {
        this.setConnected ( false );
        try {
            this.close();
        } catch ( Exception ex ) {}
    }
    public void finalize() throws Throwable {
        try {
            this.disconnect();
        } catch ( Exception ex ) {}
        try {
            this.selector.close();
        } catch ( Exception e ) {
            if ( ParallelNioSender.log.isDebugEnabled() ) {
                ParallelNioSender.log.debug ( "Failed to close selector", e );
            }
        }
        super.finalize();
    }
    @Override
    public boolean keepalive() {
        boolean result = false;
        final Iterator<Map.Entry<Member, NioSender>> i = this.nioSenders.entrySet().iterator();
        while ( i.hasNext() ) {
            final Map.Entry<Member, NioSender> entry = i.next();
            final NioSender sender = entry.getValue();
            if ( sender.keepalive() ) {
                i.remove();
                result = true;
            } else {
                try {
                    sender.read();
                } catch ( IOException x2 ) {
                    sender.disconnect();
                    sender.reset();
                    i.remove();
                    result = true;
                } catch ( Exception x ) {
                    ParallelNioSender.log.warn ( ParallelNioSender.sm.getString ( "parallelNioSender.error.keepalive", sender ), x );
                }
            }
        }
        if ( result ) {
            try {
                this.selector.selectNow();
            } catch ( Exception ex ) {}
        }
        return result;
    }
    static {
        log = LogFactory.getLog ( ParallelNioSender.class );
        sm = StringManager.getManager ( ParallelNioSender.class );
    }
}
