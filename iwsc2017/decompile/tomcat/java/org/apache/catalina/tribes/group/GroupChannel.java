package org.apache.catalina.tribes.group;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.tribes.MembershipService;
import org.apache.catalina.tribes.ChannelSender;
import org.apache.catalina.tribes.ChannelReceiver;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.group.interceptors.MessageDispatchInterceptor;
import org.apache.catalina.tribes.RemoteProcessException;
import org.apache.catalina.tribes.util.Arrays;
import java.sql.Timestamp;
import org.apache.catalina.tribes.util.Logs;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.io.BufferPool;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.ByteMessage;
import org.apache.catalina.tribes.io.ChannelData;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ErrorHandler;
import org.apache.catalina.tribes.UniqueId;
import java.io.Serializable;
import org.apache.catalina.tribes.Member;
import java.util.Iterator;
import org.apache.catalina.tribes.Heartbeat;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.catalina.tribes.ChannelListener;
import org.apache.catalina.tribes.MembershipListener;
import java.util.List;
import org.apache.catalina.tribes.ChannelInterceptor;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.tribes.ManagedChannel;
public class GroupChannel extends ChannelInterceptorBase implements ManagedChannel {
    private static final Log log;
    protected static final StringManager sm;
    protected boolean heartbeat;
    protected long heartbeatSleeptime;
    protected HeartbeatThread hbthread;
    protected final ChannelCoordinator coordinator;
    protected ChannelInterceptor interceptors;
    protected final List<MembershipListener> membershipListeners;
    protected final List<ChannelListener> channelListeners;
    protected boolean optionCheck;
    protected String name;
    public GroupChannel() {
        this.heartbeat = true;
        this.heartbeatSleeptime = 5000L;
        this.hbthread = null;
        this.coordinator = new ChannelCoordinator();
        this.interceptors = null;
        this.membershipListeners = new CopyOnWriteArrayList<MembershipListener>();
        this.channelListeners = new CopyOnWriteArrayList<ChannelListener>();
        this.optionCheck = false;
        this.name = null;
        this.addInterceptor ( this );
    }
    @Override
    public void addInterceptor ( final ChannelInterceptor interceptor ) {
        if ( this.interceptors == null ) {
            ( this.interceptors = interceptor ).setNext ( this.coordinator );
            this.interceptors.setPrevious ( null );
            this.coordinator.setPrevious ( this.interceptors );
        } else {
            ChannelInterceptor last;
            for ( last = this.interceptors; last.getNext() != this.coordinator; last = last.getNext() ) {}
            last.setNext ( interceptor );
            interceptor.setNext ( this.coordinator );
            interceptor.setPrevious ( last );
            this.coordinator.setPrevious ( interceptor );
        }
    }
    @Override
    public void heartbeat() {
        super.heartbeat();
        for ( final MembershipListener listener : this.membershipListeners ) {
            if ( listener instanceof Heartbeat ) {
                ( ( Heartbeat ) listener ).heartbeat();
            }
        }
        for ( final ChannelListener listener2 : this.channelListeners ) {
            if ( listener2 instanceof Heartbeat ) {
                ( ( Heartbeat ) listener2 ).heartbeat();
            }
        }
    }
    @Override
    public UniqueId send ( final Member[] destination, final Serializable msg, final int options ) throws ChannelException {
        return this.send ( destination, msg, options, null );
    }
    @Override
    public UniqueId send ( final Member[] destination, final Serializable msg, int options, final ErrorHandler handler ) throws ChannelException {
        if ( msg == null ) {
            throw new ChannelException ( GroupChannel.sm.getString ( "groupChannel.nullMessage" ) );
        }
        XByteBuffer buffer = null;
        try {
            if ( destination == null || destination.length == 0 ) {
                throw new ChannelException ( GroupChannel.sm.getString ( "groupChannel.noDestination" ) );
            }
            final ChannelData data = new ChannelData ( true );
            data.setAddress ( this.getLocalMember ( false ) );
            data.setTimestamp ( System.currentTimeMillis() );
            byte[] b = null;
            if ( msg instanceof ByteMessage ) {
                b = ( ( ByteMessage ) msg ).getMessage();
                options |= 0x1;
            } else {
                b = XByteBuffer.serialize ( msg );
                options &= 0xFFFFFFFE;
            }
            data.setOptions ( options );
            buffer = BufferPool.getBufferPool().getBuffer ( b.length + 128, false );
            buffer.append ( b, 0, b.length );
            data.setMessage ( buffer );
            InterceptorPayload payload = null;
            if ( handler != null ) {
                payload = new InterceptorPayload();
                payload.setErrorHandler ( handler );
            }
            this.getFirstInterceptor().sendMessage ( destination, data, payload );
            if ( Logs.MESSAGES.isTraceEnabled() ) {
                Logs.MESSAGES.trace ( "GroupChannel - Sent msg:" + new UniqueId ( data.getUniqueId() ) + " at " + new Timestamp ( System.currentTimeMillis() ) + " to " + Arrays.toNameString ( destination ) );
                Logs.MESSAGES.trace ( "GroupChannel - Send Message:" + new UniqueId ( data.getUniqueId() ) + " is " + msg );
            }
            return new UniqueId ( data.getUniqueId() );
        } catch ( Exception x ) {
            if ( x instanceof ChannelException ) {
                throw ( ChannelException ) x;
            }
            throw new ChannelException ( x );
        } finally {
            if ( buffer != null ) {
                BufferPool.getBufferPool().returnBuffer ( buffer );
            }
        }
    }
    @Override
    public void messageReceived ( final ChannelMessage msg ) {
        if ( msg == null ) {
            return;
        }
        try {
            if ( Logs.MESSAGES.isTraceEnabled() ) {
                Logs.MESSAGES.trace ( "GroupChannel - Received msg:" + new UniqueId ( msg.getUniqueId() ) + " at " + new Timestamp ( System.currentTimeMillis() ) + " from " + msg.getAddress().getName() );
            }
            Serializable fwd = null;
            if ( ( msg.getOptions() & 0x1 ) == 0x1 ) {
                fwd = new ByteMessage ( msg.getMessage().getBytes() );
            } else {
                try {
                    fwd = XByteBuffer.deserialize ( msg.getMessage().getBytesDirect(), 0, msg.getMessage().getLength() );
                } catch ( Exception sx ) {
                    GroupChannel.log.error ( GroupChannel.sm.getString ( "groupChannel.unable.deserialize", msg ), sx );
                    return;
                }
            }
            if ( Logs.MESSAGES.isTraceEnabled() ) {
                Logs.MESSAGES.trace ( "GroupChannel - Receive Message:" + new UniqueId ( msg.getUniqueId() ) + " is " + fwd );
            }
            final Member source = msg.getAddress();
            boolean rx = false;
            boolean delivered = false;
            for ( int i = 0; i < this.channelListeners.size(); ++i ) {
                final ChannelListener channelListener = this.channelListeners.get ( i );
                if ( channelListener != null && channelListener.accept ( fwd, source ) ) {
                    channelListener.messageReceived ( fwd, source );
                    delivered = true;
                    if ( channelListener instanceof RpcChannel ) {
                        rx = true;
                    }
                }
            }
            if ( !rx && fwd instanceof RpcMessage ) {
                this.sendNoRpcChannelReply ( ( RpcMessage ) fwd, source );
            }
            if ( Logs.MESSAGES.isTraceEnabled() ) {
                Logs.MESSAGES.trace ( "GroupChannel delivered[" + delivered + "] id:" + new UniqueId ( msg.getUniqueId() ) );
            }
        } catch ( Exception x ) {
            if ( GroupChannel.log.isWarnEnabled() ) {
                GroupChannel.log.warn ( GroupChannel.sm.getString ( "groupChannel.receiving.error" ), x );
            }
            throw new RemoteProcessException ( "Exception:" + x.getMessage(), x );
        }
    }
    protected void sendNoRpcChannelReply ( final RpcMessage msg, final Member destination ) {
        try {
            if ( msg instanceof RpcMessage.NoRpcChannelReply ) {
                return;
            }
            final RpcMessage.NoRpcChannelReply reply = new RpcMessage.NoRpcChannelReply ( msg.rpcId, msg.uuid );
            this.send ( new Member[] { destination }, reply, 8 );
        } catch ( Exception x ) {
            GroupChannel.log.error ( GroupChannel.sm.getString ( "groupChannel.sendFail.noRpcChannelReply" ), x );
        }
    }
    @Override
    public void memberAdded ( final Member member ) {
        for ( int i = 0; i < this.membershipListeners.size(); ++i ) {
            final MembershipListener membershipListener = this.membershipListeners.get ( i );
            if ( membershipListener != null ) {
                membershipListener.memberAdded ( member );
            }
        }
    }
    @Override
    public void memberDisappeared ( final Member member ) {
        for ( int i = 0; i < this.membershipListeners.size(); ++i ) {
            final MembershipListener membershipListener = this.membershipListeners.get ( i );
            if ( membershipListener != null ) {
                membershipListener.memberDisappeared ( member );
            }
        }
    }
    protected synchronized void setupDefaultStack() throws ChannelException {
        if ( this.getFirstInterceptor() != null && this.getFirstInterceptor().getNext() instanceof ChannelCoordinator ) {
            this.addInterceptor ( new MessageDispatchInterceptor() );
        }
        final Iterator<ChannelInterceptor> interceptors = this.getInterceptors();
        while ( interceptors.hasNext() ) {
            final ChannelInterceptor channelInterceptor = interceptors.next();
            channelInterceptor.setChannel ( this );
        }
        this.coordinator.setChannel ( this );
    }
    protected void checkOptionFlags() throws ChannelException {
        final StringBuilder conflicts = new StringBuilder();
        for ( ChannelInterceptor first = this.interceptors; first != null; first = first.getNext() ) {
            final int flag = first.getOptionFlag();
            if ( flag != 0 ) {
                for ( ChannelInterceptor next = first.getNext(); next != null; next = next.getNext() ) {
                    final int nflag = next.getOptionFlag();
                    if ( nflag != 0 && ( ( flag & nflag ) == flag || ( flag & nflag ) == nflag ) ) {
                        conflicts.append ( "[" );
                        conflicts.append ( first.getClass().getName() );
                        conflicts.append ( ":" );
                        conflicts.append ( flag );
                        conflicts.append ( " == " );
                        conflicts.append ( next.getClass().getName() );
                        conflicts.append ( ":" );
                        conflicts.append ( nflag );
                        conflicts.append ( "] " );
                    }
                }
            }
        }
        if ( conflicts.length() > 0 ) {
            throw new ChannelException ( GroupChannel.sm.getString ( "groupChannel.optionFlag.conflict", conflicts.toString() ) );
        }
    }
    @Override
    public synchronized void start ( final int svc ) throws ChannelException {
        this.setupDefaultStack();
        if ( this.optionCheck ) {
            this.checkOptionFlags();
        }
        super.start ( svc );
        if ( this.hbthread == null && this.heartbeat ) {
            ( this.hbthread = new HeartbeatThread ( this, this.heartbeatSleeptime ) ).start();
        }
    }
    @Override
    public synchronized void stop ( final int svc ) throws ChannelException {
        if ( this.hbthread != null ) {
            this.hbthread.stopHeartbeat();
            this.hbthread = null;
        }
        super.stop ( svc );
    }
    public ChannelInterceptor getFirstInterceptor() {
        if ( this.interceptors != null ) {
            return this.interceptors;
        }
        return this.coordinator;
    }
    @Override
    public ChannelReceiver getChannelReceiver() {
        return this.coordinator.getClusterReceiver();
    }
    @Override
    public ChannelSender getChannelSender() {
        return this.coordinator.getClusterSender();
    }
    @Override
    public MembershipService getMembershipService() {
        return this.coordinator.getMembershipService();
    }
    @Override
    public void setChannelReceiver ( final ChannelReceiver clusterReceiver ) {
        this.coordinator.setClusterReceiver ( clusterReceiver );
    }
    @Override
    public void setChannelSender ( final ChannelSender clusterSender ) {
        this.coordinator.setClusterSender ( clusterSender );
    }
    @Override
    public void setMembershipService ( final MembershipService membershipService ) {
        this.coordinator.setMembershipService ( membershipService );
    }
    @Override
    public void addMembershipListener ( final MembershipListener membershipListener ) {
        if ( !this.membershipListeners.contains ( membershipListener ) ) {
            this.membershipListeners.add ( membershipListener );
        }
    }
    @Override
    public void removeMembershipListener ( final MembershipListener membershipListener ) {
        this.membershipListeners.remove ( membershipListener );
    }
    @Override
    public void addChannelListener ( final ChannelListener channelListener ) {
        if ( !this.channelListeners.contains ( channelListener ) ) {
            this.channelListeners.add ( channelListener );
            return;
        }
        throw new IllegalArgumentException ( GroupChannel.sm.getString ( "groupChannel.listener.alreadyExist", channelListener, channelListener.getClass().getName() ) );
    }
    @Override
    public void removeChannelListener ( final ChannelListener channelListener ) {
        this.channelListeners.remove ( channelListener );
    }
    @Override
    public Iterator<ChannelInterceptor> getInterceptors() {
        return new InterceptorIterator ( this.getNext(), this.coordinator );
    }
    public void setOptionCheck ( final boolean optionCheck ) {
        this.optionCheck = optionCheck;
    }
    public void setHeartbeatSleeptime ( final long heartbeatSleeptime ) {
        this.heartbeatSleeptime = heartbeatSleeptime;
    }
    @Override
    public void setHeartbeat ( final boolean heartbeat ) {
        this.heartbeat = heartbeat;
    }
    public boolean getOptionCheck() {
        return this.optionCheck;
    }
    public boolean getHeartbeat() {
        return this.heartbeat;
    }
    public long getHeartbeatSleeptime() {
        return this.heartbeatSleeptime;
    }
    @Override
    public String getName() {
        return this.name;
    }
    @Override
    public void setName ( final String name ) {
        this.name = name;
    }
    static {
        log = LogFactory.getLog ( GroupChannel.class );
        sm = StringManager.getManager ( GroupChannel.class );
    }
    public static class InterceptorIterator implements Iterator<ChannelInterceptor> {
        private final ChannelInterceptor end;
        private ChannelInterceptor start;
        public InterceptorIterator ( final ChannelInterceptor start, final ChannelInterceptor end ) {
            this.end = end;
            this.start = start;
        }
        @Override
        public boolean hasNext() {
            return this.start != null && this.start != this.end;
        }
        @Override
        public ChannelInterceptor next() {
            ChannelInterceptor result = null;
            if ( this.hasNext() ) {
                result = this.start;
                this.start = this.start.getNext();
            }
            return result;
        }
        @Override
        public void remove() {
        }
    }
    public static class HeartbeatThread extends Thread {
        private static final Log log;
        protected static int counter;
        protected volatile boolean doRun;
        protected final GroupChannel channel;
        protected final long sleepTime;
        protected static synchronized int inc() {
            return HeartbeatThread.counter++;
        }
        public HeartbeatThread ( final GroupChannel channel, final long sleepTime ) {
            this.doRun = true;
            this.setPriority ( 1 );
            String channelName = "";
            if ( channel.getName() != null ) {
                channelName = "[" + channel.getName() + "]";
            }
            this.setName ( "GroupChannel-Heartbeat" + channelName + "-" + inc() );
            this.setDaemon ( true );
            this.channel = channel;
            this.sleepTime = sleepTime;
        }
        public void stopHeartbeat() {
            this.doRun = false;
            this.interrupt();
        }
        @Override
        public void run() {
            while ( this.doRun ) {
                try {
                    Thread.sleep ( this.sleepTime );
                    this.channel.heartbeat();
                } catch ( InterruptedException ex ) {}
                catch ( Exception x ) {
                    HeartbeatThread.log.error ( GroupChannel.sm.getString ( "groupChannel.unable.sendHeartbeat" ), x );
                }
            }
        }
        static {
            log = LogFactory.getLog ( HeartbeatThread.class );
            HeartbeatThread.counter = 1;
        }
    }
}
