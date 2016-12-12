package org.apache.tomcat.dbcp.dbcp2.cpdsadapter;
import org.apache.tomcat.dbcp.pool2.impl.DefaultPooledObject;
import org.apache.tomcat.dbcp.pool2.PooledObject;
import java.sql.PreparedStatement;
import javax.sql.ConnectionEvent;
import java.sql.SQLException;
import org.apache.tomcat.dbcp.pool2.KeyedObjectPool;
import javax.sql.StatementEventListener;
import javax.sql.ConnectionEventListener;
import java.util.Vector;
import org.apache.tomcat.dbcp.dbcp2.DelegatingConnection;
import java.sql.Connection;
import org.apache.tomcat.dbcp.dbcp2.PoolablePreparedStatement;
import org.apache.tomcat.dbcp.pool2.KeyedPooledObjectFactory;
import javax.sql.PooledConnection;
class PooledConnectionImpl implements PooledConnection, KeyedPooledObjectFactory<PStmtKeyCPDS, PoolablePreparedStatement<PStmtKeyCPDS>> {
    private static final String CLOSED = "Attempted to use PooledConnection after closed() was called.";
    private Connection connection;
    private final DelegatingConnection<?> delegatingConnection;
    private Connection logicalConnection;
    private final Vector<ConnectionEventListener> eventListeners;
    private final Vector<StatementEventListener> statementEventListeners;
    private boolean isClosed;
    private KeyedObjectPool<PStmtKeyCPDS, PoolablePreparedStatement<PStmtKeyCPDS>> pstmtPool;
    private boolean accessToUnderlyingConnectionAllowed;
    PooledConnectionImpl ( final Connection connection ) {
        this.connection = null;
        this.logicalConnection = null;
        this.statementEventListeners = new Vector<StatementEventListener>();
        this.pstmtPool = null;
        this.accessToUnderlyingConnectionAllowed = false;
        this.connection = connection;
        if ( connection instanceof DelegatingConnection ) {
            this.delegatingConnection = ( DelegatingConnection<?> ) connection;
        } else {
            this.delegatingConnection = new DelegatingConnection<Object> ( connection );
        }
        this.eventListeners = new Vector<ConnectionEventListener>();
        this.isClosed = false;
    }
    public void setStatementPool ( final KeyedObjectPool<PStmtKeyCPDS, PoolablePreparedStatement<PStmtKeyCPDS>> statementPool ) {
        this.pstmtPool = statementPool;
    }
    @Override
    public void addConnectionEventListener ( final ConnectionEventListener listener ) {
        if ( !this.eventListeners.contains ( listener ) ) {
            this.eventListeners.add ( listener );
        }
    }
    @Override
    public void addStatementEventListener ( final StatementEventListener listener ) {
        if ( !this.statementEventListeners.contains ( listener ) ) {
            this.statementEventListeners.add ( listener );
        }
    }
    @Override
    public void close() throws SQLException {
        this.assertOpen();
        this.isClosed = true;
        try {
            if ( this.pstmtPool != null ) {
                try {
                    this.pstmtPool.close();
                } finally {
                    this.pstmtPool = null;
                }
            }
        } catch ( RuntimeException e ) {
            throw e;
        } catch ( Exception e2 ) {
            throw new SQLException ( "Cannot close connection (return to pool failed)", e2 );
        } finally {
            try {
                this.connection.close();
            } finally {
                this.connection = null;
            }
        }
    }
    private void assertOpen() throws SQLException {
        if ( this.isClosed ) {
            throw new SQLException ( "Attempted to use PooledConnection after closed() was called." );
        }
    }
    @Override
    public Connection getConnection() throws SQLException {
        this.assertOpen();
        if ( this.logicalConnection != null && !this.logicalConnection.isClosed() ) {
            throw new SQLException ( "PooledConnection was reused, without its previous Connection being closed." );
        }
        return this.logicalConnection = new ConnectionImpl ( this, this.connection, this.isAccessToUnderlyingConnectionAllowed() );
    }
    @Override
    public void removeConnectionEventListener ( final ConnectionEventListener listener ) {
        this.eventListeners.remove ( listener );
    }
    @Override
    public void removeStatementEventListener ( final StatementEventListener listener ) {
        this.statementEventListeners.remove ( listener );
    }
    @Override
    protected void finalize() throws Throwable {
        try {
            this.connection.close();
        } catch ( Exception ex ) {}
        if ( this.logicalConnection != null && !this.logicalConnection.isClosed() ) {
            throw new SQLException ( "PooledConnection was gc'ed, withoutits last Connection being closed." );
        }
    }
    void notifyListeners() {
        final ConnectionEvent event = new ConnectionEvent ( this );
        final Object[] array;
        final Object[] listeners = array = this.eventListeners.toArray();
        for ( final Object listener : array ) {
            ( ( ConnectionEventListener ) listener ).connectionClosed ( event );
        }
    }
    PreparedStatement prepareStatement ( final String sql ) throws SQLException {
        if ( this.pstmtPool == null ) {
            return this.connection.prepareStatement ( sql );
        }
        try {
            return this.pstmtPool.borrowObject ( this.createKey ( sql ) );
        } catch ( RuntimeException e ) {
            throw e;
        } catch ( Exception e2 ) {
            throw new SQLException ( "Borrow prepareStatement from pool failed", e2 );
        }
    }
    PreparedStatement prepareStatement ( final String sql, final int resultSetType, final int resultSetConcurrency ) throws SQLException {
        if ( this.pstmtPool == null ) {
            return this.connection.prepareStatement ( sql, resultSetType, resultSetConcurrency );
        }
        try {
            return this.pstmtPool.borrowObject ( this.createKey ( sql, resultSetType, resultSetConcurrency ) );
        } catch ( RuntimeException e ) {
            throw e;
        } catch ( Exception e2 ) {
            throw new SQLException ( "Borrow prepareStatement from pool failed", e2 );
        }
    }
    PreparedStatement prepareStatement ( final String sql, final int autoGeneratedKeys ) throws SQLException {
        if ( this.pstmtPool == null ) {
            return this.connection.prepareStatement ( sql, autoGeneratedKeys );
        }
        try {
            return this.pstmtPool.borrowObject ( this.createKey ( sql, autoGeneratedKeys ) );
        } catch ( RuntimeException e ) {
            throw e;
        } catch ( Exception e2 ) {
            throw new SQLException ( "Borrow prepareStatement from pool failed", e2 );
        }
    }
    PreparedStatement prepareStatement ( final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability ) throws SQLException {
        if ( this.pstmtPool == null ) {
            return this.connection.prepareStatement ( sql, resultSetType, resultSetConcurrency, resultSetHoldability );
        }
        try {
            return this.pstmtPool.borrowObject ( this.createKey ( sql, resultSetType, resultSetConcurrency, resultSetHoldability ) );
        } catch ( RuntimeException e ) {
            throw e;
        } catch ( Exception e2 ) {
            throw new SQLException ( "Borrow prepareStatement from pool failed", e2 );
        }
    }
    PreparedStatement prepareStatement ( final String sql, final int[] columnIndexes ) throws SQLException {
        if ( this.pstmtPool == null ) {
            return this.connection.prepareStatement ( sql, columnIndexes );
        }
        try {
            return this.pstmtPool.borrowObject ( this.createKey ( sql, columnIndexes ) );
        } catch ( RuntimeException e ) {
            throw e;
        } catch ( Exception e2 ) {
            throw new SQLException ( "Borrow prepareStatement from pool failed", e2 );
        }
    }
    PreparedStatement prepareStatement ( final String sql, final String[] columnNames ) throws SQLException {
        if ( this.pstmtPool == null ) {
            return this.connection.prepareStatement ( sql, columnNames );
        }
        try {
            return this.pstmtPool.borrowObject ( this.createKey ( sql, columnNames ) );
        } catch ( RuntimeException e ) {
            throw e;
        } catch ( Exception e2 ) {
            throw new SQLException ( "Borrow prepareStatement from pool failed", e2 );
        }
    }
    protected PStmtKeyCPDS createKey ( final String sql, final int autoGeneratedKeys ) {
        return new PStmtKeyCPDS ( this.normalizeSQL ( sql ), autoGeneratedKeys );
    }
    protected PStmtKeyCPDS createKey ( final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability ) {
        return new PStmtKeyCPDS ( this.normalizeSQL ( sql ), resultSetType, resultSetConcurrency, resultSetHoldability );
    }
    protected PStmtKeyCPDS createKey ( final String sql, final int[] columnIndexes ) {
        return new PStmtKeyCPDS ( this.normalizeSQL ( sql ), columnIndexes );
    }
    protected PStmtKeyCPDS createKey ( final String sql, final String[] columnNames ) {
        return new PStmtKeyCPDS ( this.normalizeSQL ( sql ), columnNames );
    }
    protected PStmtKeyCPDS createKey ( final String sql, final int resultSetType, final int resultSetConcurrency ) {
        return new PStmtKeyCPDS ( this.normalizeSQL ( sql ), resultSetType, resultSetConcurrency );
    }
    protected PStmtKeyCPDS createKey ( final String sql ) {
        return new PStmtKeyCPDS ( this.normalizeSQL ( sql ) );
    }
    protected String normalizeSQL ( final String sql ) {
        return sql.trim();
    }
    @Override
    public PooledObject<PoolablePreparedStatement<PStmtKeyCPDS>> makeObject ( final PStmtKeyCPDS key ) throws Exception {
        if ( null == key ) {
            throw new IllegalArgumentException();
        }
        if ( null != key.getResultSetType() || null != key.getResultSetConcurrency() ) {
            return new DefaultPooledObject<PoolablePreparedStatement<PStmtKeyCPDS>> ( new PoolablePreparedStatement<PStmtKeyCPDS> ( this.connection.prepareStatement ( key.getSql(), key.getResultSetType(), key.getResultSetConcurrency() ), key, this.pstmtPool, this.delegatingConnection ) );
        }
        if ( null == key.getAutoGeneratedKeys() ) {
            return new DefaultPooledObject<PoolablePreparedStatement<PStmtKeyCPDS>> ( new PoolablePreparedStatement<PStmtKeyCPDS> ( this.connection.prepareStatement ( key.getSql() ), key, this.pstmtPool, this.delegatingConnection ) );
        }
        return new DefaultPooledObject<PoolablePreparedStatement<PStmtKeyCPDS>> ( new PoolablePreparedStatement<PStmtKeyCPDS> ( this.connection.prepareStatement ( key.getSql(), key.getAutoGeneratedKeys() ), key, this.pstmtPool, this.delegatingConnection ) );
    }
    @Override
    public void destroyObject ( final PStmtKeyCPDS key, final PooledObject<PoolablePreparedStatement<PStmtKeyCPDS>> p ) throws Exception {
        p.getObject().getInnermostDelegate().close();
    }
    @Override
    public boolean validateObject ( final PStmtKeyCPDS key, final PooledObject<PoolablePreparedStatement<PStmtKeyCPDS>> p ) {
        return true;
    }
    @Override
    public void activateObject ( final PStmtKeyCPDS key, final PooledObject<PoolablePreparedStatement<PStmtKeyCPDS>> p ) throws Exception {
        p.getObject().activate();
    }
    @Override
    public void passivateObject ( final PStmtKeyCPDS key, final PooledObject<PoolablePreparedStatement<PStmtKeyCPDS>> p ) throws Exception {
        final PoolablePreparedStatement<PStmtKeyCPDS> ppss = p.getObject();
        ppss.clearParameters();
        ppss.passivate();
    }
    public synchronized boolean isAccessToUnderlyingConnectionAllowed() {
        return this.accessToUnderlyingConnectionAllowed;
    }
    public synchronized void setAccessToUnderlyingConnectionAllowed ( final boolean allow ) {
        this.accessToUnderlyingConnectionAllowed = allow;
    }
}
