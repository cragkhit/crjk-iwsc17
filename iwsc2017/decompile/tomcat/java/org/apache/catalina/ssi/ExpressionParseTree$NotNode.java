package org.apache.catalina.ssi;
import java.util.List;
private final class NotNode extends OppNode {
    @Override
    public boolean evaluate() {
        return !this.left.evaluate();
    }
    @Override
    public int getPrecedence() {
        return 5;
    }
    @Override
    public void popValues ( final List<Node> values ) {
        this.left = values.remove ( 0 );
    }
    @Override
    public String toString() {
        return this.left + " NOT";
    }
}
