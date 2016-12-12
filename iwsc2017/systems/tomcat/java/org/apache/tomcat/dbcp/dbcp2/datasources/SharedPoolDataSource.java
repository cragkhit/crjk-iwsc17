package org.apache.tomcat.dbcp.dbcp2.datasources;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.ConnectionPoolDataSource;
import org.apache.tomcat.dbcp.pool2.KeyedObjectPool;
import org.apache.tomcat.dbcp.pool2.impl.GenericKeyedObjectPool;
import org.apache.tomcat.dbcp.pool2.impl.GenericKeyedObjectPoolConfig;
public class SharedPoolDataSource extends InstanceKeyDataSource {
    private static final long serialVersionUID = -1458539734480586454L;
    private int maxTotal = GenericKeyedObjectPoolConfig.DEFAULT_MAX_TOTAL;
    private transient KeyedObjectPool<UserPassKey, PooledConnectionAndInfo> pool = null;
    private transient KeyedCPDSConnectionFactory factory = null;
    public SharedPoolDataSource() {
    }
    @Override
    public void close() throws Exception {
        if ( pool != null ) {
            pool.close();
        }
        InstanceKeyDataSourceFactory.removeInstance ( getInstanceKey() );
    }
    public int getMaxTotal() {
        return this.maxTotal;
    }
    public void setMaxTotal ( final int maxTotal ) {
        assertInitializationAllowed();
        this.maxTotal = maxTotal;
    }
    public int getNumActive() {
        return pool == null ? 0 : pool.getNumActive();
    }
    public int getNumIdle() {
        return pool == null ? 0 : pool.getNumIdle();
    }
    @Override
    protected PooledConnectionAndInfo
    getPooledConnectionAndInfo ( final String username, final String password )
    throws SQLException {
        synchronized ( this ) {
            if ( pool == null ) {
                try {
                    registerPool ( username, password );
                } catch ( final NamingException e ) {
                    throw new SQLException ( "RegisterPool failed", e );
                }
            }
        }
        PooledConnectionAndInfo info = null;
        final UserPassKey key = new UserPassKey ( username, password );
        try {
            info = pool.borrowObject ( key );
        } catch ( final Exception e ) {
            throw new SQLException (
                "Could not retrieve connection info from pool", e );
        }
        return info;
    }
    @Override
    protected PooledConnectionManager getConnectionManager ( final UserPassKey upkey )  {
        return factory;
    }
    @Override
    public Reference getReference() throws NamingException {
        final Reference ref = new Reference ( getClass().getName(),
                                              SharedPoolDataSourceFactory.class.getName(), null );
        ref.add ( new StringRefAddr ( "instanceKey", getInstanceKey() ) );
        return ref;
    }
    private void registerPool ( final String username, final String password )
    throws NamingException, SQLException {
        final ConnectionPoolDataSource cpds = testCPDS ( username, password );
        factory = new KeyedCPDSConnectionFactory ( cpds, getValidationQuery(),
                getValidationQueryTimeout(), isRollbackAfterValidation() );
        factory.setMaxConnLifetimeMillis ( getMaxConnLifetimeMillis() );
        final GenericKeyedObjectPoolConfig config =
            new GenericKeyedObjectPoolConfig();
        config.setBlockWhenExhausted ( getDefaultBlockWhenExhausted() );
        config.setEvictionPolicyClassName ( getDefaultEvictionPolicyClassName() );
        config.setLifo ( getDefaultLifo() );
        config.setMaxIdlePerKey ( getDefaultMaxIdle() );
        config.setMaxTotal ( getMaxTotal() );
        config.setMaxTotalPerKey ( getDefaultMaxTotal() );
        config.setMaxWaitMillis ( getDefaultMaxWaitMillis() );
        config.setMinEvictableIdleTimeMillis (
            getDefaultMinEvictableIdleTimeMillis() );
        config.setMinIdlePerKey ( getDefaultMinIdle() );
        config.setNumTestsPerEvictionRun ( getDefaultNumTestsPerEvictionRun() );
        config.setSoftMinEvictableIdleTimeMillis (
            getDefaultSoftMinEvictableIdleTimeMillis() );
        config.setTestOnCreate ( getDefaultTestOnCreate() );
        config.setTestOnBorrow ( getDefaultTestOnBorrow() );
        config.setTestOnReturn ( getDefaultTestOnReturn() );
        config.setTestWhileIdle ( getDefaultTestWhileIdle() );
        config.setTimeBetweenEvictionRunsMillis (
            getDefaultTimeBetweenEvictionRunsMillis() );
        final KeyedObjectPool<UserPassKey, PooledConnectionAndInfo> tmpPool =
            new GenericKeyedObjectPool<> ( factory, config );
        factory.setPool ( tmpPool );
        pool = tmpPool;
    }
    @Override
    protected void setupDefaults ( final Connection con, final String username ) throws SQLException {
        final Boolean defaultAutoCommit = isDefaultAutoCommit();
        if ( defaultAutoCommit != null &&
                con.getAutoCommit() != defaultAutoCommit.booleanValue() ) {
            con.setAutoCommit ( defaultAutoCommit.booleanValue() );
        }
        final int defaultTransactionIsolation = getDefaultTransactionIsolation();
        if ( defaultTransactionIsolation != UNKNOWN_TRANSACTIONISOLATION ) {
            con.setTransactionIsolation ( defaultTransactionIsolation );
        }
        final Boolean defaultReadOnly = isDefaultReadOnly();
        if ( defaultReadOnly != null &&
                con.isReadOnly() != defaultReadOnly.booleanValue() ) {
            con.setReadOnly ( defaultReadOnly.booleanValue() );
        }
    }
    private void readObject ( final ObjectInputStream in )
    throws IOException, ClassNotFoundException {
        try {
            in.defaultReadObject();
            final SharedPoolDataSource oldDS = ( SharedPoolDataSource )
                                               new SharedPoolDataSourceFactory()
                                               .getObjectInstance ( getReference(), null, null, null );
            this.pool = oldDS.pool;
        } catch ( final NamingException e ) {
            throw new IOException ( "NamingException: " + e );
        }
    }
}
