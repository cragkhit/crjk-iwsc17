package org.apache.tomcat.dbcp.dbcp2;
import java.sql.Connection;
import java.sql.SQLException;
public interface ConnectionFactory {
    Connection createConnection() throws SQLException;
}
