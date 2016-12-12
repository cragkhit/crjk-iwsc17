package org.apache.tomcat.dbcp.dbcp2.datasources;
import java.sql.SQLException;
import javax.sql.PooledConnection;
interface PooledConnectionManager {
    void invalidate ( PooledConnection p0 ) throws SQLException;
    void setPassword ( String p0 );
    void closePool ( String p0 ) throws SQLException;
}
