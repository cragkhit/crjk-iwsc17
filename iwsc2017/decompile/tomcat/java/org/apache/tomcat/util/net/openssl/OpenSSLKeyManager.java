package org.apache.tomcat.util.net.openssl;
import java.io.File;
import javax.net.ssl.KeyManager;
public class OpenSSLKeyManager implements KeyManager {
    private File certificateChain;
    private File privateKey;
    public File getCertificateChain() {
        return this.certificateChain;
    }
    public void setCertificateChain ( final File certificateChain ) {
        this.certificateChain = certificateChain;
    }
    public File getPrivateKey() {
        return this.privateKey;
    }
    public void setPrivateKey ( final File privateKey ) {
        this.privateKey = privateKey;
    }
    OpenSSLKeyManager ( final String certChainFile, final String keyFile ) {
        if ( certChainFile == null ) {
            return;
        }
        if ( keyFile == null ) {
            return;
        }
        this.certificateChain = new File ( certChainFile );
        this.privateKey = new File ( keyFile );
    }
}
