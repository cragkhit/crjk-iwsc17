package org.apache.tomcat.util.threads;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class LimitLatch {
    private static final Log log = LogFactory.getLog ( LimitLatch.class );
    private class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 1L;
        public Sync() {
        }
        @Override
        protected int tryAcquireShared ( int ignored ) {
            long newCount = count.incrementAndGet();
            if ( !released && newCount > limit ) {
                count.decrementAndGet();
                return -1;
            } else {
                return 1;
            }
        }
        @Override
        protected boolean tryReleaseShared ( int arg ) {
            count.decrementAndGet();
            return true;
        }
    }
    private final Sync sync;
    private final AtomicLong count;
    private volatile long limit;
    private volatile boolean released = false;
    public LimitLatch ( long limit ) {
        this.limit = limit;
        this.count = new AtomicLong ( 0 );
        this.sync = new Sync();
    }
    public long getCount() {
        return count.get();
    }
    public long getLimit() {
        return limit;
    }
    public void setLimit ( long limit ) {
        this.limit = limit;
    }
    public void countUpOrAwait() throws InterruptedException {
        if ( log.isDebugEnabled() ) {
            log.debug ( "Counting up[" + Thread.currentThread().getName() + "] latch=" + getCount() );
        }
        sync.acquireSharedInterruptibly ( 1 );
    }
    public long countDown() {
        sync.releaseShared ( 0 );
        long result = getCount();
        if ( log.isDebugEnabled() ) {
            log.debug ( "Counting down[" + Thread.currentThread().getName() + "] latch=" + result );
        }
        return result;
    }
    public boolean releaseAll() {
        released = true;
        return sync.releaseShared ( 0 );
    }
    public void reset() {
        this.count.set ( 0 );
        released = false;
    }
    public boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }
    public Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }
}
