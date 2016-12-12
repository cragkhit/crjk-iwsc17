package org.apache.catalina.valves;
import javax.servlet.http.Cookie;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class CookieElement implements AccessLogElement {
    private final String name;
    public CookieElement ( final String name ) {
        this.name = name;
    }
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        final Cookie[] c = request.getCookies();
        for ( int i = 0; c != null && i < c.length; ++i ) {
            if ( this.name.equals ( c[i].getName() ) ) {
                buf.append ( ExtendedAccessLogValve.wrap ( c[i].getValue() ) );
            }
        }
    }
}
