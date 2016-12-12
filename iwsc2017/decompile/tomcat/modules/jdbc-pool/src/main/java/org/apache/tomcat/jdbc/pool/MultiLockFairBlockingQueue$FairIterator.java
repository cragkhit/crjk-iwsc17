// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import java.util.Iterator;

protected class FairIterator implements Iterator<E>
{
    E[] elements;
    int index;
    E element;
    
    public FairIterator() {
        this.elements = null;
        this.element = null;
        final ArrayList<E> list = new ArrayList<E>(MultiLockFairBlockingQueue.this.size());
        for (int idx = 0; idx < MultiLockFairBlockingQueue.this.LOCK_COUNT; ++idx) {
            final ReentrantLock lock = MultiLockFairBlockingQueue.access$000(MultiLockFairBlockingQueue.this)[idx];
            lock.lock();
            try {
                this.elements = new Object[MultiLockFairBlockingQueue.this.items[idx].size()];
                MultiLockFairBlockingQueue.this.items[idx].toArray(this.elements);
            }
            finally {
                lock.unlock();
            }
        }
        this.index = 0;
        list.toArray(this.elements = new Object[list.size()]);
    }
    
    @Override
    public boolean hasNext() {
        return this.index < this.elements.length;
    }
    
    @Override
    public E next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        return this.element = (E)this.elements[this.index++];
    }
    
    @Override
    public void remove() {
        for (int idx = 0; idx < MultiLockFairBlockingQueue.this.LOCK_COUNT; ++idx) {
            final ReentrantLock lock = MultiLockFairBlockingQueue.access$000(MultiLockFairBlockingQueue.this)[idx];
            lock.lock();
            try {
                final boolean result = MultiLockFairBlockingQueue.this.items[idx].remove(this.elements[this.index]);
                if (result) {
                    break;
                }
            }
            finally {
                lock.unlock();
            }
        }
    }
}
