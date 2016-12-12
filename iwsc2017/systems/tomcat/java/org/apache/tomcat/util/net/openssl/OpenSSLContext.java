package org.apache.tomcat.util.net.openssl;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.jni.CertificateVerifier;
import org.apache.tomcat.jni.Pool;
import org.apache.tomcat.jni.SSL;
import org.apache.tomcat.jni.SSLContext;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.Constants;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.apache.tomcat.util.net.jsse.JSSEKeyManager;
import org.apache.tomcat.util.net.openssl.ciphers.OpenSSLCipherConfigurationParser;
import org.apache.tomcat.util.res.StringManager;
public class OpenSSLContext implements org.apache.tomcat.util.net.SSLContext {
    private static final Log log = LogFactory.getLog ( OpenSSLContext.class );
    private static final StringManager netSm = StringManager.getManager ( AbstractEndpoint.class );
    private static final StringManager sm = StringManager.getManager ( OpenSSLContext.class );
    private static final String defaultProtocol = "TLS";
    private final SSLHostConfig sslHostConfig;
    private final SSLHostConfigCertificate certificate;
    private OpenSSLSessionContext sessionContext;
    private final List<String> negotiableProtocols;
    private List<String> jsseCipherNames = new ArrayList<>();
    public List<String> getJsseCipherNames() {
        return jsseCipherNames;
    }
    private String enabledProtocol;
    public String getEnabledProtocol() {
        return enabledProtocol;
    }
    public void setEnabledProtocol ( String protocol ) {
        enabledProtocol = ( protocol == null ) ? defaultProtocol : protocol;
    }
    private final long aprPool;
    private final AtomicInteger aprPoolDestroyed = new AtomicInteger ( 0 );
    protected final long ctx;
    static final CertificateFactory X509_CERT_FACTORY;
    private static final String BEGIN_KEY = "-----BEGIN RSA PRIVATE KEY-----\n";
    private static final Object END_KEY = "\n-----END RSA PRIVATE KEY-----";
    private boolean initialized = false;
    static {
        try {
            X509_CERT_FACTORY = CertificateFactory.getInstance ( "X.509" );
        } catch ( CertificateException e ) {
            throw new IllegalStateException ( sm.getString ( "openssl.X509FactoryError" ), e );
        }
    }
    public OpenSSLContext ( SSLHostConfigCertificate certificate, List<String> negotiableProtocols )
    throws SSLException {
        this.sslHostConfig = certificate.getSSLHostConfig();
        this.certificate = certificate;
        aprPool = Pool.create ( 0 );
        boolean success = false;
        try {
            int value = SSL.SSL_PROTOCOL_NONE;
            if ( sslHostConfig.getProtocols().size() == 0 ) {
                value = SSL.SSL_PROTOCOL_ALL;
            } else {
                for ( String protocol : sslHostConfig.getEnabledProtocols() ) {
                    if ( Constants.SSL_PROTO_SSLv2Hello.equalsIgnoreCase ( protocol ) ) {
                    } else if ( Constants.SSL_PROTO_SSLv2.equalsIgnoreCase ( protocol ) ) {
                        value |= SSL.SSL_PROTOCOL_SSLV2;
                    } else if ( Constants.SSL_PROTO_SSLv3.equalsIgnoreCase ( protocol ) ) {
                        value |= SSL.SSL_PROTOCOL_SSLV3;
                    } else if ( Constants.SSL_PROTO_TLSv1.equalsIgnoreCase ( protocol ) ) {
                        value |= SSL.SSL_PROTOCOL_TLSV1;
                    } else if ( Constants.SSL_PROTO_TLSv1_1.equalsIgnoreCase ( protocol ) ) {
                        value |= SSL.SSL_PROTOCOL_TLSV1_1;
                    } else if ( Constants.SSL_PROTO_TLSv1_2.equalsIgnoreCase ( protocol ) ) {
                        value |= SSL.SSL_PROTOCOL_TLSV1_2;
                    } else if ( Constants.SSL_PROTO_ALL.equalsIgnoreCase ( protocol ) ) {
                        value |= SSL.SSL_PROTOCOL_ALL;
                    } else {
                        throw new Exception ( netSm.getString (
                                                  "endpoint.apr.invalidSslProtocol", protocol ) );
                    }
                }
            }
            try {
                ctx = SSLContext.make ( aprPool, value, SSL.SSL_MODE_SERVER );
            } catch ( Exception e ) {
                throw new Exception (
                    netSm.getString ( "endpoint.apr.failSslContextMake" ), e );
            }
            this.negotiableProtocols = negotiableProtocols;
            success = true;
        } catch ( Exception e ) {
            throw new SSLException ( sm.getString ( "openssl.errorSSLCtxInit" ), e );
        } finally {
            if ( !success ) {
                destroy();
            }
        }
    }
    @Override
    public synchronized void destroy() {
        if ( aprPoolDestroyed.compareAndSet ( 0, 1 ) ) {
            if ( ctx != 0 ) {
                SSLContext.free ( ctx );
            }
            if ( aprPool != 0 ) {
                Pool.destroy ( aprPool );
            }
        }
    }
    @Override
    public synchronized void init ( KeyManager[] kms, TrustManager[] tms, SecureRandom sr ) {
        if ( initialized ) {
            log.warn ( sm.getString ( "openssl.doubleInit" ) );
            return;
        }
        try {
            if ( sslHostConfig.getInsecureRenegotiation() ) {
                SSLContext.setOptions ( ctx, SSL.SSL_OP_ALLOW_UNSAFE_LEGACY_RENEGOTIATION );
            } else {
                SSLContext.clearOptions ( ctx, SSL.SSL_OP_ALLOW_UNSAFE_LEGACY_RENEGOTIATION );
            }
            if ( sslHostConfig.getHonorCipherOrder() ) {
                SSLContext.setOptions ( ctx, SSL.SSL_OP_CIPHER_SERVER_PREFERENCE );
            } else {
                SSLContext.clearOptions ( ctx, SSL.SSL_OP_CIPHER_SERVER_PREFERENCE );
            }
            if ( sslHostConfig.getDisableCompression() ) {
                SSLContext.setOptions ( ctx, SSL.SSL_OP_NO_COMPRESSION );
            } else {
                SSLContext.clearOptions ( ctx, SSL.SSL_OP_NO_COMPRESSION );
            }
            if ( sslHostConfig.getDisableSessionTickets() ) {
                SSLContext.setOptions ( ctx, SSL.SSL_OP_NO_TICKET );
            } else {
                SSLContext.clearOptions ( ctx, SSL.SSL_OP_NO_TICKET );
            }
            if ( sslHostConfig.getSessionCacheSize() > 0 ) {
                SSLContext.setSessionCacheSize ( ctx, sslHostConfig.getSessionCacheSize() );
            } else {
                long sessionCacheSize = SSLContext.setSessionCacheSize ( ctx, 20480 );
                SSLContext.setSessionCacheSize ( ctx, sessionCacheSize );
            }
            if ( sslHostConfig.getSessionTimeout() > 0 ) {
                SSLContext.setSessionCacheTimeout ( ctx, sslHostConfig.getSessionTimeout() );
            } else {
                long sessionTimeout = SSLContext.setSessionCacheTimeout ( ctx, 300 );
                SSLContext.setSessionCacheTimeout ( ctx, sessionTimeout );
            }
            String opensslCipherConfig = sslHostConfig.getCiphers();
            this.jsseCipherNames = OpenSSLCipherConfigurationParser.parseExpression ( opensslCipherConfig );
            SSLContext.setCipherSuite ( ctx, opensslCipherConfig );
            if ( certificate.getCertificateFile() != null ) {
                SSLContext.setCertificate ( ctx,
                                            SSLHostConfig.adjustRelativePath ( certificate.getCertificateFile() ),
                                            SSLHostConfig.adjustRelativePath ( certificate.getCertificateKeyFile() ),
                                            certificate.getCertificateKeyPassword(), SSL.SSL_AIDX_RSA );
                SSLContext.setCertificateChainFile ( ctx,
                                                     SSLHostConfig.adjustRelativePath ( certificate.getCertificateChainFile() ), false );
                SSLContext.setCACertificate ( ctx,
                                              SSLHostConfig.adjustRelativePath ( sslHostConfig.getCaCertificateFile() ),
                                              SSLHostConfig.adjustRelativePath ( sslHostConfig.getCaCertificatePath() ) );
                SSLContext.setCARevocation ( ctx,
                                             SSLHostConfig.adjustRelativePath (
                                                 sslHostConfig.getCertificateRevocationListFile() ),
                                             SSLHostConfig.adjustRelativePath (
                                                 sslHostConfig.getCertificateRevocationListPath() ) );
            } else {
                X509KeyManager keyManager = chooseKeyManager ( kms );
                String alias = certificate.getCertificateKeyAlias();
                if ( alias == null ) {
                    alias = "tomcat";
                }
                X509Certificate[] chain = keyManager.getCertificateChain ( alias );
                PrivateKey key = keyManager.getPrivateKey ( alias );
                StringBuilder sb = new StringBuilder ( BEGIN_KEY );
                sb.append ( Base64.getMimeEncoder ( 64, new byte[] {'\n'} ).encodeToString ( key.getEncoded() ) );
                sb.append ( END_KEY );
                SSLContext.setCertificateRaw ( ctx, chain[0].getEncoded(), sb.toString().getBytes ( StandardCharsets.US_ASCII ), SSL.SSL_AIDX_RSA );
                for ( int i = 1; i < chain.length; i++ ) {
                    SSLContext.addChainCertificateRaw ( ctx, chain[i].getEncoded() );
                }
            }
            int value = 0;
            switch ( sslHostConfig.getCertificateVerification() ) {
            case NONE:
                value = SSL.SSL_CVERIFY_NONE;
                break;
            case OPTIONAL:
                value = SSL.SSL_CVERIFY_OPTIONAL;
                break;
            case OPTIONAL_NO_CA:
                value = SSL.SSL_CVERIFY_OPTIONAL_NO_CA;
                break;
            case REQUIRED:
                value = SSL.SSL_CVERIFY_REQUIRE;
                break;
            }
            SSLContext.setVerify ( ctx, value, sslHostConfig.getCertificateVerificationDepth() );
            if ( tms != null ) {
                final X509TrustManager manager = chooseTrustManager ( tms );
                SSLContext.setCertVerifyCallback ( ctx, new CertificateVerifier() {
                    @Override
                    public boolean verify ( long ssl, byte[][] chain, String auth ) {
                        X509Certificate[] peerCerts = certificates ( chain );
                        try {
                            manager.checkClientTrusted ( peerCerts, auth );
                            return true;
                        } catch ( Exception e ) {
                            log.debug ( sm.getString ( "openssl.certificateVerificationFailed" ), e );
                        }
                        return false;
                    }
                } );
            }
            if ( negotiableProtocols != null && negotiableProtocols.size() > 0 ) {
                ArrayList<String> protocols = new ArrayList<>();
                protocols.addAll ( negotiableProtocols );
                protocols.add ( "http/1.1" );
                String[] protocolsArray = protocols.toArray ( new String[0] );
                SSLContext.setAlpnProtos ( ctx, protocolsArray, SSL.SSL_SELECTOR_FAILURE_NO_ADVERTISE );
                SSLContext.setNpnProtos ( ctx, protocolsArray, SSL.SSL_SELECTOR_FAILURE_NO_ADVERTISE );
            }
            sessionContext = new OpenSSLSessionContext ( ctx );
            sslHostConfig.setOpenSslContext ( Long.valueOf ( ctx ) );
            initialized = true;
        } catch ( Exception e ) {
            log.warn ( sm.getString ( "openssl.errorSSLCtxInit" ), e );
            destroy();
        }
    }
    private static X509KeyManager chooseKeyManager ( KeyManager[] managers ) throws Exception {
        for ( KeyManager manager : managers ) {
            if ( manager instanceof JSSEKeyManager ) {
                return ( JSSEKeyManager ) manager;
            }
        }
        for ( KeyManager manager : managers ) {
            if ( manager instanceof X509KeyManager ) {
                return ( X509KeyManager ) manager;
            }
        }
        throw new IllegalStateException ( sm.getString ( "openssl.keyManagerMissing" ) );
    }
    private static X509TrustManager chooseTrustManager ( TrustManager[] managers ) {
        for ( TrustManager m : managers ) {
            if ( m instanceof X509TrustManager ) {
                return ( X509TrustManager ) m;
            }
        }
        throw new IllegalStateException ( sm.getString ( "openssl.trustManagerMissing" ) );
    }
    private static X509Certificate[] certificates ( byte[][] chain ) {
        X509Certificate[] peerCerts = new X509Certificate[chain.length];
        for ( int i = 0; i < peerCerts.length; i++ ) {
            peerCerts[i] = new OpenSslX509Certificate ( chain[i] );
        }
        return peerCerts;
    }
    @Override
    public SSLSessionContext getServerSessionContext() {
        return sessionContext;
    }
    @Override
    public SSLEngine createSSLEngine() {
        return new OpenSSLEngine ( ctx, defaultProtocol, false, sessionContext,
                                   ( negotiableProtocols != null && negotiableProtocols.size() > 0 ) );
    }
    @Override
    public SSLServerSocketFactory getServerSocketFactory() {
        throw new UnsupportedOperationException();
    }
    @Override
    public SSLParameters getSupportedSSLParameters() {
        throw new UnsupportedOperationException();
    }
}
