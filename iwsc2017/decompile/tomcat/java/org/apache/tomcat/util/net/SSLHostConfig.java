package org.apache.tomcat.util.net;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.apache.juli.logging.LogFactory;
import java.io.File;
import java.io.IOException;
import java.security.UnrecoverableKeyException;
import java.util.Collection;
import org.apache.tomcat.util.net.openssl.ciphers.OpenSSLCipherConfigurationParser;
import java.util.Iterator;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManagerFactory;
import java.util.HashSet;
import java.util.HashMap;
import java.security.KeyStore;
import java.util.List;
import org.apache.tomcat.util.net.openssl.ciphers.Cipher;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import java.io.Serializable;
public class SSLHostConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Log log;
    private static final StringManager sm;
    protected static final String DEFAULT_SSL_HOST_NAME = "_default_";
    protected static final Set<String> SSL_PROTO_ALL_SET;
    private Type configType;
    private Type currentConfigType;
    private Map<Type, Set<String>> configuredProperties;
    private String hostName;
    private transient Long openSslContext;
    private String[] enabledCiphers;
    private String[] enabledProtocols;
    private SSLHostConfigCertificate defaultCertificate;
    private Set<SSLHostConfigCertificate> certificates;
    private String certificateRevocationListFile;
    private CertificateVerification certificateVerification;
    private int certificateVerificationDepth;
    private String ciphers;
    private LinkedHashSet<Cipher> cipherList;
    private List<String> jsseCipherNames;
    private boolean honorCipherOrder;
    private Set<String> protocols;
    private String keyManagerAlgorithm;
    private int sessionCacheSize;
    private int sessionTimeout;
    private String sslProtocol;
    private String trustManagerClassName;
    private String truststoreAlgorithm;
    private String truststoreFile;
    private String truststorePassword;
    private String truststoreProvider;
    private String truststoreType;
    private transient KeyStore truststore;
    private String certificateRevocationListPath;
    private String caCertificateFile;
    private String caCertificatePath;
    private boolean disableCompression;
    private boolean disableSessionTickets;
    private boolean insecureRenegotiation;
    public SSLHostConfig() {
        this.configType = null;
        this.currentConfigType = null;
        this.configuredProperties = new HashMap<Type, Set<String>>();
        this.hostName = "_default_";
        this.defaultCertificate = null;
        this.certificates = new HashSet<SSLHostConfigCertificate> ( 4 );
        this.certificateVerification = CertificateVerification.NONE;
        this.certificateVerificationDepth = 10;
        this.ciphers = "HIGH:!aNULL:!eNULL:!EXPORT:!DES:!RC4:!MD5:!kRSA";
        this.cipherList = null;
        this.jsseCipherNames = null;
        this.honorCipherOrder = false;
        this.protocols = new HashSet<String>();
        this.keyManagerAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
        this.sessionCacheSize = 0;
        this.sessionTimeout = 86400;
        this.sslProtocol = "TLS";
        this.truststoreAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        this.truststoreFile = System.getProperty ( "javax.net.ssl.trustStore" );
        this.truststorePassword = System.getProperty ( "javax.net.ssl.trustStorePassword" );
        this.truststoreProvider = System.getProperty ( "javax.net.ssl.trustStoreProvider" );
        this.truststoreType = System.getProperty ( "javax.net.ssl.trustStoreType" );
        this.truststore = null;
        this.disableCompression = true;
        this.disableSessionTickets = false;
        this.insecureRenegotiation = false;
        this.setProtocols ( "all" );
    }
    public Long getOpenSslContext() {
        return this.openSslContext;
    }
    public void setOpenSslContext ( final Long openSslContext ) {
        this.openSslContext = openSslContext;
    }
    public void setConfigType ( final Type configType ) {
        this.configType = configType;
        if ( configType == Type.EITHER ) {
            if ( this.configuredProperties.remove ( Type.JSSE ) == null ) {
                this.configuredProperties.remove ( Type.OPENSSL );
            }
        } else {
            this.configuredProperties.remove ( configType );
        }
        for ( final Map.Entry<Type, Set<String>> entry : this.configuredProperties.entrySet() ) {
            for ( final String property : entry.getValue() ) {
                SSLHostConfig.log.warn ( SSLHostConfig.sm.getString ( "sslHostConfig.mismatch", property, this.getHostName(), entry.getKey(), configType ) );
            }
        }
    }
    void setProperty ( final String name, final Type configType ) {
        if ( this.configType == null ) {
            Set<String> properties = this.configuredProperties.get ( configType );
            if ( properties == null ) {
                properties = new HashSet<String>();
                this.configuredProperties.put ( configType, properties );
            }
            properties.add ( name );
        } else if ( this.configType == Type.EITHER ) {
            if ( this.currentConfigType == null ) {
                this.currentConfigType = configType;
            } else if ( this.currentConfigType != configType ) {
                SSLHostConfig.log.warn ( SSLHostConfig.sm.getString ( "sslHostConfig.mismatch", name, this.getHostName(), configType, this.currentConfigType ) );
            }
        } else if ( configType != this.configType ) {
            SSLHostConfig.log.warn ( SSLHostConfig.sm.getString ( "sslHostConfig.mismatch", name, this.getHostName(), configType, this.configType ) );
        }
    }
    public String[] getEnabledProtocols() {
        return this.enabledProtocols;
    }
    void setEnabledProtocols ( final String[] enabledProtocols ) {
        this.enabledProtocols = enabledProtocols;
    }
    public String[] getEnabledCiphers() {
        return this.enabledCiphers;
    }
    void setEnabledCiphers ( final String[] enabledCiphers ) {
        this.enabledCiphers = enabledCiphers;
    }
    private void registerDefaultCertificate() {
        if ( this.defaultCertificate == null ) {
            this.defaultCertificate = new SSLHostConfigCertificate ( this, SSLHostConfigCertificate.Type.UNDEFINED );
            this.certificates.add ( this.defaultCertificate );
        }
    }
    public void addCertificate ( final SSLHostConfigCertificate certificate ) {
        if ( this.certificates.size() == 0 ) {
            this.certificates.add ( certificate );
            return;
        }
        if ( ( this.certificates.size() == 1 && this.certificates.iterator().next().getType() == SSLHostConfigCertificate.Type.UNDEFINED ) || certificate.getType() == SSLHostConfigCertificate.Type.UNDEFINED ) {
            throw new IllegalArgumentException ( SSLHostConfig.sm.getString ( "sslHostConfig.certificate.notype" ) );
        }
        this.certificates.add ( certificate );
    }
    public Set<SSLHostConfigCertificate> getCertificates() {
        return this.getCertificates ( false );
    }
    public Set<SSLHostConfigCertificate> getCertificates ( final boolean createDefaultIfEmpty ) {
        if ( this.certificates.size() == 0 && createDefaultIfEmpty ) {
            this.registerDefaultCertificate();
        }
        return this.certificates;
    }
    public void setCertificateKeyPassword ( final String certificateKeyPassword ) {
        this.registerDefaultCertificate();
        this.defaultCertificate.setCertificateKeyPassword ( certificateKeyPassword );
    }
    public void setCertificateRevocationListFile ( final String certificateRevocationListFile ) {
        this.certificateRevocationListFile = certificateRevocationListFile;
    }
    public String getCertificateRevocationListFile() {
        return this.certificateRevocationListFile;
    }
    public void setCertificateVerification ( final String certificateVerification ) {
        this.certificateVerification = CertificateVerification.fromString ( certificateVerification );
    }
    public CertificateVerification getCertificateVerification() {
        return this.certificateVerification;
    }
    public void setCertificateVerificationDepth ( final int certificateVerificationDepth ) {
        this.certificateVerificationDepth = certificateVerificationDepth;
    }
    public int getCertificateVerificationDepth() {
        return this.certificateVerificationDepth;
    }
    public void setCiphers ( final String ciphersList ) {
        if ( ciphersList != null && !ciphersList.contains ( ":" ) ) {
            final StringBuilder sb = new StringBuilder();
            final String[] split;
            final String[] ciphers = split = ciphersList.split ( "," );
            for ( final String cipher : split ) {
                final String trimmed = cipher.trim();
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
        return this.ciphers;
    }
    public LinkedHashSet<Cipher> getCipherList() {
        if ( this.cipherList == null ) {
            this.cipherList = OpenSSLCipherConfigurationParser.parse ( this.ciphers );
        }
        return this.cipherList;
    }
    public List<String> getJsseCipherNames() {
        if ( this.jsseCipherNames == null ) {
            this.jsseCipherNames = OpenSSLCipherConfigurationParser.convertForJSSE ( this.getCipherList() );
        }
        return this.jsseCipherNames;
    }
    public void setHonorCipherOrder ( final boolean honorCipherOrder ) {
        this.honorCipherOrder = honorCipherOrder;
    }
    public boolean getHonorCipherOrder() {
        return this.honorCipherOrder;
    }
    public void setHostName ( final String hostName ) {
        this.hostName = hostName;
    }
    public String getHostName() {
        return this.hostName;
    }
    public void setProtocols ( final String input ) {
        this.protocols.clear();
        for ( final String value : input.split ( "(?=[-+,])" ) ) {
            String trimmed = value.trim();
            if ( trimmed.length() > 1 ) {
                if ( trimmed.charAt ( 0 ) == '+' ) {
                    trimmed = trimmed.substring ( 1 ).trim();
                    if ( trimmed.equalsIgnoreCase ( "all" ) ) {
                        this.protocols.addAll ( SSLHostConfig.SSL_PROTO_ALL_SET );
                    } else {
                        this.protocols.add ( trimmed );
                    }
                } else if ( trimmed.charAt ( 0 ) == '-' ) {
                    trimmed = trimmed.substring ( 1 ).trim();
                    if ( trimmed.equalsIgnoreCase ( "all" ) ) {
                        this.protocols.removeAll ( SSLHostConfig.SSL_PROTO_ALL_SET );
                    } else {
                        this.protocols.remove ( trimmed );
                    }
                } else {
                    if ( trimmed.charAt ( 0 ) == ',' ) {
                        trimmed = trimmed.substring ( 1 ).trim();
                    }
                    if ( !this.protocols.isEmpty() ) {
                        SSLHostConfig.log.warn ( SSLHostConfig.sm.getString ( "sslHostConfig.prefix_missing", trimmed, this.getHostName() ) );
                    }
                    if ( trimmed.equalsIgnoreCase ( "all" ) ) {
                        this.protocols.addAll ( SSLHostConfig.SSL_PROTO_ALL_SET );
                    } else {
                        this.protocols.add ( trimmed );
                    }
                }
            }
        }
    }
    public Set<String> getProtocols() {
        return this.protocols;
    }
    public void setCertificateKeyAlias ( final String certificateKeyAlias ) {
        this.registerDefaultCertificate();
        this.defaultCertificate.setCertificateKeyAlias ( certificateKeyAlias );
    }
    public void setCertificateKeystoreFile ( final String certificateKeystoreFile ) {
        this.registerDefaultCertificate();
        this.defaultCertificate.setCertificateKeystoreFile ( certificateKeystoreFile );
    }
    public void setCertificateKeystorePassword ( final String certificateKeystorePassword ) {
        this.registerDefaultCertificate();
        this.defaultCertificate.setCertificateKeystorePassword ( certificateKeystorePassword );
    }
    public void setCertificateKeystoreProvider ( final String certificateKeystoreProvider ) {
        this.registerDefaultCertificate();
        this.defaultCertificate.setCertificateKeystoreProvider ( certificateKeystoreProvider );
    }
    public void setCertificateKeystoreType ( final String certificateKeystoreType ) {
        this.registerDefaultCertificate();
        this.defaultCertificate.setCertificateKeystoreType ( certificateKeystoreType );
    }
    public void setKeyManagerAlgorithm ( final String keyManagerAlgorithm ) {
        this.setProperty ( "keyManagerAlgorithm", Type.JSSE );
        this.keyManagerAlgorithm = keyManagerAlgorithm;
    }
    public String getKeyManagerAlgorithm() {
        return this.keyManagerAlgorithm;
    }
    public void setSessionCacheSize ( final int sessionCacheSize ) {
        this.setProperty ( "sessionCacheSize", Type.JSSE );
        this.sessionCacheSize = sessionCacheSize;
    }
    public int getSessionCacheSize() {
        return this.sessionCacheSize;
    }
    public void setSessionTimeout ( final int sessionTimeout ) {
        this.setProperty ( "sessionTimeout", Type.JSSE );
        this.sessionTimeout = sessionTimeout;
    }
    public int getSessionTimeout() {
        return this.sessionTimeout;
    }
    public void setSslProtocol ( final String sslProtocol ) {
        this.setProperty ( "sslProtocol", Type.JSSE );
        this.sslProtocol = sslProtocol;
    }
    public String getSslProtocol() {
        return this.sslProtocol;
    }
    public void setTrustManagerClassName ( final String trustManagerClassName ) {
        this.setProperty ( "trustManagerClassName", Type.JSSE );
        this.trustManagerClassName = trustManagerClassName;
    }
    public String getTrustManagerClassName() {
        return this.trustManagerClassName;
    }
    public void setTruststoreAlgorithm ( final String truststoreAlgorithm ) {
        this.setProperty ( "truststoreAlgorithm", Type.JSSE );
        this.truststoreAlgorithm = truststoreAlgorithm;
    }
    public String getTruststoreAlgorithm() {
        return this.truststoreAlgorithm;
    }
    public void setTruststoreFile ( final String truststoreFile ) {
        this.setProperty ( "truststoreFile", Type.JSSE );
        this.truststoreFile = truststoreFile;
    }
    public String getTruststoreFile() {
        return this.truststoreFile;
    }
    public void setTruststorePassword ( final String truststorePassword ) {
        this.setProperty ( "truststorePassword", Type.JSSE );
        this.truststorePassword = truststorePassword;
    }
    public String getTruststorePassword() {
        return this.truststorePassword;
    }
    public void setTruststoreProvider ( final String truststoreProvider ) {
        this.setProperty ( "truststoreProvider", Type.JSSE );
        this.truststoreProvider = truststoreProvider;
    }
    public String getTruststoreProvider() {
        if ( this.truststoreProvider != null ) {
            return this.truststoreProvider;
        }
        if ( this.defaultCertificate == null ) {
            return SSLHostConfigCertificate.DEFAULT_KEYSTORE_PROVIDER;
        }
        return this.defaultCertificate.getCertificateKeystoreProvider();
    }
    public void setTruststoreType ( final String truststoreType ) {
        this.setProperty ( "truststoreType", Type.JSSE );
        this.truststoreType = truststoreType;
    }
    public String getTruststoreType() {
        if ( this.truststoreType != null ) {
            return this.truststoreType;
        }
        if ( this.defaultCertificate == null ) {
            return SSLHostConfigCertificate.DEFAULT_KEYSTORE_TYPE;
        }
        return this.defaultCertificate.getCertificateKeystoreType();
    }
    public void setTrustStore ( final KeyStore truststore ) {
        this.truststore = truststore;
    }
    public KeyStore getTruststore() throws IOException {
        KeyStore result = this.truststore;
        if ( result == null && this.truststoreFile != null ) {
            try {
                result = SSLUtilBase.getStore ( this.getTruststoreType(), this.getTruststoreProvider(), this.getTruststoreFile(), this.getTruststorePassword() );
            } catch ( IOException ioe ) {
                final Throwable cause = ioe.getCause();
                if ( ! ( cause instanceof UnrecoverableKeyException ) ) {
                    throw ioe;
                }
                SSLHostConfig.log.warn ( SSLHostConfig.sm.getString ( "jsse.invalid_truststore_password" ), cause );
                result = SSLUtilBase.getStore ( this.getTruststoreType(), this.getTruststoreProvider(), this.getTruststoreFile(), null );
            }
        }
        return result;
    }
    public void setCertificateChainFile ( final String certificateChainFile ) {
        this.registerDefaultCertificate();
        this.defaultCertificate.setCertificateChainFile ( certificateChainFile );
    }
    public void setCertificateFile ( final String certificateFile ) {
        this.registerDefaultCertificate();
        this.defaultCertificate.setCertificateFile ( certificateFile );
    }
    public void setCertificateKeyFile ( final String certificateKeyFile ) {
        this.registerDefaultCertificate();
        this.defaultCertificate.setCertificateKeyFile ( certificateKeyFile );
    }
    public void setCertificateRevocationListPath ( final String certificateRevocationListPath ) {
        this.setProperty ( "certificateRevocationListPath", Type.OPENSSL );
        this.certificateRevocationListPath = certificateRevocationListPath;
    }
    public String getCertificateRevocationListPath() {
        return this.certificateRevocationListPath;
    }
    public void setCaCertificateFile ( final String caCertificateFile ) {
        this.setProperty ( "caCertificateFile", Type.OPENSSL );
        this.caCertificateFile = caCertificateFile;
    }
    public String getCaCertificateFile() {
        return this.caCertificateFile;
    }
    public void setCaCertificatePath ( final String caCertificatePath ) {
        this.setProperty ( "caCertificatePath", Type.OPENSSL );
        this.caCertificatePath = caCertificatePath;
    }
    public String getCaCertificatePath() {
        return this.caCertificatePath;
    }
    public void setDisableCompression ( final boolean disableCompression ) {
        this.setProperty ( "disableCompression", Type.OPENSSL );
        this.disableCompression = disableCompression;
    }
    public boolean getDisableCompression() {
        return this.disableCompression;
    }
    public void setDisableSessionTickets ( final boolean disableSessionTickets ) {
        this.setProperty ( "disableSessionTickets", Type.OPENSSL );
        this.disableSessionTickets = disableSessionTickets;
    }
    public boolean getDisableSessionTickets() {
        return this.disableSessionTickets;
    }
    public void setInsecureRenegotiation ( final boolean insecureRenegotiation ) {
        this.setProperty ( "insecureRenegotiation", Type.OPENSSL );
        this.insecureRenegotiation = insecureRenegotiation;
    }
    public boolean getInsecureRenegotiation() {
        return this.insecureRenegotiation;
    }
    public static String adjustRelativePath ( final String path ) {
        if ( path == null || path.length() == 0 ) {
            return path;
        }
        String newPath = path;
        File f = new File ( newPath );
        if ( !f.isAbsolute() ) {
            newPath = System.getProperty ( "catalina.base" ) + File.separator + newPath;
            f = new File ( newPath );
        }
        if ( !f.exists() ) {
            SSLHostConfig.log.warn ( "configured file:[" + newPath + "] does not exist." );
        }
        return newPath;
    }
    static {
        log = LogFactory.getLog ( SSLHostConfig.class );
        sm = StringManager.getManager ( SSLHostConfig.class );
        ( SSL_PROTO_ALL_SET = new HashSet<String>() ).add ( "SSLv2Hello" );
        SSLHostConfig.SSL_PROTO_ALL_SET.add ( "TLSv1" );
        SSLHostConfig.SSL_PROTO_ALL_SET.add ( "TLSv1.1" );
        SSLHostConfig.SSL_PROTO_ALL_SET.add ( "TLSv1.2" );
    }
    public enum Type {
        JSSE,
        OPENSSL,
        EITHER;
    }
    public enum CertificateVerification {
        NONE,
        OPTIONAL_NO_CA,
        OPTIONAL,
        REQUIRED;
        public static CertificateVerification fromString ( final String value ) {
            if ( "true".equalsIgnoreCase ( value ) || "yes".equalsIgnoreCase ( value ) || "require".equalsIgnoreCase ( value ) || "required".equalsIgnoreCase ( value ) ) {
                return CertificateVerification.REQUIRED;
            }
            if ( "optional".equalsIgnoreCase ( value ) || "want".equalsIgnoreCase ( value ) ) {
                return CertificateVerification.OPTIONAL;
            }
            if ( "optionalNoCA".equalsIgnoreCase ( value ) || "optional_no_ca".equalsIgnoreCase ( value ) ) {
                return CertificateVerification.OPTIONAL_NO_CA;
            }
            if ( "false".equalsIgnoreCase ( value ) || "no".equalsIgnoreCase ( value ) || "none".equalsIgnoreCase ( value ) ) {
                return CertificateVerification.NONE;
            }
            throw new IllegalArgumentException ( SSLHostConfig.sm.getString ( "sslHostConfig.certificateVerificationInvalid", value ) );
        }
    }
}
