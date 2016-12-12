package org.apache.el.util;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.el.ELException;
import javax.el.MethodNotFoundException;
import org.apache.el.lang.ELSupport;
import org.apache.el.lang.EvaluationContext;
public class ReflectionUtil {
    protected static final String[] PRIMITIVE_NAMES = new String[] { "boolean",
            "byte", "char", "double", "float", "int", "long", "short", "void"
                                                                   };
    protected static final Class<?>[] PRIMITIVES = new Class[] { boolean.class,
            byte.class, char.class, double.class, float.class, int.class,
            long.class, short.class, Void.TYPE
                                                               };
    private ReflectionUtil() {
        super();
    }
    public static Class<?> forName ( String name ) throws ClassNotFoundException {
        if ( null == name || "".equals ( name ) ) {
            return null;
        }
        Class<?> c = forNamePrimitive ( name );
        if ( c == null ) {
            if ( name.endsWith ( "[]" ) ) {
                String nc = name.substring ( 0, name.length() - 2 );
                c = Class.forName ( nc, true, Thread.currentThread().getContextClassLoader() );
                c = Array.newInstance ( c, 0 ).getClass();
            } else {
                c = Class.forName ( name, true, Thread.currentThread().getContextClassLoader() );
            }
        }
        return c;
    }
    protected static Class<?> forNamePrimitive ( String name ) {
        if ( name.length() <= 8 ) {
            int p = Arrays.binarySearch ( PRIMITIVE_NAMES, name );
            if ( p >= 0 ) {
                return PRIMITIVES[p];
            }
        }
        return null;
    }
    public static Class<?>[] toTypeArray ( String[] s ) throws ClassNotFoundException {
        if ( s == null ) {
            return null;
        }
        Class<?>[] c = new Class[s.length];
        for ( int i = 0; i < s.length; i++ ) {
            c[i] = forName ( s[i] );
        }
        return c;
    }
    public static String[] toTypeNameArray ( Class<?>[] c ) {
        if ( c == null ) {
            return null;
        }
        String[] s = new String[c.length];
        for ( int i = 0; i < c.length; i++ ) {
            s[i] = c[i].getName();
        }
        return s;
    }
    @SuppressWarnings ( "null" )
    public static Method getMethod ( EvaluationContext ctx, Object base, Object property,
                                     Class<?>[] paramTypes, Object[] paramValues )
    throws MethodNotFoundException {
        if ( base == null || property == null ) {
            throw new MethodNotFoundException ( MessageFactory.get (
                                                    "error.method.notfound", base, property,
                                                    paramString ( paramTypes ) ) );
        }
        String methodName = ( property instanceof String ) ? ( String ) property
                            : property.toString();
        int paramCount;
        if ( paramTypes == null ) {
            paramCount = 0;
        } else {
            paramCount = paramTypes.length;
        }
        Method[] methods = base.getClass().getMethods();
        Map<Method, MatchResult> candidates = new HashMap<>();
        for ( Method m : methods ) {
            if ( !m.getName().equals ( methodName ) ) {
                continue;
            }
            Class<?>[] mParamTypes = m.getParameterTypes();
            int mParamCount;
            if ( mParamTypes == null ) {
                mParamCount = 0;
            } else {
                mParamCount = mParamTypes.length;
            }
            if ( ! ( paramCount == mParamCount ||
                     ( m.isVarArgs() && paramCount >= mParamCount ) ) ) {
                continue;
            }
            int exactMatch = 0;
            int assignableMatch = 0;
            int coercibleMatch = 0;
            boolean noMatch = false;
            for ( int i = 0; i < mParamCount; i++ ) {
                if ( mParamTypes[i].equals ( paramTypes[i] ) ) {
                    exactMatch++;
                } else if ( i == ( mParamCount - 1 ) && m.isVarArgs() ) {
                    Class<?> varType = mParamTypes[i].getComponentType();
                    for ( int j = i; j < paramCount; j++ ) {
                        if ( isAssignableFrom ( paramTypes[j], varType ) ) {
                            assignableMatch++;
                        } else {
                            if ( paramValues == null ) {
                                noMatch = true;
                                break;
                            } else {
                                if ( isCoercibleFrom ( ctx, paramValues[j], varType ) ) {
                                    coercibleMatch++;
                                } else {
                                    noMatch = true;
                                    break;
                                }
                            }
                        }
                    }
                } else if ( isAssignableFrom ( paramTypes[i], mParamTypes[i] ) ) {
                    assignableMatch++;
                } else {
                    if ( paramValues == null ) {
                        noMatch = true;
                        break;
                    } else {
                        if ( isCoercibleFrom ( ctx, paramValues[i], mParamTypes[i] ) ) {
                            coercibleMatch++;
                        } else {
                            noMatch = true;
                            break;
                        }
                    }
                }
            }
            if ( noMatch ) {
                continue;
            }
            if ( exactMatch == paramCount ) {
                return getMethod ( base.getClass(), m );
            }
            candidates.put ( m, new MatchResult (
                                 exactMatch, assignableMatch, coercibleMatch, m.isBridge() ) );
        }
        MatchResult bestMatch = new MatchResult ( 0, 0, 0, false );
        Method match = null;
        boolean multiple = false;
        for ( Map.Entry<Method, MatchResult> entry : candidates.entrySet() ) {
            int cmp = entry.getValue().compareTo ( bestMatch );
            if ( cmp > 0 || match == null ) {
                bestMatch = entry.getValue();
                match = entry.getKey();
                multiple = false;
            } else if ( cmp == 0 ) {
                multiple = true;
            }
        }
        if ( multiple ) {
            if ( bestMatch.getExact() == paramCount - 1 ) {
                match = resolveAmbiguousMethod ( candidates.keySet(), paramTypes );
            } else {
                match = null;
            }
            if ( match == null ) {
                throw new MethodNotFoundException ( MessageFactory.get (
                                                        "error.method.ambiguous", base, property,
                                                        paramString ( paramTypes ) ) );
            }
        }
        if ( match == null ) {
            throw new MethodNotFoundException ( MessageFactory.get (
                                                    "error.method.notfound", base, property,
                                                    paramString ( paramTypes ) ) );
        }
        return getMethod ( base.getClass(), match );
    }
    private static Method resolveAmbiguousMethod ( Set<Method> candidates,
            Class<?>[] paramTypes ) {
        Method m = candidates.iterator().next();
        int nonMatchIndex = 0;
        Class<?> nonMatchClass = null;
        for ( int i = 0; i < paramTypes.length; i++ ) {
            if ( m.getParameterTypes() [i] != paramTypes[i] ) {
                nonMatchIndex = i;
                nonMatchClass = paramTypes[i];
                break;
            }
        }
        if ( nonMatchClass == null ) {
            return null;
        }
        for ( Method c : candidates ) {
            if ( c.getParameterTypes() [nonMatchIndex] ==
                    paramTypes[nonMatchIndex] ) {
                return null;
            }
        }
        Class<?> superClass = nonMatchClass.getSuperclass();
        while ( superClass != null ) {
            for ( Method c : candidates ) {
                if ( c.getParameterTypes() [nonMatchIndex].equals ( superClass ) ) {
                    return c;
                }
            }
            superClass = superClass.getSuperclass();
        }
        Method match = null;
        if ( Number.class.isAssignableFrom ( nonMatchClass ) ) {
            for ( Method c : candidates ) {
                Class<?> candidateType = c.getParameterTypes() [nonMatchIndex];
                if ( Number.class.isAssignableFrom ( candidateType ) ||
                        candidateType.isPrimitive() ) {
                    if ( match == null ) {
                        match = c;
                    } else {
                        match = null;
                        break;
                    }
                }
            }
        }
        return match;
    }
    private static boolean isAssignableFrom ( Class<?> src, Class<?> target ) {
        if ( src == null ) {
            return true;
        }
        Class<?> targetClass;
        if ( target.isPrimitive() ) {
            if ( target == Boolean.TYPE ) {
                targetClass = Boolean.class;
            } else if ( target == Character.TYPE ) {
                targetClass = Character.class;
            } else if ( target == Byte.TYPE ) {
                targetClass = Byte.class;
            } else if ( target == Short.TYPE ) {
                targetClass = Short.class;
            } else if ( target == Integer.TYPE ) {
                targetClass = Integer.class;
            } else if ( target == Long.TYPE ) {
                targetClass = Long.class;
            } else if ( target == Float.TYPE ) {
                targetClass = Float.class;
            } else {
                targetClass = Double.class;
            }
        } else {
            targetClass = target;
        }
        return targetClass.isAssignableFrom ( src );
    }
    private static boolean isCoercibleFrom ( EvaluationContext ctx, Object src, Class<?> target ) {
        try {
            ELSupport.coerceToType ( ctx, src, target );
        } catch ( ELException e ) {
            return false;
        }
        return true;
    }
    private static Method getMethod ( Class<?> type, Method m ) {
        if ( m == null || Modifier.isPublic ( type.getModifiers() ) ) {
            return m;
        }
        Class<?>[] inf = type.getInterfaces();
        Method mp = null;
        for ( int i = 0; i < inf.length; i++ ) {
            try {
                mp = inf[i].getMethod ( m.getName(), m.getParameterTypes() );
                mp = getMethod ( mp.getDeclaringClass(), mp );
                if ( mp != null ) {
                    return mp;
                }
            } catch ( NoSuchMethodException e ) {
            }
        }
        Class<?> sup = type.getSuperclass();
        if ( sup != null ) {
            try {
                mp = sup.getMethod ( m.getName(), m.getParameterTypes() );
                mp = getMethod ( mp.getDeclaringClass(), mp );
                if ( mp != null ) {
                    return mp;
                }
            } catch ( NoSuchMethodException e ) {
            }
        }
        return null;
    }
    private static final String paramString ( Class<?>[] types ) {
        if ( types != null ) {
            StringBuilder sb = new StringBuilder();
            for ( int i = 0; i < types.length; i++ ) {
                if ( types[i] == null ) {
                    sb.append ( "null, " );
                } else {
                    sb.append ( types[i].getName() ).append ( ", " );
                }
            }
            if ( sb.length() > 2 ) {
                sb.setLength ( sb.length() - 2 );
            }
            return sb.toString();
        }
        return null;
    }
    private static class MatchResult implements Comparable<MatchResult> {
        private final int exact;
        private final int assignable;
        private final int coercible;
        private final boolean bridge;
        public MatchResult ( int exact, int assignable, int coercible, boolean bridge ) {
            this.exact = exact;
            this.assignable = assignable;
            this.coercible = coercible;
            this.bridge = bridge;
        }
        public int getExact() {
            return exact;
        }
        public int getAssignable() {
            return assignable;
        }
        public int getCoercible() {
            return coercible;
        }
        public boolean isBridge() {
            return bridge;
        }
        @Override
        public int compareTo ( MatchResult o ) {
            int cmp = Integer.compare ( this.getExact(), o.getExact() );
            if ( cmp == 0 ) {
                cmp = Integer.compare ( this.getAssignable(), o.getAssignable() );
                if ( cmp == 0 ) {
                    cmp = Integer.compare ( this.getCoercible(), o.getCoercible() );
                    if ( cmp == 0 ) {
                        cmp = Boolean.compare ( o.isBridge(), this.isBridge() );
                    }
                }
            }
            return cmp;
        }
    }
}
