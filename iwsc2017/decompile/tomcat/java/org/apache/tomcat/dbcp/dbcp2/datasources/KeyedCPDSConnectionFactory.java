package org.apache.tomcat.dbcp.dbcp2.datasources;
import javax.sql.ConnectionEvent;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import org.apache.tomcat.dbcp.dbcp2.Utils;
import java.sql.SQLException;
import org.apache.tomcat.dbcp.pool2.impl.DefaultPooledObject;
import org.apache.tomcat.dbcp.pool2.PooledObject;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import javax.sql.PooledConnection;
import java.util.Set;
import org.apache.tomcat.dbcp.pool2.KeyedObjectPool;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.ConnectionEventListener;
import org.apache.tomcat.dbcp.pool2.KeyedPooledObjectFactory;
class KeyedCPDSConnectionFactory implements KeyedPooledObjectFactory<UserPassKey, PooledConnectionAndInfo>, ConnectionEventListener, PooledConnectionManager {
    private static final String NO_KEY_MESSAGE = "close() was called on a Connection, but I have no record of the underlying PooledConnection.";
    private final ConnectionPoolDataSource _cpds;
    private final String _validationQuery;
    private final int _validationQueryTimeout;
    private final boolean _rollbackAfterValidation;
    private KeyedObjectPool<UserPassKey, PooledConnectionAndInfo> _pool;
    private long maxConnLifetimeMillis;
    private final Set<PooledConnection> validatingSet;
    private final Map<PooledConnection, PooledConnectionAndInfo> pcMap;
    public KeyedCPDSConnectionFactory ( final ConnectionPoolDataSource cpds, final String validationQuery, final int validationQueryTimeout, final boolean rollbackAfterValidation ) {
        this.maxConnLifetimeMillis = -1L;
        this.validatingSet = Collections.newSetFromMap ( new ConcurrentHashMap<PooledConnection, Boolean>() );
        this.pcMap = new ConcurrentHashMap<PooledConnection, PooledConnectionAndInfo>();
        this._cpds = cpds;
        this._validationQuery = validationQuery;
        this._validationQueryTimeout = validationQueryTimeout;
        this._rollbackAfterValidation = rollbackAfterValidation;
    }
    public void setPool ( final KeyedObjectPool<UserPassKey, PooledConnectionAndInfo> pool ) {
        this._pool = pool;
    }
    public KeyedObjectPool<UserPassKey, PooledConnectionAndInfo> getPool() {
        return this._pool;
    }
    @Override
    public synchronized PooledObject<PooledConnectionAndInfo> makeObject ( final UserPassKey upkey ) throws Exception {
        PooledConnectionAndInfo pci = null;
        PooledConnection pc = null;
        final String username = upkey.getUsername();
        final String password = upkey.getPassword();
        if ( username == null ) {
            pc = this._cpds.getPooledConnection();
        } else {
            pc = this._cpds.getPooledConnection ( username, password );
        }
        if ( pc == null ) {
            throw new IllegalStateException ( "Connection pool data source returned null from getPooledConnection" );
        }
        pc.addConnectionEventListener ( this );
        pci = new PooledConnectionAndInfo ( pc, username, password );
        this.pcMap.put ( pc, pci );
        return new DefaultPooledObject<PooledConnectionAndInfo> ( pci );
    }
    @Override
    public void destroyObject ( final UserPassKey key, final PooledObject<PooledConnectionAndInfo> p ) throws Exception {
        final PooledConnection pc = p.getObject().getPooledConnection();
        pc.removeConnectionEventListener ( this );
        this.pcMap.remove ( pc );
        pc.close();
    }
    @Override
    public boolean validateObject ( final UserPassKey key, final PooledObject<PooledConnectionAndInfo> p ) {
        try {
            this.validateLifetime ( p );
        } catch ( Exception e ) {
            return false;
        }
        boolean valid = false;
        final PooledConnection pconn = p.getObject().getPooledConnection();
        Connection conn = null;
        this.validatingSet.add ( pconn );
        if ( null == this._validationQuery ) {
            int timeout = this._validationQueryTimeout;
            if ( timeout < 0 ) {
                timeout = 0;
            }
            try {
                conn = pconn.getConnection();
                valid = conn.isValid ( timeout );
            } catch ( SQLException e2 ) {
                valid = false;
            } finally {
                Utils.closeQuietly ( conn );
                this.validatingSet.remove ( pconn );
            }
        } else {
            Statement stmt = null;
            ResultSet rset = null;
            this.validatingSet.add ( pconn );
            try {
                conn = pconn.getConnection();
                stmt = conn.createStatement();
                rset = stmt.executeQuery ( this._validationQuery );
                valid = rset.next();
                if ( this._rollbackAfterValidation ) {
                    conn.rollback();
                }
            } catch ( Exception e3 ) {
                valid = false;
            } finally {
                Utils.closeQuietly ( rset );
                Utils.closeQuietly ( stmt );
                Utils.closeQuietly ( conn );
                this.validatingSet.remove ( pconn );
            }
        }
        return valid;
    }
    @Override
    public void passivateObject ( final UserPassKey key, final PooledObject<PooledConnectionAndInfo> p ) throws Exception {
        this.validateLifetime ( p );
    }
    @Override
    public void activateObject ( final UserPassKey key, final PooledObject<PooledConnectionAndInfo> p ) throws Exception {
        this.validateLifetime ( p );
    }
    @Override
    public void connectionClosed ( final ConnectionEvent event ) {
        final PooledConnection pc = ( PooledConnection ) event.getSource();
        if ( !this.validatingSet.contains ( pc ) ) {
            final PooledConnectionAndInfo pci = this.pcMap.get ( pc );
            if ( pci == null ) {
                throw new IllegalStateException ( "close() was called on a Connection, but I have no record of the underlying PooledConnection." );
            }
            try {
                this._pool.returnObject ( pci.getUserPassKey(), pci );
            } catch ( Exception e2 ) {
                System.err.println ( "CLOSING DOWN CONNECTION AS IT COULD NOT BE RETURNED TO THE POOL" );
                pc.removeConnectionEventListener ( this );
                try {
                    this._pool.invalidateObject ( pci.getUserPassKey(), pci );
                } catch ( Exception e3 ) {
                    System.err.println ( "EXCEPTION WHILE DESTROYING OBJECT " + pci );
                    e3.printStackTrace();
                }
            }
        }
    }
    @Override
    public void connectionErrorOccurred ( final ConnectionEvent event ) {
        final PooledConnection pc = ( PooledConnection ) event.getSource();
        if ( null != event.getSQLException() ) {
            System.err.println ( "CLOSING DOWN CONNECTION DUE TO INTERNAL ERROR (" + event.getSQLException() + ")" );
        }
        pc.removeConnectionEventListener ( this );
        final PooledConnectionAndInfo info = this.pcMap.get ( pc );
        if ( info == null ) {
            throw new IllegalStateException ( "close() was called on a Connection, but I have no record of the underlying PooledConnection." );
        }
        try {
            this._pool.invalidateObject ( info.getUserPassKey(), info );
        } catch ( Exception e ) {
            System.err.println ( "EXCEPTION WHILE DESTROYING OBJECT " + info );
            e.printStackTrace();
        }
    }
    @Override
    public void invalidate ( final PooledConnection pc ) throws SQLException {
        final PooledConnectionAndInfo info = this.pcMap.get ( pc );
        if ( info == null ) {
            throw new IllegalStateException ( "close() was called on a Connection, but I have no record of the underlying PooledConnection." );
        }
        final UserPassKey key = info.getUserPassKey();
        try {
            this._pool.invalidateObject ( key, info );
            this._pool.clear ( key );
        } catch ( Exception ex ) {
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
            this._pool.clear ( new UserPassKey ( username, null ) );
        } catch ( Exception ex ) {
            throw new SQLException ( "Error closing connection pool", ex );
        }
    }
    private void validateLifetime ( final PooledObject<PooledConnectionAndInfo> p ) throws Exception {
        if ( this.maxConnLifetimeMillis > 0L ) {
            final long lifetime = System.currentTimeMillis() - p.getCreateTime();
            if ( lifetime > this.maxConnLifetimeMillis ) {
                throw new Exception ( Utils.getMessage ( "connectionFactory.lifetimeExceeded", lifetime, this.maxConnLifetimeMillis ) );
            }
        }
    }
}
