package org.apache.tomcat.util.net.jsse;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSessionContext;
import java.security.KeyManagementException;
import java.security.SecureRandom;
import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManager;
import java.security.NoSuchAlgorithmException;
import org.apache.tomcat.util.net.SSLContext;
class JSSESSLContext implements SSLContext {
    private javax.net.ssl.SSLContext context;
    JSSESSLContext ( final String protocol ) throws NoSuchAlgorithmException {
        this.context = javax.net.ssl.SSLContext.getInstance ( protocol );
    }
    @Override
    public void init ( final KeyManager[] kms, final TrustManager[] tms, final SecureRandom sr ) throws KeyManagementException {
        this.context.init ( kms, tms, sr );
    }
    @Override
    public void destroy() {
    }
    @Override
    public SSLSessionContext getServerSessionContext() {
        return this.context.getServerSessionContext();
    }
    @Override
    public SSLEngine createSSLEngine() {
        return this.context.createSSLEngine();
    }
    @Override
    public SSLServerSocketFactory getServerSocketFactory() {
        return this.context.getServerSocketFactory();
    }
    @Override
    public SSLParameters getSupportedSSLParameters() {
        return this.context.getSupportedSSLParameters();
    }
}
