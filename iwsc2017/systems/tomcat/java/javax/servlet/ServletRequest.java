package javax.servlet;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
public interface ServletRequest {
    public Object getAttribute ( String name );
    public Enumeration<String> getAttributeNames();
    public String getCharacterEncoding();
    public void setCharacterEncoding ( String env )
    throws java.io.UnsupportedEncodingException;
    public int getContentLength();
    public long getContentLengthLong();
    public String getContentType();
    public ServletInputStream getInputStream() throws IOException;
    public String getParameter ( String name );
    public Enumeration<String> getParameterNames();
    public String[] getParameterValues ( String name );
    public Map<String, String[]> getParameterMap();
    public String getProtocol();
    public String getScheme();
    public String getServerName();
    public int getServerPort();
    public BufferedReader getReader() throws IOException;
    public String getRemoteAddr();
    public String getRemoteHost();
    public void setAttribute ( String name, Object o );
    public void removeAttribute ( String name );
    public Locale getLocale();
    public Enumeration<Locale> getLocales();
    public boolean isSecure();
    public RequestDispatcher getRequestDispatcher ( String path );
    @Deprecated
    public String getRealPath ( String path );
    public int getRemotePort();
    public String getLocalName();
    public String getLocalAddr();
    public int getLocalPort();
    public ServletContext getServletContext();
    public AsyncContext startAsync() throws IllegalStateException;
    public AsyncContext startAsync ( ServletRequest servletRequest,
                                     ServletResponse servletResponse ) throws IllegalStateException;
    public boolean isAsyncStarted();
    public boolean isAsyncSupported();
    public AsyncContext getAsyncContext();
    public DispatcherType getDispatcherType();
}
