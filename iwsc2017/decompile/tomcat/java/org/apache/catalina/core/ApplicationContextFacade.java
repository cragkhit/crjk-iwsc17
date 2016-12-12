package org.apache.catalina.core;
import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.AccessController;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.util.EventListener;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.ServletRegistration;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.Servlet;
import javax.servlet.RequestDispatcher;
import java.io.InputStream;
import java.net.MalformedURLException;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.catalina.Globals;
import java.net.URL;
import org.apache.catalina.security.SecurityUtil;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.util.Map;
import javax.servlet.ServletContext;
public class ApplicationContextFacade implements ServletContext {
    private final Map<String, Class<?>[]> classCache;
    private final Map<String, Method> objectCache;
    private final ApplicationContext context;
    public ApplicationContextFacade ( final ApplicationContext context ) {
        this.context = context;
        this.classCache = new HashMap<String, Class<?>[]>();
        this.objectCache = new ConcurrentHashMap<String, Method>();
        this.initClassCache();
    }
    private void initClassCache() {
        final Class<?>[] clazz = ( Class<?>[] ) new Class[] { String.class };
        this.classCache.put ( "getContext", clazz );
        this.classCache.put ( "getMimeType", clazz );
        this.classCache.put ( "getResourcePaths", clazz );
        this.classCache.put ( "getResource", clazz );
        this.classCache.put ( "getResourceAsStream", clazz );
        this.classCache.put ( "getRequestDispatcher", clazz );
        this.classCache.put ( "getNamedDispatcher", clazz );
        this.classCache.put ( "getServlet", clazz );
        this.classCache.put ( "setInitParameter", new Class[] { String.class, String.class } );
        this.classCache.put ( "createServlet", new Class[] { Class.class } );
        this.classCache.put ( "addServlet", new Class[] { String.class, String.class } );
        this.classCache.put ( "createFilter", new Class[] { Class.class } );
        this.classCache.put ( "addFilter", new Class[] { String.class, String.class } );
        this.classCache.put ( "createListener", new Class[] { Class.class } );
        this.classCache.put ( "addListener", clazz );
        this.classCache.put ( "getFilterRegistration", clazz );
        this.classCache.put ( "getServletRegistration", clazz );
        this.classCache.put ( "getInitParameter", clazz );
        this.classCache.put ( "setAttribute", new Class[] { String.class, Object.class } );
        this.classCache.put ( "removeAttribute", clazz );
        this.classCache.put ( "getRealPath", clazz );
        this.classCache.put ( "getAttribute", clazz );
        this.classCache.put ( "log", clazz );
        this.classCache.put ( "setSessionTrackingModes", new Class[] { Set.class } );
    }
    public ServletContext getContext ( final String uripath ) {
        ServletContext theContext = null;
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            theContext = ( ServletContext ) this.doPrivileged ( "getContext", new Object[] { uripath } );
        } else {
            theContext = this.context.getContext ( uripath );
        }
        if ( theContext != null && theContext instanceof ApplicationContext ) {
            theContext = ( ( ApplicationContext ) theContext ).getFacade();
        }
        return theContext;
    }
    public int getMajorVersion() {
        return this.context.getMajorVersion();
    }
    public int getMinorVersion() {
        return this.context.getMinorVersion();
    }
    public String getMimeType ( final String file ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( String ) this.doPrivileged ( "getMimeType", new Object[] { file } );
        }
        return this.context.getMimeType ( file );
    }
    public Set<String> getResourcePaths ( final String path ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( Set<String> ) this.doPrivileged ( "getResourcePaths", new Object[] { path } );
        }
        return this.context.getResourcePaths ( path );
    }
    public URL getResource ( final String path ) throws MalformedURLException {
        if ( Globals.IS_SECURITY_ENABLED ) {
            try {
                return ( URL ) this.invokeMethod ( this.context, "getResource", new Object[] { path } );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                if ( t instanceof MalformedURLException ) {
                    throw ( MalformedURLException ) t;
                }
                return null;
            }
        }
        return this.context.getResource ( path );
    }
    public InputStream getResourceAsStream ( final String path ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( InputStream ) this.doPrivileged ( "getResourceAsStream", new Object[] { path } );
        }
        return this.context.getResourceAsStream ( path );
    }
    public RequestDispatcher getRequestDispatcher ( final String path ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( RequestDispatcher ) this.doPrivileged ( "getRequestDispatcher", new Object[] { path } );
        }
        return this.context.getRequestDispatcher ( path );
    }
    public RequestDispatcher getNamedDispatcher ( final String name ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( RequestDispatcher ) this.doPrivileged ( "getNamedDispatcher", new Object[] { name } );
        }
        return this.context.getNamedDispatcher ( name );
    }
    @Deprecated
    public Servlet getServlet ( final String name ) throws ServletException {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                return ( Servlet ) this.invokeMethod ( this.context, "getServlet", new Object[] { name } );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                if ( t instanceof ServletException ) {
                    throw ( ServletException ) t;
                }
                return null;
            }
        }
        return this.context.getServlet ( name );
    }
    @Deprecated
    public Enumeration<Servlet> getServlets() {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( Enumeration<Servlet> ) this.doPrivileged ( "getServlets", null );
        }
        return this.context.getServlets();
    }
    @Deprecated
    public Enumeration<String> getServletNames() {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( Enumeration<String> ) this.doPrivileged ( "getServletNames", null );
        }
        return this.context.getServletNames();
    }
    public void log ( final String msg ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            this.doPrivileged ( "log", new Object[] { msg } );
        } else {
            this.context.log ( msg );
        }
    }
    @Deprecated
    public void log ( final Exception exception, final String msg ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            this.doPrivileged ( "log", new Class[] { Exception.class, String.class }, new Object[] { exception, msg } );
        } else {
            this.context.log ( exception, msg );
        }
    }
    public void log ( final String message, final Throwable throwable ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            this.doPrivileged ( "log", new Class[] { String.class, Throwable.class }, new Object[] { message, throwable } );
        } else {
            this.context.log ( message, throwable );
        }
    }
    public String getRealPath ( final String path ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( String ) this.doPrivileged ( "getRealPath", new Object[] { path } );
        }
        return this.context.getRealPath ( path );
    }
    public String getServerInfo() {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( String ) this.doPrivileged ( "getServerInfo", null );
        }
        return this.context.getServerInfo();
    }
    public String getInitParameter ( final String name ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( String ) this.doPrivileged ( "getInitParameter", new Object[] { name } );
        }
        return this.context.getInitParameter ( name );
    }
    public Enumeration<String> getInitParameterNames() {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( Enumeration<String> ) this.doPrivileged ( "getInitParameterNames", null );
        }
        return this.context.getInitParameterNames();
    }
    public Object getAttribute ( final String name ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return this.doPrivileged ( "getAttribute", new Object[] { name } );
        }
        return this.context.getAttribute ( name );
    }
    public Enumeration<String> getAttributeNames() {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( Enumeration<String> ) this.doPrivileged ( "getAttributeNames", null );
        }
        return this.context.getAttributeNames();
    }
    public void setAttribute ( final String name, final Object object ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            this.doPrivileged ( "setAttribute", new Object[] { name, object } );
        } else {
            this.context.setAttribute ( name, object );
        }
    }
    public void removeAttribute ( final String name ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            this.doPrivileged ( "removeAttribute", new Object[] { name } );
        } else {
            this.context.removeAttribute ( name );
        }
    }
    public String getServletContextName() {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( String ) this.doPrivileged ( "getServletContextName", null );
        }
        return this.context.getServletContextName();
    }
    public String getContextPath() {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( String ) this.doPrivileged ( "getContextPath", null );
        }
        return this.context.getContextPath();
    }
    public FilterRegistration.Dynamic addFilter ( final String filterName, final String className ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( FilterRegistration.Dynamic ) this.doPrivileged ( "addFilter", new Object[] { filterName, className } );
        }
        return this.context.addFilter ( filterName, className );
    }
    public FilterRegistration.Dynamic addFilter ( final String filterName, final Filter filter ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( FilterRegistration.Dynamic ) this.doPrivileged ( "addFilter", new Class[] { String.class, Filter.class }, new Object[] { filterName, filter } );
        }
        return this.context.addFilter ( filterName, filter );
    }
    public FilterRegistration.Dynamic addFilter ( final String filterName, final Class<? extends Filter> filterClass ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( FilterRegistration.Dynamic ) this.doPrivileged ( "addFilter", new Class[] { String.class, Class.class }, new Object[] { filterName, filterClass } );
        }
        return this.context.addFilter ( filterName, filterClass );
    }
    public <T extends Filter> T createFilter ( final Class<T> c ) throws ServletException {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                return ( T ) this.invokeMethod ( this.context, "createFilter", new Object[] { c } );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                if ( t instanceof ServletException ) {
                    throw ( ServletException ) t;
                }
                return null;
            }
        }
        return this.context.createFilter ( c );
    }
    public FilterRegistration getFilterRegistration ( final String filterName ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( FilterRegistration ) this.doPrivileged ( "getFilterRegistration", new Object[] { filterName } );
        }
        return this.context.getFilterRegistration ( filterName );
    }
    public ServletRegistration.Dynamic addServlet ( final String servletName, final String className ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( ServletRegistration.Dynamic ) this.doPrivileged ( "addServlet", new Object[] { servletName, className } );
        }
        return this.context.addServlet ( servletName, className );
    }
    public ServletRegistration.Dynamic addServlet ( final String servletName, final Servlet servlet ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( ServletRegistration.Dynamic ) this.doPrivileged ( "addServlet", new Class[] { String.class, Servlet.class }, new Object[] { servletName, servlet } );
        }
        return this.context.addServlet ( servletName, servlet );
    }
    public ServletRegistration.Dynamic addServlet ( final String servletName, final Class<? extends Servlet> servletClass ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( ServletRegistration.Dynamic ) this.doPrivileged ( "addServlet", new Class[] { String.class, Class.class }, new Object[] { servletName, servletClass } );
        }
        return this.context.addServlet ( servletName, servletClass );
    }
    public <T extends Servlet> T createServlet ( final Class<T> c ) throws ServletException {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                return ( T ) this.invokeMethod ( this.context, "createServlet", new Object[] { c } );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                if ( t instanceof ServletException ) {
                    throw ( ServletException ) t;
                }
                return null;
            }
        }
        return this.context.createServlet ( c );
    }
    public ServletRegistration getServletRegistration ( final String servletName ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( ServletRegistration ) this.doPrivileged ( "getServletRegistration", new Object[] { servletName } );
        }
        return this.context.getServletRegistration ( servletName );
    }
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( Set<SessionTrackingMode> ) this.doPrivileged ( "getDefaultSessionTrackingModes", null );
        }
        return this.context.getDefaultSessionTrackingModes();
    }
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( Set<SessionTrackingMode> ) this.doPrivileged ( "getEffectiveSessionTrackingModes", null );
        }
        return this.context.getEffectiveSessionTrackingModes();
    }
    public SessionCookieConfig getSessionCookieConfig() {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( SessionCookieConfig ) this.doPrivileged ( "getSessionCookieConfig", null );
        }
        return this.context.getSessionCookieConfig();
    }
    public void setSessionTrackingModes ( final Set<SessionTrackingMode> sessionTrackingModes ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            this.doPrivileged ( "setSessionTrackingModes", new Object[] { sessionTrackingModes } );
        } else {
            this.context.setSessionTrackingModes ( sessionTrackingModes );
        }
    }
    public boolean setInitParameter ( final String name, final String value ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( boolean ) this.doPrivileged ( "setInitParameter", new Object[] { name, value } );
        }
        return this.context.setInitParameter ( name, value );
    }
    public void addListener ( final Class<? extends EventListener> listenerClass ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            this.doPrivileged ( "addListener", new Class[] { Class.class }, new Object[] { listenerClass } );
        } else {
            this.context.addListener ( listenerClass );
        }
    }
    public void addListener ( final String className ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            this.doPrivileged ( "addListener", new Object[] { className } );
        } else {
            this.context.addListener ( className );
        }
    }
    public <T extends EventListener> void addListener ( final T t ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            this.doPrivileged ( "addListener", new Class[] { EventListener.class }, new Object[] { t } );
        } else {
            this.context.addListener ( t );
        }
    }
    public <T extends EventListener> T createListener ( final Class<T> c ) throws ServletException {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            try {
                return ( T ) this.invokeMethod ( this.context, "createListener", new Object[] { c } );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                if ( t instanceof ServletException ) {
                    throw ( ServletException ) t;
                }
                return null;
            }
        }
        return this.context.createListener ( c );
    }
    public void declareRoles ( final String... roleNames ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            this.doPrivileged ( "declareRoles", new Object[] { roleNames } );
        } else {
            this.context.declareRoles ( roleNames );
        }
    }
    public ClassLoader getClassLoader() {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( ClassLoader ) this.doPrivileged ( "getClassLoader", null );
        }
        return this.context.getClassLoader();
    }
    public int getEffectiveMajorVersion() {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( int ) this.doPrivileged ( "getEffectiveMajorVersion", null );
        }
        return this.context.getEffectiveMajorVersion();
    }
    public int getEffectiveMinorVersion() {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( int ) this.doPrivileged ( "getEffectiveMinorVersion", null );
        }
        return this.context.getEffectiveMinorVersion();
    }
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( Map<String, ? extends FilterRegistration> ) this.doPrivileged ( "getFilterRegistrations", null );
        }
        return this.context.getFilterRegistrations();
    }
    public JspConfigDescriptor getJspConfigDescriptor() {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( JspConfigDescriptor ) this.doPrivileged ( "getJspConfigDescriptor", null );
        }
        return this.context.getJspConfigDescriptor();
    }
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( Map<String, ? extends ServletRegistration> ) this.doPrivileged ( "getServletRegistrations", null );
        }
        return this.context.getServletRegistrations();
    }
    public String getVirtualServerName() {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return ( String ) this.doPrivileged ( "getVirtualServerName", null );
        }
        return this.context.getVirtualServerName();
    }
    private Object doPrivileged ( final String methodName, final Object[] params ) {
        try {
            return this.invokeMethod ( this.context, methodName, params );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            throw new RuntimeException ( t.getMessage(), t );
        }
    }
    private Object invokeMethod ( final ApplicationContext appContext, final String methodName, Object[] params ) throws Throwable {
        try {
            Method method = this.objectCache.get ( methodName );
            if ( method == null ) {
                method = appContext.getClass().getMethod ( methodName, ( Class<?>[] ) this.classCache.get ( methodName ) );
                this.objectCache.put ( methodName, method );
            }
            return this.executeMethod ( method, appContext, params );
        } catch ( Exception ex ) {
            this.handleException ( ex );
            return null;
        } finally {
            params = null;
        }
    }
    private Object doPrivileged ( final String methodName, final Class<?>[] clazz, Object[] params ) {
        try {
            final Method method = this.context.getClass().getMethod ( methodName, clazz );
            return this.executeMethod ( method, this.context, params );
        } catch ( Exception ex ) {
            try {
                this.handleException ( ex );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                throw new RuntimeException ( t.getMessage() );
            }
            return null;
        } finally {
            params = null;
        }
    }
    private Object executeMethod ( final Method method, final ApplicationContext context, final Object[] params ) throws PrivilegedActionException, IllegalAccessException, InvocationTargetException {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return AccessController.doPrivileged ( ( PrivilegedExceptionAction<Object> ) new PrivilegedExecuteMethod ( method, context, params ) );
        }
        return method.invoke ( context, params );
    }
    private void handleException ( Exception ex ) throws Throwable {
        if ( ex instanceof PrivilegedActionException ) {
            ex = ( ( PrivilegedActionException ) ex ).getException();
        }
        Throwable realException;
        if ( ex instanceof InvocationTargetException ) {
            realException = ex.getCause();
            if ( realException == null ) {
                realException = ex;
            }
        } else {
            realException = ex;
        }
        throw realException;
    }
    private static class PrivilegedExecuteMethod implements PrivilegedExceptionAction<Object> {
        private final Method method;
        private final ApplicationContext context;
        private final Object[] params;
        public PrivilegedExecuteMethod ( final Method method, final ApplicationContext context, final Object[] params ) {
            this.method = method;
            this.context = context;
            this.params = params;
        }
        @Override
        public Object run() throws Exception {
            return this.method.invoke ( this.context, this.params );
        }
    }
}
