package org.apache.jasper.tagplugins.jstl.core;
import org.apache.jasper.compiler.tagplugin.TagPluginContext;
import org.apache.jasper.compiler.tagplugin.TagPlugin;
public class ForTokens implements TagPlugin {
    @Override
    public void doTag ( final TagPluginContext ctxt ) {
        final boolean hasVar = ctxt.isAttributeSpecified ( "var" );
        final boolean hasVarStatus = ctxt.isAttributeSpecified ( "varStatus" );
        final boolean hasBegin = ctxt.isAttributeSpecified ( "begin" );
        final boolean hasEnd = ctxt.isAttributeSpecified ( "end" );
        final boolean hasStep = ctxt.isAttributeSpecified ( "step" );
        if ( hasVarStatus ) {
            ctxt.dontUseTagPlugin();
            return;
        }
        final String itemsName = ctxt.getTemporaryVariableName();
        final String delimsName = ctxt.getTemporaryVariableName();
        final String stName = ctxt.getTemporaryVariableName();
        final String beginName = ctxt.getTemporaryVariableName();
        final String endName = ctxt.getTemporaryVariableName();
        final String stepName = ctxt.getTemporaryVariableName();
        final String index = ctxt.getTemporaryVariableName();
        final String temp = ctxt.getTemporaryVariableName();
        final String tokensCountName = ctxt.getTemporaryVariableName();
        ctxt.generateJavaSource ( "String " + itemsName + " = (String)" );
        ctxt.generateAttribute ( "items" );
        ctxt.generateJavaSource ( ";" );
        ctxt.generateJavaSource ( "String " + delimsName + " = (String)" );
        ctxt.generateAttribute ( "delims" );
        ctxt.generateJavaSource ( ";" );
        ctxt.generateJavaSource ( "java.util.StringTokenizer " + stName + " = new java.util.StringTokenizer(" + itemsName + ", " + delimsName + ");" );
        ctxt.generateJavaSource ( "int " + tokensCountName + " = " + stName + ".countTokens();" );
        if ( hasBegin ) {
            ctxt.generateJavaSource ( "int " + beginName + " = " );
            ctxt.generateAttribute ( "begin" );
            ctxt.generateJavaSource ( ";" );
            ctxt.generateJavaSource ( "for(int " + index + " = 0; " + index + " < " + beginName + " && " + stName + ".hasMoreTokens(); " + index + "++, " + stName + ".nextToken()){}" );
        } else {
            ctxt.generateJavaSource ( "int " + beginName + " = 0;" );
        }
        if ( hasEnd ) {
            ctxt.generateJavaSource ( "int " + endName + " = 0;" );
            ctxt.generateJavaSource ( "if((" + tokensCountName + " - 1) < " );
            ctxt.generateAttribute ( "end" );
            ctxt.generateJavaSource ( "){" );
            ctxt.generateJavaSource ( "    " + endName + " = " + tokensCountName + " - 1;" );
            ctxt.generateJavaSource ( "}else{" );
            ctxt.generateJavaSource ( "    " + endName + " = " );
            ctxt.generateAttribute ( "end" );
            ctxt.generateJavaSource ( ";}" );
        } else {
            ctxt.generateJavaSource ( "int " + endName + " = " + tokensCountName + " - 1;" );
        }
        if ( hasStep ) {
            ctxt.generateJavaSource ( "int " + stepName + " = " );
            ctxt.generateAttribute ( "step" );
            ctxt.generateJavaSource ( ";" );
        } else {
            ctxt.generateJavaSource ( "int " + stepName + " = 1;" );
        }
        ctxt.generateJavaSource ( "for(int " + index + " = " + beginName + "; " + index + " <= " + endName + "; " + index + "++){" );
        ctxt.generateJavaSource ( "    String " + temp + " = " + stName + ".nextToken();" );
        ctxt.generateJavaSource ( "    if(((" + index + " - " + beginName + ") % " + stepName + ") == 0){" );
        if ( hasVar ) {
            final String strVar = ctxt.getConstantAttribute ( "var" );
            ctxt.generateJavaSource ( "        pageContext.setAttribute(\"" + strVar + "\", " + temp + ");" );
        }
        ctxt.generateBody();
        ctxt.generateJavaSource ( "    }" );
        ctxt.generateJavaSource ( "}" );
    }
}
