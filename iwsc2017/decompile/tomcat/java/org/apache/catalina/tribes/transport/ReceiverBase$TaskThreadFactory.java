package org.apache.catalina.tribes.transport;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadFactory;
class TaskThreadFactory implements ThreadFactory {
    final ThreadGroup group;
    final AtomicInteger threadNumber;
    final String namePrefix;
    TaskThreadFactory ( final String namePrefix ) {
        this.threadNumber = new AtomicInteger ( 1 );
        final SecurityManager s = System.getSecurityManager();
        this.group = ( ( s != null ) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup() );
        this.namePrefix = namePrefix;
    }
    @Override
    public Thread newThread ( final Runnable r ) {
        final Thread t = new Thread ( this.group, r, this.namePrefix + this.threadNumber.getAndIncrement() );
        t.setDaemon ( ReceiverBase.access$000 ( ReceiverBase.this ) );
        t.setPriority ( 5 );
        return t;
    }
}
