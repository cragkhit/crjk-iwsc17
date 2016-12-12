package org.junit.internal.management;
public interface ThreadMXBean {
    long getThreadCpuTime ( long p0 );
    boolean isThreadCpuTimeSupported();
}
