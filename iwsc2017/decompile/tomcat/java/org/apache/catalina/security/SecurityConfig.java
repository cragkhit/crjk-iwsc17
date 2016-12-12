package org.apache.catalina.security;
import org.apache.juli.logging.LogFactory;
import java.security.Security;
import org.apache.catalina.startup.CatalinaProperties;
import org.apache.juli.logging.Log;
public final class SecurityConfig {
    private static SecurityConfig singleton;
    private static final Log log;
    private static final String PACKAGE_ACCESS = "sun.,org.apache.catalina.,org.apache.jasper.,org.apache.coyote.,org.apache.tomcat.";
    private static final String PACKAGE_DEFINITION = "java.,sun.,org.apache.catalina.,org.apache.coyote.,org.apache.tomcat.,org.apache.jasper.";
    private final String packageDefinition;
    private final String packageAccess;
    private SecurityConfig() {
        String definition = null;
        String access = null;
        try {
            definition = CatalinaProperties.getProperty ( "package.definition" );
            access = CatalinaProperties.getProperty ( "package.access" );
        } catch ( Exception ex ) {
            if ( SecurityConfig.log.isDebugEnabled() ) {
                SecurityConfig.log.debug ( "Unable to load properties using CatalinaProperties", ex );
            }
        } finally {
            this.packageDefinition = definition;
            this.packageAccess = access;
        }
    }
    public static SecurityConfig newInstance() {
        if ( SecurityConfig.singleton == null ) {
            SecurityConfig.singleton = new SecurityConfig();
        }
        return SecurityConfig.singleton;
    }
    public void setPackageAccess() {
        if ( this.packageAccess == null ) {
            this.setSecurityProperty ( "package.access", "sun.,org.apache.catalina.,org.apache.jasper.,org.apache.coyote.,org.apache.tomcat." );
        } else {
            this.setSecurityProperty ( "package.access", this.packageAccess );
        }
    }
    public void setPackageDefinition() {
        if ( this.packageDefinition == null ) {
            this.setSecurityProperty ( "package.definition", "java.,sun.,org.apache.catalina.,org.apache.coyote.,org.apache.tomcat.,org.apache.jasper." );
        } else {
            this.setSecurityProperty ( "package.definition", this.packageDefinition );
        }
    }
    private final void setSecurityProperty ( final String properties, final String packageList ) {
        if ( System.getSecurityManager() != null ) {
            String definition = Security.getProperty ( properties );
            if ( definition != null && definition.length() > 0 ) {
                if ( packageList.length() > 0 ) {
                    definition = definition + ',' + packageList;
                }
            } else {
                definition = packageList;
            }
            Security.setProperty ( properties, definition );
        }
    }
    static {
        SecurityConfig.singleton = null;
        log = LogFactory.getLog ( SecurityConfig.class );
    }
}
