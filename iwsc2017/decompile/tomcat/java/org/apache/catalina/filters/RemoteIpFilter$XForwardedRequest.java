package org.apache.catalina.filters;
import java.util.Locale;
import org.apache.catalina.core.ApplicationPushBuilder;
import javax.servlet.http.PushBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Date;
import java.text.DateFormat;
import java.util.Enumeration;
import java.util.Collections;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import javax.servlet.http.HttpServletRequestWrapper;
public static class XForwardedRequest extends HttpServletRequestWrapper {
    static final ThreadLocal<SimpleDateFormat[]> threadLocalDateFormats;
    protected final Map<String, List<String>> headers;
    protected int localPort;
    protected String remoteAddr;
    protected String remoteHost;
    protected String scheme;
    protected boolean secure;
    protected int serverPort;
    public XForwardedRequest ( final HttpServletRequest request ) {
        super ( request );
        this.localPort = request.getLocalPort();
        this.remoteAddr = request.getRemoteAddr();
        this.remoteHost = request.getRemoteHost();
        this.scheme = request.getScheme();
        this.secure = request.isSecure();
        this.serverPort = request.getServerPort();
        this.headers = new HashMap<String, List<String>>();
        final Enumeration<String> headerNames = ( Enumeration<String> ) request.getHeaderNames();
        while ( headerNames.hasMoreElements() ) {
            final String header = headerNames.nextElement();
            this.headers.put ( header, ( List<String> ) Collections.list ( ( Enumeration<Object> ) request.getHeaders ( header ) ) );
        }
    }
    public long getDateHeader ( final String name ) {
        final String value = this.getHeader ( name );
        if ( value == null ) {
            return -1L;
        }
        final DateFormat[] dateFormats = XForwardedRequest.threadLocalDateFormats.get();
        Date date = null;
        for ( int i = 0; i < dateFormats.length && date == null; ++i ) {
            final DateFormat dateFormat = dateFormats[i];
            try {
                date = dateFormat.parse ( value );
            } catch ( Exception ex ) {}
        }
        if ( date == null ) {
            throw new IllegalArgumentException ( value );
        }
        return date.getTime();
    }
    public String getHeader ( final String name ) {
        final Map.Entry<String, List<String>> header = this.getHeaderEntry ( name );
        if ( header == null || header.getValue() == null || header.getValue().isEmpty() ) {
            return null;
        }
        return header.getValue().get ( 0 );
    }
    protected Map.Entry<String, List<String>> getHeaderEntry ( final String name ) {
        for ( final Map.Entry<String, List<String>> entry : this.headers.entrySet() ) {
            if ( entry.getKey().equalsIgnoreCase ( name ) ) {
                return entry;
            }
        }
        return null;
    }
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration ( this.headers.keySet() );
    }
    public Enumeration<String> getHeaders ( final String name ) {
        final Map.Entry<String, List<String>> header = this.getHeaderEntry ( name );
        if ( header == null || header.getValue() == null ) {
            return Collections.enumeration ( ( Collection<String> ) Collections.emptyList() );
        }
        return Collections.enumeration ( header.getValue() );
    }
    public int getIntHeader ( final String name ) {
        final String value = this.getHeader ( name );
        if ( value == null ) {
            return -1;
        }
        return Integer.parseInt ( value );
    }
    public int getLocalPort() {
        return this.localPort;
    }
    public String getRemoteAddr() {
        return this.remoteAddr;
    }
    public String getRemoteHost() {
        return this.remoteHost;
    }
    public String getScheme() {
        return this.scheme;
    }
    public int getServerPort() {
        return this.serverPort;
    }
    public boolean isSecure() {
        return this.secure;
    }
    public void removeHeader ( final String name ) {
        final Map.Entry<String, List<String>> header = this.getHeaderEntry ( name );
        if ( header != null ) {
            this.headers.remove ( header.getKey() );
        }
    }
    public void setHeader ( final String name, final String value ) {
        final List<String> values = Arrays.asList ( value );
        final Map.Entry<String, List<String>> header = this.getHeaderEntry ( name );
        if ( header == null ) {
            this.headers.put ( name, values );
        } else {
            header.setValue ( values );
        }
    }
    public void setLocalPort ( final int localPort ) {
        this.localPort = localPort;
    }
    public void setRemoteAddr ( final String remoteAddr ) {
        this.remoteAddr = remoteAddr;
    }
    public void setRemoteHost ( final String remoteHost ) {
        this.remoteHost = remoteHost;
    }
    public void setScheme ( final String scheme ) {
        this.scheme = scheme;
    }
    public void setSecure ( final boolean secure ) {
        this.secure = secure;
    }
    public void setServerPort ( final int serverPort ) {
        this.serverPort = serverPort;
    }
    public StringBuffer getRequestURL() {
        final StringBuffer url = new StringBuffer();
        final String scheme = this.getScheme();
        int port = this.getServerPort();
        if ( port < 0 ) {
            port = 80;
        }
        url.append ( scheme );
        url.append ( "://" );
        url.append ( this.getServerName() );
        if ( ( scheme.equals ( "http" ) && port != 80 ) || ( scheme.equals ( "https" ) && port != 443 ) ) {
            url.append ( ':' );
            url.append ( port );
        }
        url.append ( this.getRequestURI() );
        return url;
    }
    public PushBuilder getPushBuilder() {
        return ( PushBuilder ) new ApplicationPushBuilder ( ( HttpServletRequest ) this );
    }
    static {
        threadLocalDateFormats = new ThreadLocal<SimpleDateFormat[]>() {
            @Override
            protected SimpleDateFormat[] initialValue() {
                return new SimpleDateFormat[] { new SimpleDateFormat ( "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US ), new SimpleDateFormat ( "EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US ), new SimpleDateFormat ( "EEE MMMM d HH:mm:ss yyyy", Locale.US ) };
            }
        };
    }
}
