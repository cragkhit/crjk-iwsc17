package org.apache.catalina.tribes.util;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadFactory;
public class TcclThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber;
    private static final boolean IS_SECURITY_ENABLED;
    private final ThreadGroup group;
    private final AtomicInteger threadNumber;
    private final String namePrefix;
    public TcclThreadFactory() {
        this ( "pool-" + TcclThreadFactory.poolNumber.getAndIncrement() + "-thread-" );
    }
    public TcclThreadFactory ( final String namePrefix ) {
        this.threadNumber = new AtomicInteger ( 1 );
        final SecurityManager s = System.getSecurityManager();
        this.group = ( ( s != null ) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup() );
        this.namePrefix = namePrefix;
    }
    @Override
    public Thread newThread ( final Runnable r ) {
        final Thread t = new Thread ( this.group, r, this.namePrefix + this.threadNumber.getAndIncrement() );
        if ( TcclThreadFactory.IS_SECURITY_ENABLED ) {
            AccessController.doPrivileged ( ( PrivilegedAction<Object> ) new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    t.setContextClassLoader ( this.getClass().getClassLoader() );
                    return null;
                }
            } );
        } else {
            t.setContextClassLoader ( this.getClass().getClassLoader() );
        }
        t.setDaemon ( true );
        return t;
    }
    static {
        poolNumber = new AtomicInteger ( 1 );
        IS_SECURITY_ENABLED = ( System.getSecurityManager() != null );
    }
}
