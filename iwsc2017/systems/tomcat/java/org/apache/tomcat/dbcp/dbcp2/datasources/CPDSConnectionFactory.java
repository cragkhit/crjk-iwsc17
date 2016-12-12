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
import org.apache.tomcat.dbcp.pool2.ObjectPool;
import org.apache.tomcat.dbcp.pool2.PooledObject;
import org.apache.tomcat.dbcp.pool2.PooledObjectFactory;
import org.apache.tomcat.dbcp.pool2.impl.DefaultPooledObject;
class CPDSConnectionFactory
    implements PooledObjectFactory<PooledConnectionAndInfo>,
    ConnectionEventListener, PooledConnectionManager {
    private static final String NO_KEY_MESSAGE
        = "close() was called on a Connection, but "
          + "I have no record of the underlying PooledConnection.";
    private final ConnectionPoolDataSource _cpds;
    private final String _validationQuery;
    private final int _validationQueryTimeout;
    private final boolean _rollbackAfterValidation;
    private ObjectPool<PooledConnectionAndInfo> _pool;
    private final String _username;
    private String _password = null;
    private long maxConnLifetimeMillis = -1;
    private final Set<PooledConnection> validatingSet =
        Collections.newSetFromMap ( new ConcurrentHashMap<PooledConnection, Boolean>() );
    private final Map<PooledConnection, PooledConnectionAndInfo> pcMap =
        new ConcurrentHashMap<>();
    public CPDSConnectionFactory ( final ConnectionPoolDataSource cpds,
                                   final String validationQuery,
                                   final int validationQueryTimeout,
                                   final boolean rollbackAfterValidation,
                                   final String username,
                                   final String password ) {
        _cpds = cpds;
        _validationQuery = validationQuery;
        _validationQueryTimeout = validationQueryTimeout;
        _username = username;
        _password = password;
        _rollbackAfterValidation = rollbackAfterValidation;
    }
    public ObjectPool<PooledConnectionAndInfo> getPool() {
        return _pool;
    }
    public void setPool ( final ObjectPool<PooledConnectionAndInfo> pool ) {
        this._pool = pool;
    }
    @Override
    public synchronized PooledObject<PooledConnectionAndInfo> makeObject() {
        PooledConnectionAndInfo pci;
        try {
            PooledConnection pc = null;
            if ( _username == null ) {
                pc = _cpds.getPooledConnection();
            } else {
                pc = _cpds.getPooledConnection ( _username, _password );
            }
            if ( pc == null ) {
                throw new IllegalStateException ( "Connection pool data source returned null from getPooledConnection" );
            }
            pc.addConnectionEventListener ( this );
            pci = new PooledConnectionAndInfo ( pc, _username, _password );
            pcMap.put ( pc, pci );
        } catch ( final SQLException e ) {
            throw new RuntimeException ( e.getMessage() );
        }
        return new DefaultPooledObject<> ( pci );
    }
    @Override
    public void destroyObject ( final PooledObject<PooledConnectionAndInfo> p ) throws Exception {
        doDestroyObject ( p.getObject() );
    }
    private void doDestroyObject ( final PooledConnectionAndInfo pci ) throws Exception {
        final PooledConnection pc = pci.getPooledConnection();
        pc.removeConnectionEventListener ( this );
        pcMap.remove ( pc );
        pc.close();
    }
    @Override
    public boolean validateObject ( final PooledObject<PooledConnectionAndInfo> p ) {
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
    public void passivateObject ( final PooledObject<PooledConnectionAndInfo> p )
    throws Exception {
        validateLifetime ( p );
    }
    @Override
    public void activateObject ( final PooledObject<PooledConnectionAndInfo> p )
    throws Exception {
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
                _pool.returnObject ( pci );
            } catch ( final Exception e ) {
                System.err.println ( "CLOSING DOWN CONNECTION AS IT COULD "
                                     + "NOT BE RETURNED TO THE POOL" );
                pc.removeConnectionEventListener ( this );
                try {
                    doDestroyObject ( pci );
                } catch ( final Exception e2 ) {
                    System.err.println ( "EXCEPTION WHILE DESTROYING OBJECT "
                                         + pci );
                    e2.printStackTrace();
                }
            }
        }
    }
    @Override
    public void connectionErrorOccurred ( final ConnectionEvent event ) {
        final PooledConnection pc = ( PooledConnection ) event.getSource();
        if ( null != event.getSQLException() ) {
            System.err.println (
                "CLOSING DOWN CONNECTION DUE TO INTERNAL ERROR ("
                + event.getSQLException() + ")" );
        }
        pc.removeConnectionEventListener ( this );
        final PooledConnectionAndInfo pci = pcMap.get ( pc );
        if ( pci == null ) {
            throw new IllegalStateException ( NO_KEY_MESSAGE );
        }
        try {
            _pool.invalidateObject ( pci );
        } catch ( final Exception e ) {
            System.err.println ( "EXCEPTION WHILE DESTROYING OBJECT " + pci );
            e.printStackTrace();
        }
    }
    @Override
    public void invalidate ( final PooledConnection pc ) throws SQLException {
        final PooledConnectionAndInfo pci = pcMap.get ( pc );
        if ( pci == null ) {
            throw new IllegalStateException ( NO_KEY_MESSAGE );
        }
        try {
            _pool.invalidateObject ( pci );
            _pool.close();
        } catch ( final Exception ex ) {
            throw new SQLException ( "Error invalidating connection", ex );
        }
    }
    @Override
    public synchronized void setPassword ( final String password ) {
        _password = password;
    }
    public void setMaxConnLifetimeMillis ( final long maxConnLifetimeMillis ) {
        this.maxConnLifetimeMillis = maxConnLifetimeMillis;
    }
    @Override
    public void closePool ( final String username ) throws SQLException {
        synchronized ( this ) {
            if ( username == null || !username.equals ( _username ) ) {
                return;
            }
        }
        try {
            _pool.close();
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
