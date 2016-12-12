package org.apache.catalina.connector;
import java.util.Enumeration;
import java.security.PrivilegedAction;
private final class GetHeaderNamesPrivilegedAction implements PrivilegedAction<Enumeration<String>> {
    @Override
    public Enumeration<String> run() {
        return RequestFacade.this.request.getHeaderNames();
    }
}
