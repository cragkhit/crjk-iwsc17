package javax.el;
import java.util.Iterator;
public abstract class ELResolver {
    public static final String TYPE = "type";
    public static final String RESOLVABLE_AT_DESIGN_TIME = "resolvableAtDesignTime";
    public abstract Object getValue ( ELContext context, Object base,
                                      Object property );
    public Object invoke ( ELContext context, Object base, Object method,
                           Class<?>[] paramTypes, Object[] params ) {
        return null;
    }
    public abstract Class<?> getType ( ELContext context, Object base,
                                       Object property );
    public abstract void setValue ( ELContext context, Object base,
                                    Object property, Object value );
    public abstract boolean isReadOnly ( ELContext context, Object base,
                                         Object property );
    public abstract Iterator<java.beans.FeatureDescriptor> getFeatureDescriptors ( ELContext context, Object base );
    public abstract Class<?> getCommonPropertyType ( ELContext context,
            Object base );
    public Object convertToType ( ELContext context, Object obj, Class<?> type ) {
        context.setPropertyResolved ( false );
        return null;
    }
}
