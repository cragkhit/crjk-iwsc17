package org.apache.catalina.core;
import java.io.IOException;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.descriptor.web.ErrorPage;
import org.apache.tomcat.util.res.StringManager;
final class StandardHostValve extends ValveBase {
    private static final Log log = LogFactory.getLog ( StandardHostValve.class );
    private static final ClassLoader MY_CLASSLOADER =
        StandardHostValve.class.getClassLoader();
    static final boolean STRICT_SERVLET_COMPLIANCE;
    static final boolean ACCESS_SESSION;
    static {
        STRICT_SERVLET_COMPLIANCE = Globals.STRICT_SERVLET_COMPLIANCE;
        String accessSession = System.getProperty (
                                   "org.apache.catalina.core.StandardHostValve.ACCESS_SESSION" );
        if ( accessSession == null ) {
            ACCESS_SESSION = STRICT_SERVLET_COMPLIANCE;
        } else {
            ACCESS_SESSION = Boolean.parseBoolean ( accessSession );
        }
    }
    public StandardHostValve() {
        super ( true );
    }
    private static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    @Override
    public final void invoke ( Request request, Response response )
    throws IOException, ServletException {
        Context context = request.getContext();
        if ( context == null ) {
            response.sendError ( HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                 sm.getString ( "standardHost.noContext" ) );
            return;
        }
        if ( request.isAsyncSupported() ) {
            request.setAsyncSupported ( context.getPipeline().isAsyncSupported() );
        }
        boolean asyncAtStart = request.isAsync();
        boolean asyncDispatching = request.isAsyncDispatching();
        try {
            context.bind ( Globals.IS_SECURITY_ENABLED, MY_CLASSLOADER );
            if ( !asyncAtStart && !context.fireRequestInitEvent ( request ) ) {
                return;
            }
            try {
                if ( !asyncAtStart || asyncDispatching ) {
                    context.getPipeline().getFirst().invoke ( request, response );
                } else {
                    if ( !response.isErrorReportRequired() ) {
                        throw new IllegalStateException ( sm.getString ( "standardHost.asyncStateError" ) );
                    }
                }
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                container.getLogger().error ( "Exception Processing " + request.getRequestURI(), t );
                if ( !response.isErrorReportRequired() ) {
                    request.setAttribute ( RequestDispatcher.ERROR_EXCEPTION, t );
                    throwable ( request, response, t );
                }
            }
            response.setSuspended ( false );
            Throwable t = ( Throwable ) request.getAttribute ( RequestDispatcher.ERROR_EXCEPTION );
            if ( !context.getState().isAvailable() ) {
                return;
            }
            if ( response.isErrorReportRequired() ) {
                if ( t != null ) {
                    throwable ( request, response, t );
                } else {
                    status ( request, response );
                }
            }
            if ( !request.isAsync() && ( !asyncAtStart || !response.isErrorReportRequired() ) ) {
                context.fireRequestDestroyEvent ( request );
            }
        } finally {
            if ( ACCESS_SESSION ) {
                request.getSession ( false );
            }
            context.unbind ( Globals.IS_SECURITY_ENABLED, MY_CLASSLOADER );
        }
    }
    private void status ( Request request, Response response ) {
        int statusCode = response.getStatus();
        Context context = request.getContext();
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
            request.setAttribute ( RequestDispatcher.ERROR_STATUS_CODE,
                                   Integer.valueOf ( statusCode ) );
            String message = response.getMessage();
            if ( message == null ) {
                message = "";
            }
            request.setAttribute ( RequestDispatcher.ERROR_MESSAGE, message );
            request.setAttribute ( Globals.DISPATCHER_REQUEST_PATH_ATTR,
                                   errorPage.getLocation() );
            request.setAttribute ( Globals.DISPATCHER_TYPE_ATTR,
                                   DispatcherType.ERROR );
            Wrapper wrapper = request.getWrapper();
            if ( wrapper != null ) {
                request.setAttribute ( RequestDispatcher.ERROR_SERVLET_NAME,
                                       wrapper.getName() );
            }
            request.setAttribute ( RequestDispatcher.ERROR_REQUEST_URI,
                                   request.getRequestURI() );
            if ( custom ( request, response, errorPage ) ) {
                response.setErrorReported();
                try {
                    response.finishResponse();
                } catch ( ClientAbortException e ) {
                } catch ( IOException e ) {
                    container.getLogger().warn ( "Exception Processing " + errorPage, e );
                }
            }
        }
    }
    protected void throwable ( Request request, Response response,
                               Throwable throwable ) {
        Context context = request.getContext();
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
            if ( log.isDebugEnabled() ) {
                log.debug
                ( sm.getString ( "standardHost.clientAbort",
                                 realError.getCause().getMessage() ) );
            }
            return;
        }
        ErrorPage errorPage = findErrorPage ( context, throwable );
        if ( ( errorPage == null ) && ( realError != throwable ) ) {
            errorPage = findErrorPage ( context, realError );
        }
        if ( errorPage != null ) {
            if ( response.setErrorReported() ) {
                response.setAppCommitted ( false );
                request.setAttribute ( Globals.DISPATCHER_REQUEST_PATH_ATTR,
                                       errorPage.getLocation() );
                request.setAttribute ( Globals.DISPATCHER_TYPE_ATTR,
                                       DispatcherType.ERROR );
                request.setAttribute ( RequestDispatcher.ERROR_STATUS_CODE,
                                       Integer.valueOf ( HttpServletResponse.SC_INTERNAL_SERVER_ERROR ) );
                request.setAttribute ( RequestDispatcher.ERROR_MESSAGE,
                                       throwable.getMessage() );
                request.setAttribute ( RequestDispatcher.ERROR_EXCEPTION,
                                       realError );
                Wrapper wrapper = request.getWrapper();
                if ( wrapper != null ) {
                    request.setAttribute ( RequestDispatcher.ERROR_SERVLET_NAME,
                                           wrapper.getName() );
                }
                request.setAttribute ( RequestDispatcher.ERROR_REQUEST_URI,
                                       request.getRequestURI() );
                request.setAttribute ( RequestDispatcher.ERROR_EXCEPTION_TYPE,
                                       realError.getClass() );
                if ( custom ( request, response, errorPage ) ) {
                    try {
                        response.finishResponse();
                    } catch ( IOException e ) {
                        container.getLogger().warn ( "Exception Processing " + errorPage, e );
                    }
                }
            }
        } else {
            response.setStatus ( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            response.setError();
            status ( request, response );
        }
    }
    private boolean custom ( Request request, Response response,
                             ErrorPage errorPage ) {
        if ( container.getLogger().isDebugEnabled() ) {
            container.getLogger().debug ( "Processing " + errorPage );
        }
        try {
            ServletContext servletContext =
                request.getContext().getServletContext();
            RequestDispatcher rd =
                servletContext.getRequestDispatcher ( errorPage.getLocation() );
            if ( rd == null ) {
                container.getLogger().error (
                    sm.getString ( "standardHostValue.customStatusFailed", errorPage.getLocation() ) );
                return false;
            }
            if ( response.isCommitted() ) {
                rd.include ( request.getRequest(), response.getResponse() );
            } else {
                response.resetBuffer ( true );
                response.setContentLength ( -1 );
                rd.forward ( request.getRequest(), response.getResponse() );
                response.setSuspended ( false );
            }
            return true;
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            container.getLogger().error ( "Exception Processing " + errorPage, t );
            return false;
        }
    }
    private static ErrorPage findErrorPage
    ( Context context, Throwable exception ) {
        if ( exception == null ) {
            return ( null );
        }
        Class<?> clazz = exception.getClass();
        String name = clazz.getName();
        while ( !Object.class.equals ( clazz ) ) {
            ErrorPage errorPage = context.findErrorPage ( name );
            if ( errorPage != null ) {
                return ( errorPage );
            }
            clazz = clazz.getSuperclass();
            if ( clazz == null ) {
                break;
            }
            name = clazz.getName();
        }
        return ( null );
    }
}
