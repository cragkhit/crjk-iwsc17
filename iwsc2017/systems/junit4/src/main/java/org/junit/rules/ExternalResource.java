package org.junit.rules;
import java.util.ArrayList;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
public abstract class ExternalResource implements TestRule {
    public Statement apply ( Statement base, Description description ) {
        return statement ( base );
    }
    private Statement statement ( final Statement base ) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                List<Throwable> errors = new ArrayList<Throwable>();
                try {
                    base.evaluate();
                } catch ( Throwable t ) {
                    errors.add ( t );
                } finally {
                    try {
                        after();
                    } catch ( Throwable t ) {
                        errors.add ( t );
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
