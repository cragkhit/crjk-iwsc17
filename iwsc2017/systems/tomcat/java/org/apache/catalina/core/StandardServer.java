package org.apache.catalina.core;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlException;
import java.util.Random;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.catalina.mbeans.MBeanFactory;
import org.apache.catalina.startup.Catalina;
import org.apache.catalina.util.ExtensionValidator;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.catalina.util.ServerInfo;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.buf.StringCache;
import org.apache.tomcat.util.res.StringManager;
public final class StandardServer extends LifecycleMBeanBase implements Server {
    private static final Log log = LogFactory.getLog ( StandardServer.class );
    public StandardServer() {
        super();
        globalNamingResources = new NamingResourcesImpl();
        globalNamingResources.setContainer ( this );
        if ( isUseNaming() ) {
            namingContextListener = new NamingContextListener();
            addLifecycleListener ( namingContextListener );
        } else {
            namingContextListener = null;
        }
    }
    private javax.naming.Context globalNamingContext = null;
    private NamingResourcesImpl globalNamingResources = null;
    private final NamingContextListener namingContextListener;
    private int port = 8005;
    private String address = "localhost";
    private Random random = null;
    private Service services[] = new Service[0];
    private final Object servicesLock = new Object();
    private String shutdown = "SHUTDOWN";
    private static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    final PropertyChangeSupport support = new PropertyChangeSupport ( this );
    private volatile boolean stopAwait = false;
    private Catalina catalina = null;
    private ClassLoader parentClassLoader = null;
    private volatile Thread awaitThread = null;
    private volatile ServerSocket awaitSocket = null;
    private File catalinaHome = null;
    private File catalinaBase = null;
    private final Object namingToken = new Object();
    @Override
    public Object getNamingToken() {
        return namingToken;
    }
    @Override
    public javax.naming.Context getGlobalNamingContext() {
        return ( this.globalNamingContext );
    }
    public void setGlobalNamingContext
    ( javax.naming.Context globalNamingContext ) {
        this.globalNamingContext = globalNamingContext;
    }
    @Override
    public NamingResourcesImpl getGlobalNamingResources() {
        return ( this.globalNamingResources );
    }
    @Override
    public void setGlobalNamingResources
    ( NamingResourcesImpl globalNamingResources ) {
        NamingResourcesImpl oldGlobalNamingResources =
            this.globalNamingResources;
        this.globalNamingResources = globalNamingResources;
        this.globalNamingResources.setContainer ( this );
        support.firePropertyChange ( "globalNamingResources",
                                     oldGlobalNamingResources,
                                     this.globalNamingResources );
    }
    public String getServerInfo() {
        return ServerInfo.getServerInfo();
    }
    public String getServerBuilt() {
        return ServerInfo.getServerBuilt();
    }
    public String getServerNumber() {
        return ServerInfo.getServerNumber();
    }
    @Override
    public int getPort() {
        return ( this.port );
    }
    @Override
    public void setPort ( int port ) {
        this.port = port;
    }
    @Override
    public String getAddress() {
        return ( this.address );
    }
    @Override
    public void setAddress ( String address ) {
        this.address = address;
    }
    @Override
    public String getShutdown() {
        return ( this.shutdown );
    }
    @Override
    public void setShutdown ( String shutdown ) {
        this.shutdown = shutdown;
    }
    @Override
    public Catalina getCatalina() {
        return catalina;
    }
    @Override
    public void setCatalina ( Catalina catalina ) {
        this.catalina = catalina;
    }
    @Override
    public void addService ( Service service ) {
        service.setServer ( this );
        synchronized ( servicesLock ) {
            Service results[] = new Service[services.length + 1];
            System.arraycopy ( services, 0, results, 0, services.length );
            results[services.length] = service;
            services = results;
            if ( getState().isAvailable() ) {
                try {
                    service.start();
                } catch ( LifecycleException e ) {
                }
            }
            support.firePropertyChange ( "service", null, service );
        }
    }
    public void stopAwait() {
        stopAwait = true;
        Thread t = awaitThread;
        if ( t != null ) {
            ServerSocket s = awaitSocket;
            if ( s != null ) {
                awaitSocket = null;
                try {
                    s.close();
                } catch ( IOException e ) {
                }
            }
            t.interrupt();
            try {
                t.join ( 1000 );
            } catch ( InterruptedException e ) {
            }
        }
    }
    @Override
    public void await() {
        if ( port == -2 ) {
            return;
        }
        if ( port == -1 ) {
            try {
                awaitThread = Thread.currentThread();
                while ( !stopAwait ) {
                    try {
                        Thread.sleep ( 10000 );
                    } catch ( InterruptedException ex ) {
                    }
                }
            } finally {
                awaitThread = null;
            }
            return;
        }
        try {
            awaitSocket = new ServerSocket ( port, 1,
                                             InetAddress.getByName ( address ) );
        } catch ( IOException e ) {
            log.error ( "StandardServer.await: create[" + address
                        + ":" + port
                        + "]: ", e );
            return;
        }
        try {
            awaitThread = Thread.currentThread();
            while ( !stopAwait ) {
                ServerSocket serverSocket = awaitSocket;
                if ( serverSocket == null ) {
                    break;
                }
                Socket socket = null;
                StringBuilder command = new StringBuilder();
                try {
                    InputStream stream;
                    long acceptStartTime = System.currentTimeMillis();
                    try {
                        socket = serverSocket.accept();
                        socket.setSoTimeout ( 10 * 1000 );
                        stream = socket.getInputStream();
                    } catch ( SocketTimeoutException ste ) {
                        log.warn ( sm.getString ( "standardServer.accept.timeout",
                                                  Long.valueOf ( System.currentTimeMillis() - acceptStartTime ) ), ste );
                        continue;
                    } catch ( AccessControlException ace ) {
                        log.warn ( "StandardServer.accept security exception: "
                                   + ace.getMessage(), ace );
                        continue;
                    } catch ( IOException e ) {
                        if ( stopAwait ) {
                            break;
                        }
                        log.error ( "StandardServer.await: accept: ", e );
                        break;
                    }
                    int expected = 1024;
                    while ( expected < shutdown.length() ) {
                        if ( random == null ) {
                            random = new Random();
                        }
                        expected += ( random.nextInt() % 1024 );
                    }
                    while ( expected > 0 ) {
                        int ch = -1;
                        try {
                            ch = stream.read();
                        } catch ( IOException e ) {
                            log.warn ( "StandardServer.await: read: ", e );
                            ch = -1;
                        }
                        if ( ch < 32 || ch == 127 ) {
                            break;
                        }
                        command.append ( ( char ) ch );
                        expected--;
                    }
                } finally {
                    try {
                        if ( socket != null ) {
                            socket.close();
                        }
                    } catch ( IOException e ) {
                    }
                }
                boolean match = command.toString().equals ( shutdown );
                if ( match ) {
                    log.info ( sm.getString ( "standardServer.shutdownViaPort" ) );
                    break;
                } else
                    log.warn ( "StandardServer.await: Invalid command '"
                               + command.toString() + "' received" );
            }
        } finally {
            ServerSocket serverSocket = awaitSocket;
            awaitThread = null;
            awaitSocket = null;
            if ( serverSocket != null ) {
                try {
                    serverSocket.close();
                } catch ( IOException e ) {
                }
            }
        }
    }
    @Override
    public Service findService ( String name ) {
        if ( name == null ) {
            return ( null );
        }
        synchronized ( servicesLock ) {
            for ( int i = 0; i < services.length; i++ ) {
                if ( name.equals ( services[i].getName() ) ) {
                    return ( services[i] );
                }
            }
        }
        return ( null );
    }
    @Override
    public Service[] findServices() {
        return services;
    }
    public ObjectName[] getServiceNames() {
        ObjectName onames[] = new ObjectName[ services.length ];
        for ( int i = 0; i < services.length; i++ ) {
            onames[i] = ( ( StandardService ) services[i] ).getObjectName();
        }
        return onames;
    }
    @Override
    public void removeService ( Service service ) {
        synchronized ( servicesLock ) {
            int j = -1;
            for ( int i = 0; i < services.length; i++ ) {
                if ( service == services[i] ) {
                    j = i;
                    break;
                }
            }
            if ( j < 0 ) {
                return;
            }
            try {
                services[j].stop();
            } catch ( LifecycleException e ) {
            }
            int k = 0;
            Service results[] = new Service[services.length - 1];
            for ( int i = 0; i < services.length; i++ ) {
                if ( i != j ) {
                    results[k++] = services[i];
                }
            }
            services = results;
            support.firePropertyChange ( "service", service, null );
        }
    }
    @Override
    public File getCatalinaBase() {
        if ( catalinaBase != null ) {
            return catalinaBase;
        }
        catalinaBase = getCatalinaHome();
        return catalinaBase;
    }
    @Override
    public void setCatalinaBase ( File catalinaBase ) {
        this.catalinaBase = catalinaBase;
    }
    @Override
    public File getCatalinaHome() {
        return catalinaHome;
    }
    @Override
    public void setCatalinaHome ( File catalinaHome ) {
        this.catalinaHome = catalinaHome;
    }
    public void addPropertyChangeListener ( PropertyChangeListener listener ) {
        support.addPropertyChangeListener ( listener );
    }
    public void removePropertyChangeListener ( PropertyChangeListener listener ) {
        support.removePropertyChangeListener ( listener );
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "StandardServer[" );
        sb.append ( getPort() );
        sb.append ( "]" );
        return ( sb.toString() );
    }
    public synchronized void storeConfig() throws InstanceNotFoundException, MBeanException {
        try {
            ObjectName sname = new ObjectName ( "Catalina:type=StoreConfig" );
            if ( mserver.isRegistered ( sname ) ) {
                mserver.invoke ( sname, "storeConfig", null, null );
            } else {
                log.error ( sm.getString ( "standardServer.storeConfig.notAvailable", sname ) );
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log.error ( t );
        }
    }
    public synchronized void storeContext ( Context context ) throws InstanceNotFoundException, MBeanException {
        try {
            ObjectName sname = new ObjectName ( "Catalina:type=StoreConfig" );
            if ( mserver.isRegistered ( sname ) ) {
                mserver.invoke ( sname, "store",
                                 new Object[] {context},
                                 new String [] { "java.lang.String"} );
            } else {
                log.error ( sm.getString ( "standardServer.storeConfig.notAvailable", sname ) );
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log.error ( t );
        }
    }
    private boolean isUseNaming() {
        boolean useNaming = true;
        String useNamingProperty = System.getProperty ( "catalina.useNaming" );
        if ( ( useNamingProperty != null )
                && ( useNamingProperty.equals ( "false" ) ) ) {
            useNaming = false;
        }
        return useNaming;
    }
    @Override
    protected void startInternal() throws LifecycleException {
        fireLifecycleEvent ( CONFIGURE_START_EVENT, null );
        setState ( LifecycleState.STARTING );
        globalNamingResources.start();
        synchronized ( servicesLock ) {
            for ( int i = 0; i < services.length; i++ ) {
                services[i].start();
            }
        }
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        setState ( LifecycleState.STOPPING );
        fireLifecycleEvent ( CONFIGURE_STOP_EVENT, null );
        for ( int i = 0; i < services.length; i++ ) {
            services[i].stop();
        }
        globalNamingResources.stop();
        stopAwait();
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        onameStringCache = register ( new StringCache(), "type=StringCache" );
        MBeanFactory factory = new MBeanFactory();
        factory.setContainer ( this );
        onameMBeanFactory = register ( factory, "type=MBeanFactory" );
        globalNamingResources.init();
        if ( getCatalina() != null ) {
            ClassLoader cl = getCatalina().getParentClassLoader();
            while ( cl != null && cl != ClassLoader.getSystemClassLoader() ) {
                if ( cl instanceof URLClassLoader ) {
                    URL[] urls = ( ( URLClassLoader ) cl ).getURLs();
                    for ( URL url : urls ) {
                        if ( url.getProtocol().equals ( "file" ) ) {
                            try {
                                File f = new File ( url.toURI() );
                                if ( f.isFile() &&
                                        f.getName().endsWith ( ".jar" ) ) {
                                    ExtensionValidator.addSystemResource ( f );
                                }
                            } catch ( URISyntaxException e ) {
                            } catch ( IOException e ) {
                            }
                        }
                    }
                }
                cl = cl.getParent();
            }
        }
        for ( int i = 0; i < services.length; i++ ) {
            services[i].init();
        }
    }
    @Override
    protected void destroyInternal() throws LifecycleException {
        for ( int i = 0; i < services.length; i++ ) {
            services[i].destroy();
        }
        globalNamingResources.destroy();
        unregister ( onameMBeanFactory );
        unregister ( onameStringCache );
        super.destroyInternal();
    }
    @Override
    public ClassLoader getParentClassLoader() {
        if ( parentClassLoader != null ) {
            return ( parentClassLoader );
        }
        if ( catalina != null ) {
            return ( catalina.getParentClassLoader() );
        }
        return ( ClassLoader.getSystemClassLoader() );
    }
    @Override
    public void setParentClassLoader ( ClassLoader parent ) {
        ClassLoader oldParentClassLoader = this.parentClassLoader;
        this.parentClassLoader = parent;
        support.firePropertyChange ( "parentClassLoader", oldParentClassLoader,
                                     this.parentClassLoader );
    }
    private ObjectName onameStringCache;
    private ObjectName onameMBeanFactory;
    @Override
    protected String getDomainInternal() {
        String domain = null;
        Service[] services = findServices();
        if ( services.length > 0 ) {
            Service service = services[0];
            if ( service != null ) {
                domain = service.getDomain();
            }
        }
        return domain;
    }
    @Override
    protected final String getObjectNameKeyProperties() {
        return "type=Server";
    }
}
