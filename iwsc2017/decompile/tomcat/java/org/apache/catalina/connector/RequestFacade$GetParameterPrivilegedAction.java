package org.apache.catalina.connector;
import java.security.PrivilegedAction;
private final class GetParameterPrivilegedAction implements PrivilegedAction<String> {
    public String name;
    public GetParameterPrivilegedAction ( final String name ) {
        this.name = name;
    }
    @Override
    public String run() {
        return RequestFacade.this.request.getParameter ( this.name );
    }
}
