package org.apache.tomcat.util.threads;
import org.apache.juli.logging.LogFactory;
import org.apache.juli.logging.Log;
public class TaskThread extends Thread {
    private static final Log log;
    private final long creationTime;
    public TaskThread ( final ThreadGroup group, final Runnable target, final String name ) {
        super ( group, new WrappingRunnable ( target ), name );
        this.creationTime = System.currentTimeMillis();
    }
    public TaskThread ( final ThreadGroup group, final Runnable target, final String name, final long stackSize ) {
        super ( group, new WrappingRunnable ( target ), name, stackSize );
        this.creationTime = System.currentTimeMillis();
    }
    public final long getCreationTime() {
        return this.creationTime;
    }
    static {
        log = LogFactory.getLog ( TaskThread.class );
    }
    private static class WrappingRunnable implements Runnable {
        private Runnable wrappedRunnable;
        WrappingRunnable ( final Runnable wrappedRunnable ) {
            this.wrappedRunnable = wrappedRunnable;
        }
        @Override
        public void run() {
            try {
                this.wrappedRunnable.run();
            } catch ( StopPooledThreadException exc ) {
                TaskThread.log.debug ( "Thread exiting on purpose", exc );
            }
        }
    }
}
