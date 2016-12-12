package org.apache.tomcat.util.net;
import java.util.HashSet;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import java.util.Set;
public enum Type {
    UNDEFINED ( new Authentication[0] ),
    RSA ( new Authentication[] { Authentication.RSA } ),
    DSA ( new Authentication[] { Authentication.DSS } ),
    EC ( new Authentication[] { Authentication.ECDH, Authentication.ECDSA } );
    private final Set<Authentication> compatibleAuthentications;
    private Type ( final Authentication[] authentications ) {
        this.compatibleAuthentications = new HashSet<Authentication>();
        if ( authentications != null ) {
            for ( final Authentication authentication : authentications ) {
                this.compatibleAuthentications.add ( authentication );
            }
        }
    }
    public boolean isCompatibleWith ( final Authentication au ) {
        return this.compatibleAuthentications.contains ( au );
    }
}
