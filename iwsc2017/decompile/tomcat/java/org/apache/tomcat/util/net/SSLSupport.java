package org.apache.tomcat.util.net;
import java.security.cert.X509Certificate;
import java.io.IOException;
public interface SSLSupport {
    public static final String CIPHER_SUITE_KEY = "javax.servlet.request.cipher_suite";
    public static final String KEY_SIZE_KEY = "javax.servlet.request.key_size";
    public static final String CERTIFICATE_KEY = "javax.servlet.request.X509Certificate";
    public static final String SESSION_ID_KEY = "javax.servlet.request.ssl_session_id";
    public static final String SESSION_MGR = "javax.servlet.request.ssl_session_mgr";
    public static final String PROTOCOL_VERSION_KEY = "org.apache.tomcat.util.net.secure_protocol_version";
    String getCipherSuite() throws IOException;
    X509Certificate[] getPeerCertificateChain() throws IOException;
    Integer getKeySize() throws IOException;
    String getSessionId() throws IOException;
    String getProtocol() throws IOException;
}
