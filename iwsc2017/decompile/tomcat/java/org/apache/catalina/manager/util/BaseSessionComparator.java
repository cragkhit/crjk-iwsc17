package org.apache.catalina.manager.util;
import org.apache.catalina.Session;
import java.util.Comparator;
public abstract class BaseSessionComparator<T> implements Comparator<Session> {
    public abstract Comparable<T> getComparableObject ( final Session p0 );
    @Override
    public final int compare ( final Session s1, final Session s2 ) {
        final Comparable<T> c1 = this.getComparableObject ( s1 );
        final Comparable<T> c2 = this.getComparableObject ( s2 );
        return ( c1 == null ) ? ( ( c2 == null ) ? 0 : -1 ) : ( ( c2 == null ) ? 1 : c1.compareTo ( ( T ) c2 ) );
    }
}
