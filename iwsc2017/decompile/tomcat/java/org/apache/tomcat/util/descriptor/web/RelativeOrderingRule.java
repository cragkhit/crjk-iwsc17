package org.apache.tomcat.util.descriptor.web;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
final class RelativeOrderingRule extends Rule {
    boolean isRelativeOrderingSet;
    private final boolean fragment;
    public RelativeOrderingRule ( final boolean fragment ) {
        this.isRelativeOrderingSet = false;
        this.fragment = fragment;
    }
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        if ( !this.fragment ) {
            this.digester.getLogger().warn ( WebRuleSet.sm.getString ( "webRuleSet.relativeOrdering" ) );
        }
        if ( this.isRelativeOrderingSet ) {
            throw new IllegalArgumentException ( WebRuleSet.sm.getString ( "webRuleSet.relativeOrderingCount" ) );
        }
        this.isRelativeOrderingSet = true;
    }
}
