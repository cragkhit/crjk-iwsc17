package org.apache.catalina.realm;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.digester.Rule;
final class MemoryUserRule extends Rule {
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        String username = attributes.getValue ( "username" );
        if ( username == null ) {
            username = attributes.getValue ( "name" );
        }
        final String password = attributes.getValue ( "password" );
        final String roles = attributes.getValue ( "roles" );
        final MemoryRealm realm = ( MemoryRealm ) this.digester.peek ( this.digester.getCount() - 1 );
        realm.addUser ( username, password, roles );
    }
}
