package org.apache.tomcat.dbcp.pool2.impl;
import java.lang.ref.SoftReference;
public class PooledSoftReference<T> extends DefaultPooledObject<T> {
    private volatile SoftReference<T> reference;
    public PooledSoftReference ( final SoftReference<T> reference ) {
        super ( null );
        this.reference = reference;
    }
    @Override
    public T getObject() {
        return reference.get();
    }
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append ( "Referenced Object: " );
        result.append ( getObject().toString() );
        result.append ( ", State: " );
        synchronized ( this ) {
            result.append ( getState().toString() );
        }
        return result.toString();
    }
    public synchronized SoftReference<T> getReference() {
        return reference;
    }
    public synchronized void setReference ( final SoftReference<T> reference ) {
        this.reference = reference;
    }
}
