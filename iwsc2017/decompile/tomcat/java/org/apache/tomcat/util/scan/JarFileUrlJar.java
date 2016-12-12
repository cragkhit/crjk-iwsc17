package org.apache.tomcat.util.scan;
import java.util.jar.Manifest;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.net.URISyntaxException;
import java.io.IOException;
import java.io.File;
import java.net.JarURLConnection;
import java.util.jar.JarEntry;
import java.util.Enumeration;
import java.net.URL;
import java.util.jar.JarFile;
import org.apache.tomcat.Jar;
public class JarFileUrlJar implements Jar {
    private final JarFile jarFile;
    private final URL jarFileURL;
    private Enumeration<JarEntry> entries;
    private JarEntry entry;
    public JarFileUrlJar ( final URL url, final boolean startsWithJar ) throws IOException {
        this.entry = null;
        if ( startsWithJar ) {
            final JarURLConnection jarConn = ( JarURLConnection ) url.openConnection();
            jarConn.setUseCaches ( false );
            this.jarFile = jarConn.getJarFile();
            this.jarFileURL = jarConn.getJarFileURL();
        } else {
            File f;
            try {
                f = new File ( url.toURI() );
            } catch ( URISyntaxException e ) {
                throw new IOException ( e );
            }
            this.jarFile = new JarFile ( f );
            this.jarFileURL = url;
        }
    }
    @Override
    public URL getJarFileURL() {
        return this.jarFileURL;
    }
    @Override
    public boolean entryExists ( final String name ) {
        final ZipEntry entry = this.jarFile.getEntry ( name );
        return entry != null;
    }
    @Override
    public InputStream getInputStream ( final String name ) throws IOException {
        final ZipEntry entry = this.jarFile.getEntry ( name );
        if ( entry == null ) {
            return null;
        }
        return this.jarFile.getInputStream ( entry );
    }
    @Override
    public long getLastModified ( final String name ) throws IOException {
        final ZipEntry entry = this.jarFile.getEntry ( name );
        if ( entry == null ) {
            return -1L;
        }
        return entry.getTime();
    }
    @Override
    public String getURL ( final String entry ) {
        final StringBuilder result = new StringBuilder ( "jar:" );
        result.append ( this.getJarFileURL().toExternalForm() );
        result.append ( "!/" );
        result.append ( entry );
        return result.toString();
    }
    @Override
    public void close() {
        if ( this.jarFile != null ) {
            try {
                this.jarFile.close();
            } catch ( IOException ex ) {}
        }
    }
    @Override
    public void nextEntry() {
        if ( this.entries == null ) {
            this.entries = this.jarFile.entries();
        }
        if ( this.entries.hasMoreElements() ) {
            this.entry = this.entries.nextElement();
        } else {
            this.entry = null;
        }
    }
    @Override
    public String getEntryName() {
        if ( this.entry == null ) {
            return null;
        }
        return this.entry.getName();
    }
    @Override
    public InputStream getEntryInputStream() throws IOException {
        if ( this.entry == null ) {
            return null;
        }
        return this.jarFile.getInputStream ( this.entry );
    }
    @Override
    public Manifest getManifest() throws IOException {
        return this.jarFile.getManifest();
    }
    @Override
    public void reset() throws IOException {
        this.entries = null;
        this.entry = null;
    }
}
