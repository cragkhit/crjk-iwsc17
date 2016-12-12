package org.apache.tomcat.dbcp.pool2.impl;
import java.security.PrivilegedAction;
import java.security.AccessController;
import java.util.TimerTask;
import java.util.Timer;
class EvictionTimer {
    private static Timer _timer;
    private static int _usageCount;
    static synchronized void schedule ( final TimerTask task, final long delay, final long period ) {
        if ( null == EvictionTimer._timer ) {
            final ClassLoader ccl = AccessController.doPrivileged ( ( PrivilegedAction<ClassLoader> ) new PrivilegedGetTccl() );
            try {
                AccessController.doPrivileged ( ( PrivilegedAction<Object> ) new PrivilegedSetTccl ( EvictionTimer.class.getClassLoader() ) );
                EvictionTimer._timer = AccessController.doPrivileged ( ( PrivilegedAction<Timer> ) new PrivilegedNewEvictionTimer() );
            } finally {
                AccessController.doPrivileged ( ( PrivilegedAction<Object> ) new PrivilegedSetTccl ( ccl ) );
            }
        }
        ++EvictionTimer._usageCount;
        EvictionTimer._timer.schedule ( task, delay, period );
    }
    static synchronized void cancel ( final TimerTask task ) {
        task.cancel();
        --EvictionTimer._usageCount;
        if ( EvictionTimer._usageCount == 0 ) {
            EvictionTimer._timer.cancel();
            EvictionTimer._timer = null;
        }
    }
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append ( "EvictionTimer []" );
        return builder.toString();
    }
    private static class PrivilegedGetTccl implements PrivilegedAction<ClassLoader> {
        @Override
        public ClassLoader run() {
            return Thread.currentThread().getContextClassLoader();
        }
    }
    private static class PrivilegedSetTccl implements PrivilegedAction<Void> {
        private final ClassLoader classLoader;
        PrivilegedSetTccl ( final ClassLoader cl ) {
            this.classLoader = cl;
        }
        @Override
        public Void run() {
            Thread.currentThread().setContextClassLoader ( this.classLoader );
            return null;
        }
        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append ( "PrivilegedSetTccl [classLoader=" );
            builder.append ( this.classLoader );
            builder.append ( "]" );
            return builder.toString();
        }
    }
    private static class PrivilegedNewEvictionTimer implements PrivilegedAction<Timer> {
        @Override
        public Timer run() {
            return new Timer ( "commons-pool-EvictionTimer", true );
        }
    }
}
