package org.apache.coyote.http11.filters;
import org.apache.coyote.Response;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.http11.OutputFilter;
public class IdentityOutputFilter implements OutputFilter {
    protected long contentLength;
    protected long remaining;
    protected OutputBuffer buffer;
    public IdentityOutputFilter() {
        this.contentLength = -1L;
        this.remaining = 0L;
    }
    @Override
    public int doWrite ( final ByteBuffer chunk ) throws IOException {
        int result = -1;
        if ( this.contentLength >= 0L ) {
            if ( this.remaining > 0L ) {
                result = chunk.remaining();
                if ( result > this.remaining ) {
                    chunk.limit ( chunk.position() + ( int ) this.remaining );
                    result = ( int ) this.remaining;
                    this.remaining = 0L;
                } else {
                    this.remaining -= result;
                }
                this.buffer.doWrite ( chunk );
            } else {
                chunk.position ( 0 );
                chunk.limit ( 0 );
                result = -1;
            }
        } else {
            result = chunk.remaining();
            this.buffer.doWrite ( chunk );
            result -= chunk.remaining();
        }
        return result;
    }
    @Override
    public long getBytesWritten() {
        return this.buffer.getBytesWritten();
    }
    @Override
    public void setResponse ( final Response response ) {
        this.contentLength = response.getContentLengthLong();
        this.remaining = this.contentLength;
    }
    @Override
    public void setBuffer ( final OutputBuffer buffer ) {
        this.buffer = buffer;
    }
    @Override
    public long end() throws IOException {
        if ( this.remaining > 0L ) {
            return this.remaining;
        }
        return 0L;
    }
    @Override
    public void recycle() {
        this.contentLength = -1L;
        this.remaining = 0L;
    }
}
