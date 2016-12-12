package org.apache.tomcat.dbcp.dbcp2.cpdsadapter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import org.apache.tomcat.dbcp.dbcp2.PoolablePreparedStatement;
import org.apache.tomcat.dbcp.pool2.KeyedObjectPool;
import org.apache.tomcat.dbcp.pool2.impl.BaseObjectPoolConfig;
import org.apache.tomcat.dbcp.pool2.impl.GenericKeyedObjectPool;
import org.apache.tomcat.dbcp.pool2.impl.GenericKeyedObjectPoolConfig;
public class DriverAdapterCPDS
    implements ConnectionPoolDataSource, Referenceable, Serializable,
    ObjectFactory {
    private static final long serialVersionUID = -4820523787212147844L;
    private static final String GET_CONNECTION_CALLED
        = "A PooledConnection was already requested from this source, "
          + "further initialization is not allowed.";
    private String description;
    private String password;
    private String url;
    private String user;
    private String driver;
    private int loginTimeout;
    private transient PrintWriter logWriter = null;
    private boolean poolPreparedStatements;
    private int maxIdle = 10;
    private long _timeBetweenEvictionRunsMillis =
        BaseObjectPoolConfig.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;
    private int _numTestsPerEvictionRun = -1;
    private int _minEvictableIdleTimeMillis = -1;
    private int _maxPreparedStatements = -1;
    private volatile boolean getConnectionCalled = false;
    private Properties connectionProperties = null;
    static {
        DriverManager.getDrivers();
    }
    private boolean accessToUnderlyingConnectionAllowed = false;
    public DriverAdapterCPDS() {
    }
    @Override
    public PooledConnection getPooledConnection() throws SQLException {
        return getPooledConnection ( getUser(), getPassword() );
    }
    @Override
    public PooledConnection getPooledConnection ( final String username, final String pass )
    throws SQLException {
        getConnectionCalled = true;
        PooledConnectionImpl pci = null;
        try {
            if ( connectionProperties != null ) {
                connectionProperties.put ( "user", username );
                connectionProperties.put ( "password", pass );
                pci = new PooledConnectionImpl ( DriverManager.getConnection (
                                                     getUrl(), connectionProperties ) );
            } else {
                pci = new PooledConnectionImpl ( DriverManager.getConnection (
                                                     getUrl(), username, pass ) );
            }
            pci.setAccessToUnderlyingConnectionAllowed ( isAccessToUnderlyingConnectionAllowed() );
        } catch ( final ClassCircularityError e ) {
            if ( connectionProperties != null ) {
                pci = new PooledConnectionImpl ( DriverManager.getConnection (
                                                     getUrl(), connectionProperties ) );
            } else {
                pci = new PooledConnectionImpl ( DriverManager.getConnection (
                                                     getUrl(), username, pass ) );
            }
            pci.setAccessToUnderlyingConnectionAllowed ( isAccessToUnderlyingConnectionAllowed() );
        }
        KeyedObjectPool<PStmtKeyCPDS, PoolablePreparedStatement<PStmtKeyCPDS>> stmtPool = null;
        if ( isPoolPreparedStatements() ) {
            final GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
            config.setMaxTotalPerKey ( Integer.MAX_VALUE );
            config.setBlockWhenExhausted ( false );
            config.setMaxWaitMillis ( 0 );
            config.setMaxIdlePerKey ( getMaxIdle() );
            if ( getMaxPreparedStatements() <= 0 ) {
                config.setTimeBetweenEvictionRunsMillis ( getTimeBetweenEvictionRunsMillis() );
                config.setNumTestsPerEvictionRun ( getNumTestsPerEvictionRun() );
                config.setMinEvictableIdleTimeMillis ( getMinEvictableIdleTimeMillis() );
            } else {
                config.setMaxTotal ( getMaxPreparedStatements() );
                config.setTimeBetweenEvictionRunsMillis ( -1 );
                config.setNumTestsPerEvictionRun ( 0 );
                config.setMinEvictableIdleTimeMillis ( 0 );
            }
            stmtPool = new GenericKeyedObjectPool<> ( pci, config );
            pci.setStatementPool ( stmtPool );
        }
        return pci;
    }
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
    @Override
    public Reference getReference() throws NamingException {
        final String factory = getClass().getName();
        final Reference ref = new Reference ( getClass().getName(), factory, null );
        ref.add ( new StringRefAddr ( "description", getDescription() ) );
        ref.add ( new StringRefAddr ( "driver", getDriver() ) );
        ref.add ( new StringRefAddr ( "loginTimeout",
                                      String.valueOf ( getLoginTimeout() ) ) );
        ref.add ( new StringRefAddr ( "password", getPassword() ) );
        ref.add ( new StringRefAddr ( "user", getUser() ) );
        ref.add ( new StringRefAddr ( "url", getUrl() ) );
        ref.add ( new StringRefAddr ( "poolPreparedStatements",
                                      String.valueOf ( isPoolPreparedStatements() ) ) );
        ref.add ( new StringRefAddr ( "maxIdle",
                                      String.valueOf ( getMaxIdle() ) ) );
        ref.add ( new StringRefAddr ( "timeBetweenEvictionRunsMillis",
                                      String.valueOf ( getTimeBetweenEvictionRunsMillis() ) ) );
        ref.add ( new StringRefAddr ( "numTestsPerEvictionRun",
                                      String.valueOf ( getNumTestsPerEvictionRun() ) ) );
        ref.add ( new StringRefAddr ( "minEvictableIdleTimeMillis",
                                      String.valueOf ( getMinEvictableIdleTimeMillis() ) ) );
        ref.add ( new StringRefAddr ( "maxPreparedStatements",
                                      String.valueOf ( getMaxPreparedStatements() ) ) );
        return ref;
    }
    @Override
    public Object getObjectInstance ( final Object refObj, final Name name,
                                      final Context context, final Hashtable<?, ?> env )
    throws Exception {
        DriverAdapterCPDS cpds = null;
        if ( refObj instanceof Reference ) {
            final Reference ref = ( Reference ) refObj;
            if ( ref.getClassName().equals ( getClass().getName() ) ) {
                RefAddr ra = ref.get ( "description" );
                if ( ra != null && ra.getContent() != null ) {
                    setDescription ( ra.getContent().toString() );
                }
                ra = ref.get ( "driver" );
                if ( ra != null && ra.getContent() != null ) {
                    setDriver ( ra.getContent().toString() );
                }
                ra = ref.get ( "url" );
                if ( ra != null && ra.getContent() != null ) {
                    setUrl ( ra.getContent().toString() );
                }
                ra = ref.get ( "user" );
                if ( ra != null && ra.getContent() != null ) {
                    setUser ( ra.getContent().toString() );
                }
                ra = ref.get ( "password" );
                if ( ra != null && ra.getContent() != null ) {
                    setPassword ( ra.getContent().toString() );
                }
                ra = ref.get ( "poolPreparedStatements" );
                if ( ra != null && ra.getContent() != null ) {
                    setPoolPreparedStatements ( Boolean.valueOf (
                                                    ra.getContent().toString() ).booleanValue() );
                }
                ra = ref.get ( "maxIdle" );
                if ( ra != null && ra.getContent() != null ) {
                    setMaxIdle ( Integer.parseInt ( ra.getContent().toString() ) );
                }
                ra = ref.get ( "timeBetweenEvictionRunsMillis" );
                if ( ra != null && ra.getContent() != null ) {
                    setTimeBetweenEvictionRunsMillis (
                        Integer.parseInt ( ra.getContent().toString() ) );
                }
                ra = ref.get ( "numTestsPerEvictionRun" );
                if ( ra != null && ra.getContent() != null ) {
                    setNumTestsPerEvictionRun (
                        Integer.parseInt ( ra.getContent().toString() ) );
                }
                ra = ref.get ( "minEvictableIdleTimeMillis" );
                if ( ra != null && ra.getContent() != null ) {
                    setMinEvictableIdleTimeMillis (
                        Integer.parseInt ( ra.getContent().toString() ) );
                }
                ra = ref.get ( "maxPreparedStatements" );
                if ( ra != null && ra.getContent() != null ) {
                    setMaxPreparedStatements (
                        Integer.parseInt ( ra.getContent().toString() ) );
                }
                ra = ref.get ( "accessToUnderlyingConnectionAllowed" );
                if ( ra != null && ra.getContent() != null ) {
                    setAccessToUnderlyingConnectionAllowed (
                        Boolean.valueOf ( ra.getContent().toString() ).booleanValue() );
                }
                cpds = this;
            }
        }
        return cpds;
    }
    private void assertInitializationAllowed() throws IllegalStateException {
        if ( getConnectionCalled ) {
            throw new IllegalStateException ( GET_CONNECTION_CALLED );
        }
    }
    public Properties getConnectionProperties() {
        return connectionProperties;
    }
    public void setConnectionProperties ( final Properties props ) {
        assertInitializationAllowed();
        connectionProperties = props;
        if ( connectionProperties.containsKey ( "user" ) ) {
            setUser ( connectionProperties.getProperty ( "user" ) );
        }
        if ( connectionProperties.containsKey ( "password" ) ) {
            setPassword ( connectionProperties.getProperty ( "password" ) );
        }
    }
    public String getDescription() {
        return description;
    }
    public void setDescription ( final String  v ) {
        this.description = v;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword ( final String v ) {
        assertInitializationAllowed();
        this.password = v;
        if ( connectionProperties != null ) {
            connectionProperties.setProperty ( "password", v );
        }
    }
    public String getUrl() {
        return url;
    }
    public void setUrl ( final String v ) {
        assertInitializationAllowed();
        this.url = v;
    }
    public String getUser() {
        return user;
    }
    public void setUser ( final String v ) {
        assertInitializationAllowed();
        this.user = v;
        if ( connectionProperties != null ) {
            connectionProperties.setProperty ( "user", v );
        }
    }
    public String getDriver() {
        return driver;
    }
    public void setDriver ( final String v ) throws ClassNotFoundException {
        assertInitializationAllowed();
        this.driver = v;
        Class.forName ( v );
    }
    @Override
    public int getLoginTimeout() {
        return loginTimeout;
    }
    @Override
    public PrintWriter getLogWriter() {
        return logWriter;
    }
    @Override
    public void setLoginTimeout ( final int seconds ) {
        loginTimeout = seconds;
    }
    @Override
    public void setLogWriter ( final PrintWriter out ) {
        logWriter = out;
    }
    public boolean isPoolPreparedStatements() {
        return poolPreparedStatements;
    }
    public void setPoolPreparedStatements ( final boolean v ) {
        assertInitializationAllowed();
        this.poolPreparedStatements = v;
    }
    public int getMaxIdle() {
        return this.maxIdle;
    }
    public void setMaxIdle ( final int maxIdle ) {
        assertInitializationAllowed();
        this.maxIdle = maxIdle;
    }
    public long getTimeBetweenEvictionRunsMillis() {
        return _timeBetweenEvictionRunsMillis;
    }
    public void setTimeBetweenEvictionRunsMillis (
        final long timeBetweenEvictionRunsMillis ) {
        assertInitializationAllowed();
        _timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }
    public int getNumTestsPerEvictionRun() {
        return _numTestsPerEvictionRun;
    }
    public void setNumTestsPerEvictionRun ( final int numTestsPerEvictionRun ) {
        assertInitializationAllowed();
        _numTestsPerEvictionRun = numTestsPerEvictionRun;
    }
    public int getMinEvictableIdleTimeMillis() {
        return _minEvictableIdleTimeMillis;
    }
    public void setMinEvictableIdleTimeMillis ( final int minEvictableIdleTimeMillis ) {
        assertInitializationAllowed();
        _minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }
    public synchronized boolean isAccessToUnderlyingConnectionAllowed() {
        return this.accessToUnderlyingConnectionAllowed;
    }
    public synchronized void setAccessToUnderlyingConnectionAllowed ( final boolean allow ) {
        this.accessToUnderlyingConnectionAllowed = allow;
    }
    public int getMaxPreparedStatements() {
        return _maxPreparedStatements;
    }
    public void setMaxPreparedStatements ( final int maxPreparedStatements ) {
        _maxPreparedStatements = maxPreparedStatements;
    }
}
