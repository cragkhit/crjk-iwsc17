package org.apache.naming;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
public class ContextBindings {
    private static final Hashtable<Object, Context> objectBindings = new Hashtable<>();
    private static final Hashtable<Thread, Context> threadBindings = new Hashtable<>();
    private static final Hashtable<Thread, Object> threadObjectBindings = new Hashtable<>();
    private static final Hashtable<ClassLoader, Context> clBindings = new Hashtable<>();
    private static final Hashtable<ClassLoader, Object> clObjectBindings = new Hashtable<>();
    protected static final StringManager sm = StringManager.getManager ( ContextBindings.class );
    public static void bindContext ( Object obj, Context context ) {
        bindContext ( obj, context, null );
    }
    public static void bindContext ( Object obj, Context context, Object token ) {
        if ( ContextAccessController.checkSecurityToken ( obj, token ) ) {
            objectBindings.put ( obj, context );
        }
    }
    public static void unbindContext ( Object obj, Object token ) {
        if ( ContextAccessController.checkSecurityToken ( obj, token ) ) {
            objectBindings.remove ( obj );
        }
    }
    static Context getContext ( Object obj ) {
        return objectBindings.get ( obj );
    }
    public static void bindThread ( Object obj, Object token ) throws NamingException {
        if ( ContextAccessController.checkSecurityToken ( obj, token ) ) {
            Context context = objectBindings.get ( obj );
            if ( context == null ) {
                throw new NamingException (
                    sm.getString ( "contextBindings.unknownContext", obj ) );
            }
            threadBindings.put ( Thread.currentThread(), context );
            threadObjectBindings.put ( Thread.currentThread(), obj );
        }
    }
    public static void unbindThread ( Object obj, Object token ) {
        if ( ContextAccessController.checkSecurityToken ( obj, token ) ) {
            threadBindings.remove ( Thread.currentThread() );
            threadObjectBindings.remove ( Thread.currentThread() );
        }
    }
    public static Context getThread() throws NamingException {
        Context context = threadBindings.get ( Thread.currentThread() );
        if ( context == null ) {
            throw new NamingException
            ( sm.getString ( "contextBindings.noContextBoundToThread" ) );
        }
        return context;
    }
    static String getThreadName() throws NamingException {
        Object obj = threadObjectBindings.get ( Thread.currentThread() );
        if ( obj == null ) {
            throw new NamingException
            ( sm.getString ( "contextBindings.noContextBoundToThread" ) );
        }
        return obj.toString();
    }
    public static boolean isThreadBound() {
        return ( threadBindings.containsKey ( Thread.currentThread() ) );
    }
    public static void bindClassLoader ( Object obj, Object token,
                                         ClassLoader classLoader ) throws NamingException {
        if ( ContextAccessController.checkSecurityToken ( obj, token ) ) {
            Context context = objectBindings.get ( obj );
            if ( context == null ) {
                throw new NamingException
                ( sm.getString ( "contextBindings.unknownContext", obj ) );
            }
            clBindings.put ( classLoader, context );
            clObjectBindings.put ( classLoader, obj );
        }
    }
    public static void unbindClassLoader ( Object obj, Object token,
                                           ClassLoader classLoader ) {
        if ( ContextAccessController.checkSecurityToken ( obj, token ) ) {
            Object o = clObjectBindings.get ( classLoader );
            if ( o == null || !o.equals ( obj ) ) {
                return;
            }
            clBindings.remove ( classLoader );
            clObjectBindings.remove ( classLoader );
        }
    }
    public static Context getClassLoader() throws NamingException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Context context = null;
        do {
            context = clBindings.get ( cl );
            if ( context != null ) {
                return context;
            }
        } while ( ( cl = cl.getParent() ) != null );
        throw new NamingException ( sm.getString ( "contextBindings.noContextBoundToCL" ) );
    }
    static String getClassLoaderName() throws NamingException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Object obj = null;
        do {
            obj = clObjectBindings.get ( cl );
            if ( obj != null ) {
                return obj.toString();
            }
        } while ( ( cl = cl.getParent() ) != null );
        throw new NamingException ( sm.getString ( "contextBindings.noContextBoundToCL" ) );
    }
    public static boolean isClassLoaderBound() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        do {
            if ( clBindings.containsKey ( cl ) ) {
                return true;
            }
        } while ( ( cl = cl.getParent() ) != null );
        return false;
    }
}
