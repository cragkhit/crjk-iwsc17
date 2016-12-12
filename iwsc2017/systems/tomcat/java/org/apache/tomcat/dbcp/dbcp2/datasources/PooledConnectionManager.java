package org.apache.tomcat.dbcp.dbcp2.datasources;
import java.sql.SQLException;
import javax.sql.PooledConnection;
interface PooledConnectionManager {
    void invalidate ( PooledConnection pc ) throws SQLException;
    void setPassword ( String password );
    void closePool ( String username ) throws SQLException;
}
