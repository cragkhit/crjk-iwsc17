package org.apache.catalina.ssi;
private final class OrNode extends OppNode {
    @Override
    public boolean evaluate() {
        return this.left.evaluate() || this.right.evaluate();
    }
    @Override
    public int getPrecedence() {
        return 1;
    }
    @Override
    public String toString() {
        return this.left + " " + this.right + " OR";
    }
}
