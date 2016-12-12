package org.apache.tomcat.jdbc.pool;
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
import java.util.concurrent.locks.ReentrantLock;
public class FairBlockingQueue<E> implements BlockingQueue<E> {
    static final boolean isLinux = "Linux".equals ( System.getProperty ( "os.name" ) ) &&
                                   ( !Boolean.getBoolean ( FairBlockingQueue.class.getName() + ".ignoreOS" ) );
    final ReentrantLock lock = new ReentrantLock ( false );
    final LinkedList<E> items;
    final LinkedList<ExchangeCountDownLatch<E>> waiters;
    public FairBlockingQueue() {
        items = new LinkedList<>();
        waiters = new LinkedList<>();
    }
    @Override
    public boolean offer ( E e ) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        ExchangeCountDownLatch<E> c = null;
        try {
            if ( waiters.size() > 0 ) {
                c = waiters.poll();
                c.setItem ( e );
                if ( isLinux ) {
                    c.countDown();
                }
            } else {
                items.addFirst ( e );
            }
        } finally {
            lock.unlock();
        }
        if ( !isLinux && c != null ) {
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
        E result = null;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            result = items.poll();
            if ( result == null && timeout > 0 ) {
                ExchangeCountDownLatch<E> c = new ExchangeCountDownLatch<> ( 1 );
                waiters.addLast ( c );
                lock.unlock();
                boolean didtimeout = true;
                InterruptedException interruptedException = null;
                try {
                    didtimeout = !c.await ( timeout, unit );
                } catch ( InterruptedException ix ) {
                    interruptedException = ix;
                }
                if ( didtimeout ) {
                    lock.lock();
                    try {
                        waiters.remove ( c );
                    } finally {
                        lock.unlock();
                    }
                }
                result = c.getItem();
                if ( null != interruptedException ) {
                    if ( null != result ) {
                        Thread.interrupted();
                    } else {
                        throw interruptedException;
                    }
                }
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
        Future<E> result = null;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            E item = items.poll();
            if ( item == null ) {
                ExchangeCountDownLatch<E> c = new ExchangeCountDownLatch<> ( 1 );
                waiters.addLast ( c );
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
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return items.remove ( e );
        } finally {
            lock.unlock();
        }
    }
    @Override
    public int size() {
        return items.size();
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
            return items.poll();
        } finally {
            lock.unlock();
        }
    }
    @Override
    public boolean contains ( Object e ) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return items.contains ( e );
        } finally {
            lock.unlock();
        }
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
            final ReentrantLock lock = FairBlockingQueue.this.lock;
            lock.lock();
            try {
                elements = ( E[] ) new Object[FairBlockingQueue.this.items.size()];
                FairBlockingQueue.this.items.toArray ( elements );
                index = 0;
            } finally {
                lock.unlock();
            }
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
            final ReentrantLock lock = FairBlockingQueue.this.lock;
            lock.lock();
            try {
                if ( element != null ) {
                    FairBlockingQueue.this.items.remove ( element );
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
