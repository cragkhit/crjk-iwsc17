package org.junit.internal.runners;
import org.junit.runner.manipulation.Sorter;
import java.lang.reflect.Method;
import java.util.Comparator;
class JUnit4ClassRunner$2 implements Comparator<Method> {
    final   Sorter val$sorter;
    public int compare ( final Method o1, final Method o2 ) {
        return this.val$sorter.compare ( JUnit4ClassRunner.this.methodDescription ( o1 ), JUnit4ClassRunner.this.methodDescription ( o2 ) );
    }
}
