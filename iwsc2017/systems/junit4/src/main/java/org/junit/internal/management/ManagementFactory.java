package org.junit.internal.management;
import org.junit.internal.Classes;
import java.lang.reflect.InvocationTargetException;
public class ManagementFactory {
    private static final class FactoryHolder {
        private static final Class<?> MANAGEMENT_FACTORY_CLASS;
        static {
            Class<?> managementFactoryClass = null;
            try {
                managementFactoryClass = Classes.getClass ( "java.lang.management.ManagementFactory" );
            } catch ( ClassNotFoundException e ) {
            }
            MANAGEMENT_FACTORY_CLASS = managementFactoryClass;
        }
        static Object getBeanObject ( String methodName ) {
            if ( MANAGEMENT_FACTORY_CLASS != null ) {
                try {
                    return MANAGEMENT_FACTORY_CLASS.getMethod ( methodName ).invoke ( null );
                } catch ( IllegalAccessException e ) {
                } catch ( IllegalArgumentException e ) {
                } catch ( InvocationTargetException e ) {
                } catch ( NoSuchMethodException e ) {
                } catch ( SecurityException e ) {
                }
            }
            return null;
        }
    }
    private static final class RuntimeHolder {
        private static final RuntimeMXBean RUNTIME_MX_BEAN =
            getBean ( FactoryHolder.getBeanObject ( "getRuntimeMXBean" ) );
        private static final RuntimeMXBean getBean ( Object runtimeMxBean ) {
            return runtimeMxBean != null
                   ? new ReflectiveRuntimeMXBean ( runtimeMxBean ) : new FakeRuntimeMXBean();
        }
    }
    private static final class ThreadHolder {
        private static final ThreadMXBean THREAD_MX_BEAN =
            getBean ( FactoryHolder.getBeanObject ( "getThreadMXBean" ) );
        private static final ThreadMXBean getBean ( Object threadMxBean ) {
            return threadMxBean != null
                   ? new ReflectiveThreadMXBean ( threadMxBean ) : new FakeThreadMXBean();
        }
    }
    public static RuntimeMXBean getRuntimeMXBean() {
        return RuntimeHolder.RUNTIME_MX_BEAN;
    }
    public static ThreadMXBean getThreadMXBean() {
        return ThreadHolder.THREAD_MX_BEAN;
    }
}
