package org.apache.jasper.compiler;
import org.xml.sax.Attributes;
import org.apache.jasper.JasperException;
import java.util.ArrayList;
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
