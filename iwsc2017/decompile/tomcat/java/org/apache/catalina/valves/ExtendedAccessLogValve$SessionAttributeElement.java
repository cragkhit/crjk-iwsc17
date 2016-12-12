package org.apache.catalina.valves;
import javax.servlet.http.HttpSession;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class SessionAttributeElement implements AccessLogElement {
    private final String attribute;
    public SessionAttributeElement ( final String attribute ) {
        this.attribute = attribute;
    }
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        HttpSession session = null;
        if ( request != null ) {
            session = request.getSession ( false );
            if ( session != null ) {
                buf.append ( ExtendedAccessLogValve.wrap ( session.getAttribute ( this.attribute ) ) );
            }
        }
    }
}
