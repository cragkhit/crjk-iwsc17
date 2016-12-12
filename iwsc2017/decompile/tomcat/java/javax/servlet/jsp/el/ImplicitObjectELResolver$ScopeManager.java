package javax.servlet.jsp.el;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import javax.servlet.http.Cookie;
import java.util.Map;
import javax.servlet.jsp.PageContext;
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
