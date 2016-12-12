package org.apache.tomcat.util.buf;
import java.util.concurrent.atomic.AtomicBoolean;
import java.nio.ByteBuffer;
public class ByteBufferHolder {
    private final ByteBuffer buf;
    private final AtomicBoolean flipped;
    public ByteBufferHolder ( final ByteBuffer buf, final boolean flipped ) {
        this.buf = buf;
        this.flipped = new AtomicBoolean ( flipped );
    }
    public ByteBuffer getBuf() {
        return this.buf;
    }
    public boolean isFlipped() {
        return this.flipped.get();
    }
    public boolean flip() {
        if ( this.flipped.compareAndSet ( false, true ) ) {
            this.buf.flip();
            return true;
        }
        return false;
    }
}
