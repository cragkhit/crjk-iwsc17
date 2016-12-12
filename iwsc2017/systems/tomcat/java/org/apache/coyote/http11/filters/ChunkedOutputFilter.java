package org.apache.coyote.http11.filters;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.Response;
import org.apache.coyote.http11.OutputFilter;
import org.apache.tomcat.util.buf.HexUtils;
public class ChunkedOutputFilter implements OutputFilter {
    private static final byte[] END_CHUNK_BYTES = { ( byte ) '0', ( byte ) '\r', ( byte ) '\n',
                                                    ( byte ) '\r', ( byte ) '\n'
                                                  };
    public ChunkedOutputFilter() {
        chunkHeader.put ( 8, ( byte ) '\r' );
        chunkHeader.put ( 9, ( byte ) '\n' );
    }
    protected OutputBuffer buffer;
    protected final ByteBuffer chunkHeader = ByteBuffer.allocate ( 10 );
    protected final ByteBuffer endChunk = ByteBuffer.wrap ( END_CHUNK_BYTES );
    @Override
    public int doWrite ( ByteBuffer chunk ) throws IOException {
        int result = chunk.remaining();
        if ( result <= 0 ) {
            return 0;
        }
        int pos = calculateChunkHeader ( result );
        chunkHeader.position ( pos + 1 ).limit ( chunkHeader.position() + 9 - pos );
        buffer.doWrite ( chunkHeader );
        buffer.doWrite ( chunk );
        chunkHeader.position ( 8 ).limit ( 10 );
        buffer.doWrite ( chunkHeader );
        return result;
    }
    private int calculateChunkHeader ( int len ) {
        int pos = 7;
        int current = len;
        while ( current > 0 ) {
            int digit = current % 16;
            current = current / 16;
            chunkHeader.put ( pos--, HexUtils.getHex ( digit ) );
        }
        return pos;
    }
    @Override
    public long getBytesWritten() {
        return buffer.getBytesWritten();
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
        buffer.doWrite ( endChunk );
        endChunk.position ( 0 ).limit ( endChunk.capacity() );
        return 0;
    }
    @Override
    public void recycle() {
    }
}
