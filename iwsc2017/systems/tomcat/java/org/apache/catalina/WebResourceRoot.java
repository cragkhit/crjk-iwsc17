package org.apache.catalina;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;
public interface WebResourceRoot extends Lifecycle {
    WebResource getResource ( String path );
    WebResource[] getResources ( String path );
    WebResource getClassLoaderResource ( String path );
    WebResource[] getClassLoaderResources ( String path );
    String[] list ( String path );
    Set<String> listWebAppPaths ( String path );
    WebResource[] listResources ( String path );
    boolean mkdir ( String path );
    boolean write ( String path, InputStream is, boolean overwrite );
    void createWebResourceSet ( ResourceSetType type, String webAppMount, URL url,
                                String internalPath );
    void createWebResourceSet ( ResourceSetType type, String webAppMount,
                                String base, String archivePath, String internalPath );
    void addPreResources ( WebResourceSet webResourceSet );
    WebResourceSet[] getPreResources();
    void addJarResources ( WebResourceSet webResourceSet );
    WebResourceSet[] getJarResources();
    void addPostResources ( WebResourceSet webResourceSet );
    WebResourceSet[] getPostResources();
    Context getContext();
    void setContext ( Context context );
    void setAllowLinking ( boolean allowLinking );
    boolean getAllowLinking();
    void setCachingAllowed ( boolean cachingAllowed );
    boolean isCachingAllowed();
    void setCacheTtl ( long ttl );
    long getCacheTtl();
    void setCacheMaxSize ( long cacheMaxSize );
    long getCacheMaxSize();
    void setCacheObjectMaxSize ( int cacheObjectMaxSize );
    int getCacheObjectMaxSize();
    void setTrackLockedFiles ( boolean trackLockedFiles );
    boolean getTrackLockedFiles();
    void backgroundProcess();
    void registerTrackedResource ( TrackedWebResource trackedResource );
    void deregisterTrackedResource ( TrackedWebResource trackedResource );
    List<URL> getBaseUrls();
    void gc();
    static enum ResourceSetType {
        PRE,
        RESOURCE_JAR,
        POST,
        CLASSES_JAR
    }
}
