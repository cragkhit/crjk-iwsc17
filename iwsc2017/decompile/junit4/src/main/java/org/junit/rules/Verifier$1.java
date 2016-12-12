package org.junit.rules;
import org.junit.runners.model.Statement;
class Verifier$1 extends Statement {
    final   Statement val$base;
    public void evaluate() throws Throwable {
        this.val$base.evaluate();
        Verifier.this.verify();
    }
}
