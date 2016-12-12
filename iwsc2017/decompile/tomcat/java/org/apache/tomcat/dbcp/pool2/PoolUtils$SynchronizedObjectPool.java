package org.apache.tomcat.dbcp.pool2;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
private static final class SynchronizedObjectPool<T> implements ObjectPool<T> {
    private final ReentrantReadWriteLock readWriteLock;
    private final ObjectPool<T> pool;
    SynchronizedObjectPool ( final ObjectPool<T> pool ) throws IllegalArgumentException {
        this.readWriteLock = new ReentrantReadWriteLock();
        if ( pool == null ) {
            throw new IllegalArgumentException ( "pool must not be null." );
        }
        this.pool = pool;
    }
    @Override
    public T borrowObject() throws Exception, NoSuchElementException, IllegalStateException {
        final ReentrantReadWriteLock.WriteLock writeLock = this.readWriteLock.writeLock();
        writeLock.lock();
        try {
            return this.pool.borrowObject();
        } finally {
            writeLock.unlock();
        }
    }
    @Override
    public void returnObject ( final T obj ) {
        final ReentrantReadWriteLock.WriteLock writeLock = this.readWriteLock.writeLock();
        writeLock.lock();
        try {
            this.pool.returnObject ( obj );
        } catch ( Exception ex ) {}
        finally {
            writeLock.unlock();
        }
    }
    @Override
    public void invalidateObject ( final T obj ) {
        final ReentrantReadWriteLock.WriteLock writeLock = this.readWriteLock.writeLock();
        writeLock.lock();
        try {
            this.pool.invalidateObject ( obj );
        } catch ( Exception ex ) {}
        finally {
            writeLock.unlock();
        }
    }
    @Override
    public void addObject() throws Exception, IllegalStateException, UnsupportedOperationException {
        final ReentrantReadWriteLock.WriteLock writeLock = this.readWriteLock.writeLock();
        writeLock.lock();
        try {
            this.pool.addObject();
        } finally {
            writeLock.unlock();
        }
    }
    @Override
    public int getNumIdle() {
        final ReentrantReadWriteLock.ReadLock readLock = this.readWriteLock.readLock();
        readLock.lock();
        try {
            return this.pool.getNumIdle();
        } finally {
            readLock.unlock();
        }
    }
    @Override
    public int getNumActive() {
        final ReentrantReadWriteLock.ReadLock readLock = this.readWriteLock.readLock();
        readLock.lock();
        try {
            return this.pool.getNumActive();
        } finally {
            readLock.unlock();
        }
    }
    @Override
    public void clear() throws Exception, UnsupportedOperationException {
        final ReentrantReadWriteLock.WriteLock writeLock = this.readWriteLock.writeLock();
        writeLock.lock();
        try {
            this.pool.clear();
        } finally {
            writeLock.unlock();
        }
    }
    @Override
    public void close() {
        final ReentrantReadWriteLock.WriteLock writeLock = this.readWriteLock.writeLock();
        writeLock.lock();
        try {
            this.pool.close();
        } catch ( Exception ex ) {}
        finally {
            writeLock.unlock();
        }
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append ( "SynchronizedObjectPool" );
        sb.append ( "{pool=" ).append ( this.pool );
        sb.append ( '}' );
        return sb.toString();
    }
}
