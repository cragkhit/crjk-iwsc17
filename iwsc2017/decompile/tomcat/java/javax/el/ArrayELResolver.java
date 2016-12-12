package javax.el;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.lang.reflect.Array;
import java.util.Objects;
public class ArrayELResolver extends ELResolver {
    private final boolean readOnly;
    public ArrayELResolver() {
        this.readOnly = false;
    }
    public ArrayELResolver ( final boolean readOnly ) {
        this.readOnly = readOnly;
    }
    @Override
    public Class<?> getType ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base != null && base.getClass().isArray() ) {
            context.setPropertyResolved ( base, property );
            try {
                final int idx = coerce ( property );
                checkBounds ( base, idx );
            } catch ( IllegalArgumentException ex ) {}
            return base.getClass().getComponentType();
        }
        return null;
    }
    @Override
    public Object getValue ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base == null || !base.getClass().isArray() ) {
            return null;
        }
        context.setPropertyResolved ( base, property );
        final int idx = coerce ( property );
        if ( idx < 0 || idx >= Array.getLength ( base ) ) {
            return null;
        }
        return Array.get ( base, idx );
    }
    @Override
    public void setValue ( final ELContext context, final Object base, final Object property, final Object value ) {
        Objects.requireNonNull ( context );
        if ( base != null && base.getClass().isArray() ) {
            context.setPropertyResolved ( base, property );
            if ( this.readOnly ) {
                throw new PropertyNotWritableException ( Util.message ( context, "resolverNotWriteable", base.getClass().getName() ) );
            }
            final int idx = coerce ( property );
            checkBounds ( base, idx );
            if ( value != null && !Util.isAssignableFrom ( value.getClass(), base.getClass().getComponentType() ) ) {
                throw new ClassCastException ( Util.message ( context, "objectNotAssignable", value.getClass().getName(), base.getClass().getComponentType().getName() ) );
            }
            Array.set ( base, idx, value );
        }
    }
    @Override
    public boolean isReadOnly ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base != null && base.getClass().isArray() ) {
            context.setPropertyResolved ( base, property );
            try {
                final int idx = coerce ( property );
                checkBounds ( base, idx );
            } catch ( IllegalArgumentException ex ) {}
        }
        return this.readOnly;
    }
    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors ( final ELContext context, final Object base ) {
        return null;
    }
    @Override
    public Class<?> getCommonPropertyType ( final ELContext context, final Object base ) {
        if ( base != null && base.getClass().isArray() ) {
            return Integer.class;
        }
        return null;
    }
    private static final void checkBounds ( final Object base, final int idx ) {
        if ( idx < 0 || idx >= Array.getLength ( base ) ) {
            throw new PropertyNotFoundException ( new ArrayIndexOutOfBoundsException ( idx ).getMessage() );
        }
    }
    private static final int coerce ( final Object property ) {
        if ( property instanceof Number ) {
            return ( ( Number ) property ).intValue();
        }
        if ( property instanceof Character ) {
            return ( char ) property;
        }
        if ( property instanceof Boolean ) {
            return ( ( boolean ) property ) ? 1 : 0;
        }
        if ( property instanceof String ) {
            return Integer.parseInt ( ( String ) property );
        }
        throw new IllegalArgumentException ( ( property != null ) ? property.toString() : "null" );
    }
}
