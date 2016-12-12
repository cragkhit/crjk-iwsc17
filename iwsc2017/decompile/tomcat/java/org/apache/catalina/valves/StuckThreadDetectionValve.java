package org.apache.catalina.valves;
import java.util.Date;
import java.util.concurrent.Semaphore;
import org.apache.juli.logging.LogFactory;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.servlet.ServletException;
import java.io.IOException;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import org.apache.catalina.LifecycleException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Queue;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public class StuckThreadDetectionValve extends ValveBase {
    private static final Log log;
    private static final StringManager sm;
    private final AtomicInteger stuckCount;
    private AtomicLong interruptedThreadsCount;
    private int threshold;
    private int interruptThreadThreshold;
    private final Map<Long, MonitoredThread> activeThreads;
    private final Queue<CompletedStuckThread> completedStuckThreadsQueue;
    public void setThreshold ( final int threshold ) {
        this.threshold = threshold;
    }
    public int getThreshold() {
        return this.threshold;
    }
    public int getInterruptThreadThreshold() {
        return this.interruptThreadThreshold;
    }
    public void setInterruptThreadThreshold ( final int interruptThreadThreshold ) {
        this.interruptThreadThreshold = interruptThreadThreshold;
    }
    public StuckThreadDetectionValve() {
        super ( true );
        this.stuckCount = new AtomicInteger ( 0 );
        this.interruptedThreadsCount = new AtomicLong();
        this.threshold = 600;
        this.activeThreads = new ConcurrentHashMap<Long, MonitoredThread>();
        this.completedStuckThreadsQueue = new ConcurrentLinkedQueue<CompletedStuckThread>();
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        if ( StuckThreadDetectionValve.log.isDebugEnabled() ) {
            StuckThreadDetectionValve.log.debug ( "Monitoring stuck threads with threshold = " + this.threshold + " sec" );
        }
    }
    private void notifyStuckThreadDetected ( final MonitoredThread monitoredThread, final long activeTime, final int numStuckThreads ) {
        if ( StuckThreadDetectionValve.log.isWarnEnabled() ) {
            final String msg = StuckThreadDetectionValve.sm.getString ( "stuckThreadDetectionValve.notifyStuckThreadDetected", monitoredThread.getThread().getName(), activeTime, monitoredThread.getStartTime(), numStuckThreads, monitoredThread.getRequestUri(), this.threshold, String.valueOf ( monitoredThread.getThread().getId() ) );
            final Throwable th = new Throwable();
            th.setStackTrace ( monitoredThread.getThread().getStackTrace() );
            StuckThreadDetectionValve.log.warn ( msg, th );
        }
    }
    private void notifyStuckThreadCompleted ( final CompletedStuckThread thread, final int numStuckThreads ) {
        if ( StuckThreadDetectionValve.log.isWarnEnabled() ) {
            final String msg = StuckThreadDetectionValve.sm.getString ( "stuckThreadDetectionValve.notifyStuckThreadCompleted", thread.getName(), thread.getTotalActiveTime(), numStuckThreads, String.valueOf ( thread.getId() ) );
            StuckThreadDetectionValve.log.warn ( msg );
        }
    }
    @Override
    public void invoke ( final Request request, final Response response ) throws IOException, ServletException {
        if ( this.threshold <= 0 ) {
            this.getNext().invoke ( request, response );
            return;
        }
        final Long key = Thread.currentThread().getId();
        final StringBuffer requestUrl = request.getRequestURL();
        if ( request.getQueryString() != null ) {
            requestUrl.append ( "?" );
            requestUrl.append ( request.getQueryString() );
        }
        final MonitoredThread monitoredThread = new MonitoredThread ( Thread.currentThread(), requestUrl.toString(), this.interruptThreadThreshold > 0 );
        this.activeThreads.put ( key, monitoredThread );
        try {
            this.getNext().invoke ( request, response );
        } finally {
            this.activeThreads.remove ( key );
            if ( monitoredThread.markAsDone() == MonitoredThreadState.STUCK ) {
                if ( monitoredThread.wasInterrupted() ) {
                    this.interruptedThreadsCount.incrementAndGet();
                }
                this.completedStuckThreadsQueue.add ( new CompletedStuckThread ( monitoredThread.getThread(), monitoredThread.getActiveTimeInMillis() ) );
            }
        }
    }
    @Override
    public void backgroundProcess() {
        super.backgroundProcess();
        final long thresholdInMillis = this.threshold * 1000L;
        for ( final MonitoredThread monitoredThread : this.activeThreads.values() ) {
            final long activeTime = monitoredThread.getActiveTimeInMillis();
            if ( activeTime >= thresholdInMillis && monitoredThread.markAsStuckIfStillRunning() ) {
                final int numStuckThreads = this.stuckCount.incrementAndGet();
                this.notifyStuckThreadDetected ( monitoredThread, activeTime, numStuckThreads );
            }
            if ( this.interruptThreadThreshold > 0 && activeTime >= this.interruptThreadThreshold * 1000L ) {
                monitoredThread.interruptIfStuck ( this.interruptThreadThreshold );
            }
        }
        for ( CompletedStuckThread completedStuckThread = this.completedStuckThreadsQueue.poll(); completedStuckThread != null; completedStuckThread = this.completedStuckThreadsQueue.poll() ) {
            final int numStuckThreads2 = this.stuckCount.decrementAndGet();
            this.notifyStuckThreadCompleted ( completedStuckThread, numStuckThreads2 );
        }
    }
    public int getStuckThreadCount() {
        return this.stuckCount.get();
    }
    public long[] getStuckThreadIds() {
        final List<Long> idList = new ArrayList<Long>();
        for ( final MonitoredThread monitoredThread : this.activeThreads.values() ) {
            if ( monitoredThread.isMarkedAsStuck() ) {
                idList.add ( monitoredThread.getThread().getId() );
            }
        }
        final long[] result = new long[idList.size()];
        for ( int i = 0; i < result.length; ++i ) {
            result[i] = idList.get ( i );
        }
        return result;
    }
    public String[] getStuckThreadNames() {
        final List<String> nameList = new ArrayList<String>();
        for ( final MonitoredThread monitoredThread : this.activeThreads.values() ) {
            if ( monitoredThread.isMarkedAsStuck() ) {
                nameList.add ( monitoredThread.getThread().getName() );
            }
        }
        return nameList.toArray ( new String[nameList.size()] );
    }
    public long getInterruptedThreadsCount() {
        return this.interruptedThreadsCount.get();
    }
    static {
        log = LogFactory.getLog ( StuckThreadDetectionValve.class );
        sm = StringManager.getManager ( "org.apache.catalina.valves" );
    }
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
                    StuckThreadDetectionValve.log.debug ( "thread interrupted after the request is finished, ignoring", e );
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
                if ( StuckThreadDetectionValve.log.isWarnEnabled() ) {
                    final String msg = StuckThreadDetectionValve.sm.getString ( "stuckThreadDetectionValve.notifyStuckThreadInterrupted", this.getThread().getName(), this.getActiveTimeInMillis(), this.getStartTime(), this.getRequestUri(), interruptThreadThreshold, String.valueOf ( this.getThread().getId() ) );
                    final Throwable th = new Throwable();
                    th.setStackTrace ( this.getThread().getStackTrace() );
                    StuckThreadDetectionValve.log.warn ( msg, th );
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
    private static class CompletedStuckThread {
        private final String threadName;
        private final long threadId;
        private final long totalActiveTime;
        public CompletedStuckThread ( final Thread thread, final long totalActiveTime ) {
            this.threadName = thread.getName();
            this.threadId = thread.getId();
            this.totalActiveTime = totalActiveTime;
        }
        public String getName() {
            return this.threadName;
        }
        public long getId() {
            return this.threadId;
        }
        public long getTotalActiveTime() {
            return this.totalActiveTime;
        }
    }
    private enum MonitoredThreadState {
        RUNNING,
        STUCK,
        DONE;
    }
}
