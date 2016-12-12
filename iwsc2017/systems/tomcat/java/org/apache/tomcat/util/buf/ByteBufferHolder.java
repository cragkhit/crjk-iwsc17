package org.apache.tomcat.util.buf;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
public class ByteBufferHolder {
    private final ByteBuffer buf;
    private final AtomicBoolean flipped;
    public ByteBufferHolder ( ByteBuffer buf, boolean flipped ) {
        this.buf = buf;
        this.flipped = new AtomicBoolean ( flipped );
    }
    public ByteBuffer getBuf() {
        return buf;
    }
    public boolean isFlipped() {
        return flipped.get();
    }
    public boolean flip() {
        if ( flipped.compareAndSet ( false, true ) ) {
            buf.flip();
            return true;
        } else {
            return false;
        }
    }
}
