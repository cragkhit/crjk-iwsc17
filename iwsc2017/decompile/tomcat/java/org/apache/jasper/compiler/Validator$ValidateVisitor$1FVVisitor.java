package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import javax.servlet.jsp.tagext.FunctionInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
class FVVisitor extends ELNode.Visitor {
    private Node n;
    FVVisitor ( final Node n ) {
        this.n = n;
    }
    @Override
    public void visit ( final ELNode.Function func ) throws JasperException {
        final String prefix = func.getPrefix();
        final String function = func.getName();
        String uri = null;
        if ( this.n.getRoot().isXmlSyntax() ) {
            uri = ValidateVisitor.access$100 ( ValidateVisitor.this, prefix, this.n );
        } else if ( prefix != null ) {
            uri = ValidateVisitor.access$200 ( ValidateVisitor.this ).getURI ( prefix );
        }
        if ( uri == null ) {
            if ( prefix == null ) {
                return;
            }
            ValidateVisitor.access$300 ( ValidateVisitor.this ).jspError ( this.n, "jsp.error.attribute.invalidPrefix", prefix );
        }
        final TagLibraryInfo taglib = ValidateVisitor.access$200 ( ValidateVisitor.this ).getTaglib ( uri );
        FunctionInfo funcInfo = null;
        if ( taglib != null ) {
            funcInfo = taglib.getFunction ( function );
        }
        if ( funcInfo == null ) {
            ValidateVisitor.access$300 ( ValidateVisitor.this ).jspError ( this.n, "jsp.error.noFunction", function );
        }
        func.setUri ( uri );
        func.setFunctionInfo ( funcInfo );
        ValidateVisitor.access$400 ( ValidateVisitor.this, func );
    }
}
