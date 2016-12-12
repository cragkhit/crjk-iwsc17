package org.apache.coyote;
private enum AsyncState {
    DISPATCHED ( false, false, false, false ),
    STARTING ( true, true, false, false ),
    STARTED ( true, true, false, false ),
    MUST_COMPLETE ( true, true, true, false ),
    COMPLETE_PENDING ( true, true, false, false ),
    COMPLETING ( true, false, true, false ),
    TIMING_OUT ( true, true, false, false ),
    MUST_DISPATCH ( true, true, false, true ),
    DISPATCH_PENDING ( true, true, false, false ),
    DISPATCHING ( true, false, false, true ),
    READ_WRITE_OP ( true, true, false, false ),
    ERROR ( true, true, false, false );
    private final boolean isAsync;
    private final boolean isStarted;
    private final boolean isCompleting;
    private final boolean isDispatching;
    private AsyncState ( final boolean isAsync, final boolean isStarted, final boolean isCompleting, final boolean isDispatching ) {
        this.isAsync = isAsync;
        this.isStarted = isStarted;
        this.isCompleting = isCompleting;
        this.isDispatching = isDispatching;
    }
    public boolean isAsync() {
        return this.isAsync;
    }
    public boolean isStarted() {
        return this.isStarted;
    }
    public boolean isDispatching() {
        return this.isDispatching;
    }
    public boolean isCompleting() {
        return this.isCompleting;
    }
}
