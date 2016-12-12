package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected class ProtocolElement implements AccessLogElement {
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        if ( AbstractAccessLogValve.this.requestAttributesEnabled ) {
            final Object proto = request.getAttribute ( "org.apache.catalina.AccessLog.Protocol" );
            if ( proto == null ) {
                buf.append ( request.getProtocol() );
            } else {
                buf.append ( proto.toString() );
            }
        } else {
            buf.append ( request.getProtocol() );
        }
    }
}
