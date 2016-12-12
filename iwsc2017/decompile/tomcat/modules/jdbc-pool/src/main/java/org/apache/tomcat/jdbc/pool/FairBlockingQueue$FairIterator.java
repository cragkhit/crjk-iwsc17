// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Iterator;

protected class FairIterator implements Iterator<E>
{
    E[] elements;
    int index;
    E element;
    
    public FairIterator() {
        this.elements = null;
        this.element = null;
        final ReentrantLock lock = FairBlockingQueue.this.lock;
        lock.lock();
        try {
            this.elements = new Object[FairBlockingQueue.this.items.size()];
            FairBlockingQueue.this.items.toArray(this.elements);
            this.index = 0;
        }
        finally {
            lock.unlock();
        }
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
        final ReentrantLock lock = FairBlockingQueue.this.lock;
        lock.lock();
        try {
            if (this.element != null) {
                FairBlockingQueue.this.items.remove(this.element);
            }
        }
        finally {
            lock.unlock();
        }
    }
}
