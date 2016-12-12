package org.apache.catalina.startup;
import org.apache.tomcat.util.digester.RuleSet;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSetBase;
public class RealmRuleSet extends RuleSetBase {
    private static final int MAX_NESTED_REALM_LEVELS;
    protected final String prefix;
    public RealmRuleSet() {
        this ( "" );
    }
    public RealmRuleSet ( final String prefix ) {
        this.namespaceURI = null;
        this.prefix = prefix;
    }
    @Override
    public void addRuleInstances ( final Digester digester ) {
        final StringBuilder pattern = new StringBuilder ( this.prefix );
        for ( int i = 0; i < RealmRuleSet.MAX_NESTED_REALM_LEVELS; ++i ) {
            if ( i > 0 ) {
                pattern.append ( '/' );
            }
            pattern.append ( "Realm" );
            this.addRuleInstances ( digester, pattern.toString(), ( i == 0 ) ? "setRealm" : "addRealm" );
        }
    }
    private void addRuleInstances ( final Digester digester, final String pattern, final String methodName ) {
        digester.addObjectCreate ( pattern, null, "className" );
        digester.addSetProperties ( pattern );
        digester.addSetNext ( pattern, methodName, "org.apache.catalina.Realm" );
        digester.addRuleSet ( new CredentialHandlerRuleSet ( pattern + "/" ) );
    }
    static {
        MAX_NESTED_REALM_LEVELS = Integer.getInteger ( "org.apache.catalina.startup.RealmRuleSet.MAX_NESTED_REALM_LEVELS", 3 );
    }
}
