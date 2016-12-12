package org.apache.tomcat.util.descriptor.tagplugin;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSetBase;
private static class TagPluginRuleSet extends RuleSetBase {
    @Override
    public void addRuleInstances ( final Digester digester ) {
        digester.addCallMethod ( "tag-plugins/tag-plugin", "addPlugin", 2 );
        digester.addCallParam ( "tag-plugins/tag-plugin/tag-class", 0 );
        digester.addCallParam ( "tag-plugins/tag-plugin/plugin-class", 1 );
    }
}
