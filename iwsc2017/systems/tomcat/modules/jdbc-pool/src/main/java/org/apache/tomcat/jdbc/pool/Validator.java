package org.apache.tomcat.jdbc.pool;
import java.sql.Connection;
public interface Validator {
    public boolean validate ( Connection connection, int validateAction );
}
