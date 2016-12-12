package org.apache.catalina.core;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Mapping;
import org.apache.catalina.AsyncDispatcher;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.ResponseFacade;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.res.StringManager;
final class ApplicationDispatcher implements AsyncDispatcher, RequestDispatcher {
    static final boolean STRICT_SERVLET_COMPLIANCE;
    static final boolean WRAP_SAME_OBJECT;
    static {
        STRICT_SERVLET_COMPLIANCE = Globals.STRICT_SERVLET_COMPLIANCE;
        String wrapSameObject = System.getProperty (
                                    "org.apache.catalina.core.ApplicationDispatcher.WRAP_SAME_OBJECT" );
        if ( wrapSameObject == null ) {
            WRAP_SAME_OBJECT = STRICT_SERVLET_COMPLIANCE;
        } else {
            WRAP_SAME_OBJECT = Boolean.parseBoolean ( wrapSameObject );
        }
    }
    protected class PrivilegedForward
        implements PrivilegedExceptionAction<Void> {
        private final ServletRequest request;
        private final ServletResponse response;
        PrivilegedForward ( ServletRequest request, ServletResponse response ) {
            this.request = request;
            this.response = response;
        }
        @Override
        public Void run() throws java.lang.Exception {
            doForward ( request, response );
            return null;
        }
    }
    protected class PrivilegedInclude implements
        PrivilegedExceptionAction<Void> {
        private final ServletRequest request;
        private final ServletResponse response;
        PrivilegedInclude ( ServletRequest request, ServletResponse response ) {
            this.request = request;
            this.response = response;
        }
        @Override
        public Void run() throws ServletException, IOException {
            doInclude ( request, response );
            return null;
        }
    }
    protected class PrivilegedDispatch implements
        PrivilegedExceptionAction<Void> {
        private final ServletRequest request;
        private final ServletResponse response;
        PrivilegedDispatch ( ServletRequest request, ServletResponse response ) {
            this.request = request;
            this.response = response;
        }
        @Override
        public Void run() throws ServletException, IOException {
            doDispatch ( request, response );
            return null;
        }
    }
    private static class State {
        State ( ServletRequest request, ServletResponse response,
                boolean including ) {
            this.outerRequest = request;
            this.outerResponse = response;
            this.including = including;
        }
        ServletRequest outerRequest = null;
        ServletResponse outerResponse = null;
        ServletRequest wrapRequest = null;
        ServletResponse wrapResponse = null;
        boolean including = false;
        HttpServletRequest hrequest = null;
        HttpServletResponse hresponse = null;
    }
    public ApplicationDispatcher
    ( Wrapper wrapper, String requestURI, String servletPath,
      String pathInfo, String queryString, Mapping mapping, String name ) {
        super();
        this.wrapper = wrapper;
        this.context = ( Context ) wrapper.getParent();
        this.requestURI = requestURI;
        this.servletPath = servletPath;
        this.pathInfo = pathInfo;
        this.queryString = queryString;
        this.mapping = mapping;
        this.name = name;
    }
    private final Context context;
    private final String name;
    private final String pathInfo;
    private final String queryString;
    private final String requestURI;
    private final String servletPath;
    private final Mapping mapping;
    private static final StringManager sm = StringManager.getManager ( Constants.Package );
    private final Wrapper wrapper;
    @Override
    public void forward ( ServletRequest request, ServletResponse response )
    throws ServletException, IOException {
        if ( Globals.IS_SECURITY_ENABLED ) {
            try {
                PrivilegedForward dp = new PrivilegedForward ( request, response );
                AccessController.doPrivileged ( dp );
            } catch ( PrivilegedActionException pe ) {
                Exception e = pe.getException();
                if ( e instanceof ServletException ) {
                    throw ( ServletException ) e;
                }
                throw ( IOException ) e;
            }
        } else {
            doForward ( request, response );
        }
    }
    private void doForward ( ServletRequest request, ServletResponse response )
    throws ServletException, IOException {
        if ( response.isCommitted() ) {
            throw new IllegalStateException
            ( sm.getString ( "applicationDispatcher.forward.ise" ) );
        }
        try {
            response.resetBuffer();
        } catch ( IllegalStateException e ) {
            throw e;
        }
        State state = new State ( request, response, false );
        if ( WRAP_SAME_OBJECT ) {
            checkSameObjects ( request, response );
        }
        wrapResponse ( state );
        if ( ( servletPath == null ) && ( pathInfo == null ) ) {
            ApplicationHttpRequest wrequest =
                ( ApplicationHttpRequest ) wrapRequest ( state );
            HttpServletRequest hrequest = state.hrequest;
            wrequest.setRequestURI ( hrequest.getRequestURI() );
            wrequest.setContextPath ( hrequest.getContextPath() );
            wrequest.setServletPath ( hrequest.getServletPath() );
            wrequest.setPathInfo ( hrequest.getPathInfo() );
            wrequest.setQueryString ( hrequest.getQueryString() );
            processRequest ( request, response, state );
        } else {
            ApplicationHttpRequest wrequest =
                ( ApplicationHttpRequest ) wrapRequest ( state );
            String contextPath = context.getPath();
            HttpServletRequest hrequest = state.hrequest;
            if ( hrequest.getAttribute ( RequestDispatcher.FORWARD_REQUEST_URI ) == null ) {
                wrequest.setAttribute ( RequestDispatcher.FORWARD_REQUEST_URI,
                                        hrequest.getRequestURI() );
                wrequest.setAttribute ( RequestDispatcher.FORWARD_CONTEXT_PATH,
                                        hrequest.getContextPath() );
                wrequest.setAttribute ( RequestDispatcher.FORWARD_SERVLET_PATH,
                                        hrequest.getServletPath() );
                wrequest.setAttribute ( RequestDispatcher.FORWARD_PATH_INFO,
                                        hrequest.getPathInfo() );
                wrequest.setAttribute ( RequestDispatcher.FORWARD_QUERY_STRING,
                                        hrequest.getQueryString() );
                wrequest.setAttribute ( RequestDispatcher.FORWARD_MAPPING, hrequest.getMapping() );
            }
            wrequest.setContextPath ( contextPath );
            wrequest.setRequestURI ( requestURI );
            wrequest.setServletPath ( servletPath );
            wrequest.setPathInfo ( pathInfo );
            if ( queryString != null ) {
                wrequest.setQueryString ( queryString );
                wrequest.setQueryParams ( queryString );
            }
            wrequest.setMapping ( mapping );
            processRequest ( request, response, state );
        }
        if ( request.isAsyncStarted() ) {
            return;
        }
        if ( wrapper.getLogger().isDebugEnabled() ) {
            wrapper.getLogger().debug ( " Disabling the response for futher output" );
        }
        if ( response instanceof ResponseFacade ) {
            ( ( ResponseFacade ) response ).finish();
        } else {
            if ( wrapper.getLogger().isDebugEnabled() ) {
                wrapper.getLogger().debug ( " The Response is vehiculed using a wrapper: "
                                            + response.getClass().getName() );
            }
            try {
                PrintWriter writer = response.getWriter();
                writer.close();
            } catch ( IllegalStateException e ) {
                try {
                    ServletOutputStream stream = response.getOutputStream();
                    stream.close();
                } catch ( IllegalStateException f ) {
                } catch ( IOException f ) {
                }
            } catch ( IOException e ) {
            }
        }
    }
    private void processRequest ( ServletRequest request,
                                  ServletResponse response,
                                  State state )
    throws IOException, ServletException {
        DispatcherType disInt = ( DispatcherType ) request.getAttribute ( Globals.DISPATCHER_TYPE_ATTR );
        if ( disInt != null ) {
            boolean doInvoke = true;
            if ( context.getFireRequestListenersOnForwards() &&
                    !context.fireRequestInitEvent ( request ) ) {
                doInvoke = false;
            }
            if ( doInvoke ) {
                if ( disInt != DispatcherType.ERROR ) {
                    state.outerRequest.setAttribute (
                        Globals.DISPATCHER_REQUEST_PATH_ATTR,
                        getCombinedPath() );
                    state.outerRequest.setAttribute (
                        Globals.DISPATCHER_TYPE_ATTR,
                        DispatcherType.FORWARD );
                    invoke ( state.outerRequest, response, state );
                } else {
                    invoke ( state.outerRequest, response, state );
                }
                if ( context.getFireRequestListenersOnForwards() ) {
                    context.fireRequestDestroyEvent ( request );
                }
            }
        }
    }
    private String getCombinedPath() {
        if ( servletPath == null ) {
            return null;
        }
        if ( pathInfo == null ) {
            return servletPath;
        }
        return servletPath + pathInfo;
    }
    @Override
    public void include ( ServletRequest request, ServletResponse response )
    throws ServletException, IOException {
        if ( Globals.IS_SECURITY_ENABLED ) {
            try {
                PrivilegedInclude dp = new PrivilegedInclude ( request, response );
                AccessController.doPrivileged ( dp );
            } catch ( PrivilegedActionException pe ) {
                Exception e = pe.getException();
                if ( e instanceof ServletException ) {
                    throw ( ServletException ) e;
                }
                throw ( IOException ) e;
            }
        } else {
            doInclude ( request, response );
        }
    }
    private void doInclude ( ServletRequest request, ServletResponse response )
    throws ServletException, IOException {
        State state = new State ( request, response, true );
        if ( WRAP_SAME_OBJECT ) {
            checkSameObjects ( request, response );
        }
        wrapResponse ( state );
        if ( name != null ) {
            ApplicationHttpRequest wrequest =
                ( ApplicationHttpRequest ) wrapRequest ( state );
            wrequest.setAttribute ( Globals.NAMED_DISPATCHER_ATTR, name );
            if ( servletPath != null ) {
                wrequest.setServletPath ( servletPath );
            }
            wrequest.setAttribute ( Globals.DISPATCHER_TYPE_ATTR,
                                    DispatcherType.INCLUDE );
            wrequest.setAttribute ( Globals.DISPATCHER_REQUEST_PATH_ATTR,
                                    getCombinedPath() );
            invoke ( state.outerRequest, state.outerResponse, state );
        } else {
            ApplicationHttpRequest wrequest =
                ( ApplicationHttpRequest ) wrapRequest ( state );
            String contextPath = context.getPath();
            if ( requestURI != null )
                wrequest.setAttribute ( RequestDispatcher.INCLUDE_REQUEST_URI,
                                        requestURI );
            if ( contextPath != null )
                wrequest.setAttribute ( RequestDispatcher.INCLUDE_CONTEXT_PATH,
                                        contextPath );
            if ( servletPath != null )
                wrequest.setAttribute ( RequestDispatcher.INCLUDE_SERVLET_PATH,
                                        servletPath );
            if ( pathInfo != null )
                wrequest.setAttribute ( RequestDispatcher.INCLUDE_PATH_INFO,
                                        pathInfo );
            if ( queryString != null ) {
                wrequest.setAttribute ( RequestDispatcher.INCLUDE_QUERY_STRING,
                                        queryString );
                wrequest.setQueryParams ( queryString );
            }
            if ( mapping != null ) {
                wrequest.setAttribute ( RequestDispatcher.INCLUDE_MAPPING, mapping );
            }
            wrequest.setAttribute ( Globals.DISPATCHER_TYPE_ATTR,
                                    DispatcherType.INCLUDE );
            wrequest.setAttribute ( Globals.DISPATCHER_REQUEST_PATH_ATTR,
                                    getCombinedPath() );
            invoke ( state.outerRequest, state.outerResponse, state );
        }
    }
    @Override
    public void dispatch ( ServletRequest request, ServletResponse response )
    throws ServletException, IOException {
        if ( Globals.IS_SECURITY_ENABLED ) {
            try {
                PrivilegedDispatch dp = new PrivilegedDispatch ( request, response );
                AccessController.doPrivileged ( dp );
            } catch ( PrivilegedActionException pe ) {
                Exception e = pe.getException();
                if ( e instanceof ServletException ) {
                    throw ( ServletException ) e;
                }
                throw ( IOException ) e;
            }
        } else {
            doDispatch ( request, response );
        }
    }
    private void doDispatch ( ServletRequest request, ServletResponse response )
    throws ServletException, IOException {
        State state = new State ( request, response, false );
        wrapResponse ( state );
        ApplicationHttpRequest wrequest =
            ( ApplicationHttpRequest ) wrapRequest ( state );
        if ( queryString != null ) {
            wrequest.setQueryParams ( queryString );
        }
        wrequest.setAttribute ( Globals.DISPATCHER_TYPE_ATTR,
                                DispatcherType.ASYNC );
        wrequest.setAttribute ( Globals.DISPATCHER_REQUEST_PATH_ATTR,
                                getCombinedPath() );
        wrequest.setContextPath ( context.getPath() );
        wrequest.setRequestURI ( requestURI );
        wrequest.setServletPath ( servletPath );
        wrequest.setPathInfo ( pathInfo );
        if ( queryString != null ) {
            wrequest.setQueryString ( queryString );
            wrequest.setQueryParams ( queryString );
        }
        invoke ( state.outerRequest, state.outerResponse, state );
    }
    private void invoke ( ServletRequest request, ServletResponse response,
                          State state ) throws IOException, ServletException {
        ClassLoader oldCCL = context.bind ( false, null );
        HttpServletResponse hresponse = state.hresponse;
        Servlet servlet = null;
        IOException ioException = null;
        ServletException servletException = null;
        RuntimeException runtimeException = null;
        boolean unavailable = false;
        if ( wrapper.isUnavailable() ) {
            wrapper.getLogger().warn (
                sm.getString ( "applicationDispatcher.isUnavailable",
                               wrapper.getName() ) );
            long available = wrapper.getAvailable();
            if ( ( available > 0L ) && ( available < Long.MAX_VALUE ) ) {
                hresponse.setDateHeader ( "Retry-After", available );
            }
            hresponse.sendError ( HttpServletResponse.SC_SERVICE_UNAVAILABLE, sm
                                  .getString ( "applicationDispatcher.isUnavailable", wrapper
                                               .getName() ) );
            unavailable = true;
        }
        try {
            if ( !unavailable ) {
                servlet = wrapper.allocate();
            }
        } catch ( ServletException e ) {
            wrapper.getLogger().error ( sm.getString ( "applicationDispatcher.allocateException",
                                        wrapper.getName() ), StandardWrapper.getRootCause ( e ) );
            servletException = e;
        } catch ( Throwable e ) {
            ExceptionUtils.handleThrowable ( e );
            wrapper.getLogger().error ( sm.getString ( "applicationDispatcher.allocateException",
                                        wrapper.getName() ), e );
            servletException = new ServletException
            ( sm.getString ( "applicationDispatcher.allocateException",
                             wrapper.getName() ), e );
            servlet = null;
        }
        ApplicationFilterChain filterChain =
            ApplicationFilterFactory.createFilterChain ( request, wrapper, servlet );
        try {
            if ( ( servlet != null ) && ( filterChain != null ) ) {
                filterChain.doFilter ( request, response );
            }
        } catch ( ClientAbortException e ) {
            ioException = e;
        } catch ( IOException e ) {
            wrapper.getLogger().error ( sm.getString ( "applicationDispatcher.serviceException",
                                        wrapper.getName() ), e );
            ioException = e;
        } catch ( UnavailableException e ) {
            wrapper.getLogger().error ( sm.getString ( "applicationDispatcher.serviceException",
                                        wrapper.getName() ), e );
            servletException = e;
            wrapper.unavailable ( e );
        } catch ( ServletException e ) {
            Throwable rootCause = StandardWrapper.getRootCause ( e );
            if ( ! ( rootCause instanceof ClientAbortException ) ) {
                wrapper.getLogger().error ( sm.getString ( "applicationDispatcher.serviceException",
                                            wrapper.getName() ), rootCause );
            }
            servletException = e;
        } catch ( RuntimeException e ) {
            wrapper.getLogger().error ( sm.getString ( "applicationDispatcher.serviceException",
                                        wrapper.getName() ), e );
            runtimeException = e;
        }
        try {
            if ( filterChain != null ) {
                filterChain.release();
            }
        } catch ( Throwable e ) {
            ExceptionUtils.handleThrowable ( e );
            wrapper.getLogger().error ( sm.getString ( "standardWrapper.releaseFilters",
                                        wrapper.getName() ), e );
        }
        try {
            if ( servlet != null ) {
                wrapper.deallocate ( servlet );
            }
        } catch ( ServletException e ) {
            wrapper.getLogger().error ( sm.getString ( "applicationDispatcher.deallocateException",
                                        wrapper.getName() ), e );
            servletException = e;
        } catch ( Throwable e ) {
            ExceptionUtils.handleThrowable ( e );
            wrapper.getLogger().error ( sm.getString ( "applicationDispatcher.deallocateException",
                                        wrapper.getName() ), e );
            servletException = new ServletException
            ( sm.getString ( "applicationDispatcher.deallocateException",
                             wrapper.getName() ), e );
        }
        context.unbind ( false, oldCCL );
        unwrapRequest ( state );
        unwrapResponse ( state );
        recycleRequestWrapper ( state );
        if ( ioException != null ) {
            throw ioException;
        }
        if ( servletException != null ) {
            throw servletException;
        }
        if ( runtimeException != null ) {
            throw runtimeException;
        }
    }
    private void unwrapRequest ( State state ) {
        if ( state.wrapRequest == null ) {
            return;
        }
        if ( state.outerRequest.isAsyncStarted() ) {
            if ( !state.outerRequest.getAsyncContext().hasOriginalRequestAndResponse() ) {
                return;
            }
        }
        ServletRequest previous = null;
        ServletRequest current = state.outerRequest;
        while ( current != null ) {
            if ( ( current instanceof Request )
                    || ( current instanceof RequestFacade ) ) {
                break;
            }
            if ( current == state.wrapRequest ) {
                ServletRequest next =
                    ( ( ServletRequestWrapper ) current ).getRequest();
                if ( previous == null ) {
                    state.outerRequest = next;
                } else {
                    ( ( ServletRequestWrapper ) previous ).setRequest ( next );
                }
                break;
            }
            previous = current;
            current = ( ( ServletRequestWrapper ) current ).getRequest();
        }
    }
    private void unwrapResponse ( State state ) {
        if ( state.wrapResponse == null ) {
            return;
        }
        if ( state.outerRequest.isAsyncStarted() ) {
            if ( !state.outerRequest.getAsyncContext().hasOriginalRequestAndResponse() ) {
                return;
            }
        }
        ServletResponse previous = null;
        ServletResponse current = state.outerResponse;
        while ( current != null ) {
            if ( ( current instanceof Response )
                    || ( current instanceof ResponseFacade ) ) {
                break;
            }
            if ( current == state.wrapResponse ) {
                ServletResponse next =
                    ( ( ServletResponseWrapper ) current ).getResponse();
                if ( previous == null ) {
                    state.outerResponse = next;
                } else {
                    ( ( ServletResponseWrapper ) previous ).setResponse ( next );
                }
                break;
            }
            previous = current;
            current = ( ( ServletResponseWrapper ) current ).getResponse();
        }
    }
    private ServletRequest wrapRequest ( State state ) {
        ServletRequest previous = null;
        ServletRequest current = state.outerRequest;
        while ( current != null ) {
            if ( state.hrequest == null && ( current instanceof HttpServletRequest ) ) {
                state.hrequest = ( HttpServletRequest ) current;
            }
            if ( ! ( current instanceof ServletRequestWrapper ) ) {
                break;
            }
            if ( current instanceof ApplicationHttpRequest ) {
                break;
            }
            if ( current instanceof ApplicationRequest ) {
                break;
            }
            previous = current;
            current = ( ( ServletRequestWrapper ) current ).getRequest();
        }
        ServletRequest wrapper = null;
        if ( ( current instanceof ApplicationHttpRequest ) ||
                ( current instanceof Request ) ||
                ( current instanceof HttpServletRequest ) ) {
            HttpServletRequest hcurrent = ( HttpServletRequest ) current;
            boolean crossContext = false;
            if ( ( state.outerRequest instanceof ApplicationHttpRequest ) ||
                    ( state.outerRequest instanceof Request ) ||
                    ( state.outerRequest instanceof HttpServletRequest ) ) {
                HttpServletRequest houterRequest =
                    ( HttpServletRequest ) state.outerRequest;
                Object contextPath = houterRequest.getAttribute (
                                         RequestDispatcher.INCLUDE_CONTEXT_PATH );
                if ( contextPath == null ) {
                    contextPath = houterRequest.getContextPath();
                }
                crossContext = ! ( context.getPath().equals ( contextPath ) );
            }
            wrapper = new ApplicationHttpRequest
            ( hcurrent, context, crossContext );
        } else {
            wrapper = new ApplicationRequest ( current );
        }
        if ( previous == null ) {
            state.outerRequest = wrapper;
        } else {
            ( ( ServletRequestWrapper ) previous ).setRequest ( wrapper );
        }
        state.wrapRequest = wrapper;
        return ( wrapper );
    }
    private ServletResponse wrapResponse ( State state ) {
        ServletResponse previous = null;
        ServletResponse current = state.outerResponse;
        while ( current != null ) {
            if ( state.hresponse == null && ( current instanceof HttpServletResponse ) ) {
                state.hresponse = ( HttpServletResponse ) current;
                if ( !state.including ) {
                    return null;
                }
            }
            if ( ! ( current instanceof ServletResponseWrapper ) ) {
                break;
            }
            if ( current instanceof ApplicationHttpResponse ) {
                break;
            }
            if ( current instanceof ApplicationResponse ) {
                break;
            }
            previous = current;
            current = ( ( ServletResponseWrapper ) current ).getResponse();
        }
        ServletResponse wrapper = null;
        if ( ( current instanceof ApplicationHttpResponse ) ||
                ( current instanceof Response ) ||
                ( current instanceof HttpServletResponse ) )
            wrapper =
                new ApplicationHttpResponse ( ( HttpServletResponse ) current,
                                              state.including );
        else {
            wrapper = new ApplicationResponse ( current, state.including );
        }
        if ( previous == null ) {
            state.outerResponse = wrapper;
        } else {
            ( ( ServletResponseWrapper ) previous ).setResponse ( wrapper );
        }
        state.wrapResponse = wrapper;
        return ( wrapper );
    }
    private void checkSameObjects ( ServletRequest appRequest,
                                    ServletResponse appResponse ) throws ServletException {
        ServletRequest originalRequest =
            ApplicationFilterChain.getLastServicedRequest();
        ServletResponse originalResponse =
            ApplicationFilterChain.getLastServicedResponse();
        if ( originalRequest == null || originalResponse == null ) {
            return;
        }
        boolean same = false;
        ServletRequest dispatchedRequest = appRequest;
        while ( originalRequest instanceof ServletRequestWrapper &&
                ( ( ServletRequestWrapper ) originalRequest ).getRequest() != null ) {
            originalRequest =
                ( ( ServletRequestWrapper ) originalRequest ).getRequest();
        }
        while ( !same ) {
            if ( originalRequest.equals ( dispatchedRequest ) ) {
                same = true;
            }
            if ( !same && dispatchedRequest instanceof ServletRequestWrapper ) {
                dispatchedRequest =
                    ( ( ServletRequestWrapper ) dispatchedRequest ).getRequest();
            } else {
                break;
            }
        }
        if ( !same ) {
            throw new ServletException ( sm.getString (
                                             "applicationDispatcher.specViolation.request" ) );
        }
        same = false;
        ServletResponse dispatchedResponse = appResponse;
        while ( originalResponse instanceof ServletResponseWrapper &&
                ( ( ServletResponseWrapper ) originalResponse ).getResponse() !=
                null ) {
            originalResponse =
                ( ( ServletResponseWrapper ) originalResponse ).getResponse();
        }
        while ( !same ) {
            if ( originalResponse.equals ( dispatchedResponse ) ) {
                same = true;
            }
            if ( !same && dispatchedResponse instanceof ServletResponseWrapper ) {
                dispatchedResponse =
                    ( ( ServletResponseWrapper ) dispatchedResponse ).getResponse();
            } else {
                break;
            }
        }
        if ( !same ) {
            throw new ServletException ( sm.getString (
                                             "applicationDispatcher.specViolation.response" ) );
        }
    }
    private void recycleRequestWrapper ( State state ) {
        if ( state.wrapRequest instanceof ApplicationHttpRequest ) {
            ( ( ApplicationHttpRequest ) state.wrapRequest ).recycle();
        }
    }
}
