package org.apache.tomcat.util.threads;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.tomcat.util.res.StringManager;
public class ThreadPoolExecutor extends java.util.concurrent.ThreadPoolExecutor {
    protected static final StringManager sm = StringManager
            .getManager ( "org.apache.tomcat.util.threads.res" );
    private final AtomicInteger submittedCount = new AtomicInteger ( 0 );
    private final AtomicLong lastContextStoppedTime = new AtomicLong ( 0L );
    private final AtomicLong lastTimeThreadKilledItself = new AtomicLong ( 0L );
    private long threadRenewalDelay = Constants.DEFAULT_THREAD_RENEWAL_DELAY;
    public ThreadPoolExecutor ( int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler ) {
        super ( corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler );
    }
    public ThreadPoolExecutor ( int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
                                RejectedExecutionHandler handler ) {
        super ( corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler );
    }
    public ThreadPoolExecutor ( int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory ) {
        super ( corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, new RejectHandler() );
    }
    public ThreadPoolExecutor ( int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue ) {
        super ( corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new RejectHandler() );
    }
    public long getThreadRenewalDelay() {
        return threadRenewalDelay;
    }
    public void setThreadRenewalDelay ( long threadRenewalDelay ) {
        this.threadRenewalDelay = threadRenewalDelay;
    }
    @Override
    protected void afterExecute ( Runnable r, Throwable t ) {
        submittedCount.decrementAndGet();
        if ( t == null ) {
            stopCurrentThreadIfNeeded();
        }
    }
    protected void stopCurrentThreadIfNeeded() {
        if ( currentThreadShouldBeStopped() ) {
            long lastTime = lastTimeThreadKilledItself.longValue();
            if ( lastTime + threadRenewalDelay < System.currentTimeMillis() ) {
                if ( lastTimeThreadKilledItself.compareAndSet ( lastTime,
                        System.currentTimeMillis() + 1 ) ) {
                    final String msg = sm.getString (
                                           "threadPoolExecutor.threadStoppedToAvoidPotentialLeak",
                                           Thread.currentThread().getName() );
                    throw new StopPooledThreadException ( msg );
                }
            }
        }
    }
    protected boolean currentThreadShouldBeStopped() {
        if ( threadRenewalDelay >= 0
                && Thread.currentThread() instanceof TaskThread ) {
            TaskThread currentTaskThread = ( TaskThread ) Thread.currentThread();
            if ( currentTaskThread.getCreationTime() <
                    this.lastContextStoppedTime.longValue() ) {
                return true;
            }
        }
        return false;
    }
    public int getSubmittedCount() {
        return submittedCount.get();
    }
    @Override
    public void execute ( Runnable command ) {
        execute ( command, 0, TimeUnit.MILLISECONDS );
    }
    public void execute ( Runnable command, long timeout, TimeUnit unit ) {
        submittedCount.incrementAndGet();
        try {
            super.execute ( command );
        } catch ( RejectedExecutionException rx ) {
            if ( super.getQueue() instanceof TaskQueue ) {
                final TaskQueue queue = ( TaskQueue ) super.getQueue();
                try {
                    if ( !queue.force ( command, timeout, unit ) ) {
                        submittedCount.decrementAndGet();
                        throw new RejectedExecutionException ( "Queue capacity is full." );
                    }
                } catch ( InterruptedException x ) {
                    submittedCount.decrementAndGet();
                    throw new RejectedExecutionException ( x );
                }
            } else {
                submittedCount.decrementAndGet();
                throw rx;
            }
        }
    }
    public void contextStopping() {
        this.lastContextStoppedTime.set ( System.currentTimeMillis() );
        int savedCorePoolSize = this.getCorePoolSize();
        TaskQueue taskQueue =
            getQueue() instanceof TaskQueue ? ( TaskQueue ) getQueue() : null;
        if ( taskQueue != null ) {
            taskQueue.setForcedRemainingCapacity ( Integer.valueOf ( 0 ) );
        }
        this.setCorePoolSize ( 0 );
        if ( taskQueue != null ) {
            taskQueue.setForcedRemainingCapacity ( null );
        }
        this.setCorePoolSize ( savedCorePoolSize );
    }
    private static class RejectHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution ( Runnable r,
                                        java.util.concurrent.ThreadPoolExecutor executor ) {
            throw new RejectedExecutionException();
        }
    }
}
