package org.junit.internal;
import java.lang.reflect.Method;
import java.util.Comparator;
static final class MethodSorter$2 implements Comparator<Method> {
    public int compare ( final Method m1, final Method m2 ) {
        final int comparison = m1.getName().compareTo ( m2.getName() );
        if ( comparison != 0 ) {
            return comparison;
        }
        return m1.toString().compareTo ( m2.toString() );
    }
}
