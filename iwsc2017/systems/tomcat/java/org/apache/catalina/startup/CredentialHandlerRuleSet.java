package org.apache.catalina.startup;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSetBase;
public class CredentialHandlerRuleSet extends RuleSetBase {
    private static final int MAX_NESTED_LEVELS = Integer.getInteger (
                "org.apache.catalina.startup.CredentialHandlerRuleSet.MAX_NESTED_LEVELS",
                3 ).intValue();
    protected final String prefix;
    public CredentialHandlerRuleSet() {
        this ( "" );
    }
    public CredentialHandlerRuleSet ( String prefix ) {
        this.namespaceURI = null;
        this.prefix = prefix;
    }
    @Override
    public void addRuleInstances ( Digester digester ) {
        StringBuilder pattern = new StringBuilder ( prefix );
        for ( int i = 0; i < MAX_NESTED_LEVELS; i++ ) {
            if ( i > 0 ) {
                pattern.append ( '/' );
            }
            pattern.append ( "CredentialHandler" );
            addRuleInstances ( digester, pattern.toString(), i == 0 ? "setCredentialHandler"
                               : "addCredentialHandler" );
        }
    }
    private void addRuleInstances ( Digester digester, String pattern, String methodName ) {
        digester.addObjectCreate ( pattern, null  ,
                                   "className" );
        digester.addSetProperties ( pattern );
        digester.addSetNext ( pattern, methodName, "org.apache.catalina.CredentialHandler" );
    }
}
