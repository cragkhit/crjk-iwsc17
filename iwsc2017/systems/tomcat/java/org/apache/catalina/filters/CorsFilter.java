package org.apache.catalina.filters;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.GenericFilter;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public final class CorsFilter extends GenericFilter {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog ( CorsFilter.class );
    private static final StringManager sm = StringManager.getManager ( CorsFilter.class );
    private final Collection<String> allowedOrigins = new HashSet<>();
    private boolean anyOriginAllowed;
    private final Collection<String> allowedHttpMethods = new HashSet<>();
    private final Collection<String> allowedHttpHeaders = new HashSet<>();
    private final Collection<String> exposedHeaders = new HashSet<>();
    private boolean supportsCredentials;
    private long preflightMaxAge;
    private boolean decorateRequest;
    @Override
    public void doFilter ( final ServletRequest servletRequest,
                           final ServletResponse servletResponse, final FilterChain filterChain )
    throws IOException, ServletException {
        if ( ! ( servletRequest instanceof HttpServletRequest ) ||
                ! ( servletResponse instanceof HttpServletResponse ) ) {
            throw new ServletException ( sm.getString ( "corsFilter.onlyHttp" ) );
        }
        HttpServletRequest request = ( HttpServletRequest ) servletRequest;
        HttpServletResponse response = ( HttpServletResponse ) servletResponse;
        CorsFilter.CORSRequestType requestType = checkRequestType ( request );
        if ( decorateRequest ) {
            CorsFilter.decorateCORSProperties ( request, requestType );
        }
        switch ( requestType ) {
        case SIMPLE:
            this.handleSimpleCORS ( request, response, filterChain );
            break;
        case ACTUAL:
            this.handleSimpleCORS ( request, response, filterChain );
            break;
        case PRE_FLIGHT:
            this.handlePreflightCORS ( request, response, filterChain );
            break;
        case NOT_CORS:
            this.handleNonCORS ( request, response, filterChain );
            break;
        default:
            this.handleInvalidCORS ( request, response, filterChain );
            break;
        }
    }
    @Override
    public void init() throws ServletException {
        parseAndStore ( DEFAULT_ALLOWED_ORIGINS, DEFAULT_ALLOWED_HTTP_METHODS,
                        DEFAULT_ALLOWED_HTTP_HEADERS, DEFAULT_EXPOSED_HEADERS,
                        DEFAULT_SUPPORTS_CREDENTIALS, DEFAULT_PREFLIGHT_MAXAGE,
                        DEFAULT_DECORATE_REQUEST );
        String configAllowedOrigins = getInitParameter ( PARAM_CORS_ALLOWED_ORIGINS );
        String configAllowedHttpMethods = getInitParameter ( PARAM_CORS_ALLOWED_METHODS );
        String configAllowedHttpHeaders = getInitParameter ( PARAM_CORS_ALLOWED_HEADERS );
        String configExposedHeaders = getInitParameter ( PARAM_CORS_EXPOSED_HEADERS );
        String configSupportsCredentials = getInitParameter ( PARAM_CORS_SUPPORT_CREDENTIALS );
        String configPreflightMaxAge = getInitParameter ( PARAM_CORS_PREFLIGHT_MAXAGE );
        String configDecorateRequest = getInitParameter ( PARAM_CORS_REQUEST_DECORATE );
        parseAndStore ( configAllowedOrigins, configAllowedHttpMethods,
                        configAllowedHttpHeaders, configExposedHeaders,
                        configSupportsCredentials, configPreflightMaxAge,
                        configDecorateRequest );
    }
    protected void handleSimpleCORS ( final HttpServletRequest request,
                                      final HttpServletResponse response, final FilterChain filterChain )
    throws IOException, ServletException {
        CorsFilter.CORSRequestType requestType = checkRequestType ( request );
        if ( ! ( requestType == CorsFilter.CORSRequestType.SIMPLE ||
                 requestType == CorsFilter.CORSRequestType.ACTUAL ) ) {
            throw new IllegalArgumentException (
                sm.getString ( "corsFilter.wrongType2",
                               CorsFilter.CORSRequestType.SIMPLE,
                               CorsFilter.CORSRequestType.ACTUAL ) );
        }
        final String origin = request
                              .getHeader ( CorsFilter.REQUEST_HEADER_ORIGIN );
        final String method = request.getMethod();
        if ( !isOriginAllowed ( origin ) ) {
            handleInvalidCORS ( request, response, filterChain );
            return;
        }
        if ( !allowedHttpMethods.contains ( method ) ) {
            handleInvalidCORS ( request, response, filterChain );
            return;
        }
        if ( anyOriginAllowed && !supportsCredentials ) {
            response.addHeader (
                CorsFilter.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN,
                "*" );
        } else {
            response.addHeader (
                CorsFilter.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN,
                origin );
        }
        if ( supportsCredentials ) {
            response.addHeader (
                CorsFilter.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS,
                "true" );
        }
        if ( ( exposedHeaders != null ) && ( exposedHeaders.size() > 0 ) ) {
            String exposedHeadersString = join ( exposedHeaders, "," );
            response.addHeader (
                CorsFilter.RESPONSE_HEADER_ACCESS_CONTROL_EXPOSE_HEADERS,
                exposedHeadersString );
        }
        filterChain.doFilter ( request, response );
    }
    protected void handlePreflightCORS ( final HttpServletRequest request,
                                         final HttpServletResponse response, final FilterChain filterChain )
    throws IOException, ServletException {
        CORSRequestType requestType = checkRequestType ( request );
        if ( requestType != CORSRequestType.PRE_FLIGHT ) {
            throw new IllegalArgumentException ( sm.getString ( "corsFilter.wrongType1",
                                                 CORSRequestType.PRE_FLIGHT.name().toLowerCase ( Locale.ENGLISH ) ) );
        }
        final String origin = request
                              .getHeader ( CorsFilter.REQUEST_HEADER_ORIGIN );
        if ( !isOriginAllowed ( origin ) ) {
            handleInvalidCORS ( request, response, filterChain );
            return;
        }
        String accessControlRequestMethod = request.getHeader (
                                                CorsFilter.REQUEST_HEADER_ACCESS_CONTROL_REQUEST_METHOD );
        if ( accessControlRequestMethod == null ) {
            handleInvalidCORS ( request, response, filterChain );
            return;
        } else {
            accessControlRequestMethod = accessControlRequestMethod.trim();
        }
        String accessControlRequestHeadersHeader = request.getHeader (
                    CorsFilter.REQUEST_HEADER_ACCESS_CONTROL_REQUEST_HEADERS );
        List<String> accessControlRequestHeaders = new LinkedList<>();
        if ( accessControlRequestHeadersHeader != null &&
                !accessControlRequestHeadersHeader.trim().isEmpty() ) {
            String[] headers = accessControlRequestHeadersHeader.trim().split (
                                   "," );
            for ( String header : headers ) {
                accessControlRequestHeaders.add ( header.trim().toLowerCase ( Locale.ENGLISH ) );
            }
        }
        if ( !allowedHttpMethods.contains ( accessControlRequestMethod ) ) {
            handleInvalidCORS ( request, response, filterChain );
            return;
        }
        if ( !accessControlRequestHeaders.isEmpty() ) {
            for ( String header : accessControlRequestHeaders ) {
                if ( !allowedHttpHeaders.contains ( header ) ) {
                    handleInvalidCORS ( request, response, filterChain );
                    return;
                }
            }
        }
        if ( supportsCredentials ) {
            response.addHeader (
                CorsFilter.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN,
                origin );
            response.addHeader (
                CorsFilter.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS,
                "true" );
        } else {
            if ( anyOriginAllowed ) {
                response.addHeader (
                    CorsFilter.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN,
                    "*" );
            } else {
                response.addHeader (
                    CorsFilter.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN,
                    origin );
            }
        }
        if ( preflightMaxAge > 0 ) {
            response.addHeader (
                CorsFilter.RESPONSE_HEADER_ACCESS_CONTROL_MAX_AGE,
                String.valueOf ( preflightMaxAge ) );
        }
        response.addHeader (
            CorsFilter.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_METHODS,
            accessControlRequestMethod );
        if ( ( allowedHttpHeaders != null ) && ( !allowedHttpHeaders.isEmpty() ) ) {
            response.addHeader (
                CorsFilter.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_HEADERS,
                join ( allowedHttpHeaders, "," ) );
        }
    }
    private void handleNonCORS ( final HttpServletRequest request,
                                 final HttpServletResponse response, final FilterChain filterChain )
    throws IOException, ServletException {
        filterChain.doFilter ( request, response );
    }
    private void handleInvalidCORS ( final HttpServletRequest request,
                                     final HttpServletResponse response, final FilterChain filterChain ) {
        String origin = request.getHeader ( CorsFilter.REQUEST_HEADER_ORIGIN );
        String method = request.getMethod();
        String accessControlRequestHeaders = request.getHeader (
                REQUEST_HEADER_ACCESS_CONTROL_REQUEST_HEADERS );
        response.setContentType ( "text/plain" );
        response.setStatus ( HttpServletResponse.SC_FORBIDDEN );
        response.resetBuffer();
        if ( log.isDebugEnabled() ) {
            StringBuilder message =
                new StringBuilder ( "Invalid CORS request; Origin=" );
            message.append ( origin );
            message.append ( ";Method=" );
            message.append ( method );
            if ( accessControlRequestHeaders != null ) {
                message.append ( ";Access-Control-Request-Headers=" );
                message.append ( accessControlRequestHeaders );
            }
            log.debug ( message.toString() );
        }
    }
    protected static void decorateCORSProperties (
        final HttpServletRequest request,
        final CORSRequestType corsRequestType ) {
        if ( request == null ) {
            throw new IllegalArgumentException (
                sm.getString ( "corsFilter.nullRequest" ) );
        }
        if ( corsRequestType == null ) {
            throw new IllegalArgumentException (
                sm.getString ( "corsFilter.nullRequestType" ) );
        }
        switch ( corsRequestType ) {
        case SIMPLE:
            request.setAttribute (
                CorsFilter.HTTP_REQUEST_ATTRIBUTE_IS_CORS_REQUEST,
                Boolean.TRUE );
            request.setAttribute ( CorsFilter.HTTP_REQUEST_ATTRIBUTE_ORIGIN,
                                   request.getHeader ( CorsFilter.REQUEST_HEADER_ORIGIN ) );
            request.setAttribute (
                CorsFilter.HTTP_REQUEST_ATTRIBUTE_REQUEST_TYPE,
                corsRequestType.name().toLowerCase ( Locale.ENGLISH ) );
            break;
        case ACTUAL:
            request.setAttribute (
                CorsFilter.HTTP_REQUEST_ATTRIBUTE_IS_CORS_REQUEST,
                Boolean.TRUE );
            request.setAttribute ( CorsFilter.HTTP_REQUEST_ATTRIBUTE_ORIGIN,
                                   request.getHeader ( CorsFilter.REQUEST_HEADER_ORIGIN ) );
            request.setAttribute (
                CorsFilter.HTTP_REQUEST_ATTRIBUTE_REQUEST_TYPE,
                corsRequestType.name().toLowerCase ( Locale.ENGLISH ) );
            break;
        case PRE_FLIGHT:
            request.setAttribute (
                CorsFilter.HTTP_REQUEST_ATTRIBUTE_IS_CORS_REQUEST,
                Boolean.TRUE );
            request.setAttribute ( CorsFilter.HTTP_REQUEST_ATTRIBUTE_ORIGIN,
                                   request.getHeader ( CorsFilter.REQUEST_HEADER_ORIGIN ) );
            request.setAttribute (
                CorsFilter.HTTP_REQUEST_ATTRIBUTE_REQUEST_TYPE,
                corsRequestType.name().toLowerCase ( Locale.ENGLISH ) );
            String headers = request.getHeader (
                                 REQUEST_HEADER_ACCESS_CONTROL_REQUEST_HEADERS );
            if ( headers == null ) {
                headers = "";
            }
            request.setAttribute (
                CorsFilter.HTTP_REQUEST_ATTRIBUTE_REQUEST_HEADERS, headers );
            break;
        case NOT_CORS:
            request.setAttribute (
                CorsFilter.HTTP_REQUEST_ATTRIBUTE_IS_CORS_REQUEST,
                Boolean.FALSE );
            break;
        default:
            break;
        }
    }
    protected static String join ( final Collection<String> elements,
                                   final String joinSeparator ) {
        String separator = ",";
        if ( elements == null ) {
            return null;
        }
        if ( joinSeparator != null ) {
            separator = joinSeparator;
        }
        StringBuilder buffer = new StringBuilder();
        boolean isFirst = true;
        for ( String element : elements ) {
            if ( !isFirst ) {
                buffer.append ( separator );
            } else {
                isFirst = false;
            }
            if ( element != null ) {
                buffer.append ( element );
            }
        }
        return buffer.toString();
    }
    protected CORSRequestType checkRequestType ( final HttpServletRequest request ) {
        CORSRequestType requestType = CORSRequestType.INVALID_CORS;
        if ( request == null ) {
            throw new IllegalArgumentException (
                sm.getString ( "corsFilter.nullRequest" ) );
        }
        String originHeader = request.getHeader ( REQUEST_HEADER_ORIGIN );
        if ( originHeader != null ) {
            if ( originHeader.isEmpty() ) {
                requestType = CORSRequestType.INVALID_CORS;
            } else if ( !isValidOrigin ( originHeader ) ) {
                requestType = CORSRequestType.INVALID_CORS;
            } else if ( isLocalOrigin ( request, originHeader ) ) {
                return CORSRequestType.NOT_CORS;
            } else {
                String method = request.getMethod();
                if ( method != null ) {
                    if ( "OPTIONS".equals ( method ) ) {
                        String accessControlRequestMethodHeader =
                            request.getHeader (
                                REQUEST_HEADER_ACCESS_CONTROL_REQUEST_METHOD );
                        if ( accessControlRequestMethodHeader != null &&
                                !accessControlRequestMethodHeader.isEmpty() ) {
                            requestType = CORSRequestType.PRE_FLIGHT;
                        } else if ( accessControlRequestMethodHeader != null &&
                                    accessControlRequestMethodHeader.isEmpty() ) {
                            requestType = CORSRequestType.INVALID_CORS;
                        } else {
                            requestType = CORSRequestType.ACTUAL;
                        }
                    } else if ( "GET".equals ( method ) || "HEAD".equals ( method ) ) {
                        requestType = CORSRequestType.SIMPLE;
                    } else if ( "POST".equals ( method ) ) {
                        String mediaType = getMediaType ( request.getContentType() );
                        if ( mediaType != null ) {
                            if ( SIMPLE_HTTP_REQUEST_CONTENT_TYPE_VALUES
                                    .contains ( mediaType ) ) {
                                requestType = CORSRequestType.SIMPLE;
                            } else {
                                requestType = CORSRequestType.ACTUAL;
                            }
                        }
                    } else {
                        requestType = CORSRequestType.ACTUAL;
                    }
                }
            }
        } else {
            requestType = CORSRequestType.NOT_CORS;
        }
        return requestType;
    }
    private boolean isLocalOrigin ( HttpServletRequest request, String origin ) {
        StringBuilder target = new StringBuilder();
        String scheme = request.getScheme();
        if ( scheme == null ) {
            return false;
        } else {
            scheme = scheme.toLowerCase ( Locale.ENGLISH );
        }
        target.append ( scheme );
        target.append ( "://" );
        String host = request.getServerName();
        if ( host == null ) {
            return false;
        }
        target.append ( host );
        int port = request.getServerPort();
        if ( "http".equals ( scheme ) && port != 80 ||
                "https".equals ( scheme ) && port != 443 ) {
            target.append ( ':' );
            target.append ( port );
        }
        return origin.equalsIgnoreCase ( target.toString() );
    }
    private String getMediaType ( String contentType ) {
        if ( contentType == null ) {
            return null;
        }
        String result = contentType.toLowerCase ( Locale.ENGLISH );
        int firstSemiColonIndex = result.indexOf ( ';' );
        if ( firstSemiColonIndex > -1 ) {
            result = result.substring ( 0, firstSemiColonIndex );
        }
        result = result.trim();
        return result;
    }
    private boolean isOriginAllowed ( final String origin ) {
        if ( anyOriginAllowed ) {
            return true;
        }
        return allowedOrigins.contains ( origin );
    }
    private void parseAndStore ( final String allowedOrigins,
                                 final String allowedHttpMethods, final String allowedHttpHeaders,
                                 final String exposedHeaders, final String supportsCredentials,
                                 final String preflightMaxAge, final String decorateRequest )
    throws ServletException {
        if ( allowedOrigins != null ) {
            if ( allowedOrigins.trim().equals ( "*" ) ) {
                this.anyOriginAllowed = true;
            } else {
                this.anyOriginAllowed = false;
                Set<String> setAllowedOrigins =
                    parseStringToSet ( allowedOrigins );
                this.allowedOrigins.clear();
                this.allowedOrigins.addAll ( setAllowedOrigins );
            }
        }
        if ( allowedHttpMethods != null ) {
            Set<String> setAllowedHttpMethods =
                parseStringToSet ( allowedHttpMethods );
            this.allowedHttpMethods.clear();
            this.allowedHttpMethods.addAll ( setAllowedHttpMethods );
        }
        if ( allowedHttpHeaders != null ) {
            Set<String> setAllowedHttpHeaders =
                parseStringToSet ( allowedHttpHeaders );
            Set<String> lowerCaseHeaders = new HashSet<>();
            for ( String header : setAllowedHttpHeaders ) {
                String lowerCase = header.toLowerCase ( Locale.ENGLISH );
                lowerCaseHeaders.add ( lowerCase );
            }
            this.allowedHttpHeaders.clear();
            this.allowedHttpHeaders.addAll ( lowerCaseHeaders );
        }
        if ( exposedHeaders != null ) {
            Set<String> setExposedHeaders = parseStringToSet ( exposedHeaders );
            this.exposedHeaders.clear();
            this.exposedHeaders.addAll ( setExposedHeaders );
        }
        if ( supportsCredentials != null ) {
            this.supportsCredentials = Boolean
                                       .parseBoolean ( supportsCredentials );
        }
        if ( preflightMaxAge != null ) {
            try {
                if ( !preflightMaxAge.isEmpty() ) {
                    this.preflightMaxAge = Long.parseLong ( preflightMaxAge );
                } else {
                    this.preflightMaxAge = 0L;
                }
            } catch ( NumberFormatException e ) {
                throw new ServletException (
                    sm.getString ( "corsFilter.invalidPreflightMaxAge" ), e );
            }
        }
        if ( decorateRequest != null ) {
            this.decorateRequest = Boolean.parseBoolean ( decorateRequest );
        }
    }
    private Set<String> parseStringToSet ( final String data ) {
        String[] splits;
        if ( data != null && data.length() > 0 ) {
            splits = data.split ( "," );
        } else {
            splits = new String[] {};
        }
        Set<String> set = new HashSet<>();
        if ( splits.length > 0 ) {
            for ( String split : splits ) {
                set.add ( split.trim() );
            }
        }
        return set;
    }
    protected static boolean isValidOrigin ( String origin ) {
        if ( origin.contains ( "%" ) ) {
            return false;
        }
        if ( "null".equals ( origin ) ) {
            return true;
        }
        if ( origin.startsWith ( "file://" ) ) {
            return true;
        }
        URI originURI;
        try {
            originURI = new URI ( origin );
        } catch ( URISyntaxException e ) {
            return false;
        }
        return originURI.getScheme() != null;
    }
    public boolean isAnyOriginAllowed() {
        return anyOriginAllowed;
    }
    public Collection<String> getExposedHeaders() {
        return exposedHeaders;
    }
    public boolean isSupportsCredentials() {
        return supportsCredentials;
    }
    public long getPreflightMaxAge() {
        return preflightMaxAge;
    }
    public Collection<String> getAllowedOrigins() {
        return allowedOrigins;
    }
    public Collection<String> getAllowedHttpMethods() {
        return allowedHttpMethods;
    }
    public Collection<String> getAllowedHttpHeaders() {
        return allowedHttpHeaders;
    }
    public static final String RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN =
        "Access-Control-Allow-Origin";
    public static final String RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS =
        "Access-Control-Allow-Credentials";
    public static final String RESPONSE_HEADER_ACCESS_CONTROL_EXPOSE_HEADERS =
        "Access-Control-Expose-Headers";
    public static final String RESPONSE_HEADER_ACCESS_CONTROL_MAX_AGE =
        "Access-Control-Max-Age";
    public static final String RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_METHODS =
        "Access-Control-Allow-Methods";
    public static final String RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_HEADERS =
        "Access-Control-Allow-Headers";
    public static final String REQUEST_HEADER_ORIGIN = "Origin";
    public static final String REQUEST_HEADER_ACCESS_CONTROL_REQUEST_METHOD =
        "Access-Control-Request-Method";
    public static final String REQUEST_HEADER_ACCESS_CONTROL_REQUEST_HEADERS =
        "Access-Control-Request-Headers";
    public static final String HTTP_REQUEST_ATTRIBUTE_PREFIX = "cors.";
    public static final String HTTP_REQUEST_ATTRIBUTE_ORIGIN =
        HTTP_REQUEST_ATTRIBUTE_PREFIX + "request.origin";
    public static final String HTTP_REQUEST_ATTRIBUTE_IS_CORS_REQUEST =
        HTTP_REQUEST_ATTRIBUTE_PREFIX + "isCorsRequest";
    public static final String HTTP_REQUEST_ATTRIBUTE_REQUEST_TYPE =
        HTTP_REQUEST_ATTRIBUTE_PREFIX + "request.type";
    public static final String HTTP_REQUEST_ATTRIBUTE_REQUEST_HEADERS =
        HTTP_REQUEST_ATTRIBUTE_PREFIX + "request.headers";
    protected static enum CORSRequestType {
        SIMPLE,
        ACTUAL,
        PRE_FLIGHT,
        NOT_CORS,
        INVALID_CORS
    }
    public static final Collection<String> SIMPLE_HTTP_REQUEST_CONTENT_TYPE_VALUES =
        new HashSet<> ( Arrays.asList ( "application/x-www-form-urlencoded",
                                        "multipart/form-data", "text/plain" ) );
    public static final String DEFAULT_ALLOWED_ORIGINS = "*";
    public static final String DEFAULT_ALLOWED_HTTP_METHODS =
        "GET,POST,HEAD,OPTIONS";
    public static final String DEFAULT_PREFLIGHT_MAXAGE = "1800";
    public static final String DEFAULT_SUPPORTS_CREDENTIALS = "true";
    public static final String DEFAULT_ALLOWED_HTTP_HEADERS =
        "Origin,Accept,X-Requested-With,Content-Type," +
        "Access-Control-Request-Method,Access-Control-Request-Headers";
    public static final String DEFAULT_EXPOSED_HEADERS = "";
    public static final String DEFAULT_DECORATE_REQUEST = "true";
    public static final String PARAM_CORS_ALLOWED_ORIGINS =
        "cors.allowed.origins";
    public static final String PARAM_CORS_SUPPORT_CREDENTIALS =
        "cors.support.credentials";
    public static final String PARAM_CORS_EXPOSED_HEADERS =
        "cors.exposed.headers";
    public static final String PARAM_CORS_ALLOWED_HEADERS =
        "cors.allowed.headers";
    public static final String PARAM_CORS_ALLOWED_METHODS =
        "cors.allowed.methods";
    public static final String PARAM_CORS_PREFLIGHT_MAXAGE =
        "cors.preflight.maxage";
    public static final String PARAM_CORS_REQUEST_DECORATE =
        "cors.request.decorate";
}
