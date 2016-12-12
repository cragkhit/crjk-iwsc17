package org.apache.tomcat.websocket.server;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.HandshakeRequest;
import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap;
public class WsHandshakeRequest implements HandshakeRequest {
    private final URI requestUri;
    private final Map<String, List<String>> parameterMap;
    private final String queryString;
    private final Principal userPrincipal;
    private final Map<String, List<String>> headers;
    private final Object httpSession;
    private volatile HttpServletRequest request;
    public WsHandshakeRequest ( HttpServletRequest request, Map<String, String> pathParams ) {
        this.request = request;
        queryString = request.getQueryString();
        userPrincipal = request.getUserPrincipal();
        httpSession = request.getSession ( false );
        StringBuilder sb = new StringBuilder ( request.getRequestURI() );
        if ( queryString != null ) {
            sb.append ( "?" );
            sb.append ( queryString );
        }
        try {
            requestUri = new URI ( sb.toString() );
        } catch ( URISyntaxException e ) {
            throw new IllegalArgumentException ( e );
        }
        Map<String, String[]> originalParameters = request.getParameterMap();
        Map<String, List<String>> newParameters =
            new HashMap<> ( originalParameters.size() );
        for ( Entry<String, String[]> entry : originalParameters.entrySet() ) {
            newParameters.put ( entry.getKey(),
                                Collections.unmodifiableList (
                                    Arrays.asList ( entry.getValue() ) ) );
        }
        for ( Entry<String, String> entry : pathParams.entrySet() ) {
            newParameters.put ( entry.getKey(),
                                Collections.unmodifiableList (
                                    Arrays.asList ( entry.getValue() ) ) );
        }
        parameterMap = Collections.unmodifiableMap ( newParameters );
        Map<String, List<String>> newHeaders = new CaseInsensitiveKeyMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while ( headerNames.hasMoreElements() ) {
            String headerName = headerNames.nextElement();
            newHeaders.put ( headerName, Collections.unmodifiableList (
                                 Collections.list ( request.getHeaders ( headerName ) ) ) );
        }
        headers = Collections.unmodifiableMap ( newHeaders );
    }
    @Override
    public URI getRequestURI() {
        return requestUri;
    }
    @Override
    public Map<String, List<String>> getParameterMap() {
        return parameterMap;
    }
    @Override
    public String getQueryString() {
        return queryString;
    }
    @Override
    public Principal getUserPrincipal() {
        return userPrincipal;
    }
    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }
    @Override
    public boolean isUserInRole ( String role ) {
        if ( request == null ) {
            throw new IllegalStateException();
        }
        return request.isUserInRole ( role );
    }
    @Override
    public Object getHttpSession() {
        return httpSession;
    }
    void finished() {
        request = null;
    }
}
