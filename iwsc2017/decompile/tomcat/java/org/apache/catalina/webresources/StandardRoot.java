package org.apache.catalina.webresources;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import org.apache.juli.logging.LogFactory;
import java.io.IOException;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.LifecycleException;
import java.util.Locale;
import java.net.URL;
import org.apache.tomcat.util.http.RequestUtil;
import java.io.File;
import org.apache.catalina.WebResource;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import org.apache.catalina.TrackedWebResource;
import java.util.Set;
import javax.management.ObjectName;
import org.apache.catalina.WebResourceSet;
import java.util.List;
import org.apache.catalina.Context;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.util.LifecycleMBeanBase;
public class StandardRoot extends LifecycleMBeanBase implements WebResourceRoot {
    private static final Log log;
    protected static final StringManager sm;
    private Context context;
    private boolean allowLinking;
    private final List<WebResourceSet> preResources;
    private WebResourceSet main;
    private final List<WebResourceSet> classResources;
    private final List<WebResourceSet> jarResources;
    private final List<WebResourceSet> postResources;
    private final Cache cache;
    private boolean cachingAllowed;
    private ObjectName cacheJmxName;
    private boolean trackLockedFiles;
    private final Set<TrackedWebResource> trackedResources;
    private final List<WebResourceSet> mainResources;
    private final List<List<WebResourceSet>> allResources;
    public StandardRoot() {
        this.allowLinking = false;
        this.preResources = new ArrayList<WebResourceSet>();
        this.classResources = new ArrayList<WebResourceSet>();
        this.jarResources = new ArrayList<WebResourceSet>();
        this.postResources = new ArrayList<WebResourceSet>();
        this.cache = new Cache ( this );
        this.cachingAllowed = true;
        this.cacheJmxName = null;
        this.trackLockedFiles = false;
        this.trackedResources = Collections.newSetFromMap ( new ConcurrentHashMap<TrackedWebResource, Boolean>() );
        this.mainResources = new ArrayList<WebResourceSet>();
        ( this.allResources = new ArrayList<List<WebResourceSet>>() ).add ( this.preResources );
        this.allResources.add ( this.mainResources );
        this.allResources.add ( this.classResources );
        this.allResources.add ( this.jarResources );
        this.allResources.add ( this.postResources );
    }
    public StandardRoot ( final Context context ) {
        this.allowLinking = false;
        this.preResources = new ArrayList<WebResourceSet>();
        this.classResources = new ArrayList<WebResourceSet>();
        this.jarResources = new ArrayList<WebResourceSet>();
        this.postResources = new ArrayList<WebResourceSet>();
        this.cache = new Cache ( this );
        this.cachingAllowed = true;
        this.cacheJmxName = null;
        this.trackLockedFiles = false;
        this.trackedResources = Collections.newSetFromMap ( new ConcurrentHashMap<TrackedWebResource, Boolean>() );
        this.mainResources = new ArrayList<WebResourceSet>();
        ( this.allResources = new ArrayList<List<WebResourceSet>>() ).add ( this.preResources );
        this.allResources.add ( this.mainResources );
        this.allResources.add ( this.classResources );
        this.allResources.add ( this.jarResources );
        this.allResources.add ( this.postResources );
        this.context = context;
    }
    @Override
    public String[] list ( final String path ) {
        return this.list ( path, true );
    }
    private String[] list ( String path, final boolean validate ) {
        if ( validate ) {
            path = this.validate ( path );
        }
        final HashSet<String> result = new LinkedHashSet<String>();
        for ( final List<WebResourceSet> list : this.allResources ) {
            for ( final WebResourceSet webResourceSet : list ) {
                if ( !webResourceSet.getClassLoaderOnly() ) {
                    final String[] list2;
                    final String[] entries = list2 = webResourceSet.list ( path );
                    for ( final String entry : list2 ) {
                        result.add ( entry );
                    }
                }
            }
        }
        return result.toArray ( new String[result.size()] );
    }
    @Override
    public Set<String> listWebAppPaths ( String path ) {
        path = this.validate ( path );
        final HashSet<String> result = new HashSet<String>();
        for ( final List<WebResourceSet> list : this.allResources ) {
            for ( final WebResourceSet webResourceSet : list ) {
                if ( !webResourceSet.getClassLoaderOnly() ) {
                    result.addAll ( ( Collection<?> ) webResourceSet.listWebAppPaths ( path ) );
                }
            }
        }
        if ( result.size() == 0 ) {
            return null;
        }
        return result;
    }
    @Override
    public boolean mkdir ( String path ) {
        path = this.validate ( path );
        if ( this.preResourceExists ( path ) ) {
            return false;
        }
        final boolean mkdirResult = this.main.mkdir ( path );
        if ( mkdirResult && this.isCachingAllowed() ) {
            this.cache.removeCacheEntry ( path );
        }
        return mkdirResult;
    }
    @Override
    public boolean write ( String path, final InputStream is, final boolean overwrite ) {
        path = this.validate ( path );
        if ( !overwrite && this.preResourceExists ( path ) ) {
            return false;
        }
        final boolean writeResult = this.main.write ( path, is, overwrite );
        if ( writeResult && this.isCachingAllowed() ) {
            this.cache.removeCacheEntry ( path );
        }
        return writeResult;
    }
    private boolean preResourceExists ( final String path ) {
        for ( final WebResourceSet webResourceSet : this.preResources ) {
            final WebResource webResource = webResourceSet.getResource ( path );
            if ( webResource.exists() ) {
                return true;
            }
        }
        return false;
    }
    @Override
    public WebResource getResource ( final String path ) {
        return this.getResource ( path, true, false );
    }
    private WebResource getResource ( String path, final boolean validate, final boolean useClassLoaderResources ) {
        if ( validate ) {
            path = this.validate ( path );
        }
        if ( this.isCachingAllowed() ) {
            return this.cache.getResource ( path, useClassLoaderResources );
        }
        return this.getResourceInternal ( path, useClassLoaderResources );
    }
    @Override
    public WebResource getClassLoaderResource ( final String path ) {
        return this.getResource ( "/WEB-INF/classes" + path, true, true );
    }
    @Override
    public WebResource[] getClassLoaderResources ( final String path ) {
        return this.getResources ( "/WEB-INF/classes" + path, true );
    }
    private String validate ( final String path ) {
        if ( !this.getState().isAvailable() ) {
            throw new IllegalStateException ( StandardRoot.sm.getString ( "standardRoot.checkStateNotStarted" ) );
        }
        if ( path == null || path.length() == 0 || !path.startsWith ( "/" ) ) {
            throw new IllegalArgumentException ( StandardRoot.sm.getString ( "standardRoot.invalidPath", path ) );
        }
        String result;
        if ( File.separatorChar == '\\' ) {
            result = RequestUtil.normalize ( path, true );
        } else {
            result = RequestUtil.normalize ( path, false );
        }
        if ( result == null || result.length() == 0 || !result.startsWith ( "/" ) ) {
            throw new IllegalArgumentException ( StandardRoot.sm.getString ( "standardRoot.invalidPathNormal", path, result ) );
        }
        return result;
    }
    protected final WebResource getResourceInternal ( final String path, final boolean useClassLoaderResources ) {
        WebResource result = null;
        WebResource virtual = null;
        WebResource mainEmpty = null;
        for ( final List<WebResourceSet> list : this.allResources ) {
            for ( final WebResourceSet webResourceSet : list ) {
                if ( ( !useClassLoaderResources && !webResourceSet.getClassLoaderOnly() ) || ( useClassLoaderResources && !webResourceSet.getStaticOnly() ) ) {
                    result = webResourceSet.getResource ( path );
                    if ( result.exists() ) {
                        return result;
                    }
                    if ( virtual != null ) {
                        continue;
                    }
                    if ( result.isVirtual() ) {
                        virtual = result;
                    } else {
                        if ( !this.main.equals ( webResourceSet ) ) {
                            continue;
                        }
                        mainEmpty = result;
                    }
                }
            }
        }
        if ( virtual != null ) {
            return virtual;
        }
        return mainEmpty;
    }
    @Override
    public WebResource[] getResources ( final String path ) {
        return this.getResources ( path, false );
    }
    private WebResource[] getResources ( String path, final boolean useClassLoaderResources ) {
        path = this.validate ( path );
        if ( this.isCachingAllowed() ) {
            return this.cache.getResources ( path, useClassLoaderResources );
        }
        return this.getResourcesInternal ( path, useClassLoaderResources );
    }
    protected WebResource[] getResourcesInternal ( final String path, final boolean useClassLoaderResources ) {
        final List<WebResource> result = new ArrayList<WebResource>();
        for ( final List<WebResourceSet> list : this.allResources ) {
            for ( final WebResourceSet webResourceSet : list ) {
                if ( useClassLoaderResources || !webResourceSet.getClassLoaderOnly() ) {
                    final WebResource webResource = webResourceSet.getResource ( path );
                    if ( !webResource.exists() ) {
                        continue;
                    }
                    result.add ( webResource );
                }
            }
        }
        if ( result.size() == 0 ) {
            result.add ( this.main.getResource ( path ) );
        }
        return result.toArray ( new WebResource[result.size()] );
    }
    @Override
    public WebResource[] listResources ( final String path ) {
        return this.listResources ( path, true );
    }
    private WebResource[] listResources ( String path, final boolean validate ) {
        if ( validate ) {
            path = this.validate ( path );
        }
        final String[] resources = this.list ( path, false );
        final WebResource[] result = new WebResource[resources.length];
        for ( int i = 0; i < resources.length; ++i ) {
            if ( path.charAt ( path.length() - 1 ) == '/' ) {
                result[i] = this.getResource ( path + resources[i], false, false );
            } else {
                result[i] = this.getResource ( path + '/' + resources[i], false, false );
            }
        }
        return result;
    }
    @Override
    public void createWebResourceSet ( final ResourceSetType type, final String webAppMount, final URL url, final String internalPath ) {
        final BaseLocation baseLocation = new BaseLocation ( url );
        this.createWebResourceSet ( type, webAppMount, baseLocation.getBasePath(), baseLocation.getArchivePath(), internalPath );
    }
    @Override
    public void createWebResourceSet ( final ResourceSetType type, final String webAppMount, final String base, final String archivePath, final String internalPath ) {
        List<WebResourceSet> resourceList = null;
        switch ( type ) {
        case PRE: {
            resourceList = this.preResources;
            break;
        }
        case CLASSES_JAR: {
            resourceList = this.classResources;
            break;
        }
        case RESOURCE_JAR: {
            resourceList = this.jarResources;
            break;
        }
        case POST: {
            resourceList = this.postResources;
            break;
        }
        default: {
            throw new IllegalArgumentException ( StandardRoot.sm.getString ( "standardRoot.createUnknownType", type ) );
        }
        }
        final File file = new File ( base );
        WebResourceSet resourceSet;
        if ( file.isFile() ) {
            if ( archivePath != null ) {
                resourceSet = new JarWarResourceSet ( this, webAppMount, base, archivePath, internalPath );
            } else if ( file.getName().toLowerCase ( Locale.ENGLISH ).endsWith ( ".jar" ) ) {
                resourceSet = new JarResourceSet ( this, webAppMount, base, internalPath );
            } else {
                resourceSet = new FileResourceSet ( this, webAppMount, base, internalPath );
            }
        } else {
            if ( !file.isDirectory() ) {
                throw new IllegalArgumentException ( StandardRoot.sm.getString ( "standardRoot.createInvalidFile", file ) );
            }
            resourceSet = new DirResourceSet ( this, webAppMount, base, internalPath );
        }
        if ( type.equals ( ResourceSetType.CLASSES_JAR ) ) {
            resourceSet.setClassLoaderOnly ( true );
        } else if ( type.equals ( ResourceSetType.RESOURCE_JAR ) ) {
            resourceSet.setStaticOnly ( true );
        }
        resourceList.add ( resourceSet );
    }
    @Override
    public void addPreResources ( final WebResourceSet webResourceSet ) {
        webResourceSet.setRoot ( this );
        this.preResources.add ( webResourceSet );
    }
    @Override
    public WebResourceSet[] getPreResources() {
        return this.preResources.toArray ( new WebResourceSet[this.preResources.size()] );
    }
    @Override
    public void addJarResources ( final WebResourceSet webResourceSet ) {
        webResourceSet.setRoot ( this );
        this.jarResources.add ( webResourceSet );
    }
    @Override
    public WebResourceSet[] getJarResources() {
        return this.jarResources.toArray ( new WebResourceSet[this.jarResources.size()] );
    }
    @Override
    public void addPostResources ( final WebResourceSet webResourceSet ) {
        webResourceSet.setRoot ( this );
        this.postResources.add ( webResourceSet );
    }
    @Override
    public WebResourceSet[] getPostResources() {
        return this.postResources.toArray ( new WebResourceSet[this.postResources.size()] );
    }
    protected WebResourceSet[] getClassResources() {
        return this.classResources.toArray ( new WebResourceSet[this.classResources.size()] );
    }
    protected void addClassResources ( final WebResourceSet webResourceSet ) {
        webResourceSet.setRoot ( this );
        this.classResources.add ( webResourceSet );
    }
    @Override
    public void setAllowLinking ( final boolean allowLinking ) {
        if ( this.allowLinking != allowLinking && this.cachingAllowed ) {
            this.cache.clear();
        }
        this.allowLinking = allowLinking;
    }
    @Override
    public boolean getAllowLinking() {
        return this.allowLinking;
    }
    @Override
    public void setCachingAllowed ( final boolean cachingAllowed ) {
        if ( ! ( this.cachingAllowed = cachingAllowed ) ) {
            this.cache.clear();
        }
    }
    @Override
    public boolean isCachingAllowed() {
        return this.cachingAllowed;
    }
    @Override
    public long getCacheTtl() {
        return this.cache.getTtl();
    }
    @Override
    public void setCacheTtl ( final long cacheTtl ) {
        this.cache.setTtl ( cacheTtl );
    }
    @Override
    public long getCacheMaxSize() {
        return this.cache.getMaxSize();
    }
    @Override
    public void setCacheMaxSize ( final long cacheMaxSize ) {
        this.cache.setMaxSize ( cacheMaxSize );
    }
    @Override
    public void setCacheObjectMaxSize ( final int cacheObjectMaxSize ) {
        this.cache.setObjectMaxSize ( cacheObjectMaxSize );
        if ( this.getState().isAvailable() ) {
            this.cache.enforceObjectMaxSizeLimit();
        }
    }
    @Override
    public int getCacheObjectMaxSize() {
        return this.cache.getObjectMaxSize();
    }
    @Override
    public void setTrackLockedFiles ( final boolean trackLockedFiles ) {
        if ( ! ( this.trackLockedFiles = trackLockedFiles ) ) {
            this.trackedResources.clear();
        }
    }
    @Override
    public boolean getTrackLockedFiles() {
        return this.trackLockedFiles;
    }
    public List<String> getTrackedResources() {
        final List<String> result = new ArrayList<String> ( this.trackedResources.size() );
        for ( final TrackedWebResource resource : this.trackedResources ) {
            result.add ( resource.toString() );
        }
        return result;
    }
    @Override
    public Context getContext() {
        return this.context;
    }
    @Override
    public void setContext ( final Context context ) {
        this.context = context;
    }
    private void processWebInfLib() {
        final WebResource[] listResources;
        final WebResource[] possibleJars = listResources = this.listResources ( "/WEB-INF/lib", false );
        for ( final WebResource possibleJar : listResources ) {
            if ( possibleJar.isFile() && possibleJar.getName().endsWith ( ".jar" ) ) {
                this.createWebResourceSet ( ResourceSetType.CLASSES_JAR, "/WEB-INF/classes", possibleJar.getURL(), "/" );
            }
        }
    }
    protected final void setMainResources ( final WebResourceSet main ) {
        this.main = main;
        this.mainResources.clear();
        this.mainResources.add ( main );
    }
    @Override
    public void backgroundProcess() {
        this.cache.backgroundProcess();
        this.gc();
    }
    @Override
    public void gc() {
        for ( final List<WebResourceSet> list : this.allResources ) {
            for ( final WebResourceSet webResourceSet : list ) {
                webResourceSet.gc();
            }
        }
    }
    @Override
    public void registerTrackedResource ( final TrackedWebResource trackedResource ) {
        this.trackedResources.add ( trackedResource );
    }
    @Override
    public void deregisterTrackedResource ( final TrackedWebResource trackedResource ) {
        this.trackedResources.remove ( trackedResource );
    }
    @Override
    public List<URL> getBaseUrls() {
        final List<URL> result = new ArrayList<URL>();
        for ( final List<WebResourceSet> list : this.allResources ) {
            for ( final WebResourceSet webResourceSet : list ) {
                if ( !webResourceSet.getClassLoaderOnly() ) {
                    final URL url = webResourceSet.getBaseUrl();
                    if ( url == null ) {
                        continue;
                    }
                    result.add ( url );
                }
            }
        }
        return result;
    }
    @Override
    protected String getDomainInternal() {
        return this.context.getDomain();
    }
    @Override
    protected String getObjectNameKeyProperties() {
        final StringBuilder keyProperties = new StringBuilder ( "type=WebResourceRoot" );
        keyProperties.append ( this.context.getMBeanKeyProperties() );
        return keyProperties.toString();
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        this.cacheJmxName = this.register ( this.cache, this.getObjectNameKeyProperties() + ",name=Cache" );
        this.registerURLStreamHandlerFactory();
        if ( this.context == null ) {
            throw new IllegalStateException ( StandardRoot.sm.getString ( "standardRoot.noContext" ) );
        }
        for ( final List<WebResourceSet> list : this.allResources ) {
            for ( final WebResourceSet webResourceSet : list ) {
                webResourceSet.init();
            }
        }
    }
    protected void registerURLStreamHandlerFactory() {
        TomcatURLStreamHandlerFactory.register();
    }
    @Override
    protected void startInternal() throws LifecycleException {
        this.mainResources.clear();
        this.main = this.createMainResourceSet();
        this.mainResources.add ( this.main );
        for ( final List<WebResourceSet> list : this.allResources ) {
            if ( list != this.classResources ) {
                for ( final WebResourceSet webResourceSet : list ) {
                    webResourceSet.start();
                }
            }
        }
        this.processWebInfLib();
        for ( final WebResourceSet classResource : this.classResources ) {
            classResource.start();
        }
        this.cache.enforceObjectMaxSizeLimit();
        this.setState ( LifecycleState.STARTING );
    }
    protected WebResourceSet createMainResourceSet() {
        final String docBase = this.context.getDocBase();
        WebResourceSet mainResourceSet;
        if ( docBase == null ) {
            mainResourceSet = new EmptyResourceSet ( this );
        } else {
            File f = new File ( docBase );
            if ( !f.isAbsolute() ) {
                f = new File ( ( ( Host ) this.context.getParent() ).getAppBaseFile(), f.getPath() );
            }
            if ( f.isDirectory() ) {
                mainResourceSet = new DirResourceSet ( this, "/", f.getAbsolutePath(), "/" );
            } else {
                if ( !f.isFile() || !docBase.endsWith ( ".war" ) ) {
                    throw new IllegalArgumentException ( StandardRoot.sm.getString ( "standardRoot.startInvalidMain", f.getAbsolutePath() ) );
                }
                mainResourceSet = new WarResourceSet ( this, "/", f.getAbsolutePath() );
            }
        }
        return mainResourceSet;
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        for ( final List<WebResourceSet> list : this.allResources ) {
            for ( final WebResourceSet webResourceSet : list ) {
                webResourceSet.stop();
            }
        }
        if ( this.main != null ) {
            this.main.destroy();
        }
        this.mainResources.clear();
        for ( final WebResourceSet webResourceSet2 : this.jarResources ) {
            webResourceSet2.destroy();
        }
        this.jarResources.clear();
        for ( final WebResourceSet webResourceSet2 : this.classResources ) {
            webResourceSet2.destroy();
        }
        this.classResources.clear();
        for ( final TrackedWebResource trackedResource : this.trackedResources ) {
            StandardRoot.log.error ( StandardRoot.sm.getString ( "standardRoot.lockedFile", this.context.getName(), trackedResource.getName() ), trackedResource.getCreatedBy() );
            try {
                trackedResource.close();
            } catch ( IOException ex ) {}
        }
        this.cache.clear();
        this.setState ( LifecycleState.STOPPING );
    }
    @Override
    protected void destroyInternal() throws LifecycleException {
        for ( final List<WebResourceSet> list : this.allResources ) {
            for ( final WebResourceSet webResourceSet : list ) {
                webResourceSet.destroy();
            }
        }
        this.unregister ( this.cacheJmxName );
        super.destroyInternal();
    }
    static {
        log = LogFactory.getLog ( StandardRoot.class );
        sm = StringManager.getManager ( StandardRoot.class );
    }
    static class BaseLocation {
        private final String basePath;
        private final String archivePath;
        BaseLocation ( final URL url ) {
            File f = null;
            if ( "jar".equals ( url.getProtocol() ) || "war".equals ( url.getProtocol() ) ) {
                final String jarUrl = url.toString();
                int endOfFileUrl = -1;
                if ( "jar".equals ( url.getProtocol() ) ) {
                    endOfFileUrl = jarUrl.indexOf ( "!/" );
                } else {
                    endOfFileUrl = jarUrl.indexOf ( "*/" );
                }
                final String fileUrl = jarUrl.substring ( 4, endOfFileUrl );
                try {
                    f = new File ( new URL ( fileUrl ).toURI() );
                } catch ( MalformedURLException | URISyntaxException e ) {
                    throw new IllegalArgumentException ( e );
                }
                final int startOfArchivePath = endOfFileUrl + 2;
                if ( jarUrl.length() > startOfArchivePath ) {
                    this.archivePath = jarUrl.substring ( startOfArchivePath );
                } else {
                    this.archivePath = null;
                }
            } else {
                if ( !"file".equals ( url.getProtocol() ) ) {
                    throw new IllegalArgumentException ( StandardRoot.sm.getString ( "standardRoot.unsupportedProtocol", url.getProtocol() ) );
                }
                try {
                    f = new File ( url.toURI() );
                } catch ( URISyntaxException e2 ) {
                    throw new IllegalArgumentException ( e2 );
                }
                this.archivePath = null;
            }
            this.basePath = f.getAbsolutePath();
        }
        String getBasePath() {
            return this.basePath;
        }
        String getArchivePath() {
            return this.archivePath;
        }
    }
}
