package org.apache.tomcat.dbcp.dbcp2;
import org.apache.juli.logging.LogFactory;
import java.util.NoSuchElementException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import java.sql.SQLException;
import org.apache.tomcat.dbcp.pool2.impl.GenericObjectPool;
import org.apache.tomcat.dbcp.pool2.ObjectPool;
import java.io.PrintWriter;
import org.apache.juli.logging.Log;
import javax.sql.DataSource;
import java.sql.Connection;
public class PoolingDataSource<C extends Connection> implements DataSource, AutoCloseable {
    private static final Log log;
    private boolean accessToUnderlyingConnectionAllowed;
    private PrintWriter _logWriter;
    private final ObjectPool<C> _pool;
    public PoolingDataSource ( final ObjectPool<C> pool ) {
        this.accessToUnderlyingConnectionAllowed = false;
        this._logWriter = null;
        if ( null == pool ) {
            throw new NullPointerException ( "Pool must not be null." );
        }
        this._pool = pool;
        if ( this._pool instanceof GenericObjectPool ) {
            final PoolableConnectionFactory pcf = ( PoolableConnectionFactory ) ( ( GenericObjectPool ) this._pool ).getFactory();
            if ( pcf == null ) {
                throw new NullPointerException ( "PoolableConnectionFactory must not be null." );
            }
            if ( pcf.getPool() != this._pool ) {
                PoolingDataSource.log.warn ( Utils.getMessage ( "poolingDataSource.factoryConfig" ) );
                final ObjectPool<PoolableConnection> p = ( ObjectPool<PoolableConnection> ) this._pool;
                pcf.setPool ( p );
            }
        }
    }
    @Override
    public void close() throws Exception {
        try {
            this._pool.close();
        } catch ( RuntimeException rte ) {
            throw new RuntimeException ( Utils.getMessage ( "pool.close.fail" ), rte );
        } catch ( Exception e ) {
            throw new SQLException ( Utils.getMessage ( "pool.close.fail" ), e );
        }
    }
    public boolean isAccessToUnderlyingConnectionAllowed() {
        return this.accessToUnderlyingConnectionAllowed;
    }
    public void setAccessToUnderlyingConnectionAllowed ( final boolean allow ) {
        this.accessToUnderlyingConnectionAllowed = allow;
    }
    @Override
    public boolean isWrapperFor ( final Class<?> iface ) throws SQLException {
        return false;
    }
    @Override
    public <T> T unwrap ( final Class<T> iface ) throws SQLException {
        throw new SQLException ( "PoolingDataSource is not a wrapper." );
    }
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
    @Override
    public Connection getConnection() throws SQLException {
        try {
            final C conn = this._pool.borrowObject();
            if ( conn == null ) {
                return null;
            }
            return new PoolGuardConnectionWrapper<Object> ( conn );
        } catch ( SQLException e ) {
            throw e;
        } catch ( NoSuchElementException e2 ) {
            throw new SQLException ( "Cannot get a connection, pool error " + e2.getMessage(), e2 );
        } catch ( RuntimeException e3 ) {
            throw e3;
        } catch ( Exception e4 ) {
            throw new SQLException ( "Cannot get a connection, general error", e4 );
        }
    }
    @Override
    public Connection getConnection ( final String uname, final String passwd ) throws SQLException {
        throw new UnsupportedOperationException();
    }
    @Override
    public PrintWriter getLogWriter() {
        return this._logWriter;
    }
    @Override
    public int getLoginTimeout() {
        throw new UnsupportedOperationException ( "Login timeout is not supported." );
    }
    @Override
    public void setLoginTimeout ( final int seconds ) {
        throw new UnsupportedOperationException ( "Login timeout is not supported." );
    }
    @Override
    public void setLogWriter ( final PrintWriter out ) {
        this._logWriter = out;
    }
    protected ObjectPool<C> getPool() {
        return this._pool;
    }
    static {
        log = LogFactory.getLog ( PoolingDataSource.class );
    }
    private class PoolGuardConnectionWrapper<D extends Connection> extends DelegatingConnection<D> {
        PoolGuardConnectionWrapper ( final D delegate ) {
            super ( delegate );
        }
        @Override
        public D getDelegate() {
            if ( PoolingDataSource.this.isAccessToUnderlyingConnectionAllowed() ) {
                return super.getDelegate();
            }
            return null;
        }
        @Override
        public Connection getInnermostDelegate() {
            if ( PoolingDataSource.this.isAccessToUnderlyingConnectionAllowed() ) {
                return super.getInnermostDelegate();
            }
            return null;
        }
        @Override
        public void close() throws SQLException {
            if ( this.getDelegateInternal() != null ) {
                super.close();
                super.setDelegate ( null );
            }
        }
        @Override
        public boolean isClosed() throws SQLException {
            return this.getDelegateInternal() == null || super.isClosed();
        }
    }
}
