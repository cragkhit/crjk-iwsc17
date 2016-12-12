package org.apache.catalina.filters;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
protected static class CsrfResponseWrapper extends HttpServletResponseWrapper {
    private final String nonce;
    public CsrfResponseWrapper ( final HttpServletResponse response, final String nonce ) {
        super ( response );
        this.nonce = nonce;
    }
    @Deprecated
    public String encodeRedirectUrl ( final String url ) {
        return this.encodeRedirectURL ( url );
    }
    public String encodeRedirectURL ( final String url ) {
        return this.addNonce ( super.encodeRedirectURL ( url ) );
    }
    @Deprecated
    public String encodeUrl ( final String url ) {
        return this.encodeURL ( url );
    }
    public String encodeURL ( final String url ) {
        return this.addNonce ( super.encodeURL ( url ) );
    }
    private String addNonce ( final String url ) {
        if ( url == null || this.nonce == null ) {
            return url;
        }
        String path = url;
        String query = "";
        String anchor = "";
        final int pound = path.indexOf ( 35 );
        if ( pound >= 0 ) {
            anchor = path.substring ( pound );
            path = path.substring ( 0, pound );
        }
        final int question = path.indexOf ( 63 );
        if ( question >= 0 ) {
            query = path.substring ( question );
            path = path.substring ( 0, question );
        }
        final StringBuilder sb = new StringBuilder ( path );
        if ( query.length() > 0 ) {
            sb.append ( query );
            sb.append ( '&' );
        } else {
            sb.append ( '?' );
        }
        sb.append ( "org.apache.catalina.filters.CSRF_NONCE" );
        sb.append ( '=' );
        sb.append ( this.nonce );
        sb.append ( anchor );
        return sb.toString();
    }
}
