package org.apache.tomcat.dbcp.pool2.impl;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.tomcat.dbcp.pool2.ObjectPool;
import org.apache.tomcat.dbcp.pool2.PoolUtils;
import org.apache.tomcat.dbcp.pool2.PooledObject;
import org.apache.tomcat.dbcp.pool2.PooledObjectFactory;
import org.apache.tomcat.dbcp.pool2.PooledObjectState;
import org.apache.tomcat.dbcp.pool2.UsageTracking;
public class GenericObjectPool<T> extends BaseGenericObjectPool<T>
    implements ObjectPool<T>, GenericObjectPoolMXBean, UsageTracking<T> {
    public GenericObjectPool ( final PooledObjectFactory<T> factory ) {
        this ( factory, new GenericObjectPoolConfig() );
    }
    public GenericObjectPool ( final PooledObjectFactory<T> factory,
                               final GenericObjectPoolConfig config ) {
        super ( config, ONAME_BASE, config.getJmxNamePrefix() );
        if ( factory == null ) {
            jmxUnregister();
            throw new IllegalArgumentException ( "factory may not be null" );
        }
        this.factory = factory;
        idleObjects = new LinkedBlockingDeque<> ( config.getFairness() );
        setConfig ( config );
        startEvictor ( getTimeBetweenEvictionRunsMillis() );
    }
    public GenericObjectPool ( final PooledObjectFactory<T> factory,
                               final GenericObjectPoolConfig config, final AbandonedConfig abandonedConfig ) {
        this ( factory, config );
        setAbandonedConfig ( abandonedConfig );
    }
    @Override
    public int getMaxIdle() {
        return maxIdle;
    }
    public void setMaxIdle ( final int maxIdle ) {
        this.maxIdle = maxIdle;
    }
    public void setMinIdle ( final int minIdle ) {
        this.minIdle = minIdle;
    }
    @Override
    public int getMinIdle() {
        final int maxIdleSave = getMaxIdle();
        if ( this.minIdle > maxIdleSave ) {
            return maxIdleSave;
        }
        return minIdle;
    }
    @Override
    public boolean isAbandonedConfig() {
        return abandonedConfig != null;
    }
    @Override
    public boolean getLogAbandoned() {
        final AbandonedConfig ac = this.abandonedConfig;
        return ac != null && ac.getLogAbandoned();
    }
    @Override
    public boolean getRemoveAbandonedOnBorrow() {
        final AbandonedConfig ac = this.abandonedConfig;
        return ac != null && ac.getRemoveAbandonedOnBorrow();
    }
    @Override
    public boolean getRemoveAbandonedOnMaintenance() {
        final AbandonedConfig ac = this.abandonedConfig;
        return ac != null && ac.getRemoveAbandonedOnMaintenance();
    }
    @Override
    public int getRemoveAbandonedTimeout() {
        final AbandonedConfig ac = this.abandonedConfig;
        return ac != null ? ac.getRemoveAbandonedTimeout() : Integer.MAX_VALUE;
    }
    public void setConfig ( final GenericObjectPoolConfig conf ) {
        setLifo ( conf.getLifo() );
        setMaxIdle ( conf.getMaxIdle() );
        setMinIdle ( conf.getMinIdle() );
        setMaxTotal ( conf.getMaxTotal() );
        setMaxWaitMillis ( conf.getMaxWaitMillis() );
        setBlockWhenExhausted ( conf.getBlockWhenExhausted() );
        setTestOnCreate ( conf.getTestOnCreate() );
        setTestOnBorrow ( conf.getTestOnBorrow() );
        setTestOnReturn ( conf.getTestOnReturn() );
        setTestWhileIdle ( conf.getTestWhileIdle() );
        setNumTestsPerEvictionRun ( conf.getNumTestsPerEvictionRun() );
        setMinEvictableIdleTimeMillis ( conf.getMinEvictableIdleTimeMillis() );
        setTimeBetweenEvictionRunsMillis (
            conf.getTimeBetweenEvictionRunsMillis() );
        setSoftMinEvictableIdleTimeMillis (
            conf.getSoftMinEvictableIdleTimeMillis() );
        setEvictionPolicyClassName ( conf.getEvictionPolicyClassName() );
    }
    public void setAbandonedConfig ( final AbandonedConfig abandonedConfig ) {
        if ( abandonedConfig == null ) {
            this.abandonedConfig = null;
        } else {
            this.abandonedConfig = new AbandonedConfig();
            this.abandonedConfig.setLogAbandoned ( abandonedConfig.getLogAbandoned() );
            this.abandonedConfig.setLogWriter ( abandonedConfig.getLogWriter() );
            this.abandonedConfig.setRemoveAbandonedOnBorrow ( abandonedConfig.getRemoveAbandonedOnBorrow() );
            this.abandonedConfig.setRemoveAbandonedOnMaintenance ( abandonedConfig.getRemoveAbandonedOnMaintenance() );
            this.abandonedConfig.setRemoveAbandonedTimeout ( abandonedConfig.getRemoveAbandonedTimeout() );
            this.abandonedConfig.setUseUsageTracking ( abandonedConfig.getUseUsageTracking() );
        }
    }
    public PooledObjectFactory<T> getFactory() {
        return factory;
    }
    @Override
    public T borrowObject() throws Exception {
        return borrowObject ( getMaxWaitMillis() );
    }
    public T borrowObject ( final long borrowMaxWaitMillis ) throws Exception {
        assertOpen();
        final AbandonedConfig ac = this.abandonedConfig;
        if ( ac != null && ac.getRemoveAbandonedOnBorrow() &&
                ( getNumIdle() < 2 ) &&
                ( getNumActive() > getMaxTotal() - 3 ) ) {
            removeAbandoned ( ac );
        }
        PooledObject<T> p = null;
        final boolean blockWhenExhausted = getBlockWhenExhausted();
        boolean create;
        final long waitTime = System.currentTimeMillis();
        while ( p == null ) {
            create = false;
            p = idleObjects.pollFirst();
            if ( p == null ) {
                p = create();
                if ( p != null ) {
                    create = true;
                }
            }
            if ( blockWhenExhausted ) {
                if ( p == null ) {
                    if ( borrowMaxWaitMillis < 0 ) {
                        p = idleObjects.takeFirst();
                    } else {
                        p = idleObjects.pollFirst ( borrowMaxWaitMillis,
                                                    TimeUnit.MILLISECONDS );
                    }
                }
                if ( p == null ) {
                    throw new NoSuchElementException (
                        "Timeout waiting for idle object" );
                }
            } else {
                if ( p == null ) {
                    throw new NoSuchElementException ( "Pool exhausted" );
                }
            }
            if ( !p.allocate() ) {
                p = null;
            }
            if ( p != null ) {
                try {
                    factory.activateObject ( p );
                } catch ( final Exception e ) {
                    try {
                        destroy ( p );
                    } catch ( final Exception e1 ) {
                    }
                    p = null;
                    if ( create ) {
                        final NoSuchElementException nsee = new NoSuchElementException (
                            "Unable to activate object" );
                        nsee.initCause ( e );
                        throw nsee;
                    }
                }
                if ( p != null && ( getTestOnBorrow() || create && getTestOnCreate() ) ) {
                    boolean validate = false;
                    Throwable validationThrowable = null;
                    try {
                        validate = factory.validateObject ( p );
                    } catch ( final Throwable t ) {
                        PoolUtils.checkRethrow ( t );
                        validationThrowable = t;
                    }
                    if ( !validate ) {
                        try {
                            destroy ( p );
                            destroyedByBorrowValidationCount.incrementAndGet();
                        } catch ( final Exception e ) {
                        }
                        p = null;
                        if ( create ) {
                            final NoSuchElementException nsee = new NoSuchElementException (
                                "Unable to validate object" );
                            nsee.initCause ( validationThrowable );
                            throw nsee;
                        }
                    }
                }
            }
        }
        updateStatsBorrow ( p, System.currentTimeMillis() - waitTime );
        return p.getObject();
    }
    @Override
    public void returnObject ( final T obj ) {
        final PooledObject<T> p = allObjects.get ( new IdentityWrapper<> ( obj ) );
        if ( p == null ) {
            if ( !isAbandonedConfig() ) {
                throw new IllegalStateException (
                    "Returned object not currently part of this pool" );
            }
            return;
        }
        synchronized ( p ) {
            final PooledObjectState state = p.getState();
            if ( state != PooledObjectState.ALLOCATED ) {
                throw new IllegalStateException (
                    "Object has already been returned to this pool or is invalid" );
            }
            p.markReturning();
        }
        final long activeTime = p.getActiveTimeMillis();
        if ( getTestOnReturn() ) {
            if ( !factory.validateObject ( p ) ) {
                try {
                    destroy ( p );
                } catch ( final Exception e ) {
                    swallowException ( e );
                }
                try {
                    ensureIdle ( 1, false );
                } catch ( final Exception e ) {
                    swallowException ( e );
                }
                updateStatsReturn ( activeTime );
                return;
            }
        }
        try {
            factory.passivateObject ( p );
        } catch ( final Exception e1 ) {
            swallowException ( e1 );
            try {
                destroy ( p );
            } catch ( final Exception e ) {
                swallowException ( e );
            }
            try {
                ensureIdle ( 1, false );
            } catch ( final Exception e ) {
                swallowException ( e );
            }
            updateStatsReturn ( activeTime );
            return;
        }
        if ( !p.deallocate() ) {
            throw new IllegalStateException (
                "Object has already been returned to this pool or is invalid" );
        }
        final int maxIdleSave = getMaxIdle();
        if ( isClosed() || maxIdleSave > -1 && maxIdleSave <= idleObjects.size() ) {
            try {
                destroy ( p );
            } catch ( final Exception e ) {
                swallowException ( e );
            }
        } else {
            if ( getLifo() ) {
                idleObjects.addFirst ( p );
            } else {
                idleObjects.addLast ( p );
            }
            if ( isClosed() ) {
                clear();
            }
        }
        updateStatsReturn ( activeTime );
    }
    @Override
    public void invalidateObject ( final T obj ) throws Exception {
        final PooledObject<T> p = allObjects.get ( new IdentityWrapper<> ( obj ) );
        if ( p == null ) {
            if ( isAbandonedConfig() ) {
                return;
            }
            throw new IllegalStateException (
                "Invalidated object not currently part of this pool" );
        }
        synchronized ( p ) {
            if ( p.getState() != PooledObjectState.INVALID ) {
                destroy ( p );
            }
        }
        ensureIdle ( 1, false );
    }
    @Override
    public void clear() {
        PooledObject<T> p = idleObjects.poll();
        while ( p != null ) {
            try {
                destroy ( p );
            } catch ( final Exception e ) {
                swallowException ( e );
            }
            p = idleObjects.poll();
        }
    }
    @Override
    public int getNumActive() {
        return allObjects.size() - idleObjects.size();
    }
    @Override
    public int getNumIdle() {
        return idleObjects.size();
    }
    @Override
    public void close() {
        if ( isClosed() ) {
            return;
        }
        synchronized ( closeLock ) {
            if ( isClosed() ) {
                return;
            }
            startEvictor ( -1L );
            closed = true;
            clear();
            jmxUnregister();
            idleObjects.interuptTakeWaiters();
        }
    }
    @Override
    public void evict() throws Exception {
        assertOpen();
        if ( idleObjects.size() > 0 ) {
            PooledObject<T> underTest = null;
            final EvictionPolicy<T> evictionPolicy = getEvictionPolicy();
            synchronized ( evictionLock ) {
                final EvictionConfig evictionConfig = new EvictionConfig (
                    getMinEvictableIdleTimeMillis(),
                    getSoftMinEvictableIdleTimeMillis(),
                    getMinIdle() );
                final boolean testWhileIdle = getTestWhileIdle();
                for ( int i = 0, m = getNumTests(); i < m; i++ ) {
                    if ( evictionIterator == null || !evictionIterator.hasNext() ) {
                        evictionIterator = new EvictionIterator ( idleObjects );
                    }
                    if ( !evictionIterator.hasNext() ) {
                        return;
                    }
                    try {
                        underTest = evictionIterator.next();
                    } catch ( final NoSuchElementException nsee ) {
                        i--;
                        evictionIterator = null;
                        continue;
                    }
                    if ( !underTest.startEvictionTest() ) {
                        i--;
                        continue;
                    }
                    boolean evict;
                    try {
                        evict = evictionPolicy.evict ( evictionConfig, underTest,
                                                       idleObjects.size() );
                    } catch ( final Throwable t ) {
                        PoolUtils.checkRethrow ( t );
                        swallowException ( new Exception ( t ) );
                        evict = false;
                    }
                    if ( evict ) {
                        destroy ( underTest );
                        destroyedByEvictorCount.incrementAndGet();
                    } else {
                        if ( testWhileIdle ) {
                            boolean active = false;
                            try {
                                factory.activateObject ( underTest );
                                active = true;
                            } catch ( final Exception e ) {
                                destroy ( underTest );
                                destroyedByEvictorCount.incrementAndGet();
                            }
                            if ( active ) {
                                if ( !factory.validateObject ( underTest ) ) {
                                    destroy ( underTest );
                                    destroyedByEvictorCount.incrementAndGet();
                                } else {
                                    try {
                                        factory.passivateObject ( underTest );
                                    } catch ( final Exception e ) {
                                        destroy ( underTest );
                                        destroyedByEvictorCount.incrementAndGet();
                                    }
                                }
                            }
                        }
                        if ( !underTest.endEvictionTest ( idleObjects ) ) {
                        }
                    }
                }
            }
        }
        final AbandonedConfig ac = this.abandonedConfig;
        if ( ac != null && ac.getRemoveAbandonedOnMaintenance() ) {
            removeAbandoned ( ac );
        }
    }
    public void preparePool() throws Exception {
        if ( getMinIdle() < 1 ) {
            return;
        }
        ensureMinIdle();
    }
    private PooledObject<T> create() throws Exception {
        int localMaxTotal = getMaxTotal();
        if ( localMaxTotal < 0 ) {
            localMaxTotal = Integer.MAX_VALUE;
        }
        Boolean create = null;
        while ( create == null ) {
            synchronized ( makeObjectCountLock ) {
                final long newCreateCount = createCount.incrementAndGet();
                if ( newCreateCount > localMaxTotal ) {
                    createCount.decrementAndGet();
                    if ( makeObjectCount == 0 ) {
                        create = Boolean.FALSE;
                    } else {
                        makeObjectCountLock.wait();
                    }
                } else {
                    makeObjectCount++;
                    create = Boolean.TRUE;
                }
            }
        }
        if ( !create.booleanValue() ) {
            return null;
        }
        final PooledObject<T> p;
        try {
            p = factory.makeObject();
        } catch ( Exception e ) {
            createCount.decrementAndGet();
            throw e;
        } finally {
            synchronized ( makeObjectCountLock ) {
                makeObjectCount--;
                makeObjectCountLock.notifyAll();
            }
        }
        final AbandonedConfig ac = this.abandonedConfig;
        if ( ac != null && ac.getLogAbandoned() ) {
            p.setLogAbandoned ( true );
        }
        createdCount.incrementAndGet();
        allObjects.put ( new IdentityWrapper<> ( p.getObject() ), p );
        return p;
    }
    private void destroy ( final PooledObject<T> toDestroy ) throws Exception {
        toDestroy.invalidate();
        idleObjects.remove ( toDestroy );
        allObjects.remove ( new IdentityWrapper<> ( toDestroy.getObject() ) );
        try {
            factory.destroyObject ( toDestroy );
        } finally {
            destroyedCount.incrementAndGet();
            createCount.decrementAndGet();
        }
    }
    @Override
    void ensureMinIdle() throws Exception {
        ensureIdle ( getMinIdle(), true );
    }
    private void ensureIdle ( final int idleCount, final boolean always ) throws Exception {
        if ( idleCount < 1 || isClosed() || ( !always && !idleObjects.hasTakeWaiters() ) ) {
            return;
        }
        while ( idleObjects.size() < idleCount ) {
            final PooledObject<T> p = create();
            if ( p == null ) {
                break;
            }
            if ( getLifo() ) {
                idleObjects.addFirst ( p );
            } else {
                idleObjects.addLast ( p );
            }
        }
        if ( isClosed() ) {
            clear();
        }
    }
    @Override
    public void addObject() throws Exception {
        assertOpen();
        if ( factory == null ) {
            throw new IllegalStateException (
                "Cannot add objects without a factory." );
        }
        final PooledObject<T> p = create();
        addIdleObject ( p );
    }
    private void addIdleObject ( final PooledObject<T> p ) throws Exception {
        if ( p != null ) {
            factory.passivateObject ( p );
            if ( getLifo() ) {
                idleObjects.addFirst ( p );
            } else {
                idleObjects.addLast ( p );
            }
        }
    }
    private int getNumTests() {
        final int numTestsPerEvictionRun = getNumTestsPerEvictionRun();
        if ( numTestsPerEvictionRun >= 0 ) {
            return Math.min ( numTestsPerEvictionRun, idleObjects.size() );
        }
        return ( int ) ( Math.ceil ( idleObjects.size() /
                                     Math.abs ( ( double ) numTestsPerEvictionRun ) ) );
    }
    private void removeAbandoned ( final AbandonedConfig ac ) {
        final long now = System.currentTimeMillis();
        final long timeout =
            now - ( ac.getRemoveAbandonedTimeout() * 1000L );
        final ArrayList<PooledObject<T>> remove = new ArrayList<>();
        final Iterator<PooledObject<T>> it = allObjects.values().iterator();
        while ( it.hasNext() ) {
            final PooledObject<T> pooledObject = it.next();
            synchronized ( pooledObject ) {
                if ( pooledObject.getState() == PooledObjectState.ALLOCATED &&
                        pooledObject.getLastUsedTime() <= timeout ) {
                    pooledObject.markAbandoned();
                    remove.add ( pooledObject );
                }
            }
        }
        final Iterator<PooledObject<T>> itr = remove.iterator();
        while ( itr.hasNext() ) {
            final PooledObject<T> pooledObject = itr.next();
            if ( ac.getLogAbandoned() ) {
                pooledObject.printStackTrace ( ac.getLogWriter() );
            }
            try {
                invalidateObject ( pooledObject.getObject() );
            } catch ( final Exception e ) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void use ( final T pooledObject ) {
        final AbandonedConfig ac = this.abandonedConfig;
        if ( ac != null && ac.getUseUsageTracking() ) {
            final PooledObject<T> wrapper = allObjects.get ( new IdentityWrapper<> ( pooledObject ) );
            wrapper.use();
        }
    }
    private volatile String factoryType = null;
    @Override
    public int getNumWaiters() {
        if ( getBlockWhenExhausted() ) {
            return idleObjects.getTakeQueueLength();
        }
        return 0;
    }
    @Override
    public String getFactoryType() {
        if ( factoryType == null ) {
            final StringBuilder result = new StringBuilder();
            result.append ( factory.getClass().getName() );
            result.append ( '<' );
            final Class<?> pooledObjectType =
                PoolImplUtils.getFactoryType ( factory.getClass() );
            result.append ( pooledObjectType.getName() );
            result.append ( '>' );
            factoryType = result.toString();
        }
        return factoryType;
    }
    @Override
    public Set<DefaultPooledObjectInfo> listAllObjects() {
        final Set<DefaultPooledObjectInfo> result =
            new HashSet<> ( allObjects.size() );
        for ( final PooledObject<T> p : allObjects.values() ) {
            result.add ( new DefaultPooledObjectInfo ( p ) );
        }
        return result;
    }
    private volatile int maxIdle = GenericObjectPoolConfig.DEFAULT_MAX_IDLE;
    private volatile int minIdle = GenericObjectPoolConfig.DEFAULT_MIN_IDLE;
    private final PooledObjectFactory<T> factory;
    private final Map<IdentityWrapper<T>, PooledObject<T>> allObjects =
        new ConcurrentHashMap<>();
    private final AtomicLong createCount = new AtomicLong ( 0 );
    private long makeObjectCount = 0;
    private final Object makeObjectCountLock = new Object();
    private final LinkedBlockingDeque<PooledObject<T>> idleObjects;
    private static final String ONAME_BASE =
        "org.apache.tomcat.dbcp.pool2:type=GenericObjectPool,name=";
    private volatile AbandonedConfig abandonedConfig = null;
    @Override
    protected void toStringAppendFields ( final StringBuilder builder ) {
        super.toStringAppendFields ( builder );
        builder.append ( ", factoryType=" );
        builder.append ( factoryType );
        builder.append ( ", maxIdle=" );
        builder.append ( maxIdle );
        builder.append ( ", minIdle=" );
        builder.append ( minIdle );
        builder.append ( ", factory=" );
        builder.append ( factory );
        builder.append ( ", allObjects=" );
        builder.append ( allObjects );
        builder.append ( ", createCount=" );
        builder.append ( createCount );
        builder.append ( ", idleObjects=" );
        builder.append ( idleObjects );
        builder.append ( ", abandonedConfig=" );
        builder.append ( abandonedConfig );
    }
}
