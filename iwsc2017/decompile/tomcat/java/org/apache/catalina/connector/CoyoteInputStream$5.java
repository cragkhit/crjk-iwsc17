package org.apache.catalina.connector;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.PrivilegedExceptionAction;
class CoyoteInputStream$5 implements PrivilegedExceptionAction<Integer> {
    final   ByteBuffer val$b;
    @Override
    public Integer run() throws IOException {
        final Integer integer = CoyoteInputStream.this.ib.read ( this.val$b );
        return integer;
    }
}
