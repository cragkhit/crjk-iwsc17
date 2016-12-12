package org.apache.jasper.servlet;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.tagext.TagInfo;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.Options;
import org.apache.jasper.compiler.ErrorDispatcher;
import org.apache.jasper.compiler.JavacErrorDetail;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.apache.jasper.compiler.Localizer;
import org.apache.jasper.runtime.ExceptionUtils;
import org.apache.jasper.runtime.InstanceManagerFactory;
import org.apache.jasper.runtime.JspSourceDependent;
import org.apache.jasper.util.FastRemovalDequeue;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.Jar;
@SuppressWarnings ( "deprecation" )
public class JspServletWrapper {
    private static final Map<String, Long> ALWAYS_OUTDATED_DEPENDENCIES =
        new HashMap<>();
    static {
        ALWAYS_OUTDATED_DEPENDENCIES.put ( "/WEB-INF/web.xml", Long.valueOf ( -1 ) );
    }
    private final Log log = LogFactory.getLog ( JspServletWrapper.class );
    private Servlet theServlet;
    private final String jspUri;
    private Class<?> tagHandlerClass;
    private final JspCompilationContext ctxt;
    private long available = 0L;
    private final ServletConfig config;
    private final Options options;
    private boolean firstTime = true;
    private volatile boolean reload = true;
    private final boolean isTagFile;
    private int tripCount;
    private JasperException compileException;
    private volatile long servletClassLastModifiedTime;
    private long lastModificationTest = 0L;
    private long lastUsageTime = System.currentTimeMillis();
    private FastRemovalDequeue<JspServletWrapper>.Entry unloadHandle;
    private final boolean unloadAllowed;
    private final boolean unloadByCount;
    private final boolean unloadByIdle;
    public JspServletWrapper ( ServletConfig config, Options options,
                               String jspUri, JspRuntimeContext rctxt ) {
        this.isTagFile = false;
        this.config = config;
        this.options = options;
        this.jspUri = jspUri;
        unloadByCount = options.getMaxLoadedJsps() > 0 ? true : false;
        unloadByIdle = options.getJspIdleTimeout() > 0 ? true : false;
        unloadAllowed = unloadByCount || unloadByIdle ? true : false;
        ctxt = new JspCompilationContext ( jspUri, options,
                                           config.getServletContext(),
                                           this, rctxt );
    }
    public JspServletWrapper ( ServletContext servletContext,
                               Options options,
                               String tagFilePath,
                               TagInfo tagInfo,
                               JspRuntimeContext rctxt,
                               Jar tagJar ) {
        this.isTagFile = true;
        this.config = null;
        this.options = options;
        this.jspUri = tagFilePath;
        this.tripCount = 0;
        unloadByCount = options.getMaxLoadedJsps() > 0 ? true : false;
        unloadByIdle = options.getJspIdleTimeout() > 0 ? true : false;
        unloadAllowed = unloadByCount || unloadByIdle ? true : false;
        ctxt = new JspCompilationContext ( jspUri, tagInfo, options,
                                           servletContext, this, rctxt,
                                           tagJar );
    }
    public JspCompilationContext getJspEngineContext() {
        return ctxt;
    }
    public void setReload ( boolean reload ) {
        this.reload = reload;
    }
    public Servlet getServlet() throws ServletException {
        if ( reload ) {
            synchronized ( this ) {
                if ( reload ) {
                    destroy();
                    final Servlet servlet;
                    try {
                        InstanceManager instanceManager = InstanceManagerFactory.getInstanceManager ( config );
                        servlet = ( Servlet ) instanceManager.newInstance ( ctxt.getFQCN(), ctxt.getJspLoader() );
                    } catch ( Exception e ) {
                        Throwable t = ExceptionUtils
                                      .unwrapInvocationTargetException ( e );
                        ExceptionUtils.handleThrowable ( t );
                        throw new JasperException ( t );
                    }
                    servlet.init ( config );
                    if ( !firstTime ) {
                        ctxt.getRuntimeContext().incrementJspReloadCount();
                    }
                    theServlet = servlet;
                    reload = false;
                }
            }
        }
        return theServlet;
    }
    public ServletContext getServletContext() {
        return ctxt.getServletContext();
    }
    public void setCompilationException ( JasperException je ) {
        this.compileException = je;
    }
    public void setServletClassLastModifiedTime ( long lastModified ) {
        if ( this.servletClassLastModifiedTime < lastModified ) {
            synchronized ( this ) {
                if ( this.servletClassLastModifiedTime < lastModified ) {
                    this.servletClassLastModifiedTime = lastModified;
                    reload = true;
                    ctxt.clearJspLoader();
                }
            }
        }
    }
    public Class<?> loadTagFile() throws JasperException {
        try {
            if ( ctxt.isRemoved() ) {
                throw new FileNotFoundException ( jspUri );
            }
            if ( options.getDevelopment() || firstTime ) {
                synchronized ( this ) {
                    firstTime = false;
                    ctxt.compile();
                }
            } else {
                if ( compileException != null ) {
                    throw compileException;
                }
            }
            if ( reload ) {
                tagHandlerClass = ctxt.load();
                reload = false;
            }
        } catch ( FileNotFoundException ex ) {
            throw new JasperException ( ex );
        }
        return tagHandlerClass;
    }
    public Class<?> loadTagFilePrototype() throws JasperException {
        ctxt.setPrototypeMode ( true );
        try {
            return loadTagFile();
        } finally {
            ctxt.setPrototypeMode ( false );
        }
    }
    public java.util.Map<String, Long> getDependants() {
        try {
            Object target;
            if ( isTagFile ) {
                if ( reload ) {
                    tagHandlerClass = ctxt.load();
                    reload = false;
                }
                target = tagHandlerClass.newInstance();
            } else {
                target = getServlet();
            }
            if ( target instanceof JspSourceDependent ) {
                return ( ( JspSourceDependent ) target ).getDependants();
            }
        } catch ( AbstractMethodError ame ) {
            return ALWAYS_OUTDATED_DEPENDENCIES;
        } catch ( Throwable ex ) {
            ExceptionUtils.handleThrowable ( ex );
        }
        return null;
    }
    public boolean isTagFile() {
        return this.isTagFile;
    }
    public int incTripCount() {
        return tripCount++;
    }
    public int decTripCount() {
        return tripCount--;
    }
    public String getJspUri() {
        return jspUri;
    }
    public FastRemovalDequeue<JspServletWrapper>.Entry getUnloadHandle() {
        return unloadHandle;
    }
    public void service ( HttpServletRequest request,
                          HttpServletResponse response,
                          boolean precompile )
    throws ServletException, IOException, FileNotFoundException {
        Servlet servlet;
        try {
            if ( ctxt.isRemoved() ) {
                throw new FileNotFoundException ( jspUri );
            }
            if ( ( available > 0L ) && ( available < Long.MAX_VALUE ) ) {
                if ( available > System.currentTimeMillis() ) {
                    response.setDateHeader ( "Retry-After", available );
                    response.sendError
                    ( HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                      Localizer.getMessage ( "jsp.error.unavailable" ) );
                    return;
                }
                available = 0;
            }
            if ( options.getDevelopment() || firstTime ) {
                synchronized ( this ) {
                    firstTime = false;
                    ctxt.compile();
                }
            } else {
                if ( compileException != null ) {
                    throw compileException;
                }
            }
            servlet = getServlet();
            if ( precompile ) {
                return;
            }
        } catch ( ServletException ex ) {
            if ( options.getDevelopment() ) {
                throw handleJspException ( ex );
            }
            throw ex;
        } catch ( FileNotFoundException fnfe ) {
            throw fnfe;
        } catch ( IOException ex ) {
            if ( options.getDevelopment() ) {
                throw handleJspException ( ex );
            }
            throw ex;
        } catch ( IllegalStateException ex ) {
            if ( options.getDevelopment() ) {
                throw handleJspException ( ex );
            }
            throw ex;
        } catch ( Exception ex ) {
            if ( options.getDevelopment() ) {
                throw handleJspException ( ex );
            }
            throw new JasperException ( ex );
        }
        try {
            if ( unloadAllowed ) {
                synchronized ( this ) {
                    if ( unloadByCount ) {
                        if ( unloadHandle == null ) {
                            unloadHandle = ctxt.getRuntimeContext().push ( this );
                        } else if ( lastUsageTime < ctxt.getRuntimeContext().getLastJspQueueUpdate() ) {
                            ctxt.getRuntimeContext().makeYoungest ( unloadHandle );
                            lastUsageTime = System.currentTimeMillis();
                        }
                    } else {
                        if ( lastUsageTime < ctxt.getRuntimeContext().getLastJspQueueUpdate() ) {
                            lastUsageTime = System.currentTimeMillis();
                        }
                    }
                }
            }
            if ( servlet instanceof SingleThreadModel ) {
                synchronized ( this ) {
                    servlet.service ( request, response );
                }
            } else {
                servlet.service ( request, response );
            }
        } catch ( UnavailableException ex ) {
            String includeRequestUri = ( String )
                                       request.getAttribute ( RequestDispatcher.INCLUDE_REQUEST_URI );
            if ( includeRequestUri != null ) {
                throw ex;
            }
            int unavailableSeconds = ex.getUnavailableSeconds();
            if ( unavailableSeconds <= 0 ) {
                unavailableSeconds = 60;
            }
            available = System.currentTimeMillis() +
                        ( unavailableSeconds * 1000L );
            response.sendError
            ( HttpServletResponse.SC_SERVICE_UNAVAILABLE,
              ex.getMessage() );
        } catch ( ServletException ex ) {
            if ( options.getDevelopment() ) {
                throw handleJspException ( ex );
            }
            throw ex;
        } catch ( IOException ex ) {
            if ( options.getDevelopment() ) {
                throw new IOException ( handleJspException ( ex ).getMessage(), ex );
            }
            throw ex;
        } catch ( IllegalStateException ex ) {
            if ( options.getDevelopment() ) {
                throw handleJspException ( ex );
            }
            throw ex;
        } catch ( Exception ex ) {
            if ( options.getDevelopment() ) {
                throw handleJspException ( ex );
            }
            throw new JasperException ( ex );
        }
    }
    public void destroy() {
        if ( theServlet != null ) {
            try {
                theServlet.destroy();
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                log.error ( Localizer.getMessage ( "jsp.error.servlet.destroy.failed" ), t );
            }
            InstanceManager instanceManager = InstanceManagerFactory.getInstanceManager ( config );
            try {
                instanceManager.destroyInstance ( theServlet );
            } catch ( Exception e ) {
                Throwable t = ExceptionUtils.unwrapInvocationTargetException ( e );
                ExceptionUtils.handleThrowable ( t );
                log.error ( Localizer.getMessage ( "jsp.error.file.not.found",
                                                   e.getMessage() ), t );
            }
        }
    }
    public long getLastModificationTest() {
        return lastModificationTest;
    }
    public void setLastModificationTest ( long lastModificationTest ) {
        this.lastModificationTest = lastModificationTest;
    }
    public long getLastUsageTime() {
        return lastUsageTime;
    }
    protected JasperException handleJspException ( Exception ex ) {
        try {
            Throwable realException = ex;
            if ( ex instanceof ServletException ) {
                realException = ( ( ServletException ) ex ).getRootCause();
            }
            StackTraceElement[] frames = realException.getStackTrace();
            StackTraceElement jspFrame = null;
            for ( int i = 0; i < frames.length; ++i ) {
                if ( frames[i].getClassName().equals ( this.getServlet().getClass().getName() ) ) {
                    jspFrame = frames[i];
                    break;
                }
            }
            if ( jspFrame == null ||
                    this.ctxt.getCompiler().getPageNodes() == null ) {
                return new JasperException ( ex );
            }
            int javaLineNumber = jspFrame.getLineNumber();
            JavacErrorDetail detail = ErrorDispatcher.createJavacError (
                                          jspFrame.getMethodName(),
                                          this.ctxt.getCompiler().getPageNodes(),
                                          null,
                                          javaLineNumber,
                                          ctxt );
            int jspLineNumber = detail.getJspBeginLineNumber();
            if ( jspLineNumber < 1 ) {
                throw new JasperException ( ex );
            }
            if ( options.getDisplaySourceFragment() ) {
                return new JasperException ( Localizer.getMessage
                                             ( "jsp.exception", detail.getJspFileName(),
                                               "" + jspLineNumber ) + System.lineSeparator() +
                                             System.lineSeparator() + detail.getJspExtract() +
                                             System.lineSeparator() + System.lineSeparator() +
                                             "Stacktrace:", ex );
            }
            return new JasperException ( Localizer.getMessage
                                         ( "jsp.exception", detail.getJspFileName(),
                                           "" + jspLineNumber ), ex );
        } catch ( Exception je ) {
            if ( ex instanceof JasperException ) {
                return ( JasperException ) ex;
            }
            return new JasperException ( ex );
        }
    }
}
