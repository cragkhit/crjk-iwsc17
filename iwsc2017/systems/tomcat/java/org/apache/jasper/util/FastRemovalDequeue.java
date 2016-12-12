package org.apache.jasper.util;
public class FastRemovalDequeue<T> {
    private final int maxSize;
    protected Entry first;
    protected Entry last;
    private int size;
    public FastRemovalDequeue ( int maxSize ) {
        if ( maxSize <= 1 ) {
            maxSize = 2;
        }
        this.maxSize = maxSize;
        first = null;
        last = null;
        size = 0;
    }
    public synchronized int getSize() {
        return size;
    }
    public synchronized Entry push ( final T object ) {
        Entry entry = new Entry ( object );
        if ( size >= maxSize ) {
            entry.setReplaced ( pop() );
        }
        if ( first == null ) {
            first = last = entry;
        } else {
            first.setPrevious ( entry );
            entry.setNext ( first );
            first = entry;
        }
        size++;
        return entry;
    }
    public synchronized Entry unpop ( final T object ) {
        Entry entry = new Entry ( object );
        if ( size >= maxSize ) {
            entry.setReplaced ( unpush() );
        }
        if ( first == null ) {
            first = last = entry;
        } else {
            last.setNext ( entry );
            entry.setPrevious ( last );
            last = entry;
        }
        size++;
        return entry;
    }
    public synchronized T unpush() {
        T content = null;
        if ( first != null ) {
            Entry element = first;
            first = first.getNext();
            content = element.getContent();
            if ( first == null ) {
                last = null;
            } else {
                first.setPrevious ( null );
            }
            size--;
            element.invalidate();
        }
        return content;
    }
    public synchronized T pop() {
        T content = null;
        if ( last != null ) {
            Entry element = last;
            last = last.getPrevious();
            content = element.getContent();
            if ( last == null ) {
                first = null;
            } else {
                last.setNext ( null );
            }
            size--;
            element.invalidate();
        }
        return content;
    }
    public synchronized void remove ( final Entry element ) {
        if ( element == null || !element.getValid() ) {
            return;
        }
        Entry next = element.getNext();
        Entry prev = element.getPrevious();
        if ( next != null ) {
            next.setPrevious ( prev );
        } else {
            last = prev;
        }
        if ( prev != null ) {
            prev.setNext ( next );
        } else {
            first = next;
        }
        size--;
        element.invalidate();
    }
    public synchronized void moveFirst ( final Entry element ) {
        if ( element.getValid() &&
                element.getPrevious() != null ) {
            Entry prev = element.getPrevious();
            Entry next = element.getNext();
            prev.setNext ( next );
            if ( next != null ) {
                next.setPrevious ( prev );
            } else {
                last = prev;
            }
            first.setPrevious ( element );
            element.setNext ( first );
            element.setPrevious ( null );
            first = element;
        }
    }
    public synchronized void moveLast ( final Entry element ) {
        if ( element.getValid() &&
                element.getNext() != null ) {
            Entry next = element.getNext();
            Entry prev = element.getPrevious();
            next.setPrevious ( prev );
            if ( prev != null ) {
                prev.setNext ( next );
            } else {
                first = next;
            }
            last.setNext ( element );
            element.setPrevious ( last );
            element.setNext ( null );
            last = element;
        }
    }
    public class Entry {
        private boolean valid = true;
        private final T content;
        private T replaced = null;
        private Entry next = null;
        private Entry previous = null;
        private Entry ( T object ) {
            content = object;
        }
        private final boolean getValid() {
            return valid;
        }
        private final void invalidate() {
            this.valid = false;
            this.previous = null;
            this.next = null;
        }
        public final T getContent() {
            return content;
        }
        public final T getReplaced() {
            return replaced;
        }
        private final void setReplaced ( final T replaced ) {
            this.replaced = replaced;
        }
        public final void clearReplaced() {
            this.replaced = null;
        }
        private final Entry getNext() {
            return next;
        }
        private final void setNext ( final Entry next ) {
            this.next = next;
        }
        private final Entry getPrevious() {
            return previous;
        }
        private final void setPrevious ( final Entry previous ) {
            this.previous = previous;
        }
        @Override
        public String toString() {
            return "Entry-" + content.toString();
        }
    }
}
