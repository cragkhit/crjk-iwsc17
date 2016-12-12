package org.apache.tomcat.util.digester;
public abstract class RuleSetBase implements RuleSet {
    protected String namespaceURI;
    public RuleSetBase() {
        this.namespaceURI = null;
    }
    @Override
    public String getNamespaceURI() {
        return this.namespaceURI;
    }
    @Override
    public abstract void addRuleInstances ( final Digester p0 );
}
