package org.apache.tomcat.dbcp.dbcp2;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.Properties;
import java.sql.SQLClientInfoException;
import java.sql.Struct;
import java.sql.SQLXML;
import java.sql.NClob;
import java.sql.Clob;
import java.sql.Blob;
import java.sql.Array;
import java.sql.Savepoint;
import java.util.Iterator;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLWarning;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.ClientInfoStatus;
import java.util.Map;
import java.sql.Connection;
public class DelegatingConnection<C extends Connection> extends AbandonedTrace implements Connection {
    private static final Map<String, ClientInfoStatus> EMPTY_FAILED_PROPERTIES;
    private volatile C _conn;
    private volatile boolean _closed;
    private boolean _cacheState;
    private Boolean _autoCommitCached;
    private Boolean _readOnlyCached;
    private Integer defaultQueryTimeout;
    public DelegatingConnection ( final C c ) {
        this._conn = null;
        this._closed = false;
        this._cacheState = true;
        this._autoCommitCached = null;
        this._readOnlyCached = null;
        this.defaultQueryTimeout = null;
        this._conn = c;
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
                    sb.append ( this.hashCode() );
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
            } catch ( SQLException ex ) {}
        }
        if ( s == null ) {
            s = super.toString();
        }
        return s;
    }
    public C getDelegate() {
        return this.getDelegateInternal();
    }
    protected final C getDelegateInternal() {
        return this._conn;
    }
    public boolean innermostDelegateEquals ( final Connection c ) {
        final Connection innerCon = this.getInnermostDelegateInternal();
        if ( innerCon == null ) {
            return c == null;
        }
        return innerCon.equals ( c );
    }
    public Connection getInnermostDelegate() {
        return this.getInnermostDelegateInternal();
    }
    public final Connection getInnermostDelegateInternal() {
        Connection c = this._conn;
        while ( c != null && c instanceof DelegatingConnection ) {
            c = ( ( DelegatingConnection ) c ).getDelegateInternal();
            if ( this == c ) {
                return null;
            }
        }
        return c;
    }
    public void setDelegate ( final C c ) {
        this._conn = c;
    }
    @Override
    public void close() throws SQLException {
        if ( !this._closed ) {
            this.closeInternal();
        }
    }
    protected boolean isClosedInternal() {
        return this._closed;
    }
    protected void setClosedInternal ( final boolean closed ) {
        this._closed = closed;
    }
    protected final void closeInternal() throws SQLException {
        try {
            this.passivate();
        } finally {
            if ( this._conn != null ) {
                try {
                    this._conn.close();
                } finally {
                    this._closed = true;
                }
            } else {
                this._closed = true;
            }
        }
    }
    protected void handleException ( final SQLException e ) throws SQLException {
        throw e;
    }
    private void initializeStatement ( final DelegatingStatement ds ) throws SQLException {
        if ( this.defaultQueryTimeout != null && this.defaultQueryTimeout != ds.getQueryTimeout() ) {
            ds.setQueryTimeout ( this.defaultQueryTimeout );
        }
    }
    @Override
    public Statement createStatement() throws SQLException {
        this.checkOpen();
        try {
            final DelegatingStatement ds = new DelegatingStatement ( this, this._conn.createStatement() );
            this.initializeStatement ( ds );
            return ds;
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Statement createStatement ( final int resultSetType, final int resultSetConcurrency ) throws SQLException {
        this.checkOpen();
        try {
            final DelegatingStatement ds = new DelegatingStatement ( this, this._conn.createStatement ( resultSetType, resultSetConcurrency ) );
            this.initializeStatement ( ds );
            return ds;
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public PreparedStatement prepareStatement ( final String sql ) throws SQLException {
        this.checkOpen();
        try {
            final DelegatingPreparedStatement dps = new DelegatingPreparedStatement ( this, this._conn.prepareStatement ( sql ) );
            this.initializeStatement ( dps );
            return dps;
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public PreparedStatement prepareStatement ( final String sql, final int resultSetType, final int resultSetConcurrency ) throws SQLException {
        this.checkOpen();
        try {
            final DelegatingPreparedStatement dps = new DelegatingPreparedStatement ( this, this._conn.prepareStatement ( sql, resultSetType, resultSetConcurrency ) );
            this.initializeStatement ( dps );
            return dps;
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public CallableStatement prepareCall ( final String sql ) throws SQLException {
        this.checkOpen();
        try {
            final DelegatingCallableStatement dcs = new DelegatingCallableStatement ( this, this._conn.prepareCall ( sql ) );
            this.initializeStatement ( dcs );
            return dcs;
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public CallableStatement prepareCall ( final String sql, final int resultSetType, final int resultSetConcurrency ) throws SQLException {
        this.checkOpen();
        try {
            final DelegatingCallableStatement dcs = new DelegatingCallableStatement ( this, this._conn.prepareCall ( sql, resultSetType, resultSetConcurrency ) );
            this.initializeStatement ( dcs );
            return dcs;
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public void clearWarnings() throws SQLException {
        this.checkOpen();
        try {
            this._conn.clearWarnings();
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void commit() throws SQLException {
        this.checkOpen();
        try {
            this._conn.commit();
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    public boolean getCacheState() {
        return this._cacheState;
    }
    @Override
    public boolean getAutoCommit() throws SQLException {
        this.checkOpen();
        if ( this._cacheState && this._autoCommitCached != null ) {
            return this._autoCommitCached;
        }
        try {
            this._autoCommitCached = this._conn.getAutoCommit();
            return this._autoCommitCached;
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public String getCatalog() throws SQLException {
        this.checkOpen();
        try {
            return this._conn.getCatalog();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        this.checkOpen();
        try {
            return new DelegatingDatabaseMetaData ( this, this._conn.getMetaData() );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public int getTransactionIsolation() throws SQLException {
        this.checkOpen();
        try {
            return this._conn.getTransactionIsolation();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return -1;
        }
    }
    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        this.checkOpen();
        try {
            return this._conn.getTypeMap();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public SQLWarning getWarnings() throws SQLException {
        this.checkOpen();
        try {
            return this._conn.getWarnings();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public boolean isReadOnly() throws SQLException {
        this.checkOpen();
        if ( this._cacheState && this._readOnlyCached != null ) {
            return this._readOnlyCached;
        }
        try {
            this._readOnlyCached = this._conn.isReadOnly();
            return this._readOnlyCached;
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public String nativeSQL ( final String sql ) throws SQLException {
        this.checkOpen();
        try {
            return this._conn.nativeSQL ( sql );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public void rollback() throws SQLException {
        this.checkOpen();
        try {
            this._conn.rollback();
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    public Integer getDefaultQueryTimeout() {
        return this.defaultQueryTimeout;
    }
    public void setDefaultQueryTimeout ( final Integer defaultQueryTimeout ) {
        this.defaultQueryTimeout = defaultQueryTimeout;
    }
    public void setCacheState ( final boolean cacheState ) {
        this._cacheState = cacheState;
    }
    public void clearCachedState() {
        this._autoCommitCached = null;
        this._readOnlyCached = null;
        if ( this._conn instanceof DelegatingConnection ) {
            ( ( DelegatingConnection ) this._conn ).clearCachedState();
        }
    }
    @Override
    public void setAutoCommit ( final boolean autoCommit ) throws SQLException {
        this.checkOpen();
        try {
            this._conn.setAutoCommit ( autoCommit );
            if ( this._cacheState ) {
                this._autoCommitCached = autoCommit;
            }
        } catch ( SQLException e ) {
            this._autoCommitCached = null;
            this.handleException ( e );
        }
    }
    @Override
    public void setCatalog ( final String catalog ) throws SQLException {
        this.checkOpen();
        try {
            this._conn.setCatalog ( catalog );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setReadOnly ( final boolean readOnly ) throws SQLException {
        this.checkOpen();
        try {
            this._conn.setReadOnly ( readOnly );
            if ( this._cacheState ) {
                this._readOnlyCached = readOnly;
            }
        } catch ( SQLException e ) {
            this._readOnlyCached = null;
            this.handleException ( e );
        }
    }
    @Override
    public void setTransactionIsolation ( final int level ) throws SQLException {
        this.checkOpen();
        try {
            this._conn.setTransactionIsolation ( level );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setTypeMap ( final Map<String, Class<?>> map ) throws SQLException {
        this.checkOpen();
        try {
            this._conn.setTypeMap ( map );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public boolean isClosed() throws SQLException {
        return this._closed || this._conn == null || this._conn.isClosed();
    }
    protected void checkOpen() throws SQLException {
        if ( !this._closed ) {
            return;
        }
        if ( null != this._conn ) {
            String label = "";
            try {
                label = this._conn.toString();
            } catch ( Exception ex ) {}
            throw new SQLException ( "Connection " + label + " is closed." );
        }
        throw new SQLException ( "Connection is null." );
    }
    protected void activate() {
        this._closed = false;
        this.setLastUsed();
        if ( this._conn instanceof DelegatingConnection ) {
            ( ( DelegatingConnection ) this._conn ).activate();
        }
    }
    protected void passivate() throws SQLException {
        final List<AbandonedTrace> traces = this.getTrace();
        if ( traces != null && traces.size() > 0 ) {
            for ( final Object trace : traces ) {
                if ( trace instanceof Statement ) {
                    ( ( Statement ) trace ).close();
                } else {
                    if ( ! ( trace instanceof ResultSet ) ) {
                        continue;
                    }
                    ( ( ResultSet ) trace ).close();
                }
            }
            this.clearTrace();
        }
        this.setLastUsed ( 0L );
    }
    @Override
    public int getHoldability() throws SQLException {
        this.checkOpen();
        try {
            return this._conn.getHoldability();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    @Override
    public void setHoldability ( final int holdability ) throws SQLException {
        this.checkOpen();
        try {
            this._conn.setHoldability ( holdability );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public Savepoint setSavepoint() throws SQLException {
        this.checkOpen();
        try {
            return this._conn.setSavepoint();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Savepoint setSavepoint ( final String name ) throws SQLException {
        this.checkOpen();
        try {
            return this._conn.setSavepoint ( name );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public void rollback ( final Savepoint savepoint ) throws SQLException {
        this.checkOpen();
        try {
            this._conn.rollback ( savepoint );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void releaseSavepoint ( final Savepoint savepoint ) throws SQLException {
        this.checkOpen();
        try {
            this._conn.releaseSavepoint ( savepoint );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public Statement createStatement ( final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability ) throws SQLException {
        this.checkOpen();
        try {
            final DelegatingStatement ds = new DelegatingStatement ( this, this._conn.createStatement ( resultSetType, resultSetConcurrency, resultSetHoldability ) );
            this.initializeStatement ( ds );
            return ds;
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public PreparedStatement prepareStatement ( final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability ) throws SQLException {
        this.checkOpen();
        try {
            final DelegatingPreparedStatement dps = new DelegatingPreparedStatement ( this, this._conn.prepareStatement ( sql, resultSetType, resultSetConcurrency, resultSetHoldability ) );
            this.initializeStatement ( dps );
            return dps;
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public CallableStatement prepareCall ( final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability ) throws SQLException {
        this.checkOpen();
        try {
            final DelegatingCallableStatement dcs = new DelegatingCallableStatement ( this, this._conn.prepareCall ( sql, resultSetType, resultSetConcurrency, resultSetHoldability ) );
            this.initializeStatement ( dcs );
            return dcs;
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public PreparedStatement prepareStatement ( final String sql, final int autoGeneratedKeys ) throws SQLException {
        this.checkOpen();
        try {
            final DelegatingPreparedStatement dps = new DelegatingPreparedStatement ( this, this._conn.prepareStatement ( sql, autoGeneratedKeys ) );
            this.initializeStatement ( dps );
            return dps;
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public PreparedStatement prepareStatement ( final String sql, final int[] columnIndexes ) throws SQLException {
        this.checkOpen();
        try {
            final DelegatingPreparedStatement dps = new DelegatingPreparedStatement ( this, this._conn.prepareStatement ( sql, columnIndexes ) );
            this.initializeStatement ( dps );
            return dps;
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public PreparedStatement prepareStatement ( final String sql, final String[] columnNames ) throws SQLException {
        this.checkOpen();
        try {
            final DelegatingPreparedStatement dps = new DelegatingPreparedStatement ( this, this._conn.prepareStatement ( sql, columnNames ) );
            this.initializeStatement ( dps );
            return dps;
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public boolean isWrapperFor ( final Class<?> iface ) throws SQLException {
        return iface.isAssignableFrom ( this.getClass() ) || iface.isAssignableFrom ( this._conn.getClass() ) || this._conn.isWrapperFor ( iface );
    }
    @Override
    public <T> T unwrap ( final Class<T> iface ) throws SQLException {
        if ( iface.isAssignableFrom ( this.getClass() ) ) {
            return iface.cast ( this );
        }
        if ( iface.isAssignableFrom ( this._conn.getClass() ) ) {
            return iface.cast ( this._conn );
        }
        return this._conn.unwrap ( iface );
    }
    @Override
    public Array createArrayOf ( final String typeName, final Object[] elements ) throws SQLException {
        this.checkOpen();
        try {
            return this._conn.createArrayOf ( typeName, elements );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Blob createBlob() throws SQLException {
        this.checkOpen();
        try {
            return this._conn.createBlob();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Clob createClob() throws SQLException {
        this.checkOpen();
        try {
            return this._conn.createClob();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public NClob createNClob() throws SQLException {
        this.checkOpen();
        try {
            return this._conn.createNClob();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public SQLXML createSQLXML() throws SQLException {
        this.checkOpen();
        try {
            return this._conn.createSQLXML();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public Struct createStruct ( final String typeName, final Object[] attributes ) throws SQLException {
        this.checkOpen();
        try {
            return this._conn.createStruct ( typeName, attributes );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public boolean isValid ( final int timeout ) throws SQLException {
        if ( this.isClosed() ) {
            return false;
        }
        try {
            return this._conn.isValid ( timeout );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return false;
        }
    }
    @Override
    public void setClientInfo ( final String name, final String value ) throws SQLClientInfoException {
        try {
            this.checkOpen();
            this._conn.setClientInfo ( name, value );
        } catch ( SQLClientInfoException e ) {
            throw e;
        } catch ( SQLException e2 ) {
            throw new SQLClientInfoException ( "Connection is closed.", DelegatingConnection.EMPTY_FAILED_PROPERTIES, e2 );
        }
    }
    @Override
    public void setClientInfo ( final Properties properties ) throws SQLClientInfoException {
        try {
            this.checkOpen();
            this._conn.setClientInfo ( properties );
        } catch ( SQLClientInfoException e ) {
            throw e;
        } catch ( SQLException e2 ) {
            throw new SQLClientInfoException ( "Connection is closed.", DelegatingConnection.EMPTY_FAILED_PROPERTIES, e2 );
        }
    }
    @Override
    public Properties getClientInfo() throws SQLException {
        this.checkOpen();
        try {
            return this._conn.getClientInfo();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public String getClientInfo ( final String name ) throws SQLException {
        this.checkOpen();
        try {
            return this._conn.getClientInfo ( name );
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public void setSchema ( final String schema ) throws SQLException {
        this.checkOpen();
        try {
            this._conn.setSchema ( schema );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public String getSchema() throws SQLException {
        this.checkOpen();
        try {
            return this._conn.getSchema();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return null;
        }
    }
    @Override
    public void abort ( final Executor executor ) throws SQLException {
        this.checkOpen();
        try {
            this._conn.abort ( executor );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public void setNetworkTimeout ( final Executor executor, final int milliseconds ) throws SQLException {
        this.checkOpen();
        try {
            this._conn.setNetworkTimeout ( executor, milliseconds );
        } catch ( SQLException e ) {
            this.handleException ( e );
        }
    }
    @Override
    public int getNetworkTimeout() throws SQLException {
        this.checkOpen();
        try {
            return this._conn.getNetworkTimeout();
        } catch ( SQLException e ) {
            this.handleException ( e );
            return 0;
        }
    }
    static {
        EMPTY_FAILED_PROPERTIES = Collections.emptyMap();
    }
}
