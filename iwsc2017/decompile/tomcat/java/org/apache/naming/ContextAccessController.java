package org.apache.naming;
import java.security.Permission;
import java.util.Hashtable;
public class ContextAccessController {
    private static final Hashtable<Object, Object> readOnlyContexts;
    private static final Hashtable<Object, Object> securityTokens;
    public static void setSecurityToken ( final Object name, final Object token ) {
        final SecurityManager sm = System.getSecurityManager();
        if ( sm != null ) {
            sm.checkPermission ( new RuntimePermission ( ContextAccessController.class.getName() + ".setSecurityToken" ) );
        }
        if ( !ContextAccessController.securityTokens.containsKey ( name ) && token != null ) {
            ContextAccessController.securityTokens.put ( name, token );
        }
    }
    public static void unsetSecurityToken ( final Object name, final Object token ) {
        if ( checkSecurityToken ( name, token ) ) {
            ContextAccessController.securityTokens.remove ( name );
        }
    }
    public static boolean checkSecurityToken ( final Object name, final Object token ) {
        final Object refToken = ContextAccessController.securityTokens.get ( name );
        return refToken == null || refToken.equals ( token );
    }
    public static void setWritable ( final Object name, final Object token ) {
        if ( checkSecurityToken ( name, token ) ) {
            ContextAccessController.readOnlyContexts.remove ( name );
        }
    }
    public static void setReadOnly ( final Object name ) {
        ContextAccessController.readOnlyContexts.put ( name, name );
    }
    public static boolean isWritable ( final Object name ) {
        return !ContextAccessController.readOnlyContexts.containsKey ( name );
    }
    static {
        readOnlyContexts = new Hashtable<Object, Object>();
        securityTokens = new Hashtable<Object, Object>();
    }
}
