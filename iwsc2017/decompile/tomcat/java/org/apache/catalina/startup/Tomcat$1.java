package org.apache.catalina.startup;
import org.apache.catalina.realm.GenericPrincipal;
import java.util.List;
import java.security.Principal;
import org.apache.catalina.realm.RealmBase;
class Tomcat$1 extends RealmBase {
    @Override
    protected String getName() {
        return "Simple";
    }
    @Override
    protected String getPassword ( final String username ) {
        return Tomcat.access$000 ( Tomcat.this ).get ( username );
    }
    @Override
    protected Principal getPrincipal ( final String username ) {
        Principal p = Tomcat.access$100 ( Tomcat.this ).get ( username );
        if ( p == null ) {
            final String pass = Tomcat.access$000 ( Tomcat.this ).get ( username );
            if ( pass != null ) {
                p = new GenericPrincipal ( username, pass, Tomcat.access$200 ( Tomcat.this ).get ( username ) );
                Tomcat.access$100 ( Tomcat.this ).put ( username, p );
            }
        }
        return p;
    }
}
