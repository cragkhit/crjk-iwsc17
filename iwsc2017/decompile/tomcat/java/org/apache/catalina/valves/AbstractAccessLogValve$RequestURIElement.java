package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class RequestURIElement implements AccessLogElement {
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        if ( request != null ) {
            buf.append ( request.getRequestURI() );
        } else {
            buf.append ( '-' );
        }
    }
}
