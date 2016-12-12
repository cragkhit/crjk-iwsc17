package org.apache.catalina.filters;
import java.util.Objects;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
private interface RestCsrfPreventionStrategy {
    public static final NonceSupplier<HttpServletRequest, String> nonceFromRequestHeader = ( r, k ) -> r.getHeader ( k );
    public static final NonceSupplier<HttpServletRequest, String[]> nonceFromRequestParams = ( r, k ) -> r.getParameterValues ( k );
    public static final NonceSupplier<HttpSession, String> nonceFromSession = ( s, k ) -> Objects.isNull ( s ) ? null : ( ( String ) s.getAttribute ( k ) );
    public static final NonceConsumer<HttpServletResponse> nonceToResponse = ( r, k, v ) -> r.setHeader ( k, v );
    public static final NonceConsumer<HttpSession> nonceToSession = ( s, k, v ) -> s.setAttribute ( k, ( Object ) v );
    boolean apply ( HttpServletRequest p0, HttpServletResponse p1 ) throws IOException;
}
