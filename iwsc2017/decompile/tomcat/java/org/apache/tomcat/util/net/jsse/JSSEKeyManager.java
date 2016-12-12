package org.apache.tomcat.util.net.jsse;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLEngine;
import java.net.Socket;
import java.security.Principal;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509ExtendedKeyManager;
public final class JSSEKeyManager extends X509ExtendedKeyManager {
    private X509KeyManager delegate;
    private String serverKeyAlias;
    public JSSEKeyManager ( final X509KeyManager mgr, final String serverKeyAlias ) {
        this.delegate = mgr;
        this.serverKeyAlias = serverKeyAlias;
    }
    @Override
    public String chooseServerAlias ( final String keyType, final Principal[] issuers, final Socket socket ) {
        if ( this.serverKeyAlias != null ) {
            return this.serverKeyAlias;
        }
        return this.delegate.chooseServerAlias ( keyType, issuers, socket );
    }
    @Override
    public String chooseEngineServerAlias ( final String keyType, final Principal[] issuers, final SSLEngine engine ) {
        if ( this.serverKeyAlias != null ) {
            return this.serverKeyAlias;
        }
        return super.chooseEngineServerAlias ( keyType, issuers, engine );
    }
    @Override
    public String chooseClientAlias ( final String[] keyType, final Principal[] issuers, final Socket socket ) {
        return this.delegate.chooseClientAlias ( keyType, issuers, socket );
    }
    @Override
    public X509Certificate[] getCertificateChain ( final String alias ) {
        return this.delegate.getCertificateChain ( alias );
    }
    @Override
    public String[] getClientAliases ( final String keyType, final Principal[] issuers ) {
        return this.delegate.getClientAliases ( keyType, issuers );
    }
    @Override
    public String[] getServerAliases ( final String keyType, final Principal[] issuers ) {
        return this.delegate.getServerAliases ( keyType, issuers );
    }
    @Override
    public PrivateKey getPrivateKey ( final String alias ) {
        return this.delegate.getPrivateKey ( alias );
    }
    @Override
    public String chooseEngineClientAlias ( final String[] keyType, final Principal[] issuers, final SSLEngine engine ) {
        return this.delegate.chooseClientAlias ( keyType, issuers, null );
    }
}
