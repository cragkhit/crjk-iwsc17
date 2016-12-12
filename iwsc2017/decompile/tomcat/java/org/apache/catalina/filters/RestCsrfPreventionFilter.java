package org.apache.catalina.filters;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Objects;
import javax.servlet.ServletException;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.FilterChain;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
public class RestCsrfPreventionFilter extends CsrfPreventionFilterBase {
    private static final Pattern NON_MODIFYING_METHODS_PATTERN;
    private static final Predicate<String> nonModifyingMethods;
    private Set<String> pathsAcceptingParams;
    private String pathsDelimiter;
    public RestCsrfPreventionFilter() {
        this.pathsAcceptingParams = new HashSet<String>();
        this.pathsDelimiter = ",";
    }
    public void doFilter ( final ServletRequest request, final ServletResponse response, final FilterChain chain ) throws IOException, ServletException {
        if ( request instanceof HttpServletRequest && response instanceof HttpServletResponse ) {
            MethodType mType = MethodType.MODIFYING_METHOD;
            if ( RestCsrfPreventionFilter.nonModifyingMethods.test ( ( ( HttpServletRequest ) request ).getMethod() ) ) {
                mType = MethodType.NON_MODIFYING_METHOD;
            }
            RestCsrfPreventionStrategy strategy = null;
            switch ( mType ) {
            case NON_MODIFYING_METHOD: {
                strategy = new FetchRequest();
                break;
            }
            default: {
                strategy = new StateChangingRequest();
                break;
            }
            }
            if ( !strategy.apply ( ( HttpServletRequest ) request, ( HttpServletResponse ) response ) ) {
                return;
            }
        }
        chain.doFilter ( request, response );
    }
    public void setPathsAcceptingParams ( final String pathsList ) {
        if ( Objects.nonNull ( pathsList ) ) {
            Arrays.asList ( pathsList.split ( this.pathsDelimiter ) ).forEach ( e -> this.pathsAcceptingParams.add ( e.trim() ) );
        }
    }
    public Set<String> getPathsAcceptingParams() {
        return this.pathsAcceptingParams;
    }
    static {
        NON_MODIFYING_METHODS_PATTERN = Pattern.compile ( "GET|HEAD|OPTIONS" );
        nonModifyingMethods = ( m -> Objects.nonNull ( m ) && RestCsrfPreventionFilter.NON_MODIFYING_METHODS_PATTERN.matcher ( m ).matches() );
    }
    private enum MethodType {
        NON_MODIFYING_METHOD,
        MODIFYING_METHOD;
    }
    private interface RestCsrfPreventionStrategy {
        public static final NonceSupplier<HttpServletRequest, String> nonceFromRequestHeader = ( r, k ) -> r.getHeader ( k );
        public static final NonceSupplier<HttpServletRequest, String[]> nonceFromRequestParams = ( r, k ) -> r.getParameterValues ( k );
        public static final NonceSupplier<HttpSession, String> nonceFromSession = ( s, k ) -> Objects.isNull ( s ) ? null : ( ( String ) s.getAttribute ( k ) );
        public static final NonceConsumer<HttpServletResponse> nonceToResponse = ( r, k, v ) -> r.setHeader ( k, v );
        public static final NonceConsumer<HttpSession> nonceToSession = ( s, k, v ) -> s.setAttribute ( k, ( Object ) v );
        boolean apply ( HttpServletRequest p0, HttpServletResponse p1 ) throws IOException;
    }
    private class StateChangingRequest implements RestCsrfPreventionStrategy {
        @Override
        public boolean apply ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
            if ( this.isValidStateChangingRequest ( this.extractNonceFromRequest ( request ), StateChangingRequest.nonceFromSession.getNonce ( request.getSession ( false ), "org.apache.catalina.filters.CSRF_REST_NONCE" ) ) ) {
                return true;
            }
            StateChangingRequest.nonceToResponse.setNonce ( response, "X-CSRF-Token", "Required" );
            response.sendError ( RestCsrfPreventionFilter.this.getDenyStatus(), FilterBase.sm.getString ( "restCsrfPreventionFilter.invalidNonce" ) );
            return false;
        }
        private boolean isValidStateChangingRequest ( final String reqNonce, final String sessionNonce ) {
            return Objects.nonNull ( reqNonce ) && Objects.nonNull ( sessionNonce ) && Objects.equals ( reqNonce, sessionNonce );
        }
        private String extractNonceFromRequest ( final HttpServletRequest request ) {
            String nonceFromRequest = StateChangingRequest.nonceFromRequestHeader.getNonce ( request, "X-CSRF-Token" );
            if ( ( Objects.isNull ( nonceFromRequest ) || Objects.equals ( "", nonceFromRequest ) ) && !RestCsrfPreventionFilter.this.getPathsAcceptingParams().isEmpty() && RestCsrfPreventionFilter.this.getPathsAcceptingParams().contains ( RestCsrfPreventionFilter.this.getRequestedPath ( request ) ) ) {
                nonceFromRequest = this.extractNonceFromRequestParams ( request );
            }
            return nonceFromRequest;
        }
        private String extractNonceFromRequestParams ( final HttpServletRequest request ) {
            final String[] params = StateChangingRequest.nonceFromRequestParams.getNonce ( request, "X-CSRF-Token" );
            if ( Objects.nonNull ( params ) && params.length > 0 ) {
                final String nonce = params[0];
                for ( final String param : params ) {
                    if ( !Objects.equals ( param, nonce ) ) {
                        return null;
                    }
                }
                return nonce;
            }
            return null;
        }
    }
    private class FetchRequest implements RestCsrfPreventionStrategy {
        private final Predicate<String> fetchRequest;
        private FetchRequest() {
            this.fetchRequest = ( s -> "Fetch".equalsIgnoreCase ( s ) );
        }
        @Override
        public boolean apply ( final HttpServletRequest request, final HttpServletResponse response ) {
            if ( this.fetchRequest.test ( FetchRequest.nonceFromRequestHeader.getNonce ( request, "X-CSRF-Token" ) ) ) {
                String nonceFromSessionStr = FetchRequest.nonceFromSession.getNonce ( request.getSession ( false ), "org.apache.catalina.filters.CSRF_REST_NONCE" );
                if ( nonceFromSessionStr == null ) {
                    nonceFromSessionStr = RestCsrfPreventionFilter.this.generateNonce();
                    FetchRequest.nonceToSession.setNonce ( Objects.requireNonNull ( request.getSession ( true ) ), "org.apache.catalina.filters.CSRF_REST_NONCE", nonceFromSessionStr );
                }
                FetchRequest.nonceToResponse.setNonce ( response, "X-CSRF-Token", nonceFromSessionStr );
            }
            return true;
        }
    }
    @FunctionalInterface
    private interface NonceConsumer<T> {
        void setNonce ( T p0, String p1, String p2 );
    }
    @FunctionalInterface
    private interface NonceSupplier<T, R> {
        R getNonce ( T p0, String p1 );
    }
}
