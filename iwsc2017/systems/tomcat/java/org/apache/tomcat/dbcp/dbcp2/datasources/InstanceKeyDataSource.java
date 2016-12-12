package org.apache.tomcat.dbcp.dbcp2.datasources;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Referenceable;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import org.apache.tomcat.dbcp.pool2.impl.BaseObjectPoolConfig;
import org.apache.tomcat.dbcp.pool2.impl.GenericKeyedObjectPoolConfig;
public abstract class InstanceKeyDataSource
    implements DataSource, Referenceable, Serializable, AutoCloseable {
    private static final long serialVersionUID = -6819270431752240878L;
    private static final String GET_CONNECTION_CALLED
        = "A Connection was already requested from this source, "
          + "further initialization is not allowed.";
    private static final String BAD_TRANSACTION_ISOLATION
        = "The requested TransactionIsolation level is invalid.";
    protected static final int UNKNOWN_TRANSACTIONISOLATION = -1;
    private volatile boolean getConnectionCalled = false;
    private ConnectionPoolDataSource dataSource = null;
    private String dataSourceName = null;
    private String description = null;
    private Properties jndiEnvironment = null;
    private int loginTimeout = 0;
    private PrintWriter logWriter = null;
    private String instanceKey = null;
    private boolean defaultBlockWhenExhausted =
        BaseObjectPoolConfig.DEFAULT_BLOCK_WHEN_EXHAUSTED;
    private String defaultEvictionPolicyClassName =
        BaseObjectPoolConfig.DEFAULT_EVICTION_POLICY_CLASS_NAME;
    private boolean defaultLifo = BaseObjectPoolConfig.DEFAULT_LIFO;
    private int defaultMaxIdle =
        GenericKeyedObjectPoolConfig.DEFAULT_MAX_IDLE_PER_KEY;
    private int defaultMaxTotal =
        GenericKeyedObjectPoolConfig.DEFAULT_MAX_TOTAL;
    private long defaultMaxWaitMillis =
        BaseObjectPoolConfig.DEFAULT_MAX_WAIT_MILLIS;
    private long defaultMinEvictableIdleTimeMillis =
        BaseObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
    private int defaultMinIdle =
        GenericKeyedObjectPoolConfig.DEFAULT_MIN_IDLE_PER_KEY;
    private int defaultNumTestsPerEvictionRun =
        BaseObjectPoolConfig.DEFAULT_NUM_TESTS_PER_EVICTION_RUN;
    private long defaultSoftMinEvictableIdleTimeMillis =
        BaseObjectPoolConfig.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
    private boolean defaultTestOnCreate =
        BaseObjectPoolConfig.DEFAULT_TEST_ON_CREATE;
    private boolean defaultTestOnBorrow =
        BaseObjectPoolConfig.DEFAULT_TEST_ON_BORROW;
    private boolean defaultTestOnReturn =
        BaseObjectPoolConfig.DEFAULT_TEST_ON_RETURN;
    private boolean defaultTestWhileIdle =
        BaseObjectPoolConfig.DEFAULT_TEST_WHILE_IDLE;
    private long defaultTimeBetweenEvictionRunsMillis =
        BaseObjectPoolConfig.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;
    private String validationQuery = null;
    private int validationQueryTimeout = -1;
    private boolean rollbackAfterValidation = false;
    private long maxConnLifetimeMillis = -1;
    private Boolean defaultAutoCommit = null;
    private int defaultTransactionIsolation = UNKNOWN_TRANSACTIONISOLATION;
    private Boolean defaultReadOnly = null;
    public InstanceKeyDataSource() {
    }
    protected void assertInitializationAllowed()
    throws IllegalStateException {
        if ( getConnectionCalled ) {
            throw new IllegalStateException ( GET_CONNECTION_CALLED );
        }
    }
    @Override
    public abstract void close() throws Exception;
    protected abstract PooledConnectionManager getConnectionManager ( UserPassKey upkey );
    @Override
    public boolean isWrapperFor ( final Class<?> iface ) throws SQLException {
        return false;
    }
    @Override
    public <T> T unwrap ( final Class<T> iface ) throws SQLException {
        throw new SQLException ( "InstanceKeyDataSource is not a wrapper." );
    }
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
    public boolean getDefaultBlockWhenExhausted() {
        return this.defaultBlockWhenExhausted;
    }
    public void setDefaultBlockWhenExhausted ( final boolean blockWhenExhausted ) {
        assertInitializationAllowed();
        this.defaultBlockWhenExhausted = blockWhenExhausted;
    }
    public String getDefaultEvictionPolicyClassName() {
        return this.defaultEvictionPolicyClassName;
    }
    public void setDefaultEvictionPolicyClassName (
        final String evictionPolicyClassName ) {
        assertInitializationAllowed();
        this.defaultEvictionPolicyClassName = evictionPolicyClassName;
    }
    public boolean getDefaultLifo() {
        return this.defaultLifo;
    }
    public void setDefaultLifo ( final boolean lifo ) {
        assertInitializationAllowed();
        this.defaultLifo = lifo;
    }
    public int getDefaultMaxIdle() {
        return this.defaultMaxIdle;
    }
    public void setDefaultMaxIdle ( final int maxIdle ) {
        assertInitializationAllowed();
        this.defaultMaxIdle = maxIdle;
    }
    public int getDefaultMaxTotal() {
        return this.defaultMaxTotal;
    }
    public void setDefaultMaxTotal ( final int maxTotal ) {
        assertInitializationAllowed();
        this.defaultMaxTotal = maxTotal;
    }
    public long getDefaultMaxWaitMillis() {
        return this.defaultMaxWaitMillis;
    }
    public void setDefaultMaxWaitMillis ( final long maxWaitMillis ) {
        assertInitializationAllowed();
        this.defaultMaxWaitMillis = maxWaitMillis;
    }
    public long getDefaultMinEvictableIdleTimeMillis() {
        return this.defaultMinEvictableIdleTimeMillis;
    }
    public void setDefaultMinEvictableIdleTimeMillis (
        final long minEvictableIdleTimeMillis ) {
        assertInitializationAllowed();
        this.defaultMinEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }
    public int getDefaultMinIdle() {
        return this.defaultMinIdle;
    }
    public void setDefaultMinIdle ( final int minIdle ) {
        assertInitializationAllowed();
        this.defaultMinIdle = minIdle;
    }
    public int getDefaultNumTestsPerEvictionRun() {
        return this.defaultNumTestsPerEvictionRun;
    }
    public void setDefaultNumTestsPerEvictionRun ( final int numTestsPerEvictionRun ) {
        assertInitializationAllowed();
        this.defaultNumTestsPerEvictionRun = numTestsPerEvictionRun;
    }
    public long getDefaultSoftMinEvictableIdleTimeMillis() {
        return this.defaultSoftMinEvictableIdleTimeMillis;
    }
    public void setDefaultSoftMinEvictableIdleTimeMillis (
        final long softMinEvictableIdleTimeMillis ) {
        assertInitializationAllowed();
        this.defaultSoftMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
    }
    public boolean getDefaultTestOnCreate() {
        return this.defaultTestOnCreate;
    }
    public void setDefaultTestOnCreate ( final boolean testOnCreate ) {
        assertInitializationAllowed();
        this.defaultTestOnCreate = testOnCreate;
    }
    public boolean getDefaultTestOnBorrow() {
        return this.defaultTestOnBorrow;
    }
    public void setDefaultTestOnBorrow ( final boolean testOnBorrow ) {
        assertInitializationAllowed();
        this.defaultTestOnBorrow = testOnBorrow;
    }
    public boolean getDefaultTestOnReturn() {
        return this.defaultTestOnReturn;
    }
    public void setDefaultTestOnReturn ( final boolean testOnReturn ) {
        assertInitializationAllowed();
        this.defaultTestOnReturn = testOnReturn;
    }
    public boolean getDefaultTestWhileIdle() {
        return this.defaultTestWhileIdle;
    }
    public void setDefaultTestWhileIdle ( final boolean testWhileIdle ) {
        assertInitializationAllowed();
        this.defaultTestWhileIdle = testWhileIdle;
    }
    public long getDefaultTimeBetweenEvictionRunsMillis () {
        return this.defaultTimeBetweenEvictionRunsMillis ;
    }
    public void setDefaultTimeBetweenEvictionRunsMillis (
        final long timeBetweenEvictionRunsMillis ) {
        assertInitializationAllowed();
        this.defaultTimeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis ;
    }
    public ConnectionPoolDataSource getConnectionPoolDataSource() {
        return dataSource;
    }
    public void setConnectionPoolDataSource ( final ConnectionPoolDataSource v ) {
        assertInitializationAllowed();
        if ( dataSourceName != null ) {
            throw new IllegalStateException (
                "Cannot set the DataSource, if JNDI is used." );
        }
        if ( dataSource != null ) {
            throw new IllegalStateException (
                "The CPDS has already been set. It cannot be altered." );
        }
        dataSource = v;
        instanceKey = InstanceKeyDataSourceFactory.registerNewInstance ( this );
    }
    public String getDataSourceName() {
        return dataSourceName;
    }
    public void setDataSourceName ( final String v ) {
        assertInitializationAllowed();
        if ( dataSource != null ) {
            throw new IllegalStateException (
                "Cannot set the JNDI name for the DataSource, if already " +
                "set using setConnectionPoolDataSource." );
        }
        if ( dataSourceName != null ) {
            throw new IllegalStateException (
                "The DataSourceName has already been set. " +
                "It cannot be altered." );
        }
        this.dataSourceName = v;
        instanceKey = InstanceKeyDataSourceFactory.registerNewInstance ( this );
    }
    public Boolean isDefaultAutoCommit() {
        return defaultAutoCommit;
    }
    public void setDefaultAutoCommit ( final Boolean v ) {
        assertInitializationAllowed();
        this.defaultAutoCommit = v;
    }
    public Boolean isDefaultReadOnly() {
        return defaultReadOnly;
    }
    public void setDefaultReadOnly ( final Boolean v ) {
        assertInitializationAllowed();
        this.defaultReadOnly = v;
    }
    public int getDefaultTransactionIsolation() {
        return defaultTransactionIsolation;
    }
    public void setDefaultTransactionIsolation ( final int v ) {
        assertInitializationAllowed();
        switch ( v ) {
        case Connection.TRANSACTION_NONE:
        case Connection.TRANSACTION_READ_COMMITTED:
        case Connection.TRANSACTION_READ_UNCOMMITTED:
        case Connection.TRANSACTION_REPEATABLE_READ:
        case Connection.TRANSACTION_SERIALIZABLE:
            break;
        default:
            throw new IllegalArgumentException ( BAD_TRANSACTION_ISOLATION );
        }
        this.defaultTransactionIsolation = v;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription ( final String v ) {
        this.description = v;
    }
    protected String getInstanceKey() {
        return instanceKey;
    }
    public String getJndiEnvironment ( final String key ) {
        String value = null;
        if ( jndiEnvironment != null ) {
            value = jndiEnvironment.getProperty ( key );
        }
        return value;
    }
    public void setJndiEnvironment ( final String key, final String value ) {
        if ( jndiEnvironment == null ) {
            jndiEnvironment = new Properties();
        }
        jndiEnvironment.setProperty ( key, value );
    }
    void setJndiEnvironment ( final Properties properties ) {
        if ( jndiEnvironment == null ) {
            jndiEnvironment = new Properties();
        } else {
            jndiEnvironment.clear();
        }
        jndiEnvironment.putAll ( properties );
    }
    @Override
    public int getLoginTimeout() {
        return loginTimeout;
    }
    @Override
    public void setLoginTimeout ( final int v ) {
        this.loginTimeout = v;
    }
    @Override
    public PrintWriter getLogWriter() {
        if ( logWriter == null ) {
            logWriter = new PrintWriter (
                new OutputStreamWriter ( System.out, StandardCharsets.UTF_8 ) );
        }
        return logWriter;
    }
    @Override
    public void setLogWriter ( final PrintWriter v ) {
        this.logWriter = v;
    }
    public String getValidationQuery() {
        return this.validationQuery;
    }
    public void setValidationQuery ( final String validationQuery ) {
        assertInitializationAllowed();
        this.validationQuery = validationQuery;
    }
    public int getValidationQueryTimeout() {
        return validationQueryTimeout;
    }
    public void setValidationQueryTimeout ( final int validationQueryTimeout ) {
        this.validationQueryTimeout = validationQueryTimeout;
    }
    public boolean isRollbackAfterValidation() {
        return this.rollbackAfterValidation;
    }
    public void setRollbackAfterValidation ( final boolean rollbackAfterValidation ) {
        assertInitializationAllowed();
        this.rollbackAfterValidation = rollbackAfterValidation;
    }
    public long getMaxConnLifetimeMillis() {
        return maxConnLifetimeMillis;
    }
    public void setMaxConnLifetimeMillis ( final long maxConnLifetimeMillis ) {
        this.maxConnLifetimeMillis = maxConnLifetimeMillis;
    }
    @Override
    public Connection getConnection() throws SQLException {
        return getConnection ( null, null );
    }
    @Override
    public Connection getConnection ( final String username, final String password )
    throws SQLException {
        if ( instanceKey == null ) {
            throw new SQLException ( "Must set the ConnectionPoolDataSource "
                                     + "through setDataSourceName or setConnectionPoolDataSource"
                                     + " before calling getConnection." );
        }
        getConnectionCalled = true;
        PooledConnectionAndInfo info = null;
        try {
            info = getPooledConnectionAndInfo ( username, password );
        } catch ( final NoSuchElementException e ) {
            closeDueToException ( info );
            throw new SQLException ( "Cannot borrow connection from pool", e );
        } catch ( final RuntimeException e ) {
            closeDueToException ( info );
            throw e;
        } catch ( final SQLException e ) {
            closeDueToException ( info );
            throw e;
        } catch ( final Exception e ) {
            closeDueToException ( info );
            throw new SQLException ( "Cannot borrow connection from pool", e );
        }
        if ( ! ( null == password ? null == info.getPassword()
                 : password.equals ( info.getPassword() ) ) ) {
            try {
                testCPDS ( username, password );
            } catch ( final SQLException ex ) {
                closeDueToException ( info );
                throw new SQLException ( "Given password did not match password used"
                                         + " to create the PooledConnection.", ex );
            } catch ( final javax.naming.NamingException ne ) {
                throw new SQLException (
                    "NamingException encountered connecting to database", ne );
            }
            final UserPassKey upkey = info.getUserPassKey();
            final PooledConnectionManager manager = getConnectionManager ( upkey );
            manager.invalidate ( info.getPooledConnection() );
            manager.setPassword ( upkey.getPassword() );
            info = null;
            for ( int i = 0; i < 10; i++ ) {
                try {
                    info = getPooledConnectionAndInfo ( username, password );
                } catch ( final NoSuchElementException e ) {
                    closeDueToException ( info );
                    throw new SQLException ( "Cannot borrow connection from pool", e );
                } catch ( final RuntimeException e ) {
                    closeDueToException ( info );
                    throw e;
                } catch ( final SQLException e ) {
                    closeDueToException ( info );
                    throw e;
                } catch ( final Exception e ) {
                    closeDueToException ( info );
                    throw new SQLException ( "Cannot borrow connection from pool", e );
                }
                if ( info != null && password != null && password.equals ( info.getPassword() ) ) {
                    break;
                }
                if ( info != null ) {
                    manager.invalidate ( info.getPooledConnection() );
                }
                info = null;
            }
            if ( info == null ) {
                throw new SQLException ( "Cannot borrow connection from pool - password change failure." );
            }
        }
        final Connection con = info.getPooledConnection().getConnection();
        try {
            setupDefaults ( con, username );
            con.clearWarnings();
            return con;
        } catch ( final SQLException ex ) {
            try {
                con.close();
            } catch ( final Exception exc ) {
                getLogWriter().println (
                    "ignoring exception during close: " + exc );
            }
            throw ex;
        }
    }
    protected abstract PooledConnectionAndInfo
    getPooledConnectionAndInfo ( String username, String password )
    throws SQLException;
    protected abstract void setupDefaults ( Connection con, String username )
    throws SQLException;
    private void closeDueToException ( final PooledConnectionAndInfo info ) {
        if ( info != null ) {
            try {
                info.getPooledConnection().getConnection().close();
            } catch ( final Exception e ) {
                getLogWriter().println ( "[ERROR] Could not return connection to "
                                         + "pool during exception handling. " + e.getMessage() );
            }
        }
    }
    protected ConnectionPoolDataSource
    testCPDS ( final String username, final String password )
    throws javax.naming.NamingException, SQLException {
        ConnectionPoolDataSource cpds = this.dataSource;
        if ( cpds == null ) {
            Context ctx = null;
            if ( jndiEnvironment == null ) {
                ctx = new InitialContext();
            } else {
                ctx = new InitialContext ( jndiEnvironment );
            }
            final Object ds = ctx.lookup ( dataSourceName );
            if ( ds instanceof ConnectionPoolDataSource ) {
                cpds = ( ConnectionPoolDataSource ) ds;
            } else {
                throw new SQLException ( "Illegal configuration: "
                                         + "DataSource " + dataSourceName
                                         + " (" + ds.getClass().getName() + ")"
                                         + " doesn't implement javax.sql.ConnectionPoolDataSource" );
            }
        }
        PooledConnection conn = null;
        try {
            if ( username != null ) {
                conn = cpds.getPooledConnection ( username, password );
            } else {
                conn = cpds.getPooledConnection();
            }
            if ( conn == null ) {
                throw new SQLException (
                    "Cannot connect using the supplied username/password" );
            }
        } finally {
            if ( conn != null ) {
                try {
                    conn.close();
                } catch ( final SQLException e ) {
                }
            }
        }
        return cpds;
    }
}
