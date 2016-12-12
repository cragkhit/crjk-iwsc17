package javax.servlet;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.util.EventListener;
import java.util.Map;
import java.util.Enumeration;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
public interface ServletContext {
    public static final String TEMPDIR = "javax.servlet.context.tempdir";
    public static final String ORDERED_LIBS = "javax.servlet.context.orderedLibs";
    String getContextPath();
    ServletContext getContext ( String p0 );
    int getMajorVersion();
    int getMinorVersion();
    int getEffectiveMajorVersion();
    int getEffectiveMinorVersion();
    String getMimeType ( String p0 );
    Set<String> getResourcePaths ( String p0 );
    URL getResource ( String p0 ) throws MalformedURLException;
    InputStream getResourceAsStream ( String p0 );
    RequestDispatcher getRequestDispatcher ( String p0 );
    RequestDispatcher getNamedDispatcher ( String p0 );
    @Deprecated
    Servlet getServlet ( String p0 ) throws ServletException;
    @Deprecated
    Enumeration<Servlet> getServlets();
    @Deprecated
    Enumeration<String> getServletNames();
    void log ( String p0 );
    @Deprecated
    void log ( Exception p0, String p1 );
    void log ( String p0, Throwable p1 );
    String getRealPath ( String p0 );
    String getServerInfo();
    String getInitParameter ( String p0 );
    Enumeration<String> getInitParameterNames();
    boolean setInitParameter ( String p0, String p1 );
    Object getAttribute ( String p0 );
    Enumeration<String> getAttributeNames();
    void setAttribute ( String p0, Object p1 );
    void removeAttribute ( String p0 );
    String getServletContextName();
    ServletRegistration.Dynamic addServlet ( String p0, String p1 );
    ServletRegistration.Dynamic addServlet ( String p0, Servlet p1 );
    ServletRegistration.Dynamic addServlet ( String p0, Class<? extends Servlet> p1 );
    <T extends Servlet> T createServlet ( Class<T> p0 ) throws ServletException;
    ServletRegistration getServletRegistration ( String p0 );
    Map<String, ? extends ServletRegistration> getServletRegistrations();
    FilterRegistration.Dynamic addFilter ( String p0, String p1 );
    FilterRegistration.Dynamic addFilter ( String p0, Filter p1 );
    FilterRegistration.Dynamic addFilter ( String p0, Class<? extends Filter> p1 );
    <T extends Filter> T createFilter ( Class<T> p0 ) throws ServletException;
    FilterRegistration getFilterRegistration ( String p0 );
    Map<String, ? extends FilterRegistration> getFilterRegistrations();
    SessionCookieConfig getSessionCookieConfig();
    void setSessionTrackingModes ( Set<SessionTrackingMode> p0 );
    Set<SessionTrackingMode> getDefaultSessionTrackingModes();
    Set<SessionTrackingMode> getEffectiveSessionTrackingModes();
    void addListener ( String p0 );
    <T extends EventListener> void addListener ( T p0 );
    void addListener ( Class<? extends EventListener> p0 );
    <T extends EventListener> T createListener ( Class<T> p0 ) throws ServletException;
    JspConfigDescriptor getJspConfigDescriptor();
    ClassLoader getClassLoader();
    void declareRoles ( String... p0 );
    String getVirtualServerName();
}
