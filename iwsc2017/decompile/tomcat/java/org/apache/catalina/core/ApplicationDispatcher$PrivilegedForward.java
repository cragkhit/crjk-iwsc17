package org.apache.catalina.core;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import java.security.PrivilegedExceptionAction;
protected class PrivilegedForward implements PrivilegedExceptionAction<Void> {
    private final ServletRequest request;
    private final ServletResponse response;
    PrivilegedForward ( final ServletRequest request, final ServletResponse response ) {
        this.request = request;
        this.response = response;
    }
    @Override
    public Void run() throws Exception {
        ApplicationDispatcher.access$000 ( ApplicationDispatcher.this, this.request, this.response );
        return null;
    }
}
