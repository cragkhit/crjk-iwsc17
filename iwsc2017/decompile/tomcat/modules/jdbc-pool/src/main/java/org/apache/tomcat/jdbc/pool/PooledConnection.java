// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import org.apache.juli.logging.LogFactory;
import java.sql.Statement;
import java.util.Properties;
import java.sql.DriverManager;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;
import java.sql.SQLException;
import java.sql.Driver;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.sql.XAConnection;
import java.sql.Connection;
import org.apache.juli.logging.Log;

public class PooledConnection
{
    private static final Log log;
    public static final String PROP_USER = "user";
    public static final String PROP_PASSWORD = "password";
    public static final int VALIDATE_BORROW = 1;
    public static final int VALIDATE_RETURN = 2;
    public static final int VALIDATE_IDLE = 3;
    public static final int VALIDATE_INIT = 4;
    protected PoolConfiguration poolProperties;
    private volatile Connection connection;
    protected volatile XAConnection xaConnection;
    private String abandonTrace;
    private volatile long timestamp;
    private final ReentrantReadWriteLock lock;
    private volatile boolean discarded;
    private volatile long lastConnected;
    private volatile long lastValidated;
    protected ConnectionPool parent;
    private HashMap<Object, Object> attributes;
    private volatile long connectionVersion;
    private volatile JdbcInterceptor handler;
    private AtomicBoolean released;
    private volatile boolean suspect;
    private Driver driver;
    
    public PooledConnection(final PoolConfiguration prop, final ConnectionPool parent) {
        this.abandonTrace = null;
        this.lock = new ReentrantReadWriteLock(false);
        this.discarded = false;
        this.lastConnected = -1L;
        this.lastValidated = System.currentTimeMillis();
        this.attributes = new HashMap<Object, Object>();
        this.connectionVersion = 0L;
        this.handler = null;
        this.released = new AtomicBoolean(false);
        this.suspect = false;
        this.driver = null;
        this.poolProperties = prop;
        this.parent = parent;
        this.connectionVersion = parent.getPoolVersion();
    }
    
    public long getConnectionVersion() {
        return this.connectionVersion;
    }
    
    @Deprecated
    public boolean checkUser(final String username, final String password) {
        return !this.shouldForceReconnect(username, password);
    }
    
    public boolean shouldForceReconnect(String username, String password) {
        if (!this.getPoolProperties().isAlternateUsernameAllowed()) {
            return false;
        }
        if (username == null) {
            username = this.poolProperties.getUsername();
        }
        if (password == null) {
            password = this.poolProperties.getPassword();
        }
        final String storedUsr = this.getAttributes().get("user");
        final String storedPwd = this.getAttributes().get("password");
        boolean noChangeInCredentials = username == null && storedUsr == null;
        noChangeInCredentials = (noChangeInCredentials || (username != null && username.equals(storedUsr)));
        noChangeInCredentials = (noChangeInCredentials && ((password == null && storedPwd == null) || (password != null && password.equals(storedPwd))));
        if (username == null) {
            this.getAttributes().remove("user");
        }
        else {
            this.getAttributes().put("user", username);
        }
        if (password == null) {
            this.getAttributes().remove("password");
        }
        else {
            this.getAttributes().put("password", password);
        }
        return !noChangeInCredentials;
    }
    
    public void connect() throws SQLException {
        if (this.released.get()) {
            throw new SQLException("A connection once released, can't be reestablished.");
        }
        if (this.connection != null) {
            try {
                this.disconnect(false);
            }
            catch (Exception x) {
                PooledConnection.log.debug((Object)"Unable to disconnect previous connection.", (Throwable)x);
            }
        }
        while (true) {
            if (this.poolProperties.getDataSource() != null || this.poolProperties.getDataSourceJNDI() != null) {
                if (this.poolProperties.getDataSource() != null) {
                    this.connectUsingDataSource();
                }
                else {
                    this.connectUsingDriver();
                }
                if (this.poolProperties.getJdbcInterceptors() == null || this.poolProperties.getJdbcInterceptors().indexOf(ConnectionState.class.getName()) < 0 || this.poolProperties.getJdbcInterceptors().indexOf(ConnectionState.class.getSimpleName()) < 0) {
                    if (this.poolProperties.getDefaultTransactionIsolation() != -1) {
                        this.connection.setTransactionIsolation(this.poolProperties.getDefaultTransactionIsolation());
                    }
                    if (this.poolProperties.getDefaultReadOnly() != null) {
                        this.connection.setReadOnly(this.poolProperties.getDefaultReadOnly());
                    }
                    if (this.poolProperties.getDefaultAutoCommit() != null) {
                        this.connection.setAutoCommit(this.poolProperties.getDefaultAutoCommit());
                    }
                    if (this.poolProperties.getDefaultCatalog() != null) {
                        this.connection.setCatalog(this.poolProperties.getDefaultCatalog());
                    }
                }
                this.discarded = false;
                this.lastConnected = System.currentTimeMillis();
                return;
            }
            continue;
        }
    }
    
    protected void connectUsingDataSource() throws SQLException {
        String usr = null;
        String pwd = null;
        if (this.getAttributes().containsKey("user")) {
            usr = this.getAttributes().get("user");
        }
        else {
            usr = this.poolProperties.getUsername();
            this.getAttributes().put("user", usr);
        }
        if (this.getAttributes().containsKey("password")) {
            pwd = this.getAttributes().get("password");
        }
        else {
            pwd = this.poolProperties.getPassword();
            this.getAttributes().put("password", pwd);
        }
        if (this.poolProperties.getDataSource() instanceof XADataSource) {
            final XADataSource xds = (XADataSource)this.poolProperties.getDataSource();
            if (usr != null && pwd != null) {
                this.xaConnection = xds.getXAConnection(usr, pwd);
                this.connection = this.xaConnection.getConnection();
            }
            else {
                this.xaConnection = xds.getXAConnection();
                this.connection = this.xaConnection.getConnection();
            }
        }
        else if (this.poolProperties.getDataSource() instanceof DataSource) {
            final DataSource ds = (DataSource)this.poolProperties.getDataSource();
            if (usr != null && pwd != null) {
                this.connection = ds.getConnection(usr, pwd);
            }
            else {
                this.connection = ds.getConnection();
            }
        }
        else {
            if (!(this.poolProperties.getDataSource() instanceof ConnectionPoolDataSource)) {
                throw new SQLException("DataSource is of unknown class:" + ((this.poolProperties.getDataSource() != null) ? this.poolProperties.getDataSource().getClass() : "null"));
            }
            final ConnectionPoolDataSource ds2 = (ConnectionPoolDataSource)this.poolProperties.getDataSource();
            if (usr != null && pwd != null) {
                this.connection = ds2.getPooledConnection(usr, pwd).getConnection();
            }
            else {
                this.connection = ds2.getPooledConnection().getConnection();
            }
        }
    }
    
    protected void connectUsingDriver() throws SQLException {
        try {
            if (this.driver == null) {
                if (PooledConnection.log.isDebugEnabled()) {
                    PooledConnection.log.debug((Object)("Instantiating driver using class: " + this.poolProperties.getDriverClassName() + " [url=" + this.poolProperties.getUrl() + "]"));
                }
                if (this.poolProperties.getDriverClassName() == null) {
                    PooledConnection.log.warn((Object)"Not loading a JDBC driver as driverClassName property is null.");
                }
                else {
                    this.driver = (Driver)ClassLoaderUtil.loadClass(this.poolProperties.getDriverClassName(), PooledConnection.class.getClassLoader(), Thread.currentThread().getContextClassLoader()).newInstance();
                }
            }
        }
        catch (Exception cn) {
            if (PooledConnection.log.isDebugEnabled()) {
                PooledConnection.log.debug((Object)"Unable to instantiate JDBC driver.", (Throwable)cn);
            }
            final SQLException ex = new SQLException(cn.getMessage());
            ex.initCause(cn);
            throw ex;
        }
        final String driverURL = this.poolProperties.getUrl();
        String usr = null;
        String pwd = null;
        if (this.getAttributes().containsKey("user")) {
            usr = this.getAttributes().get("user");
        }
        else {
            usr = this.poolProperties.getUsername();
            this.getAttributes().put("user", usr);
        }
        if (this.getAttributes().containsKey("password")) {
            pwd = this.getAttributes().get("password");
        }
        else {
            pwd = this.poolProperties.getPassword();
            this.getAttributes().put("password", pwd);
        }
        final Properties properties = PoolUtilities.clone(this.poolProperties.getDbProperties());
        if (usr != null) {
            properties.setProperty("user", usr);
        }
        if (pwd != null) {
            properties.setProperty("password", pwd);
        }
        try {
            if (this.driver == null) {
                this.connection = DriverManager.getConnection(driverURL, properties);
            }
            else {
                this.connection = this.driver.connect(driverURL, properties);
            }
        }
        catch (Exception x) {
            if (PooledConnection.log.isDebugEnabled()) {
                PooledConnection.log.debug((Object)"Unable to connect to database.", (Throwable)x);
            }
            if (this.parent.jmxPool != null) {
                this.parent.jmxPool.notify("CONNECTION FAILED", ConnectionPool.getStackTrace(x));
            }
            if (x instanceof SQLException) {
                throw (SQLException)x;
            }
            final SQLException ex2 = new SQLException(x.getMessage());
            ex2.initCause(x);
            throw ex2;
        }
        if (this.connection == null) {
            throw new SQLException("Driver:" + this.driver + " returned null for URL:" + driverURL);
        }
    }
    
    public boolean isInitialized() {
        return this.connection != null;
    }
    
    public boolean isMaxAgeExpired() {
        return this.getPoolProperties().getMaxAge() > 0L && System.currentTimeMillis() - this.getLastConnected() > this.getPoolProperties().getMaxAge();
    }
    
    public void reconnect() throws SQLException {
        this.disconnect(false);
        this.connect();
    }
    
    private void disconnect(final boolean finalize) {
        if (this.isDiscarded() && this.connection == null) {
            return;
        }
        this.setDiscarded(true);
        if (this.connection != null) {
            try {
                this.parent.disconnectEvent(this, finalize);
                if (this.xaConnection == null) {
                    this.connection.close();
                }
                else {
                    this.xaConnection.close();
                }
            }
            catch (Exception ignore) {
                if (PooledConnection.log.isDebugEnabled()) {
                    PooledConnection.log.debug((Object)"Unable to close underlying SQL connection", (Throwable)ignore);
                }
            }
        }
        this.connection = null;
        this.xaConnection = null;
        this.lastConnected = -1L;
        if (finalize) {
            this.parent.finalize(this);
        }
    }
    
    public long getAbandonTimeout() {
        if (this.poolProperties.getRemoveAbandonedTimeout() <= 0) {
            return Long.MAX_VALUE;
        }
        return this.poolProperties.getRemoveAbandonedTimeout() * 1000L;
    }
    
    private boolean doValidate(final int action) {
        return (action == 1 && this.poolProperties.isTestOnBorrow()) || (action == 2 && this.poolProperties.isTestOnReturn()) || (action == 3 && this.poolProperties.isTestWhileIdle()) || (action == 4 && this.poolProperties.isTestOnConnect()) || (action == 4 && this.poolProperties.getInitSQL() != null);
    }
    
    public boolean validate(final int validateAction) {
        return this.validate(validateAction, null);
    }
    
    public boolean validate(final int validateAction, final String sql) {
        if (this.isDiscarded()) {
            return false;
        }
        if (!this.doValidate(validateAction)) {
            return true;
        }
        final long now = System.currentTimeMillis();
        if (validateAction != 4 && this.poolProperties.getValidationInterval() > 0L && now - this.lastValidated < this.poolProperties.getValidationInterval()) {
            return true;
        }
        if (this.poolProperties.getValidator() != null) {
            if (this.poolProperties.getValidator().validate(this.connection, validateAction)) {
                this.lastValidated = now;
                return true;
            }
            if (this.getPoolProperties().getLogValidationErrors()) {
                PooledConnection.log.error((Object)("Custom validation through " + this.poolProperties.getValidator() + " failed."));
            }
            return false;
        }
        else {
            String query = sql;
            if (validateAction == 4 && this.poolProperties.getInitSQL() != null) {
                query = this.poolProperties.getInitSQL();
            }
            if (query == null) {
                query = this.poolProperties.getValidationQuery();
            }
            if (query == null) {
                int validationQueryTimeout = this.poolProperties.getValidationQueryTimeout();
                if (validationQueryTimeout < 0) {
                    validationQueryTimeout = 0;
                }
                try {
                    if (this.connection.isValid(validationQueryTimeout)) {
                        this.lastValidated = now;
                        return true;
                    }
                    if (this.getPoolProperties().getLogValidationErrors()) {
                        PooledConnection.log.error((Object)"isValid() returned false.");
                    }
                    return false;
                }
                catch (SQLException e) {
                    if (this.getPoolProperties().getLogValidationErrors()) {
                        PooledConnection.log.error((Object)"isValid() failed.", (Throwable)e);
                    }
                    else if (PooledConnection.log.isDebugEnabled()) {
                        PooledConnection.log.debug((Object)"isValid() failed.", (Throwable)e);
                    }
                    return false;
                }
            }
            Statement stmt = null;
            try {
                stmt = this.connection.createStatement();
                final int validationQueryTimeout2 = this.poolProperties.getValidationQueryTimeout();
                if (validationQueryTimeout2 > 0) {
                    stmt.setQueryTimeout(validationQueryTimeout2);
                }
                stmt.execute(query);
                stmt.close();
                this.lastValidated = now;
                return true;
            }
            catch (Exception ex) {
                if (this.getPoolProperties().getLogValidationErrors()) {
                    PooledConnection.log.warn((Object)"SQL Validation error", (Throwable)ex);
                }
                else if (PooledConnection.log.isDebugEnabled()) {
                    PooledConnection.log.debug((Object)"Unable to validate object:", (Throwable)ex);
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    }
                    catch (Exception ex2) {}
                }
                return false;
            }
        }
    }
    
    public long getReleaseTime() {
        return this.poolProperties.getMinEvictableIdleTimeMillis();
    }
    
    public boolean release() {
        try {
            this.disconnect(true);
        }
        catch (Exception x) {
            if (PooledConnection.log.isDebugEnabled()) {
                PooledConnection.log.debug((Object)"Unable to close SQL connection", (Throwable)x);
            }
        }
        return this.released.compareAndSet(false, true);
    }
    
    public void setStackTrace(final String trace) {
        this.abandonTrace = trace;
    }
    
    public String getStackTrace() {
        return this.abandonTrace;
    }
    
    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
        this.setSuspect(false);
    }
    
    public boolean isSuspect() {
        return this.suspect;
    }
    
    public void setSuspect(final boolean suspect) {
        this.suspect = suspect;
    }
    
    public void setDiscarded(final boolean discarded) {
        if (this.discarded && !discarded) {
            throw new IllegalStateException("Unable to change the state once the connection has been discarded");
        }
        this.discarded = discarded;
    }
    
    public void setLastValidated(final long lastValidated) {
        this.lastValidated = lastValidated;
    }
    
    public void setPoolProperties(final PoolConfiguration poolProperties) {
        this.poolProperties = poolProperties;
    }
    
    public long getTimestamp() {
        return this.timestamp;
    }
    
    public boolean isDiscarded() {
        return this.discarded;
    }
    
    public long getLastValidated() {
        return this.lastValidated;
    }
    
    public PoolConfiguration getPoolProperties() {
        return this.poolProperties;
    }
    
    public void lock() {
        if (this.poolProperties.getUseLock() || this.poolProperties.isPoolSweeperEnabled()) {
            this.lock.writeLock().lock();
        }
    }
    
    public void unlock() {
        if (this.poolProperties.getUseLock() || this.poolProperties.isPoolSweeperEnabled()) {
            this.lock.writeLock().unlock();
        }
    }
    
    public Connection getConnection() {
        return this.connection;
    }
    
    public XAConnection getXAConnection() {
        return this.xaConnection;
    }
    
    public long getLastConnected() {
        return this.lastConnected;
    }
    
    public JdbcInterceptor getHandler() {
        return this.handler;
    }
    
    public void setHandler(final JdbcInterceptor handler) {
        if (this.handler != null && this.handler != handler) {
            for (JdbcInterceptor interceptor = this.handler; interceptor != null; interceptor = interceptor.getNext()) {
                interceptor.reset(null, null);
            }
        }
        this.handler = handler;
    }
    
    @Override
    public String toString() {
        return "PooledConnection[" + ((this.connection != null) ? this.connection.toString() : "null") + "]";
    }
    
    public boolean isReleased() {
        return this.released.get();
    }
    
    public HashMap<Object, Object> getAttributes() {
        return this.attributes;
    }
    
    static {
        log = LogFactory.getLog((Class)PooledConnection.class);
    }
}
