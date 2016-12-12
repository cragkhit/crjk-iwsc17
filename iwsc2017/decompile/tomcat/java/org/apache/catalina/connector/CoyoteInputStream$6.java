package org.apache.catalina.connector;
import java.io.IOException;
import java.security.PrivilegedExceptionAction;
class CoyoteInputStream$6 implements PrivilegedExceptionAction<Void> {
    @Override
    public Void run() throws IOException {
        CoyoteInputStream.this.ib.close();
        return null;
    }
}
