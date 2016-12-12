package org.apache.catalina.startup;
import org.apache.tomcat.util.IntrospectionUtils;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
public class SetContextPropertiesRule extends Rule {
    @Override
    public void begin ( final String namespace, final String nameX, final Attributes attributes ) throws Exception {
        for ( int i = 0; i < attributes.getLength(); ++i ) {
            String name = attributes.getLocalName ( i );
            if ( "".equals ( name ) ) {
                name = attributes.getQName ( i );
            }
            if ( !"path".equals ( name ) ) {
                if ( !"docBase".equals ( name ) ) {
                    final String value = attributes.getValue ( i );
                    if ( !this.digester.isFakeAttribute ( this.digester.peek(), name ) && !IntrospectionUtils.setProperty ( this.digester.peek(), name, value ) && this.digester.getRulesValidation() ) {
                        this.digester.getLogger().warn ( "[SetContextPropertiesRule]{" + this.digester.getMatch() + "} Setting property '" + name + "' to '" + value + "' did not find a matching property." );
                    }
                }
            }
        }
    }
}
