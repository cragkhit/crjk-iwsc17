package org.apache.jasper.servlet;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.security.PrivilegedActionException;
import java.security.AccessController;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.security.PrivilegedExceptionAction;
import org.apache.jasper.security.SecurityUtil;
import java.net.MalformedURLException;
import javax.servlet.ServletException;
import org.apache.jasper.EmbeddedServletOptions;
import org.apache.jasper.runtime.ExceptionUtils;
import org.apache.jasper.compiler.Localizer;
import org.apache.jasper.Constants;
import org.apache.juli.logging.LogFactory;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.apache.jasper.Options;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import org.apache.juli.logging.Log;
import org.apache.tomcat.PeriodicEventListener;
import javax.servlet.http.HttpServlet;
public class JspServlet extends HttpServlet implements PeriodicEventListener {
    private static final long serialVersionUID = 1L;
    private final transient Log log;
    private transient ServletContext context;
    private ServletConfig config;
    private transient Options options;
    private transient JspRuntimeContext rctxt;
    private String jspFile;
    public JspServlet() {
        this.log = LogFactory.getLog ( JspServlet.class );
    }
    public void init ( final ServletConfig config ) throws ServletException {
        super.init ( config );
        this.config = config;
        this.context = config.getServletContext();
        String engineOptionsName = config.getInitParameter ( "engineOptionsClass" );
        if ( Constants.IS_SECURITY_ENABLED && engineOptionsName != null ) {
            this.log.info ( Localizer.getMessage ( "jsp.info.ignoreSetting", "engineOptionsClass", engineOptionsName ) );
            engineOptionsName = null;
        }
        if ( engineOptionsName != null ) {
            try {
                final ClassLoader loader = Thread.currentThread().getContextClassLoader();
                final Class<?> engineOptionsClass = loader.loadClass ( engineOptionsName );
                final Class<?>[] ctorSig = ( Class<?>[] ) new Class[] { ServletConfig.class, ServletContext.class };
                final Constructor<?> ctor = engineOptionsClass.getConstructor ( ctorSig );
                final Object[] args = { config, this.context };
                this.options = ( Options ) ctor.newInstance ( args );
            } catch ( Throwable e ) {
                e = ExceptionUtils.unwrapInvocationTargetException ( e );
                ExceptionUtils.handleThrowable ( e );
                this.log.warn ( "Failed to load engineOptionsClass", e );
                this.options = new EmbeddedServletOptions ( config, this.context );
            }
        } else {
            this.options = new EmbeddedServletOptions ( config, this.context );
        }
        this.rctxt = new JspRuntimeContext ( this.context, this.options );
        if ( config.getInitParameter ( "jspFile" ) != null ) {
            this.jspFile = config.getInitParameter ( "jspFile" );
            try {
                if ( null == this.context.getResource ( this.jspFile ) ) {
                    return;
                }
            } catch ( MalformedURLException e2 ) {
                throw new ServletException ( "cannot locate jsp file", ( Throwable ) e2 );
            }
            try {
                if ( SecurityUtil.isPackageProtectionEnabled() ) {
                    AccessController.doPrivileged ( ( PrivilegedExceptionAction<Object> ) new PrivilegedExceptionAction<Object>() {
                        @Override
                        public Object run() throws IOException, ServletException {
                            JspServlet.this.serviceJspFile ( null, null, JspServlet.this.jspFile, true );
                            return null;
                        }
                    } );
                } else {
                    this.serviceJspFile ( null, null, this.jspFile, true );
                }
            } catch ( IOException e3 ) {
                throw new ServletException ( "Could not precompile jsp: " + this.jspFile, ( Throwable ) e3 );
            } catch ( PrivilegedActionException e4 ) {
                final Throwable t = e4.getCause();
                if ( t instanceof ServletException ) {
                    throw ( ServletException ) t;
                }
                throw new ServletException ( "Could not precompile jsp: " + this.jspFile, ( Throwable ) e4 );
            }
        }
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( Localizer.getMessage ( "jsp.message.scratch.dir.is", this.options.getScratchDir().toString() ) );
            this.log.debug ( Localizer.getMessage ( "jsp.message.dont.modify.servlets" ) );
        }
    }
    public int getJspCount() {
        return this.rctxt.getJspCount();
    }
    public void setJspReloadCount ( final int count ) {
        this.rctxt.setJspReloadCount ( count );
    }
    public int getJspReloadCount() {
        return this.rctxt.getJspReloadCount();
    }
    public int getJspQueueLength() {
        return this.rctxt.getJspQueueLength();
    }
    public int getJspUnloadCount() {
        return this.rctxt.getJspUnloadCount();
    }
    boolean preCompile ( final HttpServletRequest request ) throws ServletException {
        String queryString = request.getQueryString();
        if ( queryString == null ) {
            return false;
        }
        final int start = queryString.indexOf ( Constants.PRECOMPILE );
        if ( start < 0 ) {
            return false;
        }
        queryString = queryString.substring ( start + Constants.PRECOMPILE.length() );
        if ( queryString.length() == 0 ) {
            return true;
        }
        if ( queryString.startsWith ( "&" ) ) {
            return true;
        }
        if ( !queryString.startsWith ( "=" ) ) {
            return false;
        }
        int limit = queryString.length();
        final int ampersand = queryString.indexOf ( 38 );
        if ( ampersand > 0 ) {
            limit = ampersand;
        }
        final String value = queryString.substring ( 1, limit );
        if ( value.equals ( "true" ) ) {
            return true;
        }
        if ( value.equals ( "false" ) ) {
            return true;
        }
        throw new ServletException ( "Cannot have request parameter " + Constants.PRECOMPILE + " set to " + value );
    }
    public void service ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException {
        String jspUri = this.jspFile;
        if ( jspUri == null ) {
            jspUri = ( String ) request.getAttribute ( "javax.servlet.include.servlet_path" );
            if ( jspUri != null ) {
                final String pathInfo = ( String ) request.getAttribute ( "javax.servlet.include.path_info" );
                if ( pathInfo != null ) {
                    jspUri += pathInfo;
                }
            } else {
                jspUri = request.getServletPath();
                final String pathInfo = request.getPathInfo();
                if ( pathInfo != null ) {
                    jspUri += pathInfo;
                }
            }
        }
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( "JspEngine --> " + jspUri );
            this.log.debug ( "\t     ServletPath: " + request.getServletPath() );
            this.log.debug ( "\t        PathInfo: " + request.getPathInfo() );
            this.log.debug ( "\t        RealPath: " + this.context.getRealPath ( jspUri ) );
            this.log.debug ( "\t      RequestURI: " + request.getRequestURI() );
            this.log.debug ( "\t     QueryString: " + request.getQueryString() );
        }
        try {
            final boolean precompile = this.preCompile ( request );
            this.serviceJspFile ( request, response, jspUri, precompile );
        } catch ( RuntimeException e ) {
            throw e;
        } catch ( ServletException e2 ) {
            throw e2;
        } catch ( IOException e3 ) {
            throw e3;
        } catch ( Throwable e4 ) {
            ExceptionUtils.handleThrowable ( e4 );
            throw new ServletException ( e4 );
        }
    }
    public void destroy() {
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( "JspServlet.destroy()" );
        }
        this.rctxt.destroy();
    }
    public void periodicEvent() {
        this.rctxt.checkUnload();
        this.rctxt.checkCompile();
    }
    private void serviceJspFile ( final HttpServletRequest request, final HttpServletResponse response, final String jspUri, final boolean precompile ) throws ServletException, IOException {
        JspServletWrapper wrapper = this.rctxt.getWrapper ( jspUri );
        if ( wrapper == null ) {
            synchronized ( this ) {
                wrapper = this.rctxt.getWrapper ( jspUri );
                if ( wrapper == null ) {
                    if ( null == this.context.getResource ( jspUri ) ) {
                        this.handleMissingResource ( request, response, jspUri );
                        return;
                    }
                    wrapper = new JspServletWrapper ( this.config, this.options, jspUri, this.rctxt );
                    this.rctxt.addWrapper ( jspUri, wrapper );
                }
            }
        }
        try {
            wrapper.service ( request, response, precompile );
        } catch ( FileNotFoundException fnfe ) {
            this.handleMissingResource ( request, response, jspUri );
        }
    }
    private void handleMissingResource ( final HttpServletRequest request, final HttpServletResponse response, final String jspUri ) throws ServletException, IOException {
        final String includeRequestUri = ( String ) request.getAttribute ( "javax.servlet.include.request_uri" );
        if ( includeRequestUri != null ) {
            final String msg = Localizer.getMessage ( "jsp.error.file.not.found", jspUri );
            throw new ServletException ( SecurityUtil.filter ( msg ) );
        }
        try {
            response.sendError ( 404, request.getRequestURI() );
        } catch ( IllegalStateException ise ) {
            this.log.error ( Localizer.getMessage ( "jsp.error.file.not.found", jspUri ) );
        }
    }
}
