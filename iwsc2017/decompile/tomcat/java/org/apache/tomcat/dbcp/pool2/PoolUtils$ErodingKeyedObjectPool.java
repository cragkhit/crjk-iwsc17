package org.apache.tomcat.dbcp.pool2;
import java.util.NoSuchElementException;
private static class ErodingKeyedObjectPool<K, V> implements KeyedObjectPool<K, V> {
    private final KeyedObjectPool<K, V> keyedPool;
    private final ErodingFactor erodingFactor;
    public ErodingKeyedObjectPool ( final KeyedObjectPool<K, V> keyedPool, final float factor ) {
        this ( keyedPool, new ErodingFactor ( factor ) );
    }
    protected ErodingKeyedObjectPool ( final KeyedObjectPool<K, V> keyedPool, final ErodingFactor erodingFactor ) {
        if ( keyedPool == null ) {
            throw new IllegalArgumentException ( "keyedPool must not be null." );
        }
        this.keyedPool = keyedPool;
        this.erodingFactor = erodingFactor;
    }
    @Override
    public V borrowObject ( final K key ) throws Exception, NoSuchElementException, IllegalStateException {
        return this.keyedPool.borrowObject ( key );
    }
    @Override
    public void returnObject ( final K key, final V obj ) throws Exception {
        boolean discard = false;
        final long now = System.currentTimeMillis();
        final ErodingFactor factor = this.getErodingFactor ( key );
        synchronized ( this.keyedPool ) {
            if ( factor.getNextShrink() < now ) {
                final int numIdle = this.getNumIdle ( key );
                if ( numIdle > 0 ) {
                    discard = true;
                }
                factor.update ( now, numIdle );
            }
        }
        try {
            if ( discard ) {
                this.keyedPool.invalidateObject ( key, obj );
            } else {
                this.keyedPool.returnObject ( key, obj );
            }
        } catch ( Exception ex ) {}
    }
    protected ErodingFactor getErodingFactor ( final K key ) {
        return this.erodingFactor;
    }
    @Override
    public void invalidateObject ( final K key, final V obj ) {
        try {
            this.keyedPool.invalidateObject ( key, obj );
        } catch ( Exception ex ) {}
    }
    @Override
    public void addObject ( final K key ) throws Exception, IllegalStateException, UnsupportedOperationException {
        this.keyedPool.addObject ( key );
    }
    @Override
    public int getNumIdle() {
        return this.keyedPool.getNumIdle();
    }
    @Override
    public int getNumIdle ( final K key ) {
        return this.keyedPool.getNumIdle ( key );
    }
    @Override
    public int getNumActive() {
        return this.keyedPool.getNumActive();
    }
    @Override
    public int getNumActive ( final K key ) {
        return this.keyedPool.getNumActive ( key );
    }
    @Override
    public void clear() throws Exception, UnsupportedOperationException {
        this.keyedPool.clear();
    }
    @Override
    public void clear ( final K key ) throws Exception, UnsupportedOperationException {
        this.keyedPool.clear ( key );
    }
    @Override
    public void close() {
        try {
            this.keyedPool.close();
        } catch ( Exception ex ) {}
    }
    protected KeyedObjectPool<K, V> getKeyedPool() {
        return this.keyedPool;
    }
    @Override
    public String toString() {
        return "ErodingKeyedObjectPool{factor=" + this.erodingFactor + ", keyedPool=" + this.keyedPool + '}';
    }
}
