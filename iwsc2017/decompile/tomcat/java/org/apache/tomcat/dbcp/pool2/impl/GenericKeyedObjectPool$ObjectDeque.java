package org.apache.tomcat.dbcp.pool2.impl;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.tomcat.dbcp.pool2.PooledObject;
private class ObjectDeque<S> {
    private final LinkedBlockingDeque<PooledObject<S>> idleObjects;
    private final AtomicInteger createCount;
    private long makeObjectCount;
    private final Object makeObjectCountLock;
    private final Map<IdentityWrapper<S>, PooledObject<S>> allObjects;
    private final AtomicLong numInterested;
    public ObjectDeque ( final boolean fairness ) {
        this.createCount = new AtomicInteger ( 0 );
        this.makeObjectCount = 0L;
        this.makeObjectCountLock = new Object();
        this.allObjects = new ConcurrentHashMap<IdentityWrapper<S>, PooledObject<S>>();
        this.numInterested = new AtomicLong ( 0L );
        this.idleObjects = new LinkedBlockingDeque<PooledObject<S>> ( fairness );
    }
    public LinkedBlockingDeque<PooledObject<S>> getIdleObjects() {
        return this.idleObjects;
    }
    public AtomicInteger getCreateCount() {
        return this.createCount;
    }
    public AtomicLong getNumInterested() {
        return this.numInterested;
    }
    public Map<IdentityWrapper<S>, PooledObject<S>> getAllObjects() {
        return this.allObjects;
    }
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append ( "ObjectDeque [idleObjects=" );
        builder.append ( this.idleObjects );
        builder.append ( ", createCount=" );
        builder.append ( this.createCount );
        builder.append ( ", allObjects=" );
        builder.append ( this.allObjects );
        builder.append ( ", numInterested=" );
        builder.append ( this.numInterested );
        builder.append ( "]" );
        return builder.toString();
    }
}
