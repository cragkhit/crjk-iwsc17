package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
private static class ErrorVisitor extends Node.Visitor {
    private final int lineNum;
    private Node found;
    public ErrorVisitor ( final int lineNum ) {
        this.lineNum = lineNum;
    }
    public void doVisit ( final Node n ) throws JasperException {
        if ( this.lineNum >= n.getBeginJavaLine() && this.lineNum < n.getEndJavaLine() ) {
            this.found = n;
        }
    }
    public Node getJspSourceNode() {
        return this.found;
    }
}
