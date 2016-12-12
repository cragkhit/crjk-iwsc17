package org.junit.runners;
import org.junit.runners.model.FrameworkMethod;
import org.junit.internal.runners.model.ReflectiveCallable;
class BlockJUnit4ClassRunner$1 extends ReflectiveCallable {
    final   FrameworkMethod val$method;
    protected Object runReflectiveCall() throws Throwable {
        return BlockJUnit4ClassRunner.this.createTest ( this.val$method );
    }
}
