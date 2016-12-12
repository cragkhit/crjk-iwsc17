package org.apache.catalina.startup;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.digester.Rule;
import org.xml.sax.Attributes;
public class SetContextPropertiesRule extends Rule {
    @Override
    public void begin ( String namespace, String nameX, Attributes attributes )
    throws Exception {
        for ( int i = 0; i < attributes.getLength(); i++ ) {
            String name = attributes.getLocalName ( i );
            if ( "".equals ( name ) ) {
                name = attributes.getQName ( i );
            }
            if ( "path".equals ( name ) || "docBase".equals ( name ) ) {
                continue;
            }
            String value = attributes.getValue ( i );
            if ( !digester.isFakeAttribute ( digester.peek(), name )
                    && !IntrospectionUtils.setProperty ( digester.peek(), name, value )
                    && digester.getRulesValidation() ) {
                digester.getLogger().warn ( "[SetContextPropertiesRule]{" + digester.getMatch() +
                                            "} Setting property '" + name + "' to '" +
                                            value + "' did not find a matching property." );
            }
        }
    }
}
