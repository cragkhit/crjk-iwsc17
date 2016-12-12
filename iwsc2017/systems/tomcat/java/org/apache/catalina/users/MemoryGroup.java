package org.apache.catalina.users;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.catalina.Role;
import org.apache.catalina.User;
import org.apache.catalina.UserDatabase;
public class MemoryGroup extends AbstractGroup {
    MemoryGroup ( MemoryUserDatabase database,
                  String groupname, String description ) {
        super();
        this.database = database;
        setGroupname ( groupname );
        setDescription ( description );
    }
    protected final MemoryUserDatabase database;
    protected final ArrayList<Role> roles = new ArrayList<>();
    @Override
    public Iterator<Role> getRoles() {
        synchronized ( roles ) {
            return ( roles.iterator() );
        }
    }
    @Override
    public UserDatabase getUserDatabase() {
        return ( this.database );
    }
    @Override
    public Iterator<User> getUsers() {
        ArrayList<User> results = new ArrayList<>();
        Iterator<User> users = database.getUsers();
        while ( users.hasNext() ) {
            User user = users.next();
            if ( user.isInGroup ( this ) ) {
                results.add ( user );
            }
        }
        return ( results.iterator() );
    }
    @Override
    public void addRole ( Role role ) {
        synchronized ( roles ) {
            if ( !roles.contains ( role ) ) {
                roles.add ( role );
            }
        }
    }
    @Override
    public boolean isInRole ( Role role ) {
        synchronized ( roles ) {
            return ( roles.contains ( role ) );
        }
    }
    @Override
    public void removeRole ( Role role ) {
        synchronized ( roles ) {
            roles.remove ( role );
        }
    }
    @Override
    public void removeRoles() {
        synchronized ( roles ) {
            roles.clear();
        }
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "<group groupname=\"" );
        sb.append ( groupname );
        sb.append ( "\"" );
        if ( description != null ) {
            sb.append ( " description=\"" );
            sb.append ( description );
            sb.append ( "\"" );
        }
        synchronized ( roles ) {
            if ( roles.size() > 0 ) {
                sb.append ( " roles=\"" );
                int n = 0;
                Iterator<Role> values = roles.iterator();
                while ( values.hasNext() ) {
                    if ( n > 0 ) {
                        sb.append ( ',' );
                    }
                    n++;
                    sb.append ( ( values.next() ).getRolename() );
                }
                sb.append ( "\"" );
            }
        }
        sb.append ( "/>" );
        return ( sb.toString() );
    }
}
