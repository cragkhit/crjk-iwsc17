package org.apache.catalina.tribes.group;
import java.util.Arrays;
public static class RpcCollectorKey {
    final byte[] id;
    public RpcCollectorKey ( final byte[] id ) {
        this.id = id;
    }
    @Override
    public int hashCode() {
        return this.id[0] + this.id[1] + this.id[2] + this.id[3];
    }
    @Override
    public boolean equals ( final Object o ) {
        if ( o instanceof RpcCollectorKey ) {
            final RpcCollectorKey r = ( RpcCollectorKey ) o;
            return Arrays.equals ( this.id, r.id );
        }
        return false;
    }
}
