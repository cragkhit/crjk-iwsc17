package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
public static class Text extends ELNode {
    private final String text;
    Text ( final String text ) {
        this.text = text;
    }
    @Override
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
    public String getText() {
        return this.text;
    }
}
