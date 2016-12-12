package org.apache.tomcat.dbcp.pool2;
import java.util.concurrent.locks.ReentrantReadWriteLock;
private static final class SynchronizedPooledObjectFactory<T> implements PooledObjectFactory<T> {
    private final ReentrantReadWriteLock.WriteLock writeLock;
    private final PooledObjectFactory<T> factory;
    SynchronizedPooledObjectFactory ( final PooledObjectFactory<T> factory ) throws IllegalArgumentException {
        this.writeLock = new ReentrantReadWriteLock().writeLock();
        if ( factory == null ) {
            throw new IllegalArgumentException ( "factory must not be null." );
        }
        this.factory = factory;
    }
    @Override
    public PooledObject<T> makeObject() throws Exception {
        this.writeLock.lock();
        try {
            return this.factory.makeObject();
        } finally {
            this.writeLock.unlock();
        }
    }
    @Override
    public void destroyObject ( final PooledObject<T> p ) throws Exception {
        this.writeLock.lock();
        try {
            this.factory.destroyObject ( p );
        } finally {
            this.writeLock.unlock();
        }
    }
    @Override
    public boolean validateObject ( final PooledObject<T> p ) {
        this.writeLock.lock();
        try {
            return this.factory.validateObject ( p );
        } finally {
            this.writeLock.unlock();
        }
    }
    @Override
    public void activateObject ( final PooledObject<T> p ) throws Exception {
        this.writeLock.lock();
        try {
            this.factory.activateObject ( p );
        } finally {
            this.writeLock.unlock();
        }
    }
    @Override
    public void passivateObject ( final PooledObject<T> p ) throws Exception {
        this.writeLock.lock();
        try {
            this.factory.passivateObject ( p );
        } finally {
            this.writeLock.unlock();
        }
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append ( "SynchronizedPoolableObjectFactory" );
        sb.append ( "{factory=" ).append ( this.factory );
        sb.append ( '}' );
        return sb.toString();
    }
}
