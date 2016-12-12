package org.apache.tomcat;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
public class SimpleInstanceManager implements InstanceManager {
    @Override
    public Object newInstance ( final Class<?> clazz ) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException {
        return this.prepareInstance ( clazz.newInstance() );
    }
    @Override
    public Object newInstance ( final String className ) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
        final Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass ( className );
        return this.prepareInstance ( clazz.newInstance() );
    }
    @Override
    public Object newInstance ( final String fqcn, final ClassLoader classLoader ) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
        final Class<?> clazz = classLoader.loadClass ( fqcn );
        return this.prepareInstance ( clazz.newInstance() );
    }
    @Override
    public void newInstance ( final Object o ) throws IllegalAccessException, InvocationTargetException, NamingException {
        this.prepareInstance ( o );
    }
    @Override
    public void destroyInstance ( final Object o ) throws IllegalAccessException, InvocationTargetException {
    }
    private Object prepareInstance ( final Object o ) {
        return o;
    }
}
