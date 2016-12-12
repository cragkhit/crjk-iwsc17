package org.apache.coyote.http11;
import java.io.IOException;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
import org.apache.coyote.InputBuffer;
private class SocketInputBuffer implements InputBuffer {
    @Override
    public int doRead ( final ApplicationBufferHandler handler ) throws IOException {
        if ( Http11InputBuffer.access$200 ( Http11InputBuffer.this ).position() >= Http11InputBuffer.access$200 ( Http11InputBuffer.this ).limit() && !Http11InputBuffer.access$300 ( Http11InputBuffer.this, true ) ) {
            return -1;
        }
        final int length = Http11InputBuffer.access$200 ( Http11InputBuffer.this ).remaining();
        handler.setByteBuffer ( Http11InputBuffer.access$200 ( Http11InputBuffer.this ).duplicate() );
        Http11InputBuffer.access$200 ( Http11InputBuffer.this ).position ( Http11InputBuffer.access$200 ( Http11InputBuffer.this ).limit() );
        return length;
    }
}
