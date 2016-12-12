package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
public static class SetProperty extends Node {
    private JspAttribute value;
    public SetProperty ( final Attributes attrs, final Mark start, final Node parent ) {
        this ( "jsp:setProperty", attrs, null, null, start, parent );
    }
    public SetProperty ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, "setProperty", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
    public void setValue ( final JspAttribute value ) {
        this.value = value;
    }
    public JspAttribute getValue() {
        return this.value;
    }
}
