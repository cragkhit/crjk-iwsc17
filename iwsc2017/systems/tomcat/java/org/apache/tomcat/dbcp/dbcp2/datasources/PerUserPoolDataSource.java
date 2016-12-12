package org.apache.tomcat.dbcp.dbcp2.datasources;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.ConnectionPoolDataSource;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.dbcp.dbcp2.SwallowedExceptionLogger;
import org.apache.tomcat.dbcp.pool2.ObjectPool;
import org.apache.tomcat.dbcp.pool2.impl.GenericObjectPool;
public class PerUserPoolDataSource extends InstanceKeyDataSource {
    private static final long serialVersionUID = 7872747993848065028L;
    private static final Log log =
        LogFactory.getLog ( PerUserPoolDataSource.class );
    private Map<String, Boolean> perUserBlockWhenExhausted = null;
    private Map<String, String> perUserEvictionPolicyClassName = null;
    private Map<String, Boolean> perUserLifo = null;
    private Map<String, Integer> perUserMaxIdle = null;
    private Map<String, Integer> perUserMaxTotal = null;
    private Map<String, Long> perUserMaxWaitMillis = null;
    private Map<String, Long> perUserMinEvictableIdleTimeMillis = null;
    private Map<String, Integer> perUserMinIdle = null;
    private Map<String, Integer> perUserNumTestsPerEvictionRun = null;
    private Map<String, Long> perUserSoftMinEvictableIdleTimeMillis = null;
    private Map<String, Boolean> perUserTestOnCreate = null;
    private Map<String, Boolean> perUserTestOnBorrow = null;
    private Map<String, Boolean> perUserTestOnReturn = null;
    private Map<String, Boolean> perUserTestWhileIdle = null;
    private Map<String, Long> perUserTimeBetweenEvictionRunsMillis = null;
    private Map<String, Boolean> perUserDefaultAutoCommit = null;
    private Map<String, Integer> perUserDefaultTransactionIsolation = null;
    private Map<String, Boolean> perUserDefaultReadOnly = null;
    private transient Map<PoolKey, PooledConnectionManager> managers =
        new HashMap<>();
    public PerUserPoolDataSource() {
    }
    @Override
    public void close() {
        for ( final PooledConnectionManager manager : managers.values() ) {
            try {
                ( ( CPDSConnectionFactory ) manager ).getPool().close();
            } catch ( final Exception closePoolException ) {
            }
        }
        InstanceKeyDataSourceFactory.removeInstance ( getInstanceKey() );
    }
    public boolean getPerUserBlockWhenExhausted ( final String key ) {
        Boolean value = null;
        if ( perUserBlockWhenExhausted != null ) {
            value = perUserBlockWhenExhausted.get ( key );
        }
        if ( value == null ) {
            return getDefaultBlockWhenExhausted();
        }
        return value.booleanValue();
    }
    public void setPerUserBlockWhenExhausted ( final String username,
            final Boolean value ) {
        assertInitializationAllowed();
        if ( perUserBlockWhenExhausted == null ) {
            perUserBlockWhenExhausted = new HashMap<>();
        }
        perUserBlockWhenExhausted.put ( username, value );
    }
    void setPerUserBlockWhenExhausted (
        final Map<String, Boolean> userDefaultBlockWhenExhausted ) {
        assertInitializationAllowed();
        if ( perUserBlockWhenExhausted == null ) {
            perUserBlockWhenExhausted = new HashMap<>();
        } else {
            perUserBlockWhenExhausted.clear();
        }
        perUserBlockWhenExhausted.putAll ( userDefaultBlockWhenExhausted );
    }
    public String getPerUserEvictionPolicyClassName ( final String key ) {
        String value = null;
        if ( perUserEvictionPolicyClassName != null ) {
            value = perUserEvictionPolicyClassName.get ( key );
        }
        if ( value == null ) {
            return getDefaultEvictionPolicyClassName();
        }
        return value;
    }
    public void setPerUserEvictionPolicyClassName ( final String username,
            final String value ) {
        assertInitializationAllowed();
        if ( perUserEvictionPolicyClassName == null ) {
            perUserEvictionPolicyClassName = new HashMap<>();
        }
        perUserEvictionPolicyClassName.put ( username, value );
    }
    void setPerUserEvictionPolicyClassName (
        final Map<String, String> userDefaultEvictionPolicyClassName ) {
        assertInitializationAllowed();
        if ( perUserEvictionPolicyClassName == null ) {
            perUserEvictionPolicyClassName = new HashMap<>();
        } else {
            perUserEvictionPolicyClassName.clear();
        }
        perUserEvictionPolicyClassName.putAll ( userDefaultEvictionPolicyClassName );
    }
    public boolean getPerUserLifo ( final String key ) {
        Boolean value = null;
        if ( perUserLifo != null ) {
            value = perUserLifo.get ( key );
        }
        if ( value == null ) {
            return getDefaultLifo();
        }
        return value.booleanValue();
    }
    public void setPerUserLifo ( final String username, final Boolean value ) {
        assertInitializationAllowed();
        if ( perUserLifo == null ) {
            perUserLifo = new HashMap<>();
        }
        perUserLifo.put ( username, value );
    }
    void setPerUserLifo ( final Map<String, Boolean> userDefaultLifo ) {
        assertInitializationAllowed();
        if ( perUserLifo == null ) {
            perUserLifo = new HashMap<>();
        } else {
            perUserLifo.clear();
        }
        perUserLifo.putAll ( userDefaultLifo );
    }
    public int getPerUserMaxIdle ( final String key ) {
        Integer value = null;
        if ( perUserMaxIdle != null ) {
            value = perUserMaxIdle.get ( key );
        }
        if ( value == null ) {
            return getDefaultMaxIdle();
        }
        return value.intValue();
    }
    public void setPerUserMaxIdle ( final String username, final Integer value ) {
        assertInitializationAllowed();
        if ( perUserMaxIdle == null ) {
            perUserMaxIdle = new HashMap<>();
        }
        perUserMaxIdle.put ( username, value );
    }
    void setPerUserMaxIdle ( final Map<String, Integer> userDefaultMaxIdle ) {
        assertInitializationAllowed();
        if ( perUserMaxIdle == null ) {
            perUserMaxIdle = new HashMap<>();
        } else {
            perUserMaxIdle.clear();
        }
        perUserMaxIdle.putAll ( userDefaultMaxIdle );
    }
    public int getPerUserMaxTotal ( final String key ) {
        Integer value = null;
        if ( perUserMaxTotal != null ) {
            value = perUserMaxTotal.get ( key );
        }
        if ( value == null ) {
            return getDefaultMaxTotal();
        }
        return value.intValue();
    }
    public void setPerUserMaxTotal ( final String username, final Integer value ) {
        assertInitializationAllowed();
        if ( perUserMaxTotal == null ) {
            perUserMaxTotal = new HashMap<>();
        }
        perUserMaxTotal.put ( username, value );
    }
    void setPerUserMaxTotal ( final Map<String, Integer> userDefaultMaxTotal ) {
        assertInitializationAllowed();
        if ( perUserMaxTotal == null ) {
            perUserMaxTotal = new HashMap<>();
        } else {
            perUserMaxTotal.clear();
        }
        perUserMaxTotal.putAll ( userDefaultMaxTotal );
    }
    public long getPerUserMaxWaitMillis ( final String key ) {
        Long value = null;
        if ( perUserMaxWaitMillis != null ) {
            value = perUserMaxWaitMillis.get ( key );
        }
        if ( value == null ) {
            return getDefaultMaxWaitMillis();
        }
        return value.longValue();
    }
    public void setPerUserMaxWaitMillis ( final String username, final Long value ) {
        assertInitializationAllowed();
        if ( perUserMaxWaitMillis == null ) {
            perUserMaxWaitMillis = new HashMap<>();
        }
        perUserMaxWaitMillis.put ( username, value );
    }
    void setPerUserMaxWaitMillis (
        final Map<String, Long> userDefaultMaxWaitMillis ) {
        assertInitializationAllowed();
        if ( perUserMaxWaitMillis == null ) {
            perUserMaxWaitMillis = new HashMap<>();
        } else {
            perUserMaxWaitMillis.clear();
        }
        perUserMaxWaitMillis.putAll ( userDefaultMaxWaitMillis );
    }
    public long getPerUserMinEvictableIdleTimeMillis ( final String key ) {
        Long value = null;
        if ( perUserMinEvictableIdleTimeMillis != null ) {
            value = perUserMinEvictableIdleTimeMillis.get ( key );
        }
        if ( value == null ) {
            return getDefaultMinEvictableIdleTimeMillis();
        }
        return value.longValue();
    }
    public void setPerUserMinEvictableIdleTimeMillis ( final String username,
            final Long value ) {
        assertInitializationAllowed();
        if ( perUserMinEvictableIdleTimeMillis == null ) {
            perUserMinEvictableIdleTimeMillis = new HashMap<>();
        }
        perUserMinEvictableIdleTimeMillis.put ( username, value );
    }
    void setPerUserMinEvictableIdleTimeMillis (
        final Map<String, Long> userDefaultMinEvictableIdleTimeMillis ) {
        assertInitializationAllowed();
        if ( perUserMinEvictableIdleTimeMillis == null ) {
            perUserMinEvictableIdleTimeMillis = new HashMap<>();
        } else {
            perUserMinEvictableIdleTimeMillis.clear();
        }
        perUserMinEvictableIdleTimeMillis.putAll (
            userDefaultMinEvictableIdleTimeMillis );
    }
    public int getPerUserMinIdle ( final String key ) {
        Integer value = null;
        if ( perUserMinIdle != null ) {
            value = perUserMinIdle.get ( key );
        }
        if ( value == null ) {
            return getDefaultMinIdle();
        }
        return value.intValue();
    }
    public void setPerUserMinIdle ( final String username, final Integer value ) {
        assertInitializationAllowed();
        if ( perUserMinIdle == null ) {
            perUserMinIdle = new HashMap<>();
        }
        perUserMinIdle.put ( username, value );
    }
    void setPerUserMinIdle ( final Map<String, Integer> userDefaultMinIdle ) {
        assertInitializationAllowed();
        if ( perUserMinIdle == null ) {
            perUserMinIdle = new HashMap<>();
        } else {
            perUserMinIdle.clear();
        }
        perUserMinIdle.putAll ( userDefaultMinIdle );
    }
    public int getPerUserNumTestsPerEvictionRun ( final String key ) {
        Integer value = null;
        if ( perUserNumTestsPerEvictionRun != null ) {
            value = perUserNumTestsPerEvictionRun.get ( key );
        }
        if ( value == null ) {
            return getDefaultNumTestsPerEvictionRun();
        }
        return value.intValue();
    }
    public void setPerUserNumTestsPerEvictionRun ( final String username,
            final Integer value ) {
        assertInitializationAllowed();
        if ( perUserNumTestsPerEvictionRun == null ) {
            perUserNumTestsPerEvictionRun = new HashMap<>();
        }
        perUserNumTestsPerEvictionRun.put ( username, value );
    }
    void setPerUserNumTestsPerEvictionRun (
        final Map<String, Integer> userDefaultNumTestsPerEvictionRun ) {
        assertInitializationAllowed();
        if ( perUserNumTestsPerEvictionRun == null ) {
            perUserNumTestsPerEvictionRun = new HashMap<>();
        } else {
            perUserNumTestsPerEvictionRun.clear();
        }
        perUserNumTestsPerEvictionRun.putAll ( userDefaultNumTestsPerEvictionRun );
    }
    public long getPerUserSoftMinEvictableIdleTimeMillis ( final String key ) {
        Long value = null;
        if ( perUserSoftMinEvictableIdleTimeMillis != null ) {
            value = perUserSoftMinEvictableIdleTimeMillis.get ( key );
        }
        if ( value == null ) {
            return getDefaultSoftMinEvictableIdleTimeMillis();
        }
        return value.longValue();
    }
    public void setPerUserSoftMinEvictableIdleTimeMillis ( final String username,
            final Long value ) {
        assertInitializationAllowed();
        if ( perUserSoftMinEvictableIdleTimeMillis == null ) {
            perUserSoftMinEvictableIdleTimeMillis = new HashMap<>();
        }
        perUserSoftMinEvictableIdleTimeMillis.put ( username, value );
    }
    void setPerUserSoftMinEvictableIdleTimeMillis (
        final Map<String, Long> userDefaultSoftMinEvictableIdleTimeMillis ) {
        assertInitializationAllowed();
        if ( perUserSoftMinEvictableIdleTimeMillis == null ) {
            perUserSoftMinEvictableIdleTimeMillis = new HashMap<>();
        } else {
            perUserSoftMinEvictableIdleTimeMillis.clear();
        }
        perUserSoftMinEvictableIdleTimeMillis.putAll ( userDefaultSoftMinEvictableIdleTimeMillis );
    }
    public boolean getPerUserTestOnCreate ( final String key ) {
        Boolean value = null;
        if ( perUserTestOnCreate != null ) {
            value = perUserTestOnCreate.get ( key );
        }
        if ( value == null ) {
            return getDefaultTestOnCreate();
        }
        return value.booleanValue();
    }
    public void setPerUserTestOnCreate ( final String username, final Boolean value ) {
        assertInitializationAllowed();
        if ( perUserTestOnCreate == null ) {
            perUserTestOnCreate = new HashMap<>();
        }
        perUserTestOnCreate.put ( username, value );
    }
    void setPerUserTestOnCreate ( final Map<String, Boolean> userDefaultTestOnCreate ) {
        assertInitializationAllowed();
        if ( perUserTestOnCreate == null ) {
            perUserTestOnCreate = new HashMap<>();
        } else {
            perUserTestOnCreate.clear();
        }
        perUserTestOnCreate.putAll ( userDefaultTestOnCreate );
    }
    public boolean getPerUserTestOnBorrow ( final String key ) {
        Boolean value = null;
        if ( perUserTestOnBorrow != null ) {
            value = perUserTestOnBorrow.get ( key );
        }
        if ( value == null ) {
            return getDefaultTestOnBorrow();
        }
        return value.booleanValue();
    }
    public void setPerUserTestOnBorrow ( final String username, final Boolean value ) {
        assertInitializationAllowed();
        if ( perUserTestOnBorrow == null ) {
            perUserTestOnBorrow = new HashMap<>();
        }
        perUserTestOnBorrow.put ( username, value );
    }
    void setPerUserTestOnBorrow ( final Map<String, Boolean> userDefaultTestOnBorrow ) {
        assertInitializationAllowed();
        if ( perUserTestOnBorrow == null ) {
            perUserTestOnBorrow = new HashMap<>();
        } else {
            perUserTestOnBorrow.clear();
        }
        perUserTestOnBorrow.putAll ( userDefaultTestOnBorrow );
    }
    public boolean getPerUserTestOnReturn ( final String key ) {
        Boolean value = null;
        if ( perUserTestOnReturn != null ) {
            value = perUserTestOnReturn.get ( key );
        }
        if ( value == null ) {
            return getDefaultTestOnReturn();
        }
        return value.booleanValue();
    }
    public void setPerUserTestOnReturn ( final String username, final Boolean value ) {
        assertInitializationAllowed();
        if ( perUserTestOnReturn == null ) {
            perUserTestOnReturn = new HashMap<>();
        }
        perUserTestOnReturn.put ( username, value );
    }
    void setPerUserTestOnReturn (
        final Map<String, Boolean> userDefaultTestOnReturn ) {
        assertInitializationAllowed();
        if ( perUserTestOnReturn == null ) {
            perUserTestOnReturn = new HashMap<>();
        } else {
            perUserTestOnReturn.clear();
        }
        perUserTestOnReturn.putAll ( userDefaultTestOnReturn );
    }
    public boolean getPerUserTestWhileIdle ( final String key ) {
        Boolean value = null;
        if ( perUserTestWhileIdle != null ) {
            value = perUserTestWhileIdle.get ( key );
        }
        if ( value == null ) {
            return getDefaultTestWhileIdle();
        }
        return value.booleanValue();
    }
    public void setPerUserTestWhileIdle ( final String username, final Boolean value ) {
        assertInitializationAllowed();
        if ( perUserTestWhileIdle == null ) {
            perUserTestWhileIdle = new HashMap<>();
        }
        perUserTestWhileIdle.put ( username, value );
    }
    void setPerUserTestWhileIdle (
        final Map<String, Boolean> userDefaultTestWhileIdle ) {
        assertInitializationAllowed();
        if ( perUserTestWhileIdle == null ) {
            perUserTestWhileIdle = new HashMap<>();
        } else {
            perUserTestWhileIdle.clear();
        }
        perUserTestWhileIdle.putAll ( userDefaultTestWhileIdle );
    }
    public long getPerUserTimeBetweenEvictionRunsMillis ( final String key ) {
        Long value = null;
        if ( perUserTimeBetweenEvictionRunsMillis != null ) {
            value = perUserTimeBetweenEvictionRunsMillis.get ( key );
        }
        if ( value == null ) {
            return getDefaultTimeBetweenEvictionRunsMillis();
        }
        return value.longValue();
    }
    public void setPerUserTimeBetweenEvictionRunsMillis ( final String username,
            final Long value ) {
        assertInitializationAllowed();
        if ( perUserTimeBetweenEvictionRunsMillis == null ) {
            perUserTimeBetweenEvictionRunsMillis = new HashMap<>();
        }
        perUserTimeBetweenEvictionRunsMillis.put ( username, value );
    }
    void setPerUserTimeBetweenEvictionRunsMillis (
        final Map<String, Long> userDefaultTimeBetweenEvictionRunsMillis ) {
        assertInitializationAllowed();
        if ( perUserTimeBetweenEvictionRunsMillis == null ) {
            perUserTimeBetweenEvictionRunsMillis = new HashMap<>();
        } else {
            perUserTimeBetweenEvictionRunsMillis.clear();
        }
        perUserTimeBetweenEvictionRunsMillis.putAll (
            userDefaultTimeBetweenEvictionRunsMillis );
    }
    public Boolean getPerUserDefaultAutoCommit ( final String key ) {
        Boolean value = null;
        if ( perUserDefaultAutoCommit != null ) {
            value = perUserDefaultAutoCommit.get ( key );
        }
        return value;
    }
    public void setPerUserDefaultAutoCommit ( final String username, final Boolean value ) {
        assertInitializationAllowed();
        if ( perUserDefaultAutoCommit == null ) {
            perUserDefaultAutoCommit = new HashMap<>();
        }
        perUserDefaultAutoCommit.put ( username, value );
    }
    void setPerUserDefaultAutoCommit ( final Map<String, Boolean> userDefaultAutoCommit ) {
        assertInitializationAllowed();
        if ( perUserDefaultAutoCommit == null ) {
            perUserDefaultAutoCommit = new HashMap<>();
        } else {
            perUserDefaultAutoCommit.clear();
        }
        perUserDefaultAutoCommit.putAll ( userDefaultAutoCommit );
    }
    public Boolean getPerUserDefaultReadOnly ( final String key ) {
        Boolean value = null;
        if ( perUserDefaultReadOnly != null ) {
            value = perUserDefaultReadOnly.get ( key );
        }
        return value;
    }
    public void setPerUserDefaultReadOnly ( final String username, final Boolean value ) {
        assertInitializationAllowed();
        if ( perUserDefaultReadOnly == null ) {
            perUserDefaultReadOnly = new HashMap<>();
        }
        perUserDefaultReadOnly.put ( username, value );
    }
    void setPerUserDefaultReadOnly ( final Map<String, Boolean> userDefaultReadOnly ) {
        assertInitializationAllowed();
        if ( perUserDefaultReadOnly == null ) {
            perUserDefaultReadOnly = new HashMap<>();
        } else {
            perUserDefaultReadOnly.clear();
        }
        perUserDefaultReadOnly.putAll ( userDefaultReadOnly );
    }
    public Integer getPerUserDefaultTransactionIsolation ( final String key ) {
        Integer value = null;
        if ( perUserDefaultTransactionIsolation != null ) {
            value = perUserDefaultTransactionIsolation.get ( key );
        }
        return value;
    }
    public void setPerUserDefaultTransactionIsolation ( final String username,
            final Integer value ) {
        assertInitializationAllowed();
        if ( perUserDefaultTransactionIsolation == null ) {
            perUserDefaultTransactionIsolation = new HashMap<>();
        }
        perUserDefaultTransactionIsolation.put ( username, value );
    }
    void setPerUserDefaultTransactionIsolation (
        final Map<String, Integer> userDefaultTransactionIsolation ) {
        assertInitializationAllowed();
        if ( perUserDefaultTransactionIsolation == null ) {
            perUserDefaultTransactionIsolation = new HashMap<>();
        } else {
            perUserDefaultTransactionIsolation.clear();
        }
        perUserDefaultTransactionIsolation.putAll ( userDefaultTransactionIsolation );
    }
    public int getNumActive() {
        return getNumActive ( null );
    }
    public int getNumActive ( final String username ) {
        final ObjectPool<PooledConnectionAndInfo> pool =
            getPool ( getPoolKey ( username ) );
        return pool == null ? 0 : pool.getNumActive();
    }
    public int getNumIdle() {
        return getNumIdle ( null );
    }
    public int getNumIdle ( final String username ) {
        final ObjectPool<PooledConnectionAndInfo> pool =
            getPool ( getPoolKey ( username ) );
        return pool == null ? 0 : pool.getNumIdle();
    }
    @Override
    protected PooledConnectionAndInfo
    getPooledConnectionAndInfo ( final String username, final String password )
    throws SQLException {
        final PoolKey key = getPoolKey ( username );
        ObjectPool<PooledConnectionAndInfo> pool;
        PooledConnectionManager manager;
        synchronized ( this ) {
            manager = managers.get ( key );
            if ( manager == null ) {
                try {
                    registerPool ( username, password );
                    manager = managers.get ( key );
                } catch ( final NamingException e ) {
                    throw new SQLException ( "RegisterPool failed", e );
                }
            }
            pool = ( ( CPDSConnectionFactory ) manager ).getPool();
        }
        PooledConnectionAndInfo info = null;
        try {
            info = pool.borrowObject();
        } catch ( final NoSuchElementException ex ) {
            throw new SQLException (
                "Could not retrieve connection info from pool", ex );
        } catch ( final Exception e ) {
            try {
                testCPDS ( username, password );
            } catch ( final Exception ex ) {
                throw new SQLException (
                    "Could not retrieve connection info from pool", ex );
            }
            manager.closePool ( username );
            synchronized ( this ) {
                managers.remove ( key );
            }
            try {
                registerPool ( username, password );
                pool = getPool ( key );
            } catch ( final NamingException ne ) {
                throw new SQLException ( "RegisterPool failed", ne );
            }
            try {
                info = pool.borrowObject();
            } catch ( final Exception ex ) {
                throw new SQLException (
                    "Could not retrieve connection info from pool", ex );
            }
        }
        return info;
    }
    @Override
    protected void setupDefaults ( final Connection con, final String username )
    throws SQLException {
        Boolean defaultAutoCommit = isDefaultAutoCommit();
        if ( username != null ) {
            final Boolean userMax = getPerUserDefaultAutoCommit ( username );
            if ( userMax != null ) {
                defaultAutoCommit = userMax;
            }
        }
        Boolean defaultReadOnly = isDefaultReadOnly();
        if ( username != null ) {
            final Boolean userMax = getPerUserDefaultReadOnly ( username );
            if ( userMax != null ) {
                defaultReadOnly = userMax;
            }
        }
        int defaultTransactionIsolation = getDefaultTransactionIsolation();
        if ( username != null ) {
            final Integer userMax = getPerUserDefaultTransactionIsolation ( username );
            if ( userMax != null ) {
                defaultTransactionIsolation = userMax.intValue();
            }
        }
        if ( defaultAutoCommit != null &&
                con.getAutoCommit() != defaultAutoCommit.booleanValue() ) {
            con.setAutoCommit ( defaultAutoCommit.booleanValue() );
        }
        if ( defaultTransactionIsolation != UNKNOWN_TRANSACTIONISOLATION ) {
            con.setTransactionIsolation ( defaultTransactionIsolation );
        }
        if ( defaultReadOnly != null &&
                con.isReadOnly() != defaultReadOnly.booleanValue() ) {
            con.setReadOnly ( defaultReadOnly.booleanValue() );
        }
    }
    @Override
    protected PooledConnectionManager getConnectionManager ( final UserPassKey upkey ) {
        return managers.get ( getPoolKey ( upkey.getUsername() ) );
    }
    @Override
    public Reference getReference() throws NamingException {
        final Reference ref = new Reference ( getClass().getName(),
                                              PerUserPoolDataSourceFactory.class.getName(), null );
        ref.add ( new StringRefAddr ( "instanceKey", getInstanceKey() ) );
        return ref;
    }
    private PoolKey getPoolKey ( final String username ) {
        return new PoolKey ( getDataSourceName(), username );
    }
    private synchronized void registerPool ( final String username, final String password )
    throws NamingException, SQLException {
        final ConnectionPoolDataSource cpds = testCPDS ( username, password );
        final CPDSConnectionFactory factory = new CPDSConnectionFactory ( cpds,
                getValidationQuery(), getValidationQueryTimeout(),
                isRollbackAfterValidation(), username, password );
        factory.setMaxConnLifetimeMillis ( getMaxConnLifetimeMillis() );
        final GenericObjectPool<PooledConnectionAndInfo> pool =
            new GenericObjectPool<> ( factory );
        factory.setPool ( pool );
        pool.setBlockWhenExhausted ( getPerUserBlockWhenExhausted ( username ) );
        pool.setEvictionPolicyClassName (
            getPerUserEvictionPolicyClassName ( username ) );
        pool.setLifo ( getPerUserLifo ( username ) );
        pool.setMaxIdle ( getPerUserMaxIdle ( username ) );
        pool.setMaxTotal ( getPerUserMaxTotal ( username ) );
        pool.setMaxWaitMillis ( getPerUserMaxWaitMillis ( username ) );
        pool.setMinEvictableIdleTimeMillis (
            getPerUserMinEvictableIdleTimeMillis ( username ) );
        pool.setMinIdle ( getPerUserMinIdle ( username ) );
        pool.setNumTestsPerEvictionRun (
            getPerUserNumTestsPerEvictionRun ( username ) );
        pool.setSoftMinEvictableIdleTimeMillis (
            getPerUserSoftMinEvictableIdleTimeMillis ( username ) );
        pool.setTestOnCreate ( getPerUserTestOnCreate ( username ) );
        pool.setTestOnBorrow ( getPerUserTestOnBorrow ( username ) );
        pool.setTestOnReturn ( getPerUserTestOnReturn ( username ) );
        pool.setTestWhileIdle ( getPerUserTestWhileIdle ( username ) );
        pool.setTimeBetweenEvictionRunsMillis (
            getPerUserTimeBetweenEvictionRunsMillis ( username ) );
        pool.setSwallowedExceptionListener ( new SwallowedExceptionLogger ( log ) );
        final Object old = managers.put ( getPoolKey ( username ), factory );
        if ( old != null ) {
            throw new IllegalStateException ( "Pool already contains an entry for this user/password: " + username );
        }
    }
    private void readObject ( final ObjectInputStream in )
    throws IOException, ClassNotFoundException {
        try {
            in.defaultReadObject();
            final PerUserPoolDataSource oldDS = ( PerUserPoolDataSource )
                                                new PerUserPoolDataSourceFactory()
                                                .getObjectInstance ( getReference(), null, null, null );
            this.managers = oldDS.managers;
        } catch ( final NamingException e ) {
            throw new IOException ( "NamingException: " + e );
        }
    }
    private ObjectPool<PooledConnectionAndInfo> getPool ( final PoolKey key ) {
        final CPDSConnectionFactory mgr = ( CPDSConnectionFactory ) managers.get ( key );
        return mgr == null ? null : mgr.getPool();
    }
}
