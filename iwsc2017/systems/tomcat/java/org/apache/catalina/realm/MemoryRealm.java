package org.apache.catalina.realm;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.catalina.LifecycleException;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.file.ConfigFileLoader;
public class MemoryRealm  extends RealmBase {
    private static final Log log = LogFactory.getLog ( MemoryRealm.class );
    private static Digester digester = null;
    protected static final String name = "MemoryRealm";
    private String pathname = "conf/tomcat-users.xml";
    private final Map<String, GenericPrincipal> principals = new HashMap<>();
    public String getPathname() {
        return pathname;
    }
    public void setPathname ( String pathname ) {
        this.pathname = pathname;
    }
    @Override
    public Principal authenticate ( String username, String credentials ) {
        if ( username == null || credentials == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "memoryRealm.authenticateFailure", username ) );
            }
            return null;
        }
        GenericPrincipal principal = principals.get ( username );
        if ( principal == null || principal.getPassword() == null ) {
            getCredentialHandler().mutate ( credentials );
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "memoryRealm.authenticateFailure", username ) );
            }
            return null;
        }
        boolean validated = getCredentialHandler().matches ( credentials, principal.getPassword() );
        if ( validated ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "memoryRealm.authenticateSuccess", username ) );
            }
            return principal;
        } else {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "memoryRealm.authenticateFailure", username ) );
            }
            return null;
        }
    }
    void addUser ( String username, String password, String roles ) {
        ArrayList<String> list = new ArrayList<>();
        roles += ",";
        while ( true ) {
            int comma = roles.indexOf ( ',' );
            if ( comma < 0 ) {
                break;
            }
            String role = roles.substring ( 0, comma ).trim();
            list.add ( role );
            roles = roles.substring ( comma + 1 );
        }
        GenericPrincipal principal =
            new GenericPrincipal ( username, password, list );
        principals.put ( username, principal );
    }
    protected synchronized Digester getDigester() {
        if ( digester == null ) {
            digester = new Digester();
            digester.setValidating ( false );
            try {
                digester.setFeature (
                    "http://apache.org/xml/features/allow-java-encodings",
                    true );
            } catch ( Exception e ) {
                log.warn ( sm.getString ( "memoryRealm.xmlFeatureEncoding" ), e );
            }
            digester.addRuleSet ( new MemoryRuleSet() );
        }
        return ( digester );
    }
    @Override
    protected String getName() {
        return ( name );
    }
    @Override
    protected String getPassword ( String username ) {
        GenericPrincipal principal = principals.get ( username );
        if ( principal != null ) {
            return ( principal.getPassword() );
        } else {
            return ( null );
        }
    }
    @Override
    protected Principal getPrincipal ( String username ) {
        return principals.get ( username );
    }
    @Override
    protected void startInternal() throws LifecycleException {
        String pathName = getPathname();
        try ( InputStream is = ConfigFileLoader.getInputStream ( pathName ) ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "memoryRealm.loadPath", pathName ) );
            }
            Digester digester = getDigester();
            try {
                synchronized ( digester ) {
                    digester.push ( this );
                    digester.parse ( is );
                }
            } catch ( Exception e ) {
                throw new LifecycleException ( sm.getString ( "memoryRealm.readXml" ), e );
            } finally {
                digester.reset();
            }
        } catch ( IOException ioe ) {
            throw new LifecycleException ( sm.getString ( "memoryRealm.loadExist", pathName ), ioe );
        }
        super.startInternal();
    }
}
