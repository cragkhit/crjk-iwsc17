package org.apache.coyote.http11.filters;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.apache.coyote.InputBuffer;
import org.apache.coyote.Request;
import org.apache.coyote.http11.InputFilter;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
public class BufferedInputFilter implements InputFilter, ApplicationBufferHandler {
    private static final String ENCODING_NAME = "buffered";
    private static final ByteChunk ENCODING = new ByteChunk();
    private ByteBuffer buffered;
    private ByteBuffer tempRead;
    private InputBuffer buffer;
    private boolean hasRead = false;
    static {
        ENCODING.setBytes ( ENCODING_NAME.getBytes ( StandardCharsets.ISO_8859_1 ),
                            0, ENCODING_NAME.length() );
    }
    public void setLimit ( int limit ) {
        if ( buffered == null ) {
            buffered = ByteBuffer.allocate ( limit );
            buffered.flip();
        }
    }
    @Override
    public void setRequest ( Request request ) {
        try {
            while ( buffer.doRead ( this ) >= 0 ) {
                buffered.mark().position ( buffered.limit() ).limit ( buffered.capacity() );
                buffered.put ( tempRead );
                buffered.limit ( buffered.position() ).reset();
                tempRead = null;
            }
        } catch ( IOException | BufferOverflowException ioe ) {
            throw new IllegalStateException (
                "Request body too large for buffer" );
        }
    }
    @Override
    public int doRead ( ApplicationBufferHandler handler ) throws IOException {
        if ( isFinished() ) {
            return -1;
        }
        handler.setByteBuffer ( buffered );
        hasRead = true;
        return buffered.remaining();
    }
    @Override
    public void setBuffer ( InputBuffer buffer ) {
        this.buffer = buffer;
    }
    @Override
    public void recycle() {
        if ( buffered != null ) {
            if ( buffered.capacity() > 65536 ) {
                buffered = null;
            } else {
                buffered.position ( 0 ).limit ( 0 );
            }
        }
        hasRead = false;
        buffer = null;
    }
    @Override
    public ByteChunk getEncodingName() {
        return ENCODING;
    }
    @Override
    public long end() throws IOException {
        return 0;
    }
    @Override
    public int available() {
        return buffered.remaining();
    }
    @Override
    public boolean isFinished() {
        return hasRead || buffered.remaining() <= 0;
    }
    @Override
    public void setByteBuffer ( ByteBuffer buffer ) {
        tempRead = buffer;
    }
    @Override
    public ByteBuffer getByteBuffer() {
        return tempRead;
    }
    @Override
    public void expand ( int size ) {
    }
}
