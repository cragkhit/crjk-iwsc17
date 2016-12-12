package org.junit.internal.management;
import org.junit.internal.Classes;
import java.lang.reflect.Method;
import java.util.Collections;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
final class ReflectiveRuntimeMXBean implements RuntimeMXBean {
    private final Object runtimeMxBean;
    ReflectiveRuntimeMXBean ( final Object runtimeMxBean ) {
        this.runtimeMxBean = runtimeMxBean;
    }
    public List<String> getInputArguments() {
        if ( Holder.getInputArgumentsMethod != null ) {
            try {
                return ( List<String> ) Holder.getInputArgumentsMethod.invoke ( this.runtimeMxBean, new Object[0] );
            } catch ( ClassCastException ex ) {}
            catch ( IllegalAccessException ex2 ) {}
            catch ( IllegalArgumentException ex3 ) {}
            catch ( InvocationTargetException ex4 ) {}
        }
        return Collections.emptyList();
    }
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
}
