package org.apache.tomcat.dbcp.dbcp2;
import java.sql.SQLWarning;
import java.sql.Connection;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
public class DelegatingStatement extends AbandonedTrace implements Statement {
    private Statement _stmt;
    private DelegatingConnection<?> _conn;
    private boolean _closed;
    public DelegatingStatement ( final DelegatingConnection<?> c, final Statement s ) {
        super ( c );
        this._stmt = null;
        this._conn = null;
        this._closed = false;
        this._stmt = s;
        this._conn = c;
    }
    public Statement getDelegate() {
        return this._stmt;
    }
    public Statement getInnermostDelegate() {
        Statement s = this._stmt;
        while ( s != null && s instanceof DelegatingStatement ) {
            s = ( ( DelegatingStatement ) s ).getDelegate();
            if ( this == s ) {
                return null;
            }
        }
        return s;
    }
    public void setDelegate ( final Statement s ) {
        this._stmt = s;
    }
    protected boolean isClosedInternal() {
        return this._closed;
    }
    protected void setClosedInternal ( final boolean closed ) {
        this._closed = closed;
    }
    protected void checkOpen() throws SQLException {
        if ( this.isClosed() ) {
            throw new SQLException ( this.getClass().getName() + " with address: \"" + this.toString() + "\" is closed." );
        }
    }
    @Override
    public void close() throws SQLException {
        if ( this.isClosed() ) {
            return;
        }
        try {
            if ( this._conn != null ) {
                this._conn.removeTrace ( this );
                this._conn = null;
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
            if ( this._stmt != null ) {
                this._stmt.close();
            }
        } catch ( SQLException e ) {
            this.handleException ( e );
        } finally {
            this._closed = true;
            this._stmt = null;
        }
    }
    protected void handleException ( final SQLException e ) throws SQLException {
        if ( this._conn != null ) {
            this._conn.handleException ( e );
            return;
        }
        throw e;
    }
    protected void activate() throws SQLException {
        if ( this._stmt instanceof DelegatingStatement ) {
            ( ( DelegatingStatement ) this._stmt ).activate();
        }
    }
    protected void passivate() throws SQLException {
        if ( this._stmt instanceof DelegatingStatement ) {
            ( ( DelegatingStatement ) this._stmt ).passivate();
        }
    }
    @Override
    public Connection getConnection() throws SQLException {
        this.checkOpen();
        return this.getConnectionInternal();
    }
    protected DelegatingConnection<?> getConnectionInternal() {
        return this._conn;
    }
    @Override
    public ResultSet executeQuery ( final String sql ) throws SQLException {
        this.checkOpen();
        if ( this._conn != null ) {
            this._conn.setLastUsed();
        }
        try {
            return DelegatingResultSet.wrapResultSet ( this, this._stmt.executeQuery ( sql ) );
        } catch ( SQLException e ) {
            this.handleException ( e );
            throw new AssertionError();
        }
    }
    @Override
    public ResultSet getResultSet() throws SQLException {
        this.checkOpen();
        try {
            return DelegatingResultSet.wrapResultSet ( this, this._stmt.getResultSet() );
        } catch ( SQLException e ) {
            this.handleException ( e );
            throw new AssertionError();
        }
    }
    @Override
    public int executeUpdate ( final String sql ) throws SQLException {
        this.checkOpen();
        if ( this._conn != null ) {
            this._conn.setLastUsed();
        }
        try {
            return this._stmt.executeUpdate ( sql );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public int getMaxFieldSize() throws SQLException {
        this.checkOpen();
        try {
            return this._stmt.getMaxFieldSize();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public void setMaxFieldSize ( final int max ) throws SQLException {
        this.checkOpen();
        try {
            this._stmt.setMaxFieldSize ( max );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public int getMaxRows() throws SQLException {
        this.checkOpen();
        try {
            return this._stmt.getMaxRows();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public void setMaxRows ( final int max ) throws SQLException {
        this.checkOpen();
        try {
            this._stmt.setMaxRows ( max );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setEscapeProcessing ( final boolean enable ) throws SQLException {
        this.checkOpen();
        try {
            this._stmt.setEscapeProcessing ( enable );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public int getQueryTimeout() throws SQLException {
        this.checkOpen();
        try {
            return this._stmt.getQueryTimeout();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public void setQueryTimeout ( final int seconds ) throws SQLException {
        this.checkOpen();
        try {
            this._stmt.setQueryTimeout ( seconds );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void cancel() throws SQLException {
        this.checkOpen();
        try {
            this._stmt.cancel();
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public SQLWarning getWarnings() throws SQLException {
        this.checkOpen();
        try {
            return this._stmt.getWarnings();
        } catch ( SQLException e ) {
            this.handleException ( e );
            throw new AssertionError();
        }
    }
    @Override
    public void clearWarnings() throws SQLException {
        this.checkOpen();
        try {
            this._stmt.clearWarnings();
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setCursorName ( final String name ) throws SQLException {
        this.checkOpen();
        try {
            this._stmt.setCursorName ( name );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public boolean execute ( final String sql ) throws SQLException {
        this.checkOpen();
        if ( this._conn != null ) {
            this._conn.setLastUsed();
        }
        try {
            return this._stmt.execute ( sql );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public int getUpdateCount() throws SQLException {
        this.checkOpen();
        try {
            return this._stmt.getUpdateCount();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public boolean getMoreResults() throws SQLException {
        this.checkOpen();
        try {
            return this._stmt.getMoreResults();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public void setFetchDirection ( final int direction ) throws SQLException {
        this.checkOpen();
        try {
            this._stmt.setFetchDirection ( direction );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public int getFetchDirection() throws SQLException {
        this.checkOpen();
        try {
            return this._stmt.getFetchDirection();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public void setFetchSize ( final int rows ) throws SQLException {
        this.checkOpen();
        try {
            this._stmt.setFetchSize ( rows );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public int getFetchSize() throws SQLException {
        this.checkOpen();
        try {
            return this._stmt.getFetchSize();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public int getResultSetConcurrency() throws SQLException {
        this.checkOpen();
        try {
            return this._stmt.getResultSetConcurrency();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public int getResultSetType() throws SQLException {
        this.checkOpen();
        try {
            return this._stmt.getResultSetType();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public void addBatch ( final String sql ) throws SQLException {
        this.checkOpen();
        try {
            this._stmt.addBatch ( sql );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void clearBatch() throws SQLException {
        this.checkOpen();
        try {
            this._stmt.clearBatch();
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public int[] executeBatch() throws SQLException {
        this.checkOpen();
        if ( this._conn != null ) {
            this._conn.setLastUsed();
        }
        try {
            return this._stmt.executeBatch();
        } catch ( SQLException e ) {
            this.handleException ( e );
            throw new AssertionError();
        }
    }
    @Override
    public String toString() {
        return ( this._stmt == null ) ? "NULL" : this._stmt.toString();
    }
    @Override
    public boolean getMoreResults ( final int current ) throws SQLException {
        this.checkOpen();
        try {
            return this._stmt.getMoreResults ( current );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        this.checkOpen();
        try {
            return DelegatingResultSet.wrapResultSet ( this, this._stmt.getGeneratedKeys() );
        } catch ( SQLException e ) {
            this.handleException ( e );
            throw new AssertionError();
        }
    }
    @Override
    public int executeUpdate ( final String sql, final int autoGeneratedKeys ) throws SQLException {
        this.checkOpen();
        if ( this._conn != null ) {
            this._conn.setLastUsed();
        }
        try {
            return this._stmt.executeUpdate ( sql, autoGeneratedKeys );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public int executeUpdate ( final String sql, final int[] columnIndexes ) throws SQLException {
        this.checkOpen();
        if ( this._conn != null ) {
            this._conn.setLastUsed();
        }
        try {
            return this._stmt.executeUpdate ( sql, columnIndexes );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public int executeUpdate ( final String sql, final String[] columnNames ) throws SQLException {
        this.checkOpen();
        if ( this._conn != null ) {
            this._conn.setLastUsed();
        }
        try {
            return this._stmt.executeUpdate ( sql, columnNames );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public boolean execute ( final String sql, final int autoGeneratedKeys ) throws SQLException {
        this.checkOpen();
        if ( this._conn != null ) {
            this._conn.setLastUsed();
        }
        try {
            return this._stmt.execute ( sql, autoGeneratedKeys );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public boolean execute ( final String sql, final int[] columnIndexes ) throws SQLException {
        this.checkOpen();
        if ( this._conn != null ) {
            this._conn.setLastUsed();
        }
        try {
            return this._stmt.execute ( sql, columnIndexes );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public boolean execute ( final String sql, final String[] columnNames ) throws SQLException {
        this.checkOpen();
        if ( this._conn != null ) {
            this._conn.setLastUsed();
        }
        try {
            return this._stmt.execute ( sql, columnNames );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public int getResultSetHoldability() throws SQLException {
        this.checkOpen();
        try {
            return this._stmt.getResultSetHoldability();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public boolean isClosed() throws SQLException {
        return this._closed;
    }
    @Override
    public boolean isWrapperFor ( final Class<?> iface ) throws SQLException {
        return iface.isAssignableFrom ( this.getClass() ) || iface.isAssignableFrom ( this._stmt.getClass() ) || this._stmt.isWrapperFor ( iface );
    }
    @Override
    public <T> T unwrap ( final Class<T> iface ) throws SQLException {
        if ( iface.isAssignableFrom ( this.getClass() ) ) {
            return iface.cast ( this );
        }
        if ( iface.isAssignableFrom ( this._stmt.getClass() ) ) {
            return iface.cast ( this._stmt );
        }
        return this._stmt.unwrap ( iface );
    }
    @Override
    public void setPoolable ( final boolean poolable ) throws SQLException {
        this.checkOpen();
        try {
            this._stmt.setPoolable ( poolable );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public boolean isPoolable() throws SQLException {
        this.checkOpen();
        try {
            return this._stmt.isPoolable();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public void closeOnCompletion() throws SQLException {
        this.checkOpen();
        try {
            this._stmt.closeOnCompletion();
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        this.checkOpen();
        try {
            return this._stmt.isCloseOnCompletion();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
}
