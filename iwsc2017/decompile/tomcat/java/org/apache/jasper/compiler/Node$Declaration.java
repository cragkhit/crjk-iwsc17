package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
public static class Declaration extends ScriptingElement {
    public Declaration ( final String text, final Mark start, final Node parent ) {
        super ( "jsp:declaration", "declaration", text, start, parent );
    }
    public Declaration ( final String qName, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, "declaration", nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
}
