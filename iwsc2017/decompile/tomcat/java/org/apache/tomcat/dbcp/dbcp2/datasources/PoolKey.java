package org.apache.tomcat.dbcp.dbcp2.datasources;
import java.io.Serializable;
class PoolKey implements Serializable {
    private static final long serialVersionUID = 2252771047542484533L;
    private final String datasourceName;
    private final String username;
    PoolKey ( final String datasourceName, final String username ) {
        this.datasourceName = datasourceName;
        this.username = username;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj instanceof PoolKey ) {
            final PoolKey pk = ( PoolKey ) obj;
            if ( null == this.datasourceName ) {
                if ( null != pk.datasourceName ) {
                    return false;
                }
            } else if ( !this.datasourceName.equals ( pk.datasourceName ) ) {
                return false;
            }
            if ( ( null != this.username ) ? this.username.equals ( pk.username ) : ( null == pk.username ) ) {
                return true;
            }
            return false;
        }
        return false;
    }
    @Override
    public int hashCode() {
        int h = 0;
        if ( this.datasourceName != null ) {
            h += this.datasourceName.hashCode();
        }
        if ( this.username != null ) {
            h = 29 * h + this.username.hashCode();
        }
        return h;
    }
    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer ( 50 );
        sb.append ( "PoolKey(" );
        sb.append ( this.username ).append ( ", " ).append ( this.datasourceName );
        sb.append ( ')' );
        return sb.toString();
    }
}
