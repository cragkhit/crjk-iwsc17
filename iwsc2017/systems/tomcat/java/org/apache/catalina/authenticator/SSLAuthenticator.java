package org.apache.catalina.authenticator;
import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Request;
public class SSLAuthenticator extends AuthenticatorBase {
    @Override
    protected boolean doAuthenticate ( Request request, HttpServletResponse response )
    throws IOException {
        if ( checkForCachedAuthentication ( request, response, false ) ) {
            return true;
        }
        if ( containerLog.isDebugEnabled() ) {
            containerLog.debug ( " Looking up certificates" );
        }
        X509Certificate certs[] = getRequestCertificates ( request );
        if ( ( certs == null ) || ( certs.length < 1 ) ) {
            if ( containerLog.isDebugEnabled() ) {
                containerLog.debug ( "  No certificates included with this request" );
            }
            response.sendError ( HttpServletResponse.SC_UNAUTHORIZED,
                                 sm.getString ( "authenticator.certificates" ) );
            return false;
        }
        Principal principal = context.getRealm().authenticate ( certs );
        if ( principal == null ) {
            if ( containerLog.isDebugEnabled() ) {
                containerLog.debug ( "  Realm.authenticate() returned false" );
            }
            response.sendError ( HttpServletResponse.SC_UNAUTHORIZED,
                                 sm.getString ( "authenticator.unauthorized" ) );
            return false;
        }
        register ( request, response, principal,
                   HttpServletRequest.CLIENT_CERT_AUTH, null, null );
        return true;
    }
    @Override
    protected String getAuthMethod() {
        return HttpServletRequest.CLIENT_CERT_AUTH;
    }
}
