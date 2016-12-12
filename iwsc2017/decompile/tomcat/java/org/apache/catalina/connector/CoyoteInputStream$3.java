package org.apache.catalina.connector;
import java.io.IOException;
import java.security.PrivilegedExceptionAction;
class CoyoteInputStream$3 implements PrivilegedExceptionAction<Integer> {
    final   byte[] val$b;
    @Override
    public Integer run() throws IOException {
        final Integer integer = CoyoteInputStream.this.ib.read ( this.val$b, 0, this.val$b.length );
        return integer;
    }
}
