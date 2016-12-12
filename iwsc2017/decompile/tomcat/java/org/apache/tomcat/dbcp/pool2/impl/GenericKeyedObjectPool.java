package org.apache.tomcat.dbcp.pool2.impl;
import java.util.concurrent.atomic.AtomicLong;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.Deque;
import java.util.Collection;
import java.util.TreeMap;
import org.apache.tomcat.dbcp.pool2.PooledObjectState;
import org.apache.tomcat.dbcp.pool2.PoolUtils;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import org.apache.tomcat.dbcp.pool2.PooledObject;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.List;
import java.util.Map;
import org.apache.tomcat.dbcp.pool2.KeyedPooledObjectFactory;
import org.apache.tomcat.dbcp.pool2.KeyedObjectPool;
public class GenericKeyedObjectPool<K, T> extends BaseGenericObjectPool<T> implements KeyedObjectPool<K, T>, GenericKeyedObjectPoolMXBean<K> {
    private volatile int maxIdlePerKey;
    private volatile int minIdlePerKey;
    private volatile int maxTotalPerKey;
    private final KeyedPooledObjectFactory<K, T> factory;
    private final boolean fairness;
    private final Map<K, ObjectDeque<T>> poolMap;
    private final List<K> poolKeyList;
    private final ReadWriteLock keyLock;
    private final AtomicInteger numTotal;
    private Iterator<K> evictionKeyIterator;
    private K evictionKey;
    private static final String ONAME_BASE = "org.apache.tomcat.dbcp.pool2:type=GenericKeyedObjectPool,name=";
    public GenericKeyedObjectPool ( final KeyedPooledObjectFactory<K, T> factory ) {
        this ( factory, new GenericKeyedObjectPoolConfig() );
    }
    public GenericKeyedObjectPool ( final KeyedPooledObjectFactory<K, T> factory, final GenericKeyedObjectPoolConfig config ) {
        super ( config, "org.apache.tomcat.dbcp.pool2:type=GenericKeyedObjectPool,name=", config.getJmxNamePrefix() );
        this.maxIdlePerKey = 8;
        this.minIdlePerKey = 0;
        this.maxTotalPerKey = 8;
        this.poolMap = new ConcurrentHashMap<K, ObjectDeque<T>>();
        this.poolKeyList = new ArrayList<K>();
        this.keyLock = new ReentrantReadWriteLock ( true );
        this.numTotal = new AtomicInteger ( 0 );
        this.evictionKeyIterator = null;
        this.evictionKey = null;
        if ( factory == null ) {
            this.jmxUnregister();
            throw new IllegalArgumentException ( "factory may not be null" );
        }
        this.factory = factory;
        this.fairness = config.getFairness();
        this.setConfig ( config );
        this.startEvictor ( this.getTimeBetweenEvictionRunsMillis() );
    }
    @Override
    public int getMaxTotalPerKey() {
        return this.maxTotalPerKey;
    }
    public void setMaxTotalPerKey ( final int maxTotalPerKey ) {
        this.maxTotalPerKey = maxTotalPerKey;
    }
    @Override
    public int getMaxIdlePerKey() {
        return this.maxIdlePerKey;
    }
    public void setMaxIdlePerKey ( final int maxIdlePerKey ) {
        this.maxIdlePerKey = maxIdlePerKey;
    }
    public void setMinIdlePerKey ( final int minIdlePerKey ) {
        this.minIdlePerKey = minIdlePerKey;
    }
    @Override
    public int getMinIdlePerKey() {
        final int maxIdlePerKeySave = this.getMaxIdlePerKey();
        if ( this.minIdlePerKey > maxIdlePerKeySave ) {
            return maxIdlePerKeySave;
        }
        return this.minIdlePerKey;
    }
    public void setConfig ( final GenericKeyedObjectPoolConfig conf ) {
        this.setLifo ( conf.getLifo() );
        this.setMaxIdlePerKey ( conf.getMaxIdlePerKey() );
        this.setMaxTotalPerKey ( conf.getMaxTotalPerKey() );
        this.setMaxTotal ( conf.getMaxTotal() );
        this.setMinIdlePerKey ( conf.getMinIdlePerKey() );
        this.setMaxWaitMillis ( conf.getMaxWaitMillis() );
        this.setBlockWhenExhausted ( conf.getBlockWhenExhausted() );
        this.setTestOnCreate ( conf.getTestOnCreate() );
        this.setTestOnBorrow ( conf.getTestOnBorrow() );
        this.setTestOnReturn ( conf.getTestOnReturn() );
        this.setTestWhileIdle ( conf.getTestWhileIdle() );
        this.setNumTestsPerEvictionRun ( conf.getNumTestsPerEvictionRun() );
        this.setMinEvictableIdleTimeMillis ( conf.getMinEvictableIdleTimeMillis() );
        this.setSoftMinEvictableIdleTimeMillis ( conf.getSoftMinEvictableIdleTimeMillis() );
        this.setTimeBetweenEvictionRunsMillis ( conf.getTimeBetweenEvictionRunsMillis() );
        this.setEvictionPolicyClassName ( conf.getEvictionPolicyClassName() );
    }
    public KeyedPooledObjectFactory<K, T> getFactory() {
        return this.factory;
    }
    @Override
    public T borrowObject ( final K key ) throws Exception {
        return this.borrowObject ( key, this.getMaxWaitMillis() );
    }
    public T borrowObject ( final K key, final long borrowMaxWaitMillis ) throws Exception {
        this.assertOpen();
        PooledObject<T> p = null;
        final boolean blockWhenExhausted = this.getBlockWhenExhausted();
        final long waitTime = System.currentTimeMillis();
        final ObjectDeque<T> objectDeque = this.register ( key );
        try {
            while ( p == null ) {
                boolean create = false;
                p = objectDeque.getIdleObjects().pollFirst();
                if ( p == null ) {
                    p = this.create ( key );
                    if ( p != null ) {
                        create = true;
                    }
                }
                if ( blockWhenExhausted ) {
                    if ( p == null ) {
                        if ( borrowMaxWaitMillis < 0L ) {
                            p = objectDeque.getIdleObjects().takeFirst();
                        } else {
                            p = objectDeque.getIdleObjects().pollFirst ( borrowMaxWaitMillis, TimeUnit.MILLISECONDS );
                        }
                    }
                    if ( p == null ) {
                        throw new NoSuchElementException ( "Timeout waiting for idle object" );
                    }
                } else if ( p == null ) {
                    throw new NoSuchElementException ( "Pool exhausted" );
                }
                if ( !p.allocate() ) {
                    p = null;
                }
                if ( p != null ) {
                    try {
                        this.factory.activateObject ( key, p );
                    } catch ( Exception e ) {
                        try {
                            this.destroy ( key, p, true );
                        } catch ( Exception ex ) {}
                        p = null;
                        if ( create ) {
                            final NoSuchElementException nsee = new NoSuchElementException ( "Unable to activate object" );
                            nsee.initCause ( e );
                            throw nsee;
                        }
                    }
                    if ( p == null || ( !this.getTestOnBorrow() && ( !create || !this.getTestOnCreate() ) ) ) {
                        continue;
                    }
                    boolean validate = false;
                    Throwable validationThrowable = null;
                    try {
                        validate = this.factory.validateObject ( key, p );
                    } catch ( Throwable t ) {
                        PoolUtils.checkRethrow ( t );
                        validationThrowable = t;
                    }
                    if ( validate ) {
                        continue;
                    }
                    try {
                        this.destroy ( key, p, true );
                        this.destroyedByBorrowValidationCount.incrementAndGet();
                    } catch ( Exception ex2 ) {}
                    p = null;
                    if ( create ) {
                        final NoSuchElementException nsee2 = new NoSuchElementException ( "Unable to validate object" );
                        nsee2.initCause ( validationThrowable );
                        throw nsee2;
                    }
                    continue;
                }
            }
        } finally {
            this.deregister ( key );
        }
        this.updateStatsBorrow ( p, System.currentTimeMillis() - waitTime );
        return p.getObject();
    }
    @Override
    public void returnObject ( final K key, final T obj ) {
        final ObjectDeque<T> objectDeque = this.poolMap.get ( key );
        final PooledObject<T> p = objectDeque.getAllObjects().get ( new IdentityWrapper ( obj ) );
        if ( p == null ) {
            throw new IllegalStateException ( "Returned object not currently part of this pool" );
        }
        synchronized ( p ) {
            final PooledObjectState state = p.getState();
            if ( state != PooledObjectState.ALLOCATED ) {
                throw new IllegalStateException ( "Object has already been returned to this pool or is invalid" );
            }
            p.markReturning();
        }
        final long activeTime = p.getActiveTimeMillis();
        try {
            if ( this.getTestOnReturn() && !this.factory.validateObject ( key, p ) ) {
                try {
                    this.destroy ( key, p, true );
                } catch ( Exception e ) {
                    this.swallowException ( e );
                }
                if ( ( ( ObjectDeque<Object> ) objectDeque ).idleObjects.hasTakeWaiters() ) {
                    try {
                        this.addObject ( key );
                    } catch ( Exception e ) {
                        this.swallowException ( e );
                    }
                }
                return;
            }
            try {
                this.factory.passivateObject ( key, p );
            } catch ( Exception e2 ) {
                this.swallowException ( e2 );
                try {
                    this.destroy ( key, p, true );
                } catch ( Exception e3 ) {
                    this.swallowException ( e3 );
                }
                if ( ( ( ObjectDeque<Object> ) objectDeque ).idleObjects.hasTakeWaiters() ) {
                    try {
                        this.addObject ( key );
                    } catch ( Exception e3 ) {
                        this.swallowException ( e3 );
                    }
                }
                return;
            }
            if ( !p.deallocate() ) {
                throw new IllegalStateException ( "Object has already been returned to this pool" );
            }
            final int maxIdle = this.getMaxIdlePerKey();
            final LinkedBlockingDeque<PooledObject<T>> idleObjects = objectDeque.getIdleObjects();
            if ( !this.isClosed() ) {
                if ( maxIdle <= -1 || maxIdle > idleObjects.size() ) {
                    if ( this.getLifo() ) {
                        idleObjects.addFirst ( p );
                    } else {
                        idleObjects.addLast ( p );
                    }
                    if ( this.isClosed() ) {
                        this.clear ( key );
                    }
                    return;
                }
            }
            try {
                this.destroy ( key, p, true );
            } catch ( Exception e4 ) {
                this.swallowException ( e4 );
            }
        } finally {
            if ( this.hasBorrowWaiters() ) {
                this.reuseCapacity();
            }
            this.updateStatsReturn ( activeTime );
        }
    }
    @Override
    public void invalidateObject ( final K key, final T obj ) throws Exception {
        final ObjectDeque<T> objectDeque = this.poolMap.get ( key );
        final PooledObject<T> p = objectDeque.getAllObjects().get ( new IdentityWrapper ( obj ) );
        if ( p == null ) {
            throw new IllegalStateException ( "Object not currently part of this pool" );
        }
        synchronized ( p ) {
            if ( p.getState() != PooledObjectState.INVALID ) {
                this.destroy ( key, p, true );
            }
        }
        if ( ( ( ObjectDeque<Object> ) objectDeque ).idleObjects.hasTakeWaiters() ) {
            this.addObject ( key );
        }
    }
    @Override
    public void clear() {
        final Iterator<K> iter = this.poolMap.keySet().iterator();
        while ( iter.hasNext() ) {
            this.clear ( iter.next() );
        }
    }
    @Override
    public void clear ( final K key ) {
        final ObjectDeque<T> objectDeque = this.register ( key );
        try {
            final LinkedBlockingDeque<PooledObject<T>> idleObjects = objectDeque.getIdleObjects();
            for ( PooledObject<T> p = idleObjects.poll(); p != null; p = idleObjects.poll() ) {
                try {
                    this.destroy ( key, p, true );
                } catch ( Exception e ) {
                    this.swallowException ( e );
                }
            }
        } finally {
            this.deregister ( key );
        }
    }
    @Override
    public int getNumActive() {
        return this.numTotal.get() - this.getNumIdle();
    }
    @Override
    public int getNumIdle() {
        final Iterator<ObjectDeque<T>> iter = this.poolMap.values().iterator();
        int result = 0;
        while ( iter.hasNext() ) {
            result += iter.next().getIdleObjects().size();
        }
        return result;
    }
    @Override
    public int getNumActive ( final K key ) {
        final ObjectDeque<T> objectDeque = this.poolMap.get ( key );
        if ( objectDeque != null ) {
            return objectDeque.getAllObjects().size() - objectDeque.getIdleObjects().size();
        }
        return 0;
    }
    @Override
    public int getNumIdle ( final K key ) {
        final ObjectDeque<T> objectDeque = this.poolMap.get ( key );
        return ( objectDeque != null ) ? objectDeque.getIdleObjects().size() : 0;
    }
    @Override
    public void close() {
        if ( this.isClosed() ) {
            return;
        }
        synchronized ( this.closeLock ) {
            if ( this.isClosed() ) {
                return;
            }
            this.startEvictor ( -1L );
            this.closed = true;
            this.clear();
            this.jmxUnregister();
            final Iterator<ObjectDeque<T>> iter = this.poolMap.values().iterator();
            while ( iter.hasNext() ) {
                iter.next().getIdleObjects().interuptTakeWaiters();
            }
            this.clear();
        }
    }
    public void clearOldest() {
        final Map<PooledObject<T>, K> map = new TreeMap<PooledObject<T>, K>();
        for ( final Map.Entry<K, ObjectDeque<T>> entry : this.poolMap.entrySet() ) {
            final K k = entry.getKey();
            final ObjectDeque<T> deque = entry.getValue();
            if ( deque != null ) {
                final LinkedBlockingDeque<PooledObject<T>> idleObjects = deque.getIdleObjects();
                for ( final PooledObject<T> p : idleObjects ) {
                    map.put ( p, k );
                }
            }
        }
        int itemsToRemove = ( int ) ( map.size() * 0.15 ) + 1;
        for ( Iterator<Map.Entry<PooledObject<T>, K>> iter = map.entrySet().iterator(); iter.hasNext() && itemsToRemove > 0; --itemsToRemove ) {
            final Map.Entry<PooledObject<T>, K> entry2 = iter.next();
            final K key = entry2.getValue();
            final PooledObject<T> p2 = entry2.getKey();
            boolean destroyed = true;
            try {
                destroyed = this.destroy ( key, p2, false );
            } catch ( Exception e ) {
                this.swallowException ( e );
            }
            if ( destroyed ) {}
        }
    }
    private void reuseCapacity() {
        final int maxTotalPerKeySave = this.getMaxTotalPerKey();
        int maxQueueLength = 0;
        LinkedBlockingDeque<PooledObject<T>> mostLoaded = null;
        K loadedKey = null;
        for ( final Map.Entry<K, ObjectDeque<T>> entry : this.poolMap.entrySet() ) {
            final K k = entry.getKey();
            final ObjectDeque<T> deque = entry.getValue();
            if ( deque != null ) {
                final LinkedBlockingDeque<PooledObject<T>> pool = deque.getIdleObjects();
                final int queueLength = pool.getTakeQueueLength();
                if ( this.getNumActive ( k ) >= maxTotalPerKeySave || queueLength <= maxQueueLength ) {
                    continue;
                }
                maxQueueLength = queueLength;
                mostLoaded = pool;
                loadedKey = k;
            }
        }
        if ( mostLoaded != null ) {
            this.register ( loadedKey );
            try {
                final PooledObject<T> p = this.create ( loadedKey );
                if ( p != null ) {
                    this.addIdleObject ( loadedKey, p );
                }
            } catch ( Exception e ) {
                this.swallowException ( e );
            } finally {
                this.deregister ( loadedKey );
            }
        }
    }
    private boolean hasBorrowWaiters() {
        for ( final Map.Entry<K, ObjectDeque<T>> entry : this.poolMap.entrySet() ) {
            final ObjectDeque<T> deque = entry.getValue();
            if ( deque != null ) {
                final LinkedBlockingDeque<PooledObject<T>> pool = deque.getIdleObjects();
                if ( pool.hasTakeWaiters() ) {
                    return true;
                }
                continue;
            }
        }
        return false;
    }
    @Override
    public void evict() throws Exception {
        this.assertOpen();
        if ( this.getNumIdle() == 0 ) {
            return;
        }
        PooledObject<T> underTest = null;
        final EvictionPolicy<T> evictionPolicy = this.getEvictionPolicy();
        synchronized ( this.evictionLock ) {
            final EvictionConfig evictionConfig = new EvictionConfig ( this.getMinEvictableIdleTimeMillis(), this.getSoftMinEvictableIdleTimeMillis(), this.getMinIdlePerKey() );
            final boolean testWhileIdle = this.getTestWhileIdle();
            for ( int i = 0, m = this.getNumTests(); i < m; ++i ) {
                if ( this.evictionIterator == null || !this.evictionIterator.hasNext() ) {
                    if ( this.evictionKeyIterator == null || !this.evictionKeyIterator.hasNext() ) {
                        final List<K> keyCopy = new ArrayList<K>();
                        final Lock readLock = this.keyLock.readLock();
                        readLock.lock();
                        try {
                            keyCopy.addAll ( ( Collection<? extends K> ) this.poolKeyList );
                        } finally {
                            readLock.unlock();
                        }
                        this.evictionKeyIterator = keyCopy.iterator();
                    }
                    while ( this.evictionKeyIterator.hasNext() ) {
                        this.evictionKey = this.evictionKeyIterator.next();
                        final ObjectDeque<T> objectDeque = this.poolMap.get ( this.evictionKey );
                        if ( objectDeque == null ) {
                            continue;
                        }
                        final Deque<PooledObject<T>> idleObjects = objectDeque.getIdleObjects();
                        this.evictionIterator = new EvictionIterator ( ( Deque<PooledObject<T>> ) idleObjects );
                        if ( this.evictionIterator.hasNext() ) {
                            break;
                        }
                        this.evictionIterator = null;
                    }
                }
                if ( this.evictionIterator == null ) {
                    return;
                }
                Deque<PooledObject<T>> idleObjects2;
                try {
                    underTest = this.evictionIterator.next();
                    idleObjects2 = this.evictionIterator.getIdleObjects();
                } catch ( NoSuchElementException nsee ) {
                    --i;
                    this.evictionIterator = null;
                    continue;
                }
                if ( !underTest.startEvictionTest() ) {
                    --i;
                } else {
                    boolean evict;
                    try {
                        evict = evictionPolicy.evict ( evictionConfig, underTest, this.poolMap.get ( this.evictionKey ).getIdleObjects().size() );
                    } catch ( Throwable t ) {
                        PoolUtils.checkRethrow ( t );
                        this.swallowException ( new Exception ( t ) );
                        evict = false;
                    }
                    if ( evict ) {
                        this.destroy ( this.evictionKey, underTest, true );
                        this.destroyedByEvictorCount.incrementAndGet();
                    } else {
                        if ( testWhileIdle ) {
                            boolean active = false;
                            try {
                                this.factory.activateObject ( this.evictionKey, underTest );
                                active = true;
                            } catch ( Exception e ) {
                                this.destroy ( this.evictionKey, underTest, true );
                                this.destroyedByEvictorCount.incrementAndGet();
                            }
                            if ( active ) {
                                if ( !this.factory.validateObject ( this.evictionKey, underTest ) ) {
                                    this.destroy ( this.evictionKey, underTest, true );
                                    this.destroyedByEvictorCount.incrementAndGet();
                                } else {
                                    try {
                                        this.factory.passivateObject ( this.evictionKey, underTest );
                                    } catch ( Exception e ) {
                                        this.destroy ( this.evictionKey, underTest, true );
                                        this.destroyedByEvictorCount.incrementAndGet();
                                    }
                                }
                            }
                        }
                        if ( !underTest.endEvictionTest ( idleObjects2 ) ) {}
                    }
                }
            }
        }
    }
    private PooledObject<T> create ( final K key ) throws Exception {
        int maxTotalPerKeySave = this.getMaxTotalPerKey();
        if ( maxTotalPerKeySave < 0 ) {
            maxTotalPerKeySave = Integer.MAX_VALUE;
        }
        final int maxTotal = this.getMaxTotal();
        final ObjectDeque<T> objectDeque = this.poolMap.get ( key );
        boolean loop = true;
        while ( loop ) {
            final int newNumTotal = this.numTotal.incrementAndGet();
            if ( maxTotal > -1 && newNumTotal > maxTotal ) {
                this.numTotal.decrementAndGet();
                if ( this.getNumIdle() == 0 ) {
                    return null;
                }
                this.clearOldest();
            } else {
                loop = false;
            }
        }
        Boolean create = null;
        while ( create == null ) {
            synchronized ( ( ( ObjectDeque<Object> ) objectDeque ).makeObjectCountLock ) {
                final long newCreateCount = objectDeque.getCreateCount().incrementAndGet();
                if ( newCreateCount > maxTotalPerKeySave ) {
                    objectDeque.getCreateCount().decrementAndGet();
                    if ( ( ( ObjectDeque<Object> ) objectDeque ).makeObjectCount == 0L ) {
                        create = Boolean.FALSE;
                    } else {
                        ( ( ObjectDeque<Object> ) objectDeque ).makeObjectCountLock.wait();
                    }
                } else {
                    ( ( ObjectDeque<Object> ) objectDeque ).makeObjectCount++;
                    create = Boolean.TRUE;
                }
            }
        }
        if ( !create ) {
            this.numTotal.decrementAndGet();
            return null;
        }
        PooledObject<T> p = null;
        try {
            p = this.factory.makeObject ( key );
        } catch ( Exception e ) {
            this.numTotal.decrementAndGet();
            objectDeque.getCreateCount().decrementAndGet();
            throw e;
        } finally {
            synchronized ( ( ( ObjectDeque<Object> ) objectDeque ).makeObjectCountLock ) {
                ( ( ObjectDeque<Object> ) objectDeque ).makeObjectCount--;
                ( ( ObjectDeque<Object> ) objectDeque ).makeObjectCountLock.notifyAll();
            }
        }
        this.createdCount.incrementAndGet();
        objectDeque.getAllObjects().put ( new IdentityWrapper<T> ( p.getObject() ), p );
        return p;
    }
    private boolean destroy ( final K key, final PooledObject<T> toDestroy, final boolean always ) throws Exception {
        final ObjectDeque<T> objectDeque = this.register ( key );
        try {
            final boolean isIdle = objectDeque.getIdleObjects().remove ( toDestroy );
            if ( isIdle || always ) {
                objectDeque.getAllObjects().remove ( new IdentityWrapper ( toDestroy.getObject() ) );
                toDestroy.invalidate();
                try {
                    this.factory.destroyObject ( key, toDestroy );
                } finally {
                    objectDeque.getCreateCount().decrementAndGet();
                    this.destroyedCount.incrementAndGet();
                    this.numTotal.decrementAndGet();
                }
                return true;
            }
            return false;
        } finally {
            this.deregister ( key );
        }
    }
    private ObjectDeque<T> register ( final K k ) {
        Lock lock = this.keyLock.readLock();
        ObjectDeque<T> objectDeque = null;
        try {
            lock.lock();
            objectDeque = this.poolMap.get ( k );
            if ( objectDeque == null ) {
                lock.unlock();
                lock = this.keyLock.writeLock();
                lock.lock();
                objectDeque = this.poolMap.get ( k );
                if ( objectDeque == null ) {
                    objectDeque = new ObjectDeque<T> ( this.fairness );
                    objectDeque.getNumInterested().incrementAndGet();
                    this.poolMap.put ( k, objectDeque );
                    this.poolKeyList.add ( k );
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
        final ObjectDeque<T> objectDeque = this.poolMap.get ( k );
        final long numInterested = objectDeque.getNumInterested().decrementAndGet();
        if ( numInterested == 0L && objectDeque.getCreateCount().get() == 0 ) {
            final Lock writeLock = this.keyLock.writeLock();
            writeLock.lock();
            try {
                if ( objectDeque.getCreateCount().get() == 0 && objectDeque.getNumInterested().get() == 0L ) {
                    this.poolMap.remove ( k );
                    this.poolKeyList.remove ( k );
                }
            } finally {
                writeLock.unlock();
            }
        }
    }
    @Override
    void ensureMinIdle() throws Exception {
        final int minIdlePerKeySave = this.getMinIdlePerKey();
        if ( minIdlePerKeySave < 1 ) {
            return;
        }
        for ( final K k : this.poolMap.keySet() ) {
            this.ensureMinIdle ( k );
        }
    }
    private void ensureMinIdle ( final K key ) throws Exception {
        final ObjectDeque<T> objectDeque = this.poolMap.get ( key );
        for ( int deficit = this.calculateDeficit ( objectDeque ), i = 0; i < deficit && this.calculateDeficit ( objectDeque ) > 0; ++i ) {
            this.addObject ( key );
        }
    }
    @Override
    public void addObject ( final K key ) throws Exception {
        this.assertOpen();
        this.register ( key );
        try {
            final PooledObject<T> p = this.create ( key );
            this.addIdleObject ( key, p );
        } finally {
            this.deregister ( key );
        }
    }
    private void addIdleObject ( final K key, final PooledObject<T> p ) throws Exception {
        if ( p != null ) {
            this.factory.passivateObject ( key, p );
            final LinkedBlockingDeque<PooledObject<T>> idleObjects = this.poolMap.get ( key ).getIdleObjects();
            if ( this.getLifo() ) {
                idleObjects.addFirst ( p );
            } else {
                idleObjects.addLast ( p );
            }
        }
    }
    public void preparePool ( final K key ) throws Exception {
        final int minIdlePerKeySave = this.getMinIdlePerKey();
        if ( minIdlePerKeySave < 1 ) {
            return;
        }
        this.ensureMinIdle ( key );
    }
    private int getNumTests() {
        final int totalIdle = this.getNumIdle();
        final int numTests = this.getNumTestsPerEvictionRun();
        if ( numTests >= 0 ) {
            return Math.min ( numTests, totalIdle );
        }
        return ( int ) Math.ceil ( totalIdle / Math.abs ( ( double ) numTests ) );
    }
    private int calculateDeficit ( final ObjectDeque<T> objectDeque ) {
        if ( objectDeque == null ) {
            return this.getMinIdlePerKey();
        }
        final int maxTotal = this.getMaxTotal();
        final int maxTotalPerKeySave = this.getMaxTotalPerKey();
        int objectDefecit = 0;
        objectDefecit = this.getMinIdlePerKey() - objectDeque.getIdleObjects().size();
        if ( maxTotalPerKeySave > 0 ) {
            final int growLimit = Math.max ( 0, maxTotalPerKeySave - objectDeque.getIdleObjects().size() );
            objectDefecit = Math.min ( objectDefecit, growLimit );
        }
        if ( maxTotal > 0 ) {
            final int growLimit = Math.max ( 0, maxTotal - this.getNumActive() - this.getNumIdle() );
            objectDefecit = Math.min ( objectDefecit, growLimit );
        }
        return objectDefecit;
    }
    @Override
    public Map<String, Integer> getNumActivePerKey() {
        final HashMap<String, Integer> result = new HashMap<String, Integer>();
        for ( final Map.Entry<K, ObjectDeque<T>> entry : this.poolMap.entrySet() ) {
            if ( entry != null ) {
                final K key = entry.getKey();
                final ObjectDeque<T> objectDequeue = entry.getValue();
                if ( key == null || objectDequeue == null ) {
                    continue;
                }
                result.put ( key.toString(), objectDequeue.getAllObjects().size() - objectDequeue.getIdleObjects().size() );
            }
        }
        return result;
    }
    @Override
    public int getNumWaiters() {
        int result = 0;
        if ( this.getBlockWhenExhausted() ) {
            final Iterator<ObjectDeque<T>> iter = this.poolMap.values().iterator();
            while ( iter.hasNext() ) {
                result += iter.next().getIdleObjects().getTakeQueueLength();
            }
        }
        return result;
    }
    @Override
    public Map<String, Integer> getNumWaitersByKey() {
        final Map<String, Integer> result = new HashMap<String, Integer>();
        for ( final Map.Entry<K, ObjectDeque<T>> entry : this.poolMap.entrySet() ) {
            final K k = entry.getKey();
            final ObjectDeque<T> deque = entry.getValue();
            if ( deque != null ) {
                if ( this.getBlockWhenExhausted() ) {
                    result.put ( k.toString(), deque.getIdleObjects().getTakeQueueLength() );
                } else {
                    result.put ( k.toString(), 0 );
                }
            }
        }
        return result;
    }
    @Override
    public Map<String, List<DefaultPooledObjectInfo>> listAllObjects() {
        final Map<String, List<DefaultPooledObjectInfo>> result = new HashMap<String, List<DefaultPooledObjectInfo>>();
        for ( final Map.Entry<K, ObjectDeque<T>> entry : this.poolMap.entrySet() ) {
            final K k = entry.getKey();
            final ObjectDeque<T> deque = entry.getValue();
            if ( deque != null ) {
                final List<DefaultPooledObjectInfo> list = new ArrayList<DefaultPooledObjectInfo>();
                result.put ( k.toString(), list );
                for ( final PooledObject<T> p : deque.getAllObjects().values() ) {
                    list.add ( new DefaultPooledObjectInfo ( p ) );
                }
            }
        }
        return result;
    }
    @Override
    protected void toStringAppendFields ( final StringBuilder builder ) {
        super.toStringAppendFields ( builder );
        builder.append ( ", maxIdlePerKey=" );
        builder.append ( this.maxIdlePerKey );
        builder.append ( ", minIdlePerKey=" );
        builder.append ( this.minIdlePerKey );
        builder.append ( ", maxTotalPerKey=" );
        builder.append ( this.maxTotalPerKey );
        builder.append ( ", factory=" );
        builder.append ( this.factory );
        builder.append ( ", fairness=" );
        builder.append ( this.fairness );
        builder.append ( ", poolMap=" );
        builder.append ( this.poolMap );
        builder.append ( ", poolKeyList=" );
        builder.append ( this.poolKeyList );
        builder.append ( ", keyLock=" );
        builder.append ( this.keyLock );
        builder.append ( ", numTotal=" );
        builder.append ( this.numTotal );
        builder.append ( ", evictionKeyIterator=" );
        builder.append ( this.evictionKeyIterator );
        builder.append ( ", evictionKey=" );
        builder.append ( this.evictionKey );
    }
    private class ObjectDeque<S> {
        private final LinkedBlockingDeque<PooledObject<S>> idleObjects;
        private final AtomicInteger createCount;
        private long makeObjectCount;
        private final Object makeObjectCountLock;
        private final Map<IdentityWrapper<S>, PooledObject<S>> allObjects;
        private final AtomicLong numInterested;
        public ObjectDeque ( final boolean fairness ) {
            this.createCount = new AtomicInteger ( 0 );
            this.makeObjectCount = 0L;
            this.makeObjectCountLock = new Object();
            this.allObjects = new ConcurrentHashMap<IdentityWrapper<S>, PooledObject<S>>();
            this.numInterested = new AtomicLong ( 0L );
            this.idleObjects = new LinkedBlockingDeque<PooledObject<S>> ( fairness );
        }
        public LinkedBlockingDeque<PooledObject<S>> getIdleObjects() {
            return this.idleObjects;
        }
        public AtomicInteger getCreateCount() {
            return this.createCount;
        }
        public AtomicLong getNumInterested() {
            return this.numInterested;
        }
        public Map<IdentityWrapper<S>, PooledObject<S>> getAllObjects() {
            return this.allObjects;
        }
        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append ( "ObjectDeque [idleObjects=" );
            builder.append ( this.idleObjects );
            builder.append ( ", createCount=" );
            builder.append ( this.createCount );
            builder.append ( ", allObjects=" );
            builder.append ( this.allObjects );
            builder.append ( ", numInterested=" );
            builder.append ( this.numInterested );
            builder.append ( "]" );
            return builder.toString();
        }
    }
}
