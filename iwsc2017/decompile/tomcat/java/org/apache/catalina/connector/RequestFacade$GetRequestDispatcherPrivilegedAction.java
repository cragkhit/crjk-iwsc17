package org.apache.catalina.connector;
import javax.servlet.RequestDispatcher;
import java.security.PrivilegedAction;
private final class GetRequestDispatcherPrivilegedAction implements PrivilegedAction<RequestDispatcher> {
    private final String path;
    public GetRequestDispatcherPrivilegedAction ( final String path ) {
        this.path = path;
    }
    @Override
    public RequestDispatcher run() {
        return RequestFacade.this.request.getRequestDispatcher ( this.path );
    }
}
