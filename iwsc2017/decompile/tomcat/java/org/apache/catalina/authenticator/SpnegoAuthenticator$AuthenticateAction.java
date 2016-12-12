package org.apache.catalina.authenticator;
import org.ietf.jgss.GSSContext;
import org.apache.catalina.Realm;
import java.security.Principal;
import java.security.PrivilegedAction;
public static class AuthenticateAction implements PrivilegedAction<Principal> {
    private final Realm realm;
    private final GSSContext gssContext;
    private final boolean storeDelegatedCredential;
    public AuthenticateAction ( final Realm realm, final GSSContext gssContext, final boolean storeDelegatedCredential ) {
        this.realm = realm;
        this.gssContext = gssContext;
        this.storeDelegatedCredential = storeDelegatedCredential;
    }
    @Override
    public Principal run() {
        return this.realm.authenticate ( this.gssContext, this.storeDelegatedCredential );
    }
}
