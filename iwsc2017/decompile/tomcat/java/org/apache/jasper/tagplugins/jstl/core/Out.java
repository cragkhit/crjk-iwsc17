package org.apache.jasper.tagplugins.jstl.core;
import java.io.IOException;
import org.apache.jasper.tagplugins.jstl.Util;
import java.io.Reader;
import javax.servlet.jsp.JspWriter;
import org.apache.jasper.compiler.tagplugin.TagPluginContext;
import org.apache.jasper.compiler.tagplugin.TagPlugin;
public final class Out implements TagPlugin {
    @Override
    public void doTag ( final TagPluginContext ctxt ) {
        boolean hasDefault = false;
        boolean hasEscapeXml = false;
        hasDefault = ctxt.isAttributeSpecified ( "default" );
        hasEscapeXml = ctxt.isAttributeSpecified ( "escapeXml" );
        final String strObjectName = ctxt.getTemporaryVariableName();
        final String strValName = ctxt.getTemporaryVariableName();
        final String strDefName = ctxt.getTemporaryVariableName();
        final String strEscapeXmlName = ctxt.getTemporaryVariableName();
        final String strSkipBodyName = ctxt.getTemporaryVariableName();
        ctxt.generateImport ( "java.io.Reader" );
        ctxt.generateJavaSource ( "Object " + strObjectName + "=" );
        ctxt.generateAttribute ( "value" );
        ctxt.generateJavaSource ( ";" );
        ctxt.generateJavaSource ( "String " + strValName + "=null;" );
        ctxt.generateJavaSource ( "if(!(" + strObjectName + " instanceof Reader) && " + strObjectName + " != null){" );
        ctxt.generateJavaSource ( strValName + " = " + strObjectName + ".toString();" );
        ctxt.generateJavaSource ( "}" );
        ctxt.generateJavaSource ( "String " + strDefName + " = null;" );
        if ( hasDefault ) {
            ctxt.generateJavaSource ( "if(" );
            ctxt.generateAttribute ( "default" );
            ctxt.generateJavaSource ( " != null){" );
            ctxt.generateJavaSource ( strDefName + " = (" );
            ctxt.generateAttribute ( "default" );
            ctxt.generateJavaSource ( ").toString();" );
            ctxt.generateJavaSource ( "}" );
        }
        ctxt.generateJavaSource ( "boolean " + strEscapeXmlName + " = true;" );
        if ( hasEscapeXml ) {
            ctxt.generateJavaSource ( strEscapeXmlName + " = " );
            ctxt.generateAttribute ( "escapeXml" );
            ctxt.generateJavaSource ( ";" );
        }
        ctxt.generateJavaSource ( "boolean " + strSkipBodyName + " = org.apache.jasper.tagplugins.jstl.core.Out.output(out, " + strObjectName + ", " + strValName + ", " + strDefName + ", " + strEscapeXmlName + ");" );
        ctxt.generateJavaSource ( "if(!" + strSkipBodyName + ") {" );
        ctxt.generateBody();
        ctxt.generateJavaSource ( "}" );
    }
    public static boolean output ( final JspWriter out, final Object input, final String value, final String defaultValue, final boolean escapeXml ) throws IOException {
        if ( input instanceof Reader ) {
            final char[] buffer = new char[8096];
            int read = 0;
            while ( read != -1 ) {
                read = ( ( Reader ) input ).read ( buffer );
                if ( read != -1 ) {
                    if ( escapeXml ) {
                        final String escaped = Util.escapeXml ( buffer, read );
                        if ( escaped == null ) {
                            out.write ( buffer, 0, read );
                        } else {
                            out.print ( escaped );
                        }
                    } else {
                        out.write ( buffer, 0, read );
                    }
                }
            }
            return true;
        }
        String v = ( value != null ) ? value : defaultValue;
        if ( v != null ) {
            if ( escapeXml ) {
                v = Util.escapeXml ( v );
            }
            out.write ( v );
            return true;
        }
        return false;
    }
}
