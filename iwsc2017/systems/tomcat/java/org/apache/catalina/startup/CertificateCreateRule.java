package org.apache.catalina.startup;
import org.apache.tomcat.util.digester.Rule;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.apache.tomcat.util.net.SSLHostConfigCertificate.Type;
import org.xml.sax.Attributes;
public class CertificateCreateRule extends Rule {
    @Override
    public void begin ( String namespace, String name, Attributes attributes ) throws Exception {
        SSLHostConfig sslHostConfig = ( SSLHostConfig ) digester.peek();
        Type type;
        String typeValue = attributes.getValue ( "type" );
        if ( typeValue == null || typeValue.length() == 0 ) {
            type = Type.UNDEFINED;
        } else {
            type = Type.valueOf ( typeValue );
        }
        SSLHostConfigCertificate certificate = new SSLHostConfigCertificate ( sslHostConfig, type );
        digester.push ( certificate );
    }
    @Override
    public void end ( String namespace, String name ) throws Exception {
        digester.pop();
    }
}
