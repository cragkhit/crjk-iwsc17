package org.apache.jasper.servlet;
import java.util.HashMap;
import org.apache.jasper.compiler.JavacErrorDetail;
import org.apache.jasper.compiler.ErrorDispatcher;
import javax.servlet.UnavailableException;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.SingleThreadModel;
import java.io.IOException;
import org.apache.jasper.compiler.Localizer;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.jasper.runtime.JspSourceDependent;
import java.io.FileNotFoundException;
import javax.servlet.ServletException;
import org.apache.tomcat.InstanceManager;
import org.apache.jasper.runtime.ExceptionUtils;
import org.apache.jasper.runtime.InstanceManagerFactory;
import org.apache.tomcat.Jar;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.ServletContext;
import org.apache.juli.logging.LogFactory;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.apache.jasper.util.FastRemovalDequeue;
import org.apache.jasper.JasperException;
import org.apache.jasper.Options;
import javax.servlet.ServletConfig;
import org.apache.jasper.JspCompilationContext;
import javax.servlet.Servlet;
import org.apache.juli.logging.Log;
import java.util.Map;
public class JspServletWrapper {
    private static final Map<String, Long> ALWAYS_OUTDATED_DEPENDENCIES;
    private final Log log;
    private Servlet theServlet;
    private final String jspUri;
    private Class<?> tagHandlerClass;
    private final JspCompilationContext ctxt;
    private long available;
    private final ServletConfig config;
    private final Options options;
    private boolean firstTime;
    private volatile boolean reload;
    private final boolean isTagFile;
    private int tripCount;
    private JasperException compileException;
    private volatile long servletClassLastModifiedTime;
    private long lastModificationTest;
    private long lastUsageTime;
    private FastRemovalDequeue.Entry unloadHandle;
    private final boolean unloadAllowed;
    private final boolean unloadByCount;
    private final boolean unloadByIdle;
    public JspServletWrapper ( final ServletConfig config, final Options options, final String jspUri, final JspRuntimeContext rctxt ) {
        this.log = LogFactory.getLog ( JspServletWrapper.class );
        this.available = 0L;
        this.firstTime = true;
        this.reload = true;
        this.lastModificationTest = 0L;
        this.lastUsageTime = System.currentTimeMillis();
        this.isTagFile = false;
        this.config = config;
        this.options = options;
        this.jspUri = jspUri;
        this.unloadByCount = ( options.getMaxLoadedJsps() > 0 );
        this.unloadByIdle = ( options.getJspIdleTimeout() > 0 );
        this.unloadAllowed = ( this.unloadByCount || this.unloadByIdle );
        this.ctxt = new JspCompilationContext ( jspUri, options, config.getServletContext(), this, rctxt );
    }
    public JspServletWrapper ( final ServletContext servletContext, final Options options, final String tagFilePath, final TagInfo tagInfo, final JspRuntimeContext rctxt, final Jar tagJar ) {
        this.log = LogFactory.getLog ( JspServletWrapper.class );
        this.available = 0L;
        this.firstTime = true;
        this.reload = true;
        this.lastModificationTest = 0L;
        this.lastUsageTime = System.currentTimeMillis();
        this.isTagFile = true;
        this.config = null;
        this.options = options;
        this.jspUri = tagFilePath;
        this.tripCount = 0;
        this.unloadByCount = ( options.getMaxLoadedJsps() > 0 );
        this.unloadByIdle = ( options.getJspIdleTimeout() > 0 );
        this.unloadAllowed = ( this.unloadByCount || this.unloadByIdle );
        this.ctxt = new JspCompilationContext ( this.jspUri, tagInfo, options, servletContext, this, rctxt, tagJar );
    }
    public JspCompilationContext getJspEngineContext() {
        return this.ctxt;
    }
    public void setReload ( final boolean reload ) {
        this.reload = reload;
    }
    public Servlet getServlet() throws ServletException {
        if ( this.reload ) {
            synchronized ( this ) {
                if ( this.reload ) {
                    this.destroy();
                    Servlet servlet;
                    try {
                        final InstanceManager instanceManager = InstanceManagerFactory.getInstanceManager ( this.config );
                        servlet = ( Servlet ) instanceManager.newInstance ( this.ctxt.getFQCN(), this.ctxt.getJspLoader() );
                    } catch ( Exception e ) {
                        final Throwable t = ExceptionUtils.unwrapInvocationTargetException ( e );
                        ExceptionUtils.handleThrowable ( t );
                        throw new JasperException ( t );
                    }
                    servlet.init ( this.config );
                    if ( !this.firstTime ) {
                        this.ctxt.getRuntimeContext().incrementJspReloadCount();
                    }
                    this.theServlet = servlet;
                    this.reload = false;
                }
            }
        }
        return this.theServlet;
    }
    public ServletContext getServletContext() {
        return this.ctxt.getServletContext();
    }
    public void setCompilationException ( final JasperException je ) {
        this.compileException = je;
    }
    public void setServletClassLastModifiedTime ( final long lastModified ) {
        if ( this.servletClassLastModifiedTime < lastModified ) {
            synchronized ( this ) {
                if ( this.servletClassLastModifiedTime < lastModified ) {
                    this.servletClassLastModifiedTime = lastModified;
                    this.reload = true;
                    this.ctxt.clearJspLoader();
                }
            }
        }
    }
    public Class<?> loadTagFile() throws JasperException {
        try {
            if ( this.ctxt.isRemoved() ) {
                throw new FileNotFoundException ( this.jspUri );
            }
            if ( this.options.getDevelopment() || this.firstTime ) {
                synchronized ( this ) {
                    this.firstTime = false;
                    this.ctxt.compile();
                }
            } else if ( this.compileException != null ) {
                throw this.compileException;
            }
            if ( this.reload ) {
                this.tagHandlerClass = this.ctxt.load();
                this.reload = false;
            }
        } catch ( FileNotFoundException ex ) {
            throw new JasperException ( ex );
        }
        return this.tagHandlerClass;
    }
    public Class<?> loadTagFilePrototype() throws JasperException {
        this.ctxt.setPrototypeMode ( true );
        try {
            return this.loadTagFile();
        } finally {
            this.ctxt.setPrototypeMode ( false );
        }
    }
    public Map<String, Long> getDependants() {
        try {
            Object target;
            if ( this.isTagFile ) {
                if ( this.reload ) {
                    this.tagHandlerClass = this.ctxt.load();
                    this.reload = false;
                }
                target = this.tagHandlerClass.newInstance();
            } else {
                target = this.getServlet();
            }
            if ( target instanceof JspSourceDependent ) {
                return ( ( JspSourceDependent ) target ).getDependants();
            }
        } catch ( AbstractMethodError ame ) {
            return JspServletWrapper.ALWAYS_OUTDATED_DEPENDENCIES;
        } catch ( Throwable ex ) {
            ExceptionUtils.handleThrowable ( ex );
        }
        return null;
    }
    public boolean isTagFile() {
        return this.isTagFile;
    }
    public int incTripCount() {
        return this.tripCount++;
    }
    public int decTripCount() {
        return this.tripCount--;
    }
    public String getJspUri() {
        return this.jspUri;
    }
    public FastRemovalDequeue.Entry getUnloadHandle() {
        return this.unloadHandle;
    }
    public void service ( final HttpServletRequest request, final HttpServletResponse response, final boolean precompile ) throws ServletException, IOException, FileNotFoundException {
        Servlet servlet;
        try {
            if ( this.ctxt.isRemoved() ) {
                throw new FileNotFoundException ( this.jspUri );
            }
            if ( this.available > 0L && this.available < Long.MAX_VALUE ) {
                if ( this.available > System.currentTimeMillis() ) {
                    response.setDateHeader ( "Retry-After", this.available );
                    response.sendError ( 503, Localizer.getMessage ( "jsp.error.unavailable" ) );
                    return;
                }
                this.available = 0L;
            }
            if ( this.options.getDevelopment() || this.firstTime ) {
                synchronized ( this ) {
                    this.firstTime = false;
                    this.ctxt.compile();
                }
            } else if ( this.compileException != null ) {
                throw this.compileException;
            }
            servlet = this.getServlet();
            if ( precompile ) {
                return;
            }
        } catch ( ServletException ex ) {
            if ( this.options.getDevelopment() ) {
                throw this.handleJspException ( ( Exception ) ex );
            }
            throw ex;
        } catch ( FileNotFoundException fnfe ) {
            throw fnfe;
        } catch ( IOException ex2 ) {
            if ( this.options.getDevelopment() ) {
                throw this.handleJspException ( ex2 );
            }
            throw ex2;
        } catch ( IllegalStateException ex3 ) {
            if ( this.options.getDevelopment() ) {
                throw this.handleJspException ( ex3 );
            }
            throw ex3;
        } catch ( Exception ex4 ) {
            if ( this.options.getDevelopment() ) {
                throw this.handleJspException ( ex4 );
            }
            throw new JasperException ( ex4 );
        }
        try {
            if ( this.unloadAllowed ) {
                synchronized ( this ) {
                    if ( this.unloadByCount ) {
                        if ( this.unloadHandle == null ) {
                            this.unloadHandle = this.ctxt.getRuntimeContext().push ( this );
                        } else if ( this.lastUsageTime < this.ctxt.getRuntimeContext().getLastJspQueueUpdate() ) {
                            this.ctxt.getRuntimeContext().makeYoungest ( this.unloadHandle );
                            this.lastUsageTime = System.currentTimeMillis();
                        }
                    } else if ( this.lastUsageTime < this.ctxt.getRuntimeContext().getLastJspQueueUpdate() ) {
                        this.lastUsageTime = System.currentTimeMillis();
                    }
                }
            }
            if ( servlet instanceof SingleThreadModel ) {
                synchronized ( this ) {
                    servlet.service ( ( ServletRequest ) request, ( ServletResponse ) response );
                }
            } else {
                servlet.service ( ( ServletRequest ) request, ( ServletResponse ) response );
            }
        } catch ( UnavailableException ex5 ) {
            final String includeRequestUri = ( String ) request.getAttribute ( "javax.servlet.include.request_uri" );
            if ( includeRequestUri != null ) {
                throw ex5;
            }
            int unavailableSeconds = ex5.getUnavailableSeconds();
            if ( unavailableSeconds <= 0 ) {
                unavailableSeconds = 60;
            }
            this.available = System.currentTimeMillis() + unavailableSeconds * 1000L;
            response.sendError ( 503, ex5.getMessage() );
        } catch ( ServletException ex ) {
            if ( this.options.getDevelopment() ) {
                throw this.handleJspException ( ( Exception ) ex );
            }
            throw ex;
        } catch ( IOException ex2 ) {
            if ( this.options.getDevelopment() ) {
                throw new IOException ( this.handleJspException ( ex2 ).getMessage(), ex2 );
            }
            throw ex2;
        } catch ( IllegalStateException ex3 ) {
            if ( this.options.getDevelopment() ) {
                throw this.handleJspException ( ex3 );
            }
            throw ex3;
        } catch ( Exception ex4 ) {
            if ( this.options.getDevelopment() ) {
                throw this.handleJspException ( ex4 );
            }
            throw new JasperException ( ex4 );
        }
    }
    public void destroy() {
        if ( this.theServlet != null ) {
            try {
                this.theServlet.destroy();
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                this.log.error ( Localizer.getMessage ( "jsp.error.servlet.destroy.failed" ), t );
            }
            final InstanceManager instanceManager = InstanceManagerFactory.getInstanceManager ( this.config );
            try {
                instanceManager.destroyInstance ( this.theServlet );
            } catch ( Exception e ) {
                final Throwable t2 = ExceptionUtils.unwrapInvocationTargetException ( e );
                ExceptionUtils.handleThrowable ( t2 );
                this.log.error ( Localizer.getMessage ( "jsp.error.file.not.found", e.getMessage() ), t2 );
            }
        }
    }
    public long getLastModificationTest() {
        return this.lastModificationTest;
    }
    public void setLastModificationTest ( final long lastModificationTest ) {
        this.lastModificationTest = lastModificationTest;
    }
    public long getLastUsageTime() {
        return this.lastUsageTime;
    }
    protected JasperException handleJspException ( final Exception ex ) {
        try {
            Throwable realException = ex;
            if ( ex instanceof ServletException ) {
                realException = ( ( ServletException ) ex ).getRootCause();
            }
            final StackTraceElement[] frames = realException.getStackTrace();
            StackTraceElement jspFrame = null;
            for ( int i = 0; i < frames.length; ++i ) {
                if ( frames[i].getClassName().equals ( this.getServlet().getClass().getName() ) ) {
                    jspFrame = frames[i];
                    break;
                }
            }
            if ( jspFrame == null || this.ctxt.getCompiler().getPageNodes() == null ) {
                return new JasperException ( ex );
            }
            final int javaLineNumber = jspFrame.getLineNumber();
            final JavacErrorDetail detail = ErrorDispatcher.createJavacError ( jspFrame.getMethodName(), this.ctxt.getCompiler().getPageNodes(), null, javaLineNumber, this.ctxt );
            final int jspLineNumber = detail.getJspBeginLineNumber();
            if ( jspLineNumber < 1 ) {
                throw new JasperException ( ex );
            }
            if ( this.options.getDisplaySourceFragment() ) {
                return new JasperException ( Localizer.getMessage ( "jsp.exception", detail.getJspFileName(), "" + jspLineNumber ) + System.lineSeparator() + System.lineSeparator() + detail.getJspExtract() + System.lineSeparator() + System.lineSeparator() + "Stacktrace:", ex );
            }
            return new JasperException ( Localizer.getMessage ( "jsp.exception", detail.getJspFileName(), "" + jspLineNumber ), ex );
        } catch ( Exception je ) {
            if ( ex instanceof JasperException ) {
                return ( JasperException ) ex;
            }
            return new JasperException ( ex );
        }
    }
    static {
        ( ALWAYS_OUTDATED_DEPENDENCIES = new HashMap<String, Long>() ).put ( "/WEB-INF/web.xml", -1L );
    }
}
