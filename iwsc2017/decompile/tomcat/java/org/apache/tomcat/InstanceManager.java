package org.apache.tomcat;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
public interface InstanceManager {
    Object newInstance ( Class<?> p0 ) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException;
    Object newInstance ( String p0 ) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException;
    Object newInstance ( String p0, ClassLoader p1 ) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException;
    void newInstance ( Object p0 ) throws IllegalAccessException, InvocationTargetException, NamingException;
    void destroyInstance ( Object p0 ) throws IllegalAccessException, InvocationTargetException;
}
