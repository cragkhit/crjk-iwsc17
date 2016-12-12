package org.apache.catalina.authenticator;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Realm;
import org.apache.catalina.connector.Request;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.http.parser.Authorization;
import org.apache.tomcat.util.security.ConcurrentMessageDigest;
import org.apache.tomcat.util.security.MD5Encoder;
public class DigestAuthenticator extends AuthenticatorBase {
    private static final Log log = LogFactory.getLog ( DigestAuthenticator.class );
    protected static final String QOP = "auth";
    public DigestAuthenticator() {
        super();
        setCache ( false );
    }
    protected Map<String, NonceInfo> nonces;
    protected long lastTimestamp = 0;
    protected final Object lastTimestampLock = new Object();
    protected int nonceCacheSize = 1000;
    protected int nonceCountWindowSize = 100;
    protected String key = null;
    protected long nonceValidity = 5 * 60 * 1000;
    protected String opaque;
    protected boolean validateUri = true;
    public int getNonceCountWindowSize() {
        return nonceCountWindowSize;
    }
    public void setNonceCountWindowSize ( int nonceCountWindowSize ) {
        this.nonceCountWindowSize = nonceCountWindowSize;
    }
    public int getNonceCacheSize() {
        return nonceCacheSize;
    }
    public void setNonceCacheSize ( int nonceCacheSize ) {
        this.nonceCacheSize = nonceCacheSize;
    }
    public String getKey() {
        return key;
    }
    public void setKey ( String key ) {
        this.key = key;
    }
    public long getNonceValidity() {
        return nonceValidity;
    }
    public void setNonceValidity ( long nonceValidity ) {
        this.nonceValidity = nonceValidity;
    }
    public String getOpaque() {
        return opaque;
    }
    public void setOpaque ( String opaque ) {
        this.opaque = opaque;
    }
    public boolean isValidateUri() {
        return validateUri;
    }
    public void setValidateUri ( boolean validateUri ) {
        this.validateUri = validateUri;
    }
    @Override
    protected boolean doAuthenticate ( Request request, HttpServletResponse response )
    throws IOException {
        if ( checkForCachedAuthentication ( request, response, false ) ) {
            return true;
        }
        Principal principal = null;
        String authorization = request.getHeader ( "authorization" );
        DigestInfo digestInfo = new DigestInfo ( getOpaque(), getNonceValidity(),
                getKey(), nonces, isValidateUri() );
        if ( authorization != null ) {
            if ( digestInfo.parse ( request, authorization ) ) {
                if ( digestInfo.validate ( request ) ) {
                    principal = digestInfo.authenticate ( context.getRealm() );
                }
                if ( principal != null && !digestInfo.isNonceStale() ) {
                    register ( request, response, principal,
                               HttpServletRequest.DIGEST_AUTH,
                               digestInfo.getUsername(), null );
                    return true;
                }
            }
        }
        String nonce = generateNonce ( request );
        setAuthenticateHeader ( request, response, nonce,
                                principal != null && digestInfo.isNonceStale() );
        response.sendError ( HttpServletResponse.SC_UNAUTHORIZED );
        return false;
    }
    @Override
    protected String getAuthMethod() {
        return HttpServletRequest.DIGEST_AUTH;
    }
    protected static String removeQuotes ( String quotedString,
                                           boolean quotesRequired ) {
        if ( quotedString.length() > 0 && quotedString.charAt ( 0 ) != '"' &&
                !quotesRequired ) {
            return quotedString;
        } else if ( quotedString.length() > 2 ) {
            return quotedString.substring ( 1, quotedString.length() - 1 );
        } else {
            return "";
        }
    }
    protected static String removeQuotes ( String quotedString ) {
        return removeQuotes ( quotedString, false );
    }
    protected String generateNonce ( Request request ) {
        long currentTime = System.currentTimeMillis();
        synchronized ( lastTimestampLock ) {
            if ( currentTime > lastTimestamp ) {
                lastTimestamp = currentTime;
            } else {
                currentTime = ++lastTimestamp;
            }
        }
        String ipTimeKey =
            request.getRemoteAddr() + ":" + currentTime + ":" + getKey();
        byte[] buffer = ConcurrentMessageDigest.digestMD5 (
                            ipTimeKey.getBytes ( StandardCharsets.ISO_8859_1 ) );
        String nonce = currentTime + ":" + MD5Encoder.encode ( buffer );
        NonceInfo info = new NonceInfo ( currentTime, getNonceCountWindowSize() );
        synchronized ( nonces ) {
            nonces.put ( nonce, info );
        }
        return nonce;
    }
    protected void setAuthenticateHeader ( HttpServletRequest request,
                                           HttpServletResponse response,
                                           String nonce,
                                           boolean isNonceStale ) {
        String realmName = getRealmName ( context );
        String authenticateHeader;
        if ( isNonceStale ) {
            authenticateHeader = "Digest realm=\"" + realmName + "\", " +
                                 "qop=\"" + QOP + "\", nonce=\"" + nonce + "\", " + "opaque=\"" +
                                 getOpaque() + "\", stale=true";
        } else {
            authenticateHeader = "Digest realm=\"" + realmName + "\", " +
                                 "qop=\"" + QOP + "\", nonce=\"" + nonce + "\", " + "opaque=\"" +
                                 getOpaque() + "\"";
        }
        response.setHeader ( AUTH_HEADER_NAME, authenticateHeader );
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        super.startInternal();
        if ( getKey() == null ) {
            setKey ( sessionIdGenerator.generateSessionId() );
        }
        if ( getOpaque() == null ) {
            setOpaque ( sessionIdGenerator.generateSessionId() );
        }
        nonces = new LinkedHashMap<String, DigestAuthenticator.NonceInfo>() {
            private static final long serialVersionUID = 1L;
            private static final long LOG_SUPPRESS_TIME = 5 * 60 * 1000;
            private long lastLog = 0;
            @Override
            protected boolean removeEldestEntry (
                Map.Entry<String, NonceInfo> eldest ) {
                long currentTime = System.currentTimeMillis();
                if ( size() > getNonceCacheSize() ) {
                    if ( lastLog < currentTime &&
                            currentTime - eldest.getValue().getTimestamp() <
                            getNonceValidity() ) {
                        log.warn ( sm.getString (
                                       "digestAuthenticator.cacheRemove" ) );
                        lastLog = currentTime + LOG_SUPPRESS_TIME;
                    }
                    return true;
                }
                return false;
            }
        };
    }
    public static class DigestInfo {
        private final String opaque;
        private final long nonceValidity;
        private final String key;
        private final Map<String, NonceInfo> nonces;
        private boolean validateUri = true;
        private String userName = null;
        private String method = null;
        private String uri = null;
        private String response = null;
        private String nonce = null;
        private String nc = null;
        private String cnonce = null;
        private String realmName = null;
        private String qop = null;
        private String opaqueReceived = null;
        private boolean nonceStale = false;
        public DigestInfo ( String opaque, long nonceValidity, String key,
                            Map<String, NonceInfo> nonces, boolean validateUri ) {
            this.opaque = opaque;
            this.nonceValidity = nonceValidity;
            this.key = key;
            this.nonces = nonces;
            this.validateUri = validateUri;
        }
        public String getUsername() {
            return userName;
        }
        public boolean parse ( Request request, String authorization ) {
            if ( authorization == null ) {
                return false;
            }
            Map<String, String> directives;
            try {
                directives = Authorization.parseAuthorizationDigest (
                                 new StringReader ( authorization ) );
            } catch ( IOException e ) {
                return false;
            }
            if ( directives == null ) {
                return false;
            }
            method = request.getMethod();
            userName = directives.get ( "username" );
            realmName = directives.get ( "realm" );
            nonce = directives.get ( "nonce" );
            nc = directives.get ( "nc" );
            cnonce = directives.get ( "cnonce" );
            qop = directives.get ( "qop" );
            uri = directives.get ( "uri" );
            response = directives.get ( "response" );
            opaqueReceived = directives.get ( "opaque" );
            return true;
        }
        public boolean validate ( Request request ) {
            if ( ( userName == null ) || ( realmName == null ) || ( nonce == null )
                    || ( uri == null ) || ( response == null ) ) {
                return false;
            }
            if ( validateUri ) {
                String uriQuery;
                String query = request.getQueryString();
                if ( query == null ) {
                    uriQuery = request.getRequestURI();
                } else {
                    uriQuery = request.getRequestURI() + "?" + query;
                }
                if ( !uri.equals ( uriQuery ) ) {
                    String host = request.getHeader ( "host" );
                    String scheme = request.getScheme();
                    if ( host != null && !uriQuery.startsWith ( scheme ) ) {
                        StringBuilder absolute = new StringBuilder();
                        absolute.append ( scheme );
                        absolute.append ( "://" );
                        absolute.append ( host );
                        absolute.append ( uriQuery );
                        if ( !uri.equals ( absolute.toString() ) ) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
            String lcRealm = getRealmName ( request.getContext() );
            if ( !lcRealm.equals ( realmName ) ) {
                return false;
            }
            if ( !opaque.equals ( opaqueReceived ) ) {
                return false;
            }
            int i = nonce.indexOf ( ':' );
            if ( i < 0 || ( i + 1 ) == nonce.length() ) {
                return false;
            }
            long nonceTime;
            try {
                nonceTime = Long.parseLong ( nonce.substring ( 0, i ) );
            } catch ( NumberFormatException nfe ) {
                return false;
            }
            String md5clientIpTimeKey = nonce.substring ( i + 1 );
            long currentTime = System.currentTimeMillis();
            if ( ( currentTime - nonceTime ) > nonceValidity ) {
                nonceStale = true;
                synchronized ( nonces ) {
                    nonces.remove ( nonce );
                }
            }
            String serverIpTimeKey =
                request.getRemoteAddr() + ":" + nonceTime + ":" + key;
            byte[] buffer = ConcurrentMessageDigest.digestMD5 (
                                serverIpTimeKey.getBytes ( StandardCharsets.ISO_8859_1 ) );
            String md5ServerIpTimeKey = MD5Encoder.encode ( buffer );
            if ( !md5ServerIpTimeKey.equals ( md5clientIpTimeKey ) ) {
                return false;
            }
            if ( qop != null && !QOP.equals ( qop ) ) {
                return false;
            }
            if ( qop == null ) {
                if ( cnonce != null || nc != null ) {
                    return false;
                }
            } else {
                if ( cnonce == null || nc == null ) {
                    return false;
                }
                if ( nc.length() < 6 || nc.length() > 8 ) {
                    return false;
                }
                long count;
                try {
                    count = Long.parseLong ( nc, 16 );
                } catch ( NumberFormatException nfe ) {
                    return false;
                }
                NonceInfo info;
                synchronized ( nonces ) {
                    info = nonces.get ( nonce );
                }
                if ( info == null ) {
                    nonceStale = true;
                } else {
                    if ( !info.nonceCountValid ( count ) ) {
                        return false;
                    }
                }
            }
            return true;
        }
        public boolean isNonceStale() {
            return nonceStale;
        }
        public Principal authenticate ( Realm realm ) {
            String a2 = method + ":" + uri;
            byte[] buffer = ConcurrentMessageDigest.digestMD5 (
                                a2.getBytes ( StandardCharsets.ISO_8859_1 ) );
            String md5a2 = MD5Encoder.encode ( buffer );
            return realm.authenticate ( userName, response, nonce, nc, cnonce,
                                        qop, realmName, md5a2 );
        }
    }
    public static class NonceInfo {
        private final long timestamp;
        private final boolean seen[];
        private final int offset;
        private int count = 0;
        public NonceInfo ( long currentTime, int seenWindowSize ) {
            this.timestamp = currentTime;
            seen = new boolean[seenWindowSize];
            offset = seenWindowSize / 2;
        }
        public synchronized boolean nonceCountValid ( long nonceCount ) {
            if ( ( count - offset ) >= nonceCount ||
                    ( nonceCount > count - offset + seen.length ) ) {
                return false;
            }
            int checkIndex = ( int ) ( ( nonceCount + offset ) % seen.length );
            if ( seen[checkIndex] ) {
                return false;
            } else {
                seen[checkIndex] = true;
                seen[count % seen.length] = false;
                count++;
                return true;
            }
        }
        public long getTimestamp() {
            return timestamp;
        }
    }
}
