package org.apache.catalina.valves;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.ServletException;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public class StuckThreadDetectionValve extends ValveBase {
    private static final Log log = LogFactory.getLog ( StuckThreadDetectionValve.class );
    private static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    private final AtomicInteger stuckCount = new AtomicInteger ( 0 );
    private AtomicLong interruptedThreadsCount = new AtomicLong();
    private int threshold = 600;
    private int interruptThreadThreshold;
    private final Map<Long, MonitoredThread> activeThreads = new ConcurrentHashMap<>();
    private final Queue<CompletedStuckThread> completedStuckThreadsQueue =
        new ConcurrentLinkedQueue<>();
    public void setThreshold ( int threshold ) {
        this.threshold = threshold;
    }
    public int getThreshold() {
        return threshold;
    }
    public int getInterruptThreadThreshold() {
        return interruptThreadThreshold;
    }
    public void setInterruptThreadThreshold ( int interruptThreadThreshold ) {
        this.interruptThreadThreshold = interruptThreadThreshold;
    }
    public StuckThreadDetectionValve() {
        super ( true );
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        if ( log.isDebugEnabled() ) {
            log.debug ( "Monitoring stuck threads with threshold = "
                        + threshold
                        + " sec" );
        }
    }
    private void notifyStuckThreadDetected ( MonitoredThread monitoredThread,
            long activeTime, int numStuckThreads ) {
        if ( log.isWarnEnabled() ) {
            String msg = sm.getString (
                             "stuckThreadDetectionValve.notifyStuckThreadDetected",
                             monitoredThread.getThread().getName(),
                             Long.valueOf ( activeTime ),
                             monitoredThread.getStartTime(),
                             Integer.valueOf ( numStuckThreads ),
                             monitoredThread.getRequestUri(),
                             Integer.valueOf ( threshold ),
                             String.valueOf ( monitoredThread.getThread().getId() )
                         );
            Throwable th = new Throwable();
            th.setStackTrace ( monitoredThread.getThread().getStackTrace() );
            log.warn ( msg, th );
        }
    }
    private void notifyStuckThreadCompleted ( CompletedStuckThread thread,
            int numStuckThreads ) {
        if ( log.isWarnEnabled() ) {
            String msg = sm.getString (
                             "stuckThreadDetectionValve.notifyStuckThreadCompleted",
                             thread.getName(),
                             Long.valueOf ( thread.getTotalActiveTime() ),
                             Integer.valueOf ( numStuckThreads ),
                             String.valueOf ( thread.getId() ) );
            log.warn ( msg );
        }
    }
    @Override
    public void invoke ( Request request, Response response )
    throws IOException, ServletException {
        if ( threshold <= 0 ) {
            getNext().invoke ( request, response );
            return;
        }
        Long key = Long.valueOf ( Thread.currentThread().getId() );
        StringBuffer requestUrl = request.getRequestURL();
        if ( request.getQueryString() != null ) {
            requestUrl.append ( "?" );
            requestUrl.append ( request.getQueryString() );
        }
        MonitoredThread monitoredThread = new MonitoredThread ( Thread.currentThread(),
                requestUrl.toString(), interruptThreadThreshold > 0 );
        activeThreads.put ( key, monitoredThread );
        try {
            getNext().invoke ( request, response );
        } finally {
            activeThreads.remove ( key );
            if ( monitoredThread.markAsDone() == MonitoredThreadState.STUCK ) {
                if ( monitoredThread.wasInterrupted() ) {
                    interruptedThreadsCount.incrementAndGet();
                }
                completedStuckThreadsQueue.add (
                    new CompletedStuckThread ( monitoredThread.getThread(),
                                               monitoredThread.getActiveTimeInMillis() ) );
            }
        }
    }
    @Override
    public void backgroundProcess() {
        super.backgroundProcess();
        long thresholdInMillis = threshold * 1000L;
        for ( MonitoredThread monitoredThread : activeThreads.values() ) {
            long activeTime = monitoredThread.getActiveTimeInMillis();
            if ( activeTime >= thresholdInMillis && monitoredThread.markAsStuckIfStillRunning() ) {
                int numStuckThreads = stuckCount.incrementAndGet();
                notifyStuckThreadDetected ( monitoredThread, activeTime, numStuckThreads );
            }
            if ( interruptThreadThreshold > 0 && activeTime >= interruptThreadThreshold * 1000L ) {
                monitoredThread.interruptIfStuck ( interruptThreadThreshold );
            }
        }
        for ( CompletedStuckThread completedStuckThread = completedStuckThreadsQueue.poll();
                completedStuckThread != null; completedStuckThread = completedStuckThreadsQueue.poll() ) {
            int numStuckThreads = stuckCount.decrementAndGet();
            notifyStuckThreadCompleted ( completedStuckThread, numStuckThreads );
        }
    }
    public int getStuckThreadCount() {
        return stuckCount.get();
    }
    public long[] getStuckThreadIds() {
        List<Long> idList = new ArrayList<>();
        for ( MonitoredThread monitoredThread : activeThreads.values() ) {
            if ( monitoredThread.isMarkedAsStuck() ) {
                idList.add ( Long.valueOf ( monitoredThread.getThread().getId() ) );
            }
        }
        long[] result = new long[idList.size()];
        for ( int i = 0; i < result.length; i++ ) {
            result[i] = idList.get ( i ).longValue();
        }
        return result;
    }
    public String[] getStuckThreadNames() {
        List<String> nameList = new ArrayList<>();
        for ( MonitoredThread monitoredThread : activeThreads.values() ) {
            if ( monitoredThread.isMarkedAsStuck() ) {
                nameList.add ( monitoredThread.getThread().getName() );
            }
        }
        return nameList.toArray ( new String[nameList.size()] );
    }
    public long getInterruptedThreadsCount() {
        return interruptedThreadsCount.get();
    }
    private static class MonitoredThread {
        private final Thread thread;
        private final String requestUri;
        private final long start;
        private final AtomicInteger state = new AtomicInteger (
            MonitoredThreadState.RUNNING.ordinal() );
        private final Semaphore interruptionSemaphore;
        private boolean interrupted;
        public MonitoredThread ( Thread thread, String requestUri,
                                 boolean interruptible ) {
            this.thread = thread;
            this.requestUri = requestUri;
            this.start = System.currentTimeMillis();
            if ( interruptible ) {
                interruptionSemaphore = new Semaphore ( 1 );
            } else {
                interruptionSemaphore = null;
            }
        }
        public Thread getThread() {
            return this.thread;
        }
        public String getRequestUri() {
            return requestUri;
        }
        public long getActiveTimeInMillis() {
            return System.currentTimeMillis() - start;
        }
        public Date getStartTime() {
            return new Date ( start );
        }
        public boolean markAsStuckIfStillRunning() {
            return this.state.compareAndSet ( MonitoredThreadState.RUNNING.ordinal(),
                                              MonitoredThreadState.STUCK.ordinal() );
        }
        public MonitoredThreadState markAsDone() {
            int val = this.state.getAndSet ( MonitoredThreadState.DONE.ordinal() );
            MonitoredThreadState threadState = MonitoredThreadState.values() [val];
            if ( threadState == MonitoredThreadState.STUCK
                    && interruptionSemaphore != null ) {
                try {
                    this.interruptionSemaphore.acquire();
                } catch ( InterruptedException e ) {
                    log.debug (
                        "thread interrupted after the request is finished, ignoring",
                        e );
                }
            }
            return threadState;
        }
        boolean isMarkedAsStuck() {
            return this.state.get() == MonitoredThreadState.STUCK.ordinal();
        }
        public boolean interruptIfStuck ( long interruptThreadThreshold ) {
            if ( !isMarkedAsStuck() || interruptionSemaphore == null
                    || !this.interruptionSemaphore.tryAcquire() ) {
                return false;
            }
            try {
                if ( log.isWarnEnabled() ) {
                    String msg = sm.getString (
                                     "stuckThreadDetectionValve.notifyStuckThreadInterrupted",
                                     this.getThread().getName(),
                                     Long.valueOf ( getActiveTimeInMillis() ),
                                     this.getStartTime(), this.getRequestUri(),
                                     Long.valueOf ( interruptThreadThreshold ),
                                     String.valueOf ( this.getThread().getId() ) );
                    Throwable th = new Throwable();
                    th.setStackTrace ( this.getThread().getStackTrace() );
                    log.warn ( msg, th );
                }
                this.thread.interrupt();
            } finally {
                this.interrupted = true;
                this.interruptionSemaphore.release();
            }
            return true;
        }
        public boolean wasInterrupted() {
            return interrupted;
        }
    }
    private static class CompletedStuckThread {
        private final String threadName;
        private final long threadId;
        private final long totalActiveTime;
        public CompletedStuckThread ( Thread thread, long totalActiveTime ) {
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
        RUNNING, STUCK, DONE;
    }
}
