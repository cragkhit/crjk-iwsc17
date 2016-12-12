package org.apache.tomcat.util.descriptor.web;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
final class SetOverrideRule extends Rule {
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        final ContextEnvironment envEntry = ( ContextEnvironment ) this.digester.peek();
        envEntry.setOverride ( false );
        if ( this.digester.getLogger().isDebugEnabled() ) {
            this.digester.getLogger().debug ( envEntry.getClass().getName() + ".setOverride(false)" );
        }
    }
}
