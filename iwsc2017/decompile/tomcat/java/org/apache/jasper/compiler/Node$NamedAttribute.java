package org.apache.jasper.compiler;
import java.util.ArrayList;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
public static class NamedAttribute extends Node {
    private String temporaryVariableName;
    private boolean trim;
    private JspAttribute omit;
    private final ChildInfo childInfo;
    private final String name;
    private String localName;
    private String prefix;
    public NamedAttribute ( final Attributes attrs, final Mark start, final Node parent ) {
        this ( "jsp:attribute", attrs, null, null, start, parent );
    }
    public NamedAttribute ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        super ( qName, "attribute", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        this.trim = true;
        if ( "false".equals ( this.getAttributeValue ( "trim" ) ) ) {
            this.trim = false;
        }
        this.childInfo = new ChildInfo();
        this.name = this.getAttributeValue ( "name" );
        if ( this.name != null ) {
            this.localName = this.name;
            final int index = this.name.indexOf ( 58 );
            if ( index != -1 ) {
                this.prefix = this.name.substring ( 0, index );
                this.localName = this.name.substring ( index + 1 );
            }
        }
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
    public String getName() {
        return this.name;
    }
    @Override
    public String getLocalName() {
        return this.localName;
    }
    public String getPrefix() {
        return this.prefix;
    }
    public ChildInfo getChildInfo() {
        return this.childInfo;
    }
    public boolean isTrim() {
        return this.trim;
    }
    public void setOmit ( final JspAttribute omit ) {
        this.omit = omit;
    }
    public JspAttribute getOmit() {
        return this.omit;
    }
    public String getTemporaryVariableName() {
        if ( this.temporaryVariableName == null ) {
            this.temporaryVariableName = this.getRoot().nextTemporaryVariableName();
        }
        return this.temporaryVariableName;
    }
    @Override
    public String getText() {
        String text = "";
        if ( this.getBody() != null ) {
            class AttributeVisitor extends Visitor {
                private String attrValue;
                AttributeVisitor() {
                    this.attrValue = null;
                }
                @Override
                public void visit ( final TemplateText txt ) {
                    this.attrValue = txt.getText();
                }
                public String getAttrValue() {
                    return this.attrValue;
                }
            }
            final AttributeVisitor attributeVisitor = new AttributeVisitor();
            try {
                this.getBody().visit ( attributeVisitor );
            } catch ( JasperException ex ) {}
            text = attributeVisitor.getAttrValue();
        }
        return text;
    }
}
