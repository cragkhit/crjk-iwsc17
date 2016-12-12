package org.apache.catalina.authenticator;
import java.security.Principal;
import org.apache.catalina.Realm;
import org.apache.tomcat.util.security.MD5Encoder;
import org.apache.tomcat.util.security.ConcurrentMessageDigest;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import org.apache.tomcat.util.http.parser.Authorization;
import java.io.StringReader;
import org.apache.catalina.connector.Request;
import java.util.Map;
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
