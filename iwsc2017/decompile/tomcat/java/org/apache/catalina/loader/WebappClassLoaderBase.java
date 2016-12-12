package org.apache.catalina.loader;
import org.apache.catalina.Container;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.lang.instrument.IllegalClassFormatException;
import java.util.ConcurrentModificationException;
import java.lang.ref.Reference;
import org.apache.tomcat.util.compat.JreCompat;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import java.beans.Introspector;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import java.util.Arrays;
import java.security.cert.Certificate;
import java.security.Policy;
import java.security.CodeSource;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Enumeration;
import java.security.PrivilegedAction;
import org.apache.catalina.Globals;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.ProtectionDomain;
import org.apache.catalina.WebResource;
import java.util.Iterator;
import java.util.Date;
import java.util.Collection;
import java.net.URI;
import java.io.FilePermission;
import java.net.URISyntaxException;
import java.io.IOException;
import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.catalina.LifecycleState;
import java.net.URL;
import java.lang.instrument.ClassFileTransformer;
import java.security.PermissionCollection;
import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.catalina.WebResourceRoot;
import org.apache.tomcat.util.res.StringManager;
import java.util.List;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.security.PermissionCheck;
import org.apache.juli.WebappProperties;
import org.apache.tomcat.InstrumentableClassLoader;
import org.apache.catalina.Lifecycle;
import java.net.URLClassLoader;
public abstract class WebappClassLoaderBase extends URLClassLoader implements Lifecycle, InstrumentableClassLoader, WebappProperties, PermissionCheck {
    private static final Log log;
    private static final List<String> JVM_THREAD_GROUP_NAMES;
    private static final String JVM_THREAD_GROUP_SYSTEM = "system";
    private static final String CLASS_FILE_SUFFIX = ".class";
    protected static final StringManager sm;
    protected WebResourceRoot resources;
    protected final Map<String, ResourceEntry> resourceEntries;
    protected boolean delegate;
    private final HashMap<String, Long> jarModificationTimes;
    protected final ArrayList<Permission> permissionList;
    protected final HashMap<String, PermissionCollection> loaderPC;
    protected final SecurityManager securityManager;
    protected final ClassLoader parent;
    private ClassLoader javaseClassLoader;
    private boolean clearReferencesRmiTargets;
    private boolean clearReferencesStopThreads;
    private boolean clearReferencesStopTimerThreads;
    private boolean clearReferencesLogFactoryRelease;
    private boolean clearReferencesHttpClientKeepAliveThread;
    private final List<ClassFileTransformer> transformers;
    private boolean hasExternalRepositories;
    private List<URL> localRepositories;
    private volatile LifecycleState state;
    protected WebappClassLoaderBase() {
        super ( new URL[0] );
        this.resources = null;
        this.resourceEntries = new ConcurrentHashMap<String, ResourceEntry>();
        this.delegate = false;
        this.jarModificationTimes = new HashMap<String, Long>();
        this.permissionList = new ArrayList<Permission>();
        this.loaderPC = new HashMap<String, PermissionCollection>();
        this.clearReferencesRmiTargets = true;
        this.clearReferencesStopThreads = false;
        this.clearReferencesStopTimerThreads = false;
        this.clearReferencesLogFactoryRelease = true;
        this.clearReferencesHttpClientKeepAliveThread = true;
        this.transformers = new CopyOnWriteArrayList<ClassFileTransformer>();
        this.hasExternalRepositories = false;
        this.localRepositories = new ArrayList<URL>();
        this.state = LifecycleState.NEW;
        ClassLoader p = this.getParent();
        if ( p == null ) {
            p = ClassLoader.getSystemClassLoader();
        }
        this.parent = p;
        ClassLoader j = String.class.getClassLoader();
        if ( j == null ) {
            for ( j = ClassLoader.getSystemClassLoader(); j.getParent() != null; j = j.getParent() ) {}
        }
        this.javaseClassLoader = j;
        this.securityManager = System.getSecurityManager();
        if ( this.securityManager != null ) {
            this.refreshPolicy();
        }
    }
    protected WebappClassLoaderBase ( final ClassLoader parent ) {
        super ( new URL[0], parent );
        this.resources = null;
        this.resourceEntries = new ConcurrentHashMap<String, ResourceEntry>();
        this.delegate = false;
        this.jarModificationTimes = new HashMap<String, Long>();
        this.permissionList = new ArrayList<Permission>();
        this.loaderPC = new HashMap<String, PermissionCollection>();
        this.clearReferencesRmiTargets = true;
        this.clearReferencesStopThreads = false;
        this.clearReferencesStopTimerThreads = false;
        this.clearReferencesLogFactoryRelease = true;
        this.clearReferencesHttpClientKeepAliveThread = true;
        this.transformers = new CopyOnWriteArrayList<ClassFileTransformer>();
        this.hasExternalRepositories = false;
        this.localRepositories = new ArrayList<URL>();
        this.state = LifecycleState.NEW;
        ClassLoader p = this.getParent();
        if ( p == null ) {
            p = ClassLoader.getSystemClassLoader();
        }
        this.parent = p;
        ClassLoader j = String.class.getClassLoader();
        if ( j == null ) {
            for ( j = ClassLoader.getSystemClassLoader(); j.getParent() != null; j = j.getParent() ) {}
        }
        this.javaseClassLoader = j;
        this.securityManager = System.getSecurityManager();
        if ( this.securityManager != null ) {
            this.refreshPolicy();
        }
    }
    public WebResourceRoot getResources() {
        return this.resources;
    }
    public void setResources ( final WebResourceRoot resources ) {
        this.resources = resources;
    }
    public String getContextName() {
        if ( this.resources == null ) {
            return "Unknown";
        }
        return this.resources.getContext().getBaseName();
    }
    public boolean getDelegate() {
        return this.delegate;
    }
    public void setDelegate ( final boolean delegate ) {
        this.delegate = delegate;
    }
    void addPermission ( final URL url ) {
        if ( url == null ) {
            return;
        }
        if ( this.securityManager != null ) {
            final String protocol = url.getProtocol();
            if ( "file".equalsIgnoreCase ( protocol ) ) {
                File f;
                String path;
                try {
                    final URI uri = url.toURI();
                    f = new File ( uri );
                    path = f.getCanonicalPath();
                } catch ( IOException | URISyntaxException e ) {
                    WebappClassLoaderBase.log.warn ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.addPermisionNoCanonicalFile", url.toExternalForm() ) );
                    return;
                }
                if ( f.isFile() ) {
                    this.addPermission ( new FilePermission ( path, "read" ) );
                } else if ( f.isDirectory() ) {
                    this.addPermission ( new FilePermission ( path, "read" ) );
                    this.addPermission ( new FilePermission ( path + File.separator + "-", "read" ) );
                }
            } else {
                WebappClassLoaderBase.log.warn ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.addPermisionNoProtocol", protocol, url.toExternalForm() ) );
            }
        }
    }
    void addPermission ( final Permission permission ) {
        if ( this.securityManager != null && permission != null ) {
            this.permissionList.add ( permission );
        }
    }
    public boolean getClearReferencesRmiTargets() {
        return this.clearReferencesRmiTargets;
    }
    public void setClearReferencesRmiTargets ( final boolean clearReferencesRmiTargets ) {
        this.clearReferencesRmiTargets = clearReferencesRmiTargets;
    }
    public boolean getClearReferencesStopThreads() {
        return this.clearReferencesStopThreads;
    }
    public void setClearReferencesStopThreads ( final boolean clearReferencesStopThreads ) {
        this.clearReferencesStopThreads = clearReferencesStopThreads;
    }
    public boolean getClearReferencesStopTimerThreads() {
        return this.clearReferencesStopTimerThreads;
    }
    public void setClearReferencesStopTimerThreads ( final boolean clearReferencesStopTimerThreads ) {
        this.clearReferencesStopTimerThreads = clearReferencesStopTimerThreads;
    }
    public boolean getClearReferencesLogFactoryRelease() {
        return this.clearReferencesLogFactoryRelease;
    }
    public void setClearReferencesLogFactoryRelease ( final boolean clearReferencesLogFactoryRelease ) {
        this.clearReferencesLogFactoryRelease = clearReferencesLogFactoryRelease;
    }
    public boolean getClearReferencesHttpClientKeepAliveThread() {
        return this.clearReferencesHttpClientKeepAliveThread;
    }
    public void setClearReferencesHttpClientKeepAliveThread ( final boolean clearReferencesHttpClientKeepAliveThread ) {
        this.clearReferencesHttpClientKeepAliveThread = clearReferencesHttpClientKeepAliveThread;
    }
    @Override
    public void addTransformer ( final ClassFileTransformer transformer ) {
        if ( transformer == null ) {
            throw new IllegalArgumentException ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.addTransformer.illegalArgument", this.getContextName() ) );
        }
        if ( this.transformers.contains ( transformer ) ) {
            WebappClassLoaderBase.log.warn ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.addTransformer.duplicate", transformer, this.getContextName() ) );
            return;
        }
        this.transformers.add ( transformer );
        WebappClassLoaderBase.log.info ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.addTransformer", transformer, this.getContextName() ) );
    }
    @Override
    public void removeTransformer ( final ClassFileTransformer transformer ) {
        if ( transformer == null ) {
            return;
        }
        if ( this.transformers.remove ( transformer ) ) {
            WebappClassLoaderBase.log.info ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.removeTransformer", transformer, this.getContextName() ) );
        }
    }
    protected void copyStateWithoutTransformers ( final WebappClassLoaderBase base ) {
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
        if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
            WebappClassLoaderBase.log.debug ( "modified()" );
        }
        for ( final Map.Entry<String, ResourceEntry> entry : this.resourceEntries.entrySet() ) {
            final long cachedLastModified = entry.getValue().lastModified;
            final long lastModified = this.resources.getClassLoaderResource ( entry.getKey() ).getLastModified();
            if ( lastModified != cachedLastModified ) {
                if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                    WebappClassLoaderBase.log.debug ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.resourceModified", entry.getKey(), new Date ( cachedLastModified ), new Date ( lastModified ) ) );
                }
                return true;
            }
        }
        final WebResource[] jars = this.resources.listResources ( "/WEB-INF/lib" );
        int jarCount = 0;
        for ( final WebResource jar : jars ) {
            if ( jar.getName().endsWith ( ".jar" ) && jar.isFile() && jar.canRead() ) {
                ++jarCount;
                final Long recordedLastModified = this.jarModificationTimes.get ( jar.getName() );
                if ( recordedLastModified == null ) {
                    WebappClassLoaderBase.log.info ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.jarsAdded", this.resources.getContext().getName() ) );
                    return true;
                }
                if ( recordedLastModified != jar.getLastModified() ) {
                    WebappClassLoaderBase.log.info ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.jarsModified", this.resources.getContext().getName() ) );
                    return true;
                }
            }
        }
        if ( jarCount < this.jarModificationTimes.size() ) {
            WebappClassLoaderBase.log.info ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.jarsRemoved", this.resources.getContext().getName() ) );
            return true;
        }
        return false;
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( this.getClass().getSimpleName() );
        sb.append ( "\r\n  context: " );
        sb.append ( this.getContextName() );
        sb.append ( "\r\n  delegate: " );
        sb.append ( this.delegate );
        sb.append ( "\r\n" );
        if ( this.parent != null ) {
            sb.append ( "----------> Parent Classloader:\r\n" );
            sb.append ( this.parent.toString() );
            sb.append ( "\r\n" );
        }
        if ( this.transformers.size() > 0 ) {
            sb.append ( "----------> Class file transformers:\r\n" );
            for ( final ClassFileTransformer transformer : this.transformers ) {
                sb.append ( transformer ).append ( "\r\n" );
            }
        }
        return sb.toString();
    }
    protected final Class<?> doDefineClass ( final String name, final byte[] b, final int off, final int len, final ProtectionDomain protectionDomain ) {
        return super.defineClass ( name, b, off, len, protectionDomain );
    }
    public Class<?> findClass ( final String name ) throws ClassNotFoundException {
        if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
            WebappClassLoaderBase.log.debug ( "    findClass(" + name + ")" );
        }
        this.checkStateForClassLoading ( name );
        if ( this.securityManager != null ) {
            final int i = name.lastIndexOf ( 46 );
            if ( i >= 0 ) {
                try {
                    if ( WebappClassLoaderBase.log.isTraceEnabled() ) {
                        WebappClassLoaderBase.log.trace ( "      securityManager.checkPackageDefinition" );
                    }
                    this.securityManager.checkPackageDefinition ( name.substring ( 0, i ) );
                } catch ( Exception se ) {
                    if ( WebappClassLoaderBase.log.isTraceEnabled() ) {
                        WebappClassLoaderBase.log.trace ( "      -->Exception-->ClassNotFoundException", se );
                    }
                    throw new ClassNotFoundException ( name, se );
                }
            }
        }
        Class<?> clazz = null;
        try {
            if ( WebappClassLoaderBase.log.isTraceEnabled() ) {
                WebappClassLoaderBase.log.trace ( "      findClassInternal(" + name + ")" );
            }
            try {
                if ( this.securityManager != null ) {
                    final PrivilegedAction<Class<?>> dp = new PrivilegedFindClassByName ( name );
                    clazz = AccessController.doPrivileged ( dp );
                } else {
                    clazz = this.findClassInternal ( name );
                }
            } catch ( AccessControlException ace ) {
                WebappClassLoaderBase.log.warn ( "WebappClassLoader.findClassInternal(" + name + ") security exception: " + ace.getMessage(), ace );
                throw new ClassNotFoundException ( name, ace );
            } catch ( RuntimeException e ) {
                if ( WebappClassLoaderBase.log.isTraceEnabled() ) {
                    WebappClassLoaderBase.log.trace ( "      -->RuntimeException Rethrown", e );
                }
                throw e;
            }
            if ( clazz == null && this.hasExternalRepositories ) {
                try {
                    clazz = super.findClass ( name );
                } catch ( AccessControlException ace ) {
                    WebappClassLoaderBase.log.warn ( "WebappClassLoader.findClassInternal(" + name + ") security exception: " + ace.getMessage(), ace );
                    throw new ClassNotFoundException ( name, ace );
                } catch ( RuntimeException e ) {
                    if ( WebappClassLoaderBase.log.isTraceEnabled() ) {
                        WebappClassLoaderBase.log.trace ( "      -->RuntimeException Rethrown", e );
                    }
                    throw e;
                }
            }
            if ( clazz == null ) {
                if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                    WebappClassLoaderBase.log.debug ( "    --> Returning ClassNotFoundException" );
                }
                throw new ClassNotFoundException ( name );
            }
        } catch ( ClassNotFoundException e2 ) {
            if ( WebappClassLoaderBase.log.isTraceEnabled() ) {
                WebappClassLoaderBase.log.trace ( "    --> Passing on ClassNotFoundException" );
            }
            throw e2;
        }
        if ( WebappClassLoaderBase.log.isTraceEnabled() ) {
            WebappClassLoaderBase.log.debug ( "      Returning class " + clazz );
        }
        if ( WebappClassLoaderBase.log.isTraceEnabled() ) {
            ClassLoader cl;
            if ( Globals.IS_SECURITY_ENABLED ) {
                cl = AccessController.doPrivileged ( ( PrivilegedAction<ClassLoader> ) new PrivilegedGetClassLoader ( clazz ) );
            } else {
                cl = clazz.getClassLoader();
            }
            WebappClassLoaderBase.log.debug ( "      Loaded by " + cl.toString() );
        }
        return clazz;
    }
    @Override
    public URL findResource ( final String name ) {
        if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
            WebappClassLoaderBase.log.debug ( "    findResource(" + name + ")" );
        }
        this.checkStateForResourceLoading ( name );
        URL url = null;
        final String path = this.nameToPath ( name );
        final WebResource resource = this.resources.getClassLoaderResource ( path );
        if ( resource.exists() ) {
            url = resource.getURL();
            this.trackLastModified ( path, resource );
        }
        if ( url == null && this.hasExternalRepositories ) {
            url = super.findResource ( name );
        }
        if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
            if ( url != null ) {
                WebappClassLoaderBase.log.debug ( "    --> Returning '" + url.toString() + "'" );
            } else {
                WebappClassLoaderBase.log.debug ( "    --> Resource not found, returning null" );
            }
        }
        return url;
    }
    private void trackLastModified ( final String path, final WebResource resource ) {
        if ( this.resourceEntries.containsKey ( path ) ) {
            return;
        }
        final ResourceEntry entry = new ResourceEntry();
        entry.lastModified = resource.getLastModified();
        synchronized ( this.resourceEntries ) {
            this.resourceEntries.putIfAbsent ( path, entry );
        }
    }
    @Override
    public Enumeration<URL> findResources ( final String name ) throws IOException {
        if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
            WebappClassLoaderBase.log.debug ( "    findResources(" + name + ")" );
        }
        this.checkStateForResourceLoading ( name );
        final LinkedHashSet<URL> result = new LinkedHashSet<URL>();
        final String path = this.nameToPath ( name );
        final WebResource[] classLoaderResources;
        final WebResource[] webResources = classLoaderResources = this.resources.getClassLoaderResources ( path );
        for ( final WebResource webResource : classLoaderResources ) {
            if ( webResource.exists() ) {
                result.add ( webResource.getURL() );
            }
        }
        if ( this.hasExternalRepositories ) {
            final Enumeration<URL> otherResourcePaths = super.findResources ( name );
            while ( otherResourcePaths.hasMoreElements() ) {
                result.add ( otherResourcePaths.nextElement() );
            }
        }
        return Collections.enumeration ( result );
    }
    @Override
    public URL getResource ( final String name ) {
        if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
            WebappClassLoaderBase.log.debug ( "getResource(" + name + ")" );
        }
        this.checkStateForResourceLoading ( name );
        URL url = null;
        final boolean delegateFirst = this.delegate || this.filter ( name, false );
        if ( delegateFirst ) {
            if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                WebappClassLoaderBase.log.debug ( "  Delegating to parent classloader " + this.parent );
            }
            url = this.parent.getResource ( name );
            if ( url != null ) {
                if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                    WebappClassLoaderBase.log.debug ( "  --> Returning '" + url.toString() + "'" );
                }
                return url;
            }
        }
        url = this.findResource ( name );
        if ( url != null ) {
            if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                WebappClassLoaderBase.log.debug ( "  --> Returning '" + url.toString() + "'" );
            }
            return url;
        }
        if ( !delegateFirst ) {
            url = this.parent.getResource ( name );
            if ( url != null ) {
                if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                    WebappClassLoaderBase.log.debug ( "  --> Returning '" + url.toString() + "'" );
                }
                return url;
            }
        }
        if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
            WebappClassLoaderBase.log.debug ( "  --> Resource not found, returning null" );
        }
        return null;
    }
    @Override
    public InputStream getResourceAsStream ( final String name ) {
        if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
            WebappClassLoaderBase.log.debug ( "getResourceAsStream(" + name + ")" );
        }
        this.checkStateForResourceLoading ( name );
        InputStream stream = null;
        final boolean delegateFirst = this.delegate || this.filter ( name, false );
        if ( delegateFirst ) {
            if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                WebappClassLoaderBase.log.debug ( "  Delegating to parent classloader " + this.parent );
            }
            stream = this.parent.getResourceAsStream ( name );
            if ( stream != null ) {
                if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                    WebappClassLoaderBase.log.debug ( "  --> Returning stream from parent" );
                }
                return stream;
            }
        }
        if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
            WebappClassLoaderBase.log.debug ( "  Searching local repositories" );
        }
        final String path = this.nameToPath ( name );
        final WebResource resource = this.resources.getClassLoaderResource ( path );
        if ( resource.exists() ) {
            stream = resource.getInputStream();
            this.trackLastModified ( path, resource );
        }
        try {
            if ( this.hasExternalRepositories && stream == null ) {
                final URL url = super.findResource ( name );
                if ( url != null ) {
                    stream = url.openStream();
                }
            }
        } catch ( IOException ex ) {}
        if ( stream != null ) {
            if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                WebappClassLoaderBase.log.debug ( "  --> Returning stream from local" );
            }
            return stream;
        }
        if ( !delegateFirst ) {
            if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                WebappClassLoaderBase.log.debug ( "  Delegating to parent classloader unconditionally " + this.parent );
            }
            stream = this.parent.getResourceAsStream ( name );
            if ( stream != null ) {
                if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                    WebappClassLoaderBase.log.debug ( "  --> Returning stream from parent" );
                }
                return stream;
            }
        }
        if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
            WebappClassLoaderBase.log.debug ( "  --> Resource not found, returning null" );
        }
        return null;
    }
    @Override
    public Class<?> loadClass ( final String name ) throws ClassNotFoundException {
        return this.loadClass ( name, false );
    }
    public Class<?> loadClass ( final String name, final boolean resolve ) throws ClassNotFoundException {
        synchronized ( this.getClassLoadingLock ( name ) ) {
            if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                WebappClassLoaderBase.log.debug ( "loadClass(" + name + ", " + resolve + ")" );
            }
            Class<?> clazz = null;
            this.checkStateForClassLoading ( name );
            clazz = this.findLoadedClass0 ( name );
            if ( clazz != null ) {
                if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                    WebappClassLoaderBase.log.debug ( "  Returning class from cache" );
                }
                if ( resolve ) {
                    this.resolveClass ( clazz );
                }
                return clazz;
            }
            clazz = this.findLoadedClass ( name );
            if ( clazz != null ) {
                if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                    WebappClassLoaderBase.log.debug ( "  Returning class from cache" );
                }
                if ( resolve ) {
                    this.resolveClass ( clazz );
                }
                return clazz;
            }
            final String resourceName = this.binaryNameToPath ( name, false );
            final ClassLoader javaseLoader = this.getJavaseClassLoader();
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
                            this.resolveClass ( clazz );
                        }
                        return clazz;
                    }
                } catch ( ClassNotFoundException ex ) {}
            }
            if ( this.securityManager != null ) {
                final int i = name.lastIndexOf ( 46 );
                if ( i >= 0 ) {
                    try {
                        this.securityManager.checkPackageAccess ( name.substring ( 0, i ) );
                    } catch ( SecurityException se ) {
                        final String error = "Security Violation, attempt to use Restricted Class: " + name;
                        WebappClassLoaderBase.log.info ( error, se );
                        throw new ClassNotFoundException ( error, se );
                    }
                }
            }
            final boolean delegateLoad = this.delegate || this.filter ( name, true );
            if ( delegateLoad ) {
                if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                    WebappClassLoaderBase.log.debug ( "  Delegating to parent classloader1 " + this.parent );
                }
                try {
                    clazz = Class.forName ( name, false, this.parent );
                    if ( clazz != null ) {
                        if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                            WebappClassLoaderBase.log.debug ( "  Loading class from parent" );
                        }
                        if ( resolve ) {
                            this.resolveClass ( clazz );
                        }
                        return clazz;
                    }
                } catch ( ClassNotFoundException ex2 ) {}
            }
            if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                WebappClassLoaderBase.log.debug ( "  Searching local repositories" );
            }
            try {
                clazz = this.findClass ( name );
                if ( clazz != null ) {
                    if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                        WebappClassLoaderBase.log.debug ( "  Loading class from local repository" );
                    }
                    if ( resolve ) {
                        this.resolveClass ( clazz );
                    }
                    return clazz;
                }
            } catch ( ClassNotFoundException ex3 ) {}
            if ( !delegateLoad ) {
                if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                    WebappClassLoaderBase.log.debug ( "  Delegating to parent classloader at end: " + this.parent );
                }
                try {
                    clazz = Class.forName ( name, false, this.parent );
                    if ( clazz != null ) {
                        if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                            WebappClassLoaderBase.log.debug ( "  Loading class from parent" );
                        }
                        if ( resolve ) {
                            this.resolveClass ( clazz );
                        }
                        return clazz;
                    }
                } catch ( ClassNotFoundException ex4 ) {}
            }
        }
        throw new ClassNotFoundException ( name );
    }
    protected void checkStateForClassLoading ( final String className ) throws ClassNotFoundException {
        try {
            this.checkStateForResourceLoading ( className );
        } catch ( IllegalStateException ise ) {
            throw new ClassNotFoundException ( ise.getMessage(), ise );
        }
    }
    protected void checkStateForResourceLoading ( final String resource ) throws IllegalStateException {
        if ( !this.state.isAvailable() ) {
            final String msg = WebappClassLoaderBase.sm.getString ( "webappClassLoader.stopped", resource );
            final IllegalStateException ise = new IllegalStateException ( msg );
            WebappClassLoaderBase.log.info ( msg, ise );
            throw ise;
        }
    }
    @Override
    protected PermissionCollection getPermissions ( final CodeSource codeSource ) {
        final String codeUrl = codeSource.getLocation().toString();
        PermissionCollection pc;
        if ( ( pc = this.loaderPC.get ( codeUrl ) ) == null ) {
            pc = super.getPermissions ( codeSource );
            if ( pc != null ) {
                for ( final Permission p : this.permissionList ) {
                    pc.add ( p );
                }
                this.loaderPC.put ( codeUrl, pc );
            }
        }
        return pc;
    }
    @Override
    public boolean check ( final Permission permission ) {
        if ( !Globals.IS_SECURITY_ENABLED ) {
            return true;
        }
        final Policy currentPolicy = Policy.getPolicy();
        if ( currentPolicy != null ) {
            final URL contextRootUrl = this.resources.getResource ( "/" ).getCodeBase();
            final CodeSource cs = new CodeSource ( contextRootUrl, ( Certificate[] ) null );
            final PermissionCollection pc = currentPolicy.getPermissions ( cs );
            if ( pc.implies ( permission ) ) {
                return true;
            }
        }
        return false;
    }
    @Override
    public URL[] getURLs() {
        final ArrayList<URL> result = new ArrayList<URL>();
        result.addAll ( this.localRepositories );
        result.addAll ( Arrays.asList ( super.getURLs() ) );
        return result.toArray ( new URL[result.size()] );
    }
    @Override
    public void addLifecycleListener ( final LifecycleListener listener ) {
    }
    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return new LifecycleListener[0];
    }
    @Override
    public void removeLifecycleListener ( final LifecycleListener listener ) {
    }
    @Override
    public LifecycleState getState() {
        return this.state;
    }
    @Override
    public String getStateName() {
        return this.getState().toString();
    }
    @Override
    public void init() {
        this.state = LifecycleState.INITIALIZED;
    }
    @Override
    public void start() throws LifecycleException {
        this.state = LifecycleState.STARTING_PREP;
        final WebResource classes = this.resources.getResource ( "/WEB-INF/classes" );
        if ( classes.isDirectory() && classes.canRead() ) {
            this.localRepositories.add ( classes.getURL() );
        }
        final WebResource[] listResources;
        final WebResource[] jars = listResources = this.resources.listResources ( "/WEB-INF/lib" );
        for ( final WebResource jar : listResources ) {
            if ( jar.getName().endsWith ( ".jar" ) && jar.isFile() && jar.canRead() ) {
                this.localRepositories.add ( jar.getURL() );
                this.jarModificationTimes.put ( jar.getName(), jar.getLastModified() );
            }
        }
        this.state = LifecycleState.STARTED;
    }
    @Override
    public void stop() throws LifecycleException {
        this.state = LifecycleState.STOPPING_PREP;
        this.clearReferences();
        this.state = LifecycleState.STOPPING;
        this.resourceEntries.clear();
        this.jarModificationTimes.clear();
        this.resources = null;
        this.permissionList.clear();
        this.loaderPC.clear();
        this.state = LifecycleState.STOPPED;
    }
    @Override
    public void destroy() {
        this.state = LifecycleState.DESTROYING;
        try {
            super.close();
        } catch ( IOException ioe ) {
            WebappClassLoaderBase.log.warn ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.superCloseFail" ), ioe );
        }
        this.state = LifecycleState.DESTROYED;
    }
    protected ClassLoader getJavaseClassLoader() {
        return this.javaseClassLoader;
    }
    protected void setJavaseClassLoader ( final ClassLoader classLoader ) {
        if ( classLoader == null ) {
            throw new IllegalArgumentException ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.javaseClassLoaderNull" ) );
        }
        this.javaseClassLoader = classLoader;
    }
    protected void clearReferences() {
        this.clearReferencesJdbc();
        this.clearReferencesThreads();
        this.checkThreadLocalsForLeaks();
        if ( this.clearReferencesRmiTargets ) {
            this.clearReferencesRmiTargets();
        }
        IntrospectionUtils.clear();
        if ( this.clearReferencesLogFactoryRelease ) {
            LogFactory.release ( this );
        }
        Introspector.flushCaches();
        TomcatURLStreamHandlerFactory.release ( this );
    }
    private final void clearReferencesJdbc() {
        byte[] classBytes = new byte[2048];
        int offset = 0;
        try ( final InputStream is = this.getResourceAsStream ( "org/apache/catalina/loader/JdbcLeakPrevention.class" ) ) {
            for ( int read = is.read ( classBytes, offset, classBytes.length - offset ); read > -1; read = is.read ( classBytes, offset, classBytes.length - offset ) ) {
                offset += read;
                if ( offset == classBytes.length ) {
                    final byte[] tmp = new byte[classBytes.length * 2];
                    System.arraycopy ( classBytes, 0, tmp, 0, classBytes.length );
                    classBytes = tmp;
                }
            }
            final Class<?> lpClass = this.defineClass ( "org.apache.catalina.loader.JdbcLeakPrevention", classBytes, 0, offset, this.getClass().getProtectionDomain() );
            final Object obj = lpClass.newInstance();
            final List<String> driverNames = ( List<String> ) obj.getClass().getMethod ( "clearJdbcDriverRegistrations", ( Class<?>[] ) new Class[0] ).invoke ( obj, new Object[0] );
            for ( final String name : driverNames ) {
                WebappClassLoaderBase.log.warn ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.clearJdbc", this.getContextName(), name ) );
            }
        } catch ( Exception e ) {
            final Throwable t = ExceptionUtils.unwrapInvocationTargetException ( e );
            ExceptionUtils.handleThrowable ( t );
            WebappClassLoaderBase.log.warn ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.jdbcRemoveFailed", this.getContextName() ), t );
        }
    }
    private void clearReferencesThreads() {
        final Thread[] threads = this.getThreads();
        final List<Thread> executorThreadsToStop = new ArrayList<Thread>();
        for ( final Thread thread : threads ) {
            if ( thread != null ) {
                final ClassLoader ccl = thread.getContextClassLoader();
                if ( ccl == this ) {
                    if ( thread != Thread.currentThread() ) {
                        final String threadName = thread.getName();
                        final ThreadGroup tg = thread.getThreadGroup();
                        if ( tg != null && WebappClassLoaderBase.JVM_THREAD_GROUP_NAMES.contains ( tg.getName() ) ) {
                            if ( this.clearReferencesHttpClientKeepAliveThread && threadName.equals ( "Keep-Alive-Timer" ) ) {
                                thread.setContextClassLoader ( this.parent );
                                WebappClassLoaderBase.log.debug ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.checkThreadsHttpClient" ) );
                            }
                        } else if ( thread.isAlive() ) {
                            if ( thread.getClass().getName().startsWith ( "java.util.Timer" ) && this.clearReferencesStopTimerThreads ) {
                                this.clearReferencesStopTimerThread ( thread );
                            } else {
                                if ( this.isRequestThread ( thread ) ) {
                                    WebappClassLoaderBase.log.warn ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.stackTraceRequestThread", this.getContextName(), threadName, this.getStackTrace ( thread ) ) );
                                } else {
                                    WebappClassLoaderBase.log.warn ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.stackTrace", this.getContextName(), threadName, this.getStackTrace ( thread ) ) );
                                }
                                if ( this.clearReferencesStopThreads ) {
                                    boolean usingExecutor = false;
                                    try {
                                        Object target = null;
                                        final String[] array2 = { "target", "runnable", "action" };
                                        final int length2 = array2.length;
                                        int j = 0;
                                        while ( j < length2 ) {
                                            final String fieldName = array2[j];
                                            try {
                                                final Field targetField = thread.getClass().getDeclaredField ( fieldName );
                                                targetField.setAccessible ( true );
                                                target = targetField.get ( thread );
                                            } catch ( NoSuchFieldException nfe ) {
                                                ++j;
                                                continue;
                                            }
                                            break;
                                        }
                                        if ( target != null && target.getClass().getCanonicalName() != null && target.getClass().getCanonicalName().equals ( "java.util.concurrent.ThreadPoolExecutor.Worker" ) ) {
                                            final Field executorField = target.getClass().getDeclaredField ( "this$0" );
                                            executorField.setAccessible ( true );
                                            final Object executor = executorField.get ( target );
                                            if ( executor instanceof ThreadPoolExecutor ) {
                                                ( ( ThreadPoolExecutor ) executor ).shutdownNow();
                                                usingExecutor = true;
                                            }
                                        }
                                    } catch ( SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e ) {
                                        WebappClassLoaderBase.log.warn ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.stopThreadFail", thread.getName(), this.getContextName() ), e );
                                    }
                                    if ( usingExecutor ) {
                                        executorThreadsToStop.add ( thread );
                                    } else {
                                        thread.stop();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        int count = 0;
        for ( final Thread t : executorThreadsToStop ) {
            while ( t.isAlive() && count < 100 ) {
                try {
                    Thread.sleep ( 20L );
                } catch ( InterruptedException e2 ) {
                    break;
                }
                ++count;
            }
            if ( t.isAlive() ) {
                t.stop();
            }
        }
    }
    private boolean isRequestThread ( final Thread thread ) {
        final StackTraceElement[] elements = thread.getStackTrace();
        if ( elements == null || elements.length == 0 ) {
            return false;
        }
        for ( int i = 0; i < elements.length; ++i ) {
            final StackTraceElement element = elements[elements.length - ( i + 1 )];
            if ( "org.apache.catalina.connector.CoyoteAdapter".equals ( element.getClassName() ) ) {
                return true;
            }
        }
        return false;
    }
    private void clearReferencesStopTimerThread ( final Thread thread ) {
        try {
            try {
                final Field newTasksMayBeScheduledField = thread.getClass().getDeclaredField ( "newTasksMayBeScheduled" );
                newTasksMayBeScheduledField.setAccessible ( true );
                final Field queueField = thread.getClass().getDeclaredField ( "queue" );
                queueField.setAccessible ( true );
                final Object queue = queueField.get ( thread );
                final Method clearMethod = queue.getClass().getDeclaredMethod ( "clear", ( Class<?>[] ) new Class[0] );
                clearMethod.setAccessible ( true );
                synchronized ( queue ) {
                    newTasksMayBeScheduledField.setBoolean ( thread, false );
                    clearMethod.invoke ( queue, new Object[0] );
                    queue.notify();
                }
            } catch ( NoSuchFieldException nfe ) {
                final Method cancelMethod = thread.getClass().getDeclaredMethod ( "cancel", ( Class<?>[] ) new Class[0] );
                synchronized ( thread ) {
                    cancelMethod.setAccessible ( true );
                    cancelMethod.invoke ( thread, new Object[0] );
                }
            }
            WebappClassLoaderBase.log.warn ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.warnTimerThread", this.getContextName(), thread.getName() ) );
        } catch ( Exception e ) {
            final Throwable t = ExceptionUtils.unwrapInvocationTargetException ( e );
            ExceptionUtils.handleThrowable ( t );
            WebappClassLoaderBase.log.warn ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.stopTimerThreadFail", thread.getName(), this.getContextName() ), t );
        }
    }
    private void checkThreadLocalsForLeaks() {
        final Thread[] threads = this.getThreads();
        try {
            final Field threadLocalsField = Thread.class.getDeclaredField ( "threadLocals" );
            threadLocalsField.setAccessible ( true );
            final Field inheritableThreadLocalsField = Thread.class.getDeclaredField ( "inheritableThreadLocals" );
            inheritableThreadLocalsField.setAccessible ( true );
            final Class<?> tlmClass = Class.forName ( "java.lang.ThreadLocal$ThreadLocalMap" );
            final Field tableField = tlmClass.getDeclaredField ( "table" );
            tableField.setAccessible ( true );
            final Method expungeStaleEntriesMethod = tlmClass.getDeclaredMethod ( "expungeStaleEntries", ( Class<?>[] ) new Class[0] );
            expungeStaleEntriesMethod.setAccessible ( true );
            for ( int i = 0; i < threads.length; ++i ) {
                if ( threads[i] != null ) {
                    Object threadLocalMap = threadLocalsField.get ( threads[i] );
                    if ( null != threadLocalMap ) {
                        expungeStaleEntriesMethod.invoke ( threadLocalMap, new Object[0] );
                        this.checkThreadLocalMapForLeaks ( threadLocalMap, tableField );
                    }
                    threadLocalMap = inheritableThreadLocalsField.get ( threads[i] );
                    if ( null != threadLocalMap ) {
                        expungeStaleEntriesMethod.invoke ( threadLocalMap, new Object[0] );
                        this.checkThreadLocalMapForLeaks ( threadLocalMap, tableField );
                    }
                }
            }
        } catch ( Throwable t ) {
            final JreCompat jreCompat = JreCompat.getInstance();
            if ( jreCompat.isInstanceOfInaccessibleObjectException ( t ) ) {
                WebappClassLoaderBase.log.warn ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.addExportsThreadLocal" ) );
            } else {
                ExceptionUtils.handleThrowable ( t );
                WebappClassLoaderBase.log.warn ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.checkThreadLocalsForLeaksFail", this.getContextName() ), t );
            }
        }
    }
    private void checkThreadLocalMapForLeaks ( final Object map, final Field internalTableField ) throws IllegalAccessException, NoSuchFieldException {
        if ( map != null ) {
            final Object[] table = ( Object[] ) internalTableField.get ( map );
            if ( table != null ) {
                for ( int j = 0; j < table.length; ++j ) {
                    final Object obj = table[j];
                    if ( obj != null ) {
                        boolean keyLoadedByWebapp = false;
                        boolean valueLoadedByWebapp = false;
                        final Object key = ( ( Reference ) obj ).get();
                        if ( this.equals ( key ) || this.loadedByThisOrChild ( key ) ) {
                            keyLoadedByWebapp = true;
                        }
                        final Field valueField = obj.getClass().getDeclaredField ( "value" );
                        valueField.setAccessible ( true );
                        final Object value = valueField.get ( obj );
                        if ( this.equals ( value ) || this.loadedByThisOrChild ( value ) ) {
                            valueLoadedByWebapp = true;
                        }
                        if ( keyLoadedByWebapp || valueLoadedByWebapp ) {
                            final Object[] args = new Object[5];
                            args[0] = this.getContextName();
                            if ( key != null ) {
                                args[1] = this.getPrettyClassName ( key.getClass() );
                                try {
                                    args[2] = key.toString();
                                } catch ( Exception e ) {
                                    WebappClassLoaderBase.log.warn ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.checkThreadLocalsForLeaks.badKey", args[1] ), e );
                                    args[2] = WebappClassLoaderBase.sm.getString ( "webappClassLoader.checkThreadLocalsForLeaks.unknown" );
                                }
                            }
                            if ( value != null ) {
                                args[3] = this.getPrettyClassName ( value.getClass() );
                                try {
                                    args[4] = value.toString();
                                } catch ( Exception e ) {
                                    WebappClassLoaderBase.log.warn ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.checkThreadLocalsForLeaks.badValue", args[3] ), e );
                                    args[4] = WebappClassLoaderBase.sm.getString ( "webappClassLoader.checkThreadLocalsForLeaks.unknown" );
                                }
                            }
                            if ( valueLoadedByWebapp ) {
                                WebappClassLoaderBase.log.error ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.checkThreadLocalsForLeaks", args ) );
                            } else if ( value == null ) {
                                if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                                    WebappClassLoaderBase.log.debug ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.checkThreadLocalsForLeaksNull", args ) );
                                }
                            } else if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                                WebappClassLoaderBase.log.debug ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.checkThreadLocalsForLeaksNone", args ) );
                            }
                        }
                    }
                }
            }
        }
    }
    private String getPrettyClassName ( final Class<?> clazz ) {
        String name = clazz.getCanonicalName();
        if ( name == null ) {
            name = clazz.getName();
        }
        return name;
    }
    private String getStackTrace ( final Thread thread ) {
        final StringBuilder builder = new StringBuilder();
        for ( final StackTraceElement ste : thread.getStackTrace() ) {
            builder.append ( "\n " ).append ( ste );
        }
        return builder.toString();
    }
    private boolean loadedByThisOrChild ( final Object o ) {
        if ( o == null ) {
            return false;
        }
        Class<?> clazz;
        if ( o instanceof Class ) {
            clazz = ( Class<?> ) o;
        } else {
            clazz = o.getClass();
        }
        for ( ClassLoader cl = clazz.getClassLoader(); cl != null; cl = cl.getParent() ) {
            if ( cl == this ) {
                return true;
            }
        }
        if ( o instanceof Collection ) {
            final Iterator<?> iter = ( ( Collection ) o ).iterator();
            try {
                while ( iter.hasNext() ) {
                    final Object entry = iter.next();
                    if ( this.loadedByThisOrChild ( entry ) ) {
                        return true;
                    }
                }
            } catch ( ConcurrentModificationException e ) {
                WebappClassLoaderBase.log.warn ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.loadedByThisOrChildFail", clazz.getName(), this.getContextName() ), e );
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
            final String msg = WebappClassLoaderBase.sm.getString ( "webappClassLoader.getThreadGroupError", tg.getName() );
            if ( WebappClassLoaderBase.log.isDebugEnabled() ) {
                WebappClassLoaderBase.log.debug ( msg, se );
            } else {
                WebappClassLoaderBase.log.warn ( msg );
            }
        }
        int threadCountGuess = tg.activeCount() + 50;
        Thread[] threads = new Thread[threadCountGuess];
        for ( int threadCountActual = tg.enumerate ( threads ); threadCountActual == threadCountGuess; threadCountGuess *= 2, threads = new Thread[threadCountGuess], threadCountActual = tg.enumerate ( threads ) ) {}
        return threads;
    }
    private void clearReferencesRmiTargets() {
        throw new IllegalStateException ( "An error occurred while decompiling this method." );
    }
    protected Class<?> findClassInternal ( final String name ) {
        this.checkStateForResourceLoading ( name );
        if ( name == null ) {
            return null;
        }
        final String path = this.binaryNameToPath ( name, true );
        ResourceEntry entry = this.resourceEntries.get ( path );
        WebResource resource = null;
        if ( entry == null ) {
            resource = this.resources.getClassLoaderResource ( path );
            if ( !resource.exists() ) {
                return null;
            }
            entry = new ResourceEntry();
            entry.lastModified = resource.getLastModified();
            synchronized ( this.resourceEntries ) {
                final ResourceEntry entry2 = this.resourceEntries.get ( path );
                if ( entry2 == null ) {
                    this.resourceEntries.put ( path, entry );
                } else {
                    entry = entry2;
                }
            }
        }
        Class<?> clazz = entry.loadedClass;
        if ( clazz != null ) {
            return clazz;
        }
        synchronized ( this.getClassLoadingLock ( name ) ) {
            clazz = entry.loadedClass;
            if ( clazz != null ) {
                return clazz;
            }
            if ( resource == null ) {
                resource = this.resources.getClassLoaderResource ( path );
            }
            if ( !resource.exists() ) {
                return null;
            }
            byte[] binaryContent = resource.getContent();
            final Manifest manifest = resource.getManifest();
            final URL codeBase = resource.getCodeBase();
            final Certificate[] certificates = resource.getCertificates();
            if ( this.transformers.size() > 0 ) {
                final String className = name.endsWith ( ".class" ) ? name.substring ( 0, name.length() - ".class".length() ) : name;
                final String internalName = className.replace ( ".", "/" );
                for ( final ClassFileTransformer transformer : this.transformers ) {
                    try {
                        final byte[] transformed = transformer.transform ( this, internalName, null, null, binaryContent );
                        if ( transformed == null ) {
                            continue;
                        }
                        binaryContent = transformed;
                    } catch ( IllegalClassFormatException e ) {
                        WebappClassLoaderBase.log.error ( WebappClassLoaderBase.sm.getString ( "webappClassLoader.transformError", name ), e );
                        return null;
                    }
                }
            }
            String packageName = null;
            final int pos = name.lastIndexOf ( 46 );
            if ( pos != -1 ) {
                packageName = name.substring ( 0, pos );
            }
            Package pkg = null;
            if ( packageName != null ) {
                pkg = this.getPackage ( packageName );
                if ( pkg == null ) {
                    try {
                        if ( manifest == null ) {
                            this.definePackage ( packageName, null, null, null, null, null, null, null );
                        } else {
                            this.definePackage ( packageName, manifest, codeBase );
                        }
                    } catch ( IllegalArgumentException ex ) {}
                    pkg = this.getPackage ( packageName );
                }
            }
            if ( this.securityManager != null && pkg != null ) {
                boolean sealCheck = true;
                if ( pkg.isSealed() ) {
                    sealCheck = pkg.isSealed ( codeBase );
                } else {
                    sealCheck = ( manifest == null || !this.isPackageSealed ( packageName, manifest ) );
                }
                if ( !sealCheck ) {
                    throw new SecurityException ( "Sealing violation loading " + name + " : Package " + packageName + " is sealed." );
                }
            }
            try {
                clazz = this.defineClass ( name, binaryContent, 0, binaryContent.length, new CodeSource ( codeBase, certificates ) );
            } catch ( UnsupportedClassVersionError ucve ) {
                throw new UnsupportedClassVersionError ( ucve.getLocalizedMessage() + " " + WebappClassLoaderBase.sm.getString ( "webappClassLoader.wrongVersion", name ) );
            }
            entry.loadedClass = clazz;
        }
        return clazz;
    }
    private String binaryNameToPath ( final String binaryName, final boolean withLeadingSlash ) {
        final StringBuilder path = new StringBuilder ( 7 + binaryName.length() );
        if ( withLeadingSlash ) {
            path.append ( '/' );
        }
        path.append ( binaryName.replace ( '.', '/' ) );
        path.append ( ".class" );
        return path.toString();
    }
    private String nameToPath ( final String name ) {
        if ( name.startsWith ( "/" ) ) {
            return name;
        }
        final StringBuilder path = new StringBuilder ( 1 + name.length() );
        path.append ( '/' );
        path.append ( name );
        return path.toString();
    }
    protected boolean isPackageSealed ( final String name, final Manifest man ) {
        final String path = name.replace ( '.', '/' ) + '/';
        Attributes attr = man.getAttributes ( path );
        String sealed = null;
        if ( attr != null ) {
            sealed = attr.getValue ( Attributes.Name.SEALED );
        }
        if ( sealed == null && ( attr = man.getMainAttributes() ) != null ) {
            sealed = attr.getValue ( Attributes.Name.SEALED );
        }
        return "true".equalsIgnoreCase ( sealed );
    }
    protected Class<?> findLoadedClass0 ( final String name ) {
        final String path = this.binaryNameToPath ( name, true );
        final ResourceEntry entry = this.resourceEntries.get ( path );
        if ( entry != null ) {
            return entry.loadedClass;
        }
        return null;
    }
    protected void refreshPolicy() {
        try {
            final Policy policy = Policy.getPolicy();
            policy.refresh();
        } catch ( AccessControlException ex ) {}
    }
    protected boolean filter ( final String name, final boolean isClassName ) {
        if ( name == null ) {
            return false;
        }
        if ( name.startsWith ( "javax" ) ) {
            if ( name.length() == 5 ) {
                return false;
            }
            final char ch = name.charAt ( 5 );
            if ( isClassName && ch == '.' ) {
                if ( name.startsWith ( "servlet.jsp.jstl.", 6 ) ) {
                    return false;
                }
                if ( name.startsWith ( "el.", 6 ) || name.startsWith ( "servlet.", 6 ) || name.startsWith ( "websocket.", 6 ) || name.startsWith ( "security.auth.message.", 6 ) ) {
                    return true;
                }
            } else if ( !isClassName && ch == '/' ) {
                if ( name.startsWith ( "servlet/jsp/jstl/", 6 ) ) {
                    return false;
                }
                if ( name.startsWith ( "el/", 6 ) || name.startsWith ( "servlet/", 6 ) || name.startsWith ( "websocket/", 6 ) || name.startsWith ( "security/auth/message/", 6 ) ) {
                    return true;
                }
            }
        } else if ( name.startsWith ( "org" ) ) {
            if ( name.length() == 3 ) {
                return false;
            }
            final char ch = name.charAt ( 3 );
            if ( isClassName && ch == '.' ) {
                if ( name.startsWith ( "apache.", 4 ) ) {
                    if ( name.startsWith ( "tomcat.jdbc.", 11 ) ) {
                        return false;
                    }
                    if ( name.startsWith ( "el.", 11 ) || name.startsWith ( "catalina.", 11 ) || name.startsWith ( "jasper.", 11 ) || name.startsWith ( "juli.", 11 ) || name.startsWith ( "tomcat.", 11 ) || name.startsWith ( "naming.", 11 ) || name.startsWith ( "coyote.", 11 ) ) {
                        return true;
                    }
                }
            } else if ( !isClassName && ch == '/' && name.startsWith ( "apache/", 4 ) ) {
                if ( name.startsWith ( "tomcat/jdbc/", 11 ) ) {
                    return false;
                }
                if ( name.startsWith ( "el/", 11 ) || name.startsWith ( "catalina/", 11 ) || name.startsWith ( "jasper/", 11 ) || name.startsWith ( "juli/", 11 ) || name.startsWith ( "tomcat/", 11 ) || name.startsWith ( "naming/", 11 ) || name.startsWith ( "coyote/", 11 ) ) {
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    protected void addURL ( final URL url ) {
        super.addURL ( url );
        this.hasExternalRepositories = true;
    }
    @Override
    public String getWebappName() {
        return this.getContextName();
    }
    @Override
    public String getHostName() {
        if ( this.resources != null ) {
            final Container host = this.resources.getContext().getParent();
            if ( host != null ) {
                return host.getName();
            }
        }
        return null;
    }
    @Override
    public String getServiceName() {
        if ( this.resources != null ) {
            final Container host = this.resources.getContext().getParent();
            if ( host != null ) {
                final Container engine = host.getParent();
                if ( engine != null ) {
                    return engine.getName();
                }
            }
        }
        return null;
    }
    static {
        log = LogFactory.getLog ( WebappClassLoaderBase.class );
        JVM_THREAD_GROUP_NAMES = new ArrayList<String>();
        ClassLoader.registerAsParallelCapable();
        WebappClassLoaderBase.JVM_THREAD_GROUP_NAMES.add ( "system" );
        WebappClassLoaderBase.JVM_THREAD_GROUP_NAMES.add ( "RMI Runtime" );
        sm = StringManager.getManager ( "org.apache.catalina.loader" );
    }
    protected class PrivilegedFindClassByName implements PrivilegedAction<Class<?>> {
        protected final String name;
        PrivilegedFindClassByName ( final String name ) {
            this.name = name;
        }
        @Override
        public Class<?> run() {
            return WebappClassLoaderBase.this.findClassInternal ( this.name );
        }
    }
    protected static final class PrivilegedGetClassLoader implements PrivilegedAction<ClassLoader> {
        public final Class<?> clazz;
        public PrivilegedGetClassLoader ( final Class<?> clazz ) {
            this.clazz = clazz;
        }
        @Override
        public ClassLoader run() {
            return this.clazz.getClassLoader();
        }
    }
}
