package org.apache.catalina.util;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.catalina.Context;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.catalina.Globals;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.beans.Introspector;
import java.lang.reflect.Method;
import org.apache.tomcat.util.res.StringManager;
public class Introspection {
    private static final StringManager sm;
    public static String getPropertyName ( final Method setter ) {
        return Introspector.decapitalize ( setter.getName().substring ( 3 ) );
    }
    public static boolean isValidSetter ( final Method method ) {
        return method.getName().startsWith ( "set" ) && method.getName().length() > 3 && method.getParameterTypes().length == 1 && method.getReturnType().getName().equals ( "void" );
    }
    public static boolean isValidLifecycleCallback ( final Method method ) {
        return method.getParameterTypes().length == 0 && !Modifier.isStatic ( method.getModifiers() ) && method.getExceptionTypes().length <= 0 && method.getReturnType().getName().equals ( "void" );
    }
    public static Field[] getDeclaredFields ( final Class<?> clazz ) {
        Field[] fields = null;
        if ( Globals.IS_SECURITY_ENABLED ) {
            fields = AccessController.doPrivileged ( ( PrivilegedAction<Field[]> ) new PrivilegedAction<Field[]>() {
                @Override
                public Field[] run() {
                    return clazz.getDeclaredFields();
                }
            } );
        } else {
            fields = clazz.getDeclaredFields();
        }
        return fields;
    }
    public static Method[] getDeclaredMethods ( final Class<?> clazz ) {
        Method[] methods = null;
        if ( Globals.IS_SECURITY_ENABLED ) {
            methods = AccessController.doPrivileged ( ( PrivilegedAction<Method[]> ) new PrivilegedAction<Method[]>() {
                @Override
                public Method[] run() {
                    return clazz.getDeclaredMethods();
                }
            } );
        } else {
            methods = clazz.getDeclaredMethods();
        }
        return methods;
    }
    public static Class<?> loadClass ( final Context context, final String className ) {
        final ClassLoader cl = context.getLoader().getClassLoader();
        final Log log = context.getLogger();
        Class<?> clazz = null;
        try {
            clazz = cl.loadClass ( className );
        } catch ( ClassNotFoundException | NoClassDefFoundError | ClassFormatError e ) {
            log.debug ( Introspection.sm.getString ( "introspection.classLoadFailed", className ), e );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log.debug ( Introspection.sm.getString ( "introspection.classLoadFailed", className ), t );
        }
        return clazz;
    }
    public static Class<?> convertPrimitiveType ( final Class<?> clazz ) {
        if ( clazz.equals ( Character.TYPE ) ) {
            return Character.class;
        }
        if ( clazz.equals ( Integer.TYPE ) ) {
            return Integer.class;
        }
        if ( clazz.equals ( Boolean.TYPE ) ) {
            return Boolean.class;
        }
        if ( clazz.equals ( Double.TYPE ) ) {
            return Double.class;
        }
        if ( clazz.equals ( Byte.TYPE ) ) {
            return Byte.class;
        }
        if ( clazz.equals ( Short.TYPE ) ) {
            return Short.class;
        }
        if ( clazz.equals ( Long.TYPE ) ) {
            return Long.class;
        }
        if ( clazz.equals ( Float.TYPE ) ) {
            return Float.class;
        }
        return clazz;
    }
    static {
        sm = StringManager.getManager ( "org.apache.catalina.util" );
    }
}
