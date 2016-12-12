package org.apache.tomcat.util.digester;
import org.apache.tomcat.util.IntrospectionUtils;
import org.xml.sax.Attributes;
public class SetPropertiesRule extends Rule {
    @Override
    public void begin ( String namespace, String theName, Attributes attributes )
    throws Exception {
        Object top = digester.peek();
        if ( digester.log.isDebugEnabled() ) {
            if ( top != null ) {
                digester.log.debug ( "[SetPropertiesRule]{" + digester.match +
                                     "} Set " + top.getClass().getName() +
                                     " properties" );
            } else {
                digester.log.debug ( "[SetPropertiesRule]{" + digester.match +
                                     "} Set NULL properties" );
            }
        }
        for ( int i = 0; i < attributes.getLength(); i++ ) {
            String name = attributes.getLocalName ( i );
            if ( "".equals ( name ) ) {
                name = attributes.getQName ( i );
            }
            String value = attributes.getValue ( i );
            if ( digester.log.isDebugEnabled() ) {
                digester.log.debug ( "[SetPropertiesRule]{" + digester.match +
                                     "} Setting property '" + name + "' to '" +
                                     value + "'" );
            }
            if ( !digester.isFakeAttribute ( top, name )
                    && !IntrospectionUtils.setProperty ( top, name, value )
                    && digester.getRulesValidation() ) {
                digester.log.warn ( "[SetPropertiesRule]{" + digester.match +
                                    "} Setting property '" + name + "' to '" +
                                    value + "' did not find a matching property." );
            }
        }
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "SetPropertiesRule[" );
        sb.append ( "]" );
        return ( sb.toString() );
    }
}
