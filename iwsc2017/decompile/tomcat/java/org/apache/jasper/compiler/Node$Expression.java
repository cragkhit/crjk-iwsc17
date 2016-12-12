package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
public static class Expression extends ScriptingElement {
    public Expression ( final String text, final Mark start, final Node parent ) {
        super ( "jsp:expression", "expression", text, start, parent );
    }
    public Expression ( final String qName, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, "expression", nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
}
