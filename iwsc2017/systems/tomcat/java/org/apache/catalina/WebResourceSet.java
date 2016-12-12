package org.apache.catalina;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;
public interface WebResourceSet extends Lifecycle {
    WebResource getResource ( String path );
    String[] list ( String path );
    Set<String> listWebAppPaths ( String path );
    boolean mkdir ( String path );
    boolean write ( String path, InputStream is, boolean overwrite );
    void setRoot ( WebResourceRoot root );
    boolean getClassLoaderOnly();
    void setClassLoaderOnly ( boolean classLoaderOnly );
    boolean getStaticOnly();
    void setStaticOnly ( boolean staticOnly );
    URL getBaseUrl();
    void setReadOnly ( boolean readOnly );
    boolean isReadOnly();
    void gc();
}
