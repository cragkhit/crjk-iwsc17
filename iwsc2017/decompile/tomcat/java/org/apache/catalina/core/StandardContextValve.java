package org.apache.catalina.core;
import javax.servlet.ServletException;
import org.apache.catalina.Wrapper;
import org.apache.tomcat.util.buf.MessageBytes;
import java.io.IOException;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import org.apache.catalina.Container;
import org.apache.tomcat.util.res.StringManager;
import org.apache.catalina.valves.ValveBase;
final class StandardContextValve extends ValveBase {
    private static final StringManager sm;
    public StandardContextValve() {
        super ( true );
    }
    @Override
    public void setContainer ( final Container container ) {
        super.setContainer ( container );
    }
    @Override
    public final void invoke ( final Request request, final Response response ) throws IOException, ServletException {
        final MessageBytes requestPathMB = request.getRequestPathMB();
        if ( requestPathMB.startsWithIgnoreCase ( "/META-INF/", 0 ) || requestPathMB.equalsIgnoreCase ( "/META-INF" ) || requestPathMB.startsWithIgnoreCase ( "/WEB-INF/", 0 ) || requestPathMB.equalsIgnoreCase ( "/WEB-INF" ) ) {
            response.sendError ( 404 );
            return;
        }
        final Wrapper wrapper = request.getWrapper();
        if ( wrapper == null || wrapper.isUnavailable() ) {
            response.sendError ( 404 );
            return;
        }
        try {
            response.sendAcknowledgement();
        } catch ( IOException ioe ) {
            this.container.getLogger().error ( StandardContextValve.sm.getString ( "standardContextValve.acknowledgeException" ), ioe );
            request.setAttribute ( "javax.servlet.error.exception", ioe );
            response.sendError ( 500 );
            return;
        }
        if ( request.isAsyncSupported() ) {
            request.setAsyncSupported ( wrapper.getPipeline().isAsyncSupported() );
        }
        wrapper.getPipeline().getFirst().invoke ( request, response );
    }
    static {
        sm = StringManager.getManager ( StandardContextValve.class );
    }
}
