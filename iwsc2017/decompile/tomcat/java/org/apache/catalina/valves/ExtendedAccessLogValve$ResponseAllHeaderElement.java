package org.apache.catalina.valves;
import java.util.Iterator;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class ResponseAllHeaderElement implements AccessLogElement {
    private final String header;
    public ResponseAllHeaderElement ( final String header ) {
        this.header = header;
    }
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        if ( null != response ) {
            final Iterator<String> iter = response.getHeaders ( this.header ).iterator();
            if ( iter.hasNext() ) {
                final StringBuilder buffer = new StringBuilder();
                boolean first = true;
                while ( iter.hasNext() ) {
                    if ( first ) {
                        first = false;
                    } else {
                        buffer.append ( "," );
                    }
                    buffer.append ( iter.next() );
                }
                buf.append ( ExtendedAccessLogValve.wrap ( buffer.toString() ) );
            }
            return;
        }
        buf.append ( "-" );
    }
}
