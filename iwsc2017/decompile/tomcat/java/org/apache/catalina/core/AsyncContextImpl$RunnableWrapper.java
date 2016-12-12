package org.apache.catalina.core;
import org.apache.coyote.ActionCode;
import org.apache.catalina.Globals;
import org.apache.coyote.Request;
import org.apache.catalina.Context;
private static class RunnableWrapper implements Runnable {
    private final Runnable wrapped;
    private final Context context;
    private final Request coyoteRequest;
    public RunnableWrapper ( final Runnable wrapped, final Context ctxt, final Request coyoteRequest ) {
        this.wrapped = wrapped;
        this.context = ctxt;
        this.coyoteRequest = coyoteRequest;
    }
    @Override
    public void run() {
        final ClassLoader oldCL = this.context.bind ( Globals.IS_SECURITY_ENABLED, null );
        try {
            this.wrapped.run();
        } finally {
            this.context.unbind ( Globals.IS_SECURITY_ENABLED, oldCL );
        }
        this.coyoteRequest.action ( ActionCode.DISPATCH_EXECUTE, null );
    }
}
