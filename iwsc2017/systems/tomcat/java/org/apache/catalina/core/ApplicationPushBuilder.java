package org.apache.catalina.core;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.SessionTrackingMode;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.PushBuilder;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.util.SessionConfig;
import org.apache.coyote.ActionCode;
import org.apache.coyote.PushToken;
import org.apache.tomcat.util.buf.B2CConverter;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap;
import org.apache.tomcat.util.http.CookieProcessor;
import org.apache.tomcat.util.res.StringManager;
public class ApplicationPushBuilder implements PushBuilder {
    private static final StringManager sm = StringManager.getManager ( ApplicationPushBuilder.class );
    private final HttpServletRequest baseRequest;
    private final Request catalinaRequest;
    private final org.apache.coyote.Request coyoteRequest;
    private final String sessionCookieName;
    private final String sessionPathParameterName;
    private final boolean addSessionCookie;
    private final boolean addSessionPathParameter;
    private final Map<String, List<String>> headers = new CaseInsensitiveKeyMap<>();
    private final List<Cookie> cookies = new ArrayList<>();
    private String method = "GET";
    private String path;
    private String etag;
    private String lastModified;
    private String queryString;
    private String sessionId;
    private boolean conditional;
    public ApplicationPushBuilder ( HttpServletRequest request ) {
        baseRequest = request;
        ServletRequest current = request;
        while ( current instanceof ServletRequestWrapper ) {
            current = ( ( ServletRequestWrapper ) current ).getRequest();
        }
        if ( current instanceof Request ) {
            catalinaRequest = ( ( Request ) current );
            coyoteRequest = catalinaRequest.getCoyoteRequest();
        } else {
            throw new UnsupportedOperationException ( sm.getString (
                        "applicationPushBuilder.noCoyoteRequest", current.getClass().getName() ) );
        }
        Enumeration<String> headerNames = request.getHeaderNames();
        while ( headerNames.hasMoreElements() ) {
            String headerName = headerNames.nextElement();
            List<String> values = new ArrayList<>();
            headers.put ( headerName, values );
            Enumeration<String> headerValues = request.getHeaders ( headerName );
            while ( headerValues.hasMoreElements() ) {
                values.add ( headerValues.nextElement() );
            }
        }
        headers.remove ( "if-match" );
        if ( headers.remove ( "if-none-match" ) != null ) {
            conditional = true;
        }
        if ( headers.remove ( "if-modified-since" ) != null ) {
            conditional = true;
        }
        headers.remove ( "if-unmodified-since" );
        headers.remove ( "if-range" );
        headers.remove ( "range" );
        headers.remove ( "expect" );
        headers.remove ( "authorization" );
        headers.remove ( "referer" );
        headers.remove ( "cookie" );
        StringBuffer referer = request.getRequestURL();
        if ( request.getQueryString() != null ) {
            referer.append ( '?' );
            referer.append ( request.getQueryString() );
        }
        addHeader ( "referer", referer.toString() );
        Context context = catalinaRequest.getContext();
        sessionCookieName = SessionConfig.getSessionCookieName ( context );
        sessionPathParameterName = SessionConfig.getSessionUriParamName ( context );
        HttpSession session = request.getSession ( false );
        if ( session != null ) {
            sessionId = session.getId();
        }
        if ( sessionId == null ) {
            sessionId = request.getRequestedSessionId();
        }
        if ( !request.isRequestedSessionIdFromCookie() && !request.isRequestedSessionIdFromURL() &&
                sessionId != null ) {
            Set<SessionTrackingMode> sessionTrackingModes =
                request.getServletContext().getEffectiveSessionTrackingModes();
            addSessionCookie = sessionTrackingModes.contains ( SessionTrackingMode.COOKIE );
            addSessionPathParameter = sessionTrackingModes.contains ( SessionTrackingMode.URL );
        } else {
            addSessionCookie = request.isRequestedSessionIdFromCookie();
            addSessionPathParameter = request.isRequestedSessionIdFromURL();
        }
        if ( request.getCookies() != null ) {
            for ( Cookie requestCookie : request.getCookies() ) {
                cookies.add ( requestCookie );
            }
        }
        for ( Cookie responseCookie : catalinaRequest.getResponse().getCookies() ) {
            if ( responseCookie.getMaxAge() < 0 ) {
                Iterator<Cookie> cookieIterator = cookies.iterator();
                while ( cookieIterator.hasNext() ) {
                    Cookie cookie = cookieIterator.next();
                    if ( cookie.getName().equals ( responseCookie.getName() ) ) {
                        cookieIterator.remove();
                    }
                }
            } else {
                cookies.add ( new Cookie ( responseCookie.getName(), responseCookie.getValue() ) );
            }
        }
    }
    @Override
    public PushBuilder path ( String path ) {
        if ( path.startsWith ( "/" ) ) {
            this.path = path;
        } else {
            String contextPath = baseRequest.getContextPath();
            int len = contextPath.length() + path.length() + 1;
            StringBuilder sb = new StringBuilder ( len );
            sb.append ( contextPath );
            sb.append ( '/' );
            sb.append ( path );
            this.path = sb.toString();
        }
        return this;
    }
    @Override
    public String getPath() {
        return path;
    }
    @Override
    public PushBuilder method ( String method ) {
        this.method = method;
        return this;
    }
    @Override
    public String getMethod() {
        return method;
    }
    @Override
    public PushBuilder etag ( String etag ) {
        this.etag = etag;
        return this;
    }
    @Override
    public String getEtag() {
        return etag;
    }
    @Override
    public PushBuilder lastModified ( String lastModified ) {
        this.lastModified = lastModified;
        return this;
    }
    @Override
    public String getLastModified() {
        return lastModified;
    }
    @Override
    public PushBuilder queryString ( String queryString ) {
        this.queryString = queryString;
        return this;
    }
    @Override
    public String getQueryString() {
        return queryString;
    }
    @Override
    public PushBuilder sessionId ( String sessionId ) {
        this.sessionId = sessionId;
        return this;
    }
    @Override
    public String getSessionId() {
        return sessionId;
    }
    @Override
    public PushBuilder conditional ( boolean conditional ) {
        this.conditional = conditional;
        return this;
    }
    @Override
    public boolean isConditional() {
        return conditional;
    }
    @Override
    public PushBuilder addHeader ( String name, String value ) {
        List<String> values = headers.get ( name );
        if ( values == null ) {
            values = new ArrayList<>();
            headers.put ( name, values );
        }
        values.add ( value );
        return this;
    }
    @Override
    public PushBuilder setHeader ( String name, String value ) {
        List<String> values = headers.get ( name );
        if ( values == null ) {
            values = new ArrayList<>();
            headers.put ( name, values );
        } else {
            values.clear();
        }
        values.add ( value );
        return this;
    }
    @Override
    public PushBuilder removeHeader ( String name ) {
        headers.remove ( name );
        return this;
    }
    @Override
    public Set<String> getHeaderNames() {
        return Collections.unmodifiableSet ( headers.keySet() );
    }
    @Override
    public String getHeader ( String name ) {
        List<String> values = headers.get ( name );
        if ( values == null ) {
            return null;
        } else {
            return values.get ( 0 );
        }
    }
    @Override
    public boolean push() {
        if ( path == null ) {
            throw new IllegalStateException ( sm.getString ( "pushBuilder.noPath" ) );
        }
        org.apache.coyote.Request pushTarget = new org.apache.coyote.Request();
        pushTarget.method().setString ( method );
        pushTarget.serverName().setString ( baseRequest.getServerName() );
        pushTarget.setServerPort ( baseRequest.getServerPort() );
        pushTarget.scheme().setString ( baseRequest.getScheme() );
        for ( Map.Entry<String, List<String>> header : headers.entrySet() ) {
            for ( String value : header.getValue() ) {
                pushTarget.getMimeHeaders().addValue ( header.getKey() ).setString ( value );
            }
        }
        int queryIndex = path.indexOf ( '?' );
        String pushPath;
        String pushQueryString = null;
        if ( queryIndex > -1 ) {
            pushPath = path.substring ( 0, queryIndex );
            if ( queryIndex + 1 < path.length() ) {
                pushQueryString = path.substring ( queryIndex + 1 );
            }
        } else {
            pushPath = path;
        }
        if ( sessionId != null ) {
            if ( addSessionPathParameter ) {
                pushPath = pushPath + ";" + sessionPathParameterName + "=" + sessionId;
                pushTarget.addPathParameter ( sessionPathParameterName, sessionId );
            }
            if ( addSessionCookie ) {
                cookies.add ( new Cookie ( sessionCookieName, sessionId ) );
            }
        }
        pushTarget.requestURI().setString ( pushPath );
        pushTarget.decodedURI().setString ( decode ( pushPath,
                                            catalinaRequest.getConnector().getURIEncodingLower() ) );
        if ( pushQueryString == null && queryString != null ) {
            pushTarget.queryString().setString ( queryString );
        } else if ( pushQueryString != null && queryString == null ) {
            pushTarget.queryString().setString ( pushQueryString );
        } else if ( pushQueryString != null && queryString != null ) {
            pushTarget.queryString().setString ( pushQueryString + "&" + queryString );
        }
        if ( conditional ) {
            if ( etag != null ) {
                setHeader ( "if-none-match", etag );
            } else if ( lastModified != null ) {
                setHeader ( "if-modified-since", lastModified );
            }
        }
        setHeader ( "cookie", generateCookieHeader ( cookies,
                    catalinaRequest.getContext().getCookieProcessor() ) );
        PushToken pushToken = new PushToken ( pushTarget );
        coyoteRequest.action ( ActionCode.PUSH_REQUEST, pushToken );
        pushTarget = null;
        path = null;
        etag = null;
        lastModified = null;
        headers.remove ( "if-none-match" );
        headers.remove ( "if-modified-since" );
        return pushToken.getResult();
    }
    static String decode ( String input, String charsetName ) {
        int start = input.indexOf ( '%' );
        int end = 0;
        if ( start == -1 ) {
            return input;
        }
        Charset charset;
        try {
            charset = B2CConverter.getCharsetLower ( charsetName );
        } catch ( UnsupportedEncodingException uee ) {
            throw new IllegalStateException ( uee );
        }
        StringBuilder result = new StringBuilder ( input.length() );
        while ( start != -1 ) {
            result.append ( input.substring ( end, start ) );
            end = start + 3;
            while ( end < input.length() && input.charAt ( end ) == '%' ) {
                end += 3;
            }
            result.append ( decode ( input.substring ( start, end ), charset ) );
            start = input.indexOf ( '%', end );
        }
        result.append ( input.substring ( end ) );
        return result.toString();
    }
    private static String decode ( String percentSequence, Charset charset ) {
        byte[] bytes = new byte[percentSequence.length() / 3];
        for ( int i = 0; i < bytes.length; i += 3 ) {
            bytes[i] = ( byte ) ( HexUtils.getDec ( percentSequence.charAt ( 1 + 3 * i ) ) << 4 +
                                  HexUtils.getDec ( percentSequence.charAt ( 2 + 3 * i ) ) );
        }
        return new String ( bytes, charset );
    }
    private static String generateCookieHeader ( List<Cookie> cookies, CookieProcessor cookieProcessor ) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for ( Cookie cookie : cookies ) {
            if ( first ) {
                first = false;
            } else {
                result.append ( ';' );
            }
            result.append ( cookieProcessor.generateHeader ( cookie ) );
        }
        return result.toString();
    }
}
