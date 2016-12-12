package javax.security.auth.message.config;
import java.security.Security;
import java.security.PrivilegedAction;
import java.util.Map;
import java.security.PrivilegedActionException;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.Permission;
import java.security.SecurityPermission;
public abstract class AuthConfigFactory {
    public static final String DEFAULT_FACTORY_SECURITY_PROPERTY = "authconfigprovider.factory";
    public static final String GET_FACTORY_PERMISSION_NAME = "getProperty.authconfigprovider.factory";
    public static final String SET_FACTORY_PERMISSION_NAME = "setProperty.authconfigprovider.factory";
    public static final String PROVIDER_REGISTRATION_PERMISSION_NAME = "setProperty.authconfigfactory.provider";
    public static final SecurityPermission getFactorySecurityPermission;
    public static final SecurityPermission setFactorySecurityPermission;
    public static final SecurityPermission providerRegistrationSecurityPermission;
    private static final String DEFAULT_JASPI_AUTHCONFIGFACTORYIMPL = "org.apache.catalina.authenticator.jaspic.AuthConfigFactoryImpl";
    private static AuthConfigFactory factory;
    public static synchronized AuthConfigFactory getFactory() {
        checkPermission ( AuthConfigFactory.getFactorySecurityPermission );
        if ( AuthConfigFactory.factory != null ) {
            return AuthConfigFactory.factory;
        }
        final String className = getFactoryClassName();
        try {
            AuthConfigFactory.factory = AccessController.doPrivileged ( ( PrivilegedExceptionAction<AuthConfigFactory> ) new PrivilegedExceptionAction<AuthConfigFactory>() {
                @Override
                public AuthConfigFactory run() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
                    final Class<?> clazz = Class.forName ( className );
                    return ( AuthConfigFactory ) clazz.newInstance();
                }
            } );
        } catch ( PrivilegedActionException e ) {
            final Exception inner = e.getException();
            if ( inner instanceof InstantiationException ) {
                throw ( SecurityException ) new SecurityException ( "AuthConfigFactory error:" + inner.getCause().getMessage() ).initCause ( inner.getCause() );
            }
            throw ( SecurityException ) new SecurityException ( "AuthConfigFactory error: " + inner ).initCause ( inner );
        }
        return AuthConfigFactory.factory;
    }
    public static synchronized void setFactory ( final AuthConfigFactory factory ) {
        checkPermission ( AuthConfigFactory.setFactorySecurityPermission );
        AuthConfigFactory.factory = factory;
    }
    public abstract AuthConfigProvider getConfigProvider ( final String p0, final String p1, final RegistrationListener p2 );
    public abstract String registerConfigProvider ( final String p0, final Map p1, final String p2, final String p3, final String p4 );
    public abstract String registerConfigProvider ( final AuthConfigProvider p0, final String p1, final String p2, final String p3 );
    public abstract boolean removeRegistration ( final String p0 );
    public abstract String[] detachListener ( final RegistrationListener p0, final String p1, final String p2 );
    public abstract String[] getRegistrationIDs ( final AuthConfigProvider p0 );
    public abstract RegistrationContext getRegistrationContext ( final String p0 );
    public abstract void refresh();
    private static void checkPermission ( final Permission permission ) {
        final SecurityManager securityManager = System.getSecurityManager();
        if ( securityManager != null ) {
            securityManager.checkPermission ( permission );
        }
    }
    private static String getFactoryClassName() {
        final String className = AccessController.doPrivileged ( ( PrivilegedAction<String> ) new PrivilegedAction<String>() {
            @Override
            public String run() {
                return Security.getProperty ( "authconfigprovider.factory" );
            }
        } );
        if ( className != null ) {
            return className;
        }
        return "org.apache.catalina.authenticator.jaspic.AuthConfigFactoryImpl";
    }
    static {
        getFactorySecurityPermission = new SecurityPermission ( "getProperty.authconfigprovider.factory" );
        setFactorySecurityPermission = new SecurityPermission ( "setProperty.authconfigprovider.factory" );
        providerRegistrationSecurityPermission = new SecurityPermission ( "setProperty.authconfigfactory.provider" );
    }
    public interface RegistrationContext {
        String getMessageLayer();
        String getAppContext();
        String getDescription();
        boolean isPersistent();
    }
}
