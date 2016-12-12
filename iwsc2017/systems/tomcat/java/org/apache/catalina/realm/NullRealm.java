package org.apache.catalina.realm;
import java.security.Principal;
public class NullRealm extends RealmBase {
    private static final String NAME = "NullRealm";
    @Override
    protected String getName() {
        return NAME;
    }
    @Override
    protected String getPassword ( String username ) {
        return null;
    }
    @Override
    protected Principal getPrincipal ( String username ) {
        return null;
    }
}
