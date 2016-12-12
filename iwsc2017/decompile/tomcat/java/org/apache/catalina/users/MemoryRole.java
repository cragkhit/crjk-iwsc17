package org.apache.catalina.users;
import org.apache.catalina.UserDatabase;
public class MemoryRole extends AbstractRole {
    protected final MemoryUserDatabase database;
    MemoryRole ( final MemoryUserDatabase database, final String rolename, final String description ) {
        this.database = database;
        this.setRolename ( rolename );
        this.setDescription ( description );
    }
    @Override
    public UserDatabase getUserDatabase() {
        return this.database;
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "<role rolename=\"" );
        sb.append ( this.rolename );
        sb.append ( "\"" );
        if ( this.description != null ) {
            sb.append ( " description=\"" );
            sb.append ( this.description );
            sb.append ( "\"" );
        }
        sb.append ( "/>" );
        return sb.toString();
    }
}
