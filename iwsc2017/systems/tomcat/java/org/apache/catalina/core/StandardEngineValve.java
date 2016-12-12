package org.apache.catalina.core;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Host;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.tomcat.util.res.StringManager;
final class StandardEngineValve extends ValveBase {
    public StandardEngineValve() {
        super ( true );
    }
    private static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    @Override
    public final void invoke ( Request request, Response response )
    throws IOException, ServletException {
        Host host = request.getHost();
        if ( host == null ) {
            response.sendError
            ( HttpServletResponse.SC_BAD_REQUEST,
              sm.getString ( "standardEngine.noHost",
                             request.getServerName() ) );
            return;
        }
        if ( request.isAsyncSupported() ) {
            request.setAsyncSupported ( host.getPipeline().isAsyncSupported() );
        }
        host.getPipeline().getFirst().invoke ( request, response );
    }
}
