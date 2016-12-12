package org.apache.jasper.compiler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.cert.Certificate;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.ServletContext;
import org.apache.jasper.Constants;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.Options;
import org.apache.jasper.runtime.ExceptionUtils;
import org.apache.jasper.servlet.JspServletWrapper;
import org.apache.jasper.util.FastRemovalDequeue;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public final class JspRuntimeContext {
    private final Log log = LogFactory.getLog ( JspRuntimeContext.class );
    private final AtomicInteger jspReloadCount = new AtomicInteger ( 0 );
    private final AtomicInteger jspUnloadCount = new AtomicInteger ( 0 );
    public JspRuntimeContext ( ServletContext context, Options options ) {
        this.context = context;
        this.options = options;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if ( loader == null ) {
            loader = this.getClass().getClassLoader();
        }
        if ( log.isDebugEnabled() ) {
            if ( loader != null ) {
                log.debug ( Localizer.getMessage ( "jsp.message.parent_class_loader_is",
                                                   loader.toString() ) );
            } else {
                log.debug ( Localizer.getMessage ( "jsp.message.parent_class_loader_is",
                                                   "<none>" ) );
            }
        }
        parentClassLoader =  loader;
        classpath = initClassPath();
        if ( context instanceof org.apache.jasper.servlet.JspCServletContext ) {
            codeSource = null;
            permissionCollection = null;
            return;
        }
        if ( Constants.IS_SECURITY_ENABLED ) {
            SecurityHolder holder = initSecurity();
            codeSource = holder.cs;
            permissionCollection = holder.pc;
        } else {
            codeSource = null;
            permissionCollection = null;
        }
        String appBase = context.getRealPath ( "/" );
        if ( !options.getDevelopment()
                && appBase != null
                && options.getCheckInterval() > 0 ) {
            lastCompileCheck = System.currentTimeMillis();
        }
        if ( options.getMaxLoadedJsps() > 0 ) {
            jspQueue = new FastRemovalDequeue<> ( options.getMaxLoadedJsps() );
            if ( log.isDebugEnabled() ) {
                log.debug ( Localizer.getMessage ( "jsp.message.jsp_queue_created",
                                                   "" + options.getMaxLoadedJsps(), context.getContextPath() ) );
            }
        }
        jspIdleTimeout = options.getJspIdleTimeout() * 1000;
    }
    private final ServletContext context;
    private final Options options;
    private final ClassLoader parentClassLoader;
    private final PermissionCollection permissionCollection;
    private final CodeSource codeSource;
    private final String classpath;
    private volatile long lastCompileCheck = -1L;
    private volatile long lastJspQueueUpdate = System.currentTimeMillis();
    private long jspIdleTimeout;
    private final Map<String, JspServletWrapper> jsps = new ConcurrentHashMap<>();
    private FastRemovalDequeue<JspServletWrapper> jspQueue = null;
    public void addWrapper ( String jspUri, JspServletWrapper jsw ) {
        jsps.put ( jspUri, jsw );
    }
    public JspServletWrapper getWrapper ( String jspUri ) {
        return jsps.get ( jspUri );
    }
    public void removeWrapper ( String jspUri ) {
        jsps.remove ( jspUri );
    }
    public FastRemovalDequeue<JspServletWrapper>.Entry push ( JspServletWrapper jsw ) {
        if ( log.isTraceEnabled() ) {
            log.trace ( Localizer.getMessage ( "jsp.message.jsp_added",
                                               jsw.getJspUri(), context.getContextPath() ) );
        }
        FastRemovalDequeue<JspServletWrapper>.Entry entry = jspQueue.push ( jsw );
        JspServletWrapper replaced = entry.getReplaced();
        if ( replaced != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( Localizer.getMessage ( "jsp.message.jsp_removed_excess",
                                                   replaced.getJspUri(), context.getContextPath() ) );
            }
            unloadJspServletWrapper ( replaced );
            entry.clearReplaced();
        }
        return entry;
    }
    public void makeYoungest ( FastRemovalDequeue<JspServletWrapper>.Entry unloadHandle ) {
        if ( log.isTraceEnabled() ) {
            JspServletWrapper jsw = unloadHandle.getContent();
            log.trace ( Localizer.getMessage ( "jsp.message.jsp_queue_update",
                                               jsw.getJspUri(), context.getContextPath() ) );
        }
        jspQueue.moveFirst ( unloadHandle );
    }
    public int getJspCount() {
        return jsps.size();
    }
    public CodeSource getCodeSource() {
        return codeSource;
    }
    public ClassLoader getParentClassLoader() {
        return parentClassLoader;
    }
    public PermissionCollection getPermissionCollection() {
        return permissionCollection;
    }
    public void destroy() {
        Iterator<JspServletWrapper> servlets = jsps.values().iterator();
        while ( servlets.hasNext() ) {
            servlets.next().destroy();
        }
    }
    public void incrementJspReloadCount() {
        jspReloadCount.incrementAndGet();
    }
    public void setJspReloadCount ( int count ) {
        jspReloadCount.set ( count );
    }
    public int getJspReloadCount() {
        return jspReloadCount.intValue();
    }
    public int getJspQueueLength() {
        if ( jspQueue != null ) {
            return jspQueue.getSize();
        }
        return -1;
    }
    public int getJspUnloadCount() {
        return jspUnloadCount.intValue();
    }
    public void checkCompile() {
        if ( lastCompileCheck < 0 ) {
            return;
        }
        long now = System.currentTimeMillis();
        if ( now > ( lastCompileCheck + ( options.getCheckInterval() * 1000L ) ) ) {
            lastCompileCheck = now;
        } else {
            return;
        }
        Object [] wrappers = jsps.values().toArray();
        for ( int i = 0; i < wrappers.length; i++ ) {
            JspServletWrapper jsw = ( JspServletWrapper ) wrappers[i];
            JspCompilationContext ctxt = jsw.getJspEngineContext();
            synchronized ( jsw ) {
                try {
                    ctxt.compile();
                } catch ( FileNotFoundException ex ) {
                    ctxt.incrementRemoved();
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    jsw.getServletContext().log ( "Background compile failed",
                                                  t );
                }
            }
        }
    }
    public String getClassPath() {
        return classpath;
    }
    public long getLastJspQueueUpdate() {
        return lastJspQueueUpdate;
    }
    private String initClassPath() {
        StringBuilder cpath = new StringBuilder();
        if ( parentClassLoader instanceof URLClassLoader ) {
            URL [] urls = ( ( URLClassLoader ) parentClassLoader ).getURLs();
            for ( int i = 0; i < urls.length; i++ ) {
                if ( urls[i].getProtocol().equals ( "file" ) ) {
                    try {
                        String decoded = URLDecoder.decode ( urls[i].getPath(), "UTF-8" );
                        cpath.append ( decoded + File.pathSeparator );
                    } catch ( UnsupportedEncodingException e ) {
                    }
                }
            }
        }
        cpath.append ( options.getScratchDir() + File.pathSeparator );
        String cp = ( String ) context.getAttribute ( Constants.SERVLET_CLASSPATH );
        if ( cp == null || cp.equals ( "" ) ) {
            cp = options.getClassPath();
        }
        String path = cpath.toString() + cp;
        if ( log.isDebugEnabled() ) {
            log.debug ( "Compilation classpath initialized: " + path );
        }
        return path;
    }
    private static class SecurityHolder {
        private final CodeSource cs;
        private final PermissionCollection pc;
        private SecurityHolder ( CodeSource cs, PermissionCollection pc ) {
            this.cs = cs;
            this.pc = pc;
        }
    }
    private SecurityHolder initSecurity() {
        Policy policy = Policy.getPolicy();
        CodeSource source = null;
        PermissionCollection permissions = null;
        if ( policy != null ) {
            try {
                String docBase = context.getRealPath ( "/" );
                if ( docBase == null ) {
                    docBase = options.getScratchDir().toString();
                }
                String codeBase = docBase;
                if ( !codeBase.endsWith ( File.separator ) ) {
                    codeBase = codeBase + File.separator;
                }
                File contextDir = new File ( codeBase );
                URL url = contextDir.getCanonicalFile().toURI().toURL();
                source = new CodeSource ( url, ( Certificate[] ) null );
                permissions = policy.getPermissions ( source );
                if ( !docBase.endsWith ( File.separator ) ) {
                    permissions.add
                    ( new FilePermission ( docBase, "read" ) );
                    docBase = docBase + File.separator;
                } else {
                    permissions.add
                    ( new FilePermission
                      ( docBase.substring ( 0, docBase.length() - 1 ), "read" ) );
                }
                docBase = docBase + "-";
                permissions.add ( new FilePermission ( docBase, "read" ) );
                String workDir = options.getScratchDir().toString();
                if ( !workDir.endsWith ( File.separator ) ) {
                    permissions.add
                    ( new FilePermission ( workDir, "read,write" ) );
                    workDir = workDir + File.separator;
                }
                workDir = workDir + "-";
                permissions.add ( new FilePermission (
                                      workDir, "read,write,delete" ) );
                permissions.add ( new RuntimePermission (
                                      "accessClassInPackage.org.apache.jasper.runtime" ) );
            } catch ( Exception e ) {
                context.log ( "Security Init for context failed", e );
            }
        }
        return new SecurityHolder ( source, permissions );
    }
    private void unloadJspServletWrapper ( JspServletWrapper jsw ) {
        removeWrapper ( jsw.getJspUri() );
        synchronized ( jsw ) {
            jsw.destroy();
        }
        jspUnloadCount.incrementAndGet();
    }
    public void checkUnload() {
        if ( log.isTraceEnabled() ) {
            int queueLength = -1;
            if ( jspQueue != null ) {
                queueLength = jspQueue.getSize();
            }
            log.trace ( Localizer.getMessage ( "jsp.message.jsp_unload_check",
                                               context.getContextPath(), "" + jsps.size(), "" + queueLength ) );
        }
        long now = System.currentTimeMillis();
        if ( jspIdleTimeout > 0 ) {
            long unloadBefore = now - jspIdleTimeout;
            Object [] wrappers = jsps.values().toArray();
            for ( int i = 0; i < wrappers.length; i++ ) {
                JspServletWrapper jsw = ( JspServletWrapper ) wrappers[i];
                synchronized ( jsw ) {
                    if ( jsw.getLastUsageTime() < unloadBefore ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug ( Localizer.getMessage ( "jsp.message.jsp_removed_idle",
                                                               jsw.getJspUri(), context.getContextPath(),
                                                               "" + ( now - jsw.getLastUsageTime() ) ) );
                        }
                        if ( jspQueue != null ) {
                            jspQueue.remove ( jsw.getUnloadHandle() );
                        }
                        unloadJspServletWrapper ( jsw );
                    }
                }
            }
        }
        lastJspQueueUpdate = now;
    }
}
