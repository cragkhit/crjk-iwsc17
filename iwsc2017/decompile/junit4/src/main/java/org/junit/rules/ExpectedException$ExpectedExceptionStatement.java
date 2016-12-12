package org.junit.rules;
import org.junit.runners.model.Statement;
private class ExpectedExceptionStatement extends Statement {
    private final Statement next;
    public ExpectedExceptionStatement ( final Statement base ) {
        this.next = base;
    }
    public void evaluate() throws Throwable {
        try {
            this.next.evaluate();
        } catch ( Throwable e ) {
            ExpectedException.access$000 ( ExpectedException.this, e );
            return;
        }
        if ( ExpectedException.access$100 ( ExpectedException.this ) ) {
            ExpectedException.access$200 ( ExpectedException.this );
        }
    }
}
