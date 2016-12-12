package org.apache.catalina;
import java.net.URL;
import java.io.InputStream;
import java.util.Set;
public interface WebResourceSet extends Lifecycle {
    WebResource getResource ( String p0 );
    String[] list ( String p0 );
    Set<String> listWebAppPaths ( String p0 );
    boolean mkdir ( String p0 );
    boolean write ( String p0, InputStream p1, boolean p2 );
    void setRoot ( WebResourceRoot p0 );
    boolean getClassLoaderOnly();
    void setClassLoaderOnly ( boolean p0 );
    boolean getStaticOnly();
    void setStaticOnly ( boolean p0 );
    URL getBaseUrl();
    void setReadOnly ( boolean p0 );
    boolean isReadOnly();
    void gc();
}
