package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
public static class ParamsAction extends Node {
    public ParamsAction ( final Mark start, final Node parent ) {
        this ( "jsp:params", ( Attributes ) null, null, start, parent );
    }
    public ParamsAction ( final String qName, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, "params", null, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
}
