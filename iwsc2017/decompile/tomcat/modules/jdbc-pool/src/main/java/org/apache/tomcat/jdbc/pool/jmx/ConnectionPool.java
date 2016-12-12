// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool.jmx;

import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.apache.tomcat.jdbc.pool.Validator;
import org.apache.tomcat.jdbc.pool.PoolUtilities;
import java.util.Properties;
import java.util.Iterator;
import javax.management.Notification;
import javax.management.MBeanNotificationInfo;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import javax.management.NotificationListener;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.juli.logging.Log;
import javax.management.NotificationBroadcasterSupport;

public class ConnectionPool extends NotificationBroadcasterSupport implements ConnectionPoolMBean
{
    private static final Log log;
    protected org.apache.tomcat.jdbc.pool.ConnectionPool pool;
    protected AtomicInteger sequence;
    protected ConcurrentLinkedQueue<NotificationListener> listeners;
    public static final String NOTIFY_INIT = "INIT FAILED";
    public static final String NOTIFY_CONNECT = "CONNECTION FAILED";
    public static final String NOTIFY_ABANDON = "CONNECTION ABANDONED";
    public static final String SLOW_QUERY_NOTIFICATION = "SLOW QUERY";
    public static final String FAILED_QUERY_NOTIFICATION = "FAILED QUERY";
    public static final String SUSPECT_ABANDONED_NOTIFICATION = "SUSPECT CONNETION ABANDONED";
    public static final String POOL_EMPTY = "POOL EMPTY";
    public static final String SUSPECT_RETURNED_NOTIFICATION = "SUSPECT CONNETION RETURNED";
    
    public ConnectionPool(final org.apache.tomcat.jdbc.pool.ConnectionPool pool) {
        this.pool = null;
        this.sequence = new AtomicInteger(0);
        this.listeners = new ConcurrentLinkedQueue<NotificationListener>();
        this.pool = pool;
    }
    
    public org.apache.tomcat.jdbc.pool.ConnectionPool getPool() {
        return this.pool;
    }
    
    public PoolConfiguration getPoolProperties() {
        return this.pool.getPoolProperties();
    }
    
    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        final MBeanNotificationInfo[] pres = super.getNotificationInfo();
        final MBeanNotificationInfo[] loc = getDefaultNotificationInfo();
        final MBeanNotificationInfo[] aug = new MBeanNotificationInfo[pres.length + loc.length];
        if (pres.length > 0) {
            System.arraycopy(pres, 0, aug, 0, pres.length);
        }
        if (loc.length > 0) {
            System.arraycopy(loc, 0, aug, pres.length, loc.length);
        }
        return aug;
    }
    
    public static MBeanNotificationInfo[] getDefaultNotificationInfo() {
        final String[] types = { "INIT FAILED", "CONNECTION FAILED", "CONNECTION ABANDONED", "SLOW QUERY", "FAILED QUERY", "SUSPECT CONNETION ABANDONED", "POOL EMPTY", "SUSPECT CONNETION RETURNED" };
        final String name = Notification.class.getName();
        final String description = "A connection pool error condition was met.";
        final MBeanNotificationInfo info = new MBeanNotificationInfo(types, name, description);
        return new MBeanNotificationInfo[] { info };
    }
    
    public boolean notify(final String type, final String message) {
        try {
            final Notification n = new Notification(type, this, this.sequence.incrementAndGet(), System.currentTimeMillis(), "[" + type + "] " + message);
            this.sendNotification(n);
            for (final NotificationListener listener : this.listeners) {
                listener.handleNotification(n, this);
            }
            return true;
        }
        catch (Exception x) {
            if (ConnectionPool.log.isDebugEnabled()) {
                ConnectionPool.log.debug((Object)("Notify failed. Type=" + type + "; Message=" + message), (Throwable)x);
            }
            return false;
        }
    }
    
    public void addListener(final NotificationListener list) {
        this.listeners.add(list);
    }
    
    public boolean removeListener(final NotificationListener list) {
        return this.listeners.remove(list);
    }
    
    @Override
    public int getSize() {
        return this.pool.getSize();
    }
    
    @Override
    public int getIdle() {
        return this.pool.getIdle();
    }
    
    @Override
    public int getActive() {
        return this.pool.getActive();
    }
    
    @Override
    public int getNumIdle() {
        return this.getIdle();
    }
    
    @Override
    public int getNumActive() {
        return this.getActive();
    }
    
    @Override
    public int getWaitCount() {
        return this.pool.getWaitCount();
    }
    
    @Override
    public void checkIdle() {
        this.pool.checkIdle();
    }
    
    @Override
    public void checkAbandoned() {
        this.pool.checkAbandoned();
    }
    
    @Override
    public void testIdle() {
        this.pool.testAllIdle();
    }
    
    @Override
    public String getConnectionProperties() {
        return this.getPoolProperties().getConnectionProperties();
    }
    
    @Override
    public Properties getDbProperties() {
        return PoolUtilities.cloneWithoutPassword(this.getPoolProperties().getDbProperties());
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
        return this.getPoolName();
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
    public String getPoolName() {
        return this.getPoolProperties().getName();
    }
    
    @Override
    public void setConnectionProperties(final String connectionProperties) {
        this.getPoolProperties().setConnectionProperties(connectionProperties);
    }
    
    @Override
    public void setDefaultAutoCommit(final Boolean defaultAutoCommit) {
        this.getPoolProperties().setDefaultAutoCommit(defaultAutoCommit);
    }
    
    @Override
    public void setDefaultCatalog(final String defaultCatalog) {
        this.getPoolProperties().setDefaultCatalog(defaultCatalog);
    }
    
    @Override
    public void setDefaultTransactionIsolation(final int defaultTransactionIsolation) {
        this.getPoolProperties().setDefaultTransactionIsolation(defaultTransactionIsolation);
    }
    
    @Override
    public void setDriverClassName(final String driverClassName) {
        this.getPoolProperties().setDriverClassName(driverClassName);
    }
    
    @Override
    public void setFairQueue(final boolean fairQueue) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void setInitialSize(final int initialSize) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void setInitSQL(final String initSQL) {
        this.getPoolProperties().setInitSQL(initSQL);
    }
    
    @Override
    public void setJdbcInterceptors(final String jdbcInterceptors) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void setJmxEnabled(final boolean jmxEnabled) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void setLogAbandoned(final boolean logAbandoned) {
        this.getPoolProperties().setLogAbandoned(logAbandoned);
    }
    
    @Override
    public void setMaxActive(final int maxActive) {
        this.getPoolProperties().setMaxActive(maxActive);
        this.pool.checkPoolConfiguration(this.getPoolProperties());
    }
    
    @Override
    public void setMaxIdle(final int maxIdle) {
        this.getPoolProperties().setMaxIdle(maxIdle);
        this.pool.checkPoolConfiguration(this.getPoolProperties());
    }
    
    @Override
    public void setMaxWait(final int maxWait) {
        this.getPoolProperties().setMaxWait(maxWait);
    }
    
    @Override
    public void setMinEvictableIdleTimeMillis(final int minEvictableIdleTimeMillis) {
        final boolean wasEnabled = this.getPoolProperties().isPoolSweeperEnabled();
        this.getPoolProperties().setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        final boolean shouldBeEnabled = this.getPoolProperties().isPoolSweeperEnabled();
        if (!wasEnabled && shouldBeEnabled) {
            this.pool.initializePoolCleaner(this.getPoolProperties());
        }
        else if (wasEnabled && !shouldBeEnabled) {
            this.pool.terminatePoolCleaner();
        }
    }
    
    @Override
    public void setMinIdle(final int minIdle) {
        this.getPoolProperties().setMinIdle(minIdle);
        this.pool.checkPoolConfiguration(this.getPoolProperties());
    }
    
    @Override
    public void setNumTestsPerEvictionRun(final int numTestsPerEvictionRun) {
        this.getPoolProperties().setNumTestsPerEvictionRun(numTestsPerEvictionRun);
    }
    
    @Override
    public void setPassword(final String password) {
        this.getPoolProperties().setPassword(password);
    }
    
    @Override
    public void setRemoveAbandoned(final boolean removeAbandoned) {
        final boolean wasEnabled = this.getPoolProperties().isPoolSweeperEnabled();
        this.getPoolProperties().setRemoveAbandoned(removeAbandoned);
        final boolean shouldBeEnabled = this.getPoolProperties().isPoolSweeperEnabled();
        if (!wasEnabled && shouldBeEnabled) {
            this.pool.initializePoolCleaner(this.getPoolProperties());
        }
        else if (wasEnabled && !shouldBeEnabled) {
            this.pool.terminatePoolCleaner();
        }
    }
    
    @Override
    public void setRemoveAbandonedTimeout(final int removeAbandonedTimeout) {
        final boolean wasEnabled = this.getPoolProperties().isPoolSweeperEnabled();
        this.getPoolProperties().setRemoveAbandonedTimeout(removeAbandonedTimeout);
        final boolean shouldBeEnabled = this.getPoolProperties().isPoolSweeperEnabled();
        if (!wasEnabled && shouldBeEnabled) {
            this.pool.initializePoolCleaner(this.getPoolProperties());
        }
        else if (wasEnabled && !shouldBeEnabled) {
            this.pool.terminatePoolCleaner();
        }
    }
    
    @Override
    public void setTestOnBorrow(final boolean testOnBorrow) {
        this.getPoolProperties().setTestOnBorrow(testOnBorrow);
    }
    
    @Override
    public void setTestOnConnect(final boolean testOnConnect) {
        this.getPoolProperties().setTestOnConnect(testOnConnect);
    }
    
    @Override
    public void setTestOnReturn(final boolean testOnReturn) {
        this.getPoolProperties().setTestOnReturn(testOnReturn);
    }
    
    @Override
    public void setTestWhileIdle(final boolean testWhileIdle) {
        final boolean wasEnabled = this.getPoolProperties().isPoolSweeperEnabled();
        this.getPoolProperties().setTestWhileIdle(testWhileIdle);
        final boolean shouldBeEnabled = this.getPoolProperties().isPoolSweeperEnabled();
        if (!wasEnabled && shouldBeEnabled) {
            this.pool.initializePoolCleaner(this.getPoolProperties());
        }
        else if (wasEnabled && !shouldBeEnabled) {
            this.pool.terminatePoolCleaner();
        }
    }
    
    @Override
    public void setTimeBetweenEvictionRunsMillis(final int timeBetweenEvictionRunsMillis) {
        final boolean wasEnabled = this.getPoolProperties().isPoolSweeperEnabled();
        this.getPoolProperties().setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        final boolean shouldBeEnabled = this.getPoolProperties().isPoolSweeperEnabled();
        if (!wasEnabled && shouldBeEnabled) {
            this.pool.initializePoolCleaner(this.getPoolProperties());
        }
        else if (wasEnabled) {
            this.pool.terminatePoolCleaner();
            if (shouldBeEnabled) {
                this.pool.initializePoolCleaner(this.getPoolProperties());
            }
        }
    }
    
    @Override
    public void setUrl(final String url) {
        this.getPoolProperties().setUrl(url);
    }
    
    @Override
    public void setUseEquals(final boolean useEquals) {
        this.getPoolProperties().setUseEquals(useEquals);
    }
    
    @Override
    public void setUseLock(final boolean useLock) {
        this.getPoolProperties().setUseLock(useLock);
    }
    
    @Override
    public void setUsername(final String username) {
        this.getPoolProperties().setUsername(username);
    }
    
    @Override
    public void setValidationInterval(final long validationInterval) {
        this.getPoolProperties().setValidationInterval(validationInterval);
    }
    
    @Override
    public void setValidationQuery(final String validationQuery) {
        this.getPoolProperties().setValidationQuery(validationQuery);
    }
    
    @Override
    public void setValidationQueryTimeout(final int validationQueryTimeout) {
        this.getPoolProperties().setValidationQueryTimeout(validationQueryTimeout);
    }
    
    @Override
    public void setValidatorClassName(final String className) {
        this.getPoolProperties().setValidatorClassName(className);
    }
    
    @Override
    public int getSuspectTimeout() {
        return this.getPoolProperties().getSuspectTimeout();
    }
    
    @Override
    public void setSuspectTimeout(final int seconds) {
        this.getPoolProperties().setSuspectTimeout(seconds);
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
    public void setValidator(final Validator validator) {
        this.getPoolProperties().setValidator(validator);
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
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void purge() {
        this.pool.purge();
    }
    
    @Override
    public void purgeOnReturn() {
        this.pool.purgeOnReturn();
    }
    
    static {
        log = LogFactory.getLog((Class)ConnectionPool.class);
    }
}
