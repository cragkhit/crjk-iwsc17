package org.apache.tomcat.util.descriptor.web;
import org.apache.tomcat.util.digester.Rule;
final class ServiceQnameRule extends Rule {
    @Override
    public void body ( final String namespace, final String name, final String text ) throws Exception {
        String namespaceuri = null;
        String localpart = text;
        final int colon = text.indexOf ( 58 );
        if ( colon >= 0 ) {
            final String prefix = text.substring ( 0, colon );
            namespaceuri = this.digester.findNamespaceURI ( prefix );
            localpart = text.substring ( colon + 1 );
        }
        final ContextService contextService = ( ContextService ) this.digester.peek();
        contextService.setServiceqnameLocalpart ( localpart );
        contextService.setServiceqnameNamespaceURI ( namespaceuri );
    }
}
