package org.apache.catalina.valves;
import org.apache.juli.logging.LogFactory;
import javax.servlet.ServletException;
import java.io.IOException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import org.apache.juli.logging.Log;
public class SSLValve extends ValveBase {
    private static final Log log;
    private String sslClientCertHeader;
    private String sslCipherHeader;
    private String sslSessionIdHeader;
    private String sslCipherUserKeySizeHeader;
    public SSLValve() {
        super ( true );
        this.sslClientCertHeader = "ssl_client_cert";
        this.sslCipherHeader = "ssl_cipher";
        this.sslSessionIdHeader = "ssl_session_id";
        this.sslCipherUserKeySizeHeader = "ssl_cipher_usekeysize";
    }
    public String getSslClientCertHeader() {
        return this.sslClientCertHeader;
    }
    public void setSslClientCertHeader ( final String sslClientCertHeader ) {
        this.sslClientCertHeader = sslClientCertHeader;
    }
    public String getSslCipherHeader() {
        return this.sslCipherHeader;
    }
    public void setSslCipherHeader ( final String sslCipherHeader ) {
        this.sslCipherHeader = sslCipherHeader;
    }
    public String getSslSessionIdHeader() {
        return this.sslSessionIdHeader;
    }
    public void setSslSessionIdHeader ( final String sslSessionIdHeader ) {
        this.sslSessionIdHeader = sslSessionIdHeader;
    }
    public String getSslCipherUserKeySizeHeader() {
        return this.sslCipherUserKeySizeHeader;
    }
    public void setSslCipherUserKeySizeHeader ( final String sslCipherUserKeySizeHeader ) {
        this.sslCipherUserKeySizeHeader = sslCipherUserKeySizeHeader;
    }
    public String mygetHeader ( final Request request, final String header ) {
        final String strcert0 = request.getHeader ( header );
        if ( strcert0 == null ) {
            return null;
        }
        if ( "(null)".equals ( strcert0 ) ) {
            return null;
        }
        return strcert0;
    }
    @Override
    public void invoke ( final Request request, final Response response ) throws IOException, ServletException {
        String strcert0 = this.mygetHeader ( request, this.sslClientCertHeader );
        if ( strcert0 != null && strcert0.length() > 28 ) {
            final String strcert = strcert0.replace ( ' ', '\n' );
            final String strcert2 = strcert.substring ( 28, strcert.length() - 26 );
            final String strcert3 = "-----BEGIN CERTIFICATE-----\n";
            final String strcert4 = strcert3.concat ( strcert2 );
            final String strcerts = strcert4.concat ( "\n-----END CERTIFICATE-----\n" );
            final ByteArrayInputStream bais = new ByteArrayInputStream ( strcerts.getBytes ( StandardCharsets.ISO_8859_1 ) );
            X509Certificate[] jsseCerts = null;
            final String providerName = ( String ) request.getConnector().getProperty ( "clientCertProvider" );
            try {
                CertificateFactory cf;
                if ( providerName == null ) {
                    cf = CertificateFactory.getInstance ( "X.509" );
                } else {
                    cf = CertificateFactory.getInstance ( "X.509", providerName );
                }
                final X509Certificate cert = ( X509Certificate ) cf.generateCertificate ( bais );
                jsseCerts = new X509Certificate[] { cert };
            } catch ( CertificateException e ) {
                SSLValve.log.warn ( SSLValve.sm.getString ( "sslValve.certError", strcerts ), e );
            } catch ( NoSuchProviderException e2 ) {
                SSLValve.log.error ( SSLValve.sm.getString ( "sslValve.invalidProvider", providerName ), e2 );
            }
            request.setAttribute ( "javax.servlet.request.X509Certificate", jsseCerts );
        }
        strcert0 = this.mygetHeader ( request, this.sslCipherHeader );
        if ( strcert0 != null ) {
            request.setAttribute ( "javax.servlet.request.cipher_suite", strcert0 );
        }
        strcert0 = this.mygetHeader ( request, this.sslSessionIdHeader );
        if ( strcert0 != null ) {
            request.setAttribute ( "javax.servlet.request.ssl_session_id", strcert0 );
        }
        strcert0 = this.mygetHeader ( request, this.sslCipherUserKeySizeHeader );
        if ( strcert0 != null ) {
            request.setAttribute ( "javax.servlet.request.key_size", Integer.valueOf ( strcert0 ) );
        }
        this.getNext().invoke ( request, response );
    }
    static {
        log = LogFactory.getLog ( SSLValve.class );
    }
}
