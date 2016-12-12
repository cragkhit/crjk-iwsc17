package org.apache.catalina.core;
import java.util.NoSuchElementException;
import java.util.Enumeration;
protected class AttributeNamesEnumerator implements Enumeration<String> {
    protected int pos;
    protected final int last;
    protected final Enumeration<String> parentEnumeration;
    protected String next;
    public AttributeNamesEnumerator() {
        this.pos = -1;
        this.next = null;
        int last = -1;
        this.parentEnumeration = ( Enumeration<String> ) ApplicationHttpRequest.this.getRequest().getAttributeNames();
        for ( int i = ApplicationHttpRequest.this.specialAttributes.length - 1; i >= 0; --i ) {
            if ( ApplicationHttpRequest.this.getAttribute ( ApplicationHttpRequest.specials[i] ) != null ) {
                last = i;
                break;
            }
        }
        this.last = last;
    }
    @Override
    public boolean hasMoreElements() {
        return this.pos != this.last || this.next != null || ( this.next = this.findNext() ) != null;
    }
    @Override
    public String nextElement() {
        if ( this.pos != this.last ) {
            for ( int i = this.pos + 1; i <= this.last; ++i ) {
                if ( ApplicationHttpRequest.this.getAttribute ( ApplicationHttpRequest.specials[i] ) != null ) {
                    this.pos = i;
                    return ApplicationHttpRequest.specials[i];
                }
            }
        }
        final String result = this.next;
        if ( this.next != null ) {
            this.next = this.findNext();
            return result;
        }
        throw new NoSuchElementException();
    }
    protected String findNext() {
        String result;
        String current;
        for ( result = null; result == null && this.parentEnumeration.hasMoreElements(); result = current ) {
            current = this.parentEnumeration.nextElement();
            if ( !ApplicationHttpRequest.this.isSpecial ( current ) ) {}
        }
        return result;
    }
}
