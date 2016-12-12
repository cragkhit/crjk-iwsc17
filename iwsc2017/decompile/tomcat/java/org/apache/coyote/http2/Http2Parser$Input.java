package org.apache.coyote.http2;
import java.nio.ByteBuffer;
import java.io.IOException;
interface Input {
    boolean fill ( boolean p0, byte[] p1, int p2, int p3 ) throws IOException;
default boolean fill ( boolean block, byte[] data ) throws IOException {
            return this.fill ( block, data, 0, data.length );
        }
default boolean fill ( boolean block, ByteBuffer data, int len ) throws IOException {
            boolean result;
            result = this.fill ( block, data.array(), data.arrayOffset() + data.position(), len );
            if ( result ) {
                data.position ( data.position() + len );
            }
            return result;
        }
    int getMaxFrameSize();
}
