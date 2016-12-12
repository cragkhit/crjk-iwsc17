package org.apache.jasper.tagplugins.jstl.core;
import org.apache.jasper.compiler.tagplugin.TagPlugin;
import org.apache.jasper.compiler.tagplugin.TagPluginContext;
public final class Otherwise implements TagPlugin {
    @Override
    public void doTag ( TagPluginContext ctxt ) {
        ctxt.generateJavaSource ( "} else {" );
        ctxt.generateBody();
    }
}
