package org.apache.catalina.authenticator;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Request;
public final class NonLoginAuthenticator extends AuthenticatorBase {
    @Override
    protected boolean doAuthenticate ( Request request, HttpServletResponse response )
    throws IOException {
        if ( checkForCachedAuthentication ( request, response, true ) ) {
            if ( cache ) {
                request.getSessionInternal ( true ).setPrincipal ( request.getUserPrincipal() );
            }
            return true;
        }
        if ( containerLog.isDebugEnabled() ) {
            containerLog.debug ( "User authenticated without any roles" );
        }
        return true;
    }
    @Override
    protected String getAuthMethod() {
        return "NONE";
    }
}
