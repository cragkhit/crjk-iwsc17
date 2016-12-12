package org.apache.catalina.filters;
import java.util.Arrays;
import org.apache.juli.logging.LogFactory;
import java.net.URISyntaxException;
import java.net.URI;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Locale;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.FilterChain;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import java.util.HashSet;
import java.util.Collection;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import javax.servlet.GenericFilter;
public final class CorsFilter extends GenericFilter {
    private static final long serialVersionUID = 1L;
    private static final Log log;
    private static final StringManager sm;
    private final Collection<String> allowedOrigins;
    private boolean anyOriginAllowed;
    private final Collection<String> allowedHttpMethods;
    private final Collection<String> allowedHttpHeaders;
    private final Collection<String> exposedHeaders;
    private boolean supportsCredentials;
    private long preflightMaxAge;
    private boolean decorateRequest;
    public static final String RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    public static final String RESPONSE_HEADER_ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    public static final String RESPONSE_HEADER_ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
    public static final String RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    public static final String RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    public static final String REQUEST_HEADER_ORIGIN = "Origin";
    public static final String REQUEST_HEADER_ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
    public static final String REQUEST_HEADER_ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
    public static final String HTTP_REQUEST_ATTRIBUTE_PREFIX = "cors.";
    public static final String HTTP_REQUEST_ATTRIBUTE_ORIGIN = "cors.request.origin";
    public static final String HTTP_REQUEST_ATTRIBUTE_IS_CORS_REQUEST = "cors.isCorsRequest";
    public static final String HTTP_REQUEST_ATTRIBUTE_REQUEST_TYPE = "cors.request.type";
    public static final String HTTP_REQUEST_ATTRIBUTE_REQUEST_HEADERS = "cors.request.headers";
    public static final Collection<String> SIMPLE_HTTP_REQUEST_CONTENT_TYPE_VALUES;
    public static final String DEFAULT_ALLOWED_ORIGINS = "*";
    public static final String DEFAULT_ALLOWED_HTTP_METHODS = "GET,POST,HEAD,OPTIONS";
    public static final String DEFAULT_PREFLIGHT_MAXAGE = "1800";
    public static final String DEFAULT_SUPPORTS_CREDENTIALS = "true";
    public static final String DEFAULT_ALLOWED_HTTP_HEADERS = "Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers";
    public static final String DEFAULT_EXPOSED_HEADERS = "";
    public static final String DEFAULT_DECORATE_REQUEST = "true";
    public static final String PARAM_CORS_ALLOWED_ORIGINS = "cors.allowed.origins";
    public static final String PARAM_CORS_SUPPORT_CREDENTIALS = "cors.support.credentials";
    public static final String PARAM_CORS_EXPOSED_HEADERS = "cors.exposed.headers";
    public static final String PARAM_CORS_ALLOWED_HEADERS = "cors.allowed.headers";
    public static final String PARAM_CORS_ALLOWED_METHODS = "cors.allowed.methods";
    public static final String PARAM_CORS_PREFLIGHT_MAXAGE = "cors.preflight.maxage";
    public static final String PARAM_CORS_REQUEST_DECORATE = "cors.request.decorate";
    public CorsFilter() {
        this.allowedOrigins = new HashSet<String>();
        this.allowedHttpMethods = new HashSet<String>();
        this.allowedHttpHeaders = new HashSet<String>();
        this.exposedHeaders = new HashSet<String>();
    }
    public void doFilter ( final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain ) throws IOException, ServletException {
        if ( ! ( servletRequest instanceof HttpServletRequest ) || ! ( servletResponse instanceof HttpServletResponse ) ) {
            throw new ServletException ( CorsFilter.sm.getString ( "corsFilter.onlyHttp" ) );
        }
        final HttpServletRequest request = ( HttpServletRequest ) servletRequest;
        final HttpServletResponse response = ( HttpServletResponse ) servletResponse;
        final CORSRequestType requestType = this.checkRequestType ( request );
        if ( this.decorateRequest ) {
            decorateCORSProperties ( request, requestType );
        }
        switch ( requestType ) {
        case SIMPLE: {
            this.handleSimpleCORS ( request, response, filterChain );
            break;
        }
        case ACTUAL: {
            this.handleSimpleCORS ( request, response, filterChain );
            break;
        }
        case PRE_FLIGHT: {
            this.handlePreflightCORS ( request, response, filterChain );
            break;
        }
        case NOT_CORS: {
            this.handleNonCORS ( request, response, filterChain );
            break;
        }
        default: {
            this.handleInvalidCORS ( request, response, filterChain );
            break;
        }
        }
    }
    public void init() throws ServletException {
        this.parseAndStore ( "*", "GET,POST,HEAD,OPTIONS", "Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers", "", "true", "1800", "true" );
        final String configAllowedOrigins = this.getInitParameter ( "cors.allowed.origins" );
        final String configAllowedHttpMethods = this.getInitParameter ( "cors.allowed.methods" );
        final String configAllowedHttpHeaders = this.getInitParameter ( "cors.allowed.headers" );
        final String configExposedHeaders = this.getInitParameter ( "cors.exposed.headers" );
        final String configSupportsCredentials = this.getInitParameter ( "cors.support.credentials" );
        final String configPreflightMaxAge = this.getInitParameter ( "cors.preflight.maxage" );
        final String configDecorateRequest = this.getInitParameter ( "cors.request.decorate" );
        this.parseAndStore ( configAllowedOrigins, configAllowedHttpMethods, configAllowedHttpHeaders, configExposedHeaders, configSupportsCredentials, configPreflightMaxAge, configDecorateRequest );
    }
    protected void handleSimpleCORS ( final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain ) throws IOException, ServletException {
        final CORSRequestType requestType = this.checkRequestType ( request );
        if ( requestType != CORSRequestType.SIMPLE && requestType != CORSRequestType.ACTUAL ) {
            throw new IllegalArgumentException ( CorsFilter.sm.getString ( "corsFilter.wrongType2", CORSRequestType.SIMPLE, CORSRequestType.ACTUAL ) );
        }
        final String origin = request.getHeader ( "Origin" );
        final String method = request.getMethod();
        if ( !this.isOriginAllowed ( origin ) ) {
            this.handleInvalidCORS ( request, response, filterChain );
            return;
        }
        if ( !this.allowedHttpMethods.contains ( method ) ) {
            this.handleInvalidCORS ( request, response, filterChain );
            return;
        }
        if ( this.anyOriginAllowed && !this.supportsCredentials ) {
            response.addHeader ( "Access-Control-Allow-Origin", "*" );
        } else {
            response.addHeader ( "Access-Control-Allow-Origin", origin );
        }
        if ( this.supportsCredentials ) {
            response.addHeader ( "Access-Control-Allow-Credentials", "true" );
        }
        if ( this.exposedHeaders != null && this.exposedHeaders.size() > 0 ) {
            final String exposedHeadersString = join ( this.exposedHeaders, "," );
            response.addHeader ( "Access-Control-Expose-Headers", exposedHeadersString );
        }
        filterChain.doFilter ( ( ServletRequest ) request, ( ServletResponse ) response );
    }
    protected void handlePreflightCORS ( final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain ) throws IOException, ServletException {
        final CORSRequestType requestType = this.checkRequestType ( request );
        if ( requestType != CORSRequestType.PRE_FLIGHT ) {
            throw new IllegalArgumentException ( CorsFilter.sm.getString ( "corsFilter.wrongType1", CORSRequestType.PRE_FLIGHT.name().toLowerCase ( Locale.ENGLISH ) ) );
        }
        final String origin = request.getHeader ( "Origin" );
        if ( !this.isOriginAllowed ( origin ) ) {
            this.handleInvalidCORS ( request, response, filterChain );
            return;
        }
        String accessControlRequestMethod = request.getHeader ( "Access-Control-Request-Method" );
        if ( accessControlRequestMethod == null ) {
            this.handleInvalidCORS ( request, response, filterChain );
            return;
        }
        accessControlRequestMethod = accessControlRequestMethod.trim();
        final String accessControlRequestHeadersHeader = request.getHeader ( "Access-Control-Request-Headers" );
        final List<String> accessControlRequestHeaders = new LinkedList<String>();
        if ( accessControlRequestHeadersHeader != null && !accessControlRequestHeadersHeader.trim().isEmpty() ) {
            final String[] split;
            final String[] headers = split = accessControlRequestHeadersHeader.trim().split ( "," );
            for ( final String header : split ) {
                accessControlRequestHeaders.add ( header.trim().toLowerCase ( Locale.ENGLISH ) );
            }
        }
        if ( !this.allowedHttpMethods.contains ( accessControlRequestMethod ) ) {
            this.handleInvalidCORS ( request, response, filterChain );
            return;
        }
        if ( !accessControlRequestHeaders.isEmpty() ) {
            for ( final String header2 : accessControlRequestHeaders ) {
                if ( !this.allowedHttpHeaders.contains ( header2 ) ) {
                    this.handleInvalidCORS ( request, response, filterChain );
                    return;
                }
            }
        }
        if ( this.supportsCredentials ) {
            response.addHeader ( "Access-Control-Allow-Origin", origin );
            response.addHeader ( "Access-Control-Allow-Credentials", "true" );
        } else if ( this.anyOriginAllowed ) {
            response.addHeader ( "Access-Control-Allow-Origin", "*" );
        } else {
            response.addHeader ( "Access-Control-Allow-Origin", origin );
        }
        if ( this.preflightMaxAge > 0L ) {
            response.addHeader ( "Access-Control-Max-Age", String.valueOf ( this.preflightMaxAge ) );
        }
        response.addHeader ( "Access-Control-Allow-Methods", accessControlRequestMethod );
        if ( this.allowedHttpHeaders != null && !this.allowedHttpHeaders.isEmpty() ) {
            response.addHeader ( "Access-Control-Allow-Headers", join ( this.allowedHttpHeaders, "," ) );
        }
    }
    private void handleNonCORS ( final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain ) throws IOException, ServletException {
        filterChain.doFilter ( ( ServletRequest ) request, ( ServletResponse ) response );
    }
    private void handleInvalidCORS ( final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain ) {
        final String origin = request.getHeader ( "Origin" );
        final String method = request.getMethod();
        final String accessControlRequestHeaders = request.getHeader ( "Access-Control-Request-Headers" );
        response.setContentType ( "text/plain" );
        response.setStatus ( 403 );
        response.resetBuffer();
        if ( CorsFilter.log.isDebugEnabled() ) {
            final StringBuilder message = new StringBuilder ( "Invalid CORS request; Origin=" );
            message.append ( origin );
            message.append ( ";Method=" );
            message.append ( method );
            if ( accessControlRequestHeaders != null ) {
                message.append ( ";Access-Control-Request-Headers=" );
                message.append ( accessControlRequestHeaders );
            }
            CorsFilter.log.debug ( message.toString() );
        }
    }
    protected static void decorateCORSProperties ( final HttpServletRequest request, final CORSRequestType corsRequestType ) {
        if ( request == null ) {
            throw new IllegalArgumentException ( CorsFilter.sm.getString ( "corsFilter.nullRequest" ) );
        }
        if ( corsRequestType == null ) {
            throw new IllegalArgumentException ( CorsFilter.sm.getString ( "corsFilter.nullRequestType" ) );
        }
        switch ( corsRequestType ) {
        case SIMPLE: {
            request.setAttribute ( "cors.isCorsRequest", ( Object ) Boolean.TRUE );
            request.setAttribute ( "cors.request.origin", ( Object ) request.getHeader ( "Origin" ) );
            request.setAttribute ( "cors.request.type", ( Object ) corsRequestType.name().toLowerCase ( Locale.ENGLISH ) );
            break;
        }
        case ACTUAL: {
            request.setAttribute ( "cors.isCorsRequest", ( Object ) Boolean.TRUE );
            request.setAttribute ( "cors.request.origin", ( Object ) request.getHeader ( "Origin" ) );
            request.setAttribute ( "cors.request.type", ( Object ) corsRequestType.name().toLowerCase ( Locale.ENGLISH ) );
            break;
        }
        case PRE_FLIGHT: {
            request.setAttribute ( "cors.isCorsRequest", ( Object ) Boolean.TRUE );
            request.setAttribute ( "cors.request.origin", ( Object ) request.getHeader ( "Origin" ) );
            request.setAttribute ( "cors.request.type", ( Object ) corsRequestType.name().toLowerCase ( Locale.ENGLISH ) );
            String headers = request.getHeader ( "Access-Control-Request-Headers" );
            if ( headers == null ) {
                headers = "";
            }
            request.setAttribute ( "cors.request.headers", ( Object ) headers );
            break;
        }
        case NOT_CORS: {
            request.setAttribute ( "cors.isCorsRequest", ( Object ) Boolean.FALSE );
            break;
        }
        }
    }
    protected static String join ( final Collection<String> elements, final String joinSeparator ) {
        String separator = ",";
        if ( elements == null ) {
            return null;
        }
        if ( joinSeparator != null ) {
            separator = joinSeparator;
        }
        final StringBuilder buffer = new StringBuilder();
        boolean isFirst = true;
        for ( final String element : elements ) {
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
            throw new IllegalArgumentException ( CorsFilter.sm.getString ( "corsFilter.nullRequest" ) );
        }
        final String originHeader = request.getHeader ( "Origin" );
        if ( originHeader != null ) {
            if ( originHeader.isEmpty() ) {
                requestType = CORSRequestType.INVALID_CORS;
            } else if ( !isValidOrigin ( originHeader ) ) {
                requestType = CORSRequestType.INVALID_CORS;
            } else {
                if ( this.isLocalOrigin ( request, originHeader ) ) {
                    return CORSRequestType.NOT_CORS;
                }
                final String method = request.getMethod();
                if ( method != null ) {
                    if ( "OPTIONS".equals ( method ) ) {
                        final String accessControlRequestMethodHeader = request.getHeader ( "Access-Control-Request-Method" );
                        if ( accessControlRequestMethodHeader != null && !accessControlRequestMethodHeader.isEmpty() ) {
                            requestType = CORSRequestType.PRE_FLIGHT;
                        } else if ( accessControlRequestMethodHeader != null && accessControlRequestMethodHeader.isEmpty() ) {
                            requestType = CORSRequestType.INVALID_CORS;
                        } else {
                            requestType = CORSRequestType.ACTUAL;
                        }
                    } else if ( "GET".equals ( method ) || "HEAD".equals ( method ) ) {
                        requestType = CORSRequestType.SIMPLE;
                    } else if ( "POST".equals ( method ) ) {
                        final String mediaType = this.getMediaType ( request.getContentType() );
                        if ( mediaType != null ) {
                            if ( CorsFilter.SIMPLE_HTTP_REQUEST_CONTENT_TYPE_VALUES.contains ( mediaType ) ) {
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
    private boolean isLocalOrigin ( final HttpServletRequest request, final String origin ) {
        final StringBuilder target = new StringBuilder();
        String scheme = request.getScheme();
        if ( scheme == null ) {
            return false;
        }
        scheme = scheme.toLowerCase ( Locale.ENGLISH );
        target.append ( scheme );
        target.append ( "://" );
        final String host = request.getServerName();
        if ( host == null ) {
            return false;
        }
        target.append ( host );
        final int port = request.getServerPort();
        if ( ( "http".equals ( scheme ) && port != 80 ) || ( "https".equals ( scheme ) && port != 443 ) ) {
            target.append ( ':' );
            target.append ( port );
        }
        return origin.equalsIgnoreCase ( target.toString() );
    }
    private String getMediaType ( final String contentType ) {
        if ( contentType == null ) {
            return null;
        }
        String result = contentType.toLowerCase ( Locale.ENGLISH );
        final int firstSemiColonIndex = result.indexOf ( 59 );
        if ( firstSemiColonIndex > -1 ) {
            result = result.substring ( 0, firstSemiColonIndex );
        }
        result = result.trim();
        return result;
    }
    private boolean isOriginAllowed ( final String origin ) {
        return this.anyOriginAllowed || this.allowedOrigins.contains ( origin );
    }
    private void parseAndStore ( final String allowedOrigins, final String allowedHttpMethods, final String allowedHttpHeaders, final String exposedHeaders, final String supportsCredentials, final String preflightMaxAge, final String decorateRequest ) throws ServletException {
        if ( allowedOrigins != null ) {
            if ( allowedOrigins.trim().equals ( "*" ) ) {
                this.anyOriginAllowed = true;
            } else {
                this.anyOriginAllowed = false;
                final Set<String> setAllowedOrigins = this.parseStringToSet ( allowedOrigins );
                this.allowedOrigins.clear();
                this.allowedOrigins.addAll ( setAllowedOrigins );
            }
        }
        if ( allowedHttpMethods != null ) {
            final Set<String> setAllowedHttpMethods = this.parseStringToSet ( allowedHttpMethods );
            this.allowedHttpMethods.clear();
            this.allowedHttpMethods.addAll ( setAllowedHttpMethods );
        }
        if ( allowedHttpHeaders != null ) {
            final Set<String> setAllowedHttpHeaders = this.parseStringToSet ( allowedHttpHeaders );
            final Set<String> lowerCaseHeaders = new HashSet<String>();
            for ( final String header : setAllowedHttpHeaders ) {
                final String lowerCase = header.toLowerCase ( Locale.ENGLISH );
                lowerCaseHeaders.add ( lowerCase );
            }
            this.allowedHttpHeaders.clear();
            this.allowedHttpHeaders.addAll ( lowerCaseHeaders );
        }
        if ( exposedHeaders != null ) {
            final Set<String> setExposedHeaders = this.parseStringToSet ( exposedHeaders );
            this.exposedHeaders.clear();
            this.exposedHeaders.addAll ( setExposedHeaders );
        }
        if ( supportsCredentials != null ) {
            this.supportsCredentials = Boolean.parseBoolean ( supportsCredentials );
        }
        if ( preflightMaxAge != null ) {
            try {
                if ( !preflightMaxAge.isEmpty() ) {
                    this.preflightMaxAge = Long.parseLong ( preflightMaxAge );
                } else {
                    this.preflightMaxAge = 0L;
                }
            } catch ( NumberFormatException e ) {
                throw new ServletException ( CorsFilter.sm.getString ( "corsFilter.invalidPreflightMaxAge" ), ( Throwable ) e );
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
            splits = new String[0];
        }
        final Set<String> set = new HashSet<String>();
        if ( splits.length > 0 ) {
            for ( final String split : splits ) {
                set.add ( split.trim() );
            }
        }
        return set;
    }
    protected static boolean isValidOrigin ( final String origin ) {
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
        return this.anyOriginAllowed;
    }
    public Collection<String> getExposedHeaders() {
        return this.exposedHeaders;
    }
    public boolean isSupportsCredentials() {
        return this.supportsCredentials;
    }
    public long getPreflightMaxAge() {
        return this.preflightMaxAge;
    }
    public Collection<String> getAllowedOrigins() {
        return this.allowedOrigins;
    }
    public Collection<String> getAllowedHttpMethods() {
        return this.allowedHttpMethods;
    }
    public Collection<String> getAllowedHttpHeaders() {
        return this.allowedHttpHeaders;
    }
    static {
        log = LogFactory.getLog ( CorsFilter.class );
        sm = StringManager.getManager ( CorsFilter.class );
        SIMPLE_HTTP_REQUEST_CONTENT_TYPE_VALUES = new HashSet<String> ( Arrays.asList ( "application/x-www-form-urlencoded", "multipart/form-data", "text/plain" ) );
    }
    protected enum CORSRequestType {
        SIMPLE,
        ACTUAL,
        PRE_FLIGHT,
        NOT_CORS,
        INVALID_CORS;
    }
}
