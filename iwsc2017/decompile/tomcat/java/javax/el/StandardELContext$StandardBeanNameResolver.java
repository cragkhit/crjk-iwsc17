package javax.el;
import java.util.Map;
private static class StandardBeanNameResolver extends BeanNameResolver {
    private final Map<String, Object> beans;
    public StandardBeanNameResolver ( final Map<String, Object> beans ) {
        this.beans = beans;
    }
    @Override
    public boolean isNameResolved ( final String beanName ) {
        return this.beans.containsKey ( beanName );
    }
    @Override
    public Object getBean ( final String beanName ) {
        return this.beans.get ( beanName );
    }
    @Override
    public void setBeanValue ( final String beanName, final Object value ) throws PropertyNotWritableException {
        this.beans.put ( beanName, value );
    }
    @Override
    public boolean isReadOnly ( final String beanName ) {
        return false;
    }
    @Override
    public boolean canCreateBean ( final String beanName ) {
        return true;
    }
}
