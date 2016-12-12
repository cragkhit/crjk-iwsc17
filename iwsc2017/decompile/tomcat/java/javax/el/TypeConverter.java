package javax.el;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
public abstract class TypeConverter extends ELResolver {
    @Override
    public Object getValue ( final ELContext context, final Object base, final Object property ) {
        return null;
    }
    @Override
    public Class<?> getType ( final ELContext context, final Object base, final Object property ) {
        return null;
    }
    @Override
    public void setValue ( final ELContext context, final Object base, final Object property, final Object value ) {
    }
    @Override
    public boolean isReadOnly ( final ELContext context, final Object base, final Object property ) {
        return false;
    }
    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors ( final ELContext context, final Object base ) {
        return null;
    }
    @Override
    public Class<?> getCommonPropertyType ( final ELContext context, final Object base ) {
        return null;
    }
    @Override
    public abstract Object convertToType ( final ELContext p0, final Object p1, final Class<?> p2 );
}
