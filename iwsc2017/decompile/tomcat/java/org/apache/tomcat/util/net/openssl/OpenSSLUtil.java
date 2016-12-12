package org.apache.tomcat.util.net.openssl;
import org.apache.juli.logging.LogFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import org.apache.tomcat.util.net.SSLHostConfig;
import javax.net.ssl.KeyManager;
import org.apache.tomcat.util.net.SSLContext;
import java.util.List;
import java.util.Set;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.apache.tomcat.util.net.jsse.JSSEUtil;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.net.SSLUtilBase;
public class OpenSSLUtil extends SSLUtilBase {
    private static final Log log;
    private final JSSEUtil jsseUtil;
    public OpenSSLUtil ( final SSLHostConfigCertificate certificate ) {
        super ( certificate );
        if ( certificate.getCertificateFile() == null ) {
            this.jsseUtil = new JSSEUtil ( certificate );
        } else {
            this.jsseUtil = null;
        }
    }
    @Override
    protected Log getLog() {
        return OpenSSLUtil.log;
    }
    @Override
    protected Set<String> getImplementedProtocols() {
        return OpenSSLEngine.IMPLEMENTED_PROTOCOLS_SET;
    }
    @Override
    protected Set<String> getImplementedCiphers() {
        return OpenSSLEngine.AVAILABLE_CIPHER_SUITES;
    }
    @Override
    public SSLContext createSSLContext ( final List<String> negotiableProtocols ) throws Exception {
        return new OpenSSLContext ( this.certificate, negotiableProtocols );
    }
    @Override
    public KeyManager[] getKeyManagers() throws Exception {
        if ( this.jsseUtil != null ) {
            return this.jsseUtil.getKeyManagers();
        }
        final KeyManager[] managers = { new OpenSSLKeyManager ( SSLHostConfig.adjustRelativePath ( this.certificate.getCertificateFile() ), SSLHostConfig.adjustRelativePath ( this.certificate.getCertificateKeyFile() ) ) };
        return managers;
    }
    @Override
    public TrustManager[] getTrustManagers() throws Exception {
        if ( this.jsseUtil != null ) {
            return this.jsseUtil.getTrustManagers();
        }
        return null;
    }
    @Override
    public void configureSessionContext ( final SSLSessionContext sslSessionContext ) {
        if ( this.jsseUtil != null ) {
            this.jsseUtil.configureSessionContext ( sslSessionContext );
        }
    }
    static {
        log = LogFactory.getLog ( OpenSSLUtil.class );
    }
}
