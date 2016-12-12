package org.apache.catalina.valves;
import java.io.IOException;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.juli.logging.Log;
public abstract class RequestFilterValve extends ValveBase {
    public RequestFilterValve() {
        super ( true );
    }
    protected volatile Pattern allow = null;
    protected volatile String allowValue = null;
    protected volatile boolean allowValid = true;
    protected volatile Pattern deny = null;
    protected volatile String denyValue = null;
    protected volatile boolean denyValid = true;
    protected int denyStatus = HttpServletResponse.SC_FORBIDDEN;
    private boolean invalidAuthenticationWhenDeny = false;
    public String getAllow() {
        return allowValue;
    }
    public void setAllow ( String allow ) {
        if ( allow == null || allow.length() == 0 ) {
            this.allow = null;
            allowValue = null;
            allowValid = true;
        } else {
            boolean success = false;
            try {
                allowValue = allow;
                this.allow = Pattern.compile ( allow );
                success = true;
            } finally {
                allowValid = success;
            }
        }
    }
    public String getDeny() {
        return denyValue;
    }
    public void setDeny ( String deny ) {
        if ( deny == null || deny.length() == 0 ) {
            this.deny = null;
            denyValue = null;
            denyValid = true;
        } else {
            boolean success = false;
            try {
                denyValue = deny;
                this.deny = Pattern.compile ( deny );
                success = true;
            } finally {
                denyValid = success;
            }
        }
    }
    public final boolean isAllowValid() {
        return allowValid;
    }
    public final boolean isDenyValid() {
        return denyValid;
    }
    public int getDenyStatus() {
        return denyStatus;
    }
    public void setDenyStatus ( int denyStatus ) {
        this.denyStatus = denyStatus;
    }
    public boolean getInvalidAuthenticationWhenDeny() {
        return invalidAuthenticationWhenDeny;
    }
    public void setInvalidAuthenticationWhenDeny ( boolean value ) {
        invalidAuthenticationWhenDeny = value;
    }
    @Override
    public abstract void invoke ( Request request, Response response )
    throws IOException, ServletException;
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        if ( !allowValid || !denyValid ) {
            throw new LifecycleException (
                sm.getString ( "requestFilterValve.configInvalid" ) );
        }
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        if ( !allowValid || !denyValid ) {
            throw new LifecycleException (
                sm.getString ( "requestFilterValve.configInvalid" ) );
        }
        super.startInternal();
    }
    protected void process ( String property, Request request, Response response )
    throws IOException, ServletException {
        if ( isAllowed ( property ) ) {
            getNext().invoke ( request, response );
            return;
        }
        if ( getLog().isDebugEnabled() ) {
            getLog().debug ( sm.getString ( "requestFilterValve.deny",
                                            request.getRequestURI(), property ) );
        }
        denyRequest ( request, response );
    }
    protected abstract Log getLog();
    protected void denyRequest ( Request request, Response response )
    throws IOException, ServletException {
        if ( invalidAuthenticationWhenDeny ) {
            Context context = request.getContext();
            if ( context != null && context.getPreemptiveAuthentication() ) {
                if ( request.getCoyoteRequest().getMimeHeaders().getValue ( "authorization" ) == null ) {
                    request.getCoyoteRequest().getMimeHeaders().addValue ( "authorization" ).setString ( "invalid" );
                }
                getNext().invoke ( request, response );
                return;
            }
        }
        response.sendError ( denyStatus );
    }
    public boolean isAllowed ( String property ) {
        Pattern deny = this.deny;
        Pattern allow = this.allow;
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
}
