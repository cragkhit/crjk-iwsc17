package org.apache.tomcat.util.net;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManager;
import java.util.List;
public interface SSLUtil {
    SSLContext createSSLContext ( List<String> p0 ) throws Exception;
    KeyManager[] getKeyManagers() throws Exception;
    TrustManager[] getTrustManagers() throws Exception;
    void configureSessionContext ( SSLSessionContext p0 );
    String[] getEnabledProtocols() throws IllegalArgumentException;
    String[] getEnabledCiphers() throws IllegalArgumentException;
    public interface ProtocolInfo {
        String getNegotiatedProtocol();
    }
}
