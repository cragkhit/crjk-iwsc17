package org.apache.catalina.authenticator;
import org.apache.catalina.Realm;
import org.apache.tomcat.util.http.parser.Authorization;
import java.io.StringReader;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.LifecycleException;
import java.util.LinkedHashMap;
import org.apache.tomcat.util.security.MD5Encoder;
import org.apache.tomcat.util.security.ConcurrentMessageDigest;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Request;
import java.util.Map;
import org.apache.juli.logging.Log;
public class DigestAuthenticator extends AuthenticatorBase {
    private static final Log log;
    protected static final String QOP = "auth";
    protected Map<String, NonceInfo> nonces;
    protected long lastTimestamp;
    protected final Object lastTimestampLock;
    protected int nonceCacheSize;
    protected int nonceCountWindowSize;
    protected String key;
    protected long nonceValidity;
    protected String opaque;
    protected boolean validateUri;
    public DigestAuthenticator() {
        this.lastTimestamp = 0L;
        this.lastTimestampLock = new Object();
        this.nonceCacheSize = 1000;
        this.nonceCountWindowSize = 100;
        this.key = null;
        this.nonceValidity = 300000L;
        this.validateUri = true;
        this.setCache ( false );
    }
    public int getNonceCountWindowSize() {
        return this.nonceCountWindowSize;
    }
    public void setNonceCountWindowSize ( final int nonceCountWindowSize ) {
        this.nonceCountWindowSize = nonceCountWindowSize;
    }
    public int getNonceCacheSize() {
        return this.nonceCacheSize;
    }
    public void setNonceCacheSize ( final int nonceCacheSize ) {
        this.nonceCacheSize = nonceCacheSize;
    }
    public String getKey() {
        return this.key;
    }
    public void setKey ( final String key ) {
        this.key = key;
    }
    public long getNonceValidity() {
        return this.nonceValidity;
    }
    public void setNonceValidity ( final long nonceValidity ) {
        this.nonceValidity = nonceValidity;
    }
    public String getOpaque() {
        return this.opaque;
    }
    public void setOpaque ( final String opaque ) {
        this.opaque = opaque;
    }
    public boolean isValidateUri() {
        return this.validateUri;
    }
    public void setValidateUri ( final boolean validateUri ) {
        this.validateUri = validateUri;
    }
    @Override
    protected boolean doAuthenticate ( final Request request, final HttpServletResponse response ) throws IOException {
        if ( this.checkForCachedAuthentication ( request, response, false ) ) {
            return true;
        }
        Principal principal = null;
        final String authorization = request.getHeader ( "authorization" );
        final DigestInfo digestInfo = new DigestInfo ( this.getOpaque(), this.getNonceValidity(), this.getKey(), this.nonces, this.isValidateUri() );
        if ( authorization != null && digestInfo.parse ( request, authorization ) ) {
            if ( digestInfo.validate ( request ) ) {
                principal = digestInfo.authenticate ( this.context.getRealm() );
            }
            if ( principal != null && !digestInfo.isNonceStale() ) {
                this.register ( request, response, principal, "DIGEST", digestInfo.getUsername(), null );
                return true;
            }
        }
        final String nonce = this.generateNonce ( request );
        this.setAuthenticateHeader ( ( HttpServletRequest ) request, response, nonce, principal != null && digestInfo.isNonceStale() );
        response.sendError ( 401 );
        return false;
    }
    @Override
    protected String getAuthMethod() {
        return "DIGEST";
    }
    protected static String removeQuotes ( final String quotedString, final boolean quotesRequired ) {
        if ( quotedString.length() > 0 && quotedString.charAt ( 0 ) != '\"' && !quotesRequired ) {
            return quotedString;
        }
        if ( quotedString.length() > 2 ) {
            return quotedString.substring ( 1, quotedString.length() - 1 );
        }
        return "";
    }
    protected static String removeQuotes ( final String quotedString ) {
        return removeQuotes ( quotedString, false );
    }
    protected String generateNonce ( final Request request ) {
        long currentTime = System.currentTimeMillis();
        synchronized ( this.lastTimestampLock ) {
            if ( currentTime > this.lastTimestamp ) {
                this.lastTimestamp = currentTime;
            } else {
                final long lastTimestamp = this.lastTimestamp + 1L;
                this.lastTimestamp = lastTimestamp;
                currentTime = lastTimestamp;
            }
        }
        final String ipTimeKey = request.getRemoteAddr() + ":" + currentTime + ":" + this.getKey();
        final byte[] buffer = ConcurrentMessageDigest.digestMD5 ( new byte[][] { ipTimeKey.getBytes ( StandardCharsets.ISO_8859_1 ) } );
        final String nonce = currentTime + ":" + MD5Encoder.encode ( buffer );
        final NonceInfo info = new NonceInfo ( currentTime, this.getNonceCountWindowSize() );
        synchronized ( this.nonces ) {
            this.nonces.put ( nonce, info );
        }
        return nonce;
    }
    protected void setAuthenticateHeader ( final HttpServletRequest request, final HttpServletResponse response, final String nonce, final boolean isNonceStale ) {
        final String realmName = AuthenticatorBase.getRealmName ( this.context );
        String authenticateHeader;
        if ( isNonceStale ) {
            authenticateHeader = "Digest realm=\"" + realmName + "\", qop=\"" + "auth" + "\", nonce=\"" + nonce + "\", opaque=\"" + this.getOpaque() + "\", stale=true";
        } else {
            authenticateHeader = "Digest realm=\"" + realmName + "\", qop=\"" + "auth" + "\", nonce=\"" + nonce + "\", opaque=\"" + this.getOpaque() + "\"";
        }
        response.setHeader ( "WWW-Authenticate", authenticateHeader );
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        super.startInternal();
        if ( this.getKey() == null ) {
            this.setKey ( this.sessionIdGenerator.generateSessionId() );
        }
        if ( this.getOpaque() == null ) {
            this.setOpaque ( this.sessionIdGenerator.generateSessionId() );
        }
        this.nonces = new LinkedHashMap<String, NonceInfo>() {
            private static final long serialVersionUID = 1L;
            private static final long LOG_SUPPRESS_TIME = 300000L;
            private long lastLog = 0L;
            @Override
            protected boolean removeEldestEntry ( final Map.Entry<String, NonceInfo> eldest ) {
                final long currentTime = System.currentTimeMillis();
                if ( this.size() > DigestAuthenticator.this.getNonceCacheSize() ) {
                    if ( this.lastLog < currentTime && currentTime - eldest.getValue().getTimestamp() < DigestAuthenticator.this.getNonceValidity() ) {
                        DigestAuthenticator.log.warn ( AuthenticatorBase.sm.getString ( "digestAuthenticator.cacheRemove" ) );
                        this.lastLog = currentTime + 300000L;
                    }
                    return true;
                }
                return false;
            }
        };
    }
    static {
        log = LogFactory.getLog ( DigestAuthenticator.class );
    }
    public static class DigestInfo {
        private final String opaque;
        private final long nonceValidity;
        private final String key;
        private final Map<String, NonceInfo> nonces;
        private boolean validateUri;
        private String userName;
        private String method;
        private String uri;
        private String response;
        private String nonce;
        private String nc;
        private String cnonce;
        private String realmName;
        private String qop;
        private String opaqueReceived;
        private boolean nonceStale;
        public DigestInfo ( final String opaque, final long nonceValidity, final String key, final Map<String, NonceInfo> nonces, final boolean validateUri ) {
            this.validateUri = true;
            this.userName = null;
            this.method = null;
            this.uri = null;
            this.response = null;
            this.nonce = null;
            this.nc = null;
            this.cnonce = null;
            this.realmName = null;
            this.qop = null;
            this.opaqueReceived = null;
            this.nonceStale = false;
            this.opaque = opaque;
            this.nonceValidity = nonceValidity;
            this.key = key;
            this.nonces = nonces;
            this.validateUri = validateUri;
        }
        public String getUsername() {
            return this.userName;
        }
        public boolean parse ( final Request request, final String authorization ) {
            if ( authorization == null ) {
                return false;
            }
            Map<String, String> directives;
            try {
                directives = Authorization.parseAuthorizationDigest ( new StringReader ( authorization ) );
            } catch ( IOException e ) {
                return false;
            }
            if ( directives == null ) {
                return false;
            }
            this.method = request.getMethod();
            this.userName = directives.get ( "username" );
            this.realmName = directives.get ( "realm" );
            this.nonce = directives.get ( "nonce" );
            this.nc = directives.get ( "nc" );
            this.cnonce = directives.get ( "cnonce" );
            this.qop = directives.get ( "qop" );
            this.uri = directives.get ( "uri" );
            this.response = directives.get ( "response" );
            this.opaqueReceived = directives.get ( "opaque" );
            return true;
        }
        public boolean validate ( final Request request ) {
            if ( this.userName == null || this.realmName == null || this.nonce == null || this.uri == null || this.response == null ) {
                return false;
            }
            if ( this.validateUri ) {
                final String query = request.getQueryString();
                String uriQuery;
                if ( query == null ) {
                    uriQuery = request.getRequestURI();
                } else {
                    uriQuery = request.getRequestURI() + "?" + query;
                }
                if ( !this.uri.equals ( uriQuery ) ) {
                    final String host = request.getHeader ( "host" );
                    final String scheme = request.getScheme();
                    if ( host == null || uriQuery.startsWith ( scheme ) ) {
                        return false;
                    }
                    final StringBuilder absolute = new StringBuilder();
                    absolute.append ( scheme );
                    absolute.append ( "://" );
                    absolute.append ( host );
                    absolute.append ( uriQuery );
                    if ( !this.uri.equals ( absolute.toString() ) ) {
                        return false;
                    }
                }
            }
            final String lcRealm = AuthenticatorBase.getRealmName ( request.getContext() );
            if ( !lcRealm.equals ( this.realmName ) ) {
                return false;
            }
            if ( !this.opaque.equals ( this.opaqueReceived ) ) {
                return false;
            }
            final int i = this.nonce.indexOf ( 58 );
            if ( i < 0 || i + 1 == this.nonce.length() ) {
                return false;
            }
            long nonceTime;
            try {
                nonceTime = Long.parseLong ( this.nonce.substring ( 0, i ) );
            } catch ( NumberFormatException nfe ) {
                return false;
            }
            final String md5clientIpTimeKey = this.nonce.substring ( i + 1 );
            final long currentTime = System.currentTimeMillis();
            if ( currentTime - nonceTime > this.nonceValidity ) {
                this.nonceStale = true;
                synchronized ( this.nonces ) {
                    this.nonces.remove ( this.nonce );
                }
            }
            final String serverIpTimeKey = request.getRemoteAddr() + ":" + nonceTime + ":" + this.key;
            final byte[] buffer = ConcurrentMessageDigest.digestMD5 ( new byte[][] { serverIpTimeKey.getBytes ( StandardCharsets.ISO_8859_1 ) } );
            final String md5ServerIpTimeKey = MD5Encoder.encode ( buffer );
            if ( !md5ServerIpTimeKey.equals ( md5clientIpTimeKey ) ) {
                return false;
            }
            if ( this.qop != null && !"auth".equals ( this.qop ) ) {
                return false;
            }
            if ( this.qop == null ) {
                if ( this.cnonce != null || this.nc != null ) {
                    return false;
                }
            } else {
                if ( this.cnonce == null || this.nc == null ) {
                    return false;
                }
                if ( this.nc.length() < 6 || this.nc.length() > 8 ) {
                    return false;
                }
                long count;
                try {
                    count = Long.parseLong ( this.nc, 16 );
                } catch ( NumberFormatException nfe2 ) {
                    return false;
                }
                final NonceInfo info;
                synchronized ( this.nonces ) {
                    info = this.nonces.get ( this.nonce );
                }
                if ( info == null ) {
                    this.nonceStale = true;
                } else if ( !info.nonceCountValid ( count ) ) {
                    return false;
                }
            }
            return true;
        }
        public boolean isNonceStale() {
            return this.nonceStale;
        }
        public Principal authenticate ( final Realm realm ) {
            final String a2 = this.method + ":" + this.uri;
            final byte[] buffer = ConcurrentMessageDigest.digestMD5 ( new byte[][] { a2.getBytes ( StandardCharsets.ISO_8859_1 ) } );
            final String md5a2 = MD5Encoder.encode ( buffer );
            return realm.authenticate ( this.userName, this.response, this.nonce, this.nc, this.cnonce, this.qop, this.realmName, md5a2 );
        }
    }
    public static class NonceInfo {
        private final long timestamp;
        private final boolean[] seen;
        private final int offset;
        private int count;
        public NonceInfo ( final long currentTime, final int seenWindowSize ) {
            this.count = 0;
            this.timestamp = currentTime;
            this.seen = new boolean[seenWindowSize];
            this.offset = seenWindowSize / 2;
        }
        public synchronized boolean nonceCountValid ( final long nonceCount ) {
            if ( this.count - this.offset >= nonceCount || nonceCount > this.count - this.offset + this.seen.length ) {
                return false;
            }
            final int checkIndex = ( int ) ( ( nonceCount + this.offset ) % this.seen.length );
            if ( this.seen[checkIndex] ) {
                return false;
            }
            this.seen[checkIndex] = true;
            this.seen[this.count % this.seen.length] = false;
            ++this.count;
            return true;
        }
        public long getTimestamp() {
            return this.timestamp;
        }
    }
}
