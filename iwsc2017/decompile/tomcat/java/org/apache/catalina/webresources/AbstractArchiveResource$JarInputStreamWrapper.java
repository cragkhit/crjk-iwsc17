package org.apache.catalina.webresources;
import java.security.cert.Certificate;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.io.InputStream;
protected class JarInputStreamWrapper extends InputStream {
    private final JarEntry jarEntry;
    private final InputStream is;
    public JarInputStreamWrapper ( final JarEntry jarEntry, final InputStream is ) {
        this.jarEntry = jarEntry;
        this.is = is;
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
        AbstractArchiveResource.access$000 ( AbstractArchiveResource.this ).closeJarFile();
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
    public Certificate[] getCertificates() {
        return this.jarEntry.getCertificates();
    }
}
