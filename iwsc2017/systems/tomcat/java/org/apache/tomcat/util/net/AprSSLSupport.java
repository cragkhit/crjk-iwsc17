package org.apache.tomcat.util.net;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.apache.tomcat.jni.SSL;
import org.apache.tomcat.jni.SSLSocket;
public class AprSSLSupport implements SSLSupport {
    private final SocketWrapperBase<Long> socketWrapper;
    private final String clientCertProvider;
    public AprSSLSupport ( SocketWrapperBase<Long> socketWrapper, String clientCertProvider ) {
        this.socketWrapper = socketWrapper;
        this.clientCertProvider = clientCertProvider;
    }
    @Override
    public String getCipherSuite() throws IOException {
        long socketRef = socketWrapper.getSocket().longValue();
        if ( socketRef == 0 ) {
            return null;
        }
        try {
            return SSLSocket.getInfoS ( socketRef, SSL.SSL_INFO_CIPHER );
        } catch ( Exception e ) {
            throw new IOException ( e );
        }
    }
    @Override
    public X509Certificate[] getPeerCertificateChain() throws IOException {
        long socketRef = socketWrapper.getSocket().longValue();
        if ( socketRef == 0 ) {
            return null;
        }
        try {
            int certLength = SSLSocket.getInfoI ( socketRef, SSL.SSL_INFO_CLIENT_CERT_CHAIN );
            byte[] clientCert = SSLSocket.getInfoB ( socketRef, SSL.SSL_INFO_CLIENT_CERT );
            X509Certificate[] certs = null;
            if ( clientCert != null  && certLength > -1 ) {
                certs = new X509Certificate[certLength + 1];
                CertificateFactory cf;
                if ( clientCertProvider == null ) {
                    cf = CertificateFactory.getInstance ( "X.509" );
                } else {
                    cf = CertificateFactory.getInstance ( "X.509", clientCertProvider );
                }
                certs[0] = ( X509Certificate ) cf.generateCertificate ( new ByteArrayInputStream ( clientCert ) );
                for ( int i = 0; i < certLength; i++ ) {
                    byte[] data = SSLSocket.getInfoB ( socketRef, SSL.SSL_INFO_CLIENT_CERT_CHAIN + i );
                    certs[i + 1] = ( X509Certificate ) cf.generateCertificate ( new ByteArrayInputStream ( data ) );
                }
            }
            return certs;
        } catch ( Exception e ) {
            throw new IOException ( e );
        }
    }
    @Override
    public Integer getKeySize() throws IOException {
        long socketRef = socketWrapper.getSocket().longValue();
        if ( socketRef == 0 ) {
            return null;
        }
        try {
            return Integer.valueOf ( SSLSocket.getInfoI ( socketRef, SSL.SSL_INFO_CIPHER_USEKEYSIZE ) );
        } catch ( Exception e ) {
            throw new IOException ( e );
        }
    }
    @Override
    public String getSessionId() throws IOException {
        long socketRef = socketWrapper.getSocket().longValue();
        if ( socketRef == 0 ) {
            return null;
        }
        try {
            return SSLSocket.getInfoS ( socketRef, SSL.SSL_INFO_SESSION_ID );
        } catch ( Exception e ) {
            throw new IOException ( e );
        }
    }
    @Override
    public String getProtocol() throws IOException {
        long socketRef = socketWrapper.getSocket().longValue();
        if ( socketRef == 0 ) {
            return null;
        }
        try {
            return SSLSocket.getInfoS ( socketRef, SSL.SSL_INFO_PROTOCOL );
        } catch ( Exception e ) {
            throw new IOException ( e );
        }
    }
}
