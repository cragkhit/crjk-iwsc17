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
class Generator$GenBuffer$1 extends Node.Visitor {
    final   int val$offset;
    public void doVisit ( final Node n ) {
        GenBuffer.access$1700 ( n, this.val$offset );
    }
    @Override
    public void visit ( final Node.CustomTag n ) throws JasperException {
        final Node.Nodes b = n.getBody();
        if ( b != null && !b.isGeneratedInBuffer() ) {
            b.visit ( this );
        }
    }
}
