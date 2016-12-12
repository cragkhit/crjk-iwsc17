package org.apache.tomcat.util;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public final class IntrospectionUtils {
    private static final Log log = LogFactory.getLog ( IntrospectionUtils.class );
    public static boolean setProperty ( Object o, String name, String value ) {
        return setProperty ( o, name, value, true );
    }
    @SuppressWarnings ( "null" )
    public static boolean setProperty ( Object o, String name, String value,
                                        boolean invokeSetProperty ) {
        if ( log.isDebugEnabled() )
            log.debug ( "IntrospectionUtils: setProperty(" +
                        o.getClass() + " " + name + "=" + value + ")" );
        String setter = "set" + capitalize ( name );
        try {
            Method methods[] = findMethods ( o.getClass() );
            Method setPropertyMethodVoid = null;
            Method setPropertyMethodBool = null;
            for ( int i = 0; i < methods.length; i++ ) {
                Class<?> paramT[] = methods[i].getParameterTypes();
                if ( setter.equals ( methods[i].getName() ) && paramT.length == 1
                        && "java.lang.String".equals ( paramT[0].getName() ) ) {
                    methods[i].invoke ( o, new Object[] { value } );
                    return true;
                }
            }
            for ( int i = 0; i < methods.length; i++ ) {
                boolean ok = true;
                if ( setter.equals ( methods[i].getName() )
                        && methods[i].getParameterTypes().length == 1 ) {
                    Class<?> paramType = methods[i].getParameterTypes() [0];
                    Object params[] = new Object[1];
                    if ( "java.lang.Integer".equals ( paramType.getName() )
                            || "int".equals ( paramType.getName() ) ) {
                        try {
                            params[0] = Integer.valueOf ( value );
                        } catch ( NumberFormatException ex ) {
                            ok = false;
                        }
                    } else if ( "java.lang.Long".equals ( paramType.getName() )
                                || "long".equals ( paramType.getName() ) ) {
                        try {
                            params[0] = Long.valueOf ( value );
                        } catch ( NumberFormatException ex ) {
                            ok = false;
                        }
                    } else if ( "java.lang.Boolean".equals ( paramType.getName() )
                                || "boolean".equals ( paramType.getName() ) ) {
                        params[0] = Boolean.valueOf ( value );
                    } else if ( "java.net.InetAddress".equals ( paramType
                                .getName() ) ) {
                        try {
                            params[0] = InetAddress.getByName ( value );
                        } catch ( UnknownHostException exc ) {
                            if ( log.isDebugEnabled() ) {
                                log.debug ( "IntrospectionUtils: Unable to resolve host name:" + value );
                            }
                            ok = false;
                        }
                    } else {
                        if ( log.isDebugEnabled() )
                            log.debug ( "IntrospectionUtils: Unknown type " +
                                        paramType.getName() );
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
            if ( invokeSetProperty && ( setPropertyMethodBool != null ||
                                        setPropertyMethodVoid != null ) ) {
                Object params[] = new Object[2];
                params[0] = name;
                params[1] = value;
                if ( setPropertyMethodBool != null ) {
                    try {
                        return ( ( Boolean ) setPropertyMethodBool.invoke ( o,
                                 params ) ).booleanValue();
                    } catch ( IllegalArgumentException biae ) {
                        if ( setPropertyMethodVoid != null ) {
                            setPropertyMethodVoid.invoke ( o, params );
                            return true;
                        } else {
                            throw biae;
                        }
                    }
                } else {
                    setPropertyMethodVoid.invoke ( o, params );
                    return true;
                }
            }
        } catch ( IllegalArgumentException ex2 ) {
            log.warn ( "IAE " + o + " " + name + " " + value, ex2 );
        } catch ( SecurityException ex1 ) {
            log.warn ( "IntrospectionUtils: SecurityException for " +
                       o.getClass() + " " + name + "=" + value + ")", ex1 );
        } catch ( IllegalAccessException iae ) {
            log.warn ( "IntrospectionUtils: IllegalAccessException for " +
                       o.getClass() + " " + name + "=" + value + ")", iae );
        } catch ( InvocationTargetException ie ) {
            ExceptionUtils.handleThrowable ( ie.getCause() );
            log.warn ( "IntrospectionUtils: InvocationTargetException for " +
                       o.getClass() + " " + name + "=" + value + ")", ie );
        }
        return false;
    }
    public static Object getProperty ( Object o, String name ) {
        String getter = "get" + capitalize ( name );
        String isGetter = "is" + capitalize ( name );
        try {
            Method methods[] = findMethods ( o.getClass() );
            Method getPropertyMethod = null;
            for ( int i = 0; i < methods.length; i++ ) {
                Class<?> paramT[] = methods[i].getParameterTypes();
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
                Object params[] = new Object[1];
                params[0] = name;
                return getPropertyMethod.invoke ( o, params );
            }
        } catch ( IllegalArgumentException ex2 ) {
            log.warn ( "IAE " + o + " " + name, ex2 );
        } catch ( SecurityException ex1 ) {
            log.warn ( "IntrospectionUtils: SecurityException for " +
                       o.getClass() + " " + name + ")", ex1 );
        } catch ( IllegalAccessException iae ) {
            log.warn ( "IntrospectionUtils: IllegalAccessException for " +
                       o.getClass() + " " + name + ")", iae );
        } catch ( InvocationTargetException ie ) {
            if ( ie.getCause() instanceof NullPointerException ) {
                return null;
            }
            ExceptionUtils.handleThrowable ( ie.getCause() );
            log.warn ( "IntrospectionUtils: InvocationTargetException for " +
                       o.getClass() + " " + name + ")", ie );
        }
        return null;
    }
    public static String replaceProperties ( String value,
            Hashtable<Object, Object> staticProp, PropertySource dynamicProp[] ) {
        if ( value.indexOf ( '$' ) < 0 ) {
            return value;
        }
        StringBuilder sb = new StringBuilder();
        int prev = 0;
        int pos;
        while ( ( pos = value.indexOf ( '$', prev ) ) >= 0 ) {
            if ( pos > 0 ) {
                sb.append ( value.substring ( prev, pos ) );
            }
            if ( pos == ( value.length() - 1 ) ) {
                sb.append ( '$' );
                prev = pos + 1;
            } else if ( value.charAt ( pos + 1 ) != '{' ) {
                sb.append ( '$' );
                prev = pos + 1;
            } else {
                int endName = value.indexOf ( '}', pos );
                if ( endName < 0 ) {
                    sb.append ( value.substring ( pos ) );
                    prev = value.length();
                    continue;
                }
                String n = value.substring ( pos + 2, endName );
                String v = null;
                if ( staticProp != null ) {
                    v = ( String ) staticProp.get ( n );
                }
                if ( v == null && dynamicProp != null ) {
                    for ( int i = 0; i < dynamicProp.length; i++ ) {
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
        if ( prev < value.length() ) {
            sb.append ( value.substring ( prev ) );
        }
        return sb.toString();
    }
    public static String capitalize ( String name ) {
        if ( name == null || name.length() == 0 ) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toUpperCase ( chars[0] );
        return new String ( chars );
    }
    public static void clear() {
        objectMethods.clear();
    }
    private static final Hashtable<Class<?>, Method[]> objectMethods = new Hashtable<>();
    public static Method[] findMethods ( Class<?> c ) {
        Method methods[] = objectMethods.get ( c );
        if ( methods != null ) {
            return methods;
        }
        methods = c.getMethods();
        objectMethods.put ( c, methods );
        return methods;
    }
    @SuppressWarnings ( "null" )
    public static Method findMethod ( Class<?> c, String name,
                                      Class<?> params[] ) {
        Method methods[] = findMethods ( c );
        if ( methods == null ) {
            return null;
        }
        for ( int i = 0; i < methods.length; i++ ) {
            if ( methods[i].getName().equals ( name ) ) {
                Class<?> methodParams[] = methods[i].getParameterTypes();
                if ( methodParams == null )
                    if ( params == null || params.length == 0 ) {
                        return methods[i];
                    }
                if ( params == null )
                    if ( methodParams == null || methodParams.length == 0 ) {
                        return methods[i];
                    }
                if ( params.length != methodParams.length ) {
                    continue;
                }
                boolean found = true;
                for ( int j = 0; j < params.length; j++ ) {
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
        return null;
    }
    public static Object callMethod1 ( Object target, String methodN,
                                       Object param1, String typeParam1, ClassLoader cl ) throws Exception {
        if ( target == null || param1 == null ) {
            throw new IllegalArgumentException (
                "IntrospectionUtils: Assert: Illegal params " +
                target + " " + param1 );
        }
        if ( log.isDebugEnabled() )
            log.debug ( "IntrospectionUtils: callMethod1 " +
                        target.getClass().getName() + " " +
                        param1.getClass().getName() + " " + typeParam1 );
        Class<?> params[] = new Class[1];
        if ( typeParam1 == null ) {
            params[0] = param1.getClass();
        } else {
            params[0] = cl.loadClass ( typeParam1 );
        }
        Method m = findMethod ( target.getClass(), methodN, params );
        if ( m == null )
            throw new NoSuchMethodException ( target.getClass().getName() + " "
                                              + methodN );
        try {
            return m.invoke ( target, new Object[] { param1 } );
        } catch ( InvocationTargetException ie ) {
            ExceptionUtils.handleThrowable ( ie.getCause() );
            throw ie;
        }
    }
    public static Object callMethodN ( Object target, String methodN,
                                       Object params[], Class<?> typeParams[] ) throws Exception {
        Method m = null;
        m = findMethod ( target.getClass(), methodN, typeParams );
        if ( m == null ) {
            if ( log.isDebugEnabled() )
                log.debug ( "IntrospectionUtils: Can't find method " + methodN +
                            " in " + target + " CLASS " + target.getClass() );
            return null;
        }
        try {
            Object o = m.invoke ( target, params );
            if ( log.isDebugEnabled() ) {
                StringBuilder sb = new StringBuilder();
                sb.append ( target.getClass().getName() ).append ( '.' )
                .append ( methodN ).append ( "( " );
                for ( int i = 0; i < params.length; i++ ) {
                    if ( i > 0 ) {
                        sb.append ( ", " );
                    }
                    sb.append ( params[i] );
                }
                sb.append ( ")" );
                log.debug ( "IntrospectionUtils:" + sb.toString() );
            }
            return o;
        } catch ( InvocationTargetException ie ) {
            ExceptionUtils.handleThrowable ( ie.getCause() );
            throw ie;
        }
    }
    public static Object convert ( String object, Class<?> paramType ) {
        Object result = null;
        if ( "java.lang.String".equals ( paramType.getName() ) ) {
            result = object;
        } else if ( "java.lang.Integer".equals ( paramType.getName() )
                    || "int".equals ( paramType.getName() ) ) {
            try {
                result = Integer.valueOf ( object );
            } catch ( NumberFormatException ex ) {
            }
        } else if ( "java.lang.Boolean".equals ( paramType.getName() )
                    || "boolean".equals ( paramType.getName() ) ) {
            result = Boolean.valueOf ( object );
        } else if ( "java.net.InetAddress".equals ( paramType
                    .getName() ) ) {
            try {
                result = InetAddress.getByName ( object );
            } catch ( UnknownHostException exc ) {
                if ( log.isDebugEnabled() )
                    log.debug ( "IntrospectionUtils: Unable to resolve host name:" +
                                object );
            }
        } else {
            if ( log.isDebugEnabled() )
                log.debug ( "IntrospectionUtils: Unknown type " +
                            paramType.getName() );
        }
        if ( result == null ) {
            throw new IllegalArgumentException ( "Can't convert argument: " + object );
        }
        return result;
    }
    public static interface PropertySource {
        public String getProperty ( String key );
    }
}
