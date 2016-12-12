package org.apache.tomcat.util.net.jsse;
import javax.net.ssl.SSLSession;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.apache.tomcat.util.net.SSLImplementation;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.SSLUtil;
public class JSSEImplementation extends SSLImplementation {
    public JSSEImplementation() {
        JSSESupport.init();
    }
    @Override
    public SSLSupport getSSLSupport ( SSLSession session ) {
        return new JSSESupport ( session );
    }
    @Override
    public SSLUtil getSSLUtil ( SSLHostConfigCertificate certificate ) {
        return new JSSEUtil ( certificate );
    }
}
