package org.apache.tomcat.dbcp.pool2.impl;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Timer;
import java.util.TimerTask;
class EvictionTimer {
    private static Timer _timer;
    private static int _usageCount;
    private EvictionTimer() {
    }
    static synchronized void schedule ( final TimerTask task, final long delay, final long period ) {
        if ( null == _timer ) {
            final ClassLoader ccl = AccessController.doPrivileged (
                                        new PrivilegedGetTccl() );
            try {
                AccessController.doPrivileged ( new PrivilegedSetTccl (
                                                    EvictionTimer.class.getClassLoader() ) );
                _timer = AccessController.doPrivileged ( new PrivilegedNewEvictionTimer() );
            } finally {
                AccessController.doPrivileged ( new PrivilegedSetTccl ( ccl ) );
            }
        }
        _usageCount++;
        _timer.schedule ( task, delay, period );
    }
    static synchronized void cancel ( final TimerTask task ) {
        task.cancel();
        _usageCount--;
        if ( _usageCount == 0 ) {
            _timer.cancel();
            _timer = null;
        }
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
            Thread.currentThread().setContextClassLoader ( classLoader );
            return null;
        }
        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append ( "PrivilegedSetTccl [classLoader=" );
            builder.append ( classLoader );
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
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append ( "EvictionTimer []" );
        return builder.toString();
    }
}
