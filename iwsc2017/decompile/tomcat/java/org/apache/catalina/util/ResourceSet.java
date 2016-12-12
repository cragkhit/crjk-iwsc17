package org.apache.catalina.util;
import java.util.Collection;
import org.apache.tomcat.util.res.StringManager;
import java.util.HashSet;
public final class ResourceSet<T> extends HashSet<T> {
    private static final long serialVersionUID = 1L;
    private boolean locked;
    private static final StringManager sm;
    public ResourceSet() {
        this.locked = false;
    }
    public ResourceSet ( final int initialCapacity ) {
        super ( initialCapacity );
        this.locked = false;
    }
    public ResourceSet ( final int initialCapacity, final float loadFactor ) {
        super ( initialCapacity, loadFactor );
        this.locked = false;
    }
    public ResourceSet ( final Collection<T> coll ) {
        super ( coll );
        this.locked = false;
    }
    public boolean isLocked() {
        return this.locked;
    }
    public void setLocked ( final boolean locked ) {
        this.locked = locked;
    }
    @Override
    public boolean add ( final T o ) {
        if ( this.locked ) {
            throw new IllegalStateException ( ResourceSet.sm.getString ( "resourceSet.locked" ) );
        }
        return super.add ( o );
    }
    @Override
    public void clear() {
        if ( this.locked ) {
            throw new IllegalStateException ( ResourceSet.sm.getString ( "resourceSet.locked" ) );
        }
        super.clear();
    }
    @Override
    public boolean remove ( final Object o ) {
        if ( this.locked ) {
            throw new IllegalStateException ( ResourceSet.sm.getString ( "resourceSet.locked" ) );
        }
        return super.remove ( o );
    }
    static {
        sm = StringManager.getManager ( "org.apache.catalina.util" );
    }
}
