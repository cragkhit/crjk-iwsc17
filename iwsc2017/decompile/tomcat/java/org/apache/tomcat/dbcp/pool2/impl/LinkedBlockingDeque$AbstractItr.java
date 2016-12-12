package org.apache.tomcat.dbcp.pool2.impl;
import java.util.NoSuchElementException;
import java.util.Iterator;
private abstract class AbstractItr implements Iterator<E> {
    Node<E> next;
    E nextItem;
    private Node<E> lastRet;
    abstract Node<E> firstNode();
    abstract Node<E> nextNode ( final Node<E> p0 );
    AbstractItr() {
        LinkedBlockingDeque.access$200 ( LinkedBlockingDeque.this ).lock();
        try {
            this.next = this.firstNode();
            this.nextItem = ( ( this.next == null ) ? null : this.next.item );
        } finally {
            LinkedBlockingDeque.access$200 ( LinkedBlockingDeque.this ).unlock();
        }
    }
    private Node<E> succ ( Node<E> n ) {
        while ( true ) {
            final Node<E> s = this.nextNode ( n );
            if ( s == null ) {
                return null;
            }
            if ( s.item != null ) {
                return s;
            }
            if ( s == n ) {
                return this.firstNode();
            }
            n = s;
        }
    }
    void advance() {
        LinkedBlockingDeque.access$200 ( LinkedBlockingDeque.this ).lock();
        try {
            this.next = this.succ ( this.next );
            this.nextItem = ( ( this.next == null ) ? null : this.next.item );
        } finally {
            LinkedBlockingDeque.access$200 ( LinkedBlockingDeque.this ).unlock();
        }
    }
    @Override
    public boolean hasNext() {
        return this.next != null;
    }
    @Override
    public E next() {
        if ( this.next == null ) {
            throw new NoSuchElementException();
        }
        this.lastRet = this.next;
        final E x = this.nextItem;
        this.advance();
        return x;
    }
    @Override
    public void remove() {
        final Node<E> n = this.lastRet;
        if ( n == null ) {
            throw new IllegalStateException();
        }
        this.lastRet = null;
        LinkedBlockingDeque.access$200 ( LinkedBlockingDeque.this ).lock();
        try {
            if ( n.item != null ) {
                LinkedBlockingDeque.access$300 ( LinkedBlockingDeque.this, n );
            }
        } finally {
            LinkedBlockingDeque.access$200 ( LinkedBlockingDeque.this ).unlock();
        }
    }
}
