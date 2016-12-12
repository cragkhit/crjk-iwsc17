package org.apache.catalina.users;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.UserDatabase;
import java.util.Iterator;
import org.apache.catalina.Role;
import org.apache.catalina.Group;
import java.util.ArrayList;
public class MemoryUser extends AbstractUser {
    protected final MemoryUserDatabase database;
    protected final ArrayList<Group> groups;
    protected final ArrayList<Role> roles;
    MemoryUser ( final MemoryUserDatabase database, final String username, final String password, final String fullName ) {
        this.groups = new ArrayList<Group>();
        this.roles = new ArrayList<Role>();
        this.database = database;
        this.setUsername ( username );
        this.setPassword ( password );
        this.setFullName ( fullName );
    }
    @Override
    public Iterator<Group> getGroups() {
        synchronized ( this.groups ) {
            return this.groups.iterator();
        }
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
    public void addGroup ( final Group group ) {
        synchronized ( this.groups ) {
            if ( !this.groups.contains ( group ) ) {
                this.groups.add ( group );
            }
        }
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
    public boolean isInGroup ( final Group group ) {
        synchronized ( this.groups ) {
            return this.groups.contains ( group );
        }
    }
    @Override
    public boolean isInRole ( final Role role ) {
        synchronized ( this.roles ) {
            return this.roles.contains ( role );
        }
    }
    @Override
    public void removeGroup ( final Group group ) {
        synchronized ( this.groups ) {
            this.groups.remove ( group );
        }
    }
    @Override
    public void removeGroups() {
        synchronized ( this.groups ) {
            this.groups.clear();
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
    public String toXml() {
        final StringBuilder sb = new StringBuilder ( "<user username=\"" );
        sb.append ( RequestUtil.filter ( this.username ) );
        sb.append ( "\" password=\"" );
        sb.append ( RequestUtil.filter ( this.password ) );
        sb.append ( "\"" );
        if ( this.fullName != null ) {
            sb.append ( " fullName=\"" );
            sb.append ( RequestUtil.filter ( this.fullName ) );
            sb.append ( "\"" );
        }
        synchronized ( this.groups ) {
            if ( this.groups.size() > 0 ) {
                sb.append ( " groups=\"" );
                int n = 0;
                final Iterator<Group> values = this.groups.iterator();
                while ( values.hasNext() ) {
                    if ( n > 0 ) {
                        sb.append ( ',' );
                    }
                    ++n;
                    sb.append ( RequestUtil.filter ( values.next().getGroupname() ) );
                }
                sb.append ( "\"" );
            }
        }
        synchronized ( this.roles ) {
            if ( this.roles.size() > 0 ) {
                sb.append ( " roles=\"" );
                int n = 0;
                final Iterator<Role> values2 = this.roles.iterator();
                while ( values2.hasNext() ) {
                    if ( n > 0 ) {
                        sb.append ( ',' );
                    }
                    ++n;
                    sb.append ( RequestUtil.filter ( values2.next().getRolename() ) );
                }
                sb.append ( "\"" );
            }
        }
        sb.append ( "/>" );
        return sb.toString();
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "User username=\"" );
        sb.append ( RequestUtil.filter ( this.username ) );
        sb.append ( "\"" );
        if ( this.fullName != null ) {
            sb.append ( ", fullName=\"" );
            sb.append ( RequestUtil.filter ( this.fullName ) );
            sb.append ( "\"" );
        }
        synchronized ( this.groups ) {
            if ( this.groups.size() > 0 ) {
                sb.append ( ", groups=\"" );
                int n = 0;
                final Iterator<Group> values = this.groups.iterator();
                while ( values.hasNext() ) {
                    if ( n > 0 ) {
                        sb.append ( ',' );
                    }
                    ++n;
                    sb.append ( RequestUtil.filter ( values.next().getGroupname() ) );
                }
                sb.append ( "\"" );
            }
        }
        synchronized ( this.roles ) {
            if ( this.roles.size() > 0 ) {
                sb.append ( ", roles=\"" );
                int n = 0;
                final Iterator<Role> values2 = this.roles.iterator();
                while ( values2.hasNext() ) {
                    if ( n > 0 ) {
                        sb.append ( ',' );
                    }
                    ++n;
                    sb.append ( RequestUtil.filter ( values2.next().getRolename() ) );
                }
                sb.append ( "\"" );
            }
        }
        return sb.toString();
    }
}
