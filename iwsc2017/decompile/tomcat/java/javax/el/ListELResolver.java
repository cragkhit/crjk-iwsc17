package javax.el;
import java.util.Collections;
import java.util.ArrayList;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
public class ListELResolver extends ELResolver {
    private final boolean readOnly;
    private static final Class<?> UNMODIFIABLE;
    public ListELResolver() {
        this.readOnly = false;
    }
    public ListELResolver ( final boolean readOnly ) {
        this.readOnly = readOnly;
    }
    @Override
    public Class<?> getType ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( ! ( base instanceof List ) ) {
            return null;
        }
        context.setPropertyResolved ( base, property );
        final List<?> list = ( List<?> ) base;
        final int idx = coerce ( property );
        if ( idx < 0 || idx >= list.size() ) {
            throw new PropertyNotFoundException ( new ArrayIndexOutOfBoundsException ( idx ).getMessage() );
        }
        return Object.class;
    }
    @Override
    public Object getValue ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( ! ( base instanceof List ) ) {
            return null;
        }
        context.setPropertyResolved ( base, property );
        final List<?> list = ( List<?> ) base;
        final int idx = coerce ( property );
        if ( idx < 0 || idx >= list.size() ) {
            return null;
        }
        return list.get ( idx );
    }
    @Override
    public void setValue ( final ELContext context, final Object base, final Object property, final Object value ) {
        Objects.requireNonNull ( context );
        if ( base instanceof List ) {
            context.setPropertyResolved ( base, property );
            final List<Object> list = ( List<Object> ) base;
            if ( this.readOnly ) {
                throw new PropertyNotWritableException ( Util.message ( context, "resolverNotWriteable", base.getClass().getName() ) );
            }
            final int idx = coerce ( property );
            try {
                list.set ( idx, value );
            } catch ( UnsupportedOperationException e ) {
                throw new PropertyNotWritableException ( e );
            } catch ( IndexOutOfBoundsException e2 ) {
                throw new PropertyNotFoundException ( e2 );
            }
        }
    }
    @Override
    public boolean isReadOnly ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base instanceof List ) {
            context.setPropertyResolved ( base, property );
            final List<?> list = ( List<?> ) base;
            try {
                final int idx = coerce ( property );
                if ( idx < 0 || idx >= list.size() ) {
                    throw new PropertyNotFoundException ( new ArrayIndexOutOfBoundsException ( idx ).getMessage() );
                }
            } catch ( IllegalArgumentException ex ) {}
            return this.readOnly || ListELResolver.UNMODIFIABLE.equals ( list.getClass() );
        }
        return this.readOnly;
    }
    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors ( final ELContext context, final Object base ) {
        return null;
    }
    @Override
    public Class<?> getCommonPropertyType ( final ELContext context, final Object base ) {
        if ( base instanceof List ) {
            return Integer.class;
        }
        return null;
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
    static {
        UNMODIFIABLE = Collections.unmodifiableList ( ( List<?> ) new ArrayList<Object>() ).getClass();
    }
}
