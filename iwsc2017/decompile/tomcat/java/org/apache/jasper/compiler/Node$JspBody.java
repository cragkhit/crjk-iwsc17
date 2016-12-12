package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
public static class JspBody extends Node {
    private final ChildInfo childInfo;
    public JspBody ( final Mark start, final Node parent ) {
        this ( "jsp:body", ( Attributes ) null, null, start, parent );
    }
    public JspBody ( final String qName, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, "body", null, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        this.childInfo = new ChildInfo();
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
    public ChildInfo getChildInfo() {
        return this.childInfo;
    }
}
