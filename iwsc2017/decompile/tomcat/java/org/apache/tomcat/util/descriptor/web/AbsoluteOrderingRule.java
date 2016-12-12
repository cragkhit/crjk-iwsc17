package org.apache.tomcat.util.descriptor.web;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
final class AbsoluteOrderingRule extends Rule {
    boolean isAbsoluteOrderingSet;
    private final boolean fragment;
    public AbsoluteOrderingRule ( final boolean fragment ) {
        this.isAbsoluteOrderingSet = false;
        this.fragment = fragment;
    }
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        if ( this.fragment ) {
            this.digester.getLogger().warn ( WebRuleSet.sm.getString ( "webRuleSet.absoluteOrdering" ) );
        }
        if ( this.isAbsoluteOrderingSet ) {
            throw new IllegalArgumentException ( WebRuleSet.sm.getString ( "webRuleSet.absoluteOrderingCount" ) );
        }
        this.isAbsoluteOrderingSet = true;
        final WebXml webXml = ( WebXml ) this.digester.peek();
        webXml.createAbsoluteOrdering();
        if ( this.digester.getLogger().isDebugEnabled() ) {
            this.digester.getLogger().debug ( webXml.getClass().getName() + ".setAbsoluteOrdering()" );
        }
    }
}
