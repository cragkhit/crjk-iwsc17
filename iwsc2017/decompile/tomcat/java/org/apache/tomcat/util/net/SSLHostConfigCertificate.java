package org.apache.tomcat.util.net;
import java.util.HashSet;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import java.util.Set;
import org.apache.juli.logging.LogFactory;
import java.io.IOException;
import java.security.KeyStore;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import java.io.Serializable;
public class SSLHostConfigCertificate implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Log log;
    private static final StringManager sm;
    public static final Type DEFAULT_TYPE;
    static final String DEFAULT_KEYSTORE_PROVIDER;
    static final String DEFAULT_KEYSTORE_TYPE;
    private transient SSLContext sslContext;
    private final SSLHostConfig sslHostConfig;
    private final Type type;
    private String certificateKeyPassword;
    private String certificateKeyAlias;
    private String certificateKeystorePassword;
    private String certificateKeystoreFile;
    private String certificateKeystoreProvider;
    private String certificateKeystoreType;
    private transient KeyStore certificateKeystore;
    private String certificateChainFile;
    private String certificateFile;
    private String certificateKeyFile;
    private StoreType storeType;
    public SSLHostConfigCertificate() {
        this ( null, Type.UNDEFINED );
    }
    public SSLHostConfigCertificate ( final SSLHostConfig sslHostConfig, final Type type ) {
        this.certificateKeyPassword = null;
        this.certificateKeystorePassword = "changeit";
        this.certificateKeystoreFile = System.getProperty ( "user.home" ) + "/.keystore";
        this.certificateKeystoreProvider = SSLHostConfigCertificate.DEFAULT_KEYSTORE_PROVIDER;
        this.certificateKeystoreType = SSLHostConfigCertificate.DEFAULT_KEYSTORE_TYPE;
        this.certificateKeystore = null;
        this.storeType = null;
        this.sslHostConfig = sslHostConfig;
        this.type = type;
    }
    public SSLContext getSslContext() {
        return this.sslContext;
    }
    public void setSslContext ( final SSLContext sslContext ) {
        this.sslContext = sslContext;
    }
    public SSLHostConfig getSSLHostConfig() {
        return this.sslHostConfig;
    }
    public Type getType() {
        return this.type;
    }
    public String getCertificateKeyPassword() {
        return this.certificateKeyPassword;
    }
    public void setCertificateKeyPassword ( final String certificateKeyPassword ) {
        this.certificateKeyPassword = certificateKeyPassword;
    }
    public void setCertificateKeyAlias ( final String certificateKeyAlias ) {
        this.sslHostConfig.setProperty ( "Certificate.certificateKeyAlias", SSLHostConfig.Type.JSSE );
        this.certificateKeyAlias = certificateKeyAlias;
    }
    public String getCertificateKeyAlias() {
        return this.certificateKeyAlias;
    }
    public void setCertificateKeystoreFile ( final String certificateKeystoreFile ) {
        this.sslHostConfig.setProperty ( "Certificate.certificateKeystoreFile", SSLHostConfig.Type.JSSE );
        this.setStoreType ( "Certificate.certificateKeystoreFile", StoreType.KEYSTORE );
        this.certificateKeystoreFile = certificateKeystoreFile;
    }
    public String getCertificateKeystoreFile() {
        return this.certificateKeystoreFile;
    }
    public void setCertificateKeystorePassword ( final String certificateKeystorePassword ) {
        this.sslHostConfig.setProperty ( "Certificate.certificateKeystorePassword", SSLHostConfig.Type.JSSE );
        this.setStoreType ( "Certificate.certificateKeystorePassword", StoreType.KEYSTORE );
        this.certificateKeystorePassword = certificateKeystorePassword;
    }
    public String getCertificateKeystorePassword() {
        return this.certificateKeystorePassword;
    }
    public void setCertificateKeystoreProvider ( final String certificateKeystoreProvider ) {
        this.sslHostConfig.setProperty ( "Certificate.certificateKeystoreProvider", SSLHostConfig.Type.JSSE );
        this.setStoreType ( "Certificate.certificateKeystoreProvider", StoreType.KEYSTORE );
        this.certificateKeystoreProvider = certificateKeystoreProvider;
    }
    public String getCertificateKeystoreProvider() {
        return this.certificateKeystoreProvider;
    }
    public void setCertificateKeystoreType ( final String certificateKeystoreType ) {
        this.sslHostConfig.setProperty ( "Certificate.certificateKeystoreType", SSLHostConfig.Type.JSSE );
        this.setStoreType ( "Certificate.certificateKeystoreType", StoreType.KEYSTORE );
        this.certificateKeystoreType = certificateKeystoreType;
    }
    public String getCertificateKeystoreType() {
        return this.certificateKeystoreType;
    }
    public void setCertificateKeystore ( final KeyStore certificateKeystore ) {
        this.certificateKeystore = certificateKeystore;
    }
    public KeyStore getCertificateKeystore() throws IOException {
        KeyStore result = this.certificateKeystore;
        if ( result == null && this.storeType == StoreType.KEYSTORE ) {
            result = SSLUtilBase.getStore ( this.getCertificateKeystoreType(), this.getCertificateKeystoreProvider(), this.getCertificateKeystoreFile(), this.getCertificateKeystorePassword() );
        }
        return result;
    }
    public void setCertificateChainFile ( final String certificateChainFile ) {
        this.setStoreType ( "Certificate.certificateChainFile", StoreType.PEM );
        this.certificateChainFile = certificateChainFile;
    }
    public String getCertificateChainFile() {
        return this.certificateChainFile;
    }
    public void setCertificateFile ( final String certificateFile ) {
        this.setStoreType ( "Certificate.certificateFile", StoreType.PEM );
        this.certificateFile = certificateFile;
    }
    public String getCertificateFile() {
        return this.certificateFile;
    }
    public void setCertificateKeyFile ( final String certificateKeyFile ) {
        this.setStoreType ( "Certificate.certificateKeyFile", StoreType.PEM );
        this.certificateKeyFile = certificateKeyFile;
    }
    public String getCertificateKeyFile() {
        return this.certificateKeyFile;
    }
    private void setStoreType ( final String name, final StoreType type ) {
        if ( this.storeType == null ) {
            this.storeType = type;
        } else if ( this.storeType != type ) {
            SSLHostConfigCertificate.log.warn ( SSLHostConfigCertificate.sm.getString ( "sslHostConfigCertificate.mismatch", name, this.sslHostConfig.getHostName(), type, this.storeType ) );
        }
    }
    static {
        log = LogFactory.getLog ( SSLHostConfigCertificate.class );
        sm = StringManager.getManager ( SSLHostConfigCertificate.class );
        DEFAULT_TYPE = Type.UNDEFINED;
        DEFAULT_KEYSTORE_PROVIDER = System.getProperty ( "javax.net.ssl.keyStoreProvider" );
        DEFAULT_KEYSTORE_TYPE = System.getProperty ( "javax.net.ssl.keyStoreType", "JKS" );
    }
    public enum Type {
        UNDEFINED ( new Authentication[0] ),
        RSA ( new Authentication[] { Authentication.RSA } ),
        DSA ( new Authentication[] { Authentication.DSS } ),
        EC ( new Authentication[] { Authentication.ECDH, Authentication.ECDSA } );
        private final Set<Authentication> compatibleAuthentications;
        private Type ( final Authentication[] authentications ) {
            this.compatibleAuthentications = new HashSet<Authentication>();
            if ( authentications != null ) {
                for ( final Authentication authentication : authentications ) {
                    this.compatibleAuthentications.add ( authentication );
                }
            }
        }
        public boolean isCompatibleWith ( final Authentication au ) {
            return this.compatibleAuthentications.contains ( au );
        }
    }
    private enum StoreType {
        KEYSTORE,
        PEM;
    }
}
