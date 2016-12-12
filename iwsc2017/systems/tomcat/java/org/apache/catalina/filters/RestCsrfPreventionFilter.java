package org.apache.catalina.filters;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
public class RestCsrfPreventionFilter extends CsrfPreventionFilterBase {
    private static enum MethodType {
        NON_MODIFYING_METHOD, MODIFYING_METHOD
    }
    private static final Pattern NON_MODIFYING_METHODS_PATTERN = Pattern.compile ( "GET|HEAD|OPTIONS" );
    private static final Predicate<String> nonModifyingMethods = m -> Objects.nonNull ( m ) &&
            NON_MODIFYING_METHODS_PATTERN.matcher ( m ).matches();
    private Set<String> pathsAcceptingParams = new HashSet<>();
    private String pathsDelimiter = ",";
    @Override
    public void doFilter ( ServletRequest request, ServletResponse response, FilterChain chain )
    throws IOException, ServletException {
        if ( request instanceof HttpServletRequest &&
                response instanceof HttpServletResponse ) {
            MethodType mType = MethodType.MODIFYING_METHOD;
            if ( nonModifyingMethods.test ( ( ( HttpServletRequest ) request ).getMethod() ) ) {
                mType = MethodType.NON_MODIFYING_METHOD;
            }
            RestCsrfPreventionStrategy strategy;
            switch ( mType ) {
            case NON_MODIFYING_METHOD:
                strategy = new FetchRequest();
                break;
            default:
                strategy = new StateChangingRequest();
                break;
            }
            if ( !strategy.apply ( ( HttpServletRequest ) request, ( HttpServletResponse ) response ) ) {
                return;
            }
        }
        chain.doFilter ( request, response );
    }
    private static interface RestCsrfPreventionStrategy {
        static final NonceSupplier<HttpServletRequest, String> nonceFromRequestHeader = ( r, k ) -> r
                .getHeader ( k );
        static final NonceSupplier<HttpServletRequest, String[]> nonceFromRequestParams = ( r, k ) -> r
                .getParameterValues ( k );
        static final NonceSupplier<HttpSession, String> nonceFromSession = ( s, k ) -> Objects
                .isNull ( s ) ? null : ( String ) s.getAttribute ( k );
        static final NonceConsumer<HttpServletResponse> nonceToResponse = ( r, k, v ) -> r.setHeader (
                    k, v );
        static final NonceConsumer<HttpSession> nonceToSession = ( s, k, v ) -> s.setAttribute ( k, v );
        boolean apply ( HttpServletRequest request, HttpServletResponse response ) throws IOException;
    }
    private class StateChangingRequest implements RestCsrfPreventionStrategy {
        @Override
        public boolean apply ( HttpServletRequest request, HttpServletResponse response )
        throws IOException {
            if ( isValidStateChangingRequest (
                        extractNonceFromRequest ( request ),
                        nonceFromSession.getNonce ( request.getSession ( false ), Constants.CSRF_REST_NONCE_SESSION_ATTR_NAME ) ) ) {
                return true;
            }
            nonceToResponse.setNonce ( response, Constants.CSRF_REST_NONCE_HEADER_NAME,
                                       Constants.CSRF_REST_NONCE_HEADER_REQUIRED_VALUE );
            response.sendError ( getDenyStatus(),
                                 sm.getString ( "restCsrfPreventionFilter.invalidNonce" ) );
            return false;
        }
        private boolean isValidStateChangingRequest ( String reqNonce, String sessionNonce ) {
            return Objects.nonNull ( reqNonce ) && Objects.nonNull ( sessionNonce )
                   && Objects.equals ( reqNonce, sessionNonce );
        }
        private String extractNonceFromRequest ( HttpServletRequest request ) {
            String nonceFromRequest = nonceFromRequestHeader.getNonce ( request,
                                      Constants.CSRF_REST_NONCE_HEADER_NAME );
            if ( ( Objects.isNull ( nonceFromRequest ) || Objects.equals ( "", nonceFromRequest ) )
                    && !getPathsAcceptingParams().isEmpty()
                    && getPathsAcceptingParams().contains ( getRequestedPath ( request ) ) ) {
                nonceFromRequest = extractNonceFromRequestParams ( request );
            }
            return nonceFromRequest;
        }
        private String extractNonceFromRequestParams ( HttpServletRequest request ) {
            String[] params = nonceFromRequestParams.getNonce ( request,
                              Constants.CSRF_REST_NONCE_HEADER_NAME );
            if ( Objects.nonNull ( params ) && params.length > 0 ) {
                String nonce = params[0];
                for ( String param : params ) {
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
        private final Predicate<String> fetchRequest = s -> Constants.CSRF_REST_NONCE_HEADER_FETCH_VALUE
                .equalsIgnoreCase ( s );
        @Override
        public boolean apply ( HttpServletRequest request, HttpServletResponse response ) {
            if ( fetchRequest.test (
                        nonceFromRequestHeader.getNonce ( request, Constants.CSRF_REST_NONCE_HEADER_NAME ) ) ) {
                String nonceFromSessionStr = nonceFromSession.getNonce ( request.getSession ( false ),
                                             Constants.CSRF_REST_NONCE_SESSION_ATTR_NAME );
                if ( nonceFromSessionStr == null ) {
                    nonceFromSessionStr = generateNonce();
                    nonceToSession.setNonce ( Objects.requireNonNull ( request.getSession ( true ) ),
                                              Constants.CSRF_REST_NONCE_SESSION_ATTR_NAME, nonceFromSessionStr );
                }
                nonceToResponse.setNonce ( response, Constants.CSRF_REST_NONCE_HEADER_NAME,
                                           nonceFromSessionStr );
            }
            return true;
        }
    }
    @FunctionalInterface
    private static interface NonceSupplier<T, R> {
        R getNonce ( T supplier, String key );
    }
    @FunctionalInterface
    private static interface NonceConsumer<T> {
        void setNonce ( T consumer, String key, String value );
    }
    public void setPathsAcceptingParams ( String pathsList ) {
        if ( Objects.nonNull ( pathsList ) ) {
            Arrays.asList ( pathsList.split ( pathsDelimiter ) ).forEach (
                e -> pathsAcceptingParams.add ( e.trim() ) );
        }
    }
    public Set<String> getPathsAcceptingParams() {
        return pathsAcceptingParams;
    }
}
