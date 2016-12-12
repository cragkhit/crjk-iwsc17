package org.apache.catalina.core;
import java.util.ArrayList;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.util.HashMap;
import java.security.Permission;
import org.apache.catalina.Globals;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionIdListener;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestListener;
import java.util.EventListener;
import java.util.Iterator;
import org.apache.catalina.connector.Connector;
import java.util.EnumSet;
import javax.servlet.ServletRegistration;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.ServletException;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.catalina.LifecycleState;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextAttributeEvent;
import org.apache.catalina.util.ServerInfo;
import java.io.InputStream;
import org.apache.catalina.WebResourceRoot;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.catalina.util.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.apache.tomcat.util.http.RequestUtil;
import javax.servlet.http.Mapping;
import org.apache.catalina.Wrapper;
import javax.servlet.RequestDispatcher;
import org.apache.catalina.Container;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.catalina.mapper.MappingData;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.catalina.Context;
import java.util.Collections;
import java.util.Collection;
import java.util.HashSet;
import java.util.Enumeration;
import org.apache.catalina.Engine;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.SessionTrackingMode;
import java.util.Set;
import javax.servlet.SessionCookieConfig;
import org.apache.tomcat.util.res.StringManager;
import javax.servlet.Servlet;
import java.util.List;
import org.apache.catalina.Service;
import java.util.Map;
import javax.servlet.ServletContext;
public class ApplicationContext implements ServletContext {
    protected static final boolean STRICT_SERVLET_COMPLIANCE;
    protected static final boolean GET_RESOURCE_REQUIRE_SLASH;
    protected Map<String, Object> attributes;
    private final Map<String, String> readOnlyAttributes;
    private final StandardContext context;
    private final Service service;
    private static final List<String> emptyString;
    private static final List<Servlet> emptyServlet;
    private final ServletContext facade;
    private final Map<String, String> parameters;
    private static final StringManager sm;
    private final ThreadLocal<DispatchData> dispatchData;
    private SessionCookieConfig sessionCookieConfig;
    private Set<SessionTrackingMode> sessionTrackingModes;
    private Set<SessionTrackingMode> defaultSessionTrackingModes;
    private Set<SessionTrackingMode> supportedSessionTrackingModes;
    private boolean newServletContextListenerAllowed;
    public ApplicationContext ( final StandardContext context ) {
        this.attributes = new ConcurrentHashMap<String, Object>();
        this.readOnlyAttributes = new ConcurrentHashMap<String, String>();
        this.facade = ( ServletContext ) new ApplicationContextFacade ( this );
        this.parameters = new ConcurrentHashMap<String, String>();
        this.dispatchData = new ThreadLocal<DispatchData>();
        this.sessionTrackingModes = null;
        this.defaultSessionTrackingModes = null;
        this.supportedSessionTrackingModes = null;
        this.newServletContextListenerAllowed = true;
        this.context = context;
        this.service = ( ( Engine ) context.getParent().getParent() ).getService();
        this.sessionCookieConfig = ( SessionCookieConfig ) new ApplicationSessionCookieConfig ( context );
        this.populateSessionTrackingModes();
    }
    public Object getAttribute ( final String name ) {
        return this.attributes.get ( name );
    }
    public Enumeration<String> getAttributeNames() {
        final Set<String> names = new HashSet<String>();
        names.addAll ( this.attributes.keySet() );
        return Collections.enumeration ( names );
    }
    public ServletContext getContext ( String uri ) {
        if ( uri == null || !uri.startsWith ( "/" ) ) {
            return null;
        }
        Context child = null;
        try {
            final Container host = this.context.getParent();
            child = ( Context ) host.findChild ( uri );
            if ( child != null && !child.getState().isAvailable() ) {
                child = null;
            }
            if ( child == null ) {
                final int i = uri.indexOf ( "##" );
                if ( i > -1 ) {
                    uri = uri.substring ( 0, i );
                }
                final MessageBytes hostMB = MessageBytes.newInstance();
                hostMB.setString ( host.getName() );
                final MessageBytes pathMB = MessageBytes.newInstance();
                pathMB.setString ( uri );
                final MappingData mappingData = new MappingData();
                ( ( Engine ) host.getParent() ).getService().getMapper().map ( hostMB, pathMB, null, mappingData );
                child = mappingData.context;
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            return null;
        }
        if ( child == null ) {
            return null;
        }
        if ( this.context.getCrossContext() ) {
            return child.getServletContext();
        }
        if ( child == this.context ) {
            return this.context.getServletContext();
        }
        return null;
    }
    public String getContextPath() {
        return this.context.getPath();
    }
    public String getInitParameter ( final String name ) {
        if ( "org.apache.jasper.XML_VALIDATE_TLD".equals ( name ) && this.context.getTldValidation() ) {
            return "true";
        }
        if ( "org.apache.jasper.XML_BLOCK_EXTERNAL".equals ( name ) && !this.context.getXmlBlockExternal() ) {
            return "false";
        }
        return this.parameters.get ( name );
    }
    public Enumeration<String> getInitParameterNames() {
        final Set<String> names = new HashSet<String>();
        names.addAll ( this.parameters.keySet() );
        if ( this.context.getTldValidation() ) {
            names.add ( "org.apache.jasper.XML_VALIDATE_TLD" );
        }
        if ( !this.context.getXmlBlockExternal() ) {
            names.add ( "org.apache.jasper.XML_BLOCK_EXTERNAL" );
        }
        return Collections.enumeration ( names );
    }
    public int getMajorVersion() {
        return 4;
    }
    public int getMinorVersion() {
        return 0;
    }
    public String getMimeType ( final String file ) {
        if ( file == null ) {
            return null;
        }
        final int period = file.lastIndexOf ( 46 );
        if ( period < 0 ) {
            return null;
        }
        final String extension = file.substring ( period + 1 );
        if ( extension.length() < 1 ) {
            return null;
        }
        return this.context.findMimeMapping ( extension );
    }
    public RequestDispatcher getNamedDispatcher ( final String name ) {
        if ( name == null ) {
            return null;
        }
        final Wrapper wrapper = ( Wrapper ) this.context.findChild ( name );
        if ( wrapper == null ) {
            return null;
        }
        return ( RequestDispatcher ) new ApplicationDispatcher ( wrapper, null, null, null, null, null, name );
    }
    public String getRealPath ( final String path ) {
        final String validatedPath = this.validateResourcePath ( path, true );
        return this.context.getRealPath ( validatedPath );
    }
    public RequestDispatcher getRequestDispatcher ( final String path ) {
        if ( path == null ) {
            return null;
        }
        if ( !path.startsWith ( "/" ) ) {
            throw new IllegalArgumentException ( ApplicationContext.sm.getString ( "applicationContext.requestDispatcher.iae", path ) );
        }
        String queryString = null;
        String normalizedPath = path;
        int pos = normalizedPath.indexOf ( 63 );
        if ( pos >= 0 ) {
            queryString = normalizedPath.substring ( pos + 1 );
            normalizedPath = normalizedPath.substring ( 0, pos );
        }
        normalizedPath = RequestUtil.normalize ( normalizedPath );
        if ( normalizedPath == null ) {
            return null;
        }
        if ( this.getContext().getDispatchersUseEncodedPaths() ) {
            String decodedPath;
            try {
                decodedPath = URLDecoder.decode ( normalizedPath, "UTF-8" );
            } catch ( UnsupportedEncodingException e2 ) {
                return null;
            }
            normalizedPath = RequestUtil.normalize ( decodedPath );
            if ( !decodedPath.equals ( normalizedPath ) ) {
                this.getContext().getLogger().warn ( ApplicationContext.sm.getString ( "applicationContext.illegalDispatchPath", path ), new IllegalArgumentException() );
                return null;
            }
        }
        pos = normalizedPath.length();
        DispatchData dd = this.dispatchData.get();
        if ( dd == null ) {
            dd = new DispatchData();
            this.dispatchData.set ( dd );
        }
        final MessageBytes uriMB = dd.uriMB;
        uriMB.recycle();
        final MappingData mappingData = dd.mappingData;
        final CharChunk uriCC = uriMB.getCharChunk();
        try {
            uriCC.append ( this.context.getPath(), 0, this.context.getPath().length() );
            int semicolon = normalizedPath.indexOf ( 59 );
            if ( pos >= 0 && semicolon > pos ) {
                semicolon = -1;
            }
            uriCC.append ( normalizedPath, 0, ( semicolon > 0 ) ? semicolon : pos );
            this.service.getMapper().map ( this.context, uriMB, mappingData );
            if ( mappingData.wrapper == null ) {
                return null;
            }
            if ( semicolon > 0 ) {
                uriCC.append ( normalizedPath, semicolon, pos - semicolon );
            }
        } catch ( Exception e ) {
            this.log ( ApplicationContext.sm.getString ( "applicationContext.mapping.error" ), e );
            return null;
        }
        final Wrapper wrapper = mappingData.wrapper;
        final String wrapperPath = mappingData.wrapperPath.toString();
        final String pathInfo = mappingData.pathInfo.toString();
        final Mapping mapping = new ApplicationMapping ( mappingData ).getMapping();
        mappingData.recycle();
        final String encodedUri = URLEncoder.DEFAULT.encode ( uriCC.toString(), "UTF-8" );
        return ( RequestDispatcher ) new ApplicationDispatcher ( wrapper, encodedUri, wrapperPath, pathInfo, queryString, mapping, null );
    }
    public URL getResource ( final String path ) throws MalformedURLException {
        final String validatedPath = this.validateResourcePath ( path, false );
        if ( validatedPath == null ) {
            throw new MalformedURLException ( ApplicationContext.sm.getString ( "applicationContext.requestDispatcher.iae", path ) );
        }
        final WebResourceRoot resources = this.context.getResources();
        if ( resources != null ) {
            return resources.getResource ( validatedPath ).getURL();
        }
        return null;
    }
    public InputStream getResourceAsStream ( final String path ) {
        final String validatedPath = this.validateResourcePath ( path, false );
        if ( validatedPath == null ) {
            return null;
        }
        final WebResourceRoot resources = this.context.getResources();
        if ( resources != null ) {
            return resources.getResource ( validatedPath ).getInputStream();
        }
        return null;
    }
    private String validateResourcePath ( final String path, final boolean allowEmptyPath ) {
        if ( path == null ) {
            return null;
        }
        if ( path.length() == 0 && allowEmptyPath ) {
            return path;
        }
        if ( path.startsWith ( "/" ) ) {
            return path;
        }
        if ( ApplicationContext.GET_RESOURCE_REQUIRE_SLASH ) {
            return null;
        }
        return "/" + path;
    }
    public Set<String> getResourcePaths ( final String path ) {
        if ( path == null ) {
            return null;
        }
        if ( !path.startsWith ( "/" ) ) {
            throw new IllegalArgumentException ( ApplicationContext.sm.getString ( "applicationContext.resourcePaths.iae", path ) );
        }
        final WebResourceRoot resources = this.context.getResources();
        if ( resources != null ) {
            return resources.listWebAppPaths ( path );
        }
        return null;
    }
    public String getServerInfo() {
        return ServerInfo.getServerInfo();
    }
    @Deprecated
    public Servlet getServlet ( final String name ) {
        return null;
    }
    public String getServletContextName() {
        return this.context.getDisplayName();
    }
    @Deprecated
    public Enumeration<String> getServletNames() {
        return Collections.enumeration ( ApplicationContext.emptyString );
    }
    @Deprecated
    public Enumeration<Servlet> getServlets() {
        return Collections.enumeration ( ApplicationContext.emptyServlet );
    }
    public void log ( final String message ) {
        this.context.getLogger().info ( message );
    }
    @Deprecated
    public void log ( final Exception exception, final String message ) {
        this.context.getLogger().error ( message, exception );
    }
    public void log ( final String message, final Throwable throwable ) {
        this.context.getLogger().error ( message, throwable );
    }
    public void removeAttribute ( final String name ) {
        Object value = null;
        if ( this.readOnlyAttributes.containsKey ( name ) ) {
            return;
        }
        value = this.attributes.remove ( name );
        if ( value == null ) {
            return;
        }
        final Object[] listeners = this.context.getApplicationEventListeners();
        if ( listeners == null || listeners.length == 0 ) {
            return;
        }
        final ServletContextAttributeEvent event = new ServletContextAttributeEvent ( this.context.getServletContext(), name, value );
        for ( int i = 0; i < listeners.length; ++i ) {
            if ( listeners[i] instanceof ServletContextAttributeListener ) {
                final ServletContextAttributeListener listener = ( ServletContextAttributeListener ) listeners[i];
                try {
                    this.context.fireContainerEvent ( "beforeContextAttributeRemoved", listener );
                    listener.attributeRemoved ( event );
                    this.context.fireContainerEvent ( "afterContextAttributeRemoved", listener );
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    this.context.fireContainerEvent ( "afterContextAttributeRemoved", listener );
                    this.log ( ApplicationContext.sm.getString ( "applicationContext.attributeEvent" ), t );
                }
            }
        }
    }
    public void setAttribute ( final String name, final Object value ) {
        if ( name == null ) {
            throw new IllegalArgumentException ( ApplicationContext.sm.getString ( "applicationContext.setAttribute.namenull" ) );
        }
        if ( value == null ) {
            this.removeAttribute ( name );
            return;
        }
        if ( this.readOnlyAttributes.containsKey ( name ) ) {
            return;
        }
        final Object oldValue = this.attributes.put ( name, value );
        final boolean replaced = oldValue != null;
        final Object[] listeners = this.context.getApplicationEventListeners();
        if ( listeners == null || listeners.length == 0 ) {
            return;
        }
        ServletContextAttributeEvent event = null;
        if ( replaced ) {
            event = new ServletContextAttributeEvent ( this.context.getServletContext(), name, oldValue );
        } else {
            event = new ServletContextAttributeEvent ( this.context.getServletContext(), name, value );
        }
        for ( int i = 0; i < listeners.length; ++i ) {
            if ( listeners[i] instanceof ServletContextAttributeListener ) {
                final ServletContextAttributeListener listener = ( ServletContextAttributeListener ) listeners[i];
                try {
                    if ( replaced ) {
                        this.context.fireContainerEvent ( "beforeContextAttributeReplaced", listener );
                        listener.attributeReplaced ( event );
                        this.context.fireContainerEvent ( "afterContextAttributeReplaced", listener );
                    } else {
                        this.context.fireContainerEvent ( "beforeContextAttributeAdded", listener );
                        listener.attributeAdded ( event );
                        this.context.fireContainerEvent ( "afterContextAttributeAdded", listener );
                    }
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    if ( replaced ) {
                        this.context.fireContainerEvent ( "afterContextAttributeReplaced", listener );
                    } else {
                        this.context.fireContainerEvent ( "afterContextAttributeAdded", listener );
                    }
                    this.log ( ApplicationContext.sm.getString ( "applicationContext.attributeEvent" ), t );
                }
            }
        }
    }
    public FilterRegistration.Dynamic addFilter ( final String filterName, final String className ) {
        return this.addFilter ( filterName, className, null );
    }
    public FilterRegistration.Dynamic addFilter ( final String filterName, final Filter filter ) {
        return this.addFilter ( filterName, null, filter );
    }
    public FilterRegistration.Dynamic addFilter ( final String filterName, final Class<? extends Filter> filterClass ) {
        return this.addFilter ( filterName, filterClass.getName(), null );
    }
    private FilterRegistration.Dynamic addFilter ( final String filterName, final String filterClass, final Filter filter ) throws IllegalStateException {
        if ( filterName == null || filterName.equals ( "" ) ) {
            throw new IllegalArgumentException ( ApplicationContext.sm.getString ( "applicationContext.invalidFilterName", filterName ) );
        }
        if ( !this.context.getState().equals ( LifecycleState.STARTING_PREP ) ) {
            throw new IllegalStateException ( ApplicationContext.sm.getString ( "applicationContext.addFilter.ise", this.getContextPath() ) );
        }
        FilterDef filterDef = this.context.findFilterDef ( filterName );
        if ( filterDef == null ) {
            filterDef = new FilterDef();
            filterDef.setFilterName ( filterName );
            this.context.addFilterDef ( filterDef );
        } else if ( filterDef.getFilterName() != null && filterDef.getFilterClass() != null ) {
            return null;
        }
        if ( filter == null ) {
            filterDef.setFilterClass ( filterClass );
        } else {
            filterDef.setFilterClass ( filter.getClass().getName() );
            filterDef.setFilter ( filter );
        }
        return ( FilterRegistration.Dynamic ) new ApplicationFilterRegistration ( filterDef, this.context );
    }
    public <T extends Filter> T createFilter ( final Class<T> c ) throws ServletException {
        try {
            final T filter = ( T ) this.context.getInstanceManager().newInstance ( c.getName() );
            return filter;
        } catch ( InvocationTargetException e ) {
            ExceptionUtils.handleThrowable ( e.getCause() );
            throw new ServletException ( ( Throwable ) e );
        } catch ( IllegalAccessException | NamingException | InstantiationException | ClassNotFoundException e2 ) {
            throw new ServletException ( ( Throwable ) e2 );
        }
    }
    public FilterRegistration getFilterRegistration ( final String filterName ) {
        final FilterDef filterDef = this.context.findFilterDef ( filterName );
        if ( filterDef == null ) {
            return null;
        }
        return ( FilterRegistration ) new ApplicationFilterRegistration ( filterDef, this.context );
    }
    public ServletRegistration.Dynamic addServlet ( final String servletName, final String className ) {
        return this.addServlet ( servletName, className, null );
    }
    public ServletRegistration.Dynamic addServlet ( final String servletName, final Servlet servlet ) {
        return this.addServlet ( servletName, null, servlet );
    }
    public ServletRegistration.Dynamic addServlet ( final String servletName, final Class<? extends Servlet> servletClass ) {
        return this.addServlet ( servletName, servletClass.getName(), null );
    }
    private ServletRegistration.Dynamic addServlet ( final String servletName, final String servletClass, final Servlet servlet ) throws IllegalStateException {
        if ( servletName == null || servletName.equals ( "" ) ) {
            throw new IllegalArgumentException ( ApplicationContext.sm.getString ( "applicationContext.invalidServletName", servletName ) );
        }
        if ( !this.context.getState().equals ( LifecycleState.STARTING_PREP ) ) {
            throw new IllegalStateException ( ApplicationContext.sm.getString ( "applicationContext.addServlet.ise", this.getContextPath() ) );
        }
        Wrapper wrapper = ( Wrapper ) this.context.findChild ( servletName );
        if ( wrapper == null ) {
            wrapper = this.context.createWrapper();
            wrapper.setName ( servletName );
            this.context.addChild ( wrapper );
        } else if ( wrapper.getName() != null && wrapper.getServletClass() != null ) {
            if ( !wrapper.isOverridable() ) {
                return null;
            }
            wrapper.setOverridable ( false );
        }
        if ( servlet == null ) {
            wrapper.setServletClass ( servletClass );
        } else {
            wrapper.setServletClass ( servlet.getClass().getName() );
            wrapper.setServlet ( servlet );
        }
        return this.context.dynamicServletAdded ( wrapper );
    }
    public <T extends Servlet> T createServlet ( final Class<T> c ) throws ServletException {
        try {
            final T servlet = ( T ) this.context.getInstanceManager().newInstance ( c.getName() );
            this.context.dynamicServletCreated ( servlet );
            return servlet;
        } catch ( InvocationTargetException e ) {
            ExceptionUtils.handleThrowable ( e.getCause() );
            throw new ServletException ( ( Throwable ) e );
        } catch ( IllegalAccessException | NamingException | InstantiationException | ClassNotFoundException e2 ) {
            throw new ServletException ( ( Throwable ) e2 );
        }
    }
    public ServletRegistration getServletRegistration ( final String servletName ) {
        final Wrapper wrapper = ( Wrapper ) this.context.findChild ( servletName );
        if ( wrapper == null ) {
            return null;
        }
        return ( ServletRegistration ) new ApplicationServletRegistration ( wrapper, this.context );
    }
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return this.defaultSessionTrackingModes;
    }
    private void populateSessionTrackingModes() {
        this.defaultSessionTrackingModes = EnumSet.of ( SessionTrackingMode.URL );
        this.supportedSessionTrackingModes = EnumSet.of ( SessionTrackingMode.URL );
        if ( this.context.getCookies() ) {
            this.defaultSessionTrackingModes.add ( SessionTrackingMode.COOKIE );
            this.supportedSessionTrackingModes.add ( SessionTrackingMode.COOKIE );
        }
        final Service s = ( ( Engine ) this.context.getParent().getParent() ).getService();
        final Connector[] connectors2;
        final Connector[] connectors = connectors2 = s.findConnectors();
        for ( final Connector connector : connectors2 ) {
            if ( Boolean.TRUE.equals ( connector.getAttribute ( "SSLEnabled" ) ) ) {
                this.supportedSessionTrackingModes.add ( SessionTrackingMode.SSL );
                break;
            }
        }
    }
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        if ( this.sessionTrackingModes != null ) {
            return this.sessionTrackingModes;
        }
        return this.defaultSessionTrackingModes;
    }
    public SessionCookieConfig getSessionCookieConfig() {
        return this.sessionCookieConfig;
    }
    public void setSessionTrackingModes ( final Set<SessionTrackingMode> sessionTrackingModes ) {
        if ( !this.context.getState().equals ( LifecycleState.STARTING_PREP ) ) {
            throw new IllegalStateException ( ApplicationContext.sm.getString ( "applicationContext.setSessionTracking.ise", this.getContextPath() ) );
        }
        for ( final SessionTrackingMode sessionTrackingMode : sessionTrackingModes ) {
            if ( !this.supportedSessionTrackingModes.contains ( sessionTrackingMode ) ) {
                throw new IllegalArgumentException ( ApplicationContext.sm.getString ( "applicationContext.setSessionTracking.iae.invalid", sessionTrackingMode.toString(), this.getContextPath() ) );
            }
        }
        if ( sessionTrackingModes.contains ( SessionTrackingMode.SSL ) && sessionTrackingModes.size() > 1 ) {
            throw new IllegalArgumentException ( ApplicationContext.sm.getString ( "applicationContext.setSessionTracking.iae.ssl", this.getContextPath() ) );
        }
        this.sessionTrackingModes = sessionTrackingModes;
    }
    public boolean setInitParameter ( final String name, final String value ) {
        if ( !this.context.getState().equals ( LifecycleState.STARTING_PREP ) ) {
            throw new IllegalStateException ( ApplicationContext.sm.getString ( "applicationContext.setInitParam.ise", this.getContextPath() ) );
        }
        return this.parameters.putIfAbsent ( name, value ) == null;
    }
    public void addListener ( final Class<? extends EventListener> listenerClass ) {
        EventListener listener;
        try {
            listener = this.createListener ( listenerClass );
        } catch ( ServletException e ) {
            throw new IllegalArgumentException ( ApplicationContext.sm.getString ( "applicationContext.addListener.iae.init", listenerClass.getName() ), ( Throwable ) e );
        }
        this.addListener ( listener );
    }
    public void addListener ( final String className ) {
        try {
            if ( this.context.getInstanceManager() != null ) {
                final Object obj = this.context.getInstanceManager().newInstance ( className );
                if ( ! ( obj instanceof EventListener ) ) {
                    throw new IllegalArgumentException ( ApplicationContext.sm.getString ( "applicationContext.addListener.iae.wrongType", className ) );
                }
                final EventListener listener = ( EventListener ) obj;
                this.addListener ( listener );
            }
        } catch ( InvocationTargetException e ) {
            ExceptionUtils.handleThrowable ( e.getCause() );
            throw new IllegalArgumentException ( ApplicationContext.sm.getString ( "applicationContext.addListener.iae.cnfe", className ), e );
        } catch ( IllegalAccessException | NamingException | InstantiationException | ClassNotFoundException e2 ) {
            throw new IllegalArgumentException ( ApplicationContext.sm.getString ( "applicationContext.addListener.iae.cnfe", className ), e2 );
        }
    }
    public <T extends EventListener> void addListener ( final T t ) {
        if ( !this.context.getState().equals ( LifecycleState.STARTING_PREP ) ) {
            throw new IllegalStateException ( ApplicationContext.sm.getString ( "applicationContext.addListener.ise", this.getContextPath() ) );
        }
        boolean match = false;
        if ( t instanceof ServletContextAttributeListener || t instanceof ServletRequestListener || t instanceof ServletRequestAttributeListener || t instanceof HttpSessionIdListener || t instanceof HttpSessionAttributeListener ) {
            this.context.addApplicationEventListener ( t );
            match = true;
        }
        if ( t instanceof HttpSessionListener || ( t instanceof ServletContextListener && this.newServletContextListenerAllowed ) ) {
            this.context.addApplicationLifecycleListener ( t );
            match = true;
        }
        if ( match ) {
            return;
        }
        if ( t instanceof ServletContextListener ) {
            throw new IllegalArgumentException ( ApplicationContext.sm.getString ( "applicationContext.addListener.iae.sclNotAllowed", t.getClass().getName() ) );
        }
        throw new IllegalArgumentException ( ApplicationContext.sm.getString ( "applicationContext.addListener.iae.wrongType", t.getClass().getName() ) );
    }
    public <T extends EventListener> T createListener ( final Class<T> c ) throws ServletException {
        throw new IllegalStateException ( "An error occurred while decompiling this method." );
    }
    public void declareRoles ( final String... roleNames ) {
        if ( !this.context.getState().equals ( LifecycleState.STARTING_PREP ) ) {
            throw new IllegalStateException ( ApplicationContext.sm.getString ( "applicationContext.addRole.ise", this.getContextPath() ) );
        }
        if ( roleNames == null ) {
            throw new IllegalArgumentException ( ApplicationContext.sm.getString ( "applicationContext.roles.iae", this.getContextPath() ) );
        }
        for ( final String role : roleNames ) {
            if ( role == null || "".equals ( role ) ) {
                throw new IllegalArgumentException ( ApplicationContext.sm.getString ( "applicationContext.role.iae", this.getContextPath() ) );
            }
            this.context.addSecurityRole ( role );
        }
    }
    public ClassLoader getClassLoader() {
        final ClassLoader result = this.context.getLoader().getClassLoader();
        if ( Globals.IS_SECURITY_ENABLED ) {
            ClassLoader tccl;
            ClassLoader parent;
            for ( tccl = Thread.currentThread().getContextClassLoader(), parent = result; parent != null && parent != tccl; parent = parent.getParent() ) {}
            if ( parent == null ) {
                System.getSecurityManager().checkPermission ( new RuntimePermission ( "getClassLoader" ) );
            }
        }
        return result;
    }
    public int getEffectiveMajorVersion() {
        return this.context.getEffectiveMajorVersion();
    }
    public int getEffectiveMinorVersion() {
        return this.context.getEffectiveMinorVersion();
    }
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        final Map<String, ApplicationFilterRegistration> result = new HashMap<String, ApplicationFilterRegistration>();
        final FilterDef[] filterDefs2;
        final FilterDef[] filterDefs = filterDefs2 = this.context.findFilterDefs();
        for ( final FilterDef filterDef : filterDefs2 ) {
            result.put ( filterDef.getFilterName(), new ApplicationFilterRegistration ( filterDef, this.context ) );
        }
        return ( Map<String, ? extends FilterRegistration> ) result;
    }
    public JspConfigDescriptor getJspConfigDescriptor() {
        return this.context.getJspConfigDescriptor();
    }
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        final Map<String, ApplicationServletRegistration> result = new HashMap<String, ApplicationServletRegistration>();
        final Container[] children;
        final Container[] wrappers = children = this.context.findChildren();
        for ( final Container wrapper : children ) {
            result.put ( ( ( Wrapper ) wrapper ).getName(), new ApplicationServletRegistration ( ( Wrapper ) wrapper, this.context ) );
        }
        return ( Map<String, ? extends ServletRegistration> ) result;
    }
    public String getVirtualServerName() {
        final Container host = this.context.getParent();
        final Container engine = host.getParent();
        return engine.getName() + "/" + host.getName();
    }
    protected StandardContext getContext() {
        return this.context;
    }
    protected void clearAttributes() {
        final ArrayList<String> list = new ArrayList<String>();
        final Iterator<String> iter = this.attributes.keySet().iterator();
        while ( iter.hasNext() ) {
            list.add ( iter.next() );
        }
        for ( final String key : list ) {
            this.removeAttribute ( key );
        }
    }
    protected ServletContext getFacade() {
        return this.facade;
    }
    void setAttributeReadOnly ( final String name ) {
        if ( this.attributes.containsKey ( name ) ) {
            this.readOnlyAttributes.put ( name, name );
        }
    }
    protected void setNewServletContextListenerAllowed ( final boolean allowed ) {
        this.newServletContextListenerAllowed = allowed;
    }
    static {
        STRICT_SERVLET_COMPLIANCE = Globals.STRICT_SERVLET_COMPLIANCE;
        final String requireSlash = System.getProperty ( "org.apache.catalina.core.ApplicationContext.GET_RESOURCE_REQUIRE_SLASH" );
        if ( requireSlash == null ) {
            GET_RESOURCE_REQUIRE_SLASH = ApplicationContext.STRICT_SERVLET_COMPLIANCE;
        } else {
            GET_RESOURCE_REQUIRE_SLASH = Boolean.parseBoolean ( requireSlash );
        }
        emptyString = Collections.emptyList();
        emptyServlet = Collections.emptyList();
        sm = StringManager.getManager ( "org.apache.catalina.core" );
    }
    private static final class DispatchData {
        public MessageBytes uriMB;
        public MappingData mappingData;
        public DispatchData() {
            this.uriMB = MessageBytes.newInstance();
            final CharChunk uriCC = this.uriMB.getCharChunk();
            uriCC.setLimit ( -1 );
            this.mappingData = new MappingData();
        }
    }
}
