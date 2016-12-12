package org.apache.tomcat.dbcp.pool2;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
public final class PoolUtils {
    static class TimerHolder {
        static final Timer MIN_IDLE_TIMER = new Timer ( true );
    }
    public PoolUtils() {
    }
    public static void checkRethrow ( final Throwable t ) {
        if ( t instanceof ThreadDeath ) {
            throw ( ThreadDeath ) t;
        }
        if ( t instanceof VirtualMachineError ) {
            throw ( VirtualMachineError ) t;
        }
    }
    public static <T> TimerTask checkMinIdle ( final ObjectPool<T> pool,
            final int minIdle, final long period )
    throws IllegalArgumentException {
        if ( pool == null ) {
            throw new IllegalArgumentException ( "keyedPool must not be null." );
        }
        if ( minIdle < 0 ) {
            throw new IllegalArgumentException ( "minIdle must be non-negative." );
        }
        final TimerTask task = new ObjectPoolMinIdleTimerTask<> ( pool, minIdle );
        getMinIdleTimer().schedule ( task, 0L, period );
        return task;
    }
    public static <K, V> TimerTask checkMinIdle (
        final KeyedObjectPool<K, V> keyedPool, final K key,
        final int minIdle, final long period )
    throws IllegalArgumentException {
        if ( keyedPool == null ) {
            throw new IllegalArgumentException ( "keyedPool must not be null." );
        }
        if ( key == null ) {
            throw new IllegalArgumentException ( "key must not be null." );
        }
        if ( minIdle < 0 ) {
            throw new IllegalArgumentException ( "minIdle must be non-negative." );
        }
        final TimerTask task = new KeyedObjectPoolMinIdleTimerTask<> (
            keyedPool, key, minIdle );
        getMinIdleTimer().schedule ( task, 0L, period );
        return task;
    }
    public static <K, V> Map<K, TimerTask> checkMinIdle (
        final KeyedObjectPool<K, V> keyedPool, final Collection<K> keys,
        final int minIdle, final long period )
    throws IllegalArgumentException {
        if ( keys == null ) {
            throw new IllegalArgumentException ( "keys must not be null." );
        }
        final Map<K, TimerTask> tasks = new HashMap<> ( keys.size() );
        final Iterator<K> iter = keys.iterator();
        while ( iter.hasNext() ) {
            final K key = iter.next();
            final TimerTask task = checkMinIdle ( keyedPool, key, minIdle, period );
            tasks.put ( key, task );
        }
        return tasks;
    }
    public static <T> void prefill ( final ObjectPool<T> pool, final int count )
    throws Exception, IllegalArgumentException {
        if ( pool == null ) {
            throw new IllegalArgumentException ( "pool must not be null." );
        }
        for ( int i = 0; i < count; i++ ) {
            pool.addObject();
        }
    }
    public static <K, V> void prefill ( final KeyedObjectPool<K, V> keyedPool,
                                        final K key, final int count ) throws Exception,
        IllegalArgumentException {
        if ( keyedPool == null ) {
            throw new IllegalArgumentException ( "keyedPool must not be null." );
        }
        if ( key == null ) {
            throw new IllegalArgumentException ( "key must not be null." );
        }
        for ( int i = 0; i < count; i++ ) {
            keyedPool.addObject ( key );
        }
    }
    public static <K, V> void prefill ( final KeyedObjectPool<K, V> keyedPool,
                                        final Collection<K> keys, final int count ) throws Exception,
        IllegalArgumentException {
        if ( keys == null ) {
            throw new IllegalArgumentException ( "keys must not be null." );
        }
        final Iterator<K> iter = keys.iterator();
        while ( iter.hasNext() ) {
            prefill ( keyedPool, iter.next(), count );
        }
    }
    public static <T> ObjectPool<T> synchronizedPool ( final ObjectPool<T> pool ) {
        if ( pool == null ) {
            throw new IllegalArgumentException ( "pool must not be null." );
        }
        return new SynchronizedObjectPool<> ( pool );
    }
    public static <K, V> KeyedObjectPool<K, V> synchronizedPool (
        final KeyedObjectPool<K, V> keyedPool ) {
        return new SynchronizedKeyedObjectPool<> ( keyedPool );
    }
    public static <T> PooledObjectFactory<T> synchronizedPooledFactory (
        final PooledObjectFactory<T> factory ) {
        return new SynchronizedPooledObjectFactory<> ( factory );
    }
    public static <K, V> KeyedPooledObjectFactory<K, V> synchronizedKeyedPooledFactory (
        final KeyedPooledObjectFactory<K, V> keyedFactory ) {
        return new SynchronizedKeyedPooledObjectFactory<> ( keyedFactory );
    }
    public static <T> ObjectPool<T> erodingPool ( final ObjectPool<T> pool ) {
        return erodingPool ( pool, 1f );
    }
    public static <T> ObjectPool<T> erodingPool ( final ObjectPool<T> pool,
            final float factor ) {
        if ( pool == null ) {
            throw new IllegalArgumentException ( "pool must not be null." );
        }
        if ( factor <= 0f ) {
            throw new IllegalArgumentException ( "factor must be positive." );
        }
        return new ErodingObjectPool<> ( pool, factor );
    }
    public static <K, V> KeyedObjectPool<K, V> erodingPool (
        final KeyedObjectPool<K, V> keyedPool ) {
        return erodingPool ( keyedPool, 1f );
    }
    public static <K, V> KeyedObjectPool<K, V> erodingPool (
        final KeyedObjectPool<K, V> keyedPool, final float factor ) {
        return erodingPool ( keyedPool, factor, false );
    }
    public static <K, V> KeyedObjectPool<K, V> erodingPool (
        final KeyedObjectPool<K, V> keyedPool, final float factor,
        final boolean perKey ) {
        if ( keyedPool == null ) {
            throw new IllegalArgumentException ( "keyedPool must not be null." );
        }
        if ( factor <= 0f ) {
            throw new IllegalArgumentException ( "factor must be positive." );
        }
        if ( perKey ) {
            return new ErodingPerKeyKeyedObjectPool<> ( keyedPool, factor );
        }
        return new ErodingKeyedObjectPool<> ( keyedPool, factor );
    }
    private static Timer getMinIdleTimer() {
        return TimerHolder.MIN_IDLE_TIMER;
    }
    private static final class ObjectPoolMinIdleTimerTask<T> extends TimerTask {
        private final int minIdle;
        private final ObjectPool<T> pool;
        ObjectPoolMinIdleTimerTask ( final ObjectPool<T> pool, final int minIdle )
        throws IllegalArgumentException {
            if ( pool == null ) {
                throw new IllegalArgumentException ( "pool must not be null." );
            }
            this.pool = pool;
            this.minIdle = minIdle;
        }
        @Override
        public void run() {
            boolean success = false;
            try {
                if ( pool.getNumIdle() < minIdle ) {
                    pool.addObject();
                }
                success = true;
            } catch ( final Exception e ) {
                cancel();
            } finally {
                if ( !success ) {
                    cancel();
                }
            }
        }
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append ( "ObjectPoolMinIdleTimerTask" );
            sb.append ( "{minIdle=" ).append ( minIdle );
            sb.append ( ", pool=" ).append ( pool );
            sb.append ( '}' );
            return sb.toString();
        }
    }
    private static final class KeyedObjectPoolMinIdleTimerTask<K, V> extends
        TimerTask {
        private final int minIdle;
        private final K key;
        private final KeyedObjectPool<K, V> keyedPool;
        KeyedObjectPoolMinIdleTimerTask ( final KeyedObjectPool<K, V> keyedPool,
                                          final K key, final int minIdle ) throws IllegalArgumentException {
            if ( keyedPool == null ) {
                throw new IllegalArgumentException (
                    "keyedPool must not be null." );
            }
            this.keyedPool = keyedPool;
            this.key = key;
            this.minIdle = minIdle;
        }
        @Override
        public void run() {
            boolean success = false;
            try {
                if ( keyedPool.getNumIdle ( key ) < minIdle ) {
                    keyedPool.addObject ( key );
                }
                success = true;
            } catch ( final Exception e ) {
                cancel();
            } finally {
                if ( !success ) {
                    cancel();
                }
            }
        }
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append ( "KeyedObjectPoolMinIdleTimerTask" );
            sb.append ( "{minIdle=" ).append ( minIdle );
            sb.append ( ", key=" ).append ( key );
            sb.append ( ", keyedPool=" ).append ( keyedPool );
            sb.append ( '}' );
            return sb.toString();
        }
    }
    private static final class SynchronizedObjectPool<T> implements ObjectPool<T> {
        private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        private final ObjectPool<T> pool;
        SynchronizedObjectPool ( final ObjectPool<T> pool )
        throws IllegalArgumentException {
            if ( pool == null ) {
                throw new IllegalArgumentException ( "pool must not be null." );
            }
            this.pool = pool;
        }
        @Override
        public T borrowObject() throws Exception, NoSuchElementException,
            IllegalStateException {
            final WriteLock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                return pool.borrowObject();
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public void returnObject ( final T obj ) {
            final WriteLock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                pool.returnObject ( obj );
            } catch ( final Exception e ) {
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public void invalidateObject ( final T obj ) {
            final WriteLock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                pool.invalidateObject ( obj );
            } catch ( final Exception e ) {
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public void addObject() throws Exception, IllegalStateException,
            UnsupportedOperationException {
            final WriteLock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                pool.addObject();
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public int getNumIdle() {
            final ReadLock readLock = readWriteLock.readLock();
            readLock.lock();
            try {
                return pool.getNumIdle();
            } finally {
                readLock.unlock();
            }
        }
        @Override
        public int getNumActive() {
            final ReadLock readLock = readWriteLock.readLock();
            readLock.lock();
            try {
                return pool.getNumActive();
            } finally {
                readLock.unlock();
            }
        }
        @Override
        public void clear() throws Exception, UnsupportedOperationException {
            final WriteLock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                pool.clear();
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public void close() {
            final WriteLock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                pool.close();
            } catch ( final Exception e ) {
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append ( "SynchronizedObjectPool" );
            sb.append ( "{pool=" ).append ( pool );
            sb.append ( '}' );
            return sb.toString();
        }
    }
    private static final class SynchronizedKeyedObjectPool<K, V> implements
        KeyedObjectPool<K, V> {
        private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        private final KeyedObjectPool<K, V> keyedPool;
        SynchronizedKeyedObjectPool ( final KeyedObjectPool<K, V> keyedPool )
        throws IllegalArgumentException {
            if ( keyedPool == null ) {
                throw new IllegalArgumentException (
                    "keyedPool must not be null." );
            }
            this.keyedPool = keyedPool;
        }
        @Override
        public V borrowObject ( final K key ) throws Exception,
            NoSuchElementException, IllegalStateException {
            final WriteLock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                return keyedPool.borrowObject ( key );
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public void returnObject ( final K key, final V obj ) {
            final WriteLock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                keyedPool.returnObject ( key, obj );
            } catch ( final Exception e ) {
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public void invalidateObject ( final K key, final V obj ) {
            final WriteLock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                keyedPool.invalidateObject ( key, obj );
            } catch ( final Exception e ) {
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public void addObject ( final K key ) throws Exception,
            IllegalStateException, UnsupportedOperationException {
            final WriteLock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                keyedPool.addObject ( key );
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public int getNumIdle ( final K key ) {
            final ReadLock readLock = readWriteLock.readLock();
            readLock.lock();
            try {
                return keyedPool.getNumIdle ( key );
            } finally {
                readLock.unlock();
            }
        }
        @Override
        public int getNumActive ( final K key ) {
            final ReadLock readLock = readWriteLock.readLock();
            readLock.lock();
            try {
                return keyedPool.getNumActive ( key );
            } finally {
                readLock.unlock();
            }
        }
        @Override
        public int getNumIdle() {
            final ReadLock readLock = readWriteLock.readLock();
            readLock.lock();
            try {
                return keyedPool.getNumIdle();
            } finally {
                readLock.unlock();
            }
        }
        @Override
        public int getNumActive() {
            final ReadLock readLock = readWriteLock.readLock();
            readLock.lock();
            try {
                return keyedPool.getNumActive();
            } finally {
                readLock.unlock();
            }
        }
        @Override
        public void clear() throws Exception, UnsupportedOperationException {
            final WriteLock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                keyedPool.clear();
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public void clear ( final K key ) throws Exception,
            UnsupportedOperationException {
            final WriteLock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                keyedPool.clear ( key );
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public void close() {
            final WriteLock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                keyedPool.close();
            } catch ( final Exception e ) {
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append ( "SynchronizedKeyedObjectPool" );
            sb.append ( "{keyedPool=" ).append ( keyedPool );
            sb.append ( '}' );
            return sb.toString();
        }
    }
    private static final class SynchronizedPooledObjectFactory<T> implements
        PooledObjectFactory<T> {
        private final WriteLock writeLock = new ReentrantReadWriteLock().writeLock();
        private final PooledObjectFactory<T> factory;
        SynchronizedPooledObjectFactory ( final PooledObjectFactory<T> factory )
        throws IllegalArgumentException {
            if ( factory == null ) {
                throw new IllegalArgumentException ( "factory must not be null." );
            }
            this.factory = factory;
        }
        @Override
        public PooledObject<T> makeObject() throws Exception {
            writeLock.lock();
            try {
                return factory.makeObject();
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public void destroyObject ( final PooledObject<T> p ) throws Exception {
            writeLock.lock();
            try {
                factory.destroyObject ( p );
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public boolean validateObject ( final PooledObject<T> p ) {
            writeLock.lock();
            try {
                return factory.validateObject ( p );
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public void activateObject ( final PooledObject<T> p ) throws Exception {
            writeLock.lock();
            try {
                factory.activateObject ( p );
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public void passivateObject ( final PooledObject<T> p ) throws Exception {
            writeLock.lock();
            try {
                factory.passivateObject ( p );
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append ( "SynchronizedPoolableObjectFactory" );
            sb.append ( "{factory=" ).append ( factory );
            sb.append ( '}' );
            return sb.toString();
        }
    }
    private static final class SynchronizedKeyedPooledObjectFactory<K, V>
        implements KeyedPooledObjectFactory<K, V> {
        private final WriteLock writeLock = new ReentrantReadWriteLock().writeLock();
        private final KeyedPooledObjectFactory<K, V> keyedFactory;
        SynchronizedKeyedPooledObjectFactory (
            final KeyedPooledObjectFactory<K, V> keyedFactory )
        throws IllegalArgumentException {
            if ( keyedFactory == null ) {
                throw new IllegalArgumentException (
                    "keyedFactory must not be null." );
            }
            this.keyedFactory = keyedFactory;
        }
        @Override
        public PooledObject<V> makeObject ( final K key ) throws Exception {
            writeLock.lock();
            try {
                return keyedFactory.makeObject ( key );
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public void destroyObject ( final K key, final PooledObject<V> p ) throws Exception {
            writeLock.lock();
            try {
                keyedFactory.destroyObject ( key, p );
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public boolean validateObject ( final K key, final PooledObject<V> p ) {
            writeLock.lock();
            try {
                return keyedFactory.validateObject ( key, p );
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public void activateObject ( final K key, final PooledObject<V> p ) throws Exception {
            writeLock.lock();
            try {
                keyedFactory.activateObject ( key, p );
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public void passivateObject ( final K key, final PooledObject<V> p ) throws Exception {
            writeLock.lock();
            try {
                keyedFactory.passivateObject ( key, p );
            } finally {
                writeLock.unlock();
            }
        }
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append ( "SynchronizedKeyedPoolableObjectFactory" );
            sb.append ( "{keyedFactory=" ).append ( keyedFactory );
            sb.append ( '}' );
            return sb.toString();
        }
    }
    private static final class ErodingFactor {
        private final float factor;
        private transient volatile long nextShrink;
        private transient volatile int idleHighWaterMark;
        public ErodingFactor ( final float factor ) {
            this.factor = factor;
            nextShrink = System.currentTimeMillis() + ( long ) ( 900000 * factor );
            idleHighWaterMark = 1;
        }
        public void update ( final long now, final int numIdle ) {
            final int idle = Math.max ( 0, numIdle );
            idleHighWaterMark = Math.max ( idle, idleHighWaterMark );
            final float maxInterval = 15f;
            final float minutes = maxInterval +
                                  ( ( 1f - maxInterval ) / idleHighWaterMark ) * idle;
            nextShrink = now + ( long ) ( minutes * 60000f * factor );
        }
        public long getNextShrink() {
            return nextShrink;
        }
        @Override
        public String toString() {
            return "ErodingFactor{" + "factor=" + factor +
                   ", idleHighWaterMark=" + idleHighWaterMark + '}';
        }
    }
    private static class ErodingObjectPool<T> implements ObjectPool<T> {
        private final ObjectPool<T> pool;
        private final ErodingFactor factor;
        public ErodingObjectPool ( final ObjectPool<T> pool, final float factor ) {
            this.pool = pool;
            this.factor = new ErodingFactor ( factor );
        }
        @Override
        public T borrowObject() throws Exception, NoSuchElementException,
            IllegalStateException {
            return pool.borrowObject();
        }
        @Override
        public void returnObject ( final T obj ) {
            boolean discard = false;
            final long now = System.currentTimeMillis();
            synchronized ( pool ) {
                if ( factor.getNextShrink() < now ) {
                    final int numIdle = pool.getNumIdle();
                    if ( numIdle > 0 ) {
                        discard = true;
                    }
                    factor.update ( now, numIdle );
                }
            }
            try {
                if ( discard ) {
                    pool.invalidateObject ( obj );
                } else {
                    pool.returnObject ( obj );
                }
            } catch ( final Exception e ) {
            }
        }
        @Override
        public void invalidateObject ( final T obj ) {
            try {
                pool.invalidateObject ( obj );
            } catch ( final Exception e ) {
            }
        }
        @Override
        public void addObject() throws Exception, IllegalStateException,
            UnsupportedOperationException {
            pool.addObject();
        }
        @Override
        public int getNumIdle() {
            return pool.getNumIdle();
        }
        @Override
        public int getNumActive() {
            return pool.getNumActive();
        }
        @Override
        public void clear() throws Exception, UnsupportedOperationException {
            pool.clear();
        }
        @Override
        public void close() {
            try {
                pool.close();
            } catch ( final Exception e ) {
            }
        }
        @Override
        public String toString() {
            return "ErodingObjectPool{" + "factor=" + factor + ", pool=" +
                   pool + '}';
        }
    }
    private static class ErodingKeyedObjectPool<K, V> implements
        KeyedObjectPool<K, V> {
        private final KeyedObjectPool<K, V> keyedPool;
        private final ErodingFactor erodingFactor;
        public ErodingKeyedObjectPool ( final KeyedObjectPool<K, V> keyedPool,
                                        final float factor ) {
            this ( keyedPool, new ErodingFactor ( factor ) );
        }
        protected ErodingKeyedObjectPool ( final KeyedObjectPool<K, V> keyedPool,
                                           final ErodingFactor erodingFactor ) {
            if ( keyedPool == null ) {
                throw new IllegalArgumentException (
                    "keyedPool must not be null." );
            }
            this.keyedPool = keyedPool;
            this.erodingFactor = erodingFactor;
        }
        @Override
        public V borrowObject ( final K key ) throws Exception,
            NoSuchElementException, IllegalStateException {
            return keyedPool.borrowObject ( key );
        }
        @Override
        public void returnObject ( final K key, final V obj ) throws Exception {
            boolean discard = false;
            final long now = System.currentTimeMillis();
            final ErodingFactor factor = getErodingFactor ( key );
            synchronized ( keyedPool ) {
                if ( factor.getNextShrink() < now ) {
                    final int numIdle = getNumIdle ( key );
                    if ( numIdle > 0 ) {
                        discard = true;
                    }
                    factor.update ( now, numIdle );
                }
            }
            try {
                if ( discard ) {
                    keyedPool.invalidateObject ( key, obj );
                } else {
                    keyedPool.returnObject ( key, obj );
                }
            } catch ( final Exception e ) {
            }
        }
        protected ErodingFactor getErodingFactor ( final K key ) {
            return erodingFactor;
        }
        @Override
        public void invalidateObject ( final K key, final V obj ) {
            try {
                keyedPool.invalidateObject ( key, obj );
            } catch ( final Exception e ) {
            }
        }
        @Override
        public void addObject ( final K key ) throws Exception,
            IllegalStateException, UnsupportedOperationException {
            keyedPool.addObject ( key );
        }
        @Override
        public int getNumIdle() {
            return keyedPool.getNumIdle();
        }
        @Override
        public int getNumIdle ( final K key ) {
            return keyedPool.getNumIdle ( key );
        }
        @Override
        public int getNumActive() {
            return keyedPool.getNumActive();
        }
        @Override
        public int getNumActive ( final K key ) {
            return keyedPool.getNumActive ( key );
        }
        @Override
        public void clear() throws Exception, UnsupportedOperationException {
            keyedPool.clear();
        }
        @Override
        public void clear ( final K key ) throws Exception,
            UnsupportedOperationException {
            keyedPool.clear ( key );
        }
        @Override
        public void close() {
            try {
                keyedPool.close();
            } catch ( final Exception e ) {
            }
        }
        protected KeyedObjectPool<K, V> getKeyedPool() {
            return keyedPool;
        }
        @Override
        public String toString() {
            return "ErodingKeyedObjectPool{" + "factor=" +
                   erodingFactor + ", keyedPool=" + keyedPool + '}';
        }
    }
    private static final class ErodingPerKeyKeyedObjectPool<K, V> extends
        ErodingKeyedObjectPool<K, V> {
        private final float factor;
        private final Map<K, ErodingFactor> factors = Collections.synchronizedMap ( new HashMap<K, ErodingFactor>() );
        public ErodingPerKeyKeyedObjectPool (
            final KeyedObjectPool<K, V> keyedPool, final float factor ) {
            super ( keyedPool, null );
            this.factor = factor;
        }
        @Override
        protected ErodingFactor getErodingFactor ( final K key ) {
            ErodingFactor eFactor = factors.get ( key );
            if ( eFactor == null ) {
                eFactor = new ErodingFactor ( this.factor );
                factors.put ( key, eFactor );
            }
            return eFactor;
        }
        @Override
        public String toString() {
            return "ErodingPerKeyKeyedObjectPool{" + "factor=" + factor +
                   ", keyedPool=" + getKeyedPool() + '}';
        }
    }
}
