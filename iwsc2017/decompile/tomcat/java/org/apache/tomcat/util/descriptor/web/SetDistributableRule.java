package org.apache.tomcat.util.descriptor.web;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
final class SetDistributableRule extends Rule {
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        final WebXml webXml = ( WebXml ) this.digester.peek();
        webXml.setDistributable ( true );
        if ( this.digester.getLogger().isDebugEnabled() ) {
            this.digester.getLogger().debug ( webXml.getClass().getName() + ".setDistributable(true)" );
        }
    }
}
