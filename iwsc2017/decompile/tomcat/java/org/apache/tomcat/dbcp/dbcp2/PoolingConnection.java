package org.apache.tomcat.dbcp.dbcp2;
import org.apache.tomcat.dbcp.pool2.impl.DefaultPooledObject;
import org.apache.tomcat.dbcp.pool2.PooledObject;
import java.sql.CallableStatement;
import java.util.NoSuchElementException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.tomcat.dbcp.pool2.KeyedObjectPool;
import org.apache.tomcat.dbcp.pool2.KeyedPooledObjectFactory;
import java.sql.Connection;
public class PoolingConnection extends DelegatingConnection<Connection> implements KeyedPooledObjectFactory<PStmtKey, DelegatingPreparedStatement> {
    private KeyedObjectPool<PStmtKey, DelegatingPreparedStatement> _pstmtPool;
    public PoolingConnection ( final Connection c ) {
        super ( c );
        this._pstmtPool = null;
    }
    public void setStatementPool ( final KeyedObjectPool<PStmtKey, DelegatingPreparedStatement> pool ) {
        this._pstmtPool = pool;
    }
    @Override
    public synchronized void close() throws SQLException {
        try {
            if ( null != this._pstmtPool ) {
                final KeyedObjectPool<PStmtKey, DelegatingPreparedStatement> oldpool = this._pstmtPool;
                this._pstmtPool = null;
                try {
                    oldpool.close();
                } catch ( RuntimeException e ) {
                    throw e;
                } catch ( Exception e2 ) {
                    throw new SQLException ( "Cannot close connection", e2 );
                }
            }
        } finally {
            try {
                this.getDelegateInternal().close();
            } finally {
                this.setClosedInternal ( true );
            }
        }
    }
    @Override
    public PreparedStatement prepareStatement ( final String sql ) throws SQLException {
        if ( null == this._pstmtPool ) {
            throw new SQLException ( "Statement pool is null - closed or invalid PoolingConnection." );
        }
        try {
            return this._pstmtPool.borrowObject ( this.createKey ( sql ) );
        } catch ( NoSuchElementException e ) {
            throw new SQLException ( "MaxOpenPreparedStatements limit reached", e );
        } catch ( RuntimeException e2 ) {
            throw e2;
        } catch ( Exception e3 ) {
            throw new SQLException ( "Borrow prepareStatement from pool failed", e3 );
        }
    }
    @Override
    public PreparedStatement prepareStatement ( final String sql, final int autoGeneratedKeys ) throws SQLException {
        if ( null == this._pstmtPool ) {
            throw new SQLException ( "Statement pool is null - closed or invalid PoolingConnection." );
        }
        try {
            return this._pstmtPool.borrowObject ( this.createKey ( sql, autoGeneratedKeys ) );
        } catch ( NoSuchElementException e ) {
            throw new SQLException ( "MaxOpenPreparedStatements limit reached", e );
        } catch ( RuntimeException e2 ) {
            throw e2;
        } catch ( Exception e3 ) {
            throw new SQLException ( "Borrow prepareStatement from pool failed", e3 );
        }
    }
    @Override
    public PreparedStatement prepareStatement ( final String sql, final int resultSetType, final int resultSetConcurrency ) throws SQLException {
        if ( null == this._pstmtPool ) {
            throw new SQLException ( "Statement pool is null - closed or invalid PoolingConnection." );
        }
        try {
            return this._pstmtPool.borrowObject ( this.createKey ( sql, resultSetType, resultSetConcurrency ) );
        } catch ( NoSuchElementException e ) {
            throw new SQLException ( "MaxOpenPreparedStatements limit reached", e );
        } catch ( RuntimeException e2 ) {
            throw e2;
        } catch ( Exception e3 ) {
            throw new SQLException ( "Borrow prepareStatement from pool failed", e3 );
        }
    }
    @Override
    public CallableStatement prepareCall ( final String sql ) throws SQLException {
        try {
            return ( CallableStatement ) this._pstmtPool.borrowObject ( this.createKey ( sql, StatementType.CALLABLE_STATEMENT ) );
        } catch ( NoSuchElementException e ) {
            throw new SQLException ( "MaxOpenCallableStatements limit reached", e );
        } catch ( RuntimeException e2 ) {
            throw e2;
        } catch ( Exception e3 ) {
            throw new SQLException ( "Borrow callableStatement from pool failed", e3 );
        }
    }
    @Override
    public CallableStatement prepareCall ( final String sql, final int resultSetType, final int resultSetConcurrency ) throws SQLException {
        try {
            return ( CallableStatement ) this._pstmtPool.borrowObject ( this.createKey ( sql, resultSetType, resultSetConcurrency, StatementType.CALLABLE_STATEMENT ) );
        } catch ( NoSuchElementException e ) {
            throw new SQLException ( "MaxOpenCallableStatements limit reached", e );
        } catch ( RuntimeException e2 ) {
            throw e2;
        } catch ( Exception e3 ) {
            throw new SQLException ( "Borrow callableStatement from pool failed", e3 );
        }
    }
    protected PStmtKey createKey ( final String sql, final int autoGeneratedKeys ) {
        String catalog = null;
        try {
            catalog = this.getCatalog();
        } catch ( SQLException ex ) {}
        return new PStmtKey ( this.normalizeSQL ( sql ), catalog, autoGeneratedKeys );
    }
    protected PStmtKey createKey ( final String sql, final int resultSetType, final int resultSetConcurrency ) {
        String catalog = null;
        try {
            catalog = this.getCatalog();
        } catch ( SQLException ex ) {}
        return new PStmtKey ( this.normalizeSQL ( sql ), catalog, resultSetType, resultSetConcurrency );
    }
    protected PStmtKey createKey ( final String sql, final int resultSetType, final int resultSetConcurrency, final StatementType stmtType ) {
        String catalog = null;
        try {
            catalog = this.getCatalog();
        } catch ( SQLException ex ) {}
        return new PStmtKey ( this.normalizeSQL ( sql ), catalog, resultSetType, resultSetConcurrency, stmtType );
    }
    protected PStmtKey createKey ( final String sql ) {
        String catalog = null;
        try {
            catalog = this.getCatalog();
        } catch ( SQLException ex ) {}
        return new PStmtKey ( this.normalizeSQL ( sql ), catalog );
    }
    protected PStmtKey createKey ( final String sql, final StatementType stmtType ) {
        String catalog = null;
        try {
            catalog = this.getCatalog();
        } catch ( SQLException ex ) {}
        return new PStmtKey ( this.normalizeSQL ( sql ), catalog, stmtType, null );
    }
    protected String normalizeSQL ( final String sql ) {
        return sql.trim();
    }
    @Override
    public PooledObject<DelegatingPreparedStatement> makeObject ( final PStmtKey key ) throws Exception {
        if ( null == key ) {
            throw new IllegalArgumentException ( "Prepared statement key is null or invalid." );
        }
        if ( null == key.getResultSetType() && null == key.getResultSetConcurrency() && null == key.getAutoGeneratedKeys() ) {
            if ( key.getStmtType() == StatementType.PREPARED_STATEMENT ) {
                final PoolablePreparedStatement pps = new PoolablePreparedStatement ( this.getDelegate().prepareStatement ( key.getSql() ), ( K ) key, ( KeyedObjectPool<K, PoolablePreparedStatement<K>> ) this._pstmtPool, this );
                return new DefaultPooledObject<DelegatingPreparedStatement> ( pps );
            }
            return new DefaultPooledObject<DelegatingPreparedStatement> ( new PoolableCallableStatement ( this.getDelegate().prepareCall ( key.getSql() ), key, this._pstmtPool, this ) );
        } else {
            if ( null == key.getResultSetType() && null == key.getResultSetConcurrency() ) {
                final PoolablePreparedStatement pps = new PoolablePreparedStatement ( this.getDelegate().prepareStatement ( key.getSql(), key.getAutoGeneratedKeys() ), ( K ) key, ( KeyedObjectPool<K, PoolablePreparedStatement<K>> ) this._pstmtPool, this );
                return new DefaultPooledObject<DelegatingPreparedStatement> ( pps );
            }
            if ( key.getStmtType() == StatementType.PREPARED_STATEMENT ) {
                final PoolablePreparedStatement pps = new PoolablePreparedStatement ( this.getDelegate().prepareStatement ( key.getSql(), key.getResultSetType(), key.getResultSetConcurrency() ), ( K ) key, ( KeyedObjectPool<K, PoolablePreparedStatement<K>> ) this._pstmtPool, this );
                return new DefaultPooledObject<DelegatingPreparedStatement> ( pps );
            }
            return new DefaultPooledObject<DelegatingPreparedStatement> ( new PoolableCallableStatement ( this.getDelegate().prepareCall ( key.getSql(), key.getResultSetType(), key.getResultSetConcurrency() ), key, this._pstmtPool, this ) );
        }
    }
    @Override
    public void destroyObject ( final PStmtKey key, final PooledObject<DelegatingPreparedStatement> p ) throws Exception {
        p.getObject().getInnermostDelegate().close();
    }
    @Override
    public boolean validateObject ( final PStmtKey key, final PooledObject<DelegatingPreparedStatement> p ) {
        return true;
    }
    @Override
    public void activateObject ( final PStmtKey key, final PooledObject<DelegatingPreparedStatement> p ) throws Exception {
        p.getObject().activate();
    }
    @Override
    public void passivateObject ( final PStmtKey key, final PooledObject<DelegatingPreparedStatement> p ) throws Exception {
        final DelegatingPreparedStatement dps = p.getObject();
        dps.clearParameters();
        dps.passivate();
    }
    @Override
    public String toString() {
        if ( this._pstmtPool != null ) {
            return "PoolingConnection: " + this._pstmtPool.toString();
        }
        return "PoolingConnection: null";
    }
    protected enum StatementType {
        CALLABLE_STATEMENT,
        PREPARED_STATEMENT;
    }
}
