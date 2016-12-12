package org.apache.coyote;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistration;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.WebConnection;
import org.apache.juli.logging.Log;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.collections.SynchronizedStack;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.AbstractEndpoint.Handler;
import org.apache.tomcat.util.net.SocketEvent;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.tomcat.util.res.StringManager;
public abstract class AbstractProtocol<S> implements ProtocolHandler,
    MBeanRegistration {
    private static final StringManager sm = StringManager.getManager ( AbstractProtocol.class );
    private static final AtomicInteger nameCounter = new AtomicInteger ( 0 );
    protected ObjectName rgOname = null;
    protected ObjectName tpOname = null;
    private int nameIndex = 0;
    private final AbstractEndpoint<S> endpoint;
    private Handler<S> handler;
    private final Set<Processor> waitingProcessors =
        Collections.newSetFromMap ( new ConcurrentHashMap<Processor, Boolean>() );
    private AsyncTimeout asyncTimeout = null;
    public AbstractProtocol ( AbstractEndpoint<S> endpoint ) {
        this.endpoint = endpoint;
        setConnectionLinger ( Constants.DEFAULT_CONNECTION_LINGER );
        setTcpNoDelay ( Constants.DEFAULT_TCP_NO_DELAY );
    }
    public boolean setProperty ( String name, String value ) {
        return endpoint.setProperty ( name, value );
    }
    public String getProperty ( String name ) {
        return endpoint.getProperty ( name );
    }
    protected Adapter adapter;
    @Override
    public void setAdapter ( Adapter adapter ) {
        this.adapter = adapter;
    }
    @Override
    public Adapter getAdapter() {
        return adapter;
    }
    protected int processorCache = 200;
    public int getProcessorCache() {
        return this.processorCache;
    }
    public void setProcessorCache ( int processorCache ) {
        this.processorCache = processorCache;
    }
    protected String clientCertProvider = null;
    public String getClientCertProvider() {
        return clientCertProvider;
    }
    public void setClientCertProvider ( String s ) {
        this.clientCertProvider = s;
    }
    @Override
    public boolean isAprRequired() {
        return false;
    }
    @Override
    public boolean isSendfileSupported() {
        return endpoint.getUseSendfile();
    }
    public AsyncTimeout getAsyncTimeout() {
        return asyncTimeout;
    }
    @Override
    public Executor getExecutor() {
        return endpoint.getExecutor();
    }
    public void setExecutor ( Executor executor ) {
        endpoint.setExecutor ( executor );
    }
    public int getMaxThreads() {
        return endpoint.getMaxThreads();
    }
    public void setMaxThreads ( int maxThreads ) {
        endpoint.setMaxThreads ( maxThreads );
    }
    public int getMaxConnections() {
        return endpoint.getMaxConnections();
    }
    public void setMaxConnections ( int maxConnections ) {
        endpoint.setMaxConnections ( maxConnections );
    }
    public int getMinSpareThreads() {
        return endpoint.getMinSpareThreads();
    }
    public void setMinSpareThreads ( int minSpareThreads ) {
        endpoint.setMinSpareThreads ( minSpareThreads );
    }
    public int getThreadPriority() {
        return endpoint.getThreadPriority();
    }
    public void setThreadPriority ( int threadPriority ) {
        endpoint.setThreadPriority ( threadPriority );
    }
    public int getAcceptCount() {
        return endpoint.getAcceptCount();
    }
    public void setAcceptCount ( int acceptCount ) {
        endpoint.setAcceptCount ( acceptCount );
    }
    public boolean getTcpNoDelay() {
        return endpoint.getTcpNoDelay();
    }
    public void setTcpNoDelay ( boolean tcpNoDelay ) {
        endpoint.setTcpNoDelay ( tcpNoDelay );
    }
    public int getConnectionLinger() {
        return endpoint.getConnectionLinger();
    }
    public void setConnectionLinger ( int connectionLinger ) {
        endpoint.setConnectionLinger ( connectionLinger );
    }
    public int getKeepAliveTimeout() {
        return endpoint.getKeepAliveTimeout();
    }
    public void setKeepAliveTimeout ( int keepAliveTimeout ) {
        endpoint.setKeepAliveTimeout ( keepAliveTimeout );
    }
    public InetAddress getAddress() {
        return endpoint.getAddress();
    }
    public void setAddress ( InetAddress ia ) {
        endpoint.setAddress ( ia );
    }
    public int getPort() {
        return endpoint.getPort();
    }
    public void setPort ( int port ) {
        endpoint.setPort ( port );
    }
    public int getLocalPort() {
        return endpoint.getLocalPort();
    }
    public int getConnectionTimeout() {
        return endpoint.getConnectionTimeout();
    }
    public void setConnectionTimeout ( int timeout ) {
        endpoint.setConnectionTimeout ( timeout );
    }
    public int getMaxHeaderCount() {
        return endpoint.getMaxHeaderCount();
    }
    public void setMaxHeaderCount ( int maxHeaderCount ) {
        endpoint.setMaxHeaderCount ( maxHeaderCount );
    }
    public long getConnectionCount() {
        return endpoint.getConnectionCount();
    }
    public void setAcceptorThreadCount ( int threadCount ) {
        endpoint.setAcceptorThreadCount ( threadCount );
    }
    public int getAcceptorThreadCount() {
        return endpoint.getAcceptorThreadCount();
    }
    public void setAcceptorThreadPriority ( int threadPriority ) {
        endpoint.setAcceptorThreadPriority ( threadPriority );
    }
    public int getAcceptorThreadPriority() {
        return endpoint.getAcceptorThreadPriority();
    }
    public synchronized int getNameIndex() {
        if ( nameIndex == 0 ) {
            nameIndex = nameCounter.incrementAndGet();
        }
        return nameIndex;
    }
    public String getName() {
        return ObjectName.quote ( getNameInternal() );
    }
    private String getNameInternal() {
        StringBuilder name = new StringBuilder ( getNamePrefix() );
        name.append ( '-' );
        if ( getAddress() != null ) {
            name.append ( getAddress().getHostAddress() );
            name.append ( '-' );
        }
        int port = getPort();
        if ( port == 0 ) {
            name.append ( "auto-" );
            name.append ( getNameIndex() );
            port = getLocalPort();
            if ( port != -1 ) {
                name.append ( '-' );
                name.append ( port );
            }
        } else {
            name.append ( port );
        }
        return name.toString();
    }
    public void addWaitingProcessor ( Processor processor ) {
        waitingProcessors.add ( processor );
    }
    public void removeWaitingProcessor ( Processor processor ) {
        waitingProcessors.remove ( processor );
    }
    protected AbstractEndpoint<S> getEndpoint() {
        return endpoint;
    }
    protected Handler<S> getHandler() {
        return handler;
    }
    protected void setHandler ( Handler<S> handler ) {
        this.handler = handler;
    }
    protected abstract Log getLog();
    protected abstract String getNamePrefix();
    protected abstract String getProtocolName();
    protected abstract UpgradeProtocol getNegotiatedProtocol ( String name );
    protected abstract UpgradeProtocol getUpgradeProtocol ( String name );
    protected abstract Processor createProcessor();
    protected abstract Processor createUpgradeProcessor (
        SocketWrapperBase<?> socket,
        UpgradeToken upgradeToken );
    protected String domain;
    protected ObjectName oname;
    protected MBeanServer mserver;
    public ObjectName getObjectName() {
        return oname;
    }
    public String getDomain() {
        return domain;
    }
    @Override
    public ObjectName preRegister ( MBeanServer server, ObjectName name )
    throws Exception {
        oname = name;
        mserver = server;
        domain = name.getDomain();
        return name;
    }
    @Override
    public void postRegister ( Boolean registrationDone ) {
    }
    @Override
    public void preDeregister() throws Exception {
    }
    @Override
    public void postDeregister() {
    }
    private ObjectName createObjectName() throws MalformedObjectNameException {
        domain = getAdapter().getDomain();
        if ( domain == null ) {
            return null;
        }
        StringBuilder name = new StringBuilder ( getDomain() );
        name.append ( ":type=ProtocolHandler,port=" );
        int port = getPort();
        if ( port > 0 ) {
            name.append ( getPort() );
        } else {
            name.append ( "auto-" );
            name.append ( getNameIndex() );
        }
        InetAddress address = getAddress();
        if ( address != null ) {
            name.append ( ",address=" );
            name.append ( ObjectName.quote ( address.getHostAddress() ) );
        }
        return new ObjectName ( name.toString() );
    }
    @Override
    public void init() throws Exception {
        if ( getLog().isInfoEnabled() ) {
            getLog().info ( sm.getString ( "abstractProtocolHandler.init", getName() ) );
        }
        if ( oname == null ) {
            oname = createObjectName();
            if ( oname != null ) {
                Registry.getRegistry ( null, null ).registerComponent ( this, oname, null );
            }
        }
        if ( this.domain != null ) {
            try {
                tpOname = new ObjectName ( domain + ":" + "type=ThreadPool,name=" + getName() );
                Registry.getRegistry ( null, null ).registerComponent ( endpoint, tpOname, null );
            } catch ( Exception e ) {
                getLog().error ( sm.getString ( "abstractProtocolHandler.mbeanRegistrationFailed",
                                                tpOname, getName() ), e );
            }
            rgOname = new ObjectName ( domain + ":type=GlobalRequestProcessor,name=" + getName() );
            Registry.getRegistry ( null, null ).registerComponent (
                getHandler().getGlobal(), rgOname, null );
        }
        String endpointName = getName();
        endpoint.setName ( endpointName.substring ( 1, endpointName.length() - 1 ) );
        endpoint.init();
    }
    @Override
    public void start() throws Exception {
        if ( getLog().isInfoEnabled() ) {
            getLog().info ( sm.getString ( "abstractProtocolHandler.start", getNameInternal() ) );
        }
        endpoint.start();
        asyncTimeout = new AsyncTimeout();
        Thread timeoutThread = new Thread ( asyncTimeout, getNameInternal() + "-AsyncTimeout" );
        int priority = endpoint.getThreadPriority();
        if ( priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY ) {
            priority = Thread.NORM_PRIORITY;
        }
        timeoutThread.setPriority ( priority );
        timeoutThread.setDaemon ( true );
        timeoutThread.start();
    }
    @Override
    public void pause() throws Exception {
        if ( getLog().isInfoEnabled() ) {
            getLog().info ( sm.getString ( "abstractProtocolHandler.pause", getName() ) );
        }
        endpoint.pause();
    }
    @Override
    public void resume() throws Exception {
        if ( getLog().isInfoEnabled() ) {
            getLog().info ( sm.getString ( "abstractProtocolHandler.resume", getName() ) );
        }
        endpoint.resume();
    }
    @Override
    public void stop() throws Exception {
        if ( getLog().isInfoEnabled() ) {
            getLog().info ( sm.getString ( "abstractProtocolHandler.stop", getName() ) );
        }
        if ( asyncTimeout != null ) {
            asyncTimeout.stop();
        }
        endpoint.stop();
    }
    @Override
    public void destroy() throws Exception {
        if ( getLog().isInfoEnabled() ) {
            getLog().info ( sm.getString ( "abstractProtocolHandler.destroy", getName() ) );
        }
        try {
            endpoint.destroy();
        } finally {
            if ( oname != null ) {
                if ( mserver == null ) {
                    Registry.getRegistry ( null, null ).unregisterComponent ( oname );
                } else {
                    try {
                        mserver.unregisterMBean ( oname );
                    } catch ( MBeanRegistrationException | InstanceNotFoundException e ) {
                        getLog().info ( sm.getString ( "abstractProtocol.mbeanDeregistrationFailed",
                                                       oname, mserver ) );
                    }
                }
            }
            if ( tpOname != null ) {
                Registry.getRegistry ( null, null ).unregisterComponent ( tpOname );
            }
            if ( rgOname != null ) {
                Registry.getRegistry ( null, null ).unregisterComponent ( rgOname );
            }
        }
    }
    protected static class ConnectionHandler<S> implements AbstractEndpoint.Handler<S> {
        private final AbstractProtocol<S> proto;
        private final RequestGroupInfo global = new RequestGroupInfo();
        private final AtomicLong registerCount = new AtomicLong ( 0 );
        private final Map<S, Processor> connections = new ConcurrentHashMap<>();
        private final RecycledProcessors recycledProcessors = new RecycledProcessors ( this );
        public ConnectionHandler ( AbstractProtocol<S> proto ) {
            this.proto = proto;
        }
        protected AbstractProtocol<S> getProtocol() {
            return proto;
        }
        protected Log getLog() {
            return getProtocol().getLog();
        }
        @Override
        public Object getGlobal() {
            return global;
        }
        @Override
        public void recycle() {
            recycledProcessors.clear();
        }
        @Override
        public SocketState process ( SocketWrapperBase<S> wrapper, SocketEvent status ) {
            if ( getLog().isDebugEnabled() ) {
                getLog().debug ( sm.getString ( "abstractConnectionHandler.process",
                                                wrapper.getSocket(), status ) );
            }
            if ( wrapper == null ) {
                return SocketState.CLOSED;
            }
            S socket = wrapper.getSocket();
            Processor processor = connections.get ( socket );
            if ( ( status == SocketEvent.DISCONNECT || status == SocketEvent.ERROR )
                    && processor == null ) {
                return SocketState.CLOSED;
            }
            ContainerThreadMarker.set();
            try {
                if ( processor == null ) {
                    String negotiatedProtocol = wrapper.getNegotiatedProtocol();
                    if ( negotiatedProtocol != null ) {
                        UpgradeProtocol upgradeProtocol =
                            getProtocol().getNegotiatedProtocol ( negotiatedProtocol );
                        if ( upgradeProtocol != null ) {
                            processor = upgradeProtocol.getProcessor (
                                            wrapper, getProtocol().getAdapter() );
                        } else if ( negotiatedProtocol.equals ( "http/1.1" ) ) {
                        } else {
                            if ( getLog().isDebugEnabled() ) {
                                getLog().debug ( sm.getString (
                                                     "abstractConnectionHandler.negotiatedProcessor.fail",
                                                     negotiatedProtocol ) );
                            }
                            return SocketState.CLOSED;
                        }
                    }
                }
                if ( processor == null ) {
                    processor = recycledProcessors.pop();
                }
                if ( processor == null ) {
                    processor = getProtocol().createProcessor();
                    register ( processor );
                }
                processor.setSslSupport (
                    wrapper.getSslSupport ( getProtocol().getClientCertProvider() ) );
                connections.put ( socket, processor );
                getProtocol().removeWaitingProcessor ( processor );
                SocketState state = SocketState.CLOSED;
                do {
                    state = processor.process ( wrapper, status );
                    if ( state == SocketState.UPGRADING ) {
                        UpgradeToken upgradeToken = processor.getUpgradeToken();
                        ByteBuffer leftOverInput = processor.getLeftoverInput();
                        if ( upgradeToken == null ) {
                            UpgradeProtocol upgradeProtocol = getProtocol().getUpgradeProtocol ( "h2c" );
                            if ( upgradeProtocol != null ) {
                                processor = upgradeProtocol.getProcessor (
                                                wrapper, getProtocol().getAdapter() );
                                wrapper.unRead ( leftOverInput );
                                connections.put ( socket, processor );
                            } else {
                                if ( getLog().isDebugEnabled() ) {
                                    getLog().debug ( sm.getString (
                                                         "abstractConnectionHandler.negotiatedProcessor.fail",
                                                         "h2c" ) );
                                }
                                return SocketState.CLOSED;
                            }
                        } else {
                            HttpUpgradeHandler httpUpgradeHandler = upgradeToken.getHttpUpgradeHandler();
                            release ( processor );
                            processor = getProtocol().createUpgradeProcessor ( wrapper, upgradeToken );
                            wrapper.unRead ( leftOverInput );
                            wrapper.setUpgraded ( true );
                            connections.put ( socket, processor );
                            if ( upgradeToken.getInstanceManager() == null ) {
                                httpUpgradeHandler.init ( ( WebConnection ) processor );
                            } else {
                                ClassLoader oldCL = upgradeToken.getContextBind().bind ( false, null );
                                try {
                                    httpUpgradeHandler.init ( ( WebConnection ) processor );
                                } finally {
                                    upgradeToken.getContextBind().unbind ( false, oldCL );
                                }
                            }
                        }
                    }
                } while ( state == SocketState.UPGRADING );
                if ( state == SocketState.LONG ) {
                    longPoll ( wrapper, processor );
                    if ( processor.isAsync() ) {
                        getProtocol().addWaitingProcessor ( processor );
                    }
                } else if ( state == SocketState.OPEN ) {
                    connections.remove ( socket );
                    release ( processor );
                    wrapper.registerReadInterest();
                } else if ( state == SocketState.SENDFILE ) {
                    connections.remove ( socket );
                    release ( processor );
                } else if ( state == SocketState.UPGRADED ) {
                    if ( status != SocketEvent.OPEN_WRITE ) {
                        longPoll ( wrapper, processor );
                    }
                } else {
                    connections.remove ( socket );
                    if ( processor.isUpgrade() ) {
                        UpgradeToken upgradeToken = processor.getUpgradeToken();
                        HttpUpgradeHandler httpUpgradeHandler = upgradeToken.getHttpUpgradeHandler();
                        InstanceManager instanceManager = upgradeToken.getInstanceManager();
                        if ( instanceManager == null ) {
                            httpUpgradeHandler.destroy();
                        } else {
                            ClassLoader oldCL = upgradeToken.getContextBind().bind ( false, null );
                            try {
                                httpUpgradeHandler.destroy();
                            } finally {
                                try {
                                    instanceManager.destroyInstance ( httpUpgradeHandler );
                                } catch ( Throwable e ) {
                                    ExceptionUtils.handleThrowable ( e );
                                    getLog().error ( sm.getString ( "abstractConnectionHandler.error" ), e );
                                }
                                upgradeToken.getContextBind().unbind ( false, oldCL );
                            }
                        }
                    } else {
                        release ( processor );
                    }
                }
                return state;
            } catch ( java.net.SocketException e ) {
                getLog().debug ( sm.getString (
                                     "abstractConnectionHandler.socketexception.debug" ), e );
            } catch ( java.io.IOException e ) {
                getLog().debug ( sm.getString (
                                     "abstractConnectionHandler.ioexception.debug" ), e );
            } catch ( ProtocolException e ) {
                getLog().debug ( sm.getString (
                                     "abstractConnectionHandler.protocolexception.debug" ), e );
            } catch ( Throwable e ) {
                ExceptionUtils.handleThrowable ( e );
                getLog().error ( sm.getString ( "abstractConnectionHandler.error" ), e );
            } finally {
                ContainerThreadMarker.clear();
            }
            connections.remove ( socket );
            release ( processor );
            return SocketState.CLOSED;
        }
        protected void longPoll ( SocketWrapperBase<?> socket, Processor processor ) {
            if ( !processor.isAsync() ) {
                socket.registerReadInterest();
            }
        }
        @Override
        public Set<S> getOpenSockets() {
            return connections.keySet();
        }
        private void release ( Processor processor ) {
            if ( processor != null ) {
                processor.recycle();
                if ( !processor.isUpgrade() ) {
                    recycledProcessors.push ( processor );
                }
            }
        }
        @Override
        public void release ( SocketWrapperBase<S> socketWrapper ) {
            S socket = socketWrapper.getSocket();
            Processor processor = connections.remove ( socket );
            release ( processor );
        }
        protected void register ( Processor processor ) {
            if ( getProtocol().getDomain() != null ) {
                synchronized ( this ) {
                    try {
                        long count = registerCount.incrementAndGet();
                        RequestInfo rp =
                            processor.getRequest().getRequestProcessor();
                        rp.setGlobalProcessor ( global );
                        ObjectName rpName = new ObjectName (
                            getProtocol().getDomain() +
                            ":type=RequestProcessor,worker="
                            + getProtocol().getName() +
                            ",name=" + getProtocol().getProtocolName() +
                            "Request" + count );
                        if ( getLog().isDebugEnabled() ) {
                            getLog().debug ( "Register " + rpName );
                        }
                        Registry.getRegistry ( null, null ).registerComponent ( rp,
                                rpName, null );
                        rp.setRpName ( rpName );
                    } catch ( Exception e ) {
                        getLog().warn ( "Error registering request" );
                    }
                }
            }
        }
        protected void unregister ( Processor processor ) {
            if ( getProtocol().getDomain() != null ) {
                synchronized ( this ) {
                    try {
                        Request r = processor.getRequest();
                        if ( r == null ) {
                            return;
                        }
                        RequestInfo rp = r.getRequestProcessor();
                        rp.setGlobalProcessor ( null );
                        ObjectName rpName = rp.getRpName();
                        if ( getLog().isDebugEnabled() ) {
                            getLog().debug ( "Unregister " + rpName );
                        }
                        Registry.getRegistry ( null, null ).unregisterComponent (
                            rpName );
                        rp.setRpName ( null );
                    } catch ( Exception e ) {
                        getLog().warn ( "Error unregistering request", e );
                    }
                }
            }
        }
        @Override
        public final void pause() {
            for ( Processor processor : connections.values() ) {
                processor.pause();
            }
        }
    }
    protected static class RecycledProcessors extends SynchronizedStack<Processor> {
        private final transient ConnectionHandler<?> handler;
        protected final AtomicInteger size = new AtomicInteger ( 0 );
        public RecycledProcessors ( ConnectionHandler<?> handler ) {
            this.handler = handler;
        }
        @SuppressWarnings ( "sync-override" )
        @Override
        public boolean push ( Processor processor ) {
            int cacheSize = handler.getProtocol().getProcessorCache();
            boolean offer = cacheSize == -1 ? true : size.get() < cacheSize;
            boolean result = false;
            if ( offer ) {
                result = super.push ( processor );
                if ( result ) {
                    size.incrementAndGet();
                }
            }
            if ( !result ) {
                handler.unregister ( processor );
            }
            return result;
        }
        @SuppressWarnings ( "sync-override" )
        @Override
        public Processor pop() {
            Processor result = super.pop();
            if ( result != null ) {
                size.decrementAndGet();
            }
            return result;
        }
        @Override
        public synchronized void clear() {
            Processor next = pop();
            while ( next != null ) {
                handler.unregister ( next );
                next = pop();
            }
            super.clear();
            size.set ( 0 );
        }
    }
    protected class AsyncTimeout implements Runnable {
        private volatile boolean asyncTimeoutRunning = true;
        @Override
        public void run() {
            while ( asyncTimeoutRunning ) {
                try {
                    Thread.sleep ( 1000 );
                } catch ( InterruptedException e ) {
                }
                long now = System.currentTimeMillis();
                for ( Processor processor : waitingProcessors ) {
                    processor.timeoutAsync ( now );
                }
                while ( endpoint.isPaused() && asyncTimeoutRunning ) {
                    try {
                        Thread.sleep ( 1000 );
                    } catch ( InterruptedException e ) {
                    }
                }
            }
        }
        protected void stop() {
            asyncTimeoutRunning = false;
            for ( Processor processor : waitingProcessors ) {
                processor.timeoutAsync ( -1 );
            }
        }
    }
}
