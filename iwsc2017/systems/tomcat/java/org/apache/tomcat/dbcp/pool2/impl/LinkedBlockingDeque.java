package org.apache.tomcat.dbcp.pool2.impl;
import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
class LinkedBlockingDeque<E> extends AbstractQueue<E>
    implements Deque<E>, Serializable {
    private static final long serialVersionUID = -387911632671998426L;
    private static final class Node<E> {
        E item;
        Node<E> prev;
        Node<E> next;
        Node ( final E x, final Node<E> p, final Node<E> n ) {
            item = x;
            prev = p;
            next = n;
        }
    }
    private transient Node<E> first;
    private transient Node<E> last;
    private transient int count;
    private final int capacity;
    private final InterruptibleReentrantLock lock;
    private final Condition notEmpty;
    private final Condition notFull;
    public LinkedBlockingDeque() {
        this ( Integer.MAX_VALUE );
    }
    public LinkedBlockingDeque ( final boolean fairness ) {
        this ( Integer.MAX_VALUE, fairness );
    }
    public LinkedBlockingDeque ( final int capacity ) {
        this ( capacity, false );
    }
    public LinkedBlockingDeque ( final int capacity, final boolean fairness ) {
        if ( capacity <= 0 ) {
            throw new IllegalArgumentException();
        }
        this.capacity = capacity;
        lock = new InterruptibleReentrantLock ( fairness );
        notEmpty = lock.newCondition();
        notFull = lock.newCondition();
    }
    public LinkedBlockingDeque ( final Collection<? extends E> c ) {
        this ( Integer.MAX_VALUE );
        lock.lock();
        try {
            for ( final E e : c ) {
                if ( e == null ) {
                    throw new NullPointerException();
                }
                if ( !linkLast ( e ) ) {
                    throw new IllegalStateException ( "Deque full" );
                }
            }
        } finally {
            lock.unlock();
        }
    }
    private boolean linkFirst ( final E e ) {
        if ( count >= capacity ) {
            return false;
        }
        final Node<E> f = first;
        final Node<E> x = new Node<> ( e, null, f );
        first = x;
        if ( last == null ) {
            last = x;
        } else {
            f.prev = x;
        }
        ++count;
        notEmpty.signal();
        return true;
    }
    private boolean linkLast ( final E e ) {
        if ( count >= capacity ) {
            return false;
        }
        final Node<E> l = last;
        final Node<E> x = new Node<> ( e, l, null );
        last = x;
        if ( first == null ) {
            first = x;
        } else {
            l.next = x;
        }
        ++count;
        notEmpty.signal();
        return true;
    }
    private E unlinkFirst() {
        final Node<E> f = first;
        if ( f == null ) {
            return null;
        }
        final Node<E> n = f.next;
        final E item = f.item;
        f.item = null;
        f.next = f;
        first = n;
        if ( n == null ) {
            last = null;
        } else {
            n.prev = null;
        }
        --count;
        notFull.signal();
        return item;
    }
    private E unlinkLast() {
        final Node<E> l = last;
        if ( l == null ) {
            return null;
        }
        final Node<E> p = l.prev;
        final E item = l.item;
        l.item = null;
        l.prev = l;
        last = p;
        if ( p == null ) {
            first = null;
        } else {
            p.next = null;
        }
        --count;
        notFull.signal();
        return item;
    }
    private void unlink ( final Node<E> x ) {
        final Node<E> p = x.prev;
        final Node<E> n = x.next;
        if ( p == null ) {
            unlinkFirst();
        } else if ( n == null ) {
            unlinkLast();
        } else {
            p.next = n;
            n.prev = p;
            x.item = null;
            --count;
            notFull.signal();
        }
    }
    @Override
    public void addFirst ( final E e ) {
        if ( !offerFirst ( e ) ) {
            throw new IllegalStateException ( "Deque full" );
        }
    }
    @Override
    public void addLast ( final E e ) {
        if ( !offerLast ( e ) ) {
            throw new IllegalStateException ( "Deque full" );
        }
    }
    @Override
    public boolean offerFirst ( final E e ) {
        if ( e == null ) {
            throw new NullPointerException();
        }
        lock.lock();
        try {
            return linkFirst ( e );
        } finally {
            lock.unlock();
        }
    }
    @Override
    public boolean offerLast ( final E e ) {
        if ( e == null ) {
            throw new NullPointerException();
        }
        lock.lock();
        try {
            return linkLast ( e );
        } finally {
            lock.unlock();
        }
    }
    public void putFirst ( final E e ) throws InterruptedException {
        if ( e == null ) {
            throw new NullPointerException();
        }
        lock.lock();
        try {
            while ( !linkFirst ( e ) ) {
                notFull.await();
            }
        } finally {
            lock.unlock();
        }
    }
    public void putLast ( final E e ) throws InterruptedException {
        if ( e == null ) {
            throw new NullPointerException();
        }
        lock.lock();
        try {
            while ( !linkLast ( e ) ) {
                notFull.await();
            }
        } finally {
            lock.unlock();
        }
    }
    public boolean offerFirst ( final E e, final long timeout, final TimeUnit unit )
    throws InterruptedException {
        if ( e == null ) {
            throw new NullPointerException();
        }
        long nanos = unit.toNanos ( timeout );
        lock.lockInterruptibly();
        try {
            while ( !linkFirst ( e ) ) {
                if ( nanos <= 0 ) {
                    return false;
                }
                nanos = notFull.awaitNanos ( nanos );
            }
            return true;
        } finally {
            lock.unlock();
        }
    }
    public boolean offerLast ( final E e, final long timeout, final TimeUnit unit )
    throws InterruptedException {
        if ( e == null ) {
            throw new NullPointerException();
        }
        long nanos = unit.toNanos ( timeout );
        lock.lockInterruptibly();
        try {
            while ( !linkLast ( e ) ) {
                if ( nanos <= 0 ) {
                    return false;
                }
                nanos = notFull.awaitNanos ( nanos );
            }
            return true;
        } finally {
            lock.unlock();
        }
    }
    @Override
    public E removeFirst() {
        final E x = pollFirst();
        if ( x == null ) {
            throw new NoSuchElementException();
        }
        return x;
    }
    @Override
    public E removeLast() {
        final E x = pollLast();
        if ( x == null ) {
            throw new NoSuchElementException();
        }
        return x;
    }
    @Override
    public E pollFirst() {
        lock.lock();
        try {
            return unlinkFirst();
        } finally {
            lock.unlock();
        }
    }
    @Override
    public E pollLast() {
        lock.lock();
        try {
            return unlinkLast();
        } finally {
            lock.unlock();
        }
    }
    public E takeFirst() throws InterruptedException {
        lock.lock();
        try {
            E x;
            while ( ( x = unlinkFirst() ) == null ) {
                notEmpty.await();
            }
            return x;
        } finally {
            lock.unlock();
        }
    }
    public E takeLast() throws InterruptedException {
        lock.lock();
        try {
            E x;
            while ( ( x = unlinkLast() ) == null ) {
                notEmpty.await();
            }
            return x;
        } finally {
            lock.unlock();
        }
    }
    public E pollFirst ( final long timeout, final TimeUnit unit )
    throws InterruptedException {
        long nanos = unit.toNanos ( timeout );
        lock.lockInterruptibly();
        try {
            E x;
            while ( ( x = unlinkFirst() ) == null ) {
                if ( nanos <= 0 ) {
                    return null;
                }
                nanos = notEmpty.awaitNanos ( nanos );
            }
            return x;
        } finally {
            lock.unlock();
        }
    }
    public E pollLast ( final long timeout, final TimeUnit unit )
    throws InterruptedException {
        long nanos = unit.toNanos ( timeout );
        lock.lockInterruptibly();
        try {
            E x;
            while ( ( x = unlinkLast() ) == null ) {
                if ( nanos <= 0 ) {
                    return null;
                }
                nanos = notEmpty.awaitNanos ( nanos );
            }
            return x;
        } finally {
            lock.unlock();
        }
    }
    @Override
    public E getFirst() {
        final E x = peekFirst();
        if ( x == null ) {
            throw new NoSuchElementException();
        }
        return x;
    }
    @Override
    public E getLast() {
        final E x = peekLast();
        if ( x == null ) {
            throw new NoSuchElementException();
        }
        return x;
    }
    @Override
    public E peekFirst() {
        lock.lock();
        try {
            return first == null ? null : first.item;
        } finally {
            lock.unlock();
        }
    }
    @Override
    public E peekLast() {
        lock.lock();
        try {
            return last == null ? null : last.item;
        } finally {
            lock.unlock();
        }
    }
    @Override
    public boolean removeFirstOccurrence ( final Object o ) {
        if ( o == null ) {
            return false;
        }
        lock.lock();
        try {
            for ( Node<E> p = first; p != null; p = p.next ) {
                if ( o.equals ( p.item ) ) {
                    unlink ( p );
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
    @Override
    public boolean removeLastOccurrence ( final Object o ) {
        if ( o == null ) {
            return false;
        }
        lock.lock();
        try {
            for ( Node<E> p = last; p != null; p = p.prev ) {
                if ( o.equals ( p.item ) ) {
                    unlink ( p );
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
    @Override
    public boolean add ( final E e ) {
        addLast ( e );
        return true;
    }
    @Override
    public boolean offer ( final E e ) {
        return offerLast ( e );
    }
    public void put ( final E e ) throws InterruptedException {
        putLast ( e );
    }
    public boolean offer ( final E e, final long timeout, final TimeUnit unit )
    throws InterruptedException {
        return offerLast ( e, timeout, unit );
    }
    @Override
    public E remove() {
        return removeFirst();
    }
    @Override
    public E poll() {
        return pollFirst();
    }
    public E take() throws InterruptedException {
        return takeFirst();
    }
    public E poll ( final long timeout, final TimeUnit unit ) throws InterruptedException {
        return pollFirst ( timeout, unit );
    }
    @Override
    public E element() {
        return getFirst();
    }
    @Override
    public E peek() {
        return peekFirst();
    }
    public int remainingCapacity() {
        lock.lock();
        try {
            return capacity - count;
        } finally {
            lock.unlock();
        }
    }
    public int drainTo ( final Collection<? super E> c ) {
        return drainTo ( c, Integer.MAX_VALUE );
    }
    public int drainTo ( final Collection<? super E> c, final int maxElements ) {
        if ( c == null ) {
            throw new NullPointerException();
        }
        if ( c == this ) {
            throw new IllegalArgumentException();
        }
        lock.lock();
        try {
            final int n = Math.min ( maxElements, count );
            for ( int i = 0; i < n; i++ ) {
                c.add ( first.item );
                unlinkFirst();
            }
            return n;
        } finally {
            lock.unlock();
        }
    }
    @Override
    public void push ( final E e ) {
        addFirst ( e );
    }
    @Override
    public E pop() {
        return removeFirst();
    }
    @Override
    public boolean remove ( final Object o ) {
        return removeFirstOccurrence ( o );
    }
    @Override
    public int size() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }
    @Override
    public boolean contains ( final Object o ) {
        if ( o == null ) {
            return false;
        }
        lock.lock();
        try {
            for ( Node<E> p = first; p != null; p = p.next ) {
                if ( o.equals ( p.item ) ) {
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
    @Override
    public Object[] toArray() {
        lock.lock();
        try {
            final Object[] a = new Object[count];
            int k = 0;
            for ( Node<E> p = first; p != null; p = p.next ) {
                a[k++] = p.item;
            }
            return a;
        } finally {
            lock.unlock();
        }
    }
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T> T[] toArray ( T[] a ) {
        lock.lock();
        try {
            if ( a.length < count ) {
                a = ( T[] ) java.lang.reflect.Array.newInstance
                    ( a.getClass().getComponentType(), count );
            }
            int k = 0;
            for ( Node<E> p = first; p != null; p = p.next ) {
                a[k++] = ( T ) p.item;
            }
            if ( a.length > k ) {
                a[k] = null;
            }
            return a;
        } finally {
            lock.unlock();
        }
    }
    @Override
    public String toString() {
        lock.lock();
        try {
            return super.toString();
        } finally {
            lock.unlock();
        }
    }
    @Override
    public void clear() {
        lock.lock();
        try {
            for ( Node<E> f = first; f != null; ) {
                f.item = null;
                final Node<E> n = f.next;
                f.prev = null;
                f.next = null;
                f = n;
            }
            first = last = null;
            count = 0;
            notFull.signalAll();
        } finally {
            lock.unlock();
        }
    }
    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }
    @Override
    public Iterator<E> descendingIterator() {
        return new DescendingItr();
    }
    private abstract class AbstractItr implements Iterator<E> {
        Node<E> next;
        E nextItem;
        private Node<E> lastRet;
        abstract Node<E> firstNode();
        abstract Node<E> nextNode ( Node<E> n );
        AbstractItr() {
            lock.lock();
            try {
                next = firstNode();
                nextItem = next == null ? null : next.item;
            } finally {
                lock.unlock();
            }
        }
        private Node<E> succ ( Node<E> n ) {
            for ( ;; ) {
                final Node<E> s = nextNode ( n );
                if ( s == null ) {
                    return null;
                } else if ( s.item != null ) {
                    return s;
                } else if ( s == n ) {
                    return firstNode();
                } else {
                    n = s;
                }
            }
        }
        void advance() {
            lock.lock();
            try {
                next = succ ( next );
                nextItem = next == null ? null : next.item;
            } finally {
                lock.unlock();
            }
        }
        @Override
        public boolean hasNext() {
            return next != null;
        }
        @Override
        public E next() {
            if ( next == null ) {
                throw new NoSuchElementException();
            }
            lastRet = next;
            final E x = nextItem;
            advance();
            return x;
        }
        @Override
        public void remove() {
            final Node<E> n = lastRet;
            if ( n == null ) {
                throw new IllegalStateException();
            }
            lastRet = null;
            lock.lock();
            try {
                if ( n.item != null ) {
                    unlink ( n );
                }
            } finally {
                lock.unlock();
            }
        }
    }
    private class Itr extends AbstractItr {
        @Override
        Node<E> firstNode() {
            return first;
        }
        @Override
        Node<E> nextNode ( final Node<E> n ) {
            return n.next;
        }
    }
    private class DescendingItr extends AbstractItr {
        @Override
        Node<E> firstNode() {
            return last;
        }
        @Override
        Node<E> nextNode ( final Node<E> n ) {
            return n.prev;
        }
    }
    private void writeObject ( final java.io.ObjectOutputStream s )
    throws java.io.IOException {
        lock.lock();
        try {
            s.defaultWriteObject();
            for ( Node<E> p = first; p != null; p = p.next ) {
                s.writeObject ( p.item );
            }
            s.writeObject ( null );
        } finally {
            lock.unlock();
        }
    }
    private void readObject ( final java.io.ObjectInputStream s )
    throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        count = 0;
        first = null;
        last = null;
        for ( ;; ) {
            @SuppressWarnings ( "unchecked" )
            final
            E item = ( E ) s.readObject();
            if ( item == null ) {
                break;
            }
            add ( item );
        }
    }
    public boolean hasTakeWaiters() {
        lock.lock();
        try {
            return lock.hasWaiters ( notEmpty );
        } finally {
            lock.unlock();
        }
    }
    public int getTakeQueueLength() {
        lock.lock();
        try {
            return lock.getWaitQueueLength ( notEmpty );
        } finally {
            lock.unlock();
        }
    }
    public void interuptTakeWaiters() {
        lock.lock();
        try {
            lock.interruptWaiters ( notEmpty );
        } finally {
            lock.unlock();
        }
    }
}
