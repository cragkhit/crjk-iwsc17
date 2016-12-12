package org.apache.catalina.authenticator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Request;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.codec.binary.Base64;
public class BasicAuthenticator extends AuthenticatorBase {
    private static final Log log = LogFactory.getLog ( BasicAuthenticator.class );
    @Override
    protected boolean doAuthenticate ( Request request, HttpServletResponse response )
    throws IOException {
        if ( checkForCachedAuthentication ( request, response, true ) ) {
            return true;
        }
        MessageBytes authorization =
            request.getCoyoteRequest().getMimeHeaders()
            .getValue ( "authorization" );
        if ( authorization != null ) {
            authorization.toBytes();
            ByteChunk authorizationBC = authorization.getByteChunk();
            BasicCredentials credentials = null;
            try {
                credentials = new BasicCredentials ( authorizationBC );
                String username = credentials.getUsername();
                String password = credentials.getPassword();
                Principal principal = context.getRealm().authenticate ( username, password );
                if ( principal != null ) {
                    register ( request, response, principal,
                               HttpServletRequest.BASIC_AUTH, username, password );
                    return true;
                }
            } catch ( IllegalArgumentException iae ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Invalid Authorization" + iae.getMessage() );
                }
            }
        }
        StringBuilder value = new StringBuilder ( 16 );
        value.append ( "Basic realm=\"" );
        value.append ( getRealmName ( context ) );
        value.append ( '\"' );
        response.setHeader ( AUTH_HEADER_NAME, value.toString() );
        response.sendError ( HttpServletResponse.SC_UNAUTHORIZED );
        return false;
    }
    @Override
    protected String getAuthMethod() {
        return HttpServletRequest.BASIC_AUTH;
    }
    public static class BasicCredentials {
        private static final String METHOD = "basic ";
        private ByteChunk authorization;
        private int initialOffset;
        private int base64blobOffset;
        private int base64blobLength;
        private String username = null;
        private String password = null;
        public BasicCredentials ( ByteChunk input )
        throws IllegalArgumentException {
            authorization = input;
            initialOffset = input.getOffset();
            parseMethod();
            byte[] decoded = parseBase64();
            parseCredentials ( decoded );
        }
        public String getUsername() {
            return username;
        }
        public String getPassword() {
            return password;
        }
        private void parseMethod() throws IllegalArgumentException {
            if ( authorization.startsWithIgnoreCase ( METHOD, 0 ) ) {
                base64blobOffset = initialOffset + METHOD.length();
                base64blobLength = authorization.getLength() - METHOD.length();
            } else {
                throw new IllegalArgumentException (
                    "Authorization header method is not \"Basic\"" );
            }
        }
        private byte[] parseBase64() throws IllegalArgumentException {
            byte[] decoded = Base64.decodeBase64 (
                                 authorization.getBuffer(),
                                 base64blobOffset, base64blobLength );
            authorization.setOffset ( initialOffset );
            if ( decoded == null ) {
                throw new IllegalArgumentException (
                    "Basic Authorization credentials are not Base64" );
            }
            return decoded;
        }
        private void parseCredentials ( byte[] decoded )
        throws IllegalArgumentException {
            int colon = -1;
            for ( int i = 0; i < decoded.length; i++ ) {
                if ( decoded[i] == ':' ) {
                    colon = i;
                    break;
                }
            }
            if ( colon < 0 ) {
                username = new String ( decoded, StandardCharsets.ISO_8859_1 );
            } else {
                username = new String (
                    decoded, 0, colon, StandardCharsets.ISO_8859_1 );
                password = new String (
                    decoded, colon + 1, decoded.length - colon - 1,
                    StandardCharsets.ISO_8859_1 );
                if ( password.length() > 1 ) {
                    password = password.trim();
                }
            }
            if ( username.length() > 1 ) {
                username = username.trim();
            }
        }
    }
}
