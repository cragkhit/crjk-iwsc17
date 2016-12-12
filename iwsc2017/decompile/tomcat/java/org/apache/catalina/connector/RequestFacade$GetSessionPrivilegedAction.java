package org.apache.catalina.connector;
import javax.servlet.http.HttpSession;
import java.security.PrivilegedAction;
private final class GetSessionPrivilegedAction implements PrivilegedAction<HttpSession> {
    private final boolean create;
    public GetSessionPrivilegedAction ( final boolean create ) {
        this.create = create;
    }
    @Override
    public HttpSession run() {
        return RequestFacade.this.request.getSession ( this.create );
    }
}
