package org.apache.catalina.valves;
import org.apache.catalina.Session;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class SessionIdElement implements AccessLogElement {
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        if ( request == null ) {
            buf.append ( '-' );
        } else {
            final Session session = request.getSessionInternal ( false );
            if ( session == null ) {
                buf.append ( '-' );
            } else {
                buf.append ( session.getIdInternal() );
            }
        }
    }
}
