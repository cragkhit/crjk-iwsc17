package org.apache.catalina.connector;
import javax.servlet.http.Cookie;
import java.security.PrivilegedAction;
private final class GetCookiesPrivilegedAction implements PrivilegedAction<Cookie[]> {
    @Override
    public Cookie[] run() {
        return RequestFacade.this.request.getCookies();
    }
}
