package org.junit.experimental.theories;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runners.model.Statement;
class Theories$TheoryAnchor$1$1 extends Statement {
    final   Statement val$statement;
    public void evaluate() throws Throwable {
        try {
            this.val$statement.evaluate();
            BlockJUnit4ClassRunner.this.this$0.handleDataPointSuccess();
        } catch ( AssumptionViolatedException e ) {
            BlockJUnit4ClassRunner.this.this$0.handleAssumptionViolation ( e );
        } catch ( Throwable e2 ) {
            BlockJUnit4ClassRunner.this.this$0.reportParameterizedError ( e2, BlockJUnit4ClassRunner.this.val$complete.getArgumentStrings ( TheoryAnchor.access$000 ( BlockJUnit4ClassRunner.this.this$0 ) ) );
        }
    }
}
