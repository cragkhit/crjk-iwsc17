package org.apache.coyote.ajp;
import java.io.IOException;
import org.apache.tomcat.util.buf.ByteChunk;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
import org.apache.coyote.InputBuffer;
protected class SocketInputBuffer implements InputBuffer {
    @Override
    public int doRead ( final ApplicationBufferHandler handler ) throws IOException {
        if ( AjpProcessor.access$000 ( AjpProcessor.this ) ) {
            return -1;
        }
        if ( AjpProcessor.access$100 ( AjpProcessor.this ) && !AjpProcessor.this.refillReadBuffer ( true ) ) {
            return -1;
        }
        final ByteChunk bc = AjpProcessor.access$200 ( AjpProcessor.this ).getByteChunk();
        handler.setByteBuffer ( ByteBuffer.wrap ( bc.getBuffer(), bc.getStart(), bc.getLength() ) );
        AjpProcessor.access$102 ( AjpProcessor.this, true );
        return handler.getByteBuffer().remaining();
    }
}
