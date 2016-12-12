package javax.servlet;
public class AsyncEvent {
    private final AsyncContext context;
    private final ServletRequest request;
    private final ServletResponse response;
    private final Throwable throwable;
    public AsyncEvent ( final AsyncContext context ) {
        this.context = context;
        this.request = null;
        this.response = null;
        this.throwable = null;
    }
    public AsyncEvent ( final AsyncContext context, final ServletRequest request, final ServletResponse response ) {
        this.context = context;
        this.request = request;
        this.response = response;
        this.throwable = null;
    }
    public AsyncEvent ( final AsyncContext context, final Throwable throwable ) {
        this.context = context;
        this.throwable = throwable;
        this.request = null;
        this.response = null;
    }
    public AsyncEvent ( final AsyncContext context, final ServletRequest request, final ServletResponse response, final Throwable throwable ) {
        this.context = context;
        this.request = request;
        this.response = response;
        this.throwable = throwable;
    }
    public AsyncContext getAsyncContext() {
        return this.context;
    }
    public ServletRequest getSuppliedRequest() {
        return this.request;
    }
    public ServletResponse getSuppliedResponse() {
        return this.response;
    }
    public Throwable getThrowable() {
        return this.throwable;
    }
}
