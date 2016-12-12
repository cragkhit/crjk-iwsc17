package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected class HostElement implements AccessLogElement {
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        String value = null;
        if ( AbstractAccessLogValve.this.requestAttributesEnabled ) {
            final Object host = request.getAttribute ( "org.apache.catalina.AccessLog.RemoteHost" );
            if ( host != null ) {
                value = host.toString();
            }
        }
        if ( value == null || value.length() == 0 ) {
            value = request.getRemoteHost();
        }
        if ( value == null || value.length() == 0 ) {
            value = "-";
        }
        buf.append ( value );
    }
}
