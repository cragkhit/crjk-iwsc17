package org.apache.tomcat.dbcp.dbcp2;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.tomcat.dbcp.pool2.TrackedUse;
public class AbandonedTrace implements TrackedUse {
    private final List<WeakReference<AbandonedTrace>> traceList = new ArrayList<>();
    private volatile long lastUsed = 0;
    public AbandonedTrace() {
        init ( null );
    }
    public AbandonedTrace ( final AbandonedTrace parent ) {
        init ( parent );
    }
    private void init ( final AbandonedTrace parent ) {
        if ( parent != null ) {
            parent.addTrace ( this );
        }
    }
    @Override
    public long getLastUsed() {
        return lastUsed;
    }
    protected void setLastUsed() {
        lastUsed = System.currentTimeMillis();
    }
    protected void setLastUsed ( final long time ) {
        lastUsed = time;
    }
    protected void addTrace ( final AbandonedTrace trace ) {
        synchronized ( this.traceList ) {
            this.traceList.add ( new WeakReference<> ( trace ) );
        }
        setLastUsed();
    }
    protected void clearTrace() {
        synchronized ( this.traceList ) {
            this.traceList.clear();
        }
    }
    protected List<AbandonedTrace> getTrace() {
        final int size = traceList.size();
        if ( size == 0 ) {
            return Collections.emptyList();
        }
        final ArrayList<AbandonedTrace> result = new ArrayList<> ( size );
        synchronized ( this.traceList ) {
            final Iterator<WeakReference<AbandonedTrace>> iter = traceList.iterator();
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
            final Iterator<WeakReference<AbandonedTrace>> iter = traceList.iterator();
            while ( iter.hasNext() ) {
                final WeakReference<AbandonedTrace> ref = iter.next();
                if ( trace.equals ( ref.get() ) ) {
                    iter.remove();
                    break;
                } else if ( ref.get() == null ) {
                    iter.remove();
                }
            }
        }
    }
}
