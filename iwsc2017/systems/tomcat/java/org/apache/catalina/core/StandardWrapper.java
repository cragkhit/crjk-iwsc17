package org.apache.catalina.core;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletSecurityElement;
import javax.servlet.SingleThreadModel;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.ServletSecurity;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerServlet;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Wrapper;
import org.apache.catalina.security.SecurityUtil;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.PeriodicEventListener;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.log.SystemLogHandler;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.modeler.Util;
@SuppressWarnings ( "deprecation" )
public class StandardWrapper extends ContainerBase
    implements ServletConfig, Wrapper, NotificationEmitter {
    private static final Log log = LogFactory.getLog ( StandardWrapper.class );
    protected static final String[] DEFAULT_SERVLET_METHODS = new String[] {
        "GET", "HEAD", "POST"
    };
    public StandardWrapper() {
        super();
        swValve = new StandardWrapperValve();
        pipeline.setBasic ( swValve );
        broadcaster = new NotificationBroadcasterSupport();
    }
    protected long available = 0L;
    protected final NotificationBroadcasterSupport broadcaster;
    protected final AtomicInteger countAllocated = new AtomicInteger ( 0 );
    protected final StandardWrapperFacade facade = new StandardWrapperFacade ( this );
    protected volatile Servlet instance = null;
    protected volatile boolean instanceInitialized = false;
    protected int loadOnStartup = -1;
    protected final ArrayList<String> mappings = new ArrayList<>();
    protected HashMap<String, String> parameters = new HashMap<>();
    protected HashMap<String, String> references = new HashMap<>();
    protected String runAs = null;
    protected long sequenceNumber = 0;
    protected String servletClass = null;
    protected volatile boolean singleThreadModel = false;
    protected volatile boolean unloading = false;
    protected int maxInstances = 20;
    protected int nInstances = 0;
    protected Stack<Servlet> instancePool = null;
    protected long unloadDelay = 2000;
    protected boolean isJspServlet;
    protected ObjectName jspMonitorON;
    protected boolean swallowOutput = false;
    protected StandardWrapperValve swValve;
    protected long loadTime = 0;
    protected int classLoadTime = 0;
    protected MultipartConfigElement multipartConfigElement = null;
    protected boolean asyncSupported = false;
    protected boolean enabled = true;
    protected volatile boolean servletSecurityAnnotationScanRequired = false;
    private boolean overridable = false;
    protected static Class<?>[] classType = new Class[] {ServletConfig.class};
    private final ReentrantReadWriteLock parametersLock =
        new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock mappingsLock =
        new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock referencesLock =
        new ReentrantReadWriteLock();
    @Override
    public boolean isOverridable() {
        return overridable;
    }
    @Override
    public void setOverridable ( boolean overridable ) {
        this.overridable = overridable;
    }
    @Override
    public long getAvailable() {
        return ( this.available );
    }
    @Override
    public void setAvailable ( long available ) {
        long oldAvailable = this.available;
        if ( available > System.currentTimeMillis() ) {
            this.available = available;
        } else {
            this.available = 0L;
        }
        support.firePropertyChange ( "available", Long.valueOf ( oldAvailable ),
                                     Long.valueOf ( this.available ) );
    }
    public int getCountAllocated() {
        return this.countAllocated.get();
    }
    @Override
    public int getLoadOnStartup() {
        if ( isJspServlet && loadOnStartup < 0 ) {
            return Integer.MAX_VALUE;
        } else {
            return ( this.loadOnStartup );
        }
    }
    @Override
    public void setLoadOnStartup ( int value ) {
        int oldLoadOnStartup = this.loadOnStartup;
        this.loadOnStartup = value;
        support.firePropertyChange ( "loadOnStartup",
                                     Integer.valueOf ( oldLoadOnStartup ),
                                     Integer.valueOf ( this.loadOnStartup ) );
    }
    public void setLoadOnStartupString ( String value ) {
        try {
            setLoadOnStartup ( Integer.parseInt ( value ) );
        } catch ( NumberFormatException e ) {
            setLoadOnStartup ( 0 );
        }
    }
    public String getLoadOnStartupString() {
        return Integer.toString ( getLoadOnStartup() );
    }
    public int getMaxInstances() {
        return ( this.maxInstances );
    }
    public void setMaxInstances ( int maxInstances ) {
        int oldMaxInstances = this.maxInstances;
        this.maxInstances = maxInstances;
        support.firePropertyChange ( "maxInstances", oldMaxInstances,
                                     this.maxInstances );
    }
    @Override
    public void setParent ( Container container ) {
        if ( ( container != null ) &&
                ! ( container instanceof Context ) )
            throw new IllegalArgumentException
            ( sm.getString ( "standardWrapper.notContext" ) );
        if ( container instanceof StandardContext ) {
            swallowOutput = ( ( StandardContext ) container ).getSwallowOutput();
            unloadDelay = ( ( StandardContext ) container ).getUnloadDelay();
        }
        super.setParent ( container );
    }
    @Override
    public String getRunAs() {
        return ( this.runAs );
    }
    @Override
    public void setRunAs ( String runAs ) {
        String oldRunAs = this.runAs;
        this.runAs = runAs;
        support.firePropertyChange ( "runAs", oldRunAs, this.runAs );
    }
    @Override
    public String getServletClass() {
        return ( this.servletClass );
    }
    @Override
    public void setServletClass ( String servletClass ) {
        String oldServletClass = this.servletClass;
        this.servletClass = servletClass;
        support.firePropertyChange ( "servletClass", oldServletClass,
                                     this.servletClass );
        if ( Constants.JSP_SERVLET_CLASS.equals ( servletClass ) ) {
            isJspServlet = true;
        }
    }
    public void setServletName ( String name ) {
        setName ( name );
    }
    public Boolean isSingleThreadModel() {
        if ( singleThreadModel || instance != null ) {
            return Boolean.valueOf ( singleThreadModel );
        }
        return null;
    }
    @Override
    public boolean isUnavailable() {
        if ( !isEnabled() ) {
            return true;
        } else if ( available == 0L ) {
            return false;
        } else if ( available <= System.currentTimeMillis() ) {
            available = 0L;
            return false;
        } else {
            return true;
        }
    }
    @Override
    public String[] getServletMethods() throws ServletException {
        instance = loadServlet();
        Class<? extends Servlet> servletClazz = instance.getClass();
        if ( !javax.servlet.http.HttpServlet.class.isAssignableFrom (
                    servletClazz ) ) {
            return DEFAULT_SERVLET_METHODS;
        }
        HashSet<String> allow = new HashSet<>();
        allow.add ( "TRACE" );
        allow.add ( "OPTIONS" );
        Method[] methods = getAllDeclaredMethods ( servletClazz );
        for ( int i = 0; methods != null && i < methods.length; i++ ) {
            Method m = methods[i];
            if ( m.getName().equals ( "doGet" ) ) {
                allow.add ( "GET" );
                allow.add ( "HEAD" );
            } else if ( m.getName().equals ( "doPost" ) ) {
                allow.add ( "POST" );
            } else if ( m.getName().equals ( "doPut" ) ) {
                allow.add ( "PUT" );
            } else if ( m.getName().equals ( "doDelete" ) ) {
                allow.add ( "DELETE" );
            }
        }
        String[] methodNames = new String[allow.size()];
        return allow.toArray ( methodNames );
    }
    @Override
    public Servlet getServlet() {
        return instance;
    }
    @Override
    public void setServlet ( Servlet servlet ) {
        instance = servlet;
    }
    @Override
    public void setServletSecurityAnnotationScanRequired ( boolean b ) {
        this.servletSecurityAnnotationScanRequired = b;
    }
    @Override
    public void backgroundProcess() {
        super.backgroundProcess();
        if ( !getState().isAvailable() ) {
            return;
        }
        if ( getServlet() instanceof PeriodicEventListener ) {
            ( ( PeriodicEventListener ) getServlet() ).periodicEvent();
        }
    }
    public static Throwable getRootCause ( ServletException e ) {
        Throwable rootCause = e;
        Throwable rootCauseCheck = null;
        int loops = 0;
        do {
            loops++;
            rootCauseCheck = rootCause.getCause();
            if ( rootCauseCheck != null ) {
                rootCause = rootCauseCheck;
            }
        } while ( rootCauseCheck != null && ( loops < 20 ) );
        return rootCause;
    }
    @Override
    public void addChild ( Container child ) {
        throw new IllegalStateException
        ( sm.getString ( "standardWrapper.notChild" ) );
    }
    @Override
    public void addInitParameter ( String name, String value ) {
        parametersLock.writeLock().lock();
        try {
            parameters.put ( name, value );
        } finally {
            parametersLock.writeLock().unlock();
        }
        fireContainerEvent ( "addInitParameter", name );
    }
    @Override
    public void addMapping ( String mapping ) {
        mappingsLock.writeLock().lock();
        try {
            mappings.add ( mapping );
        } finally {
            mappingsLock.writeLock().unlock();
        }
        if ( parent.getState().equals ( LifecycleState.STARTED ) ) {
            fireContainerEvent ( ADD_MAPPING_EVENT, mapping );
        }
    }
    @Override
    public void addSecurityReference ( String name, String link ) {
        referencesLock.writeLock().lock();
        try {
            references.put ( name, link );
        } finally {
            referencesLock.writeLock().unlock();
        }
        fireContainerEvent ( "addSecurityReference", name );
    }
    @Override
    public Servlet allocate() throws ServletException {
        if ( unloading ) {
            throw new ServletException ( sm.getString ( "standardWrapper.unloading", getName() ) );
        }
        boolean newInstance = false;
        if ( !singleThreadModel ) {
            if ( instance == null || !instanceInitialized ) {
                synchronized ( this ) {
                    if ( instance == null ) {
                        try {
                            if ( log.isDebugEnabled() ) {
                                log.debug ( "Allocating non-STM instance" );
                            }
                            instance = loadServlet();
                            newInstance = true;
                            if ( !singleThreadModel ) {
                                countAllocated.incrementAndGet();
                            }
                        } catch ( ServletException e ) {
                            throw e;
                        } catch ( Throwable e ) {
                            ExceptionUtils.handleThrowable ( e );
                            throw new ServletException ( sm.getString ( "standardWrapper.allocate" ), e );
                        }
                    }
                    if ( !instanceInitialized ) {
                        initServlet ( instance );
                    }
                }
            }
            if ( singleThreadModel ) {
                if ( newInstance ) {
                    synchronized ( instancePool ) {
                        instancePool.push ( instance );
                        nInstances++;
                    }
                }
            } else {
                if ( log.isTraceEnabled() ) {
                    log.trace ( "  Returning non-STM instance" );
                }
                if ( !newInstance ) {
                    countAllocated.incrementAndGet();
                }
                return instance;
            }
        }
        synchronized ( instancePool ) {
            while ( countAllocated.get() >= nInstances ) {
                if ( nInstances < maxInstances ) {
                    try {
                        instancePool.push ( loadServlet() );
                        nInstances++;
                    } catch ( ServletException e ) {
                        throw e;
                    } catch ( Throwable e ) {
                        ExceptionUtils.handleThrowable ( e );
                        throw new ServletException ( sm.getString ( "standardWrapper.allocate" ), e );
                    }
                } else {
                    try {
                        instancePool.wait();
                    } catch ( InterruptedException e ) {
                    }
                }
            }
            if ( log.isTraceEnabled() ) {
                log.trace ( "  Returning allocated STM instance" );
            }
            countAllocated.incrementAndGet();
            return instancePool.pop();
        }
    }
    @Override
    public void deallocate ( Servlet servlet ) throws ServletException {
        if ( !singleThreadModel ) {
            countAllocated.decrementAndGet();
            return;
        }
        synchronized ( instancePool ) {
            countAllocated.decrementAndGet();
            instancePool.push ( servlet );
            instancePool.notify();
        }
    }
    @Override
    public String findInitParameter ( String name ) {
        parametersLock.readLock().lock();
        try {
            return parameters.get ( name );
        } finally {
            parametersLock.readLock().unlock();
        }
    }
    @Override
    public String[] findInitParameters() {
        parametersLock.readLock().lock();
        try {
            String results[] = new String[parameters.size()];
            return parameters.keySet().toArray ( results );
        } finally {
            parametersLock.readLock().unlock();
        }
    }
    @Override
    public String[] findMappings() {
        mappingsLock.readLock().lock();
        try {
            return mappings.toArray ( new String[mappings.size()] );
        } finally {
            mappingsLock.readLock().unlock();
        }
    }
    @Override
    public String findSecurityReference ( String name ) {
        referencesLock.readLock().lock();
        try {
            return references.get ( name );
        } finally {
            referencesLock.readLock().unlock();
        }
    }
    @Override
    public String[] findSecurityReferences() {
        referencesLock.readLock().lock();
        try {
            String results[] = new String[references.size()];
            return references.keySet().toArray ( results );
        } finally {
            referencesLock.readLock().unlock();
        }
    }
    @Override
    public synchronized void load() throws ServletException {
        instance = loadServlet();
        if ( !instanceInitialized ) {
            initServlet ( instance );
        }
        if ( isJspServlet ) {
            StringBuilder oname = new StringBuilder ( getDomain() );
            oname.append ( ":type=JspMonitor" );
            oname.append ( getWebModuleKeyProperties() );
            oname.append ( ",name=" );
            oname.append ( getName() );
            oname.append ( getJ2EEKeyProperties() );
            try {
                jspMonitorON = new ObjectName ( oname.toString() );
                Registry.getRegistry ( null, null )
                .registerComponent ( instance, jspMonitorON, null );
            } catch ( Exception ex ) {
                log.info ( "Error registering JSP monitoring with jmx " +
                           instance );
            }
        }
    }
    public synchronized Servlet loadServlet() throws ServletException {
        if ( !singleThreadModel && ( instance != null ) ) {
            return instance;
        }
        PrintStream out = System.out;
        if ( swallowOutput ) {
            SystemLogHandler.startCapture();
        }
        Servlet servlet;
        try {
            long t1 = System.currentTimeMillis();
            if ( servletClass == null ) {
                unavailable ( null );
                throw new ServletException
                ( sm.getString ( "standardWrapper.notClass", getName() ) );
            }
            InstanceManager instanceManager = ( ( StandardContext ) getParent() ).getInstanceManager();
            try {
                servlet = ( Servlet ) instanceManager.newInstance ( servletClass );
            } catch ( ClassCastException e ) {
                unavailable ( null );
                throw new ServletException
                ( sm.getString ( "standardWrapper.notServlet", servletClass ), e );
            } catch ( Throwable e ) {
                e = ExceptionUtils.unwrapInvocationTargetException ( e );
                ExceptionUtils.handleThrowable ( e );
                unavailable ( null );
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "standardWrapper.instantiate", servletClass ), e );
                }
                throw new ServletException
                ( sm.getString ( "standardWrapper.instantiate", servletClass ), e );
            }
            if ( multipartConfigElement == null ) {
                MultipartConfig annotation =
                    servlet.getClass().getAnnotation ( MultipartConfig.class );
                if ( annotation != null ) {
                    multipartConfigElement =
                        new MultipartConfigElement ( annotation );
                }
            }
            processServletSecurityAnnotation ( servlet.getClass() );
            if ( ( servlet instanceof ContainerServlet ) &&
                    ( isContainerProvidedServlet ( servletClass ) ||
                      ( ( Context ) getParent() ).getPrivileged() ) ) {
                ( ( ContainerServlet ) servlet ).setWrapper ( this );
            }
            classLoadTime = ( int ) ( System.currentTimeMillis() - t1 );
            if ( servlet instanceof SingleThreadModel ) {
                if ( instancePool == null ) {
                    instancePool = new Stack<>();
                }
                singleThreadModel = true;
            }
            initServlet ( servlet );
            fireContainerEvent ( "load", this );
            loadTime = System.currentTimeMillis() - t1;
        } finally {
            if ( swallowOutput ) {
                String log = SystemLogHandler.stopCapture();
                if ( log != null && log.length() > 0 ) {
                    if ( getServletContext() != null ) {
                        getServletContext().log ( log );
                    } else {
                        out.println ( log );
                    }
                }
            }
        }
        return servlet;
    }
    @Override
    public void servletSecurityAnnotationScan() throws ServletException {
        if ( getServlet() == null ) {
            Class<?> clazz = null;
            try {
                clazz = ( ( Context ) getParent() ).getLoader().getClassLoader().loadClass (
                            getServletClass() );
                processServletSecurityAnnotation ( clazz );
            } catch ( ClassNotFoundException e ) {
            }
        } else {
            if ( servletSecurityAnnotationScanRequired ) {
                processServletSecurityAnnotation ( getServlet().getClass() );
            }
        }
    }
    private void processServletSecurityAnnotation ( Class<?> clazz ) {
        servletSecurityAnnotationScanRequired = false;
        Context ctxt = ( Context ) getParent();
        if ( ctxt.getIgnoreAnnotations() ) {
            return;
        }
        ServletSecurity secAnnotation =
            clazz.getAnnotation ( ServletSecurity.class );
        if ( secAnnotation != null ) {
            ctxt.addServletSecurity (
                new ApplicationServletRegistration ( this, ctxt ),
                new ServletSecurityElement ( secAnnotation ) );
        }
    }
    private synchronized void initServlet ( Servlet servlet )
    throws ServletException {
        if ( instanceInitialized && !singleThreadModel ) {
            return;
        }
        try {
            if ( Globals.IS_SECURITY_ENABLED ) {
                boolean success = false;
                try {
                    Object[] args = new Object[] { facade };
                    SecurityUtil.doAsPrivilege ( "init",
                                                 servlet,
                                                 classType,
                                                 args );
                    success = true;
                } finally {
                    if ( !success ) {
                        SecurityUtil.remove ( servlet );
                    }
                }
            } else {
                servlet.init ( facade );
            }
            instanceInitialized = true;
        } catch ( UnavailableException f ) {
            unavailable ( f );
            throw f;
        } catch ( ServletException f ) {
            throw f;
        } catch ( Throwable f ) {
            ExceptionUtils.handleThrowable ( f );
            getServletContext().log ( "StandardWrapper.Throwable", f );
            throw new ServletException
            ( sm.getString ( "standardWrapper.initException", getName() ), f );
        }
    }
    @Override
    public void removeInitParameter ( String name ) {
        parametersLock.writeLock().lock();
        try {
            parameters.remove ( name );
        } finally {
            parametersLock.writeLock().unlock();
        }
        fireContainerEvent ( "removeInitParameter", name );
    }
    @Override
    public void removeMapping ( String mapping ) {
        mappingsLock.writeLock().lock();
        try {
            mappings.remove ( mapping );
        } finally {
            mappingsLock.writeLock().unlock();
        }
        if ( parent.getState().equals ( LifecycleState.STARTED ) ) {
            fireContainerEvent ( REMOVE_MAPPING_EVENT, mapping );
        }
    }
    @Override
    public void removeSecurityReference ( String name ) {
        referencesLock.writeLock().lock();
        try {
            references.remove ( name );
        } finally {
            referencesLock.writeLock().unlock();
        }
        fireContainerEvent ( "removeSecurityReference", name );
    }
    @Override
    public void unavailable ( UnavailableException unavailable ) {
        getServletContext().log ( sm.getString ( "standardWrapper.unavailable", getName() ) );
        if ( unavailable == null ) {
            setAvailable ( Long.MAX_VALUE );
        } else if ( unavailable.isPermanent() ) {
            setAvailable ( Long.MAX_VALUE );
        } else {
            int unavailableSeconds = unavailable.getUnavailableSeconds();
            if ( unavailableSeconds <= 0 ) {
                unavailableSeconds = 60;
            }
            setAvailable ( System.currentTimeMillis() +
                           ( unavailableSeconds * 1000L ) );
        }
    }
    @Override
    public synchronized void unload() throws ServletException {
        if ( !singleThreadModel && ( instance == null ) ) {
            return;
        }
        unloading = true;
        if ( countAllocated.get() > 0 ) {
            int nRetries = 0;
            long delay = unloadDelay / 20;
            while ( ( nRetries < 21 ) && ( countAllocated.get() > 0 ) ) {
                if ( ( nRetries % 10 ) == 0 ) {
                    log.info ( sm.getString ( "standardWrapper.waiting",
                                              countAllocated.toString(),
                                              getName() ) );
                }
                try {
                    Thread.sleep ( delay );
                } catch ( InterruptedException e ) {
                }
                nRetries++;
            }
        }
        if ( instanceInitialized ) {
            PrintStream out = System.out;
            if ( swallowOutput ) {
                SystemLogHandler.startCapture();
            }
            try {
                if ( Globals.IS_SECURITY_ENABLED ) {
                    try {
                        SecurityUtil.doAsPrivilege ( "destroy", instance );
                    } finally {
                        SecurityUtil.remove ( instance );
                    }
                } else {
                    instance.destroy();
                }
            } catch ( Throwable t ) {
                t = ExceptionUtils.unwrapInvocationTargetException ( t );
                ExceptionUtils.handleThrowable ( t );
                instance = null;
                instancePool = null;
                nInstances = 0;
                fireContainerEvent ( "unload", this );
                unloading = false;
                throw new ServletException
                ( sm.getString ( "standardWrapper.destroyException", getName() ),
                  t );
            } finally {
                if ( ! ( ( Context ) getParent() ).getIgnoreAnnotations() ) {
                    try {
                        ( ( Context ) getParent() ).getInstanceManager().destroyInstance ( instance );
                    } catch ( Throwable t ) {
                        ExceptionUtils.handleThrowable ( t );
                        log.error ( sm.getString ( "standardWrapper.destroyInstance", getName() ), t );
                    }
                }
                if ( swallowOutput ) {
                    String log = SystemLogHandler.stopCapture();
                    if ( log != null && log.length() > 0 ) {
                        if ( getServletContext() != null ) {
                            getServletContext().log ( log );
                        } else {
                            out.println ( log );
                        }
                    }
                }
            }
        }
        instance = null;
        instanceInitialized = false;
        if ( isJspServlet && jspMonitorON != null ) {
            Registry.getRegistry ( null, null ).unregisterComponent ( jspMonitorON );
        }
        if ( singleThreadModel && ( instancePool != null ) ) {
            try {
                while ( !instancePool.isEmpty() ) {
                    Servlet s = instancePool.pop();
                    if ( Globals.IS_SECURITY_ENABLED ) {
                        try {
                            SecurityUtil.doAsPrivilege ( "destroy", s );
                        } finally {
                            SecurityUtil.remove ( s );
                        }
                    } else {
                        s.destroy();
                    }
                    if ( ! ( ( Context ) getParent() ).getIgnoreAnnotations() ) {
                        ( ( StandardContext ) getParent() ).getInstanceManager().destroyInstance ( s );
                    }
                }
            } catch ( Throwable t ) {
                t = ExceptionUtils.unwrapInvocationTargetException ( t );
                ExceptionUtils.handleThrowable ( t );
                instancePool = null;
                nInstances = 0;
                unloading = false;
                fireContainerEvent ( "unload", this );
                throw new ServletException
                ( sm.getString ( "standardWrapper.destroyException",
                                 getName() ), t );
            }
            instancePool = null;
            nInstances = 0;
        }
        singleThreadModel = false;
        unloading = false;
        fireContainerEvent ( "unload", this );
    }
    @Override
    public String getInitParameter ( String name ) {
        return ( findInitParameter ( name ) );
    }
    @Override
    public Enumeration<String> getInitParameterNames() {
        parametersLock.readLock().lock();
        try {
            return Collections.enumeration ( parameters.keySet() );
        } finally {
            parametersLock.readLock().unlock();
        }
    }
    @Override
    public ServletContext getServletContext() {
        if ( parent == null ) {
            return ( null );
        } else if ( ! ( parent instanceof Context ) ) {
            return ( null );
        } else {
            return ( ( ( Context ) parent ).getServletContext() );
        }
    }
    @Override
    public String getServletName() {
        return ( getName() );
    }
    public long getProcessingTime() {
        return swValve.getProcessingTime();
    }
    public long getMaxTime() {
        return swValve.getMaxTime();
    }
    public long getMinTime() {
        return swValve.getMinTime();
    }
    public int getRequestCount() {
        return swValve.getRequestCount();
    }
    public int getErrorCount() {
        return swValve.getErrorCount();
    }
    @Override
    public void incrementErrorCount() {
        swValve.incrementErrorCount();
    }
    public long getLoadTime() {
        return loadTime;
    }
    public int getClassLoadTime() {
        return classLoadTime;
    }
    @Override
    public MultipartConfigElement getMultipartConfigElement() {
        return multipartConfigElement;
    }
    @Override
    public void setMultipartConfigElement (
        MultipartConfigElement multipartConfigElement ) {
        this.multipartConfigElement = multipartConfigElement;
    }
    @Override
    public boolean isAsyncSupported() {
        return asyncSupported;
    }
    @Override
    public void setAsyncSupported ( boolean asyncSupported ) {
        this.asyncSupported = asyncSupported;
    }
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    @Override
    public void setEnabled ( boolean enabled ) {
        this.enabled = enabled;
    }
    protected boolean isContainerProvidedServlet ( String classname ) {
        if ( classname.startsWith ( "org.apache.catalina." ) ) {
            return true;
        }
        try {
            Class<?> clazz =
                this.getClass().getClassLoader().loadClass ( classname );
            return ( ContainerServlet.class.isAssignableFrom ( clazz ) );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            return false;
        }
    }
    protected Method[] getAllDeclaredMethods ( Class<?> c ) {
        if ( c.equals ( javax.servlet.http.HttpServlet.class ) ) {
            return null;
        }
        Method[] parentMethods = getAllDeclaredMethods ( c.getSuperclass() );
        Method[] thisMethods = c.getDeclaredMethods();
        if ( thisMethods.length == 0 ) {
            return parentMethods;
        }
        if ( ( parentMethods != null ) && ( parentMethods.length > 0 ) ) {
            Method[] allMethods =
                new Method[parentMethods.length + thisMethods.length];
            System.arraycopy ( parentMethods, 0, allMethods, 0,
                               parentMethods.length );
            System.arraycopy ( thisMethods, 0, allMethods, parentMethods.length,
                               thisMethods.length );
            thisMethods = allMethods;
        }
        return thisMethods;
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        if ( this.getObjectName() != null ) {
            Notification notification = new Notification ( "j2ee.state.starting",
                    this.getObjectName(),
                    sequenceNumber++ );
            broadcaster.sendNotification ( notification );
        }
        super.startInternal();
        setAvailable ( 0L );
        if ( this.getObjectName() != null ) {
            Notification notification =
                new Notification ( "j2ee.state.running", this.getObjectName(),
                                   sequenceNumber++ );
            broadcaster.sendNotification ( notification );
        }
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        setAvailable ( Long.MAX_VALUE );
        if ( this.getObjectName() != null ) {
            Notification notification =
                new Notification ( "j2ee.state.stopping", this.getObjectName(),
                                   sequenceNumber++ );
            broadcaster.sendNotification ( notification );
        }
        try {
            unload();
        } catch ( ServletException e ) {
            getServletContext().log ( sm.getString
                                      ( "standardWrapper.unloadException", getName() ), e );
        }
        super.stopInternal();
        if ( this.getObjectName() != null ) {
            Notification notification =
                new Notification ( "j2ee.state.stopped", this.getObjectName(),
                                   sequenceNumber++ );
            broadcaster.sendNotification ( notification );
        }
        Notification notification =
            new Notification ( "j2ee.object.deleted", this.getObjectName(),
                               sequenceNumber++ );
        broadcaster.sendNotification ( notification );
    }
    @Override
    protected String getObjectNameKeyProperties() {
        StringBuilder keyProperties =
            new StringBuilder ( "j2eeType=Servlet" );
        keyProperties.append ( getWebModuleKeyProperties() );
        keyProperties.append ( ",name=" );
        String name = getName();
        if ( Util.objectNameValueNeedsQuote ( name ) ) {
            name = ObjectName.quote ( name );
        }
        keyProperties.append ( name );
        keyProperties.append ( getJ2EEKeyProperties() );
        return keyProperties.toString();
    }
    private String getWebModuleKeyProperties() {
        StringBuilder keyProperties = new StringBuilder ( ",WebModule=//" );
        String hostName = getParent().getParent().getName();
        if ( hostName == null ) {
            keyProperties.append ( "DEFAULT" );
        } else {
            keyProperties.append ( hostName );
        }
        String contextName = ( ( Context ) getParent() ).getName();
        if ( !contextName.startsWith ( "/" ) ) {
            keyProperties.append ( '/' );
        }
        keyProperties.append ( contextName );
        return keyProperties.toString();
    }
    private String getJ2EEKeyProperties() {
        StringBuilder keyProperties = new StringBuilder ( ",J2EEApplication=" );
        StandardContext ctx = null;
        if ( parent instanceof StandardContext ) {
            ctx = ( StandardContext ) getParent();
        }
        if ( ctx == null ) {
            keyProperties.append ( "none" );
        } else {
            keyProperties.append ( ctx.getJ2EEApplication() );
        }
        keyProperties.append ( ",J2EEServer=" );
        if ( ctx == null ) {
            keyProperties.append ( "none" );
        } else {
            keyProperties.append ( ctx.getJ2EEServer() );
        }
        return keyProperties.toString();
    }
    @Override
    public void removeNotificationListener ( NotificationListener listener,
            NotificationFilter filter, Object object ) throws ListenerNotFoundException {
        broadcaster.removeNotificationListener ( listener, filter, object );
    }
    protected MBeanNotificationInfo[] notificationInfo;
    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        if ( notificationInfo == null ) {
            notificationInfo = new MBeanNotificationInfo[] {
                new MBeanNotificationInfo ( new String[] {
                                                "j2ee.object.created"
                                            },
                                            Notification.class.getName(),
                                            "servlet is created"
                                          ),
                new MBeanNotificationInfo ( new String[] {
                                                "j2ee.state.starting"
                                            },
                                            Notification.class.getName(),
                                            "servlet is starting"
                                          ),
                new MBeanNotificationInfo ( new String[] {
                                                "j2ee.state.running"
                                            },
                                            Notification.class.getName(),
                                            "servlet is running"
                                          ),
                new MBeanNotificationInfo ( new String[] {
                                                "j2ee.state.stopped"
                                            },
                                            Notification.class.getName(),
                                            "servlet start to stopped"
                                          ),
                new MBeanNotificationInfo ( new String[] {
                                                "j2ee.object.stopped"
                                            },
                                            Notification.class.getName(),
                                            "servlet is stopped"
                                          ),
                new MBeanNotificationInfo ( new String[] {
                                                "j2ee.object.deleted"
                                            },
                                            Notification.class.getName(),
                                            "servlet is deleted"
                                          )
            };
        }
        return notificationInfo;
    }
    @Override
    public void addNotificationListener ( NotificationListener listener,
                                          NotificationFilter filter, Object object ) throws IllegalArgumentException {
        broadcaster.addNotificationListener ( listener, filter, object );
    }
    @Override
    public void removeNotificationListener ( NotificationListener listener )
    throws ListenerNotFoundException {
        broadcaster.removeNotificationListener ( listener );
    }
}
