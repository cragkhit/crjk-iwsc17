package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
private static class CustomTagCounter extends Node.Visitor {
    private int count;
    private Node.CustomTag parent;
    @Override
    public void visit ( final Node.CustomTag n ) throws JasperException {
        n.setCustomTagParent ( this.parent );
        final Node.CustomTag tmpParent = this.parent;
        this.visitBody ( this.parent = n );
        this.parent = tmpParent;
        n.setNumCount ( this.count++ );
    }
}
