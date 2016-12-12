package org.apache.catalina.valves;
import java.util.Enumeration;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class HeaderElement implements AccessLogElement {
    private final String header;
    public HeaderElement ( final String header ) {
        this.header = header;
    }
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        final Enumeration<String> iter = request.getHeaders ( this.header );
        if ( iter.hasMoreElements() ) {
            buf.append ( iter.nextElement() );
            while ( iter.hasMoreElements() ) {
                buf.append ( ',' ).append ( iter.nextElement() );
            }
            return;
        }
        buf.append ( '-' );
    }
}
