package org.apache.catalina.connector;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.http.parser.AcceptLanguage;
import java.io.StringReader;
import java.util.TreeMap;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.http.ServerCookie;
import org.apache.tomcat.util.http.CookieProcessor;
import org.apache.catalina.Container;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.apache.catalina.core.ApplicationPart;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.RequestContext;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;
import org.apache.tomcat.util.http.fileupload.FileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.Parameters;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.core.ApplicationSessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.apache.catalina.TomcatPrincipal;
import org.apache.catalina.Realm;
import org.apache.catalina.Manager;
import javax.servlet.http.Mapping;
import java.text.DateFormat;
import org.apache.tomcat.util.http.FastHttpDateFormat;
import org.apache.tomcat.util.http.ServerCookies;
import org.apache.tomcat.util.http.RequestUtil;
import org.apache.tomcat.util.buf.UDecoder;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.ContextBind;
import org.apache.coyote.UpgradeToken;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.ServletException;
import org.apache.coyote.http11.upgrade.InternalHttpUpgradeHandler;
import javax.servlet.http.HttpUpgradeHandler;
import org.apache.catalina.core.ApplicationPushBuilder;
import javax.servlet.http.PushBuilder;
import javax.servlet.http.HttpSession;
import org.apache.tomcat.util.buf.MessageBytes;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.ServletResponse;
import javax.servlet.AsyncContext;
import java.io.UnsupportedEncodingException;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestAttributeEvent;
import java.io.File;
import org.apache.catalina.util.URLEncoder;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;
import java.util.Enumeration;
import org.apache.coyote.ActionCode;
import javax.servlet.ServletInputStream;
import org.apache.catalina.Wrapper;
import java.io.InputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.catalina.Host;
import org.apache.catalina.Context;
import org.apache.tomcat.util.ExceptionUtils;
import java.util.Iterator;
import org.apache.catalina.Globals;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.tomcat.util.buf.B2CConverter;
import org.apache.catalina.core.ApplicationMapping;
import org.apache.catalina.mapper.MappingData;
import javax.servlet.FilterChain;
import org.apache.catalina.core.AsyncContextImpl;
import org.apache.catalina.Session;
import javax.servlet.http.Part;
import java.util.Collection;
import org.apache.catalina.util.ParameterMap;
import javax.security.auth.Subject;
import java.security.Principal;
import javax.servlet.DispatcherType;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Locale;
import java.text.SimpleDateFormat;
import javax.servlet.http.Cookie;
import org.apache.tomcat.util.res.StringManager;
import java.util.TimeZone;
import org.apache.coyote.Request;
import org.apache.juli.logging.Log;
import javax.servlet.http.HttpServletRequest;
public class Request implements HttpServletRequest {
    private static final Log log;
    protected org.apache.coyote.Request coyoteRequest;
    protected static final TimeZone GMT_ZONE;
    protected static final StringManager sm;
    protected Cookie[] cookies;
    protected final SimpleDateFormat[] formats;
    private static final SimpleDateFormat[] formatsTemplate;
    protected static final Locale defaultLocale;
    private final Map<String, Object> attributes;
    protected boolean sslAttributesParsed;
    protected final ArrayList<Locale> locales;
    private final transient HashMap<String, Object> notes;
    protected String authType;
    protected DispatcherType internalDispatcherType;
    protected final InputBuffer inputBuffer;
    protected CoyoteInputStream inputStream;
    protected CoyoteReader reader;
    protected boolean usingInputStream;
    protected boolean usingReader;
    protected Principal userPrincipal;
    protected boolean parametersParsed;
    protected boolean cookiesParsed;
    protected boolean cookiesConverted;
    protected boolean secure;
    protected transient Subject subject;
    protected static final int CACHED_POST_LEN = 8192;
    protected byte[] postData;
    protected ParameterMap<String, String[]> parameterMap;
    protected Collection<Part> parts;
    protected Exception partsParseException;
    protected Session session;
    protected Object requestDispatcherPath;
    protected boolean requestedSessionCookie;
    protected String requestedSessionId;
    protected boolean requestedSessionURL;
    protected boolean requestedSessionSSL;
    protected boolean localesParsed;
    protected int localPort;
    protected String remoteAddr;
    protected String remoteHost;
    protected int remotePort;
    protected String localAddr;
    protected String localName;
    private volatile AsyncContextImpl asyncContext;
    protected Boolean asyncSupported;
    private HttpServletRequest applicationRequest;
    protected Connector connector;
    protected FilterChain filterChain;
    protected final MappingData mappingData;
    private final ApplicationMapping applicationMapping;
    protected RequestFacade facade;
    protected Response response;
    protected B2CConverter URIConverter;
    private static final Map<String, SpecialAttributeAdapter> specialAttributes;
    public Request() {
        this.cookies = null;
        this.attributes = new ConcurrentHashMap<String, Object>();
        this.sslAttributesParsed = false;
        this.locales = new ArrayList<Locale>();
        this.notes = new HashMap<String, Object>();
        this.authType = null;
        this.internalDispatcherType = null;
        this.inputBuffer = new InputBuffer();
        this.inputStream = new CoyoteInputStream ( this.inputBuffer );
        this.reader = new CoyoteReader ( this.inputBuffer );
        this.usingInputStream = false;
        this.usingReader = false;
        this.userPrincipal = null;
        this.parametersParsed = false;
        this.cookiesParsed = false;
        this.cookiesConverted = false;
        this.secure = false;
        this.subject = null;
        this.postData = null;
        this.parameterMap = new ParameterMap<String, String[]>();
        this.parts = null;
        this.partsParseException = null;
        this.session = null;
        this.requestDispatcherPath = null;
        this.requestedSessionCookie = false;
        this.requestedSessionId = null;
        this.requestedSessionURL = false;
        this.requestedSessionSSL = false;
        this.localesParsed = false;
        this.localPort = -1;
        this.remoteAddr = null;
        this.remoteHost = null;
        this.remotePort = -1;
        this.localAddr = null;
        this.localName = null;
        this.asyncContext = null;
        this.asyncSupported = null;
        this.applicationRequest = null;
        this.filterChain = null;
        this.mappingData = new MappingData();
        this.applicationMapping = new ApplicationMapping ( this.mappingData );
        this.facade = null;
        this.response = null;
        this.URIConverter = null;
        this.formats = new SimpleDateFormat[Request.formatsTemplate.length];
        for ( int i = 0; i < this.formats.length; ++i ) {
            this.formats[i] = ( SimpleDateFormat ) Request.formatsTemplate[i].clone();
        }
    }
    public void setCoyoteRequest ( final org.apache.coyote.Request coyoteRequest ) {
        this.coyoteRequest = coyoteRequest;
        this.inputBuffer.setRequest ( coyoteRequest );
    }
    public org.apache.coyote.Request getCoyoteRequest() {
        return this.coyoteRequest;
    }
    protected void addPathParameter ( final String name, final String value ) {
        this.coyoteRequest.addPathParameter ( name, value );
    }
    protected String getPathParameter ( final String name ) {
        return this.coyoteRequest.getPathParameter ( name );
    }
    public void setAsyncSupported ( final boolean asyncSupported ) {
        this.asyncSupported = asyncSupported;
    }
    public void recycle() {
        this.internalDispatcherType = null;
        this.requestDispatcherPath = null;
        this.authType = null;
        this.inputBuffer.recycle();
        this.usingInputStream = false;
        this.usingReader = false;
        this.userPrincipal = null;
        this.subject = null;
        this.parametersParsed = false;
        if ( this.parts != null ) {
            for ( final Part part : this.parts ) {
                try {
                    part.delete();
                } catch ( IOException ex ) {}
            }
            this.parts = null;
        }
        this.partsParseException = null;
        this.locales.clear();
        this.localesParsed = false;
        this.secure = false;
        this.remoteAddr = null;
        this.remoteHost = null;
        this.remotePort = -1;
        this.localPort = -1;
        this.localAddr = null;
        this.localName = null;
        this.attributes.clear();
        this.sslAttributesParsed = false;
        this.notes.clear();
        this.recycleSessionInfo();
        this.recycleCookieInfo ( false );
        if ( Globals.IS_SECURITY_ENABLED || Connector.RECYCLE_FACADES ) {
            this.parameterMap = new ParameterMap<String, String[]>();
        } else {
            this.parameterMap.setLocked ( false );
            this.parameterMap.clear();
        }
        this.mappingData.recycle();
        this.applicationMapping.recycle();
        this.applicationRequest = null;
        if ( Globals.IS_SECURITY_ENABLED || Connector.RECYCLE_FACADES ) {
            if ( this.facade != null ) {
                this.facade.clear();
                this.facade = null;
            }
            if ( this.inputStream != null ) {
                this.inputStream.clear();
                this.inputStream = null;
            }
            if ( this.reader != null ) {
                this.reader.clear();
                this.reader = null;
            }
        }
        this.asyncSupported = null;
        if ( this.asyncContext != null ) {
            this.asyncContext.recycle();
        }
        this.asyncContext = null;
    }
    protected void recycleSessionInfo() {
        if ( this.session != null ) {
            try {
                this.session.endAccess();
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                Request.log.warn ( Request.sm.getString ( "coyoteRequest.sessionEndAccessFail" ), t );
            }
        }
        this.session = null;
        this.requestedSessionCookie = false;
        this.requestedSessionId = null;
        this.requestedSessionURL = false;
        this.requestedSessionSSL = false;
    }
    protected void recycleCookieInfo ( final boolean recycleCoyote ) {
        this.cookiesParsed = false;
        this.cookiesConverted = false;
        this.cookies = null;
        if ( recycleCoyote ) {
            this.getCoyoteRequest().getCookies().recycle();
        }
    }
    public Connector getConnector() {
        return this.connector;
    }
    public void setConnector ( final Connector connector ) {
        this.connector = connector;
    }
    public Context getContext() {
        return this.mappingData.context;
    }
    @Deprecated
    public void setContext ( final Context context ) {
        this.mappingData.context = context;
    }
    public FilterChain getFilterChain() {
        return this.filterChain;
    }
    public void setFilterChain ( final FilterChain filterChain ) {
        this.filterChain = filterChain;
    }
    public Host getHost() {
        return this.mappingData.host;
    }
    public MappingData getMappingData() {
        return this.mappingData;
    }
    public HttpServletRequest getRequest() {
        if ( this.facade == null ) {
            this.facade = new RequestFacade ( this );
        }
        if ( this.applicationRequest == null ) {
            this.applicationRequest = ( HttpServletRequest ) this.facade;
        }
        return this.applicationRequest;
    }
    public void setRequest ( final HttpServletRequest applicationRequest ) {
        ServletRequest r;
        for ( r = ( ServletRequest ) applicationRequest; r instanceof HttpServletRequestWrapper; r = ( ( HttpServletRequestWrapper ) r ).getRequest() ) {}
        if ( r != this.facade ) {
            throw new IllegalArgumentException ( Request.sm.getString ( "request.illegalWrap" ) );
        }
        this.applicationRequest = applicationRequest;
    }
    public Response getResponse() {
        return this.response;
    }
    public void setResponse ( final Response response ) {
        this.response = response;
    }
    public InputStream getStream() {
        if ( this.inputStream == null ) {
            this.inputStream = new CoyoteInputStream ( this.inputBuffer );
        }
        return ( InputStream ) this.inputStream;
    }
    protected B2CConverter getURIConverter() {
        return this.URIConverter;
    }
    protected void setURIConverter ( final B2CConverter URIConverter ) {
        this.URIConverter = URIConverter;
    }
    public Wrapper getWrapper() {
        return this.mappingData.wrapper;
    }
    @Deprecated
    public void setWrapper ( final Wrapper wrapper ) {
        this.mappingData.wrapper = wrapper;
    }
    public ServletInputStream createInputStream() throws IOException {
        if ( this.inputStream == null ) {
            this.inputStream = new CoyoteInputStream ( this.inputBuffer );
        }
        return this.inputStream;
    }
    public void finishRequest() throws IOException {
        if ( this.response.getStatus() == 413 ) {
            this.checkSwallowInput();
        }
    }
    public Object getNote ( final String name ) {
        return this.notes.get ( name );
    }
    public void removeNote ( final String name ) {
        this.notes.remove ( name );
    }
    public void setLocalPort ( final int port ) {
        this.localPort = port;
    }
    public void setNote ( final String name, final Object value ) {
        this.notes.put ( name, value );
    }
    public void setRemoteAddr ( final String remoteAddr ) {
        this.remoteAddr = remoteAddr;
    }
    public void setRemoteHost ( final String remoteHost ) {
        this.remoteHost = remoteHost;
    }
    public void setSecure ( final boolean secure ) {
        this.secure = secure;
    }
    public void setServerPort ( final int port ) {
        this.coyoteRequest.setServerPort ( port );
    }
    public Object getAttribute ( final String name ) {
        final SpecialAttributeAdapter adapter = Request.specialAttributes.get ( name );
        if ( adapter != null ) {
            return adapter.get ( this, name );
        }
        Object attr = this.attributes.get ( name );
        if ( attr != null ) {
            return attr;
        }
        attr = this.coyoteRequest.getAttribute ( name );
        if ( attr != null ) {
            return attr;
        }
        if ( isSSLAttribute ( name ) || name.equals ( "org.apache.tomcat.util.net.secure_protocol_version" ) ) {
            this.coyoteRequest.action ( ActionCode.REQ_SSL_ATTRIBUTE, this.coyoteRequest );
            attr = this.coyoteRequest.getAttribute ( "javax.servlet.request.X509Certificate" );
            if ( attr != null ) {
                this.attributes.put ( "javax.servlet.request.X509Certificate", attr );
            }
            attr = this.coyoteRequest.getAttribute ( "javax.servlet.request.cipher_suite" );
            if ( attr != null ) {
                this.attributes.put ( "javax.servlet.request.cipher_suite", attr );
            }
            attr = this.coyoteRequest.getAttribute ( "javax.servlet.request.key_size" );
            if ( attr != null ) {
                this.attributes.put ( "javax.servlet.request.key_size", attr );
            }
            attr = this.coyoteRequest.getAttribute ( "javax.servlet.request.ssl_session_id" );
            if ( attr != null ) {
                this.attributes.put ( "javax.servlet.request.ssl_session_id", attr );
            }
            attr = this.coyoteRequest.getAttribute ( "javax.servlet.request.ssl_session_mgr" );
            if ( attr != null ) {
                this.attributes.put ( "javax.servlet.request.ssl_session_mgr", attr );
            }
            attr = this.coyoteRequest.getAttribute ( "org.apache.tomcat.util.net.secure_protocol_version" );
            if ( attr != null ) {
                this.attributes.put ( "org.apache.tomcat.util.net.secure_protocol_version", attr );
            }
            attr = this.attributes.get ( name );
            this.sslAttributesParsed = true;
        }
        return attr;
    }
    public long getContentLengthLong() {
        return this.coyoteRequest.getContentLengthLong();
    }
    static boolean isSSLAttribute ( final String name ) {
        return "javax.servlet.request.X509Certificate".equals ( name ) || "javax.servlet.request.cipher_suite".equals ( name ) || "javax.servlet.request.key_size".equals ( name ) || "javax.servlet.request.ssl_session_id".equals ( name ) || "javax.servlet.request.ssl_session_mgr".equals ( name );
    }
    public Enumeration<String> getAttributeNames() {
        if ( this.isSecure() && !this.sslAttributesParsed ) {
            this.getAttribute ( "javax.servlet.request.X509Certificate" );
        }
        final Set<String> names = new HashSet<String>();
        names.addAll ( this.attributes.keySet() );
        return Collections.enumeration ( names );
    }
    public String getCharacterEncoding() {
        return this.coyoteRequest.getCharacterEncoding();
    }
    public int getContentLength() {
        return this.coyoteRequest.getContentLength();
    }
    public String getContentType() {
        return this.coyoteRequest.getContentType();
    }
    public void setContentType ( final String contentType ) {
        this.coyoteRequest.setContentType ( contentType );
    }
    public ServletInputStream getInputStream() throws IOException {
        if ( this.usingReader ) {
            throw new IllegalStateException ( Request.sm.getString ( "coyoteRequest.getInputStream.ise" ) );
        }
        this.usingInputStream = true;
        if ( this.inputStream == null ) {
            this.inputStream = new CoyoteInputStream ( this.inputBuffer );
        }
        return this.inputStream;
    }
    public Locale getLocale() {
        if ( !this.localesParsed ) {
            this.parseLocales();
        }
        if ( this.locales.size() > 0 ) {
            return this.locales.get ( 0 );
        }
        return Request.defaultLocale;
    }
    public Enumeration<Locale> getLocales() {
        if ( !this.localesParsed ) {
            this.parseLocales();
        }
        if ( this.locales.size() > 0 ) {
            return Collections.enumeration ( this.locales );
        }
        final ArrayList<Locale> results = new ArrayList<Locale>();
        results.add ( Request.defaultLocale );
        return Collections.enumeration ( results );
    }
    public String getParameter ( final String name ) {
        if ( !this.parametersParsed ) {
            this.parseParameters();
        }
        return this.coyoteRequest.getParameters().getParameter ( name );
    }
    public Map<String, String[]> getParameterMap() {
        if ( this.parameterMap.isLocked() ) {
            return this.parameterMap;
        }
        final Enumeration<String> enumeration = this.getParameterNames();
        while ( enumeration.hasMoreElements() ) {
            final String name = enumeration.nextElement();
            final String[] values = this.getParameterValues ( name );
            this.parameterMap.put ( name, values );
        }
        this.parameterMap.setLocked ( true );
        return this.parameterMap;
    }
    public Enumeration<String> getParameterNames() {
        if ( !this.parametersParsed ) {
            this.parseParameters();
        }
        return this.coyoteRequest.getParameters().getParameterNames();
    }
    public String[] getParameterValues ( final String name ) {
        if ( !this.parametersParsed ) {
            this.parseParameters();
        }
        return this.coyoteRequest.getParameters().getParameterValues ( name );
    }
    public String getProtocol() {
        return this.coyoteRequest.protocol().toString();
    }
    public BufferedReader getReader() throws IOException {
        if ( this.usingInputStream ) {
            throw new IllegalStateException ( Request.sm.getString ( "coyoteRequest.getReader.ise" ) );
        }
        this.usingReader = true;
        this.inputBuffer.checkConverter();
        if ( this.reader == null ) {
            this.reader = new CoyoteReader ( this.inputBuffer );
        }
        return this.reader;
    }
    @Deprecated
    public String getRealPath ( final String path ) {
        final Context context = this.getContext();
        if ( context == null ) {
            return null;
        }
        final ServletContext servletContext = context.getServletContext();
        if ( servletContext == null ) {
            return null;
        }
        try {
            return servletContext.getRealPath ( path );
        } catch ( IllegalArgumentException e ) {
            return null;
        }
    }
    public String getRemoteAddr() {
        if ( this.remoteAddr == null ) {
            this.coyoteRequest.action ( ActionCode.REQ_HOST_ADDR_ATTRIBUTE, this.coyoteRequest );
            this.remoteAddr = this.coyoteRequest.remoteAddr().toString();
        }
        return this.remoteAddr;
    }
    public String getRemoteHost() {
        if ( this.remoteHost == null ) {
            if ( !this.connector.getEnableLookups() ) {
                this.remoteHost = this.getRemoteAddr();
            } else {
                this.coyoteRequest.action ( ActionCode.REQ_HOST_ATTRIBUTE, this.coyoteRequest );
                this.remoteHost = this.coyoteRequest.remoteHost().toString();
            }
        }
        return this.remoteHost;
    }
    public int getRemotePort() {
        if ( this.remotePort == -1 ) {
            this.coyoteRequest.action ( ActionCode.REQ_REMOTEPORT_ATTRIBUTE, this.coyoteRequest );
            this.remotePort = this.coyoteRequest.getRemotePort();
        }
        return this.remotePort;
    }
    public String getLocalName() {
        if ( this.localName == null ) {
            this.coyoteRequest.action ( ActionCode.REQ_LOCAL_NAME_ATTRIBUTE, this.coyoteRequest );
            this.localName = this.coyoteRequest.localName().toString();
        }
        return this.localName;
    }
    public String getLocalAddr() {
        if ( this.localAddr == null ) {
            this.coyoteRequest.action ( ActionCode.REQ_LOCAL_ADDR_ATTRIBUTE, this.coyoteRequest );
            this.localAddr = this.coyoteRequest.localAddr().toString();
        }
        return this.localAddr;
    }
    public int getLocalPort() {
        if ( this.localPort == -1 ) {
            this.coyoteRequest.action ( ActionCode.REQ_LOCALPORT_ATTRIBUTE, this.coyoteRequest );
            this.localPort = this.coyoteRequest.getLocalPort();
        }
        return this.localPort;
    }
    public RequestDispatcher getRequestDispatcher ( final String path ) {
        final Context context = this.getContext();
        if ( context == null ) {
            return null;
        }
        if ( path == null ) {
            return null;
        }
        if ( path.startsWith ( "/" ) ) {
            return context.getServletContext().getRequestDispatcher ( path );
        }
        String servletPath = ( String ) this.getAttribute ( "javax.servlet.include.servlet_path" );
        if ( servletPath == null ) {
            servletPath = this.getServletPath();
        }
        final String pathInfo = this.getPathInfo();
        String requestPath = null;
        if ( pathInfo == null ) {
            requestPath = servletPath;
        } else {
            requestPath = servletPath + pathInfo;
        }
        final int pos = requestPath.lastIndexOf ( 47 );
        String relative = null;
        if ( context.getDispatchersUseEncodedPaths() ) {
            if ( pos >= 0 ) {
                relative = URLEncoder.DEFAULT.encode ( requestPath.substring ( 0, pos + 1 ), "UTF-8" ) + path;
            } else {
                relative = URLEncoder.DEFAULT.encode ( requestPath, "UTF-8" ) + path;
            }
        } else if ( pos >= 0 ) {
            relative = requestPath.substring ( 0, pos + 1 ) + path;
        } else {
            relative = requestPath + path;
        }
        return context.getServletContext().getRequestDispatcher ( relative );
    }
    public String getScheme() {
        return this.coyoteRequest.scheme().toString();
    }
    public String getServerName() {
        return this.coyoteRequest.serverName().toString();
    }
    public int getServerPort() {
        return this.coyoteRequest.getServerPort();
    }
    public boolean isSecure() {
        return this.secure;
    }
    public void removeAttribute ( final String name ) {
        if ( name.startsWith ( "org.apache.tomcat." ) ) {
            this.coyoteRequest.getAttributes().remove ( name );
        }
        final boolean found = this.attributes.containsKey ( name );
        if ( found ) {
            final Object value = this.attributes.get ( name );
            this.attributes.remove ( name );
            this.notifyAttributeRemoved ( name, value );
        }
    }
    public void setAttribute ( final String name, Object value ) {
        if ( name == null ) {
            throw new IllegalArgumentException ( Request.sm.getString ( "coyoteRequest.setAttribute.namenull" ) );
        }
        if ( value == null ) {
            this.removeAttribute ( name );
            return;
        }
        final SpecialAttributeAdapter adapter = Request.specialAttributes.get ( name );
        if ( adapter != null ) {
            adapter.set ( this, name, value );
            return;
        }
        if ( Globals.IS_SECURITY_ENABLED && name.equals ( "org.apache.tomcat.sendfile.filename" ) ) {
            String canonicalPath;
            try {
                canonicalPath = new File ( value.toString() ).getCanonicalPath();
            } catch ( IOException e ) {
                throw new SecurityException ( Request.sm.getString ( "coyoteRequest.sendfileNotCanonical", value ), e );
            }
            System.getSecurityManager().checkRead ( canonicalPath );
            value = canonicalPath;
        }
        final Object oldValue = this.attributes.put ( name, value );
        if ( name.startsWith ( "org.apache.tomcat." ) ) {
            this.coyoteRequest.setAttribute ( name, value );
        }
        this.notifyAttributeAssigned ( name, value, oldValue );
    }
    private void notifyAttributeAssigned ( final String name, final Object value, final Object oldValue ) {
        final Context context = this.getContext();
        final Object[] listeners = context.getApplicationEventListeners();
        if ( listeners == null || listeners.length == 0 ) {
            return;
        }
        final boolean replaced = oldValue != null;
        ServletRequestAttributeEvent event = null;
        if ( replaced ) {
            event = new ServletRequestAttributeEvent ( context.getServletContext(), ( ServletRequest ) this.getRequest(), name, oldValue );
        } else {
            event = new ServletRequestAttributeEvent ( context.getServletContext(), ( ServletRequest ) this.getRequest(), name, value );
        }
        for ( int i = 0; i < listeners.length; ++i ) {
            if ( listeners[i] instanceof ServletRequestAttributeListener ) {
                final ServletRequestAttributeListener listener = ( ServletRequestAttributeListener ) listeners[i];
                try {
                    if ( replaced ) {
                        listener.attributeReplaced ( event );
                    } else {
                        listener.attributeAdded ( event );
                    }
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    this.attributes.put ( "javax.servlet.error.exception", t );
                    context.getLogger().error ( Request.sm.getString ( "coyoteRequest.attributeEvent" ), t );
                }
            }
        }
    }
    private void notifyAttributeRemoved ( final String name, final Object value ) {
        final Context context = this.getContext();
        final Object[] listeners = context.getApplicationEventListeners();
        if ( listeners == null || listeners.length == 0 ) {
            return;
        }
        final ServletRequestAttributeEvent event = new ServletRequestAttributeEvent ( context.getServletContext(), ( ServletRequest ) this.getRequest(), name, value );
        for ( int i = 0; i < listeners.length; ++i ) {
            if ( listeners[i] instanceof ServletRequestAttributeListener ) {
                final ServletRequestAttributeListener listener = ( ServletRequestAttributeListener ) listeners[i];
                try {
                    listener.attributeRemoved ( event );
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    this.attributes.put ( "javax.servlet.error.exception", t );
                    context.getLogger().error ( Request.sm.getString ( "coyoteRequest.attributeEvent" ), t );
                }
            }
        }
    }
    public void setCharacterEncoding ( final String enc ) throws UnsupportedEncodingException {
        if ( this.usingReader ) {
            return;
        }
        B2CConverter.getCharset ( enc );
        this.coyoteRequest.setCharacterEncoding ( enc );
    }
    public ServletContext getServletContext() {
        return this.getContext().getServletContext();
    }
    public AsyncContext startAsync() {
        return this.startAsync ( ( ServletRequest ) this.getRequest(), ( ServletResponse ) this.response.getResponse() );
    }
    public AsyncContext startAsync ( final ServletRequest request, final ServletResponse response ) {
        if ( !this.isAsyncSupported() ) {
            throw new IllegalStateException ( Request.sm.getString ( "request.asyncNotSupported" ) );
        }
        if ( this.asyncContext == null ) {
            this.asyncContext = new AsyncContextImpl ( this );
        }
        this.asyncContext.setStarted ( this.getContext(), request, response, request == this.getRequest() && response == this.getResponse().getResponse() );
        this.asyncContext.setTimeout ( this.getConnector().getAsyncTimeout() );
        return ( AsyncContext ) this.asyncContext;
    }
    public boolean isAsyncStarted() {
        return this.asyncContext != null && this.asyncContext.isStarted();
    }
    public boolean isAsyncDispatching() {
        if ( this.asyncContext == null ) {
            return false;
        }
        final AtomicBoolean result = new AtomicBoolean ( false );
        this.coyoteRequest.action ( ActionCode.ASYNC_IS_DISPATCHING, result );
        return result.get();
    }
    public boolean isAsyncCompleting() {
        if ( this.asyncContext == null ) {
            return false;
        }
        final AtomicBoolean result = new AtomicBoolean ( false );
        this.coyoteRequest.action ( ActionCode.ASYNC_IS_COMPLETING, result );
        return result.get();
    }
    public boolean isAsync() {
        if ( this.asyncContext == null ) {
            return false;
        }
        final AtomicBoolean result = new AtomicBoolean ( false );
        this.coyoteRequest.action ( ActionCode.ASYNC_IS_ASYNC, result );
        return result.get();
    }
    public boolean isAsyncSupported() {
        return this.asyncSupported == null || this.asyncSupported;
    }
    public AsyncContext getAsyncContext() {
        if ( !this.isAsyncStarted() ) {
            throw new IllegalStateException ( Request.sm.getString ( "request.notAsync" ) );
        }
        return ( AsyncContext ) this.asyncContext;
    }
    public AsyncContextImpl getAsyncContextInternal() {
        return this.asyncContext;
    }
    public DispatcherType getDispatcherType() {
        if ( this.internalDispatcherType == null ) {
            return DispatcherType.REQUEST;
        }
        return this.internalDispatcherType;
    }
    public void addCookie ( final Cookie cookie ) {
        if ( !this.cookiesConverted ) {
            this.convertCookies();
        }
        int size = 0;
        if ( this.cookies != null ) {
            size = this.cookies.length;
        }
        final Cookie[] newCookies = new Cookie[size + 1];
        for ( int i = 0; i < size; ++i ) {
            newCookies[i] = this.cookies[i];
        }
        newCookies[size] = cookie;
        this.cookies = newCookies;
    }
    public void addLocale ( final Locale locale ) {
        this.locales.add ( locale );
    }
    public void clearCookies() {
        this.cookiesParsed = true;
        this.cookiesConverted = true;
        this.cookies = null;
    }
    public void clearLocales() {
        this.locales.clear();
    }
    public void setAuthType ( final String type ) {
        this.authType = type;
    }
    public void setPathInfo ( final String path ) {
        this.mappingData.pathInfo.setString ( path );
    }
    public void setRequestedSessionCookie ( final boolean flag ) {
        this.requestedSessionCookie = flag;
    }
    public void setRequestedSessionId ( final String id ) {
        this.requestedSessionId = id;
    }
    public void setRequestedSessionURL ( final boolean flag ) {
        this.requestedSessionURL = flag;
    }
    public void setRequestedSessionSSL ( final boolean flag ) {
        this.requestedSessionSSL = flag;
    }
    public String getDecodedRequestURI() {
        return this.coyoteRequest.decodedURI().toString();
    }
    public MessageBytes getDecodedRequestURIMB() {
        return this.coyoteRequest.decodedURI();
    }
    public void setUserPrincipal ( final Principal principal ) {
        if ( Globals.IS_SECURITY_ENABLED ) {
            final HttpSession session = this.getSession ( false );
            if ( this.subject != null && !this.subject.getPrincipals().contains ( principal ) ) {
                this.subject.getPrincipals().add ( principal );
            } else if ( session != null && session.getAttribute ( "javax.security.auth.subject" ) == null ) {
                this.subject = new Subject();
                this.subject.getPrincipals().add ( principal );
            }
            if ( session != null ) {
                session.setAttribute ( "javax.security.auth.subject", ( Object ) this.subject );
            }
        }
        this.userPrincipal = principal;
    }
    public boolean isPushSupported() {
        final AtomicBoolean result = new AtomicBoolean();
        this.coyoteRequest.action ( ActionCode.IS_PUSH_SUPPORTED, result );
        return result.get();
    }
    public PushBuilder getPushBuilder() {
        return ( PushBuilder ) new ApplicationPushBuilder ( ( HttpServletRequest ) this );
    }
    public <T extends HttpUpgradeHandler> T upgrade ( final Class<T> httpUpgradeHandlerClass ) throws IOException, ServletException {
        InstanceManager instanceManager = null;
        T handler;
        try {
            if ( InternalHttpUpgradeHandler.class.isAssignableFrom ( httpUpgradeHandlerClass ) ) {
                handler = httpUpgradeHandlerClass.newInstance();
            } else {
                instanceManager = this.getContext().getInstanceManager();
                handler = ( T ) instanceManager.newInstance ( httpUpgradeHandlerClass );
            }
        } catch ( InstantiationException | IllegalAccessException | InvocationTargetException | NamingException e ) {
            throw new ServletException ( ( Throwable ) e );
        }
        final UpgradeToken upgradeToken = new UpgradeToken ( handler, this.getContext(), instanceManager );
        this.coyoteRequest.action ( ActionCode.UPGRADE, upgradeToken );
        this.response.setStatus ( 101 );
        return handler;
    }
    public String getAuthType() {
        return this.authType;
    }
    public String getContextPath() {
        final String canonicalContextPath = this.getServletContext().getContextPath();
        final String uri = this.getRequestURI();
        final char[] uriChars = uri.toCharArray();
        int lastSlash = this.mappingData.contextSlashCount;
        if ( lastSlash == 0 ) {
            return "";
        }
        int pos = 0;
        while ( lastSlash > 0 ) {
            pos = this.nextSlash ( uriChars, pos + 1 );
            if ( pos == -1 ) {
                break;
            }
            --lastSlash;
        }
        String candidate;
        if ( pos == -1 ) {
            candidate = uri;
        } else {
            candidate = uri.substring ( 0, pos );
        }
        boolean match;
        for ( candidate = this.removePathParameters ( candidate ), candidate = UDecoder.URLDecode ( candidate, this.connector.getURIEncoding() ), candidate = RequestUtil.normalize ( candidate ), match = canonicalContextPath.equals ( candidate ); !match && pos != -1; match = canonicalContextPath.equals ( candidate ) ) {
            pos = this.nextSlash ( uriChars, pos + 1 );
            if ( pos == -1 ) {
                candidate = uri;
            } else {
                candidate = uri.substring ( 0, pos );
            }
            candidate = this.removePathParameters ( candidate );
            candidate = UDecoder.URLDecode ( candidate, this.connector.getURIEncoding() );
            candidate = RequestUtil.normalize ( candidate );
        }
        if ( !match ) {
            throw new IllegalStateException ( Request.sm.getString ( "coyoteRequest.getContextPath.ise", canonicalContextPath, uri ) );
        }
        if ( pos == -1 ) {
            return uri;
        }
        return uri.substring ( 0, pos );
    }
    private String removePathParameters ( final String input ) {
        int nextSemiColon = input.indexOf ( 59 );
        if ( nextSemiColon == -1 ) {
            return input;
        }
        final StringBuilder result = new StringBuilder ( input.length() );
        result.append ( input.substring ( 0, nextSemiColon ) );
        while ( true ) {
            final int nextSlash = input.indexOf ( 47, nextSemiColon );
            if ( nextSlash == -1 ) {
                break;
            }
            nextSemiColon = input.indexOf ( 59, nextSlash );
            if ( nextSemiColon == -1 ) {
                result.append ( input.substring ( nextSlash ) );
                break;
            }
            result.append ( input.substring ( nextSlash, nextSemiColon ) );
        }
        return result.toString();
    }
    private int nextSlash ( final char[] uri, final int startPos ) {
        for ( int len = uri.length, pos = startPos; pos < len; ++pos ) {
            if ( uri[pos] == '/' ) {
                return pos;
            }
            if ( UDecoder.ALLOW_ENCODED_SLASH && uri[pos] == '%' && pos + 2 < len && uri[pos + 1] == '2' && ( uri[pos + 2] == 'f' || uri[pos + 2] == 'F' ) ) {
                return pos;
            }
        }
        return -1;
    }
    public Cookie[] getCookies() {
        if ( !this.cookiesConverted ) {
            this.convertCookies();
        }
        return this.cookies;
    }
    public ServerCookies getServerCookies() {
        this.parseCookies();
        return this.coyoteRequest.getCookies();
    }
    public long getDateHeader ( final String name ) {
        final String value = this.getHeader ( name );
        if ( value == null ) {
            return -1L;
        }
        final long result = FastHttpDateFormat.parseDate ( value, this.formats );
        if ( result != -1L ) {
            return result;
        }
        throw new IllegalArgumentException ( value );
    }
    public String getHeader ( final String name ) {
        return this.coyoteRequest.getHeader ( name );
    }
    public Enumeration<String> getHeaders ( final String name ) {
        return this.coyoteRequest.getMimeHeaders().values ( name );
    }
    public Enumeration<String> getHeaderNames() {
        return this.coyoteRequest.getMimeHeaders().names();
    }
    public int getIntHeader ( final String name ) {
        final String value = this.getHeader ( name );
        if ( value == null ) {
            return -1;
        }
        return Integer.parseInt ( value );
    }
    public Mapping getMapping() {
        return this.applicationMapping.getMapping();
    }
    public String getMethod() {
        return this.coyoteRequest.method().toString();
    }
    public String getPathInfo() {
        return this.mappingData.pathInfo.toString();
    }
    public String getPathTranslated() {
        final Context context = this.getContext();
        if ( context == null ) {
            return null;
        }
        if ( this.getPathInfo() == null ) {
            return null;
        }
        return context.getServletContext().getRealPath ( this.getPathInfo() );
    }
    public String getQueryString() {
        return this.coyoteRequest.queryString().toString();
    }
    public String getRemoteUser() {
        if ( this.userPrincipal == null ) {
            return null;
        }
        return this.userPrincipal.getName();
    }
    public MessageBytes getRequestPathMB() {
        return this.mappingData.requestPath;
    }
    public String getRequestedSessionId() {
        return this.requestedSessionId;
    }
    public String getRequestURI() {
        return this.coyoteRequest.requestURI().toString();
    }
    public StringBuffer getRequestURL() {
        final StringBuffer url = new StringBuffer();
        final String scheme = this.getScheme();
        int port = this.getServerPort();
        if ( port < 0 ) {
            port = 80;
        }
        url.append ( scheme );
        url.append ( "://" );
        url.append ( this.getServerName() );
        if ( ( scheme.equals ( "http" ) && port != 80 ) || ( scheme.equals ( "https" ) && port != 443 ) ) {
            url.append ( ':' );
            url.append ( port );
        }
        url.append ( this.getRequestURI() );
        return url;
    }
    public String getServletPath() {
        return this.mappingData.wrapperPath.toString();
    }
    public HttpSession getSession() {
        final Session session = this.doGetSession ( true );
        if ( session == null ) {
            return null;
        }
        return session.getSession();
    }
    public HttpSession getSession ( final boolean create ) {
        final Session session = this.doGetSession ( create );
        if ( session == null ) {
            return null;
        }
        return session.getSession();
    }
    public boolean isRequestedSessionIdFromCookie() {
        return this.requestedSessionId != null && this.requestedSessionCookie;
    }
    public boolean isRequestedSessionIdFromURL() {
        return this.requestedSessionId != null && this.requestedSessionURL;
    }
    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return this.isRequestedSessionIdFromURL();
    }
    public boolean isRequestedSessionIdValid() {
        if ( this.requestedSessionId == null ) {
            return false;
        }
        final Context context = this.getContext();
        if ( context == null ) {
            return false;
        }
        final Manager manager = context.getManager();
        if ( manager == null ) {
            return false;
        }
        Session session = null;
        try {
            session = manager.findSession ( this.requestedSessionId );
        } catch ( IOException ex ) {}
        if ( session != null && session.isValid() ) {
            return true;
        }
        if ( this.getMappingData().contexts == null ) {
            return false;
        }
        for ( int i = this.getMappingData().contexts.length; i > 0; --i ) {
            final Context ctxt = this.getMappingData().contexts[i - 1];
            try {
                if ( ctxt.getManager().findSession ( this.requestedSessionId ) != null ) {
                    return true;
                }
            } catch ( IOException ex2 ) {}
        }
        return false;
    }
    public boolean isUserInRole ( final String role ) {
        if ( this.userPrincipal == null ) {
            return false;
        }
        final Context context = this.getContext();
        if ( context == null ) {
            return false;
        }
        if ( "*".equals ( role ) ) {
            return false;
        }
        if ( "**".equals ( role ) && !context.findSecurityRole ( "**" ) ) {
            return this.userPrincipal != null;
        }
        final Realm realm = context.getRealm();
        return realm != null && realm.hasRole ( this.getWrapper(), this.userPrincipal, role );
    }
    public Principal getPrincipal() {
        return this.userPrincipal;
    }
    public Principal getUserPrincipal() {
        if ( this.userPrincipal instanceof TomcatPrincipal ) {
            final GSSCredential gssCredential = ( ( TomcatPrincipal ) this.userPrincipal ).getGssCredential();
            if ( gssCredential != null ) {
                int left = -1;
                try {
                    left = gssCredential.getRemainingLifetime();
                } catch ( GSSException e ) {
                    Request.log.warn ( Request.sm.getString ( "coyoteRequest.gssLifetimeFail", this.userPrincipal.getName() ), e );
                }
                if ( left == 0 ) {
                    try {
                        this.logout();
                    } catch ( ServletException ex ) {}
                    return null;
                }
            }
            return ( ( TomcatPrincipal ) this.userPrincipal ).getUserPrincipal();
        }
        return this.userPrincipal;
    }
    public Session getSessionInternal() {
        return this.doGetSession ( true );
    }
    public void changeSessionId ( final String newSessionId ) {
        if ( this.requestedSessionId != null && this.requestedSessionId.length() > 0 ) {
            this.requestedSessionId = newSessionId;
        }
        final Context context = this.getContext();
        if ( context != null && !context.getServletContext().getEffectiveSessionTrackingModes().contains ( SessionTrackingMode.COOKIE ) ) {
            return;
        }
        if ( this.response != null ) {
            final Cookie newCookie = ApplicationSessionCookieConfig.createSessionCookie ( context, newSessionId, this.isSecure() );
            this.response.addSessionCookieInternal ( newCookie );
        }
    }
    public String changeSessionId() {
        final Session session = this.getSessionInternal ( false );
        if ( session == null ) {
            throw new IllegalStateException ( Request.sm.getString ( "coyoteRequest.changeSessionId" ) );
        }
        final Manager manager = this.getContext().getManager();
        manager.changeSessionId ( session );
        final String newSessionId = session.getId();
        this.changeSessionId ( newSessionId );
        return newSessionId;
    }
    public Session getSessionInternal ( final boolean create ) {
        return this.doGetSession ( create );
    }
    public boolean isParametersParsed() {
        return this.parametersParsed;
    }
    public boolean isFinished() {
        return this.coyoteRequest.isFinished();
    }
    protected void checkSwallowInput() {
        final Context context = this.getContext();
        if ( context != null && !context.getSwallowAbortedUploads() ) {
            this.coyoteRequest.action ( ActionCode.DISABLE_SWALLOW_INPUT, null );
        }
    }
    public boolean authenticate ( final HttpServletResponse response ) throws IOException, ServletException {
        if ( response.isCommitted() ) {
            throw new IllegalStateException ( Request.sm.getString ( "coyoteRequest.authenticate.ise" ) );
        }
        return this.getContext().getAuthenticator().authenticate ( this, response );
    }
    public void login ( final String username, final String password ) throws ServletException {
        if ( this.getAuthType() != null || this.getRemoteUser() != null || this.getUserPrincipal() != null ) {
            throw new ServletException ( Request.sm.getString ( "coyoteRequest.alreadyAuthenticated" ) );
        }
        final Context context = this.getContext();
        if ( context.getAuthenticator() == null ) {
            throw new ServletException ( "no authenticator" );
        }
        context.getAuthenticator().login ( username, password, this );
    }
    public void logout() throws ServletException {
        this.getContext().getAuthenticator().logout ( this );
    }
    public Collection<Part> getParts() throws IOException, IllegalStateException, ServletException {
        this.parseParts ( true );
        if ( this.partsParseException != null ) {
            if ( this.partsParseException instanceof IOException ) {
                throw ( IOException ) this.partsParseException;
            }
            if ( this.partsParseException instanceof IllegalStateException ) {
                throw ( IllegalStateException ) this.partsParseException;
            }
            if ( this.partsParseException instanceof ServletException ) {
                throw ( ServletException ) this.partsParseException;
            }
        }
        return this.parts;
    }
    private void parseParts ( final boolean explicit ) {
        if ( this.parts != null || this.partsParseException != null ) {
            return;
        }
        final Context context = this.getContext();
        MultipartConfigElement mce = this.getWrapper().getMultipartConfigElement();
        if ( mce == null ) {
            if ( context.getAllowCasualMultipartParsing() ) {
                mce = new MultipartConfigElement ( ( String ) null, ( long ) this.connector.getMaxPostSize(), ( long ) this.connector.getMaxPostSize(), this.connector.getMaxPostSize() );
            } else {
                if ( explicit ) {
                    this.partsParseException = new IllegalStateException ( Request.sm.getString ( "coyoteRequest.noMultipartConfig" ) );
                    return;
                }
                this.parts = ( Collection<Part> ) Collections.emptyList();
                return;
            }
        }
        final Parameters parameters = this.coyoteRequest.getParameters();
        parameters.setLimit ( this.getConnector().getMaxParameterCount() );
        boolean success = false;
        try {
            final String locationStr = mce.getLocation();
            File location;
            if ( locationStr == null || locationStr.length() == 0 ) {
                location = ( File ) context.getServletContext().getAttribute ( "javax.servlet.context.tempdir" );
            } else {
                location = new File ( locationStr );
                if ( !location.isAbsolute() ) {
                    location = new File ( ( File ) context.getServletContext().getAttribute ( "javax.servlet.context.tempdir" ), locationStr ).getAbsoluteFile();
                }
            }
            if ( !location.isDirectory() ) {
                parameters.setParseFailedReason ( Parameters.FailReason.MULTIPART_CONFIG_INVALID );
                this.partsParseException = new IOException ( Request.sm.getString ( "coyoteRequest.uploadLocationInvalid", location ) );
                return;
            }
            final DiskFileItemFactory factory = new DiskFileItemFactory();
            try {
                factory.setRepository ( location.getCanonicalFile() );
            } catch ( IOException ioe ) {
                parameters.setParseFailedReason ( Parameters.FailReason.IO_ERROR );
                this.partsParseException = ioe;
                return;
            }
            factory.setSizeThreshold ( mce.getFileSizeThreshold() );
            final ServletFileUpload upload = new ServletFileUpload();
            upload.setFileItemFactory ( factory );
            upload.setFileSizeMax ( mce.getMaxFileSize() );
            upload.setSizeMax ( mce.getMaxRequestSize() );
            this.parts = new ArrayList<Part>();
            try {
                final List<FileItem> items = upload.parseRequest ( new ServletRequestContext ( ( HttpServletRequest ) this ) );
                final int maxPostSize = this.getConnector().getMaxPostSize();
                int postSize = 0;
                final String enc = this.getCharacterEncoding();
                Charset charset = null;
                if ( enc != null ) {
                    try {
                        charset = B2CConverter.getCharset ( enc );
                    } catch ( UnsupportedEncodingException ex ) {}
                }
                for ( final FileItem item : items ) {
                    final ApplicationPart part = new ApplicationPart ( item, location );
                    this.parts.add ( ( Part ) part );
                    if ( part.getSubmittedFileName() == null ) {
                        final String name = part.getName();
                        String value = null;
                        try {
                            String encoding = parameters.getEncoding();
                            if ( encoding == null ) {
                                if ( enc == null ) {
                                    encoding = "ISO-8859-1";
                                } else {
                                    encoding = enc;
                                }
                            }
                            value = part.getString ( encoding );
                        } catch ( UnsupportedEncodingException uee ) {
                            try {
                                value = part.getString ( "ISO-8859-1" );
                            } catch ( UnsupportedEncodingException ex2 ) {}
                        }
                        if ( maxPostSize >= 0 ) {
                            if ( charset == null ) {
                                postSize += name.getBytes().length;
                            } else {
                                postSize += name.getBytes ( charset ).length;
                            }
                            if ( value != null ) {
                                postSize = ( int ) ( ++postSize + part.getSize() );
                            }
                            if ( ++postSize > maxPostSize ) {
                                parameters.setParseFailedReason ( Parameters.FailReason.POST_TOO_LARGE );
                                throw new IllegalStateException ( Request.sm.getString ( "coyoteRequest.maxPostSizeExceeded" ) );
                            }
                        }
                        parameters.addParameter ( name, value );
                    }
                }
                success = true;
            } catch ( FileUploadBase.InvalidContentTypeException e ) {
                parameters.setParseFailedReason ( Parameters.FailReason.INVALID_CONTENT_TYPE );
                this.partsParseException = ( Exception ) new ServletException ( ( Throwable ) e );
            } catch ( FileUploadBase.SizeException e2 ) {
                parameters.setParseFailedReason ( Parameters.FailReason.POST_TOO_LARGE );
                this.checkSwallowInput();
                this.partsParseException = new IllegalStateException ( e2 );
            } catch ( FileUploadException e3 ) {
                parameters.setParseFailedReason ( Parameters.FailReason.IO_ERROR );
                this.partsParseException = new IOException ( e3 );
            } catch ( IllegalStateException e4 ) {
                this.checkSwallowInput();
                this.partsParseException = e4;
            }
        } finally {
            if ( this.partsParseException != null || !success ) {
                parameters.setParseFailedReason ( Parameters.FailReason.UNKNOWN );
            }
        }
    }
    public Part getPart ( final String name ) throws IOException, IllegalStateException, ServletException {
        final Collection<Part> c = this.getParts();
        for ( final Part part : c ) {
            if ( name.equals ( part.getName() ) ) {
                return part;
            }
        }
        return null;
    }
    protected Session doGetSession ( final boolean create ) {
        final Context context = this.getContext();
        if ( context == null ) {
            return null;
        }
        if ( this.session != null && !this.session.isValid() ) {
            this.session = null;
        }
        if ( this.session != null ) {
            return this.session;
        }
        final Manager manager = context.getManager();
        if ( manager == null ) {
            return null;
        }
        if ( this.requestedSessionId != null ) {
            try {
                this.session = manager.findSession ( this.requestedSessionId );
            } catch ( IOException e ) {
                this.session = null;
            }
            if ( this.session != null && !this.session.isValid() ) {
                this.session = null;
            }
            if ( this.session != null ) {
                this.session.access();
                return this.session;
            }
        }
        if ( !create ) {
            return null;
        }
        if ( this.response != null && context.getServletContext().getEffectiveSessionTrackingModes().contains ( SessionTrackingMode.COOKIE ) && this.response.getResponse().isCommitted() ) {
            throw new IllegalStateException ( Request.sm.getString ( "coyoteRequest.sessionCreateCommitted" ) );
        }
        String sessionId = this.getRequestedSessionId();
        if ( !this.requestedSessionSSL ) {
            if ( "/".equals ( context.getSessionCookiePath() ) && this.isRequestedSessionIdFromCookie() ) {
                if ( context.getValidateClientProvidedNewSessionId() ) {
                    boolean found = false;
                    for ( final Container container : this.getHost().findChildren() ) {
                        final Manager m = ( ( Context ) container ).getManager();
                        if ( m != null ) {
                            try {
                                if ( m.findSession ( sessionId ) != null ) {
                                    found = true;
                                    break;
                                }
                            } catch ( IOException ex ) {}
                        }
                    }
                    if ( !found ) {
                        sessionId = null;
                    }
                }
            } else {
                sessionId = null;
            }
        }
        this.session = manager.createSession ( sessionId );
        if ( this.session != null && context.getServletContext().getEffectiveSessionTrackingModes().contains ( SessionTrackingMode.COOKIE ) ) {
            final Cookie cookie = ApplicationSessionCookieConfig.createSessionCookie ( context, this.session.getIdInternal(), this.isSecure() );
            this.response.addSessionCookieInternal ( cookie );
        }
        if ( this.session == null ) {
            return null;
        }
        this.session.access();
        return this.session;
    }
    protected String unescape ( final String s ) {
        if ( s == null ) {
            return null;
        }
        if ( s.indexOf ( 92 ) == -1 ) {
            return s;
        }
        final StringBuilder buf = new StringBuilder();
        for ( int i = 0; i < s.length(); ++i ) {
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
        if ( this.cookiesParsed ) {
            return;
        }
        this.cookiesParsed = true;
        final ServerCookies serverCookies = this.coyoteRequest.getCookies();
        serverCookies.setLimit ( this.connector.getMaxCookieCount() );
        final CookieProcessor cookieProcessor = this.getContext().getCookieProcessor();
        cookieProcessor.parseCookieHeader ( this.coyoteRequest.getMimeHeaders(), serverCookies );
    }
    protected void convertCookies() {
        if ( this.cookiesConverted ) {
            return;
        }
        this.cookiesConverted = true;
        if ( this.getContext() == null ) {
            return;
        }
        this.parseCookies();
        final ServerCookies serverCookies = this.coyoteRequest.getCookies();
        final CookieProcessor cookieProcessor = this.getContext().getCookieProcessor();
        final int count = serverCookies.getCookieCount();
        if ( count <= 0 ) {
            return;
        }
        this.cookies = new Cookie[count];
        int idx = 0;
        for ( int i = 0; i < count; ++i ) {
            final ServerCookie scookie = serverCookies.getCookie ( i );
            try {
                final Cookie cookie = new Cookie ( scookie.getName().toString(), ( String ) null );
                final int version = scookie.getVersion();
                cookie.setVersion ( version );
                scookie.getValue().getByteChunk().setCharset ( cookieProcessor.getCharset() );
                cookie.setValue ( this.unescape ( scookie.getValue().toString() ) );
                cookie.setPath ( this.unescape ( scookie.getPath().toString() ) );
                final String domain = scookie.getDomain().toString();
                if ( domain != null ) {
                    cookie.setDomain ( this.unescape ( domain ) );
                }
                final String comment = scookie.getComment().toString();
                cookie.setComment ( ( version == 1 ) ? this.unescape ( comment ) : null );
                this.cookies[idx++] = cookie;
            } catch ( IllegalArgumentException ex ) {}
        }
        if ( idx < count ) {
            final Cookie[] ncookies = new Cookie[idx];
            System.arraycopy ( this.cookies, 0, ncookies, 0, idx );
            this.cookies = ncookies;
        }
    }
    protected void parseParameters() {
        this.parametersParsed = true;
        final Parameters parameters = this.coyoteRequest.getParameters();
        boolean success = false;
        try {
            parameters.setLimit ( this.getConnector().getMaxParameterCount() );
            final String enc = this.getCharacterEncoding();
            final boolean useBodyEncodingForURI = this.connector.getUseBodyEncodingForURI();
            if ( enc != null ) {
                parameters.setEncoding ( enc );
                if ( useBodyEncodingForURI ) {
                    parameters.setQueryStringEncoding ( enc );
                }
            } else {
                parameters.setEncoding ( "ISO-8859-1" );
                if ( useBodyEncodingForURI ) {
                    parameters.setQueryStringEncoding ( "ISO-8859-1" );
                }
            }
            parameters.handleQueryParameters();
            if ( this.usingInputStream || this.usingReader ) {
                success = true;
                return;
            }
            if ( !this.getConnector().isParseBodyMethod ( this.getMethod() ) ) {
                success = true;
                return;
            }
            String contentType = this.getContentType();
            if ( contentType == null ) {
                contentType = "";
            }
            final int semicolon = contentType.indexOf ( 59 );
            if ( semicolon >= 0 ) {
                contentType = contentType.substring ( 0, semicolon ).trim();
            } else {
                contentType = contentType.trim();
            }
            if ( "multipart/form-data".equals ( contentType ) ) {
                this.parseParts ( false );
                success = true;
                return;
            }
            if ( !"application/x-www-form-urlencoded".equals ( contentType ) ) {
                success = true;
                return;
            }
            final int len = this.getContentLength();
            if ( len > 0 ) {
                final int maxPostSize = this.connector.getMaxPostSize();
                if ( maxPostSize >= 0 && len > maxPostSize ) {
                    final Context context = this.getContext();
                    if ( context != null && context.getLogger().isDebugEnabled() ) {
                        context.getLogger().debug ( Request.sm.getString ( "coyoteRequest.postTooLarge" ) );
                    }
                    this.checkSwallowInput();
                    parameters.setParseFailedReason ( Parameters.FailReason.POST_TOO_LARGE );
                    return;
                }
                byte[] formData = null;
                if ( len < 8192 ) {
                    if ( this.postData == null ) {
                        this.postData = new byte[8192];
                    }
                    formData = this.postData;
                } else {
                    formData = new byte[len];
                }
                try {
                    if ( this.readPostBody ( formData, len ) != len ) {
                        parameters.setParseFailedReason ( Parameters.FailReason.REQUEST_BODY_INCOMPLETE );
                        return;
                    }
                } catch ( IOException e ) {
                    final Context context2 = this.getContext();
                    if ( context2 != null && context2.getLogger().isDebugEnabled() ) {
                        context2.getLogger().debug ( Request.sm.getString ( "coyoteRequest.parseParameters" ), e );
                    }
                    parameters.setParseFailedReason ( Parameters.FailReason.CLIENT_DISCONNECT );
                    return;
                }
                parameters.processParameters ( formData, 0, len );
            } else if ( "chunked".equalsIgnoreCase ( this.coyoteRequest.getHeader ( "transfer-encoding" ) ) ) {
                byte[] formData2 = null;
                try {
                    formData2 = this.readChunkedPostBody();
                } catch ( IllegalStateException ise ) {
                    parameters.setParseFailedReason ( Parameters.FailReason.POST_TOO_LARGE );
                    final Context context3 = this.getContext();
                    if ( context3 != null && context3.getLogger().isDebugEnabled() ) {
                        context3.getLogger().debug ( Request.sm.getString ( "coyoteRequest.parseParameters" ), ise );
                    }
                    return;
                } catch ( IOException e2 ) {
                    parameters.setParseFailedReason ( Parameters.FailReason.CLIENT_DISCONNECT );
                    final Context context3 = this.getContext();
                    if ( context3 != null && context3.getLogger().isDebugEnabled() ) {
                        context3.getLogger().debug ( Request.sm.getString ( "coyoteRequest.parseParameters" ), e2 );
                    }
                    return;
                }
                if ( formData2 != null ) {
                    parameters.processParameters ( formData2, 0, formData2.length );
                }
            }
            success = true;
        } finally {
            if ( !success ) {
                parameters.setParseFailedReason ( Parameters.FailReason.UNKNOWN );
            }
        }
    }
    protected int readPostBody ( final byte[] body, final int len ) throws IOException {
        int offset = 0;
        do {
            final int inputLen = this.getStream().read ( body, offset, len - offset );
            if ( inputLen <= 0 ) {
                return offset;
            }
            offset += inputLen;
        } while ( len - offset > 0 );
        return len;
    }
    protected byte[] readChunkedPostBody() throws IOException {
        final ByteChunk body = new ByteChunk();
        final byte[] buffer = new byte[8192];
        int len = 0;
        while ( len > -1 ) {
            len = this.getStream().read ( buffer, 0, 8192 );
            if ( this.connector.getMaxPostSize() >= 0 && body.getLength() + len > this.connector.getMaxPostSize() ) {
                this.checkSwallowInput();
                throw new IllegalStateException ( Request.sm.getString ( "coyoteRequest.chunkedPostTooLarge" ) );
            }
            if ( len <= 0 ) {
                continue;
            }
            body.append ( buffer, 0, len );
        }
        if ( body.getLength() == 0 ) {
            return null;
        }
        if ( body.getLength() < body.getBuffer().length ) {
            final int length = body.getLength();
            final byte[] result = new byte[length];
            System.arraycopy ( body.getBuffer(), 0, result, 0, length );
            return result;
        }
        return body.getBuffer();
    }
    protected void parseLocales() {
        this.localesParsed = true;
        final TreeMap<Double, ArrayList<Locale>> locales = new TreeMap<Double, ArrayList<Locale>>();
        final Enumeration<String> values = this.getHeaders ( "accept-language" );
        while ( values.hasMoreElements() ) {
            final String value = values.nextElement();
            this.parseLocalesHeader ( value, locales );
        }
        for ( final ArrayList<Locale> list : locales.values() ) {
            for ( final Locale locale : list ) {
                this.addLocale ( locale );
            }
        }
    }
    protected void parseLocalesHeader ( final String value, final TreeMap<Double, ArrayList<Locale>> locales ) {
        List<AcceptLanguage> acceptLanguages;
        try {
            acceptLanguages = AcceptLanguage.parse ( new StringReader ( value ) );
        } catch ( IOException e ) {
            return;
        }
        for ( final AcceptLanguage acceptLanguage : acceptLanguages ) {
            final Double key = -acceptLanguage.getQuality();
            ArrayList<Locale> values = locales.get ( key );
            if ( values == null ) {
                values = new ArrayList<Locale>();
                locales.put ( key, values );
            }
            values.add ( acceptLanguage.getLocale() );
        }
    }
    static {
        log = LogFactory.getLog ( Request.class );
        GMT_ZONE = TimeZone.getTimeZone ( "GMT" );
        sm = StringManager.getManager ( Request.class );
        formatsTemplate = new SimpleDateFormat[] { new SimpleDateFormat ( "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US ), new SimpleDateFormat ( "EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US ), new SimpleDateFormat ( "EEE MMMM d HH:mm:ss yyyy", Locale.US ) };
        defaultLocale = Locale.getDefault();
        ( specialAttributes = new HashMap<String, SpecialAttributeAdapter>() ).put ( "org.apache.catalina.core.DISPATCHER_TYPE", new SpecialAttributeAdapter() {
            @Override
            public Object get ( final Request request, final String name ) {
                return ( request.internalDispatcherType == null ) ? DispatcherType.REQUEST : request.internalDispatcherType;
            }
            @Override
            public void set ( final Request request, final String name, final Object value ) {
                request.internalDispatcherType = ( DispatcherType ) value;
            }
        } );
        Request.specialAttributes.put ( "org.apache.catalina.core.DISPATCHER_REQUEST_PATH", new SpecialAttributeAdapter() {
            @Override
            public Object get ( final Request request, final String name ) {
                return ( request.requestDispatcherPath == null ) ? request.getRequestPathMB().toString() : request.requestDispatcherPath.toString();
            }
            @Override
            public void set ( final Request request, final String name, final Object value ) {
                request.requestDispatcherPath = value;
            }
        } );
        Request.specialAttributes.put ( "org.apache.catalina.ASYNC_SUPPORTED", new SpecialAttributeAdapter() {
            @Override
            public Object get ( final Request request, final String name ) {
                return request.asyncSupported;
            }
            @Override
            public void set ( final Request request, final String name, final Object value ) {
                final Boolean oldValue = request.asyncSupported;
                request.asyncSupported = ( Boolean ) value;
                request.notifyAttributeAssigned ( name, value, oldValue );
            }
        } );
        Request.specialAttributes.put ( "org.apache.catalina.realm.GSS_CREDENTIAL", new SpecialAttributeAdapter() {
            @Override
            public Object get ( final Request request, final String name ) {
                if ( request.userPrincipal instanceof TomcatPrincipal ) {
                    return ( ( TomcatPrincipal ) request.userPrincipal ).getGssCredential();
                }
                return null;
            }
            @Override
            public void set ( final Request request, final String name, final Object value ) {
            }
        } );
        Request.specialAttributes.put ( "org.apache.catalina.parameter_parse_failed", new SpecialAttributeAdapter() {
            @Override
            public Object get ( final Request request, final String name ) {
                if ( request.getCoyoteRequest().getParameters().isParseFailed() ) {
                    return Boolean.TRUE;
                }
                return null;
            }
            @Override
            public void set ( final Request request, final String name, final Object value ) {
            }
        } );
        Request.specialAttributes.put ( "org.apache.catalina.parameter_parse_failed_reason", new SpecialAttributeAdapter() {
            @Override
            public Object get ( final Request request, final String name ) {
                return request.getCoyoteRequest().getParameters().getParseFailedReason();
            }
            @Override
            public void set ( final Request request, final String name, final Object value ) {
            }
        } );
        Request.specialAttributes.put ( "org.apache.tomcat.sendfile.support", new SpecialAttributeAdapter() {
            @Override
            public Object get ( final Request request, final String name ) {
                return request.getConnector().getProtocolHandler().isSendfileSupported() && request.getCoyoteRequest().getSendfile();
            }
            @Override
            public void set ( final Request request, final String name, final Object value ) {
            }
        } );
        for ( final SimpleDateFormat sdf : Request.formatsTemplate ) {
            sdf.setTimeZone ( Request.GMT_ZONE );
        }
    }
    private interface SpecialAttributeAdapter {
        Object get ( Request p0, String p1 );
        void set ( Request p0, String p1, Object p2 );
    }
}
