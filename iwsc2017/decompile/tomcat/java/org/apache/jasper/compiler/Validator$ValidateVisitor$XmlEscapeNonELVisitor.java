package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
private static class XmlEscapeNonELVisitor extends ELParser.TextBuilder {
    protected XmlEscapeNonELVisitor ( final boolean isDeferredSyntaxAllowedAsLiteral ) {
        super ( isDeferredSyntaxAllowedAsLiteral );
    }
    @Override
    public void visit ( final ELNode.Text n ) throws JasperException {
        this.output.append ( ELParser.escapeLiteralExpression ( Validator.xmlEscape ( n.getText() ), this.isDeferredSyntaxAllowedAsLiteral ) );
    }
}
