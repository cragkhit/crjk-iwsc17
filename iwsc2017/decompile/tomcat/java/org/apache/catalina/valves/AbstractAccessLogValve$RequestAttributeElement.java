package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class RequestAttributeElement implements AccessLogElement {
    private final String header;
    public RequestAttributeElement ( final String header ) {
        this.header = header;
    }
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        Object value = null;
        if ( request != null ) {
            value = request.getAttribute ( this.header );
        } else {
            value = "??";
        }
        if ( value != null ) {
            if ( value instanceof String ) {
                buf.append ( ( CharSequence ) value );
            } else {
                buf.append ( value.toString() );
            }
        } else {
            buf.append ( '-' );
        }
    }
}
