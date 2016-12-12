package org.apache.catalina.core;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import java.security.PrivilegedExceptionAction;
protected class PrivilegedInclude implements PrivilegedExceptionAction<Void> {
    private final ServletRequest request;
    private final ServletResponse response;
    PrivilegedInclude ( final ServletRequest request, final ServletResponse response ) {
        this.request = request;
        this.response = response;
    }
    @Override
    public Void run() throws ServletException, IOException {
        ApplicationDispatcher.access$100 ( ApplicationDispatcher.this, this.request, this.response );
        return null;
    }
}
