package org.junit.internal.management;
private static final class RuntimeHolder {
    private static final RuntimeMXBean RUNTIME_MX_BEAN;
    private static final RuntimeMXBean getBean ( final Object runtimeMxBean ) {
        return ( runtimeMxBean != null ) ? new ReflectiveRuntimeMXBean ( runtimeMxBean ) : new FakeRuntimeMXBean();
    }
    static {
        RUNTIME_MX_BEAN = getBean ( FactoryHolder.getBeanObject ( "getRuntimeMXBean" ) );
    }
}
