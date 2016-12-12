package org.apache.tomcat.util.descriptor.web;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
final class IgnoreAnnotationsRule extends Rule {
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        final WebXml webxml = ( WebXml ) this.digester.peek ( this.digester.getCount() - 1 );
        final String value = attributes.getValue ( "metadata-complete" );
        if ( "true".equals ( value ) ) {
            webxml.setMetadataComplete ( true );
        } else if ( "false".equals ( value ) ) {
            webxml.setMetadataComplete ( false );
        }
        if ( this.digester.getLogger().isDebugEnabled() ) {
            this.digester.getLogger().debug ( webxml.getClass().getName() + ".setMetadataComplete( " + webxml.isMetadataComplete() + ")" );
        }
    }
}
