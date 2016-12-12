package org.apache.coyote.http11.filters;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.Response;
import org.apache.coyote.http11.OutputFilter;
public class IdentityOutputFilter implements OutputFilter {
    protected long contentLength = -1;
    protected long remaining = 0;
    protected OutputBuffer buffer;
    @Override
    public int doWrite ( ByteBuffer chunk ) throws IOException {
        int result = -1;
        if ( contentLength >= 0 ) {
            if ( remaining > 0 ) {
                result = chunk.remaining();
                if ( result > remaining ) {
                    chunk.limit ( chunk.position() + ( int ) remaining );
                    result = ( int ) remaining;
                    remaining = 0;
                } else {
                    remaining = remaining - result;
                }
                buffer.doWrite ( chunk );
            } else {
                chunk.position ( 0 );
                chunk.limit ( 0 );
                result = -1;
            }
        } else {
            result = chunk.remaining();
            buffer.doWrite ( chunk );
            result -= chunk.remaining();
        }
        return result;
    }
    @Override
    public long getBytesWritten() {
        return buffer.getBytesWritten();
    }
    @Override
    public void setResponse ( Response response ) {
        contentLength = response.getContentLengthLong();
        remaining = contentLength;
    }
    @Override
    public void setBuffer ( OutputBuffer buffer ) {
        this.buffer = buffer;
    }
    @Override
    public long end()
    throws IOException {
        if ( remaining > 0 ) {
            return remaining;
        }
        return 0;
    }
    @Override
    public void recycle() {
        contentLength = -1;
        remaining = 0;
    }
}
