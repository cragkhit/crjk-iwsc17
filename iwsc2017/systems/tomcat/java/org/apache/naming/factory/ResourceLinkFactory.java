package org.apache.naming.factory;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import org.apache.naming.ResourceLinkRef;
import org.apache.naming.StringManager;
public class ResourceLinkFactory implements ObjectFactory {
    private static final StringManager sm = StringManager.getManager ( ResourceLinkFactory.class );
    private static Context globalContext = null;
    private static Map<ClassLoader, Map<String, String>> globalResourceRegistrations =
        new ConcurrentHashMap<>();
    public static void setGlobalContext ( Context newGlobalContext ) {
        SecurityManager sm = System.getSecurityManager();
        if ( sm != null ) {
            sm.checkPermission ( new RuntimePermission (
                                     ResourceLinkFactory.class.getName() + ".setGlobalContext" ) );
        }
        globalContext = newGlobalContext;
    }
    public static void registerGlobalResourceAccess ( Context globalContext, String localName,
            String globalName ) {
        validateGlobalContext ( globalContext );
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Map<String, String> registrations = globalResourceRegistrations.get ( cl );
        if ( registrations == null ) {
            registrations = new HashMap<>();
            globalResourceRegistrations.put ( cl, registrations );
        }
        registrations.put ( localName, globalName );
    }
    public static void deregisterGlobalResourceAccess ( Context globalContext, String localName ) {
        validateGlobalContext ( globalContext );
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Map<String, String> registrations = globalResourceRegistrations.get ( cl );
        if ( registrations != null ) {
            registrations.remove ( localName );
        }
    }
    public static void deregisterGlobalResourceAccess ( Context globalContext ) {
        validateGlobalContext ( globalContext );
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        globalResourceRegistrations.remove ( cl );
    }
    private static void validateGlobalContext ( Context globalContext ) {
        if ( ResourceLinkFactory.globalContext != null &&
                ResourceLinkFactory.globalContext != globalContext ) {
            throw new SecurityException ( "Caller provided invalid global context" );
        }
    }
    private static boolean validateGlobalResourceAccess ( String globalName ) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        while ( cl != null ) {
            Map<String, String> registrations = globalResourceRegistrations.get ( cl );
            if ( registrations != null && registrations.containsValue ( globalName ) ) {
                return true;
            }
            cl = cl.getParent();
        }
        return false;
    }
    @Override
    public Object getObjectInstance ( Object obj, Name name, Context nameCtx,
                                      Hashtable<?, ?> environment ) throws NamingException {
        if ( ! ( obj instanceof ResourceLinkRef ) ) {
            return null;
        }
        Reference ref = ( Reference ) obj;
        String globalName = null;
        RefAddr refAddr = ref.get ( ResourceLinkRef.GLOBALNAME );
        if ( refAddr != null ) {
            globalName = refAddr.getContent().toString();
            if ( !validateGlobalResourceAccess ( globalName ) ) {
                return null;
            }
            Object result = null;
            result = globalContext.lookup ( globalName );
            String expectedClassName = ref.getClassName();
            if ( expectedClassName == null ) {
                throw new IllegalArgumentException (
                    sm.getString ( "resourceLinkFactory.nullType", name, globalName ) );
            }
            try {
                Class<?> expectedClazz = Class.forName (
                                             expectedClassName, true, Thread.currentThread().getContextClassLoader() );
                if ( !expectedClazz.isAssignableFrom ( result.getClass() ) ) {
                    throw new IllegalArgumentException ( sm.getString ( "resourceLinkFactory.wrongType",
                                                         name, globalName, expectedClassName, result.getClass().getName() ) );
                }
            } catch ( ClassNotFoundException e ) {
                throw new IllegalArgumentException ( sm.getString ( "resourceLinkFactory.unknownType",
                                                     name, globalName, expectedClassName ), e );
            }
            return result;
        }
        return null;
    }
}
