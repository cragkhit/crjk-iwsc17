package org.apache.catalina.realm;
import java.io.Serializable;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import javax.security.auth.login.LoginContext;
import org.apache.catalina.TomcatPrincipal;
import org.ietf.jgss.GSSCredential;
public class GenericPrincipal implements TomcatPrincipal, Serializable {
    private static final long serialVersionUID = 1L;
    public GenericPrincipal ( String name, String password, List<String> roles ) {
        this ( name, password, roles, null );
    }
    public GenericPrincipal ( String name, String password, List<String> roles,
                              Principal userPrincipal ) {
        this ( name, password, roles, userPrincipal, null );
    }
    public GenericPrincipal ( String name, String password, List<String> roles,
                              Principal userPrincipal, LoginContext loginContext ) {
        this ( name, password, roles, userPrincipal, loginContext, null );
    }
    public GenericPrincipal ( String name, String password, List<String> roles,
                              Principal userPrincipal, LoginContext loginContext,
                              GSSCredential gssCredential ) {
        super();
        this.name = name;
        this.password = password;
        this.userPrincipal = userPrincipal;
        if ( roles == null ) {
            this.roles = new String[0];
        } else {
            this.roles = roles.toArray ( new String[roles.size()] );
            if ( this.roles.length > 1 ) {
                Arrays.sort ( this.roles );
            }
        }
        this.loginContext = loginContext;
        this.gssCredential = gssCredential;
    }
    protected final String name;
    @Override
    public String getName() {
        return this.name;
    }
    protected final String password;
    public String getPassword() {
        return this.password;
    }
    protected final String roles[];
    public String[] getRoles() {
        return this.roles;
    }
    protected final Principal userPrincipal;
    @Override
    public Principal getUserPrincipal() {
        if ( userPrincipal != null ) {
            return userPrincipal;
        } else {
            return this;
        }
    }
    protected final transient LoginContext loginContext;
    protected transient GSSCredential gssCredential = null;
    @Override
    public GSSCredential getGssCredential() {
        return this.gssCredential;
    }
    protected void setGssCredential ( GSSCredential gssCredential ) {
        this.gssCredential = gssCredential;
    }
    public boolean hasRole ( String role ) {
        if ( "*".equals ( role ) ) {
            return true;
        }
        if ( role == null ) {
            return false;
        }
        return Arrays.binarySearch ( roles, role ) >= 0;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "GenericPrincipal[" );
        sb.append ( this.name );
        sb.append ( "(" );
        for ( int i = 0; i < roles.length; i++ ) {
            sb.append ( roles[i] ).append ( "," );
        }
        sb.append ( ")]" );
        return sb.toString();
    }
    @Override
    public void logout() throws Exception {
        if ( loginContext != null ) {
            loginContext.logout();
        }
    }
    private Object writeReplace() {
        return new SerializablePrincipal ( name, password, roles, userPrincipal );
    }
    private static class SerializablePrincipal implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String name;
        private final String password;
        private final String[] roles;
        private final Principal principal;
        public SerializablePrincipal ( String name, String password, String[] roles,
                                       Principal principal ) {
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
            return new GenericPrincipal ( name, password, Arrays.asList ( roles ), principal );
        }
    }
}
