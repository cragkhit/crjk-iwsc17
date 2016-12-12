package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class UserElement implements AccessLogElement {
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        if ( request != null ) {
            final String value = request.getRemoteUser();
            if ( value != null ) {
                buf.append ( value );
            } else {
                buf.append ( '-' );
            }
        } else {
            buf.append ( '-' );
        }
    }
}
