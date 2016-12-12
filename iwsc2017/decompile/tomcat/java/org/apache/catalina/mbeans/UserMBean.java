package org.apache.catalina.mbeans;
import org.apache.catalina.Role;
import javax.management.ObjectName;
import java.util.Iterator;
import javax.management.MalformedObjectNameException;
import org.apache.catalina.Group;
import java.util.ArrayList;
import org.apache.catalina.User;
import javax.management.RuntimeOperationsException;
import javax.management.MBeanException;
import org.apache.tomcat.util.modeler.ManagedBean;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.modeler.BaseModelMBean;
public class UserMBean extends BaseModelMBean {
    protected final Registry registry;
    protected final ManagedBean managed;
    public UserMBean() throws MBeanException, RuntimeOperationsException {
        this.registry = MBeanUtils.createRegistry();
        this.managed = this.registry.findManagedBean ( "User" );
    }
    public String[] getGroups() {
        final User user = ( User ) this.resource;
        final ArrayList<String> results = new ArrayList<String>();
        final Iterator<Group> groups = user.getGroups();
        while ( groups.hasNext() ) {
            Group group = null;
            try {
                group = groups.next();
                final ObjectName oname = MBeanUtils.createObjectName ( this.managed.getDomain(), group );
                results.add ( oname.toString() );
            } catch ( MalformedObjectNameException e ) {
                final IllegalArgumentException iae = new IllegalArgumentException ( "Cannot create object name for group " + group );
                iae.initCause ( e );
                throw iae;
            }
        }
        return results.toArray ( new String[results.size()] );
    }
    public String[] getRoles() {
        final User user = ( User ) this.resource;
        final ArrayList<String> results = new ArrayList<String>();
        final Iterator<Role> roles = user.getRoles();
        while ( roles.hasNext() ) {
            Role role = null;
            try {
                role = roles.next();
                final ObjectName oname = MBeanUtils.createObjectName ( this.managed.getDomain(), role );
                results.add ( oname.toString() );
            } catch ( MalformedObjectNameException e ) {
                final IllegalArgumentException iae = new IllegalArgumentException ( "Cannot create object name for role " + role );
                iae.initCause ( e );
                throw iae;
            }
        }
        return results.toArray ( new String[results.size()] );
    }
    public void addGroup ( final String groupname ) {
        final User user = ( User ) this.resource;
        if ( user == null ) {
            return;
        }
        final Group group = user.getUserDatabase().findGroup ( groupname );
        if ( group == null ) {
            throw new IllegalArgumentException ( "Invalid group name '" + groupname + "'" );
        }
        user.addGroup ( group );
    }
    public void addRole ( final String rolename ) {
        final User user = ( User ) this.resource;
        if ( user == null ) {
            return;
        }
        final Role role = user.getUserDatabase().findRole ( rolename );
        if ( role == null ) {
            throw new IllegalArgumentException ( "Invalid role name '" + rolename + "'" );
        }
        user.addRole ( role );
    }
    public void removeGroup ( final String groupname ) {
        final User user = ( User ) this.resource;
        if ( user == null ) {
            return;
        }
        final Group group = user.getUserDatabase().findGroup ( groupname );
        if ( group == null ) {
            throw new IllegalArgumentException ( "Invalid group name '" + groupname + "'" );
        }
        user.removeGroup ( group );
    }
    public void removeRole ( final String rolename ) {
        final User user = ( User ) this.resource;
        if ( user == null ) {
            return;
        }
        final Role role = user.getUserDatabase().findRole ( rolename );
        if ( role == null ) {
            throw new IllegalArgumentException ( "Invalid role name '" + rolename + "'" );
        }
        user.removeRole ( role );
    }
}
