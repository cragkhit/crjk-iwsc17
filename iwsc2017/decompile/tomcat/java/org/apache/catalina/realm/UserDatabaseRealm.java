package org.apache.catalina.realm;
import javax.naming.Context;
import org.apache.catalina.LifecycleException;
import org.apache.tomcat.util.ExceptionUtils;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.catalina.Role;
import org.apache.catalina.Group;
import org.apache.catalina.User;
import java.security.Principal;
import org.apache.catalina.Wrapper;
import org.apache.catalina.UserDatabase;
public class UserDatabaseRealm extends RealmBase {
    protected UserDatabase database;
    protected static final String name = "UserDatabaseRealm";
    protected String resourceName;
    public UserDatabaseRealm() {
        this.database = null;
        this.resourceName = "UserDatabase";
    }
    public String getResourceName() {
        return this.resourceName;
    }
    public void setResourceName ( final String resourceName ) {
        this.resourceName = resourceName;
    }
    @Override
    public boolean hasRole ( final Wrapper wrapper, Principal principal, String role ) {
        if ( wrapper != null ) {
            final String realRole = wrapper.findSecurityReference ( role );
            if ( realRole != null ) {
                role = realRole;
            }
        }
        if ( principal instanceof GenericPrincipal ) {
            final GenericPrincipal gp = ( GenericPrincipal ) principal;
            if ( gp.getUserPrincipal() instanceof User ) {
                principal = gp.getUserPrincipal();
            }
        }
        if ( ! ( principal instanceof User ) ) {
            return super.hasRole ( null, principal, role );
        }
        if ( "*".equals ( role ) ) {
            return true;
        }
        if ( role == null ) {
            return false;
        }
        final User user = ( User ) principal;
        final Role dbrole = this.database.findRole ( role );
        if ( dbrole == null ) {
            return false;
        }
        if ( user.isInRole ( dbrole ) ) {
            return true;
        }
        final Iterator<Group> groups = user.getGroups();
        while ( groups.hasNext() ) {
            final Group group = groups.next();
            if ( group.isInRole ( dbrole ) ) {
                return true;
            }
        }
        return false;
    }
    @Override
    protected String getName() {
        return "UserDatabaseRealm";
    }
    @Override
    protected String getPassword ( final String username ) {
        final User user = this.database.findUser ( username );
        if ( user == null ) {
            return null;
        }
        return user.getPassword();
    }
    @Override
    protected Principal getPrincipal ( final String username ) {
        final User user = this.database.findUser ( username );
        if ( user == null ) {
            return null;
        }
        final List<String> roles = new ArrayList<String>();
        Iterator<Role> uroles = user.getRoles();
        while ( uroles.hasNext() ) {
            final Role role = uroles.next();
            roles.add ( role.getName() );
        }
        final Iterator<Group> groups = user.getGroups();
        while ( groups.hasNext() ) {
            final Group group = groups.next();
            uroles = group.getRoles();
            while ( uroles.hasNext() ) {
                final Role role2 = uroles.next();
                roles.add ( role2.getName() );
            }
        }
        return new GenericPrincipal ( username, user.getPassword(), roles, user );
    }
    @Override
    protected void startInternal() throws LifecycleException {
        try {
            final Context context = this.getServer().getGlobalNamingContext();
            this.database = ( UserDatabase ) context.lookup ( this.resourceName );
        } catch ( Throwable e ) {
            ExceptionUtils.handleThrowable ( e );
            this.containerLog.error ( UserDatabaseRealm.sm.getString ( "userDatabaseRealm.lookup", this.resourceName ), e );
            this.database = null;
        }
        if ( this.database == null ) {
            throw new LifecycleException ( UserDatabaseRealm.sm.getString ( "userDatabaseRealm.noDatabase", this.resourceName ) );
        }
        super.startInternal();
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        super.stopInternal();
        this.database = null;
    }
}
