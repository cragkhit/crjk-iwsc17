package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class HttpStatusCodeElement implements AccessLogElement {
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        if ( response != null ) {
            final int status = response.getStatus();
            if ( 100 <= status && status < 1000 ) {
                buf.append ( ( char ) ( 48 + status / 100 ) ).append ( ( char ) ( 48 + status / 10 % 10 ) ).append ( ( char ) ( 48 + status % 10 ) );
            } else {
                buf.append ( Integer.toString ( status ) );
            }
        } else {
            buf.append ( '-' );
        }
    }
}
