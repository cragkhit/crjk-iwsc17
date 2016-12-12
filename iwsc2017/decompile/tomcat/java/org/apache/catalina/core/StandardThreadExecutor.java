package org.apache.catalina.core;
import java.util.concurrent.RejectedExecutionException;
import org.apache.catalina.LifecycleState;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.tomcat.util.threads.TaskThreadFactory;
import org.apache.catalina.LifecycleException;
import org.apache.tomcat.util.threads.TaskQueue;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.apache.tomcat.util.threads.ResizableExecutor;
import org.apache.catalina.Executor;
import org.apache.catalina.util.LifecycleMBeanBase;
public class StandardThreadExecutor extends LifecycleMBeanBase implements Executor, ResizableExecutor {
    protected int threadPriority;
    protected boolean daemon;
    protected String namePrefix;
    protected int maxThreads;
    protected int minSpareThreads;
    protected int maxIdleTime;
    protected ThreadPoolExecutor executor;
    protected String name;
    protected boolean prestartminSpareThreads;
    protected int maxQueueSize;
    protected long threadRenewalDelay;
    private TaskQueue taskqueue;
    public StandardThreadExecutor() {
        this.threadPriority = 5;
        this.daemon = true;
        this.namePrefix = "tomcat-exec-";
        this.maxThreads = 200;
        this.minSpareThreads = 25;
        this.maxIdleTime = 60000;
        this.executor = null;
        this.prestartminSpareThreads = false;
        this.maxQueueSize = Integer.MAX_VALUE;
        this.threadRenewalDelay = 1000L;
        this.taskqueue = null;
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
    }
    @Override
    protected void startInternal() throws LifecycleException {
        this.taskqueue = new TaskQueue ( this.maxQueueSize );
        final TaskThreadFactory tf = new TaskThreadFactory ( this.namePrefix, this.daemon, this.getThreadPriority() );
        ( this.executor = new ThreadPoolExecutor ( this.getMinSpareThreads(), this.getMaxThreads(), this.maxIdleTime, TimeUnit.MILLISECONDS, this.taskqueue, tf ) ).setThreadRenewalDelay ( this.threadRenewalDelay );
        if ( this.prestartminSpareThreads ) {
            this.executor.prestartAllCoreThreads();
        }
        this.taskqueue.setParent ( this.executor );
        this.setState ( LifecycleState.STARTING );
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        this.setState ( LifecycleState.STOPPING );
        if ( this.executor != null ) {
            this.executor.shutdownNow();
        }
        this.executor = null;
        this.taskqueue = null;
    }
    @Override
    protected void destroyInternal() throws LifecycleException {
        super.destroyInternal();
    }
    @Override
    public void execute ( final Runnable command, final long timeout, final TimeUnit unit ) {
        if ( this.executor != null ) {
            this.executor.execute ( command, timeout, unit );
            return;
        }
        throw new IllegalStateException ( "StandardThreadExecutor not started." );
    }
    @Override
    public void execute ( final Runnable command ) {
        if ( this.executor != null ) {
            try {
                this.executor.execute ( command );
            } catch ( RejectedExecutionException rx ) {
                if ( ! ( ( TaskQueue ) this.executor.getQueue() ).force ( command ) ) {
                    throw new RejectedExecutionException ( "Work queue full." );
                }
            }
            return;
        }
        throw new IllegalStateException ( "StandardThreadPool not started." );
    }
    public void contextStopping() {
        if ( this.executor != null ) {
            this.executor.contextStopping();
        }
    }
    public int getThreadPriority() {
        return this.threadPriority;
    }
    public boolean isDaemon() {
        return this.daemon;
    }
    public String getNamePrefix() {
        return this.namePrefix;
    }
    public int getMaxIdleTime() {
        return this.maxIdleTime;
    }
    @Override
    public int getMaxThreads() {
        return this.maxThreads;
    }
    public int getMinSpareThreads() {
        return this.minSpareThreads;
    }
    @Override
    public String getName() {
        return this.name;
    }
    public boolean isPrestartminSpareThreads() {
        return this.prestartminSpareThreads;
    }
    public void setThreadPriority ( final int threadPriority ) {
        this.threadPriority = threadPriority;
    }
    public void setDaemon ( final boolean daemon ) {
        this.daemon = daemon;
    }
    public void setNamePrefix ( final String namePrefix ) {
        this.namePrefix = namePrefix;
    }
    public void setMaxIdleTime ( final int maxIdleTime ) {
        this.maxIdleTime = maxIdleTime;
        if ( this.executor != null ) {
            this.executor.setKeepAliveTime ( maxIdleTime, TimeUnit.MILLISECONDS );
        }
    }
    public void setMaxThreads ( final int maxThreads ) {
        this.maxThreads = maxThreads;
        if ( this.executor != null ) {
            this.executor.setMaximumPoolSize ( maxThreads );
        }
    }
    public void setMinSpareThreads ( final int minSpareThreads ) {
        this.minSpareThreads = minSpareThreads;
        if ( this.executor != null ) {
            this.executor.setCorePoolSize ( minSpareThreads );
        }
    }
    public void setPrestartminSpareThreads ( final boolean prestartminSpareThreads ) {
        this.prestartminSpareThreads = prestartminSpareThreads;
    }
    public void setName ( final String name ) {
        this.name = name;
    }
    public void setMaxQueueSize ( final int size ) {
        this.maxQueueSize = size;
    }
    public int getMaxQueueSize() {
        return this.maxQueueSize;
    }
    public long getThreadRenewalDelay() {
        return this.threadRenewalDelay;
    }
    public void setThreadRenewalDelay ( final long threadRenewalDelay ) {
        this.threadRenewalDelay = threadRenewalDelay;
        if ( this.executor != null ) {
            this.executor.setThreadRenewalDelay ( threadRenewalDelay );
        }
    }
    @Override
    public int getActiveCount() {
        return ( this.executor != null ) ? this.executor.getActiveCount() : 0;
    }
    public long getCompletedTaskCount() {
        return ( this.executor != null ) ? this.executor.getCompletedTaskCount() : 0L;
    }
    public int getCorePoolSize() {
        return ( this.executor != null ) ? this.executor.getCorePoolSize() : 0;
    }
    public int getLargestPoolSize() {
        return ( this.executor != null ) ? this.executor.getLargestPoolSize() : 0;
    }
    @Override
    public int getPoolSize() {
        return ( this.executor != null ) ? this.executor.getPoolSize() : 0;
    }
    public int getQueueSize() {
        return ( this.executor != null ) ? this.executor.getQueue().size() : -1;
    }
    @Override
    public boolean resizePool ( final int corePoolSize, final int maximumPoolSize ) {
        if ( this.executor == null ) {
            return false;
        }
        this.executor.setCorePoolSize ( corePoolSize );
        this.executor.setMaximumPoolSize ( maximumPoolSize );
        return true;
    }
    @Override
    public boolean resizeQueue ( final int capacity ) {
        return false;
    }
    @Override
    protected String getDomainInternal() {
        return null;
    }
    @Override
    protected String getObjectNameKeyProperties() {
        final StringBuilder name = new StringBuilder ( "type=Executor,name=" );
        name.append ( this.getName() );
        return name.toString();
    }
}
