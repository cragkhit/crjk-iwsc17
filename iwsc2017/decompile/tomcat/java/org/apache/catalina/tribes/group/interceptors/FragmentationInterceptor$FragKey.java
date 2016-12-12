package org.apache.catalina.tribes.group.interceptors;
import java.util.Arrays;
import org.apache.catalina.tribes.io.XByteBuffer;
public static class FragKey {
    private final byte[] uniqueId;
    private final long received;
    public FragKey ( final byte[] id ) {
        this.received = System.currentTimeMillis();
        this.uniqueId = id;
    }
    @Override
    public int hashCode() {
        return XByteBuffer.toInt ( this.uniqueId, 0 );
    }
    @Override
    public boolean equals ( final Object o ) {
        return o instanceof FragKey && Arrays.equals ( this.uniqueId, ( ( FragKey ) o ).uniqueId );
    }
    public boolean expired ( final long expire ) {
        return System.currentTimeMillis() - this.received > expire;
    }
}
