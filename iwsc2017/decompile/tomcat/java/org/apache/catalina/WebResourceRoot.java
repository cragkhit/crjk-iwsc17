package org.apache.catalina;
import java.util.List;
import java.net.URL;
import java.io.InputStream;
import java.util.Set;
public interface WebResourceRoot extends Lifecycle {
    WebResource getResource ( String p0 );
    WebResource[] getResources ( String p0 );
    WebResource getClassLoaderResource ( String p0 );
    WebResource[] getClassLoaderResources ( String p0 );
    String[] list ( String p0 );
    Set<String> listWebAppPaths ( String p0 );
    WebResource[] listResources ( String p0 );
    boolean mkdir ( String p0 );
    boolean write ( String p0, InputStream p1, boolean p2 );
    void createWebResourceSet ( ResourceSetType p0, String p1, URL p2, String p3 );
    void createWebResourceSet ( ResourceSetType p0, String p1, String p2, String p3, String p4 );
    void addPreResources ( WebResourceSet p0 );
    WebResourceSet[] getPreResources();
    void addJarResources ( WebResourceSet p0 );
    WebResourceSet[] getJarResources();
    void addPostResources ( WebResourceSet p0 );
    WebResourceSet[] getPostResources();
    Context getContext();
    void setContext ( Context p0 );
    void setAllowLinking ( boolean p0 );
    boolean getAllowLinking();
    void setCachingAllowed ( boolean p0 );
    boolean isCachingAllowed();
    void setCacheTtl ( long p0 );
    long getCacheTtl();
    void setCacheMaxSize ( long p0 );
    long getCacheMaxSize();
    void setCacheObjectMaxSize ( int p0 );
    int getCacheObjectMaxSize();
    void setTrackLockedFiles ( boolean p0 );
    boolean getTrackLockedFiles();
    void backgroundProcess();
    void registerTrackedResource ( TrackedWebResource p0 );
    void deregisterTrackedResource ( TrackedWebResource p0 );
    List<URL> getBaseUrls();
    void gc();
    public enum ResourceSetType {
        PRE,
        RESOURCE_JAR,
        POST,
        CLASSES_JAR;
    }
}
