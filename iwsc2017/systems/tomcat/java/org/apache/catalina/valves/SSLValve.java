package org.apache.catalina.valves;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.servlet.ServletException;
import org.apache.catalina.Globals;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class SSLValve extends ValveBase {
    private static final Log log = LogFactory.getLog ( SSLValve.class );
    private String sslClientCertHeader = "ssl_client_cert";
    private String sslCipherHeader = "ssl_cipher";
    private String sslSessionIdHeader = "ssl_session_id";
    private String sslCipherUserKeySizeHeader = "ssl_cipher_usekeysize";
    public SSLValve() {
        super ( true );
    }
    public String getSslClientCertHeader() {
        return sslClientCertHeader;
    }
    public void setSslClientCertHeader ( String sslClientCertHeader ) {
        this.sslClientCertHeader = sslClientCertHeader;
    }
    public String getSslCipherHeader() {
        return sslCipherHeader;
    }
    public void setSslCipherHeader ( String sslCipherHeader ) {
        this.sslCipherHeader = sslCipherHeader;
    }
    public String getSslSessionIdHeader() {
        return sslSessionIdHeader;
    }
    public void setSslSessionIdHeader ( String sslSessionIdHeader ) {
        this.sslSessionIdHeader = sslSessionIdHeader;
    }
    public String getSslCipherUserKeySizeHeader() {
        return sslCipherUserKeySizeHeader;
    }
    public void setSslCipherUserKeySizeHeader ( String sslCipherUserKeySizeHeader ) {
        this.sslCipherUserKeySizeHeader = sslCipherUserKeySizeHeader;
    }
    public String mygetHeader ( Request request, String header ) {
        String strcert0 = request.getHeader ( header );
        if ( strcert0 == null ) {
            return null;
        }
        if ( "(null)".equals ( strcert0 ) ) {
            return null;
        }
        return strcert0;
    }
    @Override
    public void invoke ( Request request, Response response )
    throws IOException, ServletException {
        String strcert0 = mygetHeader ( request, sslClientCertHeader );
        if ( strcert0 != null && strcert0.length() > 28 ) {
            String strcert1 = strcert0.replace ( ' ', '\n' );
            String strcert2 = strcert1.substring ( 28, strcert1.length() - 26 );
            String strcert3 = "-----BEGIN CERTIFICATE-----\n";
            String strcert4 = strcert3.concat ( strcert2 );
            String strcerts = strcert4.concat ( "\n-----END CERTIFICATE-----\n" );
            ByteArrayInputStream bais = new ByteArrayInputStream (
                strcerts.getBytes ( StandardCharsets.ISO_8859_1 ) );
            X509Certificate jsseCerts[] = null;
            String providerName = ( String ) request.getConnector().getProperty (
                                      "clientCertProvider" );
            try {
                CertificateFactory cf;
                if ( providerName == null ) {
                    cf = CertificateFactory.getInstance ( "X.509" );
                } else {
                    cf = CertificateFactory.getInstance ( "X.509", providerName );
                }
                X509Certificate cert = ( X509Certificate ) cf.generateCertificate ( bais );
                jsseCerts = new X509Certificate[1];
                jsseCerts[0] = cert;
            } catch ( java.security.cert.CertificateException e ) {
                log.warn ( sm.getString ( "sslValve.certError", strcerts ), e );
            } catch ( NoSuchProviderException e ) {
                log.error ( sm.getString (
                                "sslValve.invalidProvider", providerName ), e );
            }
            request.setAttribute ( Globals.CERTIFICATES_ATTR, jsseCerts );
        }
        strcert0 = mygetHeader ( request, sslCipherHeader );
        if ( strcert0 != null ) {
            request.setAttribute ( Globals.CIPHER_SUITE_ATTR, strcert0 );
        }
        strcert0 = mygetHeader ( request, sslSessionIdHeader );
        if ( strcert0 != null ) {
            request.setAttribute ( Globals.SSL_SESSION_ID_ATTR, strcert0 );
        }
        strcert0 = mygetHeader ( request, sslCipherUserKeySizeHeader );
        if ( strcert0 != null ) {
            request.setAttribute ( Globals.KEY_SIZE_ATTR,
                                   Integer.valueOf ( strcert0 ) );
        }
        getNext().invoke ( request, response );
    }
}
