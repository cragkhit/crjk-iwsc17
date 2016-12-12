package org.apache.catalina.valves;
import java.util.Iterator;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class ResponseHeaderElement implements AccessLogElement {
    private final String header;
    public ResponseHeaderElement ( final String header ) {
        this.header = header;
    }
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        if ( null != response ) {
            final Iterator<String> iter = response.getHeaders ( this.header ).iterator();
            if ( iter.hasNext() ) {
                buf.append ( iter.next() );
                while ( iter.hasNext() ) {
                    buf.append ( ',' ).append ( iter.next() );
                }
                return;
            }
        }
        buf.append ( '-' );
    }
}
