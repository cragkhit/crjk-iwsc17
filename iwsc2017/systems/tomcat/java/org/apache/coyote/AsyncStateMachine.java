package org.apache.coyote;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.tomcat.util.net.AbstractEndpoint.Handler.SocketState;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.security.PrivilegedGetTccl;
import org.apache.tomcat.util.security.PrivilegedSetTccl;
public class AsyncStateMachine {
    private static final StringManager sm = StringManager.getManager ( AsyncStateMachine.class );
    private static enum AsyncState {
        DISPATCHED ( false, false, false, false ),
        STARTING ( true,  true,  false, false ),
        STARTED ( true,  true,  false, false ),
        MUST_COMPLETE ( true,  true,  true,  false ),
        COMPLETE_PENDING ( true,  true,  false, false ),
        COMPLETING ( true,  false, true,  false ),
        TIMING_OUT ( true,  true,  false, false ),
        MUST_DISPATCH ( true,  true,  false, true ),
        DISPATCH_PENDING ( true,  true,  false, false ),
        DISPATCHING ( true,  false, false, true ),
        READ_WRITE_OP ( true,  true,  false, false ),
        ERROR ( true,  true,  false, false );
        private final boolean isAsync;
        private final boolean isStarted;
        private final boolean isCompleting;
        private final boolean isDispatching;
        private AsyncState ( boolean isAsync, boolean isStarted, boolean isCompleting,
        boolean isDispatching ) {
            this.isAsync = isAsync;
            this.isStarted = isStarted;
            this.isCompleting = isCompleting;
            this.isDispatching = isDispatching;
        }
        public boolean isAsync() {
            return isAsync;
        }
        public boolean isStarted() {
            return isStarted;
        }
        public boolean isDispatching() {
            return isDispatching;
        }
        public boolean isCompleting() {
            return isCompleting;
        }
    }
    private volatile AsyncState state = AsyncState.DISPATCHED;
    private volatile long lastAsyncStart = 0;
    private AsyncContextCallback asyncCtxt = null;
    private final AbstractProcessor processor;
    public AsyncStateMachine ( AbstractProcessor processor ) {
        this.processor = processor;
    }
    public boolean isAsync() {
        return state.isAsync();
    }
    public boolean isAsyncDispatching() {
        return state.isDispatching();
    }
    public boolean isAsyncStarted() {
        return state.isStarted();
    }
    public boolean isAsyncTimingOut() {
        return state == AsyncState.TIMING_OUT;
    }
    public boolean isAsyncError() {
        return state == AsyncState.ERROR;
    }
    public boolean isCompleting() {
        return state.isCompleting();
    }
    public long getLastAsyncStart() {
        return lastAsyncStart;
    }
    public synchronized void asyncStart ( AsyncContextCallback asyncCtxt ) {
        if ( state == AsyncState.DISPATCHED ) {
            state = AsyncState.STARTING;
            this.asyncCtxt = asyncCtxt;
            lastAsyncStart = System.currentTimeMillis();
        } else {
            throw new IllegalStateException (
                sm.getString ( "asyncStateMachine.invalidAsyncState",
                               "asyncStart()", state ) );
        }
    }
    public synchronized void asyncOperation() {
        if ( state == AsyncState.STARTED ) {
            state = AsyncState.READ_WRITE_OP;
        } else {
            throw new IllegalStateException (
                sm.getString ( "asyncStateMachine.invalidAsyncState",
                               "asyncOperation()", state ) );
        }
    }
    public synchronized SocketState asyncPostProcess() {
        if ( state == AsyncState.COMPLETE_PENDING ) {
            doComplete();
            return SocketState.ASYNC_END;
        } else if ( state == AsyncState.DISPATCH_PENDING ) {
            doDispatch();
            return SocketState.ASYNC_END;
        } else  if ( state == AsyncState.STARTING || state == AsyncState.READ_WRITE_OP ) {
            state = AsyncState.STARTED;
            return SocketState.LONG;
        } else if ( state == AsyncState.MUST_COMPLETE || state == AsyncState.COMPLETING ) {
            asyncCtxt.fireOnComplete();
            state = AsyncState.DISPATCHED;
            return SocketState.ASYNC_END;
        } else if ( state == AsyncState.MUST_DISPATCH ) {
            state = AsyncState.DISPATCHING;
            return SocketState.ASYNC_END;
        } else if ( state == AsyncState.DISPATCHING ) {
            state = AsyncState.DISPATCHED;
            return SocketState.ASYNC_END;
        } else if ( state == AsyncState.STARTED ) {
            return SocketState.LONG;
        } else {
            throw new IllegalStateException (
                sm.getString ( "asyncStateMachine.invalidAsyncState",
                               "asyncPostProcess()", state ) );
        }
    }
    public synchronized boolean asyncComplete() {
        if ( !ContainerThreadMarker.isContainerThread() && state == AsyncState.STARTING ) {
            state = AsyncState.COMPLETE_PENDING;
            return false;
        } else {
            return doComplete();
        }
    }
    private synchronized boolean doComplete() {
        clearNonBlockingListeners();
        boolean doComplete = false;
        if ( state == AsyncState.STARTING || state == AsyncState.TIMING_OUT ||
                state == AsyncState.ERROR || state == AsyncState.READ_WRITE_OP ) {
            state = AsyncState.MUST_COMPLETE;
        } else if ( state == AsyncState.STARTED || state == AsyncState.COMPLETE_PENDING ) {
            state = AsyncState.COMPLETING;
            doComplete = true;
        } else {
            throw new IllegalStateException (
                sm.getString ( "asyncStateMachine.invalidAsyncState",
                               "asyncComplete()", state ) );
        }
        return doComplete;
    }
    public synchronized boolean asyncTimeout() {
        if ( state == AsyncState.STARTED ) {
            state = AsyncState.TIMING_OUT;
            return true;
        } else if ( state == AsyncState.COMPLETING ||
                    state == AsyncState.DISPATCHING ||
                    state == AsyncState.DISPATCHED ) {
            return false;
        } else {
            throw new IllegalStateException (
                sm.getString ( "asyncStateMachine.invalidAsyncState",
                               "asyncTimeout()", state ) );
        }
    }
    public synchronized boolean asyncDispatch() {
        if ( !ContainerThreadMarker.isContainerThread() && state == AsyncState.STARTING ) {
            state = AsyncState.DISPATCH_PENDING;
            return false;
        } else {
            return doDispatch();
        }
    }
    private synchronized boolean doDispatch() {
        boolean doDispatch = false;
        if ( state == AsyncState.STARTING ||
                state == AsyncState.TIMING_OUT ||
                state == AsyncState.ERROR ) {
            state = AsyncState.MUST_DISPATCH;
        } else if ( state == AsyncState.STARTED || state == AsyncState.DISPATCH_PENDING ) {
            state = AsyncState.DISPATCHING;
            doDispatch = true;
        } else if ( state == AsyncState.READ_WRITE_OP ) {
            state = AsyncState.DISPATCHING;
            if ( !ContainerThreadMarker.isContainerThread() ) {
                doDispatch = true;
            }
        } else {
            throw new IllegalStateException (
                sm.getString ( "asyncStateMachine.invalidAsyncState",
                               "asyncDispatch()", state ) );
        }
        return doDispatch;
    }
    public synchronized void asyncDispatched() {
        if ( state == AsyncState.DISPATCHING ||
                state == AsyncState.MUST_DISPATCH ) {
            state = AsyncState.DISPATCHED;
        } else {
            throw new IllegalStateException (
                sm.getString ( "asyncStateMachine.invalidAsyncState",
                               "asyncDispatched()", state ) );
        }
    }
    public synchronized void asyncError() {
        if ( state == AsyncState.STARTING ||
                state == AsyncState.STARTED ||
                state == AsyncState.DISPATCHED ||
                state == AsyncState.TIMING_OUT ||
                state == AsyncState.MUST_COMPLETE ||
                state == AsyncState.READ_WRITE_OP ) {
            clearNonBlockingListeners();
            state = AsyncState.ERROR;
        } else {
            throw new IllegalStateException (
                sm.getString ( "asyncStateMachine.invalidAsyncState",
                               "asyncError()", state ) );
        }
    }
    public synchronized void asyncRun ( Runnable runnable ) {
        if ( state == AsyncState.STARTING || state ==  AsyncState.STARTED ||
                state == AsyncState.READ_WRITE_OP ) {
            ClassLoader oldCL;
            if ( Constants.IS_SECURITY_ENABLED ) {
                PrivilegedAction<ClassLoader> pa = new PrivilegedGetTccl();
                oldCL = AccessController.doPrivileged ( pa );
            } else {
                oldCL = Thread.currentThread().getContextClassLoader();
            }
            try {
                if ( Constants.IS_SECURITY_ENABLED ) {
                    PrivilegedAction<Void> pa = new PrivilegedSetTccl (
                        this.getClass().getClassLoader() );
                    AccessController.doPrivileged ( pa );
                } else {
                    Thread.currentThread().setContextClassLoader (
                        this.getClass().getClassLoader() );
                }
                processor.getExecutor().execute ( runnable );
            } finally {
                if ( Constants.IS_SECURITY_ENABLED ) {
                    PrivilegedAction<Void> pa = new PrivilegedSetTccl (
                        oldCL );
                    AccessController.doPrivileged ( pa );
                } else {
                    Thread.currentThread().setContextClassLoader ( oldCL );
                }
            }
        } else {
            throw new IllegalStateException (
                sm.getString ( "asyncStateMachine.invalidAsyncState",
                               "asyncRun()", state ) );
        }
    }
    public synchronized void recycle() {
        if ( lastAsyncStart == 0 ) {
            return;
        }
        notifyAll();
        asyncCtxt = null;
        state = AsyncState.DISPATCHED;
        lastAsyncStart = 0;
    }
    private void clearNonBlockingListeners() {
        processor.getRequest().listener = null;
        processor.getRequest().getResponse().listener = null;
    }
}
