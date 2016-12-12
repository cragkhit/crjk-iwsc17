package org.apache.catalina.startup;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Globals;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Realm;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.Wrapper;
import org.apache.catalina.authenticator.NonLoginAuthenticator;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.core.NamingContextListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.RealmBase;
import org.apache.tomcat.util.buf.UriUtil;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
public class Tomcat {
    private final Map<String, Logger> pinnedLoggers = new HashMap<>();
    protected Server server;
    protected int port = 8080;
    protected String hostname = "localhost";
    protected String basedir;
    private final Map<String, String> userPass = new HashMap<>();
    private final Map<String, List<String>> userRoles = new HashMap<>();
    private final Map<String, Principal> userPrincipals = new HashMap<>();
    public Tomcat() {
    }
    public void setBaseDir ( String basedir ) {
        this.basedir = basedir;
    }
    public void setPort ( int port ) {
        this.port = port;
    }
    public void setHostname ( String s ) {
        hostname = s;
    }
    public Context addWebapp ( String contextPath, String docBase ) throws ServletException {
        return addWebapp ( getHost(), contextPath, docBase );
    }
    public Context addContext ( String contextPath, String docBase ) {
        return addContext ( getHost(), contextPath, docBase );
    }
    public Wrapper addServlet ( String contextPath,
                                String servletName,
                                String servletClass ) {
        Container ctx = getHost().findChild ( contextPath );
        return addServlet ( ( Context ) ctx, servletName, servletClass );
    }
    public static Wrapper addServlet ( Context ctx,
                                       String servletName,
                                       String servletClass ) {
        Wrapper sw = ctx.createWrapper();
        sw.setServletClass ( servletClass );
        sw.setName ( servletName );
        ctx.addChild ( sw );
        return sw;
    }
    public Wrapper addServlet ( String contextPath,
                                String servletName,
                                Servlet servlet ) {
        Container ctx = getHost().findChild ( contextPath );
        return addServlet ( ( Context ) ctx, servletName, servlet );
    }
    public static Wrapper addServlet ( Context ctx,
                                       String servletName,
                                       Servlet servlet ) {
        Wrapper sw = new ExistingStandardWrapper ( servlet );
        sw.setName ( servletName );
        ctx.addChild ( sw );
        return sw;
    }
    public void init() throws LifecycleException {
        getServer();
        server.init();
    }
    public void start() throws LifecycleException {
        getServer();
        server.start();
    }
    public void stop() throws LifecycleException {
        getServer();
        server.stop();
    }
    public void destroy() throws LifecycleException {
        getServer();
        server.destroy();
    }
    public void addUser ( String user, String pass ) {
        userPass.put ( user, pass );
    }
    public void addRole ( String user, String role ) {
        List<String> roles = userRoles.get ( user );
        if ( roles == null ) {
            roles = new ArrayList<>();
            userRoles.put ( user, roles );
        }
        roles.add ( role );
    }
    public Connector getConnector() {
        Service service = getService();
        if ( service.findConnectors().length > 0 ) {
            return service.findConnectors() [0];
        }
        Connector connector = new Connector ( "HTTP/1.1" );
        connector.setPort ( port );
        service.addConnector ( connector );
        return connector;
    }
    public void setConnector ( Connector connector ) {
        Service service = getService();
        boolean found = false;
        for ( Connector serviceConnector : service.findConnectors() ) {
            if ( connector == serviceConnector ) {
                found = true;
            }
        }
        if ( !found ) {
            service.addConnector ( connector );
        }
    }
    public Service getService() {
        return getServer().findServices() [0];
    }
    public void setHost ( Host host ) {
        Engine engine = getEngine();
        boolean found = false;
        for ( Container engineHost : engine.findChildren() ) {
            if ( engineHost == host ) {
                found = true;
            }
        }
        if ( !found ) {
            engine.addChild ( host );
        }
    }
    public Host getHost() {
        Engine engine = getEngine();
        if ( engine.findChildren().length > 0 ) {
            return ( Host ) engine.findChildren() [0];
        }
        Host host = new StandardHost();
        host.setName ( hostname );
        getEngine().addChild ( host );
        return host;
    }
    public Engine getEngine() {
        Service service = getServer().findServices() [0];
        if ( service.getContainer() != null ) {
            return service.getContainer();
        }
        Engine engine = new StandardEngine();
        engine.setName ( "Tomcat" );
        engine.setDefaultHost ( hostname );
        engine.setRealm ( createDefaultRealm() );
        service.setContainer ( engine );
        return engine;
    }
    public Server getServer() {
        if ( server != null ) {
            return server;
        }
        System.setProperty ( "catalina.useNaming", "false" );
        server = new StandardServer();
        initBaseDir();
        server.setPort ( -1 );
        Service service = new StandardService();
        service.setName ( "Tomcat" );
        server.addService ( service );
        return server;
    }
    public Context addContext ( Host host, String contextPath, String dir ) {
        return addContext ( host, contextPath, contextPath, dir );
    }
    public Context addContext ( Host host, String contextPath, String contextName,
                                String dir ) {
        silence ( host, contextName );
        Context ctx = createContext ( host, contextPath );
        ctx.setName ( contextName );
        ctx.setPath ( contextPath );
        ctx.setDocBase ( dir );
        ctx.addLifecycleListener ( new FixContextListener() );
        if ( host == null ) {
            getHost().addChild ( ctx );
        } else {
            host.addChild ( ctx );
        }
        return ctx;
    }
    public Context addWebapp ( Host host, String contextPath, String docBase ) {
        LifecycleListener listener = null;
        try {
            Class<?> clazz = Class.forName ( getHost().getConfigClass() );
            listener = ( LifecycleListener ) clazz.newInstance();
        } catch ( ClassNotFoundException | InstantiationException | IllegalAccessException e ) {
            throw new IllegalArgumentException ( e );
        }
        return addWebapp ( host,  contextPath, docBase, listener );
    }
    public Context addWebapp ( Host host, String contextPath, String docBase,
                               LifecycleListener config ) {
        silence ( host, contextPath );
        Context ctx = createContext ( host, contextPath );
        ctx.setPath ( contextPath );
        ctx.setDocBase ( docBase );
        ctx.addLifecycleListener ( new DefaultWebXmlListener() );
        ctx.setConfigFile ( getWebappConfigFile ( docBase, contextPath ) );
        ctx.addLifecycleListener ( config );
        if ( config instanceof ContextConfig ) {
            ( ( ContextConfig ) config ).setDefaultWebXml ( noDefaultWebXmlPath() );
        }
        if ( host == null ) {
            getHost().addChild ( ctx );
        } else {
            host.addChild ( ctx );
        }
        return ctx;
    }
    public LifecycleListener getDefaultWebXmlListener() {
        return new DefaultWebXmlListener();
    }
    public String noDefaultWebXmlPath() {
        return Constants.NoDefaultWebXml;
    }
    protected Realm createDefaultRealm() {
        return new RealmBase() {
            @Override
            protected String getName() {
                return "Simple";
            }
            @Override
            protected String getPassword ( String username ) {
                return userPass.get ( username );
            }
            @Override
            protected Principal getPrincipal ( String username ) {
                Principal p = userPrincipals.get ( username );
                if ( p == null ) {
                    String pass = userPass.get ( username );
                    if ( pass != null ) {
                        p = new GenericPrincipal ( username, pass,
                                                   userRoles.get ( username ) );
                        userPrincipals.put ( username, p );
                    }
                }
                return p;
            }
        };
    }
    protected void initBaseDir() {
        String catalinaHome = System.getProperty ( Globals.CATALINA_HOME_PROP );
        if ( basedir == null ) {
            basedir = System.getProperty ( Globals.CATALINA_BASE_PROP );
        }
        if ( basedir == null ) {
            basedir = catalinaHome;
        }
        if ( basedir == null ) {
            basedir = System.getProperty ( "user.dir" ) +
                      "/tomcat." + port;
        }
        File baseFile = new File ( basedir );
        baseFile.mkdirs();
        try {
            baseFile = baseFile.getCanonicalFile();
        } catch ( IOException e ) {
            baseFile = baseFile.getAbsoluteFile();
        }
        server.setCatalinaBase ( baseFile );
        System.setProperty ( Globals.CATALINA_BASE_PROP, baseFile.getPath() );
        basedir = baseFile.getPath();
        if ( catalinaHome == null ) {
            server.setCatalinaHome ( baseFile );
        } else {
            File homeFile = new File ( catalinaHome );
            homeFile.mkdirs();
            try {
                homeFile = homeFile.getCanonicalFile();
            } catch ( IOException e ) {
                homeFile = homeFile.getAbsoluteFile();
            }
            server.setCatalinaHome ( homeFile );
        }
        System.setProperty ( Globals.CATALINA_HOME_PROP,
                             server.getCatalinaHome().getPath() );
    }
    static final String[] silences = new String[] {
        "org.apache.coyote.http11.Http11NioProtocol",
        "org.apache.catalina.core.StandardService",
        "org.apache.catalina.core.StandardEngine",
        "org.apache.catalina.startup.ContextConfig",
        "org.apache.catalina.core.ApplicationContext",
        "org.apache.catalina.core.AprLifecycleListener"
    };
    private boolean silent = false;
    public void setSilent ( boolean silent ) {
        this.silent = silent;
        for ( String s : silences ) {
            Logger logger = Logger.getLogger ( s );
            pinnedLoggers.put ( s, logger );
            if ( silent ) {
                logger.setLevel ( Level.WARNING );
            } else {
                logger.setLevel ( Level.INFO );
            }
        }
    }
    private void silence ( Host host, String contextPath ) {
        String loggerName = getLoggerName ( host, contextPath );
        Logger logger = Logger.getLogger ( loggerName );
        pinnedLoggers.put ( loggerName, logger );
        if ( silent ) {
            logger.setLevel ( Level.WARNING );
        } else {
            logger.setLevel ( Level.INFO );
        }
    }
    private String getLoggerName ( Host host, String contextName ) {
        if ( host == null ) {
            host = getHost();
        }
        StringBuilder loggerName = new StringBuilder();
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
    private Context createContext ( Host host, String url ) {
        String contextClass = StandardContext.class.getName();
        if ( host == null ) {
            host = this.getHost();
        }
        if ( host instanceof StandardHost ) {
            contextClass = ( ( StandardHost ) host ).getContextClass();
        }
        try {
            return ( Context ) Class.forName ( contextClass ).getConstructor()
                   .newInstance();
        } catch ( InstantiationException | IllegalAccessException
                      | IllegalArgumentException | InvocationTargetException
                      | NoSuchMethodException | SecurityException
                      | ClassNotFoundException e ) {
            throw new IllegalArgumentException (
                "Can't instantiate context-class " + contextClass
                + " for host " + host + " and url "
                + url, e );
        }
    }
    public void enableNaming() {
        getServer();
        server.addLifecycleListener ( new NamingContextListener() );
        System.setProperty ( "catalina.useNaming", "true" );
        String value = "org.apache.naming";
        String oldValue =
            System.getProperty ( javax.naming.Context.URL_PKG_PREFIXES );
        if ( oldValue != null ) {
            if ( oldValue.contains ( value ) ) {
                value = oldValue;
            } else {
                value = value + ":" + oldValue;
            }
        }
        System.setProperty ( javax.naming.Context.URL_PKG_PREFIXES, value );
        value = System.getProperty
                ( javax.naming.Context.INITIAL_CONTEXT_FACTORY );
        if ( value == null ) {
            System.setProperty
            ( javax.naming.Context.INITIAL_CONTEXT_FACTORY,
              "org.apache.naming.java.javaURLContextFactory" );
        }
    }
    public void initWebappDefaults ( String contextPath ) {
        Container ctx = getHost().findChild ( contextPath );
        initWebappDefaults ( ( Context ) ctx );
    }
    public static void initWebappDefaults ( Context ctx ) {
        Wrapper servlet = addServlet (
                              ctx, "default", "org.apache.catalina.servlets.DefaultServlet" );
        servlet.setLoadOnStartup ( 1 );
        servlet.setOverridable ( true );
        servlet = addServlet (
                      ctx, "jsp", "org.apache.jasper.servlet.JspServlet" );
        servlet.addInitParameter ( "fork", "false" );
        servlet.setLoadOnStartup ( 3 );
        servlet.setOverridable ( true );
        ctx.addServletMappingDecoded ( "/", "default" );
        ctx.addServletMappingDecoded ( "*.jsp", "jsp" );
        ctx.addServletMappingDecoded ( "*.jspx", "jsp" );
        ctx.setSessionTimeout ( 30 );
        for ( int i = 0; i < DEFAULT_MIME_MAPPINGS.length; ) {
            ctx.addMimeMapping ( DEFAULT_MIME_MAPPINGS[i++],
                                 DEFAULT_MIME_MAPPINGS[i++] );
        }
        ctx.addWelcomeFile ( "index.html" );
        ctx.addWelcomeFile ( "index.htm" );
        ctx.addWelcomeFile ( "index.jsp" );
    }
    public static class FixContextListener implements LifecycleListener {
        @Override
        public void lifecycleEvent ( LifecycleEvent event ) {
            try {
                Context context = ( Context ) event.getLifecycle();
                if ( event.getType().equals ( Lifecycle.CONFIGURE_START_EVENT ) ) {
                    context.setConfigured ( true );
                }
                if ( context.getLoginConfig() == null ) {
                    context.setLoginConfig (
                        new LoginConfig ( "NONE", null, null, null ) );
                    context.getPipeline().addValve ( new NonLoginAuthenticator() );
                }
            } catch ( ClassCastException e ) {
                return;
            }
        }
    }
    public static class DefaultWebXmlListener implements LifecycleListener {
        @Override
        public void lifecycleEvent ( LifecycleEvent event ) {
            if ( Lifecycle.BEFORE_START_EVENT.equals ( event.getType() ) ) {
                initWebappDefaults ( ( Context ) event.getLifecycle() );
            }
        }
    }
    public static class ExistingStandardWrapper extends StandardWrapper {
        private final Servlet existing;
        @SuppressWarnings ( "deprecation" )
        public ExistingStandardWrapper ( Servlet existing ) {
            this.existing = existing;
            if ( existing instanceof javax.servlet.SingleThreadModel ) {
                singleThreadModel = true;
                instancePool = new Stack<>();
            }
            this.asyncSupported = hasAsync ( existing );
        }
        private static boolean hasAsync ( Servlet existing ) {
            boolean result = false;
            Class<?> clazz = existing.getClass();
            WebServlet ws = clazz.getAnnotation ( WebServlet.class );
            if ( ws != null ) {
                result = ws.asyncSupported();
            }
            return result;
        }
        @Override
        public synchronized Servlet loadServlet() throws ServletException {
            if ( singleThreadModel ) {
                Servlet instance;
                try {
                    instance = existing.getClass().newInstance();
                } catch ( InstantiationException e ) {
                    throw new ServletException ( e );
                } catch ( IllegalAccessException e ) {
                    throw new ServletException ( e );
                }
                instance.init ( facade );
                return instance;
            } else {
                if ( !instanceInitialized ) {
                    existing.init ( facade );
                    instanceInitialized = true;
                }
                return existing;
            }
        }
        @Override
        public long getAvailable() {
            return 0;
        }
        @Override
        public boolean isUnavailable() {
            return false;
        }
        @Override
        public Servlet getServlet() {
            return existing;
        }
        @Override
        public String getServletClass() {
            return existing.getClass().getName();
        }
    }
    private static final String[] DEFAULT_MIME_MAPPINGS = {
        "abs", "audio/x-mpeg",
        "ai", "application/postscript",
        "aif", "audio/x-aiff",
        "aifc", "audio/x-aiff",
        "aiff", "audio/x-aiff",
        "aim", "application/x-aim",
        "art", "image/x-jg",
        "asf", "video/x-ms-asf",
        "asx", "video/x-ms-asf",
        "au", "audio/basic",
        "avi", "video/x-msvideo",
        "avx", "video/x-rad-screenplay",
        "bcpio", "application/x-bcpio",
        "bin", "application/octet-stream",
        "bmp", "image/bmp",
        "body", "text/html",
        "cdf", "application/x-cdf",
        "cer", "application/pkix-cert",
        "class", "application/java",
        "cpio", "application/x-cpio",
        "csh", "application/x-csh",
        "css", "text/css",
        "dib", "image/bmp",
        "doc", "application/msword",
        "dtd", "application/xml-dtd",
        "dv", "video/x-dv",
        "dvi", "application/x-dvi",
        "eps", "application/postscript",
        "etx", "text/x-setext",
        "exe", "application/octet-stream",
        "gif", "image/gif",
        "gtar", "application/x-gtar",
        "gz", "application/x-gzip",
        "hdf", "application/x-hdf",
        "hqx", "application/mac-binhex40",
        "htc", "text/x-component",
        "htm", "text/html",
        "html", "text/html",
        "ief", "image/ief",
        "jad", "text/vnd.sun.j2me.app-descriptor",
        "jar", "application/java-archive",
        "java", "text/x-java-source",
        "jnlp", "application/x-java-jnlp-file",
        "jpe", "image/jpeg",
        "jpeg", "image/jpeg",
        "jpg", "image/jpeg",
        "js", "application/javascript",
        "jsf", "text/plain",
        "jspf", "text/plain",
        "kar", "audio/midi",
        "latex", "application/x-latex",
        "m3u", "audio/x-mpegurl",
        "mac", "image/x-macpaint",
        "man", "text/troff",
        "mathml", "application/mathml+xml",
        "me", "text/troff",
        "mid", "audio/midi",
        "midi", "audio/midi",
        "mif", "application/x-mif",
        "mov", "video/quicktime",
        "movie", "video/x-sgi-movie",
        "mp1", "audio/mpeg",
        "mp2", "audio/mpeg",
        "mp3", "audio/mpeg",
        "mp4", "video/mp4",
        "mpa", "audio/mpeg",
        "mpe", "video/mpeg",
        "mpeg", "video/mpeg",
        "mpega", "audio/x-mpeg",
        "mpg", "video/mpeg",
        "mpv2", "video/mpeg2",
        "nc", "application/x-netcdf",
        "oda", "application/oda",
        "odb", "application/vnd.oasis.opendocument.database",
        "odc", "application/vnd.oasis.opendocument.chart",
        "odf", "application/vnd.oasis.opendocument.formula",
        "odg", "application/vnd.oasis.opendocument.graphics",
        "odi", "application/vnd.oasis.opendocument.image",
        "odm", "application/vnd.oasis.opendocument.text-master",
        "odp", "application/vnd.oasis.opendocument.presentation",
        "ods", "application/vnd.oasis.opendocument.spreadsheet",
        "odt", "application/vnd.oasis.opendocument.text",
        "otg", "application/vnd.oasis.opendocument.graphics-template",
        "oth", "application/vnd.oasis.opendocument.text-web",
        "otp", "application/vnd.oasis.opendocument.presentation-template",
        "ots", "application/vnd.oasis.opendocument.spreadsheet-template ",
        "ott", "application/vnd.oasis.opendocument.text-template",
        "ogx", "application/ogg",
        "ogv", "video/ogg",
        "oga", "audio/ogg",
        "ogg", "audio/ogg",
        "spx", "audio/ogg",
        "flac", "audio/flac",
        "anx", "application/annodex",
        "axa", "audio/annodex",
        "axv", "video/annodex",
        "xspf", "application/xspf+xml",
        "pbm", "image/x-portable-bitmap",
        "pct", "image/pict",
        "pdf", "application/pdf",
        "pgm", "image/x-portable-graymap",
        "pic", "image/pict",
        "pict", "image/pict",
        "pls", "audio/x-scpls",
        "png", "image/png",
        "pnm", "image/x-portable-anymap",
        "pnt", "image/x-macpaint",
        "ppm", "image/x-portable-pixmap",
        "ppt", "application/vnd.ms-powerpoint",
        "pps", "application/vnd.ms-powerpoint",
        "ps", "application/postscript",
        "psd", "image/vnd.adobe.photoshop",
        "qt", "video/quicktime",
        "qti", "image/x-quicktime",
        "qtif", "image/x-quicktime",
        "ras", "image/x-cmu-raster",
        "rdf", "application/rdf+xml",
        "rgb", "image/x-rgb",
        "rm", "application/vnd.rn-realmedia",
        "roff", "text/troff",
        "rtf", "application/rtf",
        "rtx", "text/richtext",
        "sh", "application/x-sh",
        "shar", "application/x-shar",
        "sit", "application/x-stuffit",
        "snd", "audio/basic",
        "src", "application/x-wais-source",
        "sv4cpio", "application/x-sv4cpio",
        "sv4crc", "application/x-sv4crc",
        "svg", "image/svg+xml",
        "svgz", "image/svg+xml",
        "swf", "application/x-shockwave-flash",
        "t", "text/troff",
        "tar", "application/x-tar",
        "tcl", "application/x-tcl",
        "tex", "application/x-tex",
        "texi", "application/x-texinfo",
        "texinfo", "application/x-texinfo",
        "tif", "image/tiff",
        "tiff", "image/tiff",
        "tr", "text/troff",
        "tsv", "text/tab-separated-values",
        "txt", "text/plain",
        "ulw", "audio/basic",
        "ustar", "application/x-ustar",
        "vxml", "application/voicexml+xml",
        "xbm", "image/x-xbitmap",
        "xht", "application/xhtml+xml",
        "xhtml", "application/xhtml+xml",
        "xls", "application/vnd.ms-excel",
        "xml", "application/xml",
        "xpm", "image/x-xpixmap",
        "xsl", "application/xml",
        "xslt", "application/xslt+xml",
        "xul", "application/vnd.mozilla.xul+xml",
        "xwd", "image/x-xwindowdump",
        "vsd", "application/vnd.visio",
        "wav", "audio/x-wav",
        "wbmp", "image/vnd.wap.wbmp",
        "wml", "text/vnd.wap.wml",
        "wmlc", "application/vnd.wap.wmlc",
        "wmls", "text/vnd.wap.wmlsc",
        "wmlscriptc", "application/vnd.wap.wmlscriptc",
        "wmv", "video/x-ms-wmv",
        "wrl", "model/vrml",
        "wspolicy", "application/wspolicy+xml",
        "Z", "application/x-compress",
        "z", "application/x-compress",
        "zip", "application/zip"
    };
    protected URL getWebappConfigFile ( String path, String contextName ) {
        File docBase = new File ( path );
        if ( docBase.isDirectory() ) {
            return getWebappConfigFileFromDirectory ( docBase, contextName );
        } else {
            return getWebappConfigFileFromJar ( docBase, contextName );
        }
    }
    private URL getWebappConfigFileFromDirectory ( File docBase, String contextName ) {
        URL result = null;
        File webAppContextXml = new File ( docBase, Constants.ApplicationContextXml );
        if ( webAppContextXml.exists() ) {
            try {
                result = webAppContextXml.toURI().toURL();
            } catch ( MalformedURLException e ) {
                Logger.getLogger ( getLoggerName ( getHost(), contextName ) ).log ( Level.WARNING,
                        "Unable to determine web application context.xml " + docBase, e );
            }
        }
        return result;
    }
    private URL getWebappConfigFileFromJar ( File docBase, String contextName ) {
        URL result = null;
        try ( JarFile jar = new JarFile ( docBase ) ) {
            JarEntry entry = jar.getJarEntry ( Constants.ApplicationContextXml );
            if ( entry != null ) {
                result = UriUtil.buildJarUrl ( docBase, Constants.ApplicationContextXml );
            }
        } catch ( IOException e ) {
            Logger.getLogger ( getLoggerName ( getHost(), contextName ) ).log ( Level.WARNING,
                    "Unable to determine web application context.xml " + docBase, e );
        }
        return result;
    }
}
