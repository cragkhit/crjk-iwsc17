package org.apache.tomcat.dbcp.pool2.impl;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.tomcat.dbcp.pool2.KeyedObjectPool;
import org.apache.tomcat.dbcp.pool2.KeyedPooledObjectFactory;
import org.apache.tomcat.dbcp.pool2.PoolUtils;
import org.apache.tomcat.dbcp.pool2.PooledObject;
import org.apache.tomcat.dbcp.pool2.PooledObjectState;
public class GenericKeyedObjectPool<K, T> extends BaseGenericObjectPool<T>
    implements KeyedObjectPool<K, T>, GenericKeyedObjectPoolMXBean<K> {
    public GenericKeyedObjectPool ( final KeyedPooledObjectFactory<K, T> factory ) {
        this ( factory, new GenericKeyedObjectPoolConfig() );
    }
    public GenericKeyedObjectPool ( final KeyedPooledObjectFactory<K, T> factory,
                                    final GenericKeyedObjectPoolConfig config ) {
        super ( config, ONAME_BASE, config.getJmxNamePrefix() );
        if ( factory == null ) {
            jmxUnregister();
            throw new IllegalArgumentException ( "factory may not be null" );
        }
        this.factory = factory;
        this.fairness = config.getFairness();
        setConfig ( config );
        startEvictor ( getTimeBetweenEvictionRunsMillis() );
    }
    @Override
    public int getMaxTotalPerKey() {
        return maxTotalPerKey;
    }
    public void setMaxTotalPerKey ( final int maxTotalPerKey ) {
        this.maxTotalPerKey = maxTotalPerKey;
    }
    @Override
    public int getMaxIdlePerKey() {
        return maxIdlePerKey;
    }
    public void setMaxIdlePerKey ( final int maxIdlePerKey ) {
        this.maxIdlePerKey = maxIdlePerKey;
    }
    public void setMinIdlePerKey ( final int minIdlePerKey ) {
        this.minIdlePerKey = minIdlePerKey;
    }
    @Override
    public int getMinIdlePerKey() {
        final int maxIdlePerKeySave = getMaxIdlePerKey();
        if ( this.minIdlePerKey > maxIdlePerKeySave ) {
            return maxIdlePerKeySave;
        }
        return minIdlePerKey;
    }
    public void setConfig ( final GenericKeyedObjectPoolConfig conf ) {
        setLifo ( conf.getLifo() );
        setMaxIdlePerKey ( conf.getMaxIdlePerKey() );
        setMaxTotalPerKey ( conf.getMaxTotalPerKey() );
        setMaxTotal ( conf.getMaxTotal() );
        setMinIdlePerKey ( conf.getMinIdlePerKey() );
        setMaxWaitMillis ( conf.getMaxWaitMillis() );
        setBlockWhenExhausted ( conf.getBlockWhenExhausted() );
        setTestOnCreate ( conf.getTestOnCreate() );
        setTestOnBorrow ( conf.getTestOnBorrow() );
        setTestOnReturn ( conf.getTestOnReturn() );
        setTestWhileIdle ( conf.getTestWhileIdle() );
        setNumTestsPerEvictionRun ( conf.getNumTestsPerEvictionRun() );
        setMinEvictableIdleTimeMillis ( conf.getMinEvictableIdleTimeMillis() );
        setSoftMinEvictableIdleTimeMillis (
            conf.getSoftMinEvictableIdleTimeMillis() );
        setTimeBetweenEvictionRunsMillis (
            conf.getTimeBetweenEvictionRunsMillis() );
        setEvictionPolicyClassName ( conf.getEvictionPolicyClassName() );
    }
    public KeyedPooledObjectFactory<K, T> getFactory() {
        return factory;
    }
    @Override
    public T borrowObject ( final K key ) throws Exception {
        return borrowObject ( key, getMaxWaitMillis() );
    }
    public T borrowObject ( final K key, final long borrowMaxWaitMillis ) throws Exception {
        assertOpen();
        PooledObject<T> p = null;
        final boolean blockWhenExhausted = getBlockWhenExhausted();
        boolean create;
        final long waitTime = System.currentTimeMillis();
        final ObjectDeque<T> objectDeque = register ( key );
        try {
            while ( p == null ) {
                create = false;
                p = objectDeque.getIdleObjects().pollFirst();
                if ( p == null ) {
                    p = create ( key );
                    if ( p != null ) {
                        create = true;
                    }
                }
                if ( blockWhenExhausted ) {
                    if ( p == null ) {
                        if ( borrowMaxWaitMillis < 0 ) {
                            p = objectDeque.getIdleObjects().takeFirst();
                        } else {
                            p = objectDeque.getIdleObjects().pollFirst (
                                    borrowMaxWaitMillis, TimeUnit.MILLISECONDS );
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
                        factory.activateObject ( key, p );
                    } catch ( final Exception e ) {
                        try {
                            destroy ( key, p, true );
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
                            validate = factory.validateObject ( key, p );
                        } catch ( final Throwable t ) {
                            PoolUtils.checkRethrow ( t );
                            validationThrowable = t;
                        }
                        if ( !validate ) {
                            try {
                                destroy ( key, p, true );
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
        } finally {
            deregister ( key );
        }
        updateStatsBorrow ( p, System.currentTimeMillis() - waitTime );
        return p.getObject();
    }
    @Override
    public void returnObject ( final K key, final T obj ) {
        final ObjectDeque<T> objectDeque = poolMap.get ( key );
        final PooledObject<T> p = objectDeque.getAllObjects().get ( new IdentityWrapper<> ( obj ) );
        if ( p == null ) {
            throw new IllegalStateException (
                "Returned object not currently part of this pool" );
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
        try {
            if ( getTestOnReturn() ) {
                if ( !factory.validateObject ( key, p ) ) {
                    try {
                        destroy ( key, p, true );
                    } catch ( final Exception e ) {
                        swallowException ( e );
                    }
                    if ( objectDeque.idleObjects.hasTakeWaiters() ) {
                        try {
                            addObject ( key );
                        } catch ( final Exception e ) {
                            swallowException ( e );
                        }
                    }
                    return;
                }
            }
            try {
                factory.passivateObject ( key, p );
            } catch ( final Exception e1 ) {
                swallowException ( e1 );
                try {
                    destroy ( key, p, true );
                } catch ( final Exception e ) {
                    swallowException ( e );
                }
                if ( objectDeque.idleObjects.hasTakeWaiters() ) {
                    try {
                        addObject ( key );
                    } catch ( final Exception e ) {
                        swallowException ( e );
                    }
                }
                return;
            }
            if ( !p.deallocate() ) {
                throw new IllegalStateException (
                    "Object has already been returned to this pool" );
            }
            final int maxIdle = getMaxIdlePerKey();
            final LinkedBlockingDeque<PooledObject<T>> idleObjects =
                objectDeque.getIdleObjects();
            if ( isClosed() || maxIdle > -1 && maxIdle <= idleObjects.size() ) {
                try {
                    destroy ( key, p, true );
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
                    clear ( key );
                }
            }
        } finally {
            if ( hasBorrowWaiters() ) {
                reuseCapacity();
            }
            updateStatsReturn ( activeTime );
        }
    }
    @Override
    public void invalidateObject ( final K key, final T obj ) throws Exception {
        final ObjectDeque<T> objectDeque = poolMap.get ( key );
        final PooledObject<T> p = objectDeque.getAllObjects().get ( new IdentityWrapper<> ( obj ) );
        if ( p == null ) {
            throw new IllegalStateException (
                "Object not currently part of this pool" );
        }
        synchronized ( p ) {
            if ( p.getState() != PooledObjectState.INVALID ) {
                destroy ( key, p, true );
            }
        }
        if ( objectDeque.idleObjects.hasTakeWaiters() ) {
            addObject ( key );
        }
    }
    @Override
    public void clear() {
        final Iterator<K> iter = poolMap.keySet().iterator();
        while ( iter.hasNext() ) {
            clear ( iter.next() );
        }
    }
    @Override
    public void clear ( final K key ) {
        final ObjectDeque<T> objectDeque = register ( key );
        try {
            final LinkedBlockingDeque<PooledObject<T>> idleObjects =
                objectDeque.getIdleObjects();
            PooledObject<T> p = idleObjects.poll();
            while ( p != null ) {
                try {
                    destroy ( key, p, true );
                } catch ( final Exception e ) {
                    swallowException ( e );
                }
                p = idleObjects.poll();
            }
        } finally {
            deregister ( key );
        }
    }
    @Override
    public int getNumActive() {
        return numTotal.get() - getNumIdle();
    }
    @Override
    public int getNumIdle() {
        final Iterator<ObjectDeque<T>> iter = poolMap.values().iterator();
        int result = 0;
        while ( iter.hasNext() ) {
            result += iter.next().getIdleObjects().size();
        }
        return result;
    }
    @Override
    public int getNumActive ( final K key ) {
        final ObjectDeque<T> objectDeque = poolMap.get ( key );
        if ( objectDeque != null ) {
            return objectDeque.getAllObjects().size() -
                   objectDeque.getIdleObjects().size();
        }
        return 0;
    }
    @Override
    public int getNumIdle ( final K key ) {
        final ObjectDeque<T> objectDeque = poolMap.get ( key );
        return objectDeque != null ? objectDeque.getIdleObjects().size() : 0;
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
            final Iterator<ObjectDeque<T>> iter = poolMap.values().iterator();
            while ( iter.hasNext() ) {
                iter.next().getIdleObjects().interuptTakeWaiters();
            }
            clear();
        }
    }
    public void clearOldest() {
        final Map<PooledObject<T>, K> map = new TreeMap<>();
        for ( Map.Entry<K, ObjectDeque<T>> entry : poolMap.entrySet() ) {
            final K k = entry.getKey();
            final ObjectDeque<T> deque = entry.getValue();
            if ( deque != null ) {
                final LinkedBlockingDeque<PooledObject<T>> idleObjects =
                    deque.getIdleObjects();
                for ( final PooledObject<T> p : idleObjects ) {
                    map.put ( p, k );
                }
            }
        }
        int itemsToRemove = ( ( int ) ( map.size() * 0.15 ) ) + 1;
        final Iterator<Map.Entry<PooledObject<T>, K>> iter =
            map.entrySet().iterator();
        while ( iter.hasNext() && itemsToRemove > 0 ) {
            final Map.Entry<PooledObject<T>, K> entry = iter.next();
            final K key = entry.getValue();
            final PooledObject<T> p = entry.getKey();
            boolean destroyed = true;
            try {
                destroyed = destroy ( key, p, false );
            } catch ( final Exception e ) {
                swallowException ( e );
            }
            if ( destroyed ) {
                itemsToRemove--;
            }
        }
    }
    private void reuseCapacity() {
        final int maxTotalPerKeySave = getMaxTotalPerKey();
        int maxQueueLength = 0;
        LinkedBlockingDeque<PooledObject<T>> mostLoaded = null;
        K loadedKey = null;
        for ( Map.Entry<K, ObjectDeque<T>> entry : poolMap.entrySet() ) {
            final K k = entry.getKey();
            final ObjectDeque<T> deque = entry.getValue();
            if ( deque != null ) {
                final LinkedBlockingDeque<PooledObject<T>> pool = deque.getIdleObjects();
                final int queueLength = pool.getTakeQueueLength();
                if ( getNumActive ( k ) < maxTotalPerKeySave && queueLength > maxQueueLength ) {
                    maxQueueLength = queueLength;
                    mostLoaded = pool;
                    loadedKey = k;
                }
            }
        }
        if ( mostLoaded != null ) {
            register ( loadedKey );
            try {
                final PooledObject<T> p = create ( loadedKey );
                if ( p != null ) {
                    addIdleObject ( loadedKey, p );
                }
            } catch ( final Exception e ) {
                swallowException ( e );
            } finally {
                deregister ( loadedKey );
            }
        }
    }
    private boolean hasBorrowWaiters() {
        for ( Map.Entry<K, ObjectDeque<T>> entry : poolMap.entrySet() ) {
            final ObjectDeque<T> deque = entry.getValue();
            if ( deque != null ) {
                final LinkedBlockingDeque<PooledObject<T>> pool =
                    deque.getIdleObjects();
                if ( pool.hasTakeWaiters() ) {
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public void evict() throws Exception {
        assertOpen();
        if ( getNumIdle() == 0 ) {
            return;
        }
        PooledObject<T> underTest = null;
        final EvictionPolicy<T> evictionPolicy = getEvictionPolicy();
        synchronized ( evictionLock ) {
            final EvictionConfig evictionConfig = new EvictionConfig (
                getMinEvictableIdleTimeMillis(),
                getSoftMinEvictableIdleTimeMillis(),
                getMinIdlePerKey() );
            final boolean testWhileIdle = getTestWhileIdle();
            for ( int i = 0, m = getNumTests(); i < m; i++ ) {
                if ( evictionIterator == null || !evictionIterator.hasNext() ) {
                    if ( evictionKeyIterator == null ||
                            !evictionKeyIterator.hasNext() ) {
                        final List<K> keyCopy = new ArrayList<>();
                        final Lock readLock = keyLock.readLock();
                        readLock.lock();
                        try {
                            keyCopy.addAll ( poolKeyList );
                        } finally {
                            readLock.unlock();
                        }
                        evictionKeyIterator = keyCopy.iterator();
                    }
                    while ( evictionKeyIterator.hasNext() ) {
                        evictionKey = evictionKeyIterator.next();
                        final ObjectDeque<T> objectDeque = poolMap.get ( evictionKey );
                        if ( objectDeque == null ) {
                            continue;
                        }
                        final Deque<PooledObject<T>> idleObjects = objectDeque.getIdleObjects();
                        evictionIterator = new EvictionIterator ( idleObjects );
                        if ( evictionIterator.hasNext() ) {
                            break;
                        }
                        evictionIterator = null;
                    }
                }
                if ( evictionIterator == null ) {
                    return;
                }
                final Deque<PooledObject<T>> idleObjects;
                try {
                    underTest = evictionIterator.next();
                    idleObjects = evictionIterator.getIdleObjects();
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
                                                   poolMap.get ( evictionKey ).getIdleObjects().size() );
                } catch ( final Throwable t ) {
                    PoolUtils.checkRethrow ( t );
                    swallowException ( new Exception ( t ) );
                    evict = false;
                }
                if ( evict ) {
                    destroy ( evictionKey, underTest, true );
                    destroyedByEvictorCount.incrementAndGet();
                } else {
                    if ( testWhileIdle ) {
                        boolean active = false;
                        try {
                            factory.activateObject ( evictionKey, underTest );
                            active = true;
                        } catch ( final Exception e ) {
                            destroy ( evictionKey, underTest, true );
                            destroyedByEvictorCount.incrementAndGet();
                        }
                        if ( active ) {
                            if ( !factory.validateObject ( evictionKey, underTest ) ) {
                                destroy ( evictionKey, underTest, true );
                                destroyedByEvictorCount.incrementAndGet();
                            } else {
                                try {
                                    factory.passivateObject ( evictionKey, underTest );
                                } catch ( final Exception e ) {
                                    destroy ( evictionKey, underTest, true );
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
    private PooledObject<T> create ( final K key ) throws Exception {
        int maxTotalPerKeySave = getMaxTotalPerKey();
        if ( maxTotalPerKeySave < 0 ) {
            maxTotalPerKeySave = Integer.MAX_VALUE;
        }
        final int maxTotal = getMaxTotal();
        final ObjectDeque<T> objectDeque = poolMap.get ( key );
        boolean loop = true;
        while ( loop ) {
            final int newNumTotal = numTotal.incrementAndGet();
            if ( maxTotal > -1 && newNumTotal > maxTotal ) {
                numTotal.decrementAndGet();
                if ( getNumIdle() == 0 ) {
                    return null;
                }
                clearOldest();
            } else {
                loop = false;
            }
        }
        Boolean create = null;
        while ( create == null ) {
            synchronized ( objectDeque.makeObjectCountLock ) {
                final long newCreateCount = objectDeque.getCreateCount().incrementAndGet();
                if ( newCreateCount > maxTotalPerKeySave ) {
                    objectDeque.getCreateCount().decrementAndGet();
                    if ( objectDeque.makeObjectCount == 0 ) {
                        create = Boolean.FALSE;
                    } else {
                        objectDeque.makeObjectCountLock.wait();
                    }
                } else {
                    objectDeque.makeObjectCount++;
                    create = Boolean.TRUE;
                }
            }
        }
        if ( !create.booleanValue() ) {
            numTotal.decrementAndGet();
            return null;
        }
        PooledObject<T> p = null;
        try {
            p = factory.makeObject ( key );
        } catch ( final Exception e ) {
            numTotal.decrementAndGet();
            objectDeque.getCreateCount().decrementAndGet();
            throw e;
        } finally {
            synchronized ( objectDeque.makeObjectCountLock ) {
                objectDeque.makeObjectCount--;
                objectDeque.makeObjectCountLock.notifyAll();
            }
        }
        createdCount.incrementAndGet();
        objectDeque.getAllObjects().put ( new IdentityWrapper<> ( p.getObject() ), p );
        return p;
    }
    private boolean destroy ( final K key, final PooledObject<T> toDestroy, final boolean always )
    throws Exception {
        final ObjectDeque<T> objectDeque = register ( key );
        try {
            final boolean isIdle = objectDeque.getIdleObjects().remove ( toDestroy );
            if ( isIdle || always ) {
                objectDeque.getAllObjects().remove ( new IdentityWrapper<> ( toDestroy.getObject() ) );
                toDestroy.invalidate();
                try {
                    factory.destroyObject ( key, toDestroy );
                } finally {
                    objectDeque.getCreateCount().decrementAndGet();
                    destroyedCount.incrementAndGet();
                    numTotal.decrementAndGet();
                }
                return true;
            }
            return false;
        } finally {
            deregister ( key );
        }
    }
    private ObjectDeque<T> register ( final K k ) {
        Lock lock = keyLock.readLock();
        ObjectDeque<T> objectDeque = null;
        try {
            lock.lock();
            objectDeque = poolMap.get ( k );
            if ( objectDeque == null ) {
                lock.unlock();
                lock = keyLock.writeLock();
                lock.lock();
                objectDeque = poolMap.get ( k );
                if ( objectDeque == null ) {
                    objectDeque = new ObjectDeque<> ( fairness );
                    objectDeque.getNumInterested().incrementAndGet();
                    poolMap.put ( k, objectDeque );
                    poolKeyList.add ( k );
                } else {
                    objectDeque.getNumInterested().incrementAndGet();
                }
            } else {
                objectDeque.getNumInterested().incrementAndGet();
            }
        } finally {
            lock.unlock();
        }
        return objectDeque;
    }
    private void deregister ( final K k ) {
        ObjectDeque<T> objectDeque;
        objectDeque = poolMap.get ( k );
        final long numInterested = objectDeque.getNumInterested().decrementAndGet();
        if ( numInterested == 0 && objectDeque.getCreateCount().get() == 0 ) {
            final Lock writeLock = keyLock.writeLock();
            writeLock.lock();
            try {
                if ( objectDeque.getCreateCount().get() == 0 &&
                        objectDeque.getNumInterested().get() == 0 ) {
                    poolMap.remove ( k );
                    poolKeyList.remove ( k );
                }
            } finally {
                writeLock.unlock();
            }
        }
    }
    @Override
    void ensureMinIdle() throws Exception {
        final int minIdlePerKeySave = getMinIdlePerKey();
        if ( minIdlePerKeySave < 1 ) {
            return;
        }
        for ( final K k : poolMap.keySet() ) {
            ensureMinIdle ( k );
        }
    }
    private void ensureMinIdle ( final K key ) throws Exception {
        final ObjectDeque<T> objectDeque = poolMap.get ( key );
        final int deficit = calculateDeficit ( objectDeque );
        for ( int i = 0; i < deficit && calculateDeficit ( objectDeque ) > 0; i++ ) {
            addObject ( key );
        }
    }
    @Override
    public void addObject ( final K key ) throws Exception {
        assertOpen();
        register ( key );
        try {
            final PooledObject<T> p = create ( key );
            addIdleObject ( key, p );
        } finally {
            deregister ( key );
        }
    }
    private void addIdleObject ( final K key, final PooledObject<T> p ) throws Exception {
        if ( p != null ) {
            factory.passivateObject ( key, p );
            final LinkedBlockingDeque<PooledObject<T>> idleObjects =
                poolMap.get ( key ).getIdleObjects();
            if ( getLifo() ) {
                idleObjects.addFirst ( p );
            } else {
                idleObjects.addLast ( p );
            }
        }
    }
    public void preparePool ( final K key ) throws Exception {
        final int minIdlePerKeySave = getMinIdlePerKey();
        if ( minIdlePerKeySave < 1 ) {
            return;
        }
        ensureMinIdle ( key );
    }
    private int getNumTests() {
        final int totalIdle = getNumIdle();
        final int numTests = getNumTestsPerEvictionRun();
        if ( numTests >= 0 ) {
            return Math.min ( numTests, totalIdle );
        }
        return ( int ) ( Math.ceil ( totalIdle / Math.abs ( ( double ) numTests ) ) );
    }
    private int calculateDeficit ( final ObjectDeque<T> objectDeque ) {
        if ( objectDeque == null ) {
            return getMinIdlePerKey();
        }
        final int maxTotal = getMaxTotal();
        final int maxTotalPerKeySave = getMaxTotalPerKey();
        int objectDefecit = 0;
        objectDefecit = getMinIdlePerKey() - objectDeque.getIdleObjects().size();
        if ( maxTotalPerKeySave > 0 ) {
            final int growLimit = Math.max ( 0,
                                             maxTotalPerKeySave - objectDeque.getIdleObjects().size() );
            objectDefecit = Math.min ( objectDefecit, growLimit );
        }
        if ( maxTotal > 0 ) {
            final int growLimit = Math.max ( 0, maxTotal - getNumActive() - getNumIdle() );
            objectDefecit = Math.min ( objectDefecit, growLimit );
        }
        return objectDefecit;
    }
    @Override
    public Map<String, Integer> getNumActivePerKey() {
        final HashMap<String, Integer> result = new HashMap<>();
        final Iterator<Entry<K, ObjectDeque<T>>> iter = poolMap.entrySet().iterator();
        while ( iter.hasNext() ) {
            final Entry<K, ObjectDeque<T>> entry = iter.next();
            if ( entry != null ) {
                final K key = entry.getKey();
                final ObjectDeque<T> objectDequeue = entry.getValue();
                if ( key != null && objectDequeue != null ) {
                    result.put ( key.toString(), Integer.valueOf (
                                     objectDequeue.getAllObjects().size() -
                                     objectDequeue.getIdleObjects().size() ) );
                }
            }
        }
        return result;
    }
    @Override
    public int getNumWaiters() {
        int result = 0;
        if ( getBlockWhenExhausted() ) {
            final Iterator<ObjectDeque<T>> iter = poolMap.values().iterator();
            while ( iter.hasNext() ) {
                result += iter.next().getIdleObjects().getTakeQueueLength();
            }
        }
        return result;
    }
    @Override
    public Map<String, Integer> getNumWaitersByKey() {
        final Map<String, Integer> result = new HashMap<>();
        for ( Map.Entry<K, ObjectDeque<T>> entry : poolMap.entrySet() ) {
            final K k = entry.getKey();
            final ObjectDeque<T> deque = entry.getValue();
            if ( deque != null ) {
                if ( getBlockWhenExhausted() ) {
                    result.put ( k.toString(), Integer.valueOf (
                                     deque.getIdleObjects().getTakeQueueLength() ) );
                } else {
                    result.put ( k.toString(), Integer.valueOf ( 0 ) );
                }
            }
        }
        return result;
    }
    @Override
    public Map<String, List<DefaultPooledObjectInfo>> listAllObjects() {
        final Map<String, List<DefaultPooledObjectInfo>> result =
            new HashMap<>();
        for ( Map.Entry<K, ObjectDeque<T>> entry : poolMap.entrySet() ) {
            final K k = entry.getKey();
            final ObjectDeque<T> deque = entry.getValue();
            if ( deque != null ) {
                final List<DefaultPooledObjectInfo> list =
                    new ArrayList<>();
                result.put ( k.toString(), list );
                for ( final PooledObject<T> p : deque.getAllObjects().values() ) {
                    list.add ( new DefaultPooledObjectInfo ( p ) );
                }
            }
        }
        return result;
    }
    private class ObjectDeque<S> {
        private final LinkedBlockingDeque<PooledObject<S>> idleObjects;
        private final AtomicInteger createCount = new AtomicInteger ( 0 );
        private long makeObjectCount = 0;
        private final Object makeObjectCountLock = new Object();
        private final Map<IdentityWrapper<S>, PooledObject<S>> allObjects =
            new ConcurrentHashMap<>();
        private final AtomicLong numInterested = new AtomicLong ( 0 );
        public ObjectDeque ( final boolean fairness ) {
            idleObjects = new LinkedBlockingDeque<> ( fairness );
        }
        public LinkedBlockingDeque<PooledObject<S>> getIdleObjects() {
            return idleObjects;
        }
        public AtomicInteger getCreateCount() {
            return createCount;
        }
        public AtomicLong getNumInterested() {
            return numInterested;
        }
        public Map<IdentityWrapper<S>, PooledObject<S>> getAllObjects() {
            return allObjects;
        }
        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append ( "ObjectDeque [idleObjects=" );
            builder.append ( idleObjects );
            builder.append ( ", createCount=" );
            builder.append ( createCount );
            builder.append ( ", allObjects=" );
            builder.append ( allObjects );
            builder.append ( ", numInterested=" );
            builder.append ( numInterested );
            builder.append ( "]" );
            return builder.toString();
        }
    }
    private volatile int maxIdlePerKey =
        GenericKeyedObjectPoolConfig.DEFAULT_MAX_IDLE_PER_KEY;
    private volatile int minIdlePerKey =
        GenericKeyedObjectPoolConfig.DEFAULT_MIN_IDLE_PER_KEY;
    private volatile int maxTotalPerKey =
        GenericKeyedObjectPoolConfig.DEFAULT_MAX_TOTAL_PER_KEY;
    private final KeyedPooledObjectFactory<K, T> factory;
    private final boolean fairness;
    private final Map<K, ObjectDeque<T>> poolMap =
        new ConcurrentHashMap<>();
    private final List<K> poolKeyList = new ArrayList<>();
    private final ReadWriteLock keyLock = new ReentrantReadWriteLock ( true );
    private final AtomicInteger numTotal = new AtomicInteger ( 0 );
    private Iterator<K> evictionKeyIterator = null;
    private K evictionKey = null;
    private static final String ONAME_BASE =
        "org.apache.tomcat.dbcp.pool2:type=GenericKeyedObjectPool,name=";
    @Override
    protected void toStringAppendFields ( final StringBuilder builder ) {
        super.toStringAppendFields ( builder );
        builder.append ( ", maxIdlePerKey=" );
        builder.append ( maxIdlePerKey );
        builder.append ( ", minIdlePerKey=" );
        builder.append ( minIdlePerKey );
        builder.append ( ", maxTotalPerKey=" );
        builder.append ( maxTotalPerKey );
        builder.append ( ", factory=" );
        builder.append ( factory );
        builder.append ( ", fairness=" );
        builder.append ( fairness );
        builder.append ( ", poolMap=" );
        builder.append ( poolMap );
        builder.append ( ", poolKeyList=" );
        builder.append ( poolKeyList );
        builder.append ( ", keyLock=" );
        builder.append ( keyLock );
        builder.append ( ", numTotal=" );
        builder.append ( numTotal );
        builder.append ( ", evictionKeyIterator=" );
        builder.append ( evictionKeyIterator );
        builder.append ( ", evictionKey=" );
        builder.append ( evictionKey );
    }
}
