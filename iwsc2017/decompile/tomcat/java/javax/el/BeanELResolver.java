package javax.el;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.util.Arrays;
import java.beans.PropertyDescriptor;
import java.beans.Introspector;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
public class BeanELResolver extends ELResolver {
    private static final int CACHE_SIZE;
    private static final String CACHE_SIZE_PROP = "org.apache.el.BeanELResolver.CACHE_SIZE";
    private final boolean readOnly;
    private final ConcurrentCache<String, BeanProperties> cache;
    public BeanELResolver() {
        this.cache = new ConcurrentCache<String, BeanProperties> ( BeanELResolver.CACHE_SIZE );
        this.readOnly = false;
    }
    public BeanELResolver ( final boolean readOnly ) {
        this.cache = new ConcurrentCache<String, BeanProperties> ( BeanELResolver.CACHE_SIZE );
        this.readOnly = readOnly;
    }
    @Override
    public Class<?> getType ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base == null || property == null ) {
            return null;
        }
        context.setPropertyResolved ( base, property );
        return ( Class<?> ) this.property ( context, base, property ).getPropertyType();
    }
    @Override
    public Object getValue ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base == null || property == null ) {
            return null;
        }
        context.setPropertyResolved ( base, property );
        final Method m = this.property ( context, base, property ).read ( context );
        try {
            return m.invoke ( base, ( Object[] ) null );
        } catch ( InvocationTargetException e ) {
            final Throwable cause = e.getCause();
            Util.handleThrowable ( cause );
            throw new ELException ( Util.message ( context, "propertyReadError", base.getClass().getName(), property.toString() ), cause );
        } catch ( Exception e2 ) {
            throw new ELException ( e2 );
        }
    }
    @Override
    public void setValue ( final ELContext context, final Object base, final Object property, final Object value ) {
        Objects.requireNonNull ( context );
        if ( base == null || property == null ) {
            return;
        }
        context.setPropertyResolved ( base, property );
        if ( this.readOnly ) {
            throw new PropertyNotWritableException ( Util.message ( context, "resolverNotWriteable", base.getClass().getName() ) );
        }
        final Method m = this.property ( context, base, property ).write ( context );
        try {
            m.invoke ( base, value );
        } catch ( InvocationTargetException e ) {
            final Throwable cause = e.getCause();
            Util.handleThrowable ( cause );
            throw new ELException ( Util.message ( context, "propertyWriteError", base.getClass().getName(), property.toString() ), cause );
        } catch ( Exception e2 ) {
            throw new ELException ( e2 );
        }
    }
    @Override
    public Object invoke ( final ELContext context, final Object base, final Object method, final Class<?>[] paramTypes, final Object[] params ) {
        Objects.requireNonNull ( context );
        if ( base == null || method == null ) {
            return null;
        }
        final ExpressionFactory factory = ELManager.getExpressionFactory();
        final String methodName = ( String ) factory.coerceToType ( method, String.class );
        final Method matchingMethod = Util.findMethod ( base.getClass(), methodName, paramTypes, params );
        final Object[] parameters = Util.buildParameters ( matchingMethod.getParameterTypes(), matchingMethod.isVarArgs(), params );
        Object result = null;
        try {
            result = matchingMethod.invoke ( base, parameters );
        } catch ( IllegalArgumentException | IllegalAccessException e ) {
            throw new ELException ( e );
        } catch ( InvocationTargetException e2 ) {
            final Throwable cause = e2.getCause();
            Util.handleThrowable ( cause );
            throw new ELException ( cause );
        }
        context.setPropertyResolved ( base, method );
        return result;
    }
    @Override
    public boolean isReadOnly ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base == null || property == null ) {
            return false;
        }
        context.setPropertyResolved ( base, property );
        return this.readOnly || this.property ( context, base, property ).isReadOnly();
    }
    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors ( final ELContext context, final Object base ) {
        if ( base == null ) {
            return null;
        }
        try {
            final BeanInfo info = Introspector.getBeanInfo ( base.getClass() );
            final PropertyDescriptor[] pds = info.getPropertyDescriptors();
            for ( int i = 0; i < pds.length; ++i ) {
                pds[i].setValue ( "resolvableAtDesignTime", Boolean.TRUE );
                pds[i].setValue ( "type", pds[i].getPropertyType() );
            }
            return ( Iterator<FeatureDescriptor> ) Arrays.asList ( ( PropertyDescriptor[] ) pds ).iterator();
        } catch ( IntrospectionException ex ) {
            return null;
        }
    }
    @Override
    public Class<?> getCommonPropertyType ( final ELContext context, final Object base ) {
        if ( base != null ) {
            return Object.class;
        }
        return null;
    }
    private final BeanProperty property ( final ELContext ctx, final Object base, final Object property ) {
        final Class<?> type = base.getClass();
        final String prop = property.toString();
        BeanProperties props = this.cache.get ( type.getName() );
        if ( props == null || type != props.getType() ) {
            props = new BeanProperties ( type );
            this.cache.put ( type.getName(), props );
        }
        return props.get ( ctx, prop );
    }
    static {
        String cacheSizeStr;
        if ( System.getSecurityManager() == null ) {
            cacheSizeStr = System.getProperty ( "org.apache.el.BeanELResolver.CACHE_SIZE", "1000" );
        } else {
            cacheSizeStr = AccessController.doPrivileged ( ( PrivilegedAction<String> ) new PrivilegedAction<String>() {
                @Override
                public String run() {
                    return System.getProperty ( "org.apache.el.BeanELResolver.CACHE_SIZE", "1000" );
                }
            } );
        }
        CACHE_SIZE = Integer.parseInt ( cacheSizeStr );
    }
    static final class BeanProperties {
        private final Map<String, BeanProperty> properties;
        private final Class<?> type;
        public BeanProperties ( final Class<?> type ) throws ELException {
            this.type = type;
            this.properties = new HashMap<String, BeanProperty>();
            try {
                final BeanInfo info = Introspector.getBeanInfo ( this.type );
                final PropertyDescriptor[] propertyDescriptors;
                final PropertyDescriptor[] pds = propertyDescriptors = info.getPropertyDescriptors();
                for ( final PropertyDescriptor pd : propertyDescriptors ) {
                    this.properties.put ( pd.getName(), new BeanProperty ( type, pd ) );
                }
                if ( System.getSecurityManager() != null ) {
                    this.populateFromInterfaces ( type );
                }
            } catch ( IntrospectionException ie ) {
                throw new ELException ( ie );
            }
        }
        private void populateFromInterfaces ( final Class<?> aClass ) throws IntrospectionException {
            final Class<?>[] interfaces = aClass.getInterfaces();
            if ( interfaces.length > 0 ) {
                for ( final Class<?> ifs : interfaces ) {
                    final BeanInfo info = Introspector.getBeanInfo ( ifs );
                    final PropertyDescriptor[] propertyDescriptors;
                    final PropertyDescriptor[] pds = propertyDescriptors = info.getPropertyDescriptors();
                    for ( final PropertyDescriptor pd : propertyDescriptors ) {
                        if ( !this.properties.containsKey ( pd.getName() ) ) {
                            this.properties.put ( pd.getName(), new BeanProperty ( this.type, pd ) );
                        }
                    }
                }
            }
            final Class<?> superclass = aClass.getSuperclass();
            if ( superclass != null ) {
                this.populateFromInterfaces ( superclass );
            }
        }
        private BeanProperty get ( final ELContext ctx, final String name ) {
            final BeanProperty property = this.properties.get ( name );
            if ( property == null ) {
                throw new PropertyNotFoundException ( Util.message ( ctx, "propertyNotFound", this.type.getName(), name ) );
            }
            return property;
        }
        public BeanProperty getBeanProperty ( final String name ) {
            return this.get ( null, name );
        }
        private Class<?> getType() {
            return this.type;
        }
    }
    static final class BeanProperty {
        private final Class<?> type;
        private final Class<?> owner;
        private final PropertyDescriptor descriptor;
        private Method read;
        private Method write;
        public BeanProperty ( final Class<?> owner, final PropertyDescriptor descriptor ) {
            this.owner = owner;
            this.descriptor = descriptor;
            this.type = descriptor.getPropertyType();
        }
        public Class getPropertyType() {
            return this.type;
        }
        public boolean isReadOnly() {
            return this.write == null && null == ( this.write = Util.getMethod ( this.owner, this.descriptor.getWriteMethod() ) );
        }
        public Method getWriteMethod() {
            return this.write ( null );
        }
        public Method getReadMethod() {
            return this.read ( null );
        }
        private Method write ( final ELContext ctx ) {
            if ( this.write == null ) {
                this.write = Util.getMethod ( this.owner, this.descriptor.getWriteMethod() );
                if ( this.write == null ) {
                    throw new PropertyNotWritableException ( Util.message ( ctx, "propertyNotWritable", this.owner.getName(), this.descriptor.getName() ) );
                }
            }
            return this.write;
        }
        private Method read ( final ELContext ctx ) {
            if ( this.read == null ) {
                this.read = Util.getMethod ( this.owner, this.descriptor.getReadMethod() );
                if ( this.read == null ) {
                    throw new PropertyNotFoundException ( Util.message ( ctx, "propertyNotReadable", this.owner.getName(), this.descriptor.getName() ) );
                }
            }
            return this.read;
        }
    }
    private static final class ConcurrentCache<K, V> {
        private final int size;
        private final Map<K, V> eden;
        private final Map<K, V> longterm;
        public ConcurrentCache ( final int size ) {
            this.size = size;
            this.eden = new ConcurrentHashMap<K, V> ( size );
            this.longterm = new WeakHashMap<K, V> ( size );
        }
        public V get ( final K key ) {
            V value = this.eden.get ( key );
            if ( value == null ) {
                synchronized ( this.longterm ) {
                    value = this.longterm.get ( key );
                }
                if ( value != null ) {
                    this.eden.put ( key, value );
                }
            }
            return value;
        }
        public void put ( final K key, final V value ) {
            if ( this.eden.size() >= this.size ) {
                synchronized ( this.longterm ) {
                    this.longterm.putAll ( ( Map<? extends K, ? extends V> ) this.eden );
                }
                this.eden.clear();
            }
            this.eden.put ( key, value );
        }
    }
}
