package javax.el;
import java.lang.reflect.Method;
import java.beans.PropertyDescriptor;
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
