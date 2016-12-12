package org.apache.tomcat.util.descriptor.tld;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
private static class ElementNotAllowedRule extends Rule {
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        throw new IllegalArgumentException ( ImplicitTldRuleSet.access$100().getString ( "implicitTldRule.elementNotAllowed", name ) );
    }
}
