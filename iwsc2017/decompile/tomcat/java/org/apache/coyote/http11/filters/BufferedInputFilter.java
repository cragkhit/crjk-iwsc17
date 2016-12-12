package org.apache.coyote.http11.filters;
import java.nio.charset.StandardCharsets;
import java.nio.BufferOverflowException;
import java.io.IOException;
import org.apache.coyote.Request;
import org.apache.coyote.InputBuffer;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
import org.apache.coyote.http11.InputFilter;
public class BufferedInputFilter implements InputFilter, ApplicationBufferHandler {
    private static final String ENCODING_NAME = "buffered";
    private static final ByteChunk ENCODING;
    private ByteBuffer buffered;
    private ByteBuffer tempRead;
    private InputBuffer buffer;
    private boolean hasRead;
    public BufferedInputFilter() {
        this.hasRead = false;
    }
    public void setLimit ( final int limit ) {
        if ( this.buffered == null ) {
            ( this.buffered = ByteBuffer.allocate ( limit ) ).flip();
        }
    }
    @Override
    public void setRequest ( final Request request ) {
        try {
            while ( this.buffer.doRead ( this ) >= 0 ) {
                this.buffered.mark().position ( this.buffered.limit() ).limit ( this.buffered.capacity() );
                this.buffered.put ( this.tempRead );
                this.buffered.limit ( this.buffered.position() ).reset();
                this.tempRead = null;
            }
        } catch ( IOException | BufferOverflowException ioe ) {
            throw new IllegalStateException ( "Request body too large for buffer" );
        }
    }
    @Override
    public int doRead ( final ApplicationBufferHandler handler ) throws IOException {
        if ( this.isFinished() ) {
            return -1;
        }
        handler.setByteBuffer ( this.buffered );
        this.hasRead = true;
        return this.buffered.remaining();
    }
    @Override
    public void setBuffer ( final InputBuffer buffer ) {
        this.buffer = buffer;
    }
    @Override
    public void recycle() {
        if ( this.buffered != null ) {
            if ( this.buffered.capacity() > 65536 ) {
                this.buffered = null;
            } else {
                this.buffered.position ( 0 ).limit ( 0 );
            }
        }
        this.hasRead = false;
        this.buffer = null;
    }
    @Override
    public ByteChunk getEncodingName() {
        return BufferedInputFilter.ENCODING;
    }
    @Override
    public long end() throws IOException {
        return 0L;
    }
    @Override
    public int available() {
        return this.buffered.remaining();
    }
    @Override
    public boolean isFinished() {
        return this.hasRead || this.buffered.remaining() <= 0;
    }
    @Override
    public void setByteBuffer ( final ByteBuffer buffer ) {
        this.tempRead = buffer;
    }
    @Override
    public ByteBuffer getByteBuffer() {
        return this.tempRead;
    }
    @Override
    public void expand ( final int size ) {
    }
    static {
        ( ENCODING = new ByteChunk() ).setBytes ( "buffered".getBytes ( StandardCharsets.ISO_8859_1 ), 0, "buffered".length() );
    }
}
