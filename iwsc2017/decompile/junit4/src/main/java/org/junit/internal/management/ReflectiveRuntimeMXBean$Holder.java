package org.junit.internal.management;
import org.junit.internal.Classes;
import java.lang.reflect.Method;
private static final class Holder {
    private static final Method getInputArgumentsMethod;
    static {
        Method inputArguments = null;
        try {
            final Class<?> threadMXBeanClass = Classes.getClass ( "java.lang.management.RuntimeMXBean" );
            inputArguments = threadMXBeanClass.getMethod ( "getInputArguments", ( Class<?>[] ) new Class[0] );
        } catch ( ClassNotFoundException ex ) {}
        catch ( NoSuchMethodException ex2 ) {}
        catch ( SecurityException ex3 ) {}
        getInputArgumentsMethod = inputArguments;
    }
}
