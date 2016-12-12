package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
public static class VariableDirective extends Node {
    public VariableDirective ( final Attributes attrs, final Mark start, final Node parent ) {
        this ( "jsp:directive.variable", attrs, null, null, start, parent );
    }
    public VariableDirective ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, "directive.variable", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
}
