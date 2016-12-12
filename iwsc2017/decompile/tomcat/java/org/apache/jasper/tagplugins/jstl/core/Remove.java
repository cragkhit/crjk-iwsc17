package org.apache.jasper.tagplugins.jstl.core;
import org.apache.jasper.tagplugins.jstl.Util;
import org.apache.jasper.compiler.tagplugin.TagPluginContext;
import org.apache.jasper.compiler.tagplugin.TagPlugin;
public class Remove implements TagPlugin {
    @Override
    public void doTag ( final TagPluginContext ctxt ) {
        final boolean hasScope = ctxt.isAttributeSpecified ( "scope" );
        final String strVar = ctxt.getConstantAttribute ( "var" );
        if ( hasScope ) {
            final int iScope = Util.getScope ( ctxt.getConstantAttribute ( "scope" ) );
            ctxt.generateJavaSource ( "pageContext.removeAttribute(\"" + strVar + "\"," + iScope + ");" );
        } else {
            ctxt.generateJavaSource ( "pageContext.removeAttribute(\"" + strVar + "\");" );
        }
    }
}
