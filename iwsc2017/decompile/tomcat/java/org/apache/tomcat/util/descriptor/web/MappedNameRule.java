package org.apache.tomcat.util.descriptor.web;
import org.apache.tomcat.util.digester.Rule;
final class MappedNameRule extends Rule {
    @Override
    public void body ( final String namespace, final String name, final String text ) throws Exception {
        final ResourceBase resourceBase = ( ResourceBase ) this.digester.peek();
        resourceBase.setProperty ( "mappedName", text.trim() );
    }
}
