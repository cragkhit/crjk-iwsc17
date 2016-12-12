package org.apache.jasper.tagplugins.jstl.core;
import org.apache.jasper.compiler.tagplugin.TagPlugin;
import org.apache.jasper.compiler.tagplugin.TagPluginContext;
import org.apache.jasper.tagplugins.jstl.Util;
public class Url implements TagPlugin {
    @Override
    public void doTag ( TagPluginContext ctxt ) {
        boolean hasVar, hasContext, hasScope;
        hasVar = ctxt.isAttributeSpecified ( "var" );
        hasContext = ctxt.isAttributeSpecified ( "context" );
        hasScope = ctxt.isAttributeSpecified ( "scope" );
        String valueName = ctxt.getTemporaryVariableName();
        String contextName = ctxt.getTemporaryVariableName();
        String baseUrlName = ctxt.getTemporaryVariableName();
        String resultName = ctxt.getTemporaryVariableName();
        String responseName = ctxt.getTemporaryVariableName();
        String strScope = "page";
        if ( hasScope ) {
            strScope = ctxt.getConstantAttribute ( "scope" );
        }
        int iScope = Util.getScope ( strScope );
        ctxt.generateJavaSource ( "String " + valueName + " = " );
        ctxt.generateAttribute ( "value" );
        ctxt.generateJavaSource ( ";" );
        ctxt.generateJavaSource ( "String " + contextName + " = null;" );
        if ( hasContext ) {
            ctxt.generateJavaSource ( contextName + " = " );
            ctxt.generateAttribute ( "context" );
            ctxt.generateJavaSource ( ";" );
        }
        ctxt.generateJavaSource ( "String " + baseUrlName + " = " +
                                  "org.apache.jasper.tagplugins.jstl.Util.resolveUrl(" + valueName + ", " + contextName + ", pageContext);" );
        ctxt.generateJavaSource ( "pageContext.setAttribute" +
                                  "(\"url_without_param\", " + baseUrlName + ");" );
        ctxt.generateBody();
        ctxt.generateJavaSource ( "String " + resultName + " = " +
                                  "(String)pageContext.getAttribute(\"url_without_param\");" );
        ctxt.generateJavaSource ( "pageContext.removeAttribute(\"url_without_param\");" );
        ctxt.generateJavaSource ( "if(!org.apache.jasper.tagplugins.jstl.Util.isAbsoluteUrl(" + resultName + ")){" );
        ctxt.generateJavaSource ( "    HttpServletResponse " + responseName + " = " +
                                  "((HttpServletResponse) pageContext.getResponse());" );
        ctxt.generateJavaSource ( "    " + resultName + " = "
                                  + responseName + ".encodeURL(" + resultName + ");" );
        ctxt.generateJavaSource ( "}" );
        if ( hasVar ) {
            String strVar = ctxt.getConstantAttribute ( "var" );
            ctxt.generateJavaSource ( "pageContext.setAttribute" +
                                      "(\"" + strVar + "\", " + resultName + ", " + iScope + ");" );
        } else {
            ctxt.generateJavaSource ( "try{" );
            ctxt.generateJavaSource ( "    pageContext.getOut().print(" + resultName + ");" );
            ctxt.generateJavaSource ( "}catch(java.io.IOException ex){" );
            ctxt.generateJavaSource ( "    throw new JspTagException(ex.toString(), ex);" );
            ctxt.generateJavaSource ( "}" );
        }
    }
}
