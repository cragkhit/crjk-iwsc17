package org.apache.catalina.tribes.util;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
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
