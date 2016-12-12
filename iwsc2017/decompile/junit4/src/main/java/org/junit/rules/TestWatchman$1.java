package org.junit.rules;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
class TestWatchman$1 extends Statement {
    final   FrameworkMethod val$method;
    final   Statement val$base;
    public void evaluate() throws Throwable {
        TestWatchman.this.starting ( this.val$method );
        try {
            this.val$base.evaluate();
            TestWatchman.this.succeeded ( this.val$method );
        } catch ( AssumptionViolatedException e ) {
            throw e;
        } catch ( Throwable e2 ) {
            TestWatchman.this.failed ( e2, this.val$method );
            throw e2;
        } finally {
            TestWatchman.this.finished ( this.val$method );
        }
    }
}
