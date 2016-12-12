package org.apache.catalina.startup;
import org.apache.tomcat.util.digester.Rule;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSetBase;
public class NamingRuleSet extends RuleSetBase {
    protected final String prefix;
    public NamingRuleSet() {
        this ( "" );
    }
    public NamingRuleSet ( final String prefix ) {
        this.namespaceURI = null;
        this.prefix = prefix;
    }
    @Override
    public void addRuleInstances ( final Digester digester ) {
        digester.addObjectCreate ( this.prefix + "Ejb", "org.apache.tomcat.util.descriptor.web.ContextEjb" );
        digester.addRule ( this.prefix + "Ejb", new SetAllPropertiesRule() );
        digester.addRule ( this.prefix + "Ejb", new SetNextNamingRule ( "addEjb", "org.apache.tomcat.util.descriptor.web.ContextEjb" ) );
        digester.addObjectCreate ( this.prefix + "Environment", "org.apache.tomcat.util.descriptor.web.ContextEnvironment" );
        digester.addSetProperties ( this.prefix + "Environment" );
        digester.addRule ( this.prefix + "Environment", new SetNextNamingRule ( "addEnvironment", "org.apache.tomcat.util.descriptor.web.ContextEnvironment" ) );
        digester.addObjectCreate ( this.prefix + "LocalEjb", "org.apache.tomcat.util.descriptor.web.ContextLocalEjb" );
        digester.addRule ( this.prefix + "LocalEjb", new SetAllPropertiesRule() );
        digester.addRule ( this.prefix + "LocalEjb", new SetNextNamingRule ( "addLocalEjb", "org.apache.tomcat.util.descriptor.web.ContextLocalEjb" ) );
        digester.addObjectCreate ( this.prefix + "Resource", "org.apache.tomcat.util.descriptor.web.ContextResource" );
        digester.addRule ( this.prefix + "Resource", new SetAllPropertiesRule() );
        digester.addRule ( this.prefix + "Resource", new SetNextNamingRule ( "addResource", "org.apache.tomcat.util.descriptor.web.ContextResource" ) );
        digester.addObjectCreate ( this.prefix + "ResourceEnvRef", "org.apache.tomcat.util.descriptor.web.ContextResourceEnvRef" );
        digester.addRule ( this.prefix + "ResourceEnvRef", new SetAllPropertiesRule() );
        digester.addRule ( this.prefix + "ResourceEnvRef", new SetNextNamingRule ( "addResourceEnvRef", "org.apache.tomcat.util.descriptor.web.ContextResourceEnvRef" ) );
        digester.addObjectCreate ( this.prefix + "ServiceRef", "org.apache.tomcat.util.descriptor.web.ContextService" );
        digester.addRule ( this.prefix + "ServiceRef", new SetAllPropertiesRule() );
        digester.addRule ( this.prefix + "ServiceRef", new SetNextNamingRule ( "addService", "org.apache.tomcat.util.descriptor.web.ContextService" ) );
        digester.addObjectCreate ( this.prefix + "Transaction", "org.apache.tomcat.util.descriptor.web.ContextTransaction" );
        digester.addRule ( this.prefix + "Transaction", new SetAllPropertiesRule() );
        digester.addRule ( this.prefix + "Transaction", new SetNextNamingRule ( "setTransaction", "org.apache.tomcat.util.descriptor.web.ContextTransaction" ) );
    }
}
