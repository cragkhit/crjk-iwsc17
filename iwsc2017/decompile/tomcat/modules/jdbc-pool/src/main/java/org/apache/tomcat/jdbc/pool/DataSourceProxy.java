// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.util.Hashtable;
import org.apache.juli.logging.LogFactory;
import java.io.PrintWriter;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import java.util.Iterator;
import java.util.Properties;
import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import java.util.concurrent.Future;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.juli.logging.Log;

public class DataSourceProxy implements PoolConfiguration
{
    private static final Log log;
    protected volatile ConnectionPool pool;
    protected volatile PoolConfiguration poolProperties;
    
    public DataSourceProxy() {
        this(new PoolProperties());
    }
    
    public DataSourceProxy(final PoolConfiguration poolProperties) {
        this.pool = null;
        this.poolProperties = null;
        if (poolProperties == null) {
            throw new NullPointerException("PoolConfiguration cannot be null.");
        }
        this.poolProperties = poolProperties;
    }
    
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return false;
    }
    
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return null;
    }
    
    public Connection getConnection(final String username, final String password) throws SQLException {
        if (!this.getPoolProperties().isAlternateUsernameAllowed()) {
            return this.getConnection();
        }
        if (this.pool == null) {
            return this.createPool().getConnection(username, password);
        }
        return this.pool.getConnection(username, password);
    }
    
    public PoolConfiguration getPoolProperties() {
        return this.poolProperties;
    }
    
    public ConnectionPool createPool() throws SQLException {
        if (this.pool != null) {
            return this.pool;
        }
        return this.pCreatePool();
    }
    
    private synchronized ConnectionPool pCreatePool() throws SQLException {
        if (this.pool != null) {
            return this.pool;
        }
        return this.pool = new ConnectionPool(this.poolProperties);
    }
    
    public Connection getConnection() throws SQLException {
        if (this.pool == null) {
            return this.createPool().getConnection();
        }
        return this.pool.getConnection();
    }
    
    public Future<Connection> getConnectionAsync() throws SQLException {
        if (this.pool == null) {
            return this.createPool().getConnectionAsync();
        }
        return this.pool.getConnectionAsync();
    }
    
    public XAConnection getXAConnection() throws SQLException {
        final Connection con = this.getConnection();
        if (con instanceof XAConnection) {
            return (XAConnection)con;
        }
        try {
            con.close();
        }
        catch (Exception ex) {}
        throw new SQLException("Connection from pool does not implement javax.sql.XAConnection");
    }
    
    public XAConnection getXAConnection(final String username, final String password) throws SQLException {
        final Connection con = this.getConnection(username, password);
        if (con instanceof XAConnection) {
            return (XAConnection)con;
        }
        try {
            con.close();
        }
        catch (Exception ex) {}
        throw new SQLException("Connection from pool does not implement javax.sql.XAConnection");
    }
    
    public PooledConnection getPooledConnection() throws SQLException {
        return (PooledConnection)this.getConnection();
    }
    
    public PooledConnection getPooledConnection(final String username, final String password) throws SQLException {
        return (PooledConnection)this.getConnection();
    }
    
    public ConnectionPool getPool() {
        try {
            return this.createPool();
        }
        catch (SQLException x) {
            DataSourceProxy.log.error((Object)"Error during connection pool creation.", (Throwable)x);
            return null;
        }
    }
    
    public void close() {
        this.close(false);
    }
    
    public void close(final boolean all) {
        try {
            if (this.pool != null) {
                final ConnectionPool p = this.pool;
                this.pool = null;
                if (p != null) {
                    p.close(all);
                }
            }
        }
        catch (Exception x) {
            DataSourceProxy.log.warn((Object)"Error during connection pool closure.", (Throwable)x);
        }
    }
    
    public int getPoolSize() {
        final ConnectionPool p = this.pool;
        if (p == null) {
            return 0;
        }
        return p.getSize();
    }
    
    @Override
    public String toString() {
        return super.toString() + "{" + this.getPoolProperties() + "}";
    }
    
    @Override
    public String getPoolName() {
        return this.pool.getName();
    }
    
    public void setPoolProperties(final PoolConfiguration poolProperties) {
        this.poolProperties = poolProperties;
    }
    
    @Override
    public void setDriverClassName(final String driverClassName) {
        this.poolProperties.setDriverClassName(driverClassName);
    }
    
    @Override
    public void setInitialSize(final int initialSize) {
        this.poolProperties.setInitialSize(initialSize);
    }
    
    @Override
    public void setInitSQL(final String initSQL) {
        this.poolProperties.setInitSQL(initSQL);
    }
    
    @Override
    public void setLogAbandoned(final boolean logAbandoned) {
        this.poolProperties.setLogAbandoned(logAbandoned);
    }
    
    @Override
    public void setMaxActive(final int maxActive) {
        this.poolProperties.setMaxActive(maxActive);
    }
    
    @Override
    public void setMaxIdle(final int maxIdle) {
        this.poolProperties.setMaxIdle(maxIdle);
    }
    
    @Override
    public void setMaxWait(final int maxWait) {
        this.poolProperties.setMaxWait(maxWait);
    }
    
    @Override
    public void setMinEvictableIdleTimeMillis(final int minEvictableIdleTimeMillis) {
        this.poolProperties.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
    }
    
    @Override
    public void setMinIdle(final int minIdle) {
        this.poolProperties.setMinIdle(minIdle);
    }
    
    @Override
    public void setNumTestsPerEvictionRun(final int numTestsPerEvictionRun) {
        this.poolProperties.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
    }
    
    @Override
    public void setPassword(final String password) {
        this.poolProperties.setPassword(password);
        this.poolProperties.getDbProperties().setProperty("password", this.poolProperties.getPassword());
    }
    
    @Override
    public void setRemoveAbandoned(final boolean removeAbandoned) {
        this.poolProperties.setRemoveAbandoned(removeAbandoned);
    }
    
    @Override
    public void setRemoveAbandonedTimeout(final int removeAbandonedTimeout) {
        this.poolProperties.setRemoveAbandonedTimeout(removeAbandonedTimeout);
    }
    
    @Override
    public void setTestOnBorrow(final boolean testOnBorrow) {
        this.poolProperties.setTestOnBorrow(testOnBorrow);
    }
    
    @Override
    public void setTestOnConnect(final boolean testOnConnect) {
        this.poolProperties.setTestOnConnect(testOnConnect);
    }
    
    @Override
    public void setTestOnReturn(final boolean testOnReturn) {
        this.poolProperties.setTestOnReturn(testOnReturn);
    }
    
    @Override
    public void setTestWhileIdle(final boolean testWhileIdle) {
        this.poolProperties.setTestWhileIdle(testWhileIdle);
    }
    
    @Override
    public void setTimeBetweenEvictionRunsMillis(final int timeBetweenEvictionRunsMillis) {
        this.poolProperties.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
    }
    
    @Override
    public void setUrl(final String url) {
        this.poolProperties.setUrl(url);
    }
    
    @Override
    public void setUsername(final String username) {
        this.poolProperties.setUsername(username);
        this.poolProperties.getDbProperties().setProperty("user", this.getPoolProperties().getUsername());
    }
    
    @Override
    public void setValidationInterval(final long validationInterval) {
        this.poolProperties.setValidationInterval(validationInterval);
    }
    
    @Override
    public void setValidationQuery(final String validationQuery) {
        this.poolProperties.setValidationQuery(validationQuery);
    }
    
    @Override
    public void setValidatorClassName(final String className) {
        this.poolProperties.setValidatorClassName(className);
    }
    
    @Override
    public void setValidationQueryTimeout(final int validationQueryTimeout) {
        this.poolProperties.setValidationQueryTimeout(validationQueryTimeout);
    }
    
    @Override
    public void setJdbcInterceptors(final String interceptors) {
        this.getPoolProperties().setJdbcInterceptors(interceptors);
    }
    
    @Override
    public void setJmxEnabled(final boolean enabled) {
        this.getPoolProperties().setJmxEnabled(enabled);
    }
    
    @Override
    public void setFairQueue(final boolean fairQueue) {
        this.getPoolProperties().setFairQueue(fairQueue);
    }
    
    @Override
    public void setUseLock(final boolean useLock) {
        this.getPoolProperties().setUseLock(useLock);
    }
    
    @Override
    public void setDefaultCatalog(final String catalog) {
        this.getPoolProperties().setDefaultCatalog(catalog);
    }
    
    @Override
    public void setDefaultAutoCommit(final Boolean autocommit) {
        this.getPoolProperties().setDefaultAutoCommit(autocommit);
    }
    
    @Override
    public void setDefaultTransactionIsolation(final int defaultTransactionIsolation) {
        this.getPoolProperties().setDefaultTransactionIsolation(defaultTransactionIsolation);
    }
    
    @Override
    public void setConnectionProperties(final String properties) {
        try {
            final Properties prop = DataSourceFactory.getProperties(properties);
            for (final String key : ((Hashtable<Object, V>)prop).keySet()) {
                final String value = prop.getProperty(key);
                this.getPoolProperties().getDbProperties().setProperty(key, value);
            }
        }
        catch (Exception x) {
            DataSourceProxy.log.error((Object)"Unable to parse connection properties.", (Throwable)x);
            throw new RuntimeException(x);
        }
    }
    
    @Override
    public void setUseEquals(final boolean useEquals) {
        this.getPoolProperties().setUseEquals(useEquals);
    }
    
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
    
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }
    
    public void setLogWriter(final PrintWriter out) throws SQLException {
    }
    
    public int getLoginTimeout() {
        if (this.poolProperties == null) {
            return 0;
        }
        return this.poolProperties.getMaxWait() / 1000;
    }
    
    public void setLoginTimeout(final int i) {
        if (this.poolProperties == null) {
            return;
        }
        this.poolProperties.setMaxWait(1000 * i);
    }
    
    @Override
    public int getSuspectTimeout() {
        return this.getPoolProperties().getSuspectTimeout();
    }
    
    @Override
    public void setSuspectTimeout(final int seconds) {
        this.getPoolProperties().setSuspectTimeout(seconds);
    }
    
    public int getIdle() {
        try {
            return this.createPool().getIdle();
        }
        catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }
    
    public int getNumIdle() {
        return this.getIdle();
    }
    
    public void checkAbandoned() {
        try {
            this.createPool().checkAbandoned();
        }
        catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }
    
    public void checkIdle() {
        try {
            this.createPool().checkIdle();
        }
        catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }
    
    public int getActive() {
        try {
            return this.createPool().getActive();
        }
        catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }
    
    public int getNumActive() {
        return this.getActive();
    }
    
    public int getWaitCount() {
        try {
            return this.createPool().getWaitCount();
        }
        catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }
    
    public int getSize() {
        try {
            return this.createPool().getSize();
        }
        catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }
    
    public void testIdle() {
        try {
            this.createPool().testAllIdle();
        }
        catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }
    
    @Override
    public String getConnectionProperties() {
        return this.getPoolProperties().getConnectionProperties();
    }
    
    @Override
    public Properties getDbProperties() {
        return this.getPoolProperties().getDbProperties();
    }
    
    @Override
    public String getDefaultCatalog() {
        return this.getPoolProperties().getDefaultCatalog();
    }
    
    @Override
    public int getDefaultTransactionIsolation() {
        return this.getPoolProperties().getDefaultTransactionIsolation();
    }
    
    @Override
    public String getDriverClassName() {
        return this.getPoolProperties().getDriverClassName();
    }
    
    @Override
    public int getInitialSize() {
        return this.getPoolProperties().getInitialSize();
    }
    
    @Override
    public String getInitSQL() {
        return this.getPoolProperties().getInitSQL();
    }
    
    @Override
    public String getJdbcInterceptors() {
        return this.getPoolProperties().getJdbcInterceptors();
    }
    
    @Override
    public int getMaxActive() {
        return this.getPoolProperties().getMaxActive();
    }
    
    @Override
    public int getMaxIdle() {
        return this.getPoolProperties().getMaxIdle();
    }
    
    @Override
    public int getMaxWait() {
        return this.getPoolProperties().getMaxWait();
    }
    
    @Override
    public int getMinEvictableIdleTimeMillis() {
        return this.getPoolProperties().getMinEvictableIdleTimeMillis();
    }
    
    @Override
    public int getMinIdle() {
        return this.getPoolProperties().getMinIdle();
    }
    
    @Override
    public long getMaxAge() {
        return this.getPoolProperties().getMaxAge();
    }
    
    @Override
    public String getName() {
        return this.getPoolProperties().getName();
    }
    
    @Override
    public int getNumTestsPerEvictionRun() {
        return this.getPoolProperties().getNumTestsPerEvictionRun();
    }
    
    @Override
    public String getPassword() {
        return "Password not available as DataSource/JMX operation.";
    }
    
    @Override
    public int getRemoveAbandonedTimeout() {
        return this.getPoolProperties().getRemoveAbandonedTimeout();
    }
    
    @Override
    public int getTimeBetweenEvictionRunsMillis() {
        return this.getPoolProperties().getTimeBetweenEvictionRunsMillis();
    }
    
    @Override
    public String getUrl() {
        return this.getPoolProperties().getUrl();
    }
    
    @Override
    public String getUsername() {
        return this.getPoolProperties().getUsername();
    }
    
    @Override
    public long getValidationInterval() {
        return this.getPoolProperties().getValidationInterval();
    }
    
    @Override
    public String getValidationQuery() {
        return this.getPoolProperties().getValidationQuery();
    }
    
    @Override
    public int getValidationQueryTimeout() {
        return this.getPoolProperties().getValidationQueryTimeout();
    }
    
    @Override
    public String getValidatorClassName() {
        return this.getPoolProperties().getValidatorClassName();
    }
    
    @Override
    public Validator getValidator() {
        return this.getPoolProperties().getValidator();
    }
    
    @Override
    public void setValidator(final Validator validator) {
        this.getPoolProperties().setValidator(validator);
    }
    
    @Override
    public boolean isAccessToUnderlyingConnectionAllowed() {
        return this.getPoolProperties().isAccessToUnderlyingConnectionAllowed();
    }
    
    @Override
    public Boolean isDefaultAutoCommit() {
        return this.getPoolProperties().isDefaultAutoCommit();
    }
    
    @Override
    public Boolean isDefaultReadOnly() {
        return this.getPoolProperties().isDefaultReadOnly();
    }
    
    @Override
    public boolean isLogAbandoned() {
        return this.getPoolProperties().isLogAbandoned();
    }
    
    @Override
    public boolean isPoolSweeperEnabled() {
        return this.getPoolProperties().isPoolSweeperEnabled();
    }
    
    @Override
    public boolean isRemoveAbandoned() {
        return this.getPoolProperties().isRemoveAbandoned();
    }
    
    @Override
    public int getAbandonWhenPercentageFull() {
        return this.getPoolProperties().getAbandonWhenPercentageFull();
    }
    
    @Override
    public boolean isTestOnBorrow() {
        return this.getPoolProperties().isTestOnBorrow();
    }
    
    @Override
    public boolean isTestOnConnect() {
        return this.getPoolProperties().isTestOnConnect();
    }
    
    @Override
    public boolean isTestOnReturn() {
        return this.getPoolProperties().isTestOnReturn();
    }
    
    @Override
    public boolean isTestWhileIdle() {
        return this.getPoolProperties().isTestWhileIdle();
    }
    
    @Override
    public Boolean getDefaultAutoCommit() {
        return this.getPoolProperties().getDefaultAutoCommit();
    }
    
    @Override
    public Boolean getDefaultReadOnly() {
        return this.getPoolProperties().getDefaultReadOnly();
    }
    
    @Override
    public PoolProperties.InterceptorDefinition[] getJdbcInterceptorsAsArray() {
        return this.getPoolProperties().getJdbcInterceptorsAsArray();
    }
    
    @Override
    public boolean getUseLock() {
        return this.getPoolProperties().getUseLock();
    }
    
    @Override
    public boolean isFairQueue() {
        return this.getPoolProperties().isFairQueue();
    }
    
    @Override
    public boolean isJmxEnabled() {
        return this.getPoolProperties().isJmxEnabled();
    }
    
    @Override
    public boolean isUseEquals() {
        return this.getPoolProperties().isUseEquals();
    }
    
    @Override
    public void setAbandonWhenPercentageFull(final int percentage) {
        this.getPoolProperties().setAbandonWhenPercentageFull(percentage);
    }
    
    @Override
    public void setAccessToUnderlyingConnectionAllowed(final boolean accessToUnderlyingConnectionAllowed) {
        this.getPoolProperties().setAccessToUnderlyingConnectionAllowed(accessToUnderlyingConnectionAllowed);
    }
    
    @Override
    public void setDbProperties(final Properties dbProperties) {
        this.getPoolProperties().setDbProperties(dbProperties);
    }
    
    @Override
    public void setDefaultReadOnly(final Boolean defaultReadOnly) {
        this.getPoolProperties().setDefaultReadOnly(defaultReadOnly);
    }
    
    @Override
    public void setMaxAge(final long maxAge) {
        this.getPoolProperties().setMaxAge(maxAge);
    }
    
    @Override
    public void setName(final String name) {
        this.getPoolProperties().setName(name);
    }
    
    @Override
    public void setDataSource(final Object ds) {
        this.getPoolProperties().setDataSource(ds);
    }
    
    @Override
    public Object getDataSource() {
        return this.getPoolProperties().getDataSource();
    }
    
    @Override
    public void setDataSourceJNDI(final String jndiDS) {
        this.getPoolProperties().setDataSourceJNDI(jndiDS);
    }
    
    @Override
    public String getDataSourceJNDI() {
        return this.getPoolProperties().getDataSourceJNDI();
    }
    
    @Override
    public boolean isAlternateUsernameAllowed() {
        return this.getPoolProperties().isAlternateUsernameAllowed();
    }
    
    @Override
    public void setAlternateUsernameAllowed(final boolean alternateUsernameAllowed) {
        this.getPoolProperties().setAlternateUsernameAllowed(alternateUsernameAllowed);
    }
    
    @Override
    public void setCommitOnReturn(final boolean commitOnReturn) {
        this.getPoolProperties().setCommitOnReturn(commitOnReturn);
    }
    
    @Override
    public boolean getCommitOnReturn() {
        return this.getPoolProperties().getCommitOnReturn();
    }
    
    @Override
    public void setRollbackOnReturn(final boolean rollbackOnReturn) {
        this.getPoolProperties().setRollbackOnReturn(rollbackOnReturn);
    }
    
    @Override
    public boolean getRollbackOnReturn() {
        return this.getPoolProperties().getRollbackOnReturn();
    }
    
    @Override
    public void setUseDisposableConnectionFacade(final boolean useDisposableConnectionFacade) {
        this.getPoolProperties().setUseDisposableConnectionFacade(useDisposableConnectionFacade);
    }
    
    @Override
    public boolean getUseDisposableConnectionFacade() {
        return this.getPoolProperties().getUseDisposableConnectionFacade();
    }
    
    @Override
    public void setLogValidationErrors(final boolean logValidationErrors) {
        this.getPoolProperties().setLogValidationErrors(logValidationErrors);
    }
    
    @Override
    public boolean getLogValidationErrors() {
        return this.getPoolProperties().getLogValidationErrors();
    }
    
    @Override
    public boolean getPropagateInterruptState() {
        return this.getPoolProperties().getPropagateInterruptState();
    }
    
    @Override
    public void setPropagateInterruptState(final boolean propagateInterruptState) {
        this.getPoolProperties().setPropagateInterruptState(propagateInterruptState);
    }
    
    @Override
    public boolean isIgnoreExceptionOnPreLoad() {
        return this.getPoolProperties().isIgnoreExceptionOnPreLoad();
    }
    
    @Override
    public void setIgnoreExceptionOnPreLoad(final boolean ignoreExceptionOnPreLoad) {
        this.getPoolProperties().setIgnoreExceptionOnPreLoad(ignoreExceptionOnPreLoad);
    }
    
    public void purge() {
        try {
            this.createPool().purge();
        }
        catch (SQLException x) {
            DataSourceProxy.log.error((Object)"Unable to purge pool.", (Throwable)x);
        }
    }
    
    public void purgeOnReturn() {
        try {
            this.createPool().purgeOnReturn();
        }
        catch (SQLException x) {
            DataSourceProxy.log.error((Object)"Unable to purge pool.", (Throwable)x);
        }
    }
    
    static {
        log = LogFactory.getLog((Class)DataSourceProxy.class);
    }
}
