package org.apache.catalina.users;
import org.apache.catalina.UserDatabase;
public class MemoryRole extends AbstractRole {
    MemoryRole ( MemoryUserDatabase database,
                 String rolename, String description ) {
        super();
        this.database = database;
        setRolename ( rolename );
        setDescription ( description );
    }
    protected final MemoryUserDatabase database;
    @Override
    public UserDatabase getUserDatabase() {
        return ( this.database );
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "<role rolename=\"" );
        sb.append ( rolename );
        sb.append ( "\"" );
        if ( description != null ) {
            sb.append ( " description=\"" );
            sb.append ( description );
            sb.append ( "\"" );
        }
        sb.append ( "/>" );
        return ( sb.toString() );
    }
}
