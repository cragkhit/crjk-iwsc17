package org.apache.catalina.mbeans;
import java.util.ArrayList;
import java.util.Iterator;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;
import org.apache.catalina.Group;
import org.apache.catalina.Role;
import org.apache.catalina.User;
import org.apache.tomcat.util.modeler.BaseModelMBean;
import org.apache.tomcat.util.modeler.ManagedBean;
import org.apache.tomcat.util.modeler.Registry;
public class GroupMBean extends BaseModelMBean {
    public GroupMBean()
    throws MBeanException, RuntimeOperationsException {
        super();
    }
    protected final Registry registry = MBeanUtils.createRegistry();
    protected final ManagedBean managed = registry.findManagedBean ( "Group" );
    public String[] getRoles() {
        Group group = ( Group ) this.resource;
        ArrayList<String> results = new ArrayList<>();
        Iterator<Role> roles = group.getRoles();
        while ( roles.hasNext() ) {
            Role role = null;
            try {
                role = roles.next();
                ObjectName oname =
                    MBeanUtils.createObjectName ( managed.getDomain(), role );
                results.add ( oname.toString() );
            } catch ( MalformedObjectNameException e ) {
                IllegalArgumentException iae = new IllegalArgumentException
                ( "Cannot create object name for role " + role );
                iae.initCause ( e );
                throw iae;
            }
        }
        return results.toArray ( new String[results.size()] );
    }
    public String[] getUsers() {
        Group group = ( Group ) this.resource;
        ArrayList<String> results = new ArrayList<>();
        Iterator<User> users = group.getUsers();
        while ( users.hasNext() ) {
            User user = null;
            try {
                user = users.next();
                ObjectName oname =
                    MBeanUtils.createObjectName ( managed.getDomain(), user );
                results.add ( oname.toString() );
            } catch ( MalformedObjectNameException e ) {
                IllegalArgumentException iae = new IllegalArgumentException
                ( "Cannot create object name for user " + user );
                iae.initCause ( e );
                throw iae;
            }
        }
        return results.toArray ( new String[results.size()] );
    }
    public void addRole ( String rolename ) {
        Group group = ( Group ) this.resource;
        if ( group == null ) {
            return;
        }
        Role role = group.getUserDatabase().findRole ( rolename );
        if ( role == null ) {
            throw new IllegalArgumentException
            ( "Invalid role name '" + rolename + "'" );
        }
        group.addRole ( role );
    }
    public void removeRole ( String rolename ) {
        Group group = ( Group ) this.resource;
        if ( group == null ) {
            return;
        }
        Role role = group.getUserDatabase().findRole ( rolename );
        if ( role == null ) {
            throw new IllegalArgumentException
            ( "Invalid role name '" + rolename + "'" );
        }
        group.removeRole ( role );
    }
}
