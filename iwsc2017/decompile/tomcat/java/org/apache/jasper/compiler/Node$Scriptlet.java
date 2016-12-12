package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
public static class Scriptlet extends ScriptingElement {
    public Scriptlet ( final String text, final Mark start, final Node parent ) {
        super ( "jsp:scriptlet", "scriptlet", text, start, parent );
    }
    public Scriptlet ( final String qName, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, "scriptlet", nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
}
