package org.apache.jasper.compiler;
import org.xml.sax.Attributes;
import org.apache.jasper.JasperException;
public static class Comment extends Node {
    public Comment ( final String text, final Mark start, final Node parent ) {
        super ( null, null, text, start, parent );
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
}
