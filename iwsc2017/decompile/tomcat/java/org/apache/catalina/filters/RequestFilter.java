package org.apache.catalina.filters;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import java.util.regex.Pattern;
public abstract class RequestFilter extends FilterBase {
    protected Pattern allow;
    protected Pattern deny;
    protected int denyStatus;
    private static final String PLAIN_TEXT_MIME_TYPE = "text/plain";
    public RequestFilter() {
        this.allow = null;
        this.deny = null;
        this.denyStatus = 403;
    }
    public String getAllow() {
        if ( this.allow == null ) {
            return null;
        }
        return this.allow.toString();
    }
    public void setAllow ( final String allow ) {
        if ( allow == null || allow.length() == 0 ) {
            this.allow = null;
        } else {
            this.allow = Pattern.compile ( allow );
        }
    }
    public String getDeny() {
        if ( this.deny == null ) {
            return null;
        }
        return this.deny.toString();
    }
    public void setDeny ( final String deny ) {
        if ( deny == null || deny.length() == 0 ) {
            this.deny = null;
        } else {
            this.deny = Pattern.compile ( deny );
        }
    }
    public int getDenyStatus() {
        return this.denyStatus;
    }
    public void setDenyStatus ( final int denyStatus ) {
        this.denyStatus = denyStatus;
    }
    public abstract void doFilter ( final ServletRequest p0, final ServletResponse p1, final FilterChain p2 ) throws IOException, ServletException;
    @Override
    protected boolean isConfigProblemFatal() {
        return true;
    }
    protected void process ( final String property, final ServletRequest request, final ServletResponse response, final FilterChain chain ) throws IOException, ServletException {
        if ( this.isAllowed ( property ) ) {
            chain.doFilter ( request, response );
        } else if ( response instanceof HttpServletResponse ) {
            if ( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug ( RequestFilter.sm.getString ( "requestFilter.deny", ( ( HttpServletRequest ) request ).getRequestURI(), property ) );
            }
            ( ( HttpServletResponse ) response ).sendError ( this.denyStatus );
        } else {
            this.sendErrorWhenNotHttp ( response );
        }
    }
    private boolean isAllowed ( final String property ) {
        return ( this.deny == null || !this.deny.matcher ( property ).matches() ) && ( ( this.allow != null && this.allow.matcher ( property ).matches() ) || ( this.deny != null && this.allow == null ) );
    }
    private void sendErrorWhenNotHttp ( final ServletResponse response ) throws IOException {
        response.setContentType ( "text/plain" );
        response.getWriter().write ( RequestFilter.sm.getString ( "http.403" ) );
        response.getWriter().flush();
    }
}
