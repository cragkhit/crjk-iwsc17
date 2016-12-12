package org.apache.tomcat.util.net;
import java.security.KeyManagementException;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
public interface SSLContext {
    public void init ( KeyManager[] kms, TrustManager[] tms,
                       SecureRandom sr ) throws KeyManagementException;
    public void destroy();
    public SSLSessionContext getServerSessionContext();
    public SSLEngine createSSLEngine();
    public SSLServerSocketFactory getServerSocketFactory();
    public SSLParameters getSupportedSSLParameters();
}
