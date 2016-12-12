package org.apache.tomcat.util.net.jsse;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import org.apache.tomcat.util.compat.JreVendor;
import java.util.HashSet;
import java.security.KeyManagementException;
import java.security.SecureRandom;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.file.ConfigFileLoader;
import java.security.cert.CertificateFactory;
import java.security.cert.CertStoreParameters;
import java.security.cert.CRL;
import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.CertSelector;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import javax.net.ssl.SSLSessionContext;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.net.ssl.ManagerFactoryParameters;
import java.security.cert.CertPathParameters;
import java.security.cert.CRLException;
import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import java.util.Locale;
import javax.net.ssl.KeyManagerFactory;
import java.io.IOException;
import java.security.Key;
import java.util.Collection;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.io.InputStream;
import java.security.KeyStore;
import javax.net.ssl.KeyManager;
import java.security.NoSuchAlgorithmException;
import org.apache.tomcat.util.net.SSLContext;
import java.util.List;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.apache.tomcat.util.net.SSLHostConfig;
import java.util.Set;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.net.SSLUtilBase;
public class JSSEUtil extends SSLUtilBase {
    private static final Log log;
    private static final StringManager sm;
    private static final Set<String> implementedProtocols;
    private static final Set<String> implementedCiphers;
    private final SSLHostConfig sslHostConfig;
    public JSSEUtil ( final SSLHostConfigCertificate certificate ) {
        super ( certificate );
        this.sslHostConfig = certificate.getSSLHostConfig();
    }
    @Override
    protected Log getLog() {
        return JSSEUtil.log;
    }
    @Override
    protected Set<String> getImplementedProtocols() {
        return JSSEUtil.implementedProtocols;
    }
    @Override
    protected Set<String> getImplementedCiphers() {
        return JSSEUtil.implementedCiphers;
    }
    @Override
    public SSLContext createSSLContext ( final List<String> negotiableProtocols ) throws NoSuchAlgorithmException {
        return new JSSESSLContext ( this.sslHostConfig.getSslProtocol() );
    }
    @Override
    public KeyManager[] getKeyManagers() throws Exception {
        final String keystoreType = this.certificate.getCertificateKeystoreType();
        String keyAlias = this.certificate.getCertificateKeyAlias();
        final String algorithm = this.sslHostConfig.getKeyManagerAlgorithm();
        String keyPass = this.certificate.getCertificateKeyPassword();
        if ( keyPass == null ) {
            keyPass = this.certificate.getCertificateKeystorePassword();
        }
        KeyManager[] kms = null;
        KeyStore ks = this.certificate.getCertificateKeystore();
        if ( ks == null ) {
            ks = KeyStore.getInstance ( "JKS" );
            ks.load ( null, null );
            final PEMFile privateKeyFile = new PEMFile ( SSLHostConfig.adjustRelativePath ( ( this.certificate.getCertificateKeyFile() != null ) ? this.certificate.getCertificateKeyFile() : this.certificate.getCertificateFile() ), keyPass );
            final PEMFile certificateFile = new PEMFile ( SSLHostConfig.adjustRelativePath ( this.certificate.getCertificateFile() ) );
            final Collection<Certificate> chain = new ArrayList<Certificate>();
            chain.addAll ( certificateFile.getCertificates() );
            if ( this.certificate.getCertificateChainFile() != null ) {
                final PEMFile certificateChainFile = new PEMFile ( SSLHostConfig.adjustRelativePath ( this.certificate.getCertificateChainFile() ) );
                chain.addAll ( certificateChainFile.getCertificates() );
            }
            if ( keyAlias == null ) {
                keyAlias = "tomcat";
            }
            ks.setKeyEntry ( keyAlias, privateKeyFile.getPrivateKey(), keyPass.toCharArray(), chain.toArray ( new Certificate[chain.size()] ) );
        }
        if ( keyAlias != null && !ks.isKeyEntry ( keyAlias ) ) {
            throw new IOException ( JSSEUtil.sm.getString ( "jsse.alias_no_key_entry", keyAlias ) );
        }
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance ( algorithm );
        kmf.init ( ks, keyPass.toCharArray() );
        kms = kmf.getKeyManagers();
        if ( kms == null ) {
            return kms;
        }
        if ( keyAlias != null ) {
            String alias = keyAlias;
            if ( "JKS".equals ( keystoreType ) ) {
                alias = alias.toLowerCase ( Locale.ENGLISH );
            }
            for ( int i = 0; i < kms.length; ++i ) {
                kms[i] = new JSSEKeyManager ( ( X509KeyManager ) kms[i], alias );
            }
        }
        return kms;
    }
    @Override
    public TrustManager[] getTrustManagers() throws Exception {
        final String className = this.sslHostConfig.getTrustManagerClassName();
        if ( className == null || className.length() <= 0 ) {
            TrustManager[] tms = null;
            final KeyStore trustStore = this.sslHostConfig.getTruststore();
            if ( trustStore != null ) {
                this.checkTrustStoreEntries ( trustStore );
                final String algorithm = this.sslHostConfig.getTruststoreAlgorithm();
                final String crlf = this.sslHostConfig.getCertificateRevocationListFile();
                if ( "PKIX".equalsIgnoreCase ( algorithm ) ) {
                    final TrustManagerFactory tmf = TrustManagerFactory.getInstance ( algorithm );
                    final CertPathParameters params = this.getParameters ( crlf, trustStore );
                    final ManagerFactoryParameters mfp = new CertPathTrustManagerParameters ( params );
                    tmf.init ( mfp );
                    tms = tmf.getTrustManagers();
                } else {
                    final TrustManagerFactory tmf = TrustManagerFactory.getInstance ( algorithm );
                    tmf.init ( trustStore );
                    tms = tmf.getTrustManagers();
                    if ( crlf != null && crlf.length() > 0 ) {
                        throw new CRLException ( JSSEUtil.sm.getString ( "jsseUtil.noCrlSupport", algorithm ) );
                    }
                    JSSEUtil.log.warn ( JSSEUtil.sm.getString ( "jsseUtil.noVerificationDepth" ) );
                }
            }
            return tms;
        }
        final ClassLoader classLoader = this.getClass().getClassLoader();
        final Class<?> clazz = classLoader.loadClass ( className );
        if ( !TrustManager.class.isAssignableFrom ( clazz ) ) {
            throw new InstantiationException ( JSSEUtil.sm.getString ( "jsse.invalidTrustManagerClassName", className ) );
        }
        final Object trustManagerObject = clazz.newInstance();
        final TrustManager trustManager = ( TrustManager ) trustManagerObject;
        return new TrustManager[] { trustManager };
    }
    private void checkTrustStoreEntries ( final KeyStore trustStore ) throws Exception {
        final Enumeration<String> aliases = trustStore.aliases();
        if ( aliases != null ) {
            final Date now = new Date();
            while ( aliases.hasMoreElements() ) {
                final String alias = aliases.nextElement();
                if ( trustStore.isCertificateEntry ( alias ) ) {
                    final Certificate cert = trustStore.getCertificate ( alias );
                    if ( cert instanceof X509Certificate ) {
                        try {
                            ( ( X509Certificate ) cert ).checkValidity ( now );
                        } catch ( CertificateExpiredException | CertificateNotYetValidException e ) {
                            final String msg = JSSEUtil.sm.getString ( "jsseUtil.trustedCertNotValid", alias, ( ( X509Certificate ) cert ).getSubjectDN(), e.getMessage() );
                            if ( JSSEUtil.log.isDebugEnabled() ) {
                                JSSEUtil.log.debug ( msg, e );
                            } else {
                                JSSEUtil.log.warn ( msg );
                            }
                        }
                    } else {
                        if ( !JSSEUtil.log.isDebugEnabled() ) {
                            continue;
                        }
                        JSSEUtil.log.debug ( JSSEUtil.sm.getString ( "jsseUtil.trustedCertNotChecked", alias ) );
                    }
                }
            }
        }
    }
    @Override
    public void configureSessionContext ( final SSLSessionContext sslSessionContext ) {
        sslSessionContext.setSessionCacheSize ( this.sslHostConfig.getSessionCacheSize() );
        sslSessionContext.setSessionTimeout ( this.sslHostConfig.getSessionTimeout() );
    }
    protected CertPathParameters getParameters ( final String crlf, final KeyStore trustStore ) throws Exception {
        final PKIXBuilderParameters xparams = new PKIXBuilderParameters ( trustStore, new X509CertSelector() );
        if ( crlf != null && crlf.length() > 0 ) {
            final Collection<? extends CRL> crls = this.getCRLs ( crlf );
            final CertStoreParameters csp = new CollectionCertStoreParameters ( crls );
            final CertStore store = CertStore.getInstance ( "Collection", csp );
            xparams.addCertStore ( store );
            xparams.setRevocationEnabled ( true );
        } else {
            xparams.setRevocationEnabled ( false );
        }
        xparams.setMaxPathLength ( this.sslHostConfig.getCertificateVerificationDepth() );
        return xparams;
    }
    protected Collection<? extends CRL> getCRLs ( final String crlf ) throws IOException, CRLException, CertificateException {
        Collection<? extends CRL> crls = null;
        try {
            final CertificateFactory cf = CertificateFactory.getInstance ( "X.509" );
            try ( final InputStream is = ConfigFileLoader.getInputStream ( crlf ) ) {
                crls = cf.generateCRLs ( is );
            }
        } catch ( IOException iex ) {
            throw iex;
        } catch ( CRLException crle ) {
            throw crle;
        } catch ( CertificateException ce ) {
            throw ce;
        }
        return crls;
    }
    static {
        log = LogFactory.getLog ( JSSEUtil.class );
        sm = StringManager.getManager ( JSSEUtil.class );
        SSLContext context;
        try {
            context = new JSSESSLContext ( "TLS" );
            context.init ( null, null, null );
        } catch ( NoSuchAlgorithmException | KeyManagementException e ) {
            throw new IllegalArgumentException ( e );
        }
        final String[] implementedProtocolsArray = context.getSupportedSSLParameters().getProtocols();
        implementedProtocols = new HashSet<String> ( implementedProtocolsArray.length );
        for ( final String protocol : implementedProtocolsArray ) {
            final String protocolUpper = protocol.toUpperCase ( Locale.ENGLISH );
            if ( !"SSLV2HELLO".equals ( protocolUpper ) && protocolUpper.contains ( "SSL" ) ) {
                JSSEUtil.log.debug ( JSSEUtil.sm.getString ( "jsse.excludeProtocol", protocol ) );
            } else {
                JSSEUtil.implementedProtocols.add ( protocol );
            }
        }
        if ( JSSEUtil.implementedProtocols.size() == 0 ) {
            JSSEUtil.log.warn ( JSSEUtil.sm.getString ( "jsse.noDefaultProtocols" ) );
        }
        final String[] implementedCipherSuiteArray = context.getSupportedSSLParameters().getCipherSuites();
        if ( JreVendor.IS_IBM_JVM ) {
            implementedCiphers = new HashSet<String> ( implementedCipherSuiteArray.length * 2 );
            for ( final String name : implementedCipherSuiteArray ) {
                JSSEUtil.implementedCiphers.add ( name );
                if ( name.startsWith ( "SSL" ) ) {
                    JSSEUtil.implementedCiphers.add ( "TLS" + name.substring ( 3 ) );
                }
            }
        } else {
            ( implementedCiphers = new HashSet<String> ( implementedCipherSuiteArray.length ) ).addAll ( Arrays.asList ( implementedCipherSuiteArray ) );
        }
    }
}
