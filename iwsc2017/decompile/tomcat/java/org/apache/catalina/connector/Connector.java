package org.apache.catalina.connector;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.LifecycleState;
import org.apache.tomcat.util.net.openssl.OpenSSLImplementation;
import org.apache.coyote.http11.AbstractHttp11JsseProtocol;
import org.apache.catalina.LifecycleException;
import javax.management.ObjectName;
import java.net.InetAddress;
import org.apache.coyote.UpgradeProtocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.catalina.Executor;
import java.util.Collection;
import java.util.Arrays;
import org.apache.tomcat.util.IntrospectionUtils;
import java.util.Locale;
import org.apache.catalina.Globals;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.coyote.Adapter;
import org.apache.coyote.ProtocolHandler;
import java.util.HashSet;
import org.apache.tomcat.util.res.StringManager;
import org.apache.catalina.Service;
import org.apache.juli.logging.Log;
import org.apache.catalina.util.LifecycleMBeanBase;
public class Connector extends LifecycleMBeanBase {
    private static final Log log;
    public static final boolean RECYCLE_FACADES;
    public static final String INTERNAL_EXECUTOR_NAME = "Internal";
    protected Service service;
    protected boolean allowTrace;
    protected long asyncTimeout;
    protected boolean enableLookups;
    protected boolean xpoweredBy;
    protected int port;
    protected String proxyName;
    protected int proxyPort;
    protected int redirectPort;
    protected String scheme;
    protected boolean secure;
    protected static final StringManager sm;
    private int maxCookieCount;
    protected int maxParameterCount;
    protected int maxPostSize;
    protected int maxSavePostSize;
    protected String parseBodyMethods;
    protected HashSet<String> parseBodyMethodsSet;
    protected boolean useIPVHosts;
    protected final String protocolHandlerClassName;
    protected final ProtocolHandler protocolHandler;
    protected Adapter adapter;
    protected String URIEncoding;
    protected String URIEncodingLower;
    protected boolean useBodyEncodingForURI;
    public Connector() {
        this ( "org.apache.coyote.http11.Http11NioProtocol" );
    }
    public Connector ( final String protocol ) {
        this.service = null;
        this.allowTrace = false;
        this.asyncTimeout = 30000L;
        this.enableLookups = false;
        this.xpoweredBy = false;
        this.port = -1;
        this.proxyName = null;
        this.proxyPort = 0;
        this.redirectPort = 443;
        this.scheme = "http";
        this.secure = false;
        this.maxCookieCount = 200;
        this.maxParameterCount = 10000;
        this.maxPostSize = 2097152;
        this.maxSavePostSize = 4096;
        this.parseBodyMethods = "POST";
        this.useIPVHosts = false;
        this.adapter = null;
        this.URIEncoding = null;
        this.URIEncodingLower = null;
        this.useBodyEncodingForURI = false;
        final boolean aprConnector = AprLifecycleListener.isAprAvailable() && AprLifecycleListener.getUseAprConnector();
        if ( "HTTP/1.1".equals ( protocol ) || protocol == null ) {
            if ( aprConnector ) {
                this.protocolHandlerClassName = "org.apache.coyote.http11.Http11AprProtocol";
            } else {
                this.protocolHandlerClassName = "org.apache.coyote.http11.Http11NioProtocol";
            }
        } else if ( "AJP/1.3".equals ( protocol ) ) {
            if ( aprConnector ) {
                this.protocolHandlerClassName = "org.apache.coyote.ajp.AjpAprProtocol";
            } else {
                this.protocolHandlerClassName = "org.apache.coyote.ajp.AjpNioProtocol";
            }
        } else {
            this.protocolHandlerClassName = protocol;
        }
        ProtocolHandler p = null;
        try {
            final Class<?> clazz = Class.forName ( this.protocolHandlerClassName );
            p = ( ProtocolHandler ) clazz.newInstance();
        } catch ( Exception e ) {
            Connector.log.error ( Connector.sm.getString ( "coyoteConnector.protocolHandlerInstantiationFailed" ), e );
        } finally {
            this.protocolHandler = p;
        }
        if ( !Globals.STRICT_SERVLET_COMPLIANCE ) {
            this.URIEncoding = "UTF-8";
            this.URIEncodingLower = this.URIEncoding.toLowerCase ( Locale.ENGLISH );
        }
        this.setThrowOnFailure ( Boolean.getBoolean ( "org.apache.catalina.startup.EXIT_ON_INIT_FAILURE" ) );
    }
    public Object getProperty ( final String name ) {
        if ( this.protocolHandler == null ) {
            return null;
        }
        return IntrospectionUtils.getProperty ( this.protocolHandler, name );
    }
    public boolean setProperty ( final String name, final String value ) {
        return this.protocolHandler != null && IntrospectionUtils.setProperty ( this.protocolHandler, name, value );
    }
    public Object getAttribute ( final String name ) {
        return this.getProperty ( name );
    }
    public void setAttribute ( final String name, final Object value ) {
        this.setProperty ( name, String.valueOf ( value ) );
    }
    public Service getService() {
        return this.service;
    }
    public void setService ( final Service service ) {
        this.service = service;
    }
    public boolean getAllowTrace() {
        return this.allowTrace;
    }
    public void setAllowTrace ( final boolean allowTrace ) {
        this.allowTrace = allowTrace;
        this.setProperty ( "allowTrace", String.valueOf ( allowTrace ) );
    }
    public long getAsyncTimeout() {
        return this.asyncTimeout;
    }
    public void setAsyncTimeout ( final long asyncTimeout ) {
        this.asyncTimeout = asyncTimeout;
        this.setProperty ( "asyncTimeout", String.valueOf ( asyncTimeout ) );
    }
    public boolean getEnableLookups() {
        return this.enableLookups;
    }
    public void setEnableLookups ( final boolean enableLookups ) {
        this.enableLookups = enableLookups;
        this.setProperty ( "enableLookups", String.valueOf ( enableLookups ) );
    }
    public int getMaxCookieCount() {
        return this.maxCookieCount;
    }
    public void setMaxCookieCount ( final int maxCookieCount ) {
        this.maxCookieCount = maxCookieCount;
    }
    public int getMaxParameterCount() {
        return this.maxParameterCount;
    }
    public void setMaxParameterCount ( final int maxParameterCount ) {
        this.maxParameterCount = maxParameterCount;
        this.setProperty ( "maxParameterCount", String.valueOf ( maxParameterCount ) );
    }
    public int getMaxPostSize() {
        return this.maxPostSize;
    }
    public void setMaxPostSize ( final int maxPostSize ) {
        this.maxPostSize = maxPostSize;
        this.setProperty ( "maxPostSize", String.valueOf ( maxPostSize ) );
    }
    public int getMaxSavePostSize() {
        return this.maxSavePostSize;
    }
    public void setMaxSavePostSize ( final int maxSavePostSize ) {
        this.maxSavePostSize = maxSavePostSize;
        this.setProperty ( "maxSavePostSize", String.valueOf ( maxSavePostSize ) );
    }
    public String getParseBodyMethods() {
        return this.parseBodyMethods;
    }
    public void setParseBodyMethods ( final String methods ) {
        final HashSet<String> methodSet = new HashSet<String>();
        if ( null != methods ) {
            methodSet.addAll ( ( Collection<?> ) Arrays.asList ( methods.split ( "\\s*,\\s*" ) ) );
        }
        if ( methodSet.contains ( "TRACE" ) ) {
            throw new IllegalArgumentException ( Connector.sm.getString ( "coyoteConnector.parseBodyMethodNoTrace" ) );
        }
        this.parseBodyMethods = methods;
        this.parseBodyMethodsSet = methodSet;
        this.setProperty ( "parseBodyMethods", methods );
    }
    protected boolean isParseBodyMethod ( final String method ) {
        return this.parseBodyMethodsSet.contains ( method );
    }
    public int getPort() {
        return this.port;
    }
    public void setPort ( final int port ) {
        this.port = port;
        this.setProperty ( "port", String.valueOf ( port ) );
    }
    public int getLocalPort() {
        return ( int ) this.getProperty ( "localPort" );
    }
    public String getProtocol() {
        if ( ( "org.apache.coyote.http11.Http11NioProtocol".equals ( this.getProtocolHandlerClassName() ) && ( !AprLifecycleListener.isAprAvailable() || !AprLifecycleListener.getUseAprConnector() ) ) || ( "org.apache.coyote.http11.Http11AprProtocol".equals ( this.getProtocolHandlerClassName() ) && AprLifecycleListener.getUseAprConnector() ) ) {
            return "HTTP/1.1";
        }
        if ( ( "org.apache.coyote.ajp.AjpNioProtocol".equals ( this.getProtocolHandlerClassName() ) && ( !AprLifecycleListener.isAprAvailable() || !AprLifecycleListener.getUseAprConnector() ) ) || ( "org.apache.coyote.ajp.AjpAprProtocol".equals ( this.getProtocolHandlerClassName() ) && AprLifecycleListener.getUseAprConnector() ) ) {
            return "AJP/1.3";
        }
        return this.getProtocolHandlerClassName();
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
    public void setProxyName ( final String proxyName ) {
        if ( proxyName != null && proxyName.length() > 0 ) {
            this.proxyName = proxyName;
        } else {
            this.proxyName = null;
        }
        this.setProperty ( "proxyName", this.proxyName );
    }
    public int getProxyPort() {
        return this.proxyPort;
    }
    public void setProxyPort ( final int proxyPort ) {
        this.proxyPort = proxyPort;
        this.setProperty ( "proxyPort", String.valueOf ( proxyPort ) );
    }
    public int getRedirectPort() {
        return this.redirectPort;
    }
    public void setRedirectPort ( final int redirectPort ) {
        this.redirectPort = redirectPort;
        this.setProperty ( "redirectPort", String.valueOf ( redirectPort ) );
    }
    public String getScheme() {
        return this.scheme;
    }
    public void setScheme ( final String scheme ) {
        this.scheme = scheme;
    }
    public boolean getSecure() {
        return this.secure;
    }
    public void setSecure ( final boolean secure ) {
        this.secure = secure;
        this.setProperty ( "secure", Boolean.toString ( secure ) );
    }
    public String getURIEncoding() {
        return this.URIEncoding;
    }
    public String getURIEncodingLower() {
        return this.URIEncodingLower;
    }
    public void setURIEncoding ( final String URIEncoding ) {
        this.URIEncoding = URIEncoding;
        if ( URIEncoding == null ) {
            this.URIEncodingLower = null;
        } else {
            this.URIEncodingLower = URIEncoding.toLowerCase ( Locale.ENGLISH );
        }
        this.setProperty ( "uRIEncoding", URIEncoding );
    }
    public boolean getUseBodyEncodingForURI() {
        return this.useBodyEncodingForURI;
    }
    public void setUseBodyEncodingForURI ( final boolean useBodyEncodingForURI ) {
        this.useBodyEncodingForURI = useBodyEncodingForURI;
        this.setProperty ( "useBodyEncodingForURI", String.valueOf ( useBodyEncodingForURI ) );
    }
    public boolean getXpoweredBy() {
        return this.xpoweredBy;
    }
    public void setXpoweredBy ( final boolean xpoweredBy ) {
        this.xpoweredBy = xpoweredBy;
        this.setProperty ( "xpoweredBy", String.valueOf ( xpoweredBy ) );
    }
    public void setUseIPVHosts ( final boolean useIPVHosts ) {
        this.useIPVHosts = useIPVHosts;
        this.setProperty ( "useIPVHosts", String.valueOf ( useIPVHosts ) );
    }
    public boolean getUseIPVHosts() {
        return this.useIPVHosts;
    }
    public String getExecutorName() {
        final Object obj = this.protocolHandler.getExecutor();
        if ( obj instanceof Executor ) {
            return ( ( Executor ) obj ).getName();
        }
        return "Internal";
    }
    public void addSslHostConfig ( final SSLHostConfig sslHostConfig ) {
        this.protocolHandler.addSslHostConfig ( sslHostConfig );
    }
    public SSLHostConfig[] findSslHostConfigs() {
        return this.protocolHandler.findSslHostConfigs();
    }
    public void addUpgradeProtocol ( final UpgradeProtocol upgradeProtocol ) {
        this.protocolHandler.addUpgradeProtocol ( upgradeProtocol );
    }
    public UpgradeProtocol[] findUpgradeProtocols() {
        return this.protocolHandler.findUpgradeProtocols();
    }
    public Request createRequest() {
        final Request request = new Request();
        request.setConnector ( this );
        return request;
    }
    public Response createResponse() {
        final Response response = new Response();
        response.setConnector ( this );
        return response;
    }
    protected String createObjectNameKeyProperties ( final String type ) {
        final Object addressObj = this.getProperty ( "address" );
        final StringBuilder sb = new StringBuilder ( "type=" );
        sb.append ( type );
        sb.append ( ",port=" );
        final int port = this.getPort();
        if ( port > 0 ) {
            sb.append ( port );
        } else {
            sb.append ( "auto-" );
            sb.append ( this.getProperty ( "nameIndex" ) );
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
            if ( this.protocolHandler != null ) {
                this.protocolHandler.pause();
            }
        } catch ( Exception e ) {
            Connector.log.error ( Connector.sm.getString ( "coyoteConnector.protocolHandlerPauseFailed" ), e );
        }
    }
    public void resume() {
        try {
            if ( this.protocolHandler != null ) {
                this.protocolHandler.resume();
            }
        } catch ( Exception e ) {
            Connector.log.error ( Connector.sm.getString ( "coyoteConnector.protocolHandlerResumeFailed" ), e );
        }
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        if ( this.protocolHandler == null ) {
            throw new LifecycleException ( Connector.sm.getString ( "coyoteConnector.protocolHandlerInstantiationFailed" ) );
        }
        this.adapter = new CoyoteAdapter ( this );
        this.protocolHandler.setAdapter ( this.adapter );
        if ( null == this.parseBodyMethodsSet ) {
            this.setParseBodyMethods ( this.getParseBodyMethods() );
        }
        if ( this.protocolHandler.isAprRequired() && !AprLifecycleListener.isAprAvailable() ) {
            throw new LifecycleException ( Connector.sm.getString ( "coyoteConnector.protocolHandlerNoApr", this.getProtocolHandlerClassName() ) );
        }
        if ( AprLifecycleListener.isAprAvailable() && AprLifecycleListener.getUseOpenSSL() && this.protocolHandler instanceof AbstractHttp11JsseProtocol ) {
            final AbstractHttp11JsseProtocol<?> jsseProtocolHandler = ( AbstractHttp11JsseProtocol<?> ) this.protocolHandler;
            if ( jsseProtocolHandler.isSSLEnabled() && jsseProtocolHandler.getSslImplementationName() == null ) {
                jsseProtocolHandler.setSslImplementationName ( OpenSSLImplementation.class.getName() );
            }
        }
        try {
            this.protocolHandler.init();
        } catch ( Exception e ) {
            throw new LifecycleException ( Connector.sm.getString ( "coyoteConnector.protocolHandlerInitializationFailed" ), e );
        }
    }
    @Override
    protected void startInternal() throws LifecycleException {
        if ( this.getPort() < 0 ) {
            throw new LifecycleException ( Connector.sm.getString ( "coyoteConnector.invalidPort", this.getPort() ) );
        }
        this.setState ( LifecycleState.STARTING );
        try {
            this.protocolHandler.start();
        } catch ( Exception e ) {
            throw new LifecycleException ( Connector.sm.getString ( "coyoteConnector.protocolHandlerStartFailed" ), e );
        }
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        this.setState ( LifecycleState.STOPPING );
        try {
            if ( this.protocolHandler != null ) {
                this.protocolHandler.stop();
            }
        } catch ( Exception e ) {
            throw new LifecycleException ( Connector.sm.getString ( "coyoteConnector.protocolHandlerStopFailed" ), e );
        }
    }
    @Override
    protected void destroyInternal() throws LifecycleException {
        try {
            if ( this.protocolHandler != null ) {
                this.protocolHandler.destroy();
            }
        } catch ( Exception e ) {
            throw new LifecycleException ( Connector.sm.getString ( "coyoteConnector.protocolHandlerDestroyFailed" ), e );
        }
        if ( this.getService() != null ) {
            this.getService().removeConnector ( this );
        }
        super.destroyInternal();
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "Connector[" );
        sb.append ( this.getProtocol() );
        sb.append ( '-' );
        final int port = this.getPort();
        if ( port > 0 ) {
            sb.append ( port );
        } else {
            sb.append ( "auto-" );
            sb.append ( this.getProperty ( "nameIndex" ) );
        }
        sb.append ( ']' );
        return sb.toString();
    }
    @Override
    protected String getDomainInternal() {
        final Service s = this.getService();
        if ( s == null ) {
            return null;
        }
        return this.service.getDomain();
    }
    @Override
    protected String getObjectNameKeyProperties() {
        return this.createObjectNameKeyProperties ( "Connector" );
    }
    static {
        log = LogFactory.getLog ( Connector.class );
        RECYCLE_FACADES = Boolean.parseBoolean ( System.getProperty ( "org.apache.catalina.connector.RECYCLE_FACADES", "false" ) );
        sm = StringManager.getManager ( Connector.class );
    }
}
