package org.apache.jasper.compiler;
import org.xml.sax.Attributes;
import org.apache.jasper.JasperException;
public static class AttributeGenerator extends Node {
    private String name;
    private CustomTag tag;
    public AttributeGenerator ( final Mark start, final String name, final CustomTag tag ) {
        super ( start, null );
        this.name = name;
        this.tag = tag;
    }
    public void accept ( final Visitor v ) throws JasperException {
        v.visit ( this );
    }
    public String getName() {
        return this.name;
    }
    public CustomTag getTag() {
        return this.tag;
    }
}
