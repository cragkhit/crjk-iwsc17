package org.apache.tomcat.util.descriptor.web;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
final class SetAuthConstraintRule extends Rule {
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        final SecurityConstraint securityConstraint = ( SecurityConstraint ) this.digester.peek();
        securityConstraint.setAuthConstraint ( true );
        if ( this.digester.getLogger().isDebugEnabled() ) {
            this.digester.getLogger().debug ( "Calling SecurityConstraint.setAuthConstraint(true)" );
        }
    }
}
