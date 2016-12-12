package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
public static class FallBackAction extends Node {
    public FallBackAction ( final Mark start, final Node parent ) {
        this ( "jsp:fallback", ( Attributes ) null, null, start, parent );
    }
    public FallBackAction ( final String qName, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, "fallback", null, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
}
