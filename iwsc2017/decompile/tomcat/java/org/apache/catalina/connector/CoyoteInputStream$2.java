package org.apache.catalina.connector;
import java.io.IOException;
import java.security.PrivilegedExceptionAction;
class CoyoteInputStream$2 implements PrivilegedExceptionAction<Integer> {
    @Override
    public Integer run() throws IOException {
        final Integer integer = CoyoteInputStream.this.ib.available();
        return integer;
    }
}
