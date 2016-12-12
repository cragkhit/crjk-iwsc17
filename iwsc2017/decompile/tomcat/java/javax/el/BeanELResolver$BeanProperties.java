package javax.el;
import java.beans.PropertyDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.HashMap;
import java.util.Map;
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
