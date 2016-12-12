package org.junit.internal.management;
import org.junit.internal.Classes;
import java.lang.reflect.InvocationTargetException;
private static final class FactoryHolder {
    private static final Class<?> MANAGEMENT_FACTORY_CLASS;
    static Object getBeanObject ( final String methodName ) {
        if ( FactoryHolder.MANAGEMENT_FACTORY_CLASS != null ) {
            try {
                return FactoryHolder.MANAGEMENT_FACTORY_CLASS.getMethod ( methodName, ( Class<?>[] ) new Class[0] ).invoke ( null, new Object[0] );
            } catch ( IllegalAccessException ex ) {}
            catch ( IllegalArgumentException ex2 ) {}
            catch ( InvocationTargetException ex3 ) {}
            catch ( NoSuchMethodException ex4 ) {}
            catch ( SecurityException ex5 ) {}
        }
        return null;
    }
    static {
        Class<?> managementFactoryClass = null;
        try {
            managementFactoryClass = Classes.getClass ( "java.lang.management.ManagementFactory" );
        } catch ( ClassNotFoundException ex ) {}
        MANAGEMENT_FACTORY_CLASS = managementFactoryClass;
    }
}
