package org.apache.catalina.core;
import org.apache.catalina.LifecycleException;
import org.apache.tomcat.util.buf.MessageBytes;
import javax.servlet.Servlet;
import java.io.IOException;
import org.apache.catalina.connector.ClientAbortException;
import javax.servlet.ServletResponse;
import org.apache.tomcat.util.log.SystemLogHandler;
import org.apache.catalina.Wrapper;
import javax.servlet.ServletRequest;
import javax.servlet.DispatcherType;
import org.apache.tomcat.util.ExceptionUtils;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import org.apache.tomcat.util.res.StringManager;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.catalina.valves.ValveBase;
final class StandardWrapperValve extends ValveBase {
    private volatile long processingTime;
    private volatile long maxTime;
    private volatile long minTime;
    private final AtomicInteger requestCount;
    private final AtomicInteger errorCount;
    private static final StringManager sm;
    public StandardWrapperValve() {
        super ( true );
        this.minTime = Long.MAX_VALUE;
        this.requestCount = new AtomicInteger ( 0 );
        this.errorCount = new AtomicInteger ( 0 );
    }
    @Override
    public final void invoke ( final Request request, final Response response ) throws IOException, ServletException {
        boolean unavailable = false;
        Throwable throwable = null;
        final long t1 = System.currentTimeMillis();
        this.requestCount.incrementAndGet();
        final StandardWrapper wrapper = ( StandardWrapper ) this.getContainer();
        Servlet servlet = null;
        final Context context = ( Context ) wrapper.getParent();
        if ( !context.getState().isAvailable() ) {
            response.sendError ( 503, StandardWrapperValve.sm.getString ( "standardContext.isUnavailable" ) );
            unavailable = true;
        }
        if ( !unavailable && wrapper.isUnavailable() ) {
            this.container.getLogger().info ( StandardWrapperValve.sm.getString ( "standardWrapper.isUnavailable", wrapper.getName() ) );
            final long available = wrapper.getAvailable();
            if ( available > 0L && available < Long.MAX_VALUE ) {
                response.setDateHeader ( "Retry-After", available );
                response.sendError ( 503, StandardWrapperValve.sm.getString ( "standardWrapper.isUnavailable", wrapper.getName() ) );
            } else if ( available == Long.MAX_VALUE ) {
                response.sendError ( 404, StandardWrapperValve.sm.getString ( "standardWrapper.notFound", wrapper.getName() ) );
            }
            unavailable = true;
        }
        try {
            if ( !unavailable ) {
                servlet = wrapper.allocate();
            }
        } catch ( UnavailableException e ) {
            this.container.getLogger().error ( StandardWrapperValve.sm.getString ( "standardWrapper.allocateException", wrapper.getName() ), ( Throwable ) e );
            final long available2 = wrapper.getAvailable();
            if ( available2 > 0L && available2 < Long.MAX_VALUE ) {
                response.setDateHeader ( "Retry-After", available2 );
                response.sendError ( 503, StandardWrapperValve.sm.getString ( "standardWrapper.isUnavailable", wrapper.getName() ) );
            } else if ( available2 == Long.MAX_VALUE ) {
                response.sendError ( 404, StandardWrapperValve.sm.getString ( "standardWrapper.notFound", wrapper.getName() ) );
            }
        } catch ( ServletException e2 ) {
            this.container.getLogger().error ( StandardWrapperValve.sm.getString ( "standardWrapper.allocateException", wrapper.getName() ), StandardWrapper.getRootCause ( e2 ) );
            throwable = ( Throwable ) e2;
            this.exception ( request, response, ( Throwable ) e2 );
        } catch ( Throwable e3 ) {
            ExceptionUtils.handleThrowable ( e3 );
            this.container.getLogger().error ( StandardWrapperValve.sm.getString ( "standardWrapper.allocateException", wrapper.getName() ), e3 );
            throwable = e3;
            this.exception ( request, response, e3 );
            servlet = null;
        }
        final MessageBytes requestPathMB = request.getRequestPathMB();
        DispatcherType dispatcherType = DispatcherType.REQUEST;
        if ( request.getDispatcherType() == DispatcherType.ASYNC ) {
            dispatcherType = DispatcherType.ASYNC;
        }
        request.setAttribute ( "org.apache.catalina.core.DISPATCHER_TYPE", dispatcherType );
        request.setAttribute ( "org.apache.catalina.core.DISPATCHER_REQUEST_PATH", requestPathMB );
        final ApplicationFilterChain filterChain = ApplicationFilterFactory.createFilterChain ( ( ServletRequest ) request, wrapper, servlet );
        try {
            if ( servlet != null && filterChain != null ) {
                if ( context.getSwallowOutput() ) {
                    try {
                        SystemLogHandler.startCapture();
                        if ( request.isAsyncDispatching() ) {
                            request.getAsyncContextInternal().doInternalDispatch();
                        } else {
                            filterChain.doFilter ( ( ServletRequest ) request.getRequest(), ( ServletResponse ) response.getResponse() );
                        }
                    } finally {
                        final String log = SystemLogHandler.stopCapture();
                        if ( log != null && log.length() > 0 ) {
                            context.getLogger().info ( log );
                        }
                    }
                } else if ( request.isAsyncDispatching() ) {
                    request.getAsyncContextInternal().doInternalDispatch();
                } else {
                    filterChain.doFilter ( ( ServletRequest ) request.getRequest(), ( ServletResponse ) response.getResponse() );
                }
            }
        } catch ( ClientAbortException e4 ) {
            throwable = e4;
            this.exception ( request, response, e4 );
        } catch ( IOException e5 ) {
            this.container.getLogger().error ( StandardWrapperValve.sm.getString ( "standardWrapper.serviceException", wrapper.getName(), context.getName() ), e5 );
            throwable = e5;
            this.exception ( request, response, e5 );
        } catch ( UnavailableException e6 ) {
            this.container.getLogger().error ( StandardWrapperValve.sm.getString ( "standardWrapper.serviceException", wrapper.getName(), context.getName() ), ( Throwable ) e6 );
            wrapper.unavailable ( e6 );
            final long available3 = wrapper.getAvailable();
            if ( available3 > 0L && available3 < Long.MAX_VALUE ) {
                response.setDateHeader ( "Retry-After", available3 );
                response.sendError ( 503, StandardWrapperValve.sm.getString ( "standardWrapper.isUnavailable", wrapper.getName() ) );
            } else if ( available3 == Long.MAX_VALUE ) {
                response.sendError ( 404, StandardWrapperValve.sm.getString ( "standardWrapper.notFound", wrapper.getName() ) );
            }
        } catch ( ServletException e7 ) {
            final Throwable rootCause = StandardWrapper.getRootCause ( e7 );
            if ( ! ( rootCause instanceof ClientAbortException ) ) {
                this.container.getLogger().error ( StandardWrapperValve.sm.getString ( "standardWrapper.serviceExceptionRoot", wrapper.getName(), context.getName(), e7.getMessage() ), rootCause );
            }
            throwable = ( Throwable ) e7;
            this.exception ( request, response, ( Throwable ) e7 );
        } catch ( Throwable e8 ) {
            ExceptionUtils.handleThrowable ( e8 );
            this.container.getLogger().error ( StandardWrapperValve.sm.getString ( "standardWrapper.serviceException", wrapper.getName(), context.getName() ), e8 );
            throwable = e8;
            this.exception ( request, response, e8 );
        }
        if ( filterChain != null ) {
            filterChain.release();
        }
        try {
            if ( servlet != null ) {
                wrapper.deallocate ( servlet );
            }
        } catch ( Throwable e8 ) {
            ExceptionUtils.handleThrowable ( e8 );
            this.container.getLogger().error ( StandardWrapperValve.sm.getString ( "standardWrapper.deallocateException", wrapper.getName() ), e8 );
            if ( throwable == null ) {
                throwable = e8;
                this.exception ( request, response, e8 );
            }
        }
        try {
            if ( servlet != null && wrapper.getAvailable() == Long.MAX_VALUE ) {
                wrapper.unload();
            }
        } catch ( Throwable e8 ) {
            ExceptionUtils.handleThrowable ( e8 );
            this.container.getLogger().error ( StandardWrapperValve.sm.getString ( "standardWrapper.unloadException", wrapper.getName() ), e8 );
            if ( throwable == null ) {
                throwable = e8;
                this.exception ( request, response, e8 );
            }
        }
        final long t2 = System.currentTimeMillis();
        final long time = t2 - t1;
        this.processingTime += time;
        if ( time > this.maxTime ) {
            this.maxTime = time;
        }
        if ( time < this.minTime ) {
            this.minTime = time;
        }
    }
    private void exception ( final Request request, final Response response, final Throwable exception ) {
        request.setAttribute ( "javax.servlet.error.exception", exception );
        response.setStatus ( 500 );
        response.setError();
    }
    public long getProcessingTime() {
        return this.processingTime;
    }
    public long getMaxTime() {
        return this.maxTime;
    }
    public long getMinTime() {
        return this.minTime;
    }
    public int getRequestCount() {
        return this.requestCount.get();
    }
    public int getErrorCount() {
        return this.errorCount.get();
    }
    public void incrementErrorCount() {
        this.errorCount.incrementAndGet();
    }
    @Override
    protected void initInternal() throws LifecycleException {
    }
    static {
        sm = StringManager.getManager ( "org.apache.catalina.core" );
    }
}
