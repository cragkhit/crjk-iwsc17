package org.apache.tomcat.dbcp.pool2.impl;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.apache.tomcat.dbcp.pool2.BaseObject;
import org.apache.tomcat.dbcp.pool2.PooledObject;
import org.apache.tomcat.dbcp.pool2.SwallowedExceptionListener;
public abstract class BaseGenericObjectPool<T> extends BaseObject {
    public static final int MEAN_TIMING_STATS_CACHE_SIZE = 100;
    private volatile int maxTotal =
        GenericKeyedObjectPoolConfig.DEFAULT_MAX_TOTAL;
    private volatile boolean blockWhenExhausted =
        BaseObjectPoolConfig.DEFAULT_BLOCK_WHEN_EXHAUSTED;
    private volatile long maxWaitMillis =
        BaseObjectPoolConfig.DEFAULT_MAX_WAIT_MILLIS;
    private volatile boolean lifo = BaseObjectPoolConfig.DEFAULT_LIFO;
    private final boolean fairness;
    private volatile boolean testOnCreate =
        BaseObjectPoolConfig.DEFAULT_TEST_ON_CREATE;
    private volatile boolean testOnBorrow =
        BaseObjectPoolConfig.DEFAULT_TEST_ON_BORROW;
    private volatile boolean testOnReturn =
        BaseObjectPoolConfig.DEFAULT_TEST_ON_RETURN;
    private volatile boolean testWhileIdle =
        BaseObjectPoolConfig.DEFAULT_TEST_WHILE_IDLE;
    private volatile long timeBetweenEvictionRunsMillis =
        BaseObjectPoolConfig.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;
    private volatile int numTestsPerEvictionRun =
        BaseObjectPoolConfig.DEFAULT_NUM_TESTS_PER_EVICTION_RUN;
    private volatile long minEvictableIdleTimeMillis =
        BaseObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
    private volatile long softMinEvictableIdleTimeMillis =
        BaseObjectPoolConfig.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
    private volatile EvictionPolicy<T> evictionPolicy;
    final Object closeLock = new Object();
    volatile boolean closed = false;
    final Object evictionLock = new Object();
    private Evictor evictor = null;
    EvictionIterator evictionIterator = null;
    private final WeakReference<ClassLoader> factoryClassLoader;
    private final ObjectName oname;
    private final String creationStackTrace;
    private final AtomicLong borrowedCount = new AtomicLong ( 0 );
    private final AtomicLong returnedCount = new AtomicLong ( 0 );
    final AtomicLong createdCount = new AtomicLong ( 0 );
    final AtomicLong destroyedCount = new AtomicLong ( 0 );
    final AtomicLong destroyedByEvictorCount = new AtomicLong ( 0 );
    final AtomicLong destroyedByBorrowValidationCount = new AtomicLong ( 0 );
    private final StatsStore activeTimes = new StatsStore ( MEAN_TIMING_STATS_CACHE_SIZE );
    private final StatsStore idleTimes = new StatsStore ( MEAN_TIMING_STATS_CACHE_SIZE );
    private final StatsStore waitTimes = new StatsStore ( MEAN_TIMING_STATS_CACHE_SIZE );
    private final AtomicLong maxBorrowWaitTimeMillis = new AtomicLong ( 0L );
    private volatile SwallowedExceptionListener swallowedExceptionListener = null;
    public BaseGenericObjectPool ( final BaseObjectPoolConfig config,
                                   final String jmxNameBase, final String jmxNamePrefix ) {
        if ( config.getJmxEnabled() ) {
            this.oname = jmxRegister ( config, jmxNameBase, jmxNamePrefix );
        } else {
            this.oname = null;
        }
        this.creationStackTrace = getStackTrace ( new Exception() );
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if ( cl == null ) {
            factoryClassLoader = null;
        } else {
            factoryClassLoader = new WeakReference<> ( cl );
        }
        fairness = config.getFairness();
    }
    public final int getMaxTotal() {
        return maxTotal;
    }
    public final void setMaxTotal ( final int maxTotal ) {
        this.maxTotal = maxTotal;
    }
    public final boolean getBlockWhenExhausted() {
        return blockWhenExhausted;
    }
    public final void setBlockWhenExhausted ( final boolean blockWhenExhausted ) {
        this.blockWhenExhausted = blockWhenExhausted;
    }
    public final long getMaxWaitMillis() {
        return maxWaitMillis;
    }
    public final void setMaxWaitMillis ( final long maxWaitMillis ) {
        this.maxWaitMillis = maxWaitMillis;
    }
    public final boolean getLifo() {
        return lifo;
    }
    public final boolean getFairness() {
        return fairness;
    }
    public final void setLifo ( final boolean lifo ) {
        this.lifo = lifo;
    }
    public final boolean getTestOnCreate() {
        return testOnCreate;
    }
    public final void setTestOnCreate ( final boolean testOnCreate ) {
        this.testOnCreate = testOnCreate;
    }
    public final boolean getTestOnBorrow() {
        return testOnBorrow;
    }
    public final void setTestOnBorrow ( final boolean testOnBorrow ) {
        this.testOnBorrow = testOnBorrow;
    }
    public final boolean getTestOnReturn() {
        return testOnReturn;
    }
    public final void setTestOnReturn ( final boolean testOnReturn ) {
        this.testOnReturn = testOnReturn;
    }
    public final boolean getTestWhileIdle() {
        return testWhileIdle;
    }
    public final void setTestWhileIdle ( final boolean testWhileIdle ) {
        this.testWhileIdle = testWhileIdle;
    }
    public final long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }
    public final void setTimeBetweenEvictionRunsMillis (
        final long timeBetweenEvictionRunsMillis ) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        startEvictor ( timeBetweenEvictionRunsMillis );
    }
    public final int getNumTestsPerEvictionRun() {
        return numTestsPerEvictionRun;
    }
    public final void setNumTestsPerEvictionRun ( final int numTestsPerEvictionRun ) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }
    public final long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }
    public final void setMinEvictableIdleTimeMillis (
        final long minEvictableIdleTimeMillis ) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }
    public final long getSoftMinEvictableIdleTimeMillis() {
        return softMinEvictableIdleTimeMillis;
    }
    public final void setSoftMinEvictableIdleTimeMillis (
        final long softMinEvictableIdleTimeMillis ) {
        this.softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
    }
    public final String getEvictionPolicyClassName() {
        return evictionPolicy.getClass().getName();
    }
    public final void setEvictionPolicyClassName (
        final String evictionPolicyClassName ) {
        try {
            Class<?> clazz;
            try {
                clazz = Class.forName ( evictionPolicyClassName, true,
                                        Thread.currentThread().getContextClassLoader() );
            } catch ( final ClassNotFoundException e ) {
                clazz = Class.forName ( evictionPolicyClassName );
            }
            final Object policy = clazz.newInstance();
            if ( policy instanceof EvictionPolicy<?> ) {
                @SuppressWarnings ( "unchecked" )
                final
                EvictionPolicy<T> evicPolicy = ( EvictionPolicy<T> ) policy;
                this.evictionPolicy = evicPolicy;
            }
        } catch ( final ClassNotFoundException e ) {
            throw new IllegalArgumentException (
                "Unable to create EvictionPolicy instance of type " +
                evictionPolicyClassName, e );
        } catch ( final InstantiationException e ) {
            throw new IllegalArgumentException (
                "Unable to create EvictionPolicy instance of type " +
                evictionPolicyClassName, e );
        } catch ( final IllegalAccessException e ) {
            throw new IllegalArgumentException (
                "Unable to create EvictionPolicy instance of type " +
                evictionPolicyClassName, e );
        }
    }
    public abstract void close();
    public final boolean isClosed() {
        return closed;
    }
    public abstract void evict() throws Exception;
    protected EvictionPolicy<T> getEvictionPolicy() {
        return evictionPolicy;
    }
    final void assertOpen() throws IllegalStateException {
        if ( isClosed() ) {
            throw new IllegalStateException ( "Pool not open" );
        }
    }
    final void startEvictor ( final long delay ) {
        synchronized ( evictionLock ) {
            if ( null != evictor ) {
                EvictionTimer.cancel ( evictor );
                evictor = null;
                evictionIterator = null;
            }
            if ( delay > 0 ) {
                evictor = new Evictor();
                EvictionTimer.schedule ( evictor, delay, delay );
            }
        }
    }
    abstract void ensureMinIdle() throws Exception;
    public final ObjectName getJmxName() {
        return oname;
    }
    public final String getCreationStackTrace() {
        return creationStackTrace;
    }
    public final long getBorrowedCount() {
        return borrowedCount.get();
    }
    public final long getReturnedCount() {
        return returnedCount.get();
    }
    public final long getCreatedCount() {
        return createdCount.get();
    }
    public final long getDestroyedCount() {
        return destroyedCount.get();
    }
    public final long getDestroyedByEvictorCount() {
        return destroyedByEvictorCount.get();
    }
    public final long getDestroyedByBorrowValidationCount() {
        return destroyedByBorrowValidationCount.get();
    }
    public final long getMeanActiveTimeMillis() {
        return activeTimes.getMean();
    }
    public final long getMeanIdleTimeMillis() {
        return idleTimes.getMean();
    }
    public final long getMeanBorrowWaitTimeMillis() {
        return waitTimes.getMean();
    }
    public final long getMaxBorrowWaitTimeMillis() {
        return maxBorrowWaitTimeMillis.get();
    }
    public abstract int getNumIdle();
    public final SwallowedExceptionListener getSwallowedExceptionListener() {
        return swallowedExceptionListener;
    }
    public final void setSwallowedExceptionListener (
        final SwallowedExceptionListener swallowedExceptionListener ) {
        this.swallowedExceptionListener = swallowedExceptionListener;
    }
    final void swallowException ( final Exception e ) {
        final SwallowedExceptionListener listener = getSwallowedExceptionListener();
        if ( listener == null ) {
            return;
        }
        try {
            listener.onSwallowException ( e );
        } catch ( final OutOfMemoryError oome ) {
            throw oome;
        } catch ( final VirtualMachineError vme ) {
            throw vme;
        } catch ( final Throwable t ) {
        }
    }
    final void updateStatsBorrow ( final PooledObject<T> p, final long waitTime ) {
        borrowedCount.incrementAndGet();
        idleTimes.add ( p.getIdleTimeMillis() );
        waitTimes.add ( waitTime );
        long currentMax;
        do {
            currentMax = maxBorrowWaitTimeMillis.get();
            if ( currentMax >= waitTime ) {
                break;
            }
        } while ( !maxBorrowWaitTimeMillis.compareAndSet ( currentMax, waitTime ) );
    }
    final void updateStatsReturn ( final long activeTime ) {
        returnedCount.incrementAndGet();
        activeTimes.add ( activeTime );
    }
    final void jmxUnregister() {
        if ( oname != null ) {
            try {
                ManagementFactory.getPlatformMBeanServer().unregisterMBean (
                    oname );
            } catch ( final MBeanRegistrationException e ) {
                swallowException ( e );
            } catch ( final InstanceNotFoundException e ) {
                swallowException ( e );
            }
        }
    }
    private ObjectName jmxRegister ( final BaseObjectPoolConfig config,
                                     final String jmxNameBase, String jmxNamePrefix ) {
        ObjectName objectName = null;
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        int i = 1;
        boolean registered = false;
        String base = config.getJmxNameBase();
        if ( base == null ) {
            base = jmxNameBase;
        }
        while ( !registered ) {
            try {
                ObjectName objName;
                if ( i == 1 ) {
                    objName = new ObjectName ( base + jmxNamePrefix );
                } else {
                    objName = new ObjectName ( base + jmxNamePrefix + i );
                }
                mbs.registerMBean ( this, objName );
                objectName = objName;
                registered = true;
            } catch ( final MalformedObjectNameException e ) {
                if ( BaseObjectPoolConfig.DEFAULT_JMX_NAME_PREFIX.equals (
                            jmxNamePrefix ) && jmxNameBase.equals ( base ) ) {
                    registered = true;
                } else {
                    jmxNamePrefix =
                        BaseObjectPoolConfig.DEFAULT_JMX_NAME_PREFIX;
                    base = jmxNameBase;
                }
            } catch ( final InstanceAlreadyExistsException e ) {
                i++;
            } catch ( final MBeanRegistrationException e ) {
                registered = true;
            } catch ( final NotCompliantMBeanException e ) {
                registered = true;
            }
        }
        return objectName;
    }
    private String getStackTrace ( final Exception e ) {
        final Writer w = new StringWriter();
        final PrintWriter pw = new PrintWriter ( w );
        e.printStackTrace ( pw );
        return w.toString();
    }
    class Evictor extends TimerTask {
        @Override
        public void run() {
            final ClassLoader savedClassLoader =
                Thread.currentThread().getContextClassLoader();
            try {
                if ( factoryClassLoader != null ) {
                    final ClassLoader cl = factoryClassLoader.get();
                    if ( cl == null ) {
                        cancel();
                        return;
                    }
                    Thread.currentThread().setContextClassLoader ( cl );
                }
                try {
                    evict();
                } catch ( final Exception e ) {
                    swallowException ( e );
                } catch ( final OutOfMemoryError oome ) {
                    oome.printStackTrace ( System.err );
                }
                try {
                    ensureMinIdle();
                } catch ( final Exception e ) {
                    swallowException ( e );
                }
            } finally {
                Thread.currentThread().setContextClassLoader ( savedClassLoader );
            }
        }
    }
    private class StatsStore {
        private final AtomicLong values[];
        private final int size;
        private int index;
        public StatsStore ( final int size ) {
            this.size = size;
            values = new AtomicLong[size];
            for ( int i = 0; i < size; i++ ) {
                values[i] = new AtomicLong ( -1 );
            }
        }
        public synchronized void add ( final long value ) {
            values[index].set ( value );
            index++;
            if ( index == size ) {
                index = 0;
            }
        }
        public long getMean() {
            double result = 0;
            int counter = 0;
            for ( int i = 0; i < size; i++ ) {
                final long value = values[i].get();
                if ( value != -1 ) {
                    counter++;
                    result = result * ( ( counter - 1 ) / ( double ) counter ) +
                             value / ( double ) counter;
                }
            }
            return ( long ) result;
        }
        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append ( "StatsStore [values=" );
            builder.append ( Arrays.toString ( values ) );
            builder.append ( ", size=" );
            builder.append ( size );
            builder.append ( ", index=" );
            builder.append ( index );
            builder.append ( "]" );
            return builder.toString();
        }
    }
    class EvictionIterator implements Iterator<PooledObject<T>> {
        private final Deque<PooledObject<T>> idleObjects;
        private final Iterator<PooledObject<T>> idleObjectIterator;
        EvictionIterator ( final Deque<PooledObject<T>> idleObjects ) {
            this.idleObjects = idleObjects;
            if ( getLifo() ) {
                idleObjectIterator = idleObjects.descendingIterator();
            } else {
                idleObjectIterator = idleObjects.iterator();
            }
        }
        public Deque<PooledObject<T>> getIdleObjects() {
            return idleObjects;
        }
        @Override
        public boolean hasNext() {
            return idleObjectIterator.hasNext();
        }
        @Override
        public PooledObject<T> next() {
            return idleObjectIterator.next();
        }
        @Override
        public void remove() {
            idleObjectIterator.remove();
        }
    }
    static class IdentityWrapper<T> {
        private final T instance;
        public IdentityWrapper ( final T instance ) {
            this.instance = instance;
        }
        @Override
        public int hashCode() {
            return System.identityHashCode ( instance );
        }
        @Override
        @SuppressWarnings ( "rawtypes" )
        public boolean equals ( final Object other ) {
            return  other instanceof IdentityWrapper &&
                    ( ( IdentityWrapper ) other ).instance == instance;
        }
        public T getObject() {
            return instance;
        }
        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append ( "IdentityWrapper [instance=" );
            builder.append ( instance );
            builder.append ( "]" );
            return builder.toString();
        }
    }
    @Override
    protected void toStringAppendFields ( final StringBuilder builder ) {
        builder.append ( "maxTotal=" );
        builder.append ( maxTotal );
        builder.append ( ", blockWhenExhausted=" );
        builder.append ( blockWhenExhausted );
        builder.append ( ", maxWaitMillis=" );
        builder.append ( maxWaitMillis );
        builder.append ( ", lifo=" );
        builder.append ( lifo );
        builder.append ( ", fairness=" );
        builder.append ( fairness );
        builder.append ( ", testOnCreate=" );
        builder.append ( testOnCreate );
        builder.append ( ", testOnBorrow=" );
        builder.append ( testOnBorrow );
        builder.append ( ", testOnReturn=" );
        builder.append ( testOnReturn );
        builder.append ( ", testWhileIdle=" );
        builder.append ( testWhileIdle );
        builder.append ( ", timeBetweenEvictionRunsMillis=" );
        builder.append ( timeBetweenEvictionRunsMillis );
        builder.append ( ", numTestsPerEvictionRun=" );
        builder.append ( numTestsPerEvictionRun );
        builder.append ( ", minEvictableIdleTimeMillis=" );
        builder.append ( minEvictableIdleTimeMillis );
        builder.append ( ", softMinEvictableIdleTimeMillis=" );
        builder.append ( softMinEvictableIdleTimeMillis );
        builder.append ( ", evictionPolicy=" );
        builder.append ( evictionPolicy );
        builder.append ( ", closeLock=" );
        builder.append ( closeLock );
        builder.append ( ", closed=" );
        builder.append ( closed );
        builder.append ( ", evictionLock=" );
        builder.append ( evictionLock );
        builder.append ( ", evictor=" );
        builder.append ( evictor );
        builder.append ( ", evictionIterator=" );
        builder.append ( evictionIterator );
        builder.append ( ", factoryClassLoader=" );
        builder.append ( factoryClassLoader );
        builder.append ( ", oname=" );
        builder.append ( oname );
        builder.append ( ", creationStackTrace=" );
        builder.append ( creationStackTrace );
        builder.append ( ", borrowedCount=" );
        builder.append ( borrowedCount );
        builder.append ( ", returnedCount=" );
        builder.append ( returnedCount );
        builder.append ( ", createdCount=" );
        builder.append ( createdCount );
        builder.append ( ", destroyedCount=" );
        builder.append ( destroyedCount );
        builder.append ( ", destroyedByEvictorCount=" );
        builder.append ( destroyedByEvictorCount );
        builder.append ( ", destroyedByBorrowValidationCount=" );
        builder.append ( destroyedByBorrowValidationCount );
        builder.append ( ", activeTimes=" );
        builder.append ( activeTimes );
        builder.append ( ", idleTimes=" );
        builder.append ( idleTimes );
        builder.append ( ", waitTimes=" );
        builder.append ( waitTimes );
        builder.append ( ", maxBorrowWaitTimeMillis=" );
        builder.append ( maxBorrowWaitTimeMillis );
        builder.append ( ", swallowedExceptionListener=" );
        builder.append ( swallowedExceptionListener );
    }
}
