package org.apache.catalina.tribes.transport;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.tribes.ChannelMessage;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.io.IOException;
import java.util.concurrent.ThreadFactory;
import org.apache.catalina.tribes.util.ExecutorFactory;
import java.util.concurrent.TimeUnit;
import org.apache.catalina.tribes.Channel;
import java.util.concurrent.ExecutorService;
import java.net.InetAddress;
import org.apache.catalina.tribes.MessageListener;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.tribes.io.ListenCallback;
import org.apache.catalina.tribes.ChannelReceiver;
public abstract class ReceiverBase implements ChannelReceiver, ListenCallback, RxTaskPool.TaskCreator {
    public static final int OPTION_DIRECT_BUFFER = 4;
    private static final Log log;
    private static final Object bindLock;
    protected static final StringManager sm;
    private MessageListener listener;
    private String host;
    private InetAddress bind;
    private int port;
    private int udpPort;
    private int securePort;
    private int rxBufSize;
    private int txBufSize;
    private int udpRxBufSize;
    private int udpTxBufSize;
    private volatile boolean listen;
    private RxTaskPool pool;
    private boolean direct;
    private long tcpSelectorTimeout;
    private int autoBind;
    private int maxThreads;
    private int minThreads;
    private int maxTasks;
    private int minTasks;
    private boolean tcpNoDelay;
    private boolean soKeepAlive;
    private boolean ooBInline;
    private boolean soReuseAddress;
    private boolean soLingerOn;
    private int soLingerTime;
    private int soTrafficClass;
    private int timeout;
    private boolean useBufferPool;
    private boolean daemon;
    private long maxIdleTime;
    private ExecutorService executor;
    private Channel channel;
    public ReceiverBase() {
        this.host = "auto";
        this.port = 4000;
        this.udpPort = -1;
        this.securePort = -1;
        this.rxBufSize = 43800;
        this.txBufSize = 25188;
        this.udpRxBufSize = 43800;
        this.udpTxBufSize = 25188;
        this.listen = false;
        this.direct = true;
        this.tcpSelectorTimeout = 5000L;
        this.autoBind = 100;
        this.maxThreads = 15;
        this.minThreads = 6;
        this.maxTasks = 100;
        this.minTasks = 10;
        this.tcpNoDelay = true;
        this.soKeepAlive = false;
        this.ooBInline = true;
        this.soReuseAddress = true;
        this.soLingerOn = true;
        this.soLingerTime = 3;
        this.soTrafficClass = 28;
        this.timeout = 3000;
        this.useBufferPool = true;
        this.daemon = true;
        this.maxIdleTime = 60000L;
    }
    @Override
    public void start() throws IOException {
        if ( this.executor == null ) {
            String channelName = "";
            if ( this.channel.getName() != null ) {
                channelName = "[" + this.channel.getName() + "]";
            }
            final TaskThreadFactory tf = new TaskThreadFactory ( "Tribes-Task-Receiver" + channelName + "-" );
            this.executor = ExecutorFactory.newThreadPool ( this.minThreads, this.maxThreads, this.maxIdleTime, TimeUnit.MILLISECONDS, tf );
        }
    }
    @Override
    public void stop() {
        if ( this.executor != null ) {
            this.executor.shutdownNow();
        }
        this.executor = null;
        this.channel = null;
    }
    @Override
    public MessageListener getMessageListener() {
        return this.listener;
    }
    @Override
    public int getPort() {
        return this.port;
    }
    public int getRxBufSize() {
        return this.rxBufSize;
    }
    public int getTxBufSize() {
        return this.txBufSize;
    }
    @Override
    public void setMessageListener ( final MessageListener listener ) {
        this.listener = listener;
    }
    public void setRxBufSize ( final int rxBufSize ) {
        this.rxBufSize = rxBufSize;
    }
    public void setTxBufSize ( final int txBufSize ) {
        this.txBufSize = txBufSize;
    }
    public InetAddress getBind() {
        if ( this.bind == null ) {
            try {
                if ( "auto".equals ( this.host ) ) {
                    this.host = InetAddress.getLocalHost().getHostAddress();
                }
                if ( ReceiverBase.log.isDebugEnabled() ) {
                    ReceiverBase.log.debug ( "Starting replication listener on address:" + this.host );
                }
                this.bind = InetAddress.getByName ( this.host );
            } catch ( IOException ioe ) {
                ReceiverBase.log.error ( ReceiverBase.sm.getString ( "receiverBase.bind.failed", this.host ), ioe );
            }
        }
        return this.bind;
    }
    protected void bind ( final ServerSocket socket, final int portstart, int retries ) throws IOException {
        synchronized ( ReceiverBase.bindLock ) {
            InetSocketAddress addr = null;
            int port = portstart;
            while ( retries > 0 ) {
                try {
                    addr = new InetSocketAddress ( this.getBind(), port );
                    socket.bind ( addr );
                    this.setPort ( port );
                    ReceiverBase.log.info ( ReceiverBase.sm.getString ( "receiverBase.socket.bind", addr ) );
                    retries = 0;
                } catch ( IOException x ) {
                    if ( --retries <= 0 ) {
                        ReceiverBase.log.info ( ReceiverBase.sm.getString ( "receiverBase.unable.bind", addr ) );
                        throw x;
                    }
                    ++port;
                }
            }
        }
    }
    protected int bindUdp ( final DatagramSocket socket, int portstart, int retries ) throws IOException {
        InetSocketAddress addr = null;
        while ( retries > 0 ) {
            try {
                addr = new InetSocketAddress ( this.getBind(), portstart );
                socket.bind ( addr );
                this.setUdpPort ( portstart );
                ReceiverBase.log.info ( ReceiverBase.sm.getString ( "receiverBase.udp.bind", addr ) );
                return 0;
            } catch ( IOException x ) {
                if ( --retries <= 0 ) {
                    ReceiverBase.log.info ( ReceiverBase.sm.getString ( "receiverBase.unable.bind.udp", addr ) );
                    throw x;
                }
                ++portstart;
                try {
                    Thread.sleep ( 25L );
                } catch ( InterruptedException ti ) {
                    Thread.currentThread().interrupt();
                }
                retries = this.bindUdp ( socket, portstart, retries );
                continue;
            }
            break;
        }
        return retries;
    }
    @Override
    public void messageDataReceived ( final ChannelMessage data ) {
        if ( this.listener != null && this.listener.accept ( data ) ) {
            this.listener.messageReceived ( data );
        }
    }
    public int getWorkerThreadOptions() {
        int options = 0;
        if ( this.getDirect() ) {
            options |= 0x4;
        }
        return options;
    }
    public void setBind ( final InetAddress bind ) {
        this.bind = bind;
    }
    public boolean getDirect() {
        return this.direct;
    }
    public void setDirect ( final boolean direct ) {
        this.direct = direct;
    }
    public String getAddress() {
        this.getBind();
        return this.host;
    }
    @Override
    public String getHost() {
        return this.getAddress();
    }
    public long getSelectorTimeout() {
        return this.tcpSelectorTimeout;
    }
    public boolean doListen() {
        return this.listen;
    }
    public MessageListener getListener() {
        return this.listener;
    }
    public RxTaskPool getTaskPool() {
        return this.pool;
    }
    public int getAutoBind() {
        return this.autoBind;
    }
    public int getMaxThreads() {
        return this.maxThreads;
    }
    public int getMinThreads() {
        return this.minThreads;
    }
    public boolean getTcpNoDelay() {
        return this.tcpNoDelay;
    }
    public boolean getSoKeepAlive() {
        return this.soKeepAlive;
    }
    public boolean getOoBInline() {
        return this.ooBInline;
    }
    public boolean getSoLingerOn() {
        return this.soLingerOn;
    }
    public int getSoLingerTime() {
        return this.soLingerTime;
    }
    public boolean getSoReuseAddress() {
        return this.soReuseAddress;
    }
    public int getSoTrafficClass() {
        return this.soTrafficClass;
    }
    public int getTimeout() {
        return this.timeout;
    }
    public boolean getUseBufferPool() {
        return this.useBufferPool;
    }
    @Override
    public int getSecurePort() {
        return this.securePort;
    }
    public int getMinTasks() {
        return this.minTasks;
    }
    public int getMaxTasks() {
        return this.maxTasks;
    }
    public ExecutorService getExecutor() {
        return this.executor;
    }
    public boolean isListening() {
        return this.listen;
    }
    public void setSelectorTimeout ( final long selTimeout ) {
        this.tcpSelectorTimeout = selTimeout;
    }
    public void setListen ( final boolean doListen ) {
        this.listen = doListen;
    }
    public void setAddress ( final String host ) {
        this.host = host;
    }
    public void setHost ( final String host ) {
        this.setAddress ( host );
    }
    public void setListener ( final MessageListener listener ) {
        this.listener = listener;
    }
    public void setPool ( final RxTaskPool pool ) {
        this.pool = pool;
    }
    public void setPort ( final int port ) {
        this.port = port;
    }
    public void setAutoBind ( final int autoBind ) {
        this.autoBind = autoBind;
        if ( this.autoBind <= 0 ) {
            this.autoBind = 1;
        }
    }
    public void setMaxThreads ( final int maxThreads ) {
        this.maxThreads = maxThreads;
    }
    public void setMinThreads ( final int minThreads ) {
        this.minThreads = minThreads;
    }
    public void setTcpNoDelay ( final boolean tcpNoDelay ) {
        this.tcpNoDelay = tcpNoDelay;
    }
    public void setSoKeepAlive ( final boolean soKeepAlive ) {
        this.soKeepAlive = soKeepAlive;
    }
    public void setOoBInline ( final boolean ooBInline ) {
        this.ooBInline = ooBInline;
    }
    public void setSoLingerOn ( final boolean soLingerOn ) {
        this.soLingerOn = soLingerOn;
    }
    public void setSoLingerTime ( final int soLingerTime ) {
        this.soLingerTime = soLingerTime;
    }
    public void setSoReuseAddress ( final boolean soReuseAddress ) {
        this.soReuseAddress = soReuseAddress;
    }
    public void setSoTrafficClass ( final int soTrafficClass ) {
        this.soTrafficClass = soTrafficClass;
    }
    public void setTimeout ( final int timeout ) {
        this.timeout = timeout;
    }
    public void setUseBufferPool ( final boolean useBufferPool ) {
        this.useBufferPool = useBufferPool;
    }
    public void setSecurePort ( final int securePort ) {
        this.securePort = securePort;
    }
    public void setMinTasks ( final int minTasks ) {
        this.minTasks = minTasks;
    }
    public void setMaxTasks ( final int maxTasks ) {
        this.maxTasks = maxTasks;
    }
    public void setExecutor ( final ExecutorService executor ) {
        this.executor = executor;
    }
    @Override
    public void heartbeat() {
    }
    @Override
    public int getUdpPort() {
        return this.udpPort;
    }
    public void setUdpPort ( final int udpPort ) {
        this.udpPort = udpPort;
    }
    public int getUdpRxBufSize() {
        return this.udpRxBufSize;
    }
    public void setUdpRxBufSize ( final int udpRxBufSize ) {
        this.udpRxBufSize = udpRxBufSize;
    }
    public int getUdpTxBufSize() {
        return this.udpTxBufSize;
    }
    public void setUdpTxBufSize ( final int udpTxBufSize ) {
        this.udpTxBufSize = udpTxBufSize;
    }
    @Override
    public Channel getChannel() {
        return this.channel;
    }
    @Override
    public void setChannel ( final Channel channel ) {
        this.channel = channel;
    }
    public boolean isDaemon() {
        return this.daemon;
    }
    public long getMaxIdleTime() {
        return this.maxIdleTime;
    }
    public void setDaemon ( final boolean daemon ) {
        this.daemon = daemon;
    }
    public void setMaxIdleTime ( final long maxIdleTime ) {
        this.maxIdleTime = maxIdleTime;
    }
    static {
        log = LogFactory.getLog ( ReceiverBase.class );
        bindLock = new Object();
        sm = StringManager.getManager ( "org.apache.catalina.tribes.transport" );
    }
    class TaskThreadFactory implements ThreadFactory {
        final ThreadGroup group;
        final AtomicInteger threadNumber;
        final String namePrefix;
        TaskThreadFactory ( final String namePrefix ) {
            this.threadNumber = new AtomicInteger ( 1 );
            final SecurityManager s = System.getSecurityManager();
            this.group = ( ( s != null ) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup() );
            this.namePrefix = namePrefix;
        }
        @Override
        public Thread newThread ( final Runnable r ) {
            final Thread t = new Thread ( this.group, r, this.namePrefix + this.threadNumber.getAndIncrement() );
            t.setDaemon ( ReceiverBase.this.daemon );
            t.setPriority ( 5 );
            return t;
        }
    }
}
