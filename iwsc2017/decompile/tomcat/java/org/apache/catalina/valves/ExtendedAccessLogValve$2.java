package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
class ExtendedAccessLogValve$2 implements AccessLogElement {
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        final String query = request.getQueryString();
        if ( query != null ) {
            buf.append ( query );
        } else {
            buf.append ( '-' );
        }
    }
}
