package org.apache.catalina.core;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.catalina.Executor;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.tomcat.util.threads.ResizableExecutor;
import org.apache.tomcat.util.threads.TaskQueue;
import org.apache.tomcat.util.threads.TaskThreadFactory;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
public class StandardThreadExecutor extends LifecycleMBeanBase
    implements Executor, ResizableExecutor {
    protected int threadPriority = Thread.NORM_PRIORITY;
    protected boolean daemon = true;
    protected String namePrefix = "tomcat-exec-";
    protected int maxThreads = 200;
    protected int minSpareThreads = 25;
    protected int maxIdleTime = 60000;
    protected ThreadPoolExecutor executor = null;
    protected String name;
    protected boolean prestartminSpareThreads = false;
    protected int maxQueueSize = Integer.MAX_VALUE;
    protected long threadRenewalDelay =
        org.apache.tomcat.util.threads.Constants.DEFAULT_THREAD_RENEWAL_DELAY;
    private TaskQueue taskqueue = null;
    public StandardThreadExecutor() {
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
    }
    @Override
    protected void startInternal() throws LifecycleException {
        taskqueue = new TaskQueue ( maxQueueSize );
        TaskThreadFactory tf = new TaskThreadFactory ( namePrefix, daemon, getThreadPriority() );
        executor = new ThreadPoolExecutor ( getMinSpareThreads(), getMaxThreads(), maxIdleTime, TimeUnit.MILLISECONDS, taskqueue, tf );
        executor.setThreadRenewalDelay ( threadRenewalDelay );
        if ( prestartminSpareThreads ) {
            executor.prestartAllCoreThreads();
        }
        taskqueue.setParent ( executor );
        setState ( LifecycleState.STARTING );
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        setState ( LifecycleState.STOPPING );
        if ( executor != null ) {
            executor.shutdownNow();
        }
        executor = null;
        taskqueue = null;
    }
    @Override
    protected void destroyInternal() throws LifecycleException {
        super.destroyInternal();
    }
    @Override
    public void execute ( Runnable command, long timeout, TimeUnit unit ) {
        if ( executor != null ) {
            executor.execute ( command, timeout, unit );
        } else {
            throw new IllegalStateException ( "StandardThreadExecutor not started." );
        }
    }
    @Override
    public void execute ( Runnable command ) {
        if ( executor != null ) {
            try {
                executor.execute ( command );
            } catch ( RejectedExecutionException rx ) {
                if ( ! ( ( TaskQueue ) executor.getQueue() ).force ( command ) ) {
                    throw new RejectedExecutionException ( "Work queue full." );
                }
            }
        } else {
            throw new IllegalStateException ( "StandardThreadPool not started." );
        }
    }
    public void contextStopping() {
        if ( executor != null ) {
            executor.contextStopping();
        }
    }
    public int getThreadPriority() {
        return threadPriority;
    }
    public boolean isDaemon() {
        return daemon;
    }
    public String getNamePrefix() {
        return namePrefix;
    }
    public int getMaxIdleTime() {
        return maxIdleTime;
    }
    @Override
    public int getMaxThreads() {
        return maxThreads;
    }
    public int getMinSpareThreads() {
        return minSpareThreads;
    }
    @Override
    public String getName() {
        return name;
    }
    public boolean isPrestartminSpareThreads() {
        return prestartminSpareThreads;
    }
    public void setThreadPriority ( int threadPriority ) {
        this.threadPriority = threadPriority;
    }
    public void setDaemon ( boolean daemon ) {
        this.daemon = daemon;
    }
    public void setNamePrefix ( String namePrefix ) {
        this.namePrefix = namePrefix;
    }
    public void setMaxIdleTime ( int maxIdleTime ) {
        this.maxIdleTime = maxIdleTime;
        if ( executor != null ) {
            executor.setKeepAliveTime ( maxIdleTime, TimeUnit.MILLISECONDS );
        }
    }
    public void setMaxThreads ( int maxThreads ) {
        this.maxThreads = maxThreads;
        if ( executor != null ) {
            executor.setMaximumPoolSize ( maxThreads );
        }
    }
    public void setMinSpareThreads ( int minSpareThreads ) {
        this.minSpareThreads = minSpareThreads;
        if ( executor != null ) {
            executor.setCorePoolSize ( minSpareThreads );
        }
    }
    public void setPrestartminSpareThreads ( boolean prestartminSpareThreads ) {
        this.prestartminSpareThreads = prestartminSpareThreads;
    }
    public void setName ( String name ) {
        this.name = name;
    }
    public void setMaxQueueSize ( int size ) {
        this.maxQueueSize = size;
    }
    public int getMaxQueueSize() {
        return maxQueueSize;
    }
    public long getThreadRenewalDelay() {
        return threadRenewalDelay;
    }
    public void setThreadRenewalDelay ( long threadRenewalDelay ) {
        this.threadRenewalDelay = threadRenewalDelay;
        if ( executor != null ) {
            executor.setThreadRenewalDelay ( threadRenewalDelay );
        }
    }
    @Override
    public int getActiveCount() {
        return ( executor != null ) ? executor.getActiveCount() : 0;
    }
    public long getCompletedTaskCount() {
        return ( executor != null ) ? executor.getCompletedTaskCount() : 0;
    }
    public int getCorePoolSize() {
        return ( executor != null ) ? executor.getCorePoolSize() : 0;
    }
    public int getLargestPoolSize() {
        return ( executor != null ) ? executor.getLargestPoolSize() : 0;
    }
    @Override
    public int getPoolSize() {
        return ( executor != null ) ? executor.getPoolSize() : 0;
    }
    public int getQueueSize() {
        return ( executor != null ) ? executor.getQueue().size() : -1;
    }
    @Override
    public boolean resizePool ( int corePoolSize, int maximumPoolSize ) {
        if ( executor == null ) {
            return false;
        }
        executor.setCorePoolSize ( corePoolSize );
        executor.setMaximumPoolSize ( maximumPoolSize );
        return true;
    }
    @Override
    public boolean resizeQueue ( int capacity ) {
        return false;
    }
    @Override
    protected String getDomainInternal() {
        return null;
    }
    @Override
    protected String getObjectNameKeyProperties() {
        StringBuilder name = new StringBuilder ( "type=Executor,name=" );
        name.append ( getName() );
        return name.toString();
    }
}
