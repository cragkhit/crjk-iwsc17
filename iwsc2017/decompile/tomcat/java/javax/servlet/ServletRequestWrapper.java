package javax.servlet;
import java.util.Locale;
import java.io.BufferedReader;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
public class ServletRequestWrapper implements ServletRequest {
    private ServletRequest request;
    public ServletRequestWrapper ( final ServletRequest request ) {
        if ( request == null ) {
            throw new IllegalArgumentException ( "Request cannot be null" );
        }
        this.request = request;
    }
    public ServletRequest getRequest() {
        return this.request;
    }
    public void setRequest ( final ServletRequest request ) {
        if ( request == null ) {
            throw new IllegalArgumentException ( "Request cannot be null" );
        }
        this.request = request;
    }
    @Override
    public Object getAttribute ( final String name ) {
        return this.request.getAttribute ( name );
    }
    @Override
    public Enumeration<String> getAttributeNames() {
        return this.request.getAttributeNames();
    }
    @Override
    public String getCharacterEncoding() {
        return this.request.getCharacterEncoding();
    }
    @Override
    public void setCharacterEncoding ( final String enc ) throws UnsupportedEncodingException {
        this.request.setCharacterEncoding ( enc );
    }
    @Override
    public int getContentLength() {
        return this.request.getContentLength();
    }
    @Override
    public long getContentLengthLong() {
        return this.request.getContentLengthLong();
    }
    @Override
    public String getContentType() {
        return this.request.getContentType();
    }
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return this.request.getInputStream();
    }
    @Override
    public String getParameter ( final String name ) {
        return this.request.getParameter ( name );
    }
    @Override
    public Map<String, String[]> getParameterMap() {
        return this.request.getParameterMap();
    }
    @Override
    public Enumeration<String> getParameterNames() {
        return this.request.getParameterNames();
    }
    @Override
    public String[] getParameterValues ( final String name ) {
        return this.request.getParameterValues ( name );
    }
    @Override
    public String getProtocol() {
        return this.request.getProtocol();
    }
    @Override
    public String getScheme() {
        return this.request.getScheme();
    }
    @Override
    public String getServerName() {
        return this.request.getServerName();
    }
    @Override
    public int getServerPort() {
        return this.request.getServerPort();
    }
    @Override
    public BufferedReader getReader() throws IOException {
        return this.request.getReader();
    }
    @Override
    public String getRemoteAddr() {
        return this.request.getRemoteAddr();
    }
    @Override
    public String getRemoteHost() {
        return this.request.getRemoteHost();
    }
    @Override
    public void setAttribute ( final String name, final Object o ) {
        this.request.setAttribute ( name, o );
    }
    @Override
    public void removeAttribute ( final String name ) {
        this.request.removeAttribute ( name );
    }
    @Override
    public Locale getLocale() {
        return this.request.getLocale();
    }
    @Override
    public Enumeration<Locale> getLocales() {
        return this.request.getLocales();
    }
    @Override
    public boolean isSecure() {
        return this.request.isSecure();
    }
    @Override
    public RequestDispatcher getRequestDispatcher ( final String path ) {
        return this.request.getRequestDispatcher ( path );
    }
    @Deprecated
    @Override
    public String getRealPath ( final String path ) {
        return this.request.getRealPath ( path );
    }
    @Override
    public int getRemotePort() {
        return this.request.getRemotePort();
    }
    @Override
    public String getLocalName() {
        return this.request.getLocalName();
    }
    @Override
    public String getLocalAddr() {
        return this.request.getLocalAddr();
    }
    @Override
    public int getLocalPort() {
        return this.request.getLocalPort();
    }
    @Override
    public ServletContext getServletContext() {
        return this.request.getServletContext();
    }
    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return this.request.startAsync();
    }
    @Override
    public AsyncContext startAsync ( final ServletRequest servletRequest, final ServletResponse servletResponse ) throws IllegalStateException {
        return this.request.startAsync ( servletRequest, servletResponse );
    }
    @Override
    public boolean isAsyncStarted() {
        return this.request.isAsyncStarted();
    }
    @Override
    public boolean isAsyncSupported() {
        return this.request.isAsyncSupported();
    }
    @Override
    public AsyncContext getAsyncContext() {
        return this.request.getAsyncContext();
    }
    public boolean isWrapperFor ( final ServletRequest wrapped ) {
        return this.request == wrapped || ( this.request instanceof ServletRequestWrapper && ( ( ServletRequestWrapper ) this.request ).isWrapperFor ( wrapped ) );
    }
    public boolean isWrapperFor ( final Class<?> wrappedType ) {
        return wrappedType.isAssignableFrom ( this.request.getClass() ) || ( this.request instanceof ServletRequestWrapper && ( ( ServletRequestWrapper ) this.request ).isWrapperFor ( wrappedType ) );
    }
    @Override
    public DispatcherType getDispatcherType() {
        return this.request.getDispatcherType();
    }
}
