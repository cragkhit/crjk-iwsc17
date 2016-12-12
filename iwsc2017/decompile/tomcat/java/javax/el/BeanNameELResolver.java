package javax.el;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.Objects;
public class BeanNameELResolver extends ELResolver {
    private final BeanNameResolver beanNameResolver;
    public BeanNameELResolver ( final BeanNameResolver beanNameResolver ) {
        this.beanNameResolver = beanNameResolver;
    }
    @Override
    public Object getValue ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base != null || ! ( property instanceof String ) ) {
            return null;
        }
        final String beanName = ( String ) property;
        if ( this.beanNameResolver.isNameResolved ( beanName ) ) {
            try {
                final Object result = this.beanNameResolver.getBean ( beanName );
                context.setPropertyResolved ( base, property );
                return result;
            } catch ( Throwable t ) {
                Util.handleThrowable ( t );
                throw new ELException ( t );
            }
        }
        return null;
    }
    @Override
    public void setValue ( final ELContext context, final Object base, final Object property, final Object value ) {
        Objects.requireNonNull ( context );
        if ( base != null || ! ( property instanceof String ) ) {
            return;
        }
        final String beanName = ( String ) property;
        final boolean isResolved = context.isPropertyResolved();
        boolean isReadOnly;
        try {
            isReadOnly = this.isReadOnly ( context, base, property );
        } catch ( Throwable t ) {
            Util.handleThrowable ( t );
            throw new ELException ( t );
        } finally {
            context.setPropertyResolved ( isResolved );
        }
        if ( isReadOnly ) {
            throw new PropertyNotWritableException ( Util.message ( context, "beanNameELResolver.beanReadOnly", beanName ) );
        }
        if ( !this.beanNameResolver.isNameResolved ( beanName ) ) {
            if ( !this.beanNameResolver.canCreateBean ( beanName ) ) {
                return;
            }
        }
        try {
            this.beanNameResolver.setBeanValue ( beanName, value );
            context.setPropertyResolved ( base, property );
        } catch ( Throwable t ) {
            Util.handleThrowable ( t );
            throw new ELException ( t );
        }
    }
    @Override
    public Class<?> getType ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base != null || ! ( property instanceof String ) ) {
            return null;
        }
        final String beanName = ( String ) property;
        try {
            if ( this.beanNameResolver.isNameResolved ( beanName ) ) {
                final Class<?> result = this.beanNameResolver.getBean ( beanName ).getClass();
                context.setPropertyResolved ( base, property );
                return result;
            }
        } catch ( Throwable t ) {
            Util.handleThrowable ( t );
            throw new ELException ( t );
        }
        return null;
    }
    @Override
    public boolean isReadOnly ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base != null || ! ( property instanceof String ) ) {
            return false;
        }
        final String beanName = ( String ) property;
        if ( this.beanNameResolver.isNameResolved ( beanName ) ) {
            boolean result;
            try {
                result = this.beanNameResolver.isReadOnly ( beanName );
            } catch ( Throwable t ) {
                Util.handleThrowable ( t );
                throw new ELException ( t );
            }
            context.setPropertyResolved ( base, property );
            return result;
        }
        return false;
    }
    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors ( final ELContext context, final Object base ) {
        return null;
    }
    @Override
    public Class<?> getCommonPropertyType ( final ELContext context, final Object base ) {
        return String.class;
    }
}
