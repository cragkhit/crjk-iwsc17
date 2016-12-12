package org.apache.catalina.startup;
import org.apache.tomcat.util.digester.RuleSet;
import org.apache.tomcat.util.digester.Rule;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSetBase;
public class HostRuleSet extends RuleSetBase {
    protected final String prefix;
    public HostRuleSet() {
        this ( "" );
    }
    public HostRuleSet ( final String prefix ) {
        this.namespaceURI = null;
        this.prefix = prefix;
    }
    @Override
    public void addRuleInstances ( final Digester digester ) {
        digester.addObjectCreate ( this.prefix + "Host", "org.apache.catalina.core.StandardHost", "className" );
        digester.addSetProperties ( this.prefix + "Host" );
        digester.addRule ( this.prefix + "Host", new CopyParentClassLoaderRule() );
        digester.addRule ( this.prefix + "Host", new LifecycleListenerRule ( "org.apache.catalina.startup.HostConfig", "hostConfigClass" ) );
        digester.addSetNext ( this.prefix + "Host", "addChild", "org.apache.catalina.Container" );
        digester.addCallMethod ( this.prefix + "Host/Alias", "addAlias", 0 );
        digester.addObjectCreate ( this.prefix + "Host/Cluster", null, "className" );
        digester.addSetProperties ( this.prefix + "Host/Cluster" );
        digester.addSetNext ( this.prefix + "Host/Cluster", "setCluster", "org.apache.catalina.Cluster" );
        digester.addObjectCreate ( this.prefix + "Host/Listener", null, "className" );
        digester.addSetProperties ( this.prefix + "Host/Listener" );
        digester.addSetNext ( this.prefix + "Host/Listener", "addLifecycleListener", "org.apache.catalina.LifecycleListener" );
        digester.addRuleSet ( new RealmRuleSet ( this.prefix + "Host/" ) );
        digester.addObjectCreate ( this.prefix + "Host/Valve", null, "className" );
        digester.addSetProperties ( this.prefix + "Host/Valve" );
        digester.addSetNext ( this.prefix + "Host/Valve", "addValve", "org.apache.catalina.Valve" );
    }
}
