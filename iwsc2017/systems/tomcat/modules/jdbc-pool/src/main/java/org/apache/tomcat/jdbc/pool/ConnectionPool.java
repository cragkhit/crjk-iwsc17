package org.apache.tomcat.jdbc.pool;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class ConnectionPool {
    public static final String POOL_JMX_DOMAIN = "tomcat.jdbc";
    public static final String POOL_JMX_TYPE_PREFIX = POOL_JMX_DOMAIN + ":type=";
    private static final Log log = LogFactory.getLog ( ConnectionPool.class );
    private AtomicInteger size = new AtomicInteger ( 0 );
    private PoolConfiguration poolProperties;
    private BlockingQueue<PooledConnection> busy;
    private BlockingQueue<PooledConnection> idle;
    private volatile PoolCleaner poolCleaner;
    private volatile boolean closed = false;
    private Constructor<?> proxyClassConstructor;
    private ThreadPoolExecutor cancellator = new ThreadPoolExecutor ( 0, 1, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>() );
    protected org.apache.tomcat.jdbc.pool.jmx.ConnectionPool jmxPool = null;
    private AtomicInteger waitcount = new AtomicInteger ( 0 );
    private AtomicLong poolVersion = new AtomicLong ( Long.MIN_VALUE );
    public ConnectionPool ( PoolConfiguration prop ) throws SQLException {
        init ( prop );
    }
    public Future<Connection> getConnectionAsync() throws SQLException {
        try {
            PooledConnection pc = borrowConnection ( 0, null, null );
            if ( pc != null ) {
                return new ConnectionFuture ( pc );
            }
        } catch ( SQLException x ) {
            if ( x.getMessage().indexOf ( "NoWait" ) < 0 ) {
                throw x;
            }
        }
        if ( idle instanceof FairBlockingQueue<?> ) {
            Future<PooledConnection> pcf = ( ( FairBlockingQueue<PooledConnection> ) idle ).pollAsync();
            return new ConnectionFuture ( pcf );
        } else if ( idle instanceof MultiLockFairBlockingQueue<?> ) {
            Future<PooledConnection> pcf = ( ( MultiLockFairBlockingQueue<PooledConnection> ) idle ).pollAsync();
            return new ConnectionFuture ( pcf );
        } else {
            throw new SQLException ( "Connection pool is misconfigured, doesn't support async retrieval. Set the 'fair' property to 'true'" );
        }
    }
    public Connection getConnection() throws SQLException {
        PooledConnection con = borrowConnection ( -1, null, null );
        return setupConnection ( con );
    }
    public Connection getConnection ( String username, String password ) throws SQLException {
        PooledConnection con = borrowConnection ( -1, username, password );
        return setupConnection ( con );
    }
    public String getName() {
        return getPoolProperties().getPoolName();
    }
    public int getWaitCount() {
        return waitcount.get();
    }
    public PoolConfiguration getPoolProperties() {
        return this.poolProperties;
    }
    public int getSize() {
        return size.get();
    }
    public int getActive() {
        return busy.size();
    }
    public int getIdle() {
        return idle.size();
    }
    public  boolean isClosed() {
        return this.closed;
    }
    protected Connection setupConnection ( PooledConnection con ) throws SQLException {
        JdbcInterceptor handler = con.getHandler();
        if ( handler == null ) {
            handler = new ProxyConnection ( this, con, getPoolProperties().isUseEquals() );
            PoolProperties.InterceptorDefinition[] proxies = getPoolProperties().getJdbcInterceptorsAsArray();
            for ( int i = proxies.length - 1; i >= 0; i-- ) {
                try {
                    JdbcInterceptor interceptor = proxies[i].getInterceptorClass().newInstance();
                    interceptor.setProperties ( proxies[i].getProperties() );
                    interceptor.setNext ( handler );
                    interceptor.reset ( this, con );
                    handler = interceptor;
                } catch ( Exception x ) {
                    SQLException sx = new SQLException ( "Unable to instantiate interceptor chain." );
                    sx.initCause ( x );
                    throw sx;
                }
            }
            con.setHandler ( handler );
        } else {
            JdbcInterceptor next = handler;
            while ( next != null ) {
                next.reset ( this, con );
                next = next.getNext();
            }
        }
        try {
            getProxyConstructor ( con.getXAConnection() != null );
            Connection connection = null;
            if ( getPoolProperties().getUseDisposableConnectionFacade() ) {
                connection = ( Connection ) proxyClassConstructor.newInstance ( new Object[] { new DisposableConnectionFacade ( handler ) } );
            } else {
                connection = ( Connection ) proxyClassConstructor.newInstance ( new Object[] {handler} );
            }
            return connection;
        } catch ( Exception x ) {
            SQLException s = new SQLException();
            s.initCause ( x );
            throw s;
        }
    }
    public Constructor<?> getProxyConstructor ( boolean xa ) throws NoSuchMethodException {
        if ( proxyClassConstructor == null ) {
            Class<?> proxyClass = xa ?
                                  Proxy.getProxyClass ( ConnectionPool.class.getClassLoader(), new Class[] {java.sql.Connection.class, javax.sql.PooledConnection.class, javax.sql.XAConnection.class} ) :
                                  Proxy.getProxyClass ( ConnectionPool.class.getClassLoader(), new Class[] {java.sql.Connection.class, javax.sql.PooledConnection.class} );
            proxyClassConstructor = proxyClass.getConstructor ( new Class[] { InvocationHandler.class } );
        }
        return proxyClassConstructor;
    }
    protected void close ( boolean force ) {
        if ( this.closed ) {
            return;
        }
        this.closed = true;
        if ( poolCleaner != null ) {
            poolCleaner.stopRunning();
        }
        BlockingQueue<PooledConnection> pool = ( idle.size() > 0 ) ? idle : ( force ? busy : idle );
        while ( pool.size() > 0 ) {
            try {
                PooledConnection con = pool.poll ( 1000, TimeUnit.MILLISECONDS );
                while ( con != null ) {
                    if ( pool == idle ) {
                        release ( con );
                    } else {
                        abandon ( con );
                    }
                    if ( pool.size() > 0 ) {
                        con = pool.poll ( 1000, TimeUnit.MILLISECONDS );
                    } else {
                        break;
                    }
                }
            } catch ( InterruptedException ex ) {
                if ( getPoolProperties().getPropagateInterruptState() ) {
                    Thread.currentThread().interrupt();
                }
            }
            if ( pool.size() == 0 && force && pool != busy ) {
                pool = busy;
            }
        }
        if ( this.getPoolProperties().isJmxEnabled() ) {
            this.jmxPool = null;
        }
        PoolProperties.InterceptorDefinition[] proxies = getPoolProperties().getJdbcInterceptorsAsArray();
        for ( int i = 0; i < proxies.length; i++ ) {
            try {
                JdbcInterceptor interceptor = proxies[i].getInterceptorClass().newInstance();
                interceptor.setProperties ( proxies[i].getProperties() );
                interceptor.poolClosed ( this );
            } catch ( Exception x ) {
                log.debug ( "Unable to inform interceptor of pool closure.", x );
            }
        }
    }
    protected void init ( PoolConfiguration properties ) throws SQLException {
        poolProperties = properties;
        checkPoolConfiguration ( properties );
        busy = new LinkedBlockingQueue<>();
        if ( properties.isFairQueue() ) {
            idle = new FairBlockingQueue<>();
        } else {
            idle = new LinkedBlockingQueue<>();
        }
        initializePoolCleaner ( properties );
        if ( this.getPoolProperties().isJmxEnabled() ) {
            createMBean();
        }
        PoolProperties.InterceptorDefinition[] proxies = getPoolProperties().getJdbcInterceptorsAsArray();
        for ( int i = 0; i < proxies.length; i++ ) {
            try {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Creating interceptor instance of class:" + proxies[i].getInterceptorClass() );
                }
                JdbcInterceptor interceptor = proxies[i].getInterceptorClass().newInstance();
                interceptor.setProperties ( proxies[i].getProperties() );
                interceptor.poolStarted ( this );
            } catch ( Exception x ) {
                log.error ( "Unable to inform interceptor of pool start.", x );
                if ( jmxPool != null ) {
                    jmxPool.notify ( org.apache.tomcat.jdbc.pool.jmx.ConnectionPool.NOTIFY_INIT, getStackTrace ( x ) );
                }
                close ( true );
                SQLException ex = new SQLException();
                ex.initCause ( x );
                throw ex;
            }
        }
        PooledConnection[] initialPool = new PooledConnection[poolProperties.getInitialSize()];
        try {
            for ( int i = 0; i < initialPool.length; i++ ) {
                initialPool[i] = this.borrowConnection ( 0, null, null );
            }
        } catch ( SQLException x ) {
            log.error ( "Unable to create initial connections of pool.", x );
            if ( !poolProperties.isIgnoreExceptionOnPreLoad() ) {
                if ( jmxPool != null ) {
                    jmxPool.notify ( org.apache.tomcat.jdbc.pool.jmx.ConnectionPool.NOTIFY_INIT, getStackTrace ( x ) );
                }
                close ( true );
                throw x;
            }
        } finally {
            for ( int i = 0; i < initialPool.length; i++ ) {
                if ( initialPool[i] != null ) {
                    try {
                        this.returnConnection ( initialPool[i] );
                    } catch ( Exception x ) { }
                }
            }
        }
        closed = false;
    }
    public void checkPoolConfiguration ( PoolConfiguration properties ) {
        if ( properties.getMaxActive() < 1 ) {
            log.warn ( "maxActive is smaller than 1, setting maxActive to: " + PoolProperties.DEFAULT_MAX_ACTIVE );
            properties.setMaxActive ( PoolProperties.DEFAULT_MAX_ACTIVE );
        }
        if ( properties.getMaxActive() < properties.getInitialSize() ) {
            log.warn ( "initialSize is larger than maxActive, setting initialSize to: " + properties.getMaxActive() );
            properties.setInitialSize ( properties.getMaxActive() );
        }
        if ( properties.getMinIdle() > properties.getMaxActive() ) {
            log.warn ( "minIdle is larger than maxActive, setting minIdle to: " + properties.getMaxActive() );
            properties.setMinIdle ( properties.getMaxActive() );
        }
        if ( properties.getMaxIdle() > properties.getMaxActive() ) {
            log.warn ( "maxIdle is larger than maxActive, setting maxIdle to: " + properties.getMaxActive() );
            properties.setMaxIdle ( properties.getMaxActive() );
        }
        if ( properties.getMaxIdle() < properties.getMinIdle() ) {
            log.warn ( "maxIdle is smaller than minIdle, setting maxIdle to: " + properties.getMinIdle() );
            properties.setMaxIdle ( properties.getMinIdle() );
        }
    }
    public void initializePoolCleaner ( PoolConfiguration properties ) {
        if ( properties.isPoolSweeperEnabled() ) {
            poolCleaner = new PoolCleaner ( this, properties.getTimeBetweenEvictionRunsMillis() );
            poolCleaner.start();
        }
    }
    public void terminatePoolCleaner() {
        if ( poolCleaner != null ) {
            poolCleaner.stopRunning();
            poolCleaner = null;
        }
    }
    protected void abandon ( PooledConnection con ) {
        if ( con == null ) {
            return;
        }
        try {
            con.lock();
            String trace = con.getStackTrace();
            if ( getPoolProperties().isLogAbandoned() ) {
                log.warn ( "Connection has been abandoned " + con + ":" + trace );
            }
            if ( jmxPool != null ) {
                jmxPool.notify ( org.apache.tomcat.jdbc.pool.jmx.ConnectionPool.NOTIFY_ABANDON, trace );
            }
            release ( con );
        } finally {
            con.unlock();
        }
    }
    protected void suspect ( PooledConnection con ) {
        if ( con == null ) {
            return;
        }
        if ( con.isSuspect() ) {
            return;
        }
        try {
            con.lock();
            String trace = con.getStackTrace();
            if ( getPoolProperties().isLogAbandoned() ) {
                log.warn ( "Connection has been marked suspect, possibly abandoned " + con + "[" + ( System.currentTimeMillis() - con.getTimestamp() ) + " ms.]:" + trace );
            }
            if ( jmxPool != null ) {
                jmxPool.notify ( org.apache.tomcat.jdbc.pool.jmx.ConnectionPool.SUSPECT_ABANDONED_NOTIFICATION, trace );
            }
            con.setSuspect ( true );
        } finally {
            con.unlock();
        }
    }
    protected void release ( PooledConnection con ) {
        if ( con == null ) {
            return;
        }
        try {
            con.lock();
            if ( con.release() ) {
                size.addAndGet ( -1 );
                con.setHandler ( null );
            }
        } finally {
            con.unlock();
        }
        if ( waitcount.get() > 0 ) {
            idle.offer ( create ( true ) );
        }
    }
    private PooledConnection borrowConnection ( int wait, String username, String password ) throws SQLException {
        if ( isClosed() ) {
            throw new SQLException ( "Connection pool closed." );
        }
        long now = System.currentTimeMillis();
        PooledConnection con = idle.poll();
        while ( true ) {
            if ( con != null ) {
                PooledConnection result = borrowConnection ( now, con, username, password );
                if ( result != null ) {
                    return result;
                }
            }
            if ( size.get() < getPoolProperties().getMaxActive() ) {
                if ( size.addAndGet ( 1 ) > getPoolProperties().getMaxActive() ) {
                    size.decrementAndGet();
                } else {
                    return createConnection ( now, con, username, password );
                }
            }
            long maxWait = wait;
            if ( wait == -1 ) {
                maxWait = ( getPoolProperties().getMaxWait() <= 0 ) ? Long.MAX_VALUE : getPoolProperties().getMaxWait();
            }
            long timetowait = Math.max ( 0, maxWait - ( System.currentTimeMillis() - now ) );
            waitcount.incrementAndGet();
            try {
                con = idle.poll ( timetowait, TimeUnit.MILLISECONDS );
            } catch ( InterruptedException ex ) {
                if ( getPoolProperties().getPropagateInterruptState() ) {
                    Thread.currentThread().interrupt();
                }
                SQLException sx = new SQLException ( "Pool wait interrupted." );
                sx.initCause ( ex );
                throw sx;
            } finally {
                waitcount.decrementAndGet();
            }
            if ( maxWait == 0 && con == null ) {
                if ( jmxPool != null ) {
                    jmxPool.notify ( org.apache.tomcat.jdbc.pool.jmx.ConnectionPool.POOL_EMPTY, "Pool empty - no wait." );
                }
                throw new PoolExhaustedException ( "[" + Thread.currentThread().getName() + "] " +
                                                   "NoWait: Pool empty. Unable to fetch a connection, none available[" + busy.size() + " in use]." );
            }
            if ( con == null ) {
                if ( ( System.currentTimeMillis() - now ) >= maxWait ) {
                    if ( jmxPool != null ) {
                        jmxPool.notify ( org.apache.tomcat.jdbc.pool.jmx.ConnectionPool.POOL_EMPTY, "Pool empty - timeout." );
                    }
                    throw new PoolExhaustedException ( "[" + Thread.currentThread().getName() + "] " +
                                                       "Timeout: Pool empty. Unable to fetch a connection in " + ( maxWait / 1000 ) +
                                                       " seconds, none available[size:" + size.get() + "; busy:" + busy.size() + "; idle:" + idle.size() + "; lastwait:" + timetowait + "]." );
                } else {
                    continue;
                }
            }
        }
    }
    protected PooledConnection createConnection ( long now, PooledConnection notUsed, String username, String password ) throws SQLException {
        PooledConnection con = create ( false );
        if ( username != null ) {
            con.getAttributes().put ( PooledConnection.PROP_USER, username );
        }
        if ( password != null ) {
            con.getAttributes().put ( PooledConnection.PROP_PASSWORD, password );
        }
        boolean error = false;
        try {
            con.lock();
            con.connect();
            if ( con.validate ( PooledConnection.VALIDATE_INIT ) ) {
                con.setTimestamp ( now );
                if ( getPoolProperties().isLogAbandoned() ) {
                    con.setStackTrace ( getThreadDump() );
                }
                if ( !busy.offer ( con ) ) {
                    log.debug ( "Connection doesn't fit into busy array, connection will not be traceable." );
                }
                return con;
            } else {
                throw new SQLException ( "Validation Query Failed, enable logValidationErrors for more details." );
            }
        } catch ( Exception e ) {
            error = true;
            if ( log.isDebugEnabled() ) {
                log.debug ( "Unable to create a new JDBC connection.", e );
            }
            if ( e instanceof SQLException ) {
                throw ( SQLException ) e;
            } else {
                SQLException ex = new SQLException ( e.getMessage() );
                ex.initCause ( e );
                throw ex;
            }
        } finally {
            if ( error ) {
                release ( con );
            }
            con.unlock();
        }
    }
    protected PooledConnection borrowConnection ( long now, PooledConnection con, String username, String password ) throws SQLException {
        boolean setToNull = false;
        try {
            con.lock();
            if ( con.isReleased() ) {
                return null;
            }
            boolean forceReconnect = con.shouldForceReconnect ( username, password ) || con.isMaxAgeExpired();
            if ( !con.isDiscarded() && !con.isInitialized() ) {
                forceReconnect = true;
            }
            if ( !forceReconnect ) {
                if ( ( !con.isDiscarded() ) && con.validate ( PooledConnection.VALIDATE_BORROW ) ) {
                    con.setTimestamp ( now );
                    if ( getPoolProperties().isLogAbandoned() ) {
                        con.setStackTrace ( getThreadDump() );
                    }
                    if ( !busy.offer ( con ) ) {
                        log.debug ( "Connection doesn't fit into busy array, connection will not be traceable." );
                    }
                    return con;
                }
            }
            try {
                con.reconnect();
                int validationMode = getPoolProperties().isTestOnConnect() || getPoolProperties().getInitSQL() != null ?
                                     PooledConnection.VALIDATE_INIT :
                                     PooledConnection.VALIDATE_BORROW;
                if ( con.validate ( validationMode ) ) {
                    con.setTimestamp ( now );
                    if ( getPoolProperties().isLogAbandoned() ) {
                        con.setStackTrace ( getThreadDump() );
                    }
                    if ( !busy.offer ( con ) ) {
                        log.debug ( "Connection doesn't fit into busy array, connection will not be traceable." );
                    }
                    return con;
                } else {
                    throw new SQLException ( "Failed to validate a newly established connection." );
                }
            } catch ( Exception x ) {
                release ( con );
                setToNull = true;
                if ( x instanceof SQLException ) {
                    throw ( SQLException ) x;
                } else {
                    SQLException ex  = new SQLException ( x.getMessage() );
                    ex.initCause ( x );
                    throw ex;
                }
            }
        } finally {
            con.unlock();
            if ( setToNull ) {
                con = null;
            }
        }
    }
    protected boolean terminateTransaction ( PooledConnection con ) {
        try {
            if ( Boolean.FALSE.equals ( con.getPoolProperties().getDefaultAutoCommit() ) ) {
                if ( this.getPoolProperties().getRollbackOnReturn() ) {
                    boolean autocommit = con.getConnection().getAutoCommit();
                    if ( !autocommit ) {
                        con.getConnection().rollback();
                    }
                } else if ( this.getPoolProperties().getCommitOnReturn() ) {
                    boolean autocommit = con.getConnection().getAutoCommit();
                    if ( !autocommit ) {
                        con.getConnection().commit();
                    }
                }
            }
            return true;
        } catch ( SQLException x ) {
            log.warn ( "Unable to terminate transaction, connection will be closed.", x );
            return false;
        }
    }
    protected boolean shouldClose ( PooledConnection con, int action ) {
        if ( con.getConnectionVersion() < getPoolVersion() ) {
            return true;
        }
        if ( con.isDiscarded() ) {
            return true;
        }
        if ( isClosed() ) {
            return true;
        }
        if ( !con.validate ( action ) ) {
            return true;
        }
        if ( !terminateTransaction ( con ) ) {
            return true;
        }
        if ( con.isMaxAgeExpired() ) {
            return true;
        } else {
            return false;
        }
    }
    protected void returnConnection ( PooledConnection con ) {
        if ( isClosed() ) {
            release ( con );
            return;
        }
        if ( con != null ) {
            try {
                con.lock();
                if ( con.isSuspect() ) {
                    if ( poolProperties.isLogAbandoned() && log.isInfoEnabled() ) {
                        log.info ( "Connection(" + con + ") that has been marked suspect was returned."
                                   + " The processing time is " + ( System.currentTimeMillis() - con.getTimestamp() ) + " ms." );
                    }
                    if ( jmxPool != null ) {
                        jmxPool.notify ( org.apache.tomcat.jdbc.pool.jmx.ConnectionPool.SUSPECT_RETURNED_NOTIFICATION,
                                         "Connection(" + con + ") that has been marked suspect was returned." );
                    }
                }
                if ( busy.remove ( con ) ) {
                    if ( !shouldClose ( con, PooledConnection.VALIDATE_RETURN ) ) {
                        con.setStackTrace ( null );
                        con.setTimestamp ( System.currentTimeMillis() );
                        if ( ( ( idle.size() >= poolProperties.getMaxIdle() ) && !poolProperties.isPoolSweeperEnabled() ) || ( !idle.offer ( con ) ) ) {
                            if ( log.isDebugEnabled() ) {
                                log.debug ( "Connection [" + con + "] will be closed and not returned to the pool, idle[" + idle.size() + "]>=maxIdle[" + poolProperties.getMaxIdle() + "] idle.offer failed." );
                            }
                            release ( con );
                        }
                    } else {
                        if ( log.isDebugEnabled() ) {
                            log.debug ( "Connection [" + con + "] will be closed and not returned to the pool." );
                        }
                        release ( con );
                    }
                } else {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( "Connection [" + con + "] will be closed and not returned to the pool, busy.remove failed." );
                    }
                    release ( con );
                }
            } finally {
                con.unlock();
            }
        }
    }
    protected boolean shouldAbandon() {
        if ( !poolProperties.isRemoveAbandoned() ) {
            return false;
        }
        if ( poolProperties.getAbandonWhenPercentageFull() == 0 ) {
            return true;
        }
        float used = busy.size();
        float max  = poolProperties.getMaxActive();
        float perc = poolProperties.getAbandonWhenPercentageFull();
        return ( used / max * 100f ) >= perc;
    }
    public void checkAbandoned() {
        try {
            if ( busy.size() == 0 ) {
                return;
            }
            Iterator<PooledConnection> locked = busy.iterator();
            int sto = getPoolProperties().getSuspectTimeout();
            while ( locked.hasNext() ) {
                PooledConnection con = locked.next();
                boolean setToNull = false;
                try {
                    con.lock();
                    if ( idle.contains ( con ) || con.isReleased() ) {
                        continue;
                    }
                    long time = con.getTimestamp();
                    long now = System.currentTimeMillis();
                    if ( shouldAbandon() && ( now - time ) > con.getAbandonTimeout() ) {
                        busy.remove ( con );
                        abandon ( con );
                        setToNull = true;
                    } else if ( sto > 0 && ( now - time ) > ( sto * 1000L ) ) {
                        suspect ( con );
                    } else {
                    }
                } finally {
                    con.unlock();
                    if ( setToNull ) {
                        con = null;
                    }
                }
            }
        } catch ( ConcurrentModificationException e ) {
            log.debug ( "checkAbandoned failed." , e );
        } catch ( Exception e ) {
            log.warn ( "checkAbandoned failed, it will be retried.", e );
        }
    }
    public void checkIdle() {
        checkIdle ( false );
    }
    public void checkIdle ( boolean ignoreMinSize ) {
        try {
            if ( idle.size() == 0 ) {
                return;
            }
            long now = System.currentTimeMillis();
            Iterator<PooledConnection> unlocked = idle.iterator();
            while ( ( ignoreMinSize || ( idle.size() >= getPoolProperties().getMinIdle() ) ) && unlocked.hasNext() ) {
                PooledConnection con = unlocked.next();
                boolean setToNull = false;
                try {
                    con.lock();
                    if ( busy.contains ( con ) ) {
                        continue;
                    }
                    long time = con.getTimestamp();
                    if ( shouldReleaseIdle ( now, con, time ) ) {
                        release ( con );
                        idle.remove ( con );
                        setToNull = true;
                    } else {
                    }
                } finally {
                    con.unlock();
                    if ( setToNull ) {
                        con = null;
                    }
                }
            }
        } catch ( ConcurrentModificationException e ) {
            log.debug ( "checkIdle failed." , e );
        } catch ( Exception e ) {
            log.warn ( "checkIdle failed, it will be retried.", e );
        }
    }
    protected boolean shouldReleaseIdle ( long now, PooledConnection con, long time ) {
        if ( con.getConnectionVersion() < getPoolVersion() ) {
            return true;
        } else {
            return ( con.getReleaseTime() > 0 ) && ( ( now - time ) > con.getReleaseTime() ) && ( getSize() > getPoolProperties().getMinIdle() );
        }
    }
    public void testAllIdle() {
        try {
            if ( idle.size() == 0 ) {
                return;
            }
            Iterator<PooledConnection> unlocked = idle.iterator();
            while ( unlocked.hasNext() ) {
                PooledConnection con = unlocked.next();
                try {
                    con.lock();
                    if ( busy.contains ( con ) ) {
                        continue;
                    }
                    if ( !con.validate ( PooledConnection.VALIDATE_IDLE ) ) {
                        idle.remove ( con );
                        release ( con );
                    }
                } finally {
                    con.unlock();
                }
            }
        } catch ( ConcurrentModificationException e ) {
            log.debug ( "testAllIdle failed." , e );
        } catch ( Exception e ) {
            log.warn ( "testAllIdle failed, it will be retried.", e );
        }
    }
    protected static String getThreadDump() {
        Exception x = new Exception();
        x.fillInStackTrace();
        return getStackTrace ( x );
    }
    public static String getStackTrace ( Throwable x ) {
        if ( x == null ) {
            return null;
        } else {
            java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();
            java.io.PrintStream writer = new java.io.PrintStream ( bout );
            x.printStackTrace ( writer );
            String result = bout.toString();
            return ( x.getMessage() != null && x.getMessage().length() > 0 ) ? x.getMessage() + ";" + result : result;
        }
    }
    protected PooledConnection create ( boolean incrementCounter ) {
        if ( incrementCounter ) {
            size.incrementAndGet();
        }
        PooledConnection con = new PooledConnection ( getPoolProperties(), this );
        return con;
    }
    public void purge() {
        purgeOnReturn();
        checkIdle ( true );
    }
    public void purgeOnReturn() {
        poolVersion.incrementAndGet();
    }
    protected void finalize ( PooledConnection con ) {
        JdbcInterceptor handler = con.getHandler();
        while ( handler != null ) {
            handler.reset ( null, null );
            handler = handler.getNext();
        }
    }
    protected void disconnectEvent ( PooledConnection con, boolean finalizing ) {
        JdbcInterceptor handler = con.getHandler();
        while ( handler != null ) {
            handler.disconnected ( this, con, finalizing );
            handler = handler.getNext();
        }
    }
    public org.apache.tomcat.jdbc.pool.jmx.ConnectionPool getJmxPool() {
        return jmxPool;
    }
    protected void createMBean() {
        try {
            jmxPool = new org.apache.tomcat.jdbc.pool.jmx.ConnectionPool ( this );
        } catch ( Exception x ) {
            log.warn ( "Unable to start JMX integration for connection pool. Instance[" + getName() + "] can't be monitored.", x );
        }
    }
    protected class ConnectionFuture implements Future<Connection>, Runnable {
        Future<PooledConnection> pcFuture = null;
        AtomicBoolean configured = new AtomicBoolean ( false );
        CountDownLatch latch = new CountDownLatch ( 1 );
        volatile Connection result = null;
        SQLException cause = null;
        AtomicBoolean cancelled = new AtomicBoolean ( false );
        volatile PooledConnection pc = null;
        public ConnectionFuture ( Future<PooledConnection> pcf ) {
            this.pcFuture = pcf;
        }
        public ConnectionFuture ( PooledConnection pc ) throws SQLException {
            this.pc = pc;
            result = ConnectionPool.this.setupConnection ( pc );
            configured.set ( true );
        }
        @Override
        public boolean cancel ( boolean mayInterruptIfRunning ) {
            if ( pc != null ) {
                return false;
            } else if ( ( !cancelled.get() ) && cancelled.compareAndSet ( false, true ) ) {
                ConnectionPool.this.cancellator.execute ( this );
            }
            return true;
        }
        @Override
        public Connection get() throws InterruptedException, ExecutionException {
            try {
                return get ( Long.MAX_VALUE, TimeUnit.MILLISECONDS );
            } catch ( TimeoutException x ) {
                throw new ExecutionException ( x );
            }
        }
        @Override
        public Connection get ( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
            PooledConnection pc = this.pc != null ? this.pc : pcFuture.get ( timeout, unit );
            if ( pc != null ) {
                if ( result != null ) {
                    return result;
                }
                if ( configured.compareAndSet ( false, true ) ) {
                    try {
                        pc = borrowConnection ( System.currentTimeMillis(), pc, null, null );
                        result = ConnectionPool.this.setupConnection ( pc );
                    } catch ( SQLException x ) {
                        cause = x;
                    } finally {
                        latch.countDown();
                    }
                } else {
                    latch.await ( timeout, unit );
                }
                if ( result == null ) {
                    throw new ExecutionException ( cause );
                }
                return result;
            } else {
                return null;
            }
        }
        @Override
        public boolean isCancelled() {
            return pc == null && ( pcFuture.isCancelled() || cancelled.get() );
        }
        @Override
        public boolean isDone() {
            return pc != null || pcFuture.isDone();
        }
        @Override
        public void run() {
            try {
                Connection con = get();
                con.close();
            } catch ( ExecutionException ex ) {
            } catch ( Exception x ) {
                ConnectionPool.log.error ( "Unable to cancel ConnectionFuture.", x );
            }
        }
    }
    private static volatile Timer poolCleanTimer = null;
    private static HashSet<PoolCleaner> cleaners = new HashSet<>();
    private static synchronized void registerCleaner ( PoolCleaner cleaner ) {
        unregisterCleaner ( cleaner );
        cleaners.add ( cleaner );
        if ( poolCleanTimer == null ) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader ( ConnectionPool.class.getClassLoader() );
                PrivilegedAction<Timer> pa = new PrivilegedNewTimer();
                poolCleanTimer = AccessController.doPrivileged ( pa );
            } finally {
                Thread.currentThread().setContextClassLoader ( loader );
            }
        }
        poolCleanTimer.schedule ( cleaner, cleaner.sleepTime, cleaner.sleepTime );
    }
    private static synchronized void unregisterCleaner ( PoolCleaner cleaner ) {
        boolean removed = cleaners.remove ( cleaner );
        if ( removed ) {
            cleaner.cancel();
            if ( poolCleanTimer != null ) {
                poolCleanTimer.purge();
                if ( cleaners.size() == 0 ) {
                    poolCleanTimer.cancel();
                    poolCleanTimer = null;
                }
            }
        }
    }
    private static class PrivilegedNewTimer implements PrivilegedAction<Timer> {
        @Override
        public Timer run() {
            return new Timer ( "Tomcat JDBC Pool Cleaner[" + System.identityHashCode ( ConnectionPool.class.getClassLoader() ) + ":" +
                               System.currentTimeMillis() + "]", true );
        }
    }
    public static Set<TimerTask> getPoolCleaners() {
        return Collections.<TimerTask>unmodifiableSet ( cleaners );
    }
    public long getPoolVersion() {
        return poolVersion.get();
    }
    public static Timer getPoolTimer() {
        return poolCleanTimer;
    }
    protected static class PoolCleaner extends TimerTask {
        protected WeakReference<ConnectionPool> pool;
        protected long sleepTime;
        PoolCleaner ( ConnectionPool pool, long sleepTime ) {
            this.pool = new WeakReference<> ( pool );
            this.sleepTime = sleepTime;
            if ( sleepTime <= 0 ) {
                log.warn ( "Database connection pool evicter thread interval is set to 0, defaulting to 30 seconds" );
                this.sleepTime = 1000 * 30;
            } else if ( sleepTime < 1000 ) {
                log.warn ( "Database connection pool evicter thread interval is set to lower than 1 second." );
            }
        }
        @Override
        public void run() {
            ConnectionPool pool = this.pool.get();
            if ( pool == null ) {
                stopRunning();
            } else if ( !pool.isClosed() ) {
                try {
                    if ( pool.getPoolProperties().isRemoveAbandoned()
                            || pool.getPoolProperties().getSuspectTimeout() > 0 ) {
                        pool.checkAbandoned();
                    }
                    if ( pool.getPoolProperties().getMinIdle() < pool.idle
                            .size() ) {
                        pool.checkIdle();
                    }
                    if ( pool.getPoolProperties().isTestWhileIdle() ) {
                        pool.testAllIdle();
                    }
                } catch ( Exception x ) {
                    log.error ( "", x );
                }
            }
        }
        public void start() {
            registerCleaner ( this );
        }
        public void stopRunning() {
            unregisterCleaner ( this );
        }
    }
}
