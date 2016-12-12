package org.apache.tomcat.util.net.jsse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLSession;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.SSLSessionManager;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.openssl.ciphers.Cipher;
import org.apache.tomcat.util.res.StringManager;
public class JSSESupport implements SSLSupport, SSLSessionManager {
    private static final Log log = LogFactory.getLog ( JSSESupport.class );
    private static final StringManager sm = StringManager.getManager ( JSSESupport.class );
    private static final Map<String, Integer> keySizeCache = new HashMap<>();
    static {
        for ( Cipher cipher : Cipher.values() ) {
            for ( String jsseName : cipher.getJsseNames() ) {
                keySizeCache.put ( jsseName, Integer.valueOf ( cipher.getStrength_bits() ) );
            }
        }
    }
    static void init() {
    }
    private SSLSession session;
    public JSSESupport ( SSLSession session ) {
        this.session = session;
    }
    @Override
    public String getCipherSuite() throws IOException {
        if ( session == null ) {
            return null;
        }
        return session.getCipherSuite();
    }
    @Override
    public java.security.cert.X509Certificate[] getPeerCertificateChain() throws IOException {
        if ( session == null ) {
            return null;
        }
        Certificate [] certs = null;
        try {
            certs = session.getPeerCertificates();
        } catch ( Throwable t ) {
            log.debug ( sm.getString ( "jsseSupport.clientCertError" ), t );
            return null;
        }
        if ( certs == null ) {
            return null;
        }
        java.security.cert.X509Certificate [] x509Certs =
            new java.security.cert.X509Certificate[certs.length];
        for ( int i = 0; i < certs.length; i++ ) {
            if ( certs[i] instanceof java.security.cert.X509Certificate ) {
                x509Certs[i] = ( java.security.cert.X509Certificate ) certs[i];
            } else {
                try {
                    byte [] buffer = certs[i].getEncoded();
                    CertificateFactory cf =
                        CertificateFactory.getInstance ( "X.509" );
                    ByteArrayInputStream stream =
                        new ByteArrayInputStream ( buffer );
                    x509Certs[i] = ( java.security.cert.X509Certificate )
                                   cf.generateCertificate ( stream );
                } catch ( Exception ex ) {
                    log.info ( sm.getString (
                                   "jseeSupport.certTranslationError", certs[i] ), ex );
                    return null;
                }
            }
            if ( log.isTraceEnabled() ) {
                log.trace ( "Cert #" + i + " = " + x509Certs[i] );
            }
        }
        if ( x509Certs.length < 1 ) {
            return null;
        }
        return x509Certs;
    }
    @Override
    public Integer getKeySize() throws IOException {
        if ( session == null ) {
            return null;
        }
        return keySizeCache.get ( session.getCipherSuite() );
    }
    @Override
    public String getSessionId()
    throws IOException {
        if ( session == null ) {
            return null;
        }
        byte [] ssl_session = session.getId();
        if ( ssl_session == null ) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        for ( int x = 0; x < ssl_session.length; x++ ) {
            String digit = Integer.toHexString ( ssl_session[x] );
            if ( digit.length() < 2 ) {
                buf.append ( '0' );
            }
            if ( digit.length() > 2 ) {
                digit = digit.substring ( digit.length() - 2 );
            }
            buf.append ( digit );
        }
        return buf.toString();
    }
    public void setSession ( SSLSession session ) {
        this.session = session;
    }
    @Override
    public void invalidateSession() {
        session.invalidate();
    }
    @Override
    public String getProtocol() throws IOException {
        if ( session == null ) {
            return null;
        }
        return session.getProtocol();
    }
}
