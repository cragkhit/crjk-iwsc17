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
import javax.servlet.jsp.tagext.TagData;
import org.apache.jasper.JasperException;
import javax.servlet.jsp.tagext.ValidationMessage;
import javax.servlet.jsp.tagext.TagInfo;
private static class TagExtraInfoVisitor extends Node.Visitor {
    private final ErrorDispatcher err;
    TagExtraInfoVisitor ( final Compiler compiler ) {
        this.err = compiler.getErrorDispatcher();
    }
    @Override
    public void visit ( final Node.CustomTag n ) throws JasperException {
        final TagInfo tagInfo = n.getTagInfo();
        if ( tagInfo == null ) {
            this.err.jspError ( n, "jsp.error.missing.tagInfo", n.getQName() );
        }
        final ValidationMessage[] errors = tagInfo.validate ( n.getTagData() );
        if ( errors != null && errors.length != 0 ) {
            final StringBuilder errMsg = new StringBuilder();
            errMsg.append ( "<h3>" );
            errMsg.append ( Localizer.getMessage ( "jsp.error.tei.invalid.attributes", n.getQName() ) );
            errMsg.append ( "</h3>" );
            for ( int i = 0; i < errors.length; ++i ) {
                errMsg.append ( "<p>" );
                if ( errors[i].getId() != null ) {
                    errMsg.append ( errors[i].getId() );
                    errMsg.append ( ": " );
                }
                errMsg.append ( errors[i].getMessage() );
                errMsg.append ( "</p>" );
            }
            this.err.jspError ( n, errMsg.toString(), new String[0] );
        }
        this.visitBody ( n );
    }
}
