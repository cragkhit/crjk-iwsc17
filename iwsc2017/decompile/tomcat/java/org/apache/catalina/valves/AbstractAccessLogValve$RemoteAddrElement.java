package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected class RemoteAddrElement implements AccessLogElement {
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        if ( AbstractAccessLogValve.this.requestAttributesEnabled ) {
            final Object addr = request.getAttribute ( "org.apache.catalina.AccessLog.RemoteAddr" );
            if ( addr == null ) {
                buf.append ( request.getRemoteAddr() );
            } else {
                buf.append ( addr.toString() );
            }
        } else {
            buf.append ( request.getRemoteAddr() );
        }
    }
}
