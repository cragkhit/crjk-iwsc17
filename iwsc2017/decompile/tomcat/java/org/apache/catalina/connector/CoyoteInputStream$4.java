package org.apache.catalina.connector;
import java.io.IOException;
import java.security.PrivilegedExceptionAction;
class CoyoteInputStream$4 implements PrivilegedExceptionAction<Integer> {
    final   byte[] val$b;
    final   int val$off;
    final   int val$len;
    @Override
    public Integer run() throws IOException {
        final Integer integer = CoyoteInputStream.this.ib.read ( this.val$b, this.val$off, this.val$len );
        return integer;
    }
}
