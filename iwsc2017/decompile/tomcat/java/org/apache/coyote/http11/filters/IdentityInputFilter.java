package org.apache.coyote.http11.filters;
import java.nio.charset.StandardCharsets;
import org.apache.coyote.Request;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.coyote.InputBuffer;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
import org.apache.coyote.http11.InputFilter;
public class IdentityInputFilter implements InputFilter, ApplicationBufferHandler {
    private static final StringManager sm;
    protected static final String ENCODING_NAME = "identity";
    protected static final ByteChunk ENCODING;
    protected long contentLength;
    protected long remaining;
    protected InputBuffer buffer;
    protected ByteBuffer tempRead;
    private final int maxSwallowSize;
    public IdentityInputFilter ( final int maxSwallowSize ) {
        this.contentLength = -1L;
        this.remaining = 0L;
        this.maxSwallowSize = maxSwallowSize;
    }
    @Override
    public int doRead ( final ApplicationBufferHandler handler ) throws IOException {
        int result = -1;
        if ( this.contentLength >= 0L ) {
            if ( this.remaining > 0L ) {
                final int nRead = this.buffer.doRead ( handler );
                if ( nRead > this.remaining ) {
                    handler.getByteBuffer().limit ( handler.getByteBuffer().position() + ( int ) this.remaining );
                    result = ( int ) this.remaining;
                } else {
                    result = nRead;
                }
                if ( nRead > 0 ) {
                    this.remaining -= nRead;
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
    public void setRequest ( final Request request ) {
        this.contentLength = request.getContentLengthLong();
        this.remaining = this.contentLength;
    }
    @Override
    public long end() throws IOException {
        final boolean maxSwallowSizeExceeded = this.maxSwallowSize > -1 && this.remaining > this.maxSwallowSize;
        long swallowed = 0L;
        while ( this.remaining > 0L ) {
            final int nread = this.buffer.doRead ( this );
            this.tempRead = null;
            if ( nread > 0 ) {
                swallowed += nread;
                this.remaining -= nread;
                if ( maxSwallowSizeExceeded && swallowed > this.maxSwallowSize ) {
                    throw new IOException ( IdentityInputFilter.sm.getString ( "inputFilter.maxSwallow" ) );
                }
                continue;
            } else {
                this.remaining = 0L;
            }
        }
        return -this.remaining;
    }
    @Override
    public int available() {
        return 0;
    }
    @Override
    public void setBuffer ( final InputBuffer buffer ) {
        this.buffer = buffer;
    }
    @Override
    public void recycle() {
        this.contentLength = -1L;
        this.remaining = 0L;
    }
    @Override
    public ByteChunk getEncodingName() {
        return IdentityInputFilter.ENCODING;
    }
    @Override
    public boolean isFinished() {
        return this.contentLength > -1L && this.remaining <= 0L;
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
        sm = StringManager.getManager ( IdentityInputFilter.class.getPackage().getName() );
        ( ENCODING = new ByteChunk() ).setBytes ( "identity".getBytes ( StandardCharsets.ISO_8859_1 ), 0, "identity".length() );
    }
}
