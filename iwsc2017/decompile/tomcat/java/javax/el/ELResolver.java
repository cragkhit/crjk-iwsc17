package javax.el;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
public abstract class ELResolver {
    public static final String TYPE = "type";
    public static final String RESOLVABLE_AT_DESIGN_TIME = "resolvableAtDesignTime";
    public abstract Object getValue ( final ELContext p0, final Object p1, final Object p2 );
    public Object invoke ( final ELContext context, final Object base, final Object method, final Class<?>[] paramTypes, final Object[] params ) {
        return null;
    }
    public abstract Class<?> getType ( final ELContext p0, final Object p1, final Object p2 );
    public abstract void setValue ( final ELContext p0, final Object p1, final Object p2, final Object p3 );
    public abstract boolean isReadOnly ( final ELContext p0, final Object p1, final Object p2 );
    public abstract Iterator<FeatureDescriptor> getFeatureDescriptors ( final ELContext p0, final Object p1 );
    public abstract Class<?> getCommonPropertyType ( final ELContext p0, final Object p1 );
    public Object convertToType ( final ELContext context, final Object obj, final Class<?> type ) {
        context.setPropertyResolved ( false );
        return null;
    }
}
