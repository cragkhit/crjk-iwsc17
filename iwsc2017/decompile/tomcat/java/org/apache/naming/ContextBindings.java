package org.apache.naming;
import javax.naming.NamingException;
import javax.naming.Context;
import java.util.Hashtable;
public class ContextBindings {
    private static final Hashtable<Object, Context> objectBindings;
    private static final Hashtable<Thread, Context> threadBindings;
    private static final Hashtable<Thread, Object> threadObjectBindings;
    private static final Hashtable<ClassLoader, Context> clBindings;
    private static final Hashtable<ClassLoader, Object> clObjectBindings;
    protected static final StringManager sm;
    public static void bindContext ( final Object obj, final Context context ) {
        bindContext ( obj, context, null );
    }
    public static void bindContext ( final Object obj, final Context context, final Object token ) {
        if ( ContextAccessController.checkSecurityToken ( obj, token ) ) {
            ContextBindings.objectBindings.put ( obj, context );
        }
    }
    public static void unbindContext ( final Object obj, final Object token ) {
        if ( ContextAccessController.checkSecurityToken ( obj, token ) ) {
            ContextBindings.objectBindings.remove ( obj );
        }
    }
    static Context getContext ( final Object obj ) {
        return ContextBindings.objectBindings.get ( obj );
    }
    public static void bindThread ( final Object obj, final Object token ) throws NamingException {
        if ( ContextAccessController.checkSecurityToken ( obj, token ) ) {
            final Context context = ContextBindings.objectBindings.get ( obj );
            if ( context == null ) {
                throw new NamingException ( ContextBindings.sm.getString ( "contextBindings.unknownContext", obj ) );
            }
            ContextBindings.threadBindings.put ( Thread.currentThread(), context );
            ContextBindings.threadObjectBindings.put ( Thread.currentThread(), obj );
        }
    }
    public static void unbindThread ( final Object obj, final Object token ) {
        if ( ContextAccessController.checkSecurityToken ( obj, token ) ) {
            ContextBindings.threadBindings.remove ( Thread.currentThread() );
            ContextBindings.threadObjectBindings.remove ( Thread.currentThread() );
        }
    }
    public static Context getThread() throws NamingException {
        final Context context = ContextBindings.threadBindings.get ( Thread.currentThread() );
        if ( context == null ) {
            throw new NamingException ( ContextBindings.sm.getString ( "contextBindings.noContextBoundToThread" ) );
        }
        return context;
    }
    static String getThreadName() throws NamingException {
        final Object obj = ContextBindings.threadObjectBindings.get ( Thread.currentThread() );
        if ( obj == null ) {
            throw new NamingException ( ContextBindings.sm.getString ( "contextBindings.noContextBoundToThread" ) );
        }
        return obj.toString();
    }
    public static boolean isThreadBound() {
        return ContextBindings.threadBindings.containsKey ( Thread.currentThread() );
    }
    public static void bindClassLoader ( final Object obj, final Object token, final ClassLoader classLoader ) throws NamingException {
        if ( ContextAccessController.checkSecurityToken ( obj, token ) ) {
            final Context context = ContextBindings.objectBindings.get ( obj );
            if ( context == null ) {
                throw new NamingException ( ContextBindings.sm.getString ( "contextBindings.unknownContext", obj ) );
            }
            ContextBindings.clBindings.put ( classLoader, context );
            ContextBindings.clObjectBindings.put ( classLoader, obj );
        }
    }
    public static void unbindClassLoader ( final Object obj, final Object token, final ClassLoader classLoader ) {
        if ( ContextAccessController.checkSecurityToken ( obj, token ) ) {
            final Object o = ContextBindings.clObjectBindings.get ( classLoader );
            if ( o == null || !o.equals ( obj ) ) {
                return;
            }
            ContextBindings.clBindings.remove ( classLoader );
            ContextBindings.clObjectBindings.remove ( classLoader );
        }
    }
    public static Context getClassLoader() throws NamingException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Context context = null;
        do {
            context = ContextBindings.clBindings.get ( cl );
            if ( context != null ) {
                return context;
            }
        } while ( ( cl = cl.getParent() ) != null );
        throw new NamingException ( ContextBindings.sm.getString ( "contextBindings.noContextBoundToCL" ) );
    }
    static String getClassLoaderName() throws NamingException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Object obj = null;
        do {
            obj = ContextBindings.clObjectBindings.get ( cl );
            if ( obj != null ) {
                return obj.toString();
            }
        } while ( ( cl = cl.getParent() ) != null );
        throw new NamingException ( ContextBindings.sm.getString ( "contextBindings.noContextBoundToCL" ) );
    }
    public static boolean isClassLoaderBound() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        while ( !ContextBindings.clBindings.containsKey ( cl ) ) {
            if ( ( cl = cl.getParent() ) == null ) {
                return false;
            }
        }
        return true;
    }
    static {
        objectBindings = new Hashtable<Object, Context>();
        threadBindings = new Hashtable<Thread, Context>();
        threadObjectBindings = new Hashtable<Thread, Object>();
        clBindings = new Hashtable<ClassLoader, Context>();
        clObjectBindings = new Hashtable<ClassLoader, Object>();
        sm = StringManager.getManager ( ContextBindings.class );
    }
}
