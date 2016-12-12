package org.apache.catalina.startup;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSetBase;
public class EngineRuleSet extends RuleSetBase {
    protected final String prefix;
    public EngineRuleSet() {
        this ( "" );
    }
    public EngineRuleSet ( String prefix ) {
        this.namespaceURI = null;
        this.prefix = prefix;
    }
    @Override
    public void addRuleInstances ( Digester digester ) {
        digester.addObjectCreate ( prefix + "Engine",
                                   "org.apache.catalina.core.StandardEngine",
                                   "className" );
        digester.addSetProperties ( prefix + "Engine" );
        digester.addRule ( prefix + "Engine",
                           new LifecycleListenerRule
                           ( "org.apache.catalina.startup.EngineConfig",
                             "engineConfigClass" ) );
        digester.addSetNext ( prefix + "Engine",
                              "setContainer",
                              "org.apache.catalina.Engine" );
        digester.addObjectCreate ( prefix + "Engine/Cluster",
                                   null,
                                   "className" );
        digester.addSetProperties ( prefix + "Engine/Cluster" );
        digester.addSetNext ( prefix + "Engine/Cluster",
                              "setCluster",
                              "org.apache.catalina.Cluster" );
        digester.addObjectCreate ( prefix + "Engine/Listener",
                                   null,
                                   "className" );
        digester.addSetProperties ( prefix + "Engine/Listener" );
        digester.addSetNext ( prefix + "Engine/Listener",
                              "addLifecycleListener",
                              "org.apache.catalina.LifecycleListener" );
        digester.addRuleSet ( new RealmRuleSet ( prefix + "Engine/" ) );
        digester.addObjectCreate ( prefix + "Engine/Valve",
                                   null,
                                   "className" );
        digester.addSetProperties ( prefix + "Engine/Valve" );
        digester.addSetNext ( prefix + "Engine/Valve",
                              "addValve",
                              "org.apache.catalina.Valve" );
    }
}
