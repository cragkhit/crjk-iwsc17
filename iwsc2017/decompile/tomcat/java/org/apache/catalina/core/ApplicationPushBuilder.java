package org.apache.catalina.core;
import org.apache.tomcat.util.http.CookieProcessor;
import org.apache.tomcat.util.buf.HexUtils;
import java.nio.charset.Charset;
import java.io.UnsupportedEncodingException;
import org.apache.tomcat.util.buf.B2CConverter;
import org.apache.coyote.ActionCode;
import org.apache.coyote.PushToken;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.http.HttpSession;
import org.apache.catalina.Context;
import java.util.Enumeration;
import javax.servlet.ServletRequest;
import javax.servlet.SessionTrackingMode;
import org.apache.catalina.util.SessionConfig;
import javax.servlet.ServletRequestWrapper;
import java.util.ArrayList;
import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap;
import javax.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import org.apache.catalina.connector.Request;
import javax.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.res.StringManager;
import javax.servlet.http.PushBuilder;
public class ApplicationPushBuilder implements PushBuilder {
    private static final StringManager sm;
    private final HttpServletRequest baseRequest;
    private final Request catalinaRequest;
    private final org.apache.coyote.Request coyoteRequest;
    private final String sessionCookieName;
    private final String sessionPathParameterName;
    private final boolean addSessionCookie;
    private final boolean addSessionPathParameter;
    private final Map<String, List<String>> headers;
    private final List<Cookie> cookies;
    private String method;
    private String path;
    private String etag;
    private String lastModified;
    private String queryString;
    private String sessionId;
    private boolean conditional;
    public ApplicationPushBuilder ( final HttpServletRequest request ) {
        this.headers = ( Map<String, List<String>> ) new CaseInsensitiveKeyMap();
        this.cookies = new ArrayList<Cookie>();
        this.method = "GET";
        this.baseRequest = request;
        ServletRequest current;
        for ( current = ( ServletRequest ) request; current instanceof ServletRequestWrapper; current = ( ( ServletRequestWrapper ) current ).getRequest() ) {}
        if ( current instanceof Request ) {
            this.catalinaRequest = ( Request ) current;
            this.coyoteRequest = this.catalinaRequest.getCoyoteRequest();
            final Enumeration<String> headerNames = ( Enumeration<String> ) request.getHeaderNames();
            while ( headerNames.hasMoreElements() ) {
                final String headerName = headerNames.nextElement();
                final List<String> values = new ArrayList<String>();
                this.headers.put ( headerName, values );
                final Enumeration<String> headerValues = ( Enumeration<String> ) request.getHeaders ( headerName );
                while ( headerValues.hasMoreElements() ) {
                    values.add ( headerValues.nextElement() );
                }
            }
            this.headers.remove ( "if-match" );
            if ( this.headers.remove ( "if-none-match" ) != null ) {
                this.conditional = true;
            }
            if ( this.headers.remove ( "if-modified-since" ) != null ) {
                this.conditional = true;
            }
            this.headers.remove ( "if-unmodified-since" );
            this.headers.remove ( "if-range" );
            this.headers.remove ( "range" );
            this.headers.remove ( "expect" );
            this.headers.remove ( "authorization" );
            this.headers.remove ( "referer" );
            this.headers.remove ( "cookie" );
            final StringBuffer referer = request.getRequestURL();
            if ( request.getQueryString() != null ) {
                referer.append ( '?' );
                referer.append ( request.getQueryString() );
            }
            this.addHeader ( "referer", referer.toString() );
            final Context context = this.catalinaRequest.getContext();
            this.sessionCookieName = SessionConfig.getSessionCookieName ( context );
            this.sessionPathParameterName = SessionConfig.getSessionUriParamName ( context );
            final HttpSession session = request.getSession ( false );
            if ( session != null ) {
                this.sessionId = session.getId();
            }
            if ( this.sessionId == null ) {
                this.sessionId = request.getRequestedSessionId();
            }
            if ( !request.isRequestedSessionIdFromCookie() && !request.isRequestedSessionIdFromURL() && this.sessionId != null ) {
                final Set<SessionTrackingMode> sessionTrackingModes = ( Set<SessionTrackingMode> ) request.getServletContext().getEffectiveSessionTrackingModes();
                this.addSessionCookie = sessionTrackingModes.contains ( SessionTrackingMode.COOKIE );
                this.addSessionPathParameter = sessionTrackingModes.contains ( SessionTrackingMode.URL );
            } else {
                this.addSessionCookie = request.isRequestedSessionIdFromCookie();
                this.addSessionPathParameter = request.isRequestedSessionIdFromURL();
            }
            if ( request.getCookies() != null ) {
                for ( final Cookie requestCookie : request.getCookies() ) {
                    this.cookies.add ( requestCookie );
                }
            }
            for ( final Cookie responseCookie : this.catalinaRequest.getResponse().getCookies() ) {
                if ( responseCookie.getMaxAge() < 0 ) {
                    final Iterator<Cookie> cookieIterator = this.cookies.iterator();
                    while ( cookieIterator.hasNext() ) {
                        final Cookie cookie = cookieIterator.next();
                        if ( cookie.getName().equals ( responseCookie.getName() ) ) {
                            cookieIterator.remove();
                        }
                    }
                } else {
                    this.cookies.add ( new Cookie ( responseCookie.getName(), responseCookie.getValue() ) );
                }
            }
            return;
        }
        throw new UnsupportedOperationException ( ApplicationPushBuilder.sm.getString ( "applicationPushBuilder.noCoyoteRequest", current.getClass().getName() ) );
    }
    public PushBuilder path ( final String path ) {
        if ( path.startsWith ( "/" ) ) {
            this.path = path;
        } else {
            final String contextPath = this.baseRequest.getContextPath();
            final int len = contextPath.length() + path.length() + 1;
            final StringBuilder sb = new StringBuilder ( len );
            sb.append ( contextPath );
            sb.append ( '/' );
            sb.append ( path );
            this.path = sb.toString();
        }
        return ( PushBuilder ) this;
    }
    public String getPath() {
        return this.path;
    }
    public PushBuilder method ( final String method ) {
        this.method = method;
        return ( PushBuilder ) this;
    }
    public String getMethod() {
        return this.method;
    }
    public PushBuilder etag ( final String etag ) {
        this.etag = etag;
        return ( PushBuilder ) this;
    }
    public String getEtag() {
        return this.etag;
    }
    public PushBuilder lastModified ( final String lastModified ) {
        this.lastModified = lastModified;
        return ( PushBuilder ) this;
    }
    public String getLastModified() {
        return this.lastModified;
    }
    public PushBuilder queryString ( final String queryString ) {
        this.queryString = queryString;
        return ( PushBuilder ) this;
    }
    public String getQueryString() {
        return this.queryString;
    }
    public PushBuilder sessionId ( final String sessionId ) {
        this.sessionId = sessionId;
        return ( PushBuilder ) this;
    }
    public String getSessionId() {
        return this.sessionId;
    }
    public PushBuilder conditional ( final boolean conditional ) {
        this.conditional = conditional;
        return ( PushBuilder ) this;
    }
    public boolean isConditional() {
        return this.conditional;
    }
    public PushBuilder addHeader ( final String name, final String value ) {
        List<String> values = this.headers.get ( name );
        if ( values == null ) {
            values = new ArrayList<String>();
            this.headers.put ( name, values );
        }
        values.add ( value );
        return ( PushBuilder ) this;
    }
    public PushBuilder setHeader ( final String name, final String value ) {
        List<String> values = this.headers.get ( name );
        if ( values == null ) {
            values = new ArrayList<String>();
            this.headers.put ( name, values );
        } else {
            values.clear();
        }
        values.add ( value );
        return ( PushBuilder ) this;
    }
    public PushBuilder removeHeader ( final String name ) {
        this.headers.remove ( name );
        return ( PushBuilder ) this;
    }
    public Set<String> getHeaderNames() {
        return Collections.unmodifiableSet ( ( Set<? extends String> ) this.headers.keySet() );
    }
    public String getHeader ( final String name ) {
        final List<String> values = this.headers.get ( name );
        if ( values == null ) {
            return null;
        }
        return values.get ( 0 );
    }
    public boolean push() {
        if ( this.path == null ) {
            throw new IllegalStateException ( ApplicationPushBuilder.sm.getString ( "pushBuilder.noPath" ) );
        }
        org.apache.coyote.Request pushTarget = new org.apache.coyote.Request();
        pushTarget.method().setString ( this.method );
        pushTarget.serverName().setString ( this.baseRequest.getServerName() );
        pushTarget.setServerPort ( this.baseRequest.getServerPort() );
        pushTarget.scheme().setString ( this.baseRequest.getScheme() );
        for ( final Map.Entry<String, List<String>> header : this.headers.entrySet() ) {
            for ( final String value : header.getValue() ) {
                pushTarget.getMimeHeaders().addValue ( header.getKey() ).setString ( value );
            }
        }
        final int queryIndex = this.path.indexOf ( 63 );
        String pushQueryString = null;
        String pushPath;
        if ( queryIndex > -1 ) {
            pushPath = this.path.substring ( 0, queryIndex );
            if ( queryIndex + 1 < this.path.length() ) {
                pushQueryString = this.path.substring ( queryIndex + 1 );
            }
        } else {
            pushPath = this.path;
        }
        if ( this.sessionId != null ) {
            if ( this.addSessionPathParameter ) {
                pushPath = pushPath + ";" + this.sessionPathParameterName + "=" + this.sessionId;
                pushTarget.addPathParameter ( this.sessionPathParameterName, this.sessionId );
            }
            if ( this.addSessionCookie ) {
                this.cookies.add ( new Cookie ( this.sessionCookieName, this.sessionId ) );
            }
        }
        pushTarget.requestURI().setString ( pushPath );
        pushTarget.decodedURI().setString ( decode ( pushPath, this.catalinaRequest.getConnector().getURIEncodingLower() ) );
        if ( pushQueryString == null && this.queryString != null ) {
            pushTarget.queryString().setString ( this.queryString );
        } else if ( pushQueryString != null && this.queryString == null ) {
            pushTarget.queryString().setString ( pushQueryString );
        } else if ( pushQueryString != null && this.queryString != null ) {
            pushTarget.queryString().setString ( pushQueryString + "&" + this.queryString );
        }
        if ( this.conditional ) {
            if ( this.etag != null ) {
                this.setHeader ( "if-none-match", this.etag );
            } else if ( this.lastModified != null ) {
                this.setHeader ( "if-modified-since", this.lastModified );
            }
        }
        this.setHeader ( "cookie", generateCookieHeader ( this.cookies, this.catalinaRequest.getContext().getCookieProcessor() ) );
        final PushToken pushToken = new PushToken ( pushTarget );
        this.coyoteRequest.action ( ActionCode.PUSH_REQUEST, pushToken );
        pushTarget = null;
        this.path = null;
        this.etag = null;
        this.lastModified = null;
        this.headers.remove ( "if-none-match" );
        this.headers.remove ( "if-modified-since" );
        return pushToken.getResult();
    }
    static String decode ( final String input, final String charsetName ) {
        int start = input.indexOf ( 37 );
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
        final StringBuilder result = new StringBuilder ( input.length() );
        while ( start != -1 ) {
            result.append ( input.substring ( end, start ) );
            for ( end = start + 3; end < input.length() && input.charAt ( end ) == '%'; end += 3 ) {}
            result.append ( decode ( input.substring ( start, end ), charset ) );
            start = input.indexOf ( 37, end );
        }
        result.append ( input.substring ( end ) );
        return result.toString();
    }
    private static String decode ( final String percentSequence, final Charset charset ) {
        final byte[] bytes = new byte[percentSequence.length() / 3];
        for ( int i = 0; i < bytes.length; i += 3 ) {
            bytes[i] = ( byte ) ( HexUtils.getDec ( percentSequence.charAt ( 1 + 3 * i ) ) << 4 + HexUtils.getDec ( percentSequence.charAt ( 2 + 3 * i ) ) );
        }
        return new String ( bytes, charset );
    }
    private static String generateCookieHeader ( final List<Cookie> cookies, final CookieProcessor cookieProcessor ) {
        final StringBuilder result = new StringBuilder();
        boolean first = true;
        for ( final Cookie cookie : cookies ) {
            if ( first ) {
                first = false;
            } else {
                result.append ( ';' );
            }
            result.append ( cookieProcessor.generateHeader ( cookie ) );
        }
        return result.toString();
    }
    static {
        sm = StringManager.getManager ( ApplicationPushBuilder.class );
    }
}
