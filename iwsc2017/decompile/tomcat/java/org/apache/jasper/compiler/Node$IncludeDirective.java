package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
public static class IncludeDirective extends Node {
    public IncludeDirective ( final Attributes attrs, final Mark start, final Node parent ) {
        this ( "jsp:directive.include", attrs, null, null, start, parent );
    }
    public IncludeDirective ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, "directive.include", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
}
