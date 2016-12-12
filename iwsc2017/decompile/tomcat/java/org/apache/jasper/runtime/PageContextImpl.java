package org.apache.jasper.runtime;
import java.util.Iterator;
import java.util.Set;
import javax.el.ImportHandler;
import javax.servlet.jsp.JspContext;
import javax.el.ValueExpression;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ELException;
import org.apache.jasper.el.ExpressionEvaluatorImpl;
import javax.servlet.jsp.el.ExpressionEvaluator;
import java.io.Writer;
import javax.servlet.jsp.tagext.BodyContent;
import org.apache.jasper.el.VariableResolverImpl;
import javax.servlet.jsp.el.VariableResolver;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.jasper.security.SecurityUtil;
import org.apache.jasper.compiler.Localizer;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import org.apache.jasper.el.ELContextImpl;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.Servlet;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
public class PageContextImpl extends PageContext {
    private static final JspFactory jspf;
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
        this.attributes = new HashMap<String, Object> ( 16 );
        this.depth = -1;
    }
    public void initialize ( final Servlet servlet, final ServletRequest request, final ServletResponse response, final String errorPageURL, final boolean needsSession, int bufferSize, final boolean autoFlush ) throws IOException {
        this.servlet = servlet;
        this.config = servlet.getServletConfig();
        this.context = this.config.getServletContext();
        this.errorPageURL = errorPageURL;
        this.request = request;
        this.response = response;
        this.applicationContext = JspApplicationContextImpl.getInstance ( this.context );
        if ( request instanceof HttpServletRequest && needsSession ) {
            this.session = ( ( HttpServletRequest ) request ).getSession();
        }
        if ( needsSession && this.session == null ) {
            throw new IllegalStateException ( "Page needs a session and none is available" );
        }
        this.depth = -1;
        if ( bufferSize == -1 ) {
            bufferSize = 8192;
        }
        if ( this.baseOut == null ) {
            this.baseOut = new JspWriterImpl ( response, bufferSize, autoFlush );
        } else {
            this.baseOut.init ( response, bufferSize, autoFlush );
        }
        this.setAttribute ( "javax.servlet.jsp.jspOut", this.out = this.baseOut );
        this.setAttribute ( "javax.servlet.jsp.jspRequest", request );
        this.setAttribute ( "javax.servlet.jsp.jspResponse", response );
        if ( this.session != null ) {
            this.setAttribute ( "javax.servlet.jsp.jspSession", this.session );
        }
        this.setAttribute ( "javax.servlet.jsp.jspPage", servlet );
        this.setAttribute ( "javax.servlet.jsp.jspConfig", this.config );
        this.setAttribute ( "javax.servlet.jsp.jspPageContext", this );
        this.setAttribute ( "javax.servlet.jsp.jspApplication", this.context );
        this.isIncluded = ( request.getAttribute ( "javax.servlet.include.servlet_path" ) != null );
    }
    public void release() {
        this.out = this.baseOut;
        try {
            if ( this.isIncluded ) {
                ( ( JspWriterImpl ) this.out ).flushBuffer();
            } else {
                ( ( JspWriterImpl ) this.out ).flushBuffer();
            }
        } catch ( IOException ex ) {
            final IllegalStateException ise = new IllegalStateException ( Localizer.getMessage ( "jsp.error.flush" ), ex );
            throw ise;
        } finally {
            this.servlet = null;
            this.config = null;
            this.context = null;
            this.applicationContext = null;
            this.elContext = null;
            this.errorPageURL = null;
            this.request = null;
            this.response = null;
            this.depth = -1;
            this.baseOut.recycle();
            this.session = null;
            this.attributes.clear();
            for ( final BodyContentImpl body : this.outs ) {
                body.recycle();
            }
        }
    }
    public Object getAttribute ( final String name ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer.getMessage ( "jsp.error.attribute.null_name" ) );
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<Object> ) new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    return PageContextImpl.this.doGetAttribute ( name );
                }
            } );
        }
        return this.doGetAttribute ( name );
    }
    private Object doGetAttribute ( final String name ) {
        return this.attributes.get ( name );
    }
    public Object getAttribute ( final String name, final int scope ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer.getMessage ( "jsp.error.attribute.null_name" ) );
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<Object> ) new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    return PageContextImpl.this.doGetAttribute ( name, scope );
                }
            } );
        }
        return this.doGetAttribute ( name, scope );
    }
    private Object doGetAttribute ( final String name, final int scope ) {
        switch ( scope ) {
        case 1: {
            return this.attributes.get ( name );
        }
        case 2: {
            return this.request.getAttribute ( name );
        }
        case 3: {
            if ( this.session == null ) {
                throw new IllegalStateException ( Localizer.getMessage ( "jsp.error.page.noSession" ) );
            }
            return this.session.getAttribute ( name );
        }
        case 4: {
            return this.context.getAttribute ( name );
        }
        default: {
            throw new IllegalArgumentException ( "Invalid scope" );
        }
        }
    }
    public void setAttribute ( final String name, final Object attribute ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer.getMessage ( "jsp.error.attribute.null_name" ) );
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            AccessController.doPrivileged ( ( PrivilegedAction<Object> ) new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    PageContextImpl.this.doSetAttribute ( name, attribute );
                    return null;
                }
            } );
        } else {
            this.doSetAttribute ( name, attribute );
        }
    }
    private void doSetAttribute ( final String name, final Object attribute ) {
        if ( attribute != null ) {
            this.attributes.put ( name, attribute );
        } else {
            this.removeAttribute ( name, 1 );
        }
    }
    public void setAttribute ( final String name, final Object o, final int scope ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer.getMessage ( "jsp.error.attribute.null_name" ) );
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            AccessController.doPrivileged ( ( PrivilegedAction<Object> ) new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    PageContextImpl.this.doSetAttribute ( name, o, scope );
                    return null;
                }
            } );
        } else {
            this.doSetAttribute ( name, o, scope );
        }
    }
    private void doSetAttribute ( final String name, final Object o, final int scope ) {
        if ( o != null ) {
            switch ( scope ) {
            case 1: {
                this.attributes.put ( name, o );
                break;
            }
            case 2: {
                this.request.setAttribute ( name, o );
                break;
            }
            case 3: {
                if ( this.session == null ) {
                    throw new IllegalStateException ( Localizer.getMessage ( "jsp.error.page.noSession" ) );
                }
                this.session.setAttribute ( name, o );
                break;
            }
            case 4: {
                this.context.setAttribute ( name, o );
                break;
            }
            default: {
                throw new IllegalArgumentException ( "Invalid scope" );
            }
            }
        } else {
            this.removeAttribute ( name, scope );
        }
    }
    public void removeAttribute ( final String name, final int scope ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer.getMessage ( "jsp.error.attribute.null_name" ) );
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            AccessController.doPrivileged ( ( PrivilegedAction<Object> ) new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    PageContextImpl.this.doRemoveAttribute ( name, scope );
                    return null;
                }
            } );
        } else {
            this.doRemoveAttribute ( name, scope );
        }
    }
    private void doRemoveAttribute ( final String name, final int scope ) {
        switch ( scope ) {
        case 1: {
            this.attributes.remove ( name );
            break;
        }
        case 2: {
            this.request.removeAttribute ( name );
            break;
        }
        case 3: {
            if ( this.session == null ) {
                throw new IllegalStateException ( Localizer.getMessage ( "jsp.error.page.noSession" ) );
            }
            this.session.removeAttribute ( name );
            break;
        }
        case 4: {
            this.context.removeAttribute ( name );
            break;
        }
        default: {
            throw new IllegalArgumentException ( "Invalid scope" );
        }
        }
    }
    public int getAttributesScope ( final String name ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer.getMessage ( "jsp.error.attribute.null_name" ) );
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<Integer> ) new PrivilegedAction<Integer>() {
                @Override
                public Integer run() {
                    return PageContextImpl.this.doGetAttributeScope ( name );
                }
            } );
        }
        return this.doGetAttributeScope ( name );
    }
    private int doGetAttributeScope ( final String name ) {
        if ( this.attributes.get ( name ) != null ) {
            return 1;
        }
        if ( this.request.getAttribute ( name ) != null ) {
            return 2;
        }
        if ( this.session != null ) {
            try {
                if ( this.session.getAttribute ( name ) != null ) {
                    return 3;
                }
            } catch ( IllegalStateException ex ) {}
        }
        if ( this.context.getAttribute ( name ) != null ) {
            return 4;
        }
        return 0;
    }
    public Object findAttribute ( final String name ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<Object> ) new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    if ( name == null ) {
                        throw new NullPointerException ( Localizer.getMessage ( "jsp.error.attribute.null_name" ) );
                    }
                    return PageContextImpl.this.doFindAttribute ( name );
                }
            } );
        }
        if ( name == null ) {
            throw new NullPointerException ( Localizer.getMessage ( "jsp.error.attribute.null_name" ) );
        }
        return this.doFindAttribute ( name );
    }
    private Object doFindAttribute ( final String name ) {
        Object o = this.attributes.get ( name );
        if ( o != null ) {
            return o;
        }
        o = this.request.getAttribute ( name );
        if ( o != null ) {
            return o;
        }
        if ( this.session != null ) {
            try {
                o = this.session.getAttribute ( name );
            } catch ( IllegalStateException ex ) {}
            if ( o != null ) {
                return o;
            }
        }
        return this.context.getAttribute ( name );
    }
    public Enumeration<String> getAttributeNamesInScope ( final int scope ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<Enumeration<String>> ) new PrivilegedAction<Enumeration<String>>() {
                @Override
                public Enumeration<String> run() {
                    return PageContextImpl.this.doGetAttributeNamesInScope ( scope );
                }
            } );
        }
        return this.doGetAttributeNamesInScope ( scope );
    }
    private Enumeration<String> doGetAttributeNamesInScope ( final int scope ) {
        switch ( scope ) {
        case 1: {
            return Collections.enumeration ( this.attributes.keySet() );
        }
        case 2: {
            return ( Enumeration<String> ) this.request.getAttributeNames();
        }
        case 3: {
            if ( this.session == null ) {
                throw new IllegalStateException ( Localizer.getMessage ( "jsp.error.page.noSession" ) );
            }
            return ( Enumeration<String> ) this.session.getAttributeNames();
        }
        case 4: {
            return ( Enumeration<String> ) this.context.getAttributeNames();
        }
        default: {
            throw new IllegalArgumentException ( "Invalid scope" );
        }
        }
    }
    public void removeAttribute ( final String name ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer.getMessage ( "jsp.error.attribute.null_name" ) );
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            AccessController.doPrivileged ( ( PrivilegedAction<Object> ) new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    PageContextImpl.this.doRemoveAttribute ( name );
                    return null;
                }
            } );
        } else {
            this.doRemoveAttribute ( name );
        }
    }
    private void doRemoveAttribute ( final String name ) {
        this.removeAttribute ( name, 1 );
        this.removeAttribute ( name, 2 );
        if ( this.session != null ) {
            try {
                this.removeAttribute ( name, 3 );
            } catch ( IllegalStateException ex ) {}
        }
        this.removeAttribute ( name, 4 );
    }
    public JspWriter getOut() {
        return this.out;
    }
    public HttpSession getSession() {
        return this.session;
    }
    public ServletConfig getServletConfig() {
        return this.config;
    }
    public ServletContext getServletContext() {
        return this.config.getServletContext();
    }
    public ServletRequest getRequest() {
        return this.request;
    }
    public ServletResponse getResponse() {
        return this.response;
    }
    public Exception getException() {
        Throwable t = JspRuntimeLibrary.getThrowable ( this.request );
        if ( t != null && ! ( t instanceof Exception ) ) {
            t = ( Throwable ) new JspException ( t );
        }
        return ( Exception ) t;
    }
    public Object getPage() {
        return this.servlet;
    }
    private final String getAbsolutePathRelativeToContext ( final String relativeUrlPath ) {
        String path = relativeUrlPath;
        if ( !path.startsWith ( "/" ) ) {
            String uri = ( String ) this.request.getAttribute ( "javax.servlet.include.servlet_path" );
            if ( uri == null ) {
                uri = ( ( HttpServletRequest ) this.request ).getServletPath();
            }
            final String baseURI = uri.substring ( 0, uri.lastIndexOf ( 47 ) );
            path = baseURI + '/' + path;
        }
        return path;
    }
    public void include ( final String relativeUrlPath ) throws ServletException, IOException {
        JspRuntimeLibrary.include ( this.request, this.response, relativeUrlPath, this.out, true );
    }
    public void include ( final String relativeUrlPath, final boolean flush ) throws ServletException, IOException {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                AccessController.doPrivileged ( ( PrivilegedExceptionAction<Object> ) new PrivilegedExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        PageContextImpl.this.doInclude ( relativeUrlPath, flush );
                        return null;
                    }
                } );
                return;
            } catch ( PrivilegedActionException e ) {
                final Exception ex = e.getException();
                if ( ex instanceof IOException ) {
                    throw ( IOException ) ex;
                }
                throw ( ServletException ) ex;
            }
        }
        this.doInclude ( relativeUrlPath, flush );
    }
    private void doInclude ( final String relativeUrlPath, final boolean flush ) throws ServletException, IOException {
        JspRuntimeLibrary.include ( this.request, this.response, relativeUrlPath, this.out, flush );
    }
    @Deprecated
    public VariableResolver getVariableResolver() {
        return ( VariableResolver ) new VariableResolverImpl ( this.getELContext() );
    }
    public void forward ( final String relativeUrlPath ) throws ServletException, IOException {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                AccessController.doPrivileged ( ( PrivilegedExceptionAction<Object> ) new PrivilegedExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        PageContextImpl.this.doForward ( relativeUrlPath );
                        return null;
                    }
                } );
                return;
            } catch ( PrivilegedActionException e ) {
                final Exception ex = e.getException();
                if ( ex instanceof IOException ) {
                    throw ( IOException ) ex;
                }
                throw ( ServletException ) ex;
            }
        }
        this.doForward ( relativeUrlPath );
    }
    private void doForward ( final String relativeUrlPath ) throws ServletException, IOException {
        try {
            this.out.clear();
            this.baseOut.clear();
        } catch ( IOException ex ) {
            final IllegalStateException ise = new IllegalStateException ( Localizer.getMessage ( "jsp.error.attempt_to_clear_flushed_buffer" ) );
            ise.initCause ( ex );
            throw ise;
        }
        while ( this.response instanceof ServletResponseWrapperInclude ) {
            this.response = ( ( ServletResponseWrapperInclude ) this.response ).getResponse();
        }
        final String path = this.getAbsolutePathRelativeToContext ( relativeUrlPath );
        final String includeUri = ( String ) this.request.getAttribute ( "javax.servlet.include.servlet_path" );
        if ( includeUri != null ) {
            this.request.removeAttribute ( "javax.servlet.include.servlet_path" );
        }
        try {
            this.context.getRequestDispatcher ( path ).forward ( this.request, this.response );
        } finally {
            if ( includeUri != null ) {
                this.request.setAttribute ( "javax.servlet.include.servlet_path", ( Object ) includeUri );
            }
        }
    }
    public BodyContent pushBody() {
        return ( BodyContent ) this.pushBody ( null );
    }
    public JspWriter pushBody ( final Writer writer ) {
        ++this.depth;
        if ( this.depth >= this.outs.length ) {
            final BodyContentImpl[] newOuts = new BodyContentImpl[this.depth + 1];
            for ( int i = 0; i < this.outs.length; ++i ) {
                newOuts[i] = this.outs[i];
            }
            newOuts[this.depth] = new BodyContentImpl ( this.out );
            this.outs = newOuts;
        }
        this.outs[this.depth].setWriter ( writer );
        this.setAttribute ( "javax.servlet.jsp.jspOut", this.out = ( JspWriter ) this.outs[this.depth] );
        return ( JspWriter ) this.outs[this.depth];
    }
    public JspWriter popBody() {
        --this.depth;
        if ( this.depth >= 0 ) {
            this.out = ( JspWriter ) this.outs[this.depth];
        } else {
            this.out = this.baseOut;
        }
        this.setAttribute ( "javax.servlet.jsp.jspOut", this.out );
        return this.out;
    }
    @Deprecated
    public ExpressionEvaluator getExpressionEvaluator() {
        return new ExpressionEvaluatorImpl ( this.applicationContext.getExpressionFactory() );
    }
    public void handlePageException ( final Exception ex ) throws IOException, ServletException {
        this.handlePageException ( ( Throwable ) ex );
    }
    public void handlePageException ( final Throwable t ) throws IOException, ServletException {
        if ( t == null ) {
            throw new NullPointerException ( "null Throwable" );
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                AccessController.doPrivileged ( ( PrivilegedExceptionAction<Object> ) new PrivilegedExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        PageContextImpl.this.doHandlePageException ( t );
                        return null;
                    }
                } );
                return;
            } catch ( PrivilegedActionException e ) {
                final Exception ex = e.getException();
                if ( ex instanceof IOException ) {
                    throw ( IOException ) ex;
                }
                throw ( ServletException ) ex;
            }
        }
        this.doHandlePageException ( t );
    }
    private void doHandlePageException ( final Throwable t ) throws IOException, ServletException {
        if ( this.errorPageURL != null && !this.errorPageURL.equals ( "" ) ) {
            this.request.setAttribute ( "javax.servlet.jsp.jspException", ( Object ) t );
            this.request.setAttribute ( "javax.servlet.error.status_code", ( Object ) 500 );
            this.request.setAttribute ( "javax.servlet.error.request_uri", ( Object ) ( ( HttpServletRequest ) this.request ).getRequestURI() );
            this.request.setAttribute ( "javax.servlet.error.servlet_name", ( Object ) this.config.getServletName() );
            try {
                this.forward ( this.errorPageURL );
            } catch ( IllegalStateException ise ) {
                this.include ( this.errorPageURL );
            }
            final Object newException = this.request.getAttribute ( "javax.servlet.error.exception" );
            if ( newException != null && newException == t ) {
                this.request.removeAttribute ( "javax.servlet.error.exception" );
            }
            this.request.removeAttribute ( "javax.servlet.error.status_code" );
            this.request.removeAttribute ( "javax.servlet.error.request_uri" );
            this.request.removeAttribute ( "javax.servlet.error.servlet_name" );
            this.request.removeAttribute ( "javax.servlet.jsp.jspException" );
            return;
        }
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
        if ( t instanceof JspException || t instanceof ELException || t instanceof javax.servlet.jsp.el.ELException ) {
            rootCause = t.getCause();
        }
        if ( rootCause != null ) {
            throw new ServletException ( t.getClass().getName() + ": " + t.getMessage(), rootCause );
        }
        throw new ServletException ( t );
    }
    public static Object proprietaryEvaluate ( final String expression, final Class<?> expectedType, final PageContext pageContext, final ProtectedFunctionMapper functionMap ) throws ELException {
        final ExpressionFactory exprFactory = PageContextImpl.jspf.getJspApplicationContext ( pageContext.getServletContext() ).getExpressionFactory();
        final ELContext ctx = pageContext.getELContext();
        ELContextImpl ctxImpl;
        if ( ctx instanceof JspContextWrapper.ELContextWrapper ) {
            ctxImpl = ( ELContextImpl ) ( ( JspContextWrapper.ELContextWrapper ) ctx ).getWrappedELContext();
        } else {
            ctxImpl = ( ELContextImpl ) ctx;
        }
        ctxImpl.setFunctionMapper ( functionMap );
        final ValueExpression ve = exprFactory.createValueExpression ( ctx, expression, ( Class ) expectedType );
        return ve.getValue ( ctx );
    }
    public ELContext getELContext() {
        if ( this.elContext == null ) {
            this.elContext = this.applicationContext.createELContext ( ( JspContext ) this );
            if ( this.servlet instanceof JspSourceImports ) {
                final ImportHandler ih = this.elContext.getImportHandler();
                final Set<String> packageImports = ( ( JspSourceImports ) this.servlet ).getPackageImports();
                if ( packageImports != null ) {
                    for ( final String packageImport : packageImports ) {
                        ih.importPackage ( packageImport );
                    }
                }
                final Set<String> classImports = ( ( JspSourceImports ) this.servlet ).getClassImports();
                if ( classImports != null ) {
                    for ( final String classImport : classImports ) {
                        ih.importClass ( classImport );
                    }
                }
            }
        }
        return this.elContext;
    }
    static {
        jspf = JspFactory.getDefaultFactory();
    }
}
