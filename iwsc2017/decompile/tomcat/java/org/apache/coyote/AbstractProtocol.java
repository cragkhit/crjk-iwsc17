package org.apache.coyote;
import org.apache.tomcat.util.collections.SynchronizedStack;
import java.util.Iterator;
import org.apache.tomcat.InstanceManager;
import javax.servlet.http.HttpUpgradeHandler;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.net.SocketException;
import org.apache.tomcat.util.ExceptionUtils;
import javax.servlet.http.WebConnection;
import org.apache.tomcat.util.net.AbstractEndpoint.Handler;
import org.apache.tomcat.util.net.SocketEvent;
import java.util.concurrent.atomic.AtomicLong;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import org.apache.tomcat.util.modeler.Registry;
import javax.management.MalformedObjectNameException;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.juli.logging.Log;
import java.net.InetAddress;
import java.util.concurrent.Executor;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import javax.management.MBeanServer;
import java.util.Set;
import org.apache.tomcat.util.net.AbstractEndpoint;
import javax.management.ObjectName;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.tomcat.util.res.StringManager;
import javax.management.MBeanRegistration;
public abstract class AbstractProtocol<S> implements ProtocolHandler, MBeanRegistration {
    private static final StringManager sm;
    private static final AtomicInteger nameCounter;
    protected ObjectName rgOname;
    protected ObjectName tpOname;
    private int nameIndex;
    private final AbstractEndpoint<S> endpoint;
    private AbstractEndpoint.Handler<S> handler;
    private final Set<Processor> waitingProcessors;
    private AsyncTimeout asyncTimeout;
    protected Adapter adapter;
    protected int processorCache;
    protected String clientCertProvider;
    protected String domain;
    protected ObjectName oname;
    protected MBeanServer mserver;
    public AbstractProtocol ( final AbstractEndpoint<S> endpoint ) {
        this.rgOname = null;
        this.tpOname = null;
        this.nameIndex = 0;
        this.waitingProcessors = Collections.newSetFromMap ( new ConcurrentHashMap<Processor, Boolean>() );
        this.asyncTimeout = null;
        this.processorCache = 200;
        this.clientCertProvider = null;
        this.endpoint = endpoint;
        this.setConnectionLinger ( -1 );
        this.setTcpNoDelay ( true );
    }
    public boolean setProperty ( final String name, final String value ) {
        return this.endpoint.setProperty ( name, value );
    }
    public String getProperty ( final String name ) {
        return this.endpoint.getProperty ( name );
    }
    @Override
    public void setAdapter ( final Adapter adapter ) {
        this.adapter = adapter;
    }
    @Override
    public Adapter getAdapter() {
        return this.adapter;
    }
    public int getProcessorCache() {
        return this.processorCache;
    }
    public void setProcessorCache ( final int processorCache ) {
        this.processorCache = processorCache;
    }
    public String getClientCertProvider() {
        return this.clientCertProvider;
    }
    public void setClientCertProvider ( final String s ) {
        this.clientCertProvider = s;
    }
    @Override
    public boolean isAprRequired() {
        return false;
    }
    @Override
    public boolean isSendfileSupported() {
        return this.endpoint.getUseSendfile();
    }
    public AsyncTimeout getAsyncTimeout() {
        return this.asyncTimeout;
    }
    @Override
    public Executor getExecutor() {
        return this.endpoint.getExecutor();
    }
    public void setExecutor ( final Executor executor ) {
        this.endpoint.setExecutor ( executor );
    }
    public int getMaxThreads() {
        return this.endpoint.getMaxThreads();
    }
    public void setMaxThreads ( final int maxThreads ) {
        this.endpoint.setMaxThreads ( maxThreads );
    }
    public int getMaxConnections() {
        return this.endpoint.getMaxConnections();
    }
    public void setMaxConnections ( final int maxConnections ) {
        this.endpoint.setMaxConnections ( maxConnections );
    }
    public int getMinSpareThreads() {
        return this.endpoint.getMinSpareThreads();
    }
    public void setMinSpareThreads ( final int minSpareThreads ) {
        this.endpoint.setMinSpareThreads ( minSpareThreads );
    }
    public int getThreadPriority() {
        return this.endpoint.getThreadPriority();
    }
    public void setThreadPriority ( final int threadPriority ) {
        this.endpoint.setThreadPriority ( threadPriority );
    }
    public int getAcceptCount() {
        return this.endpoint.getAcceptCount();
    }
    public void setAcceptCount ( final int acceptCount ) {
        this.endpoint.setAcceptCount ( acceptCount );
    }
    public boolean getTcpNoDelay() {
        return this.endpoint.getTcpNoDelay();
    }
    public void setTcpNoDelay ( final boolean tcpNoDelay ) {
        this.endpoint.setTcpNoDelay ( tcpNoDelay );
    }
    public int getConnectionLinger() {
        return this.endpoint.getConnectionLinger();
    }
    public void setConnectionLinger ( final int connectionLinger ) {
        this.endpoint.setConnectionLinger ( connectionLinger );
    }
    public int getKeepAliveTimeout() {
        return this.endpoint.getKeepAliveTimeout();
    }
    public void setKeepAliveTimeout ( final int keepAliveTimeout ) {
        this.endpoint.setKeepAliveTimeout ( keepAliveTimeout );
    }
    public InetAddress getAddress() {
        return this.endpoint.getAddress();
    }
    public void setAddress ( final InetAddress ia ) {
        this.endpoint.setAddress ( ia );
    }
    public int getPort() {
        return this.endpoint.getPort();
    }
    public void setPort ( final int port ) {
        this.endpoint.setPort ( port );
    }
    public int getLocalPort() {
        return this.endpoint.getLocalPort();
    }
    public int getConnectionTimeout() {
        return this.endpoint.getConnectionTimeout();
    }
    public void setConnectionTimeout ( final int timeout ) {
        this.endpoint.setConnectionTimeout ( timeout );
    }
    public int getMaxHeaderCount() {
        return this.endpoint.getMaxHeaderCount();
    }
    public void setMaxHeaderCount ( final int maxHeaderCount ) {
        this.endpoint.setMaxHeaderCount ( maxHeaderCount );
    }
    public long getConnectionCount() {
        return this.endpoint.getConnectionCount();
    }
    public void setAcceptorThreadCount ( final int threadCount ) {
        this.endpoint.setAcceptorThreadCount ( threadCount );
    }
    public int getAcceptorThreadCount() {
        return this.endpoint.getAcceptorThreadCount();
    }
    public void setAcceptorThreadPriority ( final int threadPriority ) {
        this.endpoint.setAcceptorThreadPriority ( threadPriority );
    }
    public int getAcceptorThreadPriority() {
        return this.endpoint.getAcceptorThreadPriority();
    }
    public synchronized int getNameIndex() {
        if ( this.nameIndex == 0 ) {
            this.nameIndex = AbstractProtocol.nameCounter.incrementAndGet();
        }
        return this.nameIndex;
    }
    public String getName() {
        return ObjectName.quote ( this.getNameInternal() );
    }
    private String getNameInternal() {
        final StringBuilder name = new StringBuilder ( this.getNamePrefix() );
        name.append ( '-' );
        if ( this.getAddress() != null ) {
            name.append ( this.getAddress().getHostAddress() );
            name.append ( '-' );
        }
        int port = this.getPort();
        if ( port == 0 ) {
            name.append ( "auto-" );
            name.append ( this.getNameIndex() );
            port = this.getLocalPort();
            if ( port != -1 ) {
                name.append ( '-' );
                name.append ( port );
            }
        } else {
            name.append ( port );
        }
        return name.toString();
    }
    public void addWaitingProcessor ( final Processor processor ) {
        this.waitingProcessors.add ( processor );
    }
    public void removeWaitingProcessor ( final Processor processor ) {
        this.waitingProcessors.remove ( processor );
    }
    protected AbstractEndpoint<S> getEndpoint() {
        return this.endpoint;
    }
    protected AbstractEndpoint.Handler<S> getHandler() {
        return this.handler;
    }
    protected void setHandler ( final AbstractEndpoint.Handler<S> handler ) {
        this.handler = handler;
    }
    protected abstract Log getLog();
    protected abstract String getNamePrefix();
    protected abstract String getProtocolName();
    protected abstract UpgradeProtocol getNegotiatedProtocol ( final String p0 );
    protected abstract UpgradeProtocol getUpgradeProtocol ( final String p0 );
    protected abstract Processor createProcessor();
    protected abstract Processor createUpgradeProcessor ( final SocketWrapperBase<?> p0, final UpgradeToken p1 );
    public ObjectName getObjectName() {
        return this.oname;
    }
    public String getDomain() {
        return this.domain;
    }
    @Override
    public ObjectName preRegister ( final MBeanServer server, final ObjectName name ) throws Exception {
        this.oname = name;
        this.mserver = server;
        this.domain = name.getDomain();
        return name;
    }
    @Override
    public void postRegister ( final Boolean registrationDone ) {
    }
    @Override
    public void preDeregister() throws Exception {
    }
    @Override
    public void postDeregister() {
    }
    private ObjectName createObjectName() throws MalformedObjectNameException {
        this.domain = this.getAdapter().getDomain();
        if ( this.domain == null ) {
            return null;
        }
        final StringBuilder name = new StringBuilder ( this.getDomain() );
        name.append ( ":type=ProtocolHandler,port=" );
        final int port = this.getPort();
        if ( port > 0 ) {
            name.append ( this.getPort() );
        } else {
            name.append ( "auto-" );
            name.append ( this.getNameIndex() );
        }
        final InetAddress address = this.getAddress();
        if ( address != null ) {
            name.append ( ",address=" );
            name.append ( ObjectName.quote ( address.getHostAddress() ) );
        }
        return new ObjectName ( name.toString() );
    }
    @Override
    public void init() throws Exception {
        if ( this.getLog().isInfoEnabled() ) {
            this.getLog().info ( AbstractProtocol.sm.getString ( "abstractProtocolHandler.init", this.getName() ) );
        }
        if ( this.oname == null ) {
            this.oname = this.createObjectName();
            if ( this.oname != null ) {
                Registry.getRegistry ( null, null ).registerComponent ( this, this.oname, null );
            }
        }
        if ( this.domain != null ) {
            try {
                this.tpOname = new ObjectName ( this.domain + ":type=ThreadPool,name=" + this.getName() );
                Registry.getRegistry ( null, null ).registerComponent ( this.endpoint, this.tpOname, null );
            } catch ( Exception e ) {
                this.getLog().error ( AbstractProtocol.sm.getString ( "abstractProtocolHandler.mbeanRegistrationFailed", this.tpOname, this.getName() ), e );
            }
            this.rgOname = new ObjectName ( this.domain + ":type=GlobalRequestProcessor,name=" + this.getName() );
            Registry.getRegistry ( null, null ).registerComponent ( this.getHandler().getGlobal(), this.rgOname, null );
        }
        final String endpointName = this.getName();
        this.endpoint.setName ( endpointName.substring ( 1, endpointName.length() - 1 ) );
        this.endpoint.init();
    }
    @Override
    public void start() throws Exception {
        if ( this.getLog().isInfoEnabled() ) {
            this.getLog().info ( AbstractProtocol.sm.getString ( "abstractProtocolHandler.start", this.getNameInternal() ) );
        }
        this.endpoint.start();
        this.asyncTimeout = new AsyncTimeout();
        final Thread timeoutThread = new Thread ( this.asyncTimeout, this.getNameInternal() + "-AsyncTimeout" );
        int priority = this.endpoint.getThreadPriority();
        if ( priority < 1 || priority > 10 ) {
            priority = 5;
        }
        timeoutThread.setPriority ( priority );
        timeoutThread.setDaemon ( true );
        timeoutThread.start();
    }
    @Override
    public void pause() throws Exception {
        if ( this.getLog().isInfoEnabled() ) {
            this.getLog().info ( AbstractProtocol.sm.getString ( "abstractProtocolHandler.pause", this.getName() ) );
        }
        this.endpoint.pause();
    }
    @Override
    public void resume() throws Exception {
        if ( this.getLog().isInfoEnabled() ) {
            this.getLog().info ( AbstractProtocol.sm.getString ( "abstractProtocolHandler.resume", this.getName() ) );
        }
        this.endpoint.resume();
    }
    @Override
    public void stop() throws Exception {
        if ( this.getLog().isInfoEnabled() ) {
            this.getLog().info ( AbstractProtocol.sm.getString ( "abstractProtocolHandler.stop", this.getName() ) );
        }
        if ( this.asyncTimeout != null ) {
            this.asyncTimeout.stop();
        }
        this.endpoint.stop();
    }
    @Override
    public void destroy() throws Exception {
        if ( this.getLog().isInfoEnabled() ) {
            this.getLog().info ( AbstractProtocol.sm.getString ( "abstractProtocolHandler.destroy", this.getName() ) );
        }
        try {
            this.endpoint.destroy();
        } finally {
            if ( this.oname != null ) {
                if ( this.mserver == null ) {
                    Registry.getRegistry ( null, null ).unregisterComponent ( this.oname );
                } else {
                    try {
                        this.mserver.unregisterMBean ( this.oname );
                    } catch ( MBeanRegistrationException | InstanceNotFoundException e ) {
                        this.getLog().info ( AbstractProtocol.sm.getString ( "abstractProtocol.mbeanDeregistrationFailed", this.oname, this.mserver ) );
                    }
                }
            }
            if ( this.tpOname != null ) {
                Registry.getRegistry ( null, null ).unregisterComponent ( this.tpOname );
            }
            if ( this.rgOname != null ) {
                Registry.getRegistry ( null, null ).unregisterComponent ( this.rgOname );
            }
        }
    }
    static {
        sm = StringManager.getManager ( AbstractProtocol.class );
        nameCounter = new AtomicInteger ( 0 );
    }
    protected static class ConnectionHandler<S> implements AbstractEndpoint.Handler<S> {
        private final AbstractProtocol<S> proto;
        private final RequestGroupInfo global;
        private final AtomicLong registerCount;
        private final Map<S, Processor> connections;
        private final RecycledProcessors recycledProcessors;
        public ConnectionHandler ( final AbstractProtocol<S> proto ) {
            this.global = new RequestGroupInfo();
            this.registerCount = new AtomicLong ( 0L );
            this.connections = new ConcurrentHashMap<S, Processor>();
            this.recycledProcessors = new RecycledProcessors ( this );
            this.proto = proto;
        }
        protected AbstractProtocol<S> getProtocol() {
            return this.proto;
        }
        protected Log getLog() {
            return this.getProtocol().getLog();
        }
        @Override
        public Object getGlobal() {
            return this.global;
        }
        @Override
        public void recycle() {
            this.recycledProcessors.clear();
        }
        @Override
        public SocketState process ( final SocketWrapperBase<S> wrapper, final SocketEvent status ) {
            if ( this.getLog().isDebugEnabled() ) {
                this.getLog().debug ( AbstractProtocol.sm.getString ( "abstractConnectionHandler.process", wrapper.getSocket(), status ) );
            }
            if ( wrapper == null ) {
                return SocketState.CLOSED;
            }
            final S socket = wrapper.getSocket();
            Processor processor = this.connections.get ( socket );
            if ( ( status == SocketEvent.DISCONNECT || status == SocketEvent.ERROR ) && processor == null ) {
                return SocketState.CLOSED;
            }
            ContainerThreadMarker.set();
            try {
                if ( processor == null ) {
                    final String negotiatedProtocol = wrapper.getNegotiatedProtocol();
                    if ( negotiatedProtocol != null ) {
                        final UpgradeProtocol upgradeProtocol = this.getProtocol().getNegotiatedProtocol ( negotiatedProtocol );
                        if ( upgradeProtocol != null ) {
                            processor = upgradeProtocol.getProcessor ( wrapper, this.getProtocol().getAdapter() );
                        } else if ( !negotiatedProtocol.equals ( "http/1.1" ) ) {
                            if ( this.getLog().isDebugEnabled() ) {
                                this.getLog().debug ( AbstractProtocol.sm.getString ( "abstractConnectionHandler.negotiatedProcessor.fail", negotiatedProtocol ) );
                            }
                            return SocketState.CLOSED;
                        }
                    }
                }
                if ( processor == null ) {
                    processor = this.recycledProcessors.pop();
                }
                if ( processor == null ) {
                    processor = this.getProtocol().createProcessor();
                    this.register ( processor );
                }
                processor.setSslSupport ( wrapper.getSslSupport ( this.getProtocol().getClientCertProvider() ) );
                this.connections.put ( socket, processor );
                this.getProtocol().removeWaitingProcessor ( processor );
                SocketState state = SocketState.CLOSED;
                do {
                    state = processor.process ( wrapper, status );
                    if ( state == SocketState.UPGRADING ) {
                        final UpgradeToken upgradeToken = processor.getUpgradeToken();
                        final ByteBuffer leftOverInput = processor.getLeftoverInput();
                        if ( upgradeToken == null ) {
                            final UpgradeProtocol upgradeProtocol2 = this.getProtocol().getUpgradeProtocol ( "h2c" );
                            if ( upgradeProtocol2 == null ) {
                                if ( this.getLog().isDebugEnabled() ) {
                                    this.getLog().debug ( AbstractProtocol.sm.getString ( "abstractConnectionHandler.negotiatedProcessor.fail", "h2c" ) );
                                }
                                return SocketState.CLOSED;
                            }
                            processor = upgradeProtocol2.getProcessor ( wrapper, this.getProtocol().getAdapter() );
                            wrapper.unRead ( leftOverInput );
                            this.connections.put ( socket, processor );
                        } else {
                            final HttpUpgradeHandler httpUpgradeHandler = upgradeToken.getHttpUpgradeHandler();
                            this.release ( processor );
                            processor = this.getProtocol().createUpgradeProcessor ( wrapper, upgradeToken );
                            wrapper.unRead ( leftOverInput );
                            wrapper.setUpgraded ( true );
                            this.connections.put ( socket, processor );
                            if ( upgradeToken.getInstanceManager() == null ) {
                                httpUpgradeHandler.init ( ( WebConnection ) processor );
                            } else {
                                final ClassLoader oldCL = upgradeToken.getContextBind().bind ( false, null );
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
                    this.longPoll ( wrapper, processor );
                    if ( processor.isAsync() ) {
                        this.getProtocol().addWaitingProcessor ( processor );
                    }
                } else if ( state == SocketState.OPEN ) {
                    this.connections.remove ( socket );
                    this.release ( processor );
                    wrapper.registerReadInterest();
                } else if ( state == SocketState.SENDFILE ) {
                    this.connections.remove ( socket );
                    this.release ( processor );
                } else if ( state == SocketState.UPGRADED ) {
                    if ( status != SocketEvent.OPEN_WRITE ) {
                        this.longPoll ( wrapper, processor );
                    }
                } else {
                    this.connections.remove ( socket );
                    if ( processor.isUpgrade() ) {
                        final UpgradeToken upgradeToken = processor.getUpgradeToken();
                        final HttpUpgradeHandler httpUpgradeHandler2 = upgradeToken.getHttpUpgradeHandler();
                        final InstanceManager instanceManager = upgradeToken.getInstanceManager();
                        if ( instanceManager == null ) {
                            httpUpgradeHandler2.destroy();
                        } else {
                            final ClassLoader oldCL = upgradeToken.getContextBind().bind ( false, null );
                            try {
                                httpUpgradeHandler2.destroy();
                            } finally {
                                try {
                                    instanceManager.destroyInstance ( httpUpgradeHandler2 );
                                } catch ( Throwable e ) {
                                    ExceptionUtils.handleThrowable ( e );
                                    this.getLog().error ( AbstractProtocol.sm.getString ( "abstractConnectionHandler.error" ), e );
                                }
                                upgradeToken.getContextBind().unbind ( false, oldCL );
                            }
                        }
                    } else {
                        this.release ( processor );
                    }
                }
                return state;
            } catch ( SocketException e2 ) {
                this.getLog().debug ( AbstractProtocol.sm.getString ( "abstractConnectionHandler.socketexception.debug" ), e2 );
            } catch ( IOException e3 ) {
                this.getLog().debug ( AbstractProtocol.sm.getString ( "abstractConnectionHandler.ioexception.debug" ), e3 );
            } catch ( ProtocolException e4 ) {
                this.getLog().debug ( AbstractProtocol.sm.getString ( "abstractConnectionHandler.protocolexception.debug" ), e4 );
            } catch ( Throwable e5 ) {
                ExceptionUtils.handleThrowable ( e5 );
                this.getLog().error ( AbstractProtocol.sm.getString ( "abstractConnectionHandler.error" ), e5 );
            } finally {
                ContainerThreadMarker.clear();
            }
            this.connections.remove ( socket );
            this.release ( processor );
            return SocketState.CLOSED;
        }
        protected void longPoll ( final SocketWrapperBase<?> socket, final Processor processor ) {
            if ( !processor.isAsync() ) {
                socket.registerReadInterest();
            }
        }
        @Override
        public Set<S> getOpenSockets() {
            return this.connections.keySet();
        }
        private void release ( final Processor processor ) {
            if ( processor != null ) {
                processor.recycle();
                if ( !processor.isUpgrade() ) {
                    this.recycledProcessors.push ( processor );
                }
            }
        }
        @Override
        public void release ( final SocketWrapperBase<S> socketWrapper ) {
            final S socket = socketWrapper.getSocket();
            final Processor processor = this.connections.remove ( socket );
            this.release ( processor );
        }
        protected void register ( final Processor processor ) {
            if ( this.getProtocol().getDomain() != null ) {
                synchronized ( this ) {
                    try {
                        final long count = this.registerCount.incrementAndGet();
                        final RequestInfo rp = processor.getRequest().getRequestProcessor();
                        rp.setGlobalProcessor ( this.global );
                        final ObjectName rpName = new ObjectName ( this.getProtocol().getDomain() + ":type=RequestProcessor,worker=" + this.getProtocol().getName() + ",name=" + this.getProtocol().getProtocolName() + "Request" + count );
                        if ( this.getLog().isDebugEnabled() ) {
                            this.getLog().debug ( "Register " + rpName );
                        }
                        Registry.getRegistry ( null, null ).registerComponent ( rp, rpName, null );
                        rp.setRpName ( rpName );
                    } catch ( Exception e ) {
                        this.getLog().warn ( "Error registering request" );
                    }
                }
            }
        }
        protected void unregister ( final Processor processor ) {
            if ( this.getProtocol().getDomain() != null ) {
                synchronized ( this ) {
                    try {
                        final Request r = processor.getRequest();
                        if ( r == null ) {
                            return;
                        }
                        final RequestInfo rp = r.getRequestProcessor();
                        rp.setGlobalProcessor ( null );
                        final ObjectName rpName = rp.getRpName();
                        if ( this.getLog().isDebugEnabled() ) {
                            this.getLog().debug ( "Unregister " + rpName );
                        }
                        Registry.getRegistry ( null, null ).unregisterComponent ( rpName );
                        rp.setRpName ( null );
                    } catch ( Exception e ) {
                        this.getLog().warn ( "Error unregistering request", e );
                    }
                }
            }
        }
        @Override
        public final void pause() {
            for ( final Processor processor : this.connections.values() ) {
                processor.pause();
            }
        }
    }
    protected static class RecycledProcessors extends SynchronizedStack<Processor> {
        private final transient ConnectionHandler<?> handler;
        protected final AtomicInteger size;
        public RecycledProcessors ( final ConnectionHandler<?> handler ) {
            this.size = new AtomicInteger ( 0 );
            this.handler = handler;
        }
        @Override
        public boolean push ( final Processor processor ) {
            final int cacheSize = this.handler.getProtocol().getProcessorCache();
            final boolean offer = cacheSize == -1 || this.size.get() < cacheSize;
            boolean result = false;
            if ( offer ) {
                result = super.push ( processor );
                if ( result ) {
                    this.size.incrementAndGet();
                }
            }
            if ( !result ) {
                this.handler.unregister ( processor );
            }
            return result;
        }
        @Override
        public Processor pop() {
            final Processor result = super.pop();
            if ( result != null ) {
                this.size.decrementAndGet();
            }
            return result;
        }
        @Override
        public synchronized void clear() {
            for ( Processor next = this.pop(); next != null; next = this.pop() ) {
                this.handler.unregister ( next );
            }
            super.clear();
            this.size.set ( 0 );
        }
    }
    protected class AsyncTimeout implements Runnable {
        private volatile boolean asyncTimeoutRunning;
        protected AsyncTimeout() {
            this.asyncTimeoutRunning = true;
        }
        @Override
        public void run() {
            while ( this.asyncTimeoutRunning ) {
                try {
                    Thread.sleep ( 1000L );
                } catch ( InterruptedException ex ) {}
                final long now = System.currentTimeMillis();
                for ( final Processor processor : AbstractProtocol.this.waitingProcessors ) {
                    processor.timeoutAsync ( now );
                }
                while ( AbstractProtocol.this.endpoint.isPaused() && this.asyncTimeoutRunning ) {
                    try {
                        Thread.sleep ( 1000L );
                    } catch ( InterruptedException ex2 ) {}
                }
            }
        }
        protected void stop() {
            this.asyncTimeoutRunning = false;
            for ( final Processor processor : AbstractProtocol.this.waitingProcessors ) {
                processor.timeoutAsync ( -1L );
            }
        }
    }
}
