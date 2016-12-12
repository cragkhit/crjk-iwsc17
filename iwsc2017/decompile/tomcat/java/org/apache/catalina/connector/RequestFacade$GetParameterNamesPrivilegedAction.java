package org.apache.catalina.connector;
import java.util.Enumeration;
import java.security.PrivilegedAction;
private final class GetParameterNamesPrivilegedAction implements PrivilegedAction<Enumeration<String>> {
    @Override
    public Enumeration<String> run() {
        return RequestFacade.this.request.getParameterNames();
    }
}
