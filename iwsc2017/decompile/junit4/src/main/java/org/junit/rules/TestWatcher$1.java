package org.junit.rules;
import org.junit.runners.model.MultipleFailureException;
import org.junit.internal.AssumptionViolatedException;
import java.util.List;
import java.util.ArrayList;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
class TestWatcher$1 extends Statement {
    final   Description val$description;
    final   Statement val$base;
    public void evaluate() throws Throwable {
        final List<Throwable> errors = new ArrayList<Throwable>();
        TestWatcher.access$000 ( TestWatcher.this, this.val$description, errors );
        try {
            this.val$base.evaluate();
            TestWatcher.access$100 ( TestWatcher.this, this.val$description, errors );
        } catch ( AssumptionViolatedException e ) {
            errors.add ( e );
            TestWatcher.access$200 ( TestWatcher.this, e, this.val$description, errors );
        } catch ( Throwable e2 ) {
            errors.add ( e2 );
            TestWatcher.access$300 ( TestWatcher.this, e2, this.val$description, errors );
        } finally {
            TestWatcher.access$400 ( TestWatcher.this, this.val$description, errors );
        }
        MultipleFailureException.assertEmpty ( errors );
    }
}
