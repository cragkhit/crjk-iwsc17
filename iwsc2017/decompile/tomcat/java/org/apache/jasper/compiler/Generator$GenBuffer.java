package org.apache.jasper.compiler;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.JspIdConsumer;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.SimpleTag;
import javax.servlet.jsp.tagext.TryCatchFinally;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;
import org.xml.sax.Attributes;
import org.apache.jasper.compiler.tagplugin.TagPluginContext;
import java.util.List;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.servlet.jsp.tagext.TagFileInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagData;
import org.apache.jasper.JasperException;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.CharArrayWriter;
private static class GenBuffer {
    private Node node;
    private Node.Nodes body;
    private CharArrayWriter charWriter;
    protected ServletWriter out;
    GenBuffer() {
        this ( null, null );
    }
    GenBuffer ( final Node n, final Node.Nodes b ) {
        this.node = n;
        this.body = b;
        if ( this.body != null ) {
            this.body.setGeneratedInBuffer ( true );
        }
        this.charWriter = new CharArrayWriter();
        this.out = new ServletWriter ( new PrintWriter ( this.charWriter ) );
    }
    public ServletWriter getOut() {
        return this.out;
    }
    @Override
    public String toString() {
        return this.charWriter.toString();
    }
    public void adjustJavaLines ( final int offset ) {
        if ( this.node != null ) {
            adjustJavaLine ( this.node, offset );
        }
        if ( this.body != null ) {
            try {
                this.body.visit ( new Node.Visitor() {
                    public void doVisit ( final Node n ) {
                        adjustJavaLine ( n, offset );
                    }
                    @Override
                    public void visit ( final Node.CustomTag n ) throws JasperException {
                        final Node.Nodes b = n.getBody();
                        if ( b != null && !b.isGeneratedInBuffer() ) {
                            b.visit ( this );
                        }
                    }
                } );
            } catch ( JasperException ex ) {}
        }
    }
    private static void adjustJavaLine ( final Node n, final int offset ) {
        if ( n.getBeginJavaLine() > 0 ) {
            n.setBeginJavaLine ( n.getBeginJavaLine() + offset );
            n.setEndJavaLine ( n.getEndJavaLine() + offset );
        }
    }
}
