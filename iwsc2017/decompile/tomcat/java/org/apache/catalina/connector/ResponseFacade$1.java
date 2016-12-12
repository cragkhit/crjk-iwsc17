package org.apache.catalina.connector;
import java.io.IOException;
import java.security.PrivilegedExceptionAction;
class ResponseFacade$1 implements PrivilegedExceptionAction<Void> {
    @Override
    public Void run() throws IOException {
        ResponseFacade.this.response.setAppCommitted ( true );
        ResponseFacade.this.response.flushBuffer();
        return null;
    }
}
