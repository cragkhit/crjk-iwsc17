package org.apache.catalina.servlets;
import java.util.Iterator;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.CopyOption;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.StringTokenizer;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Hashtable;
import java.io.File;
import javax.servlet.ServletContext;
protected class CGIEnvironment {
    private ServletContext context;
    private String contextPath;
    private String servletPath;
    private String pathInfo;
    private String webAppRootDir;
    private File tmpDir;
    private Hashtable<String, String> env;
    private String command;
    private final File workingDirectory;
    private final ArrayList<String> cmdLineParameters;
    private final boolean valid;
    protected CGIEnvironment ( final HttpServletRequest req, final ServletContext context ) throws IOException {
        this.context = null;
        this.contextPath = null;
        this.servletPath = null;
        this.pathInfo = null;
        this.webAppRootDir = null;
        this.tmpDir = null;
        this.env = null;
        this.command = null;
        this.cmdLineParameters = new ArrayList<String>();
        this.setupFromContext ( context );
        this.setupFromRequest ( req );
        this.valid = this.setCGIEnvironment ( req );
        if ( this.valid ) {
            this.workingDirectory = new File ( this.command.substring ( 0, this.command.lastIndexOf ( File.separator ) ) );
        } else {
            this.workingDirectory = null;
        }
    }
    protected void setupFromContext ( final ServletContext context ) {
        this.context = context;
        this.webAppRootDir = context.getRealPath ( "/" );
        this.tmpDir = ( File ) context.getAttribute ( "javax.servlet.context.tempdir" );
    }
    protected void setupFromRequest ( final HttpServletRequest req ) throws UnsupportedEncodingException {
        boolean isIncluded = false;
        if ( req.getAttribute ( "javax.servlet.include.request_uri" ) != null ) {
            isIncluded = true;
        }
        if ( isIncluded ) {
            this.contextPath = ( String ) req.getAttribute ( "javax.servlet.include.context_path" );
            this.servletPath = ( String ) req.getAttribute ( "javax.servlet.include.servlet_path" );
            this.pathInfo = ( String ) req.getAttribute ( "javax.servlet.include.path_info" );
        } else {
            this.contextPath = req.getContextPath();
            this.servletPath = req.getServletPath();
            this.pathInfo = req.getPathInfo();
        }
        if ( this.pathInfo == null ) {
            this.pathInfo = this.servletPath;
        }
        if ( req.getMethod().equals ( "GET" ) || req.getMethod().equals ( "POST" ) || req.getMethod().equals ( "HEAD" ) ) {
            String qs;
            if ( isIncluded ) {
                qs = ( String ) req.getAttribute ( "javax.servlet.include.query_string" );
            } else {
                qs = req.getQueryString();
            }
            if ( qs != null && qs.indexOf ( 61 ) == -1 ) {
                final StringTokenizer qsTokens = new StringTokenizer ( qs, "+" );
                while ( qsTokens.hasMoreTokens() ) {
                    this.cmdLineParameters.add ( URLDecoder.decode ( qsTokens.nextToken(), CGIServlet.access$000 ( CGIServlet.this ) ) );
                }
            }
        }
    }
    protected String[] findCGI ( final String pathInfo, String webAppRootDir, final String contextPath, final String servletPath, final String cgiPathPrefix ) {
        String path = null;
        String name = null;
        String scriptname = null;
        if ( webAppRootDir != null && webAppRootDir.lastIndexOf ( File.separator ) == webAppRootDir.length() - 1 ) {
            webAppRootDir = webAppRootDir.substring ( 0, webAppRootDir.length() - 1 );
        }
        if ( cgiPathPrefix != null ) {
            webAppRootDir = webAppRootDir + File.separator + cgiPathPrefix;
        }
        if ( CGIServlet.access$100().isDebugEnabled() ) {
            CGIServlet.access$100().debug ( CGIServlet.access$200().getString ( "cgiServlet.find.path", pathInfo, webAppRootDir ) );
        }
        File currentLocation = new File ( webAppRootDir );
        final StringTokenizer dirWalker = new StringTokenizer ( pathInfo, "/" );
        if ( CGIServlet.access$100().isDebugEnabled() ) {
            CGIServlet.access$100().debug ( CGIServlet.access$200().getString ( "cgiServlet.find.location", currentLocation.getAbsolutePath() ) );
        }
        final StringBuilder cginameBuilder = new StringBuilder();
        while ( !currentLocation.isFile() && dirWalker.hasMoreElements() ) {
            final String nextElement = ( String ) dirWalker.nextElement();
            currentLocation = new File ( currentLocation, nextElement );
            cginameBuilder.append ( '/' ).append ( nextElement );
            if ( CGIServlet.access$100().isDebugEnabled() ) {
                CGIServlet.access$100().debug ( CGIServlet.access$200().getString ( "cgiServlet.find.location", currentLocation.getAbsolutePath() ) );
            }
        }
        final String cginame = cginameBuilder.toString();
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
            scriptname += cginame;
        }
        if ( CGIServlet.access$100().isDebugEnabled() ) {
            CGIServlet.access$100().debug ( CGIServlet.access$200().getString ( "cgiServlet.find.found", name, path, scriptname, cginame ) );
        }
        return new String[] { path, scriptname, cginame, name };
    }
    protected boolean setCGIEnvironment ( final HttpServletRequest req ) throws IOException {
        final Hashtable<String, String> envp = new Hashtable<String, String>();
        envp.putAll ( CGIServlet.access$300 ( CGIServlet.this ) );
        String sPathInfoOrig = null;
        String sPathInfoCGI = null;
        String sPathTranslatedCGI = null;
        String sCGIFullPath = null;
        String sCGIScriptName = null;
        String sCGIFullName = null;
        String sCGIName = null;
        sPathInfoOrig = this.pathInfo;
        sPathInfoOrig = ( ( sPathInfoOrig == null ) ? "" : sPathInfoOrig );
        if ( this.webAppRootDir == null ) {
            this.webAppRootDir = this.tmpDir.toString();
            this.expandCGIScript();
        }
        final String[] sCGINames = this.findCGI ( sPathInfoOrig, this.webAppRootDir, this.contextPath, this.servletPath, CGIServlet.access$400 ( CGIServlet.this ) );
        sCGIFullPath = sCGINames[0];
        sCGIScriptName = sCGINames[1];
        sCGIFullName = sCGINames[2];
        sCGIName = sCGINames[3];
        if ( sCGIFullPath == null || sCGIScriptName == null || sCGIFullName == null || sCGIName == null ) {
            return false;
        }
        envp.put ( "SERVER_SOFTWARE", "TOMCAT" );
        envp.put ( "SERVER_NAME", this.nullsToBlanks ( req.getServerName() ) );
        envp.put ( "GATEWAY_INTERFACE", "CGI/1.1" );
        envp.put ( "SERVER_PROTOCOL", this.nullsToBlanks ( req.getProtocol() ) );
        final int port = req.getServerPort();
        final Integer iPort = ( port == 0 ) ? -1 : port;
        envp.put ( "SERVER_PORT", iPort.toString() );
        envp.put ( "REQUEST_METHOD", this.nullsToBlanks ( req.getMethod() ) );
        envp.put ( "REQUEST_URI", this.nullsToBlanks ( req.getRequestURI() ) );
        if ( this.pathInfo == null || this.pathInfo.substring ( sCGIFullName.length() ).length() <= 0 ) {
            sPathInfoCGI = "";
        } else {
            sPathInfoCGI = this.pathInfo.substring ( sCGIFullName.length() );
        }
        envp.put ( "PATH_INFO", sPathInfoCGI );
        if ( !"".equals ( sPathInfoCGI ) ) {
            sPathTranslatedCGI = this.context.getRealPath ( sPathInfoCGI );
        }
        if ( sPathTranslatedCGI != null ) {
            if ( !"".equals ( sPathTranslatedCGI ) ) {
                envp.put ( "PATH_TRANSLATED", this.nullsToBlanks ( sPathTranslatedCGI ) );
            }
        }
        envp.put ( "SCRIPT_NAME", this.nullsToBlanks ( sCGIScriptName ) );
        envp.put ( "QUERY_STRING", this.nullsToBlanks ( req.getQueryString() ) );
        envp.put ( "REMOTE_HOST", this.nullsToBlanks ( req.getRemoteHost() ) );
        envp.put ( "REMOTE_ADDR", this.nullsToBlanks ( req.getRemoteAddr() ) );
        envp.put ( "AUTH_TYPE", this.nullsToBlanks ( req.getAuthType() ) );
        envp.put ( "REMOTE_USER", this.nullsToBlanks ( req.getRemoteUser() ) );
        envp.put ( "REMOTE_IDENT", "" );
        envp.put ( "CONTENT_TYPE", this.nullsToBlanks ( req.getContentType() ) );
        final long contentLength = req.getContentLengthLong();
        final String sContentLength = ( contentLength <= 0L ) ? "" : Long.toString ( contentLength );
        envp.put ( "CONTENT_LENGTH", sContentLength );
        final Enumeration<String> headers = ( Enumeration<String> ) req.getHeaderNames();
        String header = null;
        while ( headers.hasMoreElements() ) {
            header = null;
            header = headers.nextElement().toUpperCase ( Locale.ENGLISH );
            if ( CGIServlet.access$500 ( CGIServlet.this ).matcher ( header ).matches() ) {
                envp.put ( "HTTP_" + header.replace ( '-', '_' ), req.getHeader ( header ) );
            }
        }
        final File fCGIFullPath = new File ( sCGIFullPath );
        envp.put ( "X_TOMCAT_SCRIPT_PATH", this.command = fCGIFullPath.getCanonicalPath() );
        envp.put ( "SCRIPT_FILENAME", this.command );
        this.env = envp;
        return true;
    }
    protected void expandCGIScript() {
        final StringBuilder srcPath = new StringBuilder();
        final StringBuilder destPath = new StringBuilder();
        InputStream is = null;
        if ( CGIServlet.access$400 ( CGIServlet.this ) == null ) {
            srcPath.append ( this.pathInfo );
            is = this.context.getResourceAsStream ( srcPath.toString() );
            destPath.append ( this.tmpDir );
            destPath.append ( this.pathInfo );
        } else {
            srcPath.append ( CGIServlet.access$400 ( CGIServlet.this ) );
            for ( StringTokenizer pathWalker = new StringTokenizer ( this.pathInfo, "/" ); pathWalker.hasMoreElements() && is == null; is = this.context.getResourceAsStream ( srcPath.toString() ) ) {
                srcPath.append ( "/" );
                srcPath.append ( pathWalker.nextElement() );
            }
            destPath.append ( this.tmpDir );
            destPath.append ( "/" );
            destPath.append ( ( CharSequence ) srcPath );
        }
        if ( is == null ) {
            CGIServlet.access$100().warn ( CGIServlet.access$200().getString ( "cgiServlet.expandNotFound", srcPath ) );
            return;
        }
        final File f = new File ( destPath.toString() );
        if ( f.exists() ) {
            try {
                is.close();
            } catch ( IOException e ) {
                CGIServlet.access$100().warn ( CGIServlet.access$200().getString ( "cgiServlet.expandCloseFail", srcPath ), e );
            }
            return;
        }
        final File dir = f.getParentFile();
        if ( !dir.mkdirs() && !dir.isDirectory() ) {
            CGIServlet.access$100().warn ( CGIServlet.access$200().getString ( "cgiServlet.expandCreateDirFail", dir.getAbsolutePath() ) );
            return;
        }
        try {
            synchronized ( CGIServlet.access$600() ) {
                if ( f.exists() ) {
                    return;
                }
                if ( !f.createNewFile() ) {
                    return;
                }
                try {
                    Files.copy ( is, f.toPath(), new CopyOption[0] );
                } finally {
                    is.close();
                }
                if ( CGIServlet.access$100().isDebugEnabled() ) {
                    CGIServlet.access$100().debug ( CGIServlet.access$200().getString ( "cgiServlet.expandOk", srcPath, destPath ) );
                }
            }
        } catch ( IOException ioe ) {
            CGIServlet.access$100().warn ( CGIServlet.access$200().getString ( "cgiServlet.expandFail", srcPath, destPath ), ioe );
            if ( f.exists() && !f.delete() ) {
                CGIServlet.access$100().warn ( CGIServlet.access$200().getString ( "cgiServlet.expandDeleteFail", f.getAbsolutePath() ) );
            }
        }
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append ( "CGIEnvironment Info:" );
        sb.append ( System.lineSeparator() );
        if ( this.isValid() ) {
            sb.append ( "Validity: [true]" );
            sb.append ( System.lineSeparator() );
            sb.append ( "Environment values:" );
            sb.append ( System.lineSeparator() );
            for ( final Map.Entry<String, String> entry : this.env.entrySet() ) {
                sb.append ( "  " );
                sb.append ( entry.getKey() );
                sb.append ( ": [" );
                sb.append ( this.blanksToString ( entry.getValue(), "will be set to blank" ) );
                sb.append ( "]" );
                sb.append ( System.lineSeparator() );
            }
            sb.append ( "Derived Command :[" );
            sb.append ( this.nullsToBlanks ( this.command ) );
            sb.append ( "]" );
            sb.append ( System.lineSeparator() );
            sb.append ( "Working Directory: [" );
            if ( this.workingDirectory != null ) {
                sb.append ( this.workingDirectory.toString() );
            }
            sb.append ( "]" );
            sb.append ( System.lineSeparator() );
            sb.append ( "Command Line Params:" );
            sb.append ( System.lineSeparator() );
            for ( final String param : this.cmdLineParameters ) {
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
        return this.command;
    }
    protected File getWorkingDirectory() {
        return this.workingDirectory;
    }
    protected Hashtable<String, String> getEnvironment() {
        return this.env;
    }
    protected ArrayList<String> getParameters() {
        return this.cmdLineParameters;
    }
    protected boolean isValid() {
        return this.valid;
    }
    protected String nullsToBlanks ( final String s ) {
        return this.nullsToString ( s, "" );
    }
    protected String nullsToString ( final String couldBeNull, final String subForNulls ) {
        return ( couldBeNull == null ) ? subForNulls : couldBeNull;
    }
    protected String blanksToString ( final String couldBeBlank, final String subForBlanks ) {
        return ( "".equals ( couldBeBlank ) || couldBeBlank == null ) ? subForBlanks : couldBeBlank;
    }
}
