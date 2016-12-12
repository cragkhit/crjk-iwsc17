package org.apache.tomcat.util.descriptor.web;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
final class VersionRule extends Rule {
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        final WebXml webxml = ( WebXml ) this.digester.peek ( this.digester.getCount() - 1 );
        webxml.setVersion ( attributes.getValue ( "version" ) );
        if ( this.digester.getLogger().isDebugEnabled() ) {
            this.digester.getLogger().debug ( webxml.getClass().getName() + ".setVersion( " + webxml.getVersion() + ")" );
        }
    }
}
