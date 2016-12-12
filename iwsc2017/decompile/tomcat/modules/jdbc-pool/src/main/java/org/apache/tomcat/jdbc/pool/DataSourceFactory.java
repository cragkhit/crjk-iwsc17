// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import org.apache.juli.logging.LogFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.management.ObjectName;
import javax.naming.RefAddr;
import java.util.Properties;
import javax.naming.Reference;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import org.apache.juli.logging.Log;
import javax.naming.spi.ObjectFactory;

public class DataSourceFactory implements ObjectFactory
{
    private static final Log log;
    protected static final String PROP_DEFAULTAUTOCOMMIT = "defaultAutoCommit";
    protected static final String PROP_DEFAULTREADONLY = "defaultReadOnly";
    protected static final String PROP_DEFAULTTRANSACTIONISOLATION = "defaultTransactionIsolation";
    protected static final String PROP_DEFAULTCATALOG = "defaultCatalog";
    protected static final String PROP_DRIVERCLASSNAME = "driverClassName";
    protected static final String PROP_PASSWORD = "password";
    protected static final String PROP_URL = "url";
    protected static final String PROP_USERNAME = "username";
    protected static final String PROP_MAXACTIVE = "maxActive";
    protected static final String PROP_MAXIDLE = "maxIdle";
    protected static final String PROP_MINIDLE = "minIdle";
    protected static final String PROP_INITIALSIZE = "initialSize";
    protected static final String PROP_MAXWAIT = "maxWait";
    protected static final String PROP_MAXAGE = "maxAge";
    protected static final String PROP_TESTONBORROW = "testOnBorrow";
    protected static final String PROP_TESTONRETURN = "testOnReturn";
    protected static final String PROP_TESTWHILEIDLE = "testWhileIdle";
    protected static final String PROP_TESTONCONNECT = "testOnConnect";
    protected static final String PROP_VALIDATIONQUERY = "validationQuery";
    protected static final String PROP_VALIDATIONQUERY_TIMEOUT = "validationQueryTimeout";
    protected static final String PROP_VALIDATOR_CLASS_NAME = "validatorClassName";
    protected static final String PROP_NUMTESTSPEREVICTIONRUN = "numTestsPerEvictionRun";
    protected static final String PROP_TIMEBETWEENEVICTIONRUNSMILLIS = "timeBetweenEvictionRunsMillis";
    protected static final String PROP_MINEVICTABLEIDLETIMEMILLIS = "minEvictableIdleTimeMillis";
    protected static final String PROP_ACCESSTOUNDERLYINGCONNECTIONALLOWED = "accessToUnderlyingConnectionAllowed";
    protected static final String PROP_REMOVEABANDONED = "removeAbandoned";
    protected static final String PROP_REMOVEABANDONEDTIMEOUT = "removeAbandonedTimeout";
    protected static final String PROP_LOGABANDONED = "logAbandoned";
    protected static final String PROP_ABANDONWHENPERCENTAGEFULL = "abandonWhenPercentageFull";
    protected static final String PROP_POOLPREPAREDSTATEMENTS = "poolPreparedStatements";
    protected static final String PROP_MAXOPENPREPAREDSTATEMENTS = "maxOpenPreparedStatements";
    protected static final String PROP_CONNECTIONPROPERTIES = "connectionProperties";
    protected static final String PROP_INITSQL = "initSQL";
    protected static final String PROP_INTERCEPTORS = "jdbcInterceptors";
    protected static final String PROP_VALIDATIONINTERVAL = "validationInterval";
    protected static final String PROP_JMX_ENABLED = "jmxEnabled";
    protected static final String PROP_FAIR_QUEUE = "fairQueue";
    protected static final String PROP_USE_EQUALS = "useEquals";
    protected static final String PROP_USE_CON_LOCK = "useLock";
    protected static final String PROP_DATASOURCE = "dataSource";
    protected static final String PROP_DATASOURCE_JNDI = "dataSourceJNDI";
    protected static final String PROP_SUSPECT_TIMEOUT = "suspectTimeout";
    protected static final String PROP_ALTERNATE_USERNAME_ALLOWED = "alternateUsernameAllowed";
    protected static final String PROP_COMMITONRETURN = "commitOnReturn";
    protected static final String PROP_ROLLBACKONRETURN = "rollbackOnReturn";
    protected static final String PROP_USEDISPOSABLECONNECTIONFACADE = "useDisposableConnectionFacade";
    protected static final String PROP_LOGVALIDATIONERRORS = "logValidationErrors";
    protected static final String PROP_PROPAGATEINTERRUPTSTATE = "propagateInterruptState";
    protected static final String PROP_IGNOREEXCEPTIONONPRELOAD = "ignoreExceptionOnPreLoad";
    public static final int UNKNOWN_TRANSACTIONISOLATION = -1;
    public static final String OBJECT_NAME = "object_name";
    protected static final String[] ALL_PROPERTIES;
    
    @Override
    public Object getObjectInstance(final Object obj, final Name name, final Context nameCtx, final Hashtable<?, ?> environment) throws Exception {
        if (obj == null || !(obj instanceof Reference)) {
            return null;
        }
        final Reference ref = (Reference)obj;
        boolean XA = false;
        boolean ok = false;
        if ("javax.sql.DataSource".equals(ref.getClassName())) {
            ok = true;
        }
        if ("javax.sql.XADataSource".equals(ref.getClassName())) {
            ok = true;
            XA = true;
        }
        if (DataSource.class.getName().equals(ref.getClassName())) {
            ok = true;
        }
        if (!ok) {
            DataSourceFactory.log.warn((Object)(ref.getClassName() + " is not a valid class name/type for this JNDI factory."));
            return null;
        }
        final Properties properties = new Properties();
        for (int i = 0; i < DataSourceFactory.ALL_PROPERTIES.length; ++i) {
            final String propertyName = DataSourceFactory.ALL_PROPERTIES[i];
            final RefAddr ra = ref.get(propertyName);
            if (ra != null) {
                final String propertyValue = ra.getContent().toString();
                properties.setProperty(propertyName, propertyValue);
            }
        }
        return this.createDataSource(properties, nameCtx, XA);
    }
    
    public static PoolConfiguration parsePoolProperties(final Properties properties) {
        final PoolConfiguration poolProperties = new PoolProperties();
        String value = null;
        value = properties.getProperty("defaultAutoCommit");
        if (value != null) {
            poolProperties.setDefaultAutoCommit(Boolean.valueOf(value));
        }
        value = properties.getProperty("defaultReadOnly");
        if (value != null) {
            poolProperties.setDefaultReadOnly(Boolean.valueOf(value));
        }
        value = properties.getProperty("defaultTransactionIsolation");
        if (value != null) {
            int level = -1;
            if ("NONE".equalsIgnoreCase(value)) {
                level = 0;
            }
            else if ("READ_COMMITTED".equalsIgnoreCase(value)) {
                level = 2;
            }
            else if ("READ_UNCOMMITTED".equalsIgnoreCase(value)) {
                level = 1;
            }
            else if ("REPEATABLE_READ".equalsIgnoreCase(value)) {
                level = 4;
            }
            else if ("SERIALIZABLE".equalsIgnoreCase(value)) {
                level = 8;
            }
            else {
                try {
                    level = Integer.parseInt(value);
                }
                catch (NumberFormatException e) {
                    System.err.println("Could not parse defaultTransactionIsolation: " + value);
                    System.err.println("WARNING: defaultTransactionIsolation not set");
                    System.err.println("using default value of database driver");
                    level = -1;
                }
            }
            poolProperties.setDefaultTransactionIsolation(level);
        }
        value = properties.getProperty("defaultCatalog");
        if (value != null) {
            poolProperties.setDefaultCatalog(value);
        }
        value = properties.getProperty("driverClassName");
        if (value != null) {
            poolProperties.setDriverClassName(value);
        }
        value = properties.getProperty("maxActive");
        if (value != null) {
            poolProperties.setMaxActive(Integer.parseInt(value));
        }
        value = properties.getProperty("maxIdle");
        if (value != null) {
            poolProperties.setMaxIdle(Integer.parseInt(value));
        }
        value = properties.getProperty("minIdle");
        if (value != null) {
            poolProperties.setMinIdle(Integer.parseInt(value));
        }
        value = properties.getProperty("initialSize");
        if (value != null) {
            poolProperties.setInitialSize(Integer.parseInt(value));
        }
        value = properties.getProperty("maxWait");
        if (value != null) {
            poolProperties.setMaxWait(Integer.parseInt(value));
        }
        value = properties.getProperty("testOnBorrow");
        if (value != null) {
            poolProperties.setTestOnBorrow(Boolean.parseBoolean(value));
        }
        value = properties.getProperty("testOnReturn");
        if (value != null) {
            poolProperties.setTestOnReturn(Boolean.parseBoolean(value));
        }
        value = properties.getProperty("testOnConnect");
        if (value != null) {
            poolProperties.setTestOnConnect(Boolean.parseBoolean(value));
        }
        value = properties.getProperty("timeBetweenEvictionRunsMillis");
        if (value != null) {
            poolProperties.setTimeBetweenEvictionRunsMillis(Integer.parseInt(value));
        }
        value = properties.getProperty("numTestsPerEvictionRun");
        if (value != null) {
            poolProperties.setNumTestsPerEvictionRun(Integer.parseInt(value));
        }
        value = properties.getProperty("minEvictableIdleTimeMillis");
        if (value != null) {
            poolProperties.setMinEvictableIdleTimeMillis(Integer.parseInt(value));
        }
        value = properties.getProperty("testWhileIdle");
        if (value != null) {
            poolProperties.setTestWhileIdle(Boolean.parseBoolean(value));
        }
        value = properties.getProperty("password");
        if (value != null) {
            poolProperties.setPassword(value);
        }
        value = properties.getProperty("url");
        if (value != null) {
            poolProperties.setUrl(value);
        }
        value = properties.getProperty("username");
        if (value != null) {
            poolProperties.setUsername(value);
        }
        value = properties.getProperty("validationQuery");
        if (value != null) {
            poolProperties.setValidationQuery(value);
        }
        value = properties.getProperty("validationQueryTimeout");
        if (value != null) {
            poolProperties.setValidationQueryTimeout(Integer.parseInt(value));
        }
        value = properties.getProperty("validatorClassName");
        if (value != null) {
            poolProperties.setValidatorClassName(value);
        }
        value = properties.getProperty("validationInterval");
        if (value != null) {
            poolProperties.setValidationInterval(Long.parseLong(value));
        }
        value = properties.getProperty("accessToUnderlyingConnectionAllowed");
        if (value != null) {
            poolProperties.setAccessToUnderlyingConnectionAllowed(Boolean.parseBoolean(value));
        }
        value = properties.getProperty("removeAbandoned");
        if (value != null) {
            poolProperties.setRemoveAbandoned(Boolean.parseBoolean(value));
        }
        value = properties.getProperty("removeAbandonedTimeout");
        if (value != null) {
            poolProperties.setRemoveAbandonedTimeout(Integer.parseInt(value));
        }
        value = properties.getProperty("logAbandoned");
        if (value != null) {
            poolProperties.setLogAbandoned(Boolean.parseBoolean(value));
        }
        value = properties.getProperty("poolPreparedStatements");
        if (value != null) {
            DataSourceFactory.log.warn((Object)"poolPreparedStatements is not a valid setting, it will have no effect.");
        }
        value = properties.getProperty("maxOpenPreparedStatements");
        if (value != null) {
            DataSourceFactory.log.warn((Object)"maxOpenPreparedStatements is not a valid setting, it will have no effect.");
        }
        value = properties.getProperty("connectionProperties");
        if (value != null) {
            final Properties p = getProperties(value);
            poolProperties.setDbProperties(p);
        }
        else {
            poolProperties.setDbProperties(new Properties());
        }
        if (poolProperties.getUsername() != null) {
            poolProperties.getDbProperties().setProperty("user", poolProperties.getUsername());
        }
        if (poolProperties.getPassword() != null) {
            poolProperties.getDbProperties().setProperty("password", poolProperties.getPassword());
        }
        value = properties.getProperty("initSQL");
        if (value != null) {
            poolProperties.setInitSQL(value);
        }
        value = properties.getProperty("jdbcInterceptors");
        if (value != null) {
            poolProperties.setJdbcInterceptors(value);
        }
        value = properties.getProperty("jmxEnabled");
        if (value != null) {
            poolProperties.setJmxEnabled(Boolean.parseBoolean(value));
        }
        value = properties.getProperty("fairQueue");
        if (value != null) {
            poolProperties.setFairQueue(Boolean.parseBoolean(value));
        }
        value = properties.getProperty("useEquals");
        if (value != null) {
            poolProperties.setUseEquals(Boolean.parseBoolean(value));
        }
        value = properties.getProperty("object_name");
        if (value != null) {
            poolProperties.setName(ObjectName.quote(value));
        }
        value = properties.getProperty("abandonWhenPercentageFull");
        if (value != null) {
            poolProperties.setAbandonWhenPercentageFull(Integer.parseInt(value));
        }
        value = properties.getProperty("maxAge");
        if (value != null) {
            poolProperties.setMaxAge(Long.parseLong(value));
        }
        value = properties.getProperty("useLock");
        if (value != null) {
            poolProperties.setUseLock(Boolean.parseBoolean(value));
        }
        value = properties.getProperty("dataSource");
        if (value != null) {
            throw new IllegalArgumentException("Can't set dataSource property as a string, this must be a javax.sql.DataSource object.");
        }
        value = properties.getProperty("dataSourceJNDI");
        if (value != null) {
            poolProperties.setDataSourceJNDI(value);
        }
        value = properties.getProperty("suspectTimeout");
        if (value != null) {
            poolProperties.setSuspectTimeout(Integer.parseInt(value));
        }
        value = properties.getProperty("alternateUsernameAllowed");
        if (value != null) {
            poolProperties.setAlternateUsernameAllowed(Boolean.parseBoolean(value));
        }
        value = properties.getProperty("commitOnReturn");
        if (value != null) {
            poolProperties.setCommitOnReturn(Boolean.parseBoolean(value));
        }
        value = properties.getProperty("rollbackOnReturn");
        if (value != null) {
            poolProperties.setRollbackOnReturn(Boolean.parseBoolean(value));
        }
        value = properties.getProperty("useDisposableConnectionFacade");
        if (value != null) {
            poolProperties.setUseDisposableConnectionFacade(Boolean.parseBoolean(value));
        }
        value = properties.getProperty("logValidationErrors");
        if (value != null) {
            poolProperties.setLogValidationErrors(Boolean.parseBoolean(value));
        }
        value = properties.getProperty("propagateInterruptState");
        if (value != null) {
            poolProperties.setPropagateInterruptState(Boolean.parseBoolean(value));
        }
        value = properties.getProperty("ignoreExceptionOnPreLoad");
        if (value != null) {
            poolProperties.setIgnoreExceptionOnPreLoad(Boolean.parseBoolean(value));
        }
        return poolProperties;
    }
    
    public javax.sql.DataSource createDataSource(final Properties properties) throws Exception {
        return this.createDataSource(properties, null, false);
    }
    
    public javax.sql.DataSource createDataSource(final Properties properties, final Context context, final boolean XA) throws Exception {
        final PoolConfiguration poolProperties = parsePoolProperties(properties);
        if (poolProperties.getDataSourceJNDI() != null && poolProperties.getDataSource() == null) {
            this.performJNDILookup(context, poolProperties);
        }
        final DataSource dataSource = XA ? new XADataSource(poolProperties) : new DataSource(poolProperties);
        dataSource.createPool();
        return dataSource;
    }
    
    public void performJNDILookup(Context context, final PoolConfiguration poolProperties) {
        Object jndiDS = null;
        try {
            if (context != null) {
                jndiDS = context.lookup(poolProperties.getDataSourceJNDI());
            }
            else {
                DataSourceFactory.log.warn((Object)"dataSourceJNDI property is configued, but local JNDI context is null.");
            }
        }
        catch (NamingException e) {
            DataSourceFactory.log.debug((Object)("The name \"" + poolProperties.getDataSourceJNDI() + "\" cannot be found in the local context."));
        }
        if (jndiDS == null) {
            try {
                context = new InitialContext();
                jndiDS = context.lookup(poolProperties.getDataSourceJNDI());
            }
            catch (NamingException e) {
                DataSourceFactory.log.warn((Object)("The name \"" + poolProperties.getDataSourceJNDI() + "\" cannot be found in the InitialContext."));
            }
        }
        if (jndiDS != null) {
            poolProperties.setDataSource(jndiDS);
        }
    }
    
    protected static Properties getProperties(final String propText) {
        return PoolProperties.getProperties(propText, null);
    }
    
    static {
        log = LogFactory.getLog((Class)DataSourceFactory.class);
        ALL_PROPERTIES = new String[] { "defaultAutoCommit", "defaultReadOnly", "defaultTransactionIsolation", "defaultCatalog", "driverClassName", "maxActive", "maxIdle", "minIdle", "initialSize", "maxWait", "testOnBorrow", "testOnReturn", "timeBetweenEvictionRunsMillis", "numTestsPerEvictionRun", "minEvictableIdleTimeMillis", "testWhileIdle", "testOnConnect", "password", "url", "username", "validationQuery", "validationQueryTimeout", "validatorClassName", "validationInterval", "accessToUnderlyingConnectionAllowed", "removeAbandoned", "removeAbandonedTimeout", "logAbandoned", "poolPreparedStatements", "maxOpenPreparedStatements", "connectionProperties", "initSQL", "jdbcInterceptors", "jmxEnabled", "fairQueue", "useEquals", "object_name", "abandonWhenPercentageFull", "maxAge", "useLock", "dataSource", "dataSourceJNDI", "suspectTimeout", "alternateUsernameAllowed", "commitOnReturn", "rollbackOnReturn", "useDisposableConnectionFacade", "logValidationErrors", "propagateInterruptState", "ignoreExceptionOnPreLoad" };
    }
}
