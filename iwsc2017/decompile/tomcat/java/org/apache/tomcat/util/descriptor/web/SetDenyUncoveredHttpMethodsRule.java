package org.apache.tomcat.util.descriptor.web;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
final class SetDenyUncoveredHttpMethodsRule extends Rule {
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        final WebXml webXml = ( WebXml ) this.digester.peek();
        webXml.setDenyUncoveredHttpMethods ( true );
        if ( this.digester.getLogger().isDebugEnabled() ) {
            this.digester.getLogger().debug ( webXml.getClass().getName() + ".setDenyUncoveredHttpMethods(true)" );
        }
    }
}
