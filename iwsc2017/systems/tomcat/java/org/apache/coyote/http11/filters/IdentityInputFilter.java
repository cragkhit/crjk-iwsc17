package org.apache.coyote.http11.filters;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.apache.coyote.InputBuffer;
import org.apache.coyote.Request;
import org.apache.coyote.http11.InputFilter;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
import org.apache.tomcat.util.res.StringManager;
public class IdentityInputFilter implements InputFilter, ApplicationBufferHandler {
    private static final StringManager sm = StringManager.getManager (
            IdentityInputFilter.class.getPackage().getName() );
    protected static final String ENCODING_NAME = "identity";
    protected static final ByteChunk ENCODING = new ByteChunk();
    static {
        ENCODING.setBytes ( ENCODING_NAME.getBytes ( StandardCharsets.ISO_8859_1 ),
                            0, ENCODING_NAME.length() );
    }
    protected long contentLength = -1;
    protected long remaining = 0;
    protected InputBuffer buffer;
    protected ByteBuffer tempRead;
    private final int maxSwallowSize;
    public IdentityInputFilter ( int maxSwallowSize ) {
        this.maxSwallowSize = maxSwallowSize;
    }
    @Override
    public int doRead ( ApplicationBufferHandler handler ) throws IOException {
        int result = -1;
        if ( contentLength >= 0 ) {
            if ( remaining > 0 ) {
                int nRead = buffer.doRead ( handler );
                if ( nRead > remaining ) {
                    handler.getByteBuffer().limit ( handler.getByteBuffer().position() + ( int ) remaining );
                    result = ( int ) remaining;
                } else {
                    result = nRead;
                }
                if ( nRead > 0 ) {
                    remaining = remaining - nRead;
                }
            } else {
                if ( handler.getByteBuffer() != null ) {
                    handler.getByteBuffer().position ( 0 ).limit ( 0 );
                }
                result = -1;
            }
        }
        return result;
    }
    @Override
    public void setRequest ( Request request ) {
        contentLength = request.getContentLengthLong();
        remaining = contentLength;
    }
    @Override
    public long end() throws IOException {
        final boolean maxSwallowSizeExceeded = ( maxSwallowSize > -1 && remaining > maxSwallowSize );
        long swallowed = 0;
        while ( remaining > 0 ) {
            int nread = buffer.doRead ( this );
            tempRead = null;
            if ( nread > 0 ) {
                swallowed += nread;
                remaining = remaining - nread;
                if ( maxSwallowSizeExceeded && swallowed > maxSwallowSize ) {
                    throw new IOException ( sm.getString ( "inputFilter.maxSwallow" ) );
                }
            } else {
                remaining = 0;
            }
        }
        return -remaining;
    }
    @Override
    public int available() {
        return 0;
    }
    @Override
    public void setBuffer ( InputBuffer buffer ) {
        this.buffer = buffer;
    }
    @Override
    public void recycle() {
        contentLength = -1;
        remaining = 0;
    }
    @Override
    public ByteChunk getEncodingName() {
        return ENCODING;
    }
    @Override
    public boolean isFinished() {
        return contentLength > -1 && remaining <= 0;
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
