package javax.servlet.http;
import java.util.Collection;
import javax.servlet.ServletException;
import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
public class HttpServletRequestWrapper extends ServletRequestWrapper implements HttpServletRequest {
    public HttpServletRequestWrapper ( final HttpServletRequest request ) {
        super ( request );
    }
    private HttpServletRequest _getHttpServletRequest() {
        return ( HttpServletRequest ) super.getRequest();
    }
    @Override
    public String getAuthType() {
        return this._getHttpServletRequest().getAuthType();
    }
    @Override
    public Cookie[] getCookies() {
        return this._getHttpServletRequest().getCookies();
    }
    @Override
    public long getDateHeader ( final String name ) {
        return this._getHttpServletRequest().getDateHeader ( name );
    }
    @Override
    public String getHeader ( final String name ) {
        return this._getHttpServletRequest().getHeader ( name );
    }
    @Override
    public Enumeration<String> getHeaders ( final String name ) {
        return this._getHttpServletRequest().getHeaders ( name );
    }
    @Override
    public Enumeration<String> getHeaderNames() {
        return this._getHttpServletRequest().getHeaderNames();
    }
    @Override
    public int getIntHeader ( final String name ) {
        return this._getHttpServletRequest().getIntHeader ( name );
    }
    @Override
    public Mapping getMapping() {
        return this._getHttpServletRequest().getMapping();
    }
    @Override
    public String getMethod() {
        return this._getHttpServletRequest().getMethod();
    }
    @Override
    public String getPathInfo() {
        return this._getHttpServletRequest().getPathInfo();
    }
    @Override
    public String getPathTranslated() {
        return this._getHttpServletRequest().getPathTranslated();
    }
    @Override
    public String getContextPath() {
        return this._getHttpServletRequest().getContextPath();
    }
    @Override
    public String getQueryString() {
        return this._getHttpServletRequest().getQueryString();
    }
    @Override
    public String getRemoteUser() {
        return this._getHttpServletRequest().getRemoteUser();
    }
    @Override
    public boolean isUserInRole ( final String role ) {
        return this._getHttpServletRequest().isUserInRole ( role );
    }
    @Override
    public Principal getUserPrincipal() {
        return this._getHttpServletRequest().getUserPrincipal();
    }
    @Override
    public String getRequestedSessionId() {
        return this._getHttpServletRequest().getRequestedSessionId();
    }
    @Override
    public String getRequestURI() {
        return this._getHttpServletRequest().getRequestURI();
    }
    @Override
    public StringBuffer getRequestURL() {
        return this._getHttpServletRequest().getRequestURL();
    }
    @Override
    public String getServletPath() {
        return this._getHttpServletRequest().getServletPath();
    }
    @Override
    public HttpSession getSession ( final boolean create ) {
        return this._getHttpServletRequest().getSession ( create );
    }
    @Override
    public HttpSession getSession() {
        return this._getHttpServletRequest().getSession();
    }
    @Override
    public String changeSessionId() {
        return this._getHttpServletRequest().changeSessionId();
    }
    @Override
    public boolean isRequestedSessionIdValid() {
        return this._getHttpServletRequest().isRequestedSessionIdValid();
    }
    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return this._getHttpServletRequest().isRequestedSessionIdFromCookie();
    }
    @Override
    public boolean isRequestedSessionIdFromURL() {
        return this._getHttpServletRequest().isRequestedSessionIdFromURL();
    }
    @Deprecated
    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return this._getHttpServletRequest().isRequestedSessionIdFromUrl();
    }
    @Override
    public boolean authenticate ( final HttpServletResponse response ) throws IOException, ServletException {
        return this._getHttpServletRequest().authenticate ( response );
    }
    @Override
    public void login ( final String username, final String password ) throws ServletException {
        this._getHttpServletRequest().login ( username, password );
    }
    @Override
    public void logout() throws ServletException {
        this._getHttpServletRequest().logout();
    }
    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return this._getHttpServletRequest().getParts();
    }
    @Override
    public Part getPart ( final String name ) throws IOException, ServletException {
        return this._getHttpServletRequest().getPart ( name );
    }
    @Override
    public <T extends HttpUpgradeHandler> T upgrade ( final Class<T> httpUpgradeHandlerClass ) throws IOException, ServletException {
        return this._getHttpServletRequest().upgrade ( httpUpgradeHandlerClass );
    }
    @Override
    public boolean isPushSupported() {
        return this._getHttpServletRequest().isPushSupported();
    }
    @Override
    public PushBuilder getPushBuilder() {
        return this._getHttpServletRequest().getPushBuilder();
    }
}
