package org.apache.tomcat.util.net;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.KeyStore;
import java.security.UnrecoverableKeyException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.openssl.ciphers.Cipher;
import org.apache.tomcat.util.net.openssl.ciphers.OpenSSLCipherConfigurationParser;
import org.apache.tomcat.util.res.StringManager;
public class SSLHostConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog ( SSLHostConfig.class );
    private static final StringManager sm = StringManager.getManager ( SSLHostConfig.class );
    protected static final String DEFAULT_SSL_HOST_NAME = "_default_";
    protected static final Set<String> SSL_PROTO_ALL_SET = new HashSet<>();
    static {
        SSL_PROTO_ALL_SET.add ( Constants.SSL_PROTO_SSLv2Hello );
        SSL_PROTO_ALL_SET.add ( Constants.SSL_PROTO_TLSv1 );
        SSL_PROTO_ALL_SET.add ( Constants.SSL_PROTO_TLSv1_1 );
        SSL_PROTO_ALL_SET.add ( Constants.SSL_PROTO_TLSv1_2 );
    }
    private Type configType = null;
    private Type currentConfigType = null;
    private Map<Type, Set<String>> configuredProperties = new HashMap<>();
    private String hostName = DEFAULT_SSL_HOST_NAME;
    private transient Long openSslContext;
    private String[] enabledCiphers;
    private String[] enabledProtocols;
    private SSLHostConfigCertificate defaultCertificate = null;
    private Set<SSLHostConfigCertificate> certificates = new HashSet<> ( 4 );
    private String certificateRevocationListFile;
    private CertificateVerification certificateVerification = CertificateVerification.NONE;
    private int certificateVerificationDepth = 10;
    private String ciphers = "HIGH:!aNULL:!eNULL:!EXPORT:!DES:!RC4:!MD5:!kRSA";
    private LinkedHashSet<Cipher> cipherList = null;
    private List<String> jsseCipherNames = null;
    private boolean honorCipherOrder = false;
    private Set<String> protocols = new HashSet<>();
    private String keyManagerAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
    private int sessionCacheSize = 0;
    private int sessionTimeout = 86400;
    private String sslProtocol = Constants.SSL_PROTO_TLS;
    private String trustManagerClassName;
    private String truststoreAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
    private String truststoreFile = System.getProperty ( "javax.net.ssl.trustStore" );
    private String truststorePassword = System.getProperty ( "javax.net.ssl.trustStorePassword" );
    private String truststoreProvider = System.getProperty ( "javax.net.ssl.trustStoreProvider" );
    private String truststoreType = System.getProperty ( "javax.net.ssl.trustStoreType" );
    private transient KeyStore truststore = null;
    private String certificateRevocationListPath;
    private String caCertificateFile;
    private String caCertificatePath;
    private boolean disableCompression = true;
    private boolean disableSessionTickets = false;
    private boolean insecureRenegotiation = false;
    public SSLHostConfig() {
        setProtocols ( Constants.SSL_PROTO_ALL );
    }
    public Long getOpenSslContext() {
        return openSslContext;
    }
    public void setOpenSslContext ( Long openSslContext ) {
        this.openSslContext = openSslContext;
    }
    public void setConfigType ( Type configType ) {
        this.configType = configType;
        if ( configType == Type.EITHER ) {
            if ( configuredProperties.remove ( Type.JSSE ) == null ) {
                configuredProperties.remove ( Type.OPENSSL );
            }
        } else {
            configuredProperties.remove ( configType );
        }
        for ( Map.Entry<Type, Set<String>> entry : configuredProperties.entrySet() ) {
            for ( String property : entry.getValue() ) {
                log.warn ( sm.getString ( "sslHostConfig.mismatch",
                                          property, getHostName(), entry.getKey(), configType ) );
            }
        }
    }
    void setProperty ( String name, Type configType ) {
        if ( this.configType == null ) {
            Set<String> properties = configuredProperties.get ( configType );
            if ( properties == null ) {
                properties = new HashSet<>();
                configuredProperties.put ( configType, properties );
            }
            properties.add ( name );
        } else if ( this.configType == Type.EITHER ) {
            if ( currentConfigType == null ) {
                currentConfigType = configType;
            } else if ( currentConfigType != configType ) {
                log.warn ( sm.getString ( "sslHostConfig.mismatch",
                                          name, getHostName(), configType, currentConfigType ) );
            }
        } else {
            if ( configType != this.configType ) {
                log.warn ( sm.getString ( "sslHostConfig.mismatch",
                                          name, getHostName(), configType, this.configType ) );
            }
        }
    }
    public String[] getEnabledProtocols() {
        return enabledProtocols;
    }
    void setEnabledProtocols ( String[] enabledProtocols ) {
        this.enabledProtocols = enabledProtocols;
    }
    public String[] getEnabledCiphers() {
        return enabledCiphers;
    }
    void setEnabledCiphers ( String[] enabledCiphers ) {
        this.enabledCiphers = enabledCiphers;
    }
    private void registerDefaultCertificate() {
        if ( defaultCertificate == null ) {
            defaultCertificate = new SSLHostConfigCertificate (
                this, SSLHostConfigCertificate.Type.UNDEFINED );
            certificates.add ( defaultCertificate );
        }
    }
    public void addCertificate ( SSLHostConfigCertificate certificate ) {
        if ( certificates.size() == 0 ) {
            certificates.add ( certificate );
            return;
        }
        if ( certificates.size() == 1 &&
                certificates.iterator().next().getType() == SSLHostConfigCertificate.Type.UNDEFINED ||
                certificate.getType() == SSLHostConfigCertificate.Type.UNDEFINED ) {
            throw new IllegalArgumentException ( sm.getString ( "sslHostConfig.certificate.notype" ) );
        }
        certificates.add ( certificate );
    }
    public Set<SSLHostConfigCertificate> getCertificates() {
        return getCertificates ( false );
    }
    public Set<SSLHostConfigCertificate> getCertificates ( boolean createDefaultIfEmpty ) {
        if ( certificates.size() == 0 && createDefaultIfEmpty ) {
            registerDefaultCertificate();
        }
        return certificates;
    }
    public void setCertificateKeyPassword ( String certificateKeyPassword ) {
        registerDefaultCertificate();
        defaultCertificate.setCertificateKeyPassword ( certificateKeyPassword );
    }
    public void setCertificateRevocationListFile ( String certificateRevocationListFile ) {
        this.certificateRevocationListFile = certificateRevocationListFile;
    }
    public String getCertificateRevocationListFile() {
        return certificateRevocationListFile;
    }
    public void setCertificateVerification ( String certificateVerification ) {
        this.certificateVerification = CertificateVerification.fromString ( certificateVerification );
    }
    public CertificateVerification getCertificateVerification() {
        return certificateVerification;
    }
    public void setCertificateVerificationDepth ( int certificateVerificationDepth ) {
        this.certificateVerificationDepth = certificateVerificationDepth;
    }
    public int getCertificateVerificationDepth() {
        return certificateVerificationDepth;
    }
    public void setCiphers ( String ciphersList ) {
        if ( ciphersList != null && !ciphersList.contains ( ":" ) ) {
            StringBuilder sb = new StringBuilder();
            String ciphers[] = ciphersList.split ( "," );
            for ( String cipher : ciphers ) {
                String trimmed = cipher.trim();
                if ( trimmed.length() > 0 ) {
                    String openSSLName = OpenSSLCipherConfigurationParser.jsseToOpenSSL ( trimmed );
                    if ( openSSLName == null ) {
                        openSSLName = trimmed;
                    }
                    if ( sb.length() > 0 ) {
                        sb.append ( ':' );
                    }
                    sb.append ( openSSLName );
                }
            }
            this.ciphers = sb.toString();
        } else {
            this.ciphers = ciphersList;
        }
        this.cipherList = null;
        this.jsseCipherNames = null;
    }
    public String getCiphers() {
        return ciphers;
    }
    public LinkedHashSet<Cipher> getCipherList() {
        if ( cipherList == null ) {
            cipherList = OpenSSLCipherConfigurationParser.parse ( ciphers );
        }
        return cipherList;
    }
    public List<String> getJsseCipherNames() {
        if ( jsseCipherNames == null ) {
            jsseCipherNames = OpenSSLCipherConfigurationParser.convertForJSSE ( getCipherList() );
        }
        return jsseCipherNames;
    }
    public void setHonorCipherOrder ( boolean honorCipherOrder ) {
        this.honorCipherOrder = honorCipherOrder;
    }
    public boolean getHonorCipherOrder() {
        return honorCipherOrder;
    }
    public void setHostName ( String hostName ) {
        this.hostName = hostName;
    }
    public String getHostName() {
        return hostName;
    }
    public void setProtocols ( String input ) {
        protocols.clear();
        for ( String value : input.split ( "(?=[-+,])" ) ) {
            String trimmed = value.trim();
            if ( trimmed.length() > 1 ) {
                if ( trimmed.charAt ( 0 ) == '+' ) {
                    trimmed = trimmed.substring ( 1 ).trim();
                    if ( trimmed.equalsIgnoreCase ( Constants.SSL_PROTO_ALL ) ) {
                        protocols.addAll ( SSL_PROTO_ALL_SET );
                    } else {
                        protocols.add ( trimmed );
                    }
                } else if ( trimmed.charAt ( 0 ) == '-' ) {
                    trimmed = trimmed.substring ( 1 ).trim();
                    if ( trimmed.equalsIgnoreCase ( Constants.SSL_PROTO_ALL ) ) {
                        protocols.removeAll ( SSL_PROTO_ALL_SET );
                    } else {
                        protocols.remove ( trimmed );
                    }
                } else {
                    if ( trimmed.charAt ( 0 ) == ',' ) {
                        trimmed = trimmed.substring ( 1 ).trim();
                    }
                    if ( !protocols.isEmpty() ) {
                        log.warn ( sm.getString ( "sslHostConfig.prefix_missing",
                                                  trimmed, getHostName() ) );
                    }
                    if ( trimmed.equalsIgnoreCase ( Constants.SSL_PROTO_ALL ) ) {
                        protocols.addAll ( SSL_PROTO_ALL_SET );
                    } else {
                        protocols.add ( trimmed );
                    }
                }
            }
        }
    }
    public Set<String> getProtocols() {
        return protocols;
    }
    public void setCertificateKeyAlias ( String certificateKeyAlias ) {
        registerDefaultCertificate();
        defaultCertificate.setCertificateKeyAlias ( certificateKeyAlias );
    }
    public void setCertificateKeystoreFile ( String certificateKeystoreFile ) {
        registerDefaultCertificate();
        defaultCertificate.setCertificateKeystoreFile ( certificateKeystoreFile );
    }
    public void setCertificateKeystorePassword ( String certificateKeystorePassword ) {
        registerDefaultCertificate();
        defaultCertificate.setCertificateKeystorePassword ( certificateKeystorePassword );
    }
    public void setCertificateKeystoreProvider ( String certificateKeystoreProvider ) {
        registerDefaultCertificate();
        defaultCertificate.setCertificateKeystoreProvider ( certificateKeystoreProvider );
    }
    public void setCertificateKeystoreType ( String certificateKeystoreType ) {
        registerDefaultCertificate();
        defaultCertificate.setCertificateKeystoreType ( certificateKeystoreType );
    }
    public void setKeyManagerAlgorithm ( String keyManagerAlgorithm ) {
        setProperty ( "keyManagerAlgorithm", Type.JSSE );
        this.keyManagerAlgorithm = keyManagerAlgorithm;
    }
    public String getKeyManagerAlgorithm() {
        return keyManagerAlgorithm;
    }
    public void setSessionCacheSize ( int sessionCacheSize ) {
        setProperty ( "sessionCacheSize", Type.JSSE );
        this.sessionCacheSize = sessionCacheSize;
    }
    public int getSessionCacheSize() {
        return sessionCacheSize;
    }
    public void setSessionTimeout ( int sessionTimeout ) {
        setProperty ( "sessionTimeout", Type.JSSE );
        this.sessionTimeout = sessionTimeout;
    }
    public int getSessionTimeout() {
        return sessionTimeout;
    }
    public void setSslProtocol ( String sslProtocol ) {
        setProperty ( "sslProtocol", Type.JSSE );
        this.sslProtocol = sslProtocol;
    }
    public String getSslProtocol() {
        return sslProtocol;
    }
    public void setTrustManagerClassName ( String trustManagerClassName ) {
        setProperty ( "trustManagerClassName", Type.JSSE );
        this.trustManagerClassName = trustManagerClassName;
    }
    public String getTrustManagerClassName() {
        return trustManagerClassName;
    }
    public void setTruststoreAlgorithm ( String truststoreAlgorithm ) {
        setProperty ( "truststoreAlgorithm", Type.JSSE );
        this.truststoreAlgorithm = truststoreAlgorithm;
    }
    public String getTruststoreAlgorithm() {
        return truststoreAlgorithm;
    }
    public void setTruststoreFile ( String truststoreFile ) {
        setProperty ( "truststoreFile", Type.JSSE );
        this.truststoreFile = truststoreFile;
    }
    public String getTruststoreFile() {
        return truststoreFile;
    }
    public void setTruststorePassword ( String truststorePassword ) {
        setProperty ( "truststorePassword", Type.JSSE );
        this.truststorePassword = truststorePassword;
    }
    public String getTruststorePassword() {
        return truststorePassword;
    }
    public void setTruststoreProvider ( String truststoreProvider ) {
        setProperty ( "truststoreProvider", Type.JSSE );
        this.truststoreProvider = truststoreProvider;
    }
    public String getTruststoreProvider() {
        if ( truststoreProvider == null ) {
            if ( defaultCertificate == null ) {
                return SSLHostConfigCertificate.DEFAULT_KEYSTORE_PROVIDER;
            } else {
                return defaultCertificate.getCertificateKeystoreProvider();
            }
        } else {
            return truststoreProvider;
        }
    }
    public void setTruststoreType ( String truststoreType ) {
        setProperty ( "truststoreType", Type.JSSE );
        this.truststoreType = truststoreType;
    }
    public String getTruststoreType() {
        if ( truststoreType == null ) {
            if ( defaultCertificate == null ) {
                return SSLHostConfigCertificate.DEFAULT_KEYSTORE_TYPE;
            } else {
                return defaultCertificate.getCertificateKeystoreType();
            }
        } else {
            return truststoreType;
        }
    }
    public void setTrustStore ( KeyStore truststore ) {
        this.truststore = truststore;
    }
    public KeyStore getTruststore() throws IOException {
        KeyStore result = truststore;
        if ( result == null ) {
            if ( truststoreFile != null ) {
                try {
                    result = SSLUtilBase.getStore ( getTruststoreType(), getTruststoreProvider(),
                                                    getTruststoreFile(), getTruststorePassword() );
                } catch ( IOException ioe ) {
                    Throwable cause = ioe.getCause();
                    if ( cause instanceof UnrecoverableKeyException ) {
                        log.warn ( sm.getString ( "jsse.invalid_truststore_password" ),
                                   cause );
                        result = SSLUtilBase.getStore ( getTruststoreType(), getTruststoreProvider(),
                                                        getTruststoreFile(), null );
                    } else {
                        throw ioe;
                    }
                }
            }
        }
        return result;
    }
    public void setCertificateChainFile ( String certificateChainFile ) {
        registerDefaultCertificate();
        defaultCertificate.setCertificateChainFile ( certificateChainFile );
    }
    public void setCertificateFile ( String certificateFile ) {
        registerDefaultCertificate();
        defaultCertificate.setCertificateFile ( certificateFile );
    }
    public void setCertificateKeyFile ( String certificateKeyFile ) {
        registerDefaultCertificate();
        defaultCertificate.setCertificateKeyFile ( certificateKeyFile );
    }
    public void setCertificateRevocationListPath ( String certificateRevocationListPath ) {
        setProperty ( "certificateRevocationListPath", Type.OPENSSL );
        this.certificateRevocationListPath = certificateRevocationListPath;
    }
    public String getCertificateRevocationListPath() {
        return certificateRevocationListPath;
    }
    public void setCaCertificateFile ( String caCertificateFile ) {
        setProperty ( "caCertificateFile", Type.OPENSSL );
        this.caCertificateFile = caCertificateFile;
    }
    public String getCaCertificateFile() {
        return caCertificateFile;
    }
    public void setCaCertificatePath ( String caCertificatePath ) {
        setProperty ( "caCertificatePath", Type.OPENSSL );
        this.caCertificatePath = caCertificatePath;
    }
    public String getCaCertificatePath() {
        return caCertificatePath;
    }
    public void setDisableCompression ( boolean disableCompression ) {
        setProperty ( "disableCompression", Type.OPENSSL );
        this.disableCompression = disableCompression;
    }
    public boolean getDisableCompression() {
        return disableCompression;
    }
    public void setDisableSessionTickets ( boolean disableSessionTickets ) {
        setProperty ( "disableSessionTickets", Type.OPENSSL );
        this.disableSessionTickets = disableSessionTickets;
    }
    public boolean getDisableSessionTickets() {
        return disableSessionTickets;
    }
    public void setInsecureRenegotiation ( boolean insecureRenegotiation ) {
        setProperty ( "insecureRenegotiation", Type.OPENSSL );
        this.insecureRenegotiation = insecureRenegotiation;
    }
    public boolean getInsecureRenegotiation() {
        return insecureRenegotiation;
    }
    public static String adjustRelativePath ( String path ) {
        if ( path == null || path.length() == 0 ) {
            return path;
        }
        String newPath = path;
        File f = new File ( newPath );
        if ( !f.isAbsolute() ) {
            newPath = System.getProperty ( Constants.CATALINA_BASE_PROP ) + File.separator + newPath;
            f = new File ( newPath );
        }
        if ( !f.exists() ) {
            log.warn ( "configured file:[" + newPath + "] does not exist." );
        }
        return newPath;
    }
    public static enum Type {
        JSSE,
        OPENSSL,
        EITHER
    }
    public static enum CertificateVerification {
        NONE,
        OPTIONAL_NO_CA,
        OPTIONAL,
        REQUIRED;
        public static CertificateVerification fromString ( String value ) {
            if ( "true".equalsIgnoreCase ( value ) ||
            "yes".equalsIgnoreCase ( value ) ||
            "require".equalsIgnoreCase ( value ) ||
            "required".equalsIgnoreCase ( value ) ) {
                return REQUIRED;
            } else if ( "optional".equalsIgnoreCase ( value ) ||
            "want".equalsIgnoreCase ( value ) ) {
                return OPTIONAL;
            } else if ( "optionalNoCA".equalsIgnoreCase ( value ) ||
                        "optional_no_ca".equalsIgnoreCase ( value ) ) {
                return OPTIONAL_NO_CA;
            } else if ( "false".equalsIgnoreCase ( value ) ||
                        "no".equalsIgnoreCase ( value ) ||
                        "none".equalsIgnoreCase ( value ) ) {
                return NONE;
            } else {
                throw new IllegalArgumentException (
                    sm.getString ( "sslHostConfig.certificateVerificationInvalid", value ) );
            }
        }
    }
}
