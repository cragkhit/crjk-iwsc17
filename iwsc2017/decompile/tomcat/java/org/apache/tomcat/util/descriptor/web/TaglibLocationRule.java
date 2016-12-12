package org.apache.tomcat.util.descriptor.web;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
final class TaglibLocationRule extends Rule {
    final boolean isServlet24OrLater;
    public TaglibLocationRule ( final boolean isServlet24OrLater ) {
        this.isServlet24OrLater = isServlet24OrLater;
    }
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        final WebXml webXml = ( WebXml ) this.digester.peek ( this.digester.getCount() - 1 );
        final boolean havePublicId = webXml.getPublicId() != null;
        if ( havePublicId == this.isServlet24OrLater ) {
            throw new IllegalArgumentException ( "taglib definition not consistent with specification version" );
        }
    }
}
