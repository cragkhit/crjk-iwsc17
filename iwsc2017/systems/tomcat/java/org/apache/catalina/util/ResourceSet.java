package org.apache.catalina.util;
import java.util.Collection;
import java.util.HashSet;
import org.apache.tomcat.util.res.StringManager;
public final class ResourceSet<T> extends HashSet<T> {
    private static final long serialVersionUID = 1L;
    public ResourceSet() {
        super();
    }
    public ResourceSet ( int initialCapacity ) {
        super ( initialCapacity );
    }
    public ResourceSet ( int initialCapacity, float loadFactor ) {
        super ( initialCapacity, loadFactor );
    }
    public ResourceSet ( Collection<T> coll ) {
        super ( coll );
    }
    private boolean locked = false;
    public boolean isLocked() {
        return ( this.locked );
    }
    public void setLocked ( boolean locked ) {
        this.locked = locked;
    }
    private static final StringManager sm =
        StringManager.getManager ( "org.apache.catalina.util" );
    @Override
    public boolean add ( T o ) {
        if ( locked )
            throw new IllegalStateException
            ( sm.getString ( "resourceSet.locked" ) );
        return ( super.add ( o ) );
    }
    @Override
    public void clear() {
        if ( locked )
            throw new IllegalStateException
            ( sm.getString ( "resourceSet.locked" ) );
        super.clear();
    }
    @Override
    public boolean remove ( Object o ) {
        if ( locked )
            throw new IllegalStateException
            ( sm.getString ( "resourceSet.locked" ) );
        return ( super.remove ( o ) );
    }
}
