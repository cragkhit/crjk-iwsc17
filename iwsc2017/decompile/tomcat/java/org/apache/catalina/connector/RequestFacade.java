package org.apache.catalina.connector;
import javax.servlet.http.PushBuilder;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import java.util.Collection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.DispatcherType;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.AsyncContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import javax.servlet.http.Mapping;
import javax.servlet.http.Cookie;
import javax.servlet.RequestDispatcher;
import java.util.Locale;
import java.io.BufferedReader;
import java.util.Map;
import org.apache.catalina.security.SecurityUtil;
import java.io.IOException;
import javax.servlet.ServletInputStream;
import java.io.UnsupportedEncodingException;
import java.security.PrivilegedAction;
import java.security.AccessController;
import org.apache.catalina.Globals;
import java.util.Enumeration;
import org.apache.tomcat.util.res.StringManager;
import javax.servlet.http.HttpServletRequest;
public class RequestFacade implements HttpServletRequest {
    protected Request request;
    protected static final StringManager sm;
    public RequestFacade ( final Request request ) {
        this.request = null;
        this.request = request;
    }
    public void clear() {
        this.request = null;
    }
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    public Object getAttribute ( final String name ) {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getAttribute ( name );
    }
    public Enumeration<String> getAttributeNames() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        if ( Globals.IS_SECURITY_ENABLED ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<Enumeration<String>> ) new GetAttributePrivilegedAction() );
        }
        return this.request.getAttributeNames();
    }
    public String getCharacterEncoding() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        if ( Globals.IS_SECURITY_ENABLED ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<String> ) new GetCharacterEncodingPrivilegedAction() );
        }
        return this.request.getCharacterEncoding();
    }
    public void setCharacterEncoding ( final String env ) throws UnsupportedEncodingException {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        this.request.setCharacterEncoding ( env );
    }
    public int getContentLength() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getContentLength();
    }
    public String getContentType() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getContentType();
    }
    public ServletInputStream getInputStream() throws IOException {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getInputStream();
    }
    public String getParameter ( final String name ) {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        if ( Globals.IS_SECURITY_ENABLED ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<String> ) new GetParameterPrivilegedAction ( name ) );
        }
        return this.request.getParameter ( name );
    }
    public Enumeration<String> getParameterNames() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        if ( Globals.IS_SECURITY_ENABLED ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<Enumeration<String>> ) new GetParameterNamesPrivilegedAction() );
        }
        return this.request.getParameterNames();
    }
    public String[] getParameterValues ( final String name ) {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        String[] ret = null;
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            ret = AccessController.doPrivileged ( ( PrivilegedAction<String[]> ) new GetParameterValuePrivilegedAction ( name ) );
            if ( ret != null ) {
                ret = ret.clone();
            }
        } else {
            ret = this.request.getParameterValues ( name );
        }
        return ret;
    }
    public Map<String, String[]> getParameterMap() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        if ( Globals.IS_SECURITY_ENABLED ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<Map<String, String[]>> ) new GetParameterMapPrivilegedAction() );
        }
        return this.request.getParameterMap();
    }
    public String getProtocol() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getProtocol();
    }
    public String getScheme() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getScheme();
    }
    public String getServerName() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getServerName();
    }
    public int getServerPort() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getServerPort();
    }
    public BufferedReader getReader() throws IOException {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getReader();
    }
    public String getRemoteAddr() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getRemoteAddr();
    }
    public String getRemoteHost() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getRemoteHost();
    }
    public void setAttribute ( final String name, final Object o ) {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        this.request.setAttribute ( name, o );
    }
    public void removeAttribute ( final String name ) {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        this.request.removeAttribute ( name );
    }
    public Locale getLocale() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        if ( Globals.IS_SECURITY_ENABLED ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<Locale> ) new GetLocalePrivilegedAction() );
        }
        return this.request.getLocale();
    }
    public Enumeration<Locale> getLocales() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        if ( Globals.IS_SECURITY_ENABLED ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<Enumeration<Locale>> ) new GetLocalesPrivilegedAction() );
        }
        return this.request.getLocales();
    }
    public boolean isSecure() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.isSecure();
    }
    public RequestDispatcher getRequestDispatcher ( final String path ) {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        if ( Globals.IS_SECURITY_ENABLED ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<RequestDispatcher> ) new GetRequestDispatcherPrivilegedAction ( path ) );
        }
        return this.request.getRequestDispatcher ( path );
    }
    public String getRealPath ( final String path ) {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getRealPath ( path );
    }
    public String getAuthType() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getAuthType();
    }
    public Cookie[] getCookies() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        Cookie[] ret = null;
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            ret = AccessController.doPrivileged ( ( PrivilegedAction<Cookie[]> ) new GetCookiesPrivilegedAction() );
            if ( ret != null ) {
                ret = ret.clone();
            }
        } else {
            ret = this.request.getCookies();
        }
        return ret;
    }
    public long getDateHeader ( final String name ) {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getDateHeader ( name );
    }
    public String getHeader ( final String name ) {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getHeader ( name );
    }
    public Enumeration<String> getHeaders ( final String name ) {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        if ( Globals.IS_SECURITY_ENABLED ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<Enumeration<String>> ) new GetHeadersPrivilegedAction ( name ) );
        }
        return this.request.getHeaders ( name );
    }
    public Enumeration<String> getHeaderNames() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        if ( Globals.IS_SECURITY_ENABLED ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<Enumeration<String>> ) new GetHeaderNamesPrivilegedAction() );
        }
        return this.request.getHeaderNames();
    }
    public int getIntHeader ( final String name ) {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getIntHeader ( name );
    }
    public Mapping getMapping() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getMapping();
    }
    public String getMethod() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getMethod();
    }
    public String getPathInfo() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getPathInfo();
    }
    public String getPathTranslated() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getPathTranslated();
    }
    public String getContextPath() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getContextPath();
    }
    public String getQueryString() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getQueryString();
    }
    public String getRemoteUser() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getRemoteUser();
    }
    public boolean isUserInRole ( final String role ) {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.isUserInRole ( role );
    }
    public Principal getUserPrincipal() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getUserPrincipal();
    }
    public String getRequestedSessionId() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getRequestedSessionId();
    }
    public String getRequestURI() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getRequestURI();
    }
    public StringBuffer getRequestURL() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getRequestURL();
    }
    public String getServletPath() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getServletPath();
    }
    public HttpSession getSession ( final boolean create ) {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<HttpSession> ) new GetSessionPrivilegedAction ( create ) );
        }
        return this.request.getSession ( create );
    }
    public HttpSession getSession() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.getSession ( true );
    }
    public String changeSessionId() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.changeSessionId();
    }
    public boolean isRequestedSessionIdValid() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.isRequestedSessionIdValid();
    }
    public boolean isRequestedSessionIdFromCookie() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.isRequestedSessionIdFromCookie();
    }
    public boolean isRequestedSessionIdFromURL() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.isRequestedSessionIdFromURL();
    }
    public boolean isRequestedSessionIdFromUrl() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.isRequestedSessionIdFromURL();
    }
    public String getLocalAddr() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getLocalAddr();
    }
    public String getLocalName() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getLocalName();
    }
    public int getLocalPort() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getLocalPort();
    }
    public int getRemotePort() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getRemotePort();
    }
    public ServletContext getServletContext() {
        if ( this.request == null ) {
            throw new IllegalStateException ( RequestFacade.sm.getString ( "requestFacade.nullRequest" ) );
        }
        return this.request.getServletContext();
    }
    public AsyncContext startAsync() throws IllegalStateException {
        return this.request.startAsync();
    }
    public AsyncContext startAsync ( final ServletRequest request, final ServletResponse response ) throws IllegalStateException {
        return this.request.startAsync ( request, response );
    }
    public boolean isAsyncStarted() {
        return this.request.isAsyncStarted();
    }
    public boolean isAsyncSupported() {
        return this.request.isAsyncSupported();
    }
    public AsyncContext getAsyncContext() {
        return this.request.getAsyncContext();
    }
    public DispatcherType getDispatcherType() {
        return this.request.getDispatcherType();
    }
    public boolean authenticate ( final HttpServletResponse response ) throws IOException, ServletException {
        return this.request.authenticate ( response );
    }
    public void login ( final String username, final String password ) throws ServletException {
        this.request.login ( username, password );
    }
    public void logout() throws ServletException {
        this.request.logout();
    }
    public Collection<Part> getParts() throws IllegalStateException, IOException, ServletException {
        return this.request.getParts();
    }
    public Part getPart ( final String name ) throws IllegalStateException, IOException, ServletException {
        return this.request.getPart ( name );
    }
    public boolean getAllowTrace() {
        return this.request.getConnector().getAllowTrace();
    }
    public long getContentLengthLong() {
        return this.request.getContentLengthLong();
    }
    public <T extends HttpUpgradeHandler> T upgrade ( final Class<T> httpUpgradeHandlerClass ) throws IOException, ServletException {
        return this.request.upgrade ( httpUpgradeHandlerClass );
    }
    public PushBuilder getPushBuilder() {
        return this.request.getPushBuilder();
    }
    static {
        sm = StringManager.getManager ( RequestFacade.class );
    }
    private final class GetAttributePrivilegedAction implements PrivilegedAction<Enumeration<String>> {
        @Override
        public Enumeration<String> run() {
            return RequestFacade.this.request.getAttributeNames();
        }
    }
    private final class GetParameterMapPrivilegedAction implements PrivilegedAction<Map<String, String[]>> {
        @Override
        public Map<String, String[]> run() {
            return RequestFacade.this.request.getParameterMap();
        }
    }
    private final class GetRequestDispatcherPrivilegedAction implements PrivilegedAction<RequestDispatcher> {
        private final String path;
        public GetRequestDispatcherPrivilegedAction ( final String path ) {
            this.path = path;
        }
        @Override
        public RequestDispatcher run() {
            return RequestFacade.this.request.getRequestDispatcher ( this.path );
        }
    }
    private final class GetParameterPrivilegedAction implements PrivilegedAction<String> {
        public String name;
        public GetParameterPrivilegedAction ( final String name ) {
            this.name = name;
        }
        @Override
        public String run() {
            return RequestFacade.this.request.getParameter ( this.name );
        }
    }
    private final class GetParameterNamesPrivilegedAction implements PrivilegedAction<Enumeration<String>> {
        @Override
        public Enumeration<String> run() {
            return RequestFacade.this.request.getParameterNames();
        }
    }
    private final class GetParameterValuePrivilegedAction implements PrivilegedAction<String[]> {
        public String name;
        public GetParameterValuePrivilegedAction ( final String name ) {
            this.name = name;
        }
        @Override
        public String[] run() {
            return RequestFacade.this.request.getParameterValues ( this.name );
        }
    }
    private final class GetCookiesPrivilegedAction implements PrivilegedAction<Cookie[]> {
        @Override
        public Cookie[] run() {
            return RequestFacade.this.request.getCookies();
        }
    }
    private final class GetCharacterEncodingPrivilegedAction implements PrivilegedAction<String> {
        @Override
        public String run() {
            return RequestFacade.this.request.getCharacterEncoding();
        }
    }
    private final class GetHeadersPrivilegedAction implements PrivilegedAction<Enumeration<String>> {
        private final String name;
        public GetHeadersPrivilegedAction ( final String name ) {
            this.name = name;
        }
        @Override
        public Enumeration<String> run() {
            return RequestFacade.this.request.getHeaders ( this.name );
        }
    }
    private final class GetHeaderNamesPrivilegedAction implements PrivilegedAction<Enumeration<String>> {
        @Override
        public Enumeration<String> run() {
            return RequestFacade.this.request.getHeaderNames();
        }
    }
    private final class GetLocalePrivilegedAction implements PrivilegedAction<Locale> {
        @Override
        public Locale run() {
            return RequestFacade.this.request.getLocale();
        }
    }
    private final class GetLocalesPrivilegedAction implements PrivilegedAction<Enumeration<Locale>> {
        @Override
        public Enumeration<Locale> run() {
            return RequestFacade.this.request.getLocales();
        }
    }
    private final class GetSessionPrivilegedAction implements PrivilegedAction<HttpSession> {
        private final boolean create;
        public GetSessionPrivilegedAction ( final boolean create ) {
            this.create = create;
        }
        @Override
        public HttpSession run() {
            return RequestFacade.this.request.getSession ( this.create );
        }
    }
}
