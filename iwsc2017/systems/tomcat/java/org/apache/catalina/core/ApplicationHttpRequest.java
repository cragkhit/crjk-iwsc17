package org.apache.catalina.core;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Mapping;
import javax.servlet.http.PushBuilder;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.util.ParameterMap;
import org.apache.tomcat.util.buf.B2CConverter;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.Parameters;
class ApplicationHttpRequest extends HttpServletRequestWrapper {
    protected static final String specials[] = {
        RequestDispatcher.INCLUDE_REQUEST_URI,
        RequestDispatcher.INCLUDE_CONTEXT_PATH,
        RequestDispatcher.INCLUDE_SERVLET_PATH,
        RequestDispatcher.INCLUDE_PATH_INFO,
        RequestDispatcher.INCLUDE_QUERY_STRING,
        RequestDispatcher.INCLUDE_MAPPING,
        RequestDispatcher.FORWARD_REQUEST_URI,
        RequestDispatcher.FORWARD_CONTEXT_PATH,
        RequestDispatcher.FORWARD_SERVLET_PATH,
        RequestDispatcher.FORWARD_PATH_INFO,
        RequestDispatcher.FORWARD_QUERY_STRING,
        RequestDispatcher.FORWARD_MAPPING
    };
    private static final int SPECIALS_FIRST_FORWARD_INDEX = 6;
    public ApplicationHttpRequest ( HttpServletRequest request, Context context,
                                    boolean crossContext ) {
        super ( request );
        this.context = context;
        this.crossContext = crossContext;
        setRequest ( request );
    }
    protected final Context context;
    protected String contextPath = null;
    protected final boolean crossContext;
    protected DispatcherType dispatcherType = null;
    protected Map<String, String[]> parameters = null;
    private boolean parsedParams = false;
    protected String pathInfo = null;
    private String queryParamString = null;
    protected String queryString = null;
    protected Object requestDispatcherPath = null;
    protected String requestURI = null;
    protected String servletPath = null;
    private Mapping mapping = null;
    protected Session session = null;
    protected final Object[] specialAttributes = new Object[specials.length];
    @Override
    public ServletContext getServletContext() {
        if ( context == null ) {
            return null;
        }
        return context.getServletContext();
    }
    @Override
    public Object getAttribute ( String name ) {
        if ( name.equals ( Globals.DISPATCHER_TYPE_ATTR ) ) {
            return dispatcherType;
        } else if ( name.equals ( Globals.DISPATCHER_REQUEST_PATH_ATTR ) ) {
            if ( requestDispatcherPath != null ) {
                return requestDispatcherPath.toString();
            } else {
                return null;
            }
        }
        int pos = getSpecial ( name );
        if ( pos == -1 ) {
            return getRequest().getAttribute ( name );
        } else {
            if ( ( specialAttributes[pos] == null ) &&
                    ( specialAttributes[SPECIALS_FIRST_FORWARD_INDEX] == null ) &&
                    ( pos >= SPECIALS_FIRST_FORWARD_INDEX ) ) {
                return getRequest().getAttribute ( name );
            } else {
                return specialAttributes[pos];
            }
        }
    }
    @Override
    public Enumeration<String> getAttributeNames() {
        return ( new AttributeNamesEnumerator() );
    }
    @Override
    public void removeAttribute ( String name ) {
        if ( !removeSpecial ( name ) ) {
            getRequest().removeAttribute ( name );
        }
    }
    @Override
    public void setAttribute ( String name, Object value ) {
        if ( name.equals ( Globals.DISPATCHER_TYPE_ATTR ) ) {
            dispatcherType = ( DispatcherType ) value;
            return;
        } else if ( name.equals ( Globals.DISPATCHER_REQUEST_PATH_ATTR ) ) {
            requestDispatcherPath = value;
            return;
        }
        if ( !setSpecial ( name, value ) ) {
            getRequest().setAttribute ( name, value );
        }
    }
    @Override
    public RequestDispatcher getRequestDispatcher ( String path ) {
        if ( context == null ) {
            return ( null );
        }
        if ( path == null ) {
            return ( null );
        } else if ( path.startsWith ( "/" ) ) {
            return ( context.getServletContext().getRequestDispatcher ( path ) );
        }
        String servletPath =
            ( String ) getAttribute ( RequestDispatcher.INCLUDE_SERVLET_PATH );
        if ( servletPath == null ) {
            servletPath = getServletPath();
        }
        String pathInfo = getPathInfo();
        String requestPath = null;
        if ( pathInfo == null ) {
            requestPath = servletPath;
        } else {
            requestPath = servletPath + pathInfo;
        }
        int pos = requestPath.lastIndexOf ( '/' );
        String relative = null;
        if ( pos >= 0 ) {
            relative = requestPath.substring ( 0, pos + 1 ) + path;
        } else {
            relative = requestPath + path;
        }
        return ( context.getServletContext().getRequestDispatcher ( relative ) );
    }
    @Override
    public DispatcherType getDispatcherType() {
        return dispatcherType;
    }
    @Override
    public String getContextPath() {
        return ( this.contextPath );
    }
    @Override
    public String getParameter ( String name ) {
        parseParameters();
        String[] value = parameters.get ( name );
        if ( value == null ) {
            return null;
        }
        return value[0];
    }
    @Override
    public Map<String, String[]> getParameterMap() {
        parseParameters();
        return ( parameters );
    }
    @Override
    public Enumeration<String> getParameterNames() {
        parseParameters();
        return Collections.enumeration ( parameters.keySet() );
    }
    @Override
    public String[] getParameterValues ( String name ) {
        parseParameters();
        return parameters.get ( name );
    }
    @Override
    public String getPathInfo() {
        return ( this.pathInfo );
    }
    @Override
    public String getPathTranslated() {
        if ( getPathInfo() == null || getServletContext() == null ) {
            return null;
        }
        return getServletContext().getRealPath ( getPathInfo() );
    }
    @Override
    public String getQueryString() {
        return ( this.queryString );
    }
    @Override
    public String getRequestURI() {
        return ( this.requestURI );
    }
    @Override
    public StringBuffer getRequestURL() {
        StringBuffer url = new StringBuffer();
        String scheme = getScheme();
        int port = getServerPort();
        if ( port < 0 ) {
            port = 80;
        }
        url.append ( scheme );
        url.append ( "://" );
        url.append ( getServerName() );
        if ( ( scheme.equals ( "http" ) && ( port != 80 ) )
                || ( scheme.equals ( "https" ) && ( port != 443 ) ) ) {
            url.append ( ':' );
            url.append ( port );
        }
        url.append ( getRequestURI() );
        return ( url );
    }
    @Override
    public String getServletPath() {
        return ( this.servletPath );
    }
    @Override
    public Mapping getMapping() {
        return mapping;
    }
    @Override
    public HttpSession getSession() {
        return ( getSession ( true ) );
    }
    @Override
    public HttpSession getSession ( boolean create ) {
        if ( crossContext ) {
            if ( context == null ) {
                return ( null );
            }
            if ( session != null && session.isValid() ) {
                return ( session.getSession() );
            }
            HttpSession other = super.getSession ( false );
            if ( create && ( other == null ) ) {
                other = super.getSession ( true );
            }
            if ( other != null ) {
                Session localSession = null;
                try {
                    localSession =
                        context.getManager().findSession ( other.getId() );
                    if ( localSession != null && !localSession.isValid() ) {
                        localSession = null;
                    }
                } catch ( IOException e ) {
                }
                if ( localSession == null && create ) {
                    localSession =
                        context.getManager().createSession ( other.getId() );
                }
                if ( localSession != null ) {
                    localSession.access();
                    session = localSession;
                    return session.getSession();
                }
            }
            return null;
        } else {
            return super.getSession ( create );
        }
    }
    @Override
    public boolean isRequestedSessionIdValid() {
        if ( crossContext ) {
            String requestedSessionId = getRequestedSessionId();
            if ( requestedSessionId == null ) {
                return false;
            }
            if ( context == null ) {
                return false;
            }
            Manager manager = context.getManager();
            if ( manager == null ) {
                return false;
            }
            Session session = null;
            try {
                session = manager.findSession ( requestedSessionId );
            } catch ( IOException e ) {
            }
            if ( ( session != null ) && session.isValid() ) {
                return true;
            } else {
                return false;
            }
        } else {
            return super.isRequestedSessionIdValid();
        }
    }
    @Override
    public PushBuilder getPushBuilder() {
        return new ApplicationPushBuilder ( this );
    }
    public void recycle() {
        if ( session != null ) {
            session.endAccess();
        }
    }
    void setContextPath ( String contextPath ) {
        this.contextPath = contextPath;
    }
    void setPathInfo ( String pathInfo ) {
        this.pathInfo = pathInfo;
    }
    void setQueryString ( String queryString ) {
        this.queryString = queryString;
    }
    void setRequest ( HttpServletRequest request ) {
        super.setRequest ( request );
        dispatcherType = ( DispatcherType ) request.getAttribute ( Globals.DISPATCHER_TYPE_ATTR );
        requestDispatcherPath =
            request.getAttribute ( Globals.DISPATCHER_REQUEST_PATH_ATTR );
        contextPath = request.getContextPath();
        pathInfo = request.getPathInfo();
        queryString = request.getQueryString();
        requestURI = request.getRequestURI();
        servletPath = request.getServletPath();
        mapping = request.getMapping();
    }
    void setRequestURI ( String requestURI ) {
        this.requestURI = requestURI;
    }
    void setServletPath ( String servletPath ) {
        this.servletPath = servletPath;
    }
    void parseParameters() {
        if ( parsedParams ) {
            return;
        }
        parameters = new ParameterMap<>();
        parameters.putAll ( getRequest().getParameterMap() );
        mergeParameters();
        ( ( ParameterMap<String, String[]> ) parameters ).setLocked ( true );
        parsedParams = true;
    }
    void setQueryParams ( String queryString ) {
        this.queryParamString = queryString;
    }
    void setMapping ( Mapping mapping ) {
        this.mapping = mapping;
    }
    protected boolean isSpecial ( String name ) {
        for ( int i = 0; i < specials.length; i++ ) {
            if ( specials[i].equals ( name ) ) {
                return true;
            }
        }
        return false;
    }
    protected int getSpecial ( String name ) {
        for ( int i = 0; i < specials.length; i++ ) {
            if ( specials[i].equals ( name ) ) {
                return ( i );
            }
        }
        return ( -1 );
    }
    protected boolean setSpecial ( String name, Object value ) {
        for ( int i = 0; i < specials.length; i++ ) {
            if ( specials[i].equals ( name ) ) {
                specialAttributes[i] = value;
                return true;
            }
        }
        return false;
    }
    protected boolean removeSpecial ( String name ) {
        for ( int i = 0; i < specials.length; i++ ) {
            if ( specials[i].equals ( name ) ) {
                specialAttributes[i] = null;
                return true;
            }
        }
        return false;
    }
    private String[] mergeValues ( String[] values1, String[] values2 ) {
        ArrayList<Object> results = new ArrayList<>();
        if ( values1 == null ) {
        } else {
            for ( String value : values1 ) {
                results.add ( value );
            }
        }
        if ( values2 == null ) {
        } else {
            for ( String value : values2 ) {
                results.add ( value );
            }
        }
        String values[] = new String[results.size()];
        return results.toArray ( values );
    }
    private void mergeParameters() {
        if ( ( queryParamString == null ) || ( queryParamString.length() < 1 ) ) {
            return;
        }
        Parameters paramParser = new Parameters();
        MessageBytes queryMB = MessageBytes.newInstance();
        queryMB.setString ( queryParamString );
        String encoding = getCharacterEncoding();
        if ( encoding != null ) {
            try {
                queryMB.setCharset ( B2CConverter.getCharset ( encoding ) );
            } catch ( UnsupportedEncodingException ignored ) {
            }
        }
        paramParser.setQuery ( queryMB );
        paramParser.setQueryStringEncoding ( encoding );
        paramParser.handleQueryParameters();
        Enumeration<String> dispParamNames = paramParser.getParameterNames();
        while ( dispParamNames.hasMoreElements() ) {
            String dispParamName = dispParamNames.nextElement();
            String[] dispParamValues = paramParser.getParameterValues ( dispParamName );
            String[] originalValues = parameters.get ( dispParamName );
            if ( originalValues == null ) {
                parameters.put ( dispParamName, dispParamValues );
                continue;
            }
            parameters.put ( dispParamName, mergeValues ( dispParamValues, originalValues ) );
        }
    }
    protected class AttributeNamesEnumerator implements Enumeration<String> {
        protected int pos = -1;
        protected final int last;
        protected final Enumeration<String> parentEnumeration;
        protected String next = null;
        public AttributeNamesEnumerator() {
            int last = -1;
            parentEnumeration = getRequest().getAttributeNames();
            for ( int i = specialAttributes.length - 1; i >= 0; i-- ) {
                if ( getAttribute ( specials[i] ) != null ) {
                    last = i;
                    break;
                }
            }
            this.last = last;
        }
        @Override
        public boolean hasMoreElements() {
            return ( ( pos != last ) || ( next != null )
                     || ( ( next = findNext() ) != null ) );
        }
        @Override
        public String nextElement() {
            if ( pos != last ) {
                for ( int i = pos + 1; i <= last; i++ ) {
                    if ( getAttribute ( specials[i] ) != null ) {
                        pos = i;
                        return ( specials[i] );
                    }
                }
            }
            String result = next;
            if ( next != null ) {
                next = findNext();
            } else {
                throw new NoSuchElementException();
            }
            return result;
        }
        protected String findNext() {
            String result = null;
            while ( ( result == null ) && ( parentEnumeration.hasMoreElements() ) ) {
                String current = parentEnumeration.nextElement();
                if ( !isSpecial ( current ) ) {
                    result = current;
                }
            }
            return result;
        }
    }
}
