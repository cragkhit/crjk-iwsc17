package org.apache.catalina.valves;
import org.apache.juli.logging.LogFactory;
import javax.servlet.ServletException;
import java.io.IOException;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import org.apache.juli.logging.Log;
public final class RemoteAddrValve extends RequestFilterValve {
    private static final Log log;
    volatile boolean addConnectorPort;
    public RemoteAddrValve() {
        this.addConnectorPort = false;
    }
    public boolean getAddConnectorPort() {
        return this.addConnectorPort;
    }
    public void setAddConnectorPort ( final boolean addConnectorPort ) {
        this.addConnectorPort = addConnectorPort;
    }
    @Override
    public void invoke ( final Request request, final Response response ) throws IOException, ServletException {
        String property;
        if ( this.addConnectorPort ) {
            property = request.getRequest().getRemoteAddr() + ";" + request.getConnector().getPort();
        } else {
            property = request.getRequest().getRemoteAddr();
        }
        this.process ( property, request, response );
    }
    @Override
    protected Log getLog() {
        return RemoteAddrValve.log;
    }
    static {
        log = LogFactory.getLog ( RemoteAddrValve.class );
    }
}
