// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.BlockingQueue;

public class MultiLockFairBlockingQueue<E> implements BlockingQueue<E>
{
    final int LOCK_COUNT;
    final AtomicInteger putQueue;
    final AtomicInteger pollQueue;
    private final ReentrantLock[] locks;
    final LinkedList<E>[] items;
    final LinkedList<ExchangeCountDownLatch<E>>[] waiters;
    
    public int getNextPut() {
        final int idx = Math.abs(this.putQueue.incrementAndGet()) % this.LOCK_COUNT;
        return idx;
    }
    
    public int getNextPoll() {
        final int idx = Math.abs(this.pollQueue.incrementAndGet()) % this.LOCK_COUNT;
        return idx;
    }
    
    public MultiLockFairBlockingQueue() {
        this.LOCK_COUNT = Runtime.getRuntime().availableProcessors();
        this.putQueue = new AtomicInteger(0);
        this.pollQueue = new AtomicInteger(0);
        this.locks = new ReentrantLock[this.LOCK_COUNT];
        this.items = (LinkedList<E>[])new LinkedList[this.LOCK_COUNT];
        this.waiters = (LinkedList<ExchangeCountDownLatch<E>>[])new LinkedList[this.LOCK_COUNT];
        for (int i = 0; i < this.LOCK_COUNT; ++i) {
            this.items[i] = new LinkedList<E>();
            this.waiters[i] = new LinkedList<ExchangeCountDownLatch<E>>();
            this.locks[i] = new ReentrantLock(false);
        }
    }
    
    @Override
    public boolean offer(final E e) {
        final int idx = this.getNextPut();
        final ReentrantLock lock = this.locks[idx];
        lock.lock();
        ExchangeCountDownLatch<E> c = null;
        try {
            if (this.waiters[idx].size() > 0) {
                c = this.waiters[idx].poll();
                c.setItem(e);
            }
            else {
                this.items[idx].addFirst(e);
            }
        }
        finally {
            lock.unlock();
        }
        if (c != null) {
            c.countDown();
        }
        return true;
    }
    
    @Override
    public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
        return this.offer(e);
    }
    
    @Override
    public E poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        final int idx = this.getNextPoll();
        E result = null;
        final ReentrantLock lock = this.locks[idx];
        try {
            lock.lock();
            result = this.items[idx].poll();
            if (result == null && timeout > 0L) {
                final ExchangeCountDownLatch<E> c = new ExchangeCountDownLatch<E>(1);
                this.waiters[idx].addLast(c);
                lock.unlock();
                if (!c.await(timeout, unit)) {
                    lock.lock();
                    this.waiters[idx].remove(c);
                    lock.unlock();
                }
                result = c.getItem();
            }
            else {
                lock.unlock();
            }
        }
        finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return result;
    }
    
    public Future<E> pollAsync() {
        final int idx = this.getNextPoll();
        Future<E> result = null;
        final ReentrantLock lock = this.locks[idx];
        try {
            lock.lock();
            final E item = this.items[idx].poll();
            if (item == null) {
                final ExchangeCountDownLatch<E> c = new ExchangeCountDownLatch<E>(1);
                this.waiters[idx].addLast(c);
                result = new ItemFuture<E>(c);
            }
            else {
                result = new ItemFuture<E>(item);
            }
        }
        finally {
            lock.unlock();
        }
        return result;
    }
    
    @Override
    public boolean remove(final Object e) {
        for (int idx = 0; idx < this.LOCK_COUNT; ++idx) {
            final ReentrantLock lock = this.locks[idx];
            lock.lock();
            try {
                final boolean result = this.items[idx].remove(e);
                if (result) {
                    return result;
                }
            }
            finally {
                lock.unlock();
            }
        }
        return false;
    }
    
    @Override
    public int size() {
        int size = 0;
        for (int idx = 0; idx < this.LOCK_COUNT; ++idx) {
            size += this.items[idx].size();
        }
        return size;
    }
    
    @Override
    public Iterator<E> iterator() {
        return new FairIterator();
    }
    
    @Override
    public E poll() {
        final int idx = this.getNextPoll();
        final ReentrantLock lock = this.locks[idx];
        lock.lock();
        try {
            return this.items[idx].poll();
        }
        finally {
            lock.unlock();
        }
    }
    
    @Override
    public boolean contains(final Object e) {
        for (int idx = 0; idx < this.LOCK_COUNT; ++idx) {
            final boolean result = this.items[idx].contains(e);
            if (result) {
                return result;
            }
        }
        return false;
    }
    
    @Override
    public boolean add(final E e) {
        return this.offer(e);
    }
    
    @Override
    public int drainTo(final Collection<? super E> c, final int maxElements) {
        throw new UnsupportedOperationException("int drainTo(Collection<? super E> c, int maxElements)");
    }
    
    @Override
    public int drainTo(final Collection<? super E> c) {
        return this.drainTo(c, Integer.MAX_VALUE);
    }
    
    @Override
    public void put(final E e) throws InterruptedException {
        this.offer(e);
    }
    
    @Override
    public int remainingCapacity() {
        return Integer.MAX_VALUE - this.size();
    }
    
    @Override
    public E take() throws InterruptedException {
        return this.poll(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public boolean addAll(final Collection<? extends E> c) {
        for (final E e : c) {
            this.offer(e);
        }
        return true;
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException("void clear()");
    }
    
    @Override
    public boolean containsAll(final Collection<?> c) {
        throw new UnsupportedOperationException("boolean containsAll(Collection<?> c)");
    }
    
    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }
    
    @Override
    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException("boolean removeAll(Collection<?> c)");
    }
    
    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException("boolean retainAll(Collection<?> c)");
    }
    
    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Object[] toArray()");
    }
    
    @Override
    public <T> T[] toArray(final T[] a) {
        throw new UnsupportedOperationException("<T> T[] toArray(T[] a)");
    }
    
    @Override
    public E element() {
        throw new UnsupportedOperationException("E element()");
    }
    
    @Override
    public E peek() {
        throw new UnsupportedOperationException("E peek()");
    }
    
    @Override
    public E remove() {
        throw new UnsupportedOperationException("E remove()");
    }
    
    protected class ItemFuture<T> implements Future<T>
    {
        protected volatile T item;
        protected volatile ExchangeCountDownLatch<T> latch;
        protected volatile boolean canceled;
        
        public ItemFuture(final T item) {
            this.item = null;
            this.latch = null;
            this.canceled = false;
            this.item = item;
        }
        
        public ItemFuture(final ExchangeCountDownLatch<T> latch) {
            this.item = null;
            this.latch = null;
            this.canceled = false;
            this.latch = latch;
        }
        
        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            return false;
        }
        
        @Override
        public T get() throws InterruptedException, ExecutionException {
            if (this.item != null) {
                return this.item;
            }
            if (this.latch != null) {
                this.latch.await();
                return this.latch.getItem();
            }
            throw new ExecutionException("ItemFuture incorrectly instantiated. Bug in the code?", new Exception());
        }
        
        @Override
        public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            if (this.item != null) {
                return this.item;
            }
            if (this.latch == null) {
                throw new ExecutionException("ItemFuture incorrectly instantiated. Bug in the code?", new Exception());
            }
            final boolean timedout = !this.latch.await(timeout, unit);
            if (timedout) {
                throw new TimeoutException();
            }
            return this.latch.getItem();
        }
        
        @Override
        public boolean isCancelled() {
            return false;
        }
        
        @Override
        public boolean isDone() {
            return this.item != null || this.latch.getItem() != null;
        }
    }
    
    protected class ExchangeCountDownLatch<T> extends CountDownLatch
    {
        protected volatile T item;
        
        public ExchangeCountDownLatch(final int i) {
            super(i);
        }
        
        public T getItem() {
            return this.item;
        }
        
        public void setItem(final T item) {
            this.item = item;
        }
    }
    
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
                final ReentrantLock lock = MultiLockFairBlockingQueue.this.locks[idx];
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
                final ReentrantLock lock = MultiLockFairBlockingQueue.this.locks[idx];
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
}
