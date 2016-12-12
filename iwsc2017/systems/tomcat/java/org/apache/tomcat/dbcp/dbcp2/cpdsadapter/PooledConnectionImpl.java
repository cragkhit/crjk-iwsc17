package org.apache.tomcat.dbcp.dbcp2.cpdsadapter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Vector;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;
import org.apache.tomcat.dbcp.dbcp2.DelegatingConnection;
import org.apache.tomcat.dbcp.dbcp2.PoolablePreparedStatement;
import org.apache.tomcat.dbcp.pool2.KeyedObjectPool;
import org.apache.tomcat.dbcp.pool2.KeyedPooledObjectFactory;
import org.apache.tomcat.dbcp.pool2.PooledObject;
import org.apache.tomcat.dbcp.pool2.impl.DefaultPooledObject;
class PooledConnectionImpl
    implements PooledConnection, KeyedPooledObjectFactory<PStmtKeyCPDS, PoolablePreparedStatement<PStmtKeyCPDS>> {
    private static final String CLOSED
        = "Attempted to use PooledConnection after closed() was called.";
    private Connection connection = null;
    private final DelegatingConnection<?> delegatingConnection;
    private Connection logicalConnection = null;
    private final Vector<ConnectionEventListener> eventListeners;
    private final Vector<StatementEventListener> statementEventListeners =
        new Vector<>();
    private boolean isClosed;
    private KeyedObjectPool<PStmtKeyCPDS, PoolablePreparedStatement<PStmtKeyCPDS>> pstmtPool = null;
    private boolean accessToUnderlyingConnectionAllowed = false;
    PooledConnectionImpl ( final Connection connection ) {
        this.connection = connection;
        if ( connection instanceof DelegatingConnection ) {
            this.delegatingConnection = ( DelegatingConnection<?> ) connection;
        } else {
            this.delegatingConnection = new DelegatingConnection<> ( connection );
        }
        eventListeners = new Vector<>();
        isClosed = false;
    }
    public void setStatementPool (
        final KeyedObjectPool<PStmtKeyCPDS, PoolablePreparedStatement<PStmtKeyCPDS>> statementPool ) {
        pstmtPool = statementPool;
    }
    @Override
    public void addConnectionEventListener ( final ConnectionEventListener listener ) {
        if ( !eventListeners.contains ( listener ) ) {
            eventListeners.add ( listener );
        }
    }
    @Override
    public void addStatementEventListener ( final StatementEventListener listener ) {
        if ( !statementEventListeners.contains ( listener ) ) {
            statementEventListeners.add ( listener );
        }
    }
    @Override
    public void close() throws SQLException {
        assertOpen();
        isClosed = true;
        try {
            if ( pstmtPool != null ) {
                try {
                    pstmtPool.close();
                } finally {
                    pstmtPool = null;
                }
            }
        } catch ( final RuntimeException e ) {
            throw e;
        } catch ( final Exception e ) {
            throw new SQLException ( "Cannot close connection (return to pool failed)", e );
        } finally {
            try {
                connection.close();
            } finally {
                connection = null;
            }
        }
    }
    private void assertOpen() throws SQLException {
        if ( isClosed ) {
            throw new SQLException ( CLOSED );
        }
    }
    @Override
    public Connection getConnection() throws SQLException {
        assertOpen();
        if ( logicalConnection != null && !logicalConnection.isClosed() ) {
            throw new SQLException ( "PooledConnection was reused, without "
                                     + "its previous Connection being closed." );
        }
        logicalConnection = new ConnectionImpl (
            this, connection, isAccessToUnderlyingConnectionAllowed() );
        return logicalConnection;
    }
    @Override
    public void removeConnectionEventListener (
        final ConnectionEventListener listener ) {
        eventListeners.remove ( listener );
    }
    @Override
    public void removeStatementEventListener ( final StatementEventListener listener ) {
        statementEventListeners.remove ( listener );
    }
    @Override
    protected void finalize() throws Throwable {
        try {
            connection.close();
        } catch ( final Exception ignored ) {
        }
        if ( logicalConnection != null && !logicalConnection.isClosed() ) {
            throw new SQLException ( "PooledConnection was gc'ed, without"
                                     + "its last Connection being closed." );
        }
    }
    void notifyListeners() {
        final ConnectionEvent event = new ConnectionEvent ( this );
        final Object[] listeners = eventListeners.toArray();
        for ( final Object listener : listeners ) {
            ( ( ConnectionEventListener ) listener ).connectionClosed ( event );
        }
    }
    PreparedStatement prepareStatement ( final String sql ) throws SQLException {
        if ( pstmtPool == null ) {
            return connection.prepareStatement ( sql );
        }
        try {
            return pstmtPool.borrowObject ( createKey ( sql ) );
        } catch ( final RuntimeException e ) {
            throw e;
        } catch ( final Exception e ) {
            throw new SQLException ( "Borrow prepareStatement from pool failed", e );
        }
    }
    PreparedStatement prepareStatement ( final String sql, final int resultSetType,
                                         final int resultSetConcurrency )
    throws SQLException {
        if ( pstmtPool == null ) {
            return connection.prepareStatement ( sql, resultSetType, resultSetConcurrency );
        }
        try {
            return pstmtPool.borrowObject (
                       createKey ( sql, resultSetType, resultSetConcurrency ) );
        } catch ( final RuntimeException e ) {
            throw e;
        } catch ( final Exception e ) {
            throw new SQLException ( "Borrow prepareStatement from pool failed", e );
        }
    }
    PreparedStatement prepareStatement ( final String sql, final int autoGeneratedKeys )
    throws SQLException {
        if ( pstmtPool == null ) {
            return connection.prepareStatement ( sql, autoGeneratedKeys );
        }
        try {
            return pstmtPool.borrowObject ( createKey ( sql, autoGeneratedKeys ) );
        } catch ( final RuntimeException e ) {
            throw e;
        } catch ( final Exception e ) {
            throw new SQLException ( "Borrow prepareStatement from pool failed", e );
        }
    }
    PreparedStatement prepareStatement ( final String sql, final int resultSetType,
                                         final int resultSetConcurrency, final int resultSetHoldability )
    throws SQLException {
        if ( pstmtPool == null ) {
            return connection.prepareStatement ( sql, resultSetType,
                                                 resultSetConcurrency, resultSetHoldability );
        }
        try {
            return pstmtPool.borrowObject ( createKey ( sql, resultSetType,
                                            resultSetConcurrency, resultSetHoldability ) );
        } catch ( final RuntimeException e ) {
            throw e;
        } catch ( final Exception e ) {
            throw new SQLException ( "Borrow prepareStatement from pool failed", e );
        }
    }
    PreparedStatement prepareStatement ( final String sql, final int columnIndexes[] )
    throws SQLException {
        if ( pstmtPool == null ) {
            return connection.prepareStatement ( sql, columnIndexes );
        }
        try {
            return pstmtPool.borrowObject ( createKey ( sql, columnIndexes ) );
        } catch ( final RuntimeException e ) {
            throw e;
        } catch ( final Exception e ) {
            throw new SQLException ( "Borrow prepareStatement from pool failed", e );
        }
    }
    PreparedStatement prepareStatement ( final String sql, final String columnNames[] )
    throws SQLException {
        if ( pstmtPool == null ) {
            return connection.prepareStatement ( sql, columnNames );
        }
        try {
            return pstmtPool.borrowObject ( createKey ( sql, columnNames ) );
        } catch ( final RuntimeException e ) {
            throw e;
        } catch ( final Exception e ) {
            throw new SQLException ( "Borrow prepareStatement from pool failed", e );
        }
    }
    protected PStmtKeyCPDS createKey ( final String sql, final int autoGeneratedKeys ) {
        return new PStmtKeyCPDS ( normalizeSQL ( sql ), autoGeneratedKeys );
    }
    protected PStmtKeyCPDS createKey ( final String sql, final int resultSetType,
                                       final int resultSetConcurrency, final int resultSetHoldability ) {
        return new PStmtKeyCPDS ( normalizeSQL ( sql ), resultSetType,
                                  resultSetConcurrency, resultSetHoldability );
    }
    protected PStmtKeyCPDS createKey ( final String sql, final int columnIndexes[] ) {
        return new PStmtKeyCPDS ( normalizeSQL ( sql ), columnIndexes );
    }
    protected PStmtKeyCPDS createKey ( final String sql, final String columnNames[] ) {
        return new PStmtKeyCPDS ( normalizeSQL ( sql ), columnNames );
    }
    protected PStmtKeyCPDS createKey ( final String sql, final int resultSetType,
                                       final int resultSetConcurrency ) {
        return new PStmtKeyCPDS ( normalizeSQL ( sql ), resultSetType,
                                  resultSetConcurrency );
    }
    protected PStmtKeyCPDS createKey ( final String sql ) {
        return new PStmtKeyCPDS ( normalizeSQL ( sql ) );
    }
    protected String normalizeSQL ( final String sql ) {
        return sql.trim();
    }
    @Override
    public PooledObject<PoolablePreparedStatement<PStmtKeyCPDS>> makeObject ( final PStmtKeyCPDS key ) throws Exception {
        if ( null == key ) {
            throw new IllegalArgumentException();
        }
        if ( null == key.getResultSetType()
                && null == key.getResultSetConcurrency() ) {
            if ( null == key.getAutoGeneratedKeys() ) {
                return new DefaultPooledObject<> ( new PoolablePreparedStatement<> (
                                                       connection.prepareStatement ( key.getSql() ),
                                                       key, pstmtPool, delegatingConnection ) );
            }
            return new DefaultPooledObject<> ( new PoolablePreparedStatement<> (
                                                   connection.prepareStatement ( key.getSql(),
                                                           key.getAutoGeneratedKeys().intValue() ),
                                                   key, pstmtPool, delegatingConnection ) );
        }
        return new DefaultPooledObject<> ( new PoolablePreparedStatement<> (
                                               connection.prepareStatement ( key.getSql(),
                                                       key.getResultSetType().intValue(),
                                                       key.getResultSetConcurrency().intValue() ),
                                               key, pstmtPool, delegatingConnection ) );
    }
    @Override
    public void destroyObject ( final PStmtKeyCPDS key,
                                final PooledObject<PoolablePreparedStatement<PStmtKeyCPDS>> p )
    throws Exception {
        p.getObject().getInnermostDelegate().close();
    }
    @Override
    public boolean validateObject ( final PStmtKeyCPDS key,
                                    final PooledObject<PoolablePreparedStatement<PStmtKeyCPDS>> p ) {
        return true;
    }
    @Override
    public void activateObject ( final PStmtKeyCPDS key,
                                 final PooledObject<PoolablePreparedStatement<PStmtKeyCPDS>> p )
    throws Exception {
        p.getObject().activate();
    }
    @Override
    public void passivateObject ( final PStmtKeyCPDS key,
                                  final PooledObject<PoolablePreparedStatement<PStmtKeyCPDS>> p )
    throws Exception {
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
