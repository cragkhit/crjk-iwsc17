package org.apache.catalina.webresources;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.TrackedWebResource;
import java.io.InputStream;
class TrackedInputStream extends InputStream implements TrackedWebResource {
    private final WebResourceRoot root;
    private final String name;
    private final InputStream is;
    private final Exception creation;
    TrackedInputStream ( final WebResourceRoot root, final String name, final InputStream is ) {
        this.root = root;
        this.name = name;
        this.is = is;
        this.creation = new Exception();
        root.registerTrackedResource ( this );
    }
    @Override
    public int read() throws IOException {
        return this.is.read();
    }
    @Override
    public int read ( final byte[] b ) throws IOException {
        return this.is.read ( b );
    }
    @Override
    public int read ( final byte[] b, final int off, final int len ) throws IOException {
        return this.is.read ( b, off, len );
    }
    @Override
    public long skip ( final long n ) throws IOException {
        return this.is.skip ( n );
    }
    @Override
    public int available() throws IOException {
        return this.is.available();
    }
    @Override
    public void close() throws IOException {
        this.root.deregisterTrackedResource ( this );
        this.is.close();
    }
    @Override
    public synchronized void mark ( final int readlimit ) {
        this.is.mark ( readlimit );
    }
    @Override
    public synchronized void reset() throws IOException {
        this.is.reset();
    }
    @Override
    public boolean markSupported() {
        return this.is.markSupported();
    }
    @Override
    public String getName() {
        return this.name;
    }
    @Override
    public Exception getCreatedBy() {
        return this.creation;
    }
    @Override
    public String toString() {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter ( sw );
        sw.append ( '[' );
        sw.append ( this.name );
        sw.append ( ']' );
        sw.append ( System.lineSeparator() );
        this.creation.printStackTrace ( pw );
        pw.flush();
        return sw.toString();
    }
}
