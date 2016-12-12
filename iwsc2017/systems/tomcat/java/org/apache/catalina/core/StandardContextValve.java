package org.apache.catalina.core;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Container;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.res.StringManager;
final class StandardContextValve extends ValveBase {
    private static final StringManager sm = StringManager.getManager ( StandardContextValve.class );
    public StandardContextValve() {
        super ( true );
    }
    @Override
    public void setContainer ( Container container ) {
        super.setContainer ( container );
    }
    @Override
    public final void invoke ( Request request, Response response )
    throws IOException, ServletException {
        MessageBytes requestPathMB = request.getRequestPathMB();
        if ( ( requestPathMB.startsWithIgnoreCase ( "/META-INF/", 0 ) )
                || ( requestPathMB.equalsIgnoreCase ( "/META-INF" ) )
                || ( requestPathMB.startsWithIgnoreCase ( "/WEB-INF/", 0 ) )
                || ( requestPathMB.equalsIgnoreCase ( "/WEB-INF" ) ) ) {
            response.sendError ( HttpServletResponse.SC_NOT_FOUND );
            return;
        }
        Wrapper wrapper = request.getWrapper();
        if ( wrapper == null || wrapper.isUnavailable() ) {
            response.sendError ( HttpServletResponse.SC_NOT_FOUND );
            return;
        }
        try {
            response.sendAcknowledgement();
        } catch ( IOException ioe ) {
            container.getLogger().error ( sm.getString (
                                              "standardContextValve.acknowledgeException" ), ioe );
            request.setAttribute ( RequestDispatcher.ERROR_EXCEPTION, ioe );
            response.sendError ( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            return;
        }
        if ( request.isAsyncSupported() ) {
            request.setAsyncSupported ( wrapper.getPipeline().isAsyncSupported() );
        }
        wrapper.getPipeline().getFirst().invoke ( request, response );
    }
}
