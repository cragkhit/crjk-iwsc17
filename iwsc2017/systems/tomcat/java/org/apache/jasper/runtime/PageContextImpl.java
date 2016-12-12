package org.apache.jasper.runtime;
import java.io.IOException;
import java.io.Writer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.ImportHandler;
import javax.el.ValueExpression;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import org.apache.jasper.Constants;
import org.apache.jasper.compiler.Localizer;
import org.apache.jasper.el.ELContextImpl;
import org.apache.jasper.runtime.JspContextWrapper.ELContextWrapper;
import org.apache.jasper.security.SecurityUtil;
public class PageContextImpl extends PageContext {
    private static final JspFactory jspf = JspFactory.getDefaultFactory();
    private BodyContentImpl[] outs;
    private int depth;
    private Servlet servlet;
    private ServletConfig config;
    private ServletContext context;
    private JspApplicationContextImpl applicationContext;
    private String errorPageURL;
    private final transient HashMap<String, Object> attributes;
    private transient ServletRequest request;
    private transient ServletResponse response;
    private transient HttpSession session;
    private transient ELContextImpl elContext;
    private boolean isIncluded;
    private transient JspWriter out;
    private transient JspWriterImpl baseOut;
    PageContextImpl() {
        this.outs = new BodyContentImpl[0];
        this.attributes = new HashMap<> ( 16 );
        this.depth = -1;
    }
    @Override
    public void initialize ( Servlet servlet, ServletRequest request,
                             ServletResponse response, String errorPageURL,
                             boolean needsSession, int bufferSize, boolean autoFlush )
    throws IOException {
        this.servlet = servlet;
        this.config = servlet.getServletConfig();
        this.context = config.getServletContext();
        this.errorPageURL = errorPageURL;
        this.request = request;
        this.response = response;
        this.applicationContext = JspApplicationContextImpl.getInstance ( context );
        if ( request instanceof HttpServletRequest && needsSession ) {
            this.session = ( ( HttpServletRequest ) request ).getSession();
        }
        if ( needsSession && session == null )
            throw new IllegalStateException (
                "Page needs a session and none is available" );
        depth = -1;
        if ( bufferSize == JspWriter.DEFAULT_BUFFER ) {
            bufferSize = Constants.DEFAULT_BUFFER_SIZE;
        }
        if ( this.baseOut == null ) {
            this.baseOut = new JspWriterImpl ( response, bufferSize, autoFlush );
        } else {
            this.baseOut.init ( response, bufferSize, autoFlush );
        }
        this.out = baseOut;
        setAttribute ( OUT, this.out );
        setAttribute ( REQUEST, request );
        setAttribute ( RESPONSE, response );
        if ( session != null ) {
            setAttribute ( SESSION, session );
        }
        setAttribute ( PAGE, servlet );
        setAttribute ( CONFIG, config );
        setAttribute ( PAGECONTEXT, this );
        setAttribute ( APPLICATION, context );
        isIncluded = request.getAttribute (
                         RequestDispatcher.INCLUDE_SERVLET_PATH ) != null;
    }
    @Override
    public void release() {
        out = baseOut;
        try {
            if ( isIncluded ) {
                ( ( JspWriterImpl ) out ).flushBuffer();
            } else {
                ( ( JspWriterImpl ) out ).flushBuffer();
            }
        } catch ( IOException ex ) {
            IllegalStateException ise = new IllegalStateException ( Localizer.getMessage ( "jsp.error.flush" ), ex );
            throw ise;
        } finally {
            servlet = null;
            config = null;
            context = null;
            applicationContext = null;
            elContext = null;
            errorPageURL = null;
            request = null;
            response = null;
            depth = -1;
            baseOut.recycle();
            session = null;
            attributes.clear();
            for ( BodyContentImpl body : outs ) {
                body.recycle();
            }
        }
    }
    @Override
    public Object getAttribute ( final String name ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer
                                             .getMessage ( "jsp.error.attribute.null_name" ) );
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return AccessController.doPrivileged (
            new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    return doGetAttribute ( name );
                }
            } );
        } else {
            return doGetAttribute ( name );
        }
    }
    private Object doGetAttribute ( String name ) {
        return attributes.get ( name );
    }
    @Override
    public Object getAttribute ( final String name, final int scope ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer
                                             .getMessage ( "jsp.error.attribute.null_name" ) );
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return AccessController.doPrivileged (
            new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    return doGetAttribute ( name, scope );
                }
            } );
        } else {
            return doGetAttribute ( name, scope );
        }
    }
    private Object doGetAttribute ( String name, int scope ) {
        switch ( scope ) {
        case PAGE_SCOPE:
            return attributes.get ( name );
        case REQUEST_SCOPE:
            return request.getAttribute ( name );
        case SESSION_SCOPE:
            if ( session == null ) {
                throw new IllegalStateException ( Localizer
                                                  .getMessage ( "jsp.error.page.noSession" ) );
            }
            return session.getAttribute ( name );
        case APPLICATION_SCOPE:
            return context.getAttribute ( name );
        default:
            throw new IllegalArgumentException ( "Invalid scope" );
        }
    }
    @Override
    public void setAttribute ( final String name, final Object attribute ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer
                                             .getMessage ( "jsp.error.attribute.null_name" ) );
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            AccessController.doPrivileged ( new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    doSetAttribute ( name, attribute );
                    return null;
                }
            } );
        } else {
            doSetAttribute ( name, attribute );
        }
    }
    private void doSetAttribute ( String name, Object attribute ) {
        if ( attribute != null ) {
            attributes.put ( name, attribute );
        } else {
            removeAttribute ( name, PAGE_SCOPE );
        }
    }
    @Override
    public void setAttribute ( final String name, final Object o, final int scope ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer
                                             .getMessage ( "jsp.error.attribute.null_name" ) );
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            AccessController.doPrivileged ( new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    doSetAttribute ( name, o, scope );
                    return null;
                }
            } );
        } else {
            doSetAttribute ( name, o, scope );
        }
    }
    private void doSetAttribute ( String name, Object o, int scope ) {
        if ( o != null ) {
            switch ( scope ) {
            case PAGE_SCOPE:
                attributes.put ( name, o );
                break;
            case REQUEST_SCOPE:
                request.setAttribute ( name, o );
                break;
            case SESSION_SCOPE:
                if ( session == null ) {
                    throw new IllegalStateException ( Localizer
                                                      .getMessage ( "jsp.error.page.noSession" ) );
                }
                session.setAttribute ( name, o );
                break;
            case APPLICATION_SCOPE:
                context.setAttribute ( name, o );
                break;
            default:
                throw new IllegalArgumentException ( "Invalid scope" );
            }
        } else {
            removeAttribute ( name, scope );
        }
    }
    @Override
    public void removeAttribute ( final String name, final int scope ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer
                                             .getMessage ( "jsp.error.attribute.null_name" ) );
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            AccessController.doPrivileged ( new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    doRemoveAttribute ( name, scope );
                    return null;
                }
            } );
        } else {
            doRemoveAttribute ( name, scope );
        }
    }
    private void doRemoveAttribute ( String name, int scope ) {
        switch ( scope ) {
        case PAGE_SCOPE:
            attributes.remove ( name );
            break;
        case REQUEST_SCOPE:
            request.removeAttribute ( name );
            break;
        case SESSION_SCOPE:
            if ( session == null ) {
                throw new IllegalStateException ( Localizer
                                                  .getMessage ( "jsp.error.page.noSession" ) );
            }
            session.removeAttribute ( name );
            break;
        case APPLICATION_SCOPE:
            context.removeAttribute ( name );
            break;
        default:
            throw new IllegalArgumentException ( "Invalid scope" );
        }
    }
    @Override
    public int getAttributesScope ( final String name ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer
                                             .getMessage ( "jsp.error.attribute.null_name" ) );
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( AccessController
            .doPrivileged ( new PrivilegedAction<Integer>() {
                @Override
                public Integer run() {
                    return Integer.valueOf ( doGetAttributeScope ( name ) );
                }
            } ) ).intValue();
        } else {
            return doGetAttributeScope ( name );
        }
    }
    private int doGetAttributeScope ( String name ) {
        if ( attributes.get ( name ) != null ) {
            return PAGE_SCOPE;
        }
        if ( request.getAttribute ( name ) != null ) {
            return REQUEST_SCOPE;
        }
        if ( session != null ) {
            try {
                if ( session.getAttribute ( name ) != null ) {
                    return SESSION_SCOPE;
                }
            } catch ( IllegalStateException ise ) {
            }
        }
        if ( context.getAttribute ( name ) != null ) {
            return APPLICATION_SCOPE;
        }
        return 0;
    }
    @Override
    public Object findAttribute ( final String name ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return AccessController.doPrivileged (
            new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    if ( name == null ) {
                        throw new NullPointerException ( Localizer
                                                         .getMessage ( "jsp.error.attribute.null_name" ) );
                    }
                    return doFindAttribute ( name );
                }
            } );
        } else {
            if ( name == null ) {
                throw new NullPointerException ( Localizer
                                                 .getMessage ( "jsp.error.attribute.null_name" ) );
            }
            return doFindAttribute ( name );
        }
    }
    private Object doFindAttribute ( String name ) {
        Object o = attributes.get ( name );
        if ( o != null ) {
            return o;
        }
        o = request.getAttribute ( name );
        if ( o != null ) {
            return o;
        }
        if ( session != null ) {
            try {
                o = session.getAttribute ( name );
            } catch ( IllegalStateException ise ) {
            }
            if ( o != null ) {
                return o;
            }
        }
        return context.getAttribute ( name );
    }
    @Override
    public Enumeration<String> getAttributeNamesInScope ( final int scope ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return AccessController.doPrivileged (
            new PrivilegedAction<Enumeration<String>>() {
                @Override
                public Enumeration<String> run() {
                    return doGetAttributeNamesInScope ( scope );
                }
            } );
        } else {
            return doGetAttributeNamesInScope ( scope );
        }
    }
    private Enumeration<String> doGetAttributeNamesInScope ( int scope ) {
        switch ( scope ) {
        case PAGE_SCOPE:
            return Collections.enumeration ( attributes.keySet() );
        case REQUEST_SCOPE:
            return request.getAttributeNames();
        case SESSION_SCOPE:
            if ( session == null ) {
                throw new IllegalStateException ( Localizer
                                                  .getMessage ( "jsp.error.page.noSession" ) );
            }
            return session.getAttributeNames();
        case APPLICATION_SCOPE:
            return context.getAttributeNames();
        default:
            throw new IllegalArgumentException ( "Invalid scope" );
        }
    }
    @Override
    public void removeAttribute ( final String name ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer
                                             .getMessage ( "jsp.error.attribute.null_name" ) );
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            AccessController.doPrivileged ( new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    doRemoveAttribute ( name );
                    return null;
                }
            } );
        } else {
            doRemoveAttribute ( name );
        }
    }
    private void doRemoveAttribute ( String name ) {
        removeAttribute ( name, PAGE_SCOPE );
        removeAttribute ( name, REQUEST_SCOPE );
        if ( session != null ) {
            try {
                removeAttribute ( name, SESSION_SCOPE );
            } catch ( IllegalStateException ise ) {
            }
        }
        removeAttribute ( name, APPLICATION_SCOPE );
    }
    @Override
    public JspWriter getOut() {
        return out;
    }
    @Override
    public HttpSession getSession() {
        return session;
    }
    @Override
    public ServletConfig getServletConfig() {
        return config;
    }
    @Override
    public ServletContext getServletContext() {
        return config.getServletContext();
    }
    @Override
    public ServletRequest getRequest() {
        return request;
    }
    @Override
    public ServletResponse getResponse() {
        return response;
    }
    @Override
    public Exception getException() {
        Throwable t = JspRuntimeLibrary.getThrowable ( request );
        if ( ( t != null ) && ( ! ( t instanceof Exception ) ) ) {
            t = new JspException ( t );
        }
        return ( Exception ) t;
    }
    @Override
    public Object getPage() {
        return servlet;
    }
    private final String getAbsolutePathRelativeToContext ( String relativeUrlPath ) {
        String path = relativeUrlPath;
        if ( !path.startsWith ( "/" ) ) {
            String uri = ( String ) request.getAttribute (
                             RequestDispatcher.INCLUDE_SERVLET_PATH );
            if ( uri == null ) {
                uri = ( ( HttpServletRequest ) request ).getServletPath();
            }
            String baseURI = uri.substring ( 0, uri.lastIndexOf ( '/' ) );
            path = baseURI + '/' + path;
        }
        return path;
    }
    @Override
    public void include ( String relativeUrlPath ) throws ServletException,
        IOException {
        JspRuntimeLibrary
        .include ( request, response, relativeUrlPath, out, true );
    }
    @Override
    public void include ( final String relativeUrlPath, final boolean flush )
    throws ServletException, IOException {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                AccessController.doPrivileged (
                new PrivilegedExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        doInclude ( relativeUrlPath, flush );
                        return null;
                    }
                } );
            } catch ( PrivilegedActionException e ) {
                Exception ex = e.getException();
                if ( ex instanceof IOException ) {
                    throw ( IOException ) ex;
                } else {
                    throw ( ServletException ) ex;
                }
            }
        } else {
            doInclude ( relativeUrlPath, flush );
        }
    }
    private void doInclude ( String relativeUrlPath, boolean flush )
    throws ServletException, IOException {
        JspRuntimeLibrary.include ( request, response, relativeUrlPath, out,
                                    flush );
    }
    @Override
    @Deprecated
    public javax.servlet.jsp.el.VariableResolver getVariableResolver() {
        return new org.apache.jasper.el.VariableResolverImpl (
                   this.getELContext() );
    }
    @Override
    public void forward ( final String relativeUrlPath ) throws ServletException,
        IOException {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                AccessController.doPrivileged (
                new PrivilegedExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        doForward ( relativeUrlPath );
                        return null;
                    }
                } );
            } catch ( PrivilegedActionException e ) {
                Exception ex = e.getException();
                if ( ex instanceof IOException ) {
                    throw ( IOException ) ex;
                } else {
                    throw ( ServletException ) ex;
                }
            }
        } else {
            doForward ( relativeUrlPath );
        }
    }
    private void doForward ( String relativeUrlPath ) throws ServletException,
        IOException {
        try {
            out.clear();
            baseOut.clear();
        } catch ( IOException ex ) {
            IllegalStateException ise = new IllegalStateException ( Localizer
                    .getMessage ( "jsp.error.attempt_to_clear_flushed_buffer" ) );
            ise.initCause ( ex );
            throw ise;
        }
        while ( response instanceof ServletResponseWrapperInclude ) {
            response = ( ( ServletResponseWrapperInclude ) response ).getResponse();
        }
        final String path = getAbsolutePathRelativeToContext ( relativeUrlPath );
        String includeUri = ( String ) request.getAttribute (
                                RequestDispatcher.INCLUDE_SERVLET_PATH );
        if ( includeUri != null ) {
            request.removeAttribute ( RequestDispatcher.INCLUDE_SERVLET_PATH );
        }
        try {
            context.getRequestDispatcher ( path ).forward ( request, response );
        } finally {
            if ( includeUri != null )
                request.setAttribute ( RequestDispatcher.INCLUDE_SERVLET_PATH,
                                       includeUri );
        }
    }
    @Override
    public BodyContent pushBody() {
        return ( BodyContent ) pushBody ( null );
    }
    @Override
    public JspWriter pushBody ( Writer writer ) {
        depth++;
        if ( depth >= outs.length ) {
            BodyContentImpl[] newOuts = new BodyContentImpl[depth + 1];
            for ( int i = 0; i < outs.length; i++ ) {
                newOuts[i] = outs[i];
            }
            newOuts[depth] = new BodyContentImpl ( out );
            outs = newOuts;
        }
        outs[depth].setWriter ( writer );
        out = outs[depth];
        setAttribute ( OUT, out );
        return outs[depth];
    }
    @Override
    public JspWriter popBody() {
        depth--;
        if ( depth >= 0 ) {
            out = outs[depth];
        } else {
            out = baseOut;
        }
        setAttribute ( OUT, out );
        return out;
    }
    @Override
    @Deprecated
    public javax.servlet.jsp.el.ExpressionEvaluator getExpressionEvaluator() {
        return new org.apache.jasper.el.ExpressionEvaluatorImpl (
                   this.applicationContext.getExpressionFactory() );
    }
    @Override
    public void handlePageException ( Exception ex ) throws IOException,
        ServletException {
        handlePageException ( ( Throwable ) ex );
    }
    @Override
    public void handlePageException ( final Throwable t ) throws IOException,
        ServletException {
        if ( t == null ) {
            throw new NullPointerException ( "null Throwable" );
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                AccessController.doPrivileged (
                new PrivilegedExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        doHandlePageException ( t );
                        return null;
                    }
                } );
            } catch ( PrivilegedActionException e ) {
                Exception ex = e.getException();
                if ( ex instanceof IOException ) {
                    throw ( IOException ) ex;
                } else {
                    throw ( ServletException ) ex;
                }
            }
        } else {
            doHandlePageException ( t );
        }
    }
    @SuppressWarnings ( "deprecation" )
    private void doHandlePageException ( Throwable t ) throws IOException,
        ServletException {
        if ( errorPageURL != null && !errorPageURL.equals ( "" ) ) {
            request.setAttribute ( PageContext.EXCEPTION, t );
            request.setAttribute ( RequestDispatcher.ERROR_STATUS_CODE,
                                   Integer.valueOf ( HttpServletResponse.SC_INTERNAL_SERVER_ERROR ) );
            request.setAttribute ( RequestDispatcher.ERROR_REQUEST_URI,
                                   ( ( HttpServletRequest ) request ).getRequestURI() );
            request.setAttribute ( RequestDispatcher.ERROR_SERVLET_NAME,
                                   config.getServletName() );
            try {
                forward ( errorPageURL );
            } catch ( IllegalStateException ise ) {
                include ( errorPageURL );
            }
            Object newException =
                request.getAttribute ( RequestDispatcher.ERROR_EXCEPTION );
            if ( ( newException != null ) && ( newException == t ) ) {
                request.removeAttribute ( RequestDispatcher.ERROR_EXCEPTION );
            }
            request.removeAttribute ( RequestDispatcher.ERROR_STATUS_CODE );
            request.removeAttribute ( RequestDispatcher.ERROR_REQUEST_URI );
            request.removeAttribute ( RequestDispatcher.ERROR_SERVLET_NAME );
            request.removeAttribute ( PageContext.EXCEPTION );
        } else {
            if ( t instanceof IOException ) {
                throw ( IOException ) t;
            }
            if ( t instanceof ServletException ) {
                throw ( ServletException ) t;
            }
            if ( t instanceof RuntimeException ) {
                throw ( RuntimeException ) t;
            }
            Throwable rootCause = null;
            if ( t instanceof JspException || t instanceof ELException ||
                    t instanceof javax.servlet.jsp.el.ELException ) {
                rootCause = t.getCause();
            }
            if ( rootCause != null ) {
                throw new ServletException ( t.getClass().getName() + ": "
                                             + t.getMessage(), rootCause );
            }
            throw new ServletException ( t );
        }
    }
    public static Object proprietaryEvaluate ( final String expression,
            final Class<?> expectedType, final PageContext pageContext,
            final ProtectedFunctionMapper functionMap )
    throws ELException {
        final ExpressionFactory exprFactory = jspf.getJspApplicationContext ( pageContext.getServletContext() ).getExpressionFactory();
        ELContext ctx = pageContext.getELContext();
        ELContextImpl ctxImpl;
        if ( ctx instanceof ELContextWrapper ) {
            ctxImpl = ( ELContextImpl ) ( ( ELContextWrapper ) ctx ).getWrappedELContext();
        } else {
            ctxImpl = ( ELContextImpl ) ctx;
        }
        ctxImpl.setFunctionMapper ( functionMap );
        ValueExpression ve = exprFactory.createValueExpression ( ctx, expression, expectedType );
        return ve.getValue ( ctx );
    }
    @Override
    public ELContext getELContext() {
        if ( elContext == null ) {
            elContext = applicationContext.createELContext ( this );
            if ( servlet instanceof JspSourceImports ) {
                ImportHandler ih = elContext.getImportHandler();
                Set<String> packageImports = ( ( JspSourceImports ) servlet ).getPackageImports();
                if ( packageImports != null ) {
                    for ( String packageImport : packageImports ) {
                        ih.importPackage ( packageImport );
                    }
                }
                Set<String> classImports = ( ( JspSourceImports ) servlet ).getClassImports();
                if ( classImports != null ) {
                    for ( String classImport : classImports ) {
                        ih.importClass ( classImport );
                    }
                }
            }
        }
        return this.elContext;
    }
}
