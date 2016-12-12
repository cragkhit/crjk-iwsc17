package org.apache.tomcat.util.descriptor.web;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
final class ServletDefCreateRule extends Rule {
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        final ServletDef servletDef = new ServletDef();
        this.digester.push ( servletDef );
        if ( this.digester.getLogger().isDebugEnabled() ) {
            this.digester.getLogger().debug ( "new " + servletDef.getClass().getName() );
        }
    }
    @Override
    public void end ( final String namespace, final String name ) throws Exception {
        final ServletDef servletDef = ( ServletDef ) this.digester.pop();
        if ( this.digester.getLogger().isDebugEnabled() ) {
            this.digester.getLogger().debug ( "pop " + servletDef.getClass().getName() );
        }
    }
}
