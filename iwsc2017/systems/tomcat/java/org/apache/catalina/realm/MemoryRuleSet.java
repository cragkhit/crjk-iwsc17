package org.apache.catalina.realm;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.Rule;
import org.apache.tomcat.util.digester.RuleSetBase;
import org.xml.sax.Attributes;
public class MemoryRuleSet extends RuleSetBase {
    protected final String prefix;
    public MemoryRuleSet() {
        this ( "tomcat-users/" );
    }
    public MemoryRuleSet ( String prefix ) {
        super();
        this.namespaceURI = null;
        this.prefix = prefix;
    }
    @Override
    public void addRuleInstances ( Digester digester ) {
        digester.addRule ( prefix + "user", new MemoryUserRule() );
    }
}
final class MemoryUserRule extends Rule {
    public MemoryUserRule() {
    }
    @Override
    public void begin ( String namespace, String name, Attributes attributes )
    throws Exception {
        String username = attributes.getValue ( "username" );
        if ( username == null ) {
            username = attributes.getValue ( "name" );
        }
        String password = attributes.getValue ( "password" );
        String roles = attributes.getValue ( "roles" );
        MemoryRealm realm =
            ( MemoryRealm ) digester.peek ( digester.getCount() - 1 );
        realm.addUser ( username, password, roles );
    }
}
