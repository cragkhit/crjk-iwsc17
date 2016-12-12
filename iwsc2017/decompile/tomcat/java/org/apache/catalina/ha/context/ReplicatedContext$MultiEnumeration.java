package org.apache.catalina.ha.context;
import java.util.Enumeration;
protected static class MultiEnumeration<T> implements Enumeration<T> {
    private final Enumeration<T>[] e;
    public MultiEnumeration ( final Enumeration<T>[] lists ) {
        this.e = lists;
    }
    @Override
    public boolean hasMoreElements() {
        for ( int i = 0; i < this.e.length; ++i ) {
            if ( this.e[i].hasMoreElements() ) {
                return true;
            }
        }
        return false;
    }
    @Override
    public T nextElement() {
        for ( int i = 0; i < this.e.length; ++i ) {
            if ( this.e[i].hasMoreElements() ) {
                return this.e[i].nextElement();
            }
        }
        return null;
    }
}
