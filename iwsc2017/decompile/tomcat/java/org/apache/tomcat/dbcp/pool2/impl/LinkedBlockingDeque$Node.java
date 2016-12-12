package org.apache.tomcat.dbcp.pool2.impl;
private static final class Node<E> {
    E item;
    Node<E> prev;
    Node<E> next;
    Node ( final E x, final Node<E> p, final Node<E> n ) {
        this.item = x;
        this.prev = p;
        this.next = n;
    }
}
