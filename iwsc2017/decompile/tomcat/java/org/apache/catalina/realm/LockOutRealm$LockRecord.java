package org.apache.catalina.realm;
import java.util.concurrent.atomic.AtomicInteger;
protected static class LockRecord {
    private final AtomicInteger failures;
    private long lastFailureTime;
    protected LockRecord() {
        this.failures = new AtomicInteger ( 0 );
        this.lastFailureTime = 0L;
    }
    public int getFailures() {
        return this.failures.get();
    }
    public void setFailures ( final int theFailures ) {
        this.failures.set ( theFailures );
    }
    public long getLastFailureTime() {
        return this.lastFailureTime;
    }
    public void registerFailure() {
        this.failures.incrementAndGet();
        this.lastFailureTime = System.currentTimeMillis();
    }
}
