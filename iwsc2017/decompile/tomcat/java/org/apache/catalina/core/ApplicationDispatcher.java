package org.apache.catalina.core;
import javax.servlet.ServletResponseWrapper;
import org.apache.catalina.connector.Response;
import javax.servlet.ServletRequestWrapper;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.Request;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.UnavailableException;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.tomcat.util.ExceptionUtils;
import javax.servlet.DispatcherType;
import javax.servlet.ServletOutputStream;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import org.apache.catalina.connector.ResponseFacade;
import java.security.PrivilegedActionException;
import java.io.IOException;
import javax.servlet.ServletException;
import java.security.PrivilegedExceptionAction;
import java.security.AccessController;
import org.apache.catalina.Globals;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import org.apache.catalina.Wrapper;
import org.apache.tomcat.util.res.StringManager;
import javax.servlet.http.Mapping;
import org.apache.catalina.Context;
import javax.servlet.RequestDispatcher;
import org.apache.catalina.AsyncDispatcher;
final class ApplicationDispatcher implements AsyncDispatcher, RequestDispatcher {
    static final boolean STRICT_SERVLET_COMPLIANCE;
    static final boolean WRAP_SAME_OBJECT;
    private final Context context;
    private final String name;
    private final String pathInfo;
    private final String queryString;
    private final String requestURI;
    private final String servletPath;
    private final Mapping mapping;
    private static final StringManager sm;
    private final Wrapper wrapper;
    public ApplicationDispatcher ( final Wrapper wrapper, final String requestURI, final String servletPath, final String pathInfo, final String queryString, final Mapping mapping, final String name ) {
        this.wrapper = wrapper;
        this.context = ( Context ) wrapper.getParent();
        this.requestURI = requestURI;
        this.servletPath = servletPath;
        this.pathInfo = pathInfo;
        this.queryString = queryString;
        this.mapping = mapping;
        this.name = name;
    }
    public void forward ( final ServletRequest request, final ServletResponse response ) throws ServletException, IOException {
        if ( Globals.IS_SECURITY_ENABLED ) {
            try {
                final PrivilegedForward dp = new PrivilegedForward ( request, response );
                AccessController.doPrivileged ( ( PrivilegedExceptionAction<Object> ) dp );
                return;
            } catch ( PrivilegedActionException pe ) {
                final Exception e = pe.getException();
                if ( e instanceof ServletException ) {
                    throw ( ServletException ) e;
                }
                throw ( IOException ) e;
            }
        }
        this.doForward ( request, response );
    }
    private void doForward ( final ServletRequest request, final ServletResponse response ) throws ServletException, IOException {
        if ( response.isCommitted() ) {
            throw new IllegalStateException ( ApplicationDispatcher.sm.getString ( "applicationDispatcher.forward.ise" ) );
        }
        try {
            response.resetBuffer();
        } catch ( IllegalStateException e ) {
            throw e;
        }
        final State state = new State ( request, response, false );
        if ( ApplicationDispatcher.WRAP_SAME_OBJECT ) {
            this.checkSameObjects ( request, response );
        }
        this.wrapResponse ( state );
        if ( this.servletPath == null && this.pathInfo == null ) {
            final ApplicationHttpRequest wrequest = ( ApplicationHttpRequest ) this.wrapRequest ( state );
            final HttpServletRequest hrequest = state.hrequest;
            wrequest.setRequestURI ( hrequest.getRequestURI() );
            wrequest.setContextPath ( hrequest.getContextPath() );
            wrequest.setServletPath ( hrequest.getServletPath() );
            wrequest.setPathInfo ( hrequest.getPathInfo() );
            wrequest.setQueryString ( hrequest.getQueryString() );
            this.processRequest ( request, response, state );
        } else {
            final ApplicationHttpRequest wrequest = ( ApplicationHttpRequest ) this.wrapRequest ( state );
            final String contextPath = this.context.getPath();
            final HttpServletRequest hrequest2 = state.hrequest;
            if ( hrequest2.getAttribute ( "javax.servlet.forward.request_uri" ) == null ) {
                wrequest.setAttribute ( "javax.servlet.forward.request_uri", hrequest2.getRequestURI() );
                wrequest.setAttribute ( "javax.servlet.forward.context_path", hrequest2.getContextPath() );
                wrequest.setAttribute ( "javax.servlet.forward.servlet_path", hrequest2.getServletPath() );
                wrequest.setAttribute ( "javax.servlet.forward.path_info", hrequest2.getPathInfo() );
                wrequest.setAttribute ( "javax.servlet.forward.query_string", hrequest2.getQueryString() );
                wrequest.setAttribute ( "javax.servlet.forward.mapping", hrequest2.getMapping() );
            }
            wrequest.setContextPath ( contextPath );
            wrequest.setRequestURI ( this.requestURI );
            wrequest.setServletPath ( this.servletPath );
            wrequest.setPathInfo ( this.pathInfo );
            if ( this.queryString != null ) {
                wrequest.setQueryString ( this.queryString );
                wrequest.setQueryParams ( this.queryString );
            }
            wrequest.setMapping ( this.mapping );
            this.processRequest ( request, response, state );
        }
        if ( request.isAsyncStarted() ) {
            return;
        }
        if ( this.wrapper.getLogger().isDebugEnabled() ) {
            this.wrapper.getLogger().debug ( " Disabling the response for futher output" );
        }
        if ( response instanceof ResponseFacade ) {
            ( ( ResponseFacade ) response ).finish();
        } else {
            if ( this.wrapper.getLogger().isDebugEnabled() ) {
                this.wrapper.getLogger().debug ( " The Response is vehiculed using a wrapper: " + response.getClass().getName() );
            }
            try {
                final PrintWriter writer = response.getWriter();
                writer.close();
            } catch ( IllegalStateException e2 ) {
                try {
                    final ServletOutputStream stream = response.getOutputStream();
                    stream.close();
                } catch ( IllegalStateException ex ) {}
                catch ( IOException ex2 ) {}
            } catch ( IOException ex3 ) {}
        }
    }
    private void processRequest ( final ServletRequest request, final ServletResponse response, final State state ) throws IOException, ServletException {
        final DispatcherType disInt = ( DispatcherType ) request.getAttribute ( "org.apache.catalina.core.DISPATCHER_TYPE" );
        if ( disInt != null ) {
            boolean doInvoke = true;
            if ( this.context.getFireRequestListenersOnForwards() && !this.context.fireRequestInitEvent ( request ) ) {
                doInvoke = false;
            }
            if ( doInvoke ) {
                if ( disInt != DispatcherType.ERROR ) {
                    state.outerRequest.setAttribute ( "org.apache.catalina.core.DISPATCHER_REQUEST_PATH", ( Object ) this.getCombinedPath() );
                    state.outerRequest.setAttribute ( "org.apache.catalina.core.DISPATCHER_TYPE", ( Object ) DispatcherType.FORWARD );
                    this.invoke ( state.outerRequest, response, state );
                } else {
                    this.invoke ( state.outerRequest, response, state );
                }
                if ( this.context.getFireRequestListenersOnForwards() ) {
                    this.context.fireRequestDestroyEvent ( request );
                }
            }
        }
    }
    private String getCombinedPath() {
        if ( this.servletPath == null ) {
            return null;
        }
        if ( this.pathInfo == null ) {
            return this.servletPath;
        }
        return this.servletPath + this.pathInfo;
    }
    public void include ( final ServletRequest request, final ServletResponse response ) throws ServletException, IOException {
        if ( Globals.IS_SECURITY_ENABLED ) {
            try {
                final PrivilegedInclude dp = new PrivilegedInclude ( request, response );
                AccessController.doPrivileged ( ( PrivilegedExceptionAction<Object> ) dp );
                return;
            } catch ( PrivilegedActionException pe ) {
                final Exception e = pe.getException();
                if ( e instanceof ServletException ) {
                    throw ( ServletException ) e;
                }
                throw ( IOException ) e;
            }
        }
        this.doInclude ( request, response );
    }
    private void doInclude ( final ServletRequest request, final ServletResponse response ) throws ServletException, IOException {
        final State state = new State ( request, response, true );
        if ( ApplicationDispatcher.WRAP_SAME_OBJECT ) {
            this.checkSameObjects ( request, response );
        }
        this.wrapResponse ( state );
        if ( this.name != null ) {
            final ApplicationHttpRequest wrequest = ( ApplicationHttpRequest ) this.wrapRequest ( state );
            wrequest.setAttribute ( "org.apache.catalina.NAMED", this.name );
            if ( this.servletPath != null ) {
                wrequest.setServletPath ( this.servletPath );
            }
            wrequest.setAttribute ( "org.apache.catalina.core.DISPATCHER_TYPE", DispatcherType.INCLUDE );
            wrequest.setAttribute ( "org.apache.catalina.core.DISPATCHER_REQUEST_PATH", this.getCombinedPath() );
            this.invoke ( state.outerRequest, state.outerResponse, state );
        } else {
            final ApplicationHttpRequest wrequest = ( ApplicationHttpRequest ) this.wrapRequest ( state );
            final String contextPath = this.context.getPath();
            if ( this.requestURI != null ) {
                wrequest.setAttribute ( "javax.servlet.include.request_uri", this.requestURI );
            }
            if ( contextPath != null ) {
                wrequest.setAttribute ( "javax.servlet.include.context_path", contextPath );
            }
            if ( this.servletPath != null ) {
                wrequest.setAttribute ( "javax.servlet.include.servlet_path", this.servletPath );
            }
            if ( this.pathInfo != null ) {
                wrequest.setAttribute ( "javax.servlet.include.path_info", this.pathInfo );
            }
            if ( this.queryString != null ) {
                wrequest.setAttribute ( "javax.servlet.include.query_string", this.queryString );
                wrequest.setQueryParams ( this.queryString );
            }
            if ( this.mapping != null ) {
                wrequest.setAttribute ( "javax.servlet.include.mapping", this.mapping );
            }
            wrequest.setAttribute ( "org.apache.catalina.core.DISPATCHER_TYPE", DispatcherType.INCLUDE );
            wrequest.setAttribute ( "org.apache.catalina.core.DISPATCHER_REQUEST_PATH", this.getCombinedPath() );
            this.invoke ( state.outerRequest, state.outerResponse, state );
        }
    }
    @Override
    public void dispatch ( final ServletRequest request, final ServletResponse response ) throws ServletException, IOException {
        if ( Globals.IS_SECURITY_ENABLED ) {
            try {
                final PrivilegedDispatch dp = new PrivilegedDispatch ( request, response );
                AccessController.doPrivileged ( ( PrivilegedExceptionAction<Object> ) dp );
                return;
            } catch ( PrivilegedActionException pe ) {
                final Exception e = pe.getException();
                if ( e instanceof ServletException ) {
                    throw ( ServletException ) e;
                }
                throw ( IOException ) e;
            }
        }
        this.doDispatch ( request, response );
    }
    private void doDispatch ( final ServletRequest request, final ServletResponse response ) throws ServletException, IOException {
        final State state = new State ( request, response, false );
        this.wrapResponse ( state );
        final ApplicationHttpRequest wrequest = ( ApplicationHttpRequest ) this.wrapRequest ( state );
        if ( this.queryString != null ) {
            wrequest.setQueryParams ( this.queryString );
        }
        wrequest.setAttribute ( "org.apache.catalina.core.DISPATCHER_TYPE", DispatcherType.ASYNC );
        wrequest.setAttribute ( "org.apache.catalina.core.DISPATCHER_REQUEST_PATH", this.getCombinedPath() );
        wrequest.setContextPath ( this.context.getPath() );
        wrequest.setRequestURI ( this.requestURI );
        wrequest.setServletPath ( this.servletPath );
        wrequest.setPathInfo ( this.pathInfo );
        if ( this.queryString != null ) {
            wrequest.setQueryString ( this.queryString );
            wrequest.setQueryParams ( this.queryString );
        }
        this.invoke ( state.outerRequest, state.outerResponse, state );
    }
    private void invoke ( final ServletRequest request, final ServletResponse response, final State state ) throws IOException, ServletException {
        final ClassLoader oldCCL = this.context.bind ( false, null );
        final HttpServletResponse hresponse = state.hresponse;
        Servlet servlet = null;
        IOException ioException = null;
        ServletException servletException = null;
        RuntimeException runtimeException = null;
        boolean unavailable = false;
        if ( this.wrapper.isUnavailable() ) {
            this.wrapper.getLogger().warn ( ApplicationDispatcher.sm.getString ( "applicationDispatcher.isUnavailable", this.wrapper.getName() ) );
            final long available = this.wrapper.getAvailable();
            if ( available > 0L && available < Long.MAX_VALUE ) {
                hresponse.setDateHeader ( "Retry-After", available );
            }
            hresponse.sendError ( 503, ApplicationDispatcher.sm.getString ( "applicationDispatcher.isUnavailable", this.wrapper.getName() ) );
            unavailable = true;
        }
        try {
            if ( !unavailable ) {
                servlet = this.wrapper.allocate();
            }
        } catch ( ServletException e ) {
            this.wrapper.getLogger().error ( ApplicationDispatcher.sm.getString ( "applicationDispatcher.allocateException", this.wrapper.getName() ), StandardWrapper.getRootCause ( e ) );
            servletException = e;
        } catch ( Throwable e2 ) {
            ExceptionUtils.handleThrowable ( e2 );
            this.wrapper.getLogger().error ( ApplicationDispatcher.sm.getString ( "applicationDispatcher.allocateException", this.wrapper.getName() ), e2 );
            servletException = new ServletException ( ApplicationDispatcher.sm.getString ( "applicationDispatcher.allocateException", this.wrapper.getName() ), e2 );
            servlet = null;
        }
        final ApplicationFilterChain filterChain = ApplicationFilterFactory.createFilterChain ( request, this.wrapper, servlet );
        try {
            if ( servlet != null && filterChain != null ) {
                filterChain.doFilter ( request, response );
            }
        } catch ( ClientAbortException e3 ) {
            ioException = e3;
        } catch ( IOException e4 ) {
            this.wrapper.getLogger().error ( ApplicationDispatcher.sm.getString ( "applicationDispatcher.serviceException", this.wrapper.getName() ), e4 );
            ioException = e4;
        } catch ( UnavailableException e5 ) {
            this.wrapper.getLogger().error ( ApplicationDispatcher.sm.getString ( "applicationDispatcher.serviceException", this.wrapper.getName() ), ( Throwable ) e5 );
            servletException = ( ServletException ) e5;
            this.wrapper.unavailable ( e5 );
        } catch ( ServletException e6 ) {
            final Throwable rootCause = StandardWrapper.getRootCause ( e6 );
            if ( ! ( rootCause instanceof ClientAbortException ) ) {
                this.wrapper.getLogger().error ( ApplicationDispatcher.sm.getString ( "applicationDispatcher.serviceException", this.wrapper.getName() ), rootCause );
            }
            servletException = e6;
        } catch ( RuntimeException e7 ) {
            this.wrapper.getLogger().error ( ApplicationDispatcher.sm.getString ( "applicationDispatcher.serviceException", this.wrapper.getName() ), e7 );
            runtimeException = e7;
        }
        try {
            if ( filterChain != null ) {
                filterChain.release();
            }
        } catch ( Throwable e8 ) {
            ExceptionUtils.handleThrowable ( e8 );
            this.wrapper.getLogger().error ( ApplicationDispatcher.sm.getString ( "standardWrapper.releaseFilters", this.wrapper.getName() ), e8 );
        }
        try {
            if ( servlet != null ) {
                this.wrapper.deallocate ( servlet );
            }
        } catch ( ServletException e6 ) {
            this.wrapper.getLogger().error ( ApplicationDispatcher.sm.getString ( "applicationDispatcher.deallocateException", this.wrapper.getName() ), ( Throwable ) e6 );
            servletException = e6;
        } catch ( Throwable e8 ) {
            ExceptionUtils.handleThrowable ( e8 );
            this.wrapper.getLogger().error ( ApplicationDispatcher.sm.getString ( "applicationDispatcher.deallocateException", this.wrapper.getName() ), e8 );
            servletException = new ServletException ( ApplicationDispatcher.sm.getString ( "applicationDispatcher.deallocateException", this.wrapper.getName() ), e8 );
        }
        this.context.unbind ( false, oldCCL );
        this.unwrapRequest ( state );
        this.unwrapResponse ( state );
        this.recycleRequestWrapper ( state );
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
    private void unwrapRequest ( final State state ) {
        if ( state.wrapRequest == null ) {
            return;
        }
        if ( state.outerRequest.isAsyncStarted() && !state.outerRequest.getAsyncContext().hasOriginalRequestAndResponse() ) {
            return;
        }
        ServletRequest previous = null;
        ServletRequest current = state.outerRequest;
        while ( current != null && ! ( current instanceof Request ) ) {
            if ( current instanceof RequestFacade ) {
                break;
            }
            if ( current == state.wrapRequest ) {
                final ServletRequest next = ( ( ServletRequestWrapper ) current ).getRequest();
                if ( previous == null ) {
                    state.outerRequest = next;
                    break;
                }
                ( ( ServletRequestWrapper ) previous ).setRequest ( next );
                break;
            } else {
                previous = current;
                current = ( ( ServletRequestWrapper ) current ).getRequest();
            }
        }
    }
    private void unwrapResponse ( final State state ) {
        if ( state.wrapResponse == null ) {
            return;
        }
        if ( state.outerRequest.isAsyncStarted() && !state.outerRequest.getAsyncContext().hasOriginalRequestAndResponse() ) {
            return;
        }
        ServletResponse previous = null;
        ServletResponse current = state.outerResponse;
        while ( current != null && ! ( current instanceof Response ) ) {
            if ( current instanceof ResponseFacade ) {
                break;
            }
            if ( current == state.wrapResponse ) {
                final ServletResponse next = ( ( ServletResponseWrapper ) current ).getResponse();
                if ( previous == null ) {
                    state.outerResponse = next;
                    break;
                }
                ( ( ServletResponseWrapper ) previous ).setResponse ( next );
                break;
            } else {
                previous = current;
                current = ( ( ServletResponseWrapper ) current ).getResponse();
            }
        }
    }
    private ServletRequest wrapRequest ( final State state ) {
        ServletRequest previous = null;
        ServletRequest current;
        for ( current = state.outerRequest; current != null; current = ( ( ServletRequestWrapper ) current ).getRequest() ) {
            if ( state.hrequest == null && current instanceof HttpServletRequest ) {
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
        }
        ServletRequest wrapper = null;
        if ( current instanceof ApplicationHttpRequest || current instanceof Request || current instanceof HttpServletRequest ) {
            final HttpServletRequest hcurrent = ( HttpServletRequest ) current;
            boolean crossContext = false;
            if ( state.outerRequest instanceof ApplicationHttpRequest || state.outerRequest instanceof Request || state.outerRequest instanceof HttpServletRequest ) {
                final HttpServletRequest houterRequest = ( HttpServletRequest ) state.outerRequest;
                Object contextPath = houterRequest.getAttribute ( "javax.servlet.include.context_path" );
                if ( contextPath == null ) {
                    contextPath = houterRequest.getContextPath();
                }
                crossContext = !this.context.getPath().equals ( contextPath );
            }
            wrapper = ( ServletRequest ) new ApplicationHttpRequest ( hcurrent, this.context, crossContext );
        } else {
            wrapper = ( ServletRequest ) new ApplicationRequest ( current );
        }
        if ( previous == null ) {
            state.outerRequest = wrapper;
        } else {
            ( ( ServletRequestWrapper ) previous ).setRequest ( wrapper );
        }
        return state.wrapRequest = wrapper;
    }
    private ServletResponse wrapResponse ( final State state ) {
        ServletResponse previous = null;
        ServletResponse current;
        for ( current = state.outerResponse; current != null; current = ( ( ServletResponseWrapper ) current ).getResponse() ) {
            if ( state.hresponse == null && current instanceof HttpServletResponse ) {
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
        }
        ServletResponse wrapper = null;
        if ( current instanceof ApplicationHttpResponse || current instanceof Response || current instanceof HttpServletResponse ) {
            wrapper = ( ServletResponse ) new ApplicationHttpResponse ( ( HttpServletResponse ) current, state.including );
        } else {
            wrapper = ( ServletResponse ) new ApplicationResponse ( current, state.including );
        }
        if ( previous == null ) {
            state.outerResponse = wrapper;
        } else {
            ( ( ServletResponseWrapper ) previous ).setResponse ( wrapper );
        }
        return state.wrapResponse = wrapper;
    }
    private void checkSameObjects ( final ServletRequest appRequest, final ServletResponse appResponse ) throws ServletException {
        ServletRequest originalRequest = ApplicationFilterChain.getLastServicedRequest();
        ServletResponse originalResponse = ApplicationFilterChain.getLastServicedResponse();
        if ( originalRequest == null || originalResponse == null ) {
            return;
        }
        boolean same = false;
        ServletRequest dispatchedRequest = appRequest;
        while ( originalRequest instanceof ServletRequestWrapper && ( ( ServletRequestWrapper ) originalRequest ).getRequest() != null ) {
            originalRequest = ( ( ServletRequestWrapper ) originalRequest ).getRequest();
        }
        while ( !same ) {
            if ( originalRequest.equals ( dispatchedRequest ) ) {
                same = true;
            }
            if ( same || ! ( dispatchedRequest instanceof ServletRequestWrapper ) ) {
                break;
            }
            dispatchedRequest = ( ( ServletRequestWrapper ) dispatchedRequest ).getRequest();
        }
        if ( !same ) {
            throw new ServletException ( ApplicationDispatcher.sm.getString ( "applicationDispatcher.specViolation.request" ) );
        }
        same = false;
        ServletResponse dispatchedResponse = appResponse;
        while ( originalResponse instanceof ServletResponseWrapper && ( ( ServletResponseWrapper ) originalResponse ).getResponse() != null ) {
            originalResponse = ( ( ServletResponseWrapper ) originalResponse ).getResponse();
        }
        while ( !same ) {
            if ( originalResponse.equals ( dispatchedResponse ) ) {
                same = true;
            }
            if ( same || ! ( dispatchedResponse instanceof ServletResponseWrapper ) ) {
                break;
            }
            dispatchedResponse = ( ( ServletResponseWrapper ) dispatchedResponse ).getResponse();
        }
        if ( !same ) {
            throw new ServletException ( ApplicationDispatcher.sm.getString ( "applicationDispatcher.specViolation.response" ) );
        }
    }
    private void recycleRequestWrapper ( final State state ) {
        if ( state.wrapRequest instanceof ApplicationHttpRequest ) {
            ( ( ApplicationHttpRequest ) state.wrapRequest ).recycle();
        }
    }
    static {
        STRICT_SERVLET_COMPLIANCE = Globals.STRICT_SERVLET_COMPLIANCE;
        final String wrapSameObject = System.getProperty ( "org.apache.catalina.core.ApplicationDispatcher.WRAP_SAME_OBJECT" );
        if ( wrapSameObject == null ) {
            WRAP_SAME_OBJECT = ApplicationDispatcher.STRICT_SERVLET_COMPLIANCE;
        } else {
            WRAP_SAME_OBJECT = Boolean.parseBoolean ( wrapSameObject );
        }
        sm = StringManager.getManager ( "org.apache.catalina.core" );
    }
    protected class PrivilegedForward implements PrivilegedExceptionAction<Void> {
        private final ServletRequest request;
        private final ServletResponse response;
        PrivilegedForward ( final ServletRequest request, final ServletResponse response ) {
            this.request = request;
            this.response = response;
        }
        @Override
        public Void run() throws Exception {
            ApplicationDispatcher.this.doForward ( this.request, this.response );
            return null;
        }
    }
    protected class PrivilegedInclude implements PrivilegedExceptionAction<Void> {
        private final ServletRequest request;
        private final ServletResponse response;
        PrivilegedInclude ( final ServletRequest request, final ServletResponse response ) {
            this.request = request;
            this.response = response;
        }
        @Override
        public Void run() throws ServletException, IOException {
            ApplicationDispatcher.this.doInclude ( this.request, this.response );
            return null;
        }
    }
    protected class PrivilegedDispatch implements PrivilegedExceptionAction<Void> {
        private final ServletRequest request;
        private final ServletResponse response;
        PrivilegedDispatch ( final ServletRequest request, final ServletResponse response ) {
            this.request = request;
            this.response = response;
        }
        @Override
        public Void run() throws ServletException, IOException {
            ApplicationDispatcher.this.doDispatch ( this.request, this.response );
            return null;
        }
    }
    private static class State {
        ServletRequest outerRequest;
        ServletResponse outerResponse;
        ServletRequest wrapRequest;
        ServletResponse wrapResponse;
        boolean including;
        HttpServletRequest hrequest;
        HttpServletResponse hresponse;
        State ( final ServletRequest request, final ServletResponse response, final boolean including ) {
            this.outerRequest = null;
            this.outerResponse = null;
            this.wrapRequest = null;
            this.wrapResponse = null;
            this.including = false;
            this.hrequest = null;
            this.hresponse = null;
            this.outerRequest = request;
            this.outerResponse = response;
            this.including = including;
        }
    }
}
