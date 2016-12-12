package org.junit.internal.management;
import org.junit.internal.Classes;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
final class ReflectiveThreadMXBean implements ThreadMXBean {
    private final Object threadMxBean;
    ReflectiveThreadMXBean ( final Object threadMxBean ) {
        this.threadMxBean = threadMxBean;
    }
    public long getThreadCpuTime ( final long id ) {
        if ( Holder.getThreadCpuTimeMethod != null ) {
            Exception error = null;
            try {
                return ( long ) Holder.getThreadCpuTimeMethod.invoke ( this.threadMxBean, id );
            } catch ( ClassCastException e ) {
                error = e;
            } catch ( IllegalAccessException e2 ) {
                error = e2;
            } catch ( IllegalArgumentException e3 ) {
                error = e3;
            } catch ( InvocationTargetException e4 ) {
                error = e4;
            }
            throw new UnsupportedOperationException ( "Unable to access ThreadMXBean", error );
        }
        throw new UnsupportedOperationException ( "Unable to access ThreadMXBean" );
    }
    public boolean isThreadCpuTimeSupported() {
        if ( Holder.isThreadCpuTimeSupportedMethod != null ) {
            try {
                return ( boolean ) Holder.isThreadCpuTimeSupportedMethod.invoke ( this.threadMxBean, new Object[0] );
            } catch ( ClassCastException ex ) {}
            catch ( IllegalAccessException ex2 ) {}
            catch ( IllegalArgumentException ex3 ) {}
            catch ( InvocationTargetException ex4 ) {}
        }
        return false;
    }
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
}
