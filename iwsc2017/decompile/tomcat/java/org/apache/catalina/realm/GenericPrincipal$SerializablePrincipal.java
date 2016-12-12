package org.apache.catalina.realm;
import java.util.Arrays;
import java.security.Principal;
import java.io.Serializable;
private static class SerializablePrincipal implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final String password;
    private final String[] roles;
    private final Principal principal;
    public SerializablePrincipal ( final String name, final String password, final String[] roles, final Principal principal ) {
        this.name = name;
        this.password = password;
        this.roles = roles;
        if ( principal instanceof Serializable ) {
            this.principal = principal;
        } else {
            this.principal = null;
        }
    }
    private Object readResolve() {
        return new GenericPrincipal ( this.name, this.password, Arrays.asList ( this.roles ), this.principal );
    }
}
