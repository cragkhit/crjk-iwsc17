package org.apache.coyote.http11.filters;
import org.apache.coyote.Response;
import org.apache.tomcat.util.buf.HexUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.http11.OutputFilter;
public class ChunkedOutputFilter implements OutputFilter {
    private static final byte[] END_CHUNK_BYTES;
    protected OutputBuffer buffer;
    protected final ByteBuffer chunkHeader;
    protected final ByteBuffer endChunk;
    public ChunkedOutputFilter() {
        this.chunkHeader = ByteBuffer.allocate ( 10 );
        this.endChunk = ByteBuffer.wrap ( ChunkedOutputFilter.END_CHUNK_BYTES );
        this.chunkHeader.put ( 8, ( byte ) 13 );
        this.chunkHeader.put ( 9, ( byte ) 10 );
    }
    @Override
    public int doWrite ( final ByteBuffer chunk ) throws IOException {
        final int result = chunk.remaining();
        if ( result <= 0 ) {
            return 0;
        }
        final int pos = this.calculateChunkHeader ( result );
        this.chunkHeader.position ( pos + 1 ).limit ( this.chunkHeader.position() + 9 - pos );
        this.buffer.doWrite ( this.chunkHeader );
        this.buffer.doWrite ( chunk );
        this.chunkHeader.position ( 8 ).limit ( 10 );
        this.buffer.doWrite ( this.chunkHeader );
        return result;
    }
    private int calculateChunkHeader ( final int len ) {
        int pos = 7;
        int current = len;
        while ( current > 0 ) {
            final int digit = current % 16;
            current /= 16;
            this.chunkHeader.put ( pos--, HexUtils.getHex ( digit ) );
        }
        return pos;
    }
    @Override
    public long getBytesWritten() {
        return this.buffer.getBytesWritten();
    }
    @Override
    public void setResponse ( final Response response ) {
    }
    @Override
    public void setBuffer ( final OutputBuffer buffer ) {
        this.buffer = buffer;
    }
    @Override
    public long end() throws IOException {
        this.buffer.doWrite ( this.endChunk );
        this.endChunk.position ( 0 ).limit ( this.endChunk.capacity() );
        return 0L;
    }
    @Override
    public void recycle() {
    }
    static {
        END_CHUNK_BYTES = new byte[] { 48, 13, 10, 13, 10 };
    }
}
