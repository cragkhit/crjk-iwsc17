package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
public static class InvokeAction extends Node {
    public InvokeAction ( final Attributes attrs, final Mark start, final Node parent ) {
        this ( "jsp:invoke", attrs, null, null, start, parent );
    }
    public InvokeAction ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, "invoke", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
}
