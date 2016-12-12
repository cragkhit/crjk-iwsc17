package org.apache.tomcat.util.scan;
import java.util.jar.Manifest;
import java.io.InputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.net.URL;
import org.apache.tomcat.Jar;
public abstract class AbstractInputStreamJar implements Jar {
    private final URL jarFileURL;
    private NonClosingJarInputStream jarInputStream;
    private JarEntry entry;
    public AbstractInputStreamJar ( final URL jarFileUrl ) {
        this.jarInputStream = null;
        this.entry = null;
        this.jarFileURL = jarFileUrl;
    }
    @Override
    public URL getJarFileURL() {
        return this.jarFileURL;
    }
    @Override
    public void nextEntry() {
        if ( this.jarInputStream == null ) {
            try {
                this.jarInputStream = this.createJarInputStream();
            } catch ( IOException e ) {
                this.entry = null;
                return;
            }
        }
        try {
            this.entry = this.jarInputStream.getNextJarEntry();
        } catch ( IOException ioe ) {
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
        return this.jarInputStream;
    }
    @Override
    public boolean entryExists ( final String name ) throws IOException {
        this.gotoEntry ( name );
        return this.entry != null;
    }
    @Override
    public InputStream getInputStream ( final String name ) throws IOException {
        this.gotoEntry ( name );
        if ( this.entry == null ) {
            return null;
        }
        return this.jarInputStream;
    }
    @Override
    public long getLastModified ( final String name ) throws IOException {
        this.gotoEntry ( name );
        if ( this.entry == null ) {
            return -1L;
        }
        return this.entry.getTime();
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
    public Manifest getManifest() throws IOException {
        this.reset();
        return this.jarInputStream.getManifest();
    }
    @Override
    public void reset() throws IOException {
        this.closeStream();
        this.entry = null;
        this.jarInputStream = this.createJarInputStream();
    }
    protected void closeStream() {
        if ( this.jarInputStream != null ) {
            try {
                this.jarInputStream.reallyClose();
            } catch ( IOException ex ) {}
        }
    }
    protected abstract NonClosingJarInputStream createJarInputStream() throws IOException;
    private void gotoEntry ( final String name ) throws IOException {
        if ( this.entry != null && name.equals ( this.entry.getName() ) ) {
            return;
        }
        this.reset();
        for ( JarEntry jarEntry = this.jarInputStream.getNextJarEntry(); jarEntry != null; jarEntry = this.jarInputStream.getNextJarEntry() ) {
            if ( name.equals ( jarEntry.getName() ) ) {
                this.entry = jarEntry;
                break;
            }
        }
    }
}
