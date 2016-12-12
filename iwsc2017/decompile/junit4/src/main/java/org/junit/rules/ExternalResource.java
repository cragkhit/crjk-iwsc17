package org.junit.rules;
import java.util.List;
import org.junit.runners.model.MultipleFailureException;
import java.util.ArrayList;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
public abstract class ExternalResource implements TestRule {
    public Statement apply ( final Statement base, final Description description ) {
        return this.statement ( base );
    }
    private Statement statement ( final Statement base ) {
        return new Statement() {
            public void evaluate() throws Throwable {
                ExternalResource.this.before();
                final List<Throwable> errors = new ArrayList<Throwable>();
                try {
                    base.evaluate();
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
        };
    }
    protected void before() throws Throwable {
    }
    protected void after() {
    }
}
