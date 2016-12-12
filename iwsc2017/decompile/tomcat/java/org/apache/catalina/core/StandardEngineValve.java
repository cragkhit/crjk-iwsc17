package org.apache.catalina.core;
import javax.servlet.ServletException;
import java.io.IOException;
import org.apache.catalina.Host;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import org.apache.tomcat.util.res.StringManager;
import org.apache.catalina.valves.ValveBase;
final class StandardEngineValve extends ValveBase {
    private static final StringManager sm;
    public StandardEngineValve() {
        super ( true );
    }
    @Override
    public final void invoke ( final Request request, final Response response ) throws IOException, ServletException {
        final Host host = request.getHost();
        if ( host == null ) {
            response.sendError ( 400, StandardEngineValve.sm.getString ( "standardEngine.noHost", request.getServerName() ) );
            return;
        }
        if ( request.isAsyncSupported() ) {
            request.setAsyncSupported ( host.getPipeline().isAsyncSupported() );
        }
        host.getPipeline().getFirst().invoke ( request, response );
    }
    static {
        sm = StringManager.getManager ( "org.apache.catalina.core" );
    }
}
