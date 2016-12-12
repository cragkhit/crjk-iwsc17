package org.junit.runners.model;
import org.junit.internal.MethodSorter;
import java.util.Comparator;
private static class MethodComparator implements Comparator<FrameworkMethod> {
    public int compare ( final FrameworkMethod left, final FrameworkMethod right ) {
        return MethodSorter.NAME_ASCENDING.compare ( left.getMethod(), right.getMethod() );
    }
}
