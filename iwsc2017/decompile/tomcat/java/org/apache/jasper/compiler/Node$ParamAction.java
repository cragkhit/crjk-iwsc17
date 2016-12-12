package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
public static class ParamAction extends Node {
    private JspAttribute value;
    public ParamAction ( final Attributes attrs, final Mark start, final Node parent ) {
        this ( "jsp:param", attrs, null, null, start, parent );
    }
    public ParamAction ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, "param", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
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
