package org.apache.catalina.tribes.group;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.catalina.tribes.ByteMessage;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelInterceptor;
import org.apache.catalina.tribes.ChannelListener;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.ChannelReceiver;
import org.apache.catalina.tribes.ChannelSender;
import org.apache.catalina.tribes.ErrorHandler;
import org.apache.catalina.tribes.Heartbeat;
import org.apache.catalina.tribes.ManagedChannel;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.MembershipListener;
import org.apache.catalina.tribes.MembershipService;
import org.apache.catalina.tribes.RemoteProcessException;
import org.apache.catalina.tribes.UniqueId;
import org.apache.catalina.tribes.group.interceptors.MessageDispatchInterceptor;
import org.apache.catalina.tribes.io.BufferPool;
import org.apache.catalina.tribes.io.ChannelData;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.util.Arrays;
import org.apache.catalina.tribes.util.Logs;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class GroupChannel extends ChannelInterceptorBase implements ManagedChannel {
    private static final Log log = LogFactory.getLog ( GroupChannel.class );
    protected static final StringManager sm = StringManager.getManager ( GroupChannel.class );
    protected boolean heartbeat = true;
    protected long heartbeatSleeptime = 5 * 1000;
    protected HeartbeatThread hbthread = null;
    protected final ChannelCoordinator coordinator = new ChannelCoordinator();
    protected ChannelInterceptor interceptors = null;
    protected final List<MembershipListener> membershipListeners = new CopyOnWriteArrayList<>();
    protected final List<ChannelListener> channelListeners = new CopyOnWriteArrayList<>();
    protected boolean optionCheck = false;
    protected String name = null;
    public GroupChannel() {
        addInterceptor ( this );
    }
    @Override
    public void addInterceptor ( ChannelInterceptor interceptor ) {
        if ( interceptors == null ) {
            interceptors = interceptor;
            interceptors.setNext ( coordinator );
            interceptors.setPrevious ( null );
            coordinator.setPrevious ( interceptors );
        } else {
            ChannelInterceptor last = interceptors;
            while ( last.getNext() != coordinator ) {
                last = last.getNext();
            }
            last.setNext ( interceptor );
            interceptor.setNext ( coordinator );
            interceptor.setPrevious ( last );
            coordinator.setPrevious ( interceptor );
        }
    }
    @Override
    public void heartbeat() {
        super.heartbeat();
        Iterator<MembershipListener> membershipListenerIterator = membershipListeners.iterator();
        while ( membershipListenerIterator.hasNext() ) {
            MembershipListener listener = membershipListenerIterator.next();
            if ( listener instanceof Heartbeat ) {
                ( ( Heartbeat ) listener ).heartbeat();
            }
        }
        Iterator<ChannelListener> channelListenerIterator = channelListeners.iterator();
        while ( channelListenerIterator.hasNext() ) {
            ChannelListener listener = channelListenerIterator.next();
            if ( listener instanceof Heartbeat ) {
                ( ( Heartbeat ) listener ).heartbeat();
            }
        }
    }
    @Override
    public UniqueId send ( Member[] destination, Serializable msg, int options )
    throws ChannelException {
        return send ( destination, msg, options, null );
    }
    @Override
    public UniqueId send ( Member[] destination, Serializable msg, int options, ErrorHandler handler )
    throws ChannelException {
        if ( msg == null ) {
            throw new ChannelException ( sm.getString ( "groupChannel.nullMessage" ) );
        }
        XByteBuffer buffer = null;
        try {
            if ( destination == null || destination.length == 0 ) {
                throw new ChannelException ( sm.getString ( "groupChannel.noDestination" ) );
            }
            ChannelData data = new ChannelData ( true );
            data.setAddress ( getLocalMember ( false ) );
            data.setTimestamp ( System.currentTimeMillis() );
            byte[] b = null;
            if ( msg instanceof ByteMessage ) {
                b = ( ( ByteMessage ) msg ).getMessage();
                options = options | SEND_OPTIONS_BYTE_MESSAGE;
            } else {
                b = XByteBuffer.serialize ( msg );
                options = options & ( ~SEND_OPTIONS_BYTE_MESSAGE );
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
            getFirstInterceptor().sendMessage ( destination, data, payload );
            if ( Logs.MESSAGES.isTraceEnabled() ) {
                Logs.MESSAGES.trace ( "GroupChannel - Sent msg:" + new UniqueId ( data.getUniqueId() ) +
                                      " at " + new java.sql.Timestamp ( System.currentTimeMillis() ) + " to " +
                                      Arrays.toNameString ( destination ) );
                Logs.MESSAGES.trace ( "GroupChannel - Send Message:" +
                                      new UniqueId ( data.getUniqueId() ) + " is " + msg );
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
    public void messageReceived ( ChannelMessage msg ) {
        if ( msg == null ) {
            return;
        }
        try {
            if ( Logs.MESSAGES.isTraceEnabled() ) {
                Logs.MESSAGES.trace ( "GroupChannel - Received msg:" +
                                      new UniqueId ( msg.getUniqueId() ) + " at " +
                                      new java.sql.Timestamp ( System.currentTimeMillis() ) + " from " +
                                      msg.getAddress().getName() );
            }
            Serializable fwd = null;
            if ( ( msg.getOptions() & SEND_OPTIONS_BYTE_MESSAGE ) == SEND_OPTIONS_BYTE_MESSAGE ) {
                fwd = new ByteMessage ( msg.getMessage().getBytes() );
            } else {
                try {
                    fwd = XByteBuffer.deserialize ( msg.getMessage().getBytesDirect(), 0,
                                                    msg.getMessage().getLength() );
                } catch ( Exception sx ) {
                    log.error ( sm.getString ( "groupChannel.unable.deserialize", msg ), sx );
                    return;
                }
            }
            if ( Logs.MESSAGES.isTraceEnabled() ) {
                Logs.MESSAGES.trace ( "GroupChannel - Receive Message:" +
                                      new UniqueId ( msg.getUniqueId() ) + " is " + fwd );
            }
            Member source = msg.getAddress();
            boolean rx = false;
            boolean delivered = false;
            for ( int i = 0; i < channelListeners.size(); i++ ) {
                ChannelListener channelListener = channelListeners.get ( i );
                if ( channelListener != null && channelListener.accept ( fwd, source ) ) {
                    channelListener.messageReceived ( fwd, source );
                    delivered = true;
                    if ( channelListener instanceof RpcChannel ) {
                        rx = true;
                    }
                }
            }
            if ( ( !rx ) && ( fwd instanceof RpcMessage ) ) {
                sendNoRpcChannelReply ( ( RpcMessage ) fwd, source );
            }
            if ( Logs.MESSAGES.isTraceEnabled() ) {
                Logs.MESSAGES.trace ( "GroupChannel delivered[" + delivered + "] id:" +
                                      new UniqueId ( msg.getUniqueId() ) );
            }
        } catch ( Exception x ) {
            if ( log.isWarnEnabled() ) {
                log.warn ( sm.getString ( "groupChannel.receiving.error" ), x );
            }
            throw new RemoteProcessException ( "Exception:" + x.getMessage(), x );
        }
    }
    protected void sendNoRpcChannelReply ( RpcMessage msg, Member destination ) {
        try {
            if ( msg instanceof RpcMessage.NoRpcChannelReply ) {
                return;
            }
            RpcMessage.NoRpcChannelReply reply =
                new RpcMessage.NoRpcChannelReply ( msg.rpcId, msg.uuid );
            send ( new Member[] {destination}, reply, Channel.SEND_OPTIONS_ASYNCHRONOUS );
        } catch ( Exception x ) {
            log.error ( sm.getString ( "groupChannel.sendFail.noRpcChannelReply" ), x );
        }
    }
    @Override
    public void memberAdded ( Member member ) {
        for ( int i = 0; i < membershipListeners.size(); i++ ) {
            MembershipListener membershipListener = membershipListeners.get ( i );
            if ( membershipListener != null ) {
                membershipListener.memberAdded ( member );
            }
        }
    }
    @Override
    public void memberDisappeared ( Member member ) {
        for ( int i = 0; i < membershipListeners.size(); i++ ) {
            MembershipListener membershipListener = membershipListeners.get ( i );
            if ( membershipListener != null ) {
                membershipListener.memberDisappeared ( member );
            }
        }
    }
    protected synchronized void setupDefaultStack() throws ChannelException {
        if ( getFirstInterceptor() != null &&
                ( ( getFirstInterceptor().getNext() instanceof ChannelCoordinator ) ) ) {
            addInterceptor ( new MessageDispatchInterceptor() );
        }
        Iterator<ChannelInterceptor> interceptors = getInterceptors();
        while ( interceptors.hasNext() ) {
            ChannelInterceptor channelInterceptor = interceptors.next();
            channelInterceptor.setChannel ( this );
        }
        coordinator.setChannel ( this );
    }
    protected void checkOptionFlags() throws ChannelException {
        StringBuilder conflicts = new StringBuilder();
        ChannelInterceptor first = interceptors;
        while ( first != null ) {
            int flag = first.getOptionFlag();
            if ( flag != 0 ) {
                ChannelInterceptor next = first.getNext();
                while ( next != null ) {
                    int nflag = next.getOptionFlag();
                    if ( nflag != 0 && ( ( ( flag & nflag ) == flag ) || ( ( flag & nflag ) == nflag ) ) ) {
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
                    next = next.getNext();
                }
            }
            first = first.getNext();
        }
        if ( conflicts.length() > 0 ) throw new ChannelException ( sm.getString ( "groupChannel.optionFlag.conflict",
                    conflicts.toString() ) );
    }
    @Override
    public synchronized void start ( int svc ) throws ChannelException {
        setupDefaultStack();
        if ( optionCheck ) {
            checkOptionFlags();
        }
        super.start ( svc );
        if ( hbthread == null && heartbeat ) {
            hbthread = new HeartbeatThread ( this, heartbeatSleeptime );
            hbthread.start();
        }
    }
    @Override
    public synchronized void stop ( int svc ) throws ChannelException {
        if ( hbthread != null ) {
            hbthread.stopHeartbeat();
            hbthread = null;
        }
        super.stop ( svc );
    }
    public ChannelInterceptor getFirstInterceptor() {
        if ( interceptors != null ) {
            return interceptors;
        } else {
            return coordinator;
        }
    }
    @Override
    public ChannelReceiver getChannelReceiver() {
        return coordinator.getClusterReceiver();
    }
    @Override
    public ChannelSender getChannelSender() {
        return coordinator.getClusterSender();
    }
    @Override
    public MembershipService getMembershipService() {
        return coordinator.getMembershipService();
    }
    @Override
    public void setChannelReceiver ( ChannelReceiver clusterReceiver ) {
        coordinator.setClusterReceiver ( clusterReceiver );
    }
    @Override
    public void setChannelSender ( ChannelSender clusterSender ) {
        coordinator.setClusterSender ( clusterSender );
    }
    @Override
    public void setMembershipService ( MembershipService membershipService ) {
        coordinator.setMembershipService ( membershipService );
    }
    @Override
    public void addMembershipListener ( MembershipListener membershipListener ) {
        if ( !this.membershipListeners.contains ( membershipListener ) ) {
            this.membershipListeners.add ( membershipListener );
        }
    }
    @Override
    public void removeMembershipListener ( MembershipListener membershipListener ) {
        membershipListeners.remove ( membershipListener );
    }
    @Override
    public void addChannelListener ( ChannelListener channelListener ) {
        if ( !this.channelListeners.contains ( channelListener ) ) {
            this.channelListeners.add ( channelListener );
        } else {
            throw new IllegalArgumentException ( sm.getString ( "groupChannel.listener.alreadyExist",
                                                 channelListener, channelListener.getClass().getName() ) );
        }
    }
    @Override
    public void removeChannelListener ( ChannelListener channelListener ) {
        channelListeners.remove ( channelListener );
    }
    @Override
    public Iterator<ChannelInterceptor> getInterceptors() {
        return new InterceptorIterator ( this.getNext(), this.coordinator );
    }
    public void setOptionCheck ( boolean optionCheck ) {
        this.optionCheck = optionCheck;
    }
    public void setHeartbeatSleeptime ( long heartbeatSleeptime ) {
        this.heartbeatSleeptime = heartbeatSleeptime;
    }
    @Override
    public void setHeartbeat ( boolean heartbeat ) {
        this.heartbeat = heartbeat;
    }
    public boolean getOptionCheck() {
        return optionCheck;
    }
    public boolean getHeartbeat() {
        return heartbeat;
    }
    public long getHeartbeatSleeptime() {
        return heartbeatSleeptime;
    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public void setName ( String name ) {
        this.name = name;
    }
    public static class InterceptorIterator implements Iterator<ChannelInterceptor> {
        private final ChannelInterceptor end;
        private ChannelInterceptor start;
        public InterceptorIterator ( ChannelInterceptor start, ChannelInterceptor end ) {
            this.end = end;
            this.start = start;
        }
        @Override
        public boolean hasNext() {
            return start != null && start != end;
        }
        @Override
        public ChannelInterceptor next() {
            ChannelInterceptor result = null;
            if ( hasNext() ) {
                result = start;
                start = start.getNext();
            }
            return result;
        }
        @Override
        public void remove() {
        }
    }
    public static class HeartbeatThread extends Thread {
        private static final Log log = LogFactory.getLog ( HeartbeatThread.class );
        protected static int counter = 1;
        protected static synchronized int inc() {
            return counter++;
        }
        protected volatile boolean doRun = true;
        protected final GroupChannel channel;
        protected final long sleepTime;
        public HeartbeatThread ( GroupChannel channel, long sleepTime ) {
            super();
            this.setPriority ( MIN_PRIORITY );
            String channelName = "";
            if ( channel.getName() != null ) {
                channelName = "[" + channel.getName() + "]";
            }
            setName ( "GroupChannel-Heartbeat" + channelName + "-" + inc() );
            setDaemon ( true );
            this.channel = channel;
            this.sleepTime = sleepTime;
        }
        public void stopHeartbeat() {
            doRun = false;
            interrupt();
        }
        @Override
        public void run() {
            while ( doRun ) {
                try {
                    Thread.sleep ( sleepTime );
                    channel.heartbeat();
                } catch ( InterruptedException x ) {
                } catch ( Exception x ) {
                    log.error ( sm.getString ( "groupChannel.unable.sendHeartbeat" ), x );
                }
            }
        }
    }
}
