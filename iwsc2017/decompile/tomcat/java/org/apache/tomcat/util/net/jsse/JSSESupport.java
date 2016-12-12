package org.apache.tomcat.util.net.jsse;
import java.util.Iterator;
import org.apache.tomcat.util.net.openssl.ciphers.Cipher;
import java.util.HashMap;
import org.apache.juli.logging.LogFactory;
import java.security.cert.Certificate;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.io.IOException;
import javax.net.ssl.SSLSession;
import java.util.Map;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.net.SSLSessionManager;
import org.apache.tomcat.util.net.SSLSupport;
public class JSSESupport implements SSLSupport, SSLSessionManager {
    private static final Log log;
    private static final StringManager sm;
    private static final Map<String, Integer> keySizeCache;
    private SSLSession session;
    static void init() {
    }
    public JSSESupport ( final SSLSession session ) {
        this.session = session;
    }
    @Override
    public String getCipherSuite() throws IOException {
        if ( this.session == null ) {
            return null;
        }
        return this.session.getCipherSuite();
    }
    @Override
    public X509Certificate[] getPeerCertificateChain() throws IOException {
        if ( this.session == null ) {
            return null;
        }
        Certificate[] certs = null;
        try {
            certs = this.session.getPeerCertificates();
        } catch ( Throwable t ) {
            JSSESupport.log.debug ( JSSESupport.sm.getString ( "jsseSupport.clientCertError" ), t );
            return null;
        }
        if ( certs == null ) {
            return null;
        }
        final X509Certificate[] x509Certs = new X509Certificate[certs.length];
        for ( int i = 0; i < certs.length; ++i ) {
            if ( certs[i] instanceof X509Certificate ) {
                x509Certs[i] = ( X509Certificate ) certs[i];
            } else {
                try {
                    final byte[] buffer = certs[i].getEncoded();
                    final CertificateFactory cf = CertificateFactory.getInstance ( "X.509" );
                    final ByteArrayInputStream stream = new ByteArrayInputStream ( buffer );
                    x509Certs[i] = ( X509Certificate ) cf.generateCertificate ( stream );
                } catch ( Exception ex ) {
                    JSSESupport.log.info ( JSSESupport.sm.getString ( "jseeSupport.certTranslationError", certs[i] ), ex );
                    return null;
                }
            }
            if ( JSSESupport.log.isTraceEnabled() ) {
                JSSESupport.log.trace ( "Cert #" + i + " = " + x509Certs[i] );
            }
        }
        if ( x509Certs.length < 1 ) {
            return null;
        }
        return x509Certs;
    }
    @Override
    public Integer getKeySize() throws IOException {
        if ( this.session == null ) {
            return null;
        }
        return JSSESupport.keySizeCache.get ( this.session.getCipherSuite() );
    }
    @Override
    public String getSessionId() throws IOException {
        if ( this.session == null ) {
            return null;
        }
        final byte[] ssl_session = this.session.getId();
        if ( ssl_session == null ) {
            return null;
        }
        final StringBuilder buf = new StringBuilder();
        for ( int x = 0; x < ssl_session.length; ++x ) {
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
    public void setSession ( final SSLSession session ) {
        this.session = session;
    }
    @Override
    public void invalidateSession() {
        this.session.invalidate();
    }
    @Override
    public String getProtocol() throws IOException {
        if ( this.session == null ) {
            return null;
        }
        return this.session.getProtocol();
    }
    static {
        log = LogFactory.getLog ( JSSESupport.class );
        sm = StringManager.getManager ( JSSESupport.class );
        keySizeCache = new HashMap<String, Integer>();
        for ( final Cipher cipher : Cipher.values() ) {
            for ( final String jsseName : cipher.getJsseNames() ) {
                JSSESupport.keySizeCache.put ( jsseName, cipher.getStrength_bits() );
            }
        }
    }
}
