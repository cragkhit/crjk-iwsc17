package org.apache.tomcat;
import java.util.jar.Manifest;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
public interface Jar extends AutoCloseable {
    URL getJarFileURL();
    boolean entryExists ( String p0 ) throws IOException;
    InputStream getInputStream ( String p0 ) throws IOException;
    long getLastModified ( String p0 ) throws IOException;
    void close();
    void nextEntry();
    String getEntryName();
    InputStream getEntryInputStream() throws IOException;
    String getURL ( String p0 );
    Manifest getManifest() throws IOException;
    void reset() throws IOException;
}
