package org.apache.catalina.connector;
import java.util.Enumeration;
import java.security.PrivilegedAction;
private final class GetHeadersPrivilegedAction implements PrivilegedAction<Enumeration<String>> {
    private final String name;
    public GetHeadersPrivilegedAction ( final String name ) {
        this.name = name;
    }
    @Override
    public Enumeration<String> run() {
        return RequestFacade.this.request.getHeaders ( this.name );
    }
}
