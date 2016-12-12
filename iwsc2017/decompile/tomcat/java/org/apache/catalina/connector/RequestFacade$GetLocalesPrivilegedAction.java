package org.apache.catalina.connector;
import java.util.Locale;
import java.util.Enumeration;
import java.security.PrivilegedAction;
private final class GetLocalesPrivilegedAction implements PrivilegedAction<Enumeration<Locale>> {
    @Override
    public Enumeration<Locale> run() {
        return RequestFacade.this.request.getLocales();
    }
}
