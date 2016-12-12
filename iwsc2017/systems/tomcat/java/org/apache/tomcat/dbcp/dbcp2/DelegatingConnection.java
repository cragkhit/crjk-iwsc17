package org.apache.tomcat.dbcp.dbcp2;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.ClientInfoStatus;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
public class DelegatingConnection<C extends Connection> extends AbandonedTrace
    implements Connection {
    private static final Map<String, ClientInfoStatus> EMPTY_FAILED_PROPERTIES =
        Collections.<String, ClientInfoStatus>emptyMap();
    private volatile C _conn = null;
    private volatile boolean _closed = false;
    private boolean _cacheState = true;
    private Boolean _autoCommitCached = null;
    private Boolean _readOnlyCached = null;
    private Integer defaultQueryTimeout = null;
    public DelegatingConnection ( final C c ) {
        super();
        _conn = c;
    }
    @Override
    public String toString() {
        String s = null;
        final Connection c = this.getInnermostDelegateInternal();
        if ( c != null ) {
            try {
                if ( c.isClosed() ) {
                    s = "connection is closed";
                } else {
                    final StringBuffer sb = new StringBuffer();
                    sb.append ( hashCode() );
                    final DatabaseMetaData meta = c.getMetaData();
                    if ( meta != null ) {
                        sb.append ( ", URL=" );
                        sb.append ( meta.getURL() );
                        sb.append ( ", UserName=" );
                        sb.append ( meta.getUserName() );
                        sb.append ( ", " );
                        sb.append ( meta.getDriverName() );
                        s = sb.toString();
                    }
                }
            } catch ( final SQLException ex ) {
            }
        }
        if ( s == null ) {
            s = super.toString();
        }
        return s;
    }
    public C getDelegate() {
        return getDelegateInternal();
    }
    protected final C getDelegateInternal() {
        return _conn;
    }
    public boolean innermostDelegateEquals ( final Connection c ) {
        final Connection innerCon = getInnermostDelegateInternal();
        if ( innerCon == null ) {
            return c == null;
        }
        return innerCon.equals ( c );
    }
    public Connection getInnermostDelegate() {
        return getInnermostDelegateInternal();
    }
    public final Connection getInnermostDelegateInternal() {
        Connection c = _conn;
        while ( c != null && c instanceof DelegatingConnection ) {
            c = ( ( DelegatingConnection<?> ) c ).getDelegateInternal();
            if ( this == c ) {
                return null;
            }
        }
        return c;
    }
    public void setDelegate ( final C c ) {
        _conn = c;
    }
    @Override
    public void close() throws SQLException {
        if ( !_closed ) {
            closeInternal();
        }
    }
    protected boolean isClosedInternal() {
        return _closed;
    }
    protected void setClosedInternal ( final boolean closed ) {
        this._closed = closed;
    }
    protected final void closeInternal() throws SQLException {
        try {
            passivate();
        } finally {
            if ( _conn != null ) {
                try {
                    _conn.close();
                } finally {
                    _closed = true;
                }
            } else {
                _closed = true;
            }
        }
    }
    protected void handleException ( final SQLException e ) throws SQLException {
        throw e;
    }
    private void initializeStatement ( final DelegatingStatement ds ) throws SQLException {
        if ( defaultQueryTimeout != null &&
                defaultQueryTimeout.intValue() != ds.getQueryTimeout() ) {
            ds.setQueryTimeout ( defaultQueryTimeout.intValue() );
        }
    }
    @Override
    public Statement createStatement() throws SQLException {
        checkOpen();
        try {
            final DelegatingStatement ds =
                new DelegatingStatement ( this, _conn.createStatement() );
            initializeStatement ( ds );
            return ds;
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public Statement createStatement ( final int resultSetType,
                                       final int resultSetConcurrency ) throws SQLException {
        checkOpen();
        try {
            final DelegatingStatement ds = new DelegatingStatement (
                this, _conn.createStatement ( resultSetType, resultSetConcurrency ) );
            initializeStatement ( ds );
            return ds;
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public PreparedStatement prepareStatement ( final String sql ) throws SQLException {
        checkOpen();
        try {
            final DelegatingPreparedStatement dps = new DelegatingPreparedStatement (
                this, _conn.prepareStatement ( sql ) );
            initializeStatement ( dps );
            return dps;
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public PreparedStatement prepareStatement ( final String sql,
            final int resultSetType,
            final int resultSetConcurrency ) throws SQLException {
        checkOpen();
        try {
            final DelegatingPreparedStatement dps = new DelegatingPreparedStatement (
                this, _conn.prepareStatement ( sql, resultSetType, resultSetConcurrency ) );
            initializeStatement ( dps );
            return dps;
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public CallableStatement prepareCall ( final String sql ) throws SQLException {
        checkOpen();
        try {
            final DelegatingCallableStatement dcs =
                new DelegatingCallableStatement ( this, _conn.prepareCall ( sql ) );
            initializeStatement ( dcs );
            return dcs;
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public CallableStatement prepareCall ( final String sql,
                                           final int resultSetType,
                                           final int resultSetConcurrency ) throws SQLException {
        checkOpen();
        try {
            final DelegatingCallableStatement dcs = new DelegatingCallableStatement (
                this, _conn.prepareCall ( sql, resultSetType, resultSetConcurrency ) );
            initializeStatement ( dcs );
            return dcs;
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public void clearWarnings() throws SQLException {
        checkOpen();
        try {
            _conn.clearWarnings();
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public void commit() throws SQLException {
        checkOpen();
        try {
            _conn.commit();
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    public boolean getCacheState() {
        return _cacheState;
    }
    @Override
    public boolean getAutoCommit() throws SQLException {
        checkOpen();
        if ( _cacheState && _autoCommitCached != null ) {
            return _autoCommitCached.booleanValue();
        }
        try {
            _autoCommitCached = Boolean.valueOf ( _conn.getAutoCommit() );
            return _autoCommitCached.booleanValue();
        } catch ( final SQLException e ) {
            handleException ( e );
            return false;
        }
    }
    @Override
    public String getCatalog() throws SQLException {
        checkOpen();
        try {
            return _conn.getCatalog();
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        checkOpen();
        try {
            return new DelegatingDatabaseMetaData ( this, _conn.getMetaData() );
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public int getTransactionIsolation() throws SQLException {
        checkOpen();
        try {
            return _conn.getTransactionIsolation();
        } catch ( final SQLException e ) {
            handleException ( e );
            return -1;
        }
    }
    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        checkOpen();
        try {
            return _conn.getTypeMap();
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public SQLWarning getWarnings() throws SQLException {
        checkOpen();
        try {
            return _conn.getWarnings();
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public boolean isReadOnly() throws SQLException {
        checkOpen();
        if ( _cacheState && _readOnlyCached != null ) {
            return _readOnlyCached.booleanValue();
        }
        try {
            _readOnlyCached = Boolean.valueOf ( _conn.isReadOnly() );
            return _readOnlyCached.booleanValue();
        } catch ( final SQLException e ) {
            handleException ( e );
            return false;
        }
    }
    @Override
    public String nativeSQL ( final String sql ) throws SQLException {
        checkOpen();
        try {
            return _conn.nativeSQL ( sql );
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public void rollback() throws SQLException {
        checkOpen();
        try {
            _conn.rollback();
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    public Integer getDefaultQueryTimeout() {
        return defaultQueryTimeout;
    }
    public void setDefaultQueryTimeout ( final Integer defaultQueryTimeout ) {
        this.defaultQueryTimeout = defaultQueryTimeout;
    }
    public void setCacheState ( final boolean cacheState ) {
        this._cacheState = cacheState;
    }
    public void clearCachedState() {
        _autoCommitCached = null;
        _readOnlyCached = null;
        if ( _conn instanceof DelegatingConnection ) {
            ( ( DelegatingConnection<?> ) _conn ).clearCachedState();
        }
    }
    @Override
    public void setAutoCommit ( final boolean autoCommit ) throws SQLException {
        checkOpen();
        try {
            _conn.setAutoCommit ( autoCommit );
            if ( _cacheState ) {
                _autoCommitCached = Boolean.valueOf ( autoCommit );
            }
        } catch ( final SQLException e ) {
            _autoCommitCached = null;
            handleException ( e );
        }
    }
    @Override
    public void setCatalog ( final String catalog ) throws SQLException {
        checkOpen();
        try {
            _conn.setCatalog ( catalog );
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public void setReadOnly ( final boolean readOnly ) throws SQLException {
        checkOpen();
        try {
            _conn.setReadOnly ( readOnly );
            if ( _cacheState ) {
                _readOnlyCached = Boolean.valueOf ( readOnly );
            }
        } catch ( final SQLException e ) {
            _readOnlyCached = null;
            handleException ( e );
        }
    }
    @Override
    public void setTransactionIsolation ( final int level ) throws SQLException {
        checkOpen();
        try {
            _conn.setTransactionIsolation ( level );
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public void setTypeMap ( final Map<String, Class<?>> map ) throws SQLException {
        checkOpen();
        try {
            _conn.setTypeMap ( map );
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public boolean isClosed() throws SQLException {
        return _closed || _conn == null || _conn.isClosed();
    }
    protected void checkOpen() throws SQLException {
        if ( _closed ) {
            if ( null != _conn ) {
                String label = "";
                try {
                    label = _conn.toString();
                } catch ( final Exception ex ) {
                }
                throw new SQLException
                ( "Connection " + label + " is closed." );
            }
            throw new SQLException
            ( "Connection is null." );
        }
    }
    protected void activate() {
        _closed = false;
        setLastUsed();
        if ( _conn instanceof DelegatingConnection ) {
            ( ( DelegatingConnection<?> ) _conn ).activate();
        }
    }
    protected void passivate() throws SQLException {
        final List<AbandonedTrace> traces = getTrace();
        if ( traces != null && traces.size() > 0 ) {
            final Iterator<AbandonedTrace> traceIter = traces.iterator();
            while ( traceIter.hasNext() ) {
                final Object trace = traceIter.next();
                if ( trace instanceof Statement ) {
                    ( ( Statement ) trace ).close();
                } else if ( trace instanceof ResultSet ) {
                    ( ( ResultSet ) trace ).close();
                }
            }
            clearTrace();
        }
        setLastUsed ( 0 );
    }
    @Override
    public int getHoldability() throws SQLException {
        checkOpen();
        try {
            return _conn.getHoldability();
        } catch ( final SQLException e ) {
            handleException ( e );
            return 0;
        }
    }
    @Override
    public void setHoldability ( final int holdability ) throws SQLException {
        checkOpen();
        try {
            _conn.setHoldability ( holdability );
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public Savepoint setSavepoint() throws SQLException {
        checkOpen();
        try {
            return _conn.setSavepoint();
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public Savepoint setSavepoint ( final String name ) throws SQLException {
        checkOpen();
        try {
            return _conn.setSavepoint ( name );
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public void rollback ( final Savepoint savepoint ) throws SQLException {
        checkOpen();
        try {
            _conn.rollback ( savepoint );
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public void releaseSavepoint ( final Savepoint savepoint )
    throws SQLException {
        checkOpen();
        try {
            _conn.releaseSavepoint ( savepoint );
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public Statement createStatement ( final int resultSetType,
                                       final int resultSetConcurrency,
                                       final int resultSetHoldability ) throws SQLException {
        checkOpen();
        try {
            final DelegatingStatement ds = new DelegatingStatement ( this,
                    _conn.createStatement ( resultSetType, resultSetConcurrency,
                                            resultSetHoldability ) );
            initializeStatement ( ds );
            return ds;
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public PreparedStatement prepareStatement ( final String sql, final int resultSetType,
            final int resultSetConcurrency,
            final int resultSetHoldability ) throws SQLException {
        checkOpen();
        try {
            final DelegatingPreparedStatement dps = new DelegatingPreparedStatement (
                this, _conn.prepareStatement ( sql, resultSetType,
                                               resultSetConcurrency, resultSetHoldability ) );
            initializeStatement ( dps );
            return dps;
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public CallableStatement prepareCall ( final String sql, final int resultSetType,
                                           final int resultSetConcurrency,
                                           final int resultSetHoldability ) throws SQLException {
        checkOpen();
        try {
            final DelegatingCallableStatement dcs = new DelegatingCallableStatement (
                this, _conn.prepareCall ( sql, resultSetType,
                                          resultSetConcurrency, resultSetHoldability ) );
            initializeStatement ( dcs );
            return dcs;
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public PreparedStatement prepareStatement ( final String sql, final int autoGeneratedKeys ) throws SQLException {
        checkOpen();
        try {
            final DelegatingPreparedStatement dps = new DelegatingPreparedStatement (
                this, _conn.prepareStatement ( sql, autoGeneratedKeys ) );
            initializeStatement ( dps );
            return dps;
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public PreparedStatement prepareStatement ( final String sql, final int columnIndexes[] ) throws SQLException {
        checkOpen();
        try {
            final DelegatingPreparedStatement dps = new DelegatingPreparedStatement (
                this, _conn.prepareStatement ( sql, columnIndexes ) );
            initializeStatement ( dps );
            return dps;
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public PreparedStatement prepareStatement ( final String sql, final String columnNames[] ) throws SQLException {
        checkOpen();
        try {
            final DelegatingPreparedStatement dps =  new DelegatingPreparedStatement (
                this, _conn.prepareStatement ( sql, columnNames ) );
            initializeStatement ( dps );
            return dps;
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public boolean isWrapperFor ( final Class<?> iface ) throws SQLException {
        if ( iface.isAssignableFrom ( getClass() ) ) {
            return true;
        } else if ( iface.isAssignableFrom ( _conn.getClass() ) ) {
            return true;
        } else {
            return _conn.isWrapperFor ( iface );
        }
    }
    @Override
    public <T> T unwrap ( final Class<T> iface ) throws SQLException {
        if ( iface.isAssignableFrom ( getClass() ) ) {
            return iface.cast ( this );
        } else if ( iface.isAssignableFrom ( _conn.getClass() ) ) {
            return iface.cast ( _conn );
        } else {
            return _conn.unwrap ( iface );
        }
    }
    @Override
    public Array createArrayOf ( final String typeName, final Object[] elements ) throws SQLException {
        checkOpen();
        try {
            return _conn.createArrayOf ( typeName, elements );
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public Blob createBlob() throws SQLException {
        checkOpen();
        try {
            return _conn.createBlob();
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public Clob createClob() throws SQLException {
        checkOpen();
        try {
            return _conn.createClob();
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public NClob createNClob() throws SQLException {
        checkOpen();
        try {
            return _conn.createNClob();
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public SQLXML createSQLXML() throws SQLException {
        checkOpen();
        try {
            return _conn.createSQLXML();
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public Struct createStruct ( final String typeName, final Object[] attributes ) throws SQLException {
        checkOpen();
        try {
            return _conn.createStruct ( typeName, attributes );
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public boolean isValid ( final int timeout ) throws SQLException {
        if ( isClosed() ) {
            return false;
        }
        try {
            return _conn.isValid ( timeout );
        } catch ( final SQLException e ) {
            handleException ( e );
            return false;
        }
    }
    @Override
    public void setClientInfo ( final String name, final String value ) throws SQLClientInfoException {
        try {
            checkOpen();
            _conn.setClientInfo ( name, value );
        } catch ( final SQLClientInfoException e ) {
            throw e;
        } catch ( final SQLException e ) {
            throw new SQLClientInfoException ( "Connection is closed.", EMPTY_FAILED_PROPERTIES, e );
        }
    }
    @Override
    public void setClientInfo ( final Properties properties ) throws SQLClientInfoException {
        try {
            checkOpen();
            _conn.setClientInfo ( properties );
        } catch ( final SQLClientInfoException e ) {
            throw e;
        } catch ( final SQLException e ) {
            throw new SQLClientInfoException ( "Connection is closed.", EMPTY_FAILED_PROPERTIES, e );
        }
    }
    @Override
    public Properties getClientInfo() throws SQLException {
        checkOpen();
        try {
            return _conn.getClientInfo();
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public String getClientInfo ( final String name ) throws SQLException {
        checkOpen();
        try {
            return _conn.getClientInfo ( name );
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public void setSchema ( final String schema ) throws SQLException {
        checkOpen();
        try {
            _conn.setSchema ( schema );
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public String getSchema() throws SQLException {
        checkOpen();
        try {
            return _conn.getSchema();
        } catch ( final SQLException e ) {
            handleException ( e );
            return null;
        }
    }
    @Override
    public void abort ( final Executor executor ) throws SQLException {
        checkOpen();
        try {
            _conn.abort ( executor );
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public void setNetworkTimeout ( final Executor executor, final int milliseconds )
    throws SQLException {
        checkOpen();
        try {
            _conn.setNetworkTimeout ( executor, milliseconds );
        } catch ( final SQLException e ) {
            handleException ( e );
        }
    }
    @Override
    public int getNetworkTimeout() throws SQLException {
        checkOpen();
        try {
            return _conn.getNetworkTimeout();
        } catch ( final SQLException e ) {
            handleException ( e );
            return 0;
        }
    }
}
