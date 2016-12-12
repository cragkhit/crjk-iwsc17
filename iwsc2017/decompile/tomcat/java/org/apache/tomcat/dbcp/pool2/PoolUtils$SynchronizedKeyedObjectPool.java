package org.apache.tomcat.dbcp.pool2;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
private static final class SynchronizedKeyedObjectPool<K, V> implements KeyedObjectPool<K, V> {
    private final ReentrantReadWriteLock readWriteLock;
    private final KeyedObjectPool<K, V> keyedPool;
    SynchronizedKeyedObjectPool ( final KeyedObjectPool<K, V> keyedPool ) throws IllegalArgumentException {
        this.readWriteLock = new ReentrantReadWriteLock();
        if ( keyedPool == null ) {
            throw new IllegalArgumentException ( "keyedPool must not be null." );
        }
        this.keyedPool = keyedPool;
    }
    @Override
    public V borrowObject ( final K key ) throws Exception, NoSuchElementException, IllegalStateException {
        final ReentrantReadWriteLock.WriteLock writeLock = this.readWriteLock.writeLock();
        writeLock.lock();
        try {
            return this.keyedPool.borrowObject ( key );
        } finally {
            writeLock.unlock();
        }
    }
    @Override
    public void returnObject ( final K key, final V obj ) {
        final ReentrantReadWriteLock.WriteLock writeLock = this.readWriteLock.writeLock();
        writeLock.lock();
        try {
            this.keyedPool.returnObject ( key, obj );
        } catch ( Exception ex ) {}
        finally {
            writeLock.unlock();
        }
    }
    @Override
    public void invalidateObject ( final K key, final V obj ) {
        final ReentrantReadWriteLock.WriteLock writeLock = this.readWriteLock.writeLock();
        writeLock.lock();
        try {
            this.keyedPool.invalidateObject ( key, obj );
        } catch ( Exception ex ) {}
        finally {
            writeLock.unlock();
        }
    }
    @Override
    public void addObject ( final K key ) throws Exception, IllegalStateException, UnsupportedOperationException {
        final ReentrantReadWriteLock.WriteLock writeLock = this.readWriteLock.writeLock();
        writeLock.lock();
        try {
            this.keyedPool.addObject ( key );
        } finally {
            writeLock.unlock();
        }
    }
    @Override
    public int getNumIdle ( final K key ) {
        final ReentrantReadWriteLock.ReadLock readLock = this.readWriteLock.readLock();
        readLock.lock();
        try {
            return this.keyedPool.getNumIdle ( key );
        } finally {
            readLock.unlock();
        }
    }
    @Override
    public int getNumActive ( final K key ) {
        final ReentrantReadWriteLock.ReadLock readLock = this.readWriteLock.readLock();
        readLock.lock();
        try {
            return this.keyedPool.getNumActive ( key );
        } finally {
            readLock.unlock();
        }
    }
    @Override
    public int getNumIdle() {
        final ReentrantReadWriteLock.ReadLock readLock = this.readWriteLock.readLock();
        readLock.lock();
        try {
            return this.keyedPool.getNumIdle();
        } finally {
            readLock.unlock();
        }
    }
    @Override
    public int getNumActive() {
        final ReentrantReadWriteLock.ReadLock readLock = this.readWriteLock.readLock();
        readLock.lock();
        try {
            return this.keyedPool.getNumActive();
        } finally {
            readLock.unlock();
        }
    }
    @Override
    public void clear() throws Exception, UnsupportedOperationException {
        final ReentrantReadWriteLock.WriteLock writeLock = this.readWriteLock.writeLock();
        writeLock.lock();
        try {
            this.keyedPool.clear();
        } finally {
            writeLock.unlock();
        }
    }
    @Override
    public void clear ( final K key ) throws Exception, UnsupportedOperationException {
        final ReentrantReadWriteLock.WriteLock writeLock = this.readWriteLock.writeLock();
        writeLock.lock();
        try {
            this.keyedPool.clear ( key );
        } finally {
            writeLock.unlock();
        }
    }
    @Override
    public void close() {
        final ReentrantReadWriteLock.WriteLock writeLock = this.readWriteLock.writeLock();
        writeLock.lock();
        try {
            this.keyedPool.close();
        } catch ( Exception ex ) {}
        finally {
            writeLock.unlock();
        }
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append ( "SynchronizedKeyedObjectPool" );
        sb.append ( "{keyedPool=" ).append ( this.keyedPool );
        sb.append ( '}' );
        return sb.toString();
    }
}
