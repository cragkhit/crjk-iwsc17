package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
public static class UninterpretedTag extends Node {
    private JspAttribute[] jspAttrs;
    public UninterpretedTag ( final String qName, final String localName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, localName, attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
    public void setJspAttributes ( final JspAttribute[] jspAttrs ) {
        this.jspAttrs = jspAttrs;
    }
    public JspAttribute[] getJspAttributes() {
        return this.jspAttrs;
    }
}
