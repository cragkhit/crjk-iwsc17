package org.apache.catalina.filters;
import javax.servlet.ServletException;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.FilterChain;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.GenericFilter;
public class WebdavFixFilter extends GenericFilter {
    private static final long serialVersionUID = 1L;
    private static final String LOG_MESSAGE_PREAMBLE = "WebdavFixFilter: Detected client problem: ";
    private static final String UA_MINIDIR_START = "Microsoft-WebDAV-MiniRedir";
    private static final String UA_MINIDIR_5_1_2600 = "Microsoft-WebDAV-MiniRedir/5.1.2600";
    private static final String UA_MINIDIR_5_2_3790 = "Microsoft-WebDAV-MiniRedir/5.2.3790";
    public void doFilter ( final ServletRequest request, final ServletResponse response, final FilterChain chain ) throws IOException, ServletException {
        if ( ! ( request instanceof HttpServletRequest ) || ! ( response instanceof HttpServletResponse ) ) {
            chain.doFilter ( request, response );
            return;
        }
        final HttpServletRequest httpRequest = ( HttpServletRequest ) request;
        final HttpServletResponse httpResponse = ( HttpServletResponse ) response;
        final String ua = httpRequest.getHeader ( "User-Agent" );
        if ( ua == null || ua.length() == 0 || !ua.startsWith ( "Microsoft-WebDAV-MiniRedir" ) ) {
            chain.doFilter ( request, response );
        } else if ( ua.startsWith ( "Microsoft-WebDAV-MiniRedir/5.1.2600" ) ) {
            httpResponse.sendRedirect ( this.buildRedirect ( httpRequest ) );
        } else if ( ua.startsWith ( "Microsoft-WebDAV-MiniRedir/5.2.3790" ) ) {
            if ( !"".equals ( httpRequest.getContextPath() ) ) {
                this.log ( "XP-x64-SP2 clients only work with the root context" );
            }
            this.log ( "XP-x64-SP2 is known not to work with WebDAV Servlet" );
            chain.doFilter ( request, response );
        } else {
            httpResponse.sendRedirect ( this.buildRedirect ( httpRequest ) );
        }
    }
    private String buildRedirect ( final HttpServletRequest request ) {
        final StringBuilder location = new StringBuilder ( request.getRequestURL().length() );
        location.append ( request.getScheme() );
        location.append ( "://" );
        location.append ( request.getServerName() );
        location.append ( ':' );
        location.append ( request.getServerPort() );
        location.append ( request.getRequestURI() );
        return location.toString();
    }
    private void log ( final String msg ) {
        final StringBuilder builder = new StringBuilder ( "WebdavFixFilter: Detected client problem: " );
        builder.append ( msg );
        this.getServletContext().log ( builder.toString() );
    }
}
