package org.apache.tomcat.dbcp.dbcp2;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.tomcat.dbcp.pool2.ObjectPool;
public class PoolingDriver implements Driver {
    static {
        try {
            DriverManager.registerDriver ( new PoolingDriver() );
        } catch ( final Exception e ) {
        }
    }
    protected static final HashMap<String, ObjectPool<? extends Connection>> pools =
        new HashMap<>();
    private final boolean accessToUnderlyingConnectionAllowed;
    public PoolingDriver() {
        this ( true );
    }
    protected PoolingDriver ( final boolean accessToUnderlyingConnectionAllowed ) {
        this.accessToUnderlyingConnectionAllowed = accessToUnderlyingConnectionAllowed;
    }
    protected boolean isAccessToUnderlyingConnectionAllowed() {
        return accessToUnderlyingConnectionAllowed;
    }
    public synchronized ObjectPool<? extends Connection> getConnectionPool ( final String name )
    throws SQLException {
        final ObjectPool<? extends Connection> pool = pools.get ( name );
        if ( null == pool ) {
            throw new SQLException ( "Pool not registered." );
        }
        return pool;
    }
    public synchronized void registerPool ( final String name,
                                            final ObjectPool<? extends Connection> pool ) {
        pools.put ( name, pool );
    }
    public synchronized void closePool ( final String name ) throws SQLException {
        final ObjectPool<? extends Connection> pool = pools.get ( name );
        if ( pool != null ) {
            pools.remove ( name );
            try {
                pool.close();
            } catch ( final Exception e ) {
                throw new SQLException ( "Error closing pool " + name, e );
            }
        }
    }
    public synchronized String[] getPoolNames() {
        final Set<String> names = pools.keySet();
        return names.toArray ( new String[names.size()] );
    }
    @Override
    public boolean acceptsURL ( final String url ) throws SQLException {
        try {
            return url.startsWith ( URL_PREFIX );
        } catch ( final NullPointerException e ) {
            return false;
        }
    }
    @Override
    public Connection connect ( final String url, final Properties info ) throws SQLException {
        if ( acceptsURL ( url ) ) {
            final ObjectPool<? extends Connection> pool =
                getConnectionPool ( url.substring ( URL_PREFIX_LEN ) );
            try {
                final Connection conn = pool.borrowObject();
                if ( conn == null ) {
                    return null;
                }
                return new PoolGuardConnectionWrapper ( pool, conn );
            } catch ( final SQLException e ) {
                throw e;
            } catch ( final NoSuchElementException e ) {
                throw new SQLException ( "Cannot get a connection, pool error: " + e.getMessage(), e );
            } catch ( final RuntimeException e ) {
                throw e;
            } catch ( final Exception e ) {
                throw new SQLException ( "Cannot get a connection, general error: " + e.getMessage(), e );
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
            @SuppressWarnings ( "unchecked" )
            final
            ObjectPool<Connection> pool = ( ObjectPool<Connection> ) pgconn.pool;
            try {
                pool.invalidateObject ( pgconn.getDelegateInternal() );
            } catch ( final Exception e ) {
            }
        } else {
            throw new SQLException ( "Invalid connection class" );
        }
    }
    @Override
    public int getMajorVersion() {
        return MAJOR_VERSION;
    }
    @Override
    public int getMinorVersion() {
        return MINOR_VERSION;
    }
    @Override
    public boolean jdbcCompliant() {
        return true;
    }
    @Override
    public DriverPropertyInfo[] getPropertyInfo ( final String url, final Properties info ) {
        return new DriverPropertyInfo[0];
    }
    protected static final String URL_PREFIX = "jdbc:apache:commons:dbcp:";
    protected static final int URL_PREFIX_LEN = URL_PREFIX.length();
    protected static final int MAJOR_VERSION = 1;
    protected static final int MINOR_VERSION = 0;
    private class PoolGuardConnectionWrapper extends DelegatingConnection<Connection> {
        private final ObjectPool<? extends Connection> pool;
        PoolGuardConnectionWrapper ( final ObjectPool<? extends Connection> pool,
                                     final Connection delegate ) {
            super ( delegate );
            this.pool = pool;
        }
        @Override
        public Connection getDelegate() {
            if ( isAccessToUnderlyingConnectionAllowed() ) {
                return super.getDelegate();
            }
            return null;
        }
        @Override
        public Connection getInnermostDelegate() {
            if ( isAccessToUnderlyingConnectionAllowed() ) {
                return super.getInnermostDelegate();
            }
            return null;
        }
    }
}
