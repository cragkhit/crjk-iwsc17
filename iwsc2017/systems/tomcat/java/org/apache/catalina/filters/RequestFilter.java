package org.apache.catalina.filters;
import java.io.IOException;
import java.util.regex.Pattern;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
public abstract class RequestFilter extends FilterBase {
    protected Pattern allow = null;
    protected Pattern deny = null;
    protected int denyStatus = HttpServletResponse.SC_FORBIDDEN;
    private static final String PLAIN_TEXT_MIME_TYPE = "text/plain";
    public String getAllow() {
        if ( allow == null ) {
            return null;
        }
        return allow.toString();
    }
    public void setAllow ( String allow ) {
        if ( allow == null || allow.length() == 0 ) {
            this.allow = null;
        } else {
            this.allow = Pattern.compile ( allow );
        }
    }
    public String getDeny() {
        if ( deny == null ) {
            return null;
        }
        return deny.toString();
    }
    public void setDeny ( String deny ) {
        if ( deny == null || deny.length() == 0 ) {
            this.deny = null;
        } else {
            this.deny = Pattern.compile ( deny );
        }
    }
    public int getDenyStatus() {
        return denyStatus;
    }
    public void setDenyStatus ( int denyStatus ) {
        this.denyStatus = denyStatus;
    }
    @Override
    public abstract void doFilter ( ServletRequest request,
                                    ServletResponse response, FilterChain chain ) throws IOException,
                                                        ServletException;
    @Override
    protected boolean isConfigProblemFatal() {
        return true;
    }
    protected void process ( String property, ServletRequest request,
                             ServletResponse response, FilterChain chain )
    throws IOException, ServletException {
        if ( isAllowed ( property ) ) {
            chain.doFilter ( request, response );
        } else {
            if ( response instanceof HttpServletResponse ) {
                if ( getLogger().isDebugEnabled() ) {
                    getLogger().debug ( sm.getString ( "requestFilter.deny",
                                                       ( ( HttpServletRequest ) request ).getRequestURI(), property ) );
                }
                ( ( HttpServletResponse ) response ).sendError ( denyStatus );
            } else {
                sendErrorWhenNotHttp ( response );
            }
        }
    }
    private boolean isAllowed ( String property ) {
        if ( deny != null && deny.matcher ( property ).matches() ) {
            return false;
        }
        if ( allow != null && allow.matcher ( property ).matches() ) {
            return true;
        }
        if ( deny != null && allow == null ) {
            return true;
        }
        return false;
    }
    private void sendErrorWhenNotHttp ( ServletResponse response )
    throws IOException {
        response.setContentType ( PLAIN_TEXT_MIME_TYPE );
        response.getWriter().write ( sm.getString ( "http.403" ) );
        response.getWriter().flush();
    }
}
