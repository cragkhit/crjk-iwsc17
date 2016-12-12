package org.apache.catalina.core;
import java.util.NoSuchElementException;
import java.io.UnsupportedEncodingException;
import org.apache.tomcat.util.buf.B2CConverter;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.Parameters;
import java.util.ArrayList;
import org.apache.catalina.util.ParameterMap;
import javax.servlet.ServletRequest;
import javax.servlet.http.PushBuilder;
import org.apache.catalina.Manager;
import java.io.IOException;
import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Collections;
import javax.servlet.RequestDispatcher;
import java.util.Enumeration;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.catalina.Session;
import javax.servlet.http.Mapping;
import java.util.Map;
import javax.servlet.DispatcherType;
import org.apache.catalina.Context;
import javax.servlet.http.HttpServletRequestWrapper;
class ApplicationHttpRequest extends HttpServletRequestWrapper {
    protected static final String[] specials;
    private static final int SPECIALS_FIRST_FORWARD_INDEX = 6;
    protected final Context context;
    protected String contextPath;
    protected final boolean crossContext;
    protected DispatcherType dispatcherType;
    protected Map<String, String[]> parameters;
    private boolean parsedParams;
    protected String pathInfo;
    private String queryParamString;
    protected String queryString;
    protected Object requestDispatcherPath;
    protected String requestURI;
    protected String servletPath;
    private Mapping mapping;
    protected Session session;
    protected final Object[] specialAttributes;
    public ApplicationHttpRequest ( final HttpServletRequest request, final Context context, final boolean crossContext ) {
        super ( request );
        this.contextPath = null;
        this.dispatcherType = null;
        this.parameters = null;
        this.parsedParams = false;
        this.pathInfo = null;
        this.queryParamString = null;
        this.queryString = null;
        this.requestDispatcherPath = null;
        this.requestURI = null;
        this.servletPath = null;
        this.mapping = null;
        this.session = null;
        this.specialAttributes = new Object[ApplicationHttpRequest.specials.length];
        this.context = context;
        this.crossContext = crossContext;
        this.setRequest ( request );
    }
    public ServletContext getServletContext() {
        if ( this.context == null ) {
            return null;
        }
        return this.context.getServletContext();
    }
    public Object getAttribute ( final String name ) {
        if ( name.equals ( "org.apache.catalina.core.DISPATCHER_TYPE" ) ) {
            return this.dispatcherType;
        }
        if ( name.equals ( "org.apache.catalina.core.DISPATCHER_REQUEST_PATH" ) ) {
            if ( this.requestDispatcherPath != null ) {
                return this.requestDispatcherPath.toString();
            }
            return null;
        } else {
            final int pos = this.getSpecial ( name );
            if ( pos == -1 ) {
                return this.getRequest().getAttribute ( name );
            }
            if ( this.specialAttributes[pos] == null && this.specialAttributes[6] == null && pos >= 6 ) {
                return this.getRequest().getAttribute ( name );
            }
            return this.specialAttributes[pos];
        }
    }
    public Enumeration<String> getAttributeNames() {
        return new AttributeNamesEnumerator();
    }
    public void removeAttribute ( final String name ) {
        if ( !this.removeSpecial ( name ) ) {
            this.getRequest().removeAttribute ( name );
        }
    }
    public void setAttribute ( final String name, final Object value ) {
        if ( name.equals ( "org.apache.catalina.core.DISPATCHER_TYPE" ) ) {
            this.dispatcherType = ( DispatcherType ) value;
            return;
        }
        if ( name.equals ( "org.apache.catalina.core.DISPATCHER_REQUEST_PATH" ) ) {
            this.requestDispatcherPath = value;
            return;
        }
        if ( !this.setSpecial ( name, value ) ) {
            this.getRequest().setAttribute ( name, value );
        }
    }
    public RequestDispatcher getRequestDispatcher ( final String path ) {
        if ( this.context == null ) {
            return null;
        }
        if ( path == null ) {
            return null;
        }
        if ( path.startsWith ( "/" ) ) {
            return this.context.getServletContext().getRequestDispatcher ( path );
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
        if ( pos >= 0 ) {
            relative = requestPath.substring ( 0, pos + 1 ) + path;
        } else {
            relative = requestPath + path;
        }
        return this.context.getServletContext().getRequestDispatcher ( relative );
    }
    public DispatcherType getDispatcherType() {
        return this.dispatcherType;
    }
    public String getContextPath() {
        return this.contextPath;
    }
    public String getParameter ( final String name ) {
        this.parseParameters();
        final String[] value = this.parameters.get ( name );
        if ( value == null ) {
            return null;
        }
        return value[0];
    }
    public Map<String, String[]> getParameterMap() {
        this.parseParameters();
        return this.parameters;
    }
    public Enumeration<String> getParameterNames() {
        this.parseParameters();
        return Collections.enumeration ( this.parameters.keySet() );
    }
    public String[] getParameterValues ( final String name ) {
        this.parseParameters();
        return this.parameters.get ( name );
    }
    public String getPathInfo() {
        return this.pathInfo;
    }
    public String getPathTranslated() {
        if ( this.getPathInfo() == null || this.getServletContext() == null ) {
            return null;
        }
        return this.getServletContext().getRealPath ( this.getPathInfo() );
    }
    public String getQueryString() {
        return this.queryString;
    }
    public String getRequestURI() {
        return this.requestURI;
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
        return this.servletPath;
    }
    public Mapping getMapping() {
        return this.mapping;
    }
    public HttpSession getSession() {
        return this.getSession ( true );
    }
    public HttpSession getSession ( final boolean create ) {
        if ( !this.crossContext ) {
            return super.getSession ( create );
        }
        if ( this.context == null ) {
            return null;
        }
        if ( this.session != null && this.session.isValid() ) {
            return this.session.getSession();
        }
        HttpSession other = super.getSession ( false );
        if ( create && other == null ) {
            other = super.getSession ( true );
        }
        if ( other != null ) {
            Session localSession = null;
            try {
                localSession = this.context.getManager().findSession ( other.getId() );
                if ( localSession != null && !localSession.isValid() ) {
                    localSession = null;
                }
            } catch ( IOException ex ) {}
            if ( localSession == null && create ) {
                localSession = this.context.getManager().createSession ( other.getId() );
            }
            if ( localSession != null ) {
                localSession.access();
                this.session = localSession;
                return this.session.getSession();
            }
        }
        return null;
    }
    public boolean isRequestedSessionIdValid() {
        if ( !this.crossContext ) {
            return super.isRequestedSessionIdValid();
        }
        final String requestedSessionId = this.getRequestedSessionId();
        if ( requestedSessionId == null ) {
            return false;
        }
        if ( this.context == null ) {
            return false;
        }
        final Manager manager = this.context.getManager();
        if ( manager == null ) {
            return false;
        }
        Session session = null;
        try {
            session = manager.findSession ( requestedSessionId );
        } catch ( IOException ex ) {}
        return session != null && session.isValid();
    }
    public PushBuilder getPushBuilder() {
        return ( PushBuilder ) new ApplicationPushBuilder ( ( HttpServletRequest ) this );
    }
    public void recycle() {
        if ( this.session != null ) {
            this.session.endAccess();
        }
    }
    void setContextPath ( final String contextPath ) {
        this.contextPath = contextPath;
    }
    void setPathInfo ( final String pathInfo ) {
        this.pathInfo = pathInfo;
    }
    void setQueryString ( final String queryString ) {
        this.queryString = queryString;
    }
    void setRequest ( final HttpServletRequest request ) {
        super.setRequest ( ( ServletRequest ) request );
        this.dispatcherType = ( DispatcherType ) request.getAttribute ( "org.apache.catalina.core.DISPATCHER_TYPE" );
        this.requestDispatcherPath = request.getAttribute ( "org.apache.catalina.core.DISPATCHER_REQUEST_PATH" );
        this.contextPath = request.getContextPath();
        this.pathInfo = request.getPathInfo();
        this.queryString = request.getQueryString();
        this.requestURI = request.getRequestURI();
        this.servletPath = request.getServletPath();
        this.mapping = request.getMapping();
    }
    void setRequestURI ( final String requestURI ) {
        this.requestURI = requestURI;
    }
    void setServletPath ( final String servletPath ) {
        this.servletPath = servletPath;
    }
    void parseParameters() {
        if ( this.parsedParams ) {
            return;
        }
        ( this.parameters = new ParameterMap<String, String[]>() ).putAll ( this.getRequest().getParameterMap() );
        this.mergeParameters();
        ( ( ParameterMap ) this.parameters ).setLocked ( true );
        this.parsedParams = true;
    }
    void setQueryParams ( final String queryString ) {
        this.queryParamString = queryString;
    }
    void setMapping ( final Mapping mapping ) {
        this.mapping = mapping;
    }
    protected boolean isSpecial ( final String name ) {
        for ( int i = 0; i < ApplicationHttpRequest.specials.length; ++i ) {
            if ( ApplicationHttpRequest.specials[i].equals ( name ) ) {
                return true;
            }
        }
        return false;
    }
    protected int getSpecial ( final String name ) {
        for ( int i = 0; i < ApplicationHttpRequest.specials.length; ++i ) {
            if ( ApplicationHttpRequest.specials[i].equals ( name ) ) {
                return i;
            }
        }
        return -1;
    }
    protected boolean setSpecial ( final String name, final Object value ) {
        for ( int i = 0; i < ApplicationHttpRequest.specials.length; ++i ) {
            if ( ApplicationHttpRequest.specials[i].equals ( name ) ) {
                this.specialAttributes[i] = value;
                return true;
            }
        }
        return false;
    }
    protected boolean removeSpecial ( final String name ) {
        for ( int i = 0; i < ApplicationHttpRequest.specials.length; ++i ) {
            if ( ApplicationHttpRequest.specials[i].equals ( name ) ) {
                this.specialAttributes[i] = null;
                return true;
            }
        }
        return false;
    }
    private String[] mergeValues ( final String[] values1, final String[] values2 ) {
        final ArrayList<Object> results = new ArrayList<Object>();
        if ( values1 != null ) {
            for ( final String value : values1 ) {
                results.add ( value );
            }
        }
        if ( values2 != null ) {
            for ( final String value : values2 ) {
                results.add ( value );
            }
        }
        final String[] values3 = new String[results.size()];
        return results.toArray ( values3 );
    }
    private void mergeParameters() {
        if ( this.queryParamString == null || this.queryParamString.length() < 1 ) {
            return;
        }
        final Parameters paramParser = new Parameters();
        final MessageBytes queryMB = MessageBytes.newInstance();
        queryMB.setString ( this.queryParamString );
        final String encoding = this.getCharacterEncoding();
        if ( encoding != null ) {
            try {
                queryMB.setCharset ( B2CConverter.getCharset ( encoding ) );
            } catch ( UnsupportedEncodingException ex ) {}
        }
        paramParser.setQuery ( queryMB );
        paramParser.setQueryStringEncoding ( encoding );
        paramParser.handleQueryParameters();
        final Enumeration<String> dispParamNames = paramParser.getParameterNames();
        while ( dispParamNames.hasMoreElements() ) {
            final String dispParamName = dispParamNames.nextElement();
            final String[] dispParamValues = paramParser.getParameterValues ( dispParamName );
            final String[] originalValues = this.parameters.get ( dispParamName );
            if ( originalValues == null ) {
                this.parameters.put ( dispParamName, dispParamValues );
            } else {
                this.parameters.put ( dispParamName, this.mergeValues ( dispParamValues, originalValues ) );
            }
        }
    }
    static {
        specials = new String[] { "javax.servlet.include.request_uri", "javax.servlet.include.context_path", "javax.servlet.include.servlet_path", "javax.servlet.include.path_info", "javax.servlet.include.query_string", "javax.servlet.include.mapping", "javax.servlet.forward.request_uri", "javax.servlet.forward.context_path", "javax.servlet.forward.servlet_path", "javax.servlet.forward.path_info", "javax.servlet.forward.query_string", "javax.servlet.forward.mapping" };
    }
    protected class AttributeNamesEnumerator implements Enumeration<String> {
        protected int pos;
        protected final int last;
        protected final Enumeration<String> parentEnumeration;
        protected String next;
        public AttributeNamesEnumerator() {
            this.pos = -1;
            this.next = null;
            int last = -1;
            this.parentEnumeration = ( Enumeration<String> ) ApplicationHttpRequest.this.getRequest().getAttributeNames();
            for ( int i = ApplicationHttpRequest.this.specialAttributes.length - 1; i >= 0; --i ) {
                if ( ApplicationHttpRequest.this.getAttribute ( ApplicationHttpRequest.specials[i] ) != null ) {
                    last = i;
                    break;
                }
            }
            this.last = last;
        }
        @Override
        public boolean hasMoreElements() {
            return this.pos != this.last || this.next != null || ( this.next = this.findNext() ) != null;
        }
        @Override
        public String nextElement() {
            if ( this.pos != this.last ) {
                for ( int i = this.pos + 1; i <= this.last; ++i ) {
                    if ( ApplicationHttpRequest.this.getAttribute ( ApplicationHttpRequest.specials[i] ) != null ) {
                        this.pos = i;
                        return ApplicationHttpRequest.specials[i];
                    }
                }
            }
            final String result = this.next;
            if ( this.next != null ) {
                this.next = this.findNext();
                return result;
            }
            throw new NoSuchElementException();
        }
        protected String findNext() {
            String result;
            String current;
            for ( result = null; result == null && this.parentEnumeration.hasMoreElements(); result = current ) {
                current = this.parentEnumeration.nextElement();
                if ( !ApplicationHttpRequest.this.isSpecial ( current ) ) {}
            }
            return result;
        }
    }
}
