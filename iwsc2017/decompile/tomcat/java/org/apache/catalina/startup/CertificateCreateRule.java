package org.apache.catalina.startup;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
public class CertificateCreateRule extends Rule {
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        final SSLHostConfig sslHostConfig = ( SSLHostConfig ) this.digester.peek();
        final String typeValue = attributes.getValue ( "type" );
        SSLHostConfigCertificate.Type type;
        if ( typeValue == null || typeValue.length() == 0 ) {
            type = SSLHostConfigCertificate.Type.UNDEFINED;
        } else {
            type = SSLHostConfigCertificate.Type.valueOf ( typeValue );
        }
        final SSLHostConfigCertificate certificate = new SSLHostConfigCertificate ( sslHostConfig, type );
        this.digester.push ( certificate );
    }
    @Override
    public void end ( final String namespace, final String name ) throws Exception {
        this.digester.pop();
    }
}
