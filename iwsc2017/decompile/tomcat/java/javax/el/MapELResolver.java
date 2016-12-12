package javax.el;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
public class MapELResolver extends ELResolver {
    private static final Class<?> UNMODIFIABLE;
    private final boolean readOnly;
    public MapELResolver() {
        this.readOnly = false;
    }
    public MapELResolver ( final boolean readOnly ) {
        this.readOnly = readOnly;
    }
    @Override
    public Class<?> getType ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base instanceof Map ) {
            context.setPropertyResolved ( base, property );
            return Object.class;
        }
        return null;
    }
    @Override
    public Object getValue ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base instanceof Map ) {
            context.setPropertyResolved ( base, property );
            return ( ( Map ) base ).get ( property );
        }
        return null;
    }
    @Override
    public void setValue ( final ELContext context, final Object base, final Object property, final Object value ) {
        Objects.requireNonNull ( context );
        if ( base instanceof Map ) {
            context.setPropertyResolved ( base, property );
            if ( this.readOnly ) {
                throw new PropertyNotWritableException ( Util.message ( context, "resolverNotWriteable", base.getClass().getName() ) );
            }
            try {
                final Map<Object, Object> map = ( Map<Object, Object> ) base;
                map.put ( property, value );
            } catch ( UnsupportedOperationException e ) {
                throw new PropertyNotWritableException ( e );
            }
        }
    }
    @Override
    public boolean isReadOnly ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base instanceof Map ) {
            context.setPropertyResolved ( base, property );
            return this.readOnly || MapELResolver.UNMODIFIABLE.equals ( base.getClass() );
        }
        return this.readOnly;
    }
    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors ( final ELContext context, final Object base ) {
        if ( base instanceof Map ) {
            final Iterator<?> itr = ( ( Map ) base ).keySet().iterator();
            final List<FeatureDescriptor> feats = new ArrayList<FeatureDescriptor>();
            while ( itr.hasNext() ) {
                final Object key = itr.next();
                final FeatureDescriptor desc = new FeatureDescriptor();
                desc.setDisplayName ( key.toString() );
                desc.setShortDescription ( "" );
                desc.setExpert ( false );
                desc.setHidden ( false );
                desc.setName ( key.toString() );
                desc.setPreferred ( true );
                desc.setValue ( "resolvableAtDesignTime", Boolean.TRUE );
                desc.setValue ( "type", key.getClass() );
                feats.add ( desc );
            }
            return feats.iterator();
        }
        return null;
    }
    @Override
    public Class<?> getCommonPropertyType ( final ELContext context, final Object base ) {
        if ( base instanceof Map ) {
            return Object.class;
        }
        return null;
    }
    static {
        UNMODIFIABLE = Collections.unmodifiableMap ( ( Map<?, ?> ) new HashMap<Object, Object>() ).getClass();
    }
}
