package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected class PortElement implements AccessLogElement {
    private static final String localPort = "local";
    private static final String remotePort = "remote";
    private final PortType portType;
    public PortElement() {
        this.portType = PortType.LOCAL;
    }
    public PortElement ( final String type ) {
        switch ( type ) {
        case "remote": {
            this.portType = PortType.REMOTE;
            break;
        }
        case "local": {
            this.portType = PortType.LOCAL;
            break;
        }
        default: {
            AbstractAccessLogValve.access$700().error ( ValveBase.sm.getString ( "accessLogValve.invalidPortType", type ) );
            this.portType = PortType.LOCAL;
            break;
        }
        }
    }
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        if ( AbstractAccessLogValve.this.requestAttributesEnabled && this.portType == PortType.LOCAL ) {
            final Object port = request.getAttribute ( "org.apache.catalina.AccessLog.ServerPort" );
            if ( port == null ) {
                buf.append ( Integer.toString ( request.getServerPort() ) );
            } else {
                buf.append ( port.toString() );
            }
        } else if ( this.portType == PortType.LOCAL ) {
            buf.append ( Integer.toString ( request.getServerPort() ) );
        } else {
            buf.append ( Integer.toString ( request.getRemotePort() ) );
        }
    }
}
