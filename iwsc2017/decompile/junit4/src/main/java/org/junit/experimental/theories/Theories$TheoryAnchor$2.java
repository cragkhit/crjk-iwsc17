package org.junit.experimental.theories;
import org.junit.Assume;
import org.junit.runners.model.FrameworkMethod;
import org.junit.experimental.theories.internal.Assignments;
import org.junit.runners.model.Statement;
class Theories$TheoryAnchor$2 extends Statement {
    final   Assignments val$complete;
    final   FrameworkMethod val$method;
    final   Object val$freshInstance;
    public void evaluate() throws Throwable {
        final Object[] values = this.val$complete.getMethodArguments();
        if ( !TheoryAnchor.access$000 ( TheoryAnchor.this ) ) {
            Assume.assumeNotNull ( values );
        }
        this.val$method.invokeExplosively ( this.val$freshInstance, values );
    }
}
