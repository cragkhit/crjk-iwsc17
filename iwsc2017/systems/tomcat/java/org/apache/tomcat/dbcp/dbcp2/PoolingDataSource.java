package org.apache.tomcat.dbcp.dbcp2;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.dbcp.pool2.ObjectPool;
import org.apache.tomcat.dbcp.pool2.impl.GenericObjectPool;
public class PoolingDataSource<C extends Connection> implements DataSource, AutoCloseable {
    private static final Log log = LogFactory.getLog ( PoolingDataSource.class );
    private boolean accessToUnderlyingConnectionAllowed = false;
    public PoolingDataSource ( final ObjectPool<C> pool ) {
        if ( null == pool ) {
            throw new NullPointerException ( "Pool must not be null." );
        }
        _pool = pool;
        if ( _pool instanceof GenericObjectPool<?> ) {
            final PoolableConnectionFactory pcf = ( PoolableConnectionFactory ) ( ( GenericObjectPool<?> ) _pool ).getFactory();
            if ( pcf == null ) {
                throw new NullPointerException ( "PoolableConnectionFactory must not be null." );
            }
            if ( pcf.getPool() != _pool ) {
                log.warn ( Utils.getMessage ( "poolingDataSource.factoryConfig" ) );
                @SuppressWarnings ( "unchecked" )
                final
                ObjectPool<PoolableConnection> p = ( ObjectPool<PoolableConnection> ) _pool;
                pcf.setPool ( p );
            }
        }
    }
    @Override
    public void close() throws Exception {
        try {
            _pool.close();
        } catch ( final RuntimeException rte ) {
            throw new RuntimeException ( Utils.getMessage ( "pool.close.fail" ), rte );
        } catch ( final Exception e ) {
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
            final C conn = _pool.borrowObject();
            if ( conn == null ) {
                return null;
            }
            return new PoolGuardConnectionWrapper<> ( conn );
        } catch ( final SQLException e ) {
            throw e;
        } catch ( final NoSuchElementException e ) {
            throw new SQLException ( "Cannot get a connection, pool error " + e.getMessage(), e );
        } catch ( final RuntimeException e ) {
            throw e;
        } catch ( final Exception e ) {
            throw new SQLException ( "Cannot get a connection, general error", e );
        }
    }
    @Override
    public Connection getConnection ( final String uname, final String passwd ) throws SQLException {
        throw new UnsupportedOperationException();
    }
    @Override
    public PrintWriter getLogWriter() {
        return _logWriter;
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
        _logWriter = out;
    }
    private PrintWriter _logWriter = null;
    private final ObjectPool<C> _pool;
    protected ObjectPool<C> getPool() {
        return _pool;
    }
    private class PoolGuardConnectionWrapper<D extends Connection>
        extends DelegatingConnection<D> {
        PoolGuardConnectionWrapper ( final D delegate ) {
            super ( delegate );
        }
        @Override
        public D getDelegate() {
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
        @Override
        public void close() throws SQLException {
            if ( getDelegateInternal() != null ) {
                super.close();
                super.setDelegate ( null );
            }
        }
        @Override
        public boolean isClosed() throws SQLException {
            if ( getDelegateInternal() == null ) {
                return true;
            }
            return super.isClosed();
        }
    }
}
