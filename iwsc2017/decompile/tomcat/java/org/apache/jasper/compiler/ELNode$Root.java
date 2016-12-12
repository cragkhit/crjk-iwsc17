package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
public static class Root extends ELNode {
    private final Nodes expr;
    private final char type;
    Root ( final Nodes expr, final char type ) {
        this.expr = expr;
        this.type = type;
    }
    @Override
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
    public Nodes getExpression() {
        return this.expr;
    }
    public char getType() {
        return this.type;
    }
}
