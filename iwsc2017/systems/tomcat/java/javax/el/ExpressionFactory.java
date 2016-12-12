package javax.el;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
public abstract class ExpressionFactory {
    private static final boolean IS_SECURITY_ENABLED =
        ( System.getSecurityManager() != null );
    private static final String SERVICE_RESOURCE_NAME =
        "META-INF/services/javax.el.ExpressionFactory";
    private static final String PROPERTY_NAME = "javax.el.ExpressionFactory";
    private static final String PROPERTY_FILE;
    private static final CacheValue nullTcclFactory = new CacheValue();
    private static final Map<CacheKey, CacheValue> factoryCache = new ConcurrentHashMap<>();
    static {
        if ( IS_SECURITY_ENABLED ) {
            PROPERTY_FILE = AccessController.doPrivileged (
            new PrivilegedAction<String>() {
                @Override
                public String run() {
                    return System.getProperty ( "java.home" ) + File.separator +
                           "lib" + File.separator + "el.properties";
                }
            }
                            );
        } else {
            PROPERTY_FILE = System.getProperty ( "java.home" ) + File.separator + "lib" +
                            File.separator + "el.properties";
        }
    }
    public static ExpressionFactory newInstance() {
        return newInstance ( null );
    }
    public static ExpressionFactory newInstance ( Properties properties ) {
        ExpressionFactory result = null;
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        CacheValue cacheValue;
        Class<?> clazz;
        if ( tccl == null ) {
            cacheValue = nullTcclFactory;
        } else {
            CacheKey key = new CacheKey ( tccl );
            cacheValue = factoryCache.get ( key );
            if ( cacheValue == null ) {
                CacheValue newCacheValue = new CacheValue();
                cacheValue = factoryCache.putIfAbsent ( key, newCacheValue );
                if ( cacheValue == null ) {
                    cacheValue = newCacheValue;
                }
            }
        }
        final Lock readLock = cacheValue.getLock().readLock();
        readLock.lock();
        try {
            clazz = cacheValue.getFactoryClass();
        } finally {
            readLock.unlock();
        }
        if ( clazz == null ) {
            String className = null;
            try {
                final Lock writeLock = cacheValue.getLock().writeLock();
                writeLock.lock();
                try {
                    className = cacheValue.getFactoryClassName();
                    if ( className == null ) {
                        className = discoverClassName ( tccl );
                        cacheValue.setFactoryClassName ( className );
                    }
                    if ( tccl == null ) {
                        clazz = Class.forName ( className );
                    } else {
                        clazz = tccl.loadClass ( className );
                    }
                    cacheValue.setFactoryClass ( clazz );
                } finally {
                    writeLock.unlock();
                }
            } catch ( ClassNotFoundException e ) {
                throw new ELException (
                    "Unable to find ExpressionFactory of type: " + className,
                    e );
            }
        }
        try {
            Constructor<?> constructor = null;
            if ( properties != null ) {
                try {
                    constructor = clazz.getConstructor ( Properties.class );
                } catch ( SecurityException se ) {
                    throw new ELException ( se );
                } catch ( NoSuchMethodException nsme ) {
                }
            }
            if ( constructor == null ) {
                result = ( ExpressionFactory ) clazz.newInstance();
            } else {
                result =
                    ( ExpressionFactory ) constructor.newInstance ( properties );
            }
        } catch ( InstantiationException | IllegalAccessException | IllegalArgumentException e ) {
            throw new ELException (
                "Unable to create ExpressionFactory of type: " + clazz.getName(),
                e );
        } catch ( InvocationTargetException e ) {
            Throwable cause = e.getCause();
            Util.handleThrowable ( cause );
            throw new ELException (
                "Unable to create ExpressionFactory of type: " + clazz.getName(),
                e );
        }
        return result;
    }
    public abstract ValueExpression createValueExpression ( ELContext context,
            String expression, Class<?> expectedType );
    public abstract ValueExpression createValueExpression ( Object instance,
            Class<?> expectedType );
    public abstract MethodExpression createMethodExpression ( ELContext context,
            String expression, Class<?> expectedReturnType,
            Class<?>[] expectedParamTypes );
    public abstract Object coerceToType ( Object obj, Class<?> expectedType );
    public ELResolver getStreamELResolver() {
        return null;
    }
    public Map<String, Method> getInitFunctionMap() {
        return null;
    }
    private static class CacheKey {
        private final int hash;
        private final WeakReference<ClassLoader> ref;
        public CacheKey ( ClassLoader cl ) {
            hash = cl.hashCode();
            ref = new WeakReference<> ( cl );
        }
        @Override
        public int hashCode() {
            return hash;
        }
        @Override
        public boolean equals ( Object obj ) {
            if ( obj == this ) {
                return true;
            }
            if ( ! ( obj instanceof CacheKey ) ) {
                return false;
            }
            ClassLoader thisCl = ref.get();
            if ( thisCl == null ) {
                return false;
            }
            return thisCl == ( ( CacheKey ) obj ).ref.get();
        }
    }
    private static class CacheValue {
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private String className;
        private WeakReference<Class<?>> ref;
        public CacheValue() {
        }
        public ReadWriteLock getLock() {
            return lock;
        }
        public String getFactoryClassName() {
            return className;
        }
        public void setFactoryClassName ( String className ) {
            this.className = className;
        }
        public Class<?> getFactoryClass() {
            return ref != null ? ref.get() : null;
        }
        public void setFactoryClass ( Class<?> clazz ) {
            ref = new WeakReference<> ( clazz );
        }
    }
    private static String discoverClassName ( ClassLoader tccl ) {
        String className = null;
        className = getClassNameServices ( tccl );
        if ( className == null ) {
            if ( IS_SECURITY_ENABLED ) {
                className = AccessController.doPrivileged (
                new PrivilegedAction<String>() {
                    @Override
                    public String run() {
                        return getClassNameJreDir();
                    }
                }
                            );
            } else {
                className = getClassNameJreDir();
            }
        }
        if ( className == null ) {
            if ( IS_SECURITY_ENABLED ) {
                className = AccessController.doPrivileged (
                new PrivilegedAction<String>() {
                    @Override
                    public String run() {
                        return getClassNameSysProp();
                    }
                }
                            );
            } else {
                className = getClassNameSysProp();
            }
        }
        if ( className == null ) {
            className = "org.apache.el.ExpressionFactoryImpl";
        }
        return className;
    }
    private static String getClassNameServices ( ClassLoader tccl ) {
        InputStream is = null;
        if ( tccl == null ) {
            is = ClassLoader.getSystemResourceAsStream ( SERVICE_RESOURCE_NAME );
        } else {
            is = tccl.getResourceAsStream ( SERVICE_RESOURCE_NAME );
        }
        if ( is != null ) {
            String line = null;
            try ( InputStreamReader isr = new InputStreamReader ( is, "UTF-8" );
                        BufferedReader br = new BufferedReader ( isr ) ) {
                line = br.readLine();
                if ( line != null && line.trim().length() > 0 ) {
                    return line.trim();
                }
            } catch ( UnsupportedEncodingException e ) {
            } catch ( IOException e ) {
                throw new ELException ( "Failed to read " + SERVICE_RESOURCE_NAME,
                                        e );
            } finally {
                try {
                    is.close();
                } catch ( IOException ioe ) { }
            }
        }
        return null;
    }
    private static String getClassNameJreDir() {
        File file = new File ( PROPERTY_FILE );
        if ( file.canRead() ) {
            try ( InputStream is = new FileInputStream ( file ) ) {
                Properties props = new Properties();
                props.load ( is );
                String value = props.getProperty ( PROPERTY_NAME );
                if ( value != null && value.trim().length() > 0 ) {
                    return value.trim();
                }
            } catch ( FileNotFoundException e ) {
            } catch ( IOException e ) {
                throw new ELException ( "Failed to read " + PROPERTY_FILE, e );
            }
        }
        return null;
    }
    private static final String getClassNameSysProp() {
        String value = System.getProperty ( PROPERTY_NAME );
        if ( value != null && value.trim().length() > 0 ) {
            return value.trim();
        }
        return null;
    }
}
