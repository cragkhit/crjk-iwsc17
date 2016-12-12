package org.apache.catalina.valves;
import java.util.Date;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
private static class MonitoredThread {
    private final Thread thread;
    private final String requestUri;
    private final long start;
    private final AtomicInteger state;
    private final Semaphore interruptionSemaphore;
    private boolean interrupted;
    public MonitoredThread ( final Thread thread, final String requestUri, final boolean interruptible ) {
        this.state = new AtomicInteger ( MonitoredThreadState.RUNNING.ordinal() );
        this.thread = thread;
        this.requestUri = requestUri;
        this.start = System.currentTimeMillis();
        if ( interruptible ) {
            this.interruptionSemaphore = new Semaphore ( 1 );
        } else {
            this.interruptionSemaphore = null;
        }
    }
    public Thread getThread() {
        return this.thread;
    }
    public String getRequestUri() {
        return this.requestUri;
    }
    public long getActiveTimeInMillis() {
        return System.currentTimeMillis() - this.start;
    }
    public Date getStartTime() {
        return new Date ( this.start );
    }
    public boolean markAsStuckIfStillRunning() {
        return this.state.compareAndSet ( MonitoredThreadState.RUNNING.ordinal(), MonitoredThreadState.STUCK.ordinal() );
    }
    public MonitoredThreadState markAsDone() {
        final int val = this.state.getAndSet ( MonitoredThreadState.DONE.ordinal() );
        final MonitoredThreadState threadState = MonitoredThreadState.values() [val];
        if ( threadState == MonitoredThreadState.STUCK && this.interruptionSemaphore != null ) {
            try {
                this.interruptionSemaphore.acquire();
            } catch ( InterruptedException e ) {
                StuckThreadDetectionValve.access$000().debug ( "thread interrupted after the request is finished, ignoring", e );
            }
        }
        return threadState;
    }
    boolean isMarkedAsStuck() {
        return this.state.get() == MonitoredThreadState.STUCK.ordinal();
    }
    public boolean interruptIfStuck ( final long interruptThreadThreshold ) {
        if ( !this.isMarkedAsStuck() || this.interruptionSemaphore == null || !this.interruptionSemaphore.tryAcquire() ) {
            return false;
        }
        try {
            if ( StuckThreadDetectionValve.access$000().isWarnEnabled() ) {
                final String msg = StuckThreadDetectionValve.access$100().getString ( "stuckThreadDetectionValve.notifyStuckThreadInterrupted", this.getThread().getName(), this.getActiveTimeInMillis(), this.getStartTime(), this.getRequestUri(), interruptThreadThreshold, String.valueOf ( this.getThread().getId() ) );
                final Throwable th = new Throwable();
                th.setStackTrace ( this.getThread().getStackTrace() );
                StuckThreadDetectionValve.access$000().warn ( msg, th );
            }
            this.thread.interrupt();
        } finally {
            this.interrupted = true;
            this.interruptionSemaphore.release();
        }
        return true;
    }
    public boolean wasInterrupted() {
        return this.interrupted;
    }
}
