package org.apache.catalina.util;
import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.res.StringManager;
public class Introspection {
    private static final StringManager sm =
        StringManager.getManager ( "org.apache.catalina.util" );
    public static String getPropertyName ( Method setter ) {
        return Introspector.decapitalize ( setter.getName().substring ( 3 ) );
    }
    public static boolean isValidSetter ( Method method ) {
        if ( method.getName().startsWith ( "set" )
                && method.getName().length() > 3
                && method.getParameterTypes().length == 1
                && method.getReturnType().getName().equals ( "void" ) ) {
            return true;
        }
        return false;
    }
    public static boolean isValidLifecycleCallback ( Method method ) {
        if ( method.getParameterTypes().length != 0
                || Modifier.isStatic ( method.getModifiers() )
                || method.getExceptionTypes().length > 0
                || !method.getReturnType().getName().equals ( "void" ) ) {
            return false;
        }
        return true;
    }
    public static Field[] getDeclaredFields ( final Class<?> clazz ) {
        Field[] fields = null;
        if ( Globals.IS_SECURITY_ENABLED ) {
            fields = AccessController.doPrivileged (
            new PrivilegedAction<Field[]>() {
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
            methods = AccessController.doPrivileged (
            new PrivilegedAction<Method[]>() {
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
    public static Class<?> loadClass ( Context context, String className ) {
        ClassLoader cl = context.getLoader().getClassLoader();
        Log log = context.getLogger();
        Class<?> clazz = null;
        try {
            clazz = cl.loadClass ( className );
        } catch ( ClassNotFoundException | NoClassDefFoundError | ClassFormatError e ) {
            log.debug ( sm.getString ( "introspection.classLoadFailed", className ), e );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log.debug ( sm.getString ( "introspection.classLoadFailed", className ), t );
        }
        return clazz;
    }
    public static Class<?> convertPrimitiveType ( Class<?> clazz ) {
        if ( clazz.equals ( char.class ) ) {
            return Character.class;
        } else if ( clazz.equals ( int.class ) ) {
            return Integer.class;
        } else if ( clazz.equals ( boolean.class ) ) {
            return Boolean.class;
        } else if ( clazz.equals ( double.class ) ) {
            return Double.class;
        } else if ( clazz.equals ( byte.class ) ) {
            return Byte.class;
        } else if ( clazz.equals ( short.class ) ) {
            return Short.class;
        } else if ( clazz.equals ( long.class ) ) {
            return Long.class;
        } else if ( clazz.equals ( float.class ) ) {
            return Float.class;
        } else {
            return clazz;
        }
    }
}
