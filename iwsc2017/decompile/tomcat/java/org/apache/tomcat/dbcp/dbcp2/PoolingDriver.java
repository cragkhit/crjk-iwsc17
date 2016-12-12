package org.apache.tomcat.dbcp.dbcp2;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.sql.SQLException;
import java.sql.Connection;
import org.apache.tomcat.dbcp.pool2.ObjectPool;
import java.util.HashMap;
import java.sql.Driver;
public class PoolingDriver implements Driver {
    protected static final HashMap<String, ObjectPool<? extends Connection>> pools;
    private final boolean accessToUnderlyingConnectionAllowed;
    protected static final String URL_PREFIX = "jdbc:apache:commons:dbcp:";
    protected static final int URL_PREFIX_LEN;
    protected static final int MAJOR_VERSION = 1;
    protected static final int MINOR_VERSION = 0;
    public PoolingDriver() {
        this ( true );
    }
    protected PoolingDriver ( final boolean accessToUnderlyingConnectionAllowed ) {
        this.accessToUnderlyingConnectionAllowed = accessToUnderlyingConnectionAllowed;
    }
    protected boolean isAccessToUnderlyingConnectionAllowed() {
        return this.accessToUnderlyingConnectionAllowed;
    }
    public synchronized ObjectPool<? extends Connection> getConnectionPool ( final String name ) throws SQLException {
        final ObjectPool<? extends Connection> pool = PoolingDriver.pools.get ( name );
        if ( null == pool ) {
            throw new SQLException ( "Pool not registered." );
        }
        return pool;
    }
    public synchronized void registerPool ( final String name, final ObjectPool<? extends Connection> pool ) {
        PoolingDriver.pools.put ( name, pool );
    }
    public synchronized void closePool ( final String name ) throws SQLException {
        final ObjectPool<? extends Connection> pool = PoolingDriver.pools.get ( name );
        if ( pool != null ) {
            PoolingDriver.pools.remove ( name );
            try {
                pool.close();
            } catch ( Exception e ) {
                throw new SQLException ( "Error closing pool " + name, e );
            }
        }
    }
    public synchronized String[] getPoolNames() {
        final Set<String> names = PoolingDriver.pools.keySet();
        return names.toArray ( new String[names.size()] );
    }
    @Override
    public boolean acceptsURL ( final String url ) throws SQLException {
        try {
            return url.startsWith ( "jdbc:apache:commons:dbcp:" );
        } catch ( NullPointerException e ) {
            return false;
        }
    }
    @Override
    public Connection connect ( final String url, final Properties info ) throws SQLException {
        if ( this.acceptsURL ( url ) ) {
            final ObjectPool<? extends Connection> pool = this.getConnectionPool ( url.substring ( PoolingDriver.URL_PREFIX_LEN ) );
            try {
                final Connection conn = ( Connection ) pool.borrowObject();
                if ( conn == null ) {
                    return null;
                }
                return new PoolGuardConnectionWrapper ( pool, conn );
            } catch ( SQLException e ) {
                throw e;
            } catch ( NoSuchElementException e2 ) {
                throw new SQLException ( "Cannot get a connection, pool error: " + e2.getMessage(), e2 );
            } catch ( RuntimeException e3 ) {
                throw e3;
            } catch ( Exception e4 ) {
                throw new SQLException ( "Cannot get a connection, general error: " + e4.getMessage(), e4 );
            }
        }
        return null;
    }
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
    public void invalidateConnection ( final Connection conn ) throws SQLException {
        if ( conn instanceof PoolGuardConnectionWrapper ) {
            final PoolGuardConnectionWrapper pgconn = ( PoolGuardConnectionWrapper ) conn;
            final ObjectPool<Connection> pool = ( ObjectPool<Connection> ) pgconn.pool;
            try {
                pool.invalidateObject ( pgconn.getDelegateInternal() );
            } catch ( Exception ex ) {}
            return;
        }
        throw new SQLException ( "Invalid connection class" );
    }
    @Override
    public int getMajorVersion() {
        return 1;
    }
    @Override
    public int getMinorVersion() {
        return 0;
    }
    @Override
    public boolean jdbcCompliant() {
        return true;
    }
    @Override
    public DriverPropertyInfo[] getPropertyInfo ( final String url, final Properties info ) {
        return new DriverPropertyInfo[0];
    }
    static {
        try {
            DriverManager.registerDriver ( new PoolingDriver() );
        } catch ( Exception ex ) {}
        pools = new HashMap<String, ObjectPool<? extends Connection>>();
        URL_PREFIX_LEN = "jdbc:apache:commons:dbcp:".length();
    }
    private class PoolGuardConnectionWrapper extends DelegatingConnection<Connection> {
        private final ObjectPool<? extends Connection> pool;
        PoolGuardConnectionWrapper ( final ObjectPool<? extends Connection> pool, final Connection delegate ) {
            super ( delegate );
            this.pool = pool;
        }
        @Override
        public Connection getDelegate() {
            if ( PoolingDriver.this.isAccessToUnderlyingConnectionAllowed() ) {
                return super.getDelegate();
            }
            return null;
        }
        @Override
        public Connection getInnermostDelegate() {
            if ( PoolingDriver.this.isAccessToUnderlyingConnectionAllowed() ) {
                return super.getInnermostDelegate();
            }
            return null;
        }
    }
}
