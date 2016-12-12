package org.apache.catalina.webresources;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.management.ObjectName;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.TrackedWebResource;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceSet;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.http.RequestUtil;
import org.apache.tomcat.util.res.StringManager;
public class StandardRoot extends LifecycleMBeanBase implements WebResourceRoot {
    private static final Log log = LogFactory.getLog ( StandardRoot.class );
    protected static final StringManager sm = StringManager.getManager ( StandardRoot.class );
    private Context context;
    private boolean allowLinking = false;
    private final List<WebResourceSet> preResources = new ArrayList<>();
    private WebResourceSet main;
    private final List<WebResourceSet> classResources = new ArrayList<>();
    private final List<WebResourceSet> jarResources = new ArrayList<>();
    private final List<WebResourceSet> postResources = new ArrayList<>();
    private final Cache cache = new Cache ( this );
    private boolean cachingAllowed = true;
    private ObjectName cacheJmxName = null;
    private boolean trackLockedFiles = false;
    private final Set<TrackedWebResource> trackedResources =
        Collections.newSetFromMap ( new ConcurrentHashMap<TrackedWebResource, Boolean>() );
    private final List<WebResourceSet> mainResources = new ArrayList<>();
    private final List<List<WebResourceSet>> allResources =
        new ArrayList<>();
    {
        allResources.add ( preResources );
        allResources.add ( mainResources );
        allResources.add ( classResources );
        allResources.add ( jarResources );
        allResources.add ( postResources );
    }
    public StandardRoot() {
    }
    public StandardRoot ( Context context ) {
        this.context = context;
    }
    @Override
    public String[] list ( String path ) {
        return list ( path, true );
    }
    private String[] list ( String path, boolean validate ) {
        if ( validate ) {
            path = validate ( path );
        }
        HashSet<String> result = new LinkedHashSet<>();
        for ( List<WebResourceSet> list : allResources ) {
            for ( WebResourceSet webResourceSet : list ) {
                if ( !webResourceSet.getClassLoaderOnly() ) {
                    String[] entries = webResourceSet.list ( path );
                    for ( String entry : entries ) {
                        result.add ( entry );
                    }
                }
            }
        }
        return result.toArray ( new String[result.size()] );
    }
    @Override
    public Set<String> listWebAppPaths ( String path ) {
        path = validate ( path );
        HashSet<String> result = new HashSet<>();
        for ( List<WebResourceSet> list : allResources ) {
            for ( WebResourceSet webResourceSet : list ) {
                if ( !webResourceSet.getClassLoaderOnly() ) {
                    result.addAll ( webResourceSet.listWebAppPaths ( path ) );
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
        path = validate ( path );
        if ( preResourceExists ( path ) ) {
            return false;
        }
        boolean mkdirResult = main.mkdir ( path );
        if ( mkdirResult && isCachingAllowed() ) {
            cache.removeCacheEntry ( path );
        }
        return mkdirResult;
    }
    @Override
    public boolean write ( String path, InputStream is, boolean overwrite ) {
        path = validate ( path );
        if ( !overwrite && preResourceExists ( path ) ) {
            return false;
        }
        boolean writeResult = main.write ( path, is, overwrite );
        if ( writeResult && isCachingAllowed() ) {
            cache.removeCacheEntry ( path );
        }
        return writeResult;
    }
    private boolean preResourceExists ( String path ) {
        for ( WebResourceSet webResourceSet : preResources ) {
            WebResource webResource = webResourceSet.getResource ( path );
            if ( webResource.exists() ) {
                return true;
            }
        }
        return false;
    }
    @Override
    public WebResource getResource ( String path ) {
        return getResource ( path, true, false );
    }
    private WebResource getResource ( String path, boolean validate,
                                      boolean useClassLoaderResources ) {
        if ( validate ) {
            path = validate ( path );
        }
        if ( isCachingAllowed() ) {
            return cache.getResource ( path, useClassLoaderResources );
        } else {
            return getResourceInternal ( path, useClassLoaderResources );
        }
    }
    @Override
    public WebResource getClassLoaderResource ( String path ) {
        return getResource ( "/WEB-INF/classes" + path, true, true );
    }
    @Override
    public WebResource[] getClassLoaderResources ( String path ) {
        return getResources ( "/WEB-INF/classes" + path, true );
    }
    private String validate ( String path ) {
        if ( !getState().isAvailable() ) {
            throw new IllegalStateException (
                sm.getString ( "standardRoot.checkStateNotStarted" ) );
        }
        if ( path == null || path.length() == 0 || !path.startsWith ( "/" ) ) {
            throw new IllegalArgumentException (
                sm.getString ( "standardRoot.invalidPath", path ) );
        }
        String result;
        if ( File.separatorChar == '\\' ) {
            result = RequestUtil.normalize ( path, true );
        } else {
            result = RequestUtil.normalize ( path, false );
        }
        if ( result == null || result.length() == 0 || !result.startsWith ( "/" ) ) {
            throw new IllegalArgumentException (
                sm.getString ( "standardRoot.invalidPathNormal", path, result ) );
        }
        return result;
    }
    protected final WebResource getResourceInternal ( String path,
            boolean useClassLoaderResources ) {
        WebResource result = null;
        WebResource virtual = null;
        WebResource mainEmpty = null;
        for ( List<WebResourceSet> list : allResources ) {
            for ( WebResourceSet webResourceSet : list ) {
                if ( !useClassLoaderResources &&  !webResourceSet.getClassLoaderOnly() ||
                        useClassLoaderResources && !webResourceSet.getStaticOnly() ) {
                    result = webResourceSet.getResource ( path );
                    if ( result.exists() ) {
                        return result;
                    }
                    if ( virtual == null ) {
                        if ( result.isVirtual() ) {
                            virtual = result;
                        } else if ( main.equals ( webResourceSet ) ) {
                            mainEmpty = result;
                        }
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
    public WebResource[] getResources ( String path ) {
        return getResources ( path, false );
    }
    private WebResource[] getResources ( String path,
                                         boolean useClassLoaderResources ) {
        path = validate ( path );
        if ( isCachingAllowed() ) {
            return cache.getResources ( path, useClassLoaderResources );
        } else {
            return getResourcesInternal ( path, useClassLoaderResources );
        }
    }
    protected WebResource[] getResourcesInternal ( String path,
            boolean useClassLoaderResources ) {
        List<WebResource> result = new ArrayList<>();
        for ( List<WebResourceSet> list : allResources ) {
            for ( WebResourceSet webResourceSet : list ) {
                if ( useClassLoaderResources || !webResourceSet.getClassLoaderOnly() ) {
                    WebResource webResource = webResourceSet.getResource ( path );
                    if ( webResource.exists() ) {
                        result.add ( webResource );
                    }
                }
            }
        }
        if ( result.size() == 0 ) {
            result.add ( main.getResource ( path ) );
        }
        return result.toArray ( new WebResource[result.size()] );
    }
    @Override
    public WebResource[] listResources ( String path ) {
        return listResources ( path, true );
    }
    private WebResource[] listResources ( String path, boolean validate ) {
        if ( validate ) {
            path = validate ( path );
        }
        String[] resources = list ( path, false );
        WebResource[] result = new WebResource[resources.length];
        for ( int i = 0; i < resources.length; i++ ) {
            if ( path.charAt ( path.length() - 1 ) == '/' ) {
                result[i] = getResource ( path + resources[i], false, false );
            } else {
                result[i] = getResource ( path + '/' + resources[i], false, false );
            }
        }
        return result;
    }
    @Override
    public void createWebResourceSet ( ResourceSetType type, String webAppMount,
                                       URL url, String internalPath ) {
        BaseLocation baseLocation = new BaseLocation ( url );
        createWebResourceSet ( type, webAppMount, baseLocation.getBasePath(),
                               baseLocation.getArchivePath(), internalPath );
    }
    @Override
    public void createWebResourceSet ( ResourceSetType type, String webAppMount,
                                       String base, String archivePath, String internalPath ) {
        List<WebResourceSet> resourceList;
        WebResourceSet resourceSet;
        switch ( type ) {
        case PRE:
            resourceList = preResources;
            break;
        case CLASSES_JAR:
            resourceList = classResources;
            break;
        case RESOURCE_JAR:
            resourceList = jarResources;
            break;
        case POST:
            resourceList = postResources;
            break;
        default:
            throw new IllegalArgumentException (
                sm.getString ( "standardRoot.createUnknownType", type ) );
        }
        File file = new File ( base );
        if ( file.isFile() ) {
            if ( archivePath != null ) {
                resourceSet = new JarWarResourceSet ( this, webAppMount, base,
                                                      archivePath, internalPath );
            } else if ( file.getName().toLowerCase ( Locale.ENGLISH ).endsWith ( ".jar" ) ) {
                resourceSet = new JarResourceSet ( this, webAppMount, base,
                                                   internalPath );
            } else {
                resourceSet = new FileResourceSet ( this, webAppMount, base,
                                                    internalPath );
            }
        } else if ( file.isDirectory() ) {
            resourceSet =
                new DirResourceSet ( this, webAppMount, base, internalPath );
        } else {
            throw new IllegalArgumentException (
                sm.getString ( "standardRoot.createInvalidFile", file ) );
        }
        if ( type.equals ( ResourceSetType.CLASSES_JAR ) ) {
            resourceSet.setClassLoaderOnly ( true );
        } else if ( type.equals ( ResourceSetType.RESOURCE_JAR ) ) {
            resourceSet.setStaticOnly ( true );
        }
        resourceList.add ( resourceSet );
    }
    @Override
    public void addPreResources ( WebResourceSet webResourceSet ) {
        webResourceSet.setRoot ( this );
        preResources.add ( webResourceSet );
    }
    @Override
    public WebResourceSet[] getPreResources() {
        return preResources.toArray ( new WebResourceSet[preResources.size()] );
    }
    @Override
    public void addJarResources ( WebResourceSet webResourceSet ) {
        webResourceSet.setRoot ( this );
        jarResources.add ( webResourceSet );
    }
    @Override
    public WebResourceSet[] getJarResources() {
        return jarResources.toArray ( new WebResourceSet[jarResources.size()] );
    }
    @Override
    public void addPostResources ( WebResourceSet webResourceSet ) {
        webResourceSet.setRoot ( this );
        postResources.add ( webResourceSet );
    }
    @Override
    public WebResourceSet[] getPostResources() {
        return postResources.toArray ( new WebResourceSet[postResources.size()] );
    }
    protected WebResourceSet[] getClassResources() {
        return classResources.toArray ( new WebResourceSet[classResources.size()] );
    }
    protected void addClassResources ( WebResourceSet webResourceSet ) {
        webResourceSet.setRoot ( this );
        classResources.add ( webResourceSet );
    }
    @Override
    public void setAllowLinking ( boolean allowLinking ) {
        if ( this.allowLinking != allowLinking && cachingAllowed ) {
            cache.clear();
        }
        this.allowLinking = allowLinking;
    }
    @Override
    public boolean getAllowLinking() {
        return allowLinking;
    }
    @Override
    public void setCachingAllowed ( boolean cachingAllowed ) {
        this.cachingAllowed = cachingAllowed;
        if ( !cachingAllowed ) {
            cache.clear();
        }
    }
    @Override
    public boolean isCachingAllowed() {
        return cachingAllowed;
    }
    @Override
    public long getCacheTtl() {
        return cache.getTtl();
    }
    @Override
    public void setCacheTtl ( long cacheTtl ) {
        cache.setTtl ( cacheTtl );
    }
    @Override
    public long getCacheMaxSize() {
        return cache.getMaxSize();
    }
    @Override
    public void setCacheMaxSize ( long cacheMaxSize ) {
        cache.setMaxSize ( cacheMaxSize );
    }
    @Override
    public void setCacheObjectMaxSize ( int cacheObjectMaxSize ) {
        cache.setObjectMaxSize ( cacheObjectMaxSize );
        if ( getState().isAvailable() ) {
            cache.enforceObjectMaxSizeLimit();
        }
    }
    @Override
    public int getCacheObjectMaxSize() {
        return cache.getObjectMaxSize();
    }
    @Override
    public void setTrackLockedFiles ( boolean trackLockedFiles ) {
        this.trackLockedFiles = trackLockedFiles;
        if ( !trackLockedFiles ) {
            trackedResources.clear();
        }
    }
    @Override
    public boolean getTrackLockedFiles() {
        return trackLockedFiles;
    }
    public List<String> getTrackedResources() {
        List<String> result = new ArrayList<> ( trackedResources.size() );
        for ( TrackedWebResource resource : trackedResources ) {
            result.add ( resource.toString() );
        }
        return result;
    }
    @Override
    public Context getContext() {
        return context;
    }
    @Override
    public void setContext ( Context context ) {
        this.context = context;
    }
    private void processWebInfLib() {
        WebResource[] possibleJars = listResources ( "/WEB-INF/lib", false );
        for ( WebResource possibleJar : possibleJars ) {
            if ( possibleJar.isFile() && possibleJar.getName().endsWith ( ".jar" ) ) {
                createWebResourceSet ( ResourceSetType.CLASSES_JAR,
                                       "/WEB-INF/classes", possibleJar.getURL(), "/" );
            }
        }
    }
    protected final void setMainResources ( WebResourceSet main ) {
        this.main = main;
        mainResources.clear();
        mainResources.add ( main );
    }
    @Override
    public void backgroundProcess() {
        cache.backgroundProcess();
        gc();
    }
    @Override
    public void gc() {
        for ( List<WebResourceSet> list : allResources ) {
            for ( WebResourceSet webResourceSet : list ) {
                webResourceSet.gc();
            }
        }
    }
    @Override
    public void registerTrackedResource ( TrackedWebResource trackedResource ) {
        trackedResources.add ( trackedResource );
    }
    @Override
    public void deregisterTrackedResource ( TrackedWebResource trackedResource ) {
        trackedResources.remove ( trackedResource );
    }
    @Override
    public List<URL> getBaseUrls() {
        List<URL> result = new ArrayList<>();
        for ( List<WebResourceSet> list : allResources ) {
            for ( WebResourceSet webResourceSet : list ) {
                if ( !webResourceSet.getClassLoaderOnly() ) {
                    URL url = webResourceSet.getBaseUrl();
                    if ( url != null ) {
                        result.add ( url );
                    }
                }
            }
        }
        return result;
    }
    @Override
    protected String getDomainInternal() {
        return context.getDomain();
    }
    @Override
    protected String getObjectNameKeyProperties() {
        StringBuilder keyProperties = new StringBuilder ( "type=WebResourceRoot" );
        keyProperties.append ( context.getMBeanKeyProperties() );
        return keyProperties.toString();
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        cacheJmxName = register ( cache, getObjectNameKeyProperties() + ",name=Cache" );
        registerURLStreamHandlerFactory();
        if ( context == null ) {
            throw new IllegalStateException (
                sm.getString ( "standardRoot.noContext" ) );
        }
        for ( List<WebResourceSet> list : allResources ) {
            for ( WebResourceSet webResourceSet : list ) {
                webResourceSet.init();
            }
        }
    }
    protected void registerURLStreamHandlerFactory() {
        TomcatURLStreamHandlerFactory.register();
    }
    @Override
    protected void startInternal() throws LifecycleException {
        mainResources.clear();
        main = createMainResourceSet();
        mainResources.add ( main );
        for ( List<WebResourceSet> list : allResources ) {
            if ( list != classResources ) {
                for ( WebResourceSet webResourceSet : list ) {
                    webResourceSet.start();
                }
            }
        }
        processWebInfLib();
        for ( WebResourceSet classResource : classResources ) {
            classResource.start();
        }
        cache.enforceObjectMaxSizeLimit();
        setState ( LifecycleState.STARTING );
    }
    protected WebResourceSet createMainResourceSet() {
        String docBase = context.getDocBase();
        WebResourceSet mainResourceSet;
        if ( docBase == null ) {
            mainResourceSet = new EmptyResourceSet ( this );
        } else {
            File f = new File ( docBase );
            if ( !f.isAbsolute() ) {
                f = new File ( ( ( Host ) context.getParent() ).getAppBaseFile(), f.getPath() );
            }
            if ( f.isDirectory() ) {
                mainResourceSet = new DirResourceSet ( this, "/", f.getAbsolutePath(), "/" );
            } else if ( f.isFile() && docBase.endsWith ( ".war" ) ) {
                mainResourceSet = new WarResourceSet ( this, "/", f.getAbsolutePath() );
            } else {
                throw new IllegalArgumentException (
                    sm.getString ( "standardRoot.startInvalidMain",
                                   f.getAbsolutePath() ) );
            }
        }
        return mainResourceSet;
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        for ( List<WebResourceSet> list : allResources ) {
            for ( WebResourceSet webResourceSet : list ) {
                webResourceSet.stop();
            }
        }
        if ( main != null ) {
            main.destroy();
        }
        mainResources.clear();
        for ( WebResourceSet webResourceSet : jarResources ) {
            webResourceSet.destroy();
        }
        jarResources.clear();
        for ( WebResourceSet webResourceSet : classResources ) {
            webResourceSet.destroy();
        }
        classResources.clear();
        for ( TrackedWebResource trackedResource : trackedResources ) {
            log.error ( sm.getString ( "standardRoot.lockedFile",
                                       context.getName(),
                                       trackedResource.getName() ),
                        trackedResource.getCreatedBy() );
            try {
                trackedResource.close();
            } catch ( IOException e ) {
            }
        }
        cache.clear();
        setState ( LifecycleState.STOPPING );
    }
    @Override
    protected void destroyInternal() throws LifecycleException {
        for ( List<WebResourceSet> list : allResources ) {
            for ( WebResourceSet webResourceSet : list ) {
                webResourceSet.destroy();
            }
        }
        unregister ( cacheJmxName );
        super.destroyInternal();
    }
    static class BaseLocation {
        private final String basePath;
        private final String archivePath;
        BaseLocation ( URL url ) {
            File f = null;
            if ( "jar".equals ( url.getProtocol() ) || "war".equals ( url.getProtocol() ) ) {
                String jarUrl = url.toString();
                int endOfFileUrl = -1;
                if ( "jar".equals ( url.getProtocol() ) ) {
                    endOfFileUrl = jarUrl.indexOf ( "!/" );
                } else {
                    endOfFileUrl = jarUrl.indexOf ( "*/" );
                }
                String fileUrl = jarUrl.substring ( 4, endOfFileUrl );
                try {
                    f = new File ( new URL ( fileUrl ).toURI() );
                } catch ( MalformedURLException | URISyntaxException e ) {
                    throw new IllegalArgumentException ( e );
                }
                int startOfArchivePath = endOfFileUrl + 2;
                if ( jarUrl.length() >  startOfArchivePath ) {
                    archivePath = jarUrl.substring ( startOfArchivePath );
                } else {
                    archivePath = null;
                }
            } else if ( "file".equals ( url.getProtocol() ) ) {
                try {
                    f = new File ( url.toURI() );
                } catch ( URISyntaxException e ) {
                    throw new IllegalArgumentException ( e );
                }
                archivePath = null;
            } else {
                throw new IllegalArgumentException ( sm.getString (
                        "standardRoot.unsupportedProtocol", url.getProtocol() ) );
            }
            basePath = f.getAbsolutePath();
        }
        String getBasePath() {
            return basePath;
        }
        String getArchivePath() {
            return archivePath;
        }
    }
}
