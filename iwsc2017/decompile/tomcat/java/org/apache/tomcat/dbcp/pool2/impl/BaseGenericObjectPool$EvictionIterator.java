package org.apache.tomcat.dbcp.pool2.impl;
import java.util.Deque;
import org.apache.tomcat.dbcp.pool2.PooledObject;
import java.util.Iterator;
class EvictionIterator implements Iterator<PooledObject<T>> {
    private final Deque<PooledObject<T>> idleObjects;
    private final Iterator<PooledObject<T>> idleObjectIterator;
    EvictionIterator ( final Deque<PooledObject<T>> idleObjects ) {
        this.idleObjects = idleObjects;
        if ( BaseGenericObjectPool.this.getLifo() ) {
            this.idleObjectIterator = idleObjects.descendingIterator();
        } else {
            this.idleObjectIterator = idleObjects.iterator();
        }
    }
    public Deque<PooledObject<T>> getIdleObjects() {
        return this.idleObjects;
    }
    @Override
    public boolean hasNext() {
        return this.idleObjectIterator.hasNext();
    }
    @Override
    public PooledObject<T> next() {
        return this.idleObjectIterator.next();
    }
    @Override
    public void remove() {
        this.idleObjectIterator.remove();
    }
}
