package org.apache.tomcat.dbcp.dbcp2.cpdsadapter;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.StringRefAddr;
import javax.naming.Reference;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import org.apache.tomcat.dbcp.pool2.KeyedObjectPool;
import org.apache.tomcat.dbcp.pool2.KeyedPooledObjectFactory;
import org.apache.tomcat.dbcp.dbcp2.PoolablePreparedStatement;
import org.apache.tomcat.dbcp.pool2.impl.GenericKeyedObjectPool;
import org.apache.tomcat.dbcp.pool2.impl.GenericKeyedObjectPoolConfig;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.PooledConnection;
import java.util.Properties;
import java.io.PrintWriter;
import javax.naming.spi.ObjectFactory;
import java.io.Serializable;
import javax.naming.Referenceable;
import javax.sql.ConnectionPoolDataSource;
public class DriverAdapterCPDS implements ConnectionPoolDataSource, Referenceable, Serializable, ObjectFactory {
    private static final long serialVersionUID = -4820523787212147844L;
    private static final String GET_CONNECTION_CALLED = "A PooledConnection was already requested from this source, further initialization is not allowed.";
    private String description;
    private String password;
    private String url;
    private String user;
    private String driver;
    private int loginTimeout;
    private transient PrintWriter logWriter;
    private boolean poolPreparedStatements;
    private int maxIdle;
    private long _timeBetweenEvictionRunsMillis;
    private int _numTestsPerEvictionRun;
    private int _minEvictableIdleTimeMillis;
    private int _maxPreparedStatements;
    private volatile boolean getConnectionCalled;
    private Properties connectionProperties;
    private boolean accessToUnderlyingConnectionAllowed;
    public DriverAdapterCPDS() {
        this.logWriter = null;
        this.maxIdle = 10;
        this._timeBetweenEvictionRunsMillis = -1L;
        this._numTestsPerEvictionRun = -1;
        this._minEvictableIdleTimeMillis = -1;
        this._maxPreparedStatements = -1;
        this.getConnectionCalled = false;
        this.connectionProperties = null;
        this.accessToUnderlyingConnectionAllowed = false;
    }
    @Override
    public PooledConnection getPooledConnection() throws SQLException {
        return this.getPooledConnection ( this.getUser(), this.getPassword() );
    }
    @Override
    public PooledConnection getPooledConnection ( final String username, final String pass ) throws SQLException {
        this.getConnectionCalled = true;
        PooledConnectionImpl pci = null;
        try {
            if ( this.connectionProperties != null ) {
                ( ( Hashtable<String, String> ) this.connectionProperties ).put ( "user", username );
                ( ( Hashtable<String, String> ) this.connectionProperties ).put ( "password", pass );
                pci = new PooledConnectionImpl ( DriverManager.getConnection ( this.getUrl(), this.connectionProperties ) );
            } else {
                pci = new PooledConnectionImpl ( DriverManager.getConnection ( this.getUrl(), username, pass ) );
            }
            pci.setAccessToUnderlyingConnectionAllowed ( this.isAccessToUnderlyingConnectionAllowed() );
        } catch ( ClassCircularityError e ) {
            if ( this.connectionProperties != null ) {
                pci = new PooledConnectionImpl ( DriverManager.getConnection ( this.getUrl(), this.connectionProperties ) );
            } else {
                pci = new PooledConnectionImpl ( DriverManager.getConnection ( this.getUrl(), username, pass ) );
            }
            pci.setAccessToUnderlyingConnectionAllowed ( this.isAccessToUnderlyingConnectionAllowed() );
        }
        KeyedObjectPool<PStmtKeyCPDS, PoolablePreparedStatement<PStmtKeyCPDS>> stmtPool = null;
        if ( this.isPoolPreparedStatements() ) {
            final GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
            config.setMaxTotalPerKey ( Integer.MAX_VALUE );
            config.setBlockWhenExhausted ( false );
            config.setMaxWaitMillis ( 0L );
            config.setMaxIdlePerKey ( this.getMaxIdle() );
            if ( this.getMaxPreparedStatements() <= 0 ) {
                config.setTimeBetweenEvictionRunsMillis ( this.getTimeBetweenEvictionRunsMillis() );
                config.setNumTestsPerEvictionRun ( this.getNumTestsPerEvictionRun() );
                config.setMinEvictableIdleTimeMillis ( this.getMinEvictableIdleTimeMillis() );
            } else {
                config.setMaxTotal ( this.getMaxPreparedStatements() );
                config.setTimeBetweenEvictionRunsMillis ( -1L );
                config.setNumTestsPerEvictionRun ( 0 );
                config.setMinEvictableIdleTimeMillis ( 0L );
            }
            stmtPool = new GenericKeyedObjectPool<PStmtKeyCPDS, PoolablePreparedStatement<PStmtKeyCPDS>> ( pci, config );
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
        final String factory = this.getClass().getName();
        final Reference ref = new Reference ( this.getClass().getName(), factory, null );
        ref.add ( new StringRefAddr ( "description", this.getDescription() ) );
        ref.add ( new StringRefAddr ( "driver", this.getDriver() ) );
        ref.add ( new StringRefAddr ( "loginTimeout", String.valueOf ( this.getLoginTimeout() ) ) );
        ref.add ( new StringRefAddr ( "password", this.getPassword() ) );
        ref.add ( new StringRefAddr ( "user", this.getUser() ) );
        ref.add ( new StringRefAddr ( "url", this.getUrl() ) );
        ref.add ( new StringRefAddr ( "poolPreparedStatements", String.valueOf ( this.isPoolPreparedStatements() ) ) );
        ref.add ( new StringRefAddr ( "maxIdle", String.valueOf ( this.getMaxIdle() ) ) );
        ref.add ( new StringRefAddr ( "timeBetweenEvictionRunsMillis", String.valueOf ( this.getTimeBetweenEvictionRunsMillis() ) ) );
        ref.add ( new StringRefAddr ( "numTestsPerEvictionRun", String.valueOf ( this.getNumTestsPerEvictionRun() ) ) );
        ref.add ( new StringRefAddr ( "minEvictableIdleTimeMillis", String.valueOf ( this.getMinEvictableIdleTimeMillis() ) ) );
        ref.add ( new StringRefAddr ( "maxPreparedStatements", String.valueOf ( this.getMaxPreparedStatements() ) ) );
        return ref;
    }
    @Override
    public Object getObjectInstance ( final Object refObj, final Name name, final Context context, final Hashtable<?, ?> env ) throws Exception {
        DriverAdapterCPDS cpds = null;
        if ( refObj instanceof Reference ) {
            final Reference ref = ( Reference ) refObj;
            if ( ref.getClassName().equals ( this.getClass().getName() ) ) {
                RefAddr ra = ref.get ( "description" );
                if ( ra != null && ra.getContent() != null ) {
                    this.setDescription ( ra.getContent().toString() );
                }
                ra = ref.get ( "driver" );
                if ( ra != null && ra.getContent() != null ) {
                    this.setDriver ( ra.getContent().toString() );
                }
                ra = ref.get ( "url" );
                if ( ra != null && ra.getContent() != null ) {
                    this.setUrl ( ra.getContent().toString() );
                }
                ra = ref.get ( "user" );
                if ( ra != null && ra.getContent() != null ) {
                    this.setUser ( ra.getContent().toString() );
                }
                ra = ref.get ( "password" );
                if ( ra != null && ra.getContent() != null ) {
                    this.setPassword ( ra.getContent().toString() );
                }
                ra = ref.get ( "poolPreparedStatements" );
                if ( ra != null && ra.getContent() != null ) {
                    this.setPoolPreparedStatements ( Boolean.valueOf ( ra.getContent().toString() ) );
                }
                ra = ref.get ( "maxIdle" );
                if ( ra != null && ra.getContent() != null ) {
                    this.setMaxIdle ( Integer.parseInt ( ra.getContent().toString() ) );
                }
                ra = ref.get ( "timeBetweenEvictionRunsMillis" );
                if ( ra != null && ra.getContent() != null ) {
                    this.setTimeBetweenEvictionRunsMillis ( Integer.parseInt ( ra.getContent().toString() ) );
                }
                ra = ref.get ( "numTestsPerEvictionRun" );
                if ( ra != null && ra.getContent() != null ) {
                    this.setNumTestsPerEvictionRun ( Integer.parseInt ( ra.getContent().toString() ) );
                }
                ra = ref.get ( "minEvictableIdleTimeMillis" );
                if ( ra != null && ra.getContent() != null ) {
                    this.setMinEvictableIdleTimeMillis ( Integer.parseInt ( ra.getContent().toString() ) );
                }
                ra = ref.get ( "maxPreparedStatements" );
                if ( ra != null && ra.getContent() != null ) {
                    this.setMaxPreparedStatements ( Integer.parseInt ( ra.getContent().toString() ) );
                }
                ra = ref.get ( "accessToUnderlyingConnectionAllowed" );
                if ( ra != null && ra.getContent() != null ) {
                    this.setAccessToUnderlyingConnectionAllowed ( Boolean.valueOf ( ra.getContent().toString() ) );
                }
                cpds = this;
            }
        }
        return cpds;
    }
    private void assertInitializationAllowed() throws IllegalStateException {
        if ( this.getConnectionCalled ) {
            throw new IllegalStateException ( "A PooledConnection was already requested from this source, further initialization is not allowed." );
        }
    }
    public Properties getConnectionProperties() {
        return this.connectionProperties;
    }
    public void setConnectionProperties ( final Properties props ) {
        this.assertInitializationAllowed();
        this.connectionProperties = props;
        if ( this.connectionProperties.containsKey ( "user" ) ) {
            this.setUser ( this.connectionProperties.getProperty ( "user" ) );
        }
        if ( this.connectionProperties.containsKey ( "password" ) ) {
            this.setPassword ( this.connectionProperties.getProperty ( "password" ) );
        }
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription ( final String v ) {
        this.description = v;
    }
    public String getPassword() {
        return this.password;
    }
    public void setPassword ( final String v ) {
        this.assertInitializationAllowed();
        this.password = v;
        if ( this.connectionProperties != null ) {
            this.connectionProperties.setProperty ( "password", v );
        }
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl ( final String v ) {
        this.assertInitializationAllowed();
        this.url = v;
    }
    public String getUser() {
        return this.user;
    }
    public void setUser ( final String v ) {
        this.assertInitializationAllowed();
        this.user = v;
        if ( this.connectionProperties != null ) {
            this.connectionProperties.setProperty ( "user", v );
        }
    }
    public String getDriver() {
        return this.driver;
    }
    public void setDriver ( final String v ) throws ClassNotFoundException {
        this.assertInitializationAllowed();
        Class.forName ( this.driver = v );
    }
    @Override
    public int getLoginTimeout() {
        return this.loginTimeout;
    }
    @Override
    public PrintWriter getLogWriter() {
        return this.logWriter;
    }
    @Override
    public void setLoginTimeout ( final int seconds ) {
        this.loginTimeout = seconds;
    }
    @Override
    public void setLogWriter ( final PrintWriter out ) {
        this.logWriter = out;
    }
    public boolean isPoolPreparedStatements() {
        return this.poolPreparedStatements;
    }
    public void setPoolPreparedStatements ( final boolean v ) {
        this.assertInitializationAllowed();
        this.poolPreparedStatements = v;
    }
    public int getMaxIdle() {
        return this.maxIdle;
    }
    public void setMaxIdle ( final int maxIdle ) {
        this.assertInitializationAllowed();
        this.maxIdle = maxIdle;
    }
    public long getTimeBetweenEvictionRunsMillis() {
        return this._timeBetweenEvictionRunsMillis;
    }
    public void setTimeBetweenEvictionRunsMillis ( final long timeBetweenEvictionRunsMillis ) {
        this.assertInitializationAllowed();
        this._timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }
    public int getNumTestsPerEvictionRun() {
        return this._numTestsPerEvictionRun;
    }
    public void setNumTestsPerEvictionRun ( final int numTestsPerEvictionRun ) {
        this.assertInitializationAllowed();
        this._numTestsPerEvictionRun = numTestsPerEvictionRun;
    }
    public int getMinEvictableIdleTimeMillis() {
        return this._minEvictableIdleTimeMillis;
    }
    public void setMinEvictableIdleTimeMillis ( final int minEvictableIdleTimeMillis ) {
        this.assertInitializationAllowed();
        this._minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }
    public synchronized boolean isAccessToUnderlyingConnectionAllowed() {
        return this.accessToUnderlyingConnectionAllowed;
    }
    public synchronized void setAccessToUnderlyingConnectionAllowed ( final boolean allow ) {
        this.accessToUnderlyingConnectionAllowed = allow;
    }
    public int getMaxPreparedStatements() {
        return this._maxPreparedStatements;
    }
    public void setMaxPreparedStatements ( final int maxPreparedStatements ) {
        this._maxPreparedStatements = maxPreparedStatements;
    }
    static {
        DriverManager.getDrivers();
    }
}
