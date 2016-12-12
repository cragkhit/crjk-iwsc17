package org.apache.tomcat.dbcp.dbcp2;
import org.apache.juli.logging.LogFactory;
import java.util.Iterator;
import java.sql.Statement;
import org.apache.tomcat.dbcp.pool2.KeyedObjectPool;
import java.sql.Connection;
import org.apache.tomcat.dbcp.pool2.impl.DefaultPooledObject;
import org.apache.tomcat.dbcp.pool2.KeyedPooledObjectFactory;
import org.apache.tomcat.dbcp.pool2.impl.GenericKeyedObjectPool;
import org.apache.tomcat.dbcp.pool2.impl.GenericKeyedObjectPoolConfig;
import java.sql.SQLException;
import org.apache.tomcat.dbcp.pool2.PooledObject;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.tomcat.dbcp.pool2.ObjectPool;
import java.util.Collection;
import javax.management.ObjectName;
import org.apache.juli.logging.Log;
import org.apache.tomcat.dbcp.pool2.PooledObjectFactory;
public class PoolableConnectionFactory implements PooledObjectFactory<PoolableConnection> {
    private static final Log log;
    private final ConnectionFactory _connFactory;
    private final ObjectName dataSourceJmxName;
    private volatile String _validationQuery;
    private volatile int _validationQueryTimeout;
    private Collection<String> _connectionInitSqls;
    private Collection<String> _disconnectionSqlCodes;
    private boolean _fastFailValidation;
    private volatile ObjectPool<PoolableConnection> _pool;
    private Boolean _defaultReadOnly;
    private Boolean _defaultAutoCommit;
    private boolean enableAutoCommitOnReturn;
    private boolean rollbackOnReturn;
    private int _defaultTransactionIsolation;
    private String _defaultCatalog;
    private boolean _cacheState;
    private boolean poolStatements;
    private int maxOpenPreparedStatements;
    private long maxConnLifetimeMillis;
    private final AtomicLong connectionIndex;
    private Integer defaultQueryTimeout;
    static final int UNKNOWN_TRANSACTIONISOLATION = -1;
    public PoolableConnectionFactory ( final ConnectionFactory connFactory, final ObjectName dataSourceJmxName ) {
        this._validationQuery = null;
        this._validationQueryTimeout = -1;
        this._connectionInitSqls = null;
        this._disconnectionSqlCodes = null;
        this._fastFailValidation = false;
        this._pool = null;
        this._defaultReadOnly = null;
        this._defaultAutoCommit = null;
        this.enableAutoCommitOnReturn = true;
        this.rollbackOnReturn = true;
        this._defaultTransactionIsolation = -1;
        this.poolStatements = false;
        this.maxOpenPreparedStatements = 8;
        this.maxConnLifetimeMillis = -1L;
        this.connectionIndex = new AtomicLong ( 0L );
        this.defaultQueryTimeout = null;
        this._connFactory = connFactory;
        this.dataSourceJmxName = dataSourceJmxName;
    }
    public void setValidationQuery ( final String validationQuery ) {
        this._validationQuery = validationQuery;
    }
    public void setValidationQueryTimeout ( final int timeout ) {
        this._validationQueryTimeout = timeout;
    }
    public void setConnectionInitSql ( final Collection<String> connectionInitSqls ) {
        this._connectionInitSqls = connectionInitSqls;
    }
    public synchronized void setPool ( final ObjectPool<PoolableConnection> pool ) {
        if ( null != this._pool && pool != this._pool ) {
            try {
                this._pool.close();
            } catch ( Exception ex ) {}
        }
        this._pool = pool;
    }
    public synchronized ObjectPool<PoolableConnection> getPool() {
        return this._pool;
    }
    public void setDefaultReadOnly ( final Boolean defaultReadOnly ) {
        this._defaultReadOnly = defaultReadOnly;
    }
    public void setDefaultAutoCommit ( final Boolean defaultAutoCommit ) {
        this._defaultAutoCommit = defaultAutoCommit;
    }
    public void setDefaultTransactionIsolation ( final int defaultTransactionIsolation ) {
        this._defaultTransactionIsolation = defaultTransactionIsolation;
    }
    public void setDefaultCatalog ( final String defaultCatalog ) {
        this._defaultCatalog = defaultCatalog;
    }
    public void setCacheState ( final boolean cacheState ) {
        this._cacheState = cacheState;
    }
    public void setPoolStatements ( final boolean poolStatements ) {
        this.poolStatements = poolStatements;
    }
    public void setMaxOpenPrepatedStatements ( final int maxOpenPreparedStatements ) {
        this.maxOpenPreparedStatements = maxOpenPreparedStatements;
    }
    public void setMaxConnLifetimeMillis ( final long maxConnLifetimeMillis ) {
        this.maxConnLifetimeMillis = maxConnLifetimeMillis;
    }
    public boolean isEnableAutoCommitOnReturn() {
        return this.enableAutoCommitOnReturn;
    }
    public void setEnableAutoCommitOnReturn ( final boolean enableAutoCommitOnReturn ) {
        this.enableAutoCommitOnReturn = enableAutoCommitOnReturn;
    }
    public boolean isRollbackOnReturn() {
        return this.rollbackOnReturn;
    }
    public void setRollbackOnReturn ( final boolean rollbackOnReturn ) {
        this.rollbackOnReturn = rollbackOnReturn;
    }
    public Integer getDefaultQueryTimeout() {
        return this.defaultQueryTimeout;
    }
    public void setDefaultQueryTimeout ( final Integer defaultQueryTimeout ) {
        this.defaultQueryTimeout = defaultQueryTimeout;
    }
    public Collection<String> getDisconnectionSqlCodes() {
        return this._disconnectionSqlCodes;
    }
    public void setDisconnectionSqlCodes ( final Collection<String> disconnectionSqlCodes ) {
        this._disconnectionSqlCodes = disconnectionSqlCodes;
    }
    public boolean isFastFailValidation() {
        return this._fastFailValidation;
    }
    public void setFastFailValidation ( final boolean fastFailValidation ) {
        this._fastFailValidation = fastFailValidation;
    }
    @Override
    public PooledObject<PoolableConnection> makeObject() throws Exception {
        Connection conn = this._connFactory.createConnection();
        if ( conn == null ) {
            throw new IllegalStateException ( "Connection factory returned null from createConnection" );
        }
        try {
            this.initializeConnection ( conn );
        } catch ( SQLException sqle ) {
            try {
                conn.close();
            } catch ( SQLException ex ) {}
            throw sqle;
        }
        final long connIndex = this.connectionIndex.getAndIncrement();
        if ( this.poolStatements ) {
            conn = new PoolingConnection ( conn );
            final GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
            config.setMaxTotalPerKey ( -1 );
            config.setBlockWhenExhausted ( false );
            config.setMaxWaitMillis ( 0L );
            config.setMaxIdlePerKey ( 1 );
            config.setMaxTotal ( this.maxOpenPreparedStatements );
            if ( this.dataSourceJmxName != null ) {
                final StringBuilder base = new StringBuilder ( this.dataSourceJmxName.toString() );
                base.append ( ",connectionpool=connections,connection=" );
                base.append ( Long.toString ( connIndex ) );
                config.setJmxNameBase ( base.toString() );
                config.setJmxNamePrefix ( ",statementpool=statements" );
            } else {
                config.setJmxEnabled ( false );
            }
            final KeyedObjectPool<PStmtKey, DelegatingPreparedStatement> stmtPool = new GenericKeyedObjectPool<PStmtKey, DelegatingPreparedStatement> ( ( KeyedPooledObjectFactory<PStmtKey, DelegatingPreparedStatement> ) conn, config );
            ( ( PoolingConnection ) conn ).setStatementPool ( stmtPool );
            ( ( PoolingConnection ) conn ).setCacheState ( this._cacheState );
        }
        ObjectName connJmxName;
        if ( this.dataSourceJmxName == null ) {
            connJmxName = null;
        } else {
            connJmxName = new ObjectName ( this.dataSourceJmxName.toString() + ",connectionpool=connections,connection=" + connIndex );
        }
        final PoolableConnection pc = new PoolableConnection ( conn, this._pool, connJmxName, this._disconnectionSqlCodes, this._fastFailValidation );
        pc.setCacheState ( this._cacheState );
        return new DefaultPooledObject<PoolableConnection> ( pc );
    }
    protected void initializeConnection ( final Connection conn ) throws SQLException {
        final Collection<String> sqls = this._connectionInitSqls;
        if ( conn.isClosed() ) {
            throw new SQLException ( "initializeConnection: connection closed" );
        }
        if ( null != sqls ) {
            try ( final Statement stmt = conn.createStatement() ) {
                for ( final String sql : sqls ) {
                    if ( sql == null ) {
                        throw new NullPointerException ( "null connectionInitSqls element" );
                    }
                    stmt.execute ( sql );
                }
            }
        }
    }
    @Override
    public void destroyObject ( final PooledObject<PoolableConnection> p ) throws Exception {
        p.getObject().reallyClose();
    }
    @Override
    public boolean validateObject ( final PooledObject<PoolableConnection> p ) {
        try {
            this.validateLifetime ( p );
            this.validateConnection ( p.getObject() );
            return true;
        } catch ( Exception e ) {
            if ( PoolableConnectionFactory.log.isDebugEnabled() ) {
                PoolableConnectionFactory.log.debug ( Utils.getMessage ( "poolableConnectionFactory.validateObject.fail" ), e );
            }
            return false;
        }
    }
    public void validateConnection ( final PoolableConnection conn ) throws SQLException {
        if ( conn.isClosed() ) {
            throw new SQLException ( "validateConnection: connection closed" );
        }
        conn.validate ( this._validationQuery, this._validationQueryTimeout );
    }
    @Override
    public void passivateObject ( final PooledObject<PoolableConnection> p ) throws Exception {
        this.validateLifetime ( p );
        final PoolableConnection conn = p.getObject();
        Boolean connAutoCommit = null;
        if ( this.rollbackOnReturn ) {
            connAutoCommit = conn.getAutoCommit();
            if ( !connAutoCommit && !conn.isReadOnly() ) {
                conn.rollback();
            }
        }
        conn.clearWarnings();
        if ( this.enableAutoCommitOnReturn ) {
            if ( connAutoCommit == null ) {
                connAutoCommit = conn.getAutoCommit();
            }
            if ( !connAutoCommit ) {
                conn.setAutoCommit ( true );
            }
        }
        conn.passivate();
    }
    @Override
    public void activateObject ( final PooledObject<PoolableConnection> p ) throws Exception {
        this.validateLifetime ( p );
        final PoolableConnection conn = p.getObject();
        conn.activate();
        if ( this._defaultAutoCommit != null && conn.getAutoCommit() != this._defaultAutoCommit ) {
            conn.setAutoCommit ( this._defaultAutoCommit );
        }
        if ( this._defaultTransactionIsolation != -1 && conn.getTransactionIsolation() != this._defaultTransactionIsolation ) {
            conn.setTransactionIsolation ( this._defaultTransactionIsolation );
        }
        if ( this._defaultReadOnly != null && conn.isReadOnly() != this._defaultReadOnly ) {
            conn.setReadOnly ( this._defaultReadOnly );
        }
        if ( this._defaultCatalog != null && !this._defaultCatalog.equals ( conn.getCatalog() ) ) {
            conn.setCatalog ( this._defaultCatalog );
        }
        conn.setDefaultQueryTimeout ( this.defaultQueryTimeout );
    }
    private void validateLifetime ( final PooledObject<PoolableConnection> p ) throws Exception {
        if ( this.maxConnLifetimeMillis > 0L ) {
            final long lifetime = System.currentTimeMillis() - p.getCreateTime();
            if ( lifetime > this.maxConnLifetimeMillis ) {
                throw new LifetimeExceededException ( Utils.getMessage ( "connectionFactory.lifetimeExceeded", lifetime, this.maxConnLifetimeMillis ) );
            }
        }
    }
    protected ConnectionFactory getConnectionFactory() {
        return this._connFactory;
    }
    protected boolean getPoolStatements() {
        return this.poolStatements;
    }
    protected int getMaxOpenPreparedStatements() {
        return this.maxOpenPreparedStatements;
    }
    protected boolean getCacheState() {
        return this._cacheState;
    }
    protected ObjectName getDataSourceJmxName() {
        return this.dataSourceJmxName;
    }
    protected AtomicLong getConnectionIndex() {
        return this.connectionIndex;
    }
    static {
        log = LogFactory.getLog ( PoolableConnectionFactory.class );
    }
}
