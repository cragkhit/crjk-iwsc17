// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.BlockingQueue;

public class FairBlockingQueue<E> implements BlockingQueue<E>
{
    static final boolean isLinux;
    final ReentrantLock lock;
    final LinkedList<E> items;
    final LinkedList<ExchangeCountDownLatch<E>> waiters;
    
    public FairBlockingQueue() {
        this.lock = new ReentrantLock(false);
        this.items = new LinkedList<E>();
        this.waiters = new LinkedList<ExchangeCountDownLatch<E>>();
    }
    
    @Override
    public boolean offer(final E e) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        ExchangeCountDownLatch<E> c = null;
        try {
            if (this.waiters.size() > 0) {
                c = this.waiters.poll();
                c.setItem(e);
                if (FairBlockingQueue.isLinux) {
                    c.countDown();
                }
            }
            else {
                this.items.addFirst(e);
            }
        }
        finally {
            lock.unlock();
        }
        if (!FairBlockingQueue.isLinux && c != null) {
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
        E result = null;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            result = this.items.poll();
            if (result == null && timeout > 0L) {
                final ExchangeCountDownLatch<E> c = new ExchangeCountDownLatch<E>(1);
                this.waiters.addLast(c);
                lock.unlock();
                boolean didtimeout = true;
                InterruptedException interruptedException = null;
                try {
                    didtimeout = !c.await(timeout, unit);
                }
                catch (InterruptedException ix) {
                    interruptedException = ix;
                }
                if (didtimeout) {
                    lock.lock();
                    try {
                        this.waiters.remove(c);
                    }
                    finally {
                        lock.unlock();
                    }
                }
                result = c.getItem();
                if (null != interruptedException) {
                    if (null == result) {
                        throw interruptedException;
                    }
                    Thread.interrupted();
                }
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
        Future<E> result = null;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final E item = this.items.poll();
            if (item == null) {
                final ExchangeCountDownLatch<E> c = new ExchangeCountDownLatch<E>(1);
                this.waiters.addLast(c);
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
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return this.items.remove(e);
        }
        finally {
            lock.unlock();
        }
    }
    
    @Override
    public int size() {
        return this.items.size();
    }
    
    @Override
    public Iterator<E> iterator() {
        return new FairIterator();
    }
    
    @Override
    public E poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return this.items.poll();
        }
        finally {
            lock.unlock();
        }
    }
    
    @Override
    public boolean contains(final Object e) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return this.items.contains(e);
        }
        finally {
            lock.unlock();
        }
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
    
    static {
        isLinux = ("Linux".equals(System.getProperty("os.name")) && !Boolean.getBoolean(FairBlockingQueue.class.getName() + ".ignoreOS"));
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
}
