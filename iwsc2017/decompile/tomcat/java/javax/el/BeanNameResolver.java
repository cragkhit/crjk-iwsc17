package javax.el;
public abstract class BeanNameResolver {
    public boolean isNameResolved ( final String beanName ) {
        return false;
    }
    public Object getBean ( final String beanName ) {
        return null;
    }
    public void setBeanValue ( final String beanName, final Object value ) throws PropertyNotWritableException {
        throw new PropertyNotWritableException();
    }
    public boolean isReadOnly ( final String beanName ) {
        return true;
    }
    public boolean canCreateBean ( final String beanName ) {
        return false;
    }
}
