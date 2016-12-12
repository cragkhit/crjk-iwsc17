package org.apache.catalina.connector;
import java.io.Serializable;
import java.security.Principal;
public class CoyotePrincipal implements Principal, Serializable {
    private static final long serialVersionUID = 1L;
    public CoyotePrincipal ( String name ) {
        this.name = name;
    }
    protected final String name;
    @Override
    public String getName() {
        return ( this.name );
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "CoyotePrincipal[" );
        sb.append ( this.name );
        sb.append ( "]" );
        return ( sb.toString() );
    }
}
