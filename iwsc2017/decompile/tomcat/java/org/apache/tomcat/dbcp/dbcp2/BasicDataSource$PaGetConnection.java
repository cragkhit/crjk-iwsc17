package org.apache.tomcat.dbcp.dbcp2;
import java.sql.SQLException;
import java.sql.Connection;
import java.security.PrivilegedExceptionAction;
private class PaGetConnection implements PrivilegedExceptionAction<Connection> {
    @Override
    public Connection run() throws SQLException {
        return BasicDataSource.this.createDataSource().getConnection();
    }
}
