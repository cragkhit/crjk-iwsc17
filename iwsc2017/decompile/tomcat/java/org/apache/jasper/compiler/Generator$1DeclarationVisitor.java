package org.apache.jasper.compiler;
import java.util.List;
import org.xml.sax.Attributes;
import java.util.Vector;
import org.apache.jasper.JasperException;
class DeclarationVisitor extends Node.Visitor {
    private boolean getServletInfoGenerated;
    DeclarationVisitor() {
        this.getServletInfoGenerated = false;
    }
    @Override
    public void visit ( final Node.PageDirective n ) throws JasperException {
        if ( this.getServletInfoGenerated ) {
            return;
        }
        final String info = n.getAttributeValue ( "info" );
        if ( info == null ) {
            return;
        }
        this.getServletInfoGenerated = true;
        Generator.access$000 ( Generator.this ).printil ( "public java.lang.String getServletInfo() {" );
        Generator.access$000 ( Generator.this ).pushIndent();
        Generator.access$000 ( Generator.this ).printin ( "return " );
        Generator.access$000 ( Generator.this ).print ( Generator.quote ( info ) );
        Generator.access$000 ( Generator.this ).println ( ";" );
        Generator.access$000 ( Generator.this ).popIndent();
        Generator.access$000 ( Generator.this ).printil ( "}" );
        Generator.access$000 ( Generator.this ).println();
    }
    @Override
    public void visit ( final Node.Declaration n ) throws JasperException {
        n.setBeginJavaLine ( Generator.access$000 ( Generator.this ).getJavaLine() );
        Generator.access$000 ( Generator.this ).printMultiLn ( n.getText() );
        Generator.access$000 ( Generator.this ).println();
        n.setEndJavaLine ( Generator.access$000 ( Generator.this ).getJavaLine() );
    }
    @Override
    public void visit ( final Node.CustomTag n ) throws JasperException {
        if ( n.useTagPlugin() ) {
            if ( n.getAtSTag() != null ) {
                n.getAtSTag().visit ( this );
            }
            this.visitBody ( n );
            if ( n.getAtETag() != null ) {
                n.getAtETag().visit ( this );
            }
        } else {
            this.visitBody ( n );
        }
    }
}
