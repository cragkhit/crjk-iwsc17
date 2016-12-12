package org.apache.catalina.tribes.util;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
public class ExecutorFactory {
    protected static final StringManager sm;
    public static ExecutorService newThreadPool ( final int minThreads, final int maxThreads, final long maxIdleTime, final TimeUnit unit ) {
        final TaskQueue taskqueue = new TaskQueue();
        final ThreadPoolExecutor service = new TribesThreadPoolExecutor ( minThreads, maxThreads, maxIdleTime, unit, taskqueue );
        taskqueue.setParent ( service );
        return service;
    }
    public static ExecutorService newThreadPool ( final int minThreads, final int maxThreads, final long maxIdleTime, final TimeUnit unit, final ThreadFactory threadFactory ) {
        final TaskQueue taskqueue = new TaskQueue();
        final ThreadPoolExecutor service = new TribesThreadPoolExecutor ( minThreads, maxThreads, maxIdleTime, unit, taskqueue, threadFactory );
        taskqueue.setParent ( service );
        return service;
    }
    static {
        sm = StringManager.getManager ( ExecutorFactory.class );
    }
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
    private static class TaskQueue extends LinkedBlockingQueue<Runnable> {
        private static final long serialVersionUID = 1L;
        ThreadPoolExecutor parent;
        public TaskQueue() {
            this.parent = null;
        }
        public void setParent ( final ThreadPoolExecutor tp ) {
            this.parent = tp;
        }
        public boolean force ( final Runnable o ) {
            if ( this.parent.isShutdown() ) {
                throw new RejectedExecutionException ( ExecutorFactory.sm.getString ( "executorFactory.not.running" ) );
            }
            return super.offer ( o );
        }
        @Override
        public boolean offer ( final Runnable o ) {
            if ( this.parent == null ) {
                return super.offer ( o );
            }
            if ( this.parent.getPoolSize() == this.parent.getMaximumPoolSize() ) {
                return super.offer ( o );
            }
            if ( this.parent.getActiveCount() < this.parent.getPoolSize() ) {
                return super.offer ( o );
            }
            return this.parent.getPoolSize() >= this.parent.getMaximumPoolSize() && super.offer ( o );
        }
    }
}
