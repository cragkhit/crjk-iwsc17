package org.apache.tomcat.dbcp.dbcp2;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.tomcat.dbcp.pool2.KeyedObjectPool;
public class PoolableCallableStatement extends DelegatingCallableStatement {
    private final KeyedObjectPool<PStmtKey, DelegatingPreparedStatement> _pool;
    private final PStmtKey _key;
    public PoolableCallableStatement ( final CallableStatement stmt, final PStmtKey key,
                                       final KeyedObjectPool<PStmtKey, DelegatingPreparedStatement> pool,
                                       final DelegatingConnection<Connection> conn ) {
        super ( conn, stmt );
        _pool = pool;
        _key = key;
        if ( getConnectionInternal() != null ) {
            getConnectionInternal().removeTrace ( this );
        }
    }
    @Override
    public void close() throws SQLException {
        if ( !isClosed() ) {
            try {
                _pool.returnObject ( _key, this );
            } catch ( final SQLException e ) {
                throw e;
            } catch ( final RuntimeException e ) {
                throw e;
            } catch ( final Exception e ) {
                throw new SQLException ( "Cannot close CallableStatement (return to pool failed)", e );
            }
        }
    }
    @Override
    protected void activate() throws SQLException {
        setClosedInternal ( false );
        if ( getConnectionInternal() != null ) {
            getConnectionInternal().addTrace ( this );
        }
        super.activate();
    }
    @Override
    protected void passivate() throws SQLException {
        setClosedInternal ( true );
        if ( getConnectionInternal() != null ) {
            getConnectionInternal().removeTrace ( this );
        }
        final List<AbandonedTrace> resultSets = getTrace();
        if ( resultSets != null ) {
            final ResultSet[] set = resultSets.toArray ( new ResultSet[resultSets.size()] );
            for ( final ResultSet element : set ) {
                element.close();
            }
            clearTrace();
        }
        super.passivate();
    }
}
