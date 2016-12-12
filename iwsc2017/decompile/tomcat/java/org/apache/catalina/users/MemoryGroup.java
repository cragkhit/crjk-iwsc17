package org.apache.catalina.users;
import org.apache.catalina.Group;
import org.apache.catalina.User;
import org.apache.catalina.UserDatabase;
import java.util.Iterator;
import org.apache.catalina.Role;
import java.util.ArrayList;
public class MemoryGroup extends AbstractGroup {
    protected final MemoryUserDatabase database;
    protected final ArrayList<Role> roles;
    MemoryGroup ( final MemoryUserDatabase database, final String groupname, final String description ) {
        this.roles = new ArrayList<Role>();
        this.database = database;
        this.setGroupname ( groupname );
        this.setDescription ( description );
    }
    @Override
    public Iterator<Role> getRoles() {
        synchronized ( this.roles ) {
            return this.roles.iterator();
        }
    }
    @Override
    public UserDatabase getUserDatabase() {
        return this.database;
    }
    @Override
    public Iterator<User> getUsers() {
        final ArrayList<User> results = new ArrayList<User>();
        final Iterator<User> users = this.database.getUsers();
        while ( users.hasNext() ) {
            final User user = users.next();
            if ( user.isInGroup ( this ) ) {
                results.add ( user );
            }
        }
        return results.iterator();
    }
    @Override
    public void addRole ( final Role role ) {
        synchronized ( this.roles ) {
            if ( !this.roles.contains ( role ) ) {
                this.roles.add ( role );
            }
        }
    }
    @Override
    public boolean isInRole ( final Role role ) {
        synchronized ( this.roles ) {
            return this.roles.contains ( role );
        }
    }
    @Override
    public void removeRole ( final Role role ) {
        synchronized ( this.roles ) {
            this.roles.remove ( role );
        }
    }
    @Override
    public void removeRoles() {
        synchronized ( this.roles ) {
            this.roles.clear();
        }
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "<group groupname=\"" );
        sb.append ( this.groupname );
        sb.append ( "\"" );
        if ( this.description != null ) {
            sb.append ( " description=\"" );
            sb.append ( this.description );
            sb.append ( "\"" );
        }
        synchronized ( this.roles ) {
            if ( this.roles.size() > 0 ) {
                sb.append ( " roles=\"" );
                int n = 0;
                final Iterator<Role> values = this.roles.iterator();
                while ( values.hasNext() ) {
                    if ( n > 0 ) {
                        sb.append ( ',' );
                    }
                    ++n;
                    sb.append ( values.next().getRolename() );
                }
                sb.append ( "\"" );
            }
        }
        sb.append ( "/>" );
        return sb.toString();
    }
}
