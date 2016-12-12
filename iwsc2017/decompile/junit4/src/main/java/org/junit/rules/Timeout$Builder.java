package org.junit.rules;
import java.util.concurrent.TimeUnit;
public static class Builder {
    private boolean lookForStuckThread;
    private long timeout;
    private TimeUnit timeUnit;
    protected Builder() {
        this.lookForStuckThread = false;
        this.timeout = 0L;
        this.timeUnit = TimeUnit.SECONDS;
    }
    public Builder withTimeout ( final long timeout, final TimeUnit unit ) {
        this.timeout = timeout;
        this.timeUnit = unit;
        return this;
    }
    protected long getTimeout() {
        return this.timeout;
    }
    protected TimeUnit getTimeUnit() {
        return this.timeUnit;
    }
    public Builder withLookingForStuckThread ( final boolean enable ) {
        this.lookForStuckThread = enable;
        return this;
    }
    protected boolean getLookingForStuckThread() {
        return this.lookForStuckThread;
    }
    public Timeout build() {
        return new Timeout ( this );
    }
}
