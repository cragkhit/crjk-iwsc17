package org.apache.tomcat.dbcp.dbcp2;
import java.sql.SQLException;
import java.sql.Connection;
public interface ConnectionFactory {
    Connection createConnection() throws SQLException;
}
