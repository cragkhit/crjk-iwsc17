package org.apache.tomcat.dbcp.pool2.impl;
private class Itr extends AbstractItr {
    @Override
    Node<E> firstNode() {
        return ( Node<E> ) LinkedBlockingDeque.access$400 ( LinkedBlockingDeque.this );
    }
    @Override
    Node<E> nextNode ( final Node<E> n ) {
        return n.next;
    }
}
