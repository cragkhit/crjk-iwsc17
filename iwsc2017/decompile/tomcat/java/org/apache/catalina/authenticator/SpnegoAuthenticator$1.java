package org.apache.catalina.authenticator;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSCredential;
import java.security.PrivilegedExceptionAction;
class SpnegoAuthenticator$1 implements PrivilegedExceptionAction<GSSCredential> {
    final   GSSManager val$manager;
    final   int val$credentialLifetime;
    @Override
    public GSSCredential run() throws GSSException {
        return this.val$manager.createCredential ( null, this.val$credentialLifetime, new Oid ( "1.3.6.1.5.5.2" ), 2 );
    }
}
