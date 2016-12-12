package org.apache.catalina.ssi;
private final class LessThanNode extends CompareNode {
    @Override
    public boolean evaluate() {
        return this.compareBranches() < 0;
    }
    @Override
    public int getPrecedence() {
        return 4;
    }
    @Override
    public String toString() {
        return this.left + " " + this.right + " LT";
    }
}
