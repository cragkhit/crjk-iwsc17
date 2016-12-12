// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.util.Properties;

public interface PoolConfiguration
{
    public static final String PKG_PREFIX = "org.apache.tomcat.jdbc.pool.interceptor.";
    
    void setAbandonWhenPercentageFull(int p0);
    
    int getAbandonWhenPercentageFull();
    
    boolean isFairQueue();
    
    void setFairQueue(boolean p0);
    
    boolean isAccessToUnderlyingConnectionAllowed();
    
    void setAccessToUnderlyingConnectionAllowed(boolean p0);
    
    String getConnectionProperties();
    
    void setConnectionProperties(String p0);
    
    Properties getDbProperties();
    
    void setDbProperties(Properties p0);
    
    Boolean isDefaultAutoCommit();
    
    Boolean getDefaultAutoCommit();
    
    void setDefaultAutoCommit(Boolean p0);
    
    String getDefaultCatalog();
    
    void setDefaultCatalog(String p0);
    
    Boolean isDefaultReadOnly();
    
    Boolean getDefaultReadOnly();
    
    void setDefaultReadOnly(Boolean p0);
    
    int getDefaultTransactionIsolation();
    
    void setDefaultTransactionIsolation(int p0);
    
    String getDriverClassName();
    
    void setDriverClassName(String p0);
    
    int getInitialSize();
    
    void setInitialSize(int p0);
    
    boolean isLogAbandoned();
    
    void setLogAbandoned(boolean p0);
    
    int getMaxActive();
    
    void setMaxActive(int p0);
    
    int getMaxIdle();
    
    void setMaxIdle(int p0);
    
    int getMaxWait();
    
    void setMaxWait(int p0);
    
    int getMinEvictableIdleTimeMillis();
    
    void setMinEvictableIdleTimeMillis(int p0);
    
    int getMinIdle();
    
    void setMinIdle(int p0);
    
    String getName();
    
    void setName(String p0);
    
    int getNumTestsPerEvictionRun();
    
    void setNumTestsPerEvictionRun(int p0);
    
    String getPassword();
    
    void setPassword(String p0);
    
    String getPoolName();
    
    String getUsername();
    
    void setUsername(String p0);
    
    boolean isRemoveAbandoned();
    
    void setRemoveAbandoned(boolean p0);
    
    void setRemoveAbandonedTimeout(int p0);
    
    int getRemoveAbandonedTimeout();
    
    boolean isTestOnBorrow();
    
    void setTestOnBorrow(boolean p0);
    
    boolean isTestOnReturn();
    
    void setTestOnReturn(boolean p0);
    
    boolean isTestWhileIdle();
    
    void setTestWhileIdle(boolean p0);
    
    int getTimeBetweenEvictionRunsMillis();
    
    void setTimeBetweenEvictionRunsMillis(int p0);
    
    String getUrl();
    
    void setUrl(String p0);
    
    String getValidationQuery();
    
    void setValidationQuery(String p0);
    
    int getValidationQueryTimeout();
    
    void setValidationQueryTimeout(int p0);
    
    String getValidatorClassName();
    
    void setValidatorClassName(String p0);
    
    Validator getValidator();
    
    void setValidator(Validator p0);
    
    long getValidationInterval();
    
    void setValidationInterval(long p0);
    
    String getInitSQL();
    
    void setInitSQL(String p0);
    
    boolean isTestOnConnect();
    
    void setTestOnConnect(boolean p0);
    
    String getJdbcInterceptors();
    
    void setJdbcInterceptors(String p0);
    
    PoolProperties.InterceptorDefinition[] getJdbcInterceptorsAsArray();
    
    boolean isJmxEnabled();
    
    void setJmxEnabled(boolean p0);
    
    boolean isPoolSweeperEnabled();
    
    boolean isUseEquals();
    
    void setUseEquals(boolean p0);
    
    long getMaxAge();
    
    void setMaxAge(long p0);
    
    boolean getUseLock();
    
    void setUseLock(boolean p0);
    
    void setSuspectTimeout(int p0);
    
    int getSuspectTimeout();
    
    void setDataSource(Object p0);
    
    Object getDataSource();
    
    void setDataSourceJNDI(String p0);
    
    String getDataSourceJNDI();
    
    boolean isAlternateUsernameAllowed();
    
    void setAlternateUsernameAllowed(boolean p0);
    
    void setCommitOnReturn(boolean p0);
    
    boolean getCommitOnReturn();
    
    void setRollbackOnReturn(boolean p0);
    
    boolean getRollbackOnReturn();
    
    void setUseDisposableConnectionFacade(boolean p0);
    
    boolean getUseDisposableConnectionFacade();
    
    void setLogValidationErrors(boolean p0);
    
    boolean getLogValidationErrors();
    
    boolean getPropagateInterruptState();
    
    void setPropagateInterruptState(boolean p0);
    
    void setIgnoreExceptionOnPreLoad(boolean p0);
    
    boolean isIgnoreExceptionOnPreLoad();
}
