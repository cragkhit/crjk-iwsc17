package org.apache.catalina.loader;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import org.apache.catalina.Container;
import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.apache.juli.WebappProperties;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.InstrumentableClassLoader;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.compat.JreCompat;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.security.PermissionCheck;
public abstract class WebappClassLoaderBase extends URLClassLoader
    implements Lifecycle, InstrumentableClassLoader, WebappProperties, PermissionCheck {
    private static final Log log = LogFactory.getLog ( WebappClassLoaderBase.class );
    private static final List<String> JVM_THREAD_GROUP_NAMES = new ArrayList<>();
    private static final String JVM_THREAD_GROUP_SYSTEM = "system";
    private static final String CLASS_FILE_SUFFIX = ".class";
    static {
        ClassLoader.registerAsParallelCapable();
        JVM_THREAD_GROUP_NAMES.add ( JVM_THREAD_GROUP_SYSTEM );
        JVM_THREAD_GROUP_NAMES.add ( "RMI Runtime" );
    }
    protected class PrivilegedFindClassByName
        implements PrivilegedAction<Class<?>> {
        protected final String name;
        PrivilegedFindClassByName ( String name ) {
            this.name = name;
        }
        @Override
        public Class<?> run() {
            return findClassInternal ( name );
        }
    }
    protected static final class PrivilegedGetClassLoader
        implements PrivilegedAction<ClassLoader> {
        public final Class<?> clazz;
        public PrivilegedGetClassLoader ( Class<?> clazz ) {
            this.clazz = clazz;
        }
        @Override
        public ClassLoader run() {
            return clazz.getClassLoader();
        }
    }
    protected static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    protected WebappClassLoaderBase() {
        super ( new URL[0] );
        ClassLoader p = getParent();
        if ( p == null ) {
            p = getSystemClassLoader();
        }
        this.parent = p;
        ClassLoader j = String.class.getClassLoader();
        if ( j == null ) {
            j = getSystemClassLoader();
            while ( j.getParent() != null ) {
                j = j.getParent();
            }
        }
        this.javaseClassLoader = j;
        securityManager = System.getSecurityManager();
        if ( securityManager != null ) {
            refreshPolicy();
        }
    }
    protected WebappClassLoaderBase ( ClassLoader parent ) {
        super ( new URL[0], parent );
        ClassLoader p = getParent();
        if ( p == null ) {
            p = getSystemClassLoader();
        }
        this.parent = p;
        ClassLoader j = String.class.getClassLoader();
        if ( j == null ) {
            j = getSystemClassLoader();
            while ( j.getParent() != null ) {
                j = j.getParent();
            }
        }
        this.javaseClassLoader = j;
        securityManager = System.getSecurityManager();
        if ( securityManager != null ) {
            refreshPolicy();
        }
    }
    protected WebResourceRoot resources = null;
    protected final Map<String, ResourceEntry> resourceEntries =
        new ConcurrentHashMap<>();
    protected boolean delegate = false;
    private final HashMap<String, Long> jarModificationTimes = new HashMap<>();
    protected final ArrayList<Permission> permissionList = new ArrayList<>();
    protected final HashMap<String, PermissionCollection> loaderPC = new HashMap<>();
    protected final SecurityManager securityManager;
    protected final ClassLoader parent;
    private ClassLoader javaseClassLoader;
    private boolean clearReferencesRmiTargets = true;
    private boolean clearReferencesStopThreads = false;
    private boolean clearReferencesStopTimerThreads = false;
    private boolean clearReferencesLogFactoryRelease = true;
    private boolean clearReferencesHttpClientKeepAliveThread = true;
    private final List<ClassFileTransformer> transformers = new CopyOnWriteArrayList<>();
    private boolean hasExternalRepositories = false;
    private List<URL> localRepositories = new ArrayList<>();
    private volatile LifecycleState state = LifecycleState.NEW;
    public WebResourceRoot getResources() {
        return this.resources;
    }
    public void setResources ( WebResourceRoot resources ) {
        this.resources = resources;
    }
    public String getContextName() {
        if ( resources == null ) {
            return "Unknown";
        } else {
            return resources.getContext().getBaseName();
        }
    }
    public boolean getDelegate() {
        return ( this.delegate );
    }
    public void setDelegate ( boolean delegate ) {
        this.delegate = delegate;
    }
    void addPermission ( URL url ) {
        if ( url == null ) {
            return;
        }
        if ( securityManager != null ) {
            String protocol = url.getProtocol();
            if ( "file".equalsIgnoreCase ( protocol ) ) {
                URI uri;
                File f;
                String path;
                try {
                    uri = url.toURI();
                    f = new File ( uri );
                    path = f.getCanonicalPath();
                } catch ( IOException | URISyntaxException e ) {
                    log.warn ( sm.getString (
                                   "webappClassLoader.addPermisionNoCanonicalFile",
                                   url.toExternalForm() ) );
                    return;
                }
                if ( f.isFile() ) {
                    addPermission ( new FilePermission ( path, "read" ) );
                } else if ( f.isDirectory() ) {
                    addPermission ( new FilePermission ( path, "read" ) );
                    addPermission ( new FilePermission (
                                        path + File.separator + "-", "read" ) );
                } else {
                }
            } else {
                log.warn ( sm.getString (
                               "webappClassLoader.addPermisionNoProtocol",
                               protocol, url.toExternalForm() ) );
            }
        }
    }
    void addPermission ( Permission permission ) {
        if ( ( securityManager != null ) && ( permission != null ) ) {
            permissionList.add ( permission );
        }
    }
    public boolean getClearReferencesRmiTargets() {
        return this.clearReferencesRmiTargets;
    }
    public void setClearReferencesRmiTargets ( boolean clearReferencesRmiTargets ) {
        this.clearReferencesRmiTargets = clearReferencesRmiTargets;
    }
    public boolean getClearReferencesStopThreads() {
        return ( this.clearReferencesStopThreads );
    }
    public void setClearReferencesStopThreads (
        boolean clearReferencesStopThreads ) {
        this.clearReferencesStopThreads = clearReferencesStopThreads;
    }
    public boolean getClearReferencesStopTimerThreads() {
        return ( this.clearReferencesStopTimerThreads );
    }
    public void setClearReferencesStopTimerThreads (
        boolean clearReferencesStopTimerThreads ) {
        this.clearReferencesStopTimerThreads = clearReferencesStopTimerThreads;
    }
    public boolean getClearReferencesLogFactoryRelease() {
        return ( this.clearReferencesLogFactoryRelease );
    }
    public void setClearReferencesLogFactoryRelease (
        boolean clearReferencesLogFactoryRelease ) {
        this.clearReferencesLogFactoryRelease =
            clearReferencesLogFactoryRelease;
    }
    public boolean getClearReferencesHttpClientKeepAliveThread() {
        return ( this.clearReferencesHttpClientKeepAliveThread );
    }
    public void setClearReferencesHttpClientKeepAliveThread (
        boolean clearReferencesHttpClientKeepAliveThread ) {
        this.clearReferencesHttpClientKeepAliveThread =
            clearReferencesHttpClientKeepAliveThread;
    }
    @Override
    public void addTransformer ( ClassFileTransformer transformer ) {
        if ( transformer == null ) {
            throw new IllegalArgumentException ( sm.getString (
                    "webappClassLoader.addTransformer.illegalArgument", getContextName() ) );
        }
        if ( this.transformers.contains ( transformer ) ) {
            log.warn ( sm.getString ( "webappClassLoader.addTransformer.duplicate",
                                      transformer, getContextName() ) );
            return;
        }
        this.transformers.add ( transformer );
        log.info ( sm.getString ( "webappClassLoader.addTransformer", transformer, getContextName() ) );
    }
    @Override
    public void removeTransformer ( ClassFileTransformer transformer ) {
        if ( transformer == null ) {
            return;
        }
        if ( this.transformers.remove ( transformer ) ) {
            log.info ( sm.getString ( "webappClassLoader.removeTransformer",
                                      transformer, getContextName() ) );
            return;
        }
    }
    protected void copyStateWithoutTransformers ( WebappClassLoaderBase base ) {
        base.resources = this.resources;
        base.delegate = this.delegate;
        base.state = LifecycleState.NEW;
        base.clearReferencesStopThreads = this.clearReferencesStopThreads;
        base.clearReferencesStopTimerThreads = this.clearReferencesStopTimerThreads;
        base.clearReferencesLogFactoryRelease = this.clearReferencesLogFactoryRelease;
        base.clearReferencesHttpClientKeepAliveThread = this.clearReferencesHttpClientKeepAliveThread;
        base.jarModificationTimes.putAll ( this.jarModificationTimes );
        base.permissionList.addAll ( this.permissionList );
        base.loaderPC.putAll ( this.loaderPC );
    }
    public boolean modified() {
        if ( log.isDebugEnabled() ) {
            log.debug ( "modified()" );
        }
        for ( Entry<String, ResourceEntry> entry : resourceEntries.entrySet() ) {
            long cachedLastModified = entry.getValue().lastModified;
            long lastModified = resources.getClassLoaderResource (
                                    entry.getKey() ).getLastModified();
            if ( lastModified != cachedLastModified ) {
                if ( log.isDebugEnabled() )
                    log.debug ( sm.getString ( "webappClassLoader.resourceModified",
                                               entry.getKey(),
                                               new Date ( cachedLastModified ),
                                               new Date ( lastModified ) ) );
                return true;
            }
        }
        WebResource[] jars = resources.listResources ( "/WEB-INF/lib" );
        int jarCount = 0;
        for ( WebResource jar : jars ) {
            if ( jar.getName().endsWith ( ".jar" ) && jar.isFile() && jar.canRead() ) {
                jarCount++;
                Long recordedLastModified = jarModificationTimes.get ( jar.getName() );
                if ( recordedLastModified == null ) {
                    log.info ( sm.getString ( "webappClassLoader.jarsAdded",
                                              resources.getContext().getName() ) );
                    return true;
                }
                if ( recordedLastModified.longValue() != jar.getLastModified() ) {
                    log.info ( sm.getString ( "webappClassLoader.jarsModified",
                                              resources.getContext().getName() ) );
                    return true;
                }
            }
        }
        if ( jarCount < jarModificationTimes.size() ) {
            log.info ( sm.getString ( "webappClassLoader.jarsRemoved",
                                      resources.getContext().getName() ) );
            return true;
        }
        return false;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( this.getClass().getSimpleName() );
        sb.append ( "\r\n  context: " );
        sb.append ( getContextName() );
        sb.append ( "\r\n  delegate: " );
        sb.append ( delegate );
        sb.append ( "\r\n" );
        if ( this.parent != null ) {
            sb.append ( "----------> Parent Classloader:\r\n" );
            sb.append ( this.parent.toString() );
            sb.append ( "\r\n" );
        }
        if ( this.transformers.size() > 0 ) {
            sb.append ( "----------> Class file transformers:\r\n" );
            for ( ClassFileTransformer transformer : this.transformers ) {
                sb.append ( transformer ).append ( "\r\n" );
            }
        }
        return ( sb.toString() );
    }
    protected final Class<?> doDefineClass ( String name, byte[] b, int off, int len,
            ProtectionDomain protectionDomain ) {
        return super.defineClass ( name, b, off, len, protectionDomain );
    }
    @Override
    public Class<?> findClass ( String name ) throws ClassNotFoundException {
        if ( log.isDebugEnabled() ) {
            log.debug ( "    findClass(" + name + ")" );
        }
        checkStateForClassLoading ( name );
        if ( securityManager != null ) {
            int i = name.lastIndexOf ( '.' );
            if ( i >= 0 ) {
                try {
                    if ( log.isTraceEnabled() ) {
                        log.trace ( "      securityManager.checkPackageDefinition" );
                    }
                    securityManager.checkPackageDefinition ( name.substring ( 0, i ) );
                } catch ( Exception se ) {
                    if ( log.isTraceEnabled() ) {
                        log.trace ( "      -->Exception-->ClassNotFoundException", se );
                    }
                    throw new ClassNotFoundException ( name, se );
                }
            }
        }
        Class<?> clazz = null;
        try {
            if ( log.isTraceEnabled() ) {
                log.trace ( "      findClassInternal(" + name + ")" );
            }
            try {
                if ( securityManager != null ) {
                    PrivilegedAction<Class<?>> dp =
                        new PrivilegedFindClassByName ( name );
                    clazz = AccessController.doPrivileged ( dp );
                } else {
                    clazz = findClassInternal ( name );
                }
            } catch ( AccessControlException ace ) {
                log.warn ( "WebappClassLoader.findClassInternal(" + name
                           + ") security exception: " + ace.getMessage(), ace );
                throw new ClassNotFoundException ( name, ace );
            } catch ( RuntimeException e ) {
                if ( log.isTraceEnabled() ) {
                    log.trace ( "      -->RuntimeException Rethrown", e );
                }
                throw e;
            }
            if ( ( clazz == null ) && hasExternalRepositories ) {
                try {
                    clazz = super.findClass ( name );
                } catch ( AccessControlException ace ) {
                    log.warn ( "WebappClassLoader.findClassInternal(" + name
                               + ") security exception: " + ace.getMessage(), ace );
                    throw new ClassNotFoundException ( name, ace );
                } catch ( RuntimeException e ) {
                    if ( log.isTraceEnabled() ) {
                        log.trace ( "      -->RuntimeException Rethrown", e );
                    }
                    throw e;
                }
            }
            if ( clazz == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "    --> Returning ClassNotFoundException" );
                }
                throw new ClassNotFoundException ( name );
            }
        } catch ( ClassNotFoundException e ) {
            if ( log.isTraceEnabled() ) {
                log.trace ( "    --> Passing on ClassNotFoundException" );
            }
            throw e;
        }
        if ( log.isTraceEnabled() ) {
            log.debug ( "      Returning class " + clazz );
        }
        if ( log.isTraceEnabled() ) {
            ClassLoader cl;
            if ( Globals.IS_SECURITY_ENABLED ) {
                cl = AccessController.doPrivileged (
                         new PrivilegedGetClassLoader ( clazz ) );
            } else {
                cl = clazz.getClassLoader();
            }
            log.debug ( "      Loaded by " + cl.toString() );
        }
        return ( clazz );
    }
    @Override
    public URL findResource ( final String name ) {
        if ( log.isDebugEnabled() ) {
            log.debug ( "    findResource(" + name + ")" );
        }
        checkStateForResourceLoading ( name );
        URL url = null;
        String path = nameToPath ( name );
        WebResource resource = resources.getClassLoaderResource ( path );
        if ( resource.exists() ) {
            url = resource.getURL();
            trackLastModified ( path, resource );
        }
        if ( ( url == null ) && hasExternalRepositories ) {
            url = super.findResource ( name );
        }
        if ( log.isDebugEnabled() ) {
            if ( url != null ) {
                log.debug ( "    --> Returning '" + url.toString() + "'" );
            } else {
                log.debug ( "    --> Resource not found, returning null" );
            }
        }
        return url;
    }
    private void trackLastModified ( String path, WebResource resource ) {
        if ( resourceEntries.containsKey ( path ) ) {
            return;
        }
        ResourceEntry entry = new ResourceEntry();
        entry.lastModified = resource.getLastModified();
        synchronized ( resourceEntries ) {
            resourceEntries.putIfAbsent ( path, entry );
        }
    }
    @Override
    public Enumeration<URL> findResources ( String name ) throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug ( "    findResources(" + name + ")" );
        }
        checkStateForResourceLoading ( name );
        LinkedHashSet<URL> result = new LinkedHashSet<>();
        String path = nameToPath ( name );
        WebResource[] webResources = resources.getClassLoaderResources ( path );
        for ( WebResource webResource : webResources ) {
            if ( webResource.exists() ) {
                result.add ( webResource.getURL() );
            }
        }
        if ( hasExternalRepositories ) {
            Enumeration<URL> otherResourcePaths = super.findResources ( name );
            while ( otherResourcePaths.hasMoreElements() ) {
                result.add ( otherResourcePaths.nextElement() );
            }
        }
        return Collections.enumeration ( result );
    }
    @Override
    public URL getResource ( String name ) {
        if ( log.isDebugEnabled() ) {
            log.debug ( "getResource(" + name + ")" );
        }
        checkStateForResourceLoading ( name );
        URL url = null;
        boolean delegateFirst = delegate || filter ( name, false );
        if ( delegateFirst ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "  Delegating to parent classloader " + parent );
            }
            url = parent.getResource ( name );
            if ( url != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "  --> Returning '" + url.toString() + "'" );
                }
                return ( url );
            }
        }
        url = findResource ( name );
        if ( url != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "  --> Returning '" + url.toString() + "'" );
            }
            return ( url );
        }
        if ( !delegateFirst ) {
            url = parent.getResource ( name );
            if ( url != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "  --> Returning '" + url.toString() + "'" );
                }
                return ( url );
            }
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( "  --> Resource not found, returning null" );
        }
        return ( null );
    }
    @Override
    public InputStream getResourceAsStream ( String name ) {
        if ( log.isDebugEnabled() ) {
            log.debug ( "getResourceAsStream(" + name + ")" );
        }
        checkStateForResourceLoading ( name );
        InputStream stream = null;
        boolean delegateFirst = delegate || filter ( name, false );
        if ( delegateFirst ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "  Delegating to parent classloader " + parent );
            }
            stream = parent.getResourceAsStream ( name );
            if ( stream != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "  --> Returning stream from parent" );
                }
                return stream;
            }
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( "  Searching local repositories" );
        }
        String path = nameToPath ( name );
        WebResource resource = resources.getClassLoaderResource ( path );
        if ( resource.exists() ) {
            stream = resource.getInputStream();
            trackLastModified ( path, resource );
        }
        try {
            if ( hasExternalRepositories && stream == null ) {
                URL url = super.findResource ( name );
                if ( url != null ) {
                    stream = url.openStream();
                }
            }
        } catch ( IOException e ) {
        }
        if ( stream != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "  --> Returning stream from local" );
            }
            return stream;
        }
        if ( !delegateFirst ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "  Delegating to parent classloader unconditionally " + parent );
            }
            stream = parent.getResourceAsStream ( name );
            if ( stream != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "  --> Returning stream from parent" );
                }
                return stream;
            }
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( "  --> Resource not found, returning null" );
        }
        return null;
    }
    @Override
    public Class<?> loadClass ( String name ) throws ClassNotFoundException {
        return ( loadClass ( name, false ) );
    }
    @Override
    public Class<?> loadClass ( String name, boolean resolve ) throws ClassNotFoundException {
        synchronized ( getClassLoadingLock ( name ) ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "loadClass(" + name + ", " + resolve + ")" );
            }
            Class<?> clazz = null;
            checkStateForClassLoading ( name );
            clazz = findLoadedClass0 ( name );
            if ( clazz != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "  Returning class from cache" );
                }
                if ( resolve ) {
                    resolveClass ( clazz );
                }
                return ( clazz );
            }
            clazz = findLoadedClass ( name );
            if ( clazz != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "  Returning class from cache" );
                }
                if ( resolve ) {
                    resolveClass ( clazz );
                }
                return ( clazz );
            }
            String resourceName = binaryNameToPath ( name, false );
            ClassLoader javaseLoader = getJavaseClassLoader();
            boolean tryLoadingFromJavaseLoader;
            try {
                tryLoadingFromJavaseLoader = ( javaseLoader.getResource ( resourceName ) != null );
            } catch ( ClassCircularityError cce ) {
                tryLoadingFromJavaseLoader = true;
            }
            if ( tryLoadingFromJavaseLoader ) {
                try {
                    clazz = javaseLoader.loadClass ( name );
                    if ( clazz != null ) {
                        if ( resolve ) {
                            resolveClass ( clazz );
                        }
                        return ( clazz );
                    }
                } catch ( ClassNotFoundException e ) {
                }
            }
            if ( securityManager != null ) {
                int i = name.lastIndexOf ( '.' );
                if ( i >= 0 ) {
                    try {
                        securityManager.checkPackageAccess ( name.substring ( 0, i ) );
                    } catch ( SecurityException se ) {
                        String error = "Security Violation, attempt to use " +
                                       "Restricted Class: " + name;
                        log.info ( error, se );
                        throw new ClassNotFoundException ( error, se );
                    }
                }
            }
            boolean delegateLoad = delegate || filter ( name, true );
            if ( delegateLoad ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "  Delegating to parent classloader1 " + parent );
                }
                try {
                    clazz = Class.forName ( name, false, parent );
                    if ( clazz != null ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug ( "  Loading class from parent" );
                        }
                        if ( resolve ) {
                            resolveClass ( clazz );
                        }
                        return ( clazz );
                    }
                } catch ( ClassNotFoundException e ) {
                }
            }
            if ( log.isDebugEnabled() ) {
                log.debug ( "  Searching local repositories" );
            }
            try {
                clazz = findClass ( name );
                if ( clazz != null ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( "  Loading class from local repository" );
                    }
                    if ( resolve ) {
                        resolveClass ( clazz );
                    }
                    return ( clazz );
                }
            } catch ( ClassNotFoundException e ) {
            }
            if ( !delegateLoad ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "  Delegating to parent classloader at end: " + parent );
                }
                try {
                    clazz = Class.forName ( name, false, parent );
                    if ( clazz != null ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug ( "  Loading class from parent" );
                        }
                        if ( resolve ) {
                            resolveClass ( clazz );
                        }
                        return ( clazz );
                    }
                } catch ( ClassNotFoundException e ) {
                }
            }
        }
        throw new ClassNotFoundException ( name );
    }
    protected void checkStateForClassLoading ( String className ) throws ClassNotFoundException {
        try {
            checkStateForResourceLoading ( className );
        } catch ( IllegalStateException ise ) {
            throw new ClassNotFoundException ( ise.getMessage(), ise );
        }
    }
    protected void checkStateForResourceLoading ( String resource ) throws IllegalStateException {
        if ( !state.isAvailable() ) {
            String msg = sm.getString ( "webappClassLoader.stopped", resource );
            IllegalStateException ise = new IllegalStateException ( msg );
            log.info ( msg, ise );
            throw ise;
        }
    }
    @Override
    protected PermissionCollection getPermissions ( CodeSource codeSource ) {
        String codeUrl = codeSource.getLocation().toString();
        PermissionCollection pc;
        if ( ( pc = loaderPC.get ( codeUrl ) ) == null ) {
            pc = super.getPermissions ( codeSource );
            if ( pc != null ) {
                Iterator<Permission> perms = permissionList.iterator();
                while ( perms.hasNext() ) {
                    Permission p = perms.next();
                    pc.add ( p );
                }
                loaderPC.put ( codeUrl, pc );
            }
        }
        return ( pc );
    }
    @Override
    public boolean check ( Permission permission ) {
        if ( !Globals.IS_SECURITY_ENABLED ) {
            return true;
        }
        Policy currentPolicy = Policy.getPolicy();
        if ( currentPolicy != null ) {
            URL contextRootUrl = resources.getResource ( "/" ).getCodeBase();
            CodeSource cs = new CodeSource ( contextRootUrl, ( Certificate[] ) null );
            PermissionCollection pc = currentPolicy.getPermissions ( cs );
            if ( pc.implies ( permission ) ) {
                return true;
            }
        }
        return false;
    }
    @Override
    public URL[] getURLs() {
        ArrayList<URL> result = new ArrayList<>();
        result.addAll ( localRepositories );
        result.addAll ( Arrays.asList ( super.getURLs() ) );
        return result.toArray ( new URL[result.size()] );
    }
    @Override
    public void addLifecycleListener ( LifecycleListener listener ) {
    }
    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return new LifecycleListener[0];
    }
    @Override
    public void removeLifecycleListener ( LifecycleListener listener ) {
    }
    @Override
    public LifecycleState getState() {
        return state;
    }
    @Override
    public String getStateName() {
        return getState().toString();
    }
    @Override
    public void init() {
        state = LifecycleState.INITIALIZED;
    }
    @Override
    public void start() throws LifecycleException {
        state = LifecycleState.STARTING_PREP;
        WebResource classes = resources.getResource ( "/WEB-INF/classes" );
        if ( classes.isDirectory() && classes.canRead() ) {
            localRepositories.add ( classes.getURL() );
        }
        WebResource[] jars = resources.listResources ( "/WEB-INF/lib" );
        for ( WebResource jar : jars ) {
            if ( jar.getName().endsWith ( ".jar" ) && jar.isFile() && jar.canRead() ) {
                localRepositories.add ( jar.getURL() );
                jarModificationTimes.put (
                    jar.getName(), Long.valueOf ( jar.getLastModified() ) );
            }
        }
        state = LifecycleState.STARTED;
    }
    @Override
    public void stop() throws LifecycleException {
        state = LifecycleState.STOPPING_PREP;
        clearReferences();
        state = LifecycleState.STOPPING;
        resourceEntries.clear();
        jarModificationTimes.clear();
        resources = null;
        permissionList.clear();
        loaderPC.clear();
        state = LifecycleState.STOPPED;
    }
    @Override
    public void destroy() {
        state = LifecycleState.DESTROYING;
        try {
            super.close();
        } catch ( IOException ioe ) {
            log.warn ( sm.getString ( "webappClassLoader.superCloseFail" ), ioe );
        }
        state = LifecycleState.DESTROYED;
    }
    protected ClassLoader getJavaseClassLoader() {
        return javaseClassLoader;
    }
    protected void setJavaseClassLoader ( ClassLoader classLoader ) {
        if ( classLoader == null ) {
            throw new IllegalArgumentException (
                sm.getString ( "webappClassLoader.javaseClassLoaderNull" ) );
        }
        javaseClassLoader = classLoader;
    }
    protected void clearReferences() {
        clearReferencesJdbc();
        clearReferencesThreads();
        checkThreadLocalsForLeaks();
        if ( clearReferencesRmiTargets ) {
            clearReferencesRmiTargets();
        }
        IntrospectionUtils.clear();
        if ( clearReferencesLogFactoryRelease ) {
            org.apache.juli.logging.LogFactory.release ( this );
        }
        java.beans.Introspector.flushCaches();
        TomcatURLStreamHandlerFactory.release ( this );
    }
    private final void clearReferencesJdbc() {
        byte[] classBytes = new byte[2048];
        int offset = 0;
        try ( InputStream is = getResourceAsStream (
                                       "org/apache/catalina/loader/JdbcLeakPrevention.class" ) ) {
            int read = is.read ( classBytes, offset, classBytes.length - offset );
            while ( read > -1 ) {
                offset += read;
                if ( offset == classBytes.length ) {
                    byte[] tmp = new byte[classBytes.length * 2];
                    System.arraycopy ( classBytes, 0, tmp, 0, classBytes.length );
                    classBytes = tmp;
                }
                read = is.read ( classBytes, offset, classBytes.length - offset );
            }
            Class<?> lpClass =
                defineClass ( "org.apache.catalina.loader.JdbcLeakPrevention",
                              classBytes, 0, offset, this.getClass().getProtectionDomain() );
            Object obj = lpClass.newInstance();
            @SuppressWarnings ( "unchecked" )
            List<String> driverNames = ( List<String> ) obj.getClass().getMethod (
                                           "clearJdbcDriverRegistrations" ).invoke ( obj );
            for ( String name : driverNames ) {
                log.warn ( sm.getString ( "webappClassLoader.clearJdbc",
                                          getContextName(), name ) );
            }
        } catch ( Exception e ) {
            Throwable t = ExceptionUtils.unwrapInvocationTargetException ( e );
            ExceptionUtils.handleThrowable ( t );
            log.warn ( sm.getString (
                           "webappClassLoader.jdbcRemoveFailed", getContextName() ), t );
        }
    }
    @SuppressWarnings ( "deprecation" )
    private void clearReferencesThreads() {
        Thread[] threads = getThreads();
        List<Thread> executorThreadsToStop = new ArrayList<>();
        for ( Thread thread : threads ) {
            if ( thread != null ) {
                ClassLoader ccl = thread.getContextClassLoader();
                if ( ccl == this ) {
                    if ( thread == Thread.currentThread() ) {
                        continue;
                    }
                    final String threadName = thread.getName();
                    ThreadGroup tg = thread.getThreadGroup();
                    if ( tg != null && JVM_THREAD_GROUP_NAMES.contains ( tg.getName() ) ) {
                        if ( clearReferencesHttpClientKeepAliveThread &&
                                threadName.equals ( "Keep-Alive-Timer" ) ) {
                            thread.setContextClassLoader ( parent );
                            log.debug ( sm.getString ( "webappClassLoader.checkThreadsHttpClient" ) );
                        }
                        continue;
                    }
                    if ( !thread.isAlive() ) {
                        continue;
                    }
                    if ( thread.getClass().getName().startsWith ( "java.util.Timer" ) &&
                            clearReferencesStopTimerThreads ) {
                        clearReferencesStopTimerThread ( thread );
                        continue;
                    }
                    if ( isRequestThread ( thread ) ) {
                        log.warn ( sm.getString ( "webappClassLoader.stackTraceRequestThread",
                                                  getContextName(), threadName, getStackTrace ( thread ) ) );
                    } else {
                        log.warn ( sm.getString ( "webappClassLoader.stackTrace",
                                                  getContextName(), threadName, getStackTrace ( thread ) ) );
                    }
                    if ( !clearReferencesStopThreads ) {
                        continue;
                    }
                    boolean usingExecutor = false;
                    try {
                        Object target = null;
                        for ( String fieldName : new String[] { "target", "runnable", "action" } ) {
                            try {
                                Field targetField = thread.getClass().getDeclaredField ( fieldName );
                                targetField.setAccessible ( true );
                                target = targetField.get ( thread );
                                break;
                            } catch ( NoSuchFieldException nfe ) {
                                continue;
                            }
                        }
                        if ( target != null && target.getClass().getCanonicalName() != null &&
                                target.getClass().getCanonicalName().equals (
                                    "java.util.concurrent.ThreadPoolExecutor.Worker" ) ) {
                            Field executorField = target.getClass().getDeclaredField ( "this$0" );
                            executorField.setAccessible ( true );
                            Object executor = executorField.get ( target );
                            if ( executor instanceof ThreadPoolExecutor ) {
                                ( ( ThreadPoolExecutor ) executor ).shutdownNow();
                                usingExecutor = true;
                            }
                        }
                    } catch ( SecurityException | NoSuchFieldException | IllegalArgumentException |
                                  IllegalAccessException e ) {
                        log.warn ( sm.getString ( "webappClassLoader.stopThreadFail",
                                                  thread.getName(), getContextName() ), e );
                    }
                    if ( usingExecutor ) {
                        executorThreadsToStop.add ( thread );
                    } else {
                        thread.stop();
                    }
                }
            }
        }
        int count = 0;
        for ( Thread t : executorThreadsToStop ) {
            while ( t.isAlive() && count < 100 ) {
                try {
                    Thread.sleep ( 20 );
                } catch ( InterruptedException e ) {
                    break;
                }
                count++;
            }
            if ( t.isAlive() ) {
                t.stop();
            }
        }
    }
    private boolean isRequestThread ( Thread thread ) {
        StackTraceElement[] elements = thread.getStackTrace();
        if ( elements == null || elements.length == 0 ) {
            return false;
        }
        for ( int i = 0; i < elements.length; i++ ) {
            StackTraceElement element = elements[elements.length - ( i + 1 )];
            if ( "org.apache.catalina.connector.CoyoteAdapter".equals (
                        element.getClassName() ) ) {
                return true;
            }
        }
        return false;
    }
    private void clearReferencesStopTimerThread ( Thread thread ) {
        try {
            try {
                Field newTasksMayBeScheduledField =
                    thread.getClass().getDeclaredField ( "newTasksMayBeScheduled" );
                newTasksMayBeScheduledField.setAccessible ( true );
                Field queueField = thread.getClass().getDeclaredField ( "queue" );
                queueField.setAccessible ( true );
                Object queue = queueField.get ( thread );
                Method clearMethod = queue.getClass().getDeclaredMethod ( "clear" );
                clearMethod.setAccessible ( true );
                synchronized ( queue ) {
                    newTasksMayBeScheduledField.setBoolean ( thread, false );
                    clearMethod.invoke ( queue );
                    queue.notify();
                }
            } catch ( NoSuchFieldException nfe ) {
                Method cancelMethod = thread.getClass().getDeclaredMethod ( "cancel" );
                synchronized ( thread ) {
                    cancelMethod.setAccessible ( true );
                    cancelMethod.invoke ( thread );
                }
            }
            log.warn ( sm.getString ( "webappClassLoader.warnTimerThread",
                                      getContextName(), thread.getName() ) );
        } catch ( Exception e ) {
            Throwable t = ExceptionUtils.unwrapInvocationTargetException ( e );
            ExceptionUtils.handleThrowable ( t );
            log.warn ( sm.getString (
                           "webappClassLoader.stopTimerThreadFail",
                           thread.getName(), getContextName() ), t );
        }
    }
    private void checkThreadLocalsForLeaks() {
        Thread[] threads = getThreads();
        try {
            Field threadLocalsField =
                Thread.class.getDeclaredField ( "threadLocals" );
            threadLocalsField.setAccessible ( true );
            Field inheritableThreadLocalsField =
                Thread.class.getDeclaredField ( "inheritableThreadLocals" );
            inheritableThreadLocalsField.setAccessible ( true );
            Class<?> tlmClass = Class.forName ( "java.lang.ThreadLocal$ThreadLocalMap" );
            Field tableField = tlmClass.getDeclaredField ( "table" );
            tableField.setAccessible ( true );
            Method expungeStaleEntriesMethod = tlmClass.getDeclaredMethod ( "expungeStaleEntries" );
            expungeStaleEntriesMethod.setAccessible ( true );
            for ( int i = 0; i < threads.length; i++ ) {
                Object threadLocalMap;
                if ( threads[i] != null ) {
                    threadLocalMap = threadLocalsField.get ( threads[i] );
                    if ( null != threadLocalMap ) {
                        expungeStaleEntriesMethod.invoke ( threadLocalMap );
                        checkThreadLocalMapForLeaks ( threadLocalMap, tableField );
                    }
                    threadLocalMap = inheritableThreadLocalsField.get ( threads[i] );
                    if ( null != threadLocalMap ) {
                        expungeStaleEntriesMethod.invoke ( threadLocalMap );
                        checkThreadLocalMapForLeaks ( threadLocalMap, tableField );
                    }
                }
            }
        } catch ( Throwable t ) {
            JreCompat jreCompat = JreCompat.getInstance();
            if ( jreCompat.isInstanceOfInaccessibleObjectException ( t ) ) {
                log.warn ( sm.getString ( "webappClassLoader.addExportsThreadLocal" ) );
            } else {
                ExceptionUtils.handleThrowable ( t );
                log.warn ( sm.getString (
                               "webappClassLoader.checkThreadLocalsForLeaksFail",
                               getContextName() ), t );
            }
        }
    }
    private void checkThreadLocalMapForLeaks ( Object map,
            Field internalTableField ) throws IllegalAccessException,
        NoSuchFieldException {
        if ( map != null ) {
            Object[] table = ( Object[] ) internalTableField.get ( map );
            if ( table != null ) {
                for ( int j = 0; j < table.length; j++ ) {
                    Object obj = table[j];
                    if ( obj != null ) {
                        boolean keyLoadedByWebapp = false;
                        boolean valueLoadedByWebapp = false;
                        Object key = ( ( Reference<?> ) obj ).get();
                        if ( this.equals ( key ) || loadedByThisOrChild ( key ) ) {
                            keyLoadedByWebapp = true;
                        }
                        Field valueField =
                            obj.getClass().getDeclaredField ( "value" );
                        valueField.setAccessible ( true );
                        Object value = valueField.get ( obj );
                        if ( this.equals ( value ) || loadedByThisOrChild ( value ) ) {
                            valueLoadedByWebapp = true;
                        }
                        if ( keyLoadedByWebapp || valueLoadedByWebapp ) {
                            Object[] args = new Object[5];
                            args[0] = getContextName();
                            if ( key != null ) {
                                args[1] = getPrettyClassName ( key.getClass() );
                                try {
                                    args[2] = key.toString();
                                } catch ( Exception e ) {
                                    log.warn ( sm.getString (
                                                   "webappClassLoader.checkThreadLocalsForLeaks.badKey",
                                                   args[1] ), e );
                                    args[2] = sm.getString (
                                                  "webappClassLoader.checkThreadLocalsForLeaks.unknown" );
                                }
                            }
                            if ( value != null ) {
                                args[3] = getPrettyClassName ( value.getClass() );
                                try {
                                    args[4] = value.toString();
                                } catch ( Exception e ) {
                                    log.warn ( sm.getString (
                                                   "webappClassLoader.checkThreadLocalsForLeaks.badValue",
                                                   args[3] ), e );
                                    args[4] = sm.getString (
                                                  "webappClassLoader.checkThreadLocalsForLeaks.unknown" );
                                }
                            }
                            if ( valueLoadedByWebapp ) {
                                log.error ( sm.getString (
                                                "webappClassLoader.checkThreadLocalsForLeaks",
                                                args ) );
                            } else if ( value == null ) {
                                if ( log.isDebugEnabled() ) {
                                    log.debug ( sm.getString (
                                                    "webappClassLoader.checkThreadLocalsForLeaksNull",
                                                    args ) );
                                }
                            } else {
                                if ( log.isDebugEnabled() ) {
                                    log.debug ( sm.getString (
                                                    "webappClassLoader.checkThreadLocalsForLeaksNone",
                                                    args ) );
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private String getPrettyClassName ( Class<?> clazz ) {
        String name = clazz.getCanonicalName();
        if ( name == null ) {
            name = clazz.getName();
        }
        return name;
    }
    private String getStackTrace ( Thread thread ) {
        StringBuilder builder = new StringBuilder();
        for ( StackTraceElement ste : thread.getStackTrace() ) {
            builder.append ( "\n " ).append ( ste );
        }
        return builder.toString();
    }
    private boolean loadedByThisOrChild ( Object o ) {
        if ( o == null ) {
            return false;
        }
        Class<?> clazz;
        if ( o instanceof Class ) {
            clazz = ( Class<?> ) o;
        } else {
            clazz = o.getClass();
        }
        ClassLoader cl = clazz.getClassLoader();
        while ( cl != null ) {
            if ( cl == this ) {
                return true;
            }
            cl = cl.getParent();
        }
        if ( o instanceof Collection<?> ) {
            Iterator<?> iter = ( ( Collection<?> ) o ).iterator();
            try {
                while ( iter.hasNext() ) {
                    Object entry = iter.next();
                    if ( loadedByThisOrChild ( entry ) ) {
                        return true;
                    }
                }
            } catch ( ConcurrentModificationException e ) {
                log.warn ( sm.getString (
                               "webappClassLoader.loadedByThisOrChildFail", clazz.getName(), getContextName() ),
                           e );
            }
        }
        return false;
    }
    private Thread[] getThreads() {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        try {
            while ( tg.getParent() != null ) {
                tg = tg.getParent();
            }
        } catch ( SecurityException se ) {
            String msg = sm.getString (
                             "webappClassLoader.getThreadGroupError", tg.getName() );
            if ( log.isDebugEnabled() ) {
                log.debug ( msg, se );
            } else {
                log.warn ( msg );
            }
        }
        int threadCountGuess = tg.activeCount() + 50;
        Thread[] threads = new Thread[threadCountGuess];
        int threadCountActual = tg.enumerate ( threads );
        while ( threadCountActual == threadCountGuess ) {
            threadCountGuess *= 2;
            threads = new Thread[threadCountGuess];
            threadCountActual = tg.enumerate ( threads );
        }
        return threads;
    }
    private void clearReferencesRmiTargets() {
        try {
            Class<?> objectTargetClass =
                Class.forName ( "sun.rmi.transport.Target" );
            Field cclField = objectTargetClass.getDeclaredField ( "ccl" );
            cclField.setAccessible ( true );
            Field stubField = objectTargetClass.getDeclaredField ( "stub" );
            stubField.setAccessible ( true );
            Class<?> objectTableClass =
                Class.forName ( "sun.rmi.transport.ObjectTable" );
            Field objTableField = objectTableClass.getDeclaredField ( "objTable" );
            objTableField.setAccessible ( true );
            Object objTable = objTableField.get ( null );
            if ( objTable == null ) {
                return;
            }
            if ( objTable instanceof Map<?, ?> ) {
                Iterator<?> iter = ( ( Map<?, ?> ) objTable ).values().iterator();
                while ( iter.hasNext() ) {
                    Object obj = iter.next();
                    Object cclObject = cclField.get ( obj );
                    if ( this == cclObject ) {
                        iter.remove();
                        Object stubObject = stubField.get ( obj );
                        log.error ( sm.getString ( "webappClassLoader.clearRmi",
                                                   stubObject.getClass().getName(), stubObject ) );
                    }
                }
            }
            Field implTableField = objectTableClass.getDeclaredField ( "implTable" );
            implTableField.setAccessible ( true );
            Object implTable = implTableField.get ( null );
            if ( implTable == null ) {
                return;
            }
            if ( implTable instanceof Map<?, ?> ) {
                Iterator<?> iter = ( ( Map<?, ?> ) implTable ).values().iterator();
                while ( iter.hasNext() ) {
                    Object obj = iter.next();
                    Object cclObject = cclField.get ( obj );
                    if ( this == cclObject ) {
                        iter.remove();
                    }
                }
            }
        } catch ( ClassNotFoundException e ) {
            log.info ( sm.getString ( "webappClassLoader.clearRmiInfo",
                                      getContextName() ), e );
        } catch ( SecurityException | NoSuchFieldException | IllegalArgumentException |
                      IllegalAccessException e ) {
            log.warn ( sm.getString ( "webappClassLoader.clearRmiFail",
                                      getContextName() ), e );
        } catch ( Exception e ) {
            JreCompat jreCompat = JreCompat.getInstance();
            if ( jreCompat.isInstanceOfInaccessibleObjectException ( e ) ) {
                log.warn ( sm.getString ( "webappClassLoader.addExportsRmi" ) );
            } else {
                throw e;
            }
        }
    }
    protected Class<?> findClassInternal ( String name ) {
        checkStateForResourceLoading ( name );
        if ( name == null ) {
            return null;
        }
        String path = binaryNameToPath ( name, true );
        ResourceEntry entry = resourceEntries.get ( path );
        WebResource resource = null;
        if ( entry == null ) {
            resource = resources.getClassLoaderResource ( path );
            if ( !resource.exists() ) {
                return null;
            }
            entry = new ResourceEntry();
            entry.lastModified = resource.getLastModified();
            synchronized ( resourceEntries ) {
                ResourceEntry entry2 = resourceEntries.get ( path );
                if ( entry2 == null ) {
                    resourceEntries.put ( path, entry );
                } else {
                    entry = entry2;
                }
            }
        }
        Class<?> clazz = entry.loadedClass;
        if ( clazz != null ) {
            return clazz;
        }
        synchronized ( getClassLoadingLock ( name ) ) {
            clazz = entry.loadedClass;
            if ( clazz != null ) {
                return clazz;
            }
            if ( resource == null ) {
                resource = resources.getClassLoaderResource ( path );
            }
            if ( !resource.exists() ) {
                return null;
            }
            byte[] binaryContent = resource.getContent();
            Manifest manifest = resource.getManifest();
            URL codeBase = resource.getCodeBase();
            Certificate[] certificates = resource.getCertificates();
            if ( transformers.size() > 0 ) {
                String className = name.endsWith ( CLASS_FILE_SUFFIX ) ?
                                   name.substring ( 0, name.length() - CLASS_FILE_SUFFIX.length() ) : name;
                String internalName = className.replace ( ".", "/" );
                for ( ClassFileTransformer transformer : this.transformers ) {
                    try {
                        byte[] transformed = transformer.transform (
                                                 this, internalName, null, null, binaryContent );
                        if ( transformed != null ) {
                            binaryContent = transformed;
                        }
                    } catch ( IllegalClassFormatException e ) {
                        log.error ( sm.getString ( "webappClassLoader.transformError", name ), e );
                        return null;
                    }
                }
            }
            String packageName = null;
            int pos = name.lastIndexOf ( '.' );
            if ( pos != -1 ) {
                packageName = name.substring ( 0, pos );
            }
            Package pkg = null;
            if ( packageName != null ) {
                pkg = getPackage ( packageName );
                if ( pkg == null ) {
                    try {
                        if ( manifest == null ) {
                            definePackage ( packageName, null, null, null, null, null, null, null );
                        } else {
                            definePackage ( packageName, manifest, codeBase );
                        }
                    } catch ( IllegalArgumentException e ) {
                    }
                    pkg = getPackage ( packageName );
                }
            }
            if ( securityManager != null ) {
                if ( pkg != null ) {
                    boolean sealCheck = true;
                    if ( pkg.isSealed() ) {
                        sealCheck = pkg.isSealed ( codeBase );
                    } else {
                        sealCheck = ( manifest == null ) || !isPackageSealed ( packageName, manifest );
                    }
                    if ( !sealCheck )
                        throw new SecurityException
                        ( "Sealing violation loading " + name + " : Package "
                          + packageName + " is sealed." );
                }
            }
            try {
                clazz = defineClass ( name, binaryContent, 0,
                                      binaryContent.length, new CodeSource ( codeBase, certificates ) );
            } catch ( UnsupportedClassVersionError ucve ) {
                throw new UnsupportedClassVersionError (
                    ucve.getLocalizedMessage() + " " +
                    sm.getString ( "webappClassLoader.wrongVersion",
                                   name ) );
            }
            entry.loadedClass = clazz;
        }
        return clazz;
    }
    private String binaryNameToPath ( String binaryName, boolean withLeadingSlash ) {
        StringBuilder path = new StringBuilder ( 7 + binaryName.length() );
        if ( withLeadingSlash ) {
            path.append ( '/' );
        }
        path.append ( binaryName.replace ( '.', '/' ) );
        path.append ( CLASS_FILE_SUFFIX );
        return path.toString();
    }
    private String nameToPath ( String name ) {
        if ( name.startsWith ( "/" ) ) {
            return name;
        }
        StringBuilder path = new StringBuilder (
            1 + name.length() );
        path.append ( '/' );
        path.append ( name );
        return path.toString();
    }
    protected boolean isPackageSealed ( String name, Manifest man ) {
        String path = name.replace ( '.', '/' ) + '/';
        Attributes attr = man.getAttributes ( path );
        String sealed = null;
        if ( attr != null ) {
            sealed = attr.getValue ( Name.SEALED );
        }
        if ( sealed == null ) {
            if ( ( attr = man.getMainAttributes() ) != null ) {
                sealed = attr.getValue ( Name.SEALED );
            }
        }
        return "true".equalsIgnoreCase ( sealed );
    }
    protected Class<?> findLoadedClass0 ( String name ) {
        String path = binaryNameToPath ( name, true );
        ResourceEntry entry = resourceEntries.get ( path );
        if ( entry != null ) {
            return entry.loadedClass;
        }
        return null;
    }
    protected void refreshPolicy() {
        try {
            Policy policy = Policy.getPolicy();
            policy.refresh();
        } catch ( AccessControlException e ) {
        }
    }
    protected boolean filter ( String name, boolean isClassName ) {
        if ( name == null ) {
            return false;
        }
        char ch;
        if ( name.startsWith ( "javax" ) ) {
            if ( name.length() == 5 ) {
                return false;
            }
            ch = name.charAt ( 5 );
            if ( isClassName && ch == '.' ) {
                if ( name.startsWith ( "servlet.jsp.jstl.", 6 ) ) {
                    return false;
                }
                if ( name.startsWith ( "el.", 6 ) ||
                        name.startsWith ( "servlet.", 6 ) ||
                        name.startsWith ( "websocket.", 6 ) ||
                        name.startsWith ( "security.auth.message.", 6 ) ) {
                    return true;
                }
            } else if ( !isClassName && ch == '/' ) {
                if ( name.startsWith ( "servlet/jsp/jstl/", 6 ) ) {
                    return false;
                }
                if ( name.startsWith ( "el/", 6 ) ||
                        name.startsWith ( "servlet/", 6 ) ||
                        name.startsWith ( "websocket/", 6 ) ||
                        name.startsWith ( "security/auth/message/", 6 ) ) {
                    return true;
                }
            }
        } else if ( name.startsWith ( "org" ) ) {
            if ( name.length() == 3 ) {
                return false;
            }
            ch = name.charAt ( 3 );
            if ( isClassName && ch == '.' ) {
                if ( name.startsWith ( "apache.", 4 ) ) {
                    if ( name.startsWith ( "tomcat.jdbc.", 11 ) ) {
                        return false;
                    }
                    if ( name.startsWith ( "el.", 11 ) ||
                            name.startsWith ( "catalina.", 11 ) ||
                            name.startsWith ( "jasper.", 11 ) ||
                            name.startsWith ( "juli.", 11 ) ||
                            name.startsWith ( "tomcat.", 11 ) ||
                            name.startsWith ( "naming.", 11 ) ||
                            name.startsWith ( "coyote.", 11 ) ) {
                        return true;
                    }
                }
            } else if ( !isClassName && ch == '/' ) {
                if ( name.startsWith ( "apache/", 4 ) ) {
                    if ( name.startsWith ( "tomcat/jdbc/", 11 ) ) {
                        return false;
                    }
                    if ( name.startsWith ( "el/", 11 ) ||
                            name.startsWith ( "catalina/", 11 ) ||
                            name.startsWith ( "jasper/", 11 ) ||
                            name.startsWith ( "juli/", 11 ) ||
                            name.startsWith ( "tomcat/", 11 ) ||
                            name.startsWith ( "naming/", 11 ) ||
                            name.startsWith ( "coyote/", 11 ) ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    @Override
    protected void addURL ( URL url ) {
        super.addURL ( url );
        hasExternalRepositories = true;
    }
    @Override
    public String getWebappName() {
        return getContextName();
    }
    @Override
    public String getHostName() {
        if ( resources != null ) {
            Container host = resources.getContext().getParent();
            if ( host != null ) {
                return host.getName();
            }
        }
        return null;
    }
    @Override
    public String getServiceName() {
        if ( resources != null ) {
            Container host = resources.getContext().getParent();
            if ( host != null ) {
                Container engine = host.getParent();
                if ( engine != null ) {
                    return engine.getName();
                }
            }
        }
        return null;
    }
}
