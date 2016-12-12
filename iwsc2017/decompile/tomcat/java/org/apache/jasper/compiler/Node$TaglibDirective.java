package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
public static class TaglibDirective extends Node {
    public TaglibDirective ( final Attributes attrs, final Mark start, final Node parent ) {
        super ( "jsp:taglib", "taglib", attrs, start, parent );
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
}
