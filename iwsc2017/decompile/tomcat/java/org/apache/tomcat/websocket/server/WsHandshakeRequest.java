package org.apache.tomcat.websocket.server;
import java.util.Iterator;
import java.util.Enumeration;
import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashMap;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.net.URI;
import javax.websocket.server.HandshakeRequest;
public class WsHandshakeRequest implements HandshakeRequest {
    private final URI requestUri;
    private final Map<String, List<String>> parameterMap;
    private final String queryString;
    private final Principal userPrincipal;
    private final Map<String, List<String>> headers;
    private final Object httpSession;
    private volatile HttpServletRequest request;
    public WsHandshakeRequest ( final HttpServletRequest request, final Map<String, String> pathParams ) {
        this.request = request;
        this.queryString = request.getQueryString();
        this.userPrincipal = request.getUserPrincipal();
        this.httpSession = request.getSession ( false );
        final StringBuilder sb = new StringBuilder ( request.getRequestURI() );
        if ( this.queryString != null ) {
            sb.append ( "?" );
            sb.append ( this.queryString );
        }
        try {
            this.requestUri = new URI ( sb.toString() );
        } catch ( URISyntaxException e ) {
            throw new IllegalArgumentException ( e );
        }
        final Map<String, String[]> originalParameters = ( Map<String, String[]> ) request.getParameterMap();
        final Map<String, List<String>> newParameters = new HashMap<String, List<String>> ( originalParameters.size() );
        for ( final Map.Entry<String, String[]> entry : originalParameters.entrySet() ) {
            newParameters.put ( entry.getKey(), Collections.unmodifiableList ( ( List<? extends String> ) Arrays.asList ( ( T[] ) entry.getValue() ) ) );
        }
        for ( final Map.Entry<String, String> entry2 : pathParams.entrySet() ) {
            newParameters.put ( entry2.getKey(), Collections.unmodifiableList ( ( List<? extends String> ) Arrays.asList ( entry2.getValue() ) ) );
        }
        this.parameterMap = Collections.unmodifiableMap ( ( Map<? extends String, ? extends List<String>> ) newParameters );
        final Map<String, List<String>> newHeaders = ( Map<String, List<String>> ) new CaseInsensitiveKeyMap();
        final Enumeration<String> headerNames = ( Enumeration<String> ) request.getHeaderNames();
        while ( headerNames.hasMoreElements() ) {
            final String headerName = headerNames.nextElement();
            newHeaders.put ( headerName, Collections.unmodifiableList ( ( List<? extends String> ) Collections.list ( ( Enumeration<Object> ) request.getHeaders ( headerName ) ) ) );
        }
        this.headers = Collections.unmodifiableMap ( ( Map<? extends String, ? extends List<String>> ) newHeaders );
    }
    public URI getRequestURI() {
        return this.requestUri;
    }
    public Map<String, List<String>> getParameterMap() {
        return this.parameterMap;
    }
    public String getQueryString() {
        return this.queryString;
    }
    public Principal getUserPrincipal() {
        return this.userPrincipal;
    }
    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }
    public boolean isUserInRole ( final String role ) {
        if ( this.request == null ) {
            throw new IllegalStateException();
        }
        return this.request.isUserInRole ( role );
    }
    public Object getHttpSession() {
        return this.httpSession;
    }
    void finished() {
        this.request = null;
    }
}
