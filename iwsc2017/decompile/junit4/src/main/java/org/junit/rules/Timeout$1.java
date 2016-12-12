package org.junit.rules;
import org.junit.runners.model.Statement;
class Timeout$1 extends Statement {
    final   Exception val$e;
    public void evaluate() throws Throwable {
        throw new RuntimeException ( "Invalid parameters for Timeout", this.val$e );
    }
}
