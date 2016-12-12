package javax.servlet.jsp.el;
import java.util.HashSet;
import java.util.Set;
import java.util.AbstractMap;
import javax.servlet.http.HttpSession;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import javax.servlet.http.Cookie;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
import javax.el.PropertyNotWritableException;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import java.util.Arrays;
import java.util.Objects;
import javax.el.ELContext;
import javax.el.ELResolver;
public class ImplicitObjectELResolver extends ELResolver {
    private static final String[] SCOPE_NAMES;
    private static final int APPLICATIONSCOPE = 0;
    private static final int COOKIE = 1;
    private static final int HEADER = 2;
    private static final int HEADERVALUES = 3;
    private static final int INITPARAM = 4;
    private static final int PAGECONTEXT = 5;
    private static final int PAGESCOPE = 6;
    private static final int PARAM = 7;
    private static final int PARAM_VALUES = 8;
    private static final int REQUEST_SCOPE = 9;
    private static final int SESSION_SCOPE = 10;
    @Override
    public Object getValue ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base == null && property != null ) {
            final int idx = Arrays.binarySearch ( ImplicitObjectELResolver.SCOPE_NAMES, property.toString() );
            if ( idx >= 0 ) {
                final PageContext page = ( PageContext ) context.getContext ( JspContext.class );
                context.setPropertyResolved ( base, property );
                switch ( idx ) {
                case 0: {
                    return ScopeManager.get ( page ).getApplicationScope();
                }
                case 1: {
                    return ScopeManager.get ( page ).getCookie();
                }
                case 2: {
                    return ScopeManager.get ( page ).getHeader();
                }
                case 3: {
                    return ScopeManager.get ( page ).getHeaderValues();
                }
                case 4: {
                    return ScopeManager.get ( page ).getInitParam();
                }
                case 5: {
                    return ScopeManager.get ( page ).getPageContext();
                }
                case 6: {
                    return ScopeManager.get ( page ).getPageScope();
                }
                case 7: {
                    return ScopeManager.get ( page ).getParam();
                }
                case 8: {
                    return ScopeManager.get ( page ).getParamValues();
                }
                case 9: {
                    return ScopeManager.get ( page ).getRequestScope();
                }
                case 10: {
                    return ScopeManager.get ( page ).getSessionScope();
                }
                }
            }
        }
        return null;
    }
    @Override
    public Class getType ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base == null && property != null ) {
            final int idx = Arrays.binarySearch ( ImplicitObjectELResolver.SCOPE_NAMES, property.toString() );
            if ( idx >= 0 ) {
                context.setPropertyResolved ( base, property );
            }
        }
        return null;
    }
    @Override
    public void setValue ( final ELContext context, final Object base, final Object property, final Object value ) {
        Objects.requireNonNull ( context );
        if ( base == null && property != null ) {
            final int idx = Arrays.binarySearch ( ImplicitObjectELResolver.SCOPE_NAMES, property.toString() );
            if ( idx >= 0 ) {
                context.setPropertyResolved ( base, property );
                throw new PropertyNotWritableException();
            }
        }
    }
    @Override
    public boolean isReadOnly ( final ELContext context, final Object base, final Object property ) {
        Objects.requireNonNull ( context );
        if ( base == null && property != null ) {
            final int idx = Arrays.binarySearch ( ImplicitObjectELResolver.SCOPE_NAMES, property.toString() );
            if ( idx >= 0 ) {
                context.setPropertyResolved ( base, property );
                return true;
            }
        }
        return false;
    }
    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors ( final ELContext context, final Object base ) {
        final List<FeatureDescriptor> feats = new ArrayList<FeatureDescriptor> ( ImplicitObjectELResolver.SCOPE_NAMES.length );
        for ( int i = 0; i < ImplicitObjectELResolver.SCOPE_NAMES.length; ++i ) {
            final FeatureDescriptor feat = new FeatureDescriptor();
            feat.setDisplayName ( ImplicitObjectELResolver.SCOPE_NAMES[i] );
            feat.setExpert ( false );
            feat.setHidden ( false );
            feat.setName ( ImplicitObjectELResolver.SCOPE_NAMES[i] );
            feat.setPreferred ( true );
            feat.setValue ( "resolvableAtDesignTime", Boolean.TRUE );
            feat.setValue ( "type", String.class );
            feats.add ( feat );
        }
        return feats.iterator();
    }
    @Override
    public Class<String> getCommonPropertyType ( final ELContext context, final Object base ) {
        if ( base == null ) {
            return String.class;
        }
        return null;
    }
    static {
        SCOPE_NAMES = new String[] { "applicationScope", "cookie", "header", "headerValues", "initParam", "pageContext", "pageScope", "param", "paramValues", "requestScope", "sessionScope" };
    }
    private static class ScopeManager {
        private static final String MNGR_KEY;
        private final PageContext page;
        private Map<String, Object> applicationScope;
        private Map<String, Cookie> cookie;
        private Map<String, String> header;
        private Map<String, String[]> headerValues;
        private Map<String, String> initParam;
        private Map<String, Object> pageScope;
        private Map<String, String> param;
        private Map<String, String[]> paramValues;
        private Map<String, Object> requestScope;
        private Map<String, Object> sessionScope;
        public ScopeManager ( final PageContext page ) {
            this.page = page;
        }
        public static ScopeManager get ( final PageContext page ) {
            ScopeManager mngr = ( ScopeManager ) page.getAttribute ( ScopeManager.MNGR_KEY );
            if ( mngr == null ) {
                mngr = new ScopeManager ( page );
                page.setAttribute ( ScopeManager.MNGR_KEY, mngr );
            }
            return mngr;
        }
        public Map<String, Object> getApplicationScope() {
            if ( this.applicationScope == null ) {
                this.applicationScope = ( Map<String, Object> ) new ScopeMap<Object>() {
                    @Override
                    protected void setAttribute ( final String name, final Object value ) {
                        ScopeManager.this.page.getServletContext().setAttribute ( name, value );
                    }
                    @Override
                    protected void removeAttribute ( final String name ) {
                        ScopeManager.this.page.getServletContext().removeAttribute ( name );
                    }
                    @Override
                    protected Enumeration<String> getAttributeNames() {
                        return ScopeManager.this.page.getServletContext().getAttributeNames();
                    }
                    @Override
                    protected Object getAttribute ( final String name ) {
                        return ScopeManager.this.page.getServletContext().getAttribute ( name );
                    }
                };
            }
            return this.applicationScope;
        }
        public Map<String, Cookie> getCookie() {
            if ( this.cookie == null ) {
                this.cookie = ( Map<String, Cookie> ) new ScopeMap<Cookie>() {
                    @Override
                    protected Enumeration<String> getAttributeNames() {
                        final Cookie[] c = ( ( HttpServletRequest ) ScopeManager.this.page.getRequest() ).getCookies();
                        if ( c != null ) {
                            final Vector<String> v = new Vector<String>();
                            for ( int i = 0; i < c.length; ++i ) {
                                v.add ( c[i].getName() );
                            }
                            return v.elements();
                        }
                        return null;
                    }
                    @Override
                    protected Cookie getAttribute ( final String name ) {
                        final Cookie[] c = ( ( HttpServletRequest ) ScopeManager.this.page.getRequest() ).getCookies();
                        if ( c != null ) {
                            for ( int i = 0; i < c.length; ++i ) {
                                if ( name.equals ( c[i].getName() ) ) {
                                    return c[i];
                                }
                            }
                        }
                        return null;
                    }
                };
            }
            return this.cookie;
        }
        public Map<String, String> getHeader() {
            if ( this.header == null ) {
                this.header = ( Map<String, String> ) new ScopeMap<String>() {
                    @Override
                    protected Enumeration<String> getAttributeNames() {
                        return ( ( HttpServletRequest ) ScopeManager.this.page.getRequest() ).getHeaderNames();
                    }
                    @Override
                    protected String getAttribute ( final String name ) {
                        return ( ( HttpServletRequest ) ScopeManager.this.page.getRequest() ).getHeader ( name );
                    }
                };
            }
            return this.header;
        }
        public Map<String, String[]> getHeaderValues() {
            if ( this.headerValues == null ) {
                this.headerValues = ( Map<String, String[]> ) new ScopeMap<String[]>() {
                    @Override
                    protected Enumeration<String> getAttributeNames() {
                        return ( ( HttpServletRequest ) ScopeManager.this.page.getRequest() ).getHeaderNames();
                    }
                    @Override
                    protected String[] getAttribute ( final String name ) {
                        final Enumeration<String> e = ( ( HttpServletRequest ) ScopeManager.this.page.getRequest() ).getHeaders ( name );
                        if ( e != null ) {
                            final List<String> list = new ArrayList<String>();
                            while ( e.hasMoreElements() ) {
                                list.add ( e.nextElement() );
                            }
                            return list.toArray ( new String[list.size()] );
                        }
                        return null;
                    }
                };
            }
            return this.headerValues;
        }
        public Map<String, String> getInitParam() {
            if ( this.initParam == null ) {
                this.initParam = ( Map<String, String> ) new ScopeMap<String>() {
                    @Override
                    protected Enumeration<String> getAttributeNames() {
                        return ScopeManager.this.page.getServletContext().getInitParameterNames();
                    }
                    @Override
                    protected String getAttribute ( final String name ) {
                        return ScopeManager.this.page.getServletContext().getInitParameter ( name );
                    }
                };
            }
            return this.initParam;
        }
        public PageContext getPageContext() {
            return this.page;
        }
        public Map<String, Object> getPageScope() {
            if ( this.pageScope == null ) {
                this.pageScope = ( Map<String, Object> ) new ScopeMap<Object>() {
                    @Override
                    protected void setAttribute ( final String name, final Object value ) {
                        ScopeManager.this.page.setAttribute ( name, value );
                    }
                    @Override
                    protected void removeAttribute ( final String name ) {
                        ScopeManager.this.page.removeAttribute ( name );
                    }
                    @Override
                    protected Enumeration<String> getAttributeNames() {
                        return ScopeManager.this.page.getAttributeNamesInScope ( 1 );
                    }
                    @Override
                    protected Object getAttribute ( final String name ) {
                        return ScopeManager.this.page.getAttribute ( name );
                    }
                };
            }
            return this.pageScope;
        }
        public Map<String, String> getParam() {
            if ( this.param == null ) {
                this.param = ( Map<String, String> ) new ScopeMap<String>() {
                    @Override
                    protected Enumeration<String> getAttributeNames() {
                        return ScopeManager.this.page.getRequest().getParameterNames();
                    }
                    @Override
                    protected String getAttribute ( final String name ) {
                        return ScopeManager.this.page.getRequest().getParameter ( name );
                    }
                };
            }
            return this.param;
        }
        public Map<String, String[]> getParamValues() {
            if ( this.paramValues == null ) {
                this.paramValues = ( Map<String, String[]> ) new ScopeMap<String[]>() {
                    @Override
                    protected String[] getAttribute ( final String name ) {
                        return ScopeManager.this.page.getRequest().getParameterValues ( name );
                    }
                    @Override
                    protected Enumeration<String> getAttributeNames() {
                        return ScopeManager.this.page.getRequest().getParameterNames();
                    }
                };
            }
            return this.paramValues;
        }
        public Map<String, Object> getRequestScope() {
            if ( this.requestScope == null ) {
                this.requestScope = ( Map<String, Object> ) new ScopeMap<Object>() {
                    @Override
                    protected void setAttribute ( final String name, final Object value ) {
                        ScopeManager.this.page.getRequest().setAttribute ( name, value );
                    }
                    @Override
                    protected void removeAttribute ( final String name ) {
                        ScopeManager.this.page.getRequest().removeAttribute ( name );
                    }
                    @Override
                    protected Enumeration<String> getAttributeNames() {
                        return ScopeManager.this.page.getRequest().getAttributeNames();
                    }
                    @Override
                    protected Object getAttribute ( final String name ) {
                        return ScopeManager.this.page.getRequest().getAttribute ( name );
                    }
                };
            }
            return this.requestScope;
        }
        public Map<String, Object> getSessionScope() {
            if ( this.sessionScope == null ) {
                this.sessionScope = ( Map<String, Object> ) new ScopeMap<Object>() {
                    @Override
                    protected void setAttribute ( final String name, final Object value ) {
                        ( ( HttpServletRequest ) ScopeManager.this.page.getRequest() ).getSession().setAttribute ( name, value );
                    }
                    @Override
                    protected void removeAttribute ( final String name ) {
                        final HttpSession session = ScopeManager.this.page.getSession();
                        if ( session != null ) {
                            session.removeAttribute ( name );
                        }
                    }
                    @Override
                    protected Enumeration<String> getAttributeNames() {
                        final HttpSession session = ScopeManager.this.page.getSession();
                        if ( session != null ) {
                            return session.getAttributeNames();
                        }
                        return null;
                    }
                    @Override
                    protected Object getAttribute ( final String name ) {
                        final HttpSession session = ScopeManager.this.page.getSession();
                        if ( session != null ) {
                            return session.getAttribute ( name );
                        }
                        return null;
                    }
                };
            }
            return this.sessionScope;
        }
        static {
            MNGR_KEY = ScopeManager.class.getName();
        }
    }
    private abstract static class ScopeMap<V> extends AbstractMap<String, V> {
        protected abstract Enumeration<String> getAttributeNames();
        protected abstract V getAttribute ( final String p0 );
        protected void removeAttribute ( final String name ) {
            throw new UnsupportedOperationException();
        }
        protected void setAttribute ( final String name, final Object value ) {
            throw new UnsupportedOperationException();
        }
        @Override
        public final Set<Map.Entry<String, V>> entrySet() {
            final Enumeration<String> e = this.getAttributeNames();
            final Set<Map.Entry<String, V>> set = new HashSet<Map.Entry<String, V>>();
            if ( e != null ) {
                while ( e.hasMoreElements() ) {
                    set.add ( new ScopeEntry ( e.nextElement() ) );
                }
            }
            return set;
        }
        @Override
        public final int size() {
            int size = 0;
            final Enumeration<String> e = this.getAttributeNames();
            if ( e != null ) {
                while ( e.hasMoreElements() ) {
                    e.nextElement();
                    ++size;
                }
            }
            return size;
        }
        @Override
        public final boolean containsKey ( final Object key ) {
            if ( key == null ) {
                return false;
            }
            final Enumeration<String> e = this.getAttributeNames();
            if ( e != null ) {
                while ( e.hasMoreElements() ) {
                    if ( key.equals ( e.nextElement() ) ) {
                        return true;
                    }
                }
            }
            return false;
        }
        @Override
        public final V get ( final Object key ) {
            if ( key != null ) {
                return this.getAttribute ( ( String ) key );
            }
            return null;
        }
        @Override
        public final V put ( final String key, final V value ) {
            Objects.requireNonNull ( key );
            if ( value == null ) {
                this.removeAttribute ( key );
            } else {
                this.setAttribute ( key, value );
            }
            return null;
        }
        @Override
        public final V remove ( final Object key ) {
            Objects.requireNonNull ( key );
            this.removeAttribute ( ( String ) key );
            return null;
        }
        private class ScopeEntry implements Map.Entry<String, V> {
            private final String key;
            public ScopeEntry ( final String key ) {
                this.key = key;
            }
            @Override
            public String getKey() {
                return this.key;
            }
            @Override
            public V getValue() {
                return ScopeMap.this.getAttribute ( this.key );
            }
            @Override
            public V setValue ( final Object value ) {
                if ( value == null ) {
                    ScopeMap.this.removeAttribute ( this.key );
                } else {
                    ScopeMap.this.setAttribute ( this.key, value );
                }
                return null;
            }
            @Override
            public boolean equals ( final Object obj ) {
                return obj != null && this.hashCode() == obj.hashCode();
            }
            @Override
            public int hashCode() {
                return this.key.hashCode();
            }
        }
    }
}
