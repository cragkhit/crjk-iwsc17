package org.apache.el.lang;
private static class SynchronizedStack<T> {
    public static final int DEFAULT_SIZE = 128;
    private static final int DEFAULT_LIMIT = -1;
    private int size;
    private final int limit;
    private int index;
    private Object[] stack;
    public SynchronizedStack() {
        this ( 128, -1 );
    }
    public SynchronizedStack ( final int size, final int limit ) {
        this.index = -1;
        this.size = size;
        this.limit = limit;
        this.stack = new Object[size];
    }
    public synchronized boolean push ( final T obj ) {
        ++this.index;
        if ( this.index == this.size ) {
            if ( this.limit != -1 && this.size >= this.limit ) {
                --this.index;
                return false;
            }
            this.expand();
        }
        this.stack[this.index] = obj;
        return true;
    }
    public synchronized T pop() {
        if ( this.index == -1 ) {
            return null;
        }
        final T result = ( T ) this.stack[this.index];
        this.stack[this.index--] = null;
        return result;
    }
    private void expand() {
        int newSize = this.size * 2;
        if ( this.limit != -1 && newSize > this.limit ) {
            newSize = this.limit;
        }
        final Object[] newStack = new Object[newSize];
        System.arraycopy ( this.stack, 0, newStack, 0, this.size );
        this.stack = newStack;
        this.size = newSize;
    }
}
