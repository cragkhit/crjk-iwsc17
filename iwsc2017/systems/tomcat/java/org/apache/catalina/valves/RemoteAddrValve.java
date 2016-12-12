package org.apache.catalina.valves;
import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public final class RemoteAddrValve extends RequestFilterValve {
    private static final Log log = LogFactory.getLog ( RemoteAddrValve.class );
    volatile boolean addConnectorPort = false;
    public boolean getAddConnectorPort() {
        return addConnectorPort;
    }
    public void setAddConnectorPort ( boolean addConnectorPort ) {
        this.addConnectorPort = addConnectorPort;
    }
    @Override
    public void invoke ( Request request, Response response ) throws IOException, ServletException {
        String property;
        if ( addConnectorPort ) {
            property = request.getRequest().getRemoteAddr() + ";" + request.getConnector().getPort();
        } else {
            property = request.getRequest().getRemoteAddr();
        }
        process ( property, request, response );
    }
    @Override
    protected Log getLog() {
        return log;
    }
}
