package org.apache.jasper.compiler;
import javax.servlet.jsp.tagext.TagAttributeInfo;
static class NameEntry {
    private String type;
    private Node node;
    private TagAttributeInfo attr;
    NameEntry ( final String type, final Node node, final TagAttributeInfo attr ) {
        this.type = type;
        this.node = node;
        this.attr = attr;
    }
    String getType() {
        return this.type;
    }
    Node getNode() {
        return this.node;
    }
    TagAttributeInfo getTagAttributeInfo() {
        return this.attr;
    }
}
