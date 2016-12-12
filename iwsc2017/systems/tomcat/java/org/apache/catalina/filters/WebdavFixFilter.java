package org.apache.catalina.filters;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.GenericFilter;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
public class WebdavFixFilter extends GenericFilter {
    private static final long serialVersionUID = 1L;
    private static final String LOG_MESSAGE_PREAMBLE =
        "WebdavFixFilter: Detected client problem: ";
    private static final String UA_MINIDIR_START =
        "Microsoft-WebDAV-MiniRedir";
    private static final String UA_MINIDIR_5_1_2600 =
        "Microsoft-WebDAV-MiniRedir/5.1.2600";
    private static final String UA_MINIDIR_5_2_3790 =
        "Microsoft-WebDAV-MiniRedir/5.2.3790";
    @Override
    public void doFilter ( ServletRequest request, ServletResponse response,
                           FilterChain chain ) throws IOException, ServletException {
        if ( ! ( request instanceof HttpServletRequest ) ||
                ! ( response instanceof HttpServletResponse ) ) {
            chain.doFilter ( request, response );
            return;
        }
        HttpServletRequest httpRequest = ( ( HttpServletRequest ) request );
        HttpServletResponse httpResponse = ( ( HttpServletResponse ) response );
        String ua = httpRequest.getHeader ( "User-Agent" );
        if ( ua == null || ua.length() == 0 ||
                !ua.startsWith ( UA_MINIDIR_START ) ) {
            chain.doFilter ( request, response );
        } else if ( ua.startsWith ( UA_MINIDIR_5_1_2600 ) ) {
            httpResponse.sendRedirect ( buildRedirect ( httpRequest ) );
        } else if ( ua.startsWith ( UA_MINIDIR_5_2_3790 ) ) {
            if ( !"".equals ( httpRequest.getContextPath() ) ) {
                log ( "XP-x64-SP2 clients only work with the root context" );
            }
            log ( "XP-x64-SP2 is known not to work with WebDAV Servlet" );
            chain.doFilter ( request, response );
        } else {
            httpResponse.sendRedirect ( buildRedirect ( httpRequest ) );
        }
    }
    private String buildRedirect ( HttpServletRequest request ) {
        StringBuilder location =
            new StringBuilder ( request.getRequestURL().length() );
        location.append ( request.getScheme() );
        location.append ( "://" );
        location.append ( request.getServerName() );
        location.append ( ':' );
        location.append ( request.getServerPort() );
        location.append ( request.getRequestURI() );
        return location.toString();
    }
    private void log ( String msg ) {
        StringBuilder builder = new StringBuilder ( LOG_MESSAGE_PREAMBLE );
        builder.append ( msg );
        getServletContext().log ( builder.toString() );
    }
}
