package org.junit.internal.management;
import org.junit.internal.Classes;
import java.lang.reflect.InvocationTargetException;
public class ManagementFactory {
    public static RuntimeMXBean getRuntimeMXBean() {
        return RuntimeHolder.RUNTIME_MX_BEAN;
    }
    public static ThreadMXBean getThreadMXBean() {
        return ThreadHolder.THREAD_MX_BEAN;
    }
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
    private static final class RuntimeHolder {
        private static final RuntimeMXBean RUNTIME_MX_BEAN;
        private static final RuntimeMXBean getBean ( final Object runtimeMxBean ) {
            return ( runtimeMxBean != null ) ? new ReflectiveRuntimeMXBean ( runtimeMxBean ) : new FakeRuntimeMXBean();
        }
        static {
            RUNTIME_MX_BEAN = getBean ( FactoryHolder.getBeanObject ( "getRuntimeMXBean" ) );
        }
    }
    private static final class ThreadHolder {
        private static final ThreadMXBean THREAD_MX_BEAN;
        private static final ThreadMXBean getBean ( final Object threadMxBean ) {
            return ( threadMxBean != null ) ? new ReflectiveThreadMXBean ( threadMxBean ) : new FakeThreadMXBean();
        }
        static {
            THREAD_MX_BEAN = getBean ( FactoryHolder.getBeanObject ( "getThreadMXBean" ) );
        }
    }
}
