package org.apache.tomcat.dbcp.dbcp2;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import org.apache.tomcat.dbcp.pool2.KeyedObjectPool;
public class PoolablePreparedStatement<K> extends DelegatingPreparedStatement {
    private final KeyedObjectPool<K, PoolablePreparedStatement<K>> _pool;
    private final K _key;
    private volatile boolean batchAdded;
    public PoolablePreparedStatement ( final PreparedStatement stmt, final K key, final KeyedObjectPool<K, PoolablePreparedStatement<K>> pool, final DelegatingConnection<?> conn ) {
        super ( conn, stmt );
        this.batchAdded = false;
        this._pool = pool;
        this._key = key;
        if ( this.getConnectionInternal() != null ) {
            this.getConnectionInternal().removeTrace ( this );
        }
    }
    @Override
    public void addBatch() throws SQLException {
        super.addBatch();
        this.batchAdded = true;
    }
    @Override
    public void clearBatch() throws SQLException {
        this.batchAdded = false;
        super.clearBatch();
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
                throw new SQLException ( "Cannot close preparedstatement (return to pool failed)", e3 );
            }
        }
    }
    public void activate() throws SQLException {
        this.setClosedInternal ( false );
        if ( this.getConnectionInternal() != null ) {
            this.getConnectionInternal().addTrace ( this );
        }
        super.activate();
    }
    public void passivate() throws SQLException {
        if ( this.batchAdded ) {
            this.clearBatch();
        }
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
