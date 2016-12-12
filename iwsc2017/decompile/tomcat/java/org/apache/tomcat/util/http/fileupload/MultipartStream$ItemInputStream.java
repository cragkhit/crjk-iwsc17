package org.apache.tomcat.util.http.fileupload;
import java.io.IOException;
import org.apache.tomcat.util.http.fileupload.util.Closeable;
import java.io.InputStream;
public class ItemInputStream extends InputStream implements Closeable {
    private long total;
    private int pad;
    private int pos;
    private boolean closed;
    private static final int BYTE_POSITIVE_OFFSET = 256;
    ItemInputStream() {
        this.findSeparator();
    }
    private void findSeparator() {
        this.pos = MultipartStream.this.findSeparator();
        if ( this.pos == -1 ) {
            if ( MultipartStream.access$000 ( MultipartStream.this ) - MultipartStream.access$100 ( MultipartStream.this ) > MultipartStream.access$200 ( MultipartStream.this ) ) {
                this.pad = MultipartStream.access$200 ( MultipartStream.this );
            } else {
                this.pad = MultipartStream.access$000 ( MultipartStream.this ) - MultipartStream.access$100 ( MultipartStream.this );
            }
        }
    }
    public long getBytesRead() {
        return this.total;
    }
    @Override
    public int available() throws IOException {
        if ( this.pos == -1 ) {
            return MultipartStream.access$000 ( MultipartStream.this ) - MultipartStream.access$100 ( MultipartStream.this ) - this.pad;
        }
        return this.pos - MultipartStream.access$100 ( MultipartStream.this );
    }
    @Override
    public int read() throws IOException {
        if ( this.closed ) {
            throw new FileItemStream.ItemSkippedException();
        }
        if ( this.available() == 0 && this.makeAvailable() == 0 ) {
            return -1;
        }
        ++this.total;
        final int b = MultipartStream.access$300 ( MultipartStream.this ) [MultipartStream.access$108 ( MultipartStream.this )];
        if ( b >= 0 ) {
            return b;
        }
        return b + 256;
    }
    @Override
    public int read ( final byte[] b, final int off, final int len ) throws IOException {
        if ( this.closed ) {
            throw new FileItemStream.ItemSkippedException();
        }
        if ( len == 0 ) {
            return 0;
        }
        int res = this.available();
        if ( res == 0 ) {
            res = this.makeAvailable();
            if ( res == 0 ) {
                return -1;
            }
        }
        res = Math.min ( res, len );
        System.arraycopy ( MultipartStream.access$300 ( MultipartStream.this ), MultipartStream.access$100 ( MultipartStream.this ), b, off, res );
        MultipartStream.access$102 ( MultipartStream.this, MultipartStream.access$100 ( MultipartStream.this ) + res );
        this.total += res;
        return res;
    }
    @Override
    public void close() throws IOException {
        this.close ( false );
    }
    public void close ( final boolean pCloseUnderlying ) throws IOException {
        if ( this.closed ) {
            return;
        }
        if ( pCloseUnderlying ) {
            this.closed = true;
            MultipartStream.access$400 ( MultipartStream.this ).close();
        } else {
            while ( true ) {
                int av = this.available();
                if ( av == 0 ) {
                    av = this.makeAvailable();
                    if ( av == 0 ) {
                        break;
                    }
                }
                this.skip ( av );
            }
        }
        this.closed = true;
    }
    @Override
    public long skip ( final long bytes ) throws IOException {
        if ( this.closed ) {
            throw new FileItemStream.ItemSkippedException();
        }
        int av = this.available();
        if ( av == 0 ) {
            av = this.makeAvailable();
            if ( av == 0 ) {
                return 0L;
            }
        }
        final long res = Math.min ( av, bytes );
        MultipartStream.access$102 ( MultipartStream.this, ( int ) ( MultipartStream.access$100 ( MultipartStream.this ) + res ) );
        return res;
    }
    private int makeAvailable() throws IOException {
        if ( this.pos != -1 ) {
            return 0;
        }
        this.total += MultipartStream.access$000 ( MultipartStream.this ) - MultipartStream.access$100 ( MultipartStream.this ) - this.pad;
        System.arraycopy ( MultipartStream.access$300 ( MultipartStream.this ), MultipartStream.access$000 ( MultipartStream.this ) - this.pad, MultipartStream.access$300 ( MultipartStream.this ), 0, this.pad );
        MultipartStream.access$102 ( MultipartStream.this, 0 );
        MultipartStream.access$002 ( MultipartStream.this, this.pad );
        while ( true ) {
            final int bytesRead = MultipartStream.access$400 ( MultipartStream.this ).read ( MultipartStream.access$300 ( MultipartStream.this ), MultipartStream.access$000 ( MultipartStream.this ), MultipartStream.access$500 ( MultipartStream.this ) - MultipartStream.access$000 ( MultipartStream.this ) );
            if ( bytesRead == -1 ) {
                final String msg = "Stream ended unexpectedly";
                throw new MalformedStreamException ( "Stream ended unexpectedly" );
            }
            if ( MultipartStream.access$600 ( MultipartStream.this ) != null ) {
                MultipartStream.access$600 ( MultipartStream.this ).noteBytesRead ( bytesRead );
            }
            MultipartStream.access$002 ( MultipartStream.this, MultipartStream.access$000 ( MultipartStream.this ) + bytesRead );
            this.findSeparator();
            final int av = this.available();
            if ( av > 0 || this.pos != -1 ) {
                return av;
            }
        }
    }
    @Override
    public boolean isClosed() {
        return this.closed;
    }
}
