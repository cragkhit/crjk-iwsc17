package org.apache.tomcat;
import java.lang.reflect.InvocationTargetException;
import javax.naming.NamingException;
public class SimpleInstanceManager implements InstanceManager {
    public SimpleInstanceManager() {
    }
    @Override
    public Object newInstance ( Class<?> clazz ) throws IllegalAccessException,
        InvocationTargetException, NamingException, InstantiationException {
        return prepareInstance ( clazz.newInstance() );
    }
    @Override
    public Object newInstance ( String className ) throws IllegalAccessException,
        InvocationTargetException, NamingException, InstantiationException,
        ClassNotFoundException  {
        Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass ( className );
        return prepareInstance ( clazz.newInstance() );
    }
    @Override
    public Object newInstance ( String fqcn, ClassLoader classLoader ) throws IllegalAccessException,
        InvocationTargetException, NamingException, InstantiationException,
        ClassNotFoundException  {
        Class<?> clazz = classLoader.loadClass ( fqcn );
        return prepareInstance ( clazz.newInstance() );
    }
    @Override
    public void newInstance ( Object o ) throws IllegalAccessException, InvocationTargetException,
        NamingException  {
        prepareInstance ( o );
    }
    @Override
    public void destroyInstance ( Object o ) throws IllegalAccessException, InvocationTargetException {
    }
    private Object prepareInstance ( Object o ) {
        return o;
    }
}
