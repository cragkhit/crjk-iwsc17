package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
public static class UseBean extends Node {
    private JspAttribute beanName;
    public UseBean ( final Attributes attrs, final Mark start, final Node parent ) {
        this ( "jsp:useBean", attrs, null, null, start, parent );
    }
    public UseBean ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, "useBean", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
    public void setBeanName ( final JspAttribute beanName ) {
        this.beanName = beanName;
    }
    public JspAttribute getBeanName() {
        return this.beanName;
    }
}
