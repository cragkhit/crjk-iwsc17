package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
public static class JspRoot extends Node {
    public JspRoot ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, "root", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
}
