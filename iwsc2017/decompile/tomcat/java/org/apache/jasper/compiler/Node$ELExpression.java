package org.apache.jasper.compiler;
import org.xml.sax.Attributes;
import org.apache.jasper.JasperException;
public static class ELExpression extends Node {
    private ELNode.Nodes el;
    private final char type;
    public ELExpression ( final char type, final String text, final Mark start, final Node parent ) {
        super ( null, null, text, start, parent );
        this.type = type;
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
    public void setEL ( final ELNode.Nodes el ) {
        this.el = el;
    }
    public ELNode.Nodes getEL() {
        return this.el;
    }
    public char getType() {
        return this.type;
    }
}
