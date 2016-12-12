package org.apache.tomcat.dbcp.dbcp2;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.List;
public class DelegatingStatement extends AbandonedTrace implements Statement {
    private Statement _stmt = null;
    private DelegatingConnection<?> _conn = null;
    public DelegatingStatement ( final DelegatingConnection<?> c, final Statement s ) {
        super ( c );
        _stmt = s;
        _conn = c;
    }
    public Statement getDelegate() {
        return _stmt;
    }
    public Statement getInnermostDelegate() {
        Statement s = _stmt;
        while ( s != null && s instanceof DelegatingStatement ) {
            s = ( ( DelegatingStatement ) s ).getDelegate();
            if ( this == s ) {
                return null;
            }
        }
        return s;
    }
    public void setDelegate ( final Statement s ) {
        _stmt = s;
    }
    private boolean _closed = false;
    protected boolean isClosedInternal() {
        return _closed;
    }
    protected void setClosedInternal ( final boolean closed ) {
        this._closed = closed;
    }
    protected void checkOpen() throws SQLException {
        if ( isClosed() ) {
            throw new SQLException
            ( this.getClass().getName() + " with address: \"" +
              this.toString() + "\" is closed." );
        }
    }
    @Override
    public void close() throws SQLException {
        if ( isClosed() ) {
            return;
        }
        try {
            try {
                if ( _conn != null ) {
                    _conn.removeTrace ( this );
                    _conn = null;
                }
                final List<AbandonedTrace> resultSets = getTrace();
                if ( resultSets != null ) {
                    final ResultSet[] set = resultSets.toArray ( new ResultSet[resultSets.size()] );
                    for ( final ResultSet element : set ) {
                        element.close();
                    }
                    clearTrace();
                }
                if ( _stmt != null ) {
                    _stmt.close();
                }
            } catch ( final SQLException e ) {
                handleException ( e );
            }
        } finally {
            _closed = true;
            _stmt = null;
        }
    }
    protected void handleException ( final SQLException e ) throws SQLException {
        if ( _conn != null ) {
            _conn.handleException ( e );
        } else {
            throw e;
        }
    }
    protected void activate() throws SQLException {
        if ( _stmt instanceof DelegatingStatement ) {
            ( ( DelegatingStatement ) _stmt ).activate();
        }
    }
    protected void passivate() throws SQLException {
        if ( _stmt instanceof DelegatingStatement ) {
            ( ( DelegatingStatement ) _stmt ).passivate();
        }
    }
    @Override
    public Connection getConnection() throws SQLException {
        checkOpen();
        return getConnectionInternal();
    }
    protected DelegatingConnection<?> getConnectionInternal() {
        return _conn;
    }
    @Override
    public ResultSet executeQuery ( final String sql ) throws SQLException {
        checkOpen();
        if ( _conn != null ) {
            _conn.setLastUsed();
        }
        try {
            return DelegatingResultSet.wrapResultSet ( this, _stmt.executeQuery ( sql ) );
        } catch ( final SQLException e ) {
            handleException ( e );
            throw new AssertionError();
        }
    }
    @Override
    public ResultSet getResultSet() throws SQLException {
        checkOpen();
        try {
            return DelegatingResultSet.wrapResultSet ( this, _stmt.getResultSet() );
        } catch ( final SQLException e ) {
            handleException ( e );
            throw new AssertionError();
        }
    }
    @Override
    public int executeUpdate ( final String sql ) throws SQLException {
        checkOpen();
        if ( _conn != null ) {
            _conn.setLastUsed();
        }
        try {
            return _stmt.executeUpdate ( sql );
        } catch ( final SQLException e ) {
            handleException ( e );
            return 0;
        }
    }
    @Override
    public int getMaxFieldSize() throws SQLException {
        checkOpen();
        try {
            return _stmt.getMaxFieldSize();
        } catch ( final SQLException e ) {
            handleException ( e );
            return 0;
        }
    }
    @Override
    public void setMaxFieldSize ( final int max ) throws SQLException {
        checkOpen();
        try {
            _stmt.setMaxFieldSize ( max );
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public int getMaxRows() throws SQLException {
        checkOpen();
        try {
            return _stmt.getMaxRows();
        } catch ( final SQLException e ) {
            handleException ( e );
            return 0;
        }
    }
    @Override
    public void setMaxRows ( final int max ) throws SQLException {
        checkOpen();
        try {
            _stmt.setMaxRows ( max );
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public void setEscapeProcessing ( final boolean enable ) throws SQLException {
        checkOpen();
        try {
            _stmt.setEscapeProcessing ( enable );
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public int getQueryTimeout() throws SQLException {
        checkOpen();
        try {
            return _stmt.getQueryTimeout();
        } catch ( final SQLException e ) {
            handleException ( e );
            return 0;
        }
    }
    @Override
    public void setQueryTimeout ( final int seconds ) throws SQLException {
        checkOpen();
        try {
            _stmt.setQueryTimeout ( seconds );
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public void cancel() throws SQLException {
        checkOpen();
        try {
            _stmt.cancel();
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public SQLWarning getWarnings() throws SQLException {
        checkOpen();
        try {
            return _stmt.getWarnings();
        } catch ( final SQLException e ) {
            handleException ( e );
            throw new AssertionError();
        }
    }
    @Override
    public void clearWarnings() throws SQLException {
        checkOpen();
        try {
            _stmt.clearWarnings();
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public void setCursorName ( final String name ) throws SQLException {
        checkOpen();
        try {
            _stmt.setCursorName ( name );
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public boolean execute ( final String sql ) throws SQLException {
        checkOpen();
        if ( _conn != null ) {
            _conn.setLastUsed();
        }
        try {
            return _stmt.execute ( sql );
        } catch ( final SQLException e ) {
            handleException ( e );
            return false;
        }
    }
    @Override
    public int getUpdateCount() throws SQLException {
        checkOpen();
        try {
            return _stmt.getUpdateCount();
        } catch ( final SQLException e ) {
            handleException ( e );
            return 0;
        }
    }
    @Override
    public boolean getMoreResults() throws SQLException {
        checkOpen();
        try {
            return _stmt.getMoreResults();
        } catch ( final SQLException e ) {
            handleException ( e );
            return false;
        }
    }
    @Override
    public void setFetchDirection ( final int direction ) throws SQLException {
        checkOpen();
        try {
            _stmt.setFetchDirection ( direction );
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public int getFetchDirection() throws SQLException {
        checkOpen();
        try {
            return _stmt.getFetchDirection();
        } catch ( final SQLException e ) {
            handleException ( e );
            return 0;
        }
    }
    @Override
    public void setFetchSize ( final int rows ) throws SQLException {
        checkOpen();
        try {
            _stmt.setFetchSize ( rows );
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public int getFetchSize() throws SQLException {
        checkOpen();
        try {
            return _stmt.getFetchSize();
        } catch ( final SQLException e ) {
            handleException ( e );
            return 0;
        }
    }
    @Override
    public int getResultSetConcurrency() throws SQLException {
        checkOpen();
        try {
            return _stmt.getResultSetConcurrency();
        } catch ( final SQLException e ) {
            handleException ( e );
            return 0;
        }
    }
    @Override
    public int getResultSetType() throws SQLException {
        checkOpen();
        try {
            return _stmt.getResultSetType();
        } catch ( final SQLException e ) {
            handleException ( e );
            return 0;
        }
    }
    @Override
    public void addBatch ( final String sql ) throws SQLException {
        checkOpen();
        try {
            _stmt.addBatch ( sql );
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public void clearBatch() throws SQLException {
        checkOpen();
        try {
            _stmt.clearBatch();
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public int[] executeBatch() throws SQLException {
        checkOpen();
        if ( _conn != null ) {
            _conn.setLastUsed();
        }
        try {
            return _stmt.executeBatch();
        } catch ( final SQLException e ) {
            handleException ( e );
            throw new AssertionError();
        }
    }
    @Override
    public String toString() {
        return _stmt == null ? "NULL" : _stmt.toString();
    }
    @Override
    public boolean getMoreResults ( final int current ) throws SQLException {
        checkOpen();
        try {
            return _stmt.getMoreResults ( current );
        } catch ( final SQLException e ) {
            handleException ( e );
            return false;
        }
    }
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        checkOpen();
        try {
            return DelegatingResultSet.wrapResultSet ( this, _stmt.getGeneratedKeys() );
        } catch ( final SQLException e ) {
            handleException ( e );
            throw new AssertionError();
        }
    }
    @Override
    public int executeUpdate ( final String sql, final int autoGeneratedKeys ) throws SQLException {
        checkOpen();
        if ( _conn != null ) {
            _conn.setLastUsed();
        }
        try {
            return _stmt.executeUpdate ( sql, autoGeneratedKeys );
        } catch ( final SQLException e ) {
            handleException ( e );
            return 0;
        }
    }
    @Override
    public int executeUpdate ( final String sql, final int columnIndexes[] ) throws SQLException {
        checkOpen();
        if ( _conn != null ) {
            _conn.setLastUsed();
        }
        try {
            return _stmt.executeUpdate ( sql, columnIndexes );
        } catch ( final SQLException e ) {
            handleException ( e );
            return 0;
        }
    }
    @Override
    public int executeUpdate ( final String sql, final String columnNames[] ) throws SQLException {
        checkOpen();
        if ( _conn != null ) {
            _conn.setLastUsed();
        }
        try {
            return _stmt.executeUpdate ( sql, columnNames );
        } catch ( final SQLException e ) {
            handleException ( e );
            return 0;
        }
    }
    @Override
    public boolean execute ( final String sql, final int autoGeneratedKeys ) throws SQLException {
        checkOpen();
        if ( _conn != null ) {
            _conn.setLastUsed();
        }
        try {
            return _stmt.execute ( sql, autoGeneratedKeys );
        } catch ( final SQLException e ) {
            handleException ( e );
            return false;
        }
    }
    @Override
    public boolean execute ( final String sql, final int columnIndexes[] ) throws SQLException {
        checkOpen();
        if ( _conn != null ) {
            _conn.setLastUsed();
        }
        try {
            return _stmt.execute ( sql, columnIndexes );
        } catch ( final SQLException e ) {
            handleException ( e );
            return false;
        }
    }
    @Override
    public boolean execute ( final String sql, final String columnNames[] ) throws SQLException {
        checkOpen();
        if ( _conn != null ) {
            _conn.setLastUsed();
        }
        try {
            return _stmt.execute ( sql, columnNames );
        } catch ( final SQLException e ) {
            handleException ( e );
            return false;
        }
    }
    @Override
    public int getResultSetHoldability() throws SQLException {
        checkOpen();
        try {
            return _stmt.getResultSetHoldability();
        } catch ( final SQLException e ) {
            handleException ( e );
            return 0;
        }
    }
    @Override
    public boolean isClosed() throws SQLException {
        return _closed;
    }
    @Override
    public boolean isWrapperFor ( final Class<?> iface ) throws SQLException {
        if ( iface.isAssignableFrom ( getClass() ) ) {
            return true;
        } else if ( iface.isAssignableFrom ( _stmt.getClass() ) ) {
            return true;
        } else {
            return _stmt.isWrapperFor ( iface );
        }
    }
    @Override
    public <T> T unwrap ( final Class<T> iface ) throws SQLException {
        if ( iface.isAssignableFrom ( getClass() ) ) {
            return iface.cast ( this );
        } else if ( iface.isAssignableFrom ( _stmt.getClass() ) ) {
            return iface.cast ( _stmt );
        } else {
            return _stmt.unwrap ( iface );
        }
    }
    @Override
    public void setPoolable ( final boolean poolable ) throws SQLException {
        checkOpen();
        try {
            _stmt.setPoolable ( poolable );
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public boolean isPoolable() throws SQLException {
        checkOpen();
        try {
            return _stmt.isPoolable();
        } catch ( final SQLException e ) {
            handleException ( e );
            return false;
        }
    }
    @Override
    public void closeOnCompletion() throws SQLException {
        checkOpen();
        try {
            _stmt.closeOnCompletion();
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        checkOpen();
        try {
            return _stmt.isCloseOnCompletion();
        } catch ( final SQLException e ) {
            handleException ( e );
            return false;
        }
    }
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
