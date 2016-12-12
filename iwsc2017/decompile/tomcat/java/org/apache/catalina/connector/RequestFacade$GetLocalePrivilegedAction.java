package org.apache.catalina.connector;
import java.util.Locale;
import java.security.PrivilegedAction;
private final class GetLocalePrivilegedAction implements PrivilegedAction<Locale> {
    @Override
    public Locale run() {
        return RequestFacade.this.request.getLocale();
    }
}
