package org.apache.catalina.connector;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.apache.juli.logging.LogFactory;
import java.util.Arrays;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import org.apache.tomcat.util.buf.UriUtil;
import org.apache.catalina.util.SessionConfig;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.catalina.Session;
import javax.servlet.SessionTrackingMode;
import org.apache.catalina.util.RequestUtil;
import org.apache.coyote.ActionCode;
import java.nio.charset.Charset;
import java.text.DateFormat;
import org.apache.tomcat.util.http.FastHttpDateFormat;
import java.util.TimeZone;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.catalina.security.SecurityUtil;
import java.util.Enumeration;
import java.util.Vector;
import org.apache.tomcat.util.http.MimeHeaders;
import java.util.Collection;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import java.io.PrintWriter;
import org.apache.catalina.Wrapper;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import org.apache.catalina.Globals;
import org.apache.catalina.Context;
import java.util.ArrayList;
import javax.servlet.http.Cookie;
import java.util.List;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.UEncoder;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.coyote.Response;
import java.text.SimpleDateFormat;
import org.apache.tomcat.util.http.parser.MediaTypeCache;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import javax.servlet.http.HttpServletResponse;
public class Response implements HttpServletResponse {
    private static final Log log;
    protected static final StringManager sm;
    private static final MediaTypeCache MEDIA_TYPE_CACHE;
    private static final boolean ENFORCE_ENCODING_IN_GET_WRITER;
    protected SimpleDateFormat format;
    protected org.apache.coyote.Response coyoteResponse;
    protected OutputBuffer outputBuffer;
    protected CoyoteOutputStream outputStream;
    protected CoyoteWriter writer;
    protected boolean appCommitted;
    protected boolean included;
    private boolean isCharacterEncodingSet;
    private final AtomicInteger errorState;
    protected boolean usingOutputStream;
    protected boolean usingWriter;
    protected final UEncoder urlEncoder;
    protected final CharChunk redirectURLCC;
    private final List<Cookie> cookies;
    private HttpServletResponse applicationResponse;
    protected Request request;
    protected ResponseFacade facade;
    public Response() {
        this.format = null;
        this.appCommitted = false;
        this.included = false;
        this.isCharacterEncodingSet = false;
        this.errorState = new AtomicInteger ( 0 );
        this.usingOutputStream = false;
        this.usingWriter = false;
        this.urlEncoder = new UEncoder ( UEncoder.SafeCharsSet.WITH_SLASH );
        this.redirectURLCC = new CharChunk();
        this.cookies = new ArrayList<Cookie>();
        this.applicationResponse = null;
        this.request = null;
        this.facade = null;
    }
    public void setConnector ( final Connector connector ) {
        if ( "AJP/1.3".equals ( connector.getProtocol() ) ) {
            this.outputBuffer = new OutputBuffer ( 8184 );
        } else {
            this.outputBuffer = new OutputBuffer();
        }
        this.outputStream = new CoyoteOutputStream ( this.outputBuffer );
        this.writer = new CoyoteWriter ( this.outputBuffer );
    }
    public void setCoyoteResponse ( final org.apache.coyote.Response coyoteResponse ) {
        this.coyoteResponse = coyoteResponse;
        this.outputBuffer.setResponse ( coyoteResponse );
    }
    public org.apache.coyote.Response getCoyoteResponse() {
        return this.coyoteResponse;
    }
    public Context getContext() {
        return this.request.getContext();
    }
    public void recycle() {
        this.cookies.clear();
        this.outputBuffer.recycle();
        this.usingOutputStream = false;
        this.usingWriter = false;
        this.appCommitted = false;
        this.included = false;
        this.errorState.set ( 0 );
        this.isCharacterEncodingSet = false;
        this.applicationResponse = null;
        if ( Globals.IS_SECURITY_ENABLED || Connector.RECYCLE_FACADES ) {
            if ( this.facade != null ) {
                this.facade.clear();
                this.facade = null;
            }
            if ( this.outputStream != null ) {
                this.outputStream.clear();
                this.outputStream = null;
            }
            if ( this.writer != null ) {
                this.writer.clear();
                this.writer = null;
            }
        } else {
            this.writer.recycle();
        }
    }
    public List<Cookie> getCookies() {
        return this.cookies;
    }
    public long getContentWritten() {
        return this.outputBuffer.getContentWritten();
    }
    public long getBytesWritten ( final boolean flush ) {
        if ( flush ) {
            try {
                this.outputBuffer.flush();
            } catch ( IOException ex ) {}
        }
        return this.getCoyoteResponse().getBytesWritten ( flush );
    }
    public void setAppCommitted ( final boolean appCommitted ) {
        this.appCommitted = appCommitted;
    }
    public boolean isAppCommitted() {
        return this.appCommitted || this.isCommitted() || this.isSuspended() || ( this.getContentLength() > 0 && this.getContentWritten() >= this.getContentLength() );
    }
    public Request getRequest() {
        return this.request;
    }
    public void setRequest ( final Request request ) {
        this.request = request;
    }
    public HttpServletResponse getResponse() {
        if ( this.facade == null ) {
            this.facade = new ResponseFacade ( this );
        }
        if ( this.applicationResponse == null ) {
            this.applicationResponse = ( HttpServletResponse ) this.facade;
        }
        return this.applicationResponse;
    }
    public void setResponse ( final HttpServletResponse applicationResponse ) {
        ServletResponse r;
        for ( r = ( ServletResponse ) applicationResponse; r instanceof HttpServletResponseWrapper; r = ( ( HttpServletResponseWrapper ) r ).getResponse() ) {}
        if ( r != this.facade ) {
            throw new IllegalArgumentException ( Response.sm.getString ( "response.illegalWrap" ) );
        }
        this.applicationResponse = applicationResponse;
    }
    public void setSuspended ( final boolean suspended ) {
        this.outputBuffer.setSuspended ( suspended );
    }
    public boolean isSuspended() {
        return this.outputBuffer.isSuspended();
    }
    public boolean isClosed() {
        return this.outputBuffer.isClosed();
    }
    public boolean setError() {
        final boolean result = this.errorState.compareAndSet ( 0, 1 );
        if ( result ) {
            final Wrapper wrapper = this.getRequest().getWrapper();
            if ( wrapper != null ) {
                wrapper.incrementErrorCount();
            }
        }
        return result;
    }
    public boolean isError() {
        return this.errorState.get() > 0;
    }
    public boolean isErrorReportRequired() {
        return this.errorState.get() == 1;
    }
    public boolean setErrorReported() {
        return this.errorState.compareAndSet ( 1, 2 );
    }
    public void finishResponse() throws IOException {
        this.outputBuffer.close();
    }
    public int getContentLength() {
        return this.getCoyoteResponse().getContentLength();
    }
    public String getContentType() {
        return this.getCoyoteResponse().getContentType();
    }
    public PrintWriter getReporter() throws IOException {
        if ( this.outputBuffer.isNew() ) {
            this.outputBuffer.checkConverter();
            if ( this.writer == null ) {
                this.writer = new CoyoteWriter ( this.outputBuffer );
            }
            return this.writer;
        }
        return null;
    }
    public void flushBuffer() throws IOException {
        this.outputBuffer.flush();
    }
    public int getBufferSize() {
        return this.outputBuffer.getBufferSize();
    }
    public String getCharacterEncoding() {
        return this.getCoyoteResponse().getCharacterEncoding();
    }
    public ServletOutputStream getOutputStream() throws IOException {
        if ( this.usingWriter ) {
            throw new IllegalStateException ( Response.sm.getString ( "coyoteResponse.getOutputStream.ise" ) );
        }
        this.usingOutputStream = true;
        if ( this.outputStream == null ) {
            this.outputStream = new CoyoteOutputStream ( this.outputBuffer );
        }
        return this.outputStream;
    }
    public Locale getLocale() {
        return this.getCoyoteResponse().getLocale();
    }
    public PrintWriter getWriter() throws IOException {
        if ( this.usingOutputStream ) {
            throw new IllegalStateException ( Response.sm.getString ( "coyoteResponse.getWriter.ise" ) );
        }
        if ( Response.ENFORCE_ENCODING_IN_GET_WRITER ) {
            this.setCharacterEncoding ( this.getCharacterEncoding() );
        }
        this.usingWriter = true;
        this.outputBuffer.checkConverter();
        if ( this.writer == null ) {
            this.writer = new CoyoteWriter ( this.outputBuffer );
        }
        return this.writer;
    }
    public boolean isCommitted() {
        return this.getCoyoteResponse().isCommitted();
    }
    public void reset() {
        if ( this.included ) {
            return;
        }
        this.getCoyoteResponse().reset();
        this.outputBuffer.reset();
        this.usingOutputStream = false;
        this.usingWriter = false;
        this.isCharacterEncodingSet = false;
    }
    public void resetBuffer() {
        this.resetBuffer ( false );
    }
    public void resetBuffer ( final boolean resetWriterStreamFlags ) {
        if ( this.isCommitted() ) {
            throw new IllegalStateException ( Response.sm.getString ( "coyoteResponse.resetBuffer.ise" ) );
        }
        this.outputBuffer.reset ( resetWriterStreamFlags );
        if ( resetWriterStreamFlags ) {
            this.usingOutputStream = false;
            this.usingWriter = false;
            this.isCharacterEncodingSet = false;
        }
    }
    public void setBufferSize ( final int size ) {
        if ( this.isCommitted() || !this.outputBuffer.isNew() ) {
            throw new IllegalStateException ( Response.sm.getString ( "coyoteResponse.setBufferSize.ise" ) );
        }
        this.outputBuffer.setBufferSize ( size );
    }
    public void setContentLength ( final int length ) {
        this.setContentLengthLong ( length );
    }
    public void setContentLengthLong ( final long length ) {
        if ( this.isCommitted() ) {
            return;
        }
        if ( this.included ) {
            return;
        }
        this.getCoyoteResponse().setContentLength ( length );
    }
    public void setContentType ( final String type ) {
        if ( this.isCommitted() ) {
            return;
        }
        if ( this.included ) {
            return;
        }
        if ( type == null ) {
            this.getCoyoteResponse().setContentType ( null );
            return;
        }
        final String[] m = Response.MEDIA_TYPE_CACHE.parse ( type );
        if ( m == null ) {
            this.getCoyoteResponse().setContentTypeNoCharset ( type );
            return;
        }
        this.getCoyoteResponse().setContentTypeNoCharset ( m[0] );
        if ( m[1] != null && !this.usingWriter ) {
            this.getCoyoteResponse().setCharacterEncoding ( m[1] );
            this.isCharacterEncodingSet = true;
        }
    }
    public void setCharacterEncoding ( final String charset ) {
        if ( this.isCommitted() ) {
            return;
        }
        if ( this.included ) {
            return;
        }
        if ( this.usingWriter ) {
            return;
        }
        this.getCoyoteResponse().setCharacterEncoding ( charset );
        this.isCharacterEncodingSet = true;
    }
    public void setLocale ( final Locale locale ) {
        if ( this.isCommitted() ) {
            return;
        }
        if ( this.included ) {
            return;
        }
        this.getCoyoteResponse().setLocale ( locale );
        if ( this.usingWriter ) {
            return;
        }
        if ( this.isCharacterEncodingSet ) {
            return;
        }
        final String charset = this.getContext().getCharset ( locale );
        if ( charset != null ) {
            this.getCoyoteResponse().setCharacterEncoding ( charset );
        }
    }
    public String getHeader ( final String name ) {
        return this.getCoyoteResponse().getMimeHeaders().getHeader ( name );
    }
    public Collection<String> getHeaderNames() {
        final MimeHeaders headers = this.getCoyoteResponse().getMimeHeaders();
        final int n = headers.size();
        final List<String> result = new ArrayList<String> ( n );
        for ( int i = 0; i < n; ++i ) {
            result.add ( headers.getName ( i ).toString() );
        }
        return result;
    }
    public Collection<String> getHeaders ( final String name ) {
        final Enumeration<String> enumeration = this.getCoyoteResponse().getMimeHeaders().values ( name );
        final Vector<String> result = new Vector<String>();
        while ( enumeration.hasMoreElements() ) {
            result.addElement ( enumeration.nextElement() );
        }
        return result;
    }
    public String getMessage() {
        return this.getCoyoteResponse().getMessage();
    }
    public int getStatus() {
        return this.getCoyoteResponse().getStatus();
    }
    public void addCookie ( final Cookie cookie ) {
        if ( this.included || this.isCommitted() ) {
            return;
        }
        this.cookies.add ( cookie );
        final String header = this.generateCookieString ( cookie );
        this.addHeader ( "Set-Cookie", header, this.getContext().getCookieProcessor().getCharset() );
    }
    public void addSessionCookieInternal ( final Cookie cookie ) {
        if ( this.isCommitted() ) {
            return;
        }
        final String name = cookie.getName();
        final String headername = "Set-Cookie";
        final String startsWith = name + "=";
        final String header = this.generateCookieString ( cookie );
        boolean set = false;
        final MimeHeaders headers = this.getCoyoteResponse().getMimeHeaders();
        for ( int n = headers.size(), i = 0; i < n; ++i ) {
            if ( headers.getName ( i ).toString().equals ( "Set-Cookie" ) && headers.getValue ( i ).toString().startsWith ( startsWith ) ) {
                headers.getValue ( i ).setString ( header );
                set = true;
            }
        }
        if ( !set ) {
            this.addHeader ( "Set-Cookie", header );
        }
    }
    public String generateCookieString ( final Cookie cookie ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<String> ) new PrivilegedAction<String>() {
                @Override
                public String run() {
                    return Response.this.getContext().getCookieProcessor().generateHeader ( cookie );
                }
            } );
        }
        return this.getContext().getCookieProcessor().generateHeader ( cookie );
    }
    public void addDateHeader ( final String name, final long value ) {
        if ( name == null || name.length() == 0 ) {
            return;
        }
        if ( this.isCommitted() ) {
            return;
        }
        if ( this.included ) {
            return;
        }
        if ( this.format == null ) {
            ( this.format = new SimpleDateFormat ( "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US ) ).setTimeZone ( TimeZone.getTimeZone ( "GMT" ) );
        }
        this.addHeader ( name, FastHttpDateFormat.formatDate ( value, this.format ) );
    }
    public void addHeader ( final String name, final String value ) {
        this.addHeader ( name, value, null );
    }
    private void addHeader ( final String name, final String value, final Charset charset ) {
        if ( name == null || name.length() == 0 || value == null ) {
            return;
        }
        if ( this.isCommitted() ) {
            return;
        }
        if ( this.included ) {
            return;
        }
        final char cc = name.charAt ( 0 );
        if ( ( cc == 'C' || cc == 'c' ) && this.checkSpecialHeader ( name, value ) ) {
            return;
        }
        this.getCoyoteResponse().addHeader ( name, value, charset );
    }
    private boolean checkSpecialHeader ( final String name, final String value ) {
        if ( name.equalsIgnoreCase ( "Content-Type" ) ) {
            this.setContentType ( value );
            return true;
        }
        return false;
    }
    public void addIntHeader ( final String name, final int value ) {
        if ( name == null || name.length() == 0 ) {
            return;
        }
        if ( this.isCommitted() ) {
            return;
        }
        if ( this.included ) {
            return;
        }
        this.addHeader ( name, "" + value );
    }
    public boolean containsHeader ( final String name ) {
        final char cc = name.charAt ( 0 );
        if ( cc == 'C' || cc == 'c' ) {
            if ( name.equalsIgnoreCase ( "Content-Type" ) ) {
                return this.getCoyoteResponse().getContentType() != null;
            }
            if ( name.equalsIgnoreCase ( "Content-Length" ) ) {
                return this.getCoyoteResponse().getContentLengthLong() != -1L;
            }
        }
        return this.getCoyoteResponse().containsHeader ( name );
    }
    public String encodeRedirectURL ( final String url ) {
        if ( this.isEncodeable ( this.toAbsolute ( url ) ) ) {
            return this.toEncoded ( url, this.request.getSessionInternal().getIdInternal() );
        }
        return url;
    }
    @Deprecated
    public String encodeRedirectUrl ( final String url ) {
        return this.encodeRedirectURL ( url );
    }
    public String encodeURL ( String url ) {
        String absolute;
        try {
            absolute = this.toAbsolute ( url );
        } catch ( IllegalArgumentException iae ) {
            return url;
        }
        if ( this.isEncodeable ( absolute ) ) {
            if ( url.equalsIgnoreCase ( "" ) ) {
                url = absolute;
            } else if ( url.equals ( absolute ) && !this.hasPath ( url ) ) {
                url += '/';
            }
            return this.toEncoded ( url, this.request.getSessionInternal().getIdInternal() );
        }
        return url;
    }
    @Deprecated
    public String encodeUrl ( final String url ) {
        return this.encodeURL ( url );
    }
    public void sendAcknowledgement() throws IOException {
        if ( this.isCommitted() ) {
            return;
        }
        if ( this.included ) {
            return;
        }
        this.getCoyoteResponse().action ( ActionCode.ACK, null );
    }
    public void sendError ( final int status ) throws IOException {
        this.sendError ( status, null );
    }
    public void sendError ( final int status, final String message ) throws IOException {
        if ( this.isCommitted() ) {
            throw new IllegalStateException ( Response.sm.getString ( "coyoteResponse.sendError.ise" ) );
        }
        if ( this.included ) {
            return;
        }
        this.setError();
        this.getCoyoteResponse().setStatus ( status );
        this.getCoyoteResponse().setMessage ( message );
        this.resetBuffer();
        this.setSuspended ( true );
    }
    public void sendRedirect ( final String location ) throws IOException {
        this.sendRedirect ( location, 302 );
    }
    public void sendRedirect ( final String location, final int status ) throws IOException {
        if ( this.isCommitted() ) {
            throw new IllegalStateException ( Response.sm.getString ( "coyoteResponse.sendRedirect.ise" ) );
        }
        if ( this.included ) {
            return;
        }
        this.resetBuffer ( true );
        try {
            String locationUri;
            if ( this.getRequest().getCoyoteRequest().getSupportsRelativeRedirects() && this.getContext().getUseRelativeRedirects() ) {
                locationUri = location;
            } else {
                locationUri = this.toAbsolute ( location );
            }
            this.setStatus ( status );
            this.setHeader ( "Location", locationUri );
            if ( this.getContext().getSendRedirectBody() ) {
                final PrintWriter writer = this.getWriter();
                writer.print ( Response.sm.getString ( "coyoteResponse.sendRedirect.note", RequestUtil.filter ( locationUri ) ) );
                this.flushBuffer();
            }
        } catch ( IllegalArgumentException e ) {
            Response.log.warn ( Response.sm.getString ( "response.sendRedirectFail", location ), e );
            this.setStatus ( 404 );
        }
        this.setSuspended ( true );
    }
    public void setDateHeader ( final String name, final long value ) {
        if ( name == null || name.length() == 0 ) {
            return;
        }
        if ( this.isCommitted() ) {
            return;
        }
        if ( this.included ) {
            return;
        }
        if ( this.format == null ) {
            ( this.format = new SimpleDateFormat ( "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US ) ).setTimeZone ( TimeZone.getTimeZone ( "GMT" ) );
        }
        this.setHeader ( name, FastHttpDateFormat.formatDate ( value, this.format ) );
    }
    public void setHeader ( final String name, final String value ) {
        if ( name == null || name.length() == 0 || value == null ) {
            return;
        }
        if ( this.isCommitted() ) {
            return;
        }
        if ( this.included ) {
            return;
        }
        final char cc = name.charAt ( 0 );
        if ( ( cc == 'C' || cc == 'c' ) && this.checkSpecialHeader ( name, value ) ) {
            return;
        }
        this.getCoyoteResponse().setHeader ( name, value );
    }
    public void setIntHeader ( final String name, final int value ) {
        if ( name == null || name.length() == 0 ) {
            return;
        }
        if ( this.isCommitted() ) {
            return;
        }
        if ( this.included ) {
            return;
        }
        this.setHeader ( name, "" + value );
    }
    public void setStatus ( final int status ) {
        this.setStatus ( status, null );
    }
    @Deprecated
    public void setStatus ( final int status, final String message ) {
        if ( this.isCommitted() ) {
            return;
        }
        if ( this.included ) {
            return;
        }
        this.getCoyoteResponse().setStatus ( status );
        this.getCoyoteResponse().setMessage ( message );
    }
    protected boolean isEncodeable ( final String location ) {
        if ( location == null ) {
            return false;
        }
        if ( location.startsWith ( "#" ) ) {
            return false;
        }
        final Request hreq = this.request;
        final Session session = hreq.getSessionInternal ( false );
        if ( session == null ) {
            return false;
        }
        if ( hreq.isRequestedSessionIdFromCookie() ) {
            return false;
        }
        if ( !hreq.getServletContext().getEffectiveSessionTrackingModes().contains ( SessionTrackingMode.URL ) ) {
            return false;
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<Boolean> ) new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    return Response.this.doIsEncodeable ( hreq, session, location );
                }
            } );
        }
        return this.doIsEncodeable ( hreq, session, location );
    }
    private boolean doIsEncodeable ( final Request hreq, final Session session, final String location ) {
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
        final String contextPath = this.getContext().getPath();
        if ( contextPath != null ) {
            final String file = url.getFile();
            if ( !file.startsWith ( contextPath ) ) {
                return false;
            }
            final String tok = ";" + SessionConfig.getSessionUriParamName ( this.request.getContext() ) + "=" + session.getIdInternal();
            if ( file.indexOf ( tok, contextPath.length() ) >= 0 ) {
                return false;
            }
        }
        return true;
    }
    protected String toAbsolute ( final String location ) {
        if ( location == null ) {
            return location;
        }
        final boolean leadingSlash = location.startsWith ( "/" );
        if ( location.startsWith ( "//" ) ) {
            this.redirectURLCC.recycle();
            final String scheme = this.request.getScheme();
            try {
                this.redirectURLCC.append ( scheme, 0, scheme.length() );
                this.redirectURLCC.append ( ':' );
                this.redirectURLCC.append ( location, 0, location.length() );
                return this.redirectURLCC.toString();
            } catch ( IOException e ) {
                final IllegalArgumentException iae = new IllegalArgumentException ( location );
                iae.initCause ( e );
                throw iae;
            }
        }
        if ( leadingSlash || !UriUtil.hasScheme ( location ) ) {
            this.redirectURLCC.recycle();
            final String scheme = this.request.getScheme();
            final String name = this.request.getServerName();
            final int port = this.request.getServerPort();
            try {
                this.redirectURLCC.append ( scheme, 0, scheme.length() );
                this.redirectURLCC.append ( "://", 0, 3 );
                this.redirectURLCC.append ( name, 0, name.length() );
                if ( ( scheme.equals ( "http" ) && port != 80 ) || ( scheme.equals ( "https" ) && port != 443 ) ) {
                    this.redirectURLCC.append ( ':' );
                    final String portS = port + "";
                    this.redirectURLCC.append ( portS, 0, portS.length() );
                }
                if ( !leadingSlash ) {
                    final String relativePath = this.request.getDecodedRequestURI();
                    final int pos = relativePath.lastIndexOf ( 47 );
                    CharChunk encodedURI = null;
                    final String frelativePath = relativePath;
                    final int fend = pos;
                    Label_0367: {
                        if ( SecurityUtil.isPackageProtectionEnabled() ) {
                            try {
                                encodedURI = AccessController.doPrivileged ( ( PrivilegedExceptionAction<CharChunk> ) new PrivilegedExceptionAction<CharChunk>() {
                                    @Override
                                    public CharChunk run() throws IOException {
                                        return Response.this.urlEncoder.encodeURL ( frelativePath, 0, fend );
                                    }
                                } );
                                break Label_0367;
                            } catch ( PrivilegedActionException pae ) {
                                final IllegalArgumentException iae2 = new IllegalArgumentException ( location );
                                iae2.initCause ( pae.getException() );
                                throw iae2;
                            }
                        }
                        encodedURI = this.urlEncoder.encodeURL ( relativePath, 0, pos );
                    }
                    this.redirectURLCC.append ( encodedURI );
                    encodedURI.recycle();
                    this.redirectURLCC.append ( '/' );
                }
                this.redirectURLCC.append ( location, 0, location.length() );
                this.normalize ( this.redirectURLCC );
            } catch ( IOException e2 ) {
                final IllegalArgumentException iae3 = new IllegalArgumentException ( location );
                iae3.initCause ( e2 );
                throw iae3;
            }
            return this.redirectURLCC.toString();
        }
        return location;
    }
    private void normalize ( final CharChunk cc ) {
        int truncate = cc.indexOf ( '?' );
        if ( truncate == -1 ) {
            truncate = cc.indexOf ( '#' );
        }
        char[] truncateCC = null;
        if ( truncate > -1 ) {
            truncateCC = Arrays.copyOfRange ( cc.getBuffer(), cc.getStart() + truncate, cc.getEnd() );
            cc.setEnd ( cc.getStart() + truncate );
        }
        Label_0099: {
            if ( !cc.endsWith ( "/." ) ) {
                if ( !cc.endsWith ( "/.." ) ) {
                    break Label_0099;
                }
            }
            try {
                cc.append ( '/' );
            } catch ( IOException e ) {
                throw new IllegalArgumentException ( cc.toString(), e );
            }
        }
        final char[] c = cc.getChars();
        final int start = cc.getStart();
        int end = cc.getEnd();
        int index = 0;
        int startIndex = 0;
        for ( int i = 0; i < 3; ++i ) {
            startIndex = cc.indexOf ( '/', startIndex + 1 );
        }
        index = startIndex;
        while ( true ) {
            index = cc.indexOf ( "/./", 0, 3, index );
            if ( index < 0 ) {
                break;
            }
            this.copyChars ( c, start + index, start + index + 2, end - start - index - 2 );
            end -= 2;
            cc.setEnd ( end );
        }
        index = startIndex;
        while ( true ) {
            index = cc.indexOf ( "/../", 0, 4, index );
            if ( index < 0 ) {
                if ( truncateCC != null ) {
                    try {
                        cc.append ( truncateCC, 0, truncateCC.length );
                    } catch ( IOException ioe ) {
                        throw new IllegalArgumentException ( ioe );
                    }
                }
                return;
            }
            if ( index == startIndex ) {
                throw new IllegalArgumentException();
            }
            int index2 = -1;
            for ( int pos = start + index - 1; pos >= 0 && index2 < 0; --pos ) {
                if ( c[pos] == '/' ) {
                    index2 = pos;
                }
            }
            this.copyChars ( c, start + index2, start + index + 3, end - start - index - 3 );
            end = end + index2 - index - 3;
            cc.setEnd ( end );
            index = index2;
        }
    }
    private void copyChars ( final char[] c, final int dest, final int src, final int len ) {
        for ( int pos = 0; pos < len; ++pos ) {
            c[pos + dest] = c[pos + src];
        }
    }
    private boolean hasPath ( final String uri ) {
        int pos = uri.indexOf ( "://" );
        if ( pos < 0 ) {
            return false;
        }
        pos = uri.indexOf ( 47, pos + 3 );
        return pos >= 0;
    }
    protected String toEncoded ( final String url, final String sessionId ) {
        if ( url == null || sessionId == null ) {
            return url;
        }
        String path = url;
        String query = "";
        String anchor = "";
        final int question = url.indexOf ( 63 );
        if ( question >= 0 ) {
            path = url.substring ( 0, question );
            query = url.substring ( question );
        }
        final int pound = path.indexOf ( 35 );
        if ( pound >= 0 ) {
            anchor = path.substring ( pound );
            path = path.substring ( 0, pound );
        }
        final StringBuilder sb = new StringBuilder ( path );
        if ( sb.length() > 0 ) {
            sb.append ( ";" );
            sb.append ( SessionConfig.getSessionUriParamName ( this.request.getContext() ) );
            sb.append ( "=" );
            sb.append ( sessionId );
        }
        sb.append ( anchor );
        sb.append ( query );
        return sb.toString();
    }
    static {
        log = LogFactory.getLog ( Response.class );
        sm = StringManager.getManager ( Response.class );
        MEDIA_TYPE_CACHE = new MediaTypeCache ( 100 );
        ENFORCE_ENCODING_IN_GET_WRITER = Boolean.parseBoolean ( System.getProperty ( "org.apache.catalina.connector.Response.ENFORCE_ENCODING_IN_GET_WRITER", "true" ) );
    }
}
