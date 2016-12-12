package org.apache.catalina.valves;
import javax.servlet.http.Cookie;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class CookieElement implements AccessLogElement {
    private final String header;
    public CookieElement ( final String header ) {
        this.header = header;
    }
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        String value = "-";
        final Cookie[] c = request.getCookies();
        if ( c != null ) {
            for ( int i = 0; i < c.length; ++i ) {
                if ( this.header.equals ( c[i].getName() ) ) {
                    value = c[i].getValue();
                    break;
                }
            }
        }
        buf.append ( value );
    }
}
