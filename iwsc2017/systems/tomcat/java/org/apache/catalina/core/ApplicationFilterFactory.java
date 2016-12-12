package org.apache.catalina.core;
import javax.servlet.DispatcherType;
import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import org.apache.catalina.Globals;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.tomcat.util.descriptor.web.FilterMap;
public final class ApplicationFilterFactory {
    private ApplicationFilterFactory() {
    }
    public static ApplicationFilterChain createFilterChain ( ServletRequest request,
            Wrapper wrapper, Servlet servlet ) {
        if ( servlet == null ) {
            return null;
        }
        ApplicationFilterChain filterChain = null;
        if ( request instanceof Request ) {
            Request req = ( Request ) request;
            if ( Globals.IS_SECURITY_ENABLED ) {
                filterChain = new ApplicationFilterChain();
            } else {
                filterChain = ( ApplicationFilterChain ) req.getFilterChain();
                if ( filterChain == null ) {
                    filterChain = new ApplicationFilterChain();
                    req.setFilterChain ( filterChain );
                }
            }
        } else {
            filterChain = new ApplicationFilterChain();
        }
        filterChain.setServlet ( servlet );
        filterChain.setServletSupportsAsync ( wrapper.isAsyncSupported() );
        StandardContext context = ( StandardContext ) wrapper.getParent();
        FilterMap filterMaps[] = context.findFilterMaps();
        if ( ( filterMaps == null ) || ( filterMaps.length == 0 ) ) {
            return ( filterChain );
        }
        DispatcherType dispatcher =
            ( DispatcherType ) request.getAttribute ( Globals.DISPATCHER_TYPE_ATTR );
        String requestPath = null;
        Object attribute = request.getAttribute ( Globals.DISPATCHER_REQUEST_PATH_ATTR );
        if ( attribute != null ) {
            requestPath = attribute.toString();
        }
        String servletName = wrapper.getName();
        for ( int i = 0; i < filterMaps.length; i++ ) {
            if ( !matchDispatcher ( filterMaps[i] , dispatcher ) ) {
                continue;
            }
            if ( !matchFiltersURL ( filterMaps[i], requestPath ) ) {
                continue;
            }
            ApplicationFilterConfig filterConfig = ( ApplicationFilterConfig )
                                                   context.findFilterConfig ( filterMaps[i].getFilterName() );
            if ( filterConfig == null ) {
                continue;
            }
            filterChain.addFilter ( filterConfig );
        }
        for ( int i = 0; i < filterMaps.length; i++ ) {
            if ( !matchDispatcher ( filterMaps[i] , dispatcher ) ) {
                continue;
            }
            if ( !matchFiltersServlet ( filterMaps[i], servletName ) ) {
                continue;
            }
            ApplicationFilterConfig filterConfig = ( ApplicationFilterConfig )
                                                   context.findFilterConfig ( filterMaps[i].getFilterName() );
            if ( filterConfig == null ) {
                continue;
            }
            filterChain.addFilter ( filterConfig );
        }
        return filterChain;
    }
    private static boolean matchFiltersURL ( FilterMap filterMap, String requestPath ) {
        if ( filterMap.getMatchAllUrlPatterns() ) {
            return true;
        }
        if ( requestPath == null ) {
            return false;
        }
        String[] testPaths = filterMap.getURLPatterns();
        for ( int i = 0; i < testPaths.length; i++ ) {
            if ( matchFiltersURL ( testPaths[i], requestPath ) ) {
                return true;
            }
        }
        return false;
    }
    private static boolean matchFiltersURL ( String testPath, String requestPath ) {
        if ( testPath == null ) {
            return false;
        }
        if ( testPath.equals ( requestPath ) ) {
            return true;
        }
        if ( testPath.equals ( "/*" ) ) {
            return true;
        }
        if ( testPath.endsWith ( "/*" ) ) {
            if ( testPath.regionMatches ( 0, requestPath, 0,
                                          testPath.length() - 2 ) ) {
                if ( requestPath.length() == ( testPath.length() - 2 ) ) {
                    return true;
                } else if ( '/' == requestPath.charAt ( testPath.length() - 2 ) ) {
                    return true;
                }
            }
            return false;
        }
        if ( testPath.startsWith ( "*." ) ) {
            int slash = requestPath.lastIndexOf ( '/' );
            int period = requestPath.lastIndexOf ( '.' );
            if ( ( slash >= 0 ) && ( period > slash )
                    && ( period != requestPath.length() - 1 )
                    && ( ( requestPath.length() - period )
                         == ( testPath.length() - 1 ) ) ) {
                return ( testPath.regionMatches ( 2, requestPath, period + 1,
                                                  testPath.length() - 2 ) );
            }
        }
        return false;
    }
    private static boolean matchFiltersServlet ( FilterMap filterMap,
            String servletName ) {
        if ( servletName == null ) {
            return false;
        } else if ( filterMap.getMatchAllServletNames() ) {
            return true;
        } else {
            String[] servletNames = filterMap.getServletNames();
            for ( int i = 0; i < servletNames.length; i++ ) {
                if ( servletName.equals ( servletNames[i] ) ) {
                    return true;
                }
            }
            return false;
        }
    }
    private static boolean matchDispatcher ( FilterMap filterMap, DispatcherType type ) {
        switch ( type ) {
        case FORWARD :
            if ( ( filterMap.getDispatcherMapping() & FilterMap.FORWARD ) > 0 ) {
                return true;
            }
            break;
        case INCLUDE :
            if ( ( filterMap.getDispatcherMapping() & FilterMap.INCLUDE ) > 0 ) {
                return true;
            }
            break;
        case REQUEST :
            if ( ( filterMap.getDispatcherMapping() & FilterMap.REQUEST ) > 0 ) {
                return true;
            }
            break;
        case ERROR :
            if ( ( filterMap.getDispatcherMapping() & FilterMap.ERROR ) > 0 ) {
                return true;
            }
            break;
        case ASYNC :
            if ( ( filterMap.getDispatcherMapping() & FilterMap.ASYNC ) > 0 ) {
                return true;
            }
            break;
        }
        return false;
    }
}
