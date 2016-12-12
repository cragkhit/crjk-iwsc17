package org.apache.tomcat.util.net.jsse;
import org.apache.tomcat.util.net.SSLUtil;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.apache.tomcat.util.net.SSLSupport;
import javax.net.ssl.SSLSession;
import org.apache.tomcat.util.net.SSLImplementation;
public class JSSEImplementation extends SSLImplementation {
    public JSSEImplementation() {
        JSSESupport.init();
    }
    @Override
    public SSLSupport getSSLSupport ( final SSLSession session ) {
        return new JSSESupport ( session );
    }
    @Override
    public SSLUtil getSSLUtil ( final SSLHostConfigCertificate certificate ) {
        return new JSSEUtil ( certificate );
    }
}
