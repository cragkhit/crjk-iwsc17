package org.junit.internal.management;
private static final class ThreadHolder {
    private static final ThreadMXBean THREAD_MX_BEAN;
    private static final ThreadMXBean getBean ( final Object threadMxBean ) {
        return ( threadMxBean != null ) ? new ReflectiveThreadMXBean ( threadMxBean ) : new FakeThreadMXBean();
    }
    static {
        THREAD_MX_BEAN = getBean ( FactoryHolder.getBeanObject ( "getThreadMXBean" ) );
    }
}
