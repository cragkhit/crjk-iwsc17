package org.apache.jasper.tagplugins.jstl.core;
import org.apache.jasper.compiler.tagplugin.TagPluginContext;
import org.apache.jasper.compiler.tagplugin.TagPlugin;
public class Catch implements TagPlugin {
    @Override
    public void doTag ( final TagPluginContext ctxt ) {
        final boolean hasVar = ctxt.isAttributeSpecified ( "var" );
        final String exceptionName = ctxt.getTemporaryVariableName();
        final String caughtName = ctxt.getTemporaryVariableName();
        ctxt.generateJavaSource ( "boolean " + caughtName + " = false;" );
        ctxt.generateJavaSource ( "try{" );
        ctxt.generateBody();
        ctxt.generateJavaSource ( "}" );
        ctxt.generateJavaSource ( "catch(Throwable " + exceptionName + "){" );
        if ( hasVar ) {
            final String strVar = ctxt.getConstantAttribute ( "var" );
            ctxt.generateJavaSource ( "    pageContext.setAttribute(\"" + strVar + "\", " + exceptionName + ", PageContext.PAGE_SCOPE);" );
        }
        ctxt.generateJavaSource ( "    " + caughtName + " = true;" );
        ctxt.generateJavaSource ( "}" );
        ctxt.generateJavaSource ( "finally{" );
        if ( hasVar ) {
            final String strVar = ctxt.getConstantAttribute ( "var" );
            ctxt.generateJavaSource ( "    if(!" + caughtName + "){" );
            ctxt.generateJavaSource ( "        pageContext.removeAttribute(\"" + strVar + "\", PageContext.PAGE_SCOPE);" );
            ctxt.generateJavaSource ( "    }" );
        }
        ctxt.generateJavaSource ( "}" );
    }
}
