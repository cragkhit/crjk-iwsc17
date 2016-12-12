package org.apache.tomcat.util.threads;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
public class TaskQueue extends LinkedBlockingQueue<Runnable> {
    private static final long serialVersionUID = 1L;
    private volatile ThreadPoolExecutor parent = null;
    private Integer forcedRemainingCapacity = null;
    public TaskQueue() {
        super();
    }
    public TaskQueue ( int capacity ) {
        super ( capacity );
    }
    public TaskQueue ( Collection<? extends Runnable> c ) {
        super ( c );
    }
    public void setParent ( ThreadPoolExecutor tp ) {
        parent = tp;
    }
    public boolean force ( Runnable o ) {
        if ( parent == null || parent.isShutdown() ) {
            throw new RejectedExecutionException ( "Executor not running, can't force a command into the queue" );
        }
        return super.offer ( o );
    }
    public boolean force ( Runnable o, long timeout, TimeUnit unit ) throws InterruptedException {
        if ( parent == null || parent.isShutdown() ) {
            throw new RejectedExecutionException ( "Executor not running, can't force a command into the queue" );
        }
        return super.offer ( o, timeout, unit );
    }
    @Override
    public boolean offer ( Runnable o ) {
        if ( parent == null ) {
            return super.offer ( o );
        }
        if ( parent.getPoolSize() == parent.getMaximumPoolSize() ) {
            return super.offer ( o );
        }
        if ( parent.getSubmittedCount() < ( parent.getPoolSize() ) ) {
            return super.offer ( o );
        }
        if ( parent.getPoolSize() < parent.getMaximumPoolSize() ) {
            return false;
        }
        return super.offer ( o );
    }
    @Override
    public Runnable poll ( long timeout, TimeUnit unit )
    throws InterruptedException {
        Runnable runnable = super.poll ( timeout, unit );
        if ( runnable == null && parent != null ) {
            parent.stopCurrentThreadIfNeeded();
        }
        return runnable;
    }
    @Override
    public Runnable take() throws InterruptedException {
        if ( parent != null && parent.currentThreadShouldBeStopped() ) {
            return poll ( parent.getKeepAliveTime ( TimeUnit.MILLISECONDS ),
                          TimeUnit.MILLISECONDS );
        }
        return super.take();
    }
    @Override
    public int remainingCapacity() {
        if ( forcedRemainingCapacity != null ) {
            return forcedRemainingCapacity.intValue();
        }
        return super.remainingCapacity();
    }
    public void setForcedRemainingCapacity ( Integer forcedRemainingCapacity ) {
        this.forcedRemainingCapacity = forcedRemainingCapacity;
    }
}
