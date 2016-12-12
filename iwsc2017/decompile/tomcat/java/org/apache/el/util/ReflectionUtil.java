package org.apache.el.util;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import javax.el.ELException;
import javax.el.ELContext;
import org.apache.el.lang.ELSupport;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import javax.el.MethodNotFoundException;
import java.lang.reflect.Method;
import org.apache.el.lang.EvaluationContext;
import java.util.Arrays;
import java.lang.reflect.Array;
public class ReflectionUtil {
    protected static final String[] PRIMITIVE_NAMES;
    protected static final Class<?>[] PRIMITIVES;
    public static Class<?> forName ( final String name ) throws ClassNotFoundException {
        if ( null == name || "".equals ( name ) ) {
            return null;
        }
        Class<?> c = forNamePrimitive ( name );
        if ( c == null ) {
            if ( name.endsWith ( "[]" ) ) {
                final String nc = name.substring ( 0, name.length() - 2 );
                c = Class.forName ( nc, true, Thread.currentThread().getContextClassLoader() );
                c = Array.newInstance ( c, 0 ).getClass();
            } else {
                c = Class.forName ( name, true, Thread.currentThread().getContextClassLoader() );
            }
        }
        return c;
    }
    protected static Class<?> forNamePrimitive ( final String name ) {
        if ( name.length() <= 8 ) {
            final int p = Arrays.binarySearch ( ReflectionUtil.PRIMITIVE_NAMES, name );
            if ( p >= 0 ) {
                return ReflectionUtil.PRIMITIVES[p];
            }
        }
        return null;
    }
    public static Class<?>[] toTypeArray ( final String[] s ) throws ClassNotFoundException {
        if ( s == null ) {
            return null;
        }
        final Class<?>[] c = ( Class<?>[] ) new Class[s.length];
        for ( int i = 0; i < s.length; ++i ) {
            c[i] = forName ( s[i] );
        }
        return c;
    }
    public static String[] toTypeNameArray ( final Class<?>[] c ) {
        if ( c == null ) {
            return null;
        }
        final String[] s = new String[c.length];
        for ( int i = 0; i < c.length; ++i ) {
            s[i] = c[i].getName();
        }
        return s;
    }
    public static Method getMethod ( final EvaluationContext ctx, final Object base, final Object property, final Class<?>[] paramTypes, final Object[] paramValues ) throws MethodNotFoundException {
        if ( base == null || property == null ) {
            throw new MethodNotFoundException ( MessageFactory.get ( "error.method.notfound", base, property, paramString ( paramTypes ) ) );
        }
        final String methodName = ( String ) ( ( property instanceof String ) ? property : property.toString() );
        int paramCount;
        if ( paramTypes == null ) {
            paramCount = 0;
        } else {
            paramCount = paramTypes.length;
        }
        final Method[] methods = base.getClass().getMethods();
        final Map<Method, MatchResult> candidates = new HashMap<Method, MatchResult>();
        for ( final Method m : methods ) {
            Label_0447: {
                if ( m.getName().equals ( methodName ) ) {
                    final Class<?>[] mParamTypes = m.getParameterTypes();
                    int mParamCount;
                    if ( mParamTypes == null ) {
                        mParamCount = 0;
                    } else {
                        mParamCount = mParamTypes.length;
                    }
                    if ( paramCount != mParamCount ) {
                        if ( !m.isVarArgs() ) {
                            break Label_0447;
                        }
                        if ( paramCount < mParamCount ) {
                            break Label_0447;
                        }
                    }
                    int exactMatch = 0;
                    int assignableMatch = 0;
                    int coercibleMatch = 0;
                    boolean noMatch = false;
                    for ( int i = 0; i < mParamCount; ++i ) {
                        if ( mParamTypes[i].equals ( paramTypes[i] ) ) {
                            ++exactMatch;
                        } else if ( i == mParamCount - 1 && m.isVarArgs() ) {
                            final Class<?> varType = mParamTypes[i].getComponentType();
                            for ( int j = i; j < paramCount; ++j ) {
                                if ( isAssignableFrom ( paramTypes[j], varType ) ) {
                                    ++assignableMatch;
                                } else {
                                    if ( paramValues == null ) {
                                        noMatch = true;
                                        break;
                                    }
                                    if ( !isCoercibleFrom ( ctx, paramValues[j], varType ) ) {
                                        noMatch = true;
                                        break;
                                    }
                                    ++coercibleMatch;
                                }
                            }
                        } else if ( isAssignableFrom ( paramTypes[i], mParamTypes[i] ) ) {
                            ++assignableMatch;
                        } else {
                            if ( paramValues == null ) {
                                noMatch = true;
                                break;
                            }
                            if ( !isCoercibleFrom ( ctx, paramValues[i], mParamTypes[i] ) ) {
                                noMatch = true;
                                break;
                            }
                            ++coercibleMatch;
                        }
                    }
                    if ( !noMatch ) {
                        if ( exactMatch == paramCount ) {
                            return getMethod ( base.getClass(), m );
                        }
                        candidates.put ( m, new MatchResult ( exactMatch, assignableMatch, coercibleMatch, m.isBridge() ) );
                    }
                }
            }
        }
        MatchResult bestMatch = new MatchResult ( 0, 0, 0, false );
        Method match = null;
        boolean multiple = false;
        for ( final Map.Entry<Method, MatchResult> entry : candidates.entrySet() ) {
            final int cmp = entry.getValue().compareTo ( bestMatch );
            if ( cmp > 0 || match == null ) {
                bestMatch = entry.getValue();
                match = entry.getKey();
                multiple = false;
            } else {
                if ( cmp != 0 ) {
                    continue;
                }
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
                throw new MethodNotFoundException ( MessageFactory.get ( "error.method.ambiguous", base, property, paramString ( paramTypes ) ) );
            }
        }
        if ( match == null ) {
            throw new MethodNotFoundException ( MessageFactory.get ( "error.method.notfound", base, property, paramString ( paramTypes ) ) );
        }
        return getMethod ( base.getClass(), match );
    }
    private static Method resolveAmbiguousMethod ( final Set<Method> candidates, final Class<?>[] paramTypes ) {
        final Method m = candidates.iterator().next();
        int nonMatchIndex = 0;
        Class<?> nonMatchClass = null;
        for ( int i = 0; i < paramTypes.length; ++i ) {
            if ( m.getParameterTypes() [i] != paramTypes[i] ) {
                nonMatchIndex = i;
                nonMatchClass = paramTypes[i];
                break;
            }
        }
        if ( nonMatchClass == null ) {
            return null;
        }
        for ( final Method c : candidates ) {
            if ( c.getParameterTypes() [nonMatchIndex] == paramTypes[nonMatchIndex] ) {
                return null;
            }
        }
        for ( Class<?> superClass = nonMatchClass.getSuperclass(); superClass != null; superClass = superClass.getSuperclass() ) {
            for ( final Method c2 : candidates ) {
                if ( c2.getParameterTypes() [nonMatchIndex].equals ( superClass ) ) {
                    return c2;
                }
            }
        }
        Method match = null;
        if ( Number.class.isAssignableFrom ( nonMatchClass ) ) {
            for ( final Method c3 : candidates ) {
                final Class<?> candidateType = c3.getParameterTypes() [nonMatchIndex];
                if ( Number.class.isAssignableFrom ( candidateType ) || candidateType.isPrimitive() ) {
                    if ( match != null ) {
                        match = null;
                        break;
                    }
                    match = c3;
                }
            }
        }
        return match;
    }
    private static boolean isAssignableFrom ( final Class<?> src, final Class<?> target ) {
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
    private static boolean isCoercibleFrom ( final EvaluationContext ctx, final Object src, final Class<?> target ) {
        try {
            ELSupport.coerceToType ( ctx, src, target );
        } catch ( ELException e ) {
            return false;
        }
        return true;
    }
    private static Method getMethod ( final Class<?> type, final Method m ) {
        if ( m == null || Modifier.isPublic ( type.getModifiers() ) ) {
            return m;
        }
        final Class<?>[] inf = type.getInterfaces();
        Method mp = null;
        for ( int i = 0; i < inf.length; ++i ) {
            try {
                mp = inf[i].getMethod ( m.getName(), m.getParameterTypes() );
                mp = getMethod ( mp.getDeclaringClass(), mp );
                if ( mp != null ) {
                    return mp;
                }
            } catch ( NoSuchMethodException ex ) {}
        }
        final Class<?> sup = type.getSuperclass();
        if ( sup != null ) {
            try {
                mp = sup.getMethod ( m.getName(), m.getParameterTypes() );
                mp = getMethod ( mp.getDeclaringClass(), mp );
                if ( mp != null ) {
                    return mp;
                }
            } catch ( NoSuchMethodException ex2 ) {}
        }
        return null;
    }
    private static final String paramString ( final Class<?>[] types ) {
        if ( types != null ) {
            final StringBuilder sb = new StringBuilder();
            for ( int i = 0; i < types.length; ++i ) {
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
    static {
        PRIMITIVE_NAMES = new String[] { "boolean", "byte", "char", "double", "float", "int", "long", "short", "void" };
        PRIMITIVES = new Class[] { Boolean.TYPE, Byte.TYPE, Character.TYPE, Double.TYPE, Float.TYPE, Integer.TYPE, Long.TYPE, Short.TYPE, Void.TYPE };
    }
    private static class MatchResult implements Comparable<MatchResult> {
        private final int exact;
        private final int assignable;
        private final int coercible;
        private final boolean bridge;
        public MatchResult ( final int exact, final int assignable, final int coercible, final boolean bridge ) {
            this.exact = exact;
            this.assignable = assignable;
            this.coercible = coercible;
            this.bridge = bridge;
        }
        public int getExact() {
            return this.exact;
        }
        public int getAssignable() {
            return this.assignable;
        }
        public int getCoercible() {
            return this.coercible;
        }
        public boolean isBridge() {
            return this.bridge;
        }
        @Override
        public int compareTo ( final MatchResult o ) {
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
