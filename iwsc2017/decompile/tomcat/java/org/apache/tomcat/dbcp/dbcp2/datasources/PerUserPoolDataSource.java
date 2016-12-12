package org.apache.tomcat.dbcp.dbcp2.datasources;
import org.apache.juli.logging.LogFactory;
import java.io.IOException;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import java.io.ObjectInputStream;
import javax.sql.ConnectionPoolDataSource;
import org.apache.tomcat.dbcp.pool2.SwallowedExceptionListener;
import org.apache.tomcat.dbcp.dbcp2.SwallowedExceptionLogger;
import org.apache.tomcat.dbcp.pool2.PooledObjectFactory;
import org.apache.tomcat.dbcp.pool2.impl.GenericObjectPool;
import javax.naming.RefAddr;
import javax.naming.StringRefAddr;
import javax.naming.Reference;
import java.sql.Connection;
import java.util.NoSuchElementException;
import javax.naming.NamingException;
import java.sql.SQLException;
import org.apache.tomcat.dbcp.pool2.ObjectPool;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import org.apache.juli.logging.Log;
public class PerUserPoolDataSource extends InstanceKeyDataSource {
    private static final long serialVersionUID = 7872747993848065028L;
    private static final Log log;
    private Map<String, Boolean> perUserBlockWhenExhausted;
    private Map<String, String> perUserEvictionPolicyClassName;
    private Map<String, Boolean> perUserLifo;
    private Map<String, Integer> perUserMaxIdle;
    private Map<String, Integer> perUserMaxTotal;
    private Map<String, Long> perUserMaxWaitMillis;
    private Map<String, Long> perUserMinEvictableIdleTimeMillis;
    private Map<String, Integer> perUserMinIdle;
    private Map<String, Integer> perUserNumTestsPerEvictionRun;
    private Map<String, Long> perUserSoftMinEvictableIdleTimeMillis;
    private Map<String, Boolean> perUserTestOnCreate;
    private Map<String, Boolean> perUserTestOnBorrow;
    private Map<String, Boolean> perUserTestOnReturn;
    private Map<String, Boolean> perUserTestWhileIdle;
    private Map<String, Long> perUserTimeBetweenEvictionRunsMillis;
    private Map<String, Boolean> perUserDefaultAutoCommit;
    private Map<String, Integer> perUserDefaultTransactionIsolation;
    private Map<String, Boolean> perUserDefaultReadOnly;
    private transient Map<PoolKey, PooledConnectionManager> managers;
    public PerUserPoolDataSource() {
        this.perUserBlockWhenExhausted = null;
        this.perUserEvictionPolicyClassName = null;
        this.perUserLifo = null;
        this.perUserMaxIdle = null;
        this.perUserMaxTotal = null;
        this.perUserMaxWaitMillis = null;
        this.perUserMinEvictableIdleTimeMillis = null;
        this.perUserMinIdle = null;
        this.perUserNumTestsPerEvictionRun = null;
        this.perUserSoftMinEvictableIdleTimeMillis = null;
        this.perUserTestOnCreate = null;
        this.perUserTestOnBorrow = null;
        this.perUserTestOnReturn = null;
        this.perUserTestWhileIdle = null;
        this.perUserTimeBetweenEvictionRunsMillis = null;
        this.perUserDefaultAutoCommit = null;
        this.perUserDefaultTransactionIsolation = null;
        this.perUserDefaultReadOnly = null;
        this.managers = new HashMap<PoolKey, PooledConnectionManager>();
    }
    @Override
    public void close() {
        for ( final PooledConnectionManager manager : this.managers.values() ) {
            try {
                ( ( CPDSConnectionFactory ) manager ).getPool().close();
            } catch ( Exception ex ) {}
        }
        InstanceKeyDataSourceFactory.removeInstance ( this.getInstanceKey() );
    }
    public boolean getPerUserBlockWhenExhausted ( final String key ) {
        Boolean value = null;
        if ( this.perUserBlockWhenExhausted != null ) {
            value = this.perUserBlockWhenExhausted.get ( key );
        }
        if ( value == null ) {
            return this.getDefaultBlockWhenExhausted();
        }
        return value;
    }
    public void setPerUserBlockWhenExhausted ( final String username, final Boolean value ) {
        this.assertInitializationAllowed();
        if ( this.perUserBlockWhenExhausted == null ) {
            this.perUserBlockWhenExhausted = new HashMap<String, Boolean>();
        }
        this.perUserBlockWhenExhausted.put ( username, value );
    }
    void setPerUserBlockWhenExhausted ( final Map<String, Boolean> userDefaultBlockWhenExhausted ) {
        this.assertInitializationAllowed();
        if ( this.perUserBlockWhenExhausted == null ) {
            this.perUserBlockWhenExhausted = new HashMap<String, Boolean>();
        } else {
            this.perUserBlockWhenExhausted.clear();
        }
        this.perUserBlockWhenExhausted.putAll ( userDefaultBlockWhenExhausted );
    }
    public String getPerUserEvictionPolicyClassName ( final String key ) {
        String value = null;
        if ( this.perUserEvictionPolicyClassName != null ) {
            value = this.perUserEvictionPolicyClassName.get ( key );
        }
        if ( value == null ) {
            return this.getDefaultEvictionPolicyClassName();
        }
        return value;
    }
    public void setPerUserEvictionPolicyClassName ( final String username, final String value ) {
        this.assertInitializationAllowed();
        if ( this.perUserEvictionPolicyClassName == null ) {
            this.perUserEvictionPolicyClassName = new HashMap<String, String>();
        }
        this.perUserEvictionPolicyClassName.put ( username, value );
    }
    void setPerUserEvictionPolicyClassName ( final Map<String, String> userDefaultEvictionPolicyClassName ) {
        this.assertInitializationAllowed();
        if ( this.perUserEvictionPolicyClassName == null ) {
            this.perUserEvictionPolicyClassName = new HashMap<String, String>();
        } else {
            this.perUserEvictionPolicyClassName.clear();
        }
        this.perUserEvictionPolicyClassName.putAll ( userDefaultEvictionPolicyClassName );
    }
    public boolean getPerUserLifo ( final String key ) {
        Boolean value = null;
        if ( this.perUserLifo != null ) {
            value = this.perUserLifo.get ( key );
        }
        if ( value == null ) {
            return this.getDefaultLifo();
        }
        return value;
    }
    public void setPerUserLifo ( final String username, final Boolean value ) {
        this.assertInitializationAllowed();
        if ( this.perUserLifo == null ) {
            this.perUserLifo = new HashMap<String, Boolean>();
        }
        this.perUserLifo.put ( username, value );
    }
    void setPerUserLifo ( final Map<String, Boolean> userDefaultLifo ) {
        this.assertInitializationAllowed();
        if ( this.perUserLifo == null ) {
            this.perUserLifo = new HashMap<String, Boolean>();
        } else {
            this.perUserLifo.clear();
        }
        this.perUserLifo.putAll ( userDefaultLifo );
    }
    public int getPerUserMaxIdle ( final String key ) {
        Integer value = null;
        if ( this.perUserMaxIdle != null ) {
            value = this.perUserMaxIdle.get ( key );
        }
        if ( value == null ) {
            return this.getDefaultMaxIdle();
        }
        return value;
    }
    public void setPerUserMaxIdle ( final String username, final Integer value ) {
        this.assertInitializationAllowed();
        if ( this.perUserMaxIdle == null ) {
            this.perUserMaxIdle = new HashMap<String, Integer>();
        }
        this.perUserMaxIdle.put ( username, value );
    }
    void setPerUserMaxIdle ( final Map<String, Integer> userDefaultMaxIdle ) {
        this.assertInitializationAllowed();
        if ( this.perUserMaxIdle == null ) {
            this.perUserMaxIdle = new HashMap<String, Integer>();
        } else {
            this.perUserMaxIdle.clear();
        }
        this.perUserMaxIdle.putAll ( userDefaultMaxIdle );
    }
    public int getPerUserMaxTotal ( final String key ) {
        Integer value = null;
        if ( this.perUserMaxTotal != null ) {
            value = this.perUserMaxTotal.get ( key );
        }
        if ( value == null ) {
            return this.getDefaultMaxTotal();
        }
        return value;
    }
    public void setPerUserMaxTotal ( final String username, final Integer value ) {
        this.assertInitializationAllowed();
        if ( this.perUserMaxTotal == null ) {
            this.perUserMaxTotal = new HashMap<String, Integer>();
        }
        this.perUserMaxTotal.put ( username, value );
    }
    void setPerUserMaxTotal ( final Map<String, Integer> userDefaultMaxTotal ) {
        this.assertInitializationAllowed();
        if ( this.perUserMaxTotal == null ) {
            this.perUserMaxTotal = new HashMap<String, Integer>();
        } else {
            this.perUserMaxTotal.clear();
        }
        this.perUserMaxTotal.putAll ( userDefaultMaxTotal );
    }
    public long getPerUserMaxWaitMillis ( final String key ) {
        Long value = null;
        if ( this.perUserMaxWaitMillis != null ) {
            value = this.perUserMaxWaitMillis.get ( key );
        }
        if ( value == null ) {
            return this.getDefaultMaxWaitMillis();
        }
        return value;
    }
    public void setPerUserMaxWaitMillis ( final String username, final Long value ) {
        this.assertInitializationAllowed();
        if ( this.perUserMaxWaitMillis == null ) {
            this.perUserMaxWaitMillis = new HashMap<String, Long>();
        }
        this.perUserMaxWaitMillis.put ( username, value );
    }
    void setPerUserMaxWaitMillis ( final Map<String, Long> userDefaultMaxWaitMillis ) {
        this.assertInitializationAllowed();
        if ( this.perUserMaxWaitMillis == null ) {
            this.perUserMaxWaitMillis = new HashMap<String, Long>();
        } else {
            this.perUserMaxWaitMillis.clear();
        }
        this.perUserMaxWaitMillis.putAll ( userDefaultMaxWaitMillis );
    }
    public long getPerUserMinEvictableIdleTimeMillis ( final String key ) {
        Long value = null;
        if ( this.perUserMinEvictableIdleTimeMillis != null ) {
            value = this.perUserMinEvictableIdleTimeMillis.get ( key );
        }
        if ( value == null ) {
            return this.getDefaultMinEvictableIdleTimeMillis();
        }
        return value;
    }
    public void setPerUserMinEvictableIdleTimeMillis ( final String username, final Long value ) {
        this.assertInitializationAllowed();
        if ( this.perUserMinEvictableIdleTimeMillis == null ) {
            this.perUserMinEvictableIdleTimeMillis = new HashMap<String, Long>();
        }
        this.perUserMinEvictableIdleTimeMillis.put ( username, value );
    }
    void setPerUserMinEvictableIdleTimeMillis ( final Map<String, Long> userDefaultMinEvictableIdleTimeMillis ) {
        this.assertInitializationAllowed();
        if ( this.perUserMinEvictableIdleTimeMillis == null ) {
            this.perUserMinEvictableIdleTimeMillis = new HashMap<String, Long>();
        } else {
            this.perUserMinEvictableIdleTimeMillis.clear();
        }
        this.perUserMinEvictableIdleTimeMillis.putAll ( userDefaultMinEvictableIdleTimeMillis );
    }
    public int getPerUserMinIdle ( final String key ) {
        Integer value = null;
        if ( this.perUserMinIdle != null ) {
            value = this.perUserMinIdle.get ( key );
        }
        if ( value == null ) {
            return this.getDefaultMinIdle();
        }
        return value;
    }
    public void setPerUserMinIdle ( final String username, final Integer value ) {
        this.assertInitializationAllowed();
        if ( this.perUserMinIdle == null ) {
            this.perUserMinIdle = new HashMap<String, Integer>();
        }
        this.perUserMinIdle.put ( username, value );
    }
    void setPerUserMinIdle ( final Map<String, Integer> userDefaultMinIdle ) {
        this.assertInitializationAllowed();
        if ( this.perUserMinIdle == null ) {
            this.perUserMinIdle = new HashMap<String, Integer>();
        } else {
            this.perUserMinIdle.clear();
        }
        this.perUserMinIdle.putAll ( userDefaultMinIdle );
    }
    public int getPerUserNumTestsPerEvictionRun ( final String key ) {
        Integer value = null;
        if ( this.perUserNumTestsPerEvictionRun != null ) {
            value = this.perUserNumTestsPerEvictionRun.get ( key );
        }
        if ( value == null ) {
            return this.getDefaultNumTestsPerEvictionRun();
        }
        return value;
    }
    public void setPerUserNumTestsPerEvictionRun ( final String username, final Integer value ) {
        this.assertInitializationAllowed();
        if ( this.perUserNumTestsPerEvictionRun == null ) {
            this.perUserNumTestsPerEvictionRun = new HashMap<String, Integer>();
        }
        this.perUserNumTestsPerEvictionRun.put ( username, value );
    }
    void setPerUserNumTestsPerEvictionRun ( final Map<String, Integer> userDefaultNumTestsPerEvictionRun ) {
        this.assertInitializationAllowed();
        if ( this.perUserNumTestsPerEvictionRun == null ) {
            this.perUserNumTestsPerEvictionRun = new HashMap<String, Integer>();
        } else {
            this.perUserNumTestsPerEvictionRun.clear();
        }
        this.perUserNumTestsPerEvictionRun.putAll ( userDefaultNumTestsPerEvictionRun );
    }
    public long getPerUserSoftMinEvictableIdleTimeMillis ( final String key ) {
        Long value = null;
        if ( this.perUserSoftMinEvictableIdleTimeMillis != null ) {
            value = this.perUserSoftMinEvictableIdleTimeMillis.get ( key );
        }
        if ( value == null ) {
            return this.getDefaultSoftMinEvictableIdleTimeMillis();
        }
        return value;
    }
    public void setPerUserSoftMinEvictableIdleTimeMillis ( final String username, final Long value ) {
        this.assertInitializationAllowed();
        if ( this.perUserSoftMinEvictableIdleTimeMillis == null ) {
            this.perUserSoftMinEvictableIdleTimeMillis = new HashMap<String, Long>();
        }
        this.perUserSoftMinEvictableIdleTimeMillis.put ( username, value );
    }
    void setPerUserSoftMinEvictableIdleTimeMillis ( final Map<String, Long> userDefaultSoftMinEvictableIdleTimeMillis ) {
        this.assertInitializationAllowed();
        if ( this.perUserSoftMinEvictableIdleTimeMillis == null ) {
            this.perUserSoftMinEvictableIdleTimeMillis = new HashMap<String, Long>();
        } else {
            this.perUserSoftMinEvictableIdleTimeMillis.clear();
        }
        this.perUserSoftMinEvictableIdleTimeMillis.putAll ( userDefaultSoftMinEvictableIdleTimeMillis );
    }
    public boolean getPerUserTestOnCreate ( final String key ) {
        Boolean value = null;
        if ( this.perUserTestOnCreate != null ) {
            value = this.perUserTestOnCreate.get ( key );
        }
        if ( value == null ) {
            return this.getDefaultTestOnCreate();
        }
        return value;
    }
    public void setPerUserTestOnCreate ( final String username, final Boolean value ) {
        this.assertInitializationAllowed();
        if ( this.perUserTestOnCreate == null ) {
            this.perUserTestOnCreate = new HashMap<String, Boolean>();
        }
        this.perUserTestOnCreate.put ( username, value );
    }
    void setPerUserTestOnCreate ( final Map<String, Boolean> userDefaultTestOnCreate ) {
        this.assertInitializationAllowed();
        if ( this.perUserTestOnCreate == null ) {
            this.perUserTestOnCreate = new HashMap<String, Boolean>();
        } else {
            this.perUserTestOnCreate.clear();
        }
        this.perUserTestOnCreate.putAll ( userDefaultTestOnCreate );
    }
    public boolean getPerUserTestOnBorrow ( final String key ) {
        Boolean value = null;
        if ( this.perUserTestOnBorrow != null ) {
            value = this.perUserTestOnBorrow.get ( key );
        }
        if ( value == null ) {
            return this.getDefaultTestOnBorrow();
        }
        return value;
    }
    public void setPerUserTestOnBorrow ( final String username, final Boolean value ) {
        this.assertInitializationAllowed();
        if ( this.perUserTestOnBorrow == null ) {
            this.perUserTestOnBorrow = new HashMap<String, Boolean>();
        }
        this.perUserTestOnBorrow.put ( username, value );
    }
    void setPerUserTestOnBorrow ( final Map<String, Boolean> userDefaultTestOnBorrow ) {
        this.assertInitializationAllowed();
        if ( this.perUserTestOnBorrow == null ) {
            this.perUserTestOnBorrow = new HashMap<String, Boolean>();
        } else {
            this.perUserTestOnBorrow.clear();
        }
        this.perUserTestOnBorrow.putAll ( userDefaultTestOnBorrow );
    }
    public boolean getPerUserTestOnReturn ( final String key ) {
        Boolean value = null;
        if ( this.perUserTestOnReturn != null ) {
            value = this.perUserTestOnReturn.get ( key );
        }
        if ( value == null ) {
            return this.getDefaultTestOnReturn();
        }
        return value;
    }
    public void setPerUserTestOnReturn ( final String username, final Boolean value ) {
        this.assertInitializationAllowed();
        if ( this.perUserTestOnReturn == null ) {
            this.perUserTestOnReturn = new HashMap<String, Boolean>();
        }
        this.perUserTestOnReturn.put ( username, value );
    }
    void setPerUserTestOnReturn ( final Map<String, Boolean> userDefaultTestOnReturn ) {
        this.assertInitializationAllowed();
        if ( this.perUserTestOnReturn == null ) {
            this.perUserTestOnReturn = new HashMap<String, Boolean>();
        } else {
            this.perUserTestOnReturn.clear();
        }
        this.perUserTestOnReturn.putAll ( userDefaultTestOnReturn );
    }
    public boolean getPerUserTestWhileIdle ( final String key ) {
        Boolean value = null;
        if ( this.perUserTestWhileIdle != null ) {
            value = this.perUserTestWhileIdle.get ( key );
        }
        if ( value == null ) {
            return this.getDefaultTestWhileIdle();
        }
        return value;
    }
    public void setPerUserTestWhileIdle ( final String username, final Boolean value ) {
        this.assertInitializationAllowed();
        if ( this.perUserTestWhileIdle == null ) {
            this.perUserTestWhileIdle = new HashMap<String, Boolean>();
        }
        this.perUserTestWhileIdle.put ( username, value );
    }
    void setPerUserTestWhileIdle ( final Map<String, Boolean> userDefaultTestWhileIdle ) {
        this.assertInitializationAllowed();
        if ( this.perUserTestWhileIdle == null ) {
            this.perUserTestWhileIdle = new HashMap<String, Boolean>();
        } else {
            this.perUserTestWhileIdle.clear();
        }
        this.perUserTestWhileIdle.putAll ( userDefaultTestWhileIdle );
    }
    public long getPerUserTimeBetweenEvictionRunsMillis ( final String key ) {
        Long value = null;
        if ( this.perUserTimeBetweenEvictionRunsMillis != null ) {
            value = this.perUserTimeBetweenEvictionRunsMillis.get ( key );
        }
        if ( value == null ) {
            return this.getDefaultTimeBetweenEvictionRunsMillis();
        }
        return value;
    }
    public void setPerUserTimeBetweenEvictionRunsMillis ( final String username, final Long value ) {
        this.assertInitializationAllowed();
        if ( this.perUserTimeBetweenEvictionRunsMillis == null ) {
            this.perUserTimeBetweenEvictionRunsMillis = new HashMap<String, Long>();
        }
        this.perUserTimeBetweenEvictionRunsMillis.put ( username, value );
    }
    void setPerUserTimeBetweenEvictionRunsMillis ( final Map<String, Long> userDefaultTimeBetweenEvictionRunsMillis ) {
        this.assertInitializationAllowed();
        if ( this.perUserTimeBetweenEvictionRunsMillis == null ) {
            this.perUserTimeBetweenEvictionRunsMillis = new HashMap<String, Long>();
        } else {
            this.perUserTimeBetweenEvictionRunsMillis.clear();
        }
        this.perUserTimeBetweenEvictionRunsMillis.putAll ( userDefaultTimeBetweenEvictionRunsMillis );
    }
    public Boolean getPerUserDefaultAutoCommit ( final String key ) {
        Boolean value = null;
        if ( this.perUserDefaultAutoCommit != null ) {
            value = this.perUserDefaultAutoCommit.get ( key );
        }
        return value;
    }
    public void setPerUserDefaultAutoCommit ( final String username, final Boolean value ) {
        this.assertInitializationAllowed();
        if ( this.perUserDefaultAutoCommit == null ) {
            this.perUserDefaultAutoCommit = new HashMap<String, Boolean>();
        }
        this.perUserDefaultAutoCommit.put ( username, value );
    }
    void setPerUserDefaultAutoCommit ( final Map<String, Boolean> userDefaultAutoCommit ) {
        this.assertInitializationAllowed();
        if ( this.perUserDefaultAutoCommit == null ) {
            this.perUserDefaultAutoCommit = new HashMap<String, Boolean>();
        } else {
            this.perUserDefaultAutoCommit.clear();
        }
        this.perUserDefaultAutoCommit.putAll ( userDefaultAutoCommit );
    }
    public Boolean getPerUserDefaultReadOnly ( final String key ) {
        Boolean value = null;
        if ( this.perUserDefaultReadOnly != null ) {
            value = this.perUserDefaultReadOnly.get ( key );
        }
        return value;
    }
    public void setPerUserDefaultReadOnly ( final String username, final Boolean value ) {
        this.assertInitializationAllowed();
        if ( this.perUserDefaultReadOnly == null ) {
            this.perUserDefaultReadOnly = new HashMap<String, Boolean>();
        }
        this.perUserDefaultReadOnly.put ( username, value );
    }
    void setPerUserDefaultReadOnly ( final Map<String, Boolean> userDefaultReadOnly ) {
        this.assertInitializationAllowed();
        if ( this.perUserDefaultReadOnly == null ) {
            this.perUserDefaultReadOnly = new HashMap<String, Boolean>();
        } else {
            this.perUserDefaultReadOnly.clear();
        }
        this.perUserDefaultReadOnly.putAll ( userDefaultReadOnly );
    }
    public Integer getPerUserDefaultTransactionIsolation ( final String key ) {
        Integer value = null;
        if ( this.perUserDefaultTransactionIsolation != null ) {
            value = this.perUserDefaultTransactionIsolation.get ( key );
        }
        return value;
    }
    public void setPerUserDefaultTransactionIsolation ( final String username, final Integer value ) {
        this.assertInitializationAllowed();
        if ( this.perUserDefaultTransactionIsolation == null ) {
            this.perUserDefaultTransactionIsolation = new HashMap<String, Integer>();
        }
        this.perUserDefaultTransactionIsolation.put ( username, value );
    }
    void setPerUserDefaultTransactionIsolation ( final Map<String, Integer> userDefaultTransactionIsolation ) {
        this.assertInitializationAllowed();
        if ( this.perUserDefaultTransactionIsolation == null ) {
            this.perUserDefaultTransactionIsolation = new HashMap<String, Integer>();
        } else {
            this.perUserDefaultTransactionIsolation.clear();
        }
        this.perUserDefaultTransactionIsolation.putAll ( userDefaultTransactionIsolation );
    }
    public int getNumActive() {
        return this.getNumActive ( null );
    }
    public int getNumActive ( final String username ) {
        final ObjectPool<PooledConnectionAndInfo> pool = this.getPool ( this.getPoolKey ( username ) );
        return ( pool == null ) ? 0 : pool.getNumActive();
    }
    public int getNumIdle() {
        return this.getNumIdle ( null );
    }
    public int getNumIdle ( final String username ) {
        final ObjectPool<PooledConnectionAndInfo> pool = this.getPool ( this.getPoolKey ( username ) );
        return ( pool == null ) ? 0 : pool.getNumIdle();
    }
    @Override
    protected PooledConnectionAndInfo getPooledConnectionAndInfo ( final String username, final String password ) throws SQLException {
        final PoolKey key = this.getPoolKey ( username );
        PooledConnectionManager manager;
        ObjectPool<PooledConnectionAndInfo> pool;
        synchronized ( this ) {
            manager = this.managers.get ( key );
            if ( manager == null ) {
                try {
                    this.registerPool ( username, password );
                    manager = this.managers.get ( key );
                } catch ( NamingException e ) {
                    throw new SQLException ( "RegisterPool failed", e );
                }
            }
            pool = ( ( CPDSConnectionFactory ) manager ).getPool();
        }
        PooledConnectionAndInfo info = null;
        try {
            info = pool.borrowObject();
        } catch ( NoSuchElementException ex ) {
            throw new SQLException ( "Could not retrieve connection info from pool", ex );
        } catch ( Exception e2 ) {
            try {
                this.testCPDS ( username, password );
            } catch ( Exception ex2 ) {
                throw new SQLException ( "Could not retrieve connection info from pool", ex2 );
            }
            manager.closePool ( username );
            synchronized ( this ) {
                this.managers.remove ( key );
            }
            try {
                this.registerPool ( username, password );
                pool = this.getPool ( key );
            } catch ( NamingException ne ) {
                throw new SQLException ( "RegisterPool failed", ne );
            }
            try {
                info = pool.borrowObject();
            } catch ( Exception ex2 ) {
                throw new SQLException ( "Could not retrieve connection info from pool", ex2 );
            }
        }
        return info;
    }
    @Override
    protected void setupDefaults ( final Connection con, final String username ) throws SQLException {
        Boolean defaultAutoCommit = this.isDefaultAutoCommit();
        if ( username != null ) {
            final Boolean userMax = this.getPerUserDefaultAutoCommit ( username );
            if ( userMax != null ) {
                defaultAutoCommit = userMax;
            }
        }
        Boolean defaultReadOnly = this.isDefaultReadOnly();
        if ( username != null ) {
            final Boolean userMax2 = this.getPerUserDefaultReadOnly ( username );
            if ( userMax2 != null ) {
                defaultReadOnly = userMax2;
            }
        }
        int defaultTransactionIsolation = this.getDefaultTransactionIsolation();
        if ( username != null ) {
            final Integer userMax3 = this.getPerUserDefaultTransactionIsolation ( username );
            if ( userMax3 != null ) {
                defaultTransactionIsolation = userMax3;
            }
        }
        if ( defaultAutoCommit != null && con.getAutoCommit() != defaultAutoCommit ) {
            con.setAutoCommit ( defaultAutoCommit );
        }
        if ( defaultTransactionIsolation != -1 ) {
            con.setTransactionIsolation ( defaultTransactionIsolation );
        }
        if ( defaultReadOnly != null && con.isReadOnly() != defaultReadOnly ) {
            con.setReadOnly ( defaultReadOnly );
        }
    }
    @Override
    protected PooledConnectionManager getConnectionManager ( final UserPassKey upkey ) {
        return this.managers.get ( this.getPoolKey ( upkey.getUsername() ) );
    }
    @Override
    public Reference getReference() throws NamingException {
        final Reference ref = new Reference ( this.getClass().getName(), PerUserPoolDataSourceFactory.class.getName(), null );
        ref.add ( new StringRefAddr ( "instanceKey", this.getInstanceKey() ) );
        return ref;
    }
    private PoolKey getPoolKey ( final String username ) {
        return new PoolKey ( this.getDataSourceName(), username );
    }
    private synchronized void registerPool ( final String username, final String password ) throws NamingException, SQLException {
        final ConnectionPoolDataSource cpds = this.testCPDS ( username, password );
        final CPDSConnectionFactory factory = new CPDSConnectionFactory ( cpds, this.getValidationQuery(), this.getValidationQueryTimeout(), this.isRollbackAfterValidation(), username, password );
        factory.setMaxConnLifetimeMillis ( this.getMaxConnLifetimeMillis() );
        final GenericObjectPool<PooledConnectionAndInfo> pool = new GenericObjectPool<PooledConnectionAndInfo> ( factory );
        factory.setPool ( pool );
        pool.setBlockWhenExhausted ( this.getPerUserBlockWhenExhausted ( username ) );
        pool.setEvictionPolicyClassName ( this.getPerUserEvictionPolicyClassName ( username ) );
        pool.setLifo ( this.getPerUserLifo ( username ) );
        pool.setMaxIdle ( this.getPerUserMaxIdle ( username ) );
        pool.setMaxTotal ( this.getPerUserMaxTotal ( username ) );
        pool.setMaxWaitMillis ( this.getPerUserMaxWaitMillis ( username ) );
        pool.setMinEvictableIdleTimeMillis ( this.getPerUserMinEvictableIdleTimeMillis ( username ) );
        pool.setMinIdle ( this.getPerUserMinIdle ( username ) );
        pool.setNumTestsPerEvictionRun ( this.getPerUserNumTestsPerEvictionRun ( username ) );
        pool.setSoftMinEvictableIdleTimeMillis ( this.getPerUserSoftMinEvictableIdleTimeMillis ( username ) );
        pool.setTestOnCreate ( this.getPerUserTestOnCreate ( username ) );
        pool.setTestOnBorrow ( this.getPerUserTestOnBorrow ( username ) );
        pool.setTestOnReturn ( this.getPerUserTestOnReturn ( username ) );
        pool.setTestWhileIdle ( this.getPerUserTestWhileIdle ( username ) );
        pool.setTimeBetweenEvictionRunsMillis ( this.getPerUserTimeBetweenEvictionRunsMillis ( username ) );
        pool.setSwallowedExceptionListener ( new SwallowedExceptionLogger ( PerUserPoolDataSource.log ) );
        final Object old = this.managers.put ( this.getPoolKey ( username ), factory );
        if ( old != null ) {
            throw new IllegalStateException ( "Pool already contains an entry for this user/password: " + username );
        }
    }
    private void readObject ( final ObjectInputStream in ) throws IOException, ClassNotFoundException {
        try {
            in.defaultReadObject();
            final PerUserPoolDataSource oldDS = ( PerUserPoolDataSource ) new PerUserPoolDataSourceFactory().getObjectInstance ( this.getReference(), null, null, null );
            this.managers = oldDS.managers;
        } catch ( NamingException e ) {
            throw new IOException ( "NamingException: " + e );
        }
    }
    private ObjectPool<PooledConnectionAndInfo> getPool ( final PoolKey key ) {
        final CPDSConnectionFactory mgr = this.managers.get ( key );
        return ( mgr == null ) ? null : mgr.getPool();
    }
    static {
        log = LogFactory.getLog ( PerUserPoolDataSource.class );
    }
}
