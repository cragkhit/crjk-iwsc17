package org.apache.tomcat.util.net;
import java.io.IOException;
import java.io.Serializable;
import java.security.KeyStore;
import java.util.HashSet;
import java.util.Set;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.apache.tomcat.util.res.StringManager;
public class SSLHostConfigCertificate implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog ( SSLHostConfigCertificate.class );
    private static final StringManager sm = StringManager.getManager ( SSLHostConfigCertificate.class );
    public static final Type DEFAULT_TYPE = Type.UNDEFINED;
    static final String DEFAULT_KEYSTORE_PROVIDER =
        System.getProperty ( "javax.net.ssl.keyStoreProvider" );
    static final String DEFAULT_KEYSTORE_TYPE =
        System.getProperty ( "javax.net.ssl.keyStoreType", "JKS" );
    private transient SSLContext sslContext;
    private final SSLHostConfig sslHostConfig;
    private final Type type;
    private String certificateKeyPassword = null;
    private String certificateKeyAlias;
    private String certificateKeystorePassword = "changeit";
    private String certificateKeystoreFile = System.getProperty ( "user.home" ) + "/.keystore";
    private String certificateKeystoreProvider = DEFAULT_KEYSTORE_PROVIDER;
    private String certificateKeystoreType = DEFAULT_KEYSTORE_TYPE;
    private transient KeyStore certificateKeystore = null;
    private String certificateChainFile;
    private String certificateFile;
    private String certificateKeyFile;
    private StoreType storeType = null;
    public SSLHostConfigCertificate() {
        this ( null, Type.UNDEFINED );
    }
    public SSLHostConfigCertificate ( SSLHostConfig sslHostConfig, Type type ) {
        this.sslHostConfig = sslHostConfig;
        this.type = type;
    }
    public SSLContext getSslContext() {
        return sslContext;
    }
    public void setSslContext ( SSLContext sslContext ) {
        this.sslContext = sslContext;
    }
    public SSLHostConfig getSSLHostConfig() {
        return sslHostConfig;
    }
    public Type getType() {
        return type;
    }
    public String getCertificateKeyPassword() {
        return certificateKeyPassword;
    }
    public void setCertificateKeyPassword ( String certificateKeyPassword ) {
        this.certificateKeyPassword = certificateKeyPassword;
    }
    public void setCertificateKeyAlias ( String certificateKeyAlias ) {
        sslHostConfig.setProperty (
            "Certificate.certificateKeyAlias", SSLHostConfig.Type.JSSE );
        this.certificateKeyAlias = certificateKeyAlias;
    }
    public String getCertificateKeyAlias() {
        return certificateKeyAlias;
    }
    public void setCertificateKeystoreFile ( String certificateKeystoreFile ) {
        sslHostConfig.setProperty (
            "Certificate.certificateKeystoreFile", SSLHostConfig.Type.JSSE );
        setStoreType ( "Certificate.certificateKeystoreFile", StoreType.KEYSTORE );
        this.certificateKeystoreFile = certificateKeystoreFile;
    }
    public String getCertificateKeystoreFile() {
        return certificateKeystoreFile;
    }
    public void setCertificateKeystorePassword ( String certificateKeystorePassword ) {
        sslHostConfig.setProperty (
            "Certificate.certificateKeystorePassword", SSLHostConfig.Type.JSSE );
        setStoreType ( "Certificate.certificateKeystorePassword", StoreType.KEYSTORE );
        this.certificateKeystorePassword = certificateKeystorePassword;
    }
    public String getCertificateKeystorePassword() {
        return certificateKeystorePassword;
    }
    public void setCertificateKeystoreProvider ( String certificateKeystoreProvider ) {
        sslHostConfig.setProperty (
            "Certificate.certificateKeystoreProvider", SSLHostConfig.Type.JSSE );
        setStoreType ( "Certificate.certificateKeystoreProvider", StoreType.KEYSTORE );
        this.certificateKeystoreProvider = certificateKeystoreProvider;
    }
    public String getCertificateKeystoreProvider() {
        return certificateKeystoreProvider;
    }
    public void setCertificateKeystoreType ( String certificateKeystoreType ) {
        sslHostConfig.setProperty (
            "Certificate.certificateKeystoreType", SSLHostConfig.Type.JSSE );
        setStoreType ( "Certificate.certificateKeystoreType", StoreType.KEYSTORE );
        this.certificateKeystoreType = certificateKeystoreType;
    }
    public String getCertificateKeystoreType() {
        return certificateKeystoreType;
    }
    public void setCertificateKeystore ( KeyStore certificateKeystore ) {
        this.certificateKeystore = certificateKeystore;
    }
    public KeyStore getCertificateKeystore() throws IOException {
        KeyStore result = certificateKeystore;
        if ( result == null && storeType == StoreType.KEYSTORE ) {
            result = SSLUtilBase.getStore ( getCertificateKeystoreType(),
                                            getCertificateKeystoreProvider(), getCertificateKeystoreFile(),
                                            getCertificateKeystorePassword() );
        }
        return result;
    }
    public void setCertificateChainFile ( String certificateChainFile ) {
        setStoreType ( "Certificate.certificateChainFile", StoreType.PEM );
        this.certificateChainFile = certificateChainFile;
    }
    public String getCertificateChainFile() {
        return certificateChainFile;
    }
    public void setCertificateFile ( String certificateFile ) {
        setStoreType ( "Certificate.certificateFile", StoreType.PEM );
        this.certificateFile = certificateFile;
    }
    public String getCertificateFile() {
        return certificateFile;
    }
    public void setCertificateKeyFile ( String certificateKeyFile ) {
        setStoreType ( "Certificate.certificateKeyFile", StoreType.PEM );
        this.certificateKeyFile = certificateKeyFile;
    }
    public String getCertificateKeyFile() {
        return certificateKeyFile;
    }
    private void setStoreType ( String name, StoreType type ) {
        if ( storeType == null ) {
            storeType = type;
        } else if ( storeType != type ) {
            log.warn ( sm.getString ( "sslHostConfigCertificate.mismatch",
                                      name, sslHostConfig.getHostName(), type, this.storeType ) );
        }
    }
    public static enum Type {
        UNDEFINED,
        RSA ( Authentication.RSA ),
        DSA ( Authentication.DSS ),
        EC ( Authentication.ECDH, Authentication.ECDSA );
        private final Set<Authentication> compatibleAuthentications;
        private Type ( Authentication... authentications ) {
            compatibleAuthentications = new HashSet<>();
            if ( authentications != null ) {
                for ( Authentication authentication : authentications ) {
                    compatibleAuthentications.add ( authentication );
                }
            }
        }
        public boolean isCompatibleWith ( Authentication au ) {
            return compatibleAuthentications.contains ( au );
        }
    }
    private enum StoreType {
        KEYSTORE,
        PEM
    }
}