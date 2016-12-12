package org.apache.catalina.core;
import org.apache.juli.logging.LogFactory;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletResponse;
import org.apache.catalina.Wrapper;
import org.apache.tomcat.util.descriptor.web.ErrorPage;
import org.apache.catalina.connector.ClientAbortException;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import java.io.IOException;
import org.apache.catalina.Context;
import org.apache.tomcat.util.ExceptionUtils;
import javax.servlet.ServletRequest;
import org.apache.catalina.Globals;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.valves.ValveBase;
final class StandardHostValve extends ValveBase {
    private static final Log log;
    private static final ClassLoader MY_CLASSLOADER;
    static final boolean STRICT_SERVLET_COMPLIANCE;
    static final boolean ACCESS_SESSION;
    private static final StringManager sm;
    public StandardHostValve() {
        super ( true );
    }
    @Override
    public final void invoke ( final Request request, final Response response ) throws IOException, ServletException {
        final Context context = request.getContext();
        if ( context == null ) {
            response.sendError ( 500, StandardHostValve.sm.getString ( "standardHost.noContext" ) );
            return;
        }
        if ( request.isAsyncSupported() ) {
            request.setAsyncSupported ( context.getPipeline().isAsyncSupported() );
        }
        final boolean asyncAtStart = request.isAsync();
        final boolean asyncDispatching = request.isAsyncDispatching();
        try {
            context.bind ( Globals.IS_SECURITY_ENABLED, StandardHostValve.MY_CLASSLOADER );
            if ( !asyncAtStart && !context.fireRequestInitEvent ( ( ServletRequest ) request ) ) {
                return;
            }
            try {
                if ( !asyncAtStart || asyncDispatching ) {
                    context.getPipeline().getFirst().invoke ( request, response );
                } else if ( !response.isErrorReportRequired() ) {
                    throw new IllegalStateException ( StandardHostValve.sm.getString ( "standardHost.asyncStateError" ) );
                }
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                this.container.getLogger().error ( "Exception Processing " + request.getRequestURI(), t );
                if ( !response.isErrorReportRequired() ) {
                    request.setAttribute ( "javax.servlet.error.exception", t );
                    this.throwable ( request, response, t );
                }
            }
            response.setSuspended ( false );
            final Throwable t = ( Throwable ) request.getAttribute ( "javax.servlet.error.exception" );
            if ( !context.getState().isAvailable() ) {
                return;
            }
            if ( response.isErrorReportRequired() ) {
                if ( t != null ) {
                    this.throwable ( request, response, t );
                } else {
                    this.status ( request, response );
                }
            }
            if ( !request.isAsync() && ( !asyncAtStart || !response.isErrorReportRequired() ) ) {
                context.fireRequestDestroyEvent ( ( ServletRequest ) request );
            }
        } finally {
            if ( StandardHostValve.ACCESS_SESSION ) {
                request.getSession ( false );
            }
            context.unbind ( Globals.IS_SECURITY_ENABLED, StandardHostValve.MY_CLASSLOADER );
        }
    }
    private void status ( final Request request, final Response response ) {
        final int statusCode = response.getStatus();
        final Context context = request.getContext();
        if ( context == null ) {
            return;
        }
        if ( !response.isError() ) {
            return;
        }
        ErrorPage errorPage = context.findErrorPage ( statusCode );
        if ( errorPage == null ) {
            errorPage = context.findErrorPage ( 0 );
        }
        if ( errorPage != null && response.isErrorReportRequired() ) {
            response.setAppCommitted ( false );
            request.setAttribute ( "javax.servlet.error.status_code", statusCode );
            String message = response.getMessage();
            if ( message == null ) {
                message = "";
            }
            request.setAttribute ( "javax.servlet.error.message", message );
            request.setAttribute ( "org.apache.catalina.core.DISPATCHER_REQUEST_PATH", errorPage.getLocation() );
            request.setAttribute ( "org.apache.catalina.core.DISPATCHER_TYPE", DispatcherType.ERROR );
            final Wrapper wrapper = request.getWrapper();
            if ( wrapper != null ) {
                request.setAttribute ( "javax.servlet.error.servlet_name", wrapper.getName() );
            }
            request.setAttribute ( "javax.servlet.error.request_uri", request.getRequestURI() );
            if ( this.custom ( request, response, errorPage ) ) {
                response.setErrorReported();
                try {
                    response.finishResponse();
                } catch ( ClientAbortException ex ) {}
                catch ( IOException e ) {
                    this.container.getLogger().warn ( "Exception Processing " + errorPage, e );
                }
            }
        }
    }
    protected void throwable ( final Request request, final Response response, final Throwable throwable ) {
        final Context context = request.getContext();
        if ( context == null ) {
            return;
        }
        Throwable realError = throwable;
        if ( realError instanceof ServletException ) {
            realError = ( ( ServletException ) realError ).getRootCause();
            if ( realError == null ) {
                realError = throwable;
            }
        }
        if ( realError instanceof ClientAbortException ) {
            if ( StandardHostValve.log.isDebugEnabled() ) {
                StandardHostValve.log.debug ( StandardHostValve.sm.getString ( "standardHost.clientAbort", realError.getCause().getMessage() ) );
            }
            return;
        }
        ErrorPage errorPage = findErrorPage ( context, throwable );
        if ( errorPage == null && realError != throwable ) {
            errorPage = findErrorPage ( context, realError );
        }
        if ( errorPage != null ) {
            if ( response.setErrorReported() ) {
                response.setAppCommitted ( false );
                request.setAttribute ( "org.apache.catalina.core.DISPATCHER_REQUEST_PATH", errorPage.getLocation() );
                request.setAttribute ( "org.apache.catalina.core.DISPATCHER_TYPE", DispatcherType.ERROR );
                request.setAttribute ( "javax.servlet.error.status_code", 500 );
                request.setAttribute ( "javax.servlet.error.message", throwable.getMessage() );
                request.setAttribute ( "javax.servlet.error.exception", realError );
                final Wrapper wrapper = request.getWrapper();
                if ( wrapper != null ) {
                    request.setAttribute ( "javax.servlet.error.servlet_name", wrapper.getName() );
                }
                request.setAttribute ( "javax.servlet.error.request_uri", request.getRequestURI() );
                request.setAttribute ( "javax.servlet.error.exception_type", realError.getClass() );
                if ( this.custom ( request, response, errorPage ) ) {
                    try {
                        response.finishResponse();
                    } catch ( IOException e ) {
                        this.container.getLogger().warn ( "Exception Processing " + errorPage, e );
                    }
                }
            }
        } else {
            response.setStatus ( 500 );
            response.setError();
            this.status ( request, response );
        }
    }
    private boolean custom ( final Request request, final Response response, final ErrorPage errorPage ) {
        if ( this.container.getLogger().isDebugEnabled() ) {
            this.container.getLogger().debug ( "Processing " + errorPage );
        }
        try {
            final ServletContext servletContext = request.getContext().getServletContext();
            final RequestDispatcher rd = servletContext.getRequestDispatcher ( errorPage.getLocation() );
            if ( rd == null ) {
                this.container.getLogger().error ( StandardHostValve.sm.getString ( "standardHostValue.customStatusFailed", errorPage.getLocation() ) );
                return false;
            }
            if ( response.isCommitted() ) {
                rd.include ( ( ServletRequest ) request.getRequest(), ( ServletResponse ) response.getResponse() );
            } else {
                response.resetBuffer ( true );
                response.setContentLength ( -1 );
                rd.forward ( ( ServletRequest ) request.getRequest(), ( ServletResponse ) response.getResponse() );
                response.setSuspended ( false );
            }
            return true;
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            this.container.getLogger().error ( "Exception Processing " + errorPage, t );
            return false;
        }
    }
    private static ErrorPage findErrorPage ( final Context context, final Throwable exception ) {
        if ( exception == null ) {
            return null;
        }
        Class<?> clazz = exception.getClass();
        String name = clazz.getName();
        while ( !Object.class.equals ( clazz ) ) {
            final ErrorPage errorPage = context.findErrorPage ( name );
            if ( errorPage != null ) {
                return errorPage;
            }
            clazz = clazz.getSuperclass();
            if ( clazz == null ) {
                break;
            }
            name = clazz.getName();
        }
        return null;
    }
    static {
        log = LogFactory.getLog ( StandardHostValve.class );
        MY_CLASSLOADER = StandardHostValve.class.getClassLoader();
        STRICT_SERVLET_COMPLIANCE = Globals.STRICT_SERVLET_COMPLIANCE;
        final String accessSession = System.getProperty ( "org.apache.catalina.core.StandardHostValve.ACCESS_SESSION" );
        if ( accessSession == null ) {
            ACCESS_SESSION = StandardHostValve.STRICT_SERVLET_COMPLIANCE;
        } else {
            ACCESS_SESSION = Boolean.parseBoolean ( accessSession );
        }
        sm = StringManager.getManager ( "org.apache.catalina.core" );
    }
}
