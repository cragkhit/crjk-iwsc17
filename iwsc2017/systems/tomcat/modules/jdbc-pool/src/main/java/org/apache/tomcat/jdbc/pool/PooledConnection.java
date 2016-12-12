package org.apache.tomcat.jdbc.pool;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;
public class PooledConnection {
    private static final Log log = LogFactory.getLog ( PooledConnection.class );
    public static final String PROP_USER = PoolUtilities.PROP_USER;
    public static final String PROP_PASSWORD = PoolUtilities.PROP_PASSWORD;
    public static final int VALIDATE_BORROW = 1;
    public static final int VALIDATE_RETURN = 2;
    public static final int VALIDATE_IDLE = 3;
    public static final int VALIDATE_INIT = 4;
    protected PoolConfiguration poolProperties;
    private volatile java.sql.Connection connection;
    protected volatile javax.sql.XAConnection xaConnection;
    private String abandonTrace = null;
    private volatile long timestamp;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock ( false );
    private volatile boolean discarded = false;
    private volatile long lastConnected = -1;
    private volatile long lastValidated = System.currentTimeMillis();
    protected ConnectionPool parent;
    private HashMap<Object, Object> attributes = new HashMap<>();
    private volatile long connectionVersion = 0;
    private volatile JdbcInterceptor handler = null;
    private AtomicBoolean released = new AtomicBoolean ( false );
    private volatile boolean suspect = false;
    private java.sql.Driver driver = null;
    public PooledConnection ( PoolConfiguration prop, ConnectionPool parent ) {
        poolProperties = prop;
        this.parent = parent;
        connectionVersion = parent.getPoolVersion();
    }
    public long getConnectionVersion() {
        return connectionVersion;
    }
    @Deprecated
    public boolean checkUser ( String username, String password ) {
        return !shouldForceReconnect ( username, password );
    }
    public boolean shouldForceReconnect ( String username, String password ) {
        if ( !getPoolProperties().isAlternateUsernameAllowed() ) {
            return false;
        }
        if ( username == null ) {
            username = poolProperties.getUsername();
        }
        if ( password == null ) {
            password = poolProperties.getPassword();
        }
        String storedUsr = ( String ) getAttributes().get ( PROP_USER );
        String storedPwd = ( String ) getAttributes().get ( PROP_PASSWORD );
        boolean noChangeInCredentials = ( username == null && storedUsr == null );
        noChangeInCredentials = ( noChangeInCredentials || ( username != null && username.equals ( storedUsr ) ) );
        noChangeInCredentials = noChangeInCredentials && ( ( password == null && storedPwd == null ) || ( password != null && password.equals ( storedPwd ) ) );
        if ( username == null ) {
            getAttributes().remove ( PROP_USER );
        } else {
            getAttributes().put ( PROP_USER, username );
        }
        if ( password == null ) {
            getAttributes().remove ( PROP_PASSWORD );
        } else {
            getAttributes().put ( PROP_PASSWORD, password );
        }
        return !noChangeInCredentials;
    }
    public void connect() throws SQLException {
        if ( released.get() ) {
            throw new SQLException ( "A connection once released, can't be reestablished." );
        }
        if ( connection != null ) {
            try {
                this.disconnect ( false );
            } catch ( Exception x ) {
                log.debug ( "Unable to disconnect previous connection.", x );
            }
        }
        if ( poolProperties.getDataSource() == null && poolProperties.getDataSourceJNDI() != null ) {
        }
        if ( poolProperties.getDataSource() != null ) {
            connectUsingDataSource();
        } else {
            connectUsingDriver();
        }
        if ( poolProperties.getJdbcInterceptors() == null || poolProperties.getJdbcInterceptors().indexOf ( ConnectionState.class.getName() ) < 0 ||
                poolProperties.getJdbcInterceptors().indexOf ( ConnectionState.class.getSimpleName() ) < 0 ) {
            if ( poolProperties.getDefaultTransactionIsolation() != DataSourceFactory.UNKNOWN_TRANSACTIONISOLATION ) {
                connection.setTransactionIsolation ( poolProperties.getDefaultTransactionIsolation() );
            }
            if ( poolProperties.getDefaultReadOnly() != null ) {
                connection.setReadOnly ( poolProperties.getDefaultReadOnly().booleanValue() );
            }
            if ( poolProperties.getDefaultAutoCommit() != null ) {
                connection.setAutoCommit ( poolProperties.getDefaultAutoCommit().booleanValue() );
            }
            if ( poolProperties.getDefaultCatalog() != null ) {
                connection.setCatalog ( poolProperties.getDefaultCatalog() );
            }
        }
        this.discarded = false;
        this.lastConnected = System.currentTimeMillis();
    }
    protected void connectUsingDataSource() throws SQLException {
        String usr = null;
        String pwd = null;
        if ( getAttributes().containsKey ( PROP_USER ) ) {
            usr = ( String ) getAttributes().get ( PROP_USER );
        } else {
            usr = poolProperties.getUsername();
            getAttributes().put ( PROP_USER, usr );
        }
        if ( getAttributes().containsKey ( PROP_PASSWORD ) ) {
            pwd = ( String ) getAttributes().get ( PROP_PASSWORD );
        } else {
            pwd = poolProperties.getPassword();
            getAttributes().put ( PROP_PASSWORD, pwd );
        }
        if ( poolProperties.getDataSource() instanceof javax.sql.XADataSource ) {
            javax.sql.XADataSource xds = ( javax.sql.XADataSource ) poolProperties.getDataSource();
            if ( usr != null && pwd != null ) {
                xaConnection = xds.getXAConnection ( usr, pwd );
                connection = xaConnection.getConnection();
            } else {
                xaConnection = xds.getXAConnection();
                connection = xaConnection.getConnection();
            }
        } else if ( poolProperties.getDataSource() instanceof javax.sql.DataSource ) {
            javax.sql.DataSource ds = ( javax.sql.DataSource ) poolProperties.getDataSource();
            if ( usr != null && pwd != null ) {
                connection = ds.getConnection ( usr, pwd );
            } else {
                connection = ds.getConnection();
            }
        } else if ( poolProperties.getDataSource() instanceof javax.sql.ConnectionPoolDataSource ) {
            javax.sql.ConnectionPoolDataSource ds = ( javax.sql.ConnectionPoolDataSource ) poolProperties.getDataSource();
            if ( usr != null && pwd != null ) {
                connection = ds.getPooledConnection ( usr, pwd ).getConnection();
            } else {
                connection = ds.getPooledConnection().getConnection();
            }
        } else {
            throw new SQLException ( "DataSource is of unknown class:" + ( poolProperties.getDataSource() != null ? poolProperties.getDataSource().getClass() : "null" ) );
        }
    }
    protected void connectUsingDriver() throws SQLException {
        try {
            if ( driver == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Instantiating driver using class: " + poolProperties.getDriverClassName() + " [url=" + poolProperties.getUrl() + "]" );
                }
                if ( poolProperties.getDriverClassName() == null ) {
                    log.warn ( "Not loading a JDBC driver as driverClassName property is null." );
                } else {
                    driver = ( java.sql.Driver )
                             ClassLoaderUtil.loadClass (
                                 poolProperties.getDriverClassName(),
                                 PooledConnection.class.getClassLoader(),
                                 Thread.currentThread().getContextClassLoader()
                             ).newInstance();
                }
            }
        } catch ( java.lang.Exception cn ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "Unable to instantiate JDBC driver.", cn );
            }
            SQLException ex = new SQLException ( cn.getMessage() );
            ex.initCause ( cn );
            throw ex;
        }
        String driverURL = poolProperties.getUrl();
        String usr = null;
        String pwd = null;
        if ( getAttributes().containsKey ( PROP_USER ) ) {
            usr = ( String ) getAttributes().get ( PROP_USER );
        } else {
            usr = poolProperties.getUsername();
            getAttributes().put ( PROP_USER, usr );
        }
        if ( getAttributes().containsKey ( PROP_PASSWORD ) ) {
            pwd = ( String ) getAttributes().get ( PROP_PASSWORD );
        } else {
            pwd = poolProperties.getPassword();
            getAttributes().put ( PROP_PASSWORD, pwd );
        }
        Properties properties = PoolUtilities.clone ( poolProperties.getDbProperties() );
        if ( usr != null ) {
            properties.setProperty ( PROP_USER, usr );
        }
        if ( pwd != null ) {
            properties.setProperty ( PROP_PASSWORD, pwd );
        }
        try {
            if ( driver == null ) {
                connection = DriverManager.getConnection ( driverURL, properties );
            } else {
                connection = driver.connect ( driverURL, properties );
            }
        } catch ( Exception x ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "Unable to connect to database.", x );
            }
            if ( parent.jmxPool != null ) {
                parent.jmxPool.notify ( org.apache.tomcat.jdbc.pool.jmx.ConnectionPool.NOTIFY_CONNECT,
                                        ConnectionPool.getStackTrace ( x ) );
            }
            if ( x instanceof SQLException ) {
                throw ( SQLException ) x;
            } else {
                SQLException ex = new SQLException ( x.getMessage() );
                ex.initCause ( x );
                throw ex;
            }
        }
        if ( connection == null ) {
            throw new SQLException ( "Driver:" + driver + " returned null for URL:" + driverURL );
        }
    }
    public boolean isInitialized() {
        return connection != null;
    }
    public boolean isMaxAgeExpired() {
        if ( getPoolProperties().getMaxAge() > 0 ) {
            return ( System.currentTimeMillis() - getLastConnected() ) > getPoolProperties().getMaxAge();
        } else {
            return false;
        }
    }
    public void reconnect() throws SQLException {
        this.disconnect ( false );
        this.connect();
    }
    private void disconnect ( boolean finalize ) {
        if ( isDiscarded() && connection == null ) {
            return;
        }
        setDiscarded ( true );
        if ( connection != null ) {
            try {
                parent.disconnectEvent ( this, finalize );
                if ( xaConnection == null ) {
                    connection.close();
                } else {
                    xaConnection.close();
                }
            } catch ( Exception ignore ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Unable to close underlying SQL connection", ignore );
                }
            }
        }
        connection = null;
        xaConnection = null;
        lastConnected = -1;
        if ( finalize ) {
            parent.finalize ( this );
        }
    }
    public long getAbandonTimeout() {
        if ( poolProperties.getRemoveAbandonedTimeout() <= 0 ) {
            return Long.MAX_VALUE;
        } else {
            return poolProperties.getRemoveAbandonedTimeout() * 1000L;
        }
    }
    private boolean doValidate ( int action ) {
        if ( action == PooledConnection.VALIDATE_BORROW &&
                poolProperties.isTestOnBorrow() ) {
            return true;
        } else if ( action == PooledConnection.VALIDATE_RETURN &&
                    poolProperties.isTestOnReturn() ) {
            return true;
        } else if ( action == PooledConnection.VALIDATE_IDLE &&
                    poolProperties.isTestWhileIdle() ) {
            return true;
        } else if ( action == PooledConnection.VALIDATE_INIT &&
                    poolProperties.isTestOnConnect() ) {
            return true;
        } else if ( action == PooledConnection.VALIDATE_INIT &&
                    poolProperties.getInitSQL() != null ) {
            return true;
        } else {
            return false;
        }
    }
    public boolean validate ( int validateAction ) {
        return validate ( validateAction, null );
    }
    public boolean validate ( int validateAction, String sql ) {
        if ( this.isDiscarded() ) {
            return false;
        }
        if ( !doValidate ( validateAction ) ) {
            return true;
        }
        long now = System.currentTimeMillis();
        if ( validateAction != VALIDATE_INIT &&
                poolProperties.getValidationInterval() > 0 &&
                ( now - this.lastValidated ) <
                poolProperties.getValidationInterval() ) {
            return true;
        }
        if ( poolProperties.getValidator() != null ) {
            if ( poolProperties.getValidator().validate ( connection, validateAction ) ) {
                this.lastValidated = now;
                return true;
            } else {
                if ( getPoolProperties().getLogValidationErrors() ) {
                    log.error ( "Custom validation through " + poolProperties.getValidator() + " failed." );
                }
                return false;
            }
        }
        String query = sql;
        if ( validateAction == VALIDATE_INIT && poolProperties.getInitSQL() != null ) {
            query = poolProperties.getInitSQL();
        }
        if ( query == null ) {
            query = poolProperties.getValidationQuery();
        }
        if ( query == null ) {
            int validationQueryTimeout = poolProperties.getValidationQueryTimeout();
            if ( validationQueryTimeout < 0 ) {
                validationQueryTimeout = 0;
            }
            try {
                if ( connection.isValid ( validationQueryTimeout ) ) {
                    this.lastValidated = now;
                    return true;
                } else {
                    if ( getPoolProperties().getLogValidationErrors() ) {
                        log.error ( "isValid() returned false." );
                    }
                    return false;
                }
            } catch ( SQLException e ) {
                if ( getPoolProperties().getLogValidationErrors() ) {
                    log.error ( "isValid() failed.", e );
                } else if ( log.isDebugEnabled() ) {
                    log.debug ( "isValid() failed.", e );
                }
                return false;
            }
        }
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            int validationQueryTimeout = poolProperties.getValidationQueryTimeout();
            if ( validationQueryTimeout > 0 ) {
                stmt.setQueryTimeout ( validationQueryTimeout );
            }
            stmt.execute ( query );
            stmt.close();
            this.lastValidated = now;
            return true;
        } catch ( Exception ex ) {
            if ( getPoolProperties().getLogValidationErrors() ) {
                log.warn ( "SQL Validation error", ex );
            } else if ( log.isDebugEnabled() ) {
                log.debug ( "Unable to validate object:", ex );
            }
            if ( stmt != null )
                try {
                    stmt.close();
                } catch ( Exception ignore2 ) { }
        }
        return false;
    }
    public long getReleaseTime() {
        return this.poolProperties.getMinEvictableIdleTimeMillis();
    }
    public boolean release() {
        try {
            disconnect ( true );
        } catch ( Exception x ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "Unable to close SQL connection", x );
            }
        }
        return released.compareAndSet ( false, true );
    }
    public void setStackTrace ( String trace ) {
        abandonTrace = trace;
    }
    public String getStackTrace() {
        return abandonTrace;
    }
    public void setTimestamp ( long timestamp ) {
        this.timestamp = timestamp;
        setSuspect ( false );
    }
    public boolean isSuspect() {
        return suspect;
    }
    public void setSuspect ( boolean suspect ) {
        this.suspect = suspect;
    }
    public void setDiscarded ( boolean discarded ) {
        if ( this.discarded && !discarded ) {
            throw new IllegalStateException ( "Unable to change the state once the connection has been discarded" );
        }
        this.discarded = discarded;
    }
    public void setLastValidated ( long lastValidated ) {
        this.lastValidated = lastValidated;
    }
    public void setPoolProperties ( PoolConfiguration poolProperties ) {
        this.poolProperties = poolProperties;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public boolean isDiscarded() {
        return discarded;
    }
    public long getLastValidated() {
        return lastValidated;
    }
    public PoolConfiguration getPoolProperties() {
        return poolProperties;
    }
    public void lock() {
        if ( poolProperties.getUseLock() || this.poolProperties.isPoolSweeperEnabled() ) {
            lock.writeLock().lock();
        }
    }
    public void unlock() {
        if ( poolProperties.getUseLock() || this.poolProperties.isPoolSweeperEnabled() ) {
            lock.writeLock().unlock();
        }
    }
    public java.sql.Connection getConnection() {
        return this.connection;
    }
    public javax.sql.XAConnection getXAConnection() {
        return this.xaConnection;
    }
    public long getLastConnected() {
        return lastConnected;
    }
    public JdbcInterceptor getHandler() {
        return handler;
    }
    public void setHandler ( JdbcInterceptor handler ) {
        if ( this.handler != null && this.handler != handler ) {
            JdbcInterceptor interceptor = this.handler;
            while ( interceptor != null ) {
                interceptor.reset ( null, null );
                interceptor = interceptor.getNext();
            }
        }
        this.handler = handler;
    }
    @Override
    public String toString() {
        return "PooledConnection[" + ( connection != null ? connection.toString() : "null" ) + "]";
    }
    public boolean isReleased() {
        return released.get();
    }
    public HashMap<Object, Object> getAttributes() {
        return attributes;
    }
}
