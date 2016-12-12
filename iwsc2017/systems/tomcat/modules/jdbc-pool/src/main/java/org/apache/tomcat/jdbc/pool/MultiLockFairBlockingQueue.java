package org.apache.tomcat.jdbc.pool;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
public class MultiLockFairBlockingQueue<E> implements BlockingQueue<E> {
    final int LOCK_COUNT = Runtime.getRuntime().availableProcessors();
    final AtomicInteger putQueue = new AtomicInteger ( 0 );
    final AtomicInteger pollQueue = new AtomicInteger ( 0 );
    public int getNextPut() {
        int idx = Math.abs ( putQueue.incrementAndGet() ) % LOCK_COUNT;
        return idx;
    }
    public int getNextPoll() {
        int idx = Math.abs ( pollQueue.incrementAndGet() ) % LOCK_COUNT;
        return idx;
    }
    private final ReentrantLock[] locks = new ReentrantLock[LOCK_COUNT];
    final LinkedList<E>[] items;
    final LinkedList<ExchangeCountDownLatch<E>>[] waiters;
    @SuppressWarnings ( "unchecked" )
    public MultiLockFairBlockingQueue() {
        items = new LinkedList[LOCK_COUNT];
        waiters = new LinkedList[LOCK_COUNT];
        for ( int i = 0; i < LOCK_COUNT; i++ ) {
            items[i] = new LinkedList<>();
            waiters[i] = new LinkedList<>();
            locks[i] = new ReentrantLock ( false );
        }
    }
    @Override
    public boolean offer ( E e ) {
        int idx = getNextPut();
        final ReentrantLock lock = this.locks[idx];
        lock.lock();
        ExchangeCountDownLatch<E> c = null;
        try {
            if ( waiters[idx].size() > 0 ) {
                c = waiters[idx].poll();
                c.setItem ( e );
            } else {
                items[idx].addFirst ( e );
            }
        } finally {
            lock.unlock();
        }
        if ( c != null ) {
            c.countDown();
        }
        return true;
    }
    @Override
    public boolean offer ( E e, long timeout, TimeUnit unit ) throws InterruptedException {
        return offer ( e );
    }
    @Override
    public E poll ( long timeout, TimeUnit unit ) throws InterruptedException {
        int idx = getNextPoll();
        E result = null;
        final ReentrantLock lock = this.locks[idx];
        try {
            lock.lock();
            result = items[idx].poll();
            if ( result == null && timeout > 0 ) {
                ExchangeCountDownLatch<E> c = new ExchangeCountDownLatch<> ( 1 );
                waiters[idx].addLast ( c );
                lock.unlock();
                if ( !c.await ( timeout, unit ) ) {
                    lock.lock();
                    waiters[idx].remove ( c );
                    lock.unlock();
                }
                result = c.getItem();
            } else {
                lock.unlock();
            }
        } finally {
            if ( lock.isHeldByCurrentThread() ) {
                lock.unlock();
            }
        }
        return result;
    }
    public Future<E> pollAsync() {
        int idx = getNextPoll();
        Future<E> result = null;
        final ReentrantLock lock = this.locks[idx];
        try {
            lock.lock();
            E item = items[idx].poll();
            if ( item == null ) {
                ExchangeCountDownLatch<E> c = new ExchangeCountDownLatch<> ( 1 );
                waiters[idx].addLast ( c );
                result = new ItemFuture<> ( c );
            } else {
                result = new ItemFuture<> ( item );
            }
        } finally {
            lock.unlock();
        }
        return result;
    }
    @Override
    public boolean remove ( Object e ) {
        for ( int idx = 0; idx < LOCK_COUNT; idx++ ) {
            final ReentrantLock lock = this.locks[idx];
            lock.lock();
            try {
                boolean result = items[idx].remove ( e );
                if ( result ) {
                    return result;
                }
            } finally {
                lock.unlock();
            }
        }
        return false;
    }
    @Override
    public int size() {
        int size = 0;
        for ( int idx = 0; idx < LOCK_COUNT; idx++ ) {
            size += items[idx].size();
        }
        return size;
    }
    @Override
    public Iterator<E> iterator() {
        return new FairIterator();
    }
    @Override
    public E poll() {
        int idx = getNextPoll();
        final ReentrantLock lock = this.locks[idx];
        lock.lock();
        try {
            return items[idx].poll();
        } finally {
            lock.unlock();
        }
    }
    @Override
    public boolean contains ( Object e ) {
        for ( int idx = 0; idx < LOCK_COUNT; idx++ ) {
            boolean result = items[idx].contains ( e );
            if ( result ) {
                return result;
            }
        }
        return false;
    }
    @Override
    public boolean add ( E e ) {
        return offer ( e );
    }
    @Override
    public int drainTo ( Collection<? super E> c, int maxElements ) {
        throw new UnsupportedOperationException ( "int drainTo(Collection<? super E> c, int maxElements)" );
    }
    @Override
    public int drainTo ( Collection<? super E> c ) {
        return drainTo ( c, Integer.MAX_VALUE );
    }
    @Override
    public void put ( E e ) throws InterruptedException {
        offer ( e );
    }
    @Override
    public int remainingCapacity() {
        return Integer.MAX_VALUE - size();
    }
    @Override
    public E take() throws InterruptedException {
        return this.poll ( Long.MAX_VALUE, TimeUnit.MILLISECONDS );
    }
    @Override
    public boolean addAll ( Collection<? extends E> c ) {
        Iterator<? extends E> i = c.iterator();
        while ( i.hasNext() ) {
            E e = i.next();
            offer ( e );
        }
        return true;
    }
    @Override
    public void clear() {
        throw new UnsupportedOperationException ( "void clear()" );
    }
    @Override
    public boolean containsAll ( Collection<?> c ) {
        throw new UnsupportedOperationException ( "boolean containsAll(Collection<?> c)" );
    }
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    @Override
    public boolean removeAll ( Collection<?> c ) {
        throw new UnsupportedOperationException ( "boolean removeAll(Collection<?> c)" );
    }
    @Override
    public boolean retainAll ( Collection<?> c ) {
        throw new UnsupportedOperationException ( "boolean retainAll(Collection<?> c)" );
    }
    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException ( "Object[] toArray()" );
    }
    @Override
    public <T> T[] toArray ( T[] a ) {
        throw new UnsupportedOperationException ( "<T> T[] toArray(T[] a)" );
    }
    @Override
    public E element() {
        throw new UnsupportedOperationException ( "E element()" );
    }
    @Override
    public E peek() {
        throw new UnsupportedOperationException ( "E peek()" );
    }
    @Override
    public E remove() {
        throw new UnsupportedOperationException ( "E remove()" );
    }
    protected class ItemFuture<T> implements Future<T> {
        protected volatile T item = null;
        protected volatile ExchangeCountDownLatch<T> latch = null;
        protected volatile boolean canceled = false;
        public ItemFuture ( T item ) {
            this.item = item;
        }
        public ItemFuture ( ExchangeCountDownLatch<T> latch ) {
            this.latch = latch;
        }
        @Override
        public boolean cancel ( boolean mayInterruptIfRunning ) {
            return false;
        }
        @Override
        public T get() throws InterruptedException, ExecutionException {
            if ( item != null ) {
                return item;
            } else if ( latch != null ) {
                latch.await();
                return latch.getItem();
            } else {
                throw new ExecutionException ( "ItemFuture incorrectly instantiated. Bug in the code?", new Exception() );
            }
        }
        @Override
        public T get ( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
            if ( item != null ) {
                return item;
            } else if ( latch != null ) {
                boolean timedout = !latch.await ( timeout, unit );
                if ( timedout ) {
                    throw new TimeoutException();
                } else {
                    return latch.getItem();
                }
            } else {
                throw new ExecutionException ( "ItemFuture incorrectly instantiated. Bug in the code?", new Exception() );
            }
        }
        @Override
        public boolean isCancelled() {
            return false;
        }
        @Override
        public boolean isDone() {
            return ( item != null || latch.getItem() != null );
        }
    }
    protected class ExchangeCountDownLatch<T> extends CountDownLatch {
        protected volatile T item;
        public ExchangeCountDownLatch ( int i ) {
            super ( i );
        }
        public T getItem() {
            return item;
        }
        public void setItem ( T item ) {
            this.item = item;
        }
    }
    protected class FairIterator implements Iterator<E> {
        E[] elements = null;
        int index;
        E element = null;
        @SuppressWarnings ( "unchecked" )
        public FairIterator() {
            ArrayList<E> list = new ArrayList<> ( MultiLockFairBlockingQueue.this.size() );
            for ( int idx = 0; idx < LOCK_COUNT; idx++ ) {
                final ReentrantLock lock = MultiLockFairBlockingQueue.this.locks[idx];
                lock.lock();
                try {
                    elements = ( E[] ) new Object[MultiLockFairBlockingQueue.this.items[idx].size()];
                    MultiLockFairBlockingQueue.this.items[idx].toArray ( elements );
                } finally {
                    lock.unlock();
                }
            }
            index = 0;
            elements = ( E[] ) new Object[list.size()];
            list.toArray ( elements );
        }
        @Override
        public boolean hasNext() {
            return index < elements.length;
        }
        @Override
        public E next() {
            if ( !hasNext() ) {
                throw new NoSuchElementException();
            }
            element = elements[index++];
            return element;
        }
        @Override
        public void remove() {
            for ( int idx = 0; idx < LOCK_COUNT; idx++ ) {
                final ReentrantLock lock = MultiLockFairBlockingQueue.this.locks[idx];
                lock.lock();
                try {
                    boolean result = MultiLockFairBlockingQueue.this.items[idx].remove ( elements[index] );
                    if ( result ) {
                        break;
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
