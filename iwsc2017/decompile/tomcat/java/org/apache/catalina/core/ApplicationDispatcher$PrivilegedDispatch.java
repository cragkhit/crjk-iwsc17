package org.apache.catalina.core;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import java.security.PrivilegedExceptionAction;
protected class PrivilegedDispatch implements PrivilegedExceptionAction<Void> {
    private final ServletRequest request;
    private final ServletResponse response;
    PrivilegedDispatch ( final ServletRequest request, final ServletResponse response ) {
        this.request = request;
        this.response = response;
    }
    @Override
    public Void run() throws ServletException, IOException {
        ApplicationDispatcher.access$200 ( ApplicationDispatcher.this, this.request, this.response );
        return null;
    }
}
