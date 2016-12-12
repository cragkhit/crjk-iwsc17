package org.apache.catalina.filters;
import java.util.Objects;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.function.Predicate;
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
