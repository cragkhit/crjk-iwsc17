package org.apache.jasper.servlet;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.jasper.Constants;
import org.apache.jasper.EmbeddedServletOptions;
import org.apache.jasper.Options;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.apache.jasper.compiler.Localizer;
import org.apache.jasper.runtime.ExceptionUtils;
import org.apache.jasper.security.SecurityUtil;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.PeriodicEventListener;
public class JspServlet extends HttpServlet implements PeriodicEventListener {
    private static final long serialVersionUID = 1L;
    private final transient Log log = LogFactory.getLog ( JspServlet.class );
    private transient ServletContext context;
    private ServletConfig config;
    private transient Options options;
    private transient JspRuntimeContext rctxt;
    private String jspFile;
    @Override
    public void init ( ServletConfig config ) throws ServletException {
        super.init ( config );
        this.config = config;
        this.context = config.getServletContext();
        String engineOptionsName = config.getInitParameter ( "engineOptionsClass" );
        if ( Constants.IS_SECURITY_ENABLED && engineOptionsName != null ) {
            log.info ( Localizer.getMessage (
                           "jsp.info.ignoreSetting", "engineOptionsClass", engineOptionsName ) );
            engineOptionsName = null;
        }
        if ( engineOptionsName != null ) {
            try {
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                Class<?> engineOptionsClass = loader.loadClass ( engineOptionsName );
                Class<?>[] ctorSig = { ServletConfig.class, ServletContext.class };
                Constructor<?> ctor = engineOptionsClass.getConstructor ( ctorSig );
                Object[] args = { config, context };
                options = ( Options ) ctor.newInstance ( args );
            } catch ( Throwable e ) {
                e = ExceptionUtils.unwrapInvocationTargetException ( e );
                ExceptionUtils.handleThrowable ( e );
                log.warn ( "Failed to load engineOptionsClass", e );
                options = new EmbeddedServletOptions ( config, context );
            }
        } else {
            options = new EmbeddedServletOptions ( config, context );
        }
        rctxt = new JspRuntimeContext ( context, options );
        if ( config.getInitParameter ( "jspFile" ) != null ) {
            jspFile = config.getInitParameter ( "jspFile" );
            try {
                if ( null == context.getResource ( jspFile ) ) {
                    return;
                }
            } catch ( MalformedURLException e ) {
                throw new ServletException ( "cannot locate jsp file", e );
            }
            try {
                if ( SecurityUtil.isPackageProtectionEnabled() ) {
                    AccessController.doPrivileged ( new PrivilegedExceptionAction<Object>() {
                        @Override
                        public Object run() throws IOException, ServletException {
                            serviceJspFile ( null, null, jspFile, true );
                            return null;
                        }
                    } );
                } else {
                    serviceJspFile ( null, null, jspFile, true );
                }
            } catch ( IOException e ) {
                throw new ServletException ( "Could not precompile jsp: " + jspFile, e );
            } catch ( PrivilegedActionException e ) {
                Throwable t = e.getCause();
                if ( t instanceof ServletException ) {
                    throw ( ServletException ) t;
                }
                throw new ServletException ( "Could not precompile jsp: " + jspFile, e );
            }
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( Localizer.getMessage ( "jsp.message.scratch.dir.is",
                                               options.getScratchDir().toString() ) );
            log.debug ( Localizer.getMessage ( "jsp.message.dont.modify.servlets" ) );
        }
    }
    public int getJspCount() {
        return this.rctxt.getJspCount();
    }
    public void setJspReloadCount ( int count ) {
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
    boolean preCompile ( HttpServletRequest request ) throws ServletException {
        String queryString = request.getQueryString();
        if ( queryString == null ) {
            return false;
        }
        int start = queryString.indexOf ( Constants.PRECOMPILE );
        if ( start < 0 ) {
            return false;
        }
        queryString =
            queryString.substring ( start + Constants.PRECOMPILE.length() );
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
        int ampersand = queryString.indexOf ( '&' );
        if ( ampersand > 0 ) {
            limit = ampersand;
        }
        String value = queryString.substring ( 1, limit );
        if ( value.equals ( "true" ) ) {
            return true;
        } else if ( value.equals ( "false" ) ) {
            return true;
        } else {
            throw new ServletException ( "Cannot have request parameter " +
                                         Constants.PRECOMPILE + " set to " +
                                         value );
        }
    }
    @Override
    public void service ( HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException {
        String jspUri = jspFile;
        if ( jspUri == null ) {
            jspUri = ( String ) request.getAttribute (
                         RequestDispatcher.INCLUDE_SERVLET_PATH );
            if ( jspUri != null ) {
                String pathInfo = ( String ) request.getAttribute (
                                      RequestDispatcher.INCLUDE_PATH_INFO );
                if ( pathInfo != null ) {
                    jspUri += pathInfo;
                }
            } else {
                jspUri = request.getServletPath();
                String pathInfo = request.getPathInfo();
                if ( pathInfo != null ) {
                    jspUri += pathInfo;
                }
            }
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( "JspEngine --> " + jspUri );
            log.debug ( "\t     ServletPath: " + request.getServletPath() );
            log.debug ( "\t        PathInfo: " + request.getPathInfo() );
            log.debug ( "\t        RealPath: " + context.getRealPath ( jspUri ) );
            log.debug ( "\t      RequestURI: " + request.getRequestURI() );
            log.debug ( "\t     QueryString: " + request.getQueryString() );
        }
        try {
            boolean precompile = preCompile ( request );
            serviceJspFile ( request, response, jspUri, precompile );
        } catch ( RuntimeException e ) {
            throw e;
        } catch ( ServletException e ) {
            throw e;
        } catch ( IOException e ) {
            throw e;
        } catch ( Throwable e ) {
            ExceptionUtils.handleThrowable ( e );
            throw new ServletException ( e );
        }
    }
    @Override
    public void destroy() {
        if ( log.isDebugEnabled() ) {
            log.debug ( "JspServlet.destroy()" );
        }
        rctxt.destroy();
    }
    @Override
    public void periodicEvent() {
        rctxt.checkUnload();
        rctxt.checkCompile();
    }
    private void serviceJspFile ( HttpServletRequest request,
                                  HttpServletResponse response, String jspUri,
                                  boolean precompile )
    throws ServletException, IOException {
        JspServletWrapper wrapper = rctxt.getWrapper ( jspUri );
        if ( wrapper == null ) {
            synchronized ( this ) {
                wrapper = rctxt.getWrapper ( jspUri );
                if ( wrapper == null ) {
                    if ( null == context.getResource ( jspUri ) ) {
                        handleMissingResource ( request, response, jspUri );
                        return;
                    }
                    wrapper = new JspServletWrapper ( config, options, jspUri,
                                                      rctxt );
                    rctxt.addWrapper ( jspUri, wrapper );
                }
            }
        }
        try {
            wrapper.service ( request, response, precompile );
        } catch ( FileNotFoundException fnfe ) {
            handleMissingResource ( request, response, jspUri );
        }
    }
    private void handleMissingResource ( HttpServletRequest request,
                                         HttpServletResponse response, String jspUri )
    throws ServletException, IOException {
        String includeRequestUri =
            ( String ) request.getAttribute ( RequestDispatcher.INCLUDE_REQUEST_URI );
        if ( includeRequestUri != null ) {
            String msg =
                Localizer.getMessage ( "jsp.error.file.not.found", jspUri );
            throw new ServletException ( SecurityUtil.filter ( msg ) );
        } else {
            try {
                response.sendError ( HttpServletResponse.SC_NOT_FOUND,
                                     request.getRequestURI() );
            } catch ( IllegalStateException ise ) {
                log.error ( Localizer.getMessage ( "jsp.error.file.not.found",
                                                   jspUri ) );
            }
        }
        return;
    }
}
