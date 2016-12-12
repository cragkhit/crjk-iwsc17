package org.junit.rules;
import java.util.List;
import org.junit.runners.model.MultipleFailureException;
import java.util.ArrayList;
import org.junit.runners.model.Statement;
class ExternalResource$1 extends Statement {
    final   Statement val$base;
    public void evaluate() throws Throwable {
        ExternalResource.this.before();
        final List<Throwable> errors = new ArrayList<Throwable>();
        try {
            this.val$base.evaluate();
        } catch ( Throwable t ) {
            errors.add ( t );
            try {
                ExternalResource.this.after();
            } catch ( Throwable t ) {
                errors.add ( t );
            }
        } finally {
            try {
                ExternalResource.this.after();
            } catch ( Throwable t2 ) {
                errors.add ( t2 );
            }
        }
        MultipleFailureException.assertEmpty ( errors );
    }
}
