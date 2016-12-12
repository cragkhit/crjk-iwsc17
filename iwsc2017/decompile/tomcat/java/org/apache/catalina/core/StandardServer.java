package org.apache.catalina.core;
import org.apache.juli.logging.LogFactory;
import java.net.URL;
import java.net.URISyntaxException;
import org.apache.catalina.util.ExtensionValidator;
import java.net.URLClassLoader;
import org.apache.catalina.mbeans.MBeanFactory;
import org.apache.tomcat.util.buf.StringCache;
import org.apache.catalina.LifecycleState;
import javax.management.MBeanException;
import javax.management.InstanceNotFoundException;
import org.apache.tomcat.util.ExceptionUtils;
import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.net.Socket;
import java.security.AccessControlException;
import java.net.SocketTimeoutException;
import java.net.InetAddress;
import java.io.IOException;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.util.ServerInfo;
import org.apache.catalina.LifecycleListener;
import javax.management.ObjectName;
import java.io.File;
import java.net.ServerSocket;
import org.apache.catalina.startup.Catalina;
import java.beans.PropertyChangeSupport;
import org.apache.tomcat.util.res.StringManager;
import org.apache.catalina.Service;
import java.util.Random;
import org.apache.catalina.deploy.NamingResourcesImpl;
import javax.naming.Context;
import org.apache.juli.logging.Log;
import org.apache.catalina.Server;
import org.apache.catalina.util.LifecycleMBeanBase;
public final class StandardServer extends LifecycleMBeanBase implements Server {
    private static final Log log;
    private Context globalNamingContext;
    private NamingResourcesImpl globalNamingResources;
    private final NamingContextListener namingContextListener;
    private int port;
    private String address;
    private Random random;
    private Service[] services;
    private final Object servicesLock;
    private String shutdown;
    private static final StringManager sm;
    final PropertyChangeSupport support;
    private volatile boolean stopAwait;
    private Catalina catalina;
    private ClassLoader parentClassLoader;
    private volatile Thread awaitThread;
    private volatile ServerSocket awaitSocket;
    private File catalinaHome;
    private File catalinaBase;
    private final Object namingToken;
    private ObjectName onameStringCache;
    private ObjectName onameMBeanFactory;
    public StandardServer() {
        this.globalNamingContext = null;
        this.globalNamingResources = null;
        this.port = 8005;
        this.address = "localhost";
        this.random = null;
        this.services = new Service[0];
        this.servicesLock = new Object();
        this.shutdown = "SHUTDOWN";
        this.support = new PropertyChangeSupport ( this );
        this.stopAwait = false;
        this.catalina = null;
        this.parentClassLoader = null;
        this.awaitThread = null;
        this.awaitSocket = null;
        this.catalinaHome = null;
        this.catalinaBase = null;
        this.namingToken = new Object();
        ( this.globalNamingResources = new NamingResourcesImpl() ).setContainer ( this );
        if ( this.isUseNaming() ) {
            this.addLifecycleListener ( this.namingContextListener = new NamingContextListener() );
        } else {
            this.namingContextListener = null;
        }
    }
    @Override
    public Object getNamingToken() {
        return this.namingToken;
    }
    @Override
    public Context getGlobalNamingContext() {
        return this.globalNamingContext;
    }
    public void setGlobalNamingContext ( final Context globalNamingContext ) {
        this.globalNamingContext = globalNamingContext;
    }
    @Override
    public NamingResourcesImpl getGlobalNamingResources() {
        return this.globalNamingResources;
    }
    @Override
    public void setGlobalNamingResources ( final NamingResourcesImpl globalNamingResources ) {
        final NamingResourcesImpl oldGlobalNamingResources = this.globalNamingResources;
        ( this.globalNamingResources = globalNamingResources ).setContainer ( this );
        this.support.firePropertyChange ( "globalNamingResources", oldGlobalNamingResources, this.globalNamingResources );
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
        return this.port;
    }
    @Override
    public void setPort ( final int port ) {
        this.port = port;
    }
    @Override
    public String getAddress() {
        return this.address;
    }
    @Override
    public void setAddress ( final String address ) {
        this.address = address;
    }
    @Override
    public String getShutdown() {
        return this.shutdown;
    }
    @Override
    public void setShutdown ( final String shutdown ) {
        this.shutdown = shutdown;
    }
    @Override
    public Catalina getCatalina() {
        return this.catalina;
    }
    @Override
    public void setCatalina ( final Catalina catalina ) {
        this.catalina = catalina;
    }
    @Override
    public void addService ( final Service service ) {
        service.setServer ( this );
        synchronized ( this.servicesLock ) {
            final Service[] results = new Service[this.services.length + 1];
            System.arraycopy ( this.services, 0, results, 0, this.services.length );
            results[this.services.length] = service;
            this.services = results;
            if ( this.getState().isAvailable() ) {
                try {
                    service.start();
                } catch ( LifecycleException ex ) {}
            }
            this.support.firePropertyChange ( "service", null, service );
        }
    }
    public void stopAwait() {
        this.stopAwait = true;
        final Thread t = this.awaitThread;
        if ( t != null ) {
            final ServerSocket s = this.awaitSocket;
            if ( s != null ) {
                this.awaitSocket = null;
                try {
                    s.close();
                } catch ( IOException ex ) {}
            }
            t.interrupt();
            try {
                t.join ( 1000L );
            } catch ( InterruptedException ex2 ) {}
        }
    }
    @Override
    public void await() {
        if ( this.port == -2 ) {
            return;
        }
        if ( this.port == -1 ) {
            try {
                this.awaitThread = Thread.currentThread();
                while ( !this.stopAwait ) {
                    try {
                        Thread.sleep ( 10000L );
                    } catch ( InterruptedException ex ) {}
                }
            } finally {
                this.awaitThread = null;
            }
            return;
        }
        try {
            this.awaitSocket = new ServerSocket ( this.port, 1, InetAddress.getByName ( this.address ) );
        } catch ( IOException e ) {
            StandardServer.log.error ( "StandardServer.await: create[" + this.address + ":" + this.port + "]: ", e );
            return;
        }
        try {
            this.awaitThread = Thread.currentThread();
            while ( !this.stopAwait ) {
                final ServerSocket serverSocket = this.awaitSocket;
                if ( serverSocket == null ) {
                    break;
                }
                Socket socket = null;
                final StringBuilder command = new StringBuilder();
                try {
                    final long acceptStartTime = System.currentTimeMillis();
                    InputStream stream = null;
                    try {
                        socket = serverSocket.accept();
                        socket.setSoTimeout ( 10000 );
                        stream = socket.getInputStream();
                    } catch ( SocketTimeoutException ste ) {
                        StandardServer.log.warn ( StandardServer.sm.getString ( "standardServer.accept.timeout", System.currentTimeMillis() - acceptStartTime ), ste );
                    } catch ( AccessControlException ace ) {
                        StandardServer.log.warn ( "StandardServer.accept security exception: " + ace.getMessage(), ace );
                    } catch ( IOException e2 ) {
                        if ( this.stopAwait ) {}
                        StandardServer.log.error ( "StandardServer.await: accept: ", e2 );
                    }
                    int expected;
                    for ( expected = 1024; expected < this.shutdown.length(); expected += this.random.nextInt() % 1024 ) {
                        if ( this.random == null ) {
                            this.random = new Random();
                        }
                    }
                    while ( expected > 0 ) {
                        int ch = -1;
                        try {
                            ch = stream.read();
                        } catch ( IOException e3 ) {
                            StandardServer.log.warn ( "StandardServer.await: read: ", e3 );
                            ch = -1;
                        }
                        if ( ch < 32 ) {
                            break;
                        }
                        if ( ch == 127 ) {
                            break;
                        }
                        command.append ( ( char ) ch );
                        --expected;
                    }
                } finally {
                    try {
                        if ( socket != null ) {
                            socket.close();
                        }
                    } catch ( IOException ex2 ) {}
                }
                final boolean match = command.toString().equals ( this.shutdown );
                if ( match ) {
                    StandardServer.log.info ( StandardServer.sm.getString ( "standardServer.shutdownViaPort" ) );
                    break;
                }
                StandardServer.log.warn ( "StandardServer.await: Invalid command '" + command.toString() + "' received" );
            }
        } finally {
            final ServerSocket serverSocket2 = this.awaitSocket;
            this.awaitThread = null;
            this.awaitSocket = null;
            if ( serverSocket2 != null ) {
                try {
                    serverSocket2.close();
                } catch ( IOException ex3 ) {}
            }
        }
    }
    @Override
    public Service findService ( final String name ) {
        if ( name == null ) {
            return null;
        }
        synchronized ( this.servicesLock ) {
            for ( int i = 0; i < this.services.length; ++i ) {
                if ( name.equals ( this.services[i].getName() ) ) {
                    return this.services[i];
                }
            }
        }
        return null;
    }
    @Override
    public Service[] findServices() {
        return this.services;
    }
    public ObjectName[] getServiceNames() {
        final ObjectName[] onames = new ObjectName[this.services.length];
        for ( int i = 0; i < this.services.length; ++i ) {
            onames[i] = ( ( StandardService ) this.services[i] ).getObjectName();
        }
        return onames;
    }
    @Override
    public void removeService ( final Service service ) {
        synchronized ( this.servicesLock ) {
            int j = -1;
            for ( int i = 0; i < this.services.length; ++i ) {
                if ( service == this.services[i] ) {
                    j = i;
                    break;
                }
            }
            if ( j < 0 ) {
                return;
            }
            try {
                this.services[j].stop();
            } catch ( LifecycleException ex ) {}
            int k = 0;
            final Service[] results = new Service[this.services.length - 1];
            for ( int l = 0; l < this.services.length; ++l ) {
                if ( l != j ) {
                    results[k++] = this.services[l];
                }
            }
            this.services = results;
            this.support.firePropertyChange ( "service", service, null );
        }
    }
    @Override
    public File getCatalinaBase() {
        if ( this.catalinaBase != null ) {
            return this.catalinaBase;
        }
        return this.catalinaBase = this.getCatalinaHome();
    }
    @Override
    public void setCatalinaBase ( final File catalinaBase ) {
        this.catalinaBase = catalinaBase;
    }
    @Override
    public File getCatalinaHome() {
        return this.catalinaHome;
    }
    @Override
    public void setCatalinaHome ( final File catalinaHome ) {
        this.catalinaHome = catalinaHome;
    }
    public void addPropertyChangeListener ( final PropertyChangeListener listener ) {
        this.support.addPropertyChangeListener ( listener );
    }
    public void removePropertyChangeListener ( final PropertyChangeListener listener ) {
        this.support.removePropertyChangeListener ( listener );
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "StandardServer[" );
        sb.append ( this.getPort() );
        sb.append ( "]" );
        return sb.toString();
    }
    public synchronized void storeConfig() throws InstanceNotFoundException, MBeanException {
        try {
            final ObjectName sname = new ObjectName ( "Catalina:type=StoreConfig" );
            if ( this.mserver.isRegistered ( sname ) ) {
                this.mserver.invoke ( sname, "storeConfig", null, null );
            } else {
                StandardServer.log.error ( StandardServer.sm.getString ( "standardServer.storeConfig.notAvailable", sname ) );
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            StandardServer.log.error ( t );
        }
    }
    public synchronized void storeContext ( final org.apache.catalina.Context context ) throws InstanceNotFoundException, MBeanException {
        try {
            final ObjectName sname = new ObjectName ( "Catalina:type=StoreConfig" );
            if ( this.mserver.isRegistered ( sname ) ) {
                this.mserver.invoke ( sname, "store", new Object[] { context }, new String[] { "java.lang.String" } );
            } else {
                StandardServer.log.error ( StandardServer.sm.getString ( "standardServer.storeConfig.notAvailable", sname ) );
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            StandardServer.log.error ( t );
        }
    }
    private boolean isUseNaming() {
        boolean useNaming = true;
        final String useNamingProperty = System.getProperty ( "catalina.useNaming" );
        if ( useNamingProperty != null && useNamingProperty.equals ( "false" ) ) {
            useNaming = false;
        }
        return useNaming;
    }
    @Override
    protected void startInternal() throws LifecycleException {
        this.fireLifecycleEvent ( "configure_start", null );
        this.setState ( LifecycleState.STARTING );
        this.globalNamingResources.start();
        synchronized ( this.servicesLock ) {
            for ( int i = 0; i < this.services.length; ++i ) {
                this.services[i].start();
            }
        }
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        this.setState ( LifecycleState.STOPPING );
        this.fireLifecycleEvent ( "configure_stop", null );
        for ( int i = 0; i < this.services.length; ++i ) {
            this.services[i].stop();
        }
        this.globalNamingResources.stop();
        this.stopAwait();
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        this.onameStringCache = this.register ( new StringCache(), "type=StringCache" );
        final MBeanFactory factory = new MBeanFactory();
        factory.setContainer ( this );
        this.onameMBeanFactory = this.register ( factory, "type=MBeanFactory" );
        this.globalNamingResources.init();
        if ( this.getCatalina() != null ) {
            for ( ClassLoader cl = this.getCatalina().getParentClassLoader(); cl != null && cl != ClassLoader.getSystemClassLoader(); cl = cl.getParent() ) {
                if ( cl instanceof URLClassLoader ) {
                    final URL[] urLs;
                    final URL[] urls = urLs = ( ( URLClassLoader ) cl ).getURLs();
                    for ( final URL url : urLs ) {
                        if ( url.getProtocol().equals ( "file" ) ) {
                            try {
                                final File f = new File ( url.toURI() );
                                if ( f.isFile() && f.getName().endsWith ( ".jar" ) ) {
                                    ExtensionValidator.addSystemResource ( f );
                                }
                            } catch ( URISyntaxException ex ) {}
                            catch ( IOException ex2 ) {}
                        }
                    }
                }
            }
        }
        for ( int i = 0; i < this.services.length; ++i ) {
            this.services[i].init();
        }
    }
    @Override
    protected void destroyInternal() throws LifecycleException {
        for ( int i = 0; i < this.services.length; ++i ) {
            this.services[i].destroy();
        }
        this.globalNamingResources.destroy();
        this.unregister ( this.onameMBeanFactory );
        this.unregister ( this.onameStringCache );
        super.destroyInternal();
    }
    @Override
    public ClassLoader getParentClassLoader() {
        if ( this.parentClassLoader != null ) {
            return this.parentClassLoader;
        }
        if ( this.catalina != null ) {
            return this.catalina.getParentClassLoader();
        }
        return ClassLoader.getSystemClassLoader();
    }
    @Override
    public void setParentClassLoader ( final ClassLoader parent ) {
        final ClassLoader oldParentClassLoader = this.parentClassLoader;
        this.parentClassLoader = parent;
        this.support.firePropertyChange ( "parentClassLoader", oldParentClassLoader, this.parentClassLoader );
    }
    @Override
    protected String getDomainInternal() {
        String domain = null;
        final Service[] services = this.findServices();
        if ( services.length > 0 ) {
            final Service service = services[0];
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
    static {
        log = LogFactory.getLog ( StandardServer.class );
        sm = StringManager.getManager ( "org.apache.catalina.core" );
    }
}
