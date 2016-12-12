package org.apache.tomcat.util.threads;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class TaskThread extends Thread {
    private static final Log log = LogFactory.getLog ( TaskThread.class );
    private final long creationTime;
    public TaskThread ( ThreadGroup group, Runnable target, String name ) {
        super ( group, new WrappingRunnable ( target ), name );
        this.creationTime = System.currentTimeMillis();
    }
    public TaskThread ( ThreadGroup group, Runnable target, String name,
                        long stackSize ) {
        super ( group, new WrappingRunnable ( target ), name, stackSize );
        this.creationTime = System.currentTimeMillis();
    }
    public final long getCreationTime() {
        return creationTime;
    }
    private static class WrappingRunnable implements Runnable {
        private Runnable wrappedRunnable;
        WrappingRunnable ( Runnable wrappedRunnable ) {
            this.wrappedRunnable = wrappedRunnable;
        }
        @Override
        public void run() {
            try {
                wrappedRunnable.run();
            } catch ( StopPooledThreadException exc ) {
                log.debug ( "Thread exiting on purpose", exc );
            }
        }
    }
}
