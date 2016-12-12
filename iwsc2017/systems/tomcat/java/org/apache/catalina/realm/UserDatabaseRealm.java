package org.apache.catalina.realm;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.naming.Context;
import org.apache.catalina.Group;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Role;
import org.apache.catalina.User;
import org.apache.catalina.UserDatabase;
import org.apache.catalina.Wrapper;
import org.apache.tomcat.util.ExceptionUtils;
public class UserDatabaseRealm
    extends RealmBase {
    protected UserDatabase database = null;
    protected static final String name = "UserDatabaseRealm";
    protected String resourceName = "UserDatabase";
    public String getResourceName() {
        return resourceName;
    }
    public void setResourceName ( String resourceName ) {
        this.resourceName = resourceName;
    }
    @Override
    public boolean hasRole ( Wrapper wrapper, Principal principal, String role ) {
        if ( wrapper != null ) {
            String realRole = wrapper.findSecurityReference ( role );
            if ( realRole != null ) {
                role = realRole;
            }
        }
        if ( principal instanceof GenericPrincipal ) {
            GenericPrincipal gp = ( GenericPrincipal ) principal;
            if ( gp.getUserPrincipal() instanceof User ) {
                principal = gp.getUserPrincipal();
            }
        }
        if ( ! ( principal instanceof User ) ) {
            return super.hasRole ( null, principal, role );
        }
        if ( "*".equals ( role ) ) {
            return true;
        } else if ( role == null ) {
            return false;
        }
        User user = ( User ) principal;
        Role dbrole = database.findRole ( role );
        if ( dbrole == null ) {
            return false;
        }
        if ( user.isInRole ( dbrole ) ) {
            return true;
        }
        Iterator<Group> groups = user.getGroups();
        while ( groups.hasNext() ) {
            Group group = groups.next();
            if ( group.isInRole ( dbrole ) ) {
                return true;
            }
        }
        return false;
    }
    @Override
    protected String getName() {
        return ( name );
    }
    @Override
    protected String getPassword ( String username ) {
        User user = database.findUser ( username );
        if ( user == null ) {
            return null;
        }
        return ( user.getPassword() );
    }
    @Override
    protected Principal getPrincipal ( String username ) {
        User user = database.findUser ( username );
        if ( user == null ) {
            return null;
        }
        List<String> roles = new ArrayList<>();
        Iterator<Role> uroles = user.getRoles();
        while ( uroles.hasNext() ) {
            Role role = uroles.next();
            roles.add ( role.getName() );
        }
        Iterator<Group> groups = user.getGroups();
        while ( groups.hasNext() ) {
            Group group = groups.next();
            uroles = group.getRoles();
            while ( uroles.hasNext() ) {
                Role role = uroles.next();
                roles.add ( role.getName() );
            }
        }
        return new GenericPrincipal ( username, user.getPassword(), roles, user );
    }
    @Override
    protected void startInternal() throws LifecycleException {
        try {
            Context context = getServer().getGlobalNamingContext();
            database = ( UserDatabase ) context.lookup ( resourceName );
        } catch ( Throwable e ) {
            ExceptionUtils.handleThrowable ( e );
            containerLog.error ( sm.getString ( "userDatabaseRealm.lookup",
                                                resourceName ),
                                 e );
            database = null;
        }
        if ( database == null ) {
            throw new LifecycleException
            ( sm.getString ( "userDatabaseRealm.noDatabase", resourceName ) );
        }
        super.startInternal();
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        super.stopInternal();
        database = null;
    }
}
