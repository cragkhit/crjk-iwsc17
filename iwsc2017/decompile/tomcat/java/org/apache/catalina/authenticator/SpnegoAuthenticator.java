package org.apache.catalina.authenticator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.apache.catalina.Realm;
import org.apache.juli.logging.LogFactory;
import java.io.IOException;
import org.ietf.jgss.GSSContext;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import java.security.PrivilegedActionException;
import java.security.PrivilegedAction;
import java.security.Principal;
import javax.security.auth.Subject;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.ietf.jgss.GSSCredential;
import java.security.PrivilegedExceptionAction;
import org.apache.tomcat.util.compat.JreVendor;
import org.ietf.jgss.GSSManager;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.LoginContext;
import org.apache.tomcat.util.codec.binary.Base64;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Request;
import org.apache.catalina.LifecycleException;
import java.io.File;
import java.util.regex.Pattern;
import org.apache.juli.logging.Log;
public class SpnegoAuthenticator extends AuthenticatorBase {
    private static final Log log;
    private String loginConfigName;
    private boolean storeDelegatedCredential;
    private Pattern noKeepAliveUserAgents;
    private boolean applyJava8u40Fix;
    public SpnegoAuthenticator() {
        this.loginConfigName = "com.sun.security.jgss.krb5.accept";
        this.storeDelegatedCredential = true;
        this.noKeepAliveUserAgents = null;
        this.applyJava8u40Fix = true;
    }
    public String getLoginConfigName() {
        return this.loginConfigName;
    }
    public void setLoginConfigName ( final String loginConfigName ) {
        this.loginConfigName = loginConfigName;
    }
    public boolean isStoreDelegatedCredential() {
        return this.storeDelegatedCredential;
    }
    public void setStoreDelegatedCredential ( final boolean storeDelegatedCredential ) {
        this.storeDelegatedCredential = storeDelegatedCredential;
    }
    public String getNoKeepAliveUserAgents() {
        final Pattern p = this.noKeepAliveUserAgents;
        if ( p == null ) {
            return null;
        }
        return p.pattern();
    }
    public void setNoKeepAliveUserAgents ( final String noKeepAliveUserAgents ) {
        if ( noKeepAliveUserAgents == null || noKeepAliveUserAgents.length() == 0 ) {
            this.noKeepAliveUserAgents = null;
        } else {
            this.noKeepAliveUserAgents = Pattern.compile ( noKeepAliveUserAgents );
        }
    }
    public boolean getApplyJava8u40Fix() {
        return this.applyJava8u40Fix;
    }
    public void setApplyJava8u40Fix ( final boolean applyJava8u40Fix ) {
        this.applyJava8u40Fix = applyJava8u40Fix;
    }
    @Override
    protected String getAuthMethod() {
        return "SPNEGO";
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        final String krb5Conf = System.getProperty ( "java.security.krb5.conf" );
        if ( krb5Conf == null ) {
            final File krb5ConfFile = new File ( this.container.getCatalinaBase(), "conf/krb5.ini" );
            System.setProperty ( "java.security.krb5.conf", krb5ConfFile.getAbsolutePath() );
        }
        final String jaasConf = System.getProperty ( "java.security.auth.login.config" );
        if ( jaasConf == null ) {
            final File jaasConfFile = new File ( this.container.getCatalinaBase(), "conf/jaas.conf" );
            System.setProperty ( "java.security.auth.login.config", jaasConfFile.getAbsolutePath() );
        }
    }
    @Override
    protected boolean doAuthenticate ( final Request request, final HttpServletResponse response ) throws IOException {
        if ( this.checkForCachedAuthentication ( request, response, true ) ) {
            return true;
        }
        final MessageBytes authorization = request.getCoyoteRequest().getMimeHeaders().getValue ( "authorization" );
        if ( authorization == null ) {
            if ( SpnegoAuthenticator.log.isDebugEnabled() ) {
                SpnegoAuthenticator.log.debug ( SpnegoAuthenticator.sm.getString ( "authenticator.noAuthHeader" ) );
            }
            response.setHeader ( "WWW-Authenticate", "Negotiate" );
            response.sendError ( 401 );
            return false;
        }
        authorization.toBytes();
        final ByteChunk authorizationBC = authorization.getByteChunk();
        if ( !authorizationBC.startsWithIgnoreCase ( "negotiate ", 0 ) ) {
            if ( SpnegoAuthenticator.log.isDebugEnabled() ) {
                SpnegoAuthenticator.log.debug ( SpnegoAuthenticator.sm.getString ( "spnegoAuthenticator.authHeaderNotNego" ) );
            }
            response.setHeader ( "WWW-Authenticate", "Negotiate" );
            response.sendError ( 401 );
            return false;
        }
        authorizationBC.setOffset ( authorizationBC.getOffset() + 10 );
        final byte[] decoded = Base64.decodeBase64 ( authorizationBC.getBuffer(), authorizationBC.getOffset(), authorizationBC.getLength() );
        if ( this.getApplyJava8u40Fix() ) {
            SpnegoTokenFixer.fix ( decoded );
        }
        if ( decoded.length == 0 ) {
            if ( SpnegoAuthenticator.log.isDebugEnabled() ) {
                SpnegoAuthenticator.log.debug ( SpnegoAuthenticator.sm.getString ( "spnegoAuthenticator.authHeaderNoToken" ) );
            }
            response.setHeader ( "WWW-Authenticate", "Negotiate" );
            response.sendError ( 401 );
            return false;
        }
        LoginContext lc = null;
        GSSContext gssContext = null;
        byte[] outToken = null;
        Principal principal = null;
        try {
            try {
                lc = new LoginContext ( this.getLoginConfigName() );
                lc.login();
            } catch ( LoginException e ) {
                SpnegoAuthenticator.log.error ( SpnegoAuthenticator.sm.getString ( "spnegoAuthenticator.serviceLoginFail" ), e );
                response.sendError ( 500 );
                return false;
            }
            final Subject subject = lc.getSubject();
            final GSSManager manager = GSSManager.getInstance();
            int credentialLifetime;
            if ( JreVendor.IS_IBM_JVM ) {
                credentialLifetime = Integer.MAX_VALUE;
            } else {
                credentialLifetime = 0;
            }
            final PrivilegedExceptionAction<GSSCredential> action = new PrivilegedExceptionAction<GSSCredential>() {
                @Override
                public GSSCredential run() throws GSSException {
                    return manager.createCredential ( null, credentialLifetime, new Oid ( "1.3.6.1.5.5.2" ), 2 );
                }
            };
            gssContext = manager.createContext ( Subject.doAs ( subject, action ) );
            outToken = Subject.doAs ( lc.getSubject(), ( PrivilegedExceptionAction<byte[]> ) new AcceptAction ( gssContext, decoded ) );
            if ( outToken == null ) {
                if ( SpnegoAuthenticator.log.isDebugEnabled() ) {
                    SpnegoAuthenticator.log.debug ( SpnegoAuthenticator.sm.getString ( "spnegoAuthenticator.ticketValidateFail" ) );
                }
                response.setHeader ( "WWW-Authenticate", "Negotiate" );
                response.sendError ( 401 );
                return false;
            }
            principal = Subject.doAs ( subject, ( PrivilegedAction<Principal> ) new AuthenticateAction ( this.context.getRealm(), gssContext, this.storeDelegatedCredential ) );
        } catch ( GSSException e2 ) {
            if ( SpnegoAuthenticator.log.isDebugEnabled() ) {
                SpnegoAuthenticator.log.debug ( SpnegoAuthenticator.sm.getString ( "spnegoAuthenticator.ticketValidateFail" ), e2 );
            }
            response.setHeader ( "WWW-Authenticate", "Negotiate" );
            response.sendError ( 401 );
            return false;
        } catch ( PrivilegedActionException e3 ) {
            final Throwable cause = e3.getCause();
            if ( cause instanceof GSSException ) {
                if ( SpnegoAuthenticator.log.isDebugEnabled() ) {
                    SpnegoAuthenticator.log.debug ( SpnegoAuthenticator.sm.getString ( "spnegoAuthenticator.serviceLoginFail" ), e3 );
                }
            } else {
                SpnegoAuthenticator.log.error ( SpnegoAuthenticator.sm.getString ( "spnegoAuthenticator.serviceLoginFail" ), e3 );
            }
            response.setHeader ( "WWW-Authenticate", "Negotiate" );
            response.sendError ( 401 );
            return false;
        } finally {
            if ( gssContext != null ) {
                try {
                    gssContext.dispose();
                } catch ( GSSException ex ) {}
            }
            if ( lc != null ) {
                try {
                    lc.logout();
                } catch ( LoginException ex2 ) {}
            }
        }
        response.setHeader ( "WWW-Authenticate", "Negotiate " + Base64.encodeBase64String ( outToken ) );
        if ( principal != null ) {
            this.register ( request, response, principal, "SPNEGO", principal.getName(), null );
            final Pattern p = this.noKeepAliveUserAgents;
            if ( p != null ) {
                final MessageBytes ua = request.getCoyoteRequest().getMimeHeaders().getValue ( "user-agent" );
                if ( ua != null && p.matcher ( ua.toString() ).matches() ) {
                    response.setHeader ( "Connection", "close" );
                }
            }
            return true;
        }
        response.sendError ( 401 );
        return false;
    }
    static {
        log = LogFactory.getLog ( SpnegoAuthenticator.class );
    }
    public static class AcceptAction implements PrivilegedExceptionAction<byte[]> {
        GSSContext gssContext;
        byte[] decoded;
        public AcceptAction ( final GSSContext context, final byte[] decodedToken ) {
            this.gssContext = context;
            this.decoded = decodedToken;
        }
        @Override
        public byte[] run() throws GSSException {
            return this.gssContext.acceptSecContext ( this.decoded, 0, this.decoded.length );
        }
    }
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
    public static class SpnegoTokenFixer {
        private final byte[] token;
        private int pos;
        public static void fix ( final byte[] token ) {
            final SpnegoTokenFixer fixer = new SpnegoTokenFixer ( token );
            fixer.fix();
        }
        private SpnegoTokenFixer ( final byte[] token ) {
            this.pos = 0;
            this.token = token;
        }
        private void fix() {
            if ( !this.tag ( 96 ) ) {
                return;
            }
            if ( !this.length() ) {
                return;
            }
            if ( !this.oid ( "1.3.6.1.5.5.2" ) ) {
                return;
            }
            if ( !this.tag ( 160 ) ) {
                return;
            }
            if ( !this.length() ) {
                return;
            }
            if ( !this.tag ( 48 ) ) {
                return;
            }
            if ( !this.length() ) {
                return;
            }
            if ( !this.tag ( 160 ) ) {
                return;
            }
            this.lengthAsInt();
            if ( !this.tag ( 48 ) ) {
                return;
            }
            final int mechTypesLen = this.lengthAsInt();
            final int mechTypesStart = this.pos;
            final LinkedHashMap<String, int[]> mechTypeEntries = new LinkedHashMap<String, int[]>();
            while ( this.pos < mechTypesStart + mechTypesLen ) {
                final int[] value = { this.pos, 0 };
                final String key = this.oidAsString();
                value[1] = this.pos - value[0];
                mechTypeEntries.put ( key, value );
            }
            final byte[] replacement = new byte[mechTypesLen];
            int replacementPos = 0;
            final int[] first = mechTypeEntries.remove ( "1.2.840.113554.1.2.2" );
            if ( first != null ) {
                System.arraycopy ( this.token, first[0], replacement, replacementPos, first[1] );
                replacementPos += first[1];
            }
            for ( final int[] markers : mechTypeEntries.values() ) {
                System.arraycopy ( this.token, markers[0], replacement, replacementPos, markers[1] );
                replacementPos += markers[1];
            }
            System.arraycopy ( replacement, 0, this.token, mechTypesStart, mechTypesLen );
        }
        private boolean tag ( final int expected ) {
            return ( this.token[this.pos++] & 0xFF ) == expected;
        }
        private boolean length() {
            final int len = this.lengthAsInt();
            return this.pos + len == this.token.length;
        }
        private int lengthAsInt() {
            int len = this.token[this.pos++] & 0xFF;
            if ( len > 127 ) {
                final int bytes = len - 128;
                len = 0;
                for ( int i = 0; i < bytes; ++i ) {
                    len <<= 8;
                    len += ( this.token[this.pos++] & 0xFF );
                }
            }
            return len;
        }
        private boolean oid ( final String expected ) {
            return expected.equals ( this.oidAsString() );
        }
        private String oidAsString() {
            if ( !this.tag ( 6 ) ) {
                return null;
            }
            final StringBuilder result = new StringBuilder();
            final int len = this.lengthAsInt();
            final int v = this.token[this.pos++] & 0xFF;
            int c2 = v % 40;
            c2 = ( v - c2 ) / 40;
            result.append ( c2 );
            result.append ( '.' );
            result.append ( c2 );
            int c3 = 0;
            boolean write = false;
            for ( int i = 1; i < len; ++i ) {
                int b = this.token[this.pos++] & 0xFF;
                if ( b > 127 ) {
                    b -= 128;
                } else {
                    write = true;
                }
                c3 <<= 7;
                c3 += b;
                if ( write ) {
                    result.append ( '.' );
                    result.append ( c3 );
                    c3 = 0;
                    write = false;
                }
            }
            return result.toString();
        }
    }
}
