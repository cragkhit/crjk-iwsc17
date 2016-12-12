package org.junit.runners;
import org.junit.runner.manipulation.Sorter;
import java.util.Comparator;
class ParentRunner$4 implements Comparator<T> {
    final   Sorter val$sorter;
    public int compare ( final T o1, final T o2 ) {
        return this.val$sorter.compare ( ParentRunner.this.describeChild ( o1 ), ParentRunner.this.describeChild ( o2 ) );
    }
}
