package org.apache.tomcat.util.net;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.io.IOException;
import org.apache.tomcat.jni.SSLSocket;
public class AprSSLSupport implements SSLSupport {
    private final SocketWrapperBase<Long> socketWrapper;
    private final String clientCertProvider;
    public AprSSLSupport ( final SocketWrapperBase<Long> socketWrapper, final String clientCertProvider ) {
        this.socketWrapper = socketWrapper;
        this.clientCertProvider = clientCertProvider;
    }
    @Override
    public String getCipherSuite() throws IOException {
        final long socketRef = this.socketWrapper.getSocket();
        if ( socketRef == 0L ) {
            return null;
        }
        try {
            return SSLSocket.getInfoS ( socketRef, 2 );
        } catch ( Exception e ) {
            throw new IOException ( e );
        }
    }
    @Override
    public X509Certificate[] getPeerCertificateChain() throws IOException {
        final long socketRef = this.socketWrapper.getSocket();
        if ( socketRef == 0L ) {
            return null;
        }
        try {
            final int certLength = SSLSocket.getInfoI ( socketRef, 1024 );
            final byte[] clientCert = SSLSocket.getInfoB ( socketRef, 263 );
            X509Certificate[] certs = null;
            if ( clientCert != null && certLength > -1 ) {
                certs = new X509Certificate[certLength + 1];
                CertificateFactory cf;
                if ( this.clientCertProvider == null ) {
                    cf = CertificateFactory.getInstance ( "X.509" );
                } else {
                    cf = CertificateFactory.getInstance ( "X.509", this.clientCertProvider );
                }
                certs[0] = ( X509Certificate ) cf.generateCertificate ( new ByteArrayInputStream ( clientCert ) );
                for ( int i = 0; i < certLength; ++i ) {
                    final byte[] data = SSLSocket.getInfoB ( socketRef, 1024 + i );
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
        final long socketRef = this.socketWrapper.getSocket();
        if ( socketRef == 0L ) {
            return null;
        }
        try {
            return SSLSocket.getInfoI ( socketRef, 3 );
        } catch ( Exception e ) {
            throw new IOException ( e );
        }
    }
    @Override
    public String getSessionId() throws IOException {
        final long socketRef = this.socketWrapper.getSocket();
        if ( socketRef == 0L ) {
            return null;
        }
        try {
            return SSLSocket.getInfoS ( socketRef, 1 );
        } catch ( Exception e ) {
            throw new IOException ( e );
        }
    }
    @Override
    public String getProtocol() throws IOException {
        final long socketRef = this.socketWrapper.getSocket();
        if ( socketRef == 0L ) {
            return null;
        }
        try {
            return SSLSocket.getInfoS ( socketRef, 7 );
        } catch ( Exception e ) {
            throw new IOException ( e );
        }
    }
}
