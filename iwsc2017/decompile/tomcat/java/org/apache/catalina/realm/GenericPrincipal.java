package org.apache.catalina.realm;
import java.util.Arrays;
import java.util.List;
import org.ietf.jgss.GSSCredential;
import javax.security.auth.login.LoginContext;
import java.security.Principal;
import java.io.Serializable;
import org.apache.catalina.TomcatPrincipal;
public class GenericPrincipal implements TomcatPrincipal, Serializable {
    private static final long serialVersionUID = 1L;
    protected final String name;
    protected final String password;
    protected final String[] roles;
    protected final Principal userPrincipal;
    protected final transient LoginContext loginContext;
    protected transient GSSCredential gssCredential;
    public GenericPrincipal ( final String name, final String password, final List<String> roles ) {
        this ( name, password, roles, null );
    }
    public GenericPrincipal ( final String name, final String password, final List<String> roles, final Principal userPrincipal ) {
        this ( name, password, roles, userPrincipal, null );
    }
    public GenericPrincipal ( final String name, final String password, final List<String> roles, final Principal userPrincipal, final LoginContext loginContext ) {
        this ( name, password, roles, userPrincipal, loginContext, null );
    }
    public GenericPrincipal ( final String name, final String password, final List<String> roles, final Principal userPrincipal, final LoginContext loginContext, final GSSCredential gssCredential ) {
        this.gssCredential = null;
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
    @Override
    public String getName() {
        return this.name;
    }
    public String getPassword() {
        return this.password;
    }
    public String[] getRoles() {
        return this.roles;
    }
    @Override
    public Principal getUserPrincipal() {
        if ( this.userPrincipal != null ) {
            return this.userPrincipal;
        }
        return this;
    }
    @Override
    public GSSCredential getGssCredential() {
        return this.gssCredential;
    }
    protected void setGssCredential ( final GSSCredential gssCredential ) {
        this.gssCredential = gssCredential;
    }
    public boolean hasRole ( final String role ) {
        return "*".equals ( role ) || ( role != null && Arrays.binarySearch ( this.roles, role ) >= 0 );
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "GenericPrincipal[" );
        sb.append ( this.name );
        sb.append ( "(" );
        for ( int i = 0; i < this.roles.length; ++i ) {
            sb.append ( this.roles[i] ).append ( "," );
        }
        sb.append ( ")]" );
        return sb.toString();
    }
    @Override
    public void logout() throws Exception {
        if ( this.loginContext != null ) {
            this.loginContext.logout();
        }
    }
    private Object writeReplace() {
        return new SerializablePrincipal ( this.name, this.password, this.roles, this.userPrincipal );
    }
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
}
