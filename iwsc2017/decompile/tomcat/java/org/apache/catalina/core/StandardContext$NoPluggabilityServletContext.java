package org.apache.catalina.core;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.util.EventListener;
import javax.servlet.SessionTrackingMode;
import javax.servlet.SessionCookieConfig;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import java.util.Map;
import javax.servlet.ServletRegistration;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.Servlet;
import javax.servlet.RequestDispatcher;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import javax.servlet.ServletContext;
private static class NoPluggabilityServletContext implements ServletContext {
    private final ServletContext sc;
    public NoPluggabilityServletContext ( final ServletContext sc ) {
        this.sc = sc;
    }
    public String getContextPath() {
        return this.sc.getContextPath();
    }
    public ServletContext getContext ( final String uripath ) {
        return this.sc.getContext ( uripath );
    }
    public int getMajorVersion() {
        return this.sc.getMajorVersion();
    }
    public int getMinorVersion() {
        return this.sc.getMinorVersion();
    }
    public int getEffectiveMajorVersion() {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public int getEffectiveMinorVersion() {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public String getMimeType ( final String file ) {
        return this.sc.getMimeType ( file );
    }
    public Set<String> getResourcePaths ( final String path ) {
        return ( Set<String> ) this.sc.getResourcePaths ( path );
    }
    public URL getResource ( final String path ) throws MalformedURLException {
        return this.sc.getResource ( path );
    }
    public InputStream getResourceAsStream ( final String path ) {
        return this.sc.getResourceAsStream ( path );
    }
    public RequestDispatcher getRequestDispatcher ( final String path ) {
        return this.sc.getRequestDispatcher ( path );
    }
    public RequestDispatcher getNamedDispatcher ( final String name ) {
        return this.sc.getNamedDispatcher ( name );
    }
    @Deprecated
    public Servlet getServlet ( final String name ) throws ServletException {
        return this.sc.getServlet ( name );
    }
    @Deprecated
    public Enumeration<Servlet> getServlets() {
        return ( Enumeration<Servlet> ) this.sc.getServlets();
    }
    @Deprecated
    public Enumeration<String> getServletNames() {
        return ( Enumeration<String> ) this.sc.getServletNames();
    }
    public void log ( final String msg ) {
        this.sc.log ( msg );
    }
    @Deprecated
    public void log ( final Exception exception, final String msg ) {
        this.sc.log ( exception, msg );
    }
    public void log ( final String message, final Throwable throwable ) {
        this.sc.log ( message, throwable );
    }
    public String getRealPath ( final String path ) {
        return this.sc.getRealPath ( path );
    }
    public String getServerInfo() {
        return this.sc.getServerInfo();
    }
    public String getInitParameter ( final String name ) {
        return this.sc.getInitParameter ( name );
    }
    public Enumeration<String> getInitParameterNames() {
        return ( Enumeration<String> ) this.sc.getInitParameterNames();
    }
    public boolean setInitParameter ( final String name, final String value ) {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public Object getAttribute ( final String name ) {
        return this.sc.getAttribute ( name );
    }
    public Enumeration<String> getAttributeNames() {
        return ( Enumeration<String> ) this.sc.getAttributeNames();
    }
    public void setAttribute ( final String name, final Object object ) {
        this.sc.setAttribute ( name, object );
    }
    public void removeAttribute ( final String name ) {
        this.sc.removeAttribute ( name );
    }
    public String getServletContextName() {
        return this.sc.getServletContextName();
    }
    public ServletRegistration.Dynamic addServlet ( final String servletName, final String className ) {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public ServletRegistration.Dynamic addServlet ( final String servletName, final Servlet servlet ) {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public ServletRegistration.Dynamic addServlet ( final String servletName, final Class<? extends Servlet> servletClass ) {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public <T extends Servlet> T createServlet ( final Class<T> c ) throws ServletException {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public ServletRegistration getServletRegistration ( final String servletName ) {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public FilterRegistration.Dynamic addFilter ( final String filterName, final String className ) {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public FilterRegistration.Dynamic addFilter ( final String filterName, final Filter filter ) {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public FilterRegistration.Dynamic addFilter ( final String filterName, final Class<? extends Filter> filterClass ) {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public <T extends Filter> T createFilter ( final Class<T> c ) throws ServletException {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public FilterRegistration getFilterRegistration ( final String filterName ) {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public SessionCookieConfig getSessionCookieConfig() {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public void setSessionTrackingModes ( final Set<SessionTrackingMode> sessionTrackingModes ) {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public void addListener ( final String className ) {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public <T extends EventListener> void addListener ( final T t ) {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public void addListener ( final Class<? extends EventListener> listenerClass ) {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public <T extends EventListener> T createListener ( final Class<T> c ) throws ServletException {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public JspConfigDescriptor getJspConfigDescriptor() {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public ClassLoader getClassLoader() {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public void declareRoles ( final String... roleNames ) {
        throw new UnsupportedOperationException ( ContainerBase.sm.getString ( "noPluggabilityServletContext.notAllowed" ) );
    }
    public String getVirtualServerName() {
        return this.sc.getVirtualServerName();
    }
}
