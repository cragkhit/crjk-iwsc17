package org.junit.internal.management;
import org.junit.internal.Classes;
import java.lang.reflect.Method;
private static final class Holder {
    static final Method getThreadCpuTimeMethod;
    static final Method isThreadCpuTimeSupportedMethod;
    private static final String FAILURE_MESSAGE = "Unable to access ThreadMXBean";
    static {
        Method threadCpuTime = null;
        Method threadCpuTimeSupported = null;
        try {
            final Class<?> threadMXBeanClass = Classes.getClass ( "java.lang.management.ThreadMXBean" );
            threadCpuTime = threadMXBeanClass.getMethod ( "getThreadCpuTime", Long.TYPE );
            threadCpuTimeSupported = threadMXBeanClass.getMethod ( "isThreadCpuTimeSupported", ( Class<?>[] ) new Class[0] );
        } catch ( ClassNotFoundException ex ) {}
        catch ( NoSuchMethodException ex2 ) {}
        catch ( SecurityException ex3 ) {}
        getThreadCpuTimeMethod = threadCpuTime;
        isThreadCpuTimeSupportedMethod = threadCpuTimeSupported;
    }
}
