package org.apache.catalina.storeconfig;
import java.io.PrintWriter;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
public class SSLHostConfigSF extends StoreFactoryBase {
    @Override
    public void storeChildren ( PrintWriter aWriter, int indent, Object aSSLHostConfig,
                                StoreDescription parentDesc ) throws Exception {
        if ( aSSLHostConfig instanceof SSLHostConfig ) {
            SSLHostConfig sslHostConfig = ( SSLHostConfig ) aSSLHostConfig;
            SSLHostConfigCertificate[] hostConfigsCertificates = sslHostConfig.getCertificates().toArray ( new SSLHostConfigCertificate[0] );
            storeElementArray ( aWriter, indent, hostConfigsCertificates );
        }
    }
}
