package org.apache.tomcat.util.net;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.collections.SynchronizedStack;
import org.apache.tomcat.util.net.AbstractEndpoint.Acceptor.AcceptorState;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.threads.LimitLatch;
import org.apache.tomcat.util.threads.ResizableExecutor;
import org.apache.tomcat.util.threads.TaskQueue;
import org.apache.tomcat.util.threads.TaskThreadFactory;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
public abstract class AbstractEndpoint<S> {
    protected static final StringManager sm = StringManager.getManager ( AbstractEndpoint.class );
    public static interface Handler<S> {
        public enum SocketState {
            OPEN, CLOSED, LONG, ASYNC_END, SENDFILE, UPGRADING, UPGRADED
        }
        public SocketState process ( SocketWrapperBase<S> socket,
                                     SocketEvent status );
        public Object getGlobal();
        public Set<S> getOpenSockets();
        public void release ( SocketWrapperBase<S> socketWrapper );
        public void pause();
        public void recycle();
    }
    protected enum BindState {
        UNBOUND, BOUND_ON_INIT, BOUND_ON_START
    }
    public abstract static class Acceptor implements Runnable {
        public enum AcceptorState {
            NEW, RUNNING, PAUSED, ENDED
        }
        protected volatile AcceptorState state = AcceptorState.NEW;
        public final AcceptorState getState() {
            return state;
        }
        private String threadName;
        protected final void setThreadName ( final String threadName ) {
            this.threadName = threadName;
        }
        protected final String getThreadName() {
            return threadName;
        }
    }
    private static final int INITIAL_ERROR_DELAY = 50;
    private static final int MAX_ERROR_DELAY = 1600;
    protected volatile boolean running = false;
    protected volatile boolean paused = false;
    protected volatile boolean internalExecutor = true;
    private volatile LimitLatch connectionLimitLatch = null;
    protected SocketProperties socketProperties = new SocketProperties();
    public SocketProperties getSocketProperties() {
        return socketProperties;
    }
    protected Acceptor[] acceptors;
    protected SynchronizedStack<SocketProcessorBase<S>> processorCache;
    private String defaultSSLHostConfigName = SSLHostConfig.DEFAULT_SSL_HOST_NAME;
    public String getDefaultSSLHostConfigName() {
        return defaultSSLHostConfigName;
    }
    public void setDefaultSSLHostConfigName ( String defaultSSLHostConfigName ) {
        this.defaultSSLHostConfigName = defaultSSLHostConfigName;
    }
    protected ConcurrentMap<String, SSLHostConfig> sslHostConfigs = new ConcurrentHashMap<>();
    public void addSslHostConfig ( SSLHostConfig sslHostConfig ) throws IllegalArgumentException {
        String key = sslHostConfig.getHostName();
        if ( key == null || key.length() == 0 ) {
            throw new IllegalArgumentException ( sm.getString ( "endpoint.noSslHostName" ) );
        }
        sslHostConfig.setConfigType ( getSslConfigType() );
        if ( bindState != BindState.UNBOUND ) {
            try {
                createSSLContext ( sslHostConfig );
            } catch ( Exception e ) {
                throw new IllegalArgumentException ( e );
            }
        }
        SSLHostConfig duplicate = sslHostConfigs.putIfAbsent ( key, sslHostConfig );
        if ( duplicate != null ) {
            releaseSSLContext ( sslHostConfig );
            throw new IllegalArgumentException ( sm.getString ( "endpoint.duplicateSslHostName", key ) );
        }
    }
    public SSLHostConfig[] findSslHostConfigs() {
        return sslHostConfigs.values().toArray ( new SSLHostConfig[0] );
    }
    protected abstract SSLHostConfig.Type getSslConfigType();
    protected abstract void createSSLContext ( SSLHostConfig sslHostConfig ) throws Exception;
    protected abstract void releaseSSLContext ( SSLHostConfig sslHostConfig );
    protected SSLHostConfig getSSLHostConfig ( String sniHostName ) {
        SSLHostConfig result = null;
        if ( sniHostName != null ) {
            result = sslHostConfigs.get ( sniHostName );
            if ( result != null ) {
                return result;
            }
            int indexOfDot = sniHostName.indexOf ( '.' );
            if ( indexOfDot > -1 ) {
                result = sslHostConfigs.get ( "*" + sniHostName.substring ( indexOfDot ) );
            }
        }
        if ( result == null ) {
            result = sslHostConfigs.get ( getDefaultSSLHostConfigName() );
        }
        if ( result == null ) {
            throw new IllegalStateException();
        }
        return result;
    }
    private boolean useSendfile = true;
    public boolean getUseSendfile() {
        return useSendfile;
    }
    public void setUseSendfile ( boolean useSendfile ) {
        this.useSendfile = useSendfile;
    }
    private long executorTerminationTimeoutMillis = 5000;
    public long getExecutorTerminationTimeoutMillis() {
        return executorTerminationTimeoutMillis;
    }
    public void setExecutorTerminationTimeoutMillis (
        long executorTerminationTimeoutMillis ) {
        this.executorTerminationTimeoutMillis = executorTerminationTimeoutMillis;
    }
    protected int acceptorThreadCount = 1;
    public void setAcceptorThreadCount ( int acceptorThreadCount ) {
        this.acceptorThreadCount = acceptorThreadCount;
    }
    public int getAcceptorThreadCount() {
        return acceptorThreadCount;
    }
    protected int acceptorThreadPriority = Thread.NORM_PRIORITY;
    public void setAcceptorThreadPriority ( int acceptorThreadPriority ) {
        this.acceptorThreadPriority = acceptorThreadPriority;
    }
    public int getAcceptorThreadPriority() {
        return acceptorThreadPriority;
    }
    private int maxConnections = 10000;
    public void setMaxConnections ( int maxCon ) {
        this.maxConnections = maxCon;
        LimitLatch latch = this.connectionLimitLatch;
        if ( latch != null ) {
            if ( maxCon == -1 ) {
                releaseConnectionLatch();
            } else {
                latch.setLimit ( maxCon );
            }
        } else if ( maxCon > 0 ) {
            initializeConnectionLatch();
        }
    }
    public int  getMaxConnections() {
        return this.maxConnections;
    }
    public long getConnectionCount() {
        LimitLatch latch = connectionLimitLatch;
        if ( latch != null ) {
            return latch.getCount();
        }
        return -1;
    }
    private Executor executor = null;
    public void setExecutor ( Executor executor ) {
        this.executor = executor;
        this.internalExecutor = ( executor == null );
    }
    public Executor getExecutor() {
        return executor;
    }
    private int port;
    public int getPort() {
        return port;
    }
    public void setPort ( int port ) {
        this.port = port;
    }
    public abstract int getLocalPort();
    private InetAddress address;
    public InetAddress getAddress() {
        return address;
    }
    public void setAddress ( InetAddress address ) {
        this.address = address;
    }
    private int acceptCount = 100;
    public void setAcceptCount ( int acceptCount ) {
        if ( acceptCount > 0 ) {
            this.acceptCount = acceptCount;
        }
    }
    public int getAcceptCount() {
        return acceptCount;
    }
    private boolean bindOnInit = true;
    public boolean getBindOnInit() {
        return bindOnInit;
    }
    public void setBindOnInit ( boolean b ) {
        this.bindOnInit = b;
    }
    private volatile BindState bindState = BindState.UNBOUND;
    private Integer keepAliveTimeout = null;
    public int getKeepAliveTimeout() {
        if ( keepAliveTimeout == null ) {
            return getConnectionTimeout();
        } else {
            return keepAliveTimeout.intValue();
        }
    }
    public void setKeepAliveTimeout ( int keepAliveTimeout ) {
        this.keepAliveTimeout = Integer.valueOf ( keepAliveTimeout );
    }
    public boolean getTcpNoDelay() {
        return socketProperties.getTcpNoDelay();
    }
    public void setTcpNoDelay ( boolean tcpNoDelay ) {
        socketProperties.setTcpNoDelay ( tcpNoDelay );
    }
    public int getConnectionLinger() {
        return socketProperties.getSoLingerTime();
    }
    public void setConnectionLinger ( int connectionLinger ) {
        socketProperties.setSoLingerTime ( connectionLinger );
        socketProperties.setSoLingerOn ( connectionLinger >= 0 );
    }
    public int getConnectionTimeout() {
        return socketProperties.getSoTimeout();
    }
    public void setConnectionTimeout ( int soTimeout ) {
        socketProperties.setSoTimeout ( soTimeout );
    }
    private boolean SSLEnabled = false;
    public boolean isSSLEnabled() {
        return SSLEnabled;
    }
    public void setSSLEnabled ( boolean SSLEnabled ) {
        this.SSLEnabled = SSLEnabled;
    }
    private int minSpareThreads = 10;
    public void setMinSpareThreads ( int minSpareThreads ) {
        this.minSpareThreads = minSpareThreads;
        Executor executor = this.executor;
        if ( internalExecutor && executor instanceof java.util.concurrent.ThreadPoolExecutor ) {
            ( ( java.util.concurrent.ThreadPoolExecutor ) executor ).setCorePoolSize ( minSpareThreads );
        }
    }
    public int getMinSpareThreads() {
        return Math.min ( getMinSpareThreadsInternal(), getMaxThreads() );
    }
    private int getMinSpareThreadsInternal() {
        if ( internalExecutor ) {
            return minSpareThreads;
        } else {
            return -1;
        }
    }
    private int maxThreads = 200;
    public void setMaxThreads ( int maxThreads ) {
        this.maxThreads = maxThreads;
        Executor executor = this.executor;
        if ( internalExecutor && executor instanceof java.util.concurrent.ThreadPoolExecutor ) {
            ( ( java.util.concurrent.ThreadPoolExecutor ) executor ).setMaximumPoolSize ( maxThreads );
        }
    }
    public int getMaxThreads() {
        if ( internalExecutor ) {
            return maxThreads;
        } else {
            return -1;
        }
    }
    protected int threadPriority = Thread.NORM_PRIORITY;
    public void setThreadPriority ( int threadPriority ) {
        this.threadPriority = threadPriority;
    }
    public int getThreadPriority() {
        if ( internalExecutor ) {
            return threadPriority;
        } else {
            return -1;
        }
    }
    private int maxKeepAliveRequests = 100;
    public int getMaxKeepAliveRequests() {
        return maxKeepAliveRequests;
    }
    public void setMaxKeepAliveRequests ( int maxKeepAliveRequests ) {
        this.maxKeepAliveRequests = maxKeepAliveRequests;
    }
    private int maxHeaderCount = 100;
    public int getMaxHeaderCount() {
        return maxHeaderCount;
    }
    public void setMaxHeaderCount ( int maxHeaderCount ) {
        this.maxHeaderCount = maxHeaderCount;
    }
    private String name = "TP";
    public void setName ( String name ) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    private boolean daemon = true;
    public void setDaemon ( boolean b ) {
        daemon = b;
    }
    public boolean getDaemon() {
        return daemon;
    }
    protected abstract boolean getDeferAccept();
    protected final List<String> negotiableProtocols = new ArrayList<>();
    public void addNegotiatedProtocol ( String negotiableProtocol ) {
        negotiableProtocols.add ( negotiableProtocol );
    }
    public boolean hasNegotiableProtocols() {
        return ( negotiableProtocols.size() > 0 );
    }
    private Handler<S> handler = null;
    public void setHandler ( Handler<S> handler ) {
        this.handler = handler;
    }
    public Handler<S> getHandler() {
        return handler;
    }
    protected HashMap<String, Object> attributes = new HashMap<>();
    public void setAttribute ( String name, Object value ) {
        if ( getLog().isTraceEnabled() ) {
            getLog().trace ( sm.getString ( "endpoint.setAttribute", name, value ) );
        }
        attributes.put ( name, value );
    }
    public Object getAttribute ( String key ) {
        Object value = attributes.get ( key );
        if ( getLog().isTraceEnabled() ) {
            getLog().trace ( sm.getString ( "endpoint.getAttribute", key, value ) );
        }
        return value;
    }
    public boolean setProperty ( String name, String value ) {
        setAttribute ( name, value );
        final String socketName = "socket.";
        try {
            if ( name.startsWith ( socketName ) ) {
                return IntrospectionUtils.setProperty ( socketProperties, name.substring ( socketName.length() ), value );
            } else {
                return IntrospectionUtils.setProperty ( this, name, value, false );
            }
        } catch ( Exception x ) {
            getLog().error ( "Unable to set attribute \"" + name + "\" to \"" + value + "\"", x );
            return false;
        }
    }
    public String getProperty ( String name ) {
        String value = ( String ) getAttribute ( name );
        final String socketName = "socket.";
        if ( value == null && name.startsWith ( socketName ) ) {
            Object result = IntrospectionUtils.getProperty ( socketProperties, name.substring ( socketName.length() ) );
            if ( result != null ) {
                value = result.toString();
            }
        }
        return value;
    }
    public int getCurrentThreadCount() {
        Executor executor = this.executor;
        if ( executor != null ) {
            if ( executor instanceof ThreadPoolExecutor ) {
                return ( ( ThreadPoolExecutor ) executor ).getPoolSize();
            } else if ( executor instanceof ResizableExecutor ) {
                return ( ( ResizableExecutor ) executor ).getPoolSize();
            } else {
                return -1;
            }
        } else {
            return -2;
        }
    }
    public int getCurrentThreadsBusy() {
        Executor executor = this.executor;
        if ( executor != null ) {
            if ( executor instanceof ThreadPoolExecutor ) {
                return ( ( ThreadPoolExecutor ) executor ).getActiveCount();
            } else if ( executor instanceof ResizableExecutor ) {
                return ( ( ResizableExecutor ) executor ).getActiveCount();
            } else {
                return -1;
            }
        } else {
            return -2;
        }
    }
    public boolean isRunning() {
        return running;
    }
    public boolean isPaused() {
        return paused;
    }
    public void createExecutor() {
        internalExecutor = true;
        TaskQueue taskqueue = new TaskQueue();
        TaskThreadFactory tf = new TaskThreadFactory ( getName() + "-exec-", daemon, getThreadPriority() );
        executor = new ThreadPoolExecutor ( getMinSpareThreads(), getMaxThreads(), 60, TimeUnit.SECONDS, taskqueue, tf );
        taskqueue.setParent ( ( ThreadPoolExecutor ) executor );
    }
    public void shutdownExecutor() {
        Executor executor = this.executor;
        if ( executor != null && internalExecutor ) {
            this.executor = null;
            if ( executor instanceof ThreadPoolExecutor ) {
                ThreadPoolExecutor tpe = ( ThreadPoolExecutor ) executor;
                tpe.shutdownNow();
                long timeout = getExecutorTerminationTimeoutMillis();
                if ( timeout > 0 ) {
                    try {
                        tpe.awaitTermination ( timeout, TimeUnit.MILLISECONDS );
                    } catch ( InterruptedException e ) {
                    }
                    if ( tpe.isTerminating() ) {
                        getLog().warn ( sm.getString ( "endpoint.warn.executorShutdown", getName() ) );
                    }
                }
                TaskQueue queue = ( TaskQueue ) tpe.getQueue();
                queue.setParent ( null );
            }
        }
    }
    protected void unlockAccept() {
        boolean unlockRequired = false;
        for ( Acceptor acceptor : acceptors ) {
            if ( acceptor.getState() == AcceptorState.RUNNING ) {
                unlockRequired = true;
                break;
            }
        }
        if ( !unlockRequired ) {
            return;
        }
        InetSocketAddress saddr = null;
        try {
            if ( address == null ) {
                saddr = new InetSocketAddress ( "localhost", getLocalPort() );
            } else {
                saddr = new InetSocketAddress ( address, getLocalPort() );
            }
            try ( java.net.Socket s = new java.net.Socket() ) {
                int stmo = 2 * 1000;
                int utmo = 2 * 1000;
                if ( getSocketProperties().getSoTimeout() > stmo ) {
                    stmo = getSocketProperties().getSoTimeout();
                }
                if ( getSocketProperties().getUnlockTimeout() > utmo ) {
                    utmo = getSocketProperties().getUnlockTimeout();
                }
                s.setSoTimeout ( stmo );
                s.setSoLinger ( getSocketProperties().getSoLingerOn(), getSocketProperties().getSoLingerTime() );
                if ( getLog().isDebugEnabled() ) {
                    getLog().debug ( "About to unlock socket for:" + saddr );
                }
                s.connect ( saddr, utmo );
                if ( getDeferAccept() ) {
                    OutputStreamWriter sw;
                    sw = new OutputStreamWriter ( s.getOutputStream(), "ISO-8859-1" );
                    sw.write ( "OPTIONS * HTTP/1.0\r\n" +
                               "User-Agent: Tomcat wakeup connection\r\n\r\n" );
                    sw.flush();
                }
                if ( getLog().isDebugEnabled() ) {
                    getLog().debug ( "Socket unlock completed for:" + saddr );
                }
                long waitLeft = 1000;
                for ( Acceptor acceptor : acceptors ) {
                    while ( waitLeft > 0 &&
                            acceptor.getState() == AcceptorState.RUNNING ) {
                        Thread.sleep ( 50 );
                        waitLeft -= 50;
                    }
                }
            }
        } catch ( Exception e ) {
            if ( getLog().isDebugEnabled() ) {
                getLog().debug ( sm.getString ( "endpoint.debug.unlock", "" + getPort() ), e );
            }
        }
    }
    public boolean processSocket ( SocketWrapperBase<S> socketWrapper,
                                   SocketEvent event, boolean dispatch ) {
        try {
            if ( socketWrapper == null ) {
                return false;
            }
            SocketProcessorBase<S> sc = processorCache.pop();
            if ( sc == null ) {
                sc = createSocketProcessor ( socketWrapper, event );
            } else {
                sc.reset ( socketWrapper, event );
            }
            Executor executor = getExecutor();
            if ( dispatch && executor != null ) {
                executor.execute ( sc );
            } else {
                sc.run();
            }
        } catch ( RejectedExecutionException ree ) {
            getLog().warn ( sm.getString ( "endpoint.executor.fail", socketWrapper ) , ree );
            return false;
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            getLog().error ( sm.getString ( "endpoint.process.fail" ), t );
            return false;
        }
        return true;
    }
    protected abstract SocketProcessorBase<S> createSocketProcessor (
        SocketWrapperBase<S> socketWrapper, SocketEvent event );
    public abstract void bind() throws Exception;
    public abstract void unbind() throws Exception;
    public abstract void startInternal() throws Exception;
    public abstract void stopInternal() throws Exception;
    public final void init() throws Exception {
        if ( bindOnInit ) {
            bind();
            bindState = BindState.BOUND_ON_INIT;
        }
    }
    public final void start() throws Exception {
        if ( bindState == BindState.UNBOUND ) {
            bind();
            bindState = BindState.BOUND_ON_START;
        }
        startInternal();
    }
    protected final void startAcceptorThreads() {
        int count = getAcceptorThreadCount();
        acceptors = new Acceptor[count];
        for ( int i = 0; i < count; i++ ) {
            acceptors[i] = createAcceptor();
            String threadName = getName() + "-Acceptor-" + i;
            acceptors[i].setThreadName ( threadName );
            Thread t = new Thread ( acceptors[i], threadName );
            t.setPriority ( getAcceptorThreadPriority() );
            t.setDaemon ( getDaemon() );
            t.start();
        }
    }
    protected abstract Acceptor createAcceptor();
    public void pause() {
        if ( running && !paused ) {
            paused = true;
            unlockAccept();
            getHandler().pause();
        }
    }
    public void resume() {
        if ( running ) {
            paused = false;
        }
    }
    public final void stop() throws Exception {
        stopInternal();
        if ( bindState == BindState.BOUND_ON_START ) {
            unbind();
            bindState = BindState.UNBOUND;
        }
    }
    public final void destroy() throws Exception {
        if ( bindState == BindState.BOUND_ON_INIT ) {
            unbind();
            bindState = BindState.UNBOUND;
        }
    }
    protected abstract Log getLog();
    protected LimitLatch initializeConnectionLatch() {
        if ( maxConnections == -1 ) {
            return null;
        }
        if ( connectionLimitLatch == null ) {
            connectionLimitLatch = new LimitLatch ( getMaxConnections() );
        }
        return connectionLimitLatch;
    }
    protected void releaseConnectionLatch() {
        LimitLatch latch = connectionLimitLatch;
        if ( latch != null ) {
            latch.releaseAll();
        }
        connectionLimitLatch = null;
    }
    protected void countUpOrAwaitConnection() throws InterruptedException {
        if ( maxConnections == -1 ) {
            return;
        }
        LimitLatch latch = connectionLimitLatch;
        if ( latch != null ) {
            latch.countUpOrAwait();
        }
    }
    protected long countDownConnection() {
        if ( maxConnections == -1 ) {
            return -1;
        }
        LimitLatch latch = connectionLimitLatch;
        if ( latch != null ) {
            long result = latch.countDown();
            if ( result < 0 ) {
                getLog().warn ( sm.getString ( "endpoint.warn.incorrectConnectionCount" ) );
            }
            return result;
        } else {
            return -1;
        }
    }
    protected int handleExceptionWithDelay ( int currentErrorDelay ) {
        if ( currentErrorDelay > 0 ) {
            try {
                Thread.sleep ( currentErrorDelay );
            } catch ( InterruptedException e ) {
            }
        }
        if ( currentErrorDelay == 0 ) {
            return INITIAL_ERROR_DELAY;
        } else if ( currentErrorDelay < MAX_ERROR_DELAY ) {
            return currentErrorDelay * 2;
        } else {
            return MAX_ERROR_DELAY;
        }
    }
}
