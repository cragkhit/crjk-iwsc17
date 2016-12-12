package org.apache.catalina.session;
import java.util.Collections;
import org.apache.catalina.valves.CrawlerSessionManagerValve;
import java.util.HashSet;
import java.util.Set;
public class Constants {
    public static final Set<String> excludedAttributeNames;
    static {
        final Set<String> names = new HashSet<String>();
        names.add ( "javax.security.auth.subject" );
        names.add ( "org.apache.catalina.realm.GSS_CREDENTIAL" );
        names.add ( CrawlerSessionManagerValve.class.getName() );
        excludedAttributeNames = Collections.unmodifiableSet ( ( Set<? extends String> ) names );
    }
}
