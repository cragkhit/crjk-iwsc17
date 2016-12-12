package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class RequestElement implements AccessLogElement {
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        if ( request != null ) {
            final String method = request.getMethod();
            if ( method == null ) {
                buf.append ( '-' );
            } else {
                buf.append ( request.getMethod() );
                buf.append ( ' ' );
                buf.append ( request.getRequestURI() );
                if ( request.getQueryString() != null ) {
                    buf.append ( '?' );
                    buf.append ( request.getQueryString() );
                }
                buf.append ( ' ' );
                buf.append ( request.getProtocol() );
            }
        } else {
            buf.append ( '-' );
        }
    }
}
