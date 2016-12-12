package org.apache.catalina.filters;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Locale;
import org.apache.catalina.core.ApplicationPushBuilder;
import javax.servlet.http.PushBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.text.DateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.juli.logging.LogFactory;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import java.util.LinkedList;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.List;
import org.apache.juli.logging.Log;
import java.util.regex.Pattern;
import javax.servlet.GenericFilter;
public class RemoteIpFilter extends GenericFilter {
    private static final long serialVersionUID = 1L;
    private static final Pattern commaSeparatedValuesPattern;
    protected static final String HTTP_SERVER_PORT_PARAMETER = "httpServerPort";
    protected static final String HTTPS_SERVER_PORT_PARAMETER = "httpsServerPort";
    protected static final String INTERNAL_PROXIES_PARAMETER = "internalProxies";
    private static final Log log;
    protected static final String PROTOCOL_HEADER_PARAMETER = "protocolHeader";
    protected static final String PROTOCOL_HEADER_HTTPS_VALUE_PARAMETER = "protocolHeaderHttpsValue";
    protected static final String PORT_HEADER_PARAMETER = "portHeader";
    protected static final String CHANGE_LOCAL_PORT_PARAMETER = "changeLocalPort";
    protected static final String PROXIES_HEADER_PARAMETER = "proxiesHeader";
    protected static final String REMOTE_IP_HEADER_PARAMETER = "remoteIpHeader";
    protected static final String TRUSTED_PROXIES_PARAMETER = "trustedProxies";
    private int httpServerPort;
    private int httpsServerPort;
    private Pattern internalProxies;
    private String protocolHeader;
    private String protocolHeaderHttpsValue;
    private String portHeader;
    private boolean changeLocalPort;
    private String proxiesHeader;
    private String remoteIpHeader;
    private boolean requestAttributesEnabled;
    private Pattern trustedProxies;
    public RemoteIpFilter() {
        this.httpServerPort = 80;
        this.httpsServerPort = 443;
        this.internalProxies = Pattern.compile ( "10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|192\\.168\\.\\d{1,3}\\.\\d{1,3}|169\\.254\\.\\d{1,3}\\.\\d{1,3}|127\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|172\\.1[6-9]{1}\\.\\d{1,3}\\.\\d{1,3}|172\\.2[0-9]{1}\\.\\d{1,3}\\.\\d{1,3}|172\\.3[0-1]{1}\\.\\d{1,3}\\.\\d{1,3}" );
        this.protocolHeader = null;
        this.protocolHeaderHttpsValue = "https";
        this.portHeader = null;
        this.changeLocalPort = false;
        this.proxiesHeader = "X-Forwarded-By";
        this.remoteIpHeader = "X-Forwarded-For";
        this.requestAttributesEnabled = true;
        this.trustedProxies = null;
    }
    protected static String[] commaDelimitedListToStringArray ( final String commaDelimitedStrings ) {
        return ( commaDelimitedStrings == null || commaDelimitedStrings.length() == 0 ) ? new String[0] : RemoteIpFilter.commaSeparatedValuesPattern.split ( commaDelimitedStrings );
    }
    protected static String listToCommaDelimitedString ( final List<String> stringList ) {
        if ( stringList == null ) {
            return "";
        }
        final StringBuilder result = new StringBuilder();
        final Iterator<String> it = stringList.iterator();
        while ( it.hasNext() ) {
            final Object element = it.next();
            if ( element != null ) {
                result.append ( element );
                if ( !it.hasNext() ) {
                    continue;
                }
                result.append ( ", " );
            }
        }
        return result.toString();
    }
    public void doFilter ( final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain ) throws IOException, ServletException {
        if ( this.internalProxies != null && this.internalProxies.matcher ( request.getRemoteAddr() ).matches() ) {
            String remoteIp = null;
            final LinkedList<String> proxiesHeaderValue = new LinkedList<String>();
            final StringBuilder concatRemoteIpHeaderValue = new StringBuilder();
            final Enumeration<String> e = ( Enumeration<String> ) request.getHeaders ( this.remoteIpHeader );
            while ( e.hasMoreElements() ) {
                if ( concatRemoteIpHeaderValue.length() > 0 ) {
                    concatRemoteIpHeaderValue.append ( ", " );
                }
                concatRemoteIpHeaderValue.append ( e.nextElement() );
            }
            final String[] remoteIpHeaderValue = commaDelimitedListToStringArray ( concatRemoteIpHeaderValue.toString() );
            int idx;
            for ( idx = remoteIpHeaderValue.length - 1; idx >= 0; --idx ) {
                final String currentRemoteIp = remoteIp = remoteIpHeaderValue[idx];
                if ( !this.internalProxies.matcher ( currentRemoteIp ).matches() ) {
                    if ( this.trustedProxies == null || !this.trustedProxies.matcher ( currentRemoteIp ).matches() ) {
                        --idx;
                        break;
                    }
                    proxiesHeaderValue.addFirst ( currentRemoteIp );
                }
            }
            final LinkedList<String> newRemoteIpHeaderValue = new LinkedList<String>();
            while ( idx >= 0 ) {
                final String currentRemoteIp2 = remoteIpHeaderValue[idx];
                newRemoteIpHeaderValue.addFirst ( currentRemoteIp2 );
                --idx;
            }
            final XForwardedRequest xRequest = new XForwardedRequest ( request );
            if ( remoteIp != null ) {
                xRequest.setRemoteAddr ( remoteIp );
                xRequest.setRemoteHost ( remoteIp );
                if ( proxiesHeaderValue.size() == 0 ) {
                    xRequest.removeHeader ( this.proxiesHeader );
                } else {
                    final String commaDelimitedListOfProxies = listToCommaDelimitedString ( proxiesHeaderValue );
                    xRequest.setHeader ( this.proxiesHeader, commaDelimitedListOfProxies );
                }
                if ( newRemoteIpHeaderValue.size() == 0 ) {
                    xRequest.removeHeader ( this.remoteIpHeader );
                } else {
                    final String commaDelimitedRemoteIpHeaderValue = listToCommaDelimitedString ( newRemoteIpHeaderValue );
                    xRequest.setHeader ( this.remoteIpHeader, commaDelimitedRemoteIpHeaderValue );
                }
            }
            if ( this.protocolHeader != null ) {
                final String protocolHeaderValue = request.getHeader ( this.protocolHeader );
                if ( protocolHeaderValue != null ) {
                    if ( this.protocolHeaderHttpsValue.equalsIgnoreCase ( protocolHeaderValue ) ) {
                        xRequest.setSecure ( true );
                        xRequest.setScheme ( "https" );
                        this.setPorts ( xRequest, this.httpsServerPort );
                    } else {
                        xRequest.setSecure ( false );
                        xRequest.setScheme ( "http" );
                        this.setPorts ( xRequest, this.httpServerPort );
                    }
                }
            }
            if ( RemoteIpFilter.log.isDebugEnabled() ) {
                RemoteIpFilter.log.debug ( "Incoming request " + request.getRequestURI() + " with originalRemoteAddr '" + request.getRemoteAddr() + "', originalRemoteHost='" + request.getRemoteHost() + "', originalSecure='" + request.isSecure() + "', originalScheme='" + request.getScheme() + "', original[" + this.remoteIpHeader + "]='" + ( Object ) concatRemoteIpHeaderValue + "', original[" + this.protocolHeader + "]='" + ( ( this.protocolHeader == null ) ? null : request.getHeader ( this.protocolHeader ) ) + "' will be seen as newRemoteAddr='" + xRequest.getRemoteAddr() + "', newRemoteHost='" + xRequest.getRemoteHost() + "', newScheme='" + xRequest.getScheme() + "', newSecure='" + xRequest.isSecure() + "', new[" + this.remoteIpHeader + "]='" + xRequest.getHeader ( this.remoteIpHeader ) + "', new[" + this.proxiesHeader + "]='" + xRequest.getHeader ( this.proxiesHeader ) + "'" );
            }
            if ( this.requestAttributesEnabled ) {
                request.setAttribute ( "org.apache.catalina.AccessLog.RemoteAddr", ( Object ) xRequest.getRemoteAddr() );
                request.setAttribute ( "org.apache.tomcat.remoteAddr", ( Object ) xRequest.getRemoteAddr() );
                request.setAttribute ( "org.apache.catalina.AccessLog.RemoteHost", ( Object ) xRequest.getRemoteHost() );
                request.setAttribute ( "org.apache.catalina.AccessLog.Protocol", ( Object ) xRequest.getProtocol() );
                request.setAttribute ( "org.apache.catalina.AccessLog.ServerPort", ( Object ) xRequest.getServerPort() );
            }
            chain.doFilter ( ( ServletRequest ) xRequest, ( ServletResponse ) response );
        } else {
            if ( RemoteIpFilter.log.isDebugEnabled() ) {
                RemoteIpFilter.log.debug ( "Skip RemoteIpFilter for request " + request.getRequestURI() + " with originalRemoteAddr '" + request.getRemoteAddr() + "'" );
            }
            chain.doFilter ( ( ServletRequest ) request, ( ServletResponse ) response );
        }
    }
    private void setPorts ( final XForwardedRequest xrequest, final int defaultPort ) {
        int port = defaultPort;
        if ( this.getPortHeader() != null ) {
            final String portHeaderValue = xrequest.getHeader ( this.getPortHeader() );
            if ( portHeaderValue != null ) {
                try {
                    port = Integer.parseInt ( portHeaderValue );
                } catch ( NumberFormatException nfe ) {
                    RemoteIpFilter.log.debug ( "Invalid port value [" + portHeaderValue + "] provided in header [" + this.getPortHeader() + "]" );
                }
            }
        }
        xrequest.setServerPort ( port );
        if ( this.isChangeLocalPort() ) {
            xrequest.setLocalPort ( port );
        }
    }
    public void doFilter ( final ServletRequest request, final ServletResponse response, final FilterChain chain ) throws IOException, ServletException {
        if ( request instanceof HttpServletRequest && response instanceof HttpServletResponse ) {
            this.doFilter ( ( HttpServletRequest ) request, ( HttpServletResponse ) response, chain );
        } else {
            chain.doFilter ( request, response );
        }
    }
    public boolean isChangeLocalPort() {
        return this.changeLocalPort;
    }
    public int getHttpsServerPort() {
        return this.httpsServerPort;
    }
    public Pattern getInternalProxies() {
        return this.internalProxies;
    }
    public String getProtocolHeader() {
        return this.protocolHeader;
    }
    public String getPortHeader() {
        return this.portHeader;
    }
    public String getProtocolHeaderHttpsValue() {
        return this.protocolHeaderHttpsValue;
    }
    public String getProxiesHeader() {
        return this.proxiesHeader;
    }
    public String getRemoteIpHeader() {
        return this.remoteIpHeader;
    }
    public boolean getRequestAttributesEnabled() {
        return this.requestAttributesEnabled;
    }
    public Pattern getTrustedProxies() {
        return this.trustedProxies;
    }
    public void init() throws ServletException {
        if ( this.getInitParameter ( "internalProxies" ) != null ) {
            this.setInternalProxies ( this.getInitParameter ( "internalProxies" ) );
        }
        if ( this.getInitParameter ( "protocolHeader" ) != null ) {
            this.setProtocolHeader ( this.getInitParameter ( "protocolHeader" ) );
        }
        if ( this.getInitParameter ( "protocolHeaderHttpsValue" ) != null ) {
            this.setProtocolHeaderHttpsValue ( this.getInitParameter ( "protocolHeaderHttpsValue" ) );
        }
        if ( this.getInitParameter ( "portHeader" ) != null ) {
            this.setPortHeader ( this.getInitParameter ( "portHeader" ) );
        }
        if ( this.getInitParameter ( "changeLocalPort" ) != null ) {
            this.setChangeLocalPort ( Boolean.parseBoolean ( this.getInitParameter ( "changeLocalPort" ) ) );
        }
        if ( this.getInitParameter ( "proxiesHeader" ) != null ) {
            this.setProxiesHeader ( this.getInitParameter ( "proxiesHeader" ) );
        }
        if ( this.getInitParameter ( "remoteIpHeader" ) != null ) {
            this.setRemoteIpHeader ( this.getInitParameter ( "remoteIpHeader" ) );
        }
        if ( this.getInitParameter ( "trustedProxies" ) != null ) {
            this.setTrustedProxies ( this.getInitParameter ( "trustedProxies" ) );
        }
        if ( this.getInitParameter ( "httpServerPort" ) != null ) {
            try {
                this.setHttpServerPort ( Integer.parseInt ( this.getInitParameter ( "httpServerPort" ) ) );
            } catch ( NumberFormatException e ) {
                throw new NumberFormatException ( "Illegal httpServerPort : " + e.getMessage() );
            }
        }
        if ( this.getInitParameter ( "httpsServerPort" ) != null ) {
            try {
                this.setHttpsServerPort ( Integer.parseInt ( this.getInitParameter ( "httpsServerPort" ) ) );
            } catch ( NumberFormatException e ) {
                throw new NumberFormatException ( "Illegal httpsServerPort : " + e.getMessage() );
            }
        }
    }
    public void setChangeLocalPort ( final boolean changeLocalPort ) {
        this.changeLocalPort = changeLocalPort;
    }
    public void setHttpServerPort ( final int httpServerPort ) {
        this.httpServerPort = httpServerPort;
    }
    public void setHttpsServerPort ( final int httpsServerPort ) {
        this.httpsServerPort = httpsServerPort;
    }
    public void setInternalProxies ( final String internalProxies ) {
        if ( internalProxies == null || internalProxies.length() == 0 ) {
            this.internalProxies = null;
        } else {
            this.internalProxies = Pattern.compile ( internalProxies );
        }
    }
    public void setPortHeader ( final String portHeader ) {
        this.portHeader = portHeader;
    }
    public void setProtocolHeader ( final String protocolHeader ) {
        this.protocolHeader = protocolHeader;
    }
    public void setProtocolHeaderHttpsValue ( final String protocolHeaderHttpsValue ) {
        this.protocolHeaderHttpsValue = protocolHeaderHttpsValue;
    }
    public void setProxiesHeader ( final String proxiesHeader ) {
        this.proxiesHeader = proxiesHeader;
    }
    public void setRemoteIpHeader ( final String remoteIpHeader ) {
        this.remoteIpHeader = remoteIpHeader;
    }
    public void setRequestAttributesEnabled ( final boolean requestAttributesEnabled ) {
        this.requestAttributesEnabled = requestAttributesEnabled;
    }
    public void setTrustedProxies ( final String trustedProxies ) {
        if ( trustedProxies == null || trustedProxies.length() == 0 ) {
            this.trustedProxies = null;
        } else {
            this.trustedProxies = Pattern.compile ( trustedProxies );
        }
    }
    static {
        commaSeparatedValuesPattern = Pattern.compile ( "\\s*,\\s*" );
        log = LogFactory.getLog ( RemoteIpFilter.class );
    }
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
}
