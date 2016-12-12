package org.apache.catalina.startup;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSetBase;
public class HostRuleSet extends RuleSetBase {
    protected final String prefix;
    public HostRuleSet() {
        this ( "" );
    }
    public HostRuleSet ( String prefix ) {
        this.namespaceURI = null;
        this.prefix = prefix;
    }
    @Override
    public void addRuleInstances ( Digester digester ) {
        digester.addObjectCreate ( prefix + "Host",
                                   "org.apache.catalina.core.StandardHost",
                                   "className" );
        digester.addSetProperties ( prefix + "Host" );
        digester.addRule ( prefix + "Host",
                           new CopyParentClassLoaderRule() );
        digester.addRule ( prefix + "Host",
                           new LifecycleListenerRule
                           ( "org.apache.catalina.startup.HostConfig",
                             "hostConfigClass" ) );
        digester.addSetNext ( prefix + "Host",
                              "addChild",
                              "org.apache.catalina.Container" );
        digester.addCallMethod ( prefix + "Host/Alias",
                                 "addAlias", 0 );
        digester.addObjectCreate ( prefix + "Host/Cluster",
                                   null,
                                   "className" );
        digester.addSetProperties ( prefix + "Host/Cluster" );
        digester.addSetNext ( prefix + "Host/Cluster",
                              "setCluster",
                              "org.apache.catalina.Cluster" );
        digester.addObjectCreate ( prefix + "Host/Listener",
                                   null,
                                   "className" );
        digester.addSetProperties ( prefix + "Host/Listener" );
        digester.addSetNext ( prefix + "Host/Listener",
                              "addLifecycleListener",
                              "org.apache.catalina.LifecycleListener" );
        digester.addRuleSet ( new RealmRuleSet ( prefix + "Host/" ) );
        digester.addObjectCreate ( prefix + "Host/Valve",
                                   null,
                                   "className" );
        digester.addSetProperties ( prefix + "Host/Valve" );
        digester.addSetNext ( prefix + "Host/Valve",
                              "addValve",
                              "org.apache.catalina.Valve" );
    }
}
