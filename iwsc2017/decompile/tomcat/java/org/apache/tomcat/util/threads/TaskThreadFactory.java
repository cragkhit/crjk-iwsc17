package org.apache.tomcat.util.threads;
import java.security.PrivilegedAction;
import java.security.AccessController;
import org.apache.tomcat.util.security.PrivilegedSetTccl;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadFactory;
public class TaskThreadFactory implements ThreadFactory {
    private final ThreadGroup group;
    private final AtomicInteger threadNumber;
    private final String namePrefix;
    private final boolean daemon;
    private final int threadPriority;
    public TaskThreadFactory ( final String namePrefix, final boolean daemon, final int priority ) {
        this.threadNumber = new AtomicInteger ( 1 );
        final SecurityManager s = System.getSecurityManager();
        this.group = ( ( s != null ) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup() );
        this.namePrefix = namePrefix;
        this.daemon = daemon;
        this.threadPriority = priority;
    }
    @Override
    public Thread newThread ( final Runnable r ) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            if ( Constants.IS_SECURITY_ENABLED ) {
                final PrivilegedAction<Void> pa = new PrivilegedSetTccl ( this.getClass().getClassLoader() );
                AccessController.doPrivileged ( pa );
            } else {
                Thread.currentThread().setContextClassLoader ( this.getClass().getClassLoader() );
            }
            final TaskThread t = new TaskThread ( this.group, r, this.namePrefix + this.threadNumber.getAndIncrement() );
            t.setDaemon ( this.daemon );
            t.setPriority ( this.threadPriority );
            return t;
        } finally {
            if ( Constants.IS_SECURITY_ENABLED ) {
                final PrivilegedAction<Void> pa2 = new PrivilegedSetTccl ( loader );
                AccessController.doPrivileged ( pa2 );
            } else {
                Thread.currentThread().setContextClassLoader ( loader );
            }
        }
    }
}
