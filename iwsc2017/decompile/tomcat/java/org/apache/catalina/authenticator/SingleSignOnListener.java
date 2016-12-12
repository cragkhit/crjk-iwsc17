package org.apache.catalina.authenticator;
import org.apache.catalina.Authenticator;
import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.SessionEvent;
import java.io.Serializable;
import org.apache.catalina.SessionListener;
public class SingleSignOnListener implements SessionListener, Serializable {
    private static final long serialVersionUID = 1L;
    private final String ssoId;
    public SingleSignOnListener ( final String ssoId ) {
        this.ssoId = ssoId;
    }
    @Override
    public void sessionEvent ( final SessionEvent event ) {
        if ( !"destroySession".equals ( event.getType() ) ) {
            return;
        }
        final Session session = event.getSession();
        final Manager manager = session.getManager();
        if ( manager == null ) {
            return;
        }
        final Context context = manager.getContext();
        final Authenticator authenticator = context.getAuthenticator();
        if ( ! ( authenticator instanceof AuthenticatorBase ) ) {
            return;
        }
        final SingleSignOn sso = ( ( AuthenticatorBase ) authenticator ).sso;
        if ( sso == null ) {
            return;
        }
        sso.sessionDestroyed ( this.ssoId, session );
    }
}
