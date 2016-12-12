package org.junit.internal.management;
final class FakeThreadMXBean implements ThreadMXBean {
    public long getThreadCpuTime ( final long id ) {
        throw new UnsupportedOperationException();
    }
    public boolean isThreadCpuTimeSupported() {
        return false;
    }
}
