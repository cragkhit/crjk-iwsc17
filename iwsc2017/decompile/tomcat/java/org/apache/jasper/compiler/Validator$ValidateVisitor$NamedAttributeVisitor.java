package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
private static class NamedAttributeVisitor extends Node.Visitor {
    private boolean hasDynamicContent;
    public void doVisit ( final Node n ) throws JasperException {
        if ( ! ( n instanceof Node.JspText ) && ! ( n instanceof Node.TemplateText ) ) {
            this.hasDynamicContent = true;
        }
        this.visitBody ( n );
    }
    public boolean hasDynamicContent() {
        return this.hasDynamicContent;
    }
}
