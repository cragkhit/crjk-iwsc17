package javax.el;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.lang.reflect.Method;
import java.util.concurrent.locks.Lock;
import java.util.MissingResourceException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.Map;
class Util {
    private static final CacheValue nullTcclFactory;
    private static final Map<CacheKey, CacheValue> factoryCache;
    static void handleThrowable ( final Throwable t ) {
        if ( t instanceof ThreadDeath ) {
            throw ( ThreadDeath ) t;
        }
        if ( t instanceof VirtualMachineError ) {
            throw ( VirtualMachineError ) t;
        }
    }
    static String message ( final ELContext context, final String name, final Object... props ) {
        Locale locale = null;
        if ( context != null ) {
            locale = context.getLocale();
        }
        if ( locale == null ) {
            locale = Locale.getDefault();
            if ( locale == null ) {
                return "";
            }
        }
        final ResourceBundle bundle = ResourceBundle.getBundle ( "javax.el.LocalStrings", locale );
        try {
            String template = bundle.getString ( name );
            if ( props != null ) {
                template = MessageFormat.format ( template, props );
            }
            return template;
        } catch ( MissingResourceException e ) {
            return "Missing Resource: '" + name + "' for Locale " + locale.getDisplayName();
        }
    }
    static ExpressionFactory getExpressionFactory() {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        CacheValue cacheValue = null;
        ExpressionFactory factory = null;
        if ( tccl == null ) {
            cacheValue = Util.nullTcclFactory;
        } else {
            final CacheKey key = new CacheKey ( tccl );
            cacheValue = Util.factoryCache.get ( key );
            if ( cacheValue == null ) {
                final CacheValue newCacheValue = new CacheValue();
                cacheValue = Util.factoryCache.putIfAbsent ( key, newCacheValue );
                if ( cacheValue == null ) {
                    cacheValue = newCacheValue;
                }
            }
        }
        final Lock readLock = cacheValue.getLock().readLock();
        readLock.lock();
        try {
            factory = cacheValue.getExpressionFactory();
        } finally {
            readLock.unlock();
        }
        if ( factory == null ) {
            final Lock writeLock = cacheValue.getLock().writeLock();
            writeLock.lock();
            try {
                factory = cacheValue.getExpressionFactory();
                if ( factory == null ) {
                    factory = ExpressionFactory.newInstance();
                    cacheValue.setExpressionFactory ( factory );
                }
            } finally {
                writeLock.unlock();
            }
        }
        return factory;
    }
    static Method findMethod ( final Class<?> clazz, final String methodName, Class<?>[] paramTypes, final Object[] paramValues ) {
        if ( clazz == null || methodName == null ) {
            throw new MethodNotFoundException ( message ( null, "util.method.notfound", clazz, methodName, paramString ( paramTypes ) ) );
        }
        if ( paramTypes == null ) {
            paramTypes = getTypesFromValues ( paramValues );
        }
        final Method[] methods = clazz.getMethods();
        final List<Wrapper> wrappers = Wrapper.wrap ( methods, methodName );
        final Wrapper result = findWrapper ( clazz, wrappers, methodName, paramTypes, paramValues );
        if ( result == null ) {
            return null;
        }
        return getMethod ( clazz, ( Method ) result.unWrap() );
    }
    private static Wrapper findWrapper ( final Class<?> clazz, final List<Wrapper> wrappers, final String name, final Class<?>[] paramTypes, final Object[] paramValues ) {
        final Map<Wrapper, MatchResult> candidates = new HashMap<Wrapper, MatchResult>();
        int paramCount;
        if ( paramTypes == null ) {
            paramCount = 0;
        } else {
            paramCount = paramTypes.length;
        }
        for ( final Wrapper w : wrappers ) {
            final Class<?>[] mParamTypes = w.getParameterTypes();
            int mParamCount;
            if ( mParamTypes == null ) {
                mParamCount = 0;
            } else {
                mParamCount = mParamTypes.length;
            }
            if ( paramCount != mParamCount ) {
                if ( !w.isVarArgs() ) {
                    continue;
                }
                if ( paramCount < mParamCount ) {
                    continue;
                }
            }
            int exactMatch = 0;
            int assignableMatch = 0;
            int coercibleMatch = 0;
            boolean noMatch = false;
            for ( int i = 0; i < mParamCount; ++i ) {
                if ( mParamTypes[i].equals ( paramTypes[i] ) ) {
                    ++exactMatch;
                } else if ( i == mParamCount - 1 && w.isVarArgs() ) {
                    final Class<?> varType = mParamTypes[i].getComponentType();
                    for ( int j = i; j < paramCount; ++j ) {
                        if ( isAssignableFrom ( paramTypes[j], varType ) ) {
                            ++assignableMatch;
                        } else {
                            if ( paramValues == null ) {
                                noMatch = true;
                                break;
                            }
                            if ( !isCoercibleFrom ( paramValues[j], varType ) ) {
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
                    if ( !isCoercibleFrom ( paramValues[i], mParamTypes[i] ) ) {
                        noMatch = true;
                        break;
                    }
                    ++coercibleMatch;
                }
            }
            if ( noMatch ) {
                continue;
            }
            if ( exactMatch == paramCount ) {
                return w;
            }
            candidates.put ( w, new MatchResult ( exactMatch, assignableMatch, coercibleMatch, w.isBridge() ) );
        }
        MatchResult bestMatch = new MatchResult ( 0, 0, 0, false );
        Wrapper match = null;
        boolean multiple = false;
        for ( final Map.Entry<Wrapper, MatchResult> entry : candidates.entrySet() ) {
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
                match = resolveAmbiguousWrapper ( candidates.keySet(), paramTypes );
            } else {
                match = null;
            }
            if ( match == null ) {
                throw new MethodNotFoundException ( message ( null, "util.method.ambiguous", clazz, name, paramString ( paramTypes ) ) );
            }
        }
        if ( match == null ) {
            throw new MethodNotFoundException ( message ( null, "util.method.notfound", clazz, name, paramString ( paramTypes ) ) );
        }
        return match;
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
    private static Wrapper resolveAmbiguousWrapper ( final Set<Wrapper> candidates, final Class<?>[] paramTypes ) {
        final Wrapper w = candidates.iterator().next();
        int nonMatchIndex = 0;
        Class<?> nonMatchClass = null;
        for ( int i = 0; i < paramTypes.length; ++i ) {
            if ( w.getParameterTypes() [i] != paramTypes[i] ) {
                nonMatchIndex = i;
                nonMatchClass = paramTypes[i];
                break;
            }
        }
        if ( nonMatchClass == null ) {
            return null;
        }
        for ( final Wrapper c : candidates ) {
            if ( c.getParameterTypes() [nonMatchIndex] == paramTypes[nonMatchIndex] ) {
                return null;
            }
        }
        for ( Class<?> superClass = nonMatchClass.getSuperclass(); superClass != null; superClass = superClass.getSuperclass() ) {
            for ( final Wrapper c2 : candidates ) {
                if ( c2.getParameterTypes() [nonMatchIndex].equals ( superClass ) ) {
                    return c2;
                }
            }
        }
        Wrapper match = null;
        if ( Number.class.isAssignableFrom ( nonMatchClass ) ) {
            for ( final Wrapper c3 : candidates ) {
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
    static boolean isAssignableFrom ( final Class<?> src, final Class<?> target ) {
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
    private static boolean isCoercibleFrom ( final Object src, final Class<?> target ) {
        try {
            getExpressionFactory().coerceToType ( src, target );
        } catch ( ELException e ) {
            return false;
        }
        return true;
    }
    private static Class<?>[] getTypesFromValues ( final Object[] values ) {
        if ( values == null ) {
            return null;
        }
        final Class<?>[] result = ( Class<?>[] ) new Class[values.length];
        for ( int i = 0; i < values.length; ++i ) {
            if ( values[i] == null ) {
                result[i] = null;
            } else {
                result[i] = values[i].getClass();
            }
        }
        return result;
    }
    static Method getMethod ( final Class<?> type, final Method m ) {
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
    static Constructor<?> findConstructor ( final Class<?> clazz, Class<?>[] paramTypes, final Object[] paramValues ) {
        final String methodName = "<init>";
        if ( clazz == null ) {
            throw new MethodNotFoundException ( message ( null, "util.method.notfound", clazz, methodName, paramString ( paramTypes ) ) );
        }
        if ( paramTypes == null ) {
            paramTypes = getTypesFromValues ( paramValues );
        }
        final Constructor<?>[] constructors = clazz.getConstructors();
        final List<Wrapper> wrappers = Wrapper.wrap ( constructors );
        final Wrapper result = findWrapper ( clazz, wrappers, methodName, paramTypes, paramValues );
        if ( result == null ) {
            return null;
        }
        return getConstructor ( clazz, ( Constructor<?> ) result.unWrap() );
    }
    static Constructor<?> getConstructor ( final Class<?> type, final Constructor<?> c ) {
        if ( c == null || Modifier.isPublic ( type.getModifiers() ) ) {
            return c;
        }
        Constructor<?> cp = null;
        final Class<?> sup = type.getSuperclass();
        if ( sup != null ) {
            try {
                cp = sup.getConstructor ( c.getParameterTypes() );
                cp = getConstructor ( cp.getDeclaringClass(), cp );
                if ( cp != null ) {
                    return cp;
                }
            } catch ( NoSuchMethodException ex ) {}
        }
        return null;
    }
    static Object[] buildParameters ( final Class<?>[] parameterTypes, final boolean isVarArgs, final Object[] params ) {
        final ExpressionFactory factory = getExpressionFactory();
        Object[] parameters = null;
        if ( parameterTypes.length > 0 ) {
            parameters = new Object[parameterTypes.length];
            final int paramCount = params.length;
            if ( isVarArgs ) {
                final int varArgIndex = parameterTypes.length - 1;
                for ( int i = 0; i < varArgIndex; ++i ) {
                    parameters[i] = factory.coerceToType ( params[i], parameterTypes[i] );
                }
                final Class<?> varArgClass = parameterTypes[varArgIndex].getComponentType();
                final Object varargs = Array.newInstance ( varArgClass, paramCount - varArgIndex );
                for ( int j = varArgIndex; j < paramCount; ++j ) {
                    Array.set ( varargs, j - varArgIndex, factory.coerceToType ( params[j], varArgClass ) );
                }
                parameters[varArgIndex] = varargs;
            } else {
                parameters = new Object[parameterTypes.length];
                for ( int k = 0; k < parameterTypes.length; ++k ) {
                    parameters[k] = factory.coerceToType ( params[k], parameterTypes[k] );
                }
            }
        }
        return parameters;
    }
    static {
        nullTcclFactory = new CacheValue();
        factoryCache = new ConcurrentHashMap<CacheKey, CacheValue>();
    }
    private static class CacheKey {
        private final int hash;
        private final WeakReference<ClassLoader> ref;
        public CacheKey ( final ClassLoader key ) {
            this.hash = key.hashCode();
            this.ref = new WeakReference<ClassLoader> ( key );
        }
        @Override
        public int hashCode() {
            return this.hash;
        }
        @Override
        public boolean equals ( final Object obj ) {
            if ( obj == this ) {
                return true;
            }
            if ( ! ( obj instanceof CacheKey ) ) {
                return false;
            }
            final ClassLoader thisKey = this.ref.get();
            return thisKey != null && thisKey == ( ( CacheKey ) obj ).ref.get();
        }
    }
    private static class CacheValue {
        private final ReadWriteLock lock;
        private WeakReference<ExpressionFactory> ref;
        public CacheValue() {
            this.lock = new ReentrantReadWriteLock();
        }
        public ReadWriteLock getLock() {
            return this.lock;
        }
        public ExpressionFactory getExpressionFactory() {
            return ( this.ref != null ) ? this.ref.get() : null;
        }
        public void setExpressionFactory ( final ExpressionFactory factory ) {
            this.ref = new WeakReference<ExpressionFactory> ( factory );
        }
    }
    private abstract static class Wrapper {
        public static List<Wrapper> wrap ( final Method[] methods, final String name ) {
            final List<Wrapper> result = new ArrayList<Wrapper>();
            for ( final Method method : methods ) {
                if ( method.getName().equals ( name ) ) {
                    result.add ( new MethodWrapper ( method ) );
                }
            }
            return result;
        }
        public static List<Wrapper> wrap ( final Constructor<?>[] constructors ) {
            final List<Wrapper> result = new ArrayList<Wrapper>();
            for ( final Constructor<?> constructor : constructors ) {
                result.add ( new ConstructorWrapper ( constructor ) );
            }
            return result;
        }
        public abstract Object unWrap();
        public abstract Class<?>[] getParameterTypes();
        public abstract boolean isVarArgs();
        public abstract boolean isBridge();
    }
    private static class MethodWrapper extends Wrapper {
        private final Method m;
        public MethodWrapper ( final Method m ) {
            this.m = m;
        }
        @Override
        public Object unWrap() {
            return this.m;
        }
        @Override
        public Class<?>[] getParameterTypes() {
            return this.m.getParameterTypes();
        }
        @Override
        public boolean isVarArgs() {
            return this.m.isVarArgs();
        }
        @Override
        public boolean isBridge() {
            return this.m.isBridge();
        }
    }
    private static class ConstructorWrapper extends Wrapper {
        private final Constructor<?> c;
        public ConstructorWrapper ( final Constructor<?> c ) {
            this.c = c;
        }
        @Override
        public Object unWrap() {
            return this.c;
        }
        @Override
        public Class<?>[] getParameterTypes() {
            return this.c.getParameterTypes();
        }
        @Override
        public boolean isVarArgs() {
            return this.c.isVarArgs();
        }
        @Override
        public boolean isBridge() {
            return false;
        }
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
