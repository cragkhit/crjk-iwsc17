package org.apache.catalina.startup;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSetBase;
public class RealmRuleSet extends RuleSetBase {
    private static final int MAX_NESTED_REALM_LEVELS = Integer.getInteger (
                "org.apache.catalina.startup.RealmRuleSet.MAX_NESTED_REALM_LEVELS",
                3 ).intValue();
    protected final String prefix;
    public RealmRuleSet() {
        this ( "" );
    }
    public RealmRuleSet ( String prefix ) {
        this.namespaceURI = null;
        this.prefix = prefix;
    }
    @Override
    public void addRuleInstances ( Digester digester ) {
        StringBuilder pattern = new StringBuilder ( prefix );
        for ( int i = 0; i < MAX_NESTED_REALM_LEVELS; i++ ) {
            if ( i > 0 ) {
                pattern.append ( '/' );
            }
            pattern.append ( "Realm" );
            addRuleInstances ( digester, pattern.toString(), i == 0 ? "setRealm" : "addRealm" );
        }
    }
    private void addRuleInstances ( Digester digester, String pattern, String methodName ) {
        digester.addObjectCreate ( pattern, null  ,
                                   "className" );
        digester.addSetProperties ( pattern );
        digester.addSetNext ( pattern, methodName, "org.apache.catalina.Realm" );
        digester.addRuleSet ( new CredentialHandlerRuleSet ( pattern + "/" ) );
    }
}
