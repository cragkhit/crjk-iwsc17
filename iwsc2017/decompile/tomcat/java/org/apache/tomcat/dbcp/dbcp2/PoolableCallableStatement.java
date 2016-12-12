package org.apache.tomcat.dbcp.dbcp2;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.CallableStatement;
import org.apache.tomcat.dbcp.pool2.KeyedObjectPool;
public class PoolableCallableStatement extends DelegatingCallableStatement {
    private final KeyedObjectPool<PStmtKey, DelegatingPreparedStatement> _pool;
    private final PStmtKey _key;
    public PoolableCallableStatement ( final CallableStatement stmt, final PStmtKey key, final KeyedObjectPool<PStmtKey, DelegatingPreparedStatement> pool, final DelegatingConnection<Connection> conn ) {
        super ( conn, stmt );
        this._pool = pool;
        this._key = key;
        if ( this.getConnectionInternal() != null ) {
            this.getConnectionInternal().removeTrace ( this );
        }
    }
    @Override
    public void close() throws SQLException {
        if ( !this.isClosed() ) {
            try {
                this._pool.returnObject ( this._key, this );
            } catch ( SQLException e ) {
                throw e;
            } catch ( RuntimeException e2 ) {
                throw e2;
            } catch ( Exception e3 ) {
                throw new SQLException ( "Cannot close CallableStatement (return to pool failed)", e3 );
            }
        }
    }
    @Override
    protected void activate() throws SQLException {
        this.setClosedInternal ( false );
        if ( this.getConnectionInternal() != null ) {
            this.getConnectionInternal().addTrace ( this );
        }
        super.activate();
    }
    @Override
    protected void passivate() throws SQLException {
        this.setClosedInternal ( true );
        if ( this.getConnectionInternal() != null ) {
            this.getConnectionInternal().removeTrace ( this );
        }
        final List<AbandonedTrace> resultSets = this.getTrace();
        if ( resultSets != null ) {
            final ResultSet[] array;
            final ResultSet[] set = array = resultSets.toArray ( new ResultSet[resultSets.size()] );
            for ( final ResultSet element : array ) {
                element.close();
            }
            this.clearTrace();
        }
        super.passivate();
    }
}
