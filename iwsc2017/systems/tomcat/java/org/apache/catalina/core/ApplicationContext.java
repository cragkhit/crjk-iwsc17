package org.apache.catalina.core;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestListener;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionIdListener;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.Mapping;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Service;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.mapper.MappingData;
import org.apache.catalina.util.ServerInfo;
import org.apache.catalina.util.URLEncoder;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.http.RequestUtil;
import org.apache.tomcat.util.res.StringManager;
public class ApplicationContext implements ServletContext {
    protected static final boolean STRICT_SERVLET_COMPLIANCE;
    protected static final boolean GET_RESOURCE_REQUIRE_SLASH;
    static {
        STRICT_SERVLET_COMPLIANCE = Globals.STRICT_SERVLET_COMPLIANCE;
        String requireSlash = System.getProperty (
                                  "org.apache.catalina.core.ApplicationContext.GET_RESOURCE_REQUIRE_SLASH" );
        if ( requireSlash == null ) {
            GET_RESOURCE_REQUIRE_SLASH = STRICT_SERVLET_COMPLIANCE;
        } else {
            GET_RESOURCE_REQUIRE_SLASH = Boolean.parseBoolean ( requireSlash );
        }
    }
    public ApplicationContext ( StandardContext context ) {
        super();
        this.context = context;
        this.service = ( ( Engine ) context.getParent().getParent() ).getService();
        this.sessionCookieConfig = new ApplicationSessionCookieConfig ( context );
        populateSessionTrackingModes();
    }
    protected Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final Map<String, String> readOnlyAttributes = new ConcurrentHashMap<>();
    private final StandardContext context;
    private final Service service;
    private static final List<String> emptyString = Collections.emptyList();
    private static final List<Servlet> emptyServlet = Collections.emptyList();
    private final ServletContext facade = new ApplicationContextFacade ( this );
    private final Map<String, String> parameters = new ConcurrentHashMap<>();
    private static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    private final ThreadLocal<DispatchData> dispatchData = new ThreadLocal<>();
    private SessionCookieConfig sessionCookieConfig;
    private Set<SessionTrackingMode> sessionTrackingModes = null;
    private Set<SessionTrackingMode> defaultSessionTrackingModes = null;
    private Set<SessionTrackingMode> supportedSessionTrackingModes = null;
    private boolean newServletContextListenerAllowed = true;
    @Override
    public Object getAttribute ( String name ) {
        return ( attributes.get ( name ) );
    }
    @Override
    public Enumeration<String> getAttributeNames() {
        Set<String> names = new HashSet<>();
        names.addAll ( attributes.keySet() );
        return Collections.enumeration ( names );
    }
    @Override
    public ServletContext getContext ( String uri ) {
        if ( uri == null || !uri.startsWith ( "/" ) ) {
            return null;
        }
        Context child = null;
        try {
            Container host = context.getParent();
            child = ( Context ) host.findChild ( uri );
            if ( child != null && !child.getState().isAvailable() ) {
                child = null;
            }
            if ( child == null ) {
                int i = uri.indexOf ( "##" );
                if ( i > -1 ) {
                    uri = uri.substring ( 0, i );
                }
                MessageBytes hostMB = MessageBytes.newInstance();
                hostMB.setString ( host.getName() );
                MessageBytes pathMB = MessageBytes.newInstance();
                pathMB.setString ( uri );
                MappingData mappingData = new MappingData();
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
        if ( context.getCrossContext() ) {
            return child.getServletContext();
        } else if ( child == context ) {
            return context.getServletContext();
        } else {
            return null;
        }
    }
    @Override
    public String getContextPath() {
        return context.getPath();
    }
    @Override
    public String getInitParameter ( final String name ) {
        if ( Globals.JASPER_XML_VALIDATION_TLD_INIT_PARAM.equals ( name ) &&
                context.getTldValidation() ) {
            return "true";
        }
        if ( Globals.JASPER_XML_BLOCK_EXTERNAL_INIT_PARAM.equals ( name ) ) {
            if ( !context.getXmlBlockExternal() ) {
                return "false";
            }
        }
        return parameters.get ( name );
    }
    @Override
    public Enumeration<String> getInitParameterNames() {
        Set<String> names = new HashSet<>();
        names.addAll ( parameters.keySet() );
        if ( context.getTldValidation() ) {
            names.add ( Globals.JASPER_XML_VALIDATION_TLD_INIT_PARAM );
        }
        if ( !context.getXmlBlockExternal() ) {
            names.add ( Globals.JASPER_XML_BLOCK_EXTERNAL_INIT_PARAM );
        }
        return Collections.enumeration ( names );
    }
    @Override
    public int getMajorVersion() {
        return Constants.MAJOR_VERSION;
    }
    @Override
    public int getMinorVersion() {
        return Constants.MINOR_VERSION;
    }
    @Override
    public String getMimeType ( String file ) {
        if ( file == null ) {
            return ( null );
        }
        int period = file.lastIndexOf ( '.' );
        if ( period < 0 ) {
            return ( null );
        }
        String extension = file.substring ( period + 1 );
        if ( extension.length() < 1 ) {
            return ( null );
        }
        return ( context.findMimeMapping ( extension ) );
    }
    @Override
    public RequestDispatcher getNamedDispatcher ( String name ) {
        if ( name == null ) {
            return ( null );
        }
        Wrapper wrapper = ( Wrapper ) context.findChild ( name );
        if ( wrapper == null ) {
            return ( null );
        }
        return new ApplicationDispatcher ( wrapper, null, null, null, null, null, name );
    }
    @Override
    public String getRealPath ( String path ) {
        String validatedPath = validateResourcePath ( path, true );
        return context.getRealPath ( validatedPath );
    }
    @Override
    public RequestDispatcher getRequestDispatcher ( String path ) {
        if ( path == null ) {
            return ( null );
        }
        if ( !path.startsWith ( "/" ) )
            throw new IllegalArgumentException
            ( sm.getString
              ( "applicationContext.requestDispatcher.iae", path ) );
        String queryString = null;
        String normalizedPath = path;
        int pos = normalizedPath.indexOf ( '?' );
        if ( pos >= 0 ) {
            queryString = normalizedPath.substring ( pos + 1 );
            normalizedPath = normalizedPath.substring ( 0, pos );
        }
        normalizedPath = RequestUtil.normalize ( normalizedPath );
        if ( normalizedPath == null ) {
            return ( null );
        }
        if ( getContext().getDispatchersUseEncodedPaths() ) {
            String decodedPath;
            try {
                decodedPath = URLDecoder.decode ( normalizedPath, "UTF-8" );
            } catch ( UnsupportedEncodingException e ) {
                return null;
            }
            normalizedPath = RequestUtil.normalize ( decodedPath );
            if ( !decodedPath.equals ( normalizedPath ) ) {
                getContext().getLogger().warn (
                    sm.getString ( "applicationContext.illegalDispatchPath", path ),
                    new IllegalArgumentException() );
                return null;
            }
        }
        pos = normalizedPath.length();
        DispatchData dd = dispatchData.get();
        if ( dd == null ) {
            dd = new DispatchData();
            dispatchData.set ( dd );
        }
        MessageBytes uriMB = dd.uriMB;
        uriMB.recycle();
        MappingData mappingData = dd.mappingData;
        CharChunk uriCC = uriMB.getCharChunk();
        try {
            uriCC.append ( context.getPath(), 0, context.getPath().length() );
            int semicolon = normalizedPath.indexOf ( ';' );
            if ( pos >= 0 && semicolon > pos ) {
                semicolon = -1;
            }
            uriCC.append ( normalizedPath, 0, semicolon > 0 ? semicolon : pos );
            service.getMapper().map ( context, uriMB, mappingData );
            if ( mappingData.wrapper == null ) {
                return ( null );
            }
            if ( semicolon > 0 ) {
                uriCC.append ( normalizedPath, semicolon, pos - semicolon );
            }
        } catch ( Exception e ) {
            log ( sm.getString ( "applicationContext.mapping.error" ), e );
            return ( null );
        }
        Wrapper wrapper = mappingData.wrapper;
        String wrapperPath = mappingData.wrapperPath.toString();
        String pathInfo = mappingData.pathInfo.toString();
        Mapping mapping = ( new ApplicationMapping ( mappingData ) ).getMapping();
        mappingData.recycle();
        String encodedUri = URLEncoder.DEFAULT.encode ( uriCC.toString(), "UTF-8" );
        return new ApplicationDispatcher ( wrapper, encodedUri, wrapperPath, pathInfo,
                                           queryString, mapping, null );
    }
    @Override
    public URL getResource ( String path ) throws MalformedURLException {
        String validatedPath = validateResourcePath ( path, false );
        if ( validatedPath == null ) {
            throw new MalformedURLException (
                sm.getString ( "applicationContext.requestDispatcher.iae", path ) );
        }
        WebResourceRoot resources = context.getResources();
        if ( resources != null ) {
            return resources.getResource ( validatedPath ).getURL();
        }
        return null;
    }
    @Override
    public InputStream getResourceAsStream ( String path ) {
        String validatedPath = validateResourcePath ( path, false );
        if ( validatedPath == null ) {
            return null;
        }
        WebResourceRoot resources = context.getResources();
        if ( resources != null ) {
            return resources.getResource ( validatedPath ).getInputStream();
        }
        return null;
    }
    private String validateResourcePath ( String path, boolean allowEmptyPath ) {
        if ( path == null ) {
            return null;
        }
        if ( path.length() == 0 && allowEmptyPath ) {
            return path;
        }
        if ( !path.startsWith ( "/" ) ) {
            if ( GET_RESOURCE_REQUIRE_SLASH ) {
                return null;
            } else {
                return "/" + path;
            }
        }
        return path;
    }
    @Override
    public Set<String> getResourcePaths ( String path ) {
        if ( path == null ) {
            return null;
        }
        if ( !path.startsWith ( "/" ) ) {
            throw new IllegalArgumentException
            ( sm.getString ( "applicationContext.resourcePaths.iae", path ) );
        }
        WebResourceRoot resources = context.getResources();
        if ( resources != null ) {
            return resources.listWebAppPaths ( path );
        }
        return null;
    }
    @Override
    public String getServerInfo() {
        return ServerInfo.getServerInfo();
    }
    @Override
    @Deprecated
    public Servlet getServlet ( String name ) {
        return null;
    }
    @Override
    public String getServletContextName() {
        return context.getDisplayName();
    }
    @Override
    @Deprecated
    public Enumeration<String> getServletNames() {
        return Collections.enumeration ( emptyString );
    }
    @Override
    @Deprecated
    public Enumeration<Servlet> getServlets() {
        return Collections.enumeration ( emptyServlet );
    }
    @Override
    public void log ( String message ) {
        context.getLogger().info ( message );
    }
    @Override
    @Deprecated
    public void log ( Exception exception, String message ) {
        context.getLogger().error ( message, exception );
    }
    @Override
    public void log ( String message, Throwable throwable ) {
        context.getLogger().error ( message, throwable );
    }
    @Override
    public void removeAttribute ( String name ) {
        Object value = null;
        if ( readOnlyAttributes.containsKey ( name ) ) {
            return;
        }
        value = attributes.remove ( name );
        if ( value == null ) {
            return;
        }
        Object listeners[] = context.getApplicationEventListeners();
        if ( ( listeners == null ) || ( listeners.length == 0 ) ) {
            return;
        }
        ServletContextAttributeEvent event =
            new ServletContextAttributeEvent ( context.getServletContext(),
                                               name, value );
        for ( int i = 0; i < listeners.length; i++ ) {
            if ( ! ( listeners[i] instanceof ServletContextAttributeListener ) ) {
                continue;
            }
            ServletContextAttributeListener listener =
                ( ServletContextAttributeListener ) listeners[i];
            try {
                context.fireContainerEvent ( "beforeContextAttributeRemoved",
                                             listener );
                listener.attributeRemoved ( event );
                context.fireContainerEvent ( "afterContextAttributeRemoved",
                                             listener );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                context.fireContainerEvent ( "afterContextAttributeRemoved",
                                             listener );
                log ( sm.getString ( "applicationContext.attributeEvent" ), t );
            }
        }
    }
    @Override
    public void setAttribute ( String name, Object value ) {
        if ( name == null )
            throw new IllegalArgumentException
            ( sm.getString ( "applicationContext.setAttribute.namenull" ) );
        if ( value == null ) {
            removeAttribute ( name );
            return;
        }
        if ( readOnlyAttributes.containsKey ( name ) ) {
            return;
        }
        Object oldValue = attributes.put ( name, value );
        boolean replaced = oldValue != null;
        Object listeners[] = context.getApplicationEventListeners();
        if ( ( listeners == null ) || ( listeners.length == 0 ) ) {
            return;
        }
        ServletContextAttributeEvent event = null;
        if ( replaced )
            event =
                new ServletContextAttributeEvent ( context.getServletContext(),
                                                   name, oldValue );
        else
            event =
                new ServletContextAttributeEvent ( context.getServletContext(),
                                                   name, value );
        for ( int i = 0; i < listeners.length; i++ ) {
            if ( ! ( listeners[i] instanceof ServletContextAttributeListener ) ) {
                continue;
            }
            ServletContextAttributeListener listener =
                ( ServletContextAttributeListener ) listeners[i];
            try {
                if ( replaced ) {
                    context.fireContainerEvent
                    ( "beforeContextAttributeReplaced", listener );
                    listener.attributeReplaced ( event );
                    context.fireContainerEvent ( "afterContextAttributeReplaced",
                                                 listener );
                } else {
                    context.fireContainerEvent ( "beforeContextAttributeAdded",
                                                 listener );
                    listener.attributeAdded ( event );
                    context.fireContainerEvent ( "afterContextAttributeAdded",
                                                 listener );
                }
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                if ( replaced )
                    context.fireContainerEvent ( "afterContextAttributeReplaced",
                                                 listener );
                else
                    context.fireContainerEvent ( "afterContextAttributeAdded",
                                                 listener );
                log ( sm.getString ( "applicationContext.attributeEvent" ), t );
            }
        }
    }
    @Override
    public FilterRegistration.Dynamic addFilter ( String filterName, String className ) {
        return addFilter ( filterName, className, null );
    }
    @Override
    public FilterRegistration.Dynamic addFilter ( String filterName, Filter filter ) {
        return addFilter ( filterName, null, filter );
    }
    @Override
    public FilterRegistration.Dynamic addFilter ( String filterName,
            Class<? extends Filter> filterClass ) {
        return addFilter ( filterName, filterClass.getName(), null );
    }
    private FilterRegistration.Dynamic addFilter ( String filterName,
            String filterClass, Filter filter ) throws IllegalStateException {
        if ( filterName == null || filterName.equals ( "" ) ) {
            throw new IllegalArgumentException ( sm.getString (
                    "applicationContext.invalidFilterName", filterName ) );
        }
        if ( !context.getState().equals ( LifecycleState.STARTING_PREP ) ) {
            throw new IllegalStateException (
                sm.getString ( "applicationContext.addFilter.ise",
                               getContextPath() ) );
        }
        FilterDef filterDef = context.findFilterDef ( filterName );
        if ( filterDef == null ) {
            filterDef = new FilterDef();
            filterDef.setFilterName ( filterName );
            context.addFilterDef ( filterDef );
        } else {
            if ( filterDef.getFilterName() != null &&
                    filterDef.getFilterClass() != null ) {
                return null;
            }
        }
        if ( filter == null ) {
            filterDef.setFilterClass ( filterClass );
        } else {
            filterDef.setFilterClass ( filter.getClass().getName() );
            filterDef.setFilter ( filter );
        }
        return new ApplicationFilterRegistration ( filterDef, context );
    }
    @Override
    public <T extends Filter> T createFilter ( Class<T> c ) throws ServletException {
        try {
            @SuppressWarnings ( "unchecked" )
            T filter = ( T ) context.getInstanceManager().newInstance ( c.getName() );
            return filter;
        } catch ( InvocationTargetException e ) {
            ExceptionUtils.handleThrowable ( e.getCause() );
            throw new ServletException ( e );
        } catch ( IllegalAccessException | NamingException | InstantiationException |
                      ClassNotFoundException e ) {
            throw new ServletException ( e );
        }
    }
    @Override
    public FilterRegistration getFilterRegistration ( String filterName ) {
        FilterDef filterDef = context.findFilterDef ( filterName );
        if ( filterDef == null ) {
            return null;
        }
        return new ApplicationFilterRegistration ( filterDef, context );
    }
    @Override
    public ServletRegistration.Dynamic addServlet ( String servletName, String className ) {
        return addServlet ( servletName, className, null );
    }
    @Override
    public ServletRegistration.Dynamic addServlet ( String servletName, Servlet servlet ) {
        return addServlet ( servletName, null, servlet );
    }
    @Override
    public ServletRegistration.Dynamic addServlet ( String servletName,
            Class<? extends Servlet> servletClass ) {
        return addServlet ( servletName, servletClass.getName(), null );
    }
    private ServletRegistration.Dynamic addServlet ( String servletName,
            String servletClass, Servlet servlet ) throws IllegalStateException {
        if ( servletName == null || servletName.equals ( "" ) ) {
            throw new IllegalArgumentException ( sm.getString (
                    "applicationContext.invalidServletName", servletName ) );
        }
        if ( !context.getState().equals ( LifecycleState.STARTING_PREP ) ) {
            throw new IllegalStateException (
                sm.getString ( "applicationContext.addServlet.ise",
                               getContextPath() ) );
        }
        Wrapper wrapper = ( Wrapper ) context.findChild ( servletName );
        if ( wrapper == null ) {
            wrapper = context.createWrapper();
            wrapper.setName ( servletName );
            context.addChild ( wrapper );
        } else {
            if ( wrapper.getName() != null &&
                    wrapper.getServletClass() != null ) {
                if ( wrapper.isOverridable() ) {
                    wrapper.setOverridable ( false );
                } else {
                    return null;
                }
            }
        }
        if ( servlet == null ) {
            wrapper.setServletClass ( servletClass );
        } else {
            wrapper.setServletClass ( servlet.getClass().getName() );
            wrapper.setServlet ( servlet );
        }
        return context.dynamicServletAdded ( wrapper );
    }
    @Override
    public <T extends Servlet> T createServlet ( Class<T> c )
    throws ServletException {
        try {
            @SuppressWarnings ( "unchecked" )
            T servlet = ( T ) context.getInstanceManager().newInstance ( c.getName() );
            context.dynamicServletCreated ( servlet );
            return servlet;
        } catch ( InvocationTargetException e ) {
            ExceptionUtils.handleThrowable ( e.getCause() );
            throw new ServletException ( e );
        } catch ( IllegalAccessException | NamingException | InstantiationException |
                      ClassNotFoundException e ) {
            throw new ServletException ( e );
        }
    }
    @Override
    public ServletRegistration getServletRegistration ( String servletName ) {
        Wrapper wrapper = ( Wrapper ) context.findChild ( servletName );
        if ( wrapper == null ) {
            return null;
        }
        return new ApplicationServletRegistration ( wrapper, context );
    }
    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return defaultSessionTrackingModes;
    }
    private void populateSessionTrackingModes() {
        defaultSessionTrackingModes = EnumSet.of ( SessionTrackingMode.URL );
        supportedSessionTrackingModes = EnumSet.of ( SessionTrackingMode.URL );
        if ( context.getCookies() ) {
            defaultSessionTrackingModes.add ( SessionTrackingMode.COOKIE );
            supportedSessionTrackingModes.add ( SessionTrackingMode.COOKIE );
        }
        Service s = ( ( Engine ) context.getParent().getParent() ).getService();
        Connector[] connectors = s.findConnectors();
        for ( Connector connector : connectors ) {
            if ( Boolean.TRUE.equals ( connector.getAttribute ( "SSLEnabled" ) ) ) {
                supportedSessionTrackingModes.add ( SessionTrackingMode.SSL );
                break;
            }
        }
    }
    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        if ( sessionTrackingModes != null ) {
            return sessionTrackingModes;
        }
        return defaultSessionTrackingModes;
    }
    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return sessionCookieConfig;
    }
    @Override
    public void setSessionTrackingModes ( Set<SessionTrackingMode> sessionTrackingModes ) {
        if ( !context.getState().equals ( LifecycleState.STARTING_PREP ) ) {
            throw new IllegalStateException (
                sm.getString ( "applicationContext.setSessionTracking.ise",
                               getContextPath() ) );
        }
        for ( SessionTrackingMode sessionTrackingMode : sessionTrackingModes ) {
            if ( !supportedSessionTrackingModes.contains ( sessionTrackingMode ) ) {
                throw new IllegalArgumentException ( sm.getString (
                        "applicationContext.setSessionTracking.iae.invalid",
                        sessionTrackingMode.toString(), getContextPath() ) );
            }
        }
        if ( sessionTrackingModes.contains ( SessionTrackingMode.SSL ) ) {
            if ( sessionTrackingModes.size() > 1 ) {
                throw new IllegalArgumentException ( sm.getString (
                        "applicationContext.setSessionTracking.iae.ssl",
                        getContextPath() ) );
            }
        }
        this.sessionTrackingModes = sessionTrackingModes;
    }
    @Override
    public boolean setInitParameter ( String name, String value ) {
        if ( !context.getState().equals ( LifecycleState.STARTING_PREP ) ) {
            throw new IllegalStateException (
                sm.getString ( "applicationContext.setInitParam.ise",
                               getContextPath() ) );
        }
        return parameters.putIfAbsent ( name, value ) == null;
    }
    @Override
    public void addListener ( Class<? extends EventListener> listenerClass ) {
        EventListener listener;
        try {
            listener = createListener ( listenerClass );
        } catch ( ServletException e ) {
            throw new IllegalArgumentException ( sm.getString (
                    "applicationContext.addListener.iae.init",
                    listenerClass.getName() ), e );
        }
        addListener ( listener );
    }
    @Override
    public void addListener ( String className ) {
        try {
            if ( context.getInstanceManager() != null ) {
                Object obj = context.getInstanceManager().newInstance ( className );
                if ( ! ( obj instanceof EventListener ) ) {
                    throw new IllegalArgumentException ( sm.getString (
                            "applicationContext.addListener.iae.wrongType",
                            className ) );
                }
                EventListener listener = ( EventListener ) obj;
                addListener ( listener );
            }
        } catch ( InvocationTargetException e ) {
            ExceptionUtils.handleThrowable ( e.getCause() );
            throw new IllegalArgumentException ( sm.getString (
                    "applicationContext.addListener.iae.cnfe", className ),
                                                 e );
        } catch ( IllegalAccessException | NamingException | InstantiationException |
                      ClassNotFoundException e ) {
            throw new IllegalArgumentException ( sm.getString (
                    "applicationContext.addListener.iae.cnfe", className ),
                                                 e );
        }
    }
    @Override
    public <T extends EventListener> void addListener ( T t ) {
        if ( !context.getState().equals ( LifecycleState.STARTING_PREP ) ) {
            throw new IllegalStateException (
                sm.getString ( "applicationContext.addListener.ise",
                               getContextPath() ) );
        }
        boolean match = false;
        if ( t instanceof ServletContextAttributeListener ||
                t instanceof ServletRequestListener ||
                t instanceof ServletRequestAttributeListener ||
                t instanceof HttpSessionIdListener ||
                t instanceof HttpSessionAttributeListener ) {
            context.addApplicationEventListener ( t );
            match = true;
        }
        if ( t instanceof HttpSessionListener
                || ( t instanceof ServletContextListener &&
                     newServletContextListenerAllowed ) ) {
            context.addApplicationLifecycleListener ( t );
            match = true;
        }
        if ( match ) {
            return;
        }
        if ( t instanceof ServletContextListener ) {
            throw new IllegalArgumentException ( sm.getString (
                    "applicationContext.addListener.iae.sclNotAllowed",
                    t.getClass().getName() ) );
        } else {
            throw new IllegalArgumentException ( sm.getString (
                    "applicationContext.addListener.iae.wrongType",
                    t.getClass().getName() ) );
        }
    }
    @Override
    public <T extends EventListener> T createListener ( Class<T> c )
    throws ServletException {
        try {
            @SuppressWarnings ( "unchecked" )
            T listener =
                ( T ) context.getInstanceManager().newInstance ( c );
            if ( listener instanceof ServletContextListener ||
                    listener instanceof ServletContextAttributeListener ||
                    listener instanceof ServletRequestListener ||
                    listener instanceof ServletRequestAttributeListener ||
                    listener instanceof HttpSessionListener ||
                    listener instanceof HttpSessionIdListener ||
                    listener instanceof HttpSessionAttributeListener ) {
                return listener;
            }
            throw new IllegalArgumentException ( sm.getString (
                    "applicationContext.addListener.iae.wrongType",
                    listener.getClass().getName() ) );
        } catch ( InvocationTargetException e ) {
            ExceptionUtils.handleThrowable ( e.getCause() );
            throw new ServletException ( e );
        } catch ( IllegalAccessException | NamingException | InstantiationException e ) {
            throw new ServletException ( e );
        }
    }
    @Override
    public void declareRoles ( String... roleNames ) {
        if ( !context.getState().equals ( LifecycleState.STARTING_PREP ) ) {
            throw new IllegalStateException (
                sm.getString ( "applicationContext.addRole.ise",
                               getContextPath() ) );
        }
        if ( roleNames == null ) {
            throw new IllegalArgumentException (
                sm.getString ( "applicationContext.roles.iae",
                               getContextPath() ) );
        }
        for ( String role : roleNames ) {
            if ( role == null || "".equals ( role ) ) {
                throw new IllegalArgumentException (
                    sm.getString ( "applicationContext.role.iae",
                                   getContextPath() ) );
            }
            context.addSecurityRole ( role );
        }
    }
    @Override
    public ClassLoader getClassLoader() {
        ClassLoader result = context.getLoader().getClassLoader();
        if ( Globals.IS_SECURITY_ENABLED ) {
            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            ClassLoader parent = result;
            while ( parent != null ) {
                if ( parent == tccl ) {
                    break;
                }
                parent = parent.getParent();
            }
            if ( parent == null ) {
                System.getSecurityManager().checkPermission (
                    new RuntimePermission ( "getClassLoader" ) );
            }
        }
        return result;
    }
    @Override
    public int getEffectiveMajorVersion() {
        return context.getEffectiveMajorVersion();
    }
    @Override
    public int getEffectiveMinorVersion() {
        return context.getEffectiveMinorVersion();
    }
    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        Map<String, ApplicationFilterRegistration> result = new HashMap<>();
        FilterDef[] filterDefs = context.findFilterDefs();
        for ( FilterDef filterDef : filterDefs ) {
            result.put ( filterDef.getFilterName(),
                         new ApplicationFilterRegistration ( filterDef, context ) );
        }
        return result;
    }
    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return context.getJspConfigDescriptor();
    }
    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        Map<String, ApplicationServletRegistration> result = new HashMap<>();
        Container[] wrappers = context.findChildren();
        for ( Container wrapper : wrappers ) {
            result.put ( ( ( Wrapper ) wrapper ).getName(),
                         new ApplicationServletRegistration (
                             ( Wrapper ) wrapper, context ) );
        }
        return result;
    }
    @Override
    public String getVirtualServerName() {
        Container host = context.getParent();
        Container engine = host.getParent();
        return engine.getName() + "/" + host.getName();
    }
    protected StandardContext getContext() {
        return this.context;
    }
    protected void clearAttributes() {
        ArrayList<String> list = new ArrayList<>();
        Iterator<String> iter = attributes.keySet().iterator();
        while ( iter.hasNext() ) {
            list.add ( iter.next() );
        }
        Iterator<String> keys = list.iterator();
        while ( keys.hasNext() ) {
            String key = keys.next();
            removeAttribute ( key );
        }
    }
    protected ServletContext getFacade() {
        return ( this.facade );
    }
    void setAttributeReadOnly ( String name ) {
        if ( attributes.containsKey ( name ) ) {
            readOnlyAttributes.put ( name, name );
        }
    }
    protected void setNewServletContextListenerAllowed ( boolean allowed ) {
        this.newServletContextListenerAllowed = allowed;
    }
    private static final class DispatchData {
        public MessageBytes uriMB;
        public MappingData mappingData;
        public DispatchData() {
            uriMB = MessageBytes.newInstance();
            CharChunk uriCC = uriMB.getCharChunk();
            uriCC.setLimit ( -1 );
            mappingData = new MappingData();
        }
    }
}
