package org.junit.internal;
import java.lang.reflect.Method;
import java.util.Comparator;
static final class MethodSorter$1 implements Comparator<Method> {
    public int compare ( final Method m1, final Method m2 ) {
        final int i1 = m1.getName().hashCode();
        final int i2 = m2.getName().hashCode();
        if ( i1 != i2 ) {
            return ( i1 < i2 ) ? -1 : 1;
        }
        return MethodSorter.NAME_ASCENDING.compare ( m1, m2 );
    }
}
