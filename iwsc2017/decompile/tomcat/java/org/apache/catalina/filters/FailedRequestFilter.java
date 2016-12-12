package org.apache.catalina.filters;
import org.apache.juli.logging.LogFactory;
import javax.servlet.ServletException;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.Parameters;
import javax.servlet.FilterChain;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import org.apache.juli.logging.Log;
public class FailedRequestFilter extends FilterBase {
    private static final Log log;
    @Override
    protected Log getLogger() {
        return FailedRequestFilter.log;
    }
    public void doFilter ( final ServletRequest request, final ServletResponse response, final FilterChain chain ) throws IOException, ServletException {
        if ( !this.isGoodRequest ( request ) ) {
            final Parameters.FailReason reason = ( Parameters.FailReason ) request.getAttribute ( "org.apache.catalina.parameter_parse_failed_reason" );
            int status = 0;
            switch ( reason ) {
            case IO_ERROR: {
                status = 500;
                break;
            }
            case POST_TOO_LARGE: {
                status = 413;
                break;
            }
            default: {
                status = 400;
                break;
            }
            }
            ( ( HttpServletResponse ) response ).sendError ( status );
            return;
        }
        chain.doFilter ( request, response );
    }
    private boolean isGoodRequest ( final ServletRequest request ) {
        request.getParameter ( "none" );
        return request.getAttribute ( "org.apache.catalina.parameter_parse_failed" ) == null;
    }
    @Override
    protected boolean isConfigProblemFatal() {
        return true;
    }
    static {
        log = LogFactory.getLog ( FailedRequestFilter.class );
    }
}
