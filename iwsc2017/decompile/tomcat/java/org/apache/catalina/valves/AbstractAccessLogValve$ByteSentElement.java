package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class ByteSentElement implements AccessLogElement {
    private final boolean conversion;
    public ByteSentElement ( final boolean conversion ) {
        this.conversion = conversion;
    }
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        long length = response.getBytesWritten ( false );
        if ( length <= 0L ) {
            final Object start = request.getAttribute ( "org.apache.tomcat.sendfile.start" );
            if ( start instanceof Long ) {
                final Object end = request.getAttribute ( "org.apache.tomcat.sendfile.end" );
                if ( end instanceof Long ) {
                    length = ( long ) end - ( long ) start;
                }
            }
        }
        if ( length <= 0L && this.conversion ) {
            buf.append ( '-' );
        } else {
            buf.append ( Long.toString ( length ) );
        }
    }
}
