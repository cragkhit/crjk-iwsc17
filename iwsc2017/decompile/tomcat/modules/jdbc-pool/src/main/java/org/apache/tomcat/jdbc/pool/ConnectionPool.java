// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.juli.logging.LogFactory;
import java.util.Collections;
import java.util.Set;
import java.security.PrivilegedAction;
import java.util.TimerTask;
import java.security.AccessController;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.ConcurrentModificationException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import javax.sql.XAConnection;
import java.sql.Connection;
import java.util.concurrent.Future;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.HashSet;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.tomcat.jdbc.pool.jmx.ConnectionPool;
import java.util.concurrent.ThreadPoolExecutor;
import java.lang.reflect.Constructor;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.juli.logging.Log;

public class ConnectionPool
{
    public static final String POOL_JMX_DOMAIN = "tomcat.jdbc";
    public static final String POOL_JMX_TYPE_PREFIX = "tomcat.jdbc:type=";
    private static final Log log;
    private AtomicInteger size;
    private PoolConfiguration poolProperties;
    private BlockingQueue<PooledConnection> busy;
    private BlockingQueue<PooledConnection> idle;
    private volatile PoolCleaner poolCleaner;
    private volatile boolean closed;
    private Constructor<?> proxyClassConstructor;
    private ThreadPoolExecutor cancellator;
    protected org.apache.tomcat.jdbc.pool.jmx.ConnectionPool jmxPool;
    private AtomicInteger waitcount;
    private AtomicLong poolVersion;
    private static volatile Timer poolCleanTimer;
    private static HashSet<PoolCleaner> cleaners;
    
    public ConnectionPool(final PoolConfiguration prop) throws SQLException {
        this.size = new AtomicInteger(0);
        this.closed = false;
        this.cancellator = new ThreadPoolExecutor(0, 1, 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        this.jmxPool = null;
        this.waitcount = new AtomicInteger(0);
        this.poolVersion = new AtomicLong(Long.MIN_VALUE);
        this.init(prop);
    }
    
    public Future<Connection> getConnectionAsync() throws SQLException {
        try {
            final PooledConnection pc = this.borrowConnection(0, null, null);
            if (pc != null) {
                return new ConnectionFuture(pc);
            }
        }
        catch (SQLException x) {
            if (x.getMessage().indexOf("NoWait") < 0) {
                throw x;
            }
        }
        if (this.idle instanceof FairBlockingQueue) {
            final Future<PooledConnection> pcf = ((FairBlockingQueue)this.idle).pollAsync();
            return new ConnectionFuture(pcf);
        }
        if (this.idle instanceof MultiLockFairBlockingQueue) {
            final Future<PooledConnection> pcf = ((MultiLockFairBlockingQueue)this.idle).pollAsync();
            return new ConnectionFuture(pcf);
        }
        throw new SQLException("Connection pool is misconfigured, doesn't support async retrieval. Set the 'fair' property to 'true'");
    }
    
    public Connection getConnection() throws SQLException {
        final PooledConnection con = this.borrowConnection(-1, null, null);
        return this.setupConnection(con);
    }
    
    public Connection getConnection(final String username, final String password) throws SQLException {
        final PooledConnection con = this.borrowConnection(-1, username, password);
        return this.setupConnection(con);
    }
    
    public String getName() {
        return this.getPoolProperties().getPoolName();
    }
    
    public int getWaitCount() {
        return this.waitcount.get();
    }
    
    public PoolConfiguration getPoolProperties() {
        return this.poolProperties;
    }
    
    public int getSize() {
        return this.size.get();
    }
    
    public int getActive() {
        return this.busy.size();
    }
    
    public int getIdle() {
        return this.idle.size();
    }
    
    public boolean isClosed() {
        return this.closed;
    }
    
    protected Connection setupConnection(final PooledConnection con) throws SQLException {
        JdbcInterceptor handler = con.getHandler();
        if (handler == null) {
            handler = new ProxyConnection(this, con, this.getPoolProperties().isUseEquals());
            final PoolProperties.InterceptorDefinition[] proxies = this.getPoolProperties().getJdbcInterceptorsAsArray();
            for (int i = proxies.length - 1; i >= 0; --i) {
                try {
                    final JdbcInterceptor interceptor = (JdbcInterceptor)proxies[i].getInterceptorClass().newInstance();
                    interceptor.setProperties(proxies[i].getProperties());
                    interceptor.setNext(handler);
                    interceptor.reset(this, con);
                    handler = interceptor;
                }
                catch (Exception x) {
                    final SQLException sx = new SQLException("Unable to instantiate interceptor chain.");
                    sx.initCause(x);
                    throw sx;
                }
            }
            con.setHandler(handler);
        }
        else {
            for (JdbcInterceptor next = handler; next != null; next = next.getNext()) {
                next.reset(this, con);
            }
        }
        try {
            this.getProxyConstructor(con.getXAConnection() != null);
            Connection connection = null;
            if (this.getPoolProperties().getUseDisposableConnectionFacade()) {
                connection = (Connection)this.proxyClassConstructor.newInstance(new DisposableConnectionFacade(handler));
            }
            else {
                connection = (Connection)this.proxyClassConstructor.newInstance(handler);
            }
            return connection;
        }
        catch (Exception x2) {
            final SQLException s = new SQLException();
            s.initCause(x2);
            throw s;
        }
    }
    
    public Constructor<?> getProxyConstructor(final boolean xa) throws NoSuchMethodException {
        if (this.proxyClassConstructor == null) {
            final Class<?> proxyClass = xa ? Proxy.getProxyClass(ConnectionPool.class.getClassLoader(), Connection.class, javax.sql.PooledConnection.class, XAConnection.class) : Proxy.getProxyClass(ConnectionPool.class.getClassLoader(), Connection.class, javax.sql.PooledConnection.class);
            this.proxyClassConstructor = proxyClass.getConstructor(InvocationHandler.class);
        }
        return this.proxyClassConstructor;
    }
    
    protected void close(final boolean force) {
        if (this.closed) {
            return;
        }
        this.closed = true;
        if (this.poolCleaner != null) {
            this.poolCleaner.stopRunning();
        }
        for (BlockingQueue<PooledConnection> pool = (this.idle.size() > 0) ? this.idle : (force ? this.busy : this.idle); pool.size() > 0; pool = this.busy) {
            try {
                for (PooledConnection con = pool.poll(1000L, TimeUnit.MILLISECONDS); con != null; con = pool.poll(1000L, TimeUnit.MILLISECONDS)) {
                    if (pool == this.idle) {
                        this.release(con);
                    }
                    else {
                        this.abandon(con);
                    }
                    if (pool.size() <= 0) {
                        break;
                    }
                }
            }
            catch (InterruptedException ex) {
                if (this.getPoolProperties().getPropagateInterruptState()) {
                    Thread.currentThread().interrupt();
                }
            }
            if (pool.size() == 0 && force && pool != this.busy) {}
        }
        if (this.getPoolProperties().isJmxEnabled()) {
            this.jmxPool = null;
        }
        final PoolProperties.InterceptorDefinition[] proxies = this.getPoolProperties().getJdbcInterceptorsAsArray();
        for (int i = 0; i < proxies.length; ++i) {
            try {
                final JdbcInterceptor interceptor = (JdbcInterceptor)proxies[i].getInterceptorClass().newInstance();
                interceptor.setProperties(proxies[i].getProperties());
                interceptor.poolClosed(this);
            }
            catch (Exception x) {
                ConnectionPool.log.debug((Object)"Unable to inform interceptor of pool closure.", (Throwable)x);
            }
        }
    }
    
    protected void init(final PoolConfiguration properties) throws SQLException {
        this.checkPoolConfiguration(this.poolProperties = properties);
        this.busy = new LinkedBlockingQueue<PooledConnection>();
        if (properties.isFairQueue()) {
            this.idle = new FairBlockingQueue<PooledConnection>();
        }
        else {
            this.idle = new LinkedBlockingQueue<PooledConnection>();
        }
        this.initializePoolCleaner(properties);
        if (this.getPoolProperties().isJmxEnabled()) {
            this.createMBean();
        }
        final PoolProperties.InterceptorDefinition[] proxies = this.getPoolProperties().getJdbcInterceptorsAsArray();
        for (int i = 0; i < proxies.length; ++i) {
            try {
                if (ConnectionPool.log.isDebugEnabled()) {
                    ConnectionPool.log.debug((Object)("Creating interceptor instance of class:" + proxies[i].getInterceptorClass()));
                }
                final JdbcInterceptor interceptor = (JdbcInterceptor)proxies[i].getInterceptorClass().newInstance();
                interceptor.setProperties(proxies[i].getProperties());
                interceptor.poolStarted(this);
            }
            catch (Exception x) {
                ConnectionPool.log.error((Object)"Unable to inform interceptor of pool start.", (Throwable)x);
                if (this.jmxPool != null) {
                    this.jmxPool.notify("INIT FAILED", getStackTrace(x));
                }
                this.close(true);
                final SQLException ex = new SQLException();
                ex.initCause(x);
                throw ex;
            }
        }
        final PooledConnection[] initialPool = new PooledConnection[this.poolProperties.getInitialSize()];
        try {
            for (int j = 0; j < initialPool.length; ++j) {
                initialPool[j] = this.borrowConnection(0, null, null);
            }
        }
        catch (SQLException x2) {
            ConnectionPool.log.error((Object)"Unable to create initial connections of pool.", (Throwable)x2);
            if (!this.poolProperties.isIgnoreExceptionOnPreLoad()) {
                if (this.jmxPool != null) {
                    this.jmxPool.notify("INIT FAILED", getStackTrace(x2));
                }
                this.close(true);
                throw x2;
            }
        }
        finally {
            for (int k = 0; k < initialPool.length; ++k) {
                if (initialPool[k] != null) {
                    try {
                        this.returnConnection(initialPool[k]);
                    }
                    catch (Exception ex2) {}
                }
            }
        }
        this.closed = false;
    }
    
    public void checkPoolConfiguration(final PoolConfiguration properties) {
        if (properties.getMaxActive() < 1) {
            ConnectionPool.log.warn((Object)"maxActive is smaller than 1, setting maxActive to: 100");
            properties.setMaxActive(100);
        }
        if (properties.getMaxActive() < properties.getInitialSize()) {
            ConnectionPool.log.warn((Object)("initialSize is larger than maxActive, setting initialSize to: " + properties.getMaxActive()));
            properties.setInitialSize(properties.getMaxActive());
        }
        if (properties.getMinIdle() > properties.getMaxActive()) {
            ConnectionPool.log.warn((Object)("minIdle is larger than maxActive, setting minIdle to: " + properties.getMaxActive()));
            properties.setMinIdle(properties.getMaxActive());
        }
        if (properties.getMaxIdle() > properties.getMaxActive()) {
            ConnectionPool.log.warn((Object)("maxIdle is larger than maxActive, setting maxIdle to: " + properties.getMaxActive()));
            properties.setMaxIdle(properties.getMaxActive());
        }
        if (properties.getMaxIdle() < properties.getMinIdle()) {
            ConnectionPool.log.warn((Object)("maxIdle is smaller than minIdle, setting maxIdle to: " + properties.getMinIdle()));
            properties.setMaxIdle(properties.getMinIdle());
        }
    }
    
    public void initializePoolCleaner(final PoolConfiguration properties) {
        if (properties.isPoolSweeperEnabled()) {
            (this.poolCleaner = new PoolCleaner(this, properties.getTimeBetweenEvictionRunsMillis())).start();
        }
    }
    
    public void terminatePoolCleaner() {
        if (this.poolCleaner != null) {
            this.poolCleaner.stopRunning();
            this.poolCleaner = null;
        }
    }
    
    protected void abandon(final PooledConnection con) {
        if (con == null) {
            return;
        }
        try {
            con.lock();
            final String trace = con.getStackTrace();
            if (this.getPoolProperties().isLogAbandoned()) {
                ConnectionPool.log.warn((Object)("Connection has been abandoned " + con + ":" + trace));
            }
            if (this.jmxPool != null) {
                this.jmxPool.notify("CONNECTION ABANDONED", trace);
            }
            this.release(con);
        }
        finally {
            con.unlock();
        }
    }
    
    protected void suspect(final PooledConnection con) {
        if (con == null) {
            return;
        }
        if (con.isSuspect()) {
            return;
        }
        try {
            con.lock();
            final String trace = con.getStackTrace();
            if (this.getPoolProperties().isLogAbandoned()) {
                ConnectionPool.log.warn((Object)("Connection has been marked suspect, possibly abandoned " + con + "[" + (System.currentTimeMillis() - con.getTimestamp()) + " ms.]:" + trace));
            }
            if (this.jmxPool != null) {
                this.jmxPool.notify("SUSPECT CONNETION ABANDONED", trace);
            }
            con.setSuspect(true);
        }
        finally {
            con.unlock();
        }
    }
    
    protected void release(final PooledConnection con) {
        if (con == null) {
            return;
        }
        try {
            con.lock();
            if (con.release()) {
                this.size.addAndGet(-1);
                con.setHandler(null);
            }
        }
        finally {
            con.unlock();
        }
        if (this.waitcount.get() > 0) {
            this.idle.offer(this.create(true));
        }
    }
    
    private PooledConnection borrowConnection(final int wait, final String username, final String password) throws SQLException {
        if (this.isClosed()) {
            throw new SQLException("Connection pool closed.");
        }
        final long now = System.currentTimeMillis();
        PooledConnection con = this.idle.poll();
        while (true) {
            if (con != null) {
                final PooledConnection result = this.borrowConnection(now, con, username, password);
                if (result != null) {
                    return result;
                }
            }
            if (this.size.get() < this.getPoolProperties().getMaxActive()) {
                if (this.size.addAndGet(1) <= this.getPoolProperties().getMaxActive()) {
                    return this.createConnection(now, con, username, password);
                }
                this.size.decrementAndGet();
            }
            long maxWait = wait;
            if (wait == -1) {
                maxWait = ((this.getPoolProperties().getMaxWait() <= 0) ? Long.MAX_VALUE : this.getPoolProperties().getMaxWait());
            }
            final long timetowait = Math.max(0L, maxWait - (System.currentTimeMillis() - now));
            this.waitcount.incrementAndGet();
            try {
                con = this.idle.poll(timetowait, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException ex) {
                if (this.getPoolProperties().getPropagateInterruptState()) {
                    Thread.currentThread().interrupt();
                }
                final SQLException sx = new SQLException("Pool wait interrupted.");
                sx.initCause(ex);
                throw sx;
            }
            finally {
                this.waitcount.decrementAndGet();
            }
            if (maxWait == 0L && con == null) {
                if (this.jmxPool != null) {
                    this.jmxPool.notify("POOL EMPTY", "Pool empty - no wait.");
                }
                throw new PoolExhaustedException("[" + Thread.currentThread().getName() + "] NoWait: Pool empty. Unable to fetch a connection, none available[" + this.busy.size() + " in use].");
            }
            if (con != null) {
                continue;
            }
            if (System.currentTimeMillis() - now >= maxWait) {
                if (this.jmxPool != null) {
                    this.jmxPool.notify("POOL EMPTY", "Pool empty - timeout.");
                }
                throw new PoolExhaustedException("[" + Thread.currentThread().getName() + "] Timeout: Pool empty. Unable to fetch a connection in " + maxWait / 1000L + " seconds, none available[size:" + this.size.get() + "; busy:" + this.busy.size() + "; idle:" + this.idle.size() + "; lastwait:" + timetowait + "].");
            }
        }
    }
    
    protected PooledConnection createConnection(final long now, final PooledConnection notUsed, final String username, final String password) throws SQLException {
        final PooledConnection con = this.create(false);
        if (username != null) {
            con.getAttributes().put("user", username);
        }
        if (password != null) {
            con.getAttributes().put("password", password);
        }
        boolean error = false;
        try {
            con.lock();
            con.connect();
            if (con.validate(4)) {
                con.setTimestamp(now);
                if (this.getPoolProperties().isLogAbandoned()) {
                    con.setStackTrace(getThreadDump());
                }
                if (!this.busy.offer(con)) {
                    ConnectionPool.log.debug((Object)"Connection doesn't fit into busy array, connection will not be traceable.");
                }
                return con;
            }
            throw new SQLException("Validation Query Failed, enable logValidationErrors for more details.");
        }
        catch (Exception e) {
            error = true;
            if (ConnectionPool.log.isDebugEnabled()) {
                ConnectionPool.log.debug((Object)"Unable to create a new JDBC connection.", (Throwable)e);
            }
            if (e instanceof SQLException) {
                throw (SQLException)e;
            }
            final SQLException ex = new SQLException(e.getMessage());
            ex.initCause(e);
            throw ex;
        }
        finally {
            if (error) {
                this.release(con);
            }
            con.unlock();
        }
    }
    
    protected PooledConnection borrowConnection(final long now, PooledConnection con, final String username, final String password) throws SQLException {
        boolean setToNull = false;
        try {
            con.lock();
            if (con.isReleased()) {
                return null;
            }
            boolean forceReconnect = con.shouldForceReconnect(username, password) || con.isMaxAgeExpired();
            if (!con.isDiscarded() && !con.isInitialized()) {
                forceReconnect = true;
            }
            if (!forceReconnect && !con.isDiscarded() && con.validate(1)) {
                con.setTimestamp(now);
                if (this.getPoolProperties().isLogAbandoned()) {
                    con.setStackTrace(getThreadDump());
                }
                if (!this.busy.offer(con)) {
                    ConnectionPool.log.debug((Object)"Connection doesn't fit into busy array, connection will not be traceable.");
                }
                return con;
            }
            try {
                con.reconnect();
                final int validationMode = (this.getPoolProperties().isTestOnConnect() || this.getPoolProperties().getInitSQL() != null) ? 4 : 1;
                if (con.validate(validationMode)) {
                    con.setTimestamp(now);
                    if (this.getPoolProperties().isLogAbandoned()) {
                        con.setStackTrace(getThreadDump());
                    }
                    if (!this.busy.offer(con)) {
                        ConnectionPool.log.debug((Object)"Connection doesn't fit into busy array, connection will not be traceable.");
                    }
                    return con;
                }
                throw new SQLException("Failed to validate a newly established connection.");
            }
            catch (Exception x) {
                this.release(con);
                setToNull = true;
                if (x instanceof SQLException) {
                    throw (SQLException)x;
                }
                final SQLException ex = new SQLException(x.getMessage());
                ex.initCause(x);
                throw ex;
            }
        }
        finally {
            con.unlock();
            if (setToNull) {
                con = null;
            }
        }
    }
    
    protected boolean terminateTransaction(final PooledConnection con) {
        try {
            if (Boolean.FALSE.equals(con.getPoolProperties().getDefaultAutoCommit())) {
                if (this.getPoolProperties().getRollbackOnReturn()) {
                    final boolean autocommit = con.getConnection().getAutoCommit();
                    if (!autocommit) {
                        con.getConnection().rollback();
                    }
                }
                else if (this.getPoolProperties().getCommitOnReturn()) {
                    final boolean autocommit = con.getConnection().getAutoCommit();
                    if (!autocommit) {
                        con.getConnection().commit();
                    }
                }
            }
            return true;
        }
        catch (SQLException x) {
            ConnectionPool.log.warn((Object)"Unable to terminate transaction, connection will be closed.", (Throwable)x);
            return false;
        }
    }
    
    protected boolean shouldClose(final PooledConnection con, final int action) {
        return con.getConnectionVersion() < this.getPoolVersion() || con.isDiscarded() || this.isClosed() || !con.validate(action) || !this.terminateTransaction(con) || con.isMaxAgeExpired();
    }
    
    protected void returnConnection(final PooledConnection con) {
        if (this.isClosed()) {
            this.release(con);
            return;
        }
        if (con != null) {
            try {
                con.lock();
                if (con.isSuspect()) {
                    if (this.poolProperties.isLogAbandoned() && ConnectionPool.log.isInfoEnabled()) {
                        ConnectionPool.log.info((Object)("Connection(" + con + ") that has been marked suspect was returned. The processing time is " + (System.currentTimeMillis() - con.getTimestamp()) + " ms."));
                    }
                    if (this.jmxPool != null) {
                        this.jmxPool.notify("SUSPECT CONNETION RETURNED", "Connection(" + con + ") that has been marked suspect was returned.");
                    }
                }
                if (this.busy.remove(con)) {
                    if (!this.shouldClose(con, 2)) {
                        con.setStackTrace(null);
                        con.setTimestamp(System.currentTimeMillis());
                        if ((this.idle.size() >= this.poolProperties.getMaxIdle() && !this.poolProperties.isPoolSweeperEnabled()) || !this.idle.offer(con)) {
                            if (ConnectionPool.log.isDebugEnabled()) {
                                ConnectionPool.log.debug((Object)("Connection [" + con + "] will be closed and not returned to the pool, idle[" + this.idle.size() + "]>=maxIdle[" + this.poolProperties.getMaxIdle() + "] idle.offer failed."));
                            }
                            this.release(con);
                        }
                    }
                    else {
                        if (ConnectionPool.log.isDebugEnabled()) {
                            ConnectionPool.log.debug((Object)("Connection [" + con + "] will be closed and not returned to the pool."));
                        }
                        this.release(con);
                    }
                }
                else {
                    if (ConnectionPool.log.isDebugEnabled()) {
                        ConnectionPool.log.debug((Object)("Connection [" + con + "] will be closed and not returned to the pool, busy.remove failed."));
                    }
                    this.release(con);
                }
            }
            finally {
                con.unlock();
            }
        }
    }
    
    protected boolean shouldAbandon() {
        if (!this.poolProperties.isRemoveAbandoned()) {
            return false;
        }
        if (this.poolProperties.getAbandonWhenPercentageFull() == 0) {
            return true;
        }
        final float used = this.busy.size();
        final float max = this.poolProperties.getMaxActive();
        final float perc = this.poolProperties.getAbandonWhenPercentageFull();
        return used / max * 100.0f >= perc;
    }
    
    public void checkAbandoned() {
        try {
            if (this.busy.size() == 0) {
                return;
            }
            final Iterator<PooledConnection> locked = this.busy.iterator();
            final int sto = this.getPoolProperties().getSuspectTimeout();
            while (locked.hasNext()) {
                PooledConnection con = locked.next();
                boolean setToNull = false;
                try {
                    con.lock();
                    if (this.idle.contains(con) || con.isReleased()) {
                        continue;
                    }
                    final long time = con.getTimestamp();
                    final long now = System.currentTimeMillis();
                    if (this.shouldAbandon() && now - time > con.getAbandonTimeout()) {
                        this.busy.remove(con);
                        this.abandon(con);
                        setToNull = true;
                    }
                    else {
                        if (sto <= 0 || now - time <= sto * 1000L) {
                            continue;
                        }
                        this.suspect(con);
                    }
                }
                finally {
                    con.unlock();
                    if (setToNull) {
                        con = null;
                    }
                }
            }
        }
        catch (ConcurrentModificationException e) {
            ConnectionPool.log.debug((Object)"checkAbandoned failed.", (Throwable)e);
        }
        catch (Exception e2) {
            ConnectionPool.log.warn((Object)"checkAbandoned failed, it will be retried.", (Throwable)e2);
        }
    }
    
    public void checkIdle() {
        this.checkIdle(false);
    }
    
    public void checkIdle(final boolean ignoreMinSize) {
        try {
            if (this.idle.size() == 0) {
                return;
            }
            final long now = System.currentTimeMillis();
            final Iterator<PooledConnection> unlocked = this.idle.iterator();
            while ((ignoreMinSize || this.idle.size() >= this.getPoolProperties().getMinIdle()) && unlocked.hasNext()) {
                PooledConnection con = unlocked.next();
                boolean setToNull = false;
                try {
                    con.lock();
                    if (this.busy.contains(con)) {
                        continue;
                    }
                    final long time = con.getTimestamp();
                    if (!this.shouldReleaseIdle(now, con, time)) {
                        continue;
                    }
                    this.release(con);
                    this.idle.remove(con);
                    setToNull = true;
                }
                finally {
                    con.unlock();
                    if (setToNull) {
                        con = null;
                    }
                }
            }
        }
        catch (ConcurrentModificationException e) {
            ConnectionPool.log.debug((Object)"checkIdle failed.", (Throwable)e);
        }
        catch (Exception e2) {
            ConnectionPool.log.warn((Object)"checkIdle failed, it will be retried.", (Throwable)e2);
        }
    }
    
    protected boolean shouldReleaseIdle(final long now, final PooledConnection con, final long time) {
        return con.getConnectionVersion() < this.getPoolVersion() || (con.getReleaseTime() > 0L && now - time > con.getReleaseTime() && this.getSize() > this.getPoolProperties().getMinIdle());
    }
    
    public void testAllIdle() {
        try {
            if (this.idle.size() == 0) {
                return;
            }
            for (final PooledConnection con : this.idle) {
                try {
                    con.lock();
                    if (this.busy.contains(con)) {
                        continue;
                    }
                    if (con.validate(3)) {
                        continue;
                    }
                    this.idle.remove(con);
                    this.release(con);
                }
                finally {
                    con.unlock();
                }
            }
        }
        catch (ConcurrentModificationException e) {
            ConnectionPool.log.debug((Object)"testAllIdle failed.", (Throwable)e);
        }
        catch (Exception e2) {
            ConnectionPool.log.warn((Object)"testAllIdle failed, it will be retried.", (Throwable)e2);
        }
    }
    
    protected static String getThreadDump() {
        final Exception x = new Exception();
        x.fillInStackTrace();
        return getStackTrace(x);
    }
    
    public static String getStackTrace(final Throwable x) {
        if (x == null) {
            return null;
        }
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final PrintStream writer = new PrintStream(bout);
        x.printStackTrace(writer);
        final String result = bout.toString();
        return (x.getMessage() != null && x.getMessage().length() > 0) ? (x.getMessage() + ";" + result) : result;
    }
    
    protected PooledConnection create(final boolean incrementCounter) {
        if (incrementCounter) {
            this.size.incrementAndGet();
        }
        final PooledConnection con = new PooledConnection(this.getPoolProperties(), this);
        return con;
    }
    
    public void purge() {
        this.purgeOnReturn();
        this.checkIdle(true);
    }
    
    public void purgeOnReturn() {
        this.poolVersion.incrementAndGet();
    }
    
    protected void finalize(final PooledConnection con) {
        for (JdbcInterceptor handler = con.getHandler(); handler != null; handler = handler.getNext()) {
            handler.reset(null, null);
        }
    }
    
    protected void disconnectEvent(final PooledConnection con, final boolean finalizing) {
        for (JdbcInterceptor handler = con.getHandler(); handler != null; handler = handler.getNext()) {
            handler.disconnected(this, con, finalizing);
        }
    }
    
    public org.apache.tomcat.jdbc.pool.jmx.ConnectionPool getJmxPool() {
        return this.jmxPool;
    }
    
    protected void createMBean() {
        try {
            this.jmxPool = new org.apache.tomcat.jdbc.pool.jmx.ConnectionPool(this);
        }
        catch (Exception x) {
            ConnectionPool.log.warn((Object)("Unable to start JMX integration for connection pool. Instance[" + this.getName() + "] can't be monitored."), (Throwable)x);
        }
    }
    
    private static synchronized void registerCleaner(final PoolCleaner cleaner) {
        unregisterCleaner(cleaner);
        ConnectionPool.cleaners.add(cleaner);
        if (ConnectionPool.poolCleanTimer == null) {
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(ConnectionPool.class.getClassLoader());
                final PrivilegedAction<Timer> pa = new PrivilegedNewTimer();
                ConnectionPool.poolCleanTimer = AccessController.doPrivileged(pa);
            }
            finally {
                Thread.currentThread().setContextClassLoader(loader);
            }
        }
        ConnectionPool.poolCleanTimer.schedule(cleaner, cleaner.sleepTime, cleaner.sleepTime);
    }
    
    private static synchronized void unregisterCleaner(final PoolCleaner cleaner) {
        final boolean removed = ConnectionPool.cleaners.remove(cleaner);
        if (removed) {
            cleaner.cancel();
            if (ConnectionPool.poolCleanTimer != null) {
                ConnectionPool.poolCleanTimer.purge();
                if (ConnectionPool.cleaners.size() == 0) {
                    ConnectionPool.poolCleanTimer.cancel();
                    ConnectionPool.poolCleanTimer = null;
                }
            }
        }
    }
    
    public static Set<TimerTask> getPoolCleaners() {
        return Collections.unmodifiableSet((Set<? extends TimerTask>)ConnectionPool.cleaners);
    }
    
    public long getPoolVersion() {
        return this.poolVersion.get();
    }
    
    public static Timer getPoolTimer() {
        return ConnectionPool.poolCleanTimer;
    }
    
    static {
        log = LogFactory.getLog((Class)ConnectionPool.class);
        ConnectionPool.poolCleanTimer = null;
        ConnectionPool.cleaners = new HashSet<PoolCleaner>();
    }
    
    protected class ConnectionFuture implements Future<Connection>, Runnable
    {
        Future<PooledConnection> pcFuture;
        AtomicBoolean configured;
        CountDownLatch latch;
        volatile Connection result;
        SQLException cause;
        AtomicBoolean cancelled;
        volatile PooledConnection pc;
        
        public ConnectionFuture(final Future<PooledConnection> pcf) {
            this.pcFuture = null;
            this.configured = new AtomicBoolean(false);
            this.latch = new CountDownLatch(1);
            this.result = null;
            this.cause = null;
            this.cancelled = new AtomicBoolean(false);
            this.pc = null;
            this.pcFuture = pcf;
        }
        
        public ConnectionFuture(final PooledConnection pc) throws SQLException {
            this.pcFuture = null;
            this.configured = new AtomicBoolean(false);
            this.latch = new CountDownLatch(1);
            this.result = null;
            this.cause = null;
            this.cancelled = new AtomicBoolean(false);
            this.pc = null;
            this.pc = pc;
            this.result = ConnectionPool.this.setupConnection(pc);
            this.configured.set(true);
        }
        
        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            if (this.pc != null) {
                return false;
            }
            if (!this.cancelled.get() && this.cancelled.compareAndSet(false, true)) {
                ConnectionPool.this.cancellator.execute(this);
            }
            return true;
        }
        
        @Override
        public Connection get() throws InterruptedException, ExecutionException {
            try {
                return this.get(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            }
            catch (TimeoutException x) {
                throw new ExecutionException(x);
            }
        }
        
        @Override
        public Connection get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            PooledConnection pc = (this.pc != null) ? this.pc : this.pcFuture.get(timeout, unit);
            if (pc == null) {
                return null;
            }
            if (this.result != null) {
                return this.result;
            }
            if (this.configured.compareAndSet(false, true)) {
                try {
                    pc = ConnectionPool.this.borrowConnection(System.currentTimeMillis(), pc, null, null);
                    this.result = ConnectionPool.this.setupConnection(pc);
                }
                catch (SQLException x) {
                    this.cause = x;
                }
                finally {
                    this.latch.countDown();
                }
            }
            else {
                this.latch.await(timeout, unit);
            }
            if (this.result == null) {
                throw new ExecutionException(this.cause);
            }
            return this.result;
        }
        
        @Override
        public boolean isCancelled() {
            return this.pc == null && (this.pcFuture.isCancelled() || this.cancelled.get());
        }
        
        @Override
        public boolean isDone() {
            return this.pc != null || this.pcFuture.isDone();
        }
        
        @Override
        public void run() {
            try {
                final Connection con = this.get();
                con.close();
            }
            catch (ExecutionException ex) {}
            catch (Exception x) {
                ConnectionPool.log.error((Object)"Unable to cancel ConnectionFuture.", (Throwable)x);
            }
        }
    }
    
    private static class PrivilegedNewTimer implements PrivilegedAction<Timer>
    {
        @Override
        public Timer run() {
            return new Timer("Tomcat JDBC Pool Cleaner[" + System.identityHashCode(ConnectionPool.class.getClassLoader()) + ":" + System.currentTimeMillis() + "]", true);
        }
    }
    
    protected static class PoolCleaner extends TimerTask
    {
        protected WeakReference<ConnectionPool> pool;
        protected long sleepTime;
        
        PoolCleaner(final ConnectionPool pool, final long sleepTime) {
            this.pool = new WeakReference<ConnectionPool>(pool);
            this.sleepTime = sleepTime;
            if (sleepTime <= 0L) {
                ConnectionPool.log.warn((Object)"Database connection pool evicter thread interval is set to 0, defaulting to 30 seconds");
                this.sleepTime = 30000L;
            }
            else if (sleepTime < 1000L) {
                ConnectionPool.log.warn((Object)"Database connection pool evicter thread interval is set to lower than 1 second.");
            }
        }
        
        @Override
        public void run() {
            final ConnectionPool pool = this.pool.get();
            if (pool == null) {
                this.stopRunning();
            }
            else if (!pool.isClosed()) {
                try {
                    if (pool.getPoolProperties().isRemoveAbandoned() || pool.getPoolProperties().getSuspectTimeout() > 0) {
                        pool.checkAbandoned();
                    }
                    if (pool.getPoolProperties().getMinIdle() < pool.idle.size()) {
                        pool.checkIdle();
                    }
                    if (pool.getPoolProperties().isTestWhileIdle()) {
                        pool.testAllIdle();
                    }
                }
                catch (Exception x) {
                    ConnectionPool.log.error((Object)"", (Throwable)x);
                }
            }
        }
        
        public void start() {
            registerCleaner(this);
        }
        
        public void stopRunning() {
            unregisterCleaner(this);
        }
    }
}
