package org.apache.tomcat.dbcp.dbcp2.datasources;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import org.apache.tomcat.dbcp.dbcp2.Utils;
import org.apache.tomcat.dbcp.pool2.KeyedObjectPool;
import org.apache.tomcat.dbcp.pool2.KeyedPooledObjectFactory;
import org.apache.tomcat.dbcp.pool2.PooledObject;
import org.apache.tomcat.dbcp.pool2.impl.DefaultPooledObject;
class KeyedCPDSConnectionFactory
    implements KeyedPooledObjectFactory<UserPassKey, PooledConnectionAndInfo>,
    ConnectionEventListener, PooledConnectionManager {
    private static final String NO_KEY_MESSAGE
        = "close() was called on a Connection, but "
          + "I have no record of the underlying PooledConnection.";
    private final ConnectionPoolDataSource _cpds;
    private final String _validationQuery;
    private final int _validationQueryTimeout;
    private final boolean _rollbackAfterValidation;
    private KeyedObjectPool<UserPassKey, PooledConnectionAndInfo> _pool;
    private long maxConnLifetimeMillis = -1;
    private final Set<PooledConnection> validatingSet =
        Collections.newSetFromMap ( new ConcurrentHashMap<PooledConnection, Boolean>() );
    private final Map<PooledConnection, PooledConnectionAndInfo> pcMap =
        new ConcurrentHashMap<>();
    public KeyedCPDSConnectionFactory ( final ConnectionPoolDataSource cpds,
                                        final String validationQuery,
                                        final int validationQueryTimeout,
                                        final boolean rollbackAfterValidation ) {
        _cpds = cpds;
        _validationQuery = validationQuery;
        _validationQueryTimeout = validationQueryTimeout;
        _rollbackAfterValidation = rollbackAfterValidation;
    }
    public void setPool ( final KeyedObjectPool<UserPassKey, PooledConnectionAndInfo> pool ) {
        this._pool = pool;
    }
    public KeyedObjectPool<UserPassKey, PooledConnectionAndInfo> getPool() {
        return _pool;
    }
    @Override
    public synchronized PooledObject<PooledConnectionAndInfo> makeObject ( final UserPassKey upkey )
    throws Exception {
        PooledConnectionAndInfo pci = null;
        PooledConnection pc = null;
        final String username = upkey.getUsername();
        final String password = upkey.getPassword();
        if ( username == null ) {
            pc = _cpds.getPooledConnection();
        } else {
            pc = _cpds.getPooledConnection ( username, password );
        }
        if ( pc == null ) {
            throw new IllegalStateException ( "Connection pool data source returned null from getPooledConnection" );
        }
        pc.addConnectionEventListener ( this );
        pci = new PooledConnectionAndInfo ( pc, username, password );
        pcMap.put ( pc, pci );
        return new DefaultPooledObject<> ( pci );
    }
    @Override
    public void destroyObject ( final UserPassKey key, final PooledObject<PooledConnectionAndInfo> p )
    throws Exception {
        final PooledConnection pc = p.getObject().getPooledConnection();
        pc.removeConnectionEventListener ( this );
        pcMap.remove ( pc );
        pc.close();
    }
    @Override
    public boolean validateObject ( final UserPassKey key,
                                    final PooledObject<PooledConnectionAndInfo> p ) {
        try {
            validateLifetime ( p );
        } catch ( final Exception e ) {
            return false;
        }
        boolean valid = false;
        final PooledConnection pconn = p.getObject().getPooledConnection();
        Connection conn = null;
        validatingSet.add ( pconn );
        if ( null == _validationQuery ) {
            int timeout = _validationQueryTimeout;
            if ( timeout < 0 ) {
                timeout = 0;
            }
            try {
                conn = pconn.getConnection();
                valid = conn.isValid ( timeout );
            } catch ( final SQLException e ) {
                valid = false;
            } finally {
                Utils.closeQuietly ( conn );
                validatingSet.remove ( pconn );
            }
        } else {
            Statement stmt = null;
            ResultSet rset = null;
            validatingSet.add ( pconn );
            try {
                conn = pconn.getConnection();
                stmt = conn.createStatement();
                rset = stmt.executeQuery ( _validationQuery );
                if ( rset.next() ) {
                    valid = true;
                } else {
                    valid = false;
                }
                if ( _rollbackAfterValidation ) {
                    conn.rollback();
                }
            } catch ( final Exception e ) {
                valid = false;
            } finally {
                Utils.closeQuietly ( rset );
                Utils.closeQuietly ( stmt );
                Utils.closeQuietly ( conn );
                validatingSet.remove ( pconn );
            }
        }
        return valid;
    }
    @Override
    public void passivateObject ( final UserPassKey key,
                                  final PooledObject<PooledConnectionAndInfo> p ) throws Exception {
        validateLifetime ( p );
    }
    @Override
    public void activateObject ( final UserPassKey key,
                                 final PooledObject<PooledConnectionAndInfo> p ) throws Exception {
        validateLifetime ( p );
    }
    @Override
    public void connectionClosed ( final ConnectionEvent event ) {
        final PooledConnection pc = ( PooledConnection ) event.getSource();
        if ( !validatingSet.contains ( pc ) ) {
            final PooledConnectionAndInfo pci = pcMap.get ( pc );
            if ( pci == null ) {
                throw new IllegalStateException ( NO_KEY_MESSAGE );
            }
            try {
                _pool.returnObject ( pci.getUserPassKey(), pci );
            } catch ( final Exception e ) {
                System.err.println ( "CLOSING DOWN CONNECTION AS IT COULD " +
                                     "NOT BE RETURNED TO THE POOL" );
                pc.removeConnectionEventListener ( this );
                try {
                    _pool.invalidateObject ( pci.getUserPassKey(), pci );
                } catch ( final Exception e3 ) {
                    System.err.println ( "EXCEPTION WHILE DESTROYING OBJECT " +
                                         pci );
                    e3.printStackTrace();
                }
            }
        }
    }
    @Override
    public void connectionErrorOccurred ( final ConnectionEvent event ) {
        final PooledConnection pc = ( PooledConnection ) event.getSource();
        if ( null != event.getSQLException() ) {
            System.err
            .println ( "CLOSING DOWN CONNECTION DUE TO INTERNAL ERROR (" +
                       event.getSQLException() + ")" );
        }
        pc.removeConnectionEventListener ( this );
        final PooledConnectionAndInfo info = pcMap.get ( pc );
        if ( info == null ) {
            throw new IllegalStateException ( NO_KEY_MESSAGE );
        }
        try {
            _pool.invalidateObject ( info.getUserPassKey(), info );
        } catch ( final Exception e ) {
            System.err.println ( "EXCEPTION WHILE DESTROYING OBJECT " + info );
            e.printStackTrace();
        }
    }
    @Override
    public void invalidate ( final PooledConnection pc ) throws SQLException {
        final PooledConnectionAndInfo info = pcMap.get ( pc );
        if ( info == null ) {
            throw new IllegalStateException ( NO_KEY_MESSAGE );
        }
        final UserPassKey key = info.getUserPassKey();
        try {
            _pool.invalidateObject ( key, info );
            _pool.clear ( key );
        } catch ( final Exception ex ) {
            throw new SQLException ( "Error invalidating connection", ex );
        }
    }
    @Override
    public void setPassword ( final String password ) {
    }
    public void setMaxConnLifetimeMillis ( final long maxConnLifetimeMillis ) {
        this.maxConnLifetimeMillis = maxConnLifetimeMillis;
    }
    @Override
    public void closePool ( final String username ) throws SQLException {
        try {
            _pool.clear ( new UserPassKey ( username, null ) );
        } catch ( final Exception ex ) {
            throw new SQLException ( "Error closing connection pool", ex );
        }
    }
    private void validateLifetime ( final PooledObject<PooledConnectionAndInfo> p )
    throws Exception {
        if ( maxConnLifetimeMillis > 0 ) {
            final long lifetime = System.currentTimeMillis() - p.getCreateTime();
            if ( lifetime > maxConnLifetimeMillis ) {
                throw new Exception ( Utils.getMessage (
                                          "connectionFactory.lifetimeExceeded",
                                          Long.valueOf ( lifetime ),
                                          Long.valueOf ( maxConnLifetimeMillis ) ) );
            }
        }
    }
}
