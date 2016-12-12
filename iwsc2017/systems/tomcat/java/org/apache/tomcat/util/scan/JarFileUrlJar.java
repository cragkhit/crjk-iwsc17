package org.apache.tomcat.util.scan;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import org.apache.tomcat.Jar;
public class JarFileUrlJar implements Jar {
    private final JarFile jarFile;
    private final URL jarFileURL;
    private Enumeration<JarEntry> entries;
    private JarEntry entry = null;
    public JarFileUrlJar ( URL url, boolean startsWithJar ) throws IOException {
        if ( startsWithJar ) {
            JarURLConnection jarConn = ( JarURLConnection ) url.openConnection();
            jarConn.setUseCaches ( false );
            jarFile = jarConn.getJarFile();
            jarFileURL = jarConn.getJarFileURL();
        } else {
            File f;
            try {
                f = new File ( url.toURI() );
            } catch ( URISyntaxException e ) {
                throw new IOException ( e );
            }
            jarFile = new JarFile ( f );
            jarFileURL = url;
        }
    }
    @Override
    public URL getJarFileURL() {
        return jarFileURL;
    }
    @Override
    public boolean entryExists ( String name ) {
        ZipEntry entry = jarFile.getEntry ( name );
        return entry != null;
    }
    @Override
    public InputStream getInputStream ( String name ) throws IOException {
        ZipEntry entry = jarFile.getEntry ( name );
        if ( entry == null ) {
            return null;
        } else {
            return jarFile.getInputStream ( entry );
        }
    }
    @Override
    public long getLastModified ( String name ) throws IOException {
        ZipEntry entry = jarFile.getEntry ( name );
        if ( entry == null ) {
            return -1;
        } else {
            return entry.getTime();
        }
    }
    @Override
    public String getURL ( String entry ) {
        StringBuilder result = new StringBuilder ( "jar:" );
        result.append ( getJarFileURL().toExternalForm() );
        result.append ( "!/" );
        result.append ( entry );
        return result.toString();
    }
    @Override
    public void close() {
        if ( jarFile != null ) {
            try {
                jarFile.close();
            } catch ( IOException e ) {
            }
        }
    }
    @Override
    public void nextEntry() {
        if ( entries == null ) {
            entries = jarFile.entries();
        }
        if ( entries.hasMoreElements() ) {
            entry = entries.nextElement();
        } else {
            entry = null;
        }
    }
    @Override
    public String getEntryName() {
        if ( entry == null ) {
            return null;
        } else {
            return entry.getName();
        }
    }
    @Override
    public InputStream getEntryInputStream() throws IOException {
        if ( entry == null ) {
            return null;
        } else {
            return jarFile.getInputStream ( entry );
        }
    }
    @Override
    public Manifest getManifest() throws IOException {
        return jarFile.getManifest();
    }
    @Override
    public void reset() throws IOException {
        entries = null;
        entry = null;
    }
}
