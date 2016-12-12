package org.apache.naming.factory;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import org.apache.naming.ResourceLinkRef;
import java.util.Hashtable;
import javax.naming.Name;
import java.util.HashMap;
import java.security.Permission;
import java.util.Map;
import javax.naming.Context;
import org.apache.naming.StringManager;
import javax.naming.spi.ObjectFactory;
public class ResourceLinkFactory implements ObjectFactory {
    private static final StringManager sm;
    private static Context globalContext;
    private static Map<ClassLoader, Map<String, String>> globalResourceRegistrations;
    public static void setGlobalContext ( final Context newGlobalContext ) {
        final SecurityManager sm = System.getSecurityManager();
        if ( sm != null ) {
            sm.checkPermission ( new RuntimePermission ( ResourceLinkFactory.class.getName() + ".setGlobalContext" ) );
        }
        ResourceLinkFactory.globalContext = newGlobalContext;
    }
    public static void registerGlobalResourceAccess ( final Context globalContext, final String localName, final String globalName ) {
        validateGlobalContext ( globalContext );
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Map<String, String> registrations = ResourceLinkFactory.globalResourceRegistrations.get ( cl );
        if ( registrations == null ) {
            registrations = new HashMap<String, String>();
            ResourceLinkFactory.globalResourceRegistrations.put ( cl, registrations );
        }
        registrations.put ( localName, globalName );
    }
    public static void deregisterGlobalResourceAccess ( final Context globalContext, final String localName ) {
        validateGlobalContext ( globalContext );
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final Map<String, String> registrations = ResourceLinkFactory.globalResourceRegistrations.get ( cl );
        if ( registrations != null ) {
            registrations.remove ( localName );
        }
    }
    public static void deregisterGlobalResourceAccess ( final Context globalContext ) {
        validateGlobalContext ( globalContext );
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        ResourceLinkFactory.globalResourceRegistrations.remove ( cl );
    }
    private static void validateGlobalContext ( final Context globalContext ) {
        if ( ResourceLinkFactory.globalContext != null && ResourceLinkFactory.globalContext != globalContext ) {
            throw new SecurityException ( "Caller provided invalid global context" );
        }
    }
    private static boolean validateGlobalResourceAccess ( final String globalName ) {
        for ( ClassLoader cl = Thread.currentThread().getContextClassLoader(); cl != null; cl = cl.getParent() ) {
            final Map<String, String> registrations = ResourceLinkFactory.globalResourceRegistrations.get ( cl );
            if ( registrations != null && registrations.containsValue ( globalName ) ) {
                return true;
            }
        }
        return false;
    }
    @Override
    public Object getObjectInstance ( final Object obj, final Name name, final Context nameCtx, final Hashtable<?, ?> environment ) throws NamingException {
        if ( ! ( obj instanceof ResourceLinkRef ) ) {
            return null;
        }
        final Reference ref = ( Reference ) obj;
        String globalName = null;
        final RefAddr refAddr = ref.get ( "globalName" );
        if ( refAddr == null ) {
            return null;
        }
        globalName = refAddr.getContent().toString();
        if ( !validateGlobalResourceAccess ( globalName ) ) {
            return null;
        }
        Object result = null;
        result = ResourceLinkFactory.globalContext.lookup ( globalName );
        final String expectedClassName = ref.getClassName();
        if ( expectedClassName == null ) {
            throw new IllegalArgumentException ( ResourceLinkFactory.sm.getString ( "resourceLinkFactory.nullType", name, globalName ) );
        }
        try {
            final Class<?> expectedClazz = Class.forName ( expectedClassName, true, Thread.currentThread().getContextClassLoader() );
            if ( !expectedClazz.isAssignableFrom ( result.getClass() ) ) {
                throw new IllegalArgumentException ( ResourceLinkFactory.sm.getString ( "resourceLinkFactory.wrongType", name, globalName, expectedClassName, result.getClass().getName() ) );
            }
        } catch ( ClassNotFoundException e ) {
            throw new IllegalArgumentException ( ResourceLinkFactory.sm.getString ( "resourceLinkFactory.unknownType", name, globalName, expectedClassName ), e );
        }
        return result;
    }
    static {
        sm = StringManager.getManager ( ResourceLinkFactory.class );
        ResourceLinkFactory.globalContext = null;
        ResourceLinkFactory.globalResourceRegistrations = new ConcurrentHashMap<ClassLoader, Map<String, String>>();
    }
}
