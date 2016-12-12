package org.apache.catalina.tribes.util;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;
private static class TribesThreadPoolExecutor extends ThreadPoolExecutor {
    public TribesThreadPoolExecutor ( final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final RejectedExecutionHandler handler ) {
        super ( corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler );
    }
    public TribesThreadPoolExecutor ( final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory, final RejectedExecutionHandler handler ) {
        super ( corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler );
    }
    public TribesThreadPoolExecutor ( final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory ) {
        super ( corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory );
    }
    public TribesThreadPoolExecutor ( final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue ) {
        super ( corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue );
    }
    @Override
    public void execute ( final Runnable command ) {
        try {
            super.execute ( command );
        } catch ( RejectedExecutionException rx ) {
            if ( super.getQueue() instanceof TaskQueue ) {
                final TaskQueue queue = ( TaskQueue ) super.getQueue();
                if ( !queue.force ( command ) ) {
                    throw new RejectedExecutionException ( ExecutorFactory.sm.getString ( "executorFactory.queue.full" ) );
                }
            }
        }
    }
}
