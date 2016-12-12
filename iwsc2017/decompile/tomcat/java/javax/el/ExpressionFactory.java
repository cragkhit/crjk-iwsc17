package javax.el;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.concurrent.locks.Lock;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.Map;
public abstract class ExpressionFactory {
    private static final boolean IS_SECURITY_ENABLED;
    private static final String SERVICE_RESOURCE_NAME = "META-INF/services/javax.el.ExpressionFactory";
    private static final String PROPERTY_NAME = "javax.el.ExpressionFactory";
    private static final String PROPERTY_FILE;
    private static final CacheValue nullTcclFactory;
    private static final Map<CacheKey, CacheValue> factoryCache;
    public static ExpressionFactory newInstance() {
        return newInstance ( null );
    }
    public static ExpressionFactory newInstance ( final Properties properties ) {
        ExpressionFactory result = null;
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        CacheValue cacheValue;
        if ( tccl == null ) {
            cacheValue = ExpressionFactory.nullTcclFactory;
        } else {
            final CacheKey key = new CacheKey ( tccl );
            cacheValue = ExpressionFactory.factoryCache.get ( key );
            if ( cacheValue == null ) {
                final CacheValue newCacheValue = new CacheValue();
                cacheValue = ExpressionFactory.factoryCache.putIfAbsent ( key, newCacheValue );
                if ( cacheValue == null ) {
                    cacheValue = newCacheValue;
                }
            }
        }
        final Lock readLock = cacheValue.getLock().readLock();
        readLock.lock();
        Class<?> clazz;
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
                throw new ELException ( "Unable to find ExpressionFactory of type: " + className, e );
            }
        }
        try {
            Constructor<?> constructor = null;
            if ( properties != null ) {
                try {
                    constructor = clazz.getConstructor ( Properties.class );
                } catch ( SecurityException se ) {
                    throw new ELException ( se );
                } catch ( NoSuchMethodException ex ) {}
            }
            if ( constructor == null ) {
                result = ( ExpressionFactory ) clazz.newInstance();
            } else {
                result = ( ExpressionFactory ) constructor.newInstance ( properties );
            }
        } catch ( InstantiationException | IllegalAccessException | IllegalArgumentException e2 ) {
            throw new ELException ( "Unable to create ExpressionFactory of type: " + clazz.getName(), e2 );
        } catch ( InvocationTargetException e3 ) {
            final Throwable cause = e3.getCause();
            Util.handleThrowable ( cause );
            throw new ELException ( "Unable to create ExpressionFactory of type: " + clazz.getName(), e3 );
        }
        return result;
    }
    public abstract ValueExpression createValueExpression ( final ELContext p0, final String p1, final Class<?> p2 );
    public abstract ValueExpression createValueExpression ( final Object p0, final Class<?> p1 );
    public abstract MethodExpression createMethodExpression ( final ELContext p0, final String p1, final Class<?> p2, final Class<?>[] p3 );
    public abstract Object coerceToType ( final Object p0, final Class<?> p1 );
    public ELResolver getStreamELResolver() {
        return null;
    }
    public Map<String, Method> getInitFunctionMap() {
        return null;
    }
    private static String discoverClassName ( final ClassLoader tccl ) {
        String className = null;
        className = getClassNameServices ( tccl );
        if ( className == null ) {
            if ( ExpressionFactory.IS_SECURITY_ENABLED ) {
                className = AccessController.doPrivileged ( ( PrivilegedAction<String> ) new PrivilegedAction<String>() {
                    @Override
                    public String run() {
                        return getClassNameJreDir();
                    }
                } );
            } else {
                className = getClassNameJreDir();
            }
        }
        if ( className == null ) {
            if ( ExpressionFactory.IS_SECURITY_ENABLED ) {
                className = AccessController.doPrivileged ( ( PrivilegedAction<String> ) new PrivilegedAction<String>() {
                    @Override
                    public String run() {
                        return getClassNameSysProp();
                    }
                } );
            } else {
                className = getClassNameSysProp();
            }
        }
        if ( className == null ) {
            className = "org.apache.el.ExpressionFactoryImpl";
        }
        return className;
    }
    private static String getClassNameServices ( final ClassLoader tccl ) {
        InputStream is = null;
        if ( tccl == null ) {
            is = ClassLoader.getSystemResourceAsStream ( "META-INF/services/javax.el.ExpressionFactory" );
        } else {
            is = tccl.getResourceAsStream ( "META-INF/services/javax.el.ExpressionFactory" );
        }
        if ( is != null ) {
            String line = null;
            try ( final InputStreamReader isr = new InputStreamReader ( is, "UTF-8" );
                        final BufferedReader br = new BufferedReader ( isr ) ) {
                line = br.readLine();
                if ( line != null && line.trim().length() > 0 ) {
                    return line.trim();
                }
            } catch ( UnsupportedEncodingException ex ) {}
            catch ( IOException e ) {
                throw new ELException ( "Failed to read META-INF/services/javax.el.ExpressionFactory", e );
            } finally {
                try {
                    is.close();
                } catch ( IOException ex2 ) {}
            }
        }
        return null;
    }
    private static String getClassNameJreDir() {
        final File file = new File ( ExpressionFactory.PROPERTY_FILE );
        if ( file.canRead() ) {
            try ( final InputStream is = new FileInputStream ( file ) ) {
                final Properties props = new Properties();
                props.load ( is );
                final String value = props.getProperty ( "javax.el.ExpressionFactory" );
                if ( value != null && value.trim().length() > 0 ) {
                    return value.trim();
                }
            } catch ( FileNotFoundException ex ) {}
            catch ( IOException e ) {
                throw new ELException ( "Failed to read " + ExpressionFactory.PROPERTY_FILE, e );
            }
        }
        return null;
    }
    private static final String getClassNameSysProp() {
        final String value = System.getProperty ( "javax.el.ExpressionFactory" );
        if ( value != null && value.trim().length() > 0 ) {
            return value.trim();
        }
        return null;
    }
    static {
        IS_SECURITY_ENABLED = ( System.getSecurityManager() != null );
        nullTcclFactory = new CacheValue();
        factoryCache = new ConcurrentHashMap<CacheKey, CacheValue>();
        if ( ExpressionFactory.IS_SECURITY_ENABLED ) {
            PROPERTY_FILE = AccessController.doPrivileged ( ( PrivilegedAction<String> ) new PrivilegedAction<String>() {
                @Override
                public String run() {
                    return System.getProperty ( "java.home" ) + File.separator + "lib" + File.separator + "el.properties";
                }
            } );
        } else {
            PROPERTY_FILE = System.getProperty ( "java.home" ) + File.separator + "lib" + File.separator + "el.properties";
        }
    }
    private static class CacheKey {
        private final int hash;
        private final WeakReference<ClassLoader> ref;
        public CacheKey ( final ClassLoader cl ) {
            this.hash = cl.hashCode();
            this.ref = new WeakReference<ClassLoader> ( cl );
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
            final ClassLoader thisCl = this.ref.get();
            return thisCl != null && thisCl == ( ( CacheKey ) obj ).ref.get();
        }
    }
    private static class CacheValue {
        private final ReadWriteLock lock;
        private String className;
        private WeakReference<Class<?>> ref;
        public CacheValue() {
            this.lock = new ReentrantReadWriteLock();
        }
        public ReadWriteLock getLock() {
            return this.lock;
        }
        public String getFactoryClassName() {
            return this.className;
        }
        public void setFactoryClassName ( final String className ) {
            this.className = className;
        }
        public Class<?> getFactoryClass() {
            return ( this.ref != null ) ? this.ref.get() : null;
        }
        public void setFactoryClass ( final Class<?> clazz ) {
            this.ref = new WeakReference<Class<?>> ( clazz );
        }
    }
}
