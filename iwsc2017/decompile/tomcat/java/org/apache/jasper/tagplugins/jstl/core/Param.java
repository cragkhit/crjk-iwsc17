package org.apache.jasper.tagplugins.jstl.core;
import org.apache.jasper.compiler.tagplugin.TagPluginContext;
import org.apache.jasper.compiler.tagplugin.TagPlugin;
public class Param implements TagPlugin {
    @Override
    public void doTag ( final TagPluginContext ctxt ) {
        final String nameName = ctxt.getTemporaryVariableName();
        final String valueName = ctxt.getTemporaryVariableName();
        final String urlName = ctxt.getTemporaryVariableName();
        final String encName = ctxt.getTemporaryVariableName();
        final String index = ctxt.getTemporaryVariableName();
        final TagPluginContext parent = ctxt.getParentContext();
        if ( parent == null ) {
            ctxt.generateJavaSource ( " throw new JspTagExcption(\"&lt;param&gt; outside &lt;import&gt; or &lt;urlEncode&gt;\");" );
            return;
        }
        ctxt.generateJavaSource ( "String " + urlName + " = (String)pageContext.getAttribute(\"url_without_param\");" );
        ctxt.generateJavaSource ( "String " + nameName + " = " );
        ctxt.generateAttribute ( "name" );
        ctxt.generateJavaSource ( ";" );
        ctxt.generateJavaSource ( "if(" + nameName + " != null && !" + nameName + ".equals(\"\")){" );
        ctxt.generateJavaSource ( "    String " + valueName + " = " );
        ctxt.generateAttribute ( "value" );
        ctxt.generateJavaSource ( ";" );
        ctxt.generateJavaSource ( "    if(" + valueName + " == null) " + valueName + " = \"\";" );
        ctxt.generateJavaSource ( "    String " + encName + " = pageContext.getResponse().getCharacterEncoding();" );
        ctxt.generateJavaSource ( "    " + nameName + " = java.net.URLEncoder.encode(" + nameName + ", " + encName + ");" );
        ctxt.generateJavaSource ( "    " + valueName + " = java.net.URLEncoder.encode(" + valueName + ", " + encName + ");" );
        ctxt.generateJavaSource ( "    int " + index + ";" );
        ctxt.generateJavaSource ( "    " + index + " = " + urlName + ".indexOf('?');" );
        ctxt.generateJavaSource ( "    if(" + index + " == -1){" );
        ctxt.generateJavaSource ( "        " + urlName + " = " + urlName + " + \"?\" + " + nameName + " + \"=\" + " + valueName + ";" );
        ctxt.generateJavaSource ( "    }else{" );
        ctxt.generateJavaSource ( "        " + urlName + " = " + urlName + " + \"&\" + " + nameName + " + \"=\" + " + valueName + ";" );
        ctxt.generateJavaSource ( "    }" );
        ctxt.generateJavaSource ( "    pageContext.setAttribute(\"url_without_param\"," + urlName + ");" );
        ctxt.generateJavaSource ( "}" );
    }
}
