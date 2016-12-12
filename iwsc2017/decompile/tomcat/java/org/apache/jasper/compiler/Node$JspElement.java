package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
public static class JspElement extends Node {
    private JspAttribute[] jspAttrs;
    private JspAttribute nameAttr;
    public JspElement ( final Attributes attrs, final Mark start, final Node parent ) {
        this ( "jsp:element", attrs, null, null, start, parent );
    }
    public JspElement ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, "element", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
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
    public void setNameAttribute ( final JspAttribute nameAttr ) {
        this.nameAttr = nameAttr;
    }
    public JspAttribute getNameAttribute() {
        return this.nameAttr;
    }
}
