package org.apache.catalina.valves;
import org.apache.juli.logging.LogFactory;
import javax.servlet.ServletException;
import java.io.IOException;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import org.apache.juli.logging.Log;
public final class RemoteHostValve extends RequestFilterValve {
    private static final Log log;
    volatile boolean addConnectorPort;
    public RemoteHostValve() {
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
            property = request.getRequest().getRemoteHost() + ";" + request.getConnector().getPort();
        } else {
            property = request.getRequest().getRemoteHost();
        }
        this.process ( property, request, response );
    }
    @Override
    protected Log getLog() {
        return RemoteHostValve.log;
    }
    static {
        log = LogFactory.getLog ( RemoteHostValve.class );
    }
}
