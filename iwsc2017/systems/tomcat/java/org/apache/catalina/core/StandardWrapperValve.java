package org.apache.catalina.core;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.log.SystemLogHandler;
import org.apache.tomcat.util.res.StringManager;
final class StandardWrapperValve
    extends ValveBase {
    public StandardWrapperValve() {
        super ( true );
    }
    private volatile long processingTime;
    private volatile long maxTime;
    private volatile long minTime = Long.MAX_VALUE;
    private final AtomicInteger requestCount = new AtomicInteger ( 0 );
    private final AtomicInteger errorCount = new AtomicInteger ( 0 );
    private static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    @Override
    public final void invoke ( Request request, Response response )
    throws IOException, ServletException {
        boolean unavailable = false;
        Throwable throwable = null;
        long t1 = System.currentTimeMillis();
        requestCount.incrementAndGet();
        StandardWrapper wrapper = ( StandardWrapper ) getContainer();
        Servlet servlet = null;
        Context context = ( Context ) wrapper.getParent();
        if ( !context.getState().isAvailable() ) {
            response.sendError ( HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                                 sm.getString ( "standardContext.isUnavailable" ) );
            unavailable = true;
        }
        if ( !unavailable && wrapper.isUnavailable() ) {
            container.getLogger().info ( sm.getString ( "standardWrapper.isUnavailable",
                                         wrapper.getName() ) );
            long available = wrapper.getAvailable();
            if ( ( available > 0L ) && ( available < Long.MAX_VALUE ) ) {
                response.setDateHeader ( "Retry-After", available );
                response.sendError ( HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                                     sm.getString ( "standardWrapper.isUnavailable",
                                                    wrapper.getName() ) );
            } else if ( available == Long.MAX_VALUE ) {
                response.sendError ( HttpServletResponse.SC_NOT_FOUND,
                                     sm.getString ( "standardWrapper.notFound",
                                                    wrapper.getName() ) );
            }
            unavailable = true;
        }
        try {
            if ( !unavailable ) {
                servlet = wrapper.allocate();
            }
        } catch ( UnavailableException e ) {
            container.getLogger().error (
                sm.getString ( "standardWrapper.allocateException",
                               wrapper.getName() ), e );
            long available = wrapper.getAvailable();
            if ( ( available > 0L ) && ( available < Long.MAX_VALUE ) ) {
                response.setDateHeader ( "Retry-After", available );
                response.sendError ( HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                                     sm.getString ( "standardWrapper.isUnavailable",
                                                    wrapper.getName() ) );
            } else if ( available == Long.MAX_VALUE ) {
                response.sendError ( HttpServletResponse.SC_NOT_FOUND,
                                     sm.getString ( "standardWrapper.notFound",
                                                    wrapper.getName() ) );
            }
        } catch ( ServletException e ) {
            container.getLogger().error ( sm.getString ( "standardWrapper.allocateException",
                                          wrapper.getName() ), StandardWrapper.getRootCause ( e ) );
            throwable = e;
            exception ( request, response, e );
        } catch ( Throwable e ) {
            ExceptionUtils.handleThrowable ( e );
            container.getLogger().error ( sm.getString ( "standardWrapper.allocateException",
                                          wrapper.getName() ), e );
            throwable = e;
            exception ( request, response, e );
            servlet = null;
        }
        MessageBytes requestPathMB = request.getRequestPathMB();
        DispatcherType dispatcherType = DispatcherType.REQUEST;
        if ( request.getDispatcherType() == DispatcherType.ASYNC ) {
            dispatcherType = DispatcherType.ASYNC;
        }
        request.setAttribute ( Globals.DISPATCHER_TYPE_ATTR, dispatcherType );
        request.setAttribute ( Globals.DISPATCHER_REQUEST_PATH_ATTR,
                               requestPathMB );
        ApplicationFilterChain filterChain =
            ApplicationFilterFactory.createFilterChain ( request, wrapper, servlet );
        try {
            if ( ( servlet != null ) && ( filterChain != null ) ) {
                if ( context.getSwallowOutput() ) {
                    try {
                        SystemLogHandler.startCapture();
                        if ( request.isAsyncDispatching() ) {
                            request.getAsyncContextInternal().doInternalDispatch();
                        } else {
                            filterChain.doFilter ( request.getRequest(),
                                                   response.getResponse() );
                        }
                    } finally {
                        String log = SystemLogHandler.stopCapture();
                        if ( log != null && log.length() > 0 ) {
                            context.getLogger().info ( log );
                        }
                    }
                } else {
                    if ( request.isAsyncDispatching() ) {
                        request.getAsyncContextInternal().doInternalDispatch();
                    } else {
                        filterChain.doFilter
                        ( request.getRequest(), response.getResponse() );
                    }
                }
            }
        } catch ( ClientAbortException e ) {
            throwable = e;
            exception ( request, response, e );
        } catch ( IOException e ) {
            container.getLogger().error ( sm.getString (
                                              "standardWrapper.serviceException", wrapper.getName(),
                                              context.getName() ), e );
            throwable = e;
            exception ( request, response, e );
        } catch ( UnavailableException e ) {
            container.getLogger().error ( sm.getString (
                                              "standardWrapper.serviceException", wrapper.getName(),
                                              context.getName() ), e );
            wrapper.unavailable ( e );
            long available = wrapper.getAvailable();
            if ( ( available > 0L ) && ( available < Long.MAX_VALUE ) ) {
                response.setDateHeader ( "Retry-After", available );
                response.sendError ( HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                                     sm.getString ( "standardWrapper.isUnavailable",
                                                    wrapper.getName() ) );
            } else if ( available == Long.MAX_VALUE ) {
                response.sendError ( HttpServletResponse.SC_NOT_FOUND,
                                     sm.getString ( "standardWrapper.notFound",
                                                    wrapper.getName() ) );
            }
        } catch ( ServletException e ) {
            Throwable rootCause = StandardWrapper.getRootCause ( e );
            if ( ! ( rootCause instanceof ClientAbortException ) ) {
                container.getLogger().error ( sm.getString (
                                                  "standardWrapper.serviceExceptionRoot",
                                                  wrapper.getName(), context.getName(), e.getMessage() ),
                                              rootCause );
            }
            throwable = e;
            exception ( request, response, e );
        } catch ( Throwable e ) {
            ExceptionUtils.handleThrowable ( e );
            container.getLogger().error ( sm.getString (
                                              "standardWrapper.serviceException", wrapper.getName(),
                                              context.getName() ), e );
            throwable = e;
            exception ( request, response, e );
        }
        if ( filterChain != null ) {
            filterChain.release();
        }
        try {
            if ( servlet != null ) {
                wrapper.deallocate ( servlet );
            }
        } catch ( Throwable e ) {
            ExceptionUtils.handleThrowable ( e );
            container.getLogger().error ( sm.getString ( "standardWrapper.deallocateException",
                                          wrapper.getName() ), e );
            if ( throwable == null ) {
                throwable = e;
                exception ( request, response, e );
            }
        }
        try {
            if ( ( servlet != null ) &&
                    ( wrapper.getAvailable() == Long.MAX_VALUE ) ) {
                wrapper.unload();
            }
        } catch ( Throwable e ) {
            ExceptionUtils.handleThrowable ( e );
            container.getLogger().error ( sm.getString ( "standardWrapper.unloadException",
                                          wrapper.getName() ), e );
            if ( throwable == null ) {
                throwable = e;
                exception ( request, response, e );
            }
        }
        long t2 = System.currentTimeMillis();
        long time = t2 - t1;
        processingTime += time;
        if ( time > maxTime ) {
            maxTime = time;
        }
        if ( time < minTime ) {
            minTime = time;
        }
    }
    private void exception ( Request request, Response response,
                             Throwable exception ) {
        request.setAttribute ( RequestDispatcher.ERROR_EXCEPTION, exception );
        response.setStatus ( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        response.setError();
    }
    public long getProcessingTime() {
        return processingTime;
    }
    public long getMaxTime() {
        return maxTime;
    }
    public long getMinTime() {
        return minTime;
    }
    public int getRequestCount() {
        return requestCount.get();
    }
    public int getErrorCount() {
        return errorCount.get();
    }
    public void incrementErrorCount() {
        errorCount.incrementAndGet();
    }
    @Override
    protected void initInternal() throws LifecycleException {
    }
}
