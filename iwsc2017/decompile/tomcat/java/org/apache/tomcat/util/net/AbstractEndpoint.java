package org.apache.tomcat.util.net;
import java.util.Set;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.ExceptionUtils;
import java.util.concurrent.RejectedExecutionException;
import java.io.OutputStreamWriter;
import java.net.SocketAddress;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.tomcat.util.threads.TaskThreadFactory;
import org.apache.tomcat.util.threads.TaskQueue;
import org.apache.tomcat.util.threads.ResizableExecutor;
import org.apache.tomcat.util.IntrospectionUtils;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.List;
import java.net.InetAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.ConcurrentMap;
import org.apache.tomcat.util.collections.SynchronizedStack;
import org.apache.tomcat.util.threads.LimitLatch;
import org.apache.tomcat.util.res.StringManager;
public abstract class AbstractEndpoint<S> {
    protected static final StringManager sm;
    private static final int INITIAL_ERROR_DELAY = 50;
    private static final int MAX_ERROR_DELAY = 1600;
    protected volatile boolean running;
    protected volatile boolean paused;
    protected volatile boolean internalExecutor;
    private volatile LimitLatch connectionLimitLatch;
    protected SocketProperties socketProperties;
    protected Acceptor[] acceptors;
    protected SynchronizedStack<SocketProcessorBase<S>> processorCache;
    private String defaultSSLHostConfigName;
    protected ConcurrentMap<String, SSLHostConfig> sslHostConfigs;
    private boolean useSendfile;
    private long executorTerminationTimeoutMillis;
    protected int acceptorThreadCount;
    protected int acceptorThreadPriority;
    private int maxConnections;
    private Executor executor;
    private int port;
    private InetAddress address;
    private int acceptCount;
    private boolean bindOnInit;
    private volatile BindState bindState;
    private Integer keepAliveTimeout;
    private boolean SSLEnabled;
    private int minSpareThreads;
    private int maxThreads;
    protected int threadPriority;
    private int maxKeepAliveRequests;
    private int maxHeaderCount;
    private String name;
    private boolean daemon;
    protected final List<String> negotiableProtocols;
    private Handler<S> handler;
    protected HashMap<String, Object> attributes;
    public AbstractEndpoint() {
        this.running = false;
        this.paused = false;
        this.internalExecutor = true;
        this.connectionLimitLatch = null;
        this.socketProperties = new SocketProperties();
        this.defaultSSLHostConfigName = "_default_";
        this.sslHostConfigs = new ConcurrentHashMap<String, SSLHostConfig>();
        this.useSendfile = true;
        this.executorTerminationTimeoutMillis = 5000L;
        this.acceptorThreadCount = 1;
        this.acceptorThreadPriority = 5;
        this.maxConnections = 10000;
        this.executor = null;
        this.acceptCount = 100;
        this.bindOnInit = true;
        this.bindState = BindState.UNBOUND;
        this.keepAliveTimeout = null;
        this.SSLEnabled = false;
        this.minSpareThreads = 10;
        this.maxThreads = 200;
        this.threadPriority = 5;
        this.maxKeepAliveRequests = 100;
        this.maxHeaderCount = 100;
        this.name = "TP";
        this.daemon = true;
        this.negotiableProtocols = new ArrayList<String>();
        this.handler = null;
        this.attributes = new HashMap<String, Object>();
    }
    public SocketProperties getSocketProperties() {
        return this.socketProperties;
    }
    public String getDefaultSSLHostConfigName() {
        return this.defaultSSLHostConfigName;
    }
    public void setDefaultSSLHostConfigName ( final String defaultSSLHostConfigName ) {
        this.defaultSSLHostConfigName = defaultSSLHostConfigName;
    }
    public void addSslHostConfig ( final SSLHostConfig sslHostConfig ) throws IllegalArgumentException {
        final String key = sslHostConfig.getHostName();
        if ( key == null || key.length() == 0 ) {
            throw new IllegalArgumentException ( AbstractEndpoint.sm.getString ( "endpoint.noSslHostName" ) );
        }
        sslHostConfig.setConfigType ( this.getSslConfigType() );
        if ( this.bindState != BindState.UNBOUND ) {
            try {
                this.createSSLContext ( sslHostConfig );
            } catch ( Exception e ) {
                throw new IllegalArgumentException ( e );
            }
        }
        final SSLHostConfig duplicate = this.sslHostConfigs.putIfAbsent ( key, sslHostConfig );
        if ( duplicate != null ) {
            this.releaseSSLContext ( sslHostConfig );
            throw new IllegalArgumentException ( AbstractEndpoint.sm.getString ( "endpoint.duplicateSslHostName", key ) );
        }
    }
    public SSLHostConfig[] findSslHostConfigs() {
        return this.sslHostConfigs.values().toArray ( new SSLHostConfig[0] );
    }
    protected abstract SSLHostConfig.Type getSslConfigType();
    protected abstract void createSSLContext ( final SSLHostConfig p0 ) throws Exception;
    protected abstract void releaseSSLContext ( final SSLHostConfig p0 );
    protected SSLHostConfig getSSLHostConfig ( final String sniHostName ) {
        SSLHostConfig result = null;
        if ( sniHostName != null ) {
            result = this.sslHostConfigs.get ( sniHostName );
            if ( result != null ) {
                return result;
            }
            final int indexOfDot = sniHostName.indexOf ( 46 );
            if ( indexOfDot > -1 ) {
                result = this.sslHostConfigs.get ( "*" + sniHostName.substring ( indexOfDot ) );
            }
        }
        if ( result == null ) {
            result = this.sslHostConfigs.get ( this.getDefaultSSLHostConfigName() );
        }
        if ( result == null ) {
            throw new IllegalStateException();
        }
        return result;
    }
    public boolean getUseSendfile() {
        return this.useSendfile;
    }
    public void setUseSendfile ( final boolean useSendfile ) {
        this.useSendfile = useSendfile;
    }
    public long getExecutorTerminationTimeoutMillis() {
        return this.executorTerminationTimeoutMillis;
    }
    public void setExecutorTerminationTimeoutMillis ( final long executorTerminationTimeoutMillis ) {
        this.executorTerminationTimeoutMillis = executorTerminationTimeoutMillis;
    }
    public void setAcceptorThreadCount ( final int acceptorThreadCount ) {
        this.acceptorThreadCount = acceptorThreadCount;
    }
    public int getAcceptorThreadCount() {
        return this.acceptorThreadCount;
    }
    public void setAcceptorThreadPriority ( final int acceptorThreadPriority ) {
        this.acceptorThreadPriority = acceptorThreadPriority;
    }
    public int getAcceptorThreadPriority() {
        return this.acceptorThreadPriority;
    }
    public void setMaxConnections ( final int maxCon ) {
        this.maxConnections = maxCon;
        final LimitLatch latch = this.connectionLimitLatch;
        if ( latch != null ) {
            if ( maxCon == -1 ) {
                this.releaseConnectionLatch();
            } else {
                latch.setLimit ( maxCon );
            }
        } else if ( maxCon > 0 ) {
            this.initializeConnectionLatch();
        }
    }
    public int getMaxConnections() {
        return this.maxConnections;
    }
    public long getConnectionCount() {
        final LimitLatch latch = this.connectionLimitLatch;
        if ( latch != null ) {
            return latch.getCount();
        }
        return -1L;
    }
    public void setExecutor ( final Executor executor ) {
        this.executor = executor;
        this.internalExecutor = ( executor == null );
    }
    public Executor getExecutor() {
        return this.executor;
    }
    public int getPort() {
        return this.port;
    }
    public void setPort ( final int port ) {
        this.port = port;
    }
    public abstract int getLocalPort();
    public InetAddress getAddress() {
        return this.address;
    }
    public void setAddress ( final InetAddress address ) {
        this.address = address;
    }
    public void setAcceptCount ( final int acceptCount ) {
        if ( acceptCount > 0 ) {
            this.acceptCount = acceptCount;
        }
    }
    public int getAcceptCount() {
        return this.acceptCount;
    }
    public boolean getBindOnInit() {
        return this.bindOnInit;
    }
    public void setBindOnInit ( final boolean b ) {
        this.bindOnInit = b;
    }
    public int getKeepAliveTimeout() {
        if ( this.keepAliveTimeout == null ) {
            return this.getConnectionTimeout();
        }
        return this.keepAliveTimeout;
    }
    public void setKeepAliveTimeout ( final int keepAliveTimeout ) {
        this.keepAliveTimeout = keepAliveTimeout;
    }
    public boolean getTcpNoDelay() {
        return this.socketProperties.getTcpNoDelay();
    }
    public void setTcpNoDelay ( final boolean tcpNoDelay ) {
        this.socketProperties.setTcpNoDelay ( tcpNoDelay );
    }
    public int getConnectionLinger() {
        return this.socketProperties.getSoLingerTime();
    }
    public void setConnectionLinger ( final int connectionLinger ) {
        this.socketProperties.setSoLingerTime ( connectionLinger );
        this.socketProperties.setSoLingerOn ( connectionLinger >= 0 );
    }
    public int getConnectionTimeout() {
        return this.socketProperties.getSoTimeout();
    }
    public void setConnectionTimeout ( final int soTimeout ) {
        this.socketProperties.setSoTimeout ( soTimeout );
    }
    public boolean isSSLEnabled() {
        return this.SSLEnabled;
    }
    public void setSSLEnabled ( final boolean SSLEnabled ) {
        this.SSLEnabled = SSLEnabled;
    }
    public void setMinSpareThreads ( final int minSpareThreads ) {
        this.minSpareThreads = minSpareThreads;
        final Executor executor = this.executor;
        if ( this.internalExecutor && executor instanceof ThreadPoolExecutor ) {
            ( ( ThreadPoolExecutor ) executor ).setCorePoolSize ( minSpareThreads );
        }
    }
    public int getMinSpareThreads() {
        return Math.min ( this.getMinSpareThreadsInternal(), this.getMaxThreads() );
    }
    private int getMinSpareThreadsInternal() {
        if ( this.internalExecutor ) {
            return this.minSpareThreads;
        }
        return -1;
    }
    public void setMaxThreads ( final int maxThreads ) {
        this.maxThreads = maxThreads;
        final Executor executor = this.executor;
        if ( this.internalExecutor && executor instanceof ThreadPoolExecutor ) {
            ( ( ThreadPoolExecutor ) executor ).setMaximumPoolSize ( maxThreads );
        }
    }
    public int getMaxThreads() {
        if ( this.internalExecutor ) {
            return this.maxThreads;
        }
        return -1;
    }
    public void setThreadPriority ( final int threadPriority ) {
        this.threadPriority = threadPriority;
    }
    public int getThreadPriority() {
        if ( this.internalExecutor ) {
            return this.threadPriority;
        }
        return -1;
    }
    public int getMaxKeepAliveRequests() {
        return this.maxKeepAliveRequests;
    }
    public void setMaxKeepAliveRequests ( final int maxKeepAliveRequests ) {
        this.maxKeepAliveRequests = maxKeepAliveRequests;
    }
    public int getMaxHeaderCount() {
        return this.maxHeaderCount;
    }
    public void setMaxHeaderCount ( final int maxHeaderCount ) {
        this.maxHeaderCount = maxHeaderCount;
    }
    public void setName ( final String name ) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
    public void setDaemon ( final boolean b ) {
        this.daemon = b;
    }
    public boolean getDaemon() {
        return this.daemon;
    }
    protected abstract boolean getDeferAccept();
    public void addNegotiatedProtocol ( final String negotiableProtocol ) {
        this.negotiableProtocols.add ( negotiableProtocol );
    }
    public boolean hasNegotiableProtocols() {
        return this.negotiableProtocols.size() > 0;
    }
    public void setHandler ( final Handler<S> handler ) {
        this.handler = handler;
    }
    public Handler<S> getHandler() {
        return this.handler;
    }
    public void setAttribute ( final String name, final Object value ) {
        if ( this.getLog().isTraceEnabled() ) {
            this.getLog().trace ( AbstractEndpoint.sm.getString ( "endpoint.setAttribute", name, value ) );
        }
        this.attributes.put ( name, value );
    }
    public Object getAttribute ( final String key ) {
        final Object value = this.attributes.get ( key );
        if ( this.getLog().isTraceEnabled() ) {
            this.getLog().trace ( AbstractEndpoint.sm.getString ( "endpoint.getAttribute", key, value ) );
        }
        return value;
    }
    public boolean setProperty ( final String name, final String value ) {
        this.setAttribute ( name, value );
        final String socketName = "socket.";
        try {
            if ( name.startsWith ( "socket." ) ) {
                return IntrospectionUtils.setProperty ( this.socketProperties, name.substring ( "socket.".length() ), value );
            }
            return IntrospectionUtils.setProperty ( this, name, value, false );
        } catch ( Exception x ) {
            this.getLog().error ( "Unable to set attribute \"" + name + "\" to \"" + value + "\"", x );
            return false;
        }
    }
    public String getProperty ( final String name ) {
        String value = ( String ) this.getAttribute ( name );
        final String socketName = "socket.";
        if ( value == null && name.startsWith ( "socket." ) ) {
            final Object result = IntrospectionUtils.getProperty ( this.socketProperties, name.substring ( "socket.".length() ) );
            if ( result != null ) {
                value = result.toString();
            }
        }
        return value;
    }
    public int getCurrentThreadCount() {
        final Executor executor = this.executor;
        if ( executor == null ) {
            return -2;
        }
        if ( executor instanceof org.apache.tomcat.util.threads.ThreadPoolExecutor ) {
            return ( ( org.apache.tomcat.util.threads.ThreadPoolExecutor ) executor ).getPoolSize();
        }
        if ( executor instanceof ResizableExecutor ) {
            return ( ( ResizableExecutor ) executor ).getPoolSize();
        }
        return -1;
    }
    public int getCurrentThreadsBusy() {
        final Executor executor = this.executor;
        if ( executor == null ) {
            return -2;
        }
        if ( executor instanceof org.apache.tomcat.util.threads.ThreadPoolExecutor ) {
            return ( ( org.apache.tomcat.util.threads.ThreadPoolExecutor ) executor ).getActiveCount();
        }
        if ( executor instanceof ResizableExecutor ) {
            return ( ( ResizableExecutor ) executor ).getActiveCount();
        }
        return -1;
    }
    public boolean isRunning() {
        return this.running;
    }
    public boolean isPaused() {
        return this.paused;
    }
    public void createExecutor() {
        this.internalExecutor = true;
        final TaskQueue taskqueue = new TaskQueue();
        final TaskThreadFactory tf = new TaskThreadFactory ( this.getName() + "-exec-", this.daemon, this.getThreadPriority() );
        this.executor = new org.apache.tomcat.util.threads.ThreadPoolExecutor ( this.getMinSpareThreads(), this.getMaxThreads(), 60L, TimeUnit.SECONDS, taskqueue, tf );
        taskqueue.setParent ( ( org.apache.tomcat.util.threads.ThreadPoolExecutor ) this.executor );
    }
    public void shutdownExecutor() {
        final Executor executor = this.executor;
        if ( executor != null && this.internalExecutor ) {
            this.executor = null;
            if ( executor instanceof org.apache.tomcat.util.threads.ThreadPoolExecutor ) {
                final org.apache.tomcat.util.threads.ThreadPoolExecutor tpe = ( org.apache.tomcat.util.threads.ThreadPoolExecutor ) executor;
                tpe.shutdownNow();
                final long timeout = this.getExecutorTerminationTimeoutMillis();
                if ( timeout > 0L ) {
                    try {
                        tpe.awaitTermination ( timeout, TimeUnit.MILLISECONDS );
                    } catch ( InterruptedException ex ) {}
                    if ( tpe.isTerminating() ) {
                        this.getLog().warn ( AbstractEndpoint.sm.getString ( "endpoint.warn.executorShutdown", this.getName() ) );
                    }
                }
                final TaskQueue queue = ( TaskQueue ) tpe.getQueue();
                queue.setParent ( null );
            }
        }
    }
    protected void unlockAccept() {
        boolean unlockRequired = false;
        for ( final Acceptor acceptor : this.acceptors ) {
            if ( acceptor.getState() == Acceptor.AcceptorState.RUNNING ) {
                unlockRequired = true;
                break;
            }
        }
        if ( !unlockRequired ) {
            return;
        }
        InetSocketAddress saddr = null;
        try {
            if ( this.address == null ) {
                saddr = new InetSocketAddress ( "localhost", this.getLocalPort() );
            } else {
                saddr = new InetSocketAddress ( this.address, this.getLocalPort() );
            }
            try ( final Socket s = new Socket() ) {
                int stmo = 2000;
                int utmo = 2000;
                if ( this.getSocketProperties().getSoTimeout() > stmo ) {
                    stmo = this.getSocketProperties().getSoTimeout();
                }
                if ( this.getSocketProperties().getUnlockTimeout() > utmo ) {
                    utmo = this.getSocketProperties().getUnlockTimeout();
                }
                s.setSoTimeout ( stmo );
                s.setSoLinger ( this.getSocketProperties().getSoLingerOn(), this.getSocketProperties().getSoLingerTime() );
                if ( this.getLog().isDebugEnabled() ) {
                    this.getLog().debug ( "About to unlock socket for:" + saddr );
                }
                s.connect ( saddr, utmo );
                if ( this.getDeferAccept() ) {
                    final OutputStreamWriter sw = new OutputStreamWriter ( s.getOutputStream(), "ISO-8859-1" );
                    sw.write ( "OPTIONS * HTTP/1.0\r\nUser-Agent: Tomcat wakeup connection\r\n\r\n" );
                    sw.flush();
                }
                if ( this.getLog().isDebugEnabled() ) {
                    this.getLog().debug ( "Socket unlock completed for:" + saddr );
                }
                long waitLeft = 1000L;
                for ( final Acceptor acceptor2 : this.acceptors ) {
                    while ( waitLeft > 0L && acceptor2.getState() == Acceptor.AcceptorState.RUNNING ) {
                        Thread.sleep ( 50L );
                        waitLeft -= 50L;
                    }
                }
            }
        } catch ( Exception e ) {
            if ( this.getLog().isDebugEnabled() ) {
                this.getLog().debug ( AbstractEndpoint.sm.getString ( "endpoint.debug.unlock", "" + this.getPort() ), e );
            }
        }
    }
    public boolean processSocket ( final SocketWrapperBase<S> socketWrapper, final SocketEvent event, final boolean dispatch ) {
        try {
            if ( socketWrapper == null ) {
                return false;
            }
            SocketProcessorBase<S> sc = this.processorCache.pop();
            if ( sc == null ) {
                sc = this.createSocketProcessor ( socketWrapper, event );
            } else {
                sc.reset ( socketWrapper, event );
            }
            final Executor executor = this.getExecutor();
            if ( dispatch && executor != null ) {
                executor.execute ( sc );
            } else {
                sc.run();
            }
        } catch ( RejectedExecutionException ree ) {
            this.getLog().warn ( AbstractEndpoint.sm.getString ( "endpoint.executor.fail", socketWrapper ), ree );
            return false;
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            this.getLog().error ( AbstractEndpoint.sm.getString ( "endpoint.process.fail" ), t );
            return false;
        }
        return true;
    }
    protected abstract SocketProcessorBase<S> createSocketProcessor ( final SocketWrapperBase<S> p0, final SocketEvent p1 );
    public abstract void bind() throws Exception;
    public abstract void unbind() throws Exception;
    public abstract void startInternal() throws Exception;
    public abstract void stopInternal() throws Exception;
    public final void init() throws Exception {
        if ( this.bindOnInit ) {
            this.bind();
            this.bindState = BindState.BOUND_ON_INIT;
        }
    }
    public final void start() throws Exception {
        if ( this.bindState == BindState.UNBOUND ) {
            this.bind();
            this.bindState = BindState.BOUND_ON_START;
        }
        this.startInternal();
    }
    protected final void startAcceptorThreads() {
        final int count = this.getAcceptorThreadCount();
        this.acceptors = new Acceptor[count];
        for ( int i = 0; i < count; ++i ) {
            this.acceptors[i] = this.createAcceptor();
            final String threadName = this.getName() + "-Acceptor-" + i;
            this.acceptors[i].setThreadName ( threadName );
            final Thread t = new Thread ( this.acceptors[i], threadName );
            t.setPriority ( this.getAcceptorThreadPriority() );
            t.setDaemon ( this.getDaemon() );
            t.start();
        }
    }
    protected abstract Acceptor createAcceptor();
    public void pause() {
        if ( this.running && !this.paused ) {
            this.paused = true;
            this.unlockAccept();
            this.getHandler().pause();
        }
    }
    public void resume() {
        if ( this.running ) {
            this.paused = false;
        }
    }
    public final void stop() throws Exception {
        this.stopInternal();
        if ( this.bindState == BindState.BOUND_ON_START ) {
            this.unbind();
            this.bindState = BindState.UNBOUND;
        }
    }
    public final void destroy() throws Exception {
        if ( this.bindState == BindState.BOUND_ON_INIT ) {
            this.unbind();
            this.bindState = BindState.UNBOUND;
        }
    }
    protected abstract Log getLog();
    protected LimitLatch initializeConnectionLatch() {
        if ( this.maxConnections == -1 ) {
            return null;
        }
        if ( this.connectionLimitLatch == null ) {
            this.connectionLimitLatch = new LimitLatch ( this.getMaxConnections() );
        }
        return this.connectionLimitLatch;
    }
    protected void releaseConnectionLatch() {
        final LimitLatch latch = this.connectionLimitLatch;
        if ( latch != null ) {
            latch.releaseAll();
        }
        this.connectionLimitLatch = null;
    }
    protected void countUpOrAwaitConnection() throws InterruptedException {
        if ( this.maxConnections == -1 ) {
            return;
        }
        final LimitLatch latch = this.connectionLimitLatch;
        if ( latch != null ) {
            latch.countUpOrAwait();
        }
    }
    protected long countDownConnection() {
        if ( this.maxConnections == -1 ) {
            return -1L;
        }
        final LimitLatch latch = this.connectionLimitLatch;
        if ( latch != null ) {
            final long result = latch.countDown();
            if ( result < 0L ) {
                this.getLog().warn ( AbstractEndpoint.sm.getString ( "endpoint.warn.incorrectConnectionCount" ) );
            }
            return result;
        }
        return -1L;
    }
    protected int handleExceptionWithDelay ( final int currentErrorDelay ) {
        if ( currentErrorDelay > 0 ) {
            try {
                Thread.sleep ( currentErrorDelay );
            } catch ( InterruptedException ex ) {}
        }
        if ( currentErrorDelay == 0 ) {
            return 50;
        }
        if ( currentErrorDelay < 1600 ) {
            return currentErrorDelay * 2;
        }
        return 1600;
    }
    static {
        sm = StringManager.getManager ( AbstractEndpoint.class );
    }
    protected enum BindState {
        UNBOUND,
        BOUND_ON_INIT,
        BOUND_ON_START;
    }
    public abstract static class Acceptor implements Runnable {
        protected volatile AcceptorState state;
        private String threadName;
        public Acceptor() {
            this.state = AcceptorState.NEW;
        }
        public final AcceptorState getState() {
            return this.state;
        }
        protected final void setThreadName ( final String threadName ) {
            this.threadName = threadName;
        }
        protected final String getThreadName() {
            return this.threadName;
        }
        public enum AcceptorState {
            NEW,
            RUNNING,
            PAUSED,
            ENDED;
        }
    }
    public interface Handler<S> {
        SocketState process ( SocketWrapperBase<S> p0, SocketEvent p1 );
        Object getGlobal();
        Set<S> getOpenSockets();
        void release ( SocketWrapperBase<S> p0 );
        void pause();
        void recycle();
        public enum SocketState {
            OPEN,
            CLOSED,
            LONG,
            ASYNC_END,
            SENDFILE,
            UPGRADING,
            UPGRADED;
        }
    }
}
