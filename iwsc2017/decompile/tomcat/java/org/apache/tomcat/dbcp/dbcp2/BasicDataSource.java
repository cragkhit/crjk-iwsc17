package org.apache.tomcat.dbcp.dbcp2;
import java.util.Hashtable;
import org.apache.juli.logging.LogFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MalformedObjectNameException;
import org.apache.tomcat.dbcp.pool2.PooledObject;
import org.apache.tomcat.dbcp.pool2.ObjectPool;
import org.apache.tomcat.dbcp.pool2.SwallowedExceptionListener;
import org.apache.tomcat.dbcp.pool2.PooledObjectFactory;
import org.apache.tomcat.dbcp.pool2.impl.GenericObjectPoolConfig;
import java.sql.DriverManager;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import javax.management.JMException;
import java.lang.management.ManagementFactory;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.sql.SQLException;
import java.security.AccessController;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.io.Writer;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import javax.management.ObjectName;
import org.apache.tomcat.dbcp.pool2.impl.AbandonedConfig;
import java.io.PrintWriter;
import java.util.Properties;
import org.apache.tomcat.dbcp.pool2.impl.GenericObjectPool;
import java.util.Set;
import java.util.List;
import java.sql.Driver;
import org.apache.juli.logging.Log;
import javax.management.MBeanRegistration;
import javax.sql.DataSource;
public class BasicDataSource implements DataSource, BasicDataSourceMXBean, MBeanRegistration, AutoCloseable {
    private static final Log log;
    private volatile Boolean defaultAutoCommit;
    private transient Boolean defaultReadOnly;
    private volatile int defaultTransactionIsolation;
    private Integer defaultQueryTimeout;
    private volatile String defaultCatalog;
    private boolean cacheState;
    private Driver driver;
    private String driverClassName;
    private ClassLoader driverClassLoader;
    private boolean lifo;
    private int maxTotal;
    private int maxIdle;
    private int minIdle;
    private int initialSize;
    private long maxWaitMillis;
    private boolean poolPreparedStatements;
    private int maxOpenPreparedStatements;
    private boolean testOnCreate;
    private boolean testOnBorrow;
    private boolean testOnReturn;
    private long timeBetweenEvictionRunsMillis;
    private int numTestsPerEvictionRun;
    private long minEvictableIdleTimeMillis;
    private long softMinEvictableIdleTimeMillis;
    private String evictionPolicyClassName;
    private boolean testWhileIdle;
    private volatile String password;
    private String url;
    private String username;
    private volatile String validationQuery;
    private volatile int validationQueryTimeout;
    private volatile List<String> connectionInitSqls;
    private boolean accessToUnderlyingConnectionAllowed;
    private long maxConnLifetimeMillis;
    private boolean logExpiredConnections;
    private String jmxName;
    private boolean enableAutoCommitOnReturn;
    private boolean rollbackOnReturn;
    private volatile Set<String> disconnectionSqlCodes;
    private boolean fastFailValidation;
    private volatile GenericObjectPool<PoolableConnection> connectionPool;
    private Properties connectionProperties;
    private volatile DataSource dataSource;
    private volatile PrintWriter logWriter;
    private AbandonedConfig abandonedConfig;
    private boolean closed;
    private ObjectName registeredJmxName;
    public BasicDataSource() {
        this.defaultAutoCommit = null;
        this.defaultReadOnly = null;
        this.defaultTransactionIsolation = -1;
        this.defaultQueryTimeout = null;
        this.defaultCatalog = null;
        this.cacheState = true;
        this.driver = null;
        this.driverClassName = null;
        this.driverClassLoader = null;
        this.lifo = true;
        this.maxTotal = 8;
        this.maxIdle = 8;
        this.minIdle = 0;
        this.initialSize = 0;
        this.maxWaitMillis = -1L;
        this.poolPreparedStatements = false;
        this.maxOpenPreparedStatements = -1;
        this.testOnCreate = false;
        this.testOnBorrow = true;
        this.testOnReturn = false;
        this.timeBetweenEvictionRunsMillis = -1L;
        this.numTestsPerEvictionRun = 3;
        this.minEvictableIdleTimeMillis = 1800000L;
        this.softMinEvictableIdleTimeMillis = -1L;
        this.evictionPolicyClassName = "org.apache.tomcat.dbcp.pool2.impl.DefaultEvictionPolicy";
        this.testWhileIdle = false;
        this.password = null;
        this.url = null;
        this.username = null;
        this.validationQuery = null;
        this.validationQueryTimeout = -1;
        this.accessToUnderlyingConnectionAllowed = false;
        this.maxConnLifetimeMillis = -1L;
        this.logExpiredConnections = true;
        this.jmxName = null;
        this.enableAutoCommitOnReturn = true;
        this.rollbackOnReturn = true;
        this.connectionPool = null;
        this.connectionProperties = new Properties();
        this.dataSource = null;
        this.logWriter = new PrintWriter ( new OutputStreamWriter ( System.out, StandardCharsets.UTF_8 ) );
        this.registeredJmxName = null;
    }
    @Override
    public Boolean getDefaultAutoCommit() {
        return this.defaultAutoCommit;
    }
    public void setDefaultAutoCommit ( final Boolean defaultAutoCommit ) {
        this.defaultAutoCommit = defaultAutoCommit;
    }
    @Override
    public Boolean getDefaultReadOnly() {
        return this.defaultReadOnly;
    }
    public void setDefaultReadOnly ( final Boolean defaultReadOnly ) {
        this.defaultReadOnly = defaultReadOnly;
    }
    @Override
    public int getDefaultTransactionIsolation() {
        return this.defaultTransactionIsolation;
    }
    public void setDefaultTransactionIsolation ( final int defaultTransactionIsolation ) {
        this.defaultTransactionIsolation = defaultTransactionIsolation;
    }
    public Integer getDefaultQueryTimeout() {
        return this.defaultQueryTimeout;
    }
    public void setDefaultQueryTimeout ( final Integer defaultQueryTimeout ) {
        this.defaultQueryTimeout = defaultQueryTimeout;
    }
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
    @Override
    public boolean getCacheState() {
        return this.cacheState;
    }
    public void setCacheState ( final boolean cacheState ) {
        this.cacheState = cacheState;
    }
    public synchronized Driver getDriver() {
        return this.driver;
    }
    public synchronized void setDriver ( final Driver driver ) {
        this.driver = driver;
    }
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
    public synchronized ClassLoader getDriverClassLoader() {
        return this.driverClassLoader;
    }
    public synchronized void setDriverClassLoader ( final ClassLoader driverClassLoader ) {
        this.driverClassLoader = driverClassLoader;
    }
    @Override
    public synchronized boolean getLifo() {
        return this.lifo;
    }
    public synchronized void setLifo ( final boolean lifo ) {
        this.lifo = lifo;
        if ( this.connectionPool != null ) {
            this.connectionPool.setLifo ( lifo );
        }
    }
    @Override
    public synchronized int getMaxTotal() {
        return this.maxTotal;
    }
    public synchronized void setMaxTotal ( final int maxTotal ) {
        this.maxTotal = maxTotal;
        if ( this.connectionPool != null ) {
            this.connectionPool.setMaxTotal ( maxTotal );
        }
    }
    @Override
    public synchronized int getMaxIdle() {
        return this.maxIdle;
    }
    public synchronized void setMaxIdle ( final int maxIdle ) {
        this.maxIdle = maxIdle;
        if ( this.connectionPool != null ) {
            this.connectionPool.setMaxIdle ( maxIdle );
        }
    }
    @Override
    public synchronized int getMinIdle() {
        return this.minIdle;
    }
    public synchronized void setMinIdle ( final int minIdle ) {
        this.minIdle = minIdle;
        if ( this.connectionPool != null ) {
            this.connectionPool.setMinIdle ( minIdle );
        }
    }
    @Override
    public synchronized int getInitialSize() {
        return this.initialSize;
    }
    public synchronized void setInitialSize ( final int initialSize ) {
        this.initialSize = initialSize;
    }
    @Override
    public synchronized long getMaxWaitMillis() {
        return this.maxWaitMillis;
    }
    public synchronized void setMaxWaitMillis ( final long maxWaitMillis ) {
        this.maxWaitMillis = maxWaitMillis;
        if ( this.connectionPool != null ) {
            this.connectionPool.setMaxWaitMillis ( maxWaitMillis );
        }
    }
    @Override
    public synchronized boolean isPoolPreparedStatements() {
        return this.poolPreparedStatements;
    }
    public synchronized void setPoolPreparedStatements ( final boolean poolingStatements ) {
        this.poolPreparedStatements = poolingStatements;
    }
    @Override
    public synchronized int getMaxOpenPreparedStatements() {
        return this.maxOpenPreparedStatements;
    }
    public synchronized void setMaxOpenPreparedStatements ( final int maxOpenStatements ) {
        this.maxOpenPreparedStatements = maxOpenStatements;
    }
    @Override
    public synchronized boolean getTestOnCreate() {
        return this.testOnCreate;
    }
    public synchronized void setTestOnCreate ( final boolean testOnCreate ) {
        this.testOnCreate = testOnCreate;
        if ( this.connectionPool != null ) {
            this.connectionPool.setTestOnCreate ( testOnCreate );
        }
    }
    @Override
    public synchronized boolean getTestOnBorrow() {
        return this.testOnBorrow;
    }
    public synchronized void setTestOnBorrow ( final boolean testOnBorrow ) {
        this.testOnBorrow = testOnBorrow;
        if ( this.connectionPool != null ) {
            this.connectionPool.setTestOnBorrow ( testOnBorrow );
        }
    }
    public synchronized boolean getTestOnReturn() {
        return this.testOnReturn;
    }
    public synchronized void setTestOnReturn ( final boolean testOnReturn ) {
        this.testOnReturn = testOnReturn;
        if ( this.connectionPool != null ) {
            this.connectionPool.setTestOnReturn ( testOnReturn );
        }
    }
    @Override
    public synchronized long getTimeBetweenEvictionRunsMillis() {
        return this.timeBetweenEvictionRunsMillis;
    }
    public synchronized void setTimeBetweenEvictionRunsMillis ( final long timeBetweenEvictionRunsMillis ) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        if ( this.connectionPool != null ) {
            this.connectionPool.setTimeBetweenEvictionRunsMillis ( timeBetweenEvictionRunsMillis );
        }
    }
    @Override
    public synchronized int getNumTestsPerEvictionRun() {
        return this.numTestsPerEvictionRun;
    }
    public synchronized void setNumTestsPerEvictionRun ( final int numTestsPerEvictionRun ) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
        if ( this.connectionPool != null ) {
            this.connectionPool.setNumTestsPerEvictionRun ( numTestsPerEvictionRun );
        }
    }
    @Override
    public synchronized long getMinEvictableIdleTimeMillis() {
        return this.minEvictableIdleTimeMillis;
    }
    public synchronized void setMinEvictableIdleTimeMillis ( final long minEvictableIdleTimeMillis ) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
        if ( this.connectionPool != null ) {
            this.connectionPool.setMinEvictableIdleTimeMillis ( minEvictableIdleTimeMillis );
        }
    }
    public synchronized void setSoftMinEvictableIdleTimeMillis ( final long softMinEvictableIdleTimeMillis ) {
        this.softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
        if ( this.connectionPool != null ) {
            this.connectionPool.setSoftMinEvictableIdleTimeMillis ( softMinEvictableIdleTimeMillis );
        }
    }
    @Override
    public synchronized long getSoftMinEvictableIdleTimeMillis() {
        return this.softMinEvictableIdleTimeMillis;
    }
    public synchronized String getEvictionPolicyClassName() {
        return this.evictionPolicyClassName;
    }
    public synchronized void setEvictionPolicyClassName ( final String evictionPolicyClassName ) {
        if ( this.connectionPool != null ) {
            this.connectionPool.setEvictionPolicyClassName ( evictionPolicyClassName );
        }
        this.evictionPolicyClassName = evictionPolicyClassName;
    }
    @Override
    public synchronized boolean getTestWhileIdle() {
        return this.testWhileIdle;
    }
    public synchronized void setTestWhileIdle ( final boolean testWhileIdle ) {
        this.testWhileIdle = testWhileIdle;
        if ( this.connectionPool != null ) {
            this.connectionPool.setTestWhileIdle ( testWhileIdle );
        }
    }
    @Override
    public int getNumActive() {
        final GenericObjectPool<PoolableConnection> pool = this.connectionPool;
        if ( pool != null ) {
            return pool.getNumActive();
        }
        return 0;
    }
    @Override
    public int getNumIdle() {
        final GenericObjectPool<PoolableConnection> pool = this.connectionPool;
        if ( pool != null ) {
            return pool.getNumIdle();
        }
        return 0;
    }
    @Override
    public String getPassword() {
        return this.password;
    }
    public void setPassword ( final String password ) {
        this.password = password;
    }
    @Override
    public synchronized String getUrl() {
        return this.url;
    }
    public synchronized void setUrl ( final String url ) {
        this.url = url;
    }
    @Override
    public String getUsername() {
        return this.username;
    }
    public void setUsername ( final String username ) {
        this.username = username;
    }
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
    @Override
    public int getValidationQueryTimeout() {
        return this.validationQueryTimeout;
    }
    public void setValidationQueryTimeout ( final int timeout ) {
        this.validationQueryTimeout = timeout;
    }
    public List<String> getConnectionInitSqls() {
        final List<String> result = this.connectionInitSqls;
        if ( result == null ) {
            return Collections.emptyList();
        }
        return result;
    }
    @Override
    public String[] getConnectionInitSqlsAsArray() {
        final Collection<String> result = this.getConnectionInitSqls();
        return result.toArray ( new String[result.size()] );
    }
    public void setConnectionInitSqls ( final Collection<String> connectionInitSqls ) {
        if ( connectionInitSqls != null && connectionInitSqls.size() > 0 ) {
            ArrayList<String> newVal = null;
            for ( final String s : connectionInitSqls ) {
                if ( s != null && s.trim().length() > 0 ) {
                    if ( newVal == null ) {
                        newVal = new ArrayList<String>();
                    }
                    newVal.add ( s );
                }
            }
            this.connectionInitSqls = newVal;
        } else {
            this.connectionInitSqls = null;
        }
    }
    @Override
    public synchronized boolean isAccessToUnderlyingConnectionAllowed() {
        return this.accessToUnderlyingConnectionAllowed;
    }
    public synchronized void setAccessToUnderlyingConnectionAllowed ( final boolean allow ) {
        this.accessToUnderlyingConnectionAllowed = allow;
    }
    @Override
    public long getMaxConnLifetimeMillis() {
        return this.maxConnLifetimeMillis;
    }
    @Override
    public boolean getLogExpiredConnections() {
        return this.logExpiredConnections;
    }
    public void setMaxConnLifetimeMillis ( final long maxConnLifetimeMillis ) {
        this.maxConnLifetimeMillis = maxConnLifetimeMillis;
    }
    public void setLogExpiredConnections ( final boolean logExpiredConnections ) {
        this.logExpiredConnections = logExpiredConnections;
    }
    public String getJmxName() {
        return this.jmxName;
    }
    public void setJmxName ( final String jmxName ) {
        this.jmxName = jmxName;
    }
    public boolean getEnableAutoCommitOnReturn() {
        return this.enableAutoCommitOnReturn;
    }
    public void setEnableAutoCommitOnReturn ( final boolean enableAutoCommitOnReturn ) {
        this.enableAutoCommitOnReturn = enableAutoCommitOnReturn;
    }
    public boolean getRollbackOnReturn() {
        return this.rollbackOnReturn;
    }
    public void setRollbackOnReturn ( final boolean rollbackOnReturn ) {
        this.rollbackOnReturn = rollbackOnReturn;
    }
    public Set<String> getDisconnectionSqlCodes() {
        final Set<String> result = this.disconnectionSqlCodes;
        if ( result == null ) {
            return Collections.emptySet();
        }
        return result;
    }
    @Override
    public String[] getDisconnectionSqlCodesAsArray() {
        final Collection<String> result = this.getDisconnectionSqlCodes();
        return result.toArray ( new String[result.size()] );
    }
    public void setDisconnectionSqlCodes ( final Collection<String> disconnectionSqlCodes ) {
        if ( disconnectionSqlCodes != null && disconnectionSqlCodes.size() > 0 ) {
            HashSet<String> newVal = null;
            for ( final String s : disconnectionSqlCodes ) {
                if ( s != null && s.trim().length() > 0 ) {
                    if ( newVal == null ) {
                        newVal = new HashSet<String>();
                    }
                    newVal.add ( s );
                }
            }
            this.disconnectionSqlCodes = newVal;
        } else {
            this.disconnectionSqlCodes = null;
        }
    }
    @Override
    public boolean getFastFailValidation() {
        return this.fastFailValidation;
    }
    public void setFastFailValidation ( final boolean fastFailValidation ) {
        this.fastFailValidation = fastFailValidation;
    }
    protected GenericObjectPool<PoolableConnection> getConnectionPool() {
        return this.connectionPool;
    }
    Properties getConnectionProperties() {
        return this.connectionProperties;
    }
    @Override
    public Connection getConnection() throws SQLException {
        if ( Utils.IS_SECURITY_ENABLED ) {
            final PrivilegedExceptionAction<Connection> action = new PaGetConnection();
            try {
                return AccessController.doPrivileged ( action );
            } catch ( PrivilegedActionException e ) {
                final Throwable cause = e.getCause();
                if ( cause instanceof SQLException ) {
                    throw ( SQLException ) cause;
                }
                throw new SQLException ( e );
            }
        }
        return this.createDataSource().getConnection();
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
        return this.createDataSource().getLogWriter();
    }
    @Override
    public void setLoginTimeout ( final int loginTimeout ) throws SQLException {
        throw new UnsupportedOperationException ( "Not supported by BasicDataSource" );
    }
    @Override
    public void setLogWriter ( final PrintWriter logWriter ) throws SQLException {
        this.createDataSource().setLogWriter ( logWriter );
        this.logWriter = logWriter;
    }
    @Override
    public boolean getRemoveAbandonedOnBorrow() {
        return this.abandonedConfig != null && this.abandonedConfig.getRemoveAbandonedOnBorrow();
    }
    public void setRemoveAbandonedOnMaintenance ( final boolean removeAbandonedOnMaintenance ) {
        if ( this.abandonedConfig == null ) {
            this.abandonedConfig = new AbandonedConfig();
        }
        this.abandonedConfig.setRemoveAbandonedOnMaintenance ( removeAbandonedOnMaintenance );
    }
    @Override
    public boolean getRemoveAbandonedOnMaintenance() {
        return this.abandonedConfig != null && this.abandonedConfig.getRemoveAbandonedOnMaintenance();
    }
    public void setRemoveAbandonedOnBorrow ( final boolean removeAbandonedOnBorrow ) {
        if ( this.abandonedConfig == null ) {
            this.abandonedConfig = new AbandonedConfig();
        }
        this.abandonedConfig.setRemoveAbandonedOnBorrow ( removeAbandonedOnBorrow );
    }
    @Override
    public int getRemoveAbandonedTimeout() {
        if ( this.abandonedConfig != null ) {
            return this.abandonedConfig.getRemoveAbandonedTimeout();
        }
        return 300;
    }
    public void setRemoveAbandonedTimeout ( final int removeAbandonedTimeout ) {
        if ( this.abandonedConfig == null ) {
            this.abandonedConfig = new AbandonedConfig();
        }
        this.abandonedConfig.setRemoveAbandonedTimeout ( removeAbandonedTimeout );
    }
    @Override
    public boolean getLogAbandoned() {
        return this.abandonedConfig != null && this.abandonedConfig.getLogAbandoned();
    }
    public void setLogAbandoned ( final boolean logAbandoned ) {
        if ( this.abandonedConfig == null ) {
            this.abandonedConfig = new AbandonedConfig();
        }
        this.abandonedConfig.setLogAbandoned ( logAbandoned );
    }
    public PrintWriter getAbandonedLogWriter() {
        if ( this.abandonedConfig != null ) {
            return this.abandonedConfig.getLogWriter();
        }
        return null;
    }
    public void setAbandonedLogWriter ( final PrintWriter logWriter ) {
        if ( this.abandonedConfig == null ) {
            this.abandonedConfig = new AbandonedConfig();
        }
        this.abandonedConfig.setLogWriter ( logWriter );
    }
    @Override
    public boolean getAbandonedUsageTracking() {
        return this.abandonedConfig != null && this.abandonedConfig.getUseUsageTracking();
    }
    public void setAbandonedUsageTracking ( final boolean usageTracking ) {
        if ( this.abandonedConfig == null ) {
            this.abandonedConfig = new AbandonedConfig();
        }
        this.abandonedConfig.setUseUsageTracking ( usageTracking );
    }
    public void addConnectionProperty ( final String name, final String value ) {
        ( ( Hashtable<String, String> ) this.connectionProperties ).put ( name, value );
    }
    public void removeConnectionProperty ( final String name ) {
        this.connectionProperties.remove ( name );
    }
    public void setConnectionProperties ( final String connectionProperties ) {
        if ( connectionProperties == null ) {
            throw new NullPointerException ( "connectionProperties is null" );
        }
        final String[] entries = connectionProperties.split ( ";" );
        final Properties properties = new Properties();
        for ( final String entry : entries ) {
            if ( entry.length() > 0 ) {
                final int index = entry.indexOf ( 61 );
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
    @Override
    public synchronized void close() throws SQLException {
        if ( this.registeredJmxName != null ) {
            final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            try {
                mbs.unregisterMBean ( this.registeredJmxName );
            } catch ( JMException e ) {
                BasicDataSource.log.warn ( "Failed to unregister the JMX name: " + this.registeredJmxName, e );
            } finally {
                this.registeredJmxName = null;
            }
        }
        this.closed = true;
        final GenericObjectPool<?> oldpool = this.connectionPool;
        this.connectionPool = null;
        this.dataSource = null;
        try {
            if ( oldpool != null ) {
                oldpool.close();
            }
        } catch ( RuntimeException e2 ) {
            throw e2;
        } catch ( Exception e3 ) {
            throw new SQLException ( Utils.getMessage ( "pool.close.fail" ), e3 );
        }
    }
    @Override
    public synchronized boolean isClosed() {
        return this.closed;
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
        if ( this.connectionPool == null ) {
            throw new IllegalStateException ( "Cannot invalidate connection: ConnectionPool is null." );
        }
        PoolableConnection poolableConnection;
        try {
            poolableConnection = connection.unwrap ( PoolableConnection.class );
            if ( poolableConnection == null ) {
                throw new IllegalStateException ( "Cannot invalidate connection: Connection is not a poolable connection." );
            }
        } catch ( SQLException e ) {
            throw new IllegalStateException ( "Cannot invalidate connection: Unwrapping poolable connection failed.", e );
        }
        try {
            this.connectionPool.invalidateObject ( poolableConnection );
        } catch ( Exception e2 ) {
            throw new IllegalStateException ( "Invalidating connection threw unexpected exception", e2 );
        }
    }
    protected DataSource createDataSource() throws SQLException {
        if ( this.closed ) {
            throw new SQLException ( "Data source is closed" );
        }
        if ( this.dataSource != null ) {
            return this.dataSource;
        }
        synchronized ( this ) {
            if ( this.dataSource != null ) {
                return this.dataSource;
            }
            this.jmxRegister();
            final ConnectionFactory driverConnectionFactory = this.createConnectionFactory();
            boolean success = false;
            PoolableConnectionFactory poolableConnectionFactory;
            try {
                poolableConnectionFactory = this.createPoolableConnectionFactory ( driverConnectionFactory );
                poolableConnectionFactory.setPoolStatements ( this.poolPreparedStatements );
                poolableConnectionFactory.setMaxOpenPrepatedStatements ( this.maxOpenPreparedStatements );
                success = true;
            } catch ( SQLException se ) {
                throw se;
            } catch ( RuntimeException rte ) {
                throw rte;
            } catch ( Exception ex ) {
                throw new SQLException ( "Error creating connection factory", ex );
            }
            if ( success ) {
                this.createConnectionPool ( poolableConnectionFactory );
            }
            success = false;
            DataSource newDataSource;
            try {
                newDataSource = this.createDataSourceInstance();
                newDataSource.setLogWriter ( this.logWriter );
                success = true;
            } catch ( SQLException se2 ) {
                throw se2;
            } catch ( RuntimeException rte2 ) {
                throw rte2;
            } catch ( Exception ex2 ) {
                throw new SQLException ( "Error creating datasource", ex2 );
            } finally {
                if ( !success ) {
                    this.closeConnectionPool();
                }
            }
            try {
                for ( int i = 0; i < this.initialSize; ++i ) {
                    this.connectionPool.addObject();
                }
            } catch ( Exception e ) {
                this.closeConnectionPool();
                throw new SQLException ( "Error preloading the connection pool", e );
            }
            this.startPoolMaintenance();
            return this.dataSource = newDataSource;
        }
    }
    protected ConnectionFactory createConnectionFactory() throws SQLException {
        Driver driverToUse = this.driver;
        if ( driverToUse == null ) {
            Class<?> driverFromCCL = null;
            if ( this.driverClassName != null ) {
                try {
                    try {
                        if ( this.driverClassLoader == null ) {
                            driverFromCCL = Class.forName ( this.driverClassName );
                        } else {
                            driverFromCCL = Class.forName ( this.driverClassName, true, this.driverClassLoader );
                        }
                    } catch ( ClassNotFoundException cnfe ) {
                        driverFromCCL = Thread.currentThread().getContextClassLoader().loadClass ( this.driverClassName );
                    }
                } catch ( Exception t ) {
                    final String message = "Cannot load JDBC driver class '" + this.driverClassName + "'";
                    this.logWriter.println ( message );
                    t.printStackTrace ( this.logWriter );
                    throw new SQLException ( message, t );
                }
            }
            try {
                if ( driverFromCCL == null ) {
                    driverToUse = DriverManager.getDriver ( this.url );
                } else {
                    driverToUse = ( Driver ) driverFromCCL.newInstance();
                    if ( !driverToUse.acceptsURL ( this.url ) ) {
                        throw new SQLException ( "No suitable driver", "08001" );
                    }
                }
            } catch ( Exception t ) {
                final String message = "Cannot create JDBC driver of class '" + ( ( this.driverClassName != null ) ? this.driverClassName : "" ) + "' for connect URL '" + this.url + "'";
                this.logWriter.println ( message );
                t.printStackTrace ( this.logWriter );
                throw new SQLException ( message, t );
            }
        }
        final String user = this.username;
        if ( user != null ) {
            ( ( Hashtable<String, String> ) this.connectionProperties ).put ( "user", user );
        } else {
            this.log ( "DBCP DataSource configured without a 'username'" );
        }
        final String pwd = this.password;
        if ( pwd != null ) {
            ( ( Hashtable<String, String> ) this.connectionProperties ).put ( "password", pwd );
        } else {
            this.log ( "DBCP DataSource configured without a 'password'" );
        }
        final ConnectionFactory driverConnectionFactory = new DriverConnectionFactory ( driverToUse, this.url, this.connectionProperties );
        return driverConnectionFactory;
    }
    protected void createConnectionPool ( final PoolableConnectionFactory factory ) {
        final GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        this.updateJmxName ( config );
        config.setJmxEnabled ( this.registeredJmxName != null );
        GenericObjectPool<PoolableConnection> gop;
        if ( this.abandonedConfig != null && ( this.abandonedConfig.getRemoveAbandonedOnBorrow() || this.abandonedConfig.getRemoveAbandonedOnMaintenance() ) ) {
            gop = new GenericObjectPool<PoolableConnection> ( factory, config, this.abandonedConfig );
        } else {
            gop = new GenericObjectPool<PoolableConnection> ( factory, config );
        }
        gop.setMaxTotal ( this.maxTotal );
        gop.setMaxIdle ( this.maxIdle );
        gop.setMinIdle ( this.minIdle );
        gop.setMaxWaitMillis ( this.maxWaitMillis );
        gop.setTestOnCreate ( this.testOnCreate );
        gop.setTestOnBorrow ( this.testOnBorrow );
        gop.setTestOnReturn ( this.testOnReturn );
        gop.setNumTestsPerEvictionRun ( this.numTestsPerEvictionRun );
        gop.setMinEvictableIdleTimeMillis ( this.minEvictableIdleTimeMillis );
        gop.setSoftMinEvictableIdleTimeMillis ( this.softMinEvictableIdleTimeMillis );
        gop.setTestWhileIdle ( this.testWhileIdle );
        gop.setLifo ( this.lifo );
        gop.setSwallowedExceptionListener ( new SwallowedExceptionLogger ( BasicDataSource.log, this.logExpiredConnections ) );
        gop.setEvictionPolicyClassName ( this.evictionPolicyClassName );
        factory.setPool ( gop );
        this.connectionPool = gop;
    }
    private void closeConnectionPool() {
        final GenericObjectPool<?> oldpool = this.connectionPool;
        this.connectionPool = null;
        try {
            if ( oldpool != null ) {
                oldpool.close();
            }
        } catch ( Exception ex ) {}
    }
    protected void startPoolMaintenance() {
        if ( this.connectionPool != null && this.timeBetweenEvictionRunsMillis > 0L ) {
            this.connectionPool.setTimeBetweenEvictionRunsMillis ( this.timeBetweenEvictionRunsMillis );
        }
    }
    protected DataSource createDataSourceInstance() throws SQLException {
        final PoolingDataSource<PoolableConnection> pds = new PoolingDataSource<PoolableConnection> ( this.connectionPool );
        pds.setAccessToUnderlyingConnectionAllowed ( this.isAccessToUnderlyingConnectionAllowed() );
        return pds;
    }
    protected PoolableConnectionFactory createPoolableConnectionFactory ( final ConnectionFactory driverConnectionFactory ) throws SQLException {
        PoolableConnectionFactory connectionFactory = null;
        try {
            connectionFactory = new PoolableConnectionFactory ( driverConnectionFactory, this.registeredJmxName );
            connectionFactory.setValidationQuery ( this.validationQuery );
            connectionFactory.setValidationQueryTimeout ( this.validationQueryTimeout );
            connectionFactory.setConnectionInitSql ( this.connectionInitSqls );
            connectionFactory.setDefaultReadOnly ( this.defaultReadOnly );
            connectionFactory.setDefaultAutoCommit ( this.defaultAutoCommit );
            connectionFactory.setDefaultTransactionIsolation ( this.defaultTransactionIsolation );
            connectionFactory.setDefaultCatalog ( this.defaultCatalog );
            connectionFactory.setCacheState ( this.cacheState );
            connectionFactory.setPoolStatements ( this.poolPreparedStatements );
            connectionFactory.setMaxOpenPrepatedStatements ( this.maxOpenPreparedStatements );
            connectionFactory.setMaxConnLifetimeMillis ( this.maxConnLifetimeMillis );
            connectionFactory.setRollbackOnReturn ( this.getRollbackOnReturn() );
            connectionFactory.setEnableAutoCommitOnReturn ( this.getEnableAutoCommitOnReturn() );
            connectionFactory.setDefaultQueryTimeout ( this.getDefaultQueryTimeout() );
            connectionFactory.setFastFailValidation ( this.fastFailValidation );
            connectionFactory.setDisconnectionSqlCodes ( this.disconnectionSqlCodes );
            validateConnectionFactory ( connectionFactory );
        } catch ( RuntimeException e ) {
            throw e;
        } catch ( Exception e2 ) {
            throw new SQLException ( "Cannot create PoolableConnectionFactory (" + e2.getMessage() + ")", e2 );
        }
        return connectionFactory;
    }
    protected static void validateConnectionFactory ( final PoolableConnectionFactory connectionFactory ) throws Exception {
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
        if ( this.logWriter != null ) {
            this.logWriter.println ( message );
        }
    }
    private void jmxRegister() {
        if ( this.registeredJmxName != null ) {
            return;
        }
        final String requestedName = this.getJmxName();
        if ( requestedName == null ) {
            return;
        }
        ObjectName oname;
        try {
            oname = new ObjectName ( requestedName );
        } catch ( MalformedObjectNameException e2 ) {
            BasicDataSource.log.warn ( "The requested JMX name [" + requestedName + "] was not valid and will be ignored." );
            return;
        }
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            mbs.registerMBean ( this, oname );
        } catch ( InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e ) {
            BasicDataSource.log.warn ( "Failed to complete JMX registration", e );
        }
    }
    @Override
    public ObjectName preRegister ( final MBeanServer server, final ObjectName name ) {
        final String requestedName = this.getJmxName();
        if ( requestedName != null ) {
            try {
                this.registeredJmxName = new ObjectName ( requestedName );
            } catch ( MalformedObjectNameException e ) {
                BasicDataSource.log.warn ( "The requested JMX name [" + requestedName + "] was not valid and will be ignored." );
            }
        }
        if ( this.registeredJmxName == null ) {
            this.registeredJmxName = name;
        }
        return this.registeredJmxName;
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
        if ( this.registeredJmxName == null ) {
            return;
        }
        final StringBuilder base = new StringBuilder ( this.registeredJmxName.toString() );
        base.append ( ",connectionpool=" );
        config.setJmxNameBase ( base.toString() );
        config.setJmxNamePrefix ( "connections" );
    }
    protected ObjectName getRegisteredJmxName() {
        return this.registeredJmxName;
    }
    static {
        log = LogFactory.getLog ( BasicDataSource.class );
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
        } catch ( ClassNotFoundException cnfe ) {
            throw new IllegalStateException ( "Unable to pre-load classes", cnfe );
        }
    }
    private class PaGetConnection implements PrivilegedExceptionAction<Connection> {
        @Override
        public Connection run() throws SQLException {
            return BasicDataSource.this.createDataSource().getConnection();
        }
    }
}
