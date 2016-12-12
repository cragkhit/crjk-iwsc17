package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
public static class PlugIn extends Node {
    private JspAttribute width;
    private JspAttribute height;
    public PlugIn ( final Attributes attrs, final Mark start, final Node parent ) {
        this ( "jsp:plugin", attrs, null, null, start, parent );
    }
    public PlugIn ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, "plugin", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
    public void setHeight ( final JspAttribute height ) {
        this.height = height;
    }
    public void setWidth ( final JspAttribute width ) {
        this.width = width;
    }
    public JspAttribute getHeight() {
        return this.height;
    }
    public JspAttribute getWidth() {
        return this.width;
    }
}
