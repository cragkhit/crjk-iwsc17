package org.apache.catalina.valves;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import org.apache.catalina.AccessLog;
import org.apache.catalina.Globals;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.http.MimeHeaders;
public class RemoteIpValve extends ValveBase {
    private static final Pattern commaSeparatedValuesPattern = Pattern.compile ( "\\s*,\\s*" );
    private static final Log log = LogFactory.getLog ( RemoteIpValve.class );
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
    private boolean changeLocalPort = false;
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
    private String proxiesHeader = "X-Forwarded-By";
    private String remoteIpHeader = "X-Forwarded-For";
    private boolean requestAttributesEnabled = true;
    private Pattern trustedProxies = null;
    public RemoteIpValve() {
        super ( true );
    }
    public int getHttpsServerPort() {
        return httpsServerPort;
    }
    public int getHttpServerPort() {
        return httpServerPort;
    }
    public boolean isChangeLocalPort() {
        return changeLocalPort;
    }
    public void setChangeLocalPort ( boolean changeLocalPort ) {
        this.changeLocalPort = changeLocalPort;
    }
    public String getPortHeader() {
        return portHeader;
    }
    public void setPortHeader ( String portHeader ) {
        this.portHeader = portHeader;
    }
    public String getInternalProxies() {
        if ( internalProxies == null ) {
            return null;
        }
        return internalProxies.toString();
    }
    public String getProtocolHeader() {
        return protocolHeader;
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
    public String getTrustedProxies() {
        if ( trustedProxies == null ) {
            return null;
        }
        return trustedProxies.toString();
    }
    @Override
    public void invoke ( Request request, Response response ) throws IOException, ServletException {
        final String originalRemoteAddr = request.getRemoteAddr();
        final String originalRemoteHost = request.getRemoteHost();
        final String originalScheme = request.getScheme();
        final boolean originalSecure = request.isSecure();
        final int originalServerPort = request.getServerPort();
        final String originalProxiesHeader = request.getHeader ( proxiesHeader );
        final String originalRemoteIpHeader = request.getHeader ( remoteIpHeader );
        if ( internalProxies != null &&
                internalProxies.matcher ( originalRemoteAddr ).matches() ) {
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
            if ( remoteIp != null ) {
                request.setRemoteAddr ( remoteIp );
                request.setRemoteHost ( remoteIp );
                if ( proxiesHeaderValue.size() == 0 ) {
                    request.getCoyoteRequest().getMimeHeaders().removeHeader ( proxiesHeader );
                } else {
                    String commaDelimitedListOfProxies = listToCommaDelimitedString ( proxiesHeaderValue );
                    request.getCoyoteRequest().getMimeHeaders().setValue ( proxiesHeader ).setString ( commaDelimitedListOfProxies );
                }
                if ( newRemoteIpHeaderValue.size() == 0 ) {
                    request.getCoyoteRequest().getMimeHeaders().removeHeader ( remoteIpHeader );
                } else {
                    String commaDelimitedRemoteIpHeaderValue = listToCommaDelimitedString ( newRemoteIpHeaderValue );
                    request.getCoyoteRequest().getMimeHeaders().setValue ( remoteIpHeader ).setString ( commaDelimitedRemoteIpHeaderValue );
                }
            }
            if ( protocolHeader != null ) {
                String protocolHeaderValue = request.getHeader ( protocolHeader );
                if ( protocolHeaderValue == null ) {
                } else if ( protocolHeaderHttpsValue.equalsIgnoreCase ( protocolHeaderValue ) ) {
                    request.setSecure ( true );
                    request.getCoyoteRequest().scheme().setString ( "https" );
                    setPorts ( request, httpsServerPort );
                } else {
                    request.setSecure ( false );
                    request.getCoyoteRequest().scheme().setString ( "http" );
                    setPorts ( request, httpServerPort );
                }
            }
            if ( log.isDebugEnabled() ) {
                log.debug ( "Incoming request " + request.getRequestURI() + " with originalRemoteAddr '" + originalRemoteAddr
                            + "', originalRemoteHost='" + originalRemoteHost + "', originalSecure='" + originalSecure + "', originalScheme='"
                            + originalScheme + "' will be seen as newRemoteAddr='" + request.getRemoteAddr() + "', newRemoteHost='"
                            + request.getRemoteHost() + "', newScheme='" + request.getScheme() + "', newSecure='" + request.isSecure() + "'" );
            }
        } else {
            if ( log.isDebugEnabled() ) {
                log.debug ( "Skip RemoteIpValve for request " + request.getRequestURI() + " with originalRemoteAddr '"
                            + request.getRemoteAddr() + "'" );
            }
        }
        if ( requestAttributesEnabled ) {
            request.setAttribute ( AccessLog.REMOTE_ADDR_ATTRIBUTE,
                                   request.getRemoteAddr() );
            request.setAttribute ( Globals.REMOTE_ADDR_ATTRIBUTE,
                                   request.getRemoteAddr() );
            request.setAttribute ( AccessLog.REMOTE_HOST_ATTRIBUTE,
                                   request.getRemoteHost() );
            request.setAttribute ( AccessLog.PROTOCOL_ATTRIBUTE,
                                   request.getProtocol() );
            request.setAttribute ( AccessLog.SERVER_PORT_ATTRIBUTE,
                                   Integer.valueOf ( request.getServerPort() ) );
        }
        try {
            getNext().invoke ( request, response );
        } finally {
            request.setRemoteAddr ( originalRemoteAddr );
            request.setRemoteHost ( originalRemoteHost );
            request.setSecure ( originalSecure );
            MimeHeaders headers = request.getCoyoteRequest().getMimeHeaders();
            request.getCoyoteRequest().scheme().setString ( originalScheme );
            request.setServerPort ( originalServerPort );
            if ( originalProxiesHeader == null || originalProxiesHeader.length() == 0 ) {
                headers.removeHeader ( proxiesHeader );
            } else {
                headers.setValue ( proxiesHeader ).setString ( originalProxiesHeader );
            }
            if ( originalRemoteIpHeader == null || originalRemoteIpHeader.length() == 0 ) {
                headers.removeHeader ( remoteIpHeader );
            } else {
                headers.setValue ( remoteIpHeader ).setString ( originalRemoteIpHeader );
            }
        }
    }
    private void setPorts ( Request request, int defaultPort ) {
        int port = defaultPort;
        if ( portHeader != null ) {
            String portHeaderValue = request.getHeader ( portHeader );
            if ( portHeaderValue != null ) {
                try {
                    port = Integer.parseInt ( portHeaderValue );
                } catch ( NumberFormatException nfe ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( sm.getString (
                                        "remoteIpValve.invalidPortHeader",
                                        portHeaderValue, portHeader ), nfe );
                    }
                }
            }
        }
        request.setServerPort ( port );
        if ( changeLocalPort ) {
            request.setLocalPort ( port );
        }
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
