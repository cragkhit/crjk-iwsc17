package org.junit.internal.management;
import org.junit.internal.Classes;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
final class ReflectiveRuntimeMXBean implements RuntimeMXBean {
    private final Object runtimeMxBean;
    private static final class Holder {
        private static final Method getInputArgumentsMethod;
        static {
            Method inputArguments = null;
            try {
                Class<?> threadMXBeanClass = Classes.getClass ( "java.lang.management.RuntimeMXBean" );
                inputArguments = threadMXBeanClass.getMethod ( "getInputArguments" );
            } catch ( ClassNotFoundException e ) {
            } catch ( NoSuchMethodException e ) {
            } catch ( SecurityException e ) {
            }
            getInputArgumentsMethod = inputArguments;
        }
    }
    ReflectiveRuntimeMXBean ( Object runtimeMxBean ) {
        super();
        this.runtimeMxBean = runtimeMxBean;
    }
    @SuppressWarnings ( "unchecked" )
    @Override
    public List<String> getInputArguments() {
        if ( Holder.getInputArgumentsMethod != null ) {
            try {
                return ( List<String> ) Holder.getInputArgumentsMethod.invoke ( runtimeMxBean );
            } catch ( ClassCastException e ) {
            } catch ( IllegalAccessException e ) {
            } catch ( IllegalArgumentException e ) {
            } catch ( InvocationTargetException e ) {
            }
        }
        return Collections.emptyList();
    }
}
