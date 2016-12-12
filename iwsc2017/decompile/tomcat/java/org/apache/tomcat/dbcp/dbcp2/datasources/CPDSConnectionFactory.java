package org.apache.tomcat.dbcp.dbcp2.datasources;
import javax.sql.ConnectionEvent;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import org.apache.tomcat.dbcp.dbcp2.Utils;
import org.apache.tomcat.dbcp.pool2.impl.DefaultPooledObject;
import java.sql.SQLException;
import org.apache.tomcat.dbcp.pool2.PooledObject;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import javax.sql.PooledConnection;
import java.util.Set;
import org.apache.tomcat.dbcp.pool2.ObjectPool;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.ConnectionEventListener;
import org.apache.tomcat.dbcp.pool2.PooledObjectFactory;
class CPDSConnectionFactory implements PooledObjectFactory<PooledConnectionAndInfo>, ConnectionEventListener, PooledConnectionManager {
    private static final String NO_KEY_MESSAGE = "close() was called on a Connection, but I have no record of the underlying PooledConnection.";
    private final ConnectionPoolDataSource _cpds;
    private final String _validationQuery;
    private final int _validationQueryTimeout;
    private final boolean _rollbackAfterValidation;
    private ObjectPool<PooledConnectionAndInfo> _pool;
    private final String _username;
    private String _password;
    private long maxConnLifetimeMillis;
    private final Set<PooledConnection> validatingSet;
    private final Map<PooledConnection, PooledConnectionAndInfo> pcMap;
    public CPDSConnectionFactory ( final ConnectionPoolDataSource cpds, final String validationQuery, final int validationQueryTimeout, final boolean rollbackAfterValidation, final String username, final String password ) {
        this._password = null;
        this.maxConnLifetimeMillis = -1L;
        this.validatingSet = Collections.newSetFromMap ( new ConcurrentHashMap<PooledConnection, Boolean>() );
        this.pcMap = new ConcurrentHashMap<PooledConnection, PooledConnectionAndInfo>();
        this._cpds = cpds;
        this._validationQuery = validationQuery;
        this._validationQueryTimeout = validationQueryTimeout;
        this._username = username;
        this._password = password;
        this._rollbackAfterValidation = rollbackAfterValidation;
    }
    public ObjectPool<PooledConnectionAndInfo> getPool() {
        return this._pool;
    }
    public void setPool ( final ObjectPool<PooledConnectionAndInfo> pool ) {
        this._pool = pool;
    }
    @Override
    public synchronized PooledObject<PooledConnectionAndInfo> makeObject() {
        PooledConnectionAndInfo pci;
        try {
            PooledConnection pc = null;
            if ( this._username == null ) {
                pc = this._cpds.getPooledConnection();
            } else {
                pc = this._cpds.getPooledConnection ( this._username, this._password );
            }
            if ( pc == null ) {
                throw new IllegalStateException ( "Connection pool data source returned null from getPooledConnection" );
            }
            pc.addConnectionEventListener ( this );
            pci = new PooledConnectionAndInfo ( pc, this._username, this._password );
            this.pcMap.put ( pc, pci );
        } catch ( SQLException e ) {
            throw new RuntimeException ( e.getMessage() );
        }
        return new DefaultPooledObject<PooledConnectionAndInfo> ( pci );
    }
    @Override
    public void destroyObject ( final PooledObject<PooledConnectionAndInfo> p ) throws Exception {
        this.doDestroyObject ( p.getObject() );
    }
    private void doDestroyObject ( final PooledConnectionAndInfo pci ) throws Exception {
        final PooledConnection pc = pci.getPooledConnection();
        pc.removeConnectionEventListener ( this );
        this.pcMap.remove ( pc );
        pc.close();
    }
    @Override
    public boolean validateObject ( final PooledObject<PooledConnectionAndInfo> p ) {
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
    public void passivateObject ( final PooledObject<PooledConnectionAndInfo> p ) throws Exception {
        this.validateLifetime ( p );
    }
    @Override
    public void activateObject ( final PooledObject<PooledConnectionAndInfo> p ) throws Exception {
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
                this._pool.returnObject ( pci );
            } catch ( Exception e2 ) {
                System.err.println ( "CLOSING DOWN CONNECTION AS IT COULD NOT BE RETURNED TO THE POOL" );
                pc.removeConnectionEventListener ( this );
                try {
                    this.doDestroyObject ( pci );
                } catch ( Exception e2 ) {
                    System.err.println ( "EXCEPTION WHILE DESTROYING OBJECT " + pci );
                    e2.printStackTrace();
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
        final PooledConnectionAndInfo pci = this.pcMap.get ( pc );
        if ( pci == null ) {
            throw new IllegalStateException ( "close() was called on a Connection, but I have no record of the underlying PooledConnection." );
        }
        try {
            this._pool.invalidateObject ( pci );
        } catch ( Exception e ) {
            System.err.println ( "EXCEPTION WHILE DESTROYING OBJECT " + pci );
            e.printStackTrace();
        }
    }
    @Override
    public void invalidate ( final PooledConnection pc ) throws SQLException {
        final PooledConnectionAndInfo pci = this.pcMap.get ( pc );
        if ( pci == null ) {
            throw new IllegalStateException ( "close() was called on a Connection, but I have no record of the underlying PooledConnection." );
        }
        try {
            this._pool.invalidateObject ( pci );
            this._pool.close();
        } catch ( Exception ex ) {
            throw new SQLException ( "Error invalidating connection", ex );
        }
    }
    @Override
    public synchronized void setPassword ( final String password ) {
        this._password = password;
    }
    public void setMaxConnLifetimeMillis ( final long maxConnLifetimeMillis ) {
        this.maxConnLifetimeMillis = maxConnLifetimeMillis;
    }
    @Override
    public void closePool ( final String username ) throws SQLException {
        synchronized ( this ) {
            if ( username == null || !username.equals ( this._username ) ) {
                return;
            }
        }
        try {
            this._pool.close();
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
