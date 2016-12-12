package org.junit.internal.management;
final class FakeThreadMXBean implements ThreadMXBean {
    @Override
    public long getThreadCpuTime ( long id ) {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean isThreadCpuTimeSupported() {
        return false;
    }
}
