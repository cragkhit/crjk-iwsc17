package javax.el;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Objects;
public class ResourceBundleELResolver extends ELResolver {
    @Override
    public Object getValue ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base instanceof ResourceBundle ) {
            context.setPropertyResolved ( base, property );
            if ( property != null ) {
                try {
                    return ( ( ResourceBundle ) base ).getObject ( property.toString() );
                } catch ( MissingResourceException mre ) {
                    return "???" + property.toString() + "???";
                }
            }
        }
        return null;
    }
    @Override
    public Class<?> getType ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base instanceof ResourceBundle ) {
            context.setPropertyResolved ( base, property );
        }
        return null;
    }
    @Override
    public void setValue ( final ELContext context, final Object base, final Object property, final Object value ) {
        Objects.requireNonNull ( context );
        if ( base instanceof ResourceBundle ) {
            context.setPropertyResolved ( base, property );
            throw new PropertyNotWritableException ( Util.message ( context, "resolverNotWriteable", base.getClass().getName() ) );
        }
    }
    @Override
    public boolean isReadOnly ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base instanceof ResourceBundle ) {
            context.setPropertyResolved ( base, property );
            return true;
        }
        return false;
    }
    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors ( final ELContext context, final Object base ) {
        if ( base instanceof ResourceBundle ) {
            final List<FeatureDescriptor> feats = new ArrayList<FeatureDescriptor>();
            final Enumeration<String> e = ( ( ResourceBundle ) base ).getKeys();
            while ( e.hasMoreElements() ) {
                final String key = e.nextElement();
                final FeatureDescriptor feat = new FeatureDescriptor();
                feat.setDisplayName ( key );
                feat.setShortDescription ( "" );
                feat.setExpert ( false );
                feat.setHidden ( false );
                feat.setName ( key );
                feat.setPreferred ( true );
                feat.setValue ( "resolvableAtDesignTime", Boolean.TRUE );
                feat.setValue ( "type", String.class );
                feats.add ( feat );
            }
            return feats.iterator();
        }
        return null;
    }
    @Override
    public Class<?> getCommonPropertyType ( final ELContext context, final Object base ) {
        if ( base instanceof ResourceBundle ) {
            return String.class;
        }
        return null;
    }
}
