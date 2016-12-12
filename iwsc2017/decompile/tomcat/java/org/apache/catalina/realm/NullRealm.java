package org.apache.catalina.realm;
import java.security.Principal;
public class NullRealm extends RealmBase {
    private static final String NAME = "NullRealm";
    @Override
    protected String getName() {
        return "NullRealm";
    }
    @Override
    protected String getPassword ( final String username ) {
        return null;
    }
    @Override
    protected Principal getPrincipal ( final String username ) {
        return null;
    }
}
