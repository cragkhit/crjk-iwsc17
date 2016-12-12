package org.apache.tomcat.dbcp.dbcp2;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.tomcat.dbcp.pool2.KeyedObjectPool;
public class PoolablePreparedStatement<K> extends DelegatingPreparedStatement {
    private final KeyedObjectPool<K, PoolablePreparedStatement<K>> _pool;
    private final K _key;
    private volatile boolean batchAdded = false;
    public PoolablePreparedStatement ( final PreparedStatement stmt, final K key,
                                       final KeyedObjectPool<K, PoolablePreparedStatement<K>> pool,
                                       final DelegatingConnection<?> conn ) {
        super ( conn, stmt );
        _pool = pool;
        _key = key;
        if ( getConnectionInternal() != null ) {
            getConnectionInternal().removeTrace ( this );
        }
    }
    @Override
    public void addBatch() throws SQLException {
        super.addBatch();
        batchAdded = true;
    }
    @Override
    public void clearBatch() throws SQLException {
        batchAdded = false;
        super.clearBatch();
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
                throw new SQLException ( "Cannot close preparedstatement (return to pool failed)", e );
            }
        }
    }
    @Override
    public void activate() throws SQLException {
        setClosedInternal ( false );
        if ( getConnectionInternal() != null ) {
            getConnectionInternal().addTrace ( this );
        }
        super.activate();
    }
    @Override
    public void passivate() throws SQLException {
        if ( batchAdded ) {
            clearBatch();
        }
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
