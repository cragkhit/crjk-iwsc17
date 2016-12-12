package org.apache.catalina.users;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.catalina.Group;
import org.apache.catalina.Role;
import org.apache.catalina.UserDatabase;
import org.apache.catalina.util.RequestUtil;
public class MemoryUser extends AbstractUser {
    MemoryUser ( MemoryUserDatabase database, String username,
                 String password, String fullName ) {
        super();
        this.database = database;
        setUsername ( username );
        setPassword ( password );
        setFullName ( fullName );
    }
    protected final MemoryUserDatabase database;
    protected final ArrayList<Group> groups = new ArrayList<>();
    protected final ArrayList<Role> roles = new ArrayList<>();
    @Override
    public Iterator<Group> getGroups() {
        synchronized ( groups ) {
            return ( groups.iterator() );
        }
    }
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
    public void addGroup ( Group group ) {
        synchronized ( groups ) {
            if ( !groups.contains ( group ) ) {
                groups.add ( group );
            }
        }
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
    public boolean isInGroup ( Group group ) {
        synchronized ( groups ) {
            return ( groups.contains ( group ) );
        }
    }
    @Override
    public boolean isInRole ( Role role ) {
        synchronized ( roles ) {
            return ( roles.contains ( role ) );
        }
    }
    @Override
    public void removeGroup ( Group group ) {
        synchronized ( groups ) {
            groups.remove ( group );
        }
    }
    @Override
    public void removeGroups() {
        synchronized ( groups ) {
            groups.clear();
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
    public String toXml() {
        StringBuilder sb = new StringBuilder ( "<user username=\"" );
        sb.append ( RequestUtil.filter ( username ) );
        sb.append ( "\" password=\"" );
        sb.append ( RequestUtil.filter ( password ) );
        sb.append ( "\"" );
        if ( fullName != null ) {
            sb.append ( " fullName=\"" );
            sb.append ( RequestUtil.filter ( fullName ) );
            sb.append ( "\"" );
        }
        synchronized ( groups ) {
            if ( groups.size() > 0 ) {
                sb.append ( " groups=\"" );
                int n = 0;
                Iterator<Group> values = groups.iterator();
                while ( values.hasNext() ) {
                    if ( n > 0 ) {
                        sb.append ( ',' );
                    }
                    n++;
                    sb.append ( RequestUtil.filter ( values.next().getGroupname() ) );
                }
                sb.append ( "\"" );
            }
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
                    sb.append ( RequestUtil.filter ( values.next().getRolename() ) );
                }
                sb.append ( "\"" );
            }
        }
        sb.append ( "/>" );
        return ( sb.toString() );
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "User username=\"" );
        sb.append ( RequestUtil.filter ( username ) );
        sb.append ( "\"" );
        if ( fullName != null ) {
            sb.append ( ", fullName=\"" );
            sb.append ( RequestUtil.filter ( fullName ) );
            sb.append ( "\"" );
        }
        synchronized ( groups ) {
            if ( groups.size() > 0 ) {
                sb.append ( ", groups=\"" );
                int n = 0;
                Iterator<Group> values = groups.iterator();
                while ( values.hasNext() ) {
                    if ( n > 0 ) {
                        sb.append ( ',' );
                    }
                    n++;
                    sb.append ( RequestUtil.filter ( values.next().getGroupname() ) );
                }
                sb.append ( "\"" );
            }
        }
        synchronized ( roles ) {
            if ( roles.size() > 0 ) {
                sb.append ( ", roles=\"" );
                int n = 0;
                Iterator<Role> values = roles.iterator();
                while ( values.hasNext() ) {
                    if ( n > 0 ) {
                        sb.append ( ',' );
                    }
                    n++;
                    sb.append ( RequestUtil.filter ( values.next().getRolename() ) );
                }
                sb.append ( "\"" );
            }
        }
        return ( sb.toString() );
    }
}
