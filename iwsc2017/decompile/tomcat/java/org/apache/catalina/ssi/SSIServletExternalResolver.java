package org.apache.catalina.ssi;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import org.apache.tomcat.util.buf.B2CConverter;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.ServletOutputStream;
import java.net.URL;
import java.net.URLConnection;
import org.apache.tomcat.util.http.RequestUtil;
import java.io.IOException;
import java.util.Date;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.apache.catalina.connector.Request;
import java.util.Locale;
import java.util.Enumeration;
import java.util.Collection;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
public class SSIServletExternalResolver implements SSIExternalResolver {
    protected final String[] VARIABLE_NAMES;
    protected final ServletContext context;
    protected final HttpServletRequest req;
    protected final HttpServletResponse res;
    protected final boolean isVirtualWebappRelative;
    protected final int debug;
    protected final String inputEncoding;
    public SSIServletExternalResolver ( final ServletContext context, final HttpServletRequest req, final HttpServletResponse res, final boolean isVirtualWebappRelative, final int debug, final String inputEncoding ) {
        this.VARIABLE_NAMES = new String[] { "AUTH_TYPE", "CONTENT_LENGTH", "CONTENT_TYPE", "DOCUMENT_NAME", "DOCUMENT_URI", "GATEWAY_INTERFACE", "HTTP_ACCEPT", "HTTP_ACCEPT_ENCODING", "HTTP_ACCEPT_LANGUAGE", "HTTP_CONNECTION", "HTTP_HOST", "HTTP_REFERER", "HTTP_USER_AGENT", "PATH_INFO", "PATH_TRANSLATED", "QUERY_STRING", "QUERY_STRING_UNESCAPED", "REMOTE_ADDR", "REMOTE_HOST", "REMOTE_PORT", "REMOTE_USER", "REQUEST_METHOD", "REQUEST_URI", "SCRIPT_FILENAME", "SCRIPT_NAME", "SERVER_ADDR", "SERVER_NAME", "SERVER_PORT", "SERVER_PROTOCOL", "SERVER_SOFTWARE", "UNIQUE_ID" };
        this.context = context;
        this.req = req;
        this.res = res;
        this.isVirtualWebappRelative = isVirtualWebappRelative;
        this.debug = debug;
        this.inputEncoding = inputEncoding;
    }
    @Override
    public void log ( final String message, final Throwable throwable ) {
        if ( throwable != null ) {
            this.context.log ( message, throwable );
        } else {
            this.context.log ( message );
        }
    }
    @Override
    public void addVariableNames ( final Collection<String> variableNames ) {
        for ( int i = 0; i < this.VARIABLE_NAMES.length; ++i ) {
            final String variableName = this.VARIABLE_NAMES[i];
            final String variableValue = this.getVariableValue ( variableName );
            if ( variableValue != null ) {
                variableNames.add ( variableName );
            }
        }
        final Enumeration<String> e = ( Enumeration<String> ) this.req.getAttributeNames();
        while ( e.hasMoreElements() ) {
            final String name = e.nextElement();
            if ( !this.isNameReserved ( name ) ) {
                variableNames.add ( name );
            }
        }
    }
    protected Object getReqAttributeIgnoreCase ( final String targetName ) {
        Object object = null;
        if ( !this.isNameReserved ( targetName ) ) {
            object = this.req.getAttribute ( targetName );
            if ( object == null ) {
                final Enumeration<String> e = ( Enumeration<String> ) this.req.getAttributeNames();
                while ( e.hasMoreElements() ) {
                    final String name = e.nextElement();
                    if ( targetName.equalsIgnoreCase ( name ) && !this.isNameReserved ( name ) ) {
                        object = this.req.getAttribute ( name );
                        if ( object != null ) {
                            break;
                        }
                        continue;
                    }
                }
            }
        }
        return object;
    }
    protected boolean isNameReserved ( final String name ) {
        return name.startsWith ( "java." ) || name.startsWith ( "javax." ) || name.startsWith ( "sun." );
    }
    @Override
    public void setVariableValue ( final String name, final String value ) {
        if ( !this.isNameReserved ( name ) ) {
            this.req.setAttribute ( name, ( Object ) value );
        }
    }
    @Override
    public String getVariableValue ( final String name ) {
        String retVal = null;
        final Object object = this.getReqAttributeIgnoreCase ( name );
        if ( object != null ) {
            retVal = object.toString();
        } else {
            retVal = this.getCGIVariable ( name );
        }
        return retVal;
    }
    protected String getCGIVariable ( final String name ) {
        String retVal = null;
        final String[] nameParts = name.toUpperCase ( Locale.ENGLISH ).split ( "_" );
        int requiredParts = 2;
        if ( nameParts.length == 1 ) {
            if ( nameParts[0].equals ( "PATH" ) ) {
                requiredParts = 1;
            }
        } else if ( nameParts[0].equals ( "AUTH" ) ) {
            if ( nameParts[1].equals ( "TYPE" ) ) {
                retVal = this.req.getAuthType();
            }
        } else if ( nameParts[0].equals ( "CONTENT" ) ) {
            if ( nameParts[1].equals ( "LENGTH" ) ) {
                final long contentLength = this.req.getContentLengthLong();
                if ( contentLength >= 0L ) {
                    retVal = Long.toString ( contentLength );
                }
            } else if ( nameParts[1].equals ( "TYPE" ) ) {
                retVal = this.req.getContentType();
            }
        } else if ( nameParts[0].equals ( "DOCUMENT" ) ) {
            if ( nameParts[1].equals ( "NAME" ) ) {
                final String requestURI = this.req.getRequestURI();
                retVal = requestURI.substring ( requestURI.lastIndexOf ( 47 ) + 1 );
            } else if ( nameParts[1].equals ( "URI" ) ) {
                retVal = this.req.getRequestURI();
            }
        } else if ( name.equalsIgnoreCase ( "GATEWAY_INTERFACE" ) ) {
            retVal = "CGI/1.1";
        } else if ( nameParts[0].equals ( "HTTP" ) ) {
            if ( nameParts[1].equals ( "ACCEPT" ) ) {
                String accept = null;
                if ( nameParts.length == 2 ) {
                    accept = "Accept";
                } else if ( nameParts[2].equals ( "ENCODING" ) ) {
                    requiredParts = 3;
                    accept = "Accept-Encoding";
                } else if ( nameParts[2].equals ( "LANGUAGE" ) ) {
                    requiredParts = 3;
                    accept = "Accept-Language";
                }
                if ( accept != null ) {
                    final Enumeration<String> acceptHeaders = ( Enumeration<String> ) this.req.getHeaders ( accept );
                    if ( acceptHeaders != null && acceptHeaders.hasMoreElements() ) {
                        final StringBuilder rv = new StringBuilder ( acceptHeaders.nextElement() );
                        while ( acceptHeaders.hasMoreElements() ) {
                            rv.append ( ", " );
                            rv.append ( acceptHeaders.nextElement() );
                        }
                        retVal = rv.toString();
                    }
                }
            } else if ( nameParts[1].equals ( "CONNECTION" ) ) {
                retVal = this.req.getHeader ( "Connection" );
            } else if ( nameParts[1].equals ( "HOST" ) ) {
                retVal = this.req.getHeader ( "Host" );
            } else if ( nameParts[1].equals ( "REFERER" ) ) {
                retVal = this.req.getHeader ( "Referer" );
            } else if ( nameParts[1].equals ( "USER" ) && nameParts.length == 3 && nameParts[2].equals ( "AGENT" ) ) {
                requiredParts = 3;
                retVal = this.req.getHeader ( "User-Agent" );
            }
        } else if ( nameParts[0].equals ( "PATH" ) ) {
            if ( nameParts[1].equals ( "INFO" ) ) {
                retVal = this.req.getPathInfo();
            } else if ( nameParts[1].equals ( "TRANSLATED" ) ) {
                retVal = this.req.getPathTranslated();
            }
        } else if ( nameParts[0].equals ( "QUERY" ) ) {
            if ( nameParts[1].equals ( "STRING" ) ) {
                final String queryString = this.req.getQueryString();
                if ( nameParts.length == 2 ) {
                    retVal = this.nullToEmptyString ( queryString );
                } else if ( nameParts[2].equals ( "UNESCAPED" ) ) {
                    requiredParts = 3;
                    if ( queryString != null ) {
                        String queryStringEncoding = "ISO-8859-1";
                        String uriEncoding = null;
                        boolean useBodyEncodingForURI = false;
                        final String requestEncoding = this.req.getCharacterEncoding();
                        if ( this.req instanceof Request ) {
                            uriEncoding = ( ( Request ) this.req ).getConnector().getURIEncoding();
                            useBodyEncodingForURI = ( ( Request ) this.req ).getConnector().getUseBodyEncodingForURI();
                        }
                        if ( uriEncoding != null ) {
                            queryStringEncoding = uriEncoding;
                        } else if ( useBodyEncodingForURI && requestEncoding != null ) {
                            queryStringEncoding = requestEncoding;
                        }
                        try {
                            retVal = URLDecoder.decode ( queryString, queryStringEncoding );
                        } catch ( UnsupportedEncodingException e ) {
                            retVal = queryString;
                        }
                    }
                }
            }
        } else if ( nameParts[0].equals ( "REMOTE" ) ) {
            if ( nameParts[1].equals ( "ADDR" ) ) {
                retVal = this.req.getRemoteAddr();
            } else if ( nameParts[1].equals ( "HOST" ) ) {
                retVal = this.req.getRemoteHost();
            } else if ( !nameParts[1].equals ( "IDENT" ) ) {
                if ( nameParts[1].equals ( "PORT" ) ) {
                    retVal = Integer.toString ( this.req.getRemotePort() );
                } else if ( nameParts[1].equals ( "USER" ) ) {
                    retVal = this.req.getRemoteUser();
                }
            }
        } else if ( nameParts[0].equals ( "REQUEST" ) ) {
            if ( nameParts[1].equals ( "METHOD" ) ) {
                retVal = this.req.getMethod();
            } else if ( nameParts[1].equals ( "URI" ) ) {
                retVal = ( String ) this.req.getAttribute ( "javax.servlet.forward.request_uri" );
                if ( retVal == null ) {
                    retVal = this.req.getRequestURI();
                }
            }
        } else if ( nameParts[0].equals ( "SCRIPT" ) ) {
            final String scriptName = this.req.getServletPath();
            if ( nameParts[1].equals ( "FILENAME" ) ) {
                retVal = this.context.getRealPath ( scriptName );
            } else if ( nameParts[1].equals ( "NAME" ) ) {
                retVal = scriptName;
            }
        } else if ( nameParts[0].equals ( "SERVER" ) ) {
            if ( nameParts[1].equals ( "ADDR" ) ) {
                retVal = this.req.getLocalAddr();
            }
            if ( nameParts[1].equals ( "NAME" ) ) {
                retVal = this.req.getServerName();
            } else if ( nameParts[1].equals ( "PORT" ) ) {
                retVal = Integer.toString ( this.req.getServerPort() );
            } else if ( nameParts[1].equals ( "PROTOCOL" ) ) {
                retVal = this.req.getProtocol();
            } else if ( nameParts[1].equals ( "SOFTWARE" ) ) {
                final StringBuilder rv2 = new StringBuilder ( this.context.getServerInfo() );
                rv2.append ( " " );
                rv2.append ( System.getProperty ( "java.vm.name" ) );
                rv2.append ( "/" );
                rv2.append ( System.getProperty ( "java.vm.version" ) );
                rv2.append ( " " );
                rv2.append ( System.getProperty ( "os.name" ) );
                retVal = rv2.toString();
            }
        } else if ( name.equalsIgnoreCase ( "UNIQUE_ID" ) ) {
            retVal = this.req.getRequestedSessionId();
        }
        if ( requiredParts != nameParts.length ) {
            return null;
        }
        return retVal;
    }
    @Override
    public Date getCurrentDate() {
        return new Date();
    }
    protected String nullToEmptyString ( final String string ) {
        String retVal = string;
        if ( retVal == null ) {
            retVal = "";
        }
        return retVal;
    }
    protected String getPathWithoutFileName ( final String servletPath ) {
        String retVal = null;
        final int lastSlash = servletPath.lastIndexOf ( 47 );
        if ( lastSlash >= 0 ) {
            retVal = servletPath.substring ( 0, lastSlash + 1 );
        }
        return retVal;
    }
    protected String getPathWithoutContext ( final String contextPath, final String servletPath ) {
        if ( servletPath.startsWith ( contextPath ) ) {
            return servletPath.substring ( contextPath.length() );
        }
        return servletPath;
    }
    protected String getAbsolutePath ( final String path ) throws IOException {
        final String pathWithoutContext = SSIServletRequestUtil.getRelativePath ( this.req );
        final String prefix = this.getPathWithoutFileName ( pathWithoutContext );
        if ( prefix == null ) {
            throw new IOException ( "Couldn't remove filename from path: " + pathWithoutContext );
        }
        final String fullPath = prefix + path;
        final String retVal = RequestUtil.normalize ( fullPath );
        if ( retVal == null ) {
            throw new IOException ( "Normalization yielded null on path: " + fullPath );
        }
        return retVal;
    }
    protected ServletContextAndPath getServletContextAndPathFromNonVirtualPath ( final String nonVirtualPath ) throws IOException {
        if ( nonVirtualPath.startsWith ( "/" ) || nonVirtualPath.startsWith ( "\\" ) ) {
            throw new IOException ( "A non-virtual path can't be absolute: " + nonVirtualPath );
        }
        if ( nonVirtualPath.indexOf ( "../" ) >= 0 ) {
            throw new IOException ( "A non-virtual path can't contain '../' : " + nonVirtualPath );
        }
        final String path = this.getAbsolutePath ( nonVirtualPath );
        final ServletContextAndPath csAndP = new ServletContextAndPath ( this.context, path );
        return csAndP;
    }
    protected ServletContextAndPath getServletContextAndPathFromVirtualPath ( final String virtualPath ) throws IOException {
        if ( !virtualPath.startsWith ( "/" ) && !virtualPath.startsWith ( "\\" ) ) {
            return new ServletContextAndPath ( this.context, this.getAbsolutePath ( virtualPath ) );
        }
        final String normalized = RequestUtil.normalize ( virtualPath );
        if ( this.isVirtualWebappRelative ) {
            return new ServletContextAndPath ( this.context, normalized );
        }
        final ServletContext normContext = this.context.getContext ( normalized );
        if ( normContext == null ) {
            throw new IOException ( "Couldn't get context for path: " + normalized );
        }
        if ( this.isRootContext ( normContext ) ) {
            return new ServletContextAndPath ( normContext, normalized );
        }
        final String noContext = this.getPathWithoutContext ( normContext.getContextPath(), normalized );
        if ( noContext == null ) {
            throw new IOException ( "Couldn't remove context from path: " + normalized );
        }
        return new ServletContextAndPath ( normContext, noContext );
    }
    protected boolean isRootContext ( final ServletContext servletContext ) {
        return servletContext == servletContext.getContext ( "/" );
    }
    protected ServletContextAndPath getServletContextAndPath ( final String originalPath, final boolean virtual ) throws IOException {
        ServletContextAndPath csAndP = null;
        if ( this.debug > 0 ) {
            this.log ( "SSIServletExternalResolver.getServletContextAndPath( " + originalPath + ", " + virtual + ")", null );
        }
        if ( virtual ) {
            csAndP = this.getServletContextAndPathFromVirtualPath ( originalPath );
        } else {
            csAndP = this.getServletContextAndPathFromNonVirtualPath ( originalPath );
        }
        return csAndP;
    }
    protected URLConnection getURLConnection ( final String originalPath, final boolean virtual ) throws IOException {
        final ServletContextAndPath csAndP = this.getServletContextAndPath ( originalPath, virtual );
        final ServletContext context = csAndP.getServletContext();
        final String path = csAndP.getPath();
        final URL url = context.getResource ( path );
        if ( url == null ) {
            throw new IOException ( "Context did not contain resource: " + path );
        }
        final URLConnection urlConnection = url.openConnection();
        return urlConnection;
    }
    @Override
    public long getFileLastModified ( final String path, final boolean virtual ) throws IOException {
        long lastModified = 0L;
        try {
            final URLConnection urlConnection = this.getURLConnection ( path, virtual );
            lastModified = urlConnection.getLastModified();
        } catch ( IOException ex ) {}
        return lastModified;
    }
    @Override
    public long getFileSize ( final String path, final boolean virtual ) throws IOException {
        long fileSize = -1L;
        try {
            final URLConnection urlConnection = this.getURLConnection ( path, virtual );
            fileSize = urlConnection.getContentLengthLong();
        } catch ( IOException ex ) {}
        return fileSize;
    }
    @Override
    public String getFileText ( final String originalPath, final boolean virtual ) throws IOException {
        try {
            final ServletContextAndPath csAndP = this.getServletContextAndPath ( originalPath, virtual );
            final ServletContext context = csAndP.getServletContext();
            final String path = csAndP.getPath();
            final RequestDispatcher rd = context.getRequestDispatcher ( path );
            if ( rd == null ) {
                throw new IOException ( "Couldn't get request dispatcher for path: " + path );
            }
            final ByteArrayServletOutputStream basos = new ByteArrayServletOutputStream();
            final ResponseIncludeWrapper responseIncludeWrapper = new ResponseIncludeWrapper ( context, this.req, this.res, basos );
            rd.include ( ( ServletRequest ) this.req, ( ServletResponse ) responseIncludeWrapper );
            responseIncludeWrapper.flushOutputStreamOrWriter();
            final byte[] bytes = basos.toByteArray();
            String retVal;
            if ( this.inputEncoding == null ) {
                retVal = new String ( bytes );
            } else {
                retVal = new String ( bytes, B2CConverter.getCharset ( this.inputEncoding ) );
            }
            if ( retVal.equals ( "" ) && !this.req.getMethod().equalsIgnoreCase ( "HEAD" ) ) {
                throw new IOException ( "Couldn't find file: " + path );
            }
            return retVal;
        } catch ( ServletException e ) {
            throw new IOException ( "Couldn't include file: " + originalPath + " because of ServletException: " + e.getMessage() );
        }
    }
    protected static class ServletContextAndPath {
        protected final ServletContext servletContext;
        protected final String path;
        public ServletContextAndPath ( final ServletContext servletContext, final String path ) {
            this.servletContext = servletContext;
            this.path = path;
        }
        public ServletContext getServletContext() {
            return this.servletContext;
        }
        public String getPath() {
            return this.path;
        }
    }
}
