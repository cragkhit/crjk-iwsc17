package org.junit.runners.model;
import org.junit.internal.runners.model.ReflectiveCallable;
class FrameworkMethod$1 extends ReflectiveCallable {
    final   Object val$target;
    final   Object[] val$params;
    protected Object runReflectiveCall() throws Throwable {
        return FrameworkMethod.access$000 ( FrameworkMethod.this ).invoke ( this.val$target, this.val$params );
    }
}
