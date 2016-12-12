package org.apache.tomcat.dbcp.dbcp2;
import java.util.Iterator;
import java.util.Collections;
import java.util.ArrayList;
import java.lang.ref.WeakReference;
import java.util.List;
import org.apache.tomcat.dbcp.pool2.TrackedUse;
public class AbandonedTrace implements TrackedUse {
    private final List<WeakReference<AbandonedTrace>> traceList;
    private volatile long lastUsed;
    public AbandonedTrace() {
        this.traceList = new ArrayList<WeakReference<AbandonedTrace>>();
        this.lastUsed = 0L;
        this.init ( null );
    }
    public AbandonedTrace ( final AbandonedTrace parent ) {
        this.traceList = new ArrayList<WeakReference<AbandonedTrace>>();
        this.lastUsed = 0L;
        this.init ( parent );
    }
    private void init ( final AbandonedTrace parent ) {
        if ( parent != null ) {
            parent.addTrace ( this );
        }
    }
    @Override
    public long getLastUsed() {
        return this.lastUsed;
    }
    protected void setLastUsed() {
        this.lastUsed = System.currentTimeMillis();
    }
    protected void setLastUsed ( final long time ) {
        this.lastUsed = time;
    }
    protected void addTrace ( final AbandonedTrace trace ) {
        synchronized ( this.traceList ) {
            this.traceList.add ( new WeakReference<AbandonedTrace> ( trace ) );
        }
        this.setLastUsed();
    }
    protected void clearTrace() {
        synchronized ( this.traceList ) {
            this.traceList.clear();
        }
    }
    protected List<AbandonedTrace> getTrace() {
        final int size = this.traceList.size();
        if ( size == 0 ) {
            return Collections.emptyList();
        }
        final ArrayList<AbandonedTrace> result = new ArrayList<AbandonedTrace> ( size );
        synchronized ( this.traceList ) {
            final Iterator<WeakReference<AbandonedTrace>> iter = this.traceList.iterator();
            while ( iter.hasNext() ) {
                final WeakReference<AbandonedTrace> ref = iter.next();
                if ( ref.get() == null ) {
                    iter.remove();
                } else {
                    result.add ( ref.get() );
                }
            }
        }
        return result;
    }
    protected void removeTrace ( final AbandonedTrace trace ) {
        synchronized ( this.traceList ) {
            final Iterator<WeakReference<AbandonedTrace>> iter = this.traceList.iterator();
            while ( iter.hasNext() ) {
                final WeakReference<AbandonedTrace> ref = iter.next();
                if ( trace.equals ( ref.get() ) ) {
                    iter.remove();
                    break;
                }
                if ( ref.get() != null ) {
                    continue;
                }
                iter.remove();
            }
        }
    }
}
