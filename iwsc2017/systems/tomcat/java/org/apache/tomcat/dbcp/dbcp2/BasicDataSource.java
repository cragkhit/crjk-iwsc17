package org.apache.tomcat.dbcp.dbcp2;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import javax.management.InstanceAlreadyExistsException;
import javax.management.JMException;
import javax.management.MBeanRegistration;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.sql.DataSource;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.dbcp.pool2.PooledObject;
import org.apache.tomcat.dbcp.pool2.impl.AbandonedConfig;
import org.apache.tomcat.dbcp.pool2.impl.BaseObjectPoolConfig;
import org.apache.tomcat.dbcp.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.tomcat.dbcp.pool2.impl.GenericObjectPool;
import org.apache.tomcat.dbcp.pool2.impl.GenericObjectPoolConfig;
public class BasicDataSource implements DataSource, BasicDataSourceMXBean, MBeanRegistration, AutoCloseable {
    private static final Log log = LogFactory.getLog ( BasicDataSource.class );
    static {
        DriverManager.getDrivers();
        try {
            if ( Utils.IS_SECURITY_ENABLED ) {
                final ClassLoader loader = BasicDataSource.class.getClassLoader();
                final String dbcpPackageName = BasicDataSource.class.getPackage().getName();
                loader.loadClass ( dbcpPackageName + ".BasicDataSource$PaGetConnection" );
                loader.loadClass ( dbcpPackageName + ".DelegatingCallableStatement" );
                loader.loadClass ( dbcpPackageName + ".DelegatingDatabaseMetaData" );
                loader.loadClass ( dbcpPackageName + ".DelegatingPreparedStatement" );
                loader.loadClass ( dbcpPackageName + ".DelegatingResultSet" );
                loader.loadClass ( dbcpPackageName + ".PoolableCallableStatement" );
                loader.loadClass ( dbcpPackageName + ".PoolablePreparedStatement" );
                loader.loadClass ( dbcpPackageName + ".PoolingConnection$StatementType" );
                loader.loadClass ( dbcpPackageName + ".PStmtKey" );
                final String poolPackageName = PooledObject.class.getPackage().getName();
                loader.loadClass ( poolPackageName + ".impl.LinkedBlockingDeque$Node" );
                loader.loadClass ( poolPackageName + ".impl.GenericKeyedObjectPool$ObjectDeque" );
            }
        } catch ( final ClassNotFoundException cnfe ) {
            throw new IllegalStateException ( "Unable to pre-load classes", cnfe );
        }
    }
    private volatile Boolean defaultAutoCommit = null;
    @Override
    public Boolean getDefaultAutoCommit() {
        return defaultAutoCommit;
    }
    public void setDefaultAutoCommit ( final Boolean defaultAutoCommit ) {
        this.defaultAutoCommit = defaultAutoCommit;
    }
    private transient Boolean defaultReadOnly = null;
    @Override
    public Boolean getDefaultReadOnly() {
        return defaultReadOnly;
    }
    public void setDefaultReadOnly ( final Boolean defaultReadOnly ) {
        this.defaultReadOnly = defaultReadOnly;
    }
    private volatile int defaultTransactionIsolation =
        PoolableConnectionFactory.UNKNOWN_TRANSACTIONISOLATION;
    @Override
    public int getDefaultTransactionIsolation() {
        return this.defaultTransactionIsolation;
    }
    public void setDefaultTransactionIsolation ( final int defaultTransactionIsolation ) {
        this.defaultTransactionIsolation = defaultTransactionIsolation;
    }
    private Integer defaultQueryTimeout = null;
    public Integer getDefaultQueryTimeout() {
        return defaultQueryTimeout;
    }
    public void setDefaultQueryTimeout ( final Integer defaultQueryTimeout ) {
        this.defaultQueryTimeout = defaultQueryTimeout;
    }
    private volatile String defaultCatalog = null;
    @Override
    public String getDefaultCatalog() {
        return this.defaultCatalog;
    }
    public void setDefaultCatalog ( final String defaultCatalog ) {
        if ( defaultCatalog != null && defaultCatalog.trim().length() > 0 ) {
            this.defaultCatalog = defaultCatalog;
        } else {
            this.defaultCatalog = null;
        }
    }
    private boolean cacheState = true;
    @Override
    public boolean getCacheState() {
        return cacheState;
    }
    public void setCacheState ( final boolean cacheState ) {
        this.cacheState = cacheState;
    }
    private Driver driver = null;
    public synchronized Driver getDriver() {
        return driver;
    }
    public synchronized void setDriver ( final Driver driver ) {
        this.driver = driver;
    }
    private String driverClassName = null;
    @Override
    public synchronized String getDriverClassName() {
        return this.driverClassName;
    }
    public synchronized void setDriverClassName ( final String driverClassName ) {
        if ( driverClassName != null && driverClassName.trim().length() > 0 ) {
            this.driverClassName = driverClassName;
        } else {
            this.driverClassName = null;
        }
    }
    private ClassLoader driverClassLoader = null;
    public synchronized ClassLoader getDriverClassLoader() {
        return this.driverClassLoader;
    }
    public synchronized void setDriverClassLoader (
        final ClassLoader driverClassLoader ) {
        this.driverClassLoader = driverClassLoader;
    }
    private boolean lifo = BaseObjectPoolConfig.DEFAULT_LIFO;
    @Override
    public synchronized boolean getLifo() {
        return this.lifo;
    }
    public synchronized void setLifo ( final boolean lifo ) {
        this.lifo = lifo;
        if ( connectionPool != null ) {
            connectionPool.setLifo ( lifo );
        }
    }
    private int maxTotal = GenericObjectPoolConfig.DEFAULT_MAX_TOTAL;
    @Override
    public synchronized int getMaxTotal() {
        return this.maxTotal;
    }
    public synchronized void setMaxTotal ( final int maxTotal ) {
        this.maxTotal = maxTotal;
        if ( connectionPool != null ) {
            connectionPool.setMaxTotal ( maxTotal );
        }
    }
    private int maxIdle = GenericObjectPoolConfig.DEFAULT_MAX_IDLE;
    @Override
    public synchronized int getMaxIdle() {
        return this.maxIdle;
    }
    public synchronized void setMaxIdle ( final int maxIdle ) {
        this.maxIdle = maxIdle;
        if ( connectionPool != null ) {
            connectionPool.setMaxIdle ( maxIdle );
        }
    }
    private int minIdle = GenericObjectPoolConfig.DEFAULT_MIN_IDLE;
    @Override
    public synchronized int getMinIdle() {
        return this.minIdle;
    }
    public synchronized void setMinIdle ( final int minIdle ) {
        this.minIdle = minIdle;
        if ( connectionPool != null ) {
            connectionPool.setMinIdle ( minIdle );
        }
    }
    private int initialSize = 0;
    @Override
    public synchronized int getInitialSize() {
        return this.initialSize;
    }
    public synchronized void setInitialSize ( final int initialSize ) {
        this.initialSize = initialSize;
    }
    private long maxWaitMillis =
        BaseObjectPoolConfig.DEFAULT_MAX_WAIT_MILLIS;
    @Override
    public synchronized long getMaxWaitMillis() {
        return this.maxWaitMillis;
    }
    public synchronized void setMaxWaitMillis ( final long maxWaitMillis ) {
        this.maxWaitMillis = maxWaitMillis;
        if ( connectionPool != null ) {
            connectionPool.setMaxWaitMillis ( maxWaitMillis );
        }
    }
    private boolean poolPreparedStatements = false;
    @Override
    public synchronized boolean isPoolPreparedStatements() {
        return this.poolPreparedStatements;
    }
    public synchronized void setPoolPreparedStatements ( final boolean poolingStatements ) {
        this.poolPreparedStatements = poolingStatements;
    }
    private int maxOpenPreparedStatements =
        GenericKeyedObjectPoolConfig.DEFAULT_MAX_TOTAL;
    @Override
    public synchronized int getMaxOpenPreparedStatements() {
        return this.maxOpenPreparedStatements;
    }
    public synchronized void setMaxOpenPreparedStatements ( final int maxOpenStatements ) {
        this.maxOpenPreparedStatements = maxOpenStatements;
    }
    private boolean testOnCreate = false;
    @Override
    public synchronized boolean getTestOnCreate() {
        return this.testOnCreate;
    }
    public synchronized void setTestOnCreate ( final boolean testOnCreate ) {
        this.testOnCreate = testOnCreate;
        if ( connectionPool != null ) {
            connectionPool.setTestOnCreate ( testOnCreate );
        }
    }
    private boolean testOnBorrow = true;
    @Override
    public synchronized boolean getTestOnBorrow() {
        return this.testOnBorrow;
    }
    public synchronized void setTestOnBorrow ( final boolean testOnBorrow ) {
        this.testOnBorrow = testOnBorrow;
        if ( connectionPool != null ) {
            connectionPool.setTestOnBorrow ( testOnBorrow );
        }
    }
    private boolean testOnReturn = false;
    public synchronized boolean getTestOnReturn() {
        return this.testOnReturn;
    }
    public synchronized void setTestOnReturn ( final boolean testOnReturn ) {
        this.testOnReturn = testOnReturn;
        if ( connectionPool != null ) {
            connectionPool.setTestOnReturn ( testOnReturn );
        }
    }
    private long timeBetweenEvictionRunsMillis =
        BaseObjectPoolConfig.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;
    @Override
    public synchronized long getTimeBetweenEvictionRunsMillis() {
        return this.timeBetweenEvictionRunsMillis;
    }
    public synchronized void setTimeBetweenEvictionRunsMillis ( final long timeBetweenEvictionRunsMillis ) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        if ( connectionPool != null ) {
            connectionPool.setTimeBetweenEvictionRunsMillis ( timeBetweenEvictionRunsMillis );
        }
    }
    private int numTestsPerEvictionRun =
        BaseObjectPoolConfig.DEFAULT_NUM_TESTS_PER_EVICTION_RUN;
    @Override
    public synchronized int getNumTestsPerEvictionRun() {
        return this.numTestsPerEvictionRun;
    }
    public synchronized void setNumTestsPerEvictionRun ( final int numTestsPerEvictionRun ) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
        if ( connectionPool != null ) {
            connectionPool.setNumTestsPerEvictionRun ( numTestsPerEvictionRun );
        }
    }
    private long minEvictableIdleTimeMillis =
        BaseObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
    @Override
    public synchronized long getMinEvictableIdleTimeMillis() {
        return this.minEvictableIdleTimeMillis;
    }
    public synchronized void setMinEvictableIdleTimeMillis ( final long minEvictableIdleTimeMillis ) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
        if ( connectionPool != null ) {
            connectionPool.setMinEvictableIdleTimeMillis ( minEvictableIdleTimeMillis );
        }
    }
    private long softMinEvictableIdleTimeMillis =
        BaseObjectPoolConfig.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
    public synchronized void setSoftMinEvictableIdleTimeMillis ( final long softMinEvictableIdleTimeMillis ) {
        this.softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
        if ( connectionPool != null ) {
            connectionPool.setSoftMinEvictableIdleTimeMillis ( softMinEvictableIdleTimeMillis );
        }
    }
    @Override
    public synchronized long getSoftMinEvictableIdleTimeMillis() {
        return softMinEvictableIdleTimeMillis;
    }
    private String evictionPolicyClassName =
        BaseObjectPoolConfig.DEFAULT_EVICTION_POLICY_CLASS_NAME;
    public synchronized String getEvictionPolicyClassName() {
        return evictionPolicyClassName;
    }
    public synchronized void setEvictionPolicyClassName (
        final String evictionPolicyClassName ) {
        if ( connectionPool != null ) {
            connectionPool.setEvictionPolicyClassName ( evictionPolicyClassName );
        }
        this.evictionPolicyClassName = evictionPolicyClassName;
    }
    private boolean testWhileIdle = false;
    @Override
    public synchronized boolean getTestWhileIdle() {
        return this.testWhileIdle;
    }
    public synchronized void setTestWhileIdle ( final boolean testWhileIdle ) {
        this.testWhileIdle = testWhileIdle;
        if ( connectionPool != null ) {
            connectionPool.setTestWhileIdle ( testWhileIdle );
        }
    }
    @Override
    public int getNumActive() {
        final GenericObjectPool<PoolableConnection> pool = connectionPool;
        if ( pool != null ) {
            return pool.getNumActive();
        }
        return 0;
    }
    @Override
    public int getNumIdle() {
        final GenericObjectPool<PoolableConnection> pool = connectionPool;
        if ( pool != null ) {
            return pool.getNumIdle();
        }
        return 0;
    }
    private volatile String password = null;
    @Override
    public String getPassword() {
        return this.password;
    }
    public void setPassword ( final String password ) {
        this.password = password;
    }
    private String url = null;
    @Override
    public synchronized String getUrl() {
        return this.url;
    }
    public synchronized void setUrl ( final String url ) {
        this.url = url;
    }
    private String username = null;
    @Override
    public String getUsername() {
        return this.username;
    }
    public void setUsername ( final String username ) {
        this.username = username;
    }
    private volatile String validationQuery = null;
    @Override
    public String getValidationQuery() {
        return this.validationQuery;
    }
    public void setValidationQuery ( final String validationQuery ) {
        if ( validationQuery != null && validationQuery.trim().length() > 0 ) {
            this.validationQuery = validationQuery;
        } else {
            this.validationQuery = null;
        }
    }
    private volatile int validationQueryTimeout = -1;
    @Override
    public int getValidationQueryTimeout() {
        return validationQueryTimeout;
    }
    public void setValidationQueryTimeout ( final int timeout ) {
        this.validationQueryTimeout = timeout;
    }
    private volatile List<String> connectionInitSqls;
    public List<String> getConnectionInitSqls() {
        final List<String> result = connectionInitSqls;
        if ( result == null ) {
            return Collections.emptyList();
        }
        return result;
    }
    @Override
    public String[] getConnectionInitSqlsAsArray() {
        final Collection<String> result = getConnectionInitSqls();
        return result.toArray ( new String[result.size()] );
    }
    public void setConnectionInitSqls ( final Collection<String> connectionInitSqls ) {
        if ( connectionInitSqls != null && connectionInitSqls.size() > 0 ) {
            ArrayList<String> newVal = null;
            for ( final String s : connectionInitSqls ) {
                if ( s != null && s.trim().length() > 0 ) {
                    if ( newVal == null ) {
                        newVal = new ArrayList<>();
                    }
                    newVal.add ( s );
                }
            }
            this.connectionInitSqls = newVal;
        } else {
            this.connectionInitSqls = null;
        }
    }
    private boolean accessToUnderlyingConnectionAllowed = false;
    @Override
    public synchronized boolean isAccessToUnderlyingConnectionAllowed() {
        return this.accessToUnderlyingConnectionAllowed;
    }
    public synchronized void setAccessToUnderlyingConnectionAllowed ( final boolean allow ) {
        this.accessToUnderlyingConnectionAllowed = allow;
    }
    private long maxConnLifetimeMillis = -1;
    @Override
    public long getMaxConnLifetimeMillis() {
        return maxConnLifetimeMillis;
    }
    private boolean logExpiredConnections = true;
    @Override
    public boolean getLogExpiredConnections() {
        return logExpiredConnections;
    }
    public void setMaxConnLifetimeMillis ( final long maxConnLifetimeMillis ) {
        this.maxConnLifetimeMillis = maxConnLifetimeMillis;
    }
    public void setLogExpiredConnections ( final boolean logExpiredConnections ) {
        this.logExpiredConnections = logExpiredConnections;
    }
    private String jmxName = null;
    public String getJmxName() {
        return jmxName;
    }
    public void setJmxName ( final String jmxName ) {
        this.jmxName = jmxName;
    }
    private boolean enableAutoCommitOnReturn = true;
    public boolean getEnableAutoCommitOnReturn() {
        return enableAutoCommitOnReturn;
    }
    public void setEnableAutoCommitOnReturn ( final boolean enableAutoCommitOnReturn ) {
        this.enableAutoCommitOnReturn = enableAutoCommitOnReturn;
    }
    private boolean rollbackOnReturn = true;
    public boolean getRollbackOnReturn() {
        return rollbackOnReturn;
    }
    public void setRollbackOnReturn ( final boolean rollbackOnReturn ) {
        this.rollbackOnReturn = rollbackOnReturn;
    }
    private volatile Set<String> disconnectionSqlCodes;
    public Set<String> getDisconnectionSqlCodes() {
        final Set<String> result = disconnectionSqlCodes;
        if ( result == null ) {
            return Collections.emptySet();
        }
        return result;
    }
    @Override
    public String[] getDisconnectionSqlCodesAsArray() {
        final Collection<String> result = getDisconnectionSqlCodes();
        return result.toArray ( new String[result.size()] );
    }
    public void setDisconnectionSqlCodes ( final Collection<String> disconnectionSqlCodes ) {
        if ( disconnectionSqlCodes != null && disconnectionSqlCodes.size() > 0 ) {
            HashSet<String> newVal = null;
            for ( final String s : disconnectionSqlCodes ) {
                if ( s != null && s.trim().length() > 0 ) {
                    if ( newVal == null ) {
                        newVal = new HashSet<>();
                    }
                    newVal.add ( s );
                }
            }
            this.disconnectionSqlCodes = newVal;
        } else {
            this.disconnectionSqlCodes = null;
        }
    }
    private boolean fastFailValidation;
    @Override
    public boolean getFastFailValidation() {
        return fastFailValidation;
    }
    public void setFastFailValidation ( final boolean fastFailValidation ) {
        this.fastFailValidation = fastFailValidation;
    }
    private volatile GenericObjectPool<PoolableConnection> connectionPool = null;
    protected GenericObjectPool<PoolableConnection> getConnectionPool() {
        return connectionPool;
    }
    private Properties connectionProperties = new Properties();
    Properties getConnectionProperties() {
        return connectionProperties;
    }
    private volatile DataSource dataSource = null;
    private volatile PrintWriter logWriter = new PrintWriter ( new OutputStreamWriter (
                System.out, StandardCharsets.UTF_8 ) );
    @Override
    public Connection getConnection() throws SQLException {
        if ( Utils.IS_SECURITY_ENABLED ) {
            final PrivilegedExceptionAction<Connection> action = new PaGetConnection();
            try {
                return AccessController.doPrivileged ( action );
            } catch ( final PrivilegedActionException e ) {
                final Throwable cause = e.getCause();
                if ( cause instanceof SQLException ) {
                    throw ( SQLException ) cause;
                }
                throw new SQLException ( e );
            }
        }
        return createDataSource().getConnection();
    }
    @Override
    public Connection getConnection ( final String user, final String pass ) throws SQLException {
        throw new UnsupportedOperationException ( "Not supported by BasicDataSource" );
    }
    @Override
    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException ( "Not supported by BasicDataSource" );
    }
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return createDataSource().getLogWriter();
    }
    @Override
    public void setLoginTimeout ( final int loginTimeout ) throws SQLException {
        throw new UnsupportedOperationException ( "Not supported by BasicDataSource" );
    }
    @Override
    public void setLogWriter ( final PrintWriter logWriter ) throws SQLException {
        createDataSource().setLogWriter ( logWriter );
        this.logWriter = logWriter;
    }
    private AbandonedConfig abandonedConfig;
    @Override
    public boolean getRemoveAbandonedOnBorrow() {
        if ( abandonedConfig != null ) {
            return abandonedConfig.getRemoveAbandonedOnBorrow();
        }
        return false;
    }
    public void setRemoveAbandonedOnMaintenance (
        final boolean removeAbandonedOnMaintenance ) {
        if ( abandonedConfig == null ) {
            abandonedConfig = new AbandonedConfig();
        }
        abandonedConfig.setRemoveAbandonedOnMaintenance (
            removeAbandonedOnMaintenance );
    }
    @Override
    public boolean getRemoveAbandonedOnMaintenance() {
        if ( abandonedConfig != null ) {
            return abandonedConfig.getRemoveAbandonedOnMaintenance();
        }
        return false;
    }
    public void setRemoveAbandonedOnBorrow ( final boolean removeAbandonedOnBorrow ) {
        if ( abandonedConfig == null ) {
            abandonedConfig = new AbandonedConfig();
        }
        abandonedConfig.setRemoveAbandonedOnBorrow ( removeAbandonedOnBorrow );
    }
    @Override
    public int getRemoveAbandonedTimeout() {
        if ( abandonedConfig != null ) {
            return abandonedConfig.getRemoveAbandonedTimeout();
        }
        return 300;
    }
    public void setRemoveAbandonedTimeout ( final int removeAbandonedTimeout ) {
        if ( abandonedConfig == null ) {
            abandonedConfig = new AbandonedConfig();
        }
        abandonedConfig.setRemoveAbandonedTimeout ( removeAbandonedTimeout );
    }
    @Override
    public boolean getLogAbandoned() {
        if ( abandonedConfig != null ) {
            return abandonedConfig.getLogAbandoned();
        }
        return false;
    }
    public void setLogAbandoned ( final boolean logAbandoned ) {
        if ( abandonedConfig == null ) {
            abandonedConfig = new AbandonedConfig();
        }
        abandonedConfig.setLogAbandoned ( logAbandoned );
    }
    public PrintWriter getAbandonedLogWriter() {
        if ( abandonedConfig != null ) {
            return abandonedConfig.getLogWriter();
        }
        return null;
    }
    public void setAbandonedLogWriter ( final PrintWriter logWriter ) {
        if ( abandonedConfig == null ) {
            abandonedConfig = new AbandonedConfig();
        }
        abandonedConfig.setLogWriter ( logWriter );
    }
    @Override
    public boolean getAbandonedUsageTracking() {
        if ( abandonedConfig != null ) {
            return abandonedConfig.getUseUsageTracking();
        }
        return false;
    }
    public void setAbandonedUsageTracking ( final boolean usageTracking ) {
        if ( abandonedConfig == null ) {
            abandonedConfig = new AbandonedConfig();
        }
        abandonedConfig.setUseUsageTracking ( usageTracking );
    }
    public void addConnectionProperty ( final String name, final String value ) {
        connectionProperties.put ( name, value );
    }
    public void removeConnectionProperty ( final String name ) {
        connectionProperties.remove ( name );
    }
    public void setConnectionProperties ( final String connectionProperties ) {
        if ( connectionProperties == null ) {
            throw new NullPointerException ( "connectionProperties is null" );
        }
        final String[] entries = connectionProperties.split ( ";" );
        final Properties properties = new Properties();
        for ( final String entry : entries ) {
            if ( entry.length() > 0 ) {
                final int index = entry.indexOf ( '=' );
                if ( index > 0 ) {
                    final String name = entry.substring ( 0, index );
                    final String value = entry.substring ( index + 1 );
                    properties.setProperty ( name, value );
                } else {
                    properties.setProperty ( entry, "" );
                }
            }
        }
        this.connectionProperties = properties;
    }
    private boolean closed;
    @Override
    public synchronized void close() throws SQLException {
        if ( registeredJmxName != null ) {
            final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            try {
                mbs.unregisterMBean ( registeredJmxName );
            } catch ( final JMException e ) {
                log.warn ( "Failed to unregister the JMX name: " + registeredJmxName, e );
            } finally {
                registeredJmxName = null;
            }
        }
        closed = true;
        final GenericObjectPool<?> oldpool = connectionPool;
        connectionPool = null;
        dataSource = null;
        try {
            if ( oldpool != null ) {
                oldpool.close();
            }
        } catch ( final RuntimeException e ) {
            throw e;
        } catch ( final Exception e ) {
            throw new SQLException ( Utils.getMessage ( "pool.close.fail" ), e );
        }
    }
    @Override
    public synchronized boolean isClosed() {
        return closed;
    }
    @Override
    public boolean isWrapperFor ( final Class<?> iface ) throws SQLException {
        return false;
    }
    @Override
    public <T> T unwrap ( final Class<T> iface ) throws SQLException {
        throw new SQLException ( "BasicDataSource is not a wrapper." );
    }
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
    public void invalidateConnection ( final Connection connection ) throws IllegalStateException {
        if ( connection == null ) {
            return;
        }
        if ( connectionPool == null ) {
            throw new IllegalStateException ( "Cannot invalidate connection: ConnectionPool is null." );
        }
        final PoolableConnection poolableConnection;
        try {
            poolableConnection = connection.unwrap ( PoolableConnection.class );
            if ( poolableConnection == null ) {
                throw new IllegalStateException (
                    "Cannot invalidate connection: Connection is not a poolable connection." );
            }
        } catch ( final SQLException e ) {
            throw new IllegalStateException ( "Cannot invalidate connection: Unwrapping poolable connection failed.", e );
        }
        try {
            connectionPool.invalidateObject ( poolableConnection );
        } catch ( final Exception e ) {
            throw new IllegalStateException ( "Invalidating connection threw unexpected exception", e );
        }
    }
    protected DataSource createDataSource()
    throws SQLException {
        if ( closed ) {
            throw new SQLException ( "Data source is closed" );
        }
        if ( dataSource != null ) {
            return dataSource;
        }
        synchronized ( this ) {
            if ( dataSource != null ) {
                return dataSource;
            }
            jmxRegister();
            final ConnectionFactory driverConnectionFactory = createConnectionFactory();
            boolean success = false;
            PoolableConnectionFactory poolableConnectionFactory;
            try {
                poolableConnectionFactory = createPoolableConnectionFactory (
                                                driverConnectionFactory );
                poolableConnectionFactory.setPoolStatements (
                    poolPreparedStatements );
                poolableConnectionFactory.setMaxOpenPrepatedStatements (
                    maxOpenPreparedStatements );
                success = true;
            } catch ( final SQLException se ) {
                throw se;
            } catch ( final RuntimeException rte ) {
                throw rte;
            } catch ( final Exception ex ) {
                throw new SQLException ( "Error creating connection factory", ex );
            }
            if ( success ) {
                createConnectionPool ( poolableConnectionFactory );
            }
            DataSource newDataSource;
            success = false;
            try {
                newDataSource = createDataSourceInstance();
                newDataSource.setLogWriter ( logWriter );
                success = true;
            } catch ( final SQLException se ) {
                throw se;
            } catch ( final RuntimeException rte ) {
                throw rte;
            } catch ( final Exception ex ) {
                throw new SQLException ( "Error creating datasource", ex );
            } finally {
                if ( !success ) {
                    closeConnectionPool();
                }
            }
            try {
                for ( int i = 0 ; i < initialSize ; i++ ) {
                    connectionPool.addObject();
                }
            } catch ( final Exception e ) {
                closeConnectionPool();
                throw new SQLException ( "Error preloading the connection pool", e );
            }
            startPoolMaintenance();
            dataSource = newDataSource;
            return dataSource;
        }
    }
    protected ConnectionFactory createConnectionFactory() throws SQLException {
        Driver driverToUse = this.driver;
        if ( driverToUse == null ) {
            Class<?> driverFromCCL = null;
            if ( driverClassName != null ) {
                try {
                    try {
                        if ( driverClassLoader == null ) {
                            driverFromCCL = Class.forName ( driverClassName );
                        } else {
                            driverFromCCL = Class.forName (
                                                driverClassName, true, driverClassLoader );
                        }
                    } catch ( final ClassNotFoundException cnfe ) {
                        driverFromCCL = Thread.currentThread (
                                        ).getContextClassLoader().loadClass (
                                            driverClassName );
                    }
                } catch ( final Exception t ) {
                    final String message = "Cannot load JDBC driver class '" +
                                           driverClassName + "'";
                    logWriter.println ( message );
                    t.printStackTrace ( logWriter );
                    throw new SQLException ( message, t );
                }
            }
            try {
                if ( driverFromCCL == null ) {
                    driverToUse = DriverManager.getDriver ( url );
                } else {
                    driverToUse = ( Driver ) driverFromCCL.newInstance();
                    if ( !driverToUse.acceptsURL ( url ) ) {
                        throw new SQLException ( "No suitable driver", "08001" );
                    }
                }
            } catch ( final Exception t ) {
                final String message = "Cannot create JDBC driver of class '" +
                                       ( driverClassName != null ? driverClassName : "" ) +
                                       "' for connect URL '" + url + "'";
                logWriter.println ( message );
                t.printStackTrace ( logWriter );
                throw new SQLException ( message, t );
            }
        }
        final String user = username;
        if ( user != null ) {
            connectionProperties.put ( "user", user );
        } else {
            log ( "DBCP DataSource configured without a 'username'" );
        }
        final String pwd = password;
        if ( pwd != null ) {
            connectionProperties.put ( "password", pwd );
        } else {
            log ( "DBCP DataSource configured without a 'password'" );
        }
        final ConnectionFactory driverConnectionFactory =
            new DriverConnectionFactory ( driverToUse, url, connectionProperties );
        return driverConnectionFactory;
    }
    protected void createConnectionPool ( final PoolableConnectionFactory factory ) {
        final GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        updateJmxName ( config );
        config.setJmxEnabled ( registeredJmxName != null );
        GenericObjectPool<PoolableConnection> gop;
        if ( abandonedConfig != null &&
                ( abandonedConfig.getRemoveAbandonedOnBorrow() ||
                  abandonedConfig.getRemoveAbandonedOnMaintenance() ) ) {
            gop = new GenericObjectPool<> ( factory, config, abandonedConfig );
        } else {
            gop = new GenericObjectPool<> ( factory, config );
        }
        gop.setMaxTotal ( maxTotal );
        gop.setMaxIdle ( maxIdle );
        gop.setMinIdle ( minIdle );
        gop.setMaxWaitMillis ( maxWaitMillis );
        gop.setTestOnCreate ( testOnCreate );
        gop.setTestOnBorrow ( testOnBorrow );
        gop.setTestOnReturn ( testOnReturn );
        gop.setNumTestsPerEvictionRun ( numTestsPerEvictionRun );
        gop.setMinEvictableIdleTimeMillis ( minEvictableIdleTimeMillis );
        gop.setSoftMinEvictableIdleTimeMillis ( softMinEvictableIdleTimeMillis );
        gop.setTestWhileIdle ( testWhileIdle );
        gop.setLifo ( lifo );
        gop.setSwallowedExceptionListener ( new SwallowedExceptionLogger ( log, logExpiredConnections ) );
        gop.setEvictionPolicyClassName ( evictionPolicyClassName );
        factory.setPool ( gop );
        connectionPool = gop;
    }
    private void closeConnectionPool() {
        final GenericObjectPool<?> oldpool = connectionPool;
        connectionPool = null;
        try {
            if ( oldpool != null ) {
                oldpool.close();
            }
        } catch ( final Exception e ) {
        }
    }
    protected void startPoolMaintenance() {
        if ( connectionPool != null && timeBetweenEvictionRunsMillis > 0 ) {
            connectionPool.setTimeBetweenEvictionRunsMillis ( timeBetweenEvictionRunsMillis );
        }
    }
    protected DataSource createDataSourceInstance() throws SQLException {
        final PoolingDataSource<PoolableConnection> pds = new PoolingDataSource<> ( connectionPool );
        pds.setAccessToUnderlyingConnectionAllowed ( isAccessToUnderlyingConnectionAllowed() );
        return pds;
    }
    protected PoolableConnectionFactory createPoolableConnectionFactory (
        final ConnectionFactory driverConnectionFactory ) throws SQLException {
        PoolableConnectionFactory connectionFactory = null;
        try {
            connectionFactory = new PoolableConnectionFactory ( driverConnectionFactory, registeredJmxName );
            connectionFactory.setValidationQuery ( validationQuery );
            connectionFactory.setValidationQueryTimeout ( validationQueryTimeout );
            connectionFactory.setConnectionInitSql ( connectionInitSqls );
            connectionFactory.setDefaultReadOnly ( defaultReadOnly );
            connectionFactory.setDefaultAutoCommit ( defaultAutoCommit );
            connectionFactory.setDefaultTransactionIsolation ( defaultTransactionIsolation );
            connectionFactory.setDefaultCatalog ( defaultCatalog );
            connectionFactory.setCacheState ( cacheState );
            connectionFactory.setPoolStatements ( poolPreparedStatements );
            connectionFactory.setMaxOpenPrepatedStatements ( maxOpenPreparedStatements );
            connectionFactory.setMaxConnLifetimeMillis ( maxConnLifetimeMillis );
            connectionFactory.setRollbackOnReturn ( getRollbackOnReturn() );
            connectionFactory.setEnableAutoCommitOnReturn ( getEnableAutoCommitOnReturn() );
            connectionFactory.setDefaultQueryTimeout ( getDefaultQueryTimeout() );
            connectionFactory.setFastFailValidation ( fastFailValidation );
            connectionFactory.setDisconnectionSqlCodes ( disconnectionSqlCodes );
            validateConnectionFactory ( connectionFactory );
        } catch ( final RuntimeException e ) {
            throw e;
        } catch ( final Exception e ) {
            throw new SQLException ( "Cannot create PoolableConnectionFactory (" + e.getMessage() + ")", e );
        }
        return connectionFactory;
    }
    protected static void validateConnectionFactory (
        final PoolableConnectionFactory connectionFactory ) throws Exception {
        PoolableConnection conn = null;
        PooledObject<PoolableConnection> p = null;
        try {
            p = connectionFactory.makeObject();
            conn = p.getObject();
            connectionFactory.activateObject ( p );
            connectionFactory.validateConnection ( conn );
            connectionFactory.passivateObject ( p );
        } finally {
            if ( p != null ) {
                connectionFactory.destroyObject ( p );
            }
        }
    }
    protected void log ( final String message ) {
        if ( logWriter != null ) {
            logWriter.println ( message );
        }
    }
    private ObjectName registeredJmxName = null;
    private void jmxRegister() {
        if ( registeredJmxName != null ) {
            return;
        }
        final String requestedName = getJmxName();
        if ( requestedName == null ) {
            return;
        }
        ObjectName oname;
        try {
            oname = new ObjectName ( requestedName );
        } catch ( final MalformedObjectNameException e ) {
            log.warn ( "The requested JMX name [" + requestedName +
                       "] was not valid and will be ignored." );
            return;
        }
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            mbs.registerMBean ( this, oname );
        } catch ( InstanceAlreadyExistsException | MBeanRegistrationException
                      | NotCompliantMBeanException e ) {
            log.warn ( "Failed to complete JMX registration", e );
        }
    }
    @Override
    public ObjectName preRegister ( final MBeanServer server, final ObjectName name ) {
        final String requestedName = getJmxName();
        if ( requestedName != null ) {
            try {
                registeredJmxName = new ObjectName ( requestedName );
            } catch ( final MalformedObjectNameException e ) {
                log.warn ( "The requested JMX name [" + requestedName +
                           "] was not valid and will be ignored." );
            }
        }
        if ( registeredJmxName == null ) {
            registeredJmxName = name;
        }
        return registeredJmxName;
    }
    @Override
    public void postRegister ( final Boolean registrationDone ) {
    }
    @Override
    public void preDeregister() throws Exception {
    }
    @Override
    public void postDeregister() {
    }
    private void updateJmxName ( final GenericObjectPoolConfig config ) {
        if ( registeredJmxName == null ) {
            return;
        }
        final StringBuilder base = new StringBuilder ( registeredJmxName.toString() );
        base.append ( Constants.JMX_CONNECTION_POOL_BASE_EXT );
        config.setJmxNameBase ( base.toString() );
        config.setJmxNamePrefix ( Constants.JMX_CONNECTION_POOL_PREFIX );
    }
    protected ObjectName getRegisteredJmxName() {
        return registeredJmxName;
    }
    private class PaGetConnection implements PrivilegedExceptionAction<Connection> {
        @Override
        public Connection run() throws SQLException {
            return createDataSource().getConnection();
        }
    }
}
