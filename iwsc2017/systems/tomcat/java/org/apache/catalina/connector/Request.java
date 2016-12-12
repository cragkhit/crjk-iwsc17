package org.apache.catalina.connector;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.MultipartConfigElement;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletResponse;
import javax.servlet.SessionTrackingMode;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Mapping;
import javax.servlet.http.Part;
import javax.servlet.http.PushBuilder;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Host;
import org.apache.catalina.Manager;
import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.TomcatPrincipal;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.ApplicationMapping;
import org.apache.catalina.core.ApplicationPart;
import org.apache.catalina.core.ApplicationPushBuilder;
import org.apache.catalina.core.ApplicationSessionCookieConfig;
import org.apache.catalina.core.AsyncContextImpl;
import org.apache.catalina.mapper.MappingData;
import org.apache.catalina.util.ParameterMap;
import org.apache.catalina.util.URLEncoder;
import org.apache.coyote.ActionCode;
import org.apache.coyote.UpgradeToken;
import org.apache.coyote.http11.upgrade.InternalHttpUpgradeHandler;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.buf.B2CConverter;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.buf.UDecoder;
import org.apache.tomcat.util.http.CookieProcessor;
import org.apache.tomcat.util.http.FastHttpDateFormat;
import org.apache.tomcat.util.http.Parameters;
import org.apache.tomcat.util.http.Parameters.FailReason;
import org.apache.tomcat.util.http.ServerCookie;
import org.apache.tomcat.util.http.ServerCookies;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.apache.tomcat.util.http.fileupload.FileUploadBase.InvalidContentTypeException;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;
import org.apache.tomcat.util.http.parser.AcceptLanguage;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.res.StringManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
public class Request implements HttpServletRequest {
    private static final Log log = LogFactory.getLog ( Request.class );
    public Request() {
        formats = new SimpleDateFormat[formatsTemplate.length];
        for ( int i = 0; i < formats.length; i++ ) {
            formats[i] = ( SimpleDateFormat ) formatsTemplate[i].clone();
        }
    }
    protected org.apache.coyote.Request coyoteRequest;
    public void setCoyoteRequest ( org.apache.coyote.Request coyoteRequest ) {
        this.coyoteRequest = coyoteRequest;
        inputBuffer.setRequest ( coyoteRequest );
    }
    public org.apache.coyote.Request getCoyoteRequest() {
        return ( this.coyoteRequest );
    }
    protected static final TimeZone GMT_ZONE = TimeZone.getTimeZone ( "GMT" );
    protected static final StringManager sm = StringManager.getManager ( Request.class );
    protected Cookie[] cookies = null;
    protected final SimpleDateFormat formats[];
    private static final SimpleDateFormat formatsTemplate[] = {
        new SimpleDateFormat ( FastHttpDateFormat.RFC1123_DATE, Locale.US ),
        new SimpleDateFormat ( "EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US ),
        new SimpleDateFormat ( "EEE MMMM d HH:mm:ss yyyy", Locale.US )
    };
    protected static final Locale defaultLocale = Locale.getDefault();
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    protected boolean sslAttributesParsed = false;
    protected final ArrayList<Locale> locales = new ArrayList<>();
    private final transient HashMap<String, Object> notes = new HashMap<>();
    protected String authType = null;
    protected DispatcherType internalDispatcherType = null;
    protected final InputBuffer inputBuffer = new InputBuffer();
    protected CoyoteInputStream inputStream =
        new CoyoteInputStream ( inputBuffer );
    protected CoyoteReader reader = new CoyoteReader ( inputBuffer );
    protected boolean usingInputStream = false;
    protected boolean usingReader = false;
    protected Principal userPrincipal = null;
    protected boolean parametersParsed = false;
    protected boolean cookiesParsed = false;
    protected boolean cookiesConverted = false;
    protected boolean secure = false;
    protected transient Subject subject = null;
    protected static final int CACHED_POST_LEN = 8192;
    protected byte[] postData = null;
    protected ParameterMap<String, String[]> parameterMap = new ParameterMap<>();
    protected Collection<Part> parts = null;
    protected Exception partsParseException = null;
    protected Session session = null;
    protected Object requestDispatcherPath = null;
    protected boolean requestedSessionCookie = false;
    protected String requestedSessionId = null;
    protected boolean requestedSessionURL = false;
    protected boolean requestedSessionSSL = false;
    protected boolean localesParsed = false;
    protected int localPort = -1;
    protected String remoteAddr = null;
    protected String remoteHost = null;
    protected int remotePort = -1;
    protected String localAddr = null;
    protected String localName = null;
    private volatile AsyncContextImpl asyncContext = null;
    protected Boolean asyncSupported = null;
    private HttpServletRequest applicationRequest = null;
    protected void addPathParameter ( String name, String value ) {
        coyoteRequest.addPathParameter ( name, value );
    }
    protected String getPathParameter ( String name ) {
        return coyoteRequest.getPathParameter ( name );
    }
    public void setAsyncSupported ( boolean asyncSupported ) {
        this.asyncSupported = Boolean.valueOf ( asyncSupported );
    }
    public void recycle() {
        internalDispatcherType = null;
        requestDispatcherPath = null;
        authType = null;
        inputBuffer.recycle();
        usingInputStream = false;
        usingReader = false;
        userPrincipal = null;
        subject = null;
        parametersParsed = false;
        if ( parts != null ) {
            for ( Part part : parts ) {
                try {
                    part.delete();
                } catch ( IOException ignored ) {
                }
            }
            parts = null;
        }
        partsParseException = null;
        locales.clear();
        localesParsed = false;
        secure = false;
        remoteAddr = null;
        remoteHost = null;
        remotePort = -1;
        localPort = -1;
        localAddr = null;
        localName = null;
        attributes.clear();
        sslAttributesParsed = false;
        notes.clear();
        recycleSessionInfo();
        recycleCookieInfo ( false );
        if ( Globals.IS_SECURITY_ENABLED || Connector.RECYCLE_FACADES ) {
            parameterMap = new ParameterMap<>();
        } else {
            parameterMap.setLocked ( false );
            parameterMap.clear();
        }
        mappingData.recycle();
        applicationMapping.recycle();
        applicationRequest = null;
        if ( Globals.IS_SECURITY_ENABLED || Connector.RECYCLE_FACADES ) {
            if ( facade != null ) {
                facade.clear();
                facade = null;
            }
            if ( inputStream != null ) {
                inputStream.clear();
                inputStream = null;
            }
            if ( reader != null ) {
                reader.clear();
                reader = null;
            }
        }
        asyncSupported = null;
        if ( asyncContext != null ) {
            asyncContext.recycle();
        }
        asyncContext = null;
    }
    protected void recycleSessionInfo() {
        if ( session != null ) {
            try {
                session.endAccess();
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                log.warn ( sm.getString ( "coyoteRequest.sessionEndAccessFail" ), t );
            }
        }
        session = null;
        requestedSessionCookie = false;
        requestedSessionId = null;
        requestedSessionURL = false;
        requestedSessionSSL = false;
    }
    protected void recycleCookieInfo ( boolean recycleCoyote ) {
        cookiesParsed = false;
        cookiesConverted = false;
        cookies = null;
        if ( recycleCoyote ) {
            getCoyoteRequest().getCookies().recycle();
        }
    }
    protected Connector connector;
    public Connector getConnector() {
        return this.connector;
    }
    public void setConnector ( Connector connector ) {
        this.connector = connector;
    }
    public Context getContext() {
        return mappingData.context;
    }
    @Deprecated
    public void setContext ( Context context ) {
        mappingData.context = context;
    }
    protected FilterChain filterChain = null;
    public FilterChain getFilterChain() {
        return this.filterChain;
    }
    public void setFilterChain ( FilterChain filterChain ) {
        this.filterChain = filterChain;
    }
    public Host getHost() {
        return mappingData.host;
    }
    protected final MappingData mappingData = new MappingData();
    private final ApplicationMapping applicationMapping = new ApplicationMapping ( mappingData );
    public MappingData getMappingData() {
        return mappingData;
    }
    protected RequestFacade facade = null;
    public HttpServletRequest getRequest() {
        if ( facade == null ) {
            facade = new RequestFacade ( this );
        }
        if ( applicationRequest == null ) {
            applicationRequest = facade;
        }
        return applicationRequest;
    }
    public void setRequest ( HttpServletRequest applicationRequest ) {
        ServletRequest r = applicationRequest;
        while ( r instanceof HttpServletRequestWrapper ) {
            r = ( ( HttpServletRequestWrapper ) r ).getRequest();
        }
        if ( r != facade ) {
            throw new IllegalArgumentException ( sm.getString ( "request.illegalWrap" ) );
        }
        this.applicationRequest = applicationRequest;
    }
    protected org.apache.catalina.connector.Response response = null;
    public org.apache.catalina.connector.Response getResponse() {
        return this.response;
    }
    public void setResponse ( org.apache.catalina.connector.Response response ) {
        this.response = response;
    }
    public InputStream getStream() {
        if ( inputStream == null ) {
            inputStream = new CoyoteInputStream ( inputBuffer );
        }
        return inputStream;
    }
    protected B2CConverter URIConverter = null;
    protected B2CConverter getURIConverter() {
        return URIConverter;
    }
    protected void setURIConverter ( B2CConverter URIConverter ) {
        this.URIConverter = URIConverter;
    }
    public Wrapper getWrapper() {
        return mappingData.wrapper;
    }
    @Deprecated
    public void setWrapper ( Wrapper wrapper ) {
        mappingData.wrapper = wrapper;
    }
    public ServletInputStream createInputStream()
    throws IOException {
        if ( inputStream == null ) {
            inputStream = new CoyoteInputStream ( inputBuffer );
        }
        return inputStream;
    }
    public void finishRequest() throws IOException {
        if ( response.getStatus() == HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE ) {
            checkSwallowInput();
        }
    }
    public Object getNote ( String name ) {
        return notes.get ( name );
    }
    public void removeNote ( String name ) {
        notes.remove ( name );
    }
    public void setLocalPort ( int port ) {
        localPort = port;
    }
    public void setNote ( String name, Object value ) {
        notes.put ( name, value );
    }
    public void setRemoteAddr ( String remoteAddr ) {
        this.remoteAddr = remoteAddr;
    }
    public void setRemoteHost ( String remoteHost ) {
        this.remoteHost = remoteHost;
    }
    public void setSecure ( boolean secure ) {
        this.secure = secure;
    }
    public void setServerPort ( int port ) {
        coyoteRequest.setServerPort ( port );
    }
    @Override
    public Object getAttribute ( String name ) {
        SpecialAttributeAdapter adapter = specialAttributes.get ( name );
        if ( adapter != null ) {
            return adapter.get ( this, name );
        }
        Object attr = attributes.get ( name );
        if ( attr != null ) {
            return ( attr );
        }
        attr =  coyoteRequest.getAttribute ( name );
        if ( attr != null ) {
            return attr;
        }
        if ( isSSLAttribute ( name ) || name.equals ( SSLSupport.PROTOCOL_VERSION_KEY ) ) {
            coyoteRequest.action ( ActionCode.REQ_SSL_ATTRIBUTE,
                                   coyoteRequest );
            attr = coyoteRequest.getAttribute ( Globals.CERTIFICATES_ATTR );
            if ( attr != null ) {
                attributes.put ( Globals.CERTIFICATES_ATTR, attr );
            }
            attr = coyoteRequest.getAttribute ( Globals.CIPHER_SUITE_ATTR );
            if ( attr != null ) {
                attributes.put ( Globals.CIPHER_SUITE_ATTR, attr );
            }
            attr = coyoteRequest.getAttribute ( Globals.KEY_SIZE_ATTR );
            if ( attr != null ) {
                attributes.put ( Globals.KEY_SIZE_ATTR, attr );
            }
            attr = coyoteRequest.getAttribute ( Globals.SSL_SESSION_ID_ATTR );
            if ( attr != null ) {
                attributes.put ( Globals.SSL_SESSION_ID_ATTR, attr );
            }
            attr = coyoteRequest.getAttribute ( Globals.SSL_SESSION_MGR_ATTR );
            if ( attr != null ) {
                attributes.put ( Globals.SSL_SESSION_MGR_ATTR, attr );
            }
            attr = coyoteRequest.getAttribute ( SSLSupport.PROTOCOL_VERSION_KEY );
            if ( attr != null ) {
                attributes.put ( SSLSupport.PROTOCOL_VERSION_KEY, attr );
            }
            attr = attributes.get ( name );
            sslAttributesParsed = true;
        }
        return attr;
    }
    @Override
    public long getContentLengthLong() {
        return coyoteRequest.getContentLengthLong();
    }
    static boolean isSSLAttribute ( String name ) {
        return Globals.CERTIFICATES_ATTR.equals ( name ) ||
               Globals.CIPHER_SUITE_ATTR.equals ( name ) ||
               Globals.KEY_SIZE_ATTR.equals ( name )  ||
               Globals.SSL_SESSION_ID_ATTR.equals ( name ) ||
               Globals.SSL_SESSION_MGR_ATTR.equals ( name );
    }
    @Override
    public Enumeration<String> getAttributeNames() {
        if ( isSecure() && !sslAttributesParsed ) {
            getAttribute ( Globals.CERTIFICATES_ATTR );
        }
        Set<String> names = new HashSet<>();
        names.addAll ( attributes.keySet() );
        return Collections.enumeration ( names );
    }
    @Override
    public String getCharacterEncoding() {
        return coyoteRequest.getCharacterEncoding();
    }
    @Override
    public int getContentLength() {
        return coyoteRequest.getContentLength();
    }
    @Override
    public String getContentType() {
        return coyoteRequest.getContentType();
    }
    public void setContentType ( String contentType ) {
        coyoteRequest.setContentType ( contentType );
    }
    @Override
    public ServletInputStream getInputStream() throws IOException {
        if ( usingReader ) {
            throw new IllegalStateException
            ( sm.getString ( "coyoteRequest.getInputStream.ise" ) );
        }
        usingInputStream = true;
        if ( inputStream == null ) {
            inputStream = new CoyoteInputStream ( inputBuffer );
        }
        return inputStream;
    }
    @Override
    public Locale getLocale() {
        if ( !localesParsed ) {
            parseLocales();
        }
        if ( locales.size() > 0 ) {
            return locales.get ( 0 );
        }
        return defaultLocale;
    }
    @Override
    public Enumeration<Locale> getLocales() {
        if ( !localesParsed ) {
            parseLocales();
        }
        if ( locales.size() > 0 ) {
            return Collections.enumeration ( locales );
        }
        ArrayList<Locale> results = new ArrayList<>();
        results.add ( defaultLocale );
        return Collections.enumeration ( results );
    }
    @Override
    public String getParameter ( String name ) {
        if ( !parametersParsed ) {
            parseParameters();
        }
        return coyoteRequest.getParameters().getParameter ( name );
    }
    @Override
    public Map<String, String[]> getParameterMap() {
        if ( parameterMap.isLocked() ) {
            return parameterMap;
        }
        Enumeration<String> enumeration = getParameterNames();
        while ( enumeration.hasMoreElements() ) {
            String name = enumeration.nextElement();
            String[] values = getParameterValues ( name );
            parameterMap.put ( name, values );
        }
        parameterMap.setLocked ( true );
        return parameterMap;
    }
    @Override
    public Enumeration<String> getParameterNames() {
        if ( !parametersParsed ) {
            parseParameters();
        }
        return coyoteRequest.getParameters().getParameterNames();
    }
    @Override
    public String[] getParameterValues ( String name ) {
        if ( !parametersParsed ) {
            parseParameters();
        }
        return coyoteRequest.getParameters().getParameterValues ( name );
    }
    @Override
    public String getProtocol() {
        return coyoteRequest.protocol().toString();
    }
    @Override
    public BufferedReader getReader() throws IOException {
        if ( usingInputStream ) {
            throw new IllegalStateException
            ( sm.getString ( "coyoteRequest.getReader.ise" ) );
        }
        usingReader = true;
        inputBuffer.checkConverter();
        if ( reader == null ) {
            reader = new CoyoteReader ( inputBuffer );
        }
        return reader;
    }
    @Override
    @Deprecated
    public String getRealPath ( String path ) {
        Context context = getContext();
        if ( context == null ) {
            return null;
        }
        ServletContext servletContext = context.getServletContext();
        if ( servletContext == null ) {
            return null;
        }
        try {
            return ( servletContext.getRealPath ( path ) );
        } catch ( IllegalArgumentException e ) {
            return null;
        }
    }
    @Override
    public String getRemoteAddr() {
        if ( remoteAddr == null ) {
            coyoteRequest.action
            ( ActionCode.REQ_HOST_ADDR_ATTRIBUTE, coyoteRequest );
            remoteAddr = coyoteRequest.remoteAddr().toString();
        }
        return remoteAddr;
    }
    @Override
    public String getRemoteHost() {
        if ( remoteHost == null ) {
            if ( !connector.getEnableLookups() ) {
                remoteHost = getRemoteAddr();
            } else {
                coyoteRequest.action
                ( ActionCode.REQ_HOST_ATTRIBUTE, coyoteRequest );
                remoteHost = coyoteRequest.remoteHost().toString();
            }
        }
        return remoteHost;
    }
    @Override
    public int getRemotePort() {
        if ( remotePort == -1 ) {
            coyoteRequest.action
            ( ActionCode.REQ_REMOTEPORT_ATTRIBUTE, coyoteRequest );
            remotePort = coyoteRequest.getRemotePort();
        }
        return remotePort;
    }
    @Override
    public String getLocalName() {
        if ( localName == null ) {
            coyoteRequest.action
            ( ActionCode.REQ_LOCAL_NAME_ATTRIBUTE, coyoteRequest );
            localName = coyoteRequest.localName().toString();
        }
        return localName;
    }
    @Override
    public String getLocalAddr() {
        if ( localAddr == null ) {
            coyoteRequest.action
            ( ActionCode.REQ_LOCAL_ADDR_ATTRIBUTE, coyoteRequest );
            localAddr = coyoteRequest.localAddr().toString();
        }
        return localAddr;
    }
    @Override
    public int getLocalPort() {
        if ( localPort == -1 ) {
            coyoteRequest.action
            ( ActionCode.REQ_LOCALPORT_ATTRIBUTE, coyoteRequest );
            localPort = coyoteRequest.getLocalPort();
        }
        return localPort;
    }
    @Override
    public RequestDispatcher getRequestDispatcher ( String path ) {
        Context context = getContext();
        if ( context == null ) {
            return null;
        }
        if ( path == null ) {
            return null;
        } else if ( path.startsWith ( "/" ) ) {
            return ( context.getServletContext().getRequestDispatcher ( path ) );
        }
        String servletPath = ( String ) getAttribute (
                                 RequestDispatcher.INCLUDE_SERVLET_PATH );
        if ( servletPath == null ) {
            servletPath = getServletPath();
        }
        String pathInfo = getPathInfo();
        String requestPath = null;
        if ( pathInfo == null ) {
            requestPath = servletPath;
        } else {
            requestPath = servletPath + pathInfo;
        }
        int pos = requestPath.lastIndexOf ( '/' );
        String relative = null;
        if ( context.getDispatchersUseEncodedPaths() ) {
            if ( pos >= 0 ) {
                relative = URLEncoder.DEFAULT.encode (
                               requestPath.substring ( 0, pos + 1 ), "UTF-8" ) + path;
            } else {
                relative = URLEncoder.DEFAULT.encode ( requestPath, "UTF-8" ) + path;
            }
        } else {
            if ( pos >= 0 ) {
                relative = requestPath.substring ( 0, pos + 1 ) + path;
            } else {
                relative = requestPath + path;
            }
        }
        return context.getServletContext().getRequestDispatcher ( relative );
    }
    @Override
    public String getScheme() {
        return coyoteRequest.scheme().toString();
    }
    @Override
    public String getServerName() {
        return coyoteRequest.serverName().toString();
    }
    @Override
    public int getServerPort() {
        return coyoteRequest.getServerPort();
    }
    @Override
    public boolean isSecure() {
        return secure;
    }
    @Override
    public void removeAttribute ( String name ) {
        if ( name.startsWith ( "org.apache.tomcat." ) ) {
            coyoteRequest.getAttributes().remove ( name );
        }
        boolean found = attributes.containsKey ( name );
        if ( found ) {
            Object value = attributes.get ( name );
            attributes.remove ( name );
            notifyAttributeRemoved ( name, value );
        } else {
            return;
        }
    }
    @Override
    public void setAttribute ( String name, Object value ) {
        if ( name == null ) {
            throw new IllegalArgumentException
            ( sm.getString ( "coyoteRequest.setAttribute.namenull" ) );
        }
        if ( value == null ) {
            removeAttribute ( name );
            return;
        }
        SpecialAttributeAdapter adapter = specialAttributes.get ( name );
        if ( adapter != null ) {
            adapter.set ( this, name, value );
            return;
        }
        if ( Globals.IS_SECURITY_ENABLED &&
                name.equals ( Globals.SENDFILE_FILENAME_ATTR ) ) {
            String canonicalPath;
            try {
                canonicalPath = new File ( value.toString() ).getCanonicalPath();
            } catch ( IOException e ) {
                throw new SecurityException ( sm.getString (
                                                  "coyoteRequest.sendfileNotCanonical", value ), e );
            }
            System.getSecurityManager().checkRead ( canonicalPath );
            value = canonicalPath;
        }
        Object oldValue = attributes.put ( name, value );
        if ( name.startsWith ( "org.apache.tomcat." ) ) {
            coyoteRequest.setAttribute ( name, value );
        }
        notifyAttributeAssigned ( name, value, oldValue );
    }
    private void notifyAttributeAssigned ( String name, Object value,
                                           Object oldValue ) {
        Context context = getContext();
        Object listeners[] = context.getApplicationEventListeners();
        if ( ( listeners == null ) || ( listeners.length == 0 ) ) {
            return;
        }
        boolean replaced = ( oldValue != null );
        ServletRequestAttributeEvent event = null;
        if ( replaced ) {
            event = new ServletRequestAttributeEvent (
                context.getServletContext(), getRequest(), name, oldValue );
        } else {
            event = new ServletRequestAttributeEvent (
                context.getServletContext(), getRequest(), name, value );
        }
        for ( int i = 0; i < listeners.length; i++ ) {
            if ( ! ( listeners[i] instanceof ServletRequestAttributeListener ) ) {
                continue;
            }
            ServletRequestAttributeListener listener =
                ( ServletRequestAttributeListener ) listeners[i];
            try {
                if ( replaced ) {
                    listener.attributeReplaced ( event );
                } else {
                    listener.attributeAdded ( event );
                }
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                attributes.put ( RequestDispatcher.ERROR_EXCEPTION, t );
                context.getLogger().error ( sm.getString ( "coyoteRequest.attributeEvent" ), t );
            }
        }
    }
    private void notifyAttributeRemoved ( String name, Object value ) {
        Context context = getContext();
        Object listeners[] = context.getApplicationEventListeners();
        if ( ( listeners == null ) || ( listeners.length == 0 ) ) {
            return;
        }
        ServletRequestAttributeEvent event =
            new ServletRequestAttributeEvent ( context.getServletContext(),
                                               getRequest(), name, value );
        for ( int i = 0; i < listeners.length; i++ ) {
            if ( ! ( listeners[i] instanceof ServletRequestAttributeListener ) ) {
                continue;
            }
            ServletRequestAttributeListener listener =
                ( ServletRequestAttributeListener ) listeners[i];
            try {
                listener.attributeRemoved ( event );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                attributes.put ( RequestDispatcher.ERROR_EXCEPTION, t );
                context.getLogger().error ( sm.getString ( "coyoteRequest.attributeEvent" ), t );
            }
        }
    }
    @Override
    public void setCharacterEncoding ( String enc )
    throws UnsupportedEncodingException {
        if ( usingReader ) {
            return;
        }
        B2CConverter.getCharset ( enc );
        coyoteRequest.setCharacterEncoding ( enc );
    }
    @Override
    public ServletContext getServletContext() {
        return getContext().getServletContext();
    }
    @Override
    public AsyncContext startAsync() {
        return startAsync ( getRequest(), response.getResponse() );
    }
    @Override
    public AsyncContext startAsync ( ServletRequest request,
                                     ServletResponse response ) {
        if ( !isAsyncSupported() ) {
            throw new IllegalStateException ( sm.getString ( "request.asyncNotSupported" ) );
        }
        if ( asyncContext == null ) {
            asyncContext = new AsyncContextImpl ( this );
        }
        asyncContext.setStarted ( getContext(), request, response,
                                  request == getRequest() && response == getResponse().getResponse() );
        asyncContext.setTimeout ( getConnector().getAsyncTimeout() );
        return asyncContext;
    }
    @Override
    public boolean isAsyncStarted() {
        if ( asyncContext == null ) {
            return false;
        }
        return asyncContext.isStarted();
    }
    public boolean isAsyncDispatching() {
        if ( asyncContext == null ) {
            return false;
        }
        AtomicBoolean result = new AtomicBoolean ( false );
        coyoteRequest.action ( ActionCode.ASYNC_IS_DISPATCHING, result );
        return result.get();
    }
    public boolean isAsyncCompleting() {
        if ( asyncContext == null ) {
            return false;
        }
        AtomicBoolean result = new AtomicBoolean ( false );
        coyoteRequest.action ( ActionCode.ASYNC_IS_COMPLETING, result );
        return result.get();
    }
    public boolean isAsync() {
        if ( asyncContext == null ) {
            return false;
        }
        AtomicBoolean result = new AtomicBoolean ( false );
        coyoteRequest.action ( ActionCode.ASYNC_IS_ASYNC, result );
        return result.get();
    }
    @Override
    public boolean isAsyncSupported() {
        if ( this.asyncSupported == null ) {
            return true;
        }
        return asyncSupported.booleanValue();
    }
    @Override
    public AsyncContext getAsyncContext() {
        if ( !isAsyncStarted() ) {
            throw new IllegalStateException ( sm.getString ( "request.notAsync" ) );
        }
        return asyncContext;
    }
    public AsyncContextImpl getAsyncContextInternal() {
        return asyncContext;
    }
    @Override
    public DispatcherType getDispatcherType() {
        if ( internalDispatcherType == null ) {
            return DispatcherType.REQUEST;
        }
        return this.internalDispatcherType;
    }
    public void addCookie ( Cookie cookie ) {
        if ( !cookiesConverted ) {
            convertCookies();
        }
        int size = 0;
        if ( cookies != null ) {
            size = cookies.length;
        }
        Cookie[] newCookies = new Cookie[size + 1];
        for ( int i = 0; i < size; i++ ) {
            newCookies[i] = cookies[i];
        }
        newCookies[size] = cookie;
        cookies = newCookies;
    }
    public void addLocale ( Locale locale ) {
        locales.add ( locale );
    }
    public void clearCookies() {
        cookiesParsed = true;
        cookiesConverted = true;
        cookies = null;
    }
    public void clearLocales() {
        locales.clear();
    }
    public void setAuthType ( String type ) {
        this.authType = type;
    }
    public void setPathInfo ( String path ) {
        mappingData.pathInfo.setString ( path );
    }
    public void setRequestedSessionCookie ( boolean flag ) {
        this.requestedSessionCookie = flag;
    }
    public void setRequestedSessionId ( String id ) {
        this.requestedSessionId = id;
    }
    public void setRequestedSessionURL ( boolean flag ) {
        this.requestedSessionURL = flag;
    }
    public void setRequestedSessionSSL ( boolean flag ) {
        this.requestedSessionSSL = flag;
    }
    public String getDecodedRequestURI() {
        return coyoteRequest.decodedURI().toString();
    }
    public MessageBytes getDecodedRequestURIMB() {
        return coyoteRequest.decodedURI();
    }
    public void setUserPrincipal ( Principal principal ) {
        if ( Globals.IS_SECURITY_ENABLED ) {
            HttpSession session = getSession ( false );
            if ( ( subject != null ) &&
                    ( !subject.getPrincipals().contains ( principal ) ) ) {
                subject.getPrincipals().add ( principal );
            } else if ( session != null &&
                        session.getAttribute ( Globals.SUBJECT_ATTR ) == null ) {
                subject = new Subject();
                subject.getPrincipals().add ( principal );
            }
            if ( session != null ) {
                session.setAttribute ( Globals.SUBJECT_ATTR, subject );
            }
        }
        this.userPrincipal = principal;
    }
    @Override
    public boolean isPushSupported() {
        AtomicBoolean result = new AtomicBoolean();
        coyoteRequest.action ( ActionCode.IS_PUSH_SUPPORTED, result );
        return result.get();
    }
    @Override
    public PushBuilder getPushBuilder() {
        return new ApplicationPushBuilder ( this );
    }
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T extends HttpUpgradeHandler> T upgrade (
        Class<T> httpUpgradeHandlerClass ) throws java.io.IOException, ServletException {
        T handler;
        InstanceManager instanceManager = null;
        try {
            if ( InternalHttpUpgradeHandler.class.isAssignableFrom ( httpUpgradeHandlerClass ) ) {
                handler = httpUpgradeHandlerClass.newInstance();
            } else {
                instanceManager = getContext().getInstanceManager();
                handler = ( T ) instanceManager.newInstance ( httpUpgradeHandlerClass );
            }
        } catch ( InstantiationException | IllegalAccessException | InvocationTargetException | NamingException e ) {
            throw new ServletException ( e );
        }
        UpgradeToken upgradeToken = new UpgradeToken ( handler,
                getContext(), instanceManager );
        coyoteRequest.action ( ActionCode.UPGRADE, upgradeToken );
        response.setStatus ( HttpServletResponse.SC_SWITCHING_PROTOCOLS );
        return handler;
    }
    @Override
    public String getAuthType() {
        return authType;
    }
    @Override
    public String getContextPath() {
        String canonicalContextPath = getServletContext().getContextPath();
        String uri = getRequestURI();
        char[] uriChars = uri.toCharArray();
        int lastSlash = mappingData.contextSlashCount;
        if ( lastSlash == 0 ) {
            return "";
        }
        int pos = 0;
        while ( lastSlash > 0 ) {
            pos = nextSlash ( uriChars, pos + 1 );
            if ( pos == -1 ) {
                break;
            }
            lastSlash--;
        }
        String candidate;
        if ( pos == -1 ) {
            candidate = uri;
        } else {
            candidate = uri.substring ( 0, pos );
        }
        candidate = removePathParameters ( candidate );
        candidate = UDecoder.URLDecode ( candidate, connector.getURIEncoding() );
        candidate = org.apache.tomcat.util.http.RequestUtil.normalize ( candidate );
        boolean match = canonicalContextPath.equals ( candidate );
        while ( !match && pos != -1 ) {
            pos = nextSlash ( uriChars, pos + 1 );
            if ( pos == -1 ) {
                candidate = uri;
            } else {
                candidate = uri.substring ( 0, pos );
            }
            candidate = removePathParameters ( candidate );
            candidate = UDecoder.URLDecode ( candidate, connector.getURIEncoding() );
            candidate = org.apache.tomcat.util.http.RequestUtil.normalize ( candidate );
            match = canonicalContextPath.equals ( candidate );
        }
        if ( match ) {
            if ( pos == -1 ) {
                return uri;
            } else {
                return uri.substring ( 0, pos );
            }
        } else {
            throw new IllegalStateException ( sm.getString (
                                                  "coyoteRequest.getContextPath.ise", canonicalContextPath, uri ) );
        }
    }
    private String removePathParameters ( String input ) {
        int nextSemiColon = input.indexOf ( ';' );
        if ( nextSemiColon == -1 ) {
            return input;
        }
        StringBuilder result = new StringBuilder ( input.length() );
        result.append ( input.substring ( 0, nextSemiColon ) );
        while ( true ) {
            int nextSlash = input.indexOf ( '/', nextSemiColon );
            if ( nextSlash == -1 ) {
                break;
            }
            nextSemiColon = input.indexOf ( ';', nextSlash );
            if ( nextSemiColon == -1 ) {
                result.append ( input.substring ( nextSlash ) );
                break;
            } else {
                result.append ( input.substring ( nextSlash, nextSemiColon ) );
            }
        }
        return result.toString();
    }
    private int nextSlash ( char[] uri, int startPos ) {
        int len = uri.length;
        int pos = startPos;
        while ( pos < len ) {
            if ( uri[pos] == '/' ) {
                return pos;
            } else if ( UDecoder.ALLOW_ENCODED_SLASH && uri[pos] == '%' && pos + 2 < len &&
                        uri[pos + 1] == '2' && ( uri[pos + 2] == 'f' || uri[pos + 2] == 'F' ) ) {
                return pos;
            }
            pos++;
        }
        return -1;
    }
    @Override
    public Cookie[] getCookies() {
        if ( !cookiesConverted ) {
            convertCookies();
        }
        return cookies;
    }
    public ServerCookies getServerCookies() {
        parseCookies();
        return coyoteRequest.getCookies();
    }
    @Override
    public long getDateHeader ( String name ) {
        String value = getHeader ( name );
        if ( value == null ) {
            return ( -1L );
        }
        long result = FastHttpDateFormat.parseDate ( value, formats );
        if ( result != ( -1L ) ) {
            return result;
        }
        throw new IllegalArgumentException ( value );
    }
    @Override
    public String getHeader ( String name ) {
        return coyoteRequest.getHeader ( name );
    }
    @Override
    public Enumeration<String> getHeaders ( String name ) {
        return coyoteRequest.getMimeHeaders().values ( name );
    }
    @Override
    public Enumeration<String> getHeaderNames() {
        return coyoteRequest.getMimeHeaders().names();
    }
    @Override
    public int getIntHeader ( String name ) {
        String value = getHeader ( name );
        if ( value == null ) {
            return ( -1 );
        }
        return Integer.parseInt ( value );
    }
    @Override
    public Mapping getMapping() {
        return applicationMapping.getMapping();
    }
    @Override
    public String getMethod() {
        return coyoteRequest.method().toString();
    }
    @Override
    public String getPathInfo() {
        return mappingData.pathInfo.toString();
    }
    @Override
    public String getPathTranslated() {
        Context context = getContext();
        if ( context == null ) {
            return null;
        }
        if ( getPathInfo() == null ) {
            return null;
        }
        return context.getServletContext().getRealPath ( getPathInfo() );
    }
    @Override
    public String getQueryString() {
        return coyoteRequest.queryString().toString();
    }
    @Override
    public String getRemoteUser() {
        if ( userPrincipal == null ) {
            return null;
        }
        return userPrincipal.getName();
    }
    public MessageBytes getRequestPathMB() {
        return mappingData.requestPath;
    }
    @Override
    public String getRequestedSessionId() {
        return requestedSessionId;
    }
    @Override
    public String getRequestURI() {
        return coyoteRequest.requestURI().toString();
    }
    @Override
    public StringBuffer getRequestURL() {
        StringBuffer url = new StringBuffer();
        String scheme = getScheme();
        int port = getServerPort();
        if ( port < 0 ) {
            port = 80;
        }
        url.append ( scheme );
        url.append ( "://" );
        url.append ( getServerName() );
        if ( ( scheme.equals ( "http" ) && ( port != 80 ) )
                || ( scheme.equals ( "https" ) && ( port != 443 ) ) ) {
            url.append ( ':' );
            url.append ( port );
        }
        url.append ( getRequestURI() );
        return url;
    }
    @Override
    public String getServletPath() {
        return ( mappingData.wrapperPath.toString() );
    }
    @Override
    public HttpSession getSession() {
        Session session = doGetSession ( true );
        if ( session == null ) {
            return null;
        }
        return session.getSession();
    }
    @Override
    public HttpSession getSession ( boolean create ) {
        Session session = doGetSession ( create );
        if ( session == null ) {
            return null;
        }
        return session.getSession();
    }
    @Override
    public boolean isRequestedSessionIdFromCookie() {
        if ( requestedSessionId == null ) {
            return false;
        }
        return requestedSessionCookie;
    }
    @Override
    public boolean isRequestedSessionIdFromURL() {
        if ( requestedSessionId == null ) {
            return false;
        }
        return requestedSessionURL;
    }
    @Override
    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return ( isRequestedSessionIdFromURL() );
    }
    @Override
    public boolean isRequestedSessionIdValid() {
        if ( requestedSessionId == null ) {
            return false;
        }
        Context context = getContext();
        if ( context == null ) {
            return false;
        }
        Manager manager = context.getManager();
        if ( manager == null ) {
            return false;
        }
        Session session = null;
        try {
            session = manager.findSession ( requestedSessionId );
        } catch ( IOException e ) {
        }
        if ( ( session == null ) || !session.isValid() ) {
            if ( getMappingData().contexts == null ) {
                return false;
            } else {
                for ( int i = ( getMappingData().contexts.length ); i > 0; i-- ) {
                    Context ctxt = getMappingData().contexts[i - 1];
                    try {
                        if ( ctxt.getManager().findSession ( requestedSessionId ) !=
                                null ) {
                            return true;
                        }
                    } catch ( IOException e ) {
                    }
                }
                return false;
            }
        }
        return true;
    }
    @Override
    public boolean isUserInRole ( String role ) {
        if ( userPrincipal == null ) {
            return false;
        }
        Context context = getContext();
        if ( context == null ) {
            return false;
        }
        if ( "*".equals ( role ) ) {
            return false;
        }
        if ( "**".equals ( role ) && !context.findSecurityRole ( "**" ) ) {
            return userPrincipal != null;
        }
        Realm realm = context.getRealm();
        if ( realm == null ) {
            return false;
        }
        return ( realm.hasRole ( getWrapper(), userPrincipal, role ) );
    }
    public Principal getPrincipal() {
        return userPrincipal;
    }
    @Override
    public Principal getUserPrincipal() {
        if ( userPrincipal instanceof TomcatPrincipal ) {
            GSSCredential gssCredential =
                ( ( TomcatPrincipal ) userPrincipal ).getGssCredential();
            if ( gssCredential != null ) {
                int left = -1;
                try {
                    left = gssCredential.getRemainingLifetime();
                } catch ( GSSException e ) {
                    log.warn ( sm.getString ( "coyoteRequest.gssLifetimeFail",
                                              userPrincipal.getName() ), e );
                }
                if ( left == 0 ) {
                    try {
                        logout();
                    } catch ( ServletException e ) {
                    }
                    return null;
                }
            }
            return ( ( TomcatPrincipal ) userPrincipal ).getUserPrincipal();
        }
        return userPrincipal;
    }
    public Session getSessionInternal() {
        return doGetSession ( true );
    }
    public void changeSessionId ( String newSessionId ) {
        if ( requestedSessionId != null && requestedSessionId.length() > 0 ) {
            requestedSessionId = newSessionId;
        }
        Context context = getContext();
        if ( context != null
                && !context.getServletContext()
                .getEffectiveSessionTrackingModes()
                .contains ( SessionTrackingMode.COOKIE ) ) {
            return;
        }
        if ( response != null ) {
            Cookie newCookie =
                ApplicationSessionCookieConfig.createSessionCookie ( context,
                        newSessionId, isSecure() );
            response.addSessionCookieInternal ( newCookie );
        }
    }
    @Override
    public String changeSessionId() {
        Session session = this.getSessionInternal ( false );
        if ( session == null ) {
            throw new IllegalStateException (
                sm.getString ( "coyoteRequest.changeSessionId" ) );
        }
        Manager manager = this.getContext().getManager();
        manager.changeSessionId ( session );
        String newSessionId = session.getId();
        this.changeSessionId ( newSessionId );
        return newSessionId;
    }
    public Session getSessionInternal ( boolean create ) {
        return doGetSession ( create );
    }
    public boolean isParametersParsed() {
        return parametersParsed;
    }
    public boolean isFinished() {
        return coyoteRequest.isFinished();
    }
    protected void checkSwallowInput() {
        Context context = getContext();
        if ( context != null && !context.getSwallowAbortedUploads() ) {
            coyoteRequest.action ( ActionCode.DISABLE_SWALLOW_INPUT, null );
        }
    }
    @Override
    public boolean authenticate ( HttpServletResponse response )
    throws IOException, ServletException {
        if ( response.isCommitted() ) {
            throw new IllegalStateException (
                sm.getString ( "coyoteRequest.authenticate.ise" ) );
        }
        return getContext().getAuthenticator().authenticate ( this, response );
    }
    @Override
    public void login ( String username, String password )
    throws ServletException {
        if ( getAuthType() != null || getRemoteUser() != null ||
                getUserPrincipal() != null ) {
            throw new ServletException (
                sm.getString ( "coyoteRequest.alreadyAuthenticated" ) );
        }
        Context context = getContext();
        if ( context.getAuthenticator() == null ) {
            throw new ServletException ( "no authenticator" );
        }
        context.getAuthenticator().login ( username, password, this );
    }
    @Override
    public void logout() throws ServletException {
        getContext().getAuthenticator().logout ( this );
    }
    @Override
    public Collection<Part> getParts() throws IOException, IllegalStateException,
        ServletException {
        parseParts ( true );
        if ( partsParseException != null ) {
            if ( partsParseException instanceof IOException ) {
                throw ( IOException ) partsParseException;
            } else if ( partsParseException instanceof IllegalStateException ) {
                throw ( IllegalStateException ) partsParseException;
            } else if ( partsParseException instanceof ServletException ) {
                throw ( ServletException ) partsParseException;
            }
        }
        return parts;
    }
    private void parseParts ( boolean explicit ) {
        if ( parts != null || partsParseException != null ) {
            return;
        }
        Context context = getContext();
        MultipartConfigElement mce = getWrapper().getMultipartConfigElement();
        if ( mce == null ) {
            if ( context.getAllowCasualMultipartParsing() ) {
                mce = new MultipartConfigElement ( null,
                                                   connector.getMaxPostSize(),
                                                   connector.getMaxPostSize(),
                                                   connector.getMaxPostSize() );
            } else {
                if ( explicit ) {
                    partsParseException = new IllegalStateException (
                        sm.getString ( "coyoteRequest.noMultipartConfig" ) );
                    return;
                } else {
                    parts = Collections.emptyList();
                    return;
                }
            }
        }
        Parameters parameters = coyoteRequest.getParameters();
        parameters.setLimit ( getConnector().getMaxParameterCount() );
        boolean success = false;
        try {
            File location;
            String locationStr = mce.getLocation();
            if ( locationStr == null || locationStr.length() == 0 ) {
                location = ( ( File ) context.getServletContext().getAttribute (
                                 ServletContext.TEMPDIR ) );
            } else {
                location = new File ( locationStr );
                if ( !location.isAbsolute() ) {
                    location = new File (
                        ( File ) context.getServletContext().getAttribute (
                            ServletContext.TEMPDIR ),
                        locationStr ).getAbsoluteFile();
                }
            }
            if ( !location.isDirectory() ) {
                parameters.setParseFailedReason ( FailReason.MULTIPART_CONFIG_INVALID );
                partsParseException = new IOException (
                    sm.getString ( "coyoteRequest.uploadLocationInvalid",
                                   location ) );
                return;
            }
            DiskFileItemFactory factory = new DiskFileItemFactory();
            try {
                factory.setRepository ( location.getCanonicalFile() );
            } catch ( IOException ioe ) {
                parameters.setParseFailedReason ( FailReason.IO_ERROR );
                partsParseException = ioe;
                return;
            }
            factory.setSizeThreshold ( mce.getFileSizeThreshold() );
            ServletFileUpload upload = new ServletFileUpload();
            upload.setFileItemFactory ( factory );
            upload.setFileSizeMax ( mce.getMaxFileSize() );
            upload.setSizeMax ( mce.getMaxRequestSize() );
            parts = new ArrayList<>();
            try {
                List<FileItem> items =
                    upload.parseRequest ( new ServletRequestContext ( this ) );
                int maxPostSize = getConnector().getMaxPostSize();
                int postSize = 0;
                String enc = getCharacterEncoding();
                Charset charset = null;
                if ( enc != null ) {
                    try {
                        charset = B2CConverter.getCharset ( enc );
                    } catch ( UnsupportedEncodingException e ) {
                    }
                }
                for ( FileItem item : items ) {
                    ApplicationPart part = new ApplicationPart ( item, location );
                    parts.add ( part );
                    if ( part.getSubmittedFileName() == null ) {
                        String name = part.getName();
                        String value = null;
                        try {
                            String encoding = parameters.getEncoding();
                            if ( encoding == null ) {
                                if ( enc == null ) {
                                    encoding = Parameters.DEFAULT_ENCODING;
                                } else {
                                    encoding = enc;
                                }
                            }
                            value = part.getString ( encoding );
                        } catch ( UnsupportedEncodingException uee ) {
                            try {
                                value = part.getString ( Parameters.DEFAULT_ENCODING );
                            } catch ( UnsupportedEncodingException e ) {
                            }
                        }
                        if ( maxPostSize >= 0 ) {
                            if ( charset == null ) {
                                postSize += name.getBytes().length;
                            } else {
                                postSize += name.getBytes ( charset ).length;
                            }
                            if ( value != null ) {
                                postSize++;
                                postSize += part.getSize();
                            }
                            postSize++;
                            if ( postSize > maxPostSize ) {
                                parameters.setParseFailedReason ( FailReason.POST_TOO_LARGE );
                                throw new IllegalStateException ( sm.getString (
                                                                      "coyoteRequest.maxPostSizeExceeded" ) );
                            }
                        }
                        parameters.addParameter ( name, value );
                    }
                }
                success = true;
            } catch ( InvalidContentTypeException e ) {
                parameters.setParseFailedReason ( FailReason.INVALID_CONTENT_TYPE );
                partsParseException = new ServletException ( e );
            } catch ( FileUploadBase.SizeException e ) {
                parameters.setParseFailedReason ( FailReason.POST_TOO_LARGE );
                checkSwallowInput();
                partsParseException = new IllegalStateException ( e );
            } catch ( FileUploadException e ) {
                parameters.setParseFailedReason ( FailReason.IO_ERROR );
                partsParseException = new IOException ( e );
            } catch ( IllegalStateException e ) {
                checkSwallowInput();
                partsParseException = e;
            }
        } finally {
            if ( partsParseException != null || !success ) {
                parameters.setParseFailedReason ( FailReason.UNKNOWN );
            }
        }
    }
    @Override
    public Part getPart ( String name ) throws IOException, IllegalStateException,
        ServletException {
        Collection<Part> c = getParts();
        Iterator<Part> iterator = c.iterator();
        while ( iterator.hasNext() ) {
            Part part = iterator.next();
            if ( name.equals ( part.getName() ) ) {
                return part;
            }
        }
        return null;
    }
    protected Session doGetSession ( boolean create ) {
        Context context = getContext();
        if ( context == null ) {
            return ( null );
        }
        if ( ( session != null ) && !session.isValid() ) {
            session = null;
        }
        if ( session != null ) {
            return ( session );
        }
        Manager manager = context.getManager();
        if ( manager == null ) {
            return ( null );
        }
        if ( requestedSessionId != null ) {
            try {
                session = manager.findSession ( requestedSessionId );
            } catch ( IOException e ) {
                session = null;
            }
            if ( ( session != null ) && !session.isValid() ) {
                session = null;
            }
            if ( session != null ) {
                session.access();
                return ( session );
            }
        }
        if ( !create ) {
            return ( null );
        }
        if ( response != null
                && context.getServletContext()
                .getEffectiveSessionTrackingModes()
                .contains ( SessionTrackingMode.COOKIE )
                && response.getResponse().isCommitted() ) {
            throw new IllegalStateException (
                sm.getString ( "coyoteRequest.sessionCreateCommitted" ) );
        }
        String sessionId = getRequestedSessionId();
        if ( requestedSessionSSL ) {
        } else if ( ( "/".equals ( context.getSessionCookiePath() )
                      && isRequestedSessionIdFromCookie() ) ) {
            if ( context.getValidateClientProvidedNewSessionId() ) {
                boolean found = false;
                for ( Container container : getHost().findChildren() ) {
                    Manager m = ( ( Context ) container ).getManager();
                    if ( m != null ) {
                        try {
                            if ( m.findSession ( sessionId ) != null ) {
                                found = true;
                                break;
                            }
                        } catch ( IOException e ) {
                        }
                    }
                }
                if ( !found ) {
                    sessionId = null;
                }
            }
        } else {
            sessionId = null;
        }
        session = manager.createSession ( sessionId );
        if ( session != null
                && context.getServletContext()
                .getEffectiveSessionTrackingModes()
                .contains ( SessionTrackingMode.COOKIE ) ) {
            Cookie cookie =
                ApplicationSessionCookieConfig.createSessionCookie (
                    context, session.getIdInternal(), isSecure() );
            response.addSessionCookieInternal ( cookie );
        }
        if ( session == null ) {
            return null;
        }
        session.access();
        return session;
    }
    protected String unescape ( String s ) {
        if ( s == null ) {
            return null;
        }
        if ( s.indexOf ( '\\' ) == -1 ) {
            return s;
        }
        StringBuilder buf = new StringBuilder();
        for ( int i = 0; i < s.length(); i++ ) {
            char c = s.charAt ( i );
            if ( c != '\\' ) {
                buf.append ( c );
            } else {
                if ( ++i >= s.length() ) {
                    throw new IllegalArgumentException();
                }
                c = s.charAt ( i );
                buf.append ( c );
            }
        }
        return buf.toString();
    }
    protected void parseCookies() {
        if ( cookiesParsed ) {
            return;
        }
        cookiesParsed = true;
        ServerCookies serverCookies = coyoteRequest.getCookies();
        serverCookies.setLimit ( connector.getMaxCookieCount() );
        CookieProcessor cookieProcessor = getContext().getCookieProcessor();
        cookieProcessor.parseCookieHeader ( coyoteRequest.getMimeHeaders(), serverCookies );
    }
    protected void convertCookies() {
        if ( cookiesConverted ) {
            return;
        }
        cookiesConverted = true;
        if ( getContext() == null ) {
            return;
        }
        parseCookies();
        ServerCookies serverCookies = coyoteRequest.getCookies();
        CookieProcessor cookieProcessor = getContext().getCookieProcessor();
        int count = serverCookies.getCookieCount();
        if ( count <= 0 ) {
            return;
        }
        cookies = new Cookie[count];
        int idx = 0;
        for ( int i = 0; i < count; i++ ) {
            ServerCookie scookie = serverCookies.getCookie ( i );
            try {
                Cookie cookie = new Cookie ( scookie.getName().toString(), null );
                int version = scookie.getVersion();
                cookie.setVersion ( version );
                scookie.getValue().getByteChunk().setCharset ( cookieProcessor.getCharset() );
                cookie.setValue ( unescape ( scookie.getValue().toString() ) );
                cookie.setPath ( unescape ( scookie.getPath().toString() ) );
                String domain = scookie.getDomain().toString();
                if ( domain != null ) {
                    cookie.setDomain ( unescape ( domain ) );
                }
                String comment = scookie.getComment().toString();
                cookie.setComment ( version == 1 ? unescape ( comment ) : null );
                cookies[idx++] = cookie;
            } catch ( IllegalArgumentException e ) {
            }
        }
        if ( idx < count ) {
            Cookie [] ncookies = new Cookie[idx];
            System.arraycopy ( cookies, 0, ncookies, 0, idx );
            cookies = ncookies;
        }
    }
    protected void parseParameters() {
        parametersParsed = true;
        Parameters parameters = coyoteRequest.getParameters();
        boolean success = false;
        try {
            parameters.setLimit ( getConnector().getMaxParameterCount() );
            String enc = getCharacterEncoding();
            boolean useBodyEncodingForURI = connector.getUseBodyEncodingForURI();
            if ( enc != null ) {
                parameters.setEncoding ( enc );
                if ( useBodyEncodingForURI ) {
                    parameters.setQueryStringEncoding ( enc );
                }
            } else {
                parameters.setEncoding
                ( org.apache.coyote.Constants.DEFAULT_CHARACTER_ENCODING );
                if ( useBodyEncodingForURI ) {
                    parameters.setQueryStringEncoding
                    ( org.apache.coyote.Constants.DEFAULT_CHARACTER_ENCODING );
                }
            }
            parameters.handleQueryParameters();
            if ( usingInputStream || usingReader ) {
                success = true;
                return;
            }
            if ( !getConnector().isParseBodyMethod ( getMethod() ) ) {
                success = true;
                return;
            }
            String contentType = getContentType();
            if ( contentType == null ) {
                contentType = "";
            }
            int semicolon = contentType.indexOf ( ';' );
            if ( semicolon >= 0 ) {
                contentType = contentType.substring ( 0, semicolon ).trim();
            } else {
                contentType = contentType.trim();
            }
            if ( "multipart/form-data".equals ( contentType ) ) {
                parseParts ( false );
                success = true;
                return;
            }
            if ( ! ( "application/x-www-form-urlencoded".equals ( contentType ) ) ) {
                success = true;
                return;
            }
            int len = getContentLength();
            if ( len > 0 ) {
                int maxPostSize = connector.getMaxPostSize();
                if ( ( maxPostSize >= 0 ) && ( len > maxPostSize ) ) {
                    Context context = getContext();
                    if ( context != null && context.getLogger().isDebugEnabled() ) {
                        context.getLogger().debug (
                            sm.getString ( "coyoteRequest.postTooLarge" ) );
                    }
                    checkSwallowInput();
                    parameters.setParseFailedReason ( FailReason.POST_TOO_LARGE );
                    return;
                }
                byte[] formData = null;
                if ( len < CACHED_POST_LEN ) {
                    if ( postData == null ) {
                        postData = new byte[CACHED_POST_LEN];
                    }
                    formData = postData;
                } else {
                    formData = new byte[len];
                }
                try {
                    if ( readPostBody ( formData, len ) != len ) {
                        parameters.setParseFailedReason ( FailReason.REQUEST_BODY_INCOMPLETE );
                        return;
                    }
                } catch ( IOException e ) {
                    Context context = getContext();
                    if ( context != null && context.getLogger().isDebugEnabled() ) {
                        context.getLogger().debug (
                            sm.getString ( "coyoteRequest.parseParameters" ),
                            e );
                    }
                    parameters.setParseFailedReason ( FailReason.CLIENT_DISCONNECT );
                    return;
                }
                parameters.processParameters ( formData, 0, len );
            } else if ( "chunked".equalsIgnoreCase (
                            coyoteRequest.getHeader ( "transfer-encoding" ) ) ) {
                byte[] formData = null;
                try {
                    formData = readChunkedPostBody();
                } catch ( IllegalStateException ise ) {
                    parameters.setParseFailedReason ( FailReason.POST_TOO_LARGE );
                    Context context = getContext();
                    if ( context != null && context.getLogger().isDebugEnabled() ) {
                        context.getLogger().debug (
                            sm.getString ( "coyoteRequest.parseParameters" ),
                            ise );
                    }
                    return;
                } catch ( IOException e ) {
                    parameters.setParseFailedReason ( FailReason.CLIENT_DISCONNECT );
                    Context context = getContext();
                    if ( context != null && context.getLogger().isDebugEnabled() ) {
                        context.getLogger().debug (
                            sm.getString ( "coyoteRequest.parseParameters" ),
                            e );
                    }
                    return;
                }
                if ( formData != null ) {
                    parameters.processParameters ( formData, 0, formData.length );
                }
            }
            success = true;
        } finally {
            if ( !success ) {
                parameters.setParseFailedReason ( FailReason.UNKNOWN );
            }
        }
    }
    protected int readPostBody ( byte[] body, int len )
    throws IOException {
        int offset = 0;
        do {
            int inputLen = getStream().read ( body, offset, len - offset );
            if ( inputLen <= 0 ) {
                return offset;
            }
            offset += inputLen;
        } while ( ( len - offset ) > 0 );
        return len;
    }
    protected byte[] readChunkedPostBody() throws IOException {
        ByteChunk body = new ByteChunk();
        byte[] buffer = new byte[CACHED_POST_LEN];
        int len = 0;
        while ( len > -1 ) {
            len = getStream().read ( buffer, 0, CACHED_POST_LEN );
            if ( connector.getMaxPostSize() >= 0 &&
                    ( body.getLength() + len ) > connector.getMaxPostSize() ) {
                checkSwallowInput();
                throw new IllegalStateException (
                    sm.getString ( "coyoteRequest.chunkedPostTooLarge" ) );
            }
            if ( len > 0 ) {
                body.append ( buffer, 0, len );
            }
        }
        if ( body.getLength() == 0 ) {
            return null;
        }
        if ( body.getLength() < body.getBuffer().length ) {
            int length = body.getLength();
            byte[] result = new byte[length];
            System.arraycopy ( body.getBuffer(), 0, result, 0, length );
            return result;
        }
        return body.getBuffer();
    }
    protected void parseLocales() {
        localesParsed = true;
        TreeMap<Double, ArrayList<Locale>> locales = new TreeMap<>();
        Enumeration<String> values = getHeaders ( "accept-language" );
        while ( values.hasMoreElements() ) {
            String value = values.nextElement();
            parseLocalesHeader ( value, locales );
        }
        for ( ArrayList<Locale> list : locales.values() ) {
            for ( Locale locale : list ) {
                addLocale ( locale );
            }
        }
    }
    protected void parseLocalesHeader ( String value, TreeMap<Double, ArrayList<Locale>> locales ) {
        List<AcceptLanguage> acceptLanguages;
        try {
            acceptLanguages = AcceptLanguage.parse ( new StringReader ( value ) );
        } catch ( IOException e ) {
            return;
        }
        for ( AcceptLanguage acceptLanguage : acceptLanguages ) {
            Double key = Double.valueOf ( -acceptLanguage.getQuality() );
            ArrayList<Locale> values = locales.get ( key );
            if ( values == null ) {
                values = new ArrayList<>();
                locales.put ( key, values );
            }
            values.add ( acceptLanguage.getLocale() );
        }
    }
    private static interface SpecialAttributeAdapter {
        Object get ( Request request, String name );
        void set ( Request request, String name, Object value );
    }
    private static final Map<String, SpecialAttributeAdapter> specialAttributes
        = new HashMap<>();
    static {
        specialAttributes.put ( Globals.DISPATCHER_TYPE_ATTR,
        new SpecialAttributeAdapter() {
            @Override
            public Object get ( Request request, String name ) {
                return ( request.internalDispatcherType == null ) ? DispatcherType.REQUEST
                       : request.internalDispatcherType;
            }
            @Override
            public void set ( Request request, String name, Object value ) {
                request.internalDispatcherType = ( DispatcherType ) value;
            }
        } );
        specialAttributes.put ( Globals.DISPATCHER_REQUEST_PATH_ATTR,
        new SpecialAttributeAdapter() {
            @Override
            public Object get ( Request request, String name ) {
                return ( request.requestDispatcherPath == null ) ? request
                       .getRequestPathMB().toString()
                       : request.requestDispatcherPath.toString();
            }
            @Override
            public void set ( Request request, String name, Object value ) {
                request.requestDispatcherPath = value;
            }
        } );
        specialAttributes.put ( Globals.ASYNC_SUPPORTED_ATTR,
        new SpecialAttributeAdapter() {
            @Override
            public Object get ( Request request, String name ) {
                return request.asyncSupported;
            }
            @Override
            public void set ( Request request, String name, Object value ) {
                Boolean oldValue = request.asyncSupported;
                request.asyncSupported = ( Boolean ) value;
                request.notifyAttributeAssigned ( name, value, oldValue );
            }
        } );
        specialAttributes.put ( Globals.GSS_CREDENTIAL_ATTR,
        new SpecialAttributeAdapter() {
            @Override
            public Object get ( Request request, String name ) {
                if ( request.userPrincipal instanceof TomcatPrincipal ) {
                    return ( ( TomcatPrincipal ) request.userPrincipal )
                           .getGssCredential();
                }
                return null;
            }
            @Override
            public void set ( Request request, String name, Object value ) {
            }
        } );
        specialAttributes.put ( Globals.PARAMETER_PARSE_FAILED_ATTR,
        new SpecialAttributeAdapter() {
            @Override
            public Object get ( Request request, String name ) {
                if ( request.getCoyoteRequest().getParameters()
                        .isParseFailed() ) {
                    return Boolean.TRUE;
                }
                return null;
            }
            @Override
            public void set ( Request request, String name, Object value ) {
            }
        } );
        specialAttributes.put ( Globals.PARAMETER_PARSE_FAILED_REASON_ATTR,
        new SpecialAttributeAdapter() {
            @Override
            public Object get ( Request request, String name ) {
                return request.getCoyoteRequest().getParameters().getParseFailedReason();
            }
            @Override
            public void set ( Request request, String name, Object value ) {
            }
        } );
        specialAttributes.put ( Globals.SENDFILE_SUPPORTED_ATTR,
        new SpecialAttributeAdapter() {
            @Override
            public Object get ( Request request, String name ) {
                return Boolean.valueOf (
                           request.getConnector().getProtocolHandler (
                           ).isSendfileSupported() && request.getCoyoteRequest().getSendfile() );
            }
            @Override
            public void set ( Request request, String name, Object value ) {
            }
        } );
        for ( SimpleDateFormat sdf : formatsTemplate ) {
            sdf.setTimeZone ( GMT_ZONE );
        }
    }
}
