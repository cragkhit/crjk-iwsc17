package org.apache.catalina.connector;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import javax.management.ObjectName;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Service;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.coyote.Adapter;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.UpgradeProtocol;
import org.apache.coyote.http11.AbstractHttp11JsseProtocol;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.openssl.OpenSSLImplementation;
import org.apache.tomcat.util.res.StringManager;
public class Connector extends LifecycleMBeanBase  {
    private static final Log log = LogFactory.getLog ( Connector.class );
    public static final boolean RECYCLE_FACADES =
        Boolean.parseBoolean ( System.getProperty ( "org.apache.catalina.connector.RECYCLE_FACADES", "false" ) );
    public static final String INTERNAL_EXECUTOR_NAME = "Internal";
    public Connector() {
        this ( "org.apache.coyote.http11.Http11NioProtocol" );
    }
    public Connector ( String protocol ) {
        boolean aprConnector = AprLifecycleListener.isAprAvailable() &&
                               AprLifecycleListener.getUseAprConnector();
        if ( "HTTP/1.1".equals ( protocol ) || protocol == null ) {
            if ( aprConnector ) {
                protocolHandlerClassName = "org.apache.coyote.http11.Http11AprProtocol";
            } else {
                protocolHandlerClassName = "org.apache.coyote.http11.Http11NioProtocol";
            }
        } else if ( "AJP/1.3".equals ( protocol ) ) {
            if ( aprConnector ) {
                protocolHandlerClassName = "org.apache.coyote.ajp.AjpAprProtocol";
            } else {
                protocolHandlerClassName = "org.apache.coyote.ajp.AjpNioProtocol";
            }
        } else {
            protocolHandlerClassName = protocol;
        }
        ProtocolHandler p = null;
        try {
            Class<?> clazz = Class.forName ( protocolHandlerClassName );
            p = ( ProtocolHandler ) clazz.newInstance();
        } catch ( Exception e ) {
            log.error ( sm.getString (
                            "coyoteConnector.protocolHandlerInstantiationFailed" ), e );
        } finally {
            this.protocolHandler = p;
        }
        if ( !Globals.STRICT_SERVLET_COMPLIANCE ) {
            URIEncoding = "UTF-8";
            URIEncodingLower = URIEncoding.toLowerCase ( Locale.ENGLISH );
        }
        setThrowOnFailure ( Boolean.getBoolean ( "org.apache.catalina.startup.EXIT_ON_INIT_FAILURE" ) );
    }
    protected Service service = null;
    protected boolean allowTrace = false;
    protected long asyncTimeout = 30000;
    protected boolean enableLookups = false;
    protected boolean xpoweredBy = false;
    protected int port = -1;
    protected String proxyName = null;
    protected int proxyPort = 0;
    protected int redirectPort = 443;
    protected String scheme = "http";
    protected boolean secure = false;
    protected static final StringManager sm = StringManager.getManager ( Connector.class );
    private int maxCookieCount = 200;
    protected int maxParameterCount = 10000;
    protected int maxPostSize = 2 * 1024 * 1024;
    protected int maxSavePostSize = 4 * 1024;
    protected String parseBodyMethods = "POST";
    protected HashSet<String> parseBodyMethodsSet;
    protected boolean useIPVHosts = false;
    protected final String protocolHandlerClassName;
    protected final ProtocolHandler protocolHandler;
    protected Adapter adapter = null;
    protected String URIEncoding = null;
    protected String URIEncodingLower = null;
    protected boolean useBodyEncodingForURI = false;
    public Object getProperty ( String name ) {
        if ( protocolHandler == null ) {
            return null;
        }
        return IntrospectionUtils.getProperty ( protocolHandler, name );
    }
    public boolean setProperty ( String name, String value ) {
        if ( protocolHandler == null ) {
            return false;
        }
        return IntrospectionUtils.setProperty ( protocolHandler, name, value );
    }
    public Object getAttribute ( String name ) {
        return getProperty ( name );
    }
    public void setAttribute ( String name, Object value ) {
        setProperty ( name, String.valueOf ( value ) );
    }
    public Service getService() {
        return this.service;
    }
    public void setService ( Service service ) {
        this.service = service;
    }
    public boolean getAllowTrace() {
        return this.allowTrace;
    }
    public void setAllowTrace ( boolean allowTrace ) {
        this.allowTrace = allowTrace;
        setProperty ( "allowTrace", String.valueOf ( allowTrace ) );
    }
    public long getAsyncTimeout() {
        return asyncTimeout;
    }
    public void setAsyncTimeout ( long asyncTimeout ) {
        this.asyncTimeout = asyncTimeout;
        setProperty ( "asyncTimeout", String.valueOf ( asyncTimeout ) );
    }
    public boolean getEnableLookups() {
        return this.enableLookups;
    }
    public void setEnableLookups ( boolean enableLookups ) {
        this.enableLookups = enableLookups;
        setProperty ( "enableLookups", String.valueOf ( enableLookups ) );
    }
    public int getMaxCookieCount() {
        return maxCookieCount;
    }
    public void setMaxCookieCount ( int maxCookieCount ) {
        this.maxCookieCount = maxCookieCount;
    }
    public int getMaxParameterCount() {
        return maxParameterCount;
    }
    public void setMaxParameterCount ( int maxParameterCount ) {
        this.maxParameterCount = maxParameterCount;
        setProperty ( "maxParameterCount", String.valueOf ( maxParameterCount ) );
    }
    public int getMaxPostSize() {
        return maxPostSize;
    }
    public void setMaxPostSize ( int maxPostSize ) {
        this.maxPostSize = maxPostSize;
        setProperty ( "maxPostSize", String.valueOf ( maxPostSize ) );
    }
    public int getMaxSavePostSize() {
        return maxSavePostSize;
    }
    public void setMaxSavePostSize ( int maxSavePostSize ) {
        this.maxSavePostSize = maxSavePostSize;
        setProperty ( "maxSavePostSize", String.valueOf ( maxSavePostSize ) );
    }
    public String getParseBodyMethods() {
        return this.parseBodyMethods;
    }
    public void setParseBodyMethods ( String methods ) {
        HashSet<String> methodSet = new HashSet<>();
        if ( null != methods ) {
            methodSet.addAll ( Arrays.asList ( methods.split ( "\\s*,\\s*" ) ) );
        }
        if ( methodSet.contains ( "TRACE" ) ) {
            throw new IllegalArgumentException ( sm.getString ( "coyoteConnector.parseBodyMethodNoTrace" ) );
        }
        this.parseBodyMethods = methods;
        this.parseBodyMethodsSet = methodSet;
        setProperty ( "parseBodyMethods", methods );
    }
    protected boolean isParseBodyMethod ( String method ) {
        return parseBodyMethodsSet.contains ( method );
    }
    public int getPort() {
        return this.port;
    }
    public void setPort ( int port ) {
        this.port = port;
        setProperty ( "port", String.valueOf ( port ) );
    }
    public int getLocalPort() {
        return ( ( Integer ) getProperty ( "localPort" ) ).intValue();
    }
    public String getProtocol() {
        if ( ( "org.apache.coyote.http11.Http11NioProtocol".equals ( getProtocolHandlerClassName() ) &&
                ( !AprLifecycleListener.isAprAvailable() || !AprLifecycleListener.getUseAprConnector() ) ) ||
                "org.apache.coyote.http11.Http11AprProtocol".equals ( getProtocolHandlerClassName() ) &&
                AprLifecycleListener.getUseAprConnector() ) {
            return "HTTP/1.1";
        } else if ( ( "org.apache.coyote.ajp.AjpNioProtocol".equals ( getProtocolHandlerClassName() ) &&
                      ( !AprLifecycleListener.isAprAvailable() || !AprLifecycleListener.getUseAprConnector() ) ) ||
                    "org.apache.coyote.ajp.AjpAprProtocol".equals ( getProtocolHandlerClassName() ) &&
                    AprLifecycleListener.getUseAprConnector() ) {
            return "AJP/1.3";
        }
        return getProtocolHandlerClassName();
    }
    public String getProtocolHandlerClassName() {
        return this.protocolHandlerClassName;
    }
    public ProtocolHandler getProtocolHandler() {
        return this.protocolHandler;
    }
    public String getProxyName() {
        return this.proxyName;
    }
    public void setProxyName ( String proxyName ) {
        if ( proxyName != null && proxyName.length() > 0 ) {
            this.proxyName = proxyName;
        } else {
            this.proxyName = null;
        }
        setProperty ( "proxyName", this.proxyName );
    }
    public int getProxyPort() {
        return this.proxyPort;
    }
    public void setProxyPort ( int proxyPort ) {
        this.proxyPort = proxyPort;
        setProperty ( "proxyPort", String.valueOf ( proxyPort ) );
    }
    public int getRedirectPort() {
        return this.redirectPort;
    }
    public void setRedirectPort ( int redirectPort ) {
        this.redirectPort = redirectPort;
        setProperty ( "redirectPort", String.valueOf ( redirectPort ) );
    }
    public String getScheme() {
        return this.scheme;
    }
    public void setScheme ( String scheme ) {
        this.scheme = scheme;
    }
    public boolean getSecure() {
        return this.secure;
    }
    public void setSecure ( boolean secure ) {
        this.secure = secure;
        setProperty ( "secure", Boolean.toString ( secure ) );
    }
    public String getURIEncoding() {
        return this.URIEncoding;
    }
    public String getURIEncodingLower() {
        return this.URIEncodingLower;
    }
    public void setURIEncoding ( String URIEncoding ) {
        this.URIEncoding = URIEncoding;
        if ( URIEncoding == null ) {
            URIEncodingLower = null;
        } else {
            this.URIEncodingLower = URIEncoding.toLowerCase ( Locale.ENGLISH );
        }
        setProperty ( "uRIEncoding", URIEncoding );
    }
    public boolean getUseBodyEncodingForURI() {
        return this.useBodyEncodingForURI;
    }
    public void setUseBodyEncodingForURI ( boolean useBodyEncodingForURI ) {
        this.useBodyEncodingForURI = useBodyEncodingForURI;
        setProperty ( "useBodyEncodingForURI", String.valueOf ( useBodyEncodingForURI ) );
    }
    public boolean getXpoweredBy() {
        return xpoweredBy;
    }
    public void setXpoweredBy ( boolean xpoweredBy ) {
        this.xpoweredBy = xpoweredBy;
        setProperty ( "xpoweredBy", String.valueOf ( xpoweredBy ) );
    }
    public void setUseIPVHosts ( boolean useIPVHosts ) {
        this.useIPVHosts = useIPVHosts;
        setProperty ( "useIPVHosts", String.valueOf ( useIPVHosts ) );
    }
    public boolean getUseIPVHosts() {
        return useIPVHosts;
    }
    public String getExecutorName() {
        Object obj = protocolHandler.getExecutor();
        if ( obj instanceof org.apache.catalina.Executor ) {
            return ( ( org.apache.catalina.Executor ) obj ).getName();
        }
        return INTERNAL_EXECUTOR_NAME;
    }
    public void addSslHostConfig ( SSLHostConfig sslHostConfig ) {
        protocolHandler.addSslHostConfig ( sslHostConfig );
    }
    public SSLHostConfig[] findSslHostConfigs() {
        return protocolHandler.findSslHostConfigs();
    }
    public void addUpgradeProtocol ( UpgradeProtocol upgradeProtocol ) {
        protocolHandler.addUpgradeProtocol ( upgradeProtocol );
    }
    public UpgradeProtocol[] findUpgradeProtocols() {
        return protocolHandler.findUpgradeProtocols();
    }
    public Request createRequest() {
        Request request = new Request();
        request.setConnector ( this );
        return ( request );
    }
    public Response createResponse() {
        Response response = new Response();
        response.setConnector ( this );
        return ( response );
    }
    protected String createObjectNameKeyProperties ( String type ) {
        Object addressObj = getProperty ( "address" );
        StringBuilder sb = new StringBuilder ( "type=" );
        sb.append ( type );
        sb.append ( ",port=" );
        int port = getPort();
        if ( port > 0 ) {
            sb.append ( port );
        } else {
            sb.append ( "auto-" );
            sb.append ( getProperty ( "nameIndex" ) );
        }
        String address = "";
        if ( addressObj instanceof InetAddress ) {
            address = ( ( InetAddress ) addressObj ).getHostAddress();
        } else if ( addressObj != null ) {
            address = addressObj.toString();
        }
        if ( address.length() > 0 ) {
            sb.append ( ",address=" );
            sb.append ( ObjectName.quote ( address ) );
        }
        return sb.toString();
    }
    public void pause() {
        try {
            if ( protocolHandler != null ) {
                protocolHandler.pause();
            }
        } catch ( Exception e ) {
            log.error ( sm.getString ( "coyoteConnector.protocolHandlerPauseFailed" ), e );
        }
    }
    public void resume() {
        try {
            if ( protocolHandler != null ) {
                protocolHandler.resume();
            }
        } catch ( Exception e ) {
            log.error ( sm.getString ( "coyoteConnector.protocolHandlerResumeFailed" ), e );
        }
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        if ( protocolHandler == null ) {
            throw new LifecycleException (
                sm.getString ( "coyoteConnector.protocolHandlerInstantiationFailed" ) );
        }
        adapter = new CoyoteAdapter ( this );
        protocolHandler.setAdapter ( adapter );
        if ( null == parseBodyMethodsSet ) {
            setParseBodyMethods ( getParseBodyMethods() );
        }
        if ( protocolHandler.isAprRequired() && !AprLifecycleListener.isAprAvailable() ) {
            throw new LifecycleException ( sm.getString ( "coyoteConnector.protocolHandlerNoApr",
                                           getProtocolHandlerClassName() ) );
        }
        if ( AprLifecycleListener.isAprAvailable() && AprLifecycleListener.getUseOpenSSL() &&
                protocolHandler instanceof AbstractHttp11JsseProtocol ) {
            AbstractHttp11JsseProtocol<?> jsseProtocolHandler =
                ( AbstractHttp11JsseProtocol<?> ) protocolHandler;
            if ( jsseProtocolHandler.isSSLEnabled() &&
                    jsseProtocolHandler.getSslImplementationName() == null ) {
                jsseProtocolHandler.setSslImplementationName ( OpenSSLImplementation.class.getName() );
            }
        }
        try {
            protocolHandler.init();
        } catch ( Exception e ) {
            throw new LifecycleException (
                sm.getString ( "coyoteConnector.protocolHandlerInitializationFailed" ), e );
        }
    }
    @Override
    protected void startInternal() throws LifecycleException {
        if ( getPort() < 0 ) {
            throw new LifecycleException ( sm.getString (
                                               "coyoteConnector.invalidPort", Integer.valueOf ( getPort() ) ) );
        }
        setState ( LifecycleState.STARTING );
        try {
            protocolHandler.start();
        } catch ( Exception e ) {
            throw new LifecycleException (
                sm.getString ( "coyoteConnector.protocolHandlerStartFailed" ), e );
        }
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        setState ( LifecycleState.STOPPING );
        try {
            if ( protocolHandler != null ) {
                protocolHandler.stop();
            }
        } catch ( Exception e ) {
            throw new LifecycleException (
                sm.getString ( "coyoteConnector.protocolHandlerStopFailed" ), e );
        }
    }
    @Override
    protected void destroyInternal() throws LifecycleException {
        try {
            if ( protocolHandler != null ) {
                protocolHandler.destroy();
            }
        } catch ( Exception e ) {
            throw new LifecycleException (
                sm.getString ( "coyoteConnector.protocolHandlerDestroyFailed" ), e );
        }
        if ( getService() != null ) {
            getService().removeConnector ( this );
        }
        super.destroyInternal();
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "Connector[" );
        sb.append ( getProtocol() );
        sb.append ( '-' );
        int port = getPort();
        if ( port > 0 ) {
            sb.append ( port );
        } else {
            sb.append ( "auto-" );
            sb.append ( getProperty ( "nameIndex" ) );
        }
        sb.append ( ']' );
        return sb.toString();
    }
    @Override
    protected String getDomainInternal() {
        Service s = getService();
        if ( s == null ) {
            return null;
        } else {
            return service.getDomain();
        }
    }
    @Override
    protected String getObjectNameKeyProperties() {
        return createObjectNameKeyProperties ( "Connector" );
    }
}
