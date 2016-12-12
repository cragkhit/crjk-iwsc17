package org.apache.tomcat;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Manifest;
public interface Jar extends AutoCloseable {
    URL getJarFileURL();
    boolean entryExists ( String name ) throws IOException;
    InputStream getInputStream ( String name ) throws IOException;
    long getLastModified ( String name ) throws IOException;
    @Override
    void close();
    void nextEntry();
    String getEntryName();
    InputStream getEntryInputStream() throws IOException;
    String getURL ( String entry );
    Manifest getManifest() throws IOException;
    void reset() throws IOException;
}
