package org.apache.catalina.startup;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import java.util.Stack;
import javax.servlet.SingleThreadModel;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.Valve;
import org.apache.catalina.authenticator.NonLoginAuthenticator;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.catalina.LifecycleEvent;
import java.util.jar.JarEntry;
import org.apache.tomcat.util.buf.UriUtil;
import java.util.jar.JarFile;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.catalina.core.NamingContextListener;
import java.lang.reflect.InvocationTargetException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.ContainerBase;
import java.util.logging.Level;
import java.io.IOException;
import java.io.File;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.RealmBase;
import org.apache.catalina.Realm;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import java.util.ArrayList;
import org.apache.catalina.LifecycleException;
import javax.servlet.Servlet;
import org.apache.catalina.Container;
import org.apache.catalina.Wrapper;
import javax.servlet.ServletException;
import org.apache.catalina.Context;
import java.util.HashMap;
import java.security.Principal;
import java.util.List;
import org.apache.catalina.Server;
import java.util.logging.Logger;
import java.util.Map;
public class Tomcat {
    private final Map<String, Logger> pinnedLoggers;
    protected Server server;
    protected int port;
    protected String hostname;
    protected String basedir;
    private final Map<String, String> userPass;
    private final Map<String, List<String>> userRoles;
    private final Map<String, Principal> userPrincipals;
    static final String[] silences;
    private boolean silent;
    private static final String[] DEFAULT_MIME_MAPPINGS;
    public Tomcat() {
        this.pinnedLoggers = new HashMap<String, Logger>();
        this.port = 8080;
        this.hostname = "localhost";
        this.userPass = new HashMap<String, String>();
        this.userRoles = new HashMap<String, List<String>>();
        this.userPrincipals = new HashMap<String, Principal>();
        this.silent = false;
    }
    public void setBaseDir ( final String basedir ) {
        this.basedir = basedir;
    }
    public void setPort ( final int port ) {
        this.port = port;
    }
    public void setHostname ( final String s ) {
        this.hostname = s;
    }
    public Context addWebapp ( final String contextPath, final String docBase ) throws ServletException {
        return this.addWebapp ( this.getHost(), contextPath, docBase );
    }
    public Context addContext ( final String contextPath, final String docBase ) {
        return this.addContext ( this.getHost(), contextPath, docBase );
    }
    public Wrapper addServlet ( final String contextPath, final String servletName, final String servletClass ) {
        final Container ctx = this.getHost().findChild ( contextPath );
        return addServlet ( ( Context ) ctx, servletName, servletClass );
    }
    public static Wrapper addServlet ( final Context ctx, final String servletName, final String servletClass ) {
        final Wrapper sw = ctx.createWrapper();
        sw.setServletClass ( servletClass );
        sw.setName ( servletName );
        ctx.addChild ( sw );
        return sw;
    }
    public Wrapper addServlet ( final String contextPath, final String servletName, final Servlet servlet ) {
        final Container ctx = this.getHost().findChild ( contextPath );
        return addServlet ( ( Context ) ctx, servletName, servlet );
    }
    public static Wrapper addServlet ( final Context ctx, final String servletName, final Servlet servlet ) {
        final Wrapper sw = new ExistingStandardWrapper ( servlet );
        sw.setName ( servletName );
        ctx.addChild ( sw );
        return sw;
    }
    public void init() throws LifecycleException {
        this.getServer();
        this.server.init();
    }
    public void start() throws LifecycleException {
        this.getServer();
        this.server.start();
    }
    public void stop() throws LifecycleException {
        this.getServer();
        this.server.stop();
    }
    public void destroy() throws LifecycleException {
        this.getServer();
        this.server.destroy();
    }
    public void addUser ( final String user, final String pass ) {
        this.userPass.put ( user, pass );
    }
    public void addRole ( final String user, final String role ) {
        List<String> roles = this.userRoles.get ( user );
        if ( roles == null ) {
            roles = new ArrayList<String>();
            this.userRoles.put ( user, roles );
        }
        roles.add ( role );
    }
    public Connector getConnector() {
        final Service service = this.getService();
        if ( service.findConnectors().length > 0 ) {
            return service.findConnectors() [0];
        }
        final Connector connector = new Connector ( "HTTP/1.1" );
        connector.setPort ( this.port );
        service.addConnector ( connector );
        return connector;
    }
    public void setConnector ( final Connector connector ) {
        final Service service = this.getService();
        boolean found = false;
        for ( final Connector serviceConnector : service.findConnectors() ) {
            if ( connector == serviceConnector ) {
                found = true;
            }
        }
        if ( !found ) {
            service.addConnector ( connector );
        }
    }
    public Service getService() {
        return this.getServer().findServices() [0];
    }
    public void setHost ( final Host host ) {
        final Engine engine = this.getEngine();
        boolean found = false;
        for ( final Container engineHost : engine.findChildren() ) {
            if ( engineHost == host ) {
                found = true;
            }
        }
        if ( !found ) {
            engine.addChild ( host );
        }
    }
    public Host getHost() {
        final Engine engine = this.getEngine();
        if ( engine.findChildren().length > 0 ) {
            return ( Host ) engine.findChildren() [0];
        }
        final Host host = new StandardHost();
        host.setName ( this.hostname );
        this.getEngine().addChild ( host );
        return host;
    }
    public Engine getEngine() {
        final Service service = this.getServer().findServices() [0];
        if ( service.getContainer() != null ) {
            return service.getContainer();
        }
        final Engine engine = new StandardEngine();
        engine.setName ( "Tomcat" );
        engine.setDefaultHost ( this.hostname );
        engine.setRealm ( this.createDefaultRealm() );
        service.setContainer ( engine );
        return engine;
    }
    public Server getServer() {
        if ( this.server != null ) {
            return this.server;
        }
        System.setProperty ( "catalina.useNaming", "false" );
        this.server = new StandardServer();
        this.initBaseDir();
        this.server.setPort ( -1 );
        final Service service = new StandardService();
        service.setName ( "Tomcat" );
        this.server.addService ( service );
        return this.server;
    }
    public Context addContext ( final Host host, final String contextPath, final String dir ) {
        return this.addContext ( host, contextPath, contextPath, dir );
    }
    public Context addContext ( final Host host, final String contextPath, final String contextName, final String dir ) {
        this.silence ( host, contextName );
        final Context ctx = this.createContext ( host, contextPath );
        ctx.setName ( contextName );
        ctx.setPath ( contextPath );
        ctx.setDocBase ( dir );
        ctx.addLifecycleListener ( new FixContextListener() );
        if ( host == null ) {
            this.getHost().addChild ( ctx );
        } else {
            host.addChild ( ctx );
        }
        return ctx;
    }
    public Context addWebapp ( final Host host, final String contextPath, final String docBase ) {
        LifecycleListener listener = null;
        try {
            final Class<?> clazz = Class.forName ( this.getHost().getConfigClass() );
            listener = ( LifecycleListener ) clazz.newInstance();
        } catch ( ClassNotFoundException | InstantiationException | IllegalAccessException e ) {
            throw new IllegalArgumentException ( e );
        }
        return this.addWebapp ( host, contextPath, docBase, listener );
    }
    public Context addWebapp ( final Host host, final String contextPath, final String docBase, final LifecycleListener config ) {
        this.silence ( host, contextPath );
        final Context ctx = this.createContext ( host, contextPath );
        ctx.setPath ( contextPath );
        ctx.setDocBase ( docBase );
        ctx.addLifecycleListener ( new DefaultWebXmlListener() );
        ctx.setConfigFile ( this.getWebappConfigFile ( docBase, contextPath ) );
        ctx.addLifecycleListener ( config );
        if ( config instanceof ContextConfig ) {
            ( ( ContextConfig ) config ).setDefaultWebXml ( this.noDefaultWebXmlPath() );
        }
        if ( host == null ) {
            this.getHost().addChild ( ctx );
        } else {
            host.addChild ( ctx );
        }
        return ctx;
    }
    public LifecycleListener getDefaultWebXmlListener() {
        return new DefaultWebXmlListener();
    }
    public String noDefaultWebXmlPath() {
        return "org/apache/catalina/startup/NO_DEFAULT_XML";
    }
    protected Realm createDefaultRealm() {
        return new RealmBase() {
            @Override
            protected String getName() {
                return "Simple";
            }
            @Override
            protected String getPassword ( final String username ) {
                return Tomcat.this.userPass.get ( username );
            }
            @Override
            protected Principal getPrincipal ( final String username ) {
                Principal p = Tomcat.this.userPrincipals.get ( username );
                if ( p == null ) {
                    final String pass = Tomcat.this.userPass.get ( username );
                    if ( pass != null ) {
                        p = new GenericPrincipal ( username, pass, Tomcat.this.userRoles.get ( username ) );
                        Tomcat.this.userPrincipals.put ( username, p );
                    }
                }
                return p;
            }
        };
    }
    protected void initBaseDir() {
        final String catalinaHome = System.getProperty ( "catalina.home" );
        if ( this.basedir == null ) {
            this.basedir = System.getProperty ( "catalina.base" );
        }
        if ( this.basedir == null ) {
            this.basedir = catalinaHome;
        }
        if ( this.basedir == null ) {
            this.basedir = System.getProperty ( "user.dir" ) + "/tomcat." + this.port;
        }
        File baseFile = new File ( this.basedir );
        baseFile.mkdirs();
        try {
            baseFile = baseFile.getCanonicalFile();
        } catch ( IOException e ) {
            baseFile = baseFile.getAbsoluteFile();
        }
        this.server.setCatalinaBase ( baseFile );
        System.setProperty ( "catalina.base", baseFile.getPath() );
        this.basedir = baseFile.getPath();
        if ( catalinaHome == null ) {
            this.server.setCatalinaHome ( baseFile );
        } else {
            File homeFile = new File ( catalinaHome );
            homeFile.mkdirs();
            try {
                homeFile = homeFile.getCanonicalFile();
            } catch ( IOException e2 ) {
                homeFile = homeFile.getAbsoluteFile();
            }
            this.server.setCatalinaHome ( homeFile );
        }
        System.setProperty ( "catalina.home", this.server.getCatalinaHome().getPath() );
    }
    public void setSilent ( final boolean silent ) {
        this.silent = silent;
        for ( final String s : Tomcat.silences ) {
            final Logger logger = Logger.getLogger ( s );
            this.pinnedLoggers.put ( s, logger );
            if ( silent ) {
                logger.setLevel ( Level.WARNING );
            } else {
                logger.setLevel ( Level.INFO );
            }
        }
    }
    private void silence ( final Host host, final String contextPath ) {
        final String loggerName = this.getLoggerName ( host, contextPath );
        final Logger logger = Logger.getLogger ( loggerName );
        this.pinnedLoggers.put ( loggerName, logger );
        if ( this.silent ) {
            logger.setLevel ( Level.WARNING );
        } else {
            logger.setLevel ( Level.INFO );
        }
    }
    private String getLoggerName ( Host host, final String contextName ) {
        if ( host == null ) {
            host = this.getHost();
        }
        final StringBuilder loggerName = new StringBuilder();
        loggerName.append ( ContainerBase.class.getName() );
        loggerName.append ( ".[" );
        loggerName.append ( host.getParent().getName() );
        loggerName.append ( "].[" );
        loggerName.append ( host.getName() );
        loggerName.append ( "].[" );
        if ( contextName == null || contextName.equals ( "" ) ) {
            loggerName.append ( "/" );
        } else if ( contextName.startsWith ( "##" ) ) {
            loggerName.append ( "/" );
            loggerName.append ( contextName );
        }
        loggerName.append ( ']' );
        return loggerName.toString();
    }
    private Context createContext ( Host host, final String url ) {
        String contextClass = StandardContext.class.getName();
        if ( host == null ) {
            host = this.getHost();
        }
        if ( host instanceof StandardHost ) {
            contextClass = ( ( StandardHost ) host ).getContextClass();
        }
        try {
            return ( Context ) Class.forName ( contextClass ).getConstructor ( ( Class<?>[] ) new Class[0] ).newInstance ( new Object[0] );
        } catch ( InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e ) {
            throw new IllegalArgumentException ( "Can't instantiate context-class " + contextClass + " for host " + host + " and url " + url, e );
        }
    }
    public void enableNaming() {
        this.getServer();
        this.server.addLifecycleListener ( new NamingContextListener() );
        System.setProperty ( "catalina.useNaming", "true" );
        String value = "org.apache.naming";
        final String oldValue = System.getProperty ( "java.naming.factory.url.pkgs" );
        if ( oldValue != null ) {
            if ( oldValue.contains ( value ) ) {
                value = oldValue;
            } else {
                value = value + ":" + oldValue;
            }
        }
        System.setProperty ( "java.naming.factory.url.pkgs", value );
        value = System.getProperty ( "java.naming.factory.initial" );
        if ( value == null ) {
            System.setProperty ( "java.naming.factory.initial", "org.apache.naming.java.javaURLContextFactory" );
        }
    }
    public void initWebappDefaults ( final String contextPath ) {
        final Container ctx = this.getHost().findChild ( contextPath );
        initWebappDefaults ( ( Context ) ctx );
    }
    public static void initWebappDefaults ( final Context ctx ) {
        Wrapper servlet = addServlet ( ctx, "default", "org.apache.catalina.servlets.DefaultServlet" );
        servlet.setLoadOnStartup ( 1 );
        servlet.setOverridable ( true );
        servlet = addServlet ( ctx, "jsp", "org.apache.jasper.servlet.JspServlet" );
        servlet.addInitParameter ( "fork", "false" );
        servlet.setLoadOnStartup ( 3 );
        servlet.setOverridable ( true );
        ctx.addServletMappingDecoded ( "/", "default" );
        ctx.addServletMappingDecoded ( "*.jsp", "jsp" );
        ctx.addServletMappingDecoded ( "*.jspx", "jsp" );
        ctx.setSessionTimeout ( 30 );
        int i = 0;
        while ( i < Tomcat.DEFAULT_MIME_MAPPINGS.length ) {
            ctx.addMimeMapping ( Tomcat.DEFAULT_MIME_MAPPINGS[i++], Tomcat.DEFAULT_MIME_MAPPINGS[i++] );
        }
        ctx.addWelcomeFile ( "index.html" );
        ctx.addWelcomeFile ( "index.htm" );
        ctx.addWelcomeFile ( "index.jsp" );
    }
    protected URL getWebappConfigFile ( final String path, final String contextName ) {
        final File docBase = new File ( path );
        if ( docBase.isDirectory() ) {
            return this.getWebappConfigFileFromDirectory ( docBase, contextName );
        }
        return this.getWebappConfigFileFromJar ( docBase, contextName );
    }
    private URL getWebappConfigFileFromDirectory ( final File docBase, final String contextName ) {
        URL result = null;
        final File webAppContextXml = new File ( docBase, "META-INF/context.xml" );
        if ( webAppContextXml.exists() ) {
            try {
                result = webAppContextXml.toURI().toURL();
            } catch ( MalformedURLException e ) {
                Logger.getLogger ( this.getLoggerName ( this.getHost(), contextName ) ).log ( Level.WARNING, "Unable to determine web application context.xml " + docBase, e );
            }
        }
        return result;
    }
    private URL getWebappConfigFileFromJar ( final File docBase, final String contextName ) {
        URL result = null;
        try ( final JarFile jar = new JarFile ( docBase ) ) {
            final JarEntry entry = jar.getJarEntry ( "META-INF/context.xml" );
            if ( entry != null ) {
                result = UriUtil.buildJarUrl ( docBase, "META-INF/context.xml" );
            }
        } catch ( IOException e ) {
            Logger.getLogger ( this.getLoggerName ( this.getHost(), contextName ) ).log ( Level.WARNING, "Unable to determine web application context.xml " + docBase, e );
        }
        return result;
    }
    static {
        silences = new String[] { "org.apache.coyote.http11.Http11NioProtocol", "org.apache.catalina.core.StandardService", "org.apache.catalina.core.StandardEngine", "org.apache.catalina.startup.ContextConfig", "org.apache.catalina.core.ApplicationContext", "org.apache.catalina.core.AprLifecycleListener" };
        DEFAULT_MIME_MAPPINGS = new String[] { "abs", "audio/x-mpeg", "ai", "application/postscript", "aif", "audio/x-aiff", "aifc", "audio/x-aiff", "aiff", "audio/x-aiff", "aim", "application/x-aim", "art", "image/x-jg", "asf", "video/x-ms-asf", "asx", "video/x-ms-asf", "au", "audio/basic", "avi", "video/x-msvideo", "avx", "video/x-rad-screenplay", "bcpio", "application/x-bcpio", "bin", "application/octet-stream", "bmp", "image/bmp", "body", "text/html", "cdf", "application/x-cdf", "cer", "application/pkix-cert", "class", "application/java", "cpio", "application/x-cpio", "csh", "application/x-csh", "css", "text/css", "dib", "image/bmp", "doc", "application/msword", "dtd", "application/xml-dtd", "dv", "video/x-dv", "dvi", "application/x-dvi", "eps", "application/postscript", "etx", "text/x-setext", "exe", "application/octet-stream", "gif", "image/gif", "gtar", "application/x-gtar", "gz", "application/x-gzip", "hdf", "application/x-hdf", "hqx", "application/mac-binhex40", "htc", "text/x-component", "htm", "text/html", "html", "text/html", "ief", "image/ief", "jad", "text/vnd.sun.j2me.app-descriptor", "jar", "application/java-archive", "java", "text/x-java-source", "jnlp", "application/x-java-jnlp-file", "jpe", "image/jpeg", "jpeg", "image/jpeg", "jpg", "image/jpeg", "js", "application/javascript", "jsf", "text/plain", "jspf", "text/plain", "kar", "audio/midi", "latex", "application/x-latex", "m3u", "audio/x-mpegurl", "mac", "image/x-macpaint", "man", "text/troff", "mathml", "application/mathml+xml", "me", "text/troff", "mid", "audio/midi", "midi", "audio/midi", "mif", "application/x-mif", "mov", "video/quicktime", "movie", "video/x-sgi-movie", "mp1", "audio/mpeg", "mp2", "audio/mpeg", "mp3", "audio/mpeg", "mp4", "video/mp4", "mpa", "audio/mpeg", "mpe", "video/mpeg", "mpeg", "video/mpeg", "mpega", "audio/x-mpeg", "mpg", "video/mpeg", "mpv2", "video/mpeg2", "nc", "application/x-netcdf", "oda", "application/oda", "odb", "application/vnd.oasis.opendocument.database", "odc", "application/vnd.oasis.opendocument.chart", "odf", "application/vnd.oasis.opendocument.formula", "odg", "application/vnd.oasis.opendocument.graphics", "odi", "application/vnd.oasis.opendocument.image", "odm", "application/vnd.oasis.opendocument.text-master", "odp", "application/vnd.oasis.opendocument.presentation", "ods", "application/vnd.oasis.opendocument.spreadsheet", "odt", "application/vnd.oasis.opendocument.text", "otg", "application/vnd.oasis.opendocument.graphics-template", "oth", "application/vnd.oasis.opendocument.text-web", "otp", "application/vnd.oasis.opendocument.presentation-template", "ots", "application/vnd.oasis.opendocument.spreadsheet-template ", "ott", "application/vnd.oasis.opendocument.text-template", "ogx", "application/ogg", "ogv", "video/ogg", "oga", "audio/ogg", "ogg", "audio/ogg", "spx", "audio/ogg", "flac", "audio/flac", "anx", "application/annodex", "axa", "audio/annodex", "axv", "video/annodex", "xspf", "application/xspf+xml", "pbm", "image/x-portable-bitmap", "pct", "image/pict", "pdf", "application/pdf", "pgm", "image/x-portable-graymap", "pic", "image/pict", "pict", "image/pict", "pls", "audio/x-scpls", "png", "image/png", "pnm", "image/x-portable-anymap", "pnt", "image/x-macpaint", "ppm", "image/x-portable-pixmap", "ppt", "application/vnd.ms-powerpoint", "pps", "application/vnd.ms-powerpoint", "ps", "application/postscript", "psd", "image/vnd.adobe.photoshop", "qt", "video/quicktime", "qti", "image/x-quicktime", "qtif", "image/x-quicktime", "ras", "image/x-cmu-raster", "rdf", "application/rdf+xml", "rgb", "image/x-rgb", "rm", "application/vnd.rn-realmedia", "roff", "text/troff", "rtf", "application/rtf", "rtx", "text/richtext", "sh", "application/x-sh", "shar", "application/x-shar", "sit", "application/x-stuffit", "snd", "audio/basic", "src", "application/x-wais-source", "sv4cpio", "application/x-sv4cpio", "sv4crc", "application/x-sv4crc", "svg", "image/svg+xml", "svgz", "image/svg+xml", "swf", "application/x-shockwave-flash", "t", "text/troff", "tar", "application/x-tar", "tcl", "application/x-tcl", "tex", "application/x-tex", "texi", "application/x-texinfo", "texinfo", "application/x-texinfo", "tif", "image/tiff", "tiff", "image/tiff", "tr", "text/troff", "tsv", "text/tab-separated-values", "txt", "text/plain", "ulw", "audio/basic", "ustar", "application/x-ustar", "vxml", "application/voicexml+xml", "xbm", "image/x-xbitmap", "xht", "application/xhtml+xml", "xhtml", "application/xhtml+xml", "xls", "application/vnd.ms-excel", "xml", "application/xml", "xpm", "image/x-xpixmap", "xsl", "application/xml", "xslt", "application/xslt+xml", "xul", "application/vnd.mozilla.xul+xml", "xwd", "image/x-xwindowdump", "vsd", "application/vnd.visio", "wav", "audio/x-wav", "wbmp", "image/vnd.wap.wbmp", "wml", "text/vnd.wap.wml", "wmlc", "application/vnd.wap.wmlc", "wmls", "text/vnd.wap.wmlsc", "wmlscriptc", "application/vnd.wap.wmlscriptc", "wmv", "video/x-ms-wmv", "wrl", "model/vrml", "wspolicy", "application/wspolicy+xml", "Z", "application/x-compress", "z", "application/x-compress", "zip", "application/zip" };
    }
    public static class FixContextListener implements LifecycleListener {
        @Override
        public void lifecycleEvent ( final LifecycleEvent event ) {
            try {
                final Context context = ( Context ) event.getLifecycle();
                if ( event.getType().equals ( "configure_start" ) ) {
                    context.setConfigured ( true );
                }
                if ( context.getLoginConfig() == null ) {
                    context.setLoginConfig ( new LoginConfig ( "NONE", null, null, null ) );
                    context.getPipeline().addValve ( new NonLoginAuthenticator() );
                }
            } catch ( ClassCastException e ) {}
        }
    }
    public static class DefaultWebXmlListener implements LifecycleListener {
        @Override
        public void lifecycleEvent ( final LifecycleEvent event ) {
            if ( "before_start".equals ( event.getType() ) ) {
                Tomcat.initWebappDefaults ( ( Context ) event.getLifecycle() );
            }
        }
    }
    public static class ExistingStandardWrapper extends StandardWrapper {
        private final Servlet existing;
        public ExistingStandardWrapper ( final Servlet existing ) {
            this.existing = existing;
            if ( existing instanceof SingleThreadModel ) {
                this.singleThreadModel = true;
                this.instancePool = new Stack<Servlet>();
            }
            this.asyncSupported = hasAsync ( existing );
        }
        private static boolean hasAsync ( final Servlet existing ) {
            boolean result = false;
            final Class<?> clazz = existing.getClass();
            final WebServlet ws = clazz.getAnnotation ( WebServlet.class );
            if ( ws != null ) {
                result = ws.asyncSupported();
            }
            return result;
        }
        @Override
        public synchronized Servlet loadServlet() throws ServletException {
            if ( this.singleThreadModel ) {
                Servlet instance;
                try {
                    instance = ( Servlet ) this.existing.getClass().newInstance();
                } catch ( InstantiationException e ) {
                    throw new ServletException ( ( Throwable ) e );
                } catch ( IllegalAccessException e2 ) {
                    throw new ServletException ( ( Throwable ) e2 );
                }
                instance.init ( ( ServletConfig ) this.facade );
                return instance;
            }
            if ( !this.instanceInitialized ) {
                this.existing.init ( ( ServletConfig ) this.facade );
                this.instanceInitialized = true;
            }
            return this.existing;
        }
        @Override
        public long getAvailable() {
            return 0L;
        }
        @Override
        public boolean isUnavailable() {
            return false;
        }
        @Override
        public Servlet getServlet() {
            return this.existing;
        }
        @Override
        public String getServletClass() {
            return this.existing.getClass().getName();
        }
    }
}
