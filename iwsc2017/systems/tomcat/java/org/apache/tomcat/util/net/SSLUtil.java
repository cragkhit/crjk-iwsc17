package org.apache.tomcat.util.net;
import java.util.List;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
public interface SSLUtil {
    public SSLContext createSSLContext ( List<String> negotiableProtocols ) throws Exception;
    public KeyManager[] getKeyManagers() throws Exception;
    public TrustManager[] getTrustManagers() throws Exception;
    public void configureSessionContext ( SSLSessionContext sslSessionContext );
    public String[] getEnabledProtocols() throws IllegalArgumentException;
    public String[] getEnabledCiphers() throws IllegalArgumentException;
    public interface ProtocolInfo {
        public String getNegotiatedProtocol();
    }
}
