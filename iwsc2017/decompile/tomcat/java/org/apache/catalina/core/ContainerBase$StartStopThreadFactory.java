package org.apache.catalina.core;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadFactory;
private static class StartStopThreadFactory implements ThreadFactory {
    private final ThreadGroup group;
    private final AtomicInteger threadNumber;
    private final String namePrefix;
    public StartStopThreadFactory ( final String namePrefix ) {
        this.threadNumber = new AtomicInteger ( 1 );
        final SecurityManager s = System.getSecurityManager();
        this.group = ( ( s != null ) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup() );
        this.namePrefix = namePrefix;
    }
    @Override
    public Thread newThread ( final Runnable r ) {
        final Thread thread = new Thread ( this.group, r, this.namePrefix + this.threadNumber.getAndIncrement() );
        thread.setDaemon ( true );
        return thread;
    }
}
