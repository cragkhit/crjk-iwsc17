package org.junit.internal.runners.statements;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.internal.management.ManagementFactory;
import org.junit.internal.management.ThreadMXBean;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestTimedOutException;
public class FailOnTimeout extends Statement {
    private final Statement originalStatement;
    private final TimeUnit timeUnit;
    private final long timeout;
    private final boolean lookForStuckThread;
    public static Builder builder() {
        return new Builder();
    }
    @Deprecated
    public FailOnTimeout ( Statement statement, long timeoutMillis ) {
        this ( builder().withTimeout ( timeoutMillis, TimeUnit.MILLISECONDS ), statement );
    }
    private FailOnTimeout ( Builder builder, Statement statement ) {
        originalStatement = statement;
        timeout = builder.timeout;
        timeUnit = builder.unit;
        lookForStuckThread = builder.lookForStuckThread;
    }
    public static class Builder {
        private boolean lookForStuckThread = false;
        private long timeout = 0;
        private TimeUnit unit = TimeUnit.SECONDS;
        private Builder() {
        }
        public Builder withTimeout ( long timeout, TimeUnit unit ) {
            if ( timeout < 0 ) {
                throw new IllegalArgumentException ( "timeout must be non-negative" );
            }
            if ( unit == null ) {
                throw new NullPointerException ( "TimeUnit cannot be null" );
            }
            this.timeout = timeout;
            this.unit = unit;
            return this;
        }
        public Builder withLookingForStuckThread ( boolean enable ) {
            this.lookForStuckThread = enable;
            return this;
        }
        public FailOnTimeout build ( Statement statement ) {
            if ( statement == null ) {
                throw new NullPointerException ( "statement cannot be null" );
            }
            return new FailOnTimeout ( this, statement );
        }
    }
    @Override
    public void evaluate() throws Throwable {
        CallableStatement callable = new CallableStatement();
        FutureTask<Throwable> task = new FutureTask<Throwable> ( callable );
        ThreadGroup threadGroup = new ThreadGroup ( "FailOnTimeoutGroup" );
        Thread thread = new Thread ( threadGroup, task, "Time-limited test" );
        thread.setDaemon ( true );
        thread.start();
        callable.awaitStarted();
        Throwable throwable = getResult ( task, thread );
        if ( throwable != null ) {
            throw throwable;
        }
    }
    private Throwable getResult ( FutureTask<Throwable> task, Thread thread ) {
        try {
            if ( timeout > 0 ) {
                return task.get ( timeout, timeUnit );
            } else {
                return task.get();
            }
        } catch ( InterruptedException e ) {
            return e;
        } catch ( ExecutionException e ) {
            return e.getCause();
        } catch ( TimeoutException e ) {
            return createTimeoutException ( thread );
        }
    }
    private Exception createTimeoutException ( Thread thread ) {
        StackTraceElement[] stackTrace = thread.getStackTrace();
        final Thread stuckThread = lookForStuckThread ? getStuckThread ( thread ) : null;
        Exception currThreadException = new TestTimedOutException ( timeout, timeUnit );
        if ( stackTrace != null ) {
            currThreadException.setStackTrace ( stackTrace );
            thread.interrupt();
        }
        if ( stuckThread != null ) {
            Exception stuckThreadException =
                new Exception ( "Appears to be stuck in thread " +
                                stuckThread.getName() );
            stuckThreadException.setStackTrace ( getStackTrace ( stuckThread ) );
            return new MultipleFailureException (
                       Arrays.<Throwable>asList ( currThreadException, stuckThreadException ) );
        } else {
            return currThreadException;
        }
    }
    private StackTraceElement[] getStackTrace ( Thread thread ) {
        try {
            return thread.getStackTrace();
        } catch ( SecurityException e ) {
            return new StackTraceElement[0];
        }
    }
    private Thread getStuckThread ( Thread mainThread ) {
        List<Thread> threadsInGroup = getThreadsInGroup ( mainThread.getThreadGroup() );
        if ( threadsInGroup.isEmpty() ) {
            return null;
        }
        Thread stuckThread = null;
        long maxCpuTime = 0;
        for ( Thread thread : threadsInGroup ) {
            if ( thread.getState() == Thread.State.RUNNABLE ) {
                long threadCpuTime = cpuTime ( thread );
                if ( stuckThread == null || threadCpuTime > maxCpuTime ) {
                    stuckThread = thread;
                    maxCpuTime = threadCpuTime;
                }
            }
        }
        return ( stuckThread == mainThread ) ? null : stuckThread;
    }
    private List<Thread> getThreadsInGroup ( ThreadGroup group ) {
        final int activeThreadCount = group.activeCount();
        int threadArraySize = Math.max ( activeThreadCount * 2, 100 );
        for ( int loopCount = 0; loopCount < 5; loopCount++ ) {
            Thread[] threads = new Thread[threadArraySize];
            int enumCount = group.enumerate ( threads );
            if ( enumCount < threadArraySize ) {
                return Arrays.asList ( threads ).subList ( 0, enumCount );
            }
            threadArraySize += 100;
        }
        return Collections.emptyList();
    }
    private long cpuTime ( Thread thr ) {
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        if ( mxBean.isThreadCpuTimeSupported() ) {
            try {
                return mxBean.getThreadCpuTime ( thr.getId() );
            } catch ( UnsupportedOperationException e ) {
            }
        }
        return 0;
    }
    private class CallableStatement implements Callable<Throwable> {
        private final CountDownLatch startLatch = new CountDownLatch ( 1 );
        public Throwable call() throws Exception {
            try {
                startLatch.countDown();
                originalStatement.evaluate();
            } catch ( Exception e ) {
                throw e;
            } catch ( Throwable e ) {
                return e;
            }
            return null;
        }
        public void awaitStarted() throws InterruptedException {
            startLatch.await();
        }
    }
}
