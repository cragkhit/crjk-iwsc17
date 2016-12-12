package org.apache.tomcat.util.net;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSessionContext;
import java.security.KeyManagementException;
import java.security.SecureRandom;
import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManager;
public interface SSLContext {
    void init ( KeyManager[] p0, TrustManager[] p1, SecureRandom p2 ) throws KeyManagementException;
    void destroy();
    SSLSessionContext getServerSessionContext();
    SSLEngine createSSLEngine();
    SSLServerSocketFactory getServerSocketFactory();
    SSLParameters getSupportedSSLParameters();
}
