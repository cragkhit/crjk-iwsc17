package org.apache.tomcat.dbcp.pool2;
import java.util.concurrent.locks.ReentrantReadWriteLock;
private static final class SynchronizedKeyedPooledObjectFactory<K, V> implements KeyedPooledObjectFactory<K, V> {
    private final ReentrantReadWriteLock.WriteLock writeLock;
    private final KeyedPooledObjectFactory<K, V> keyedFactory;
    SynchronizedKeyedPooledObjectFactory ( final KeyedPooledObjectFactory<K, V> keyedFactory ) throws IllegalArgumentException {
        this.writeLock = new ReentrantReadWriteLock().writeLock();
        if ( keyedFactory == null ) {
            throw new IllegalArgumentException ( "keyedFactory must not be null." );
        }
        this.keyedFactory = keyedFactory;
    }
    @Override
    public PooledObject<V> makeObject ( final K key ) throws Exception {
        this.writeLock.lock();
        try {
            return this.keyedFactory.makeObject ( key );
        } finally {
            this.writeLock.unlock();
        }
    }
    @Override
    public void destroyObject ( final K key, final PooledObject<V> p ) throws Exception {
        this.writeLock.lock();
        try {
            this.keyedFactory.destroyObject ( key, p );
        } finally {
            this.writeLock.unlock();
        }
    }
    @Override
    public boolean validateObject ( final K key, final PooledObject<V> p ) {
        this.writeLock.lock();
        try {
            return this.keyedFactory.validateObject ( key, p );
        } finally {
            this.writeLock.unlock();
        }
    }
    @Override
    public void activateObject ( final K key, final PooledObject<V> p ) throws Exception {
        this.writeLock.lock();
        try {
            this.keyedFactory.activateObject ( key, p );
        } finally {
            this.writeLock.unlock();
        }
    }
    @Override
    public void passivateObject ( final K key, final PooledObject<V> p ) throws Exception {
        this.writeLock.lock();
        try {
            this.keyedFactory.passivateObject ( key, p );
        } finally {
            this.writeLock.unlock();
        }
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append ( "SynchronizedKeyedPoolableObjectFactory" );
        sb.append ( "{keyedFactory=" ).append ( this.keyedFactory );
        sb.append ( '}' );
        return sb.toString();
    }
}
