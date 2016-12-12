package org.apache.el.stream;
import java.lang.reflect.Array;
import java.util.Iterator;
private static class ArrayIterator implements Iterator<Object> {
    private final Object base;
    private final int size;
    private int index;
    public ArrayIterator ( final Object base ) {
        this.index = 0;
        this.base = base;
        this.size = Array.getLength ( base );
    }
    @Override
    public boolean hasNext() {
        return this.size > this.index;
    }
    @Override
    public Object next() {
        return Array.get ( this.base, this.index++ );
    }
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
