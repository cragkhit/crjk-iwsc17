package org.apache.tomcat.util.net.openssl;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;
import org.apache.tomcat.jni.CertificateVerifier;
class OpenSSLContext$1 implements CertificateVerifier {
    final   X509TrustManager val$manager;
    @Override
    public boolean verify ( final long ssl, final byte[][] chain, final String auth ) {
        final X509Certificate[] peerCerts = OpenSSLContext.access$000 ( chain );
        try {
            this.val$manager.checkClientTrusted ( peerCerts, auth );
            return true;
        } catch ( Exception e ) {
            OpenSSLContext.access$200().debug ( OpenSSLContext.access$100().getString ( "openssl.certificateVerificationFailed" ), e );
            return false;
        }
    }
}
