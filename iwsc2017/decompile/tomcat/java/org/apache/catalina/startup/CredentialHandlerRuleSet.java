package org.apache.catalina.startup;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSetBase;
public class CredentialHandlerRuleSet extends RuleSetBase {
    private static final int MAX_NESTED_LEVELS;
    protected final String prefix;
    public CredentialHandlerRuleSet() {
        this ( "" );
    }
    public CredentialHandlerRuleSet ( final String prefix ) {
        this.namespaceURI = null;
        this.prefix = prefix;
    }
    @Override
    public void addRuleInstances ( final Digester digester ) {
        final StringBuilder pattern = new StringBuilder ( this.prefix );
        for ( int i = 0; i < CredentialHandlerRuleSet.MAX_NESTED_LEVELS; ++i ) {
            if ( i > 0 ) {
                pattern.append ( '/' );
            }
            pattern.append ( "CredentialHandler" );
            this.addRuleInstances ( digester, pattern.toString(), ( i == 0 ) ? "setCredentialHandler" : "addCredentialHandler" );
        }
    }
    private void addRuleInstances ( final Digester digester, final String pattern, final String methodName ) {
        digester.addObjectCreate ( pattern, null, "className" );
        digester.addSetProperties ( pattern );
        digester.addSetNext ( pattern, methodName, "org.apache.catalina.CredentialHandler" );
    }
    static {
        MAX_NESTED_LEVELS = Integer.getInteger ( "org.apache.catalina.startup.CredentialHandlerRuleSet.MAX_NESTED_LEVELS", 3 );
    }
}
