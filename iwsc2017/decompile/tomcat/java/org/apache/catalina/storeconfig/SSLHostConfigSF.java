package org.apache.catalina.storeconfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.apache.tomcat.util.net.SSLHostConfig;
import java.io.PrintWriter;
public class SSLHostConfigSF extends StoreFactoryBase {
    @Override
    public void storeChildren ( final PrintWriter aWriter, final int indent, final Object aSSLHostConfig, final StoreDescription parentDesc ) throws Exception {
        if ( aSSLHostConfig instanceof SSLHostConfig ) {
            final SSLHostConfig sslHostConfig = ( SSLHostConfig ) aSSLHostConfig;
            final SSLHostConfigCertificate[] hostConfigsCertificates = sslHostConfig.getCertificates().toArray ( new SSLHostConfigCertificate[0] );
            this.storeElementArray ( aWriter, indent, hostConfigsCertificates );
        }
    }
}
