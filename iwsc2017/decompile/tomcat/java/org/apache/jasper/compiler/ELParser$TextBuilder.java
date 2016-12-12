package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
static class TextBuilder extends ELNode.Visitor {
    protected final boolean isDeferredSyntaxAllowedAsLiteral;
    protected final StringBuilder output;
    protected TextBuilder ( final boolean isDeferredSyntaxAllowedAsLiteral ) {
        this.output = new StringBuilder();
        this.isDeferredSyntaxAllowedAsLiteral = isDeferredSyntaxAllowedAsLiteral;
    }
    public String getText() {
        return this.output.toString();
    }
    @Override
    public void visit ( final ELNode.Root n ) throws JasperException {
        this.output.append ( n.getType() );
        this.output.append ( '{' );
        n.getExpression().visit ( this );
        this.output.append ( '}' );
    }
    @Override
    public void visit ( final ELNode.Function n ) throws JasperException {
        this.output.append ( ELParser.escapeLiteralExpression ( n.getOriginalText(), this.isDeferredSyntaxAllowedAsLiteral ) );
        this.output.append ( '(' );
    }
    @Override
    public void visit ( final ELNode.Text n ) throws JasperException {
        this.output.append ( ELParser.escapeLiteralExpression ( n.getText(), this.isDeferredSyntaxAllowedAsLiteral ) );
    }
    @Override
    public void visit ( final ELNode.ELText n ) throws JasperException {
        this.output.append ( ELParser.access$000 ( n.getText() ) );
    }
}
