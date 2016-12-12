package org.apache.tomcat.dbcp.pool2.impl;
import java.util.Deque;
import java.util.Iterator;
import java.util.Arrays;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MalformedObjectNameException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import java.lang.management.ManagementFactory;
import org.apache.tomcat.dbcp.pool2.PooledObject;
import java.util.TimerTask;
import org.apache.tomcat.dbcp.pool2.SwallowedExceptionListener;
import java.util.concurrent.atomic.AtomicLong;
import javax.management.ObjectName;
import java.lang.ref.WeakReference;
import org.apache.tomcat.dbcp.pool2.BaseObject;
public abstract class BaseGenericObjectPool<T> extends BaseObject {
    public static final int MEAN_TIMING_STATS_CACHE_SIZE = 100;
    private volatile int maxTotal;
    private volatile boolean blockWhenExhausted;
    private volatile long maxWaitMillis;
    private volatile boolean lifo;
    private final boolean fairness;
    private volatile boolean testOnCreate;
    private volatile boolean testOnBorrow;
    private volatile boolean testOnReturn;
    private volatile boolean testWhileIdle;
    private volatile long timeBetweenEvictionRunsMillis;
    private volatile int numTestsPerEvictionRun;
    private volatile long minEvictableIdleTimeMillis;
    private volatile long softMinEvictableIdleTimeMillis;
    private volatile EvictionPolicy<T> evictionPolicy;
    final Object closeLock;
    volatile boolean closed;
    final Object evictionLock;
    private Evictor evictor;
    EvictionIterator evictionIterator;
    private final WeakReference<ClassLoader> factoryClassLoader;
    private final ObjectName oname;
    private final String creationStackTrace;
    private final AtomicLong borrowedCount;
    private final AtomicLong returnedCount;
    final AtomicLong createdCount;
    final AtomicLong destroyedCount;
    final AtomicLong destroyedByEvictorCount;
    final AtomicLong destroyedByBorrowValidationCount;
    private final StatsStore activeTimes;
    private final StatsStore idleTimes;
    private final StatsStore waitTimes;
    private final AtomicLong maxBorrowWaitTimeMillis;
    private volatile SwallowedExceptionListener swallowedExceptionListener;
    public BaseGenericObjectPool ( final BaseObjectPoolConfig config, final String jmxNameBase, final String jmxNamePrefix ) {
        this.maxTotal = -1;
        this.blockWhenExhausted = true;
        this.maxWaitMillis = -1L;
        this.lifo = true;
        this.testOnCreate = false;
        this.testOnBorrow = false;
        this.testOnReturn = false;
        this.testWhileIdle = false;
        this.timeBetweenEvictionRunsMillis = -1L;
        this.numTestsPerEvictionRun = 3;
        this.minEvictableIdleTimeMillis = 1800000L;
        this.softMinEvictableIdleTimeMillis = -1L;
        this.closeLock = new Object();
        this.closed = false;
        this.evictionLock = new Object();
        this.evictor = null;
        this.evictionIterator = null;
        this.borrowedCount = new AtomicLong ( 0L );
        this.returnedCount = new AtomicLong ( 0L );
        this.createdCount = new AtomicLong ( 0L );
        this.destroyedCount = new AtomicLong ( 0L );
        this.destroyedByEvictorCount = new AtomicLong ( 0L );
        this.destroyedByBorrowValidationCount = new AtomicLong ( 0L );
        this.activeTimes = new StatsStore ( 100 );
        this.idleTimes = new StatsStore ( 100 );
        this.waitTimes = new StatsStore ( 100 );
        this.maxBorrowWaitTimeMillis = new AtomicLong ( 0L );
        this.swallowedExceptionListener = null;
        if ( config.getJmxEnabled() ) {
            this.oname = this.jmxRegister ( config, jmxNameBase, jmxNamePrefix );
        } else {
            this.oname = null;
        }
        this.creationStackTrace = this.getStackTrace ( new Exception() );
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if ( cl == null ) {
            this.factoryClassLoader = null;
        } else {
            this.factoryClassLoader = new WeakReference<ClassLoader> ( cl );
        }
        this.fairness = config.getFairness();
    }
    public final int getMaxTotal() {
        return this.maxTotal;
    }
    public final void setMaxTotal ( final int maxTotal ) {
        this.maxTotal = maxTotal;
    }
    public final boolean getBlockWhenExhausted() {
        return this.blockWhenExhausted;
    }
    public final void setBlockWhenExhausted ( final boolean blockWhenExhausted ) {
        this.blockWhenExhausted = blockWhenExhausted;
    }
    public final long getMaxWaitMillis() {
        return this.maxWaitMillis;
    }
    public final void setMaxWaitMillis ( final long maxWaitMillis ) {
        this.maxWaitMillis = maxWaitMillis;
    }
    public final boolean getLifo() {
        return this.lifo;
    }
    public final boolean getFairness() {
        return this.fairness;
    }
    public final void setLifo ( final boolean lifo ) {
        this.lifo = lifo;
    }
    public final boolean getTestOnCreate() {
        return this.testOnCreate;
    }
    public final void setTestOnCreate ( final boolean testOnCreate ) {
        this.testOnCreate = testOnCreate;
    }
    public final boolean getTestOnBorrow() {
        return this.testOnBorrow;
    }
    public final void setTestOnBorrow ( final boolean testOnBorrow ) {
        this.testOnBorrow = testOnBorrow;
    }
    public final boolean getTestOnReturn() {
        return this.testOnReturn;
    }
    public final void setTestOnReturn ( final boolean testOnReturn ) {
        this.testOnReturn = testOnReturn;
    }
    public final boolean getTestWhileIdle() {
        return this.testWhileIdle;
    }
    public final void setTestWhileIdle ( final boolean testWhileIdle ) {
        this.testWhileIdle = testWhileIdle;
    }
    public final long getTimeBetweenEvictionRunsMillis() {
        return this.timeBetweenEvictionRunsMillis;
    }
    public final void setTimeBetweenEvictionRunsMillis ( final long timeBetweenEvictionRunsMillis ) {
        this.startEvictor ( this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis );
    }
    public final int getNumTestsPerEvictionRun() {
        return this.numTestsPerEvictionRun;
    }
    public final void setNumTestsPerEvictionRun ( final int numTestsPerEvictionRun ) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }
    public final long getMinEvictableIdleTimeMillis() {
        return this.minEvictableIdleTimeMillis;
    }
    public final void setMinEvictableIdleTimeMillis ( final long minEvictableIdleTimeMillis ) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }
    public final long getSoftMinEvictableIdleTimeMillis() {
        return this.softMinEvictableIdleTimeMillis;
    }
    public final void setSoftMinEvictableIdleTimeMillis ( final long softMinEvictableIdleTimeMillis ) {
        this.softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
    }
    public final String getEvictionPolicyClassName() {
        return this.evictionPolicy.getClass().getName();
    }
    public final void setEvictionPolicyClassName ( final String evictionPolicyClassName ) {
        try {
            Class<?> clazz;
            try {
                clazz = Class.forName ( evictionPolicyClassName, true, Thread.currentThread().getContextClassLoader() );
            } catch ( ClassNotFoundException e4 ) {
                clazz = Class.forName ( evictionPolicyClassName );
            }
            final Object policy = clazz.newInstance();
            if ( policy instanceof EvictionPolicy ) {
                final EvictionPolicy<T> evicPolicy = ( EvictionPolicy<T> ) policy;
                this.evictionPolicy = evicPolicy;
            }
        } catch ( ClassNotFoundException e ) {
            throw new IllegalArgumentException ( "Unable to create EvictionPolicy instance of type " + evictionPolicyClassName, e );
        } catch ( InstantiationException e2 ) {
            throw new IllegalArgumentException ( "Unable to create EvictionPolicy instance of type " + evictionPolicyClassName, e2 );
        } catch ( IllegalAccessException e3 ) {
            throw new IllegalArgumentException ( "Unable to create EvictionPolicy instance of type " + evictionPolicyClassName, e3 );
        }
    }
    public abstract void close();
    public final boolean isClosed() {
        return this.closed;
    }
    public abstract void evict() throws Exception;
    protected EvictionPolicy<T> getEvictionPolicy() {
        return this.evictionPolicy;
    }
    final void assertOpen() throws IllegalStateException {
        if ( this.isClosed() ) {
            throw new IllegalStateException ( "Pool not open" );
        }
    }
    final void startEvictor ( final long delay ) {
        synchronized ( this.evictionLock ) {
            if ( null != this.evictor ) {
                EvictionTimer.cancel ( this.evictor );
                this.evictor = null;
                this.evictionIterator = null;
            }
            if ( delay > 0L ) {
                EvictionTimer.schedule ( this.evictor = new Evictor(), delay, delay );
            }
        }
    }
    abstract void ensureMinIdle() throws Exception;
    public final ObjectName getJmxName() {
        return this.oname;
    }
    public final String getCreationStackTrace() {
        return this.creationStackTrace;
    }
    public final long getBorrowedCount() {
        return this.borrowedCount.get();
    }
    public final long getReturnedCount() {
        return this.returnedCount.get();
    }
    public final long getCreatedCount() {
        return this.createdCount.get();
    }
    public final long getDestroyedCount() {
        return this.destroyedCount.get();
    }
    public final long getDestroyedByEvictorCount() {
        return this.destroyedByEvictorCount.get();
    }
    public final long getDestroyedByBorrowValidationCount() {
        return this.destroyedByBorrowValidationCount.get();
    }
    public final long getMeanActiveTimeMillis() {
        return this.activeTimes.getMean();
    }
    public final long getMeanIdleTimeMillis() {
        return this.idleTimes.getMean();
    }
    public final long getMeanBorrowWaitTimeMillis() {
        return this.waitTimes.getMean();
    }
    public final long getMaxBorrowWaitTimeMillis() {
        return this.maxBorrowWaitTimeMillis.get();
    }
    public abstract int getNumIdle();
    public final SwallowedExceptionListener getSwallowedExceptionListener() {
        return this.swallowedExceptionListener;
    }
    public final void setSwallowedExceptionListener ( final SwallowedExceptionListener swallowedExceptionListener ) {
        this.swallowedExceptionListener = swallowedExceptionListener;
    }
    final void swallowException ( final Exception e ) {
        final SwallowedExceptionListener listener = this.getSwallowedExceptionListener();
        if ( listener == null ) {
            return;
        }
        try {
            listener.onSwallowException ( e );
        } catch ( OutOfMemoryError oome ) {
            throw oome;
        } catch ( VirtualMachineError vme ) {
            throw vme;
        } catch ( Throwable t ) {}
    }
    final void updateStatsBorrow ( final PooledObject<T> p, final long waitTime ) {
        this.borrowedCount.incrementAndGet();
        this.idleTimes.add ( p.getIdleTimeMillis() );
        this.waitTimes.add ( waitTime );
        long currentMax;
        do {
            currentMax = this.maxBorrowWaitTimeMillis.get();
            if ( currentMax >= waitTime ) {
                break;
            }
        } while ( !this.maxBorrowWaitTimeMillis.compareAndSet ( currentMax, waitTime ) );
    }
    final void updateStatsReturn ( final long activeTime ) {
        this.returnedCount.incrementAndGet();
        this.activeTimes.add ( activeTime );
    }
    final void jmxUnregister() {
        if ( this.oname != null ) {
            try {
                ManagementFactory.getPlatformMBeanServer().unregisterMBean ( this.oname );
            } catch ( MBeanRegistrationException e ) {
                this.swallowException ( e );
            } catch ( InstanceNotFoundException e2 ) {
                this.swallowException ( e2 );
            }
        }
    }
    private ObjectName jmxRegister ( final BaseObjectPoolConfig config, final String jmxNameBase, String jmxNamePrefix ) {
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
            } catch ( MalformedObjectNameException e ) {
                if ( "pool".equals ( jmxNamePrefix ) && jmxNameBase.equals ( base ) ) {
                    registered = true;
                } else {
                    jmxNamePrefix = "pool";
                    base = jmxNameBase;
                }
            } catch ( InstanceAlreadyExistsException e2 ) {
                ++i;
            } catch ( MBeanRegistrationException e3 ) {
                registered = true;
            } catch ( NotCompliantMBeanException e4 ) {
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
    @Override
    protected void toStringAppendFields ( final StringBuilder builder ) {
        builder.append ( "maxTotal=" );
        builder.append ( this.maxTotal );
        builder.append ( ", blockWhenExhausted=" );
        builder.append ( this.blockWhenExhausted );
        builder.append ( ", maxWaitMillis=" );
        builder.append ( this.maxWaitMillis );
        builder.append ( ", lifo=" );
        builder.append ( this.lifo );
        builder.append ( ", fairness=" );
        builder.append ( this.fairness );
        builder.append ( ", testOnCreate=" );
        builder.append ( this.testOnCreate );
        builder.append ( ", testOnBorrow=" );
        builder.append ( this.testOnBorrow );
        builder.append ( ", testOnReturn=" );
        builder.append ( this.testOnReturn );
        builder.append ( ", testWhileIdle=" );
        builder.append ( this.testWhileIdle );
        builder.append ( ", timeBetweenEvictionRunsMillis=" );
        builder.append ( this.timeBetweenEvictionRunsMillis );
        builder.append ( ", numTestsPerEvictionRun=" );
        builder.append ( this.numTestsPerEvictionRun );
        builder.append ( ", minEvictableIdleTimeMillis=" );
        builder.append ( this.minEvictableIdleTimeMillis );
        builder.append ( ", softMinEvictableIdleTimeMillis=" );
        builder.append ( this.softMinEvictableIdleTimeMillis );
        builder.append ( ", evictionPolicy=" );
        builder.append ( this.evictionPolicy );
        builder.append ( ", closeLock=" );
        builder.append ( this.closeLock );
        builder.append ( ", closed=" );
        builder.append ( this.closed );
        builder.append ( ", evictionLock=" );
        builder.append ( this.evictionLock );
        builder.append ( ", evictor=" );
        builder.append ( this.evictor );
        builder.append ( ", evictionIterator=" );
        builder.append ( this.evictionIterator );
        builder.append ( ", factoryClassLoader=" );
        builder.append ( this.factoryClassLoader );
        builder.append ( ", oname=" );
        builder.append ( this.oname );
        builder.append ( ", creationStackTrace=" );
        builder.append ( this.creationStackTrace );
        builder.append ( ", borrowedCount=" );
        builder.append ( this.borrowedCount );
        builder.append ( ", returnedCount=" );
        builder.append ( this.returnedCount );
        builder.append ( ", createdCount=" );
        builder.append ( this.createdCount );
        builder.append ( ", destroyedCount=" );
        builder.append ( this.destroyedCount );
        builder.append ( ", destroyedByEvictorCount=" );
        builder.append ( this.destroyedByEvictorCount );
        builder.append ( ", destroyedByBorrowValidationCount=" );
        builder.append ( this.destroyedByBorrowValidationCount );
        builder.append ( ", activeTimes=" );
        builder.append ( this.activeTimes );
        builder.append ( ", idleTimes=" );
        builder.append ( this.idleTimes );
        builder.append ( ", waitTimes=" );
        builder.append ( this.waitTimes );
        builder.append ( ", maxBorrowWaitTimeMillis=" );
        builder.append ( this.maxBorrowWaitTimeMillis );
        builder.append ( ", swallowedExceptionListener=" );
        builder.append ( this.swallowedExceptionListener );
    }
    class Evictor extends TimerTask {
        @Override
        public void run() {
            final ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                if ( BaseGenericObjectPool.this.factoryClassLoader != null ) {
                    final ClassLoader cl = ( ClassLoader ) BaseGenericObjectPool.this.factoryClassLoader.get();
                    if ( cl == null ) {
                        this.cancel();
                        return;
                    }
                    Thread.currentThread().setContextClassLoader ( cl );
                }
                try {
                    BaseGenericObjectPool.this.evict();
                } catch ( Exception e ) {
                    BaseGenericObjectPool.this.swallowException ( e );
                } catch ( OutOfMemoryError oome ) {
                    oome.printStackTrace ( System.err );
                }
                try {
                    BaseGenericObjectPool.this.ensureMinIdle();
                } catch ( Exception e ) {
                    BaseGenericObjectPool.this.swallowException ( e );
                }
            } finally {
                Thread.currentThread().setContextClassLoader ( savedClassLoader );
            }
        }
    }
    private class StatsStore {
        private final AtomicLong[] values;
        private final int size;
        private int index;
        public StatsStore ( final int size ) {
            this.size = size;
            this.values = new AtomicLong[size];
            for ( int i = 0; i < size; ++i ) {
                this.values[i] = new AtomicLong ( -1L );
            }
        }
        public synchronized void add ( final long value ) {
            this.values[this.index].set ( value );
            ++this.index;
            if ( this.index == this.size ) {
                this.index = 0;
            }
        }
        public long getMean() {
            double result = 0.0;
            int counter = 0;
            for ( int i = 0; i < this.size; ++i ) {
                final long value = this.values[i].get();
                if ( value != -1L ) {
                    ++counter;
                    result = result * ( ( counter - 1 ) / counter ) + value / counter;
                }
            }
            return ( long ) result;
        }
        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append ( "StatsStore [values=" );
            builder.append ( Arrays.toString ( this.values ) );
            builder.append ( ", size=" );
            builder.append ( this.size );
            builder.append ( ", index=" );
            builder.append ( this.index );
            builder.append ( "]" );
            return builder.toString();
        }
    }
    class EvictionIterator implements Iterator<PooledObject<T>> {
        private final Deque<PooledObject<T>> idleObjects;
        private final Iterator<PooledObject<T>> idleObjectIterator;
        EvictionIterator ( final Deque<PooledObject<T>> idleObjects ) {
            this.idleObjects = idleObjects;
            if ( BaseGenericObjectPool.this.getLifo() ) {
                this.idleObjectIterator = idleObjects.descendingIterator();
            } else {
                this.idleObjectIterator = idleObjects.iterator();
            }
        }
        public Deque<PooledObject<T>> getIdleObjects() {
            return this.idleObjects;
        }
        @Override
        public boolean hasNext() {
            return this.idleObjectIterator.hasNext();
        }
        @Override
        public PooledObject<T> next() {
            return this.idleObjectIterator.next();
        }
        @Override
        public void remove() {
            this.idleObjectIterator.remove();
        }
    }
    static class IdentityWrapper<T> {
        private final T instance;
        public IdentityWrapper ( final T instance ) {
            this.instance = instance;
        }
        @Override
        public int hashCode() {
            return System.identityHashCode ( this.instance );
        }
        @Override
        public boolean equals ( final Object other ) {
            return other instanceof IdentityWrapper && ( ( IdentityWrapper ) other ).instance == this.instance;
        }
        public T getObject() {
            return this.instance;
        }
        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append ( "IdentityWrapper [instance=" );
            builder.append ( this.instance );
            builder.append ( "]" );
            return builder.toString();
        }
    }
}
