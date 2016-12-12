package org.apache.catalina.filters;
import java.util.Objects;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
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
