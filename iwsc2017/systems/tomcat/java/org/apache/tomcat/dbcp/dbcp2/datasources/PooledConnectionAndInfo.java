package org.apache.tomcat.dbcp.dbcp2.datasources;
import javax.sql.PooledConnection;
final class PooledConnectionAndInfo {
    private final PooledConnection pooledConnection;
    private final String password;
    private final String username;
    private final UserPassKey upkey;
    PooledConnectionAndInfo ( final PooledConnection pc, final String username, final String password ) {
        this.pooledConnection = pc;
        this.username = username;
        this.password = password;
        upkey = new UserPassKey ( username, password );
    }
    PooledConnection getPooledConnection() {
        return pooledConnection;
    }
    UserPassKey getUserPassKey() {
        return upkey;
    }
    String getPassword() {
        return password;
    }
    String getUsername() {
        return username;
    }
}
