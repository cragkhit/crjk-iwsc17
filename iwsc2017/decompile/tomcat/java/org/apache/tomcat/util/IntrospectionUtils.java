package org.apache.tomcat.util;
import org.apache.juli.logging.LogFactory;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.lang.reflect.Method;
import java.util.Hashtable;
import org.apache.juli.logging.Log;
public final class IntrospectionUtils {
    private static final Log log;
    private static final Hashtable<Class<?>, Method[]> objectMethods;
    public static boolean setProperty ( final Object o, final String name, final String value ) {
        return setProperty ( o, name, value, true );
    }
    public static boolean setProperty ( final Object o, final String name, final String value, final boolean invokeSetProperty ) {
        if ( IntrospectionUtils.log.isDebugEnabled() ) {
            IntrospectionUtils.log.debug ( "IntrospectionUtils: setProperty(" + o.getClass() + " " + name + "=" + value + ")" );
        }
        final String setter = "set" + capitalize ( name );
        try {
            final Method[] methods = findMethods ( o.getClass() );
            Method setPropertyMethodVoid = null;
            Method setPropertyMethodBool = null;
            for ( int i = 0; i < methods.length; ++i ) {
                final Class<?>[] paramT = methods[i].getParameterTypes();
                if ( setter.equals ( methods[i].getName() ) && paramT.length == 1 && "java.lang.String".equals ( paramT[0].getName() ) ) {
                    methods[i].invoke ( o, value );
                    return true;
                }
            }
            for ( int i = 0; i < methods.length; ++i ) {
                boolean ok = true;
                if ( setter.equals ( methods[i].getName() ) && methods[i].getParameterTypes().length == 1 ) {
                    final Class<?> paramType = methods[i].getParameterTypes() [0];
                    final Object[] params = { null };
                    Label_0488: {
                        if ( !"java.lang.Integer".equals ( paramType.getName() ) ) {
                            if ( !"int".equals ( paramType.getName() ) ) {
                                if ( !"java.lang.Long".equals ( paramType.getName() ) ) {
                                    if ( !"long".equals ( paramType.getName() ) ) {
                                        if ( "java.lang.Boolean".equals ( paramType.getName() ) || "boolean".equals ( paramType.getName() ) ) {
                                            params[0] = Boolean.valueOf ( value );
                                            break Label_0488;
                                        }
                                        if ( "java.net.InetAddress".equals ( paramType.getName() ) ) {
                                            try {
                                                params[0] = InetAddress.getByName ( value );
                                            } catch ( UnknownHostException exc ) {
                                                if ( IntrospectionUtils.log.isDebugEnabled() ) {
                                                    IntrospectionUtils.log.debug ( "IntrospectionUtils: Unable to resolve host name:" + value );
                                                }
                                                ok = false;
                                            }
                                            break Label_0488;
                                        }
                                        if ( IntrospectionUtils.log.isDebugEnabled() ) {
                                            IntrospectionUtils.log.debug ( "IntrospectionUtils: Unknown type " + paramType.getName() );
                                        }
                                        break Label_0488;
                                    }
                                }
                                try {
                                    params[0] = Long.valueOf ( value );
                                } catch ( NumberFormatException ex3 ) {
                                    ok = false;
                                }
                                break Label_0488;
                            }
                        }
                        try {
                            params[0] = Integer.valueOf ( value );
                        } catch ( NumberFormatException ex3 ) {
                            ok = false;
                        }
                    }
                    if ( ok ) {
                        methods[i].invoke ( o, params );
                        return true;
                    }
                }
                if ( "setProperty".equals ( methods[i].getName() ) ) {
                    if ( methods[i].getReturnType() == Boolean.TYPE ) {
                        setPropertyMethodBool = methods[i];
                    } else {
                        setPropertyMethodVoid = methods[i];
                    }
                }
            }
            if ( invokeSetProperty && ( setPropertyMethodBool != null || setPropertyMethodVoid != null ) ) {
                final Object[] params2 = { name, value };
                if ( setPropertyMethodBool != null ) {
                    try {
                        return ( boolean ) setPropertyMethodBool.invoke ( o, params2 );
                    } catch ( IllegalArgumentException biae ) {
                        if ( setPropertyMethodVoid != null ) {
                            setPropertyMethodVoid.invoke ( o, params2 );
                            return true;
                        }
                        throw biae;
                    }
                }
                setPropertyMethodVoid.invoke ( o, params2 );
                return true;
            }
        } catch ( IllegalArgumentException ex2 ) {
            IntrospectionUtils.log.warn ( "IAE " + o + " " + name + " " + value, ex2 );
        } catch ( SecurityException ex2 ) {
            IntrospectionUtils.log.warn ( "IntrospectionUtils: SecurityException for " + o.getClass() + " " + name + "=" + value + ")", ex2 );
        } catch ( IllegalAccessException iae ) {
            IntrospectionUtils.log.warn ( "IntrospectionUtils: IllegalAccessException for " + o.getClass() + " " + name + "=" + value + ")", iae );
        } catch ( InvocationTargetException ie ) {
            ExceptionUtils.handleThrowable ( ie.getCause() );
            IntrospectionUtils.log.warn ( "IntrospectionUtils: InvocationTargetException for " + o.getClass() + " " + name + "=" + value + ")", ie );
        }
        return false;
    }
    public static Object getProperty ( final Object o, final String name ) {
        final String getter = "get" + capitalize ( name );
        final String isGetter = "is" + capitalize ( name );
        try {
            final Method[] methods = findMethods ( o.getClass() );
            Method getPropertyMethod = null;
            for ( int i = 0; i < methods.length; ++i ) {
                final Class<?>[] paramT = methods[i].getParameterTypes();
                if ( getter.equals ( methods[i].getName() ) && paramT.length == 0 ) {
                    return methods[i].invoke ( o, ( Object[] ) null );
                }
                if ( isGetter.equals ( methods[i].getName() ) && paramT.length == 0 ) {
                    return methods[i].invoke ( o, ( Object[] ) null );
                }
                if ( "getProperty".equals ( methods[i].getName() ) ) {
                    getPropertyMethod = methods[i];
                }
            }
            if ( getPropertyMethod != null ) {
                final Object[] params = { name };
                return getPropertyMethod.invoke ( o, params );
            }
        } catch ( IllegalArgumentException ex2 ) {
            IntrospectionUtils.log.warn ( "IAE " + o + " " + name, ex2 );
        } catch ( SecurityException ex2 ) {
            IntrospectionUtils.log.warn ( "IntrospectionUtils: SecurityException for " + o.getClass() + " " + name + ")", ex2 );
        } catch ( IllegalAccessException iae ) {
            IntrospectionUtils.log.warn ( "IntrospectionUtils: IllegalAccessException for " + o.getClass() + " " + name + ")", iae );
        } catch ( InvocationTargetException ie ) {
            if ( ie.getCause() instanceof NullPointerException ) {
                return null;
            }
            ExceptionUtils.handleThrowable ( ie.getCause() );
            IntrospectionUtils.log.warn ( "IntrospectionUtils: InvocationTargetException for " + o.getClass() + " " + name + ")", ie );
        }
        return null;
    }
    public static String replaceProperties ( final String value, final Hashtable<Object, Object> staticProp, final PropertySource[] dynamicProp ) {
        if ( value.indexOf ( 36 ) < 0 ) {
            return value;
        }
        final StringBuilder sb = new StringBuilder();
        int prev = 0;
        int pos;
        while ( ( pos = value.indexOf ( 36, prev ) ) >= 0 ) {
            if ( pos > 0 ) {
                sb.append ( value.substring ( prev, pos ) );
            }
            if ( pos == value.length() - 1 ) {
                sb.append ( '$' );
                prev = pos + 1;
            } else if ( value.charAt ( pos + 1 ) != '{' ) {
                sb.append ( '$' );
                prev = pos + 1;
            } else {
                final int endName = value.indexOf ( 125, pos );
                if ( endName < 0 ) {
                    sb.append ( value.substring ( pos ) );
                    prev = value.length();
                } else {
                    final String n = value.substring ( pos + 2, endName );
                    String v = null;
                    if ( staticProp != null ) {
                        v = staticProp.get ( n );
                    }
                    if ( v == null && dynamicProp != null ) {
                        for ( int i = 0; i < dynamicProp.length; ++i ) {
                            v = dynamicProp[i].getProperty ( n );
                            if ( v != null ) {
                                break;
                            }
                        }
                    }
                    if ( v == null ) {
                        v = "${" + n + "}";
                    }
                    sb.append ( v );
                    prev = endName + 1;
                }
            }
        }
        if ( prev < value.length() ) {
            sb.append ( value.substring ( prev ) );
        }
        return sb.toString();
    }
    public static String capitalize ( final String name ) {
        if ( name == null || name.length() == 0 ) {
            return name;
        }
        final char[] chars = name.toCharArray();
        chars[0] = Character.toUpperCase ( chars[0] );
        return new String ( chars );
    }
    public static void clear() {
        IntrospectionUtils.objectMethods.clear();
    }
    public static Method[] findMethods ( final Class<?> c ) {
        Method[] methods = IntrospectionUtils.objectMethods.get ( c );
        if ( methods != null ) {
            return methods;
        }
        methods = c.getMethods();
        IntrospectionUtils.objectMethods.put ( c, methods );
        return methods;
    }
    public static Method findMethod ( final Class<?> c, final String name, final Class<?>[] params ) {
        final Method[] methods = findMethods ( c );
        if ( methods == null ) {
            return null;
        }
        for ( int i = 0; i < methods.length; ++i ) {
            if ( methods[i].getName().equals ( name ) ) {
                final Class<?>[] methodParams = methods[i].getParameterTypes();
                if ( methodParams == null && ( params == null || params.length == 0 ) ) {
                    return methods[i];
                }
                if ( params == null && ( methodParams == null || methodParams.length == 0 ) ) {
                    return methods[i];
                }
                if ( params.length == methodParams.length ) {
                    boolean found = true;
                    for ( int j = 0; j < params.length; ++j ) {
                        if ( params[j] != methodParams[j] ) {
                            found = false;
                            break;
                        }
                    }
                    if ( found ) {
                        return methods[i];
                    }
                }
            }
        }
        return null;
    }
    public static Object callMethod1 ( final Object target, final String methodN, final Object param1, final String typeParam1, final ClassLoader cl ) throws Exception {
        if ( target == null || param1 == null ) {
            throw new IllegalArgumentException ( "IntrospectionUtils: Assert: Illegal params " + target + " " + param1 );
        }
        if ( IntrospectionUtils.log.isDebugEnabled() ) {
            IntrospectionUtils.log.debug ( "IntrospectionUtils: callMethod1 " + target.getClass().getName() + " " + param1.getClass().getName() + " " + typeParam1 );
        }
        final Class<?>[] params = ( Class<?>[] ) new Class[] { null };
        if ( typeParam1 == null ) {
            params[0] = param1.getClass();
        } else {
            params[0] = cl.loadClass ( typeParam1 );
        }
        final Method m = findMethod ( target.getClass(), methodN, params );
        if ( m == null ) {
            throw new NoSuchMethodException ( target.getClass().getName() + " " + methodN );
        }
        try {
            return m.invoke ( target, param1 );
        } catch ( InvocationTargetException ie ) {
            ExceptionUtils.handleThrowable ( ie.getCause() );
            throw ie;
        }
    }
    public static Object callMethodN ( final Object target, final String methodN, final Object[] params, final Class<?>[] typeParams ) throws Exception {
        Method m = null;
        m = findMethod ( target.getClass(), methodN, typeParams );
        if ( m == null ) {
            if ( IntrospectionUtils.log.isDebugEnabled() ) {
                IntrospectionUtils.log.debug ( "IntrospectionUtils: Can't find method " + methodN + " in " + target + " CLASS " + target.getClass() );
            }
            return null;
        }
        try {
            final Object o = m.invoke ( target, params );
            if ( IntrospectionUtils.log.isDebugEnabled() ) {
                final StringBuilder sb = new StringBuilder();
                sb.append ( target.getClass().getName() ).append ( '.' ).append ( methodN ).append ( "( " );
                for ( int i = 0; i < params.length; ++i ) {
                    if ( i > 0 ) {
                        sb.append ( ", " );
                    }
                    sb.append ( params[i] );
                }
                sb.append ( ")" );
                IntrospectionUtils.log.debug ( "IntrospectionUtils:" + sb.toString() );
            }
            return o;
        } catch ( InvocationTargetException ie ) {
            ExceptionUtils.handleThrowable ( ie.getCause() );
            throw ie;
        }
    }
    public static Object convert ( final String object, final Class<?> paramType ) {
        Object result = null;
        Label_0190: {
            if ( "java.lang.String".equals ( paramType.getName() ) ) {
                result = object;
            } else {
                if ( !"java.lang.Integer".equals ( paramType.getName() ) ) {
                    if ( !"int".equals ( paramType.getName() ) ) {
                        if ( "java.lang.Boolean".equals ( paramType.getName() ) || "boolean".equals ( paramType.getName() ) ) {
                            result = Boolean.valueOf ( object );
                            break Label_0190;
                        }
                        if ( "java.net.InetAddress".equals ( paramType.getName() ) ) {
                            try {
                                result = InetAddress.getByName ( object );
                            } catch ( UnknownHostException exc ) {
                                if ( IntrospectionUtils.log.isDebugEnabled() ) {
                                    IntrospectionUtils.log.debug ( "IntrospectionUtils: Unable to resolve host name:" + object );
                                }
                            }
                            break Label_0190;
                        }
                        if ( IntrospectionUtils.log.isDebugEnabled() ) {
                            IntrospectionUtils.log.debug ( "IntrospectionUtils: Unknown type " + paramType.getName() );
                        }
                        break Label_0190;
                    }
                }
                try {
                    result = Integer.valueOf ( object );
                } catch ( NumberFormatException ex ) {}
            }
        }
        if ( result == null ) {
            throw new IllegalArgumentException ( "Can't convert argument: " + object );
        }
        return result;
    }
    static {
        log = LogFactory.getLog ( IntrospectionUtils.class );
        objectMethods = new Hashtable<Class<?>, Method[]>();
    }
    public interface PropertySource {
        String getProperty ( String p0 );
    }
}
