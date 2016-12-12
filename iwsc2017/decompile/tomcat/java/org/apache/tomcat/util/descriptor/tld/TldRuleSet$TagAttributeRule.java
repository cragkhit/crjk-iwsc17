package org.apache.tomcat.util.descriptor.tld;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
private static class TagAttributeRule extends Rule {
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        final TaglibXml taglibXml = ( TaglibXml ) this.digester.peek ( this.digester.getCount() - 1 );
        this.digester.push ( new Attribute ( "1.2".equals ( taglibXml.getJspVersion() ) ) );
    }
    @Override
    public void end ( final String namespace, final String name ) throws Exception {
        final Attribute attribute = ( Attribute ) this.digester.pop();
        final TagXml tag = ( TagXml ) this.digester.peek();
        tag.getAttributes().add ( attribute.toTagAttributeInfo() );
    }
}
