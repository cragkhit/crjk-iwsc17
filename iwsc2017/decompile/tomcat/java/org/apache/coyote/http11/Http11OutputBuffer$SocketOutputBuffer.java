package org.apache.coyote.http11;
import java.io.IOException;
import org.apache.coyote.ActionCode;
import java.nio.ByteBuffer;
import org.apache.coyote.OutputBuffer;
protected class SocketOutputBuffer implements OutputBuffer {
    @Override
    public int doWrite ( final ByteBuffer chunk ) throws IOException {
        try {
            int len = chunk.remaining();
            Http11OutputBuffer.this.socketWrapper.write ( Http11OutputBuffer.this.isBlocking(), chunk );
            len -= chunk.remaining();
            final Http11OutputBuffer this$0 = Http11OutputBuffer.this;
            this$0.byteCount += len;
            return len;
        } catch ( IOException ioe ) {
            Http11OutputBuffer.this.response.action ( ActionCode.CLOSE_NOW, ioe );
            throw ioe;
        }
    }
    @Override
    public long getBytesWritten() {
        return Http11OutputBuffer.this.byteCount;
    }
}
