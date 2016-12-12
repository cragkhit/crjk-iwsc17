package org.junit.internal.runners.statements;
import org.junit.runners.model.Statement;
import java.util.concurrent.TimeUnit;
public static class Builder {
    private boolean lookForStuckThread;
    private long timeout;
    private TimeUnit unit;
    private Builder() {
        this.lookForStuckThread = false;
        this.timeout = 0L;
        this.unit = TimeUnit.SECONDS;
    }
    public Builder withTimeout ( final long timeout, final TimeUnit unit ) {
        if ( timeout < 0L ) {
            throw new IllegalArgumentException ( "timeout must be non-negative" );
        }
        if ( unit == null ) {
            throw new NullPointerException ( "TimeUnit cannot be null" );
        }
        this.timeout = timeout;
        this.unit = unit;
        return this;
    }
    public Builder withLookingForStuckThread ( final boolean enable ) {
        this.lookForStuckThread = enable;
        return this;
    }
    public FailOnTimeout build ( final Statement statement ) {
        if ( statement == null ) {
            throw new NullPointerException ( "statement cannot be null" );
        }
        return new FailOnTimeout ( this, statement, null );
    }
}
