package org.apache.catalina.connector;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.SessionTrackingMode;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Session;
import org.apache.catalina.Wrapper;
import org.apache.catalina.security.SecurityUtil;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.SessionConfig;
import org.apache.coyote.ActionCode;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.UEncoder;
import org.apache.tomcat.util.buf.UEncoder.SafeCharsSet;
import org.apache.tomcat.util.buf.UriUtil;
import org.apache.tomcat.util.http.FastHttpDateFormat;
import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.http.parser.MediaTypeCache;
import org.apache.tomcat.util.res.StringManager;
public class Response implements HttpServletResponse {
    private static final Log log = LogFactory.getLog ( Response.class );
    protected static final StringManager sm = StringManager.getManager ( Response.class );
    private static final MediaTypeCache MEDIA_TYPE_CACHE = new MediaTypeCache ( 100 );
    private static final boolean ENFORCE_ENCODING_IN_GET_WRITER;
    static {
        ENFORCE_ENCODING_IN_GET_WRITER = Boolean.parseBoolean (
                                             System.getProperty ( "org.apache.catalina.connector.Response.ENFORCE_ENCODING_IN_GET_WRITER",
                                                     "true" ) );
    }
    protected SimpleDateFormat format = null;
    public void setConnector ( Connector connector ) {
        if ( "AJP/1.3".equals ( connector.getProtocol() ) ) {
            outputBuffer = new OutputBuffer ( 8184 );
        } else {
            outputBuffer = new OutputBuffer();
        }
        outputStream = new CoyoteOutputStream ( outputBuffer );
        writer = new CoyoteWriter ( outputBuffer );
    }
    protected org.apache.coyote.Response coyoteResponse;
    public void setCoyoteResponse ( org.apache.coyote.Response coyoteResponse ) {
        this.coyoteResponse = coyoteResponse;
        outputBuffer.setResponse ( coyoteResponse );
    }
    public org.apache.coyote.Response getCoyoteResponse() {
        return this.coyoteResponse;
    }
    public Context getContext() {
        return ( request.getContext() );
    }
    protected OutputBuffer outputBuffer;
    protected CoyoteOutputStream outputStream;
    protected CoyoteWriter writer;
    protected boolean appCommitted = false;
    protected boolean included = false;
    private boolean isCharacterEncodingSet = false;
    private final AtomicInteger errorState = new AtomicInteger ( 0 );
    protected boolean usingOutputStream = false;
    protected boolean usingWriter = false;
    protected final UEncoder urlEncoder = new UEncoder ( SafeCharsSet.WITH_SLASH );
    protected final CharChunk redirectURLCC = new CharChunk();
    private final List<Cookie> cookies = new ArrayList<>();
    private HttpServletResponse applicationResponse = null;
    public void recycle() {
        cookies.clear();
        outputBuffer.recycle();
        usingOutputStream = false;
        usingWriter = false;
        appCommitted = false;
        included = false;
        errorState.set ( 0 );
        isCharacterEncodingSet = false;
        applicationResponse = null;
        if ( Globals.IS_SECURITY_ENABLED || Connector.RECYCLE_FACADES ) {
            if ( facade != null ) {
                facade.clear();
                facade = null;
            }
            if ( outputStream != null ) {
                outputStream.clear();
                outputStream = null;
            }
            if ( writer != null ) {
                writer.clear();
                writer = null;
            }
        } else {
            writer.recycle();
        }
    }
    public List<Cookie> getCookies() {
        return cookies;
    }
    public long getContentWritten() {
        return outputBuffer.getContentWritten();
    }
    public long getBytesWritten ( boolean flush ) {
        if ( flush ) {
            try {
                outputBuffer.flush();
            } catch ( IOException ioe ) {
            }
        }
        return getCoyoteResponse().getBytesWritten ( flush );
    }
    public void setAppCommitted ( boolean appCommitted ) {
        this.appCommitted = appCommitted;
    }
    public boolean isAppCommitted() {
        return ( this.appCommitted || isCommitted() || isSuspended()
                 || ( ( getContentLength() > 0 )
                      && ( getContentWritten() >= getContentLength() ) ) );
    }
    protected Request request = null;
    public org.apache.catalina.connector.Request getRequest() {
        return ( this.request );
    }
    public void setRequest ( org.apache.catalina.connector.Request request ) {
        this.request = request;
    }
    protected ResponseFacade facade = null;
    public HttpServletResponse getResponse() {
        if ( facade == null ) {
            facade = new ResponseFacade ( this );
        }
        if ( applicationResponse == null ) {
            applicationResponse = facade;
        }
        return applicationResponse;
    }
    public void setResponse ( HttpServletResponse applicationResponse ) {
        ServletResponse r = applicationResponse;
        while ( r instanceof HttpServletResponseWrapper ) {
            r = ( ( HttpServletResponseWrapper ) r ).getResponse();
        }
        if ( r != facade ) {
            throw new IllegalArgumentException ( sm.getString ( "response.illegalWrap" ) );
        }
        this.applicationResponse = applicationResponse;
    }
    public void setSuspended ( boolean suspended ) {
        outputBuffer.setSuspended ( suspended );
    }
    public boolean isSuspended() {
        return outputBuffer.isSuspended();
    }
    public boolean isClosed() {
        return outputBuffer.isClosed();
    }
    public boolean setError() {
        boolean result = errorState.compareAndSet ( 0, 1 );
        if ( result ) {
            Wrapper wrapper = getRequest().getWrapper();
            if ( wrapper != null ) {
                wrapper.incrementErrorCount();
            }
        }
        return result;
    }
    public boolean isError() {
        return errorState.get() > 0;
    }
    public boolean isErrorReportRequired() {
        return errorState.get() == 1;
    }
    public boolean setErrorReported() {
        return errorState.compareAndSet ( 1, 2 );
    }
    public void finishResponse() throws IOException {
        outputBuffer.close();
    }
    public int getContentLength() {
        return getCoyoteResponse().getContentLength();
    }
    @Override
    public String getContentType() {
        return getCoyoteResponse().getContentType();
    }
    public PrintWriter getReporter() throws IOException {
        if ( outputBuffer.isNew() ) {
            outputBuffer.checkConverter();
            if ( writer == null ) {
                writer = new CoyoteWriter ( outputBuffer );
            }
            return writer;
        } else {
            return null;
        }
    }
    @Override
    public void flushBuffer() throws IOException {
        outputBuffer.flush();
    }
    @Override
    public int getBufferSize() {
        return outputBuffer.getBufferSize();
    }
    @Override
    public String getCharacterEncoding() {
        return ( getCoyoteResponse().getCharacterEncoding() );
    }
    @Override
    public ServletOutputStream getOutputStream()
    throws IOException {
        if ( usingWriter ) {
            throw new IllegalStateException
            ( sm.getString ( "coyoteResponse.getOutputStream.ise" ) );
        }
        usingOutputStream = true;
        if ( outputStream == null ) {
            outputStream = new CoyoteOutputStream ( outputBuffer );
        }
        return outputStream;
    }
    @Override
    public Locale getLocale() {
        return ( getCoyoteResponse().getLocale() );
    }
    @Override
    public PrintWriter getWriter()
    throws IOException {
        if ( usingOutputStream ) {
            throw new IllegalStateException
            ( sm.getString ( "coyoteResponse.getWriter.ise" ) );
        }
        if ( ENFORCE_ENCODING_IN_GET_WRITER ) {
            setCharacterEncoding ( getCharacterEncoding() );
        }
        usingWriter = true;
        outputBuffer.checkConverter();
        if ( writer == null ) {
            writer = new CoyoteWriter ( outputBuffer );
        }
        return writer;
    }
    @Override
    public boolean isCommitted() {
        return getCoyoteResponse().isCommitted();
    }
    @Override
    public void reset() {
        if ( included ) {
            return;
        }
        getCoyoteResponse().reset();
        outputBuffer.reset();
        usingOutputStream = false;
        usingWriter = false;
        isCharacterEncodingSet = false;
    }
    @Override
    public void resetBuffer() {
        resetBuffer ( false );
    }
    public void resetBuffer ( boolean resetWriterStreamFlags ) {
        if ( isCommitted() ) {
            throw new IllegalStateException
            ( sm.getString ( "coyoteResponse.resetBuffer.ise" ) );
        }
        outputBuffer.reset ( resetWriterStreamFlags );
        if ( resetWriterStreamFlags ) {
            usingOutputStream = false;
            usingWriter = false;
            isCharacterEncodingSet = false;
        }
    }
    @Override
    public void setBufferSize ( int size ) {
        if ( isCommitted() || !outputBuffer.isNew() ) {
            throw new IllegalStateException
            ( sm.getString ( "coyoteResponse.setBufferSize.ise" ) );
        }
        outputBuffer.setBufferSize ( size );
    }
    @Override
    public void setContentLength ( int length ) {
        setContentLengthLong ( length );
    }
    @Override
    public void setContentLengthLong ( long length ) {
        if ( isCommitted() ) {
            return;
        }
        if ( included ) {
            return;
        }
        getCoyoteResponse().setContentLength ( length );
    }
    @Override
    public void setContentType ( String type ) {
        if ( isCommitted() ) {
            return;
        }
        if ( included ) {
            return;
        }
        if ( type == null ) {
            getCoyoteResponse().setContentType ( null );
            return;
        }
        String[] m = MEDIA_TYPE_CACHE.parse ( type );
        if ( m == null ) {
            getCoyoteResponse().setContentTypeNoCharset ( type );
            return;
        }
        getCoyoteResponse().setContentTypeNoCharset ( m[0] );
        if ( m[1] != null ) {
            if ( !usingWriter ) {
                getCoyoteResponse().setCharacterEncoding ( m[1] );
                isCharacterEncodingSet = true;
            }
        }
    }
    @Override
    public void setCharacterEncoding ( String charset ) {
        if ( isCommitted() ) {
            return;
        }
        if ( included ) {
            return;
        }
        if ( usingWriter ) {
            return;
        }
        getCoyoteResponse().setCharacterEncoding ( charset );
        isCharacterEncodingSet = true;
    }
    @Override
    public void setLocale ( Locale locale ) {
        if ( isCommitted() ) {
            return;
        }
        if ( included ) {
            return;
        }
        getCoyoteResponse().setLocale ( locale );
        if ( usingWriter ) {
            return;
        }
        if ( isCharacterEncodingSet ) {
            return;
        }
        String charset = getContext().getCharset ( locale );
        if ( charset != null ) {
            getCoyoteResponse().setCharacterEncoding ( charset );
        }
    }
    @Override
    public String getHeader ( String name ) {
        return getCoyoteResponse().getMimeHeaders().getHeader ( name );
    }
    @Override
    public Collection<String> getHeaderNames() {
        MimeHeaders headers = getCoyoteResponse().getMimeHeaders();
        int n = headers.size();
        List<String> result = new ArrayList<> ( n );
        for ( int i = 0; i < n; i++ ) {
            result.add ( headers.getName ( i ).toString() );
        }
        return result;
    }
    @Override
    public Collection<String> getHeaders ( String name ) {
        Enumeration<String> enumeration =
            getCoyoteResponse().getMimeHeaders().values ( name );
        Vector<String> result = new Vector<>();
        while ( enumeration.hasMoreElements() ) {
            result.addElement ( enumeration.nextElement() );
        }
        return result;
    }
    public String getMessage() {
        return getCoyoteResponse().getMessage();
    }
    @Override
    public int getStatus() {
        return getCoyoteResponse().getStatus();
    }
    @Override
    public void addCookie ( final Cookie cookie ) {
        if ( included || isCommitted() ) {
            return;
        }
        cookies.add ( cookie );
        String header = generateCookieString ( cookie );
        addHeader ( "Set-Cookie", header, getContext().getCookieProcessor().getCharset() );
    }
    public void addSessionCookieInternal ( final Cookie cookie ) {
        if ( isCommitted() ) {
            return;
        }
        String name = cookie.getName();
        final String headername = "Set-Cookie";
        final String startsWith = name + "=";
        String header = generateCookieString ( cookie );
        boolean set = false;
        MimeHeaders headers = getCoyoteResponse().getMimeHeaders();
        int n = headers.size();
        for ( int i = 0; i < n; i++ ) {
            if ( headers.getName ( i ).toString().equals ( headername ) ) {
                if ( headers.getValue ( i ).toString().startsWith ( startsWith ) ) {
                    headers.getValue ( i ).setString ( header );
                    set = true;
                }
            }
        }
        if ( !set ) {
            addHeader ( headername, header );
        }
    }
    public String generateCookieString ( final Cookie cookie ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return AccessController.doPrivileged ( new PrivilegedAction<String>() {
                @Override
                public String run() {
                    return getContext().getCookieProcessor().generateHeader ( cookie );
                }
            } );
        } else {
            return getContext().getCookieProcessor().generateHeader ( cookie );
        }
    }
    @Override
    public void addDateHeader ( String name, long value ) {
        if ( name == null || name.length() == 0 ) {
            return;
        }
        if ( isCommitted() ) {
            return;
        }
        if ( included ) {
            return;
        }
        if ( format == null ) {
            format = new SimpleDateFormat ( FastHttpDateFormat.RFC1123_DATE,
                                            Locale.US );
            format.setTimeZone ( TimeZone.getTimeZone ( "GMT" ) );
        }
        addHeader ( name, FastHttpDateFormat.formatDate ( value, format ) );
    }
    @Override
    public void addHeader ( String name, String value ) {
        addHeader ( name, value, null );
    }
    private void addHeader ( String name, String value, Charset charset ) {
        if ( name == null || name.length() == 0 || value == null ) {
            return;
        }
        if ( isCommitted() ) {
            return;
        }
        if ( included ) {
            return;
        }
        char cc = name.charAt ( 0 );
        if ( cc == 'C' || cc == 'c' ) {
            if ( checkSpecialHeader ( name, value ) ) {
                return;
            }
        }
        getCoyoteResponse().addHeader ( name, value, charset );
    }
    private boolean checkSpecialHeader ( String name, String value ) {
        if ( name.equalsIgnoreCase ( "Content-Type" ) ) {
            setContentType ( value );
            return true;
        }
        return false;
    }
    @Override
    public void addIntHeader ( String name, int value ) {
        if ( name == null || name.length() == 0 ) {
            return;
        }
        if ( isCommitted() ) {
            return;
        }
        if ( included ) {
            return;
        }
        addHeader ( name, "" + value );
    }
    @Override
    public boolean containsHeader ( String name ) {
        char cc = name.charAt ( 0 );
        if ( cc == 'C' || cc == 'c' ) {
            if ( name.equalsIgnoreCase ( "Content-Type" ) ) {
                return ( getCoyoteResponse().getContentType() != null );
            }
            if ( name.equalsIgnoreCase ( "Content-Length" ) ) {
                return ( getCoyoteResponse().getContentLengthLong() != -1 );
            }
        }
        return getCoyoteResponse().containsHeader ( name );
    }
    @Override
    public String encodeRedirectURL ( String url ) {
        if ( isEncodeable ( toAbsolute ( url ) ) ) {
            return ( toEncoded ( url, request.getSessionInternal().getIdInternal() ) );
        } else {
            return ( url );
        }
    }
    @Override
    @Deprecated
    public String encodeRedirectUrl ( String url ) {
        return ( encodeRedirectURL ( url ) );
    }
    @Override
    public String encodeURL ( String url ) {
        String absolute;
        try {
            absolute = toAbsolute ( url );
        } catch ( IllegalArgumentException iae ) {
            return url;
        }
        if ( isEncodeable ( absolute ) ) {
            if ( url.equalsIgnoreCase ( "" ) ) {
                url = absolute;
            } else if ( url.equals ( absolute ) && !hasPath ( url ) ) {
                url += '/';
            }
            return ( toEncoded ( url, request.getSessionInternal().getIdInternal() ) );
        } else {
            return ( url );
        }
    }
    @Override
    @Deprecated
    public String encodeUrl ( String url ) {
        return ( encodeURL ( url ) );
    }
    public void sendAcknowledgement()
    throws IOException {
        if ( isCommitted() ) {
            return;
        }
        if ( included ) {
            return;
        }
        getCoyoteResponse().action ( ActionCode.ACK, null );
    }
    @Override
    public void sendError ( int status ) throws IOException {
        sendError ( status, null );
    }
    @Override
    public void sendError ( int status, String message ) throws IOException {
        if ( isCommitted() ) {
            throw new IllegalStateException
            ( sm.getString ( "coyoteResponse.sendError.ise" ) );
        }
        if ( included ) {
            return;
        }
        setError();
        getCoyoteResponse().setStatus ( status );
        getCoyoteResponse().setMessage ( message );
        resetBuffer();
        setSuspended ( true );
    }
    @Override
    public void sendRedirect ( String location ) throws IOException {
        sendRedirect ( location, SC_FOUND );
    }
    public void sendRedirect ( String location, int status ) throws IOException {
        if ( isCommitted() ) {
            throw new IllegalStateException ( sm.getString ( "coyoteResponse.sendRedirect.ise" ) );
        }
        if ( included ) {
            return;
        }
        resetBuffer ( true );
        try {
            String locationUri;
            if ( getRequest().getCoyoteRequest().getSupportsRelativeRedirects() &&
                    getContext().getUseRelativeRedirects() ) {
                locationUri = location;
            } else {
                locationUri = toAbsolute ( location );
            }
            setStatus ( status );
            setHeader ( "Location", locationUri );
            if ( getContext().getSendRedirectBody() ) {
                PrintWriter writer = getWriter();
                writer.print ( sm.getString ( "coyoteResponse.sendRedirect.note",
                                              RequestUtil.filter ( locationUri ) ) );
                flushBuffer();
            }
        } catch ( IllegalArgumentException e ) {
            log.warn ( sm.getString ( "response.sendRedirectFail", location ), e );
            setStatus ( SC_NOT_FOUND );
        }
        setSuspended ( true );
    }
    @Override
    public void setDateHeader ( String name, long value ) {
        if ( name == null || name.length() == 0 ) {
            return;
        }
        if ( isCommitted() ) {
            return;
        }
        if ( included ) {
            return;
        }
        if ( format == null ) {
            format = new SimpleDateFormat ( FastHttpDateFormat.RFC1123_DATE,
                                            Locale.US );
            format.setTimeZone ( TimeZone.getTimeZone ( "GMT" ) );
        }
        setHeader ( name, FastHttpDateFormat.formatDate ( value, format ) );
    }
    @Override
    public void setHeader ( String name, String value ) {
        if ( name == null || name.length() == 0 || value == null ) {
            return;
        }
        if ( isCommitted() ) {
            return;
        }
        if ( included ) {
            return;
        }
        char cc = name.charAt ( 0 );
        if ( cc == 'C' || cc == 'c' ) {
            if ( checkSpecialHeader ( name, value ) ) {
                return;
            }
        }
        getCoyoteResponse().setHeader ( name, value );
    }
    @Override
    public void setIntHeader ( String name, int value ) {
        if ( name == null || name.length() == 0 ) {
            return;
        }
        if ( isCommitted() ) {
            return;
        }
        if ( included ) {
            return;
        }
        setHeader ( name, "" + value );
    }
    @Override
    public void setStatus ( int status ) {
        setStatus ( status, null );
    }
    @Override
    @Deprecated
    public void setStatus ( int status, String message ) {
        if ( isCommitted() ) {
            return;
        }
        if ( included ) {
            return;
        }
        getCoyoteResponse().setStatus ( status );
        getCoyoteResponse().setMessage ( message );
    }
    protected boolean isEncodeable ( final String location ) {
        if ( location == null ) {
            return false;
        }
        if ( location.startsWith ( "#" ) ) {
            return false;
        }
        final Request hreq = request;
        final Session session = hreq.getSessionInternal ( false );
        if ( session == null ) {
            return false;
        }
        if ( hreq.isRequestedSessionIdFromCookie() ) {
            return false;
        }
        if ( !hreq.getServletContext().getEffectiveSessionTrackingModes().
                contains ( SessionTrackingMode.URL ) ) {
            return false;
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return (
            AccessController.doPrivileged ( new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    return Boolean.valueOf ( doIsEncodeable ( hreq, session, location ) );
                }
            } ) ).booleanValue();
        } else {
            return doIsEncodeable ( hreq, session, location );
        }
    }
    private boolean doIsEncodeable ( Request hreq, Session session,
                                     String location ) {
        URL url = null;
        try {
            url = new URL ( location );
        } catch ( MalformedURLException e ) {
            return false;
        }
        if ( !hreq.getScheme().equalsIgnoreCase ( url.getProtocol() ) ) {
            return false;
        }
        if ( !hreq.getServerName().equalsIgnoreCase ( url.getHost() ) ) {
            return false;
        }
        int serverPort = hreq.getServerPort();
        if ( serverPort == -1 ) {
            if ( "https".equals ( hreq.getScheme() ) ) {
                serverPort = 443;
            } else {
                serverPort = 80;
            }
        }
        int urlPort = url.getPort();
        if ( urlPort == -1 ) {
            if ( "https".equals ( url.getProtocol() ) ) {
                urlPort = 443;
            } else {
                urlPort = 80;
            }
        }
        if ( serverPort != urlPort ) {
            return false;
        }
        String contextPath = getContext().getPath();
        if ( contextPath != null ) {
            String file = url.getFile();
            if ( !file.startsWith ( contextPath ) ) {
                return false;
            }
            String tok = ";" +
                         SessionConfig.getSessionUriParamName ( request.getContext() ) +
                         "=" + session.getIdInternal();
            if ( file.indexOf ( tok, contextPath.length() ) >= 0 ) {
                return false;
            }
        }
        return true;
    }
    protected String toAbsolute ( String location ) {
        if ( location == null ) {
            return ( location );
        }
        boolean leadingSlash = location.startsWith ( "/" );
        if ( location.startsWith ( "//" ) ) {
            redirectURLCC.recycle();
            String scheme = request.getScheme();
            try {
                redirectURLCC.append ( scheme, 0, scheme.length() );
                redirectURLCC.append ( ':' );
                redirectURLCC.append ( location, 0, location.length() );
                return redirectURLCC.toString();
            } catch ( IOException e ) {
                IllegalArgumentException iae =
                    new IllegalArgumentException ( location );
                iae.initCause ( e );
                throw iae;
            }
        } else if ( leadingSlash || !UriUtil.hasScheme ( location ) ) {
            redirectURLCC.recycle();
            String scheme = request.getScheme();
            String name = request.getServerName();
            int port = request.getServerPort();
            try {
                redirectURLCC.append ( scheme, 0, scheme.length() );
                redirectURLCC.append ( "://", 0, 3 );
                redirectURLCC.append ( name, 0, name.length() );
                if ( ( scheme.equals ( "http" ) && port != 80 )
                        || ( scheme.equals ( "https" ) && port != 443 ) ) {
                    redirectURLCC.append ( ':' );
                    String portS = port + "";
                    redirectURLCC.append ( portS, 0, portS.length() );
                }
                if ( !leadingSlash ) {
                    String relativePath = request.getDecodedRequestURI();
                    int pos = relativePath.lastIndexOf ( '/' );
                    CharChunk encodedURI = null;
                    final String frelativePath = relativePath;
                    final int fend = pos;
                    if ( SecurityUtil.isPackageProtectionEnabled() ) {
                        try {
                            encodedURI = AccessController.doPrivileged (
                            new PrivilegedExceptionAction<CharChunk>() {
                                @Override
                                public CharChunk run() throws IOException {
                                    return urlEncoder.encodeURL ( frelativePath, 0, fend );
                                }
                            } );
                        } catch ( PrivilegedActionException pae ) {
                            IllegalArgumentException iae =
                                new IllegalArgumentException ( location );
                            iae.initCause ( pae.getException() );
                            throw iae;
                        }
                    } else {
                        encodedURI = urlEncoder.encodeURL ( relativePath, 0, pos );
                    }
                    redirectURLCC.append ( encodedURI );
                    encodedURI.recycle();
                    redirectURLCC.append ( '/' );
                }
                redirectURLCC.append ( location, 0, location.length() );
                normalize ( redirectURLCC );
            } catch ( IOException e ) {
                IllegalArgumentException iae =
                    new IllegalArgumentException ( location );
                iae.initCause ( e );
                throw iae;
            }
            return redirectURLCC.toString();
        } else {
            return ( location );
        }
    }
    private void normalize ( CharChunk cc ) {
        int truncate = cc.indexOf ( '?' );
        if ( truncate == -1 ) {
            truncate = cc.indexOf ( '#' );
        }
        char[] truncateCC = null;
        if ( truncate > -1 ) {
            truncateCC = Arrays.copyOfRange ( cc.getBuffer(),
                                              cc.getStart() + truncate, cc.getEnd() );
            cc.setEnd ( cc.getStart() + truncate );
        }
        if ( cc.endsWith ( "/." ) || cc.endsWith ( "/.." ) ) {
            try {
                cc.append ( '/' );
            } catch ( IOException e ) {
                throw new IllegalArgumentException ( cc.toString(), e );
            }
        }
        char[] c = cc.getChars();
        int start = cc.getStart();
        int end = cc.getEnd();
        int index = 0;
        int startIndex = 0;
        for ( int i = 0; i < 3; i++ ) {
            startIndex = cc.indexOf ( '/', startIndex + 1 );
        }
        index = startIndex;
        while ( true ) {
            index = cc.indexOf ( "/./", 0, 3, index );
            if ( index < 0 ) {
                break;
            }
            copyChars ( c, start + index, start + index + 2,
                        end - start - index - 2 );
            end = end - 2;
            cc.setEnd ( end );
        }
        index = startIndex;
        int pos;
        while ( true ) {
            index = cc.indexOf ( "/../", 0, 4, index );
            if ( index < 0 ) {
                break;
            }
            if ( index == startIndex ) {
                throw new IllegalArgumentException();
            }
            int index2 = -1;
            for ( pos = start + index - 1; ( pos >= 0 ) && ( index2 < 0 ); pos -- ) {
                if ( c[pos] == ( byte ) '/' ) {
                    index2 = pos;
                }
            }
            copyChars ( c, start + index2, start + index + 3,
                        end - start - index - 3 );
            end = end + index2 - index - 3;
            cc.setEnd ( end );
            index = index2;
        }
        if ( truncateCC != null ) {
            try {
                cc.append ( truncateCC, 0, truncateCC.length );
            } catch ( IOException ioe ) {
                throw new IllegalArgumentException ( ioe );
            }
        }
    }
    private void copyChars ( char[] c, int dest, int src, int len ) {
        for ( int pos = 0; pos < len; pos++ ) {
            c[pos + dest] = c[pos + src];
        }
    }
    private boolean hasPath ( String uri ) {
        int pos = uri.indexOf ( "://" );
        if ( pos < 0 ) {
            return false;
        }
        pos = uri.indexOf ( '/', pos + 3 );
        if ( pos < 0 ) {
            return false;
        }
        return true;
    }
    protected String toEncoded ( String url, String sessionId ) {
        if ( ( url == null ) || ( sessionId == null ) ) {
            return ( url );
        }
        String path = url;
        String query = "";
        String anchor = "";
        int question = url.indexOf ( '?' );
        if ( question >= 0 ) {
            path = url.substring ( 0, question );
            query = url.substring ( question );
        }
        int pound = path.indexOf ( '#' );
        if ( pound >= 0 ) {
            anchor = path.substring ( pound );
            path = path.substring ( 0, pound );
        }
        StringBuilder sb = new StringBuilder ( path );
        if ( sb.length() > 0 ) {
            sb.append ( ";" );
            sb.append ( SessionConfig.getSessionUriParamName (
                            request.getContext() ) );
            sb.append ( "=" );
            sb.append ( sessionId );
        }
        sb.append ( anchor );
        sb.append ( query );
        return ( sb.toString() );
    }
}
