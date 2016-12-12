package org.apache.catalina.filters;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Globals;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.http.Parameters.FailReason;
public class FailedRequestFilter extends FilterBase {
    private static final Log log = LogFactory.getLog ( FailedRequestFilter.class );
    @Override
    protected Log getLogger() {
        return log;
    }
    @Override
    public void doFilter ( ServletRequest request, ServletResponse response,
                           FilterChain chain ) throws IOException, ServletException {
        if ( !isGoodRequest ( request ) ) {
            FailReason reason = ( FailReason ) request.getAttribute (
                                    Globals.PARAMETER_PARSE_FAILED_REASON_ATTR );
            int status;
            switch ( reason ) {
            case IO_ERROR:
                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                break;
            case POST_TOO_LARGE:
                status = HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE;
                break;
            case TOO_MANY_PARAMETERS:
            case UNKNOWN:
            case INVALID_CONTENT_TYPE:
            case MULTIPART_CONFIG_INVALID:
            case NO_NAME:
            case REQUEST_BODY_INCOMPLETE:
            case URL_DECODING:
            case CLIENT_DISCONNECT:
            default:
                status = HttpServletResponse.SC_BAD_REQUEST;
                break;
            }
            ( ( HttpServletResponse ) response ).sendError ( status );
            return;
        }
        chain.doFilter ( request, response );
    }
    private boolean isGoodRequest ( ServletRequest request ) {
        request.getParameter ( "none" );
        if ( request.getAttribute ( Globals.PARAMETER_PARSE_FAILED_ATTR ) != null ) {
            return false;
        }
        return true;
    }
    @Override
    protected boolean isConfigProblemFatal() {
        return true;
    }
}
