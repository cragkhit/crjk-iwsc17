package org.apache.catalina.core;
import java.io.IOException;
import java.security.Principal;
import java.security.PrivilegedActionException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Globals;
import org.apache.catalina.security.SecurityUtil;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.res.StringManager;
final class ApplicationFilterChain implements FilterChain {
    private static final ThreadLocal<ServletRequest> lastServicedRequest;
    private static final ThreadLocal<ServletResponse> lastServicedResponse;
    static {
        if ( ApplicationDispatcher.WRAP_SAME_OBJECT ) {
            lastServicedRequest = new ThreadLocal<>();
            lastServicedResponse = new ThreadLocal<>();
        } else {
            lastServicedRequest = null;
            lastServicedResponse = null;
        }
    }
    public static final int INCREMENT = 10;
    private ApplicationFilterConfig[] filters = new ApplicationFilterConfig[0];
    private int pos = 0;
    private int n = 0;
    private Servlet servlet = null;
    private boolean servletSupportsAsync = false;
    private static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    private static final Class<?>[] classType = new Class[] {
        ServletRequest.class, ServletResponse.class, FilterChain.class
    };
    private static final Class<?>[] classTypeUsedInService = new Class[] {
        ServletRequest.class, ServletResponse.class
    };
    @Override
    public void doFilter ( ServletRequest request, ServletResponse response )
    throws IOException, ServletException {
        if ( Globals.IS_SECURITY_ENABLED ) {
            final ServletRequest req = request;
            final ServletResponse res = response;
            try {
                java.security.AccessController.doPrivileged (
                new java.security.PrivilegedExceptionAction<Void>() {
                    @Override
                    public Void run()
                    throws ServletException, IOException {
                        internalDoFilter ( req, res );
                        return null;
                    }
                }
                );
            } catch ( PrivilegedActionException pe ) {
                Exception e = pe.getException();
                if ( e instanceof ServletException ) {
                    throw ( ServletException ) e;
                } else if ( e instanceof IOException ) {
                    throw ( IOException ) e;
                } else if ( e instanceof RuntimeException ) {
                    throw ( RuntimeException ) e;
                } else {
                    throw new ServletException ( e.getMessage(), e );
                }
            }
        } else {
            internalDoFilter ( request, response );
        }
    }
    private void internalDoFilter ( ServletRequest request,
                                    ServletResponse response )
    throws IOException, ServletException {
        if ( pos < n ) {
            ApplicationFilterConfig filterConfig = filters[pos++];
            try {
                Filter filter = filterConfig.getFilter();
                if ( request.isAsyncSupported() && "false".equalsIgnoreCase (
                            filterConfig.getFilterDef().getAsyncSupported() ) ) {
                    request.setAttribute ( Globals.ASYNC_SUPPORTED_ATTR, Boolean.FALSE );
                }
                if ( Globals.IS_SECURITY_ENABLED ) {
                    final ServletRequest req = request;
                    final ServletResponse res = response;
                    Principal principal =
                        ( ( HttpServletRequest ) req ).getUserPrincipal();
                    Object[] args = new Object[] {req, res, this};
                    SecurityUtil.doAsPrivilege ( "doFilter", filter, classType, args, principal );
                } else {
                    filter.doFilter ( request, response, this );
                }
            } catch ( IOException | ServletException | RuntimeException e ) {
                throw e;
            } catch ( Throwable e ) {
                e = ExceptionUtils.unwrapInvocationTargetException ( e );
                ExceptionUtils.handleThrowable ( e );
                throw new ServletException ( sm.getString ( "filterChain.filter" ), e );
            }
            return;
        }
        try {
            if ( ApplicationDispatcher.WRAP_SAME_OBJECT ) {
                lastServicedRequest.set ( request );
                lastServicedResponse.set ( response );
            }
            if ( request.isAsyncSupported() && !servletSupportsAsync ) {
                request.setAttribute ( Globals.ASYNC_SUPPORTED_ATTR,
                                       Boolean.FALSE );
            }
            if ( ( request instanceof HttpServletRequest ) &&
                    ( response instanceof HttpServletResponse ) &&
                    Globals.IS_SECURITY_ENABLED ) {
                final ServletRequest req = request;
                final ServletResponse res = response;
                Principal principal =
                    ( ( HttpServletRequest ) req ).getUserPrincipal();
                Object[] args = new Object[] {req, res};
                SecurityUtil.doAsPrivilege ( "service",
                                             servlet,
                                             classTypeUsedInService,
                                             args,
                                             principal );
            } else {
                servlet.service ( request, response );
            }
        } catch ( IOException | ServletException | RuntimeException e ) {
            throw e;
        } catch ( Throwable e ) {
            e = ExceptionUtils.unwrapInvocationTargetException ( e );
            ExceptionUtils.handleThrowable ( e );
            throw new ServletException ( sm.getString ( "filterChain.servlet" ), e );
        } finally {
            if ( ApplicationDispatcher.WRAP_SAME_OBJECT ) {
                lastServicedRequest.set ( null );
                lastServicedResponse.set ( null );
            }
        }
    }
    public static ServletRequest getLastServicedRequest() {
        return lastServicedRequest.get();
    }
    public static ServletResponse getLastServicedResponse() {
        return lastServicedResponse.get();
    }
    void addFilter ( ApplicationFilterConfig filterConfig ) {
        for ( ApplicationFilterConfig filter : filters )
            if ( filter == filterConfig ) {
                return;
            }
        if ( n == filters.length ) {
            ApplicationFilterConfig[] newFilters =
                new ApplicationFilterConfig[n + INCREMENT];
            System.arraycopy ( filters, 0, newFilters, 0, n );
            filters = newFilters;
        }
        filters[n++] = filterConfig;
    }
    void release() {
        for ( int i = 0; i < n; i++ ) {
            filters[i] = null;
        }
        n = 0;
        pos = 0;
        servlet = null;
        servletSupportsAsync = false;
    }
    void reuse() {
        pos = 0;
    }
    void setServlet ( Servlet servlet ) {
        this.servlet = servlet;
    }
    void setServletSupportsAsync ( boolean servletSupportsAsync ) {
        this.servletSupportsAsync = servletSupportsAsync;
    }
}
