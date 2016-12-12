package org.apache.coyote.http11.filters;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.zip.GZIPOutputStream;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.Response;
import org.apache.coyote.http11.OutputFilter;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class GzipOutputFilter implements OutputFilter {
    protected static final Log log = LogFactory.getLog ( GzipOutputFilter.class );
    protected OutputBuffer buffer;
    protected GZIPOutputStream compressionStream = null;
    protected final OutputStream fakeOutputStream = new FakeOutputStream();
    @Override
    public int doWrite ( ByteBuffer chunk ) throws IOException {
        if ( compressionStream == null ) {
            compressionStream = new GZIPOutputStream ( fakeOutputStream, true );
        }
        int len = chunk.remaining();
        if ( chunk.hasArray() ) {
            compressionStream.write ( chunk.array(), chunk.arrayOffset() + chunk.position(), len );
        } else {
            byte[] bytes = new byte[len];
            chunk.put ( bytes );
            compressionStream.write ( bytes, 0, len );
        }
        return len;
    }
    @Override
    public long getBytesWritten() {
        return buffer.getBytesWritten();
    }
    public void flush() {
        if ( compressionStream != null ) {
            try {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Flushing the compression stream!" );
                }
                compressionStream.flush();
            } catch ( IOException e ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Ignored exception while flushing gzip filter", e );
                }
            }
        }
    }
    @Override
    public void setResponse ( Response response ) {
    }
    @Override
    public void setBuffer ( OutputBuffer buffer ) {
        this.buffer = buffer;
    }
    @Override
    public long end()
    throws IOException {
        if ( compressionStream == null ) {
            compressionStream = new GZIPOutputStream ( fakeOutputStream, true );
        }
        compressionStream.finish();
        compressionStream.close();
        return ( ( OutputFilter ) buffer ).end();
    }
    @Override
    public void recycle() {
        compressionStream = null;
    }
    protected class FakeOutputStream
        extends OutputStream {
        protected final ByteBuffer outputChunk = ByteBuffer.allocate ( 1 );
        @Override
        public void write ( int b )
        throws IOException {
            outputChunk.put ( 0, ( byte ) ( b & 0xff ) );
            buffer.doWrite ( outputChunk );
        }
        @Override
        public void write ( byte[] b, int off, int len )
        throws IOException {
            buffer.doWrite ( ByteBuffer.wrap ( b, off, len ) );
        }
        @Override
        public void flush() throws IOException { }
        @Override
        public void close() throws IOException { }
    }
}
