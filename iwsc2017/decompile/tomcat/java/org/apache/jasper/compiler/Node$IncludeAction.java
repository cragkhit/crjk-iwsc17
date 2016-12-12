package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
public static class IncludeAction extends Node {
    private JspAttribute page;
    public IncludeAction ( final Attributes attrs, final Mark start, final Node parent ) {
        this ( "jsp:include", attrs, null, null, start, parent );
    }
    public IncludeAction ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, "include", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
    public void setPage ( final JspAttribute page ) {
        this.page = page;
    }
    public JspAttribute getPage() {
        return this.page;
    }
}
