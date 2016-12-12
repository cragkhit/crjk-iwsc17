package org.apache.tomcat.util.digester;
import org.apache.tomcat.util.IntrospectionUtils;
import org.xml.sax.Attributes;
public class SetPropertiesRule extends Rule {
    @Override
    public void begin ( final String namespace, final String theName, final Attributes attributes ) throws Exception {
        final Object top = this.digester.peek();
        if ( this.digester.log.isDebugEnabled() ) {
            if ( top != null ) {
                this.digester.log.debug ( "[SetPropertiesRule]{" + this.digester.match + "} Set " + top.getClass().getName() + " properties" );
            } else {
                this.digester.log.debug ( "[SetPropertiesRule]{" + this.digester.match + "} Set NULL properties" );
            }
        }
        for ( int i = 0; i < attributes.getLength(); ++i ) {
            String name = attributes.getLocalName ( i );
            if ( "".equals ( name ) ) {
                name = attributes.getQName ( i );
            }
            final String value = attributes.getValue ( i );
            if ( this.digester.log.isDebugEnabled() ) {
                this.digester.log.debug ( "[SetPropertiesRule]{" + this.digester.match + "} Setting property '" + name + "' to '" + value + "'" );
            }
            if ( !this.digester.isFakeAttribute ( top, name ) && !IntrospectionUtils.setProperty ( top, name, value ) && this.digester.getRulesValidation() ) {
                this.digester.log.warn ( "[SetPropertiesRule]{" + this.digester.match + "} Setting property '" + name + "' to '" + value + "' did not find a matching property." );
            }
        }
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "SetPropertiesRule[" );
        sb.append ( "]" );
        return sb.toString();
    }
}
