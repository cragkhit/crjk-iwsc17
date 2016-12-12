package org.apache.catalina.servlets;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.catalina.util.IOTools;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public final class CGIServlet extends HttpServlet {
    private static final Log log = LogFactory.getLog ( CGIServlet.class );
    private static final StringManager sm = StringManager.getManager ( CGIServlet.class );
    private static final long serialVersionUID = 1L;
    private String cgiPathPrefix = null;
    private String cgiExecutable = "perl";
    private List<String> cgiExecutableArgs = null;
    private String parameterEncoding =
        System.getProperty ( "file.encoding", "UTF-8" );
    private long stderrTimeout = 2000;
    private Pattern envHttpHeadersPattern = Pattern.compile (
            "ACCEPT[-0-9A-Z]*|CACHE-CONTROL|COOKIE|HOST|IF-[-0-9A-Z]*|REFERER|USER-AGENT" );
    private static final Object expandFileLock = new Object();
    private final Hashtable<String, String> shellEnv = new Hashtable<>();
    @Override
    public void init ( ServletConfig config ) throws ServletException {
        super.init ( config );
        cgiPathPrefix = getServletConfig().getInitParameter ( "cgiPathPrefix" );
        boolean passShellEnvironment =
            Boolean.parseBoolean ( getServletConfig().getInitParameter ( "passShellEnvironment" ) );
        if ( passShellEnvironment ) {
            shellEnv.putAll ( System.getenv() );
        }
        if ( getServletConfig().getInitParameter ( "executable" ) != null ) {
            cgiExecutable = getServletConfig().getInitParameter ( "executable" );
        }
        if ( getServletConfig().getInitParameter ( "executable-arg-1" ) != null ) {
            List<String> args = new ArrayList<>();
            for ( int i = 1;; i++ ) {
                String arg = getServletConfig().getInitParameter (
                                 "executable-arg-" + i );
                if ( arg == null ) {
                    break;
                }
                args.add ( arg );
            }
            cgiExecutableArgs = args;
        }
        if ( getServletConfig().getInitParameter ( "parameterEncoding" ) != null ) {
            parameterEncoding = getServletConfig().getInitParameter ( "parameterEncoding" );
        }
        if ( getServletConfig().getInitParameter ( "stderrTimeout" ) != null ) {
            stderrTimeout = Long.parseLong ( getServletConfig().getInitParameter (
                                                 "stderrTimeout" ) );
        }
        if ( getServletConfig().getInitParameter ( "envHttpHeaders" ) != null ) {
            envHttpHeadersPattern =
                Pattern.compile ( getServletConfig().getInitParameter ( "envHttpHeaders" ) );
        }
    }
    private void printServletEnvironment ( HttpServletRequest req ) throws IOException {
        log.trace ( "ServletRequest Properties" );
        Enumeration<String> attrs = req.getAttributeNames();
        while ( attrs.hasMoreElements() ) {
            String attr = attrs.nextElement();
            log.trace ( "Request Attribute: " + attr + ": [ " + req.getAttribute ( attr ) + "]" );
        }
        log.trace ( "Character Encoding: [" + req.getCharacterEncoding() + "]" );
        log.trace ( "Content Length: [" + req.getContentLengthLong() + "]" );
        log.trace ( "Content Type: [" + req.getContentType() + "]" );
        Enumeration<Locale> locales = req.getLocales();
        while ( locales.hasMoreElements() ) {
            Locale locale = locales.nextElement();
            log.trace ( "Locale: [" + locale + "]" );
        }
        Enumeration<String> params = req.getParameterNames();
        while ( params.hasMoreElements() ) {
            String param = params.nextElement();
            for ( String value : req.getParameterValues ( param ) ) {
                log.trace ( "Request Parameter: " + param + ":  [" + value + "]" );
            }
        }
        log.trace ( "Protocol: [" + req.getProtocol() + "]" );
        log.trace ( "Remote Address: [" + req.getRemoteAddr() + "]" );
        log.trace ( "Remote Host: [" + req.getRemoteHost() + "]" );
        log.trace ( "Scheme: [" + req.getScheme() + "]" );
        log.trace ( "Secure: [" + req.isSecure() + "]" );
        log.trace ( "Server Name: [" + req.getServerName() + "]" );
        log.trace ( "Server Port: [" + req.getServerPort() + "]" );
        log.trace ( "HttpServletRequest Properties" );
        log.trace ( "Auth Type: [" + req.getAuthType() + "]" );
        log.trace ( "Context Path: [" + req.getContextPath() + "]" );
        Cookie cookies[] = req.getCookies();
        if ( cookies != null ) {
            for ( Cookie cookie : cookies ) {
                log.trace ( "Cookie: " + cookie.getName() + ": [" + cookie.getValue() + "]" );
            }
        }
        Enumeration<String> headers = req.getHeaderNames();
        while ( headers.hasMoreElements() ) {
            String header = headers.nextElement();
            log.trace ( "HTTP Header: " + header + ": [" + req.getHeader ( header ) + "]" );
        }
        log.trace ( "Method: [" + req.getMethod() + "]" );
        log.trace ( "Path Info: [" + req.getPathInfo() + "]" );
        log.trace ( "Path Translated: [" + req.getPathTranslated() + "]" );
        log.trace ( "Query String: [" + req.getQueryString() + "]" );
        log.trace ( "Remote User: [" + req.getRemoteUser() + "]" );
        log.trace ( "Requested Session ID: [" + req.getRequestedSessionId() + "]" );
        log.trace ( "Requested Session ID From Cookie: [" +
                    req.isRequestedSessionIdFromCookie() + "]" );
        log.trace ( "Requested Session ID From URL: [" + req.isRequestedSessionIdFromURL() + "]" );
        log.trace ( "Requested Session ID Valid: [" + req.isRequestedSessionIdValid() + "]" );
        log.trace ( "Request URI: [" + req.getRequestURI() + "]" );
        log.trace ( "Servlet Path: [" + req.getServletPath() + "]" );
        log.trace ( "User Principal: [" + req.getUserPrincipal() + "]" );
        HttpSession session = req.getSession ( false );
        if ( session != null ) {
            log.trace ( "HttpSession Properties" );
            log.trace ( "ID: [" + session.getId() + "]" );
            log.trace ( "Creation Time: [" + new Date ( session.getCreationTime() ) + "]" );
            log.trace ( "Last Accessed Time: [" + new Date ( session.getLastAccessedTime() ) + "]" );
            log.trace ( "Max Inactive Interval: [" + session.getMaxInactiveInterval() + "]" );
            attrs = session.getAttributeNames();
            while ( attrs.hasMoreElements() ) {
                String attr = attrs.nextElement();
                log.trace ( "Session Attribute: " + attr + ": [" + session.getAttribute ( attr ) + "]" );
            }
        }
        log.trace ( "ServletConfig Properties" );
        log.trace ( "Servlet Name: [" + getServletConfig().getServletName() + "]" );
        params = getServletConfig().getInitParameterNames();
        while ( params.hasMoreElements() ) {
            String param = params.nextElement();
            String value = getServletConfig().getInitParameter ( param );
            log.trace ( "Servlet Init Param: " + param + ": [" + value + "]" );
        }
        log.trace ( "ServletContext Properties" );
        log.trace ( "Major Version: [" + getServletContext().getMajorVersion() + "]" );
        log.trace ( "Minor Version: [" + getServletContext().getMinorVersion() + "]" );
        log.trace ( "Real Path for '/': [" + getServletContext().getRealPath ( "/" ) + "]" );
        log.trace ( "Server Info: [" + getServletContext().getServerInfo() + "]" );
        log.trace ( "ServletContext Initialization Parameters" );
        params = getServletContext().getInitParameterNames();
        while ( params.hasMoreElements() ) {
            String param = params.nextElement();
            String value = getServletContext().getInitParameter ( param );
            log.trace ( "Servlet Context Init Param: " + param + ": [" + value + "]" );
        }
        log.trace ( "ServletContext Attributes" );
        attrs = getServletContext().getAttributeNames();
        while ( attrs.hasMoreElements() ) {
            String attr = attrs.nextElement();
            log.trace ( "Servlet Context Attribute: " + attr +
                        ": [" + getServletContext().getAttribute ( attr ) + "]" );
        }
    }
    @Override
    protected void doPost ( HttpServletRequest req, HttpServletResponse res )
    throws IOException, ServletException {
        doGet ( req, res );
    }
    @Override
    protected void doGet ( HttpServletRequest req, HttpServletResponse res )
    throws ServletException, IOException {
        CGIEnvironment cgiEnv = new CGIEnvironment ( req, getServletContext() );
        if ( cgiEnv.isValid() ) {
            CGIRunner cgi = new CGIRunner ( cgiEnv.getCommand(),
                                            cgiEnv.getEnvironment(),
                                            cgiEnv.getWorkingDirectory(),
                                            cgiEnv.getParameters() );
            if ( "POST".equals ( req.getMethod() ) ) {
                cgi.setInput ( req.getInputStream() );
            }
            cgi.setResponse ( res );
            cgi.run();
        } else {
            res.sendError ( 404 );
        }
        if ( log.isTraceEnabled() ) {
            String[] cgiEnvLines = cgiEnv.toString().split ( System.lineSeparator() );
            for ( String cgiEnvLine : cgiEnvLines ) {
                log.trace ( cgiEnvLine );
            }
            printServletEnvironment ( req );
        }
    }
    private boolean setStatus ( HttpServletResponse response, int status ) throws IOException {
        if ( status >= HttpServletResponse.SC_BAD_REQUEST ) {
            response.sendError ( status );
            return true;
        } else {
            response.setStatus ( status );
            return false;
        }
    }
    protected class CGIEnvironment {
        private ServletContext context = null;
        private String contextPath = null;
        private String servletPath = null;
        private String pathInfo = null;
        private String webAppRootDir = null;
        private File tmpDir = null;
        private Hashtable<String, String> env = null;
        private String command = null;
        private final File workingDirectory;
        private final ArrayList<String> cmdLineParameters = new ArrayList<>();
        private final boolean valid;
        protected CGIEnvironment ( HttpServletRequest req,
                                   ServletContext context ) throws IOException {
            setupFromContext ( context );
            setupFromRequest ( req );
            this.valid = setCGIEnvironment ( req );
            if ( this.valid ) {
                workingDirectory = new File ( command.substring ( 0,
                                              command.lastIndexOf ( File.separator ) ) );
            } else {
                workingDirectory = null;
            }
        }
        protected void setupFromContext ( ServletContext context ) {
            this.context = context;
            this.webAppRootDir = context.getRealPath ( "/" );
            this.tmpDir = ( File ) context.getAttribute ( ServletContext.TEMPDIR );
        }
        protected void setupFromRequest ( HttpServletRequest req )
        throws UnsupportedEncodingException {
            boolean isIncluded = false;
            if ( req.getAttribute (
                        RequestDispatcher.INCLUDE_REQUEST_URI ) != null ) {
                isIncluded = true;
            }
            if ( isIncluded ) {
                this.contextPath = ( String ) req.getAttribute (
                                       RequestDispatcher.INCLUDE_CONTEXT_PATH );
                this.servletPath = ( String ) req.getAttribute (
                                       RequestDispatcher.INCLUDE_SERVLET_PATH );
                this.pathInfo = ( String ) req.getAttribute (
                                    RequestDispatcher.INCLUDE_PATH_INFO );
            } else {
                this.contextPath = req.getContextPath();
                this.servletPath = req.getServletPath();
                this.pathInfo = req.getPathInfo();
            }
            if ( this.pathInfo == null ) {
                this.pathInfo = this.servletPath;
            }
            if ( req.getMethod().equals ( "GET" )
                    || req.getMethod().equals ( "POST" )
                    || req.getMethod().equals ( "HEAD" ) ) {
                String qs;
                if ( isIncluded ) {
                    qs = ( String ) req.getAttribute (
                             RequestDispatcher.INCLUDE_QUERY_STRING );
                } else {
                    qs = req.getQueryString();
                }
                if ( qs != null && qs.indexOf ( '=' ) == -1 ) {
                    StringTokenizer qsTokens = new StringTokenizer ( qs, "+" );
                    while ( qsTokens.hasMoreTokens() ) {
                        cmdLineParameters.add ( URLDecoder.decode ( qsTokens.nextToken(),
                                                parameterEncoding ) );
                    }
                }
            }
        }
        protected String[] findCGI ( String pathInfo, String webAppRootDir,
                                     String contextPath, String servletPath,
                                     String cgiPathPrefix ) {
            String path = null;
            String name = null;
            String scriptname = null;
            if ( webAppRootDir != null &&
                    webAppRootDir.lastIndexOf ( File.separator ) == ( webAppRootDir.length() - 1 ) ) {
                webAppRootDir = webAppRootDir.substring ( 0, ( webAppRootDir.length() - 1 ) );
            }
            if ( cgiPathPrefix != null ) {
                webAppRootDir = webAppRootDir + File.separator + cgiPathPrefix;
            }
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "cgiServlet.find.path", pathInfo, webAppRootDir ) );
            }
            File currentLocation = new File ( webAppRootDir );
            StringTokenizer dirWalker = new StringTokenizer ( pathInfo, "/" );
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "cgiServlet.find.location",
                                           currentLocation.getAbsolutePath() ) );
            }
            StringBuilder cginameBuilder = new StringBuilder();
            while ( !currentLocation.isFile() && dirWalker.hasMoreElements() ) {
                String nextElement = ( String ) dirWalker.nextElement();
                currentLocation = new File ( currentLocation, nextElement );
                cginameBuilder.append ( '/' ).append ( nextElement );
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "cgiServlet.find.location",
                                               currentLocation.getAbsolutePath() ) );
                }
            }
            String cginame = cginameBuilder.toString();
            if ( !currentLocation.isFile() ) {
                return new String[] { null, null, null, null };
            }
            path = currentLocation.getAbsolutePath();
            name = currentLocation.getName();
            if ( ".".equals ( contextPath ) ) {
                scriptname = servletPath;
            } else {
                scriptname = contextPath + servletPath;
            }
            if ( !servletPath.equals ( cginame ) ) {
                scriptname = scriptname + cginame;
            }
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "cgiServlet.find.found", name, path, scriptname, cginame ) );
            }
            return new String[] { path, scriptname, cginame, name };
        }
        protected boolean setCGIEnvironment ( HttpServletRequest req ) throws IOException {
            Hashtable<String, String> envp = new Hashtable<>();
            envp.putAll ( shellEnv );
            String sPathInfoOrig = null;
            String sPathInfoCGI = null;
            String sPathTranslatedCGI = null;
            String sCGIFullPath = null;
            String sCGIScriptName = null;
            String sCGIFullName = null;
            String sCGIName = null;
            String[] sCGINames;
            sPathInfoOrig = this.pathInfo;
            sPathInfoOrig = sPathInfoOrig == null ? "" : sPathInfoOrig;
            if ( webAppRootDir == null ) {
                webAppRootDir = tmpDir.toString();
                expandCGIScript();
            }
            sCGINames = findCGI ( sPathInfoOrig,
                                  webAppRootDir,
                                  contextPath,
                                  servletPath,
                                  cgiPathPrefix );
            sCGIFullPath = sCGINames[0];
            sCGIScriptName = sCGINames[1];
            sCGIFullName = sCGINames[2];
            sCGIName = sCGINames[3];
            if ( sCGIFullPath == null
                    || sCGIScriptName == null
                    || sCGIFullName == null
                    || sCGIName == null ) {
                return false;
            }
            envp.put ( "SERVER_SOFTWARE", "TOMCAT" );
            envp.put ( "SERVER_NAME", nullsToBlanks ( req.getServerName() ) );
            envp.put ( "GATEWAY_INTERFACE", "CGI/1.1" );
            envp.put ( "SERVER_PROTOCOL", nullsToBlanks ( req.getProtocol() ) );
            int port = req.getServerPort();
            Integer iPort =
                ( port == 0 ? Integer.valueOf ( -1 ) : Integer.valueOf ( port ) );
            envp.put ( "SERVER_PORT", iPort.toString() );
            envp.put ( "REQUEST_METHOD", nullsToBlanks ( req.getMethod() ) );
            envp.put ( "REQUEST_URI", nullsToBlanks ( req.getRequestURI() ) );
            if ( pathInfo == null
                    || ( pathInfo.substring ( sCGIFullName.length() ).length() <= 0 ) ) {
                sPathInfoCGI = "";
            } else {
                sPathInfoCGI = pathInfo.substring ( sCGIFullName.length() );
            }
            envp.put ( "PATH_INFO", sPathInfoCGI );
            if ( ! ( "".equals ( sPathInfoCGI ) ) ) {
                sPathTranslatedCGI = context.getRealPath ( sPathInfoCGI );
            }
            if ( sPathTranslatedCGI == null || "".equals ( sPathTranslatedCGI ) ) {
            } else {
                envp.put ( "PATH_TRANSLATED", nullsToBlanks ( sPathTranslatedCGI ) );
            }
            envp.put ( "SCRIPT_NAME", nullsToBlanks ( sCGIScriptName ) );
            envp.put ( "QUERY_STRING", nullsToBlanks ( req.getQueryString() ) );
            envp.put ( "REMOTE_HOST", nullsToBlanks ( req.getRemoteHost() ) );
            envp.put ( "REMOTE_ADDR", nullsToBlanks ( req.getRemoteAddr() ) );
            envp.put ( "AUTH_TYPE", nullsToBlanks ( req.getAuthType() ) );
            envp.put ( "REMOTE_USER", nullsToBlanks ( req.getRemoteUser() ) );
            envp.put ( "REMOTE_IDENT", "" );
            envp.put ( "CONTENT_TYPE", nullsToBlanks ( req.getContentType() ) );
            long contentLength = req.getContentLengthLong();
            String sContentLength = ( contentLength <= 0 ? "" :
                                      Long.toString ( contentLength ) );
            envp.put ( "CONTENT_LENGTH", sContentLength );
            Enumeration<String> headers = req.getHeaderNames();
            String header = null;
            while ( headers.hasMoreElements() ) {
                header = null;
                header = headers.nextElement().toUpperCase ( Locale.ENGLISH );
                if ( envHttpHeadersPattern.matcher ( header ).matches() ) {
                    envp.put ( "HTTP_" + header.replace ( '-', '_' ), req.getHeader ( header ) );
                }
            }
            File fCGIFullPath = new File ( sCGIFullPath );
            command = fCGIFullPath.getCanonicalPath();
            envp.put ( "X_TOMCAT_SCRIPT_PATH", command );
            envp.put ( "SCRIPT_FILENAME", command );
            this.env = envp;
            return true;
        }
        protected void expandCGIScript() {
            StringBuilder srcPath = new StringBuilder();
            StringBuilder destPath = new StringBuilder();
            InputStream is = null;
            if ( cgiPathPrefix == null ) {
                srcPath.append ( pathInfo );
                is = context.getResourceAsStream ( srcPath.toString() );
                destPath.append ( tmpDir );
                destPath.append ( pathInfo );
            } else {
                srcPath.append ( cgiPathPrefix );
                StringTokenizer pathWalker =
                    new StringTokenizer ( pathInfo, "/" );
                while ( pathWalker.hasMoreElements() && ( is == null ) ) {
                    srcPath.append ( "/" );
                    srcPath.append ( pathWalker.nextElement() );
                    is = context.getResourceAsStream ( srcPath.toString() );
                }
                destPath.append ( tmpDir );
                destPath.append ( "/" );
                destPath.append ( srcPath );
            }
            if ( is == null ) {
                log.warn ( sm.getString ( "cgiServlet.expandNotFound", srcPath ) );
                return;
            }
            File f = new File ( destPath.toString() );
            if ( f.exists() ) {
                try {
                    is.close();
                } catch ( IOException e ) {
                    log.warn ( sm.getString ( "cgiServlet.expandCloseFail", srcPath ), e );
                }
                return;
            }
            File dir = f.getParentFile();
            if ( !dir.mkdirs() && !dir.isDirectory() ) {
                log.warn ( sm.getString ( "cgiServlet.expandCreateDirFail", dir.getAbsolutePath() ) );
                return;
            }
            try {
                synchronized ( expandFileLock ) {
                    if ( f.exists() ) {
                        return;
                    }
                    if ( !f.createNewFile() ) {
                        return;
                    }
                    try {
                        Files.copy ( is, f.toPath() );
                    } finally {
                        is.close();
                    }
                    if ( log.isDebugEnabled() ) {
                        log.debug ( sm.getString ( "cgiServlet.expandOk", srcPath, destPath ) );
                    }
                }
            } catch ( IOException ioe ) {
                log.warn ( sm.getString ( "cgiServlet.expandFail", srcPath, destPath ), ioe );
                if ( f.exists() ) {
                    if ( !f.delete() ) {
                        log.warn ( sm.getString ( "cgiServlet.expandDeleteFail", f.getAbsolutePath() ) );
                    }
                }
            }
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append ( "CGIEnvironment Info:" );
            sb.append ( System.lineSeparator() );
            if ( isValid() ) {
                sb.append ( "Validity: [true]" );
                sb.append ( System.lineSeparator() );
                sb.append ( "Environment values:" );
                sb.append ( System.lineSeparator() );
                for ( Entry<String, String> entry : env.entrySet() ) {
                    sb.append ( "  " );
                    sb.append ( entry.getKey() );
                    sb.append ( ": [" );
                    sb.append ( blanksToString ( entry.getValue(), "will be set to blank" ) );
                    sb.append ( "]" );
                    sb.append ( System.lineSeparator() );
                }
                sb.append ( "Derived Command :[" );
                sb.append ( nullsToBlanks ( command ) );
                sb.append ( "]" );
                sb.append ( System.lineSeparator() );
                sb.append ( "Working Directory: [" );
                if ( workingDirectory != null ) {
                    sb.append ( workingDirectory.toString() );
                }
                sb.append ( "]" );
                sb.append ( System.lineSeparator() );
                sb.append ( "Command Line Params:" );
                sb.append ( System.lineSeparator() );
                for ( String param : cmdLineParameters ) {
                    sb.append ( "  [" );
                    sb.append ( param );
                    sb.append ( "]" );
                    sb.append ( System.lineSeparator() );
                }
            } else {
                sb.append ( "Validity: [false]" );
                sb.append ( System.lineSeparator() );
                sb.append ( "CGI script not found or not specified." );
                sb.append ( System.lineSeparator() );
                sb.append ( "Check the HttpServletRequest pathInfo property to see if it is what " );
                sb.append ( System.lineSeparator() );
                sb.append ( "you meant it to be. You must specify an existant and executable file " );
                sb.append ( System.lineSeparator() );
                sb.append ( "as part of the path-info." );
                sb.append ( System.lineSeparator() );
            }
            return sb.toString();
        }
        protected String getCommand() {
            return command;
        }
        protected File getWorkingDirectory() {
            return workingDirectory;
        }
        protected Hashtable<String, String> getEnvironment() {
            return env;
        }
        protected ArrayList<String> getParameters() {
            return cmdLineParameters;
        }
        protected boolean isValid() {
            return valid;
        }
        protected String nullsToBlanks ( String s ) {
            return nullsToString ( s, "" );
        }
        protected String nullsToString ( String couldBeNull,
                                         String subForNulls ) {
            return ( couldBeNull == null ? subForNulls : couldBeNull );
        }
        protected String blanksToString ( String couldBeBlank,
                                          String subForBlanks ) {
            return ( ( "".equals ( couldBeBlank ) || couldBeBlank == null )
                     ? subForBlanks
                     : couldBeBlank );
        }
    }
    protected class CGIRunner {
        private final String command;
        private final Hashtable<String, String> env;
        private final File wd;
        private final ArrayList<String> params;
        private InputStream stdin = null;
        private HttpServletResponse response = null;
        private boolean readyToRun = false;
        protected CGIRunner ( String command, Hashtable<String, String> env,
                              File wd, ArrayList<String> params ) {
            this.command = command;
            this.env = env;
            this.wd = wd;
            this.params = params;
            updateReadyStatus();
        }
        protected void updateReadyStatus() {
            if ( command != null
                    && env != null
                    && wd != null
                    && params != null
                    && response != null ) {
                readyToRun = true;
            } else {
                readyToRun = false;
            }
        }
        protected boolean isReady() {
            return readyToRun;
        }
        protected void setResponse ( HttpServletResponse response ) {
            this.response = response;
            updateReadyStatus();
        }
        protected void setInput ( InputStream stdin ) {
            this.stdin = stdin;
            updateReadyStatus();
        }
        protected String[] hashToStringArray ( Hashtable<String, ?> h )
        throws NullPointerException {
            Vector<String> v = new Vector<>();
            Enumeration<String> e = h.keys();
            while ( e.hasMoreElements() ) {
                String k = e.nextElement();
                v.add ( k + "=" + h.get ( k ).toString() );
            }
            String[] strArr = new String[v.size()];
            v.copyInto ( strArr );
            return strArr;
        }
        protected void run() throws IOException {
            if ( !isReady() ) {
                throw new IOException ( this.getClass().getName() + ": not ready to run." );
            }
            if ( log.isDebugEnabled() ) {
                log.debug ( "envp: [" + env + "], command: [" + command + "]" );
            }
            if ( ( command.indexOf ( File.separator + "." + File.separator ) >= 0 )
                    || ( command.indexOf ( File.separator + ".." ) >= 0 )
                    || ( command.indexOf ( ".." + File.separator ) >= 0 ) ) {
                throw new IOException ( this.getClass().getName()
                                        + "Illegal Character in CGI command "
                                        + "path ('.' or '..') detected.  Not "
                                        + "running CGI [" + command + "]." );
            }
            Runtime rt = null;
            BufferedReader cgiHeaderReader = null;
            InputStream cgiOutput = null;
            BufferedReader commandsStdErr = null;
            Thread errReaderThread = null;
            BufferedOutputStream commandsStdIn = null;
            Process proc = null;
            int bufRead = -1;
            List<String> cmdAndArgs = new ArrayList<>();
            if ( cgiExecutable.length() != 0 ) {
                cmdAndArgs.add ( cgiExecutable );
            }
            if ( cgiExecutableArgs != null ) {
                cmdAndArgs.addAll ( cgiExecutableArgs );
            }
            cmdAndArgs.add ( command );
            cmdAndArgs.addAll ( params );
            try {
                rt = Runtime.getRuntime();
                proc = rt.exec (
                           cmdAndArgs.toArray ( new String[cmdAndArgs.size()] ),
                           hashToStringArray ( env ), wd );
                String sContentLength = env.get ( "CONTENT_LENGTH" );
                if ( !"".equals ( sContentLength ) ) {
                    commandsStdIn = new BufferedOutputStream ( proc.getOutputStream() );
                    IOTools.flow ( stdin, commandsStdIn );
                    commandsStdIn.flush();
                    commandsStdIn.close();
                }
                boolean isRunning = true;
                commandsStdErr = new BufferedReader
                ( new InputStreamReader ( proc.getErrorStream() ) );
                final BufferedReader stdErrRdr = commandsStdErr ;
                errReaderThread = new Thread() {
                    @Override
                    public void run () {
                        sendToLog ( stdErrRdr );
                    }
                };
                errReaderThread.start();
                InputStream cgiHeaderStream =
                    new HTTPHeaderInputStream ( proc.getInputStream() );
                cgiHeaderReader =
                    new BufferedReader ( new InputStreamReader ( cgiHeaderStream ) );
                boolean skipBody = false;
                while ( isRunning ) {
                    try {
                        String line = null;
                        while ( ( ( line = cgiHeaderReader.readLine() ) != null ) && ! ( "".equals ( line ) ) ) {
                            if ( log.isTraceEnabled() ) {
                                log.trace ( "addHeader(\"" + line + "\")" );
                            }
                            if ( line.startsWith ( "HTTP" ) ) {
                                skipBody = setStatus ( response, getSCFromHttpStatusLine ( line ) );
                            } else if ( line.indexOf ( ':' ) >= 0 ) {
                                String header =
                                    line.substring ( 0, line.indexOf ( ':' ) ).trim();
                                String value =
                                    line.substring ( line.indexOf ( ':' ) + 1 ).trim();
                                if ( header.equalsIgnoreCase ( "status" ) ) {
                                    skipBody = setStatus ( response, getSCFromCGIStatusHeader ( value ) );
                                } else {
                                    response.addHeader ( header , value );
                                }
                            } else {
                                log.info ( sm.getString ( "cgiServlet.runBadHeader", line ) );
                            }
                        }
                        byte[] bBuf = new byte[2048];
                        OutputStream out = response.getOutputStream();
                        cgiOutput = proc.getInputStream();
                        try {
                            while ( !skipBody && ( bufRead = cgiOutput.read ( bBuf ) ) != -1 ) {
                                if ( log.isTraceEnabled() ) {
                                    log.trace ( "output " + bufRead + " bytes of data" );
                                }
                                out.write ( bBuf, 0, bufRead );
                            }
                        } finally {
                            if ( bufRead != -1 ) {
                                while ( ( bufRead = cgiOutput.read ( bBuf ) ) != -1 ) {
                                }
                            }
                        }
                        proc.exitValue();
                        isRunning = false;
                    } catch ( IllegalThreadStateException e ) {
                        try {
                            Thread.sleep ( 500 );
                        } catch ( InterruptedException ignored ) {
                        }
                    }
                }
            } catch ( IOException e ) {
                log.warn ( sm.getString ( "cgiServlet.runFail" ), e );
                throw e;
            } finally {
                if ( cgiHeaderReader != null ) {
                    try {
                        cgiHeaderReader.close();
                    } catch ( IOException ioe ) {
                        log.warn ( sm.getString ( "cgiServlet.runHeaderReaderFail" ), ioe );
                    }
                }
                if ( cgiOutput != null ) {
                    try {
                        cgiOutput.close();
                    } catch ( IOException ioe ) {
                        log.warn ( sm.getString ( "cgiServlet.runOutputStreamFail" ), ioe );
                    }
                }
                if ( errReaderThread != null ) {
                    try {
                        errReaderThread.join ( stderrTimeout );
                    } catch ( InterruptedException e ) {
                        log.warn ( sm.getString ( "cgiServlet.runReaderInterupt" ) );
                    }
                }
                if ( proc != null ) {
                    proc.destroy();
                    proc = null;
                }
            }
        }
        private int getSCFromHttpStatusLine ( String line ) {
            int statusStart = line.indexOf ( ' ' ) + 1;
            if ( statusStart < 1 || line.length() < statusStart + 3 ) {
                log.warn ( sm.getString ( "cgiServlet.runInvalidStatus", line ) );
                return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }
            String status = line.substring ( statusStart, statusStart + 3 );
            int statusCode;
            try {
                statusCode = Integer.parseInt ( status );
            } catch ( NumberFormatException nfe ) {
                log.warn ( sm.getString ( "cgiServlet.runInvalidStatus", status ) );
                return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }
            return statusCode;
        }
        private int getSCFromCGIStatusHeader ( String value ) {
            if ( value.length() < 3 ) {
                log.warn ( sm.getString ( "cgiServlet.runInvalidStatus", value ) );
                return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }
            String status = value.substring ( 0, 3 );
            int statusCode;
            try {
                statusCode = Integer.parseInt ( status );
            } catch ( NumberFormatException nfe ) {
                log.warn ( sm.getString ( "cgiServlet.runInvalidStatus", status ) );
                return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }
            return statusCode;
        }
        private void sendToLog ( BufferedReader rdr ) {
            String line = null;
            int lineCount = 0 ;
            try {
                while ( ( line = rdr.readLine() ) != null ) {
                    log.warn ( sm.getString ( "cgiServlet.runStdErr", line ) );
                    lineCount++ ;
                }
            } catch ( IOException e ) {
                log.warn ( sm.getString ( "cgiServlet.runStdErrFail" ), e );
            } finally {
                try {
                    rdr.close();
                } catch ( IOException e ) {
                    log.warn ( sm.getString ( "cgiServlet.runStdErrFail" ), e );
                }
            }
            if ( lineCount > 0 ) {
                log.warn ( sm.getString ( "cgiServlet.runStdErrCount", Integer.valueOf ( lineCount ) ) );
            }
        }
    }
    protected static class HTTPHeaderInputStream extends InputStream {
        private static final int STATE_CHARACTER = 0;
        private static final int STATE_FIRST_CR = 1;
        private static final int STATE_FIRST_LF = 2;
        private static final int STATE_SECOND_CR = 3;
        private static final int STATE_HEADER_END = 4;
        private final InputStream input;
        private int state;
        HTTPHeaderInputStream ( InputStream theInput ) {
            input = theInput;
            state = STATE_CHARACTER;
        }
        @Override
        public int read() throws IOException {
            if ( state == STATE_HEADER_END ) {
                return -1;
            }
            int i = input.read();
            if ( i == 10 ) {
                switch ( state ) {
                case STATE_CHARACTER:
                    state = STATE_FIRST_LF;
                    break;
                case STATE_FIRST_CR:
                    state = STATE_FIRST_LF;
                    break;
                case STATE_FIRST_LF:
                case STATE_SECOND_CR:
                    state = STATE_HEADER_END;
                    break;
                }
            } else if ( i == 13 ) {
                switch ( state ) {
                case STATE_CHARACTER:
                    state = STATE_FIRST_CR;
                    break;
                case STATE_FIRST_CR:
                    state = STATE_HEADER_END;
                    break;
                case STATE_FIRST_LF:
                    state = STATE_SECOND_CR;
                    break;
                }
            } else {
                state = STATE_CHARACTER;
            }
            return i;
        }
    }
}
