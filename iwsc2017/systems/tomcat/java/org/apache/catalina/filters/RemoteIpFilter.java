package org.apache.catalina.filters;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.FilterChain;
import javax.servlet.GenericFilter;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.PushBuilder;
import org.apache.catalina.AccessLog;
import org.apache.catalina.Globals;
import org.apache.catalina.core.ApplicationPushBuilder;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class RemoteIpFilter extends GenericFilter {
    private static final long serialVersionUID = 1L;
    public static class XForwardedRequest extends HttpServletRequestWrapper {
        static final ThreadLocal<SimpleDateFormat[]> threadLocalDateFormats = new ThreadLocal<SimpleDateFormat[]>() {
            @Override
            protected SimpleDateFormat[] initialValue() {
                return new SimpleDateFormat[] {
                           new SimpleDateFormat ( "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US ),
                           new SimpleDateFormat ( "EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US ),
                           new SimpleDateFormat ( "EEE MMMM d HH:mm:ss yyyy", Locale.US )
                       };
            }
        };
        protected final Map<String, List<String>> headers;
        protected int localPort;
        protected String remoteAddr;
        protected String remoteHost;
        protected String scheme;
        protected boolean secure;
        protected int serverPort;
        public XForwardedRequest ( HttpServletRequest request ) {
            super ( request );
            this.localPort = request.getLocalPort();
            this.remoteAddr = request.getRemoteAddr();
            this.remoteHost = request.getRemoteHost();
            this.scheme = request.getScheme();
            this.secure = request.isSecure();
            this.serverPort = request.getServerPort();
            headers = new HashMap<>();
            for ( Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements(); ) {
                String header = headerNames.nextElement();
                headers.put ( header, Collections.list ( request.getHeaders ( header ) ) );
            }
        }
        @Override
        public long getDateHeader ( String name ) {
            String value = getHeader ( name );
            if ( value == null ) {
                return -1;
            }
            DateFormat[] dateFormats = threadLocalDateFormats.get();
            Date date = null;
            for ( int i = 0; ( ( i < dateFormats.length ) && ( date == null ) ); i++ ) {
                DateFormat dateFormat = dateFormats[i];
                try {
                    date = dateFormat.parse ( value );
                } catch ( Exception ParseException ) {
                }
            }
            if ( date == null ) {
                throw new IllegalArgumentException ( value );
            }
            return date.getTime();
        }
        @Override
        public String getHeader ( String name ) {
            Map.Entry<String, List<String>> header = getHeaderEntry ( name );
            if ( header == null || header.getValue() == null || header.getValue().isEmpty() ) {
                return null;
            }
            return header.getValue().get ( 0 );
        }
        protected Map.Entry<String, List<String>> getHeaderEntry ( String name ) {
            for ( Map.Entry<String, List<String>> entry : headers.entrySet() ) {
                if ( entry.getKey().equalsIgnoreCase ( name ) ) {
                    return entry;
                }
            }
            return null;
        }
        @Override
        public Enumeration<String> getHeaderNames() {
            return Collections.enumeration ( headers.keySet() );
        }
        @Override
        public Enumeration<String> getHeaders ( String name ) {
            Map.Entry<String, List<String>> header = getHeaderEntry ( name );
            if ( header == null || header.getValue() == null ) {
                return Collections.enumeration ( Collections.<String>emptyList() );
            }
            return Collections.enumeration ( header.getValue() );
        }
        @Override
        public int getIntHeader ( String name ) {
            String value = getHeader ( name );
            if ( value == null ) {
                return -1;
            }
            return Integer.parseInt ( value );
        }
        @Override
        public int getLocalPort() {
            return localPort;
        }
        @Override
        public String getRemoteAddr() {
            return this.remoteAddr;
        }
        @Override
        public String getRemoteHost() {
            return this.remoteHost;
        }
        @Override
        public String getScheme() {
            return scheme;
        }
        @Override
        public int getServerPort() {
            return serverPort;
        }
        @Override
        public boolean isSecure() {
            return secure;
        }
        public void removeHeader ( String name ) {
            Map.Entry<String, List<String>> header = getHeaderEntry ( name );
            if ( header != null ) {
                headers.remove ( header.getKey() );
            }
        }
        public void setHeader ( String name, String value ) {
            List<String> values = Arrays.asList ( value );
            Map.Entry<String, List<String>> header = getHeaderEntry ( name );
            if ( header == null ) {
                headers.put ( name, values );
            } else {
                header.setValue ( values );
            }
        }
        public void setLocalPort ( int localPort ) {
            this.localPort = localPort;
        }
        public void setRemoteAddr ( String remoteAddr ) {
            this.remoteAddr = remoteAddr;
        }
        public void setRemoteHost ( String remoteHost ) {
            this.remoteHost = remoteHost;
        }
        public void setScheme ( String scheme ) {
            this.scheme = scheme;
        }
        public void setSecure ( boolean secure ) {
            this.secure = secure;
        }
        public void setServerPort ( int serverPort ) {
            this.serverPort = serverPort;
        }
        @Override
        public StringBuffer getRequestURL() {
            StringBuffer url = new StringBuffer();
            String scheme = getScheme();
            int port = getServerPort();
            if ( port < 0 ) {
                port = 80;
            }
            url.append ( scheme );
            url.append ( "://" );
            url.append ( getServerName() );
            if ( ( scheme.equals ( "http" ) && ( port != 80 ) )
                    || ( scheme.equals ( "https" ) && ( port != 443 ) ) ) {
                url.append ( ':' );
                url.append ( port );
            }
            url.append ( getRequestURI() );
            return url;
        }
        @Override
        public PushBuilder getPushBuilder() {
            return new ApplicationPushBuilder ( this );
        }
    }
    private static final Pattern commaSeparatedValuesPattern = Pattern.compile ( "\\s*,\\s*" );
    protected static final String HTTP_SERVER_PORT_PARAMETER = "httpServerPort";
    protected static final String HTTPS_SERVER_PORT_PARAMETER = "httpsServerPort";
    protected static final String INTERNAL_PROXIES_PARAMETER = "internalProxies";
    private static final Log log = LogFactory.getLog ( RemoteIpFilter.class );
    protected static final String PROTOCOL_HEADER_PARAMETER = "protocolHeader";
    protected static final String PROTOCOL_HEADER_HTTPS_VALUE_PARAMETER = "protocolHeaderHttpsValue";
    protected static final String PORT_HEADER_PARAMETER = "portHeader";
    protected static final String CHANGE_LOCAL_PORT_PARAMETER = "changeLocalPort";
    protected static final String PROXIES_HEADER_PARAMETER = "proxiesHeader";
    protected static final String REMOTE_IP_HEADER_PARAMETER = "remoteIpHeader";
    protected static final String TRUSTED_PROXIES_PARAMETER = "trustedProxies";
    protected static String[] commaDelimitedListToStringArray ( String commaDelimitedStrings ) {
        return ( commaDelimitedStrings == null || commaDelimitedStrings.length() == 0 ) ? new String[0] : commaSeparatedValuesPattern
               .split ( commaDelimitedStrings );
    }
    protected static String listToCommaDelimitedString ( List<String> stringList ) {
        if ( stringList == null ) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for ( Iterator<String> it = stringList.iterator(); it.hasNext(); ) {
            Object element = it.next();
            if ( element != null ) {
                result.append ( element );
                if ( it.hasNext() ) {
                    result.append ( ", " );
                }
            }
        }
        return result.toString();
    }
    private int httpServerPort = 80;
    private int httpsServerPort = 443;
    private Pattern internalProxies = Pattern.compile (
                                          "10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|" +
                                          "192\\.168\\.\\d{1,3}\\.\\d{1,3}|" +
                                          "169\\.254\\.\\d{1,3}\\.\\d{1,3}|" +
                                          "127\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|" +
                                          "172\\.1[6-9]{1}\\.\\d{1,3}\\.\\d{1,3}|" +
                                          "172\\.2[0-9]{1}\\.\\d{1,3}\\.\\d{1,3}|" +
                                          "172\\.3[0-1]{1}\\.\\d{1,3}\\.\\d{1,3}" );
    private String protocolHeader = null;
    private String protocolHeaderHttpsValue = "https";
    private String portHeader = null;
    private boolean changeLocalPort = false;
    private String proxiesHeader = "X-Forwarded-By";
    private String remoteIpHeader = "X-Forwarded-For";
    private boolean requestAttributesEnabled = true;
    private Pattern trustedProxies = null;
    public void doFilter ( HttpServletRequest request, HttpServletResponse response, FilterChain chain ) throws IOException, ServletException {
        if ( internalProxies != null &&
                internalProxies.matcher ( request.getRemoteAddr() ).matches() ) {
            String remoteIp = null;
            LinkedList<String> proxiesHeaderValue = new LinkedList<>();
            StringBuilder concatRemoteIpHeaderValue = new StringBuilder();
            for ( Enumeration<String> e = request.getHeaders ( remoteIpHeader ); e.hasMoreElements(); ) {
                if ( concatRemoteIpHeaderValue.length() > 0 ) {
                    concatRemoteIpHeaderValue.append ( ", " );
                }
                concatRemoteIpHeaderValue.append ( e.nextElement() );
            }
            String[] remoteIpHeaderValue = commaDelimitedListToStringArray ( concatRemoteIpHeaderValue.toString() );
            int idx;
            for ( idx = remoteIpHeaderValue.length - 1; idx >= 0; idx-- ) {
                String currentRemoteIp = remoteIpHeaderValue[idx];
                remoteIp = currentRemoteIp;
                if ( internalProxies.matcher ( currentRemoteIp ).matches() ) {
                } else if ( trustedProxies != null &&
                            trustedProxies.matcher ( currentRemoteIp ).matches() ) {
                    proxiesHeaderValue.addFirst ( currentRemoteIp );
                } else {
                    idx--;
                    break;
                }
            }
            LinkedList<String> newRemoteIpHeaderValue = new LinkedList<>();
            for ( ; idx >= 0; idx-- ) {
                String currentRemoteIp = remoteIpHeaderValue[idx];
                newRemoteIpHeaderValue.addFirst ( currentRemoteIp );
            }
            XForwardedRequest xRequest = new XForwardedRequest ( request );
            if ( remoteIp != null ) {
                xRequest.setRemoteAddr ( remoteIp );
                xRequest.setRemoteHost ( remoteIp );
                if ( proxiesHeaderValue.size() == 0 ) {
                    xRequest.removeHeader ( proxiesHeader );
                } else {
                    String commaDelimitedListOfProxies = listToCommaDelimitedString ( proxiesHeaderValue );
                    xRequest.setHeader ( proxiesHeader, commaDelimitedListOfProxies );
                }
                if ( newRemoteIpHeaderValue.size() == 0 ) {
                    xRequest.removeHeader ( remoteIpHeader );
                } else {
                    String commaDelimitedRemoteIpHeaderValue = listToCommaDelimitedString ( newRemoteIpHeaderValue );
                    xRequest.setHeader ( remoteIpHeader, commaDelimitedRemoteIpHeaderValue );
                }
            }
            if ( protocolHeader != null ) {
                String protocolHeaderValue = request.getHeader ( protocolHeader );
                if ( protocolHeaderValue == null ) {
                } else if ( protocolHeaderHttpsValue.equalsIgnoreCase ( protocolHeaderValue ) ) {
                    xRequest.setSecure ( true );
                    xRequest.setScheme ( "https" );
                    setPorts ( xRequest, httpsServerPort );
                } else {
                    xRequest.setSecure ( false );
                    xRequest.setScheme ( "http" );
                    setPorts ( xRequest, httpServerPort );
                }
            }
            if ( log.isDebugEnabled() ) {
                log.debug ( "Incoming request " + request.getRequestURI() + " with originalRemoteAddr '" + request.getRemoteAddr()
                            + "', originalRemoteHost='" + request.getRemoteHost() + "', originalSecure='" + request.isSecure()
                            + "', originalScheme='" + request.getScheme() + "', original[" + remoteIpHeader + "]='"
                            + concatRemoteIpHeaderValue + "', original[" + protocolHeader + "]='"
                            + ( protocolHeader == null ? null : request.getHeader ( protocolHeader ) ) + "' will be seen as newRemoteAddr='"
                            + xRequest.getRemoteAddr() + "', newRemoteHost='" + xRequest.getRemoteHost() + "', newScheme='"
                            + xRequest.getScheme() + "', newSecure='" + xRequest.isSecure() + "', new[" + remoteIpHeader + "]='"
                            + xRequest.getHeader ( remoteIpHeader ) + "', new[" + proxiesHeader + "]='" + xRequest.getHeader ( proxiesHeader ) + "'" );
            }
            if ( requestAttributesEnabled ) {
                request.setAttribute ( AccessLog.REMOTE_ADDR_ATTRIBUTE,
                                       xRequest.getRemoteAddr() );
                request.setAttribute ( Globals.REMOTE_ADDR_ATTRIBUTE,
                                       xRequest.getRemoteAddr() );
                request.setAttribute ( AccessLog.REMOTE_HOST_ATTRIBUTE,
                                       xRequest.getRemoteHost() );
                request.setAttribute ( AccessLog.PROTOCOL_ATTRIBUTE,
                                       xRequest.getProtocol() );
                request.setAttribute ( AccessLog.SERVER_PORT_ATTRIBUTE,
                                       Integer.valueOf ( xRequest.getServerPort() ) );
            }
            chain.doFilter ( xRequest, response );
        } else {
            if ( log.isDebugEnabled() ) {
                log.debug ( "Skip RemoteIpFilter for request " + request.getRequestURI() + " with originalRemoteAddr '"
                            + request.getRemoteAddr() + "'" );
            }
            chain.doFilter ( request, response );
        }
    }
    private void setPorts ( XForwardedRequest xrequest, int defaultPort ) {
        int port = defaultPort;
        if ( getPortHeader() != null ) {
            String portHeaderValue = xrequest.getHeader ( getPortHeader() );
            if ( portHeaderValue != null ) {
                try {
                    port = Integer.parseInt ( portHeaderValue );
                } catch ( NumberFormatException nfe ) {
                    log.debug ( "Invalid port value [" + portHeaderValue +
                                "] provided in header [" + getPortHeader() + "]" );
                }
            }
        }
        xrequest.setServerPort ( port );
        if ( isChangeLocalPort() ) {
            xrequest.setLocalPort ( port );
        }
    }
    @Override
    public void doFilter ( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException {
        if ( request instanceof HttpServletRequest && response instanceof HttpServletResponse ) {
            doFilter ( ( HttpServletRequest ) request, ( HttpServletResponse ) response, chain );
        } else {
            chain.doFilter ( request, response );
        }
    }
    public boolean isChangeLocalPort() {
        return changeLocalPort;
    }
    public int getHttpsServerPort() {
        return httpsServerPort;
    }
    public Pattern getInternalProxies() {
        return internalProxies;
    }
    public String getProtocolHeader() {
        return protocolHeader;
    }
    public String getPortHeader() {
        return portHeader;
    }
    public String getProtocolHeaderHttpsValue() {
        return protocolHeaderHttpsValue;
    }
    public String getProxiesHeader() {
        return proxiesHeader;
    }
    public String getRemoteIpHeader() {
        return remoteIpHeader;
    }
    public boolean getRequestAttributesEnabled() {
        return requestAttributesEnabled;
    }
    public Pattern getTrustedProxies() {
        return trustedProxies;
    }
    @Override
    public void init() throws ServletException {
        if ( getInitParameter ( INTERNAL_PROXIES_PARAMETER ) != null ) {
            setInternalProxies ( getInitParameter ( INTERNAL_PROXIES_PARAMETER ) );
        }
        if ( getInitParameter ( PROTOCOL_HEADER_PARAMETER ) != null ) {
            setProtocolHeader ( getInitParameter ( PROTOCOL_HEADER_PARAMETER ) );
        }
        if ( getInitParameter ( PROTOCOL_HEADER_HTTPS_VALUE_PARAMETER ) != null ) {
            setProtocolHeaderHttpsValue ( getInitParameter ( PROTOCOL_HEADER_HTTPS_VALUE_PARAMETER ) );
        }
        if ( getInitParameter ( PORT_HEADER_PARAMETER ) != null ) {
            setPortHeader ( getInitParameter ( PORT_HEADER_PARAMETER ) );
        }
        if ( getInitParameter ( CHANGE_LOCAL_PORT_PARAMETER ) != null ) {
            setChangeLocalPort ( Boolean.parseBoolean ( getInitParameter ( CHANGE_LOCAL_PORT_PARAMETER ) ) );
        }
        if ( getInitParameter ( PROXIES_HEADER_PARAMETER ) != null ) {
            setProxiesHeader ( getInitParameter ( PROXIES_HEADER_PARAMETER ) );
        }
        if ( getInitParameter ( REMOTE_IP_HEADER_PARAMETER ) != null ) {
            setRemoteIpHeader ( getInitParameter ( REMOTE_IP_HEADER_PARAMETER ) );
        }
        if ( getInitParameter ( TRUSTED_PROXIES_PARAMETER ) != null ) {
            setTrustedProxies ( getInitParameter ( TRUSTED_PROXIES_PARAMETER ) );
        }
        if ( getInitParameter ( HTTP_SERVER_PORT_PARAMETER ) != null ) {
            try {
                setHttpServerPort ( Integer.parseInt ( getInitParameter ( HTTP_SERVER_PORT_PARAMETER ) ) );
            } catch ( NumberFormatException e ) {
                throw new NumberFormatException ( "Illegal " + HTTP_SERVER_PORT_PARAMETER + " : " + e.getMessage() );
            }
        }
        if ( getInitParameter ( HTTPS_SERVER_PORT_PARAMETER ) != null ) {
            try {
                setHttpsServerPort ( Integer.parseInt ( getInitParameter ( HTTPS_SERVER_PORT_PARAMETER ) ) );
            } catch ( NumberFormatException e ) {
                throw new NumberFormatException ( "Illegal " + HTTPS_SERVER_PORT_PARAMETER + " : " + e.getMessage() );
            }
        }
    }
    public void setChangeLocalPort ( boolean changeLocalPort ) {
        this.changeLocalPort = changeLocalPort;
    }
    public void setHttpServerPort ( int httpServerPort ) {
        this.httpServerPort = httpServerPort;
    }
    public void setHttpsServerPort ( int httpsServerPort ) {
        this.httpsServerPort = httpsServerPort;
    }
    public void setInternalProxies ( String internalProxies ) {
        if ( internalProxies == null || internalProxies.length() == 0 ) {
            this.internalProxies = null;
        } else {
            this.internalProxies = Pattern.compile ( internalProxies );
        }
    }
    public void setPortHeader ( String portHeader ) {
        this.portHeader = portHeader;
    }
    public void setProtocolHeader ( String protocolHeader ) {
        this.protocolHeader = protocolHeader;
    }
    public void setProtocolHeaderHttpsValue ( String protocolHeaderHttpsValue ) {
        this.protocolHeaderHttpsValue = protocolHeaderHttpsValue;
    }
    public void setProxiesHeader ( String proxiesHeader ) {
        this.proxiesHeader = proxiesHeader;
    }
    public void setRemoteIpHeader ( String remoteIpHeader ) {
        this.remoteIpHeader = remoteIpHeader;
    }
    public void setRequestAttributesEnabled ( boolean requestAttributesEnabled ) {
        this.requestAttributesEnabled = requestAttributesEnabled;
    }
    public void setTrustedProxies ( String trustedProxies ) {
        if ( trustedProxies == null || trustedProxies.length() == 0 ) {
            this.trustedProxies = null;
        } else {
            this.trustedProxies = Pattern.compile ( trustedProxies );
        }
    }
}
