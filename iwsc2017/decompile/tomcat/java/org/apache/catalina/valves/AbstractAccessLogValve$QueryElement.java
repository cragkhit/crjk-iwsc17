package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class QueryElement implements AccessLogElement {
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        String query = null;
        if ( request != null ) {
            query = request.getQueryString();
        }
        if ( query != null ) {
            buf.append ( '?' );
            buf.append ( query );
        }
    }
}
