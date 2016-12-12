package org.apache.tomcat.util.descriptor.tld;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
class ImplicitTldRuleSet$1 extends Rule {
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) {
        final TaglibXml taglibXml = ( TaglibXml ) this.digester.peek();
        taglibXml.setJspVersion ( attributes.getValue ( "version" ) );
    }
}
