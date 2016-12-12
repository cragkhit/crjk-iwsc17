package org.apache.coyote.ajp;
import java.io.IOException;
import org.apache.coyote.ErrorState;
import java.nio.ByteBuffer;
import org.apache.coyote.OutputBuffer;
protected class SocketOutputBuffer implements OutputBuffer {
    @Override
    public int doWrite ( final ByteBuffer chunk ) throws IOException {
        if ( !AjpProcessor.access$300 ( AjpProcessor.this ).isCommitted() ) {
            try {
                AjpProcessor.this.prepareResponse();
            } catch ( IOException e ) {
                AjpProcessor.access$400 ( AjpProcessor.this, ErrorState.CLOSE_CONNECTION_NOW, e );
            }
        }
        int len = 0;
        if ( !AjpProcessor.access$500 ( AjpProcessor.this ) ) {
            try {
                len = chunk.remaining();
                AjpProcessor.access$600 ( AjpProcessor.this, chunk );
                len -= chunk.remaining();
            } catch ( IOException ioe ) {
                AjpProcessor.access$700 ( AjpProcessor.this, ErrorState.CLOSE_CONNECTION_NOW, ioe );
                throw ioe;
            }
        }
        return len;
    }
    @Override
    public long getBytesWritten() {
        return AjpProcessor.access$800 ( AjpProcessor.this );
    }
}
