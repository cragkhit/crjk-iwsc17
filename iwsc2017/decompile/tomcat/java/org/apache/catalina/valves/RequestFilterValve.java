package org.apache.catalina.valves;
import org.apache.catalina.Context;
import org.apache.juli.logging.Log;
import org.apache.catalina.LifecycleException;
import javax.servlet.ServletException;
import java.io.IOException;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.regex.Pattern;
public abstract class RequestFilterValve extends ValveBase {
    protected volatile Pattern allow;
    protected volatile String allowValue;
    protected volatile boolean allowValid;
    protected volatile Pattern deny;
    protected volatile String denyValue;
    protected volatile boolean denyValid;
    protected int denyStatus;
    private boolean invalidAuthenticationWhenDeny;
    public RequestFilterValve() {
        super ( true );
        this.allow = null;
        this.allowValue = null;
        this.allowValid = true;
        this.deny = null;
        this.denyValue = null;
        this.denyValid = true;
        this.denyStatus = 403;
        this.invalidAuthenticationWhenDeny = false;
    }
    public String getAllow() {
        return this.allowValue;
    }
    public void setAllow ( final String allow ) {
        if ( allow == null || allow.length() == 0 ) {
            this.allow = null;
            this.allowValue = null;
            this.allowValid = true;
        } else {
            boolean success = false;
            try {
                this.allowValue = allow;
                this.allow = Pattern.compile ( allow );
                success = true;
            } finally {
                this.allowValid = success;
            }
        }
    }
    public String getDeny() {
        return this.denyValue;
    }
    public void setDeny ( final String deny ) {
        if ( deny == null || deny.length() == 0 ) {
            this.deny = null;
            this.denyValue = null;
            this.denyValid = true;
        } else {
            boolean success = false;
            try {
                this.denyValue = deny;
                this.deny = Pattern.compile ( deny );
                success = true;
            } finally {
                this.denyValid = success;
            }
        }
    }
    public final boolean isAllowValid() {
        return this.allowValid;
    }
    public final boolean isDenyValid() {
        return this.denyValid;
    }
    public int getDenyStatus() {
        return this.denyStatus;
    }
    public void setDenyStatus ( final int denyStatus ) {
        this.denyStatus = denyStatus;
    }
    public boolean getInvalidAuthenticationWhenDeny() {
        return this.invalidAuthenticationWhenDeny;
    }
    public void setInvalidAuthenticationWhenDeny ( final boolean value ) {
        this.invalidAuthenticationWhenDeny = value;
    }
    @Override
    public abstract void invoke ( final Request p0, final Response p1 ) throws IOException, ServletException;
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        if ( !this.allowValid || !this.denyValid ) {
            throw new LifecycleException ( RequestFilterValve.sm.getString ( "requestFilterValve.configInvalid" ) );
        }
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        if ( !this.allowValid || !this.denyValid ) {
            throw new LifecycleException ( RequestFilterValve.sm.getString ( "requestFilterValve.configInvalid" ) );
        }
        super.startInternal();
    }
    protected void process ( final String property, final Request request, final Response response ) throws IOException, ServletException {
        if ( this.isAllowed ( property ) ) {
            this.getNext().invoke ( request, response );
            return;
        }
        if ( this.getLog().isDebugEnabled() ) {
            this.getLog().debug ( RequestFilterValve.sm.getString ( "requestFilterValve.deny", request.getRequestURI(), property ) );
        }
        this.denyRequest ( request, response );
    }
    protected abstract Log getLog();
    protected void denyRequest ( final Request request, final Response response ) throws IOException, ServletException {
        if ( this.invalidAuthenticationWhenDeny ) {
            final Context context = request.getContext();
            if ( context != null && context.getPreemptiveAuthentication() ) {
                if ( request.getCoyoteRequest().getMimeHeaders().getValue ( "authorization" ) == null ) {
                    request.getCoyoteRequest().getMimeHeaders().addValue ( "authorization" ).setString ( "invalid" );
                }
                this.getNext().invoke ( request, response );
                return;
            }
        }
        response.sendError ( this.denyStatus );
    }
    public boolean isAllowed ( final String property ) {
        final Pattern deny = this.deny;
        final Pattern allow = this.allow;
        return ( deny == null || !deny.matcher ( property ).matches() ) && ( ( allow != null && allow.matcher ( property ).matches() ) || ( deny != null && allow == null ) );
    }
}
