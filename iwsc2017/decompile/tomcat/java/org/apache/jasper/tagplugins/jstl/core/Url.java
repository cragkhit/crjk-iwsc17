package org.apache.jasper.tagplugins.jstl.core;
import org.apache.jasper.tagplugins.jstl.Util;
import org.apache.jasper.compiler.tagplugin.TagPluginContext;
import org.apache.jasper.compiler.tagplugin.TagPlugin;
public class Url implements TagPlugin {
    @Override
    public void doTag ( final TagPluginContext ctxt ) {
        final boolean hasVar = ctxt.isAttributeSpecified ( "var" );
        final boolean hasContext = ctxt.isAttributeSpecified ( "context" );
        final boolean hasScope = ctxt.isAttributeSpecified ( "scope" );
        final String valueName = ctxt.getTemporaryVariableName();
        final String contextName = ctxt.getTemporaryVariableName();
        final String baseUrlName = ctxt.getTemporaryVariableName();
        final String resultName = ctxt.getTemporaryVariableName();
        final String responseName = ctxt.getTemporaryVariableName();
        String strScope = "page";
        if ( hasScope ) {
            strScope = ctxt.getConstantAttribute ( "scope" );
        }
        final int iScope = Util.getScope ( strScope );
        ctxt.generateJavaSource ( "String " + valueName + " = " );
        ctxt.generateAttribute ( "value" );
        ctxt.generateJavaSource ( ";" );
        ctxt.generateJavaSource ( "String " + contextName + " = null;" );
        if ( hasContext ) {
            ctxt.generateJavaSource ( contextName + " = " );
            ctxt.generateAttribute ( "context" );
            ctxt.generateJavaSource ( ";" );
        }
        ctxt.generateJavaSource ( "String " + baseUrlName + " = org.apache.jasper.tagplugins.jstl.Util.resolveUrl(" + valueName + ", " + contextName + ", pageContext);" );
        ctxt.generateJavaSource ( "pageContext.setAttribute(\"url_without_param\", " + baseUrlName + ");" );
        ctxt.generateBody();
        ctxt.generateJavaSource ( "String " + resultName + " = (String)pageContext.getAttribute(\"url_without_param\");" );
        ctxt.generateJavaSource ( "pageContext.removeAttribute(\"url_without_param\");" );
        ctxt.generateJavaSource ( "if(!org.apache.jasper.tagplugins.jstl.Util.isAbsoluteUrl(" + resultName + ")){" );
        ctxt.generateJavaSource ( "    HttpServletResponse " + responseName + " = ((HttpServletResponse) pageContext.getResponse());" );
        ctxt.generateJavaSource ( "    " + resultName + " = " + responseName + ".encodeURL(" + resultName + ");" );
        ctxt.generateJavaSource ( "}" );
        if ( hasVar ) {
            final String strVar = ctxt.getConstantAttribute ( "var" );
            ctxt.generateJavaSource ( "pageContext.setAttribute(\"" + strVar + "\", " + resultName + ", " + iScope + ");" );
        } else {
            ctxt.generateJavaSource ( "try{" );
            ctxt.generateJavaSource ( "    pageContext.getOut().print(" + resultName + ");" );
            ctxt.generateJavaSource ( "}catch(java.io.IOException ex){" );
            ctxt.generateJavaSource ( "    throw new JspTagException(ex.toString(), ex);" );
            ctxt.generateJavaSource ( "}" );
        }
    }
}
