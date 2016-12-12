package org.apache.tomcat.dbcp.pool2;
import java.util.NoSuchElementException;
private static class ErodingObjectPool<T> implements ObjectPool<T> {
    private final ObjectPool<T> pool;
    private final ErodingFactor factor;
    public ErodingObjectPool ( final ObjectPool<T> pool, final float factor ) {
        this.pool = pool;
        this.factor = new ErodingFactor ( factor );
    }
    @Override
    public T borrowObject() throws Exception, NoSuchElementException, IllegalStateException {
        return this.pool.borrowObject();
    }
    @Override
    public void returnObject ( final T obj ) {
        boolean discard = false;
        final long now = System.currentTimeMillis();
        synchronized ( this.pool ) {
            if ( this.factor.getNextShrink() < now ) {
                final int numIdle = this.pool.getNumIdle();
                if ( numIdle > 0 ) {
                    discard = true;
                }
                this.factor.update ( now, numIdle );
            }
        }
        try {
            if ( discard ) {
                this.pool.invalidateObject ( obj );
            } else {
                this.pool.returnObject ( obj );
            }
        } catch ( Exception ex ) {}
    }
    @Override
    public void invalidateObject ( final T obj ) {
        try {
            this.pool.invalidateObject ( obj );
        } catch ( Exception ex ) {}
    }
    @Override
    public void addObject() throws Exception, IllegalStateException, UnsupportedOperationException {
        this.pool.addObject();
    }
    @Override
    public int getNumIdle() {
        return this.pool.getNumIdle();
    }
    @Override
    public int getNumActive() {
        return this.pool.getNumActive();
    }
    @Override
    public void clear() throws Exception, UnsupportedOperationException {
        this.pool.clear();
    }
    @Override
    public void close() {
        try {
            this.pool.close();
        } catch ( Exception ex ) {}
    }
    @Override
    public String toString() {
        return "ErodingObjectPool{factor=" + this.factor + ", pool=" + this.pool + '}';
    }
}
