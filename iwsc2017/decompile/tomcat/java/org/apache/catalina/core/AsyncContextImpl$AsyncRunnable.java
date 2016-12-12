package org.apache.catalina.core;
import org.apache.coyote.ActionCode;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import org.apache.catalina.connector.Request;
import org.apache.catalina.AsyncDispatcher;
private static class AsyncRunnable implements Runnable {
    private final AsyncDispatcher applicationDispatcher;
    private final Request request;
    private final ServletRequest servletRequest;
    private final ServletResponse servletResponse;
    public AsyncRunnable ( final Request request, final AsyncDispatcher applicationDispatcher, final ServletRequest servletRequest, final ServletResponse servletResponse ) {
        this.request = request;
        this.applicationDispatcher = applicationDispatcher;
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
    }
    @Override
    public void run() {
        this.request.getCoyoteRequest().action ( ActionCode.ASYNC_DISPATCHED, null );
        try {
            this.applicationDispatcher.dispatch ( this.servletRequest, this.servletResponse );
        } catch ( Exception x ) {
            throw new RuntimeException ( x );
        }
    }
}
