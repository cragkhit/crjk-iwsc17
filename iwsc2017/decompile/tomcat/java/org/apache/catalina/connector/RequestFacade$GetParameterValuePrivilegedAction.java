package org.apache.catalina.connector;
import java.security.PrivilegedAction;
private final class GetParameterValuePrivilegedAction implements PrivilegedAction<String[]> {
    public String name;
    public GetParameterValuePrivilegedAction ( final String name ) {
        this.name = name;
    }
    @Override
    public String[] run() {
        return RequestFacade.this.request.getParameterValues ( this.name );
    }
}
