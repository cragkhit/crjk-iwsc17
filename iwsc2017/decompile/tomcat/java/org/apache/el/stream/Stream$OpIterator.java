package org.apache.el.stream;
import java.util.NoSuchElementException;
import java.util.Iterator;
private abstract static class OpIterator implements Iterator<Object> {
    protected boolean foundNext;
    protected Object next;
    private OpIterator() {
        this.foundNext = false;
    }
    @Override
    public boolean hasNext() {
        if ( this.foundNext ) {
            return true;
        }
        this.findNext();
        return this.foundNext;
    }
    @Override
    public Object next() {
        if ( this.foundNext ) {
            this.foundNext = false;
            return this.next;
        }
        this.findNext();
        if ( this.foundNext ) {
            this.foundNext = false;
            return this.next;
        }
        throw new NoSuchElementException();
    }
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
    protected abstract void findNext();
}
