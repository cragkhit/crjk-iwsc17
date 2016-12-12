// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.util.HashMap;
import java.util.Map;
import org.apache.juli.logging.LogFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.juli.logging.Log;
import java.io.Serializable;

public class PoolProperties implements PoolConfiguration, Cloneable, Serializable
{
    private static final long serialVersionUID = -8519283440854213745L;
    private static final Log log;
    public static final int DEFAULT_MAX_ACTIVE = 100;
    protected static final AtomicInteger poolCounter;
    private volatile Properties dbProperties;
    private volatile String url;
    private volatile String driverClassName;
    private volatile Boolean defaultAutoCommit;
    private volatile Boolean defaultReadOnly;
    private volatile int defaultTransactionIsolation;
    private volatile String defaultCatalog;
    private volatile String connectionProperties;
    private volatile int initialSize;
    private volatile int maxActive;
    private volatile int maxIdle;
    private volatile int minIdle;
    private volatile int maxWait;
    private volatile String validationQuery;
    private volatile int validationQueryTimeout;
    private volatile String validatorClassName;
    private volatile Validator validator;
    private volatile boolean testOnBorrow;
    private volatile boolean testOnReturn;
    private volatile boolean testWhileIdle;
    private volatile int timeBetweenEvictionRunsMillis;
    private volatile int numTestsPerEvictionRun;
    private volatile int minEvictableIdleTimeMillis;
    private volatile boolean accessToUnderlyingConnectionAllowed;
    private volatile boolean removeAbandoned;
    private volatile int removeAbandonedTimeout;
    private volatile boolean logAbandoned;
    private volatile String name;
    private volatile String password;
    private volatile String username;
    private volatile long validationInterval;
    private volatile boolean jmxEnabled;
    private volatile String initSQL;
    private volatile boolean testOnConnect;
    private volatile String jdbcInterceptors;
    private volatile boolean fairQueue;
    private volatile boolean useEquals;
    private volatile int abandonWhenPercentageFull;
    private volatile long maxAge;
    private volatile boolean useLock;
    private volatile InterceptorDefinition[] interceptors;
    private volatile int suspectTimeout;
    private volatile Object dataSource;
    private volatile String dataSourceJNDI;
    private volatile boolean alternateUsernameAllowed;
    private volatile boolean commitOnReturn;
    private volatile boolean rollbackOnReturn;
    private volatile boolean useDisposableConnectionFacade;
    private volatile boolean logValidationErrors;
    private volatile boolean propagateInterruptState;
    private volatile boolean ignoreExceptionOnPreLoad;
    
    public PoolProperties() {
        this.dbProperties = new Properties();
        this.url = null;
        this.driverClassName = null;
        this.defaultAutoCommit = null;
        this.defaultReadOnly = null;
        this.defaultTransactionIsolation = -1;
        this.defaultCatalog = null;
        this.initialSize = 10;
        this.maxActive = 100;
        this.maxIdle = this.maxActive;
        this.minIdle = this.initialSize;
        this.maxWait = 30000;
        this.validationQueryTimeout = -1;
        this.testOnBorrow = false;
        this.testOnReturn = false;
        this.testWhileIdle = false;
        this.timeBetweenEvictionRunsMillis = 5000;
        this.minEvictableIdleTimeMillis = 60000;
        this.accessToUnderlyingConnectionAllowed = true;
        this.removeAbandoned = false;
        this.removeAbandonedTimeout = 60;
        this.logAbandoned = false;
        this.name = "Tomcat Connection Pool[" + PoolProperties.poolCounter.addAndGet(1) + "-" + System.identityHashCode(PoolProperties.class) + "]";
        this.validationInterval = 3000L;
        this.jmxEnabled = true;
        this.testOnConnect = false;
        this.jdbcInterceptors = null;
        this.fairQueue = true;
        this.useEquals = true;
        this.abandonWhenPercentageFull = 0;
        this.maxAge = 0L;
        this.useLock = false;
        this.interceptors = null;
        this.suspectTimeout = 0;
        this.dataSource = null;
        this.dataSourceJNDI = null;
        this.alternateUsernameAllowed = false;
        this.commitOnReturn = false;
        this.rollbackOnReturn = false;
        this.useDisposableConnectionFacade = true;
        this.logValidationErrors = false;
        this.propagateInterruptState = false;
        this.ignoreExceptionOnPreLoad = false;
    }
    
    @Override
    public void setAbandonWhenPercentageFull(final int percentage) {
        if (percentage < 0) {
            this.abandonWhenPercentageFull = 0;
        }
        else if (percentage > 100) {
            this.abandonWhenPercentageFull = 100;
        }
        else {
            this.abandonWhenPercentageFull = percentage;
        }
    }
    
    @Override
    public int getAbandonWhenPercentageFull() {
        return this.abandonWhenPercentageFull;
    }
    
    @Override
    public boolean isFairQueue() {
        return this.fairQueue;
    }
    
    @Override
    public void setFairQueue(final boolean fairQueue) {
        this.fairQueue = fairQueue;
    }
    
    @Override
    public boolean isAccessToUnderlyingConnectionAllowed() {
        return this.accessToUnderlyingConnectionAllowed;
    }
    
    @Override
    public String getConnectionProperties() {
        return this.connectionProperties;
    }
    
    @Override
    public Properties getDbProperties() {
        return this.dbProperties;
    }
    
    @Override
    public Boolean isDefaultAutoCommit() {
        return this.defaultAutoCommit;
    }
    
    @Override
    public String getDefaultCatalog() {
        return this.defaultCatalog;
    }
    
    @Override
    public Boolean isDefaultReadOnly() {
        return this.defaultReadOnly;
    }
    
    @Override
    public int getDefaultTransactionIsolation() {
        return this.defaultTransactionIsolation;
    }
    
    @Override
    public String getDriverClassName() {
        return this.driverClassName;
    }
    
    @Override
    public int getInitialSize() {
        return this.initialSize;
    }
    
    @Override
    public boolean isLogAbandoned() {
        return this.logAbandoned;
    }
    
    @Override
    public int getMaxActive() {
        return this.maxActive;
    }
    
    @Override
    public int getMaxIdle() {
        return this.maxIdle;
    }
    
    @Override
    public int getMaxWait() {
        return this.maxWait;
    }
    
    @Override
    public int getMinEvictableIdleTimeMillis() {
        return this.minEvictableIdleTimeMillis;
    }
    
    @Override
    public int getMinIdle() {
        return this.minIdle;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public int getNumTestsPerEvictionRun() {
        return this.numTestsPerEvictionRun;
    }
    
    @Override
    public String getPassword() {
        return this.password;
    }
    
    @Override
    public String getPoolName() {
        return this.getName();
    }
    
    @Override
    public boolean isRemoveAbandoned() {
        return this.removeAbandoned;
    }
    
    @Override
    public int getRemoveAbandonedTimeout() {
        return this.removeAbandonedTimeout;
    }
    
    @Override
    public boolean isTestOnBorrow() {
        return this.testOnBorrow;
    }
    
    @Override
    public boolean isTestOnReturn() {
        return this.testOnReturn;
    }
    
    @Override
    public boolean isTestWhileIdle() {
        return this.testWhileIdle;
    }
    
    @Override
    public int getTimeBetweenEvictionRunsMillis() {
        return this.timeBetweenEvictionRunsMillis;
    }
    
    @Override
    public String getUrl() {
        return this.url;
    }
    
    @Override
    public String getUsername() {
        return this.username;
    }
    
    @Override
    public String getValidationQuery() {
        return this.validationQuery;
    }
    
    @Override
    public int getValidationQueryTimeout() {
        return this.validationQueryTimeout;
    }
    
    @Override
    public void setValidationQueryTimeout(final int validationQueryTimeout) {
        this.validationQueryTimeout = validationQueryTimeout;
    }
    
    @Override
    public String getValidatorClassName() {
        return this.validatorClassName;
    }
    
    @Override
    public Validator getValidator() {
        return this.validator;
    }
    
    @Override
    public void setValidator(final Validator validator) {
        this.validator = validator;
        if (validator != null) {
            this.validatorClassName = validator.getClass().getName();
        }
        else {
            this.validatorClassName = null;
        }
    }
    
    @Override
    public long getValidationInterval() {
        return this.validationInterval;
    }
    
    @Override
    public String getInitSQL() {
        return this.initSQL;
    }
    
    @Override
    public boolean isTestOnConnect() {
        return this.testOnConnect;
    }
    
    @Override
    public String getJdbcInterceptors() {
        return this.jdbcInterceptors;
    }
    
    @Override
    public InterceptorDefinition[] getJdbcInterceptorsAsArray() {
        if (this.interceptors == null) {
            if (this.jdbcInterceptors == null) {
                this.interceptors = new InterceptorDefinition[0];
            }
            else {
                final String[] interceptorValues = this.jdbcInterceptors.split(";");
                final InterceptorDefinition[] definitions = new InterceptorDefinition[interceptorValues.length + 1];
                definitions[0] = new InterceptorDefinition(TrapException.class);
                for (int i = 0; i < interceptorValues.length; ++i) {
                    final int propIndex = interceptorValues[i].indexOf(40);
                    final int endIndex = interceptorValues[i].indexOf(41);
                    if (propIndex < 0 || endIndex < 0 || endIndex <= propIndex) {
                        definitions[i + 1] = new InterceptorDefinition(interceptorValues[i].trim());
                    }
                    else {
                        final String name = interceptorValues[i].substring(0, propIndex).trim();
                        definitions[i + 1] = new InterceptorDefinition(name);
                        final String propsAsString = interceptorValues[i].substring(propIndex + 1, endIndex);
                        final String[] props = propsAsString.split(",");
                        for (int j = 0; j < props.length; ++j) {
                            final int pidx = props[j].indexOf(61);
                            final String propName = props[j].substring(0, pidx).trim();
                            final String propValue = props[j].substring(pidx + 1).trim();
                            definitions[i + 1].addProperty(new InterceptorProperty(propName, propValue));
                        }
                    }
                }
                this.interceptors = definitions;
            }
        }
        return this.interceptors;
    }
    
    @Override
    public void setAccessToUnderlyingConnectionAllowed(final boolean accessToUnderlyingConnectionAllowed) {
    }
    
    @Override
    public void setConnectionProperties(final String connectionProperties) {
        getProperties(this.connectionProperties = connectionProperties, this.getDbProperties());
    }
    
    @Override
    public void setDbProperties(final Properties dbProperties) {
        this.dbProperties = dbProperties;
    }
    
    @Override
    public void setDefaultAutoCommit(final Boolean defaultAutoCommit) {
        this.defaultAutoCommit = defaultAutoCommit;
    }
    
    @Override
    public void setDefaultCatalog(final String defaultCatalog) {
        this.defaultCatalog = defaultCatalog;
    }
    
    @Override
    public void setDefaultReadOnly(final Boolean defaultReadOnly) {
        this.defaultReadOnly = defaultReadOnly;
    }
    
    @Override
    public void setDefaultTransactionIsolation(final int defaultTransactionIsolation) {
        this.defaultTransactionIsolation = defaultTransactionIsolation;
    }
    
    @Override
    public void setDriverClassName(final String driverClassName) {
        this.driverClassName = driverClassName;
    }
    
    @Override
    public void setInitialSize(final int initialSize) {
        this.initialSize = initialSize;
    }
    
    @Override
    public void setLogAbandoned(final boolean logAbandoned) {
        this.logAbandoned = logAbandoned;
    }
    
    @Override
    public void setMaxActive(final int maxActive) {
        this.maxActive = maxActive;
    }
    
    @Override
    public void setMaxIdle(final int maxIdle) {
        this.maxIdle = maxIdle;
    }
    
    @Override
    public void setMaxWait(final int maxWait) {
        this.maxWait = maxWait;
    }
    
    @Override
    public void setMinEvictableIdleTimeMillis(final int minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }
    
    @Override
    public void setMinIdle(final int minIdle) {
        this.minIdle = minIdle;
    }
    
    @Override
    public void setName(final String name) {
        this.name = name;
    }
    
    @Override
    public void setNumTestsPerEvictionRun(final int numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }
    
    @Override
    public void setPassword(final String password) {
        this.password = password;
    }
    
    @Override
    public void setRemoveAbandoned(final boolean removeAbandoned) {
        this.removeAbandoned = removeAbandoned;
    }
    
    @Override
    public void setRemoveAbandonedTimeout(final int removeAbandonedTimeout) {
        this.removeAbandonedTimeout = removeAbandonedTimeout;
    }
    
    @Override
    public void setTestOnBorrow(final boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }
    
    @Override
    public void setTestWhileIdle(final boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }
    
    @Override
    public void setTestOnReturn(final boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }
    
    @Override
    public void setTimeBetweenEvictionRunsMillis(final int timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }
    
    @Override
    public void setUrl(final String url) {
        this.url = url;
    }
    
    @Override
    public void setUsername(final String username) {
        this.username = username;
    }
    
    @Override
    public void setValidationInterval(final long validationInterval) {
        this.validationInterval = validationInterval;
    }
    
    @Override
    public void setValidationQuery(final String validationQuery) {
        this.validationQuery = validationQuery;
    }
    
    @Override
    public void setValidatorClassName(final String className) {
        this.validatorClassName = className;
        this.validator = null;
        if (className == null) {
            return;
        }
        try {
            final Class<Validator> validatorClass = (Class<Validator>)ClassLoaderUtil.loadClass(className, PoolProperties.class.getClassLoader(), Thread.currentThread().getContextClassLoader());
            this.validator = validatorClass.newInstance();
        }
        catch (ClassNotFoundException e) {
            PoolProperties.log.warn((Object)("The class " + className + " cannot be found."), (Throwable)e);
        }
        catch (ClassCastException e2) {
            PoolProperties.log.warn((Object)("The class " + className + " does not implement the Validator interface."), (Throwable)e2);
        }
        catch (InstantiationException e3) {
            PoolProperties.log.warn((Object)("An object of class " + className + " cannot be instantiated. Make sure that it includes an implicit or explicit no-arg constructor."), (Throwable)e3);
        }
        catch (IllegalAccessException e4) {
            PoolProperties.log.warn((Object)("The class " + className + " or its no-arg constructor are inaccessible."), (Throwable)e4);
        }
    }
    
    @Override
    public void setInitSQL(final String initSQL) {
        this.initSQL = ((initSQL != null && initSQL.trim().length() > 0) ? initSQL : null);
    }
    
    @Override
    public void setTestOnConnect(final boolean testOnConnect) {
        this.testOnConnect = testOnConnect;
    }
    
    @Override
    public void setJdbcInterceptors(final String jdbcInterceptors) {
        this.jdbcInterceptors = jdbcInterceptors;
        this.interceptors = null;
    }
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("ConnectionPool[");
        try {
            final String[] all_PROPERTIES;
            final String[] fields = all_PROPERTIES = DataSourceFactory.ALL_PROPERTIES;
            for (final String field : all_PROPERTIES) {
                final String[] prefix = { "get", "is" };
                int j = 0;
                while (j < prefix.length) {
                    final String name = prefix[j] + field.substring(0, 1).toUpperCase(Locale.ENGLISH) + field.substring(1);
                    Method m = null;
                    Label_0134: {
                        Label_0193: {
                            try {
                                m = this.getClass().getMethod(name, (Class<?>[])new Class[0]);
                            }
                            catch (NoSuchMethodException nm) {
                                break Label_0193;
                            }
                            break Label_0134;
                        }
                        ++j;
                        continue;
                    }
                    buf.append(field);
                    buf.append("=");
                    if ("password".equals(field)) {
                        buf.append("********");
                    }
                    else {
                        buf.append(m.invoke(this, new Object[0]));
                    }
                    buf.append("; ");
                    break;
                }
            }
        }
        catch (Exception x) {
            PoolProperties.log.debug((Object)"toString() call failed", (Throwable)x);
        }
        return buf.toString();
    }
    
    public static int getPoolCounter() {
        return PoolProperties.poolCounter.get();
    }
    
    @Override
    public boolean isJmxEnabled() {
        return this.jmxEnabled;
    }
    
    @Override
    public void setJmxEnabled(final boolean jmxEnabled) {
        this.jmxEnabled = jmxEnabled;
    }
    
    @Override
    public Boolean getDefaultAutoCommit() {
        return this.defaultAutoCommit;
    }
    
    @Override
    public Boolean getDefaultReadOnly() {
        return this.defaultReadOnly;
    }
    
    @Override
    public int getSuspectTimeout() {
        return this.suspectTimeout;
    }
    
    @Override
    public void setSuspectTimeout(final int seconds) {
        this.suspectTimeout = seconds;
    }
    
    @Override
    public boolean isPoolSweeperEnabled() {
        final boolean timer = this.getTimeBetweenEvictionRunsMillis() > 0;
        boolean result = timer && this.isRemoveAbandoned() && this.getRemoveAbandonedTimeout() > 0;
        result = (result || (timer && this.getSuspectTimeout() > 0));
        result = (result || (timer && this.isTestWhileIdle() && this.getValidationQuery() != null));
        result = (result || (timer && this.getMinEvictableIdleTimeMillis() > 0));
        return result;
    }
    
    @Override
    public boolean isUseEquals() {
        return this.useEquals;
    }
    
    @Override
    public void setUseEquals(final boolean useEquals) {
        this.useEquals = useEquals;
    }
    
    @Override
    public long getMaxAge() {
        return this.maxAge;
    }
    
    @Override
    public void setMaxAge(final long maxAge) {
        this.maxAge = maxAge;
    }
    
    @Override
    public boolean getUseLock() {
        return this.useLock;
    }
    
    @Override
    public void setUseLock(final boolean useLock) {
        this.useLock = useLock;
    }
    
    @Override
    public void setDataSource(final Object ds) {
        if (ds instanceof DataSourceProxy) {
            throw new IllegalArgumentException("Layered pools are not allowed.");
        }
        this.dataSource = ds;
    }
    
    @Override
    public Object getDataSource() {
        return this.dataSource;
    }
    
    @Override
    public void setDataSourceJNDI(final String jndiDS) {
        this.dataSourceJNDI = jndiDS;
    }
    
    @Override
    public String getDataSourceJNDI() {
        return this.dataSourceJNDI;
    }
    
    public static Properties getProperties(final String propText, Properties props) {
        if (props == null) {
            props = new Properties();
        }
        if (propText != null) {
            try {
                props.load(new ByteArrayInputStream(propText.replace(';', '\n').getBytes()));
            }
            catch (IOException x) {
                throw new RuntimeException(x);
            }
        }
        return props;
    }
    
    @Override
    public boolean isAlternateUsernameAllowed() {
        return this.alternateUsernameAllowed;
    }
    
    @Override
    public void setAlternateUsernameAllowed(final boolean alternateUsernameAllowed) {
        this.alternateUsernameAllowed = alternateUsernameAllowed;
    }
    
    @Override
    public void setCommitOnReturn(final boolean commitOnReturn) {
        this.commitOnReturn = commitOnReturn;
    }
    
    @Override
    public boolean getCommitOnReturn() {
        return this.commitOnReturn;
    }
    
    @Override
    public void setRollbackOnReturn(final boolean rollbackOnReturn) {
        this.rollbackOnReturn = rollbackOnReturn;
    }
    
    @Override
    public boolean getRollbackOnReturn() {
        return this.rollbackOnReturn;
    }
    
    @Override
    public void setUseDisposableConnectionFacade(final boolean useDisposableConnectionFacade) {
        this.useDisposableConnectionFacade = useDisposableConnectionFacade;
    }
    
    @Override
    public boolean getUseDisposableConnectionFacade() {
        return this.useDisposableConnectionFacade;
    }
    
    @Override
    public void setLogValidationErrors(final boolean logValidationErrors) {
        this.logValidationErrors = logValidationErrors;
    }
    
    @Override
    public boolean getLogValidationErrors() {
        return this.logValidationErrors;
    }
    
    @Override
    public boolean getPropagateInterruptState() {
        return this.propagateInterruptState;
    }
    
    @Override
    public void setPropagateInterruptState(final boolean propagateInterruptState) {
        this.propagateInterruptState = propagateInterruptState;
    }
    
    @Override
    public boolean isIgnoreExceptionOnPreLoad() {
        return this.ignoreExceptionOnPreLoad;
    }
    
    @Override
    public void setIgnoreExceptionOnPreLoad(final boolean ignoreExceptionOnPreLoad) {
        this.ignoreExceptionOnPreLoad = ignoreExceptionOnPreLoad;
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    static {
        log = LogFactory.getLog((Class)PoolProperties.class);
        poolCounter = new AtomicInteger(0);
    }
    
    public static class InterceptorDefinition implements Serializable
    {
        private static final long serialVersionUID = 1L;
        protected String className;
        protected Map<String, InterceptorProperty> properties;
        protected volatile Class<?> clazz;
        
        public InterceptorDefinition(final String className) {
            this.properties = new HashMap<String, InterceptorProperty>();
            this.clazz = null;
            this.className = className;
        }
        
        public InterceptorDefinition(final Class<?> cl) {
            this(cl.getName());
            this.clazz = cl;
        }
        
        public String getClassName() {
            return this.className;
        }
        
        public void addProperty(final String name, final String value) {
            final InterceptorProperty p = new InterceptorProperty(name, value);
            this.addProperty(p);
        }
        
        public void addProperty(final InterceptorProperty p) {
            this.properties.put(p.getName(), p);
        }
        
        public Map<String, InterceptorProperty> getProperties() {
            return this.properties;
        }
        
        public Class<? extends JdbcInterceptor> getInterceptorClass() throws ClassNotFoundException {
            if (this.clazz == null) {
                if (this.getClassName().indexOf(46) < 0) {
                    if (PoolProperties.log.isDebugEnabled()) {
                        PoolProperties.log.debug((Object)("Loading interceptor class:org.apache.tomcat.jdbc.pool.interceptor." + this.getClassName()));
                    }
                    this.clazz = ClassLoaderUtil.loadClass("org.apache.tomcat.jdbc.pool.interceptor." + this.getClassName(), PoolProperties.class.getClassLoader(), Thread.currentThread().getContextClassLoader());
                }
                else {
                    if (PoolProperties.log.isDebugEnabled()) {
                        PoolProperties.log.debug((Object)("Loading interceptor class:" + this.getClassName()));
                    }
                    this.clazz = ClassLoaderUtil.loadClass(this.getClassName(), PoolProperties.class.getClassLoader(), Thread.currentThread().getContextClassLoader());
                }
            }
            return (Class<? extends JdbcInterceptor>)this.clazz;
        }
    }
    
    public static class InterceptorProperty implements Serializable
    {
        private static final long serialVersionUID = 1L;
        String name;
        String value;
        
        public InterceptorProperty(final String name, final String value) {
            assert name != null;
            this.name = name;
            this.value = value;
        }
        
        public String getName() {
            return this.name;
        }
        
        public String getValue() {
            return this.value;
        }
        
        public boolean getValueAsBoolean(final boolean def) {
            if (this.value == null) {
                return def;
            }
            return "true".equals(this.value) || (!"false".equals(this.value) && def);
        }
        
        public int getValueAsInt(final int def) {
            if (this.value == null) {
                return def;
            }
            try {
                final int v = Integer.parseInt(this.value);
                return v;
            }
            catch (NumberFormatException nfe) {
                return def;
            }
        }
        
        public long getValueAsLong(final long def) {
            if (this.value == null) {
                return def;
            }
            try {
                return Long.parseLong(this.value);
            }
            catch (NumberFormatException nfe) {
                return def;
            }
        }
        
        public byte getValueAsByte(final byte def) {
            if (this.value == null) {
                return def;
            }
            try {
                return Byte.parseByte(this.value);
            }
            catch (NumberFormatException nfe) {
                return def;
            }
        }
        
        public short getValueAsShort(final short def) {
            if (this.value == null) {
                return def;
            }
            try {
                return Short.parseShort(this.value);
            }
            catch (NumberFormatException nfe) {
                return def;
            }
        }
        
        public float getValueAsFloat(final float def) {
            if (this.value == null) {
                return def;
            }
            try {
                return Float.parseFloat(this.value);
            }
            catch (NumberFormatException nfe) {
                return def;
            }
        }
        
        public double getValueAsDouble(final double def) {
            if (this.value == null) {
                return def;
            }
            try {
                return Double.parseDouble(this.value);
            }
            catch (NumberFormatException nfe) {
                return def;
            }
        }
        
        public char getValueAschar(final char def) {
            if (this.value == null) {
                return def;
            }
            try {
                return this.value.charAt(0);
            }
            catch (StringIndexOutOfBoundsException nfe) {
                return def;
            }
        }
        
        @Override
        public int hashCode() {
            return this.name.hashCode();
        }
        
        @Override
        public boolean equals(final Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof InterceptorProperty) {
                final InterceptorProperty other = (InterceptorProperty)o;
                return other.name.equals(this.name);
            }
            return false;
        }
    }
}
