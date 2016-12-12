package org.apache.catalina.tribes.group.interceptors;
import org.apache.juli.logging.LogFactory;
import java.util.Arrays;
import org.apache.catalina.tribes.group.InterceptorPayload;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.io.ChannelData;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelInterceptor;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
public class TcpPingInterceptor extends ChannelInterceptorBase {
    private static final Log log;
    protected static final StringManager sm;
    protected static final byte[] TCP_PING_DATA;
    protected long interval;
    protected boolean useThread;
    protected boolean staticOnly;
    protected volatile boolean running;
    protected PingThread thread;
    protected static final AtomicInteger cnt;
    WeakReference<TcpFailureDetector> failureDetector;
    WeakReference<StaticMembershipInterceptor> staticMembers;
    public TcpPingInterceptor() {
        this.interval = 1000L;
        this.useThread = false;
        this.staticOnly = false;
        this.running = true;
        this.thread = null;
        this.failureDetector = null;
        this.staticMembers = null;
    }
    @Override
    public synchronized void start ( final int svc ) throws ChannelException {
        super.start ( svc );
        this.running = true;
        if ( this.thread == null && this.useThread ) {
            ( this.thread = new PingThread() ).setDaemon ( true );
            String channelName = "";
            if ( this.getChannel().getName() != null ) {
                channelName = "[" + this.getChannel().getName() + "]";
            }
            this.thread.setName ( "TcpPingInterceptor.PingThread" + channelName + "-" + TcpPingInterceptor.cnt.addAndGet ( 1 ) );
            this.thread.start();
        }
        for ( ChannelInterceptor next = this.getNext(); next != null; next = next.getNext() ) {
            if ( next instanceof TcpFailureDetector ) {
                this.failureDetector = new WeakReference<TcpFailureDetector> ( ( TcpFailureDetector ) next );
            }
            if ( next instanceof StaticMembershipInterceptor ) {
                this.staticMembers = new WeakReference<StaticMembershipInterceptor> ( ( StaticMembershipInterceptor ) next );
            }
        }
    }
    @Override
    public void stop ( final int svc ) throws ChannelException {
        this.running = false;
        if ( this.thread != null ) {
            this.thread.interrupt();
            this.thread = null;
        }
        super.stop ( svc );
    }
    @Override
    public void heartbeat() {
        super.heartbeat();
        if ( !this.getUseThread() ) {
            this.sendPing();
        }
    }
    public long getInterval() {
        return this.interval;
    }
    public void setInterval ( final long interval ) {
        this.interval = interval;
    }
    public void setUseThread ( final boolean useThread ) {
        this.useThread = useThread;
    }
    public void setStaticOnly ( final boolean staticOnly ) {
        this.staticOnly = staticOnly;
    }
    public boolean getUseThread() {
        return this.useThread;
    }
    public boolean getStaticOnly() {
        return this.staticOnly;
    }
    protected void sendPing() {
        final TcpFailureDetector tcpFailureDetector = ( this.failureDetector != null ) ? this.failureDetector.get() : null;
        if ( tcpFailureDetector != null ) {
            tcpFailureDetector.checkMembers ( true );
        } else {
            final StaticMembershipInterceptor smi = ( this.staticOnly && this.staticMembers != null ) ? this.staticMembers.get() : null;
            if ( smi != null ) {
                this.sendPingMessage ( smi.getMembers() );
            } else {
                this.sendPingMessage ( this.getMembers() );
            }
        }
    }
    protected void sendPingMessage ( final Member[] members ) {
        if ( members == null || members.length == 0 ) {
            return;
        }
        final ChannelData data = new ChannelData ( true );
        data.setAddress ( this.getLocalMember ( false ) );
        data.setTimestamp ( System.currentTimeMillis() );
        data.setOptions ( this.getOptionFlag() );
        data.setMessage ( new XByteBuffer ( TcpPingInterceptor.TCP_PING_DATA, false ) );
        try {
            super.sendMessage ( members, data, null );
        } catch ( ChannelException x ) {
            TcpPingInterceptor.log.warn ( TcpPingInterceptor.sm.getString ( "tcpPingInterceptor.ping.failed" ), x );
        }
    }
    @Override
    public void messageReceived ( final ChannelMessage msg ) {
        boolean process = true;
        if ( this.okToProcess ( msg.getOptions() ) ) {
            process = ( msg.getMessage().getLength() != TcpPingInterceptor.TCP_PING_DATA.length || !Arrays.equals ( TcpPingInterceptor.TCP_PING_DATA, msg.getMessage().getBytes() ) );
        }
        if ( process ) {
            super.messageReceived ( msg );
        } else if ( TcpPingInterceptor.log.isDebugEnabled() ) {
            TcpPingInterceptor.log.debug ( "Received a TCP ping packet:" + msg );
        }
    }
    static {
        log = LogFactory.getLog ( TcpPingInterceptor.class );
        sm = StringManager.getManager ( TcpPingInterceptor.class );
        TCP_PING_DATA = new byte[] { 79, -89, 115, 72, 121, -33, 67, -55, -97, 111, -119, -128, -95, 91, 7, 20, 125, -39, 82, 91, -21, -33, 67, -102, -73, 126, -66, -113, -127, 103, 30, -74, 55, 21, -66, -121, 69, 33, 76, -88, -65, 10, 77, 19, 83, 56, 21, 50, 85, -10, -108, -73, 58, -33, 33, 120, -111, 4, 125, -41, 114, -124, -64, -43 };
        cnt = new AtomicInteger ( 0 );
    }
    protected class PingThread extends Thread {
        @Override
        public void run() {
            while ( TcpPingInterceptor.this.running ) {
                try {
                    Thread.sleep ( TcpPingInterceptor.this.interval );
                    TcpPingInterceptor.this.sendPing();
                } catch ( InterruptedException ex ) {}
                catch ( Exception x ) {
                    TcpPingInterceptor.log.warn ( TcpPingInterceptor.sm.getString ( "tcpPingInterceptor.pingFailed.pingThread" ), x );
                }
            }
        }
    }
}
