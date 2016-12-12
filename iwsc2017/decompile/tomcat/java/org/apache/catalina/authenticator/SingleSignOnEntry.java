package org.apache.catalina.authenticator;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Set;
import org.apache.catalina.Session;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.security.Principal;
import java.io.Serializable;
public class SingleSignOnEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private String authType;
    private String password;
    private transient Principal principal;
    private final Map<SingleSignOnSessionKey, SingleSignOnSessionKey> sessionKeys;
    private String username;
    private boolean canReauthenticate;
    public SingleSignOnEntry ( final Principal principal, final String authType, final String username, final String password ) {
        this.authType = null;
        this.password = null;
        this.principal = null;
        this.sessionKeys = new ConcurrentHashMap<SingleSignOnSessionKey, SingleSignOnSessionKey>();
        this.username = null;
        this.canReauthenticate = false;
        this.updateCredentials ( principal, authType, username, password );
    }
    public void addSession ( final SingleSignOn sso, final String ssoId, final Session session ) {
        final SingleSignOnSessionKey key = new SingleSignOnSessionKey ( session );
        final SingleSignOnSessionKey currentKey = this.sessionKeys.putIfAbsent ( key, key );
        if ( currentKey == null ) {
            session.addSessionListener ( sso.getSessionListener ( ssoId ) );
        }
    }
    public void removeSession ( final Session session ) {
        final SingleSignOnSessionKey key = new SingleSignOnSessionKey ( session );
        this.sessionKeys.remove ( key );
    }
    public Set<SingleSignOnSessionKey> findSessions() {
        return this.sessionKeys.keySet();
    }
    public String getAuthType() {
        return this.authType;
    }
    public boolean getCanReauthenticate() {
        return this.canReauthenticate;
    }
    public String getPassword() {
        return this.password;
    }
    public Principal getPrincipal() {
        return this.principal;
    }
    public String getUsername() {
        return this.username;
    }
    public synchronized void updateCredentials ( final Principal principal, final String authType, final String username, final String password ) {
        this.principal = principal;
        this.authType = authType;
        this.username = username;
        this.password = password;
        this.canReauthenticate = ( "BASIC".equals ( authType ) || "FORM".equals ( authType ) );
    }
    private void writeObject ( final ObjectOutputStream out ) throws IOException {
        out.defaultWriteObject();
        if ( this.principal instanceof Serializable ) {
            out.writeBoolean ( true );
            out.writeObject ( this.principal );
        } else {
            out.writeBoolean ( false );
        }
    }
    private void readObject ( final ObjectInputStream in ) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        final boolean hasPrincipal = in.readBoolean();
        if ( hasPrincipal ) {
            this.principal = ( Principal ) in.readObject();
        }
    }
}
