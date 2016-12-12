package org.apache.tomcat.dbcp.pool2.impl;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.tomcat.dbcp.pool2.BaseObjectPool;
import org.apache.tomcat.dbcp.pool2.PoolUtils;
import org.apache.tomcat.dbcp.pool2.PooledObjectFactory;
public class SoftReferenceObjectPool<T> extends BaseObjectPool<T> {
    private final PooledObjectFactory<T> factory;
    private final ReferenceQueue<T> refQueue = new ReferenceQueue<>();
    private int numActive = 0;
    private long destroyCount = 0;
    private long createCount = 0;
    private final LinkedBlockingDeque<PooledSoftReference<T>> idleReferences =
        new LinkedBlockingDeque<>();
    private final ArrayList<PooledSoftReference<T>> allReferences =
        new ArrayList<>();
    public SoftReferenceObjectPool ( final PooledObjectFactory<T> factory ) {
        this.factory = factory;
    }
    @SuppressWarnings ( "null" )
    @Override
    public synchronized T borrowObject() throws Exception {
        assertOpen();
        T obj = null;
        boolean newlyCreated = false;
        PooledSoftReference<T> ref = null;
        while ( null == obj ) {
            if ( idleReferences.isEmpty() ) {
                if ( null == factory ) {
                    throw new NoSuchElementException();
                }
                newlyCreated = true;
                obj = factory.makeObject().getObject();
                createCount++;
                ref = new PooledSoftReference<> ( new SoftReference<> ( obj ) );
                allReferences.add ( ref );
            } else {
                ref = idleReferences.pollFirst();
                obj = ref.getObject();
                ref.getReference().clear();
                ref.setReference ( new SoftReference<> ( obj ) );
            }
            if ( null != factory && null != obj ) {
                try {
                    factory.activateObject ( ref );
                    if ( !factory.validateObject ( ref ) ) {
                        throw new Exception ( "ValidateObject failed" );
                    }
                } catch ( final Throwable t ) {
                    PoolUtils.checkRethrow ( t );
                    try {
                        destroy ( ref );
                    } catch ( final Throwable t2 ) {
                        PoolUtils.checkRethrow ( t2 );
                    } finally {
                        obj = null;
                    }
                    if ( newlyCreated ) {
                        throw new NoSuchElementException (
                            "Could not create a validated object, cause: " +
                            t.getMessage() );
                    }
                }
            }
        }
        numActive++;
        ref.allocate();
        return obj;
    }
    @Override
    public synchronized void returnObject ( final T obj ) throws Exception {
        boolean success = !isClosed();
        final PooledSoftReference<T> ref = findReference ( obj );
        if ( ref == null ) {
            throw new IllegalStateException (
                "Returned object not currently part of this pool" );
        }
        if ( factory != null ) {
            if ( !factory.validateObject ( ref ) ) {
                success = false;
            } else {
                try {
                    factory.passivateObject ( ref );
                } catch ( final Exception e ) {
                    success = false;
                }
            }
        }
        final boolean shouldDestroy = !success;
        numActive--;
        if ( success ) {
            ref.deallocate();
            idleReferences.add ( ref );
        }
        notifyAll();
        if ( shouldDestroy && factory != null ) {
            try {
                destroy ( ref );
            } catch ( final Exception e ) {
            }
        }
    }
    @Override
    public synchronized void invalidateObject ( final T obj ) throws Exception {
        final PooledSoftReference<T> ref = findReference ( obj );
        if ( ref == null ) {
            throw new IllegalStateException (
                "Object to invalidate is not currently part of this pool" );
        }
        if ( factory != null ) {
            destroy ( ref );
        }
        numActive--;
        notifyAll();
    }
    @Override
    public synchronized void addObject() throws Exception {
        assertOpen();
        if ( factory == null ) {
            throw new IllegalStateException (
                "Cannot add objects without a factory." );
        }
        final T obj = factory.makeObject().getObject();
        createCount++;
        final PooledSoftReference<T> ref = new PooledSoftReference<> (
            new SoftReference<> ( obj, refQueue ) );
        allReferences.add ( ref );
        boolean success = true;
        if ( !factory.validateObject ( ref ) ) {
            success = false;
        } else {
            factory.passivateObject ( ref );
        }
        final boolean shouldDestroy = !success;
        if ( success ) {
            idleReferences.add ( ref );
            notifyAll();
        }
        if ( shouldDestroy ) {
            try {
                destroy ( ref );
            } catch ( final Exception e ) {
            }
        }
    }
    @Override
    public synchronized int getNumIdle() {
        pruneClearedReferences();
        return idleReferences.size();
    }
    @Override
    public synchronized int getNumActive() {
        return numActive;
    }
    @Override
    public synchronized void clear() {
        if ( null != factory ) {
            final Iterator<PooledSoftReference<T>> iter = idleReferences.iterator();
            while ( iter.hasNext() ) {
                try {
                    final PooledSoftReference<T> ref = iter.next();
                    if ( null != ref.getObject() ) {
                        factory.destroyObject ( ref );
                    }
                } catch ( final Exception e ) {
                }
            }
        }
        idleReferences.clear();
        pruneClearedReferences();
    }
    @Override
    public void close() {
        super.close();
        clear();
    }
    public synchronized PooledObjectFactory<T> getFactory() {
        return factory;
    }
    private void pruneClearedReferences() {
        removeClearedReferences ( idleReferences.iterator() );
        removeClearedReferences ( allReferences.iterator() );
        while ( refQueue.poll() != null ) {}
    }
    private PooledSoftReference<T> findReference ( final T obj ) {
        final Iterator<PooledSoftReference<T>> iterator = allReferences.iterator();
        while ( iterator.hasNext() ) {
            final PooledSoftReference<T> reference = iterator.next();
            if ( reference.getObject() != null && reference.getObject().equals ( obj ) ) {
                return reference;
            }
        }
        return null;
    }
    private void destroy ( final PooledSoftReference<T> toDestroy ) throws Exception {
        toDestroy.invalidate();
        idleReferences.remove ( toDestroy );
        allReferences.remove ( toDestroy );
        try {
            factory.destroyObject ( toDestroy );
        } finally {
            destroyCount++;
            toDestroy.getReference().clear();
        }
    }
    private void removeClearedReferences ( final Iterator<PooledSoftReference<T>> iterator ) {
        PooledSoftReference<T> ref;
        while ( iterator.hasNext() ) {
            ref = iterator.next();
            if ( ref.getReference() == null || ref.getReference().isEnqueued() ) {
                iterator.remove();
            }
        }
    }
    @Override
    protected void toStringAppendFields ( final StringBuilder builder ) {
        super.toStringAppendFields ( builder );
        builder.append ( ", factory=" );
        builder.append ( factory );
        builder.append ( ", refQueue=" );
        builder.append ( refQueue );
        builder.append ( ", numActive=" );
        builder.append ( numActive );
        builder.append ( ", destroyCount=" );
        builder.append ( destroyCount );
        builder.append ( ", createCount=" );
        builder.append ( createCount );
        builder.append ( ", idleReferences=" );
        builder.append ( idleReferences );
        builder.append ( ", allReferences=" );
        builder.append ( allReferences );
    }
}
