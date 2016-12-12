package org.apache.jasper.tagplugins.jstl.core;
import org.apache.jasper.compiler.tagplugin.TagPluginContext;
import org.apache.jasper.compiler.tagplugin.TagPlugin;
public final class Choose implements TagPlugin {
    @Override
    public void doTag ( final TagPluginContext ctxt ) {
        ctxt.generateBody();
        ctxt.generateJavaSource ( "}" );
    }
}
