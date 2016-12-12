package org.apache.tomcat.util.threads;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
private class Sync extends AbstractQueuedSynchronizer {
    private static final long serialVersionUID = 1L;
    @Override
    protected int tryAcquireShared ( final int ignored ) {
        final long newCount = LimitLatch.access$000 ( LimitLatch.this ).incrementAndGet();
        if ( !LimitLatch.access$100 ( LimitLatch.this ) && newCount > LimitLatch.access$200 ( LimitLatch.this ) ) {
            LimitLatch.access$000 ( LimitLatch.this ).decrementAndGet();
            return -1;
        }
        return 1;
    }
    @Override
    protected boolean tryReleaseShared ( final int arg ) {
        LimitLatch.access$000 ( LimitLatch.this ).decrementAndGet();
        return true;
    }
}
