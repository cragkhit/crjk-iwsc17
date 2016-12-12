package org.apache.tomcat.util.net.openssl;
import java.security.cert.CertificateException;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.juli.logging.LogFactory;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSessionContext;
import org.apache.tomcat.util.net.jsse.JSSEKeyManager;
import java.security.PrivateKey;
import javax.net.ssl.X509KeyManager;
import java.util.Collection;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;
import org.apache.tomcat.jni.CertificateVerifier;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.apache.tomcat.util.net.openssl.ciphers.OpenSSLCipherConfigurationParser;
import java.security.SecureRandom;
import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLException;
import org.apache.tomcat.jni.Pool;
import java.util.ArrayList;
import java.security.cert.CertificateFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.net.SSLContext;
public class OpenSSLContext implements SSLContext {
    private static final Log log;
    private static final StringManager netSm;
    private static final StringManager sm;
    private static final String defaultProtocol = "TLS";
    private final SSLHostConfig sslHostConfig;
    private final SSLHostConfigCertificate certificate;
    private OpenSSLSessionContext sessionContext;
    private final List<String> negotiableProtocols;
    private List<String> jsseCipherNames;
    private String enabledProtocol;
    private final long aprPool;
    private final AtomicInteger aprPoolDestroyed;
    protected final long ctx;
    static final CertificateFactory X509_CERT_FACTORY;
    private static final String BEGIN_KEY = "-----BEGIN RSA PRIVATE KEY-----\n";
    private static final Object END_KEY;
    private boolean initialized;
    public List<String> getJsseCipherNames() {
        return this.jsseCipherNames;
    }
    public String getEnabledProtocol() {
        return this.enabledProtocol;
    }
    public void setEnabledProtocol ( final String protocol ) {
        this.enabledProtocol = ( ( protocol == null ) ? "TLS" : protocol );
    }
    public OpenSSLContext ( final SSLHostConfigCertificate certificate, final List<String> negotiableProtocols ) throws SSLException {
        this.jsseCipherNames = new ArrayList<String>();
        this.aprPoolDestroyed = new AtomicInteger ( 0 );
        this.initialized = false;
        this.sslHostConfig = certificate.getSSLHostConfig();
        this.certificate = certificate;
        this.aprPool = Pool.create ( 0L );
        boolean success = false;
        try {
            int value = 0;
            if ( this.sslHostConfig.getProtocols().size() == 0 ) {
                value = 28;
            } else {
                for ( final String protocol : this.sslHostConfig.getEnabledProtocols() ) {
                    if ( !"SSLv2Hello".equalsIgnoreCase ( protocol ) ) {
                        if ( "SSLv2".equalsIgnoreCase ( protocol ) ) {
                            value |= 0x1;
                        } else if ( "SSLv3".equalsIgnoreCase ( protocol ) ) {
                            value |= 0x2;
                        } else if ( "TLSv1".equalsIgnoreCase ( protocol ) ) {
                            value |= 0x4;
                        } else if ( "TLSv1.1".equalsIgnoreCase ( protocol ) ) {
                            value |= 0x8;
                        } else if ( "TLSv1.2".equalsIgnoreCase ( protocol ) ) {
                            value |= 0x10;
                        } else {
                            if ( !"all".equalsIgnoreCase ( protocol ) ) {
                                throw new Exception ( OpenSSLContext.netSm.getString ( "endpoint.apr.invalidSslProtocol", protocol ) );
                            }
                            value |= 0x1C;
                        }
                    }
                }
            }
            try {
                this.ctx = org.apache.tomcat.jni.SSLContext.make ( this.aprPool, value, 1 );
            } catch ( Exception e ) {
                throw new Exception ( OpenSSLContext.netSm.getString ( "endpoint.apr.failSslContextMake" ), e );
            }
            this.negotiableProtocols = negotiableProtocols;
            success = true;
        } catch ( Exception e2 ) {
            throw new SSLException ( OpenSSLContext.sm.getString ( "openssl.errorSSLCtxInit" ), e2 );
        } finally {
            if ( !success ) {
                this.destroy();
            }
        }
    }
    @Override
    public synchronized void destroy() {
        if ( this.aprPoolDestroyed.compareAndSet ( 0, 1 ) ) {
            if ( this.ctx != 0L ) {
                org.apache.tomcat.jni.SSLContext.free ( this.ctx );
            }
            if ( this.aprPool != 0L ) {
                Pool.destroy ( this.aprPool );
            }
        }
    }
    @Override
    public synchronized void init ( final KeyManager[] kms, final TrustManager[] tms, final SecureRandom sr ) {
        if ( this.initialized ) {
            OpenSSLContext.log.warn ( OpenSSLContext.sm.getString ( "openssl.doubleInit" ) );
            return;
        }
        try {
            if ( this.sslHostConfig.getInsecureRenegotiation() ) {
                org.apache.tomcat.jni.SSLContext.setOptions ( this.ctx, 262144 );
            } else {
                org.apache.tomcat.jni.SSLContext.clearOptions ( this.ctx, 262144 );
            }
            if ( this.sslHostConfig.getHonorCipherOrder() ) {
                org.apache.tomcat.jni.SSLContext.setOptions ( this.ctx, 4194304 );
            } else {
                org.apache.tomcat.jni.SSLContext.clearOptions ( this.ctx, 4194304 );
            }
            if ( this.sslHostConfig.getDisableCompression() ) {
                org.apache.tomcat.jni.SSLContext.setOptions ( this.ctx, 131072 );
            } else {
                org.apache.tomcat.jni.SSLContext.clearOptions ( this.ctx, 131072 );
            }
            if ( this.sslHostConfig.getDisableSessionTickets() ) {
                org.apache.tomcat.jni.SSLContext.setOptions ( this.ctx, 16384 );
            } else {
                org.apache.tomcat.jni.SSLContext.clearOptions ( this.ctx, 16384 );
            }
            if ( this.sslHostConfig.getSessionCacheSize() > 0 ) {
                org.apache.tomcat.jni.SSLContext.setSessionCacheSize ( this.ctx, this.sslHostConfig.getSessionCacheSize() );
            } else {
                final long sessionCacheSize = org.apache.tomcat.jni.SSLContext.setSessionCacheSize ( this.ctx, 20480L );
                org.apache.tomcat.jni.SSLContext.setSessionCacheSize ( this.ctx, sessionCacheSize );
            }
            if ( this.sslHostConfig.getSessionTimeout() > 0 ) {
                org.apache.tomcat.jni.SSLContext.setSessionCacheTimeout ( this.ctx, this.sslHostConfig.getSessionTimeout() );
            } else {
                final long sessionTimeout = org.apache.tomcat.jni.SSLContext.setSessionCacheTimeout ( this.ctx, 300L );
                org.apache.tomcat.jni.SSLContext.setSessionCacheTimeout ( this.ctx, sessionTimeout );
            }
            final String opensslCipherConfig = this.sslHostConfig.getCiphers();
            this.jsseCipherNames = OpenSSLCipherConfigurationParser.parseExpression ( opensslCipherConfig );
            org.apache.tomcat.jni.SSLContext.setCipherSuite ( this.ctx, opensslCipherConfig );
            if ( this.certificate.getCertificateFile() != null ) {
                org.apache.tomcat.jni.SSLContext.setCertificate ( this.ctx, SSLHostConfig.adjustRelativePath ( this.certificate.getCertificateFile() ), SSLHostConfig.adjustRelativePath ( this.certificate.getCertificateKeyFile() ), this.certificate.getCertificateKeyPassword(), 0 );
                org.apache.tomcat.jni.SSLContext.setCertificateChainFile ( this.ctx, SSLHostConfig.adjustRelativePath ( this.certificate.getCertificateChainFile() ), false );
                org.apache.tomcat.jni.SSLContext.setCACertificate ( this.ctx, SSLHostConfig.adjustRelativePath ( this.sslHostConfig.getCaCertificateFile() ), SSLHostConfig.adjustRelativePath ( this.sslHostConfig.getCaCertificatePath() ) );
                org.apache.tomcat.jni.SSLContext.setCARevocation ( this.ctx, SSLHostConfig.adjustRelativePath ( this.sslHostConfig.getCertificateRevocationListFile() ), SSLHostConfig.adjustRelativePath ( this.sslHostConfig.getCertificateRevocationListPath() ) );
            } else {
                final X509KeyManager keyManager = chooseKeyManager ( kms );
                String alias = this.certificate.getCertificateKeyAlias();
                if ( alias == null ) {
                    alias = "tomcat";
                }
                final X509Certificate[] chain = keyManager.getCertificateChain ( alias );
                final PrivateKey key = keyManager.getPrivateKey ( alias );
                final StringBuilder sb = new StringBuilder ( "-----BEGIN RSA PRIVATE KEY-----\n" );
                sb.append ( Base64.getMimeEncoder ( 64, new byte[] { 10 } ).encodeToString ( key.getEncoded() ) );
                sb.append ( OpenSSLContext.END_KEY );
                org.apache.tomcat.jni.SSLContext.setCertificateRaw ( this.ctx, chain[0].getEncoded(), sb.toString().getBytes ( StandardCharsets.US_ASCII ), 0 );
                for ( int i = 1; i < chain.length; ++i ) {
                    org.apache.tomcat.jni.SSLContext.addChainCertificateRaw ( this.ctx, chain[i].getEncoded() );
                }
            }
            int value = 0;
            switch ( this.sslHostConfig.getCertificateVerification() ) {
            case NONE: {
                value = 0;
                break;
            }
            case OPTIONAL: {
                value = 1;
                break;
            }
            case OPTIONAL_NO_CA: {
                value = 3;
                break;
            }
            case REQUIRED: {
                value = 2;
                break;
            }
            }
            org.apache.tomcat.jni.SSLContext.setVerify ( this.ctx, value, this.sslHostConfig.getCertificateVerificationDepth() );
            if ( tms != null ) {
                final X509TrustManager manager = chooseTrustManager ( tms );
                org.apache.tomcat.jni.SSLContext.setCertVerifyCallback ( this.ctx, new CertificateVerifier() {
                    @Override
                    public boolean verify ( final long ssl, final byte[][] chain, final String auth ) {
                        final X509Certificate[] peerCerts = certificates ( chain );
                        try {
                            manager.checkClientTrusted ( peerCerts, auth );
                            return true;
                        } catch ( Exception e ) {
                            OpenSSLContext.log.debug ( OpenSSLContext.sm.getString ( "openssl.certificateVerificationFailed" ), e );
                            return false;
                        }
                    }
                } );
            }
            if ( this.negotiableProtocols != null && this.negotiableProtocols.size() > 0 ) {
                final ArrayList<String> protocols = new ArrayList<String>();
                protocols.addAll ( this.negotiableProtocols );
                protocols.add ( "http/1.1" );
                final String[] protocolsArray = protocols.toArray ( new String[0] );
                org.apache.tomcat.jni.SSLContext.setAlpnProtos ( this.ctx, protocolsArray, 0 );
                org.apache.tomcat.jni.SSLContext.setNpnProtos ( this.ctx, protocolsArray, 0 );
            }
            this.sessionContext = new OpenSSLSessionContext ( this.ctx );
            this.sslHostConfig.setOpenSslContext ( this.ctx );
            this.initialized = true;
        } catch ( Exception e ) {
            OpenSSLContext.log.warn ( OpenSSLContext.sm.getString ( "openssl.errorSSLCtxInit" ), e );
            this.destroy();
        }
    }
    private static X509KeyManager chooseKeyManager ( final KeyManager[] managers ) throws Exception {
        for ( final KeyManager manager : managers ) {
            if ( manager instanceof JSSEKeyManager ) {
                return ( JSSEKeyManager ) manager;
            }
        }
        for ( final KeyManager manager : managers ) {
            if ( manager instanceof X509KeyManager ) {
                return ( X509KeyManager ) manager;
            }
        }
        throw new IllegalStateException ( OpenSSLContext.sm.getString ( "openssl.keyManagerMissing" ) );
    }
    private static X509TrustManager chooseTrustManager ( final TrustManager[] managers ) {
        for ( final TrustManager m : managers ) {
            if ( m instanceof X509TrustManager ) {
                return ( X509TrustManager ) m;
            }
        }
        throw new IllegalStateException ( OpenSSLContext.sm.getString ( "openssl.trustManagerMissing" ) );
    }
    private static X509Certificate[] certificates ( final byte[][] chain ) {
        final X509Certificate[] peerCerts = new X509Certificate[chain.length];
        for ( int i = 0; i < peerCerts.length; ++i ) {
            peerCerts[i] = new OpenSslX509Certificate ( chain[i] );
        }
        return peerCerts;
    }
    @Override
    public SSLSessionContext getServerSessionContext() {
        return this.sessionContext;
    }
    @Override
    public SSLEngine createSSLEngine() {
        return new OpenSSLEngine ( this.ctx, "TLS", false, this.sessionContext, this.negotiableProtocols != null && this.negotiableProtocols.size() > 0 );
    }
    @Override
    public SSLServerSocketFactory getServerSocketFactory() {
        throw new UnsupportedOperationException();
    }
    @Override
    public SSLParameters getSupportedSSLParameters() {
        throw new UnsupportedOperationException();
    }
    static {
        log = LogFactory.getLog ( OpenSSLContext.class );
        netSm = StringManager.getManager ( AbstractEndpoint.class );
        sm = StringManager.getManager ( OpenSSLContext.class );
        END_KEY = "\n-----END RSA PRIVATE KEY-----";
        try {
            X509_CERT_FACTORY = CertificateFactory.getInstance ( "X.509" );
        } catch ( CertificateException e ) {
            throw new IllegalStateException ( OpenSSLContext.sm.getString ( "openssl.X509FactoryError" ), e );
        }
    }
}
