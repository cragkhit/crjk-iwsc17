package org.junit.internal.management;
public interface ThreadMXBean {
    long getThreadCpuTime ( long id );
    boolean isThreadCpuTimeSupported();
}
