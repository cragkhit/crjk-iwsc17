package org.apache.tomcat.util.http.fileupload.util;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
public abstract class LimitedInputStream extends FilterInputStream implements Closeable {
    private final long sizeMax;
    private long count;
    private boolean closed;
    public LimitedInputStream ( InputStream inputStream, long pSizeMax ) {
        super ( inputStream );
        sizeMax = pSizeMax;
    }
    protected abstract void raiseError ( long pSizeMax, long pCount )
    throws IOException;
    private void checkLimit() throws IOException {
        if ( count > sizeMax ) {
            raiseError ( sizeMax, count );
        }
    }
    @Override
    public int read() throws IOException {
        int res = super.read();
        if ( res != -1 ) {
            count++;
            checkLimit();
        }
        return res;
    }
    @Override
    public int read ( byte[] b, int off, int len ) throws IOException {
        int res = super.read ( b, off, len );
        if ( res > 0 ) {
            count += res;
            checkLimit();
        }
        return res;
    }
    @Override
    public boolean isClosed() throws IOException {
        return closed;
    }
    @Override
    public void close() throws IOException {
        closed = true;
        super.close();
    }
}
