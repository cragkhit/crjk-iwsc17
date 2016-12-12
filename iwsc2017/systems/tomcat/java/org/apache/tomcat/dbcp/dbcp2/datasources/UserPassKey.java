package org.apache.tomcat.dbcp.dbcp2.datasources;
import java.io.Serializable;
class UserPassKey implements Serializable {
    private static final long serialVersionUID = 5142970911626584817L;
    private final String password;
    private final String username;
    UserPassKey ( final String username, final String password ) {
        this.username = username;
        this.password = password;
    }
    public String getPassword() {
        return password;
    }
    public String getUsername() {
        return username;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == null ) {
            return false;
        }
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof UserPassKey ) ) {
            return false;
        }
        final UserPassKey key = ( UserPassKey ) obj;
        return this.username == null ?
               key.username == null :
               this.username.equals ( key.username );
    }
    @Override
    public int hashCode() {
        return this.username != null ?
               this.username.hashCode() : 0;
    }
    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer ( 50 );
        sb.append ( "UserPassKey(" );
        sb.append ( username ).append ( ", " ).append ( password ).append ( ')' );
        return sb.toString();
    }
}
