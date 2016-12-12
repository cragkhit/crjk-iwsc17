package org.junit.experimental.theories;
import org.junit.Assume;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runners.model.Statement;
import org.junit.runners.model.FrameworkMethod;
import java.util.List;
import org.junit.experimental.theories.internal.Assignments;
import org.junit.runners.BlockJUnit4ClassRunner;
class Theories$TheoryAnchor$1 extends BlockJUnit4ClassRunner {
    final   Assignments val$complete;
    protected void collectInitializationErrors ( final List<Throwable> errors ) {
    }
    public Statement methodBlock ( final FrameworkMethod method ) {
        final Statement statement = super.methodBlock ( method );
        return new Statement() {
            public void evaluate() throws Throwable {
                try {
                    statement.evaluate();
                    TheoryAnchor.this.handleDataPointSuccess();
                } catch ( AssumptionViolatedException e ) {
                    TheoryAnchor.this.handleAssumptionViolation ( e );
                } catch ( Throwable e2 ) {
                    TheoryAnchor.this.reportParameterizedError ( e2, BlockJUnit4ClassRunner.this.val$complete.getArgumentStrings ( TheoryAnchor.access$000 ( TheoryAnchor.this ) ) );
                }
            }
        };
    }
    protected Statement methodInvoker ( final FrameworkMethod method, final Object test ) {
        return TheoryAnchor.access$100 ( TheoryAnchor.this, method, this.val$complete, test );
    }
    public Object createTest() throws Exception {
        final Object[] params = this.val$complete.getConstructorArguments();
        if ( !TheoryAnchor.access$000 ( TheoryAnchor.this ) ) {
            Assume.assumeNotNull ( params );
        }
        return this.getTestClass().getOnlyConstructor().newInstance ( params );
    }
}
