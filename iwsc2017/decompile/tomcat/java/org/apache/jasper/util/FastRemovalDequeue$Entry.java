package org.apache.jasper.util;
public class Entry {
    private boolean valid;
    private final T content;
    private T replaced;
    private Entry next;
    private Entry previous;
    private Entry ( final T object ) {
        this.valid = true;
        this.replaced = null;
        this.next = null;
        this.previous = null;
        this.content = object;
    }
    private final boolean getValid() {
        return this.valid;
    }
    private final void invalidate() {
        this.valid = false;
        this.previous = null;
        this.next = null;
    }
    public final T getContent() {
        return this.content;
    }
    public final T getReplaced() {
        return this.replaced;
    }
    private final void setReplaced ( final T replaced ) {
        this.replaced = replaced;
    }
    public final void clearReplaced() {
        this.replaced = null;
    }
    private final Entry getNext() {
        return this.next;
    }
    private final void setNext ( final Entry next ) {
        this.next = next;
    }
    private final Entry getPrevious() {
        return this.previous;
    }
    private final void setPrevious ( final Entry previous ) {
        this.previous = previous;
    }
    @Override
    public String toString() {
        return "Entry-" + this.content.toString();
    }
}
