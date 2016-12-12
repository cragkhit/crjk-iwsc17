package org.apache.jasper.compiler;
import org.xml.sax.Attributes;
import java.util.ArrayList;
import org.apache.jasper.Options;
import org.apache.jasper.JasperException;
public class TextOptimizer {
    public static void concatenate ( final Compiler compiler, final Node.Nodes page ) throws JasperException {
        final TextCatVisitor v = new TextCatVisitor ( compiler );
        page.visit ( v );
        v.collectText();
    }
    private static class TextCatVisitor extends Node.Visitor {
        private static final String EMPTY_TEXT = "";
        private final Options options;
        private final PageInfo pageInfo;
        private int textNodeCount;
        private Node.TemplateText firstTextNode;
        private StringBuilder textBuffer;
        public TextCatVisitor ( final Compiler compiler ) {
            this.textNodeCount = 0;
            this.firstTextNode = null;
            this.options = compiler.getCompilationContext().getOptions();
            this.pageInfo = compiler.getPageInfo();
        }
        public void doVisit ( final Node n ) throws JasperException {
            this.collectText();
        }
        @Override
        public void visit ( final Node.PageDirective n ) throws JasperException {
        }
        @Override
        public void visit ( final Node.TagDirective n ) throws JasperException {
        }
        @Override
        public void visit ( final Node.TaglibDirective n ) throws JasperException {
        }
        @Override
        public void visit ( final Node.AttributeDirective n ) throws JasperException {
        }
        @Override
        public void visit ( final Node.VariableDirective n ) throws JasperException {
        }
        public void visitBody ( final Node n ) throws JasperException {
            super.visitBody ( n );
            this.collectText();
        }
        @Override
        public void visit ( final Node.TemplateText n ) throws JasperException {
            if ( ( this.options.getTrimSpaces() || this.pageInfo.isTrimDirectiveWhitespaces() ) && n.isAllSpace() ) {
                n.setText ( "" );
                return;
            }
            if ( this.textNodeCount++ == 0 ) {
                this.firstTextNode = n;
                this.textBuffer = new StringBuilder ( n.getText() );
            } else {
                this.textBuffer.append ( n.getText() );
                n.setText ( "" );
            }
        }
        private void collectText() {
            if ( this.textNodeCount > 1 ) {
                this.firstTextNode.setText ( this.textBuffer.toString() );
            }
            this.textNodeCount = 0;
        }
    }
}
