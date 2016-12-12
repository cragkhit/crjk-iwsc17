package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
public static class AttributeDirective extends Node {
    public AttributeDirective ( final Attributes attrs, final Mark start, final Node parent ) {
        this ( "jsp:directive.attribute", attrs, null, null, start, parent );
    }
    public AttributeDirective ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, "directive.attribute", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
}
