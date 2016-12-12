package org.apache.tomcat.websocket;
import javax.websocket.CloseReason;
import java.io.InputStream;
import javax.net.ssl.TrustManager;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.io.File;
import javax.net.ssl.SSLContext;
import java.util.Locale;
import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap;
import org.apache.tomcat.util.codec.binary.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.nio.charset.StandardCharsets;
import javax.websocket.HandshakeResponse;
import javax.net.ssl.SSLEngine;
import java.util.concurrent.Future;
import java.util.Iterator;
import java.net.SocketAddress;
import javax.websocket.EndpointConfig;
import java.security.Principal;
import java.util.Collections;
import javax.net.ssl.SSLException;
import java.io.EOFException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import javax.websocket.Extension;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.List;
import javax.websocket.ClientEndpointConfig;
import org.apache.tomcat.websocket.pojo.PojoEndpointClient;
import javax.websocket.Decoder;
import java.util.Arrays;
import javax.websocket.DeploymentException;
import javax.websocket.ClientEndpoint;
import javax.websocket.Session;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.InstanceManager;
import java.util.Set;
import javax.websocket.Endpoint;
import java.util.Map;
import org.apache.juli.logging.Log;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.Random;
import org.apache.tomcat.util.res.StringManager;
import javax.websocket.WebSocketContainer;
public class WsWebSocketContainer implements WebSocketContainer, BackgroundProcess {
    private static final StringManager sm;
    private static final Random random;
    private static final byte[] crlf;
    private static final byte[] GET_BYTES;
    private static final byte[] ROOT_URI_BYTES;
    private static final byte[] HTTP_VERSION_BYTES;
    private volatile AsynchronousChannelGroup asynchronousChannelGroup;
    private final Object asynchronousChannelGroupLock;
    private final Log log;
    private final Map<Endpoint, Set<WsSession>> endpointSessionMap;
    private final Map<WsSession, WsSession> sessions;
    private final Object endPointSessionMapLock;
    private long defaultAsyncTimeout;
    private int maxBinaryMessageBufferSize;
    private int maxTextMessageBufferSize;
    private volatile long defaultMaxSessionIdleTimeout;
    private int backgroundProcessCount;
    private int processPeriod;
    private InstanceManager instanceManager;
    public WsWebSocketContainer() {
        this.asynchronousChannelGroup = null;
        this.asynchronousChannelGroupLock = new Object();
        this.log = LogFactory.getLog ( WsWebSocketContainer.class );
        this.endpointSessionMap = new HashMap<Endpoint, Set<WsSession>>();
        this.sessions = new ConcurrentHashMap<WsSession, WsSession>();
        this.endPointSessionMapLock = new Object();
        this.defaultAsyncTimeout = -1L;
        this.maxBinaryMessageBufferSize = Constants.DEFAULT_BUFFER_SIZE;
        this.maxTextMessageBufferSize = Constants.DEFAULT_BUFFER_SIZE;
        this.defaultMaxSessionIdleTimeout = 0L;
        this.backgroundProcessCount = 0;
        this.processPeriod = Constants.DEFAULT_PROCESS_PERIOD;
    }
    InstanceManager getInstanceManager() {
        return this.instanceManager;
    }
    protected void setInstanceManager ( final InstanceManager instanceManager ) {
        this.instanceManager = instanceManager;
    }
    public Session connectToServer ( final Object pojo, final URI path ) throws DeploymentException {
        final ClientEndpoint annotation = pojo.getClass().getAnnotation ( ClientEndpoint.class );
        if ( annotation == null ) {
            throw new DeploymentException ( WsWebSocketContainer.sm.getString ( "wsWebSocketContainer.missingAnnotation", pojo.getClass().getName() ) );
        }
        final Endpoint ep = new PojoEndpointClient ( pojo, Arrays.asList ( ( Class<? extends Decoder>[] ) annotation.decoders() ) );
        final Class<? extends ClientEndpointConfig.Configurator> configuratorClazz = ( Class<? extends ClientEndpointConfig.Configurator> ) annotation.configurator();
        ClientEndpointConfig.Configurator configurator = null;
        if ( !ClientEndpointConfig.Configurator.class.equals ( configuratorClazz ) ) {
            try {
                configurator = ( ClientEndpointConfig.Configurator ) configuratorClazz.newInstance();
            } catch ( InstantiationException | IllegalAccessException e ) {
                throw new DeploymentException ( WsWebSocketContainer.sm.getString ( "wsWebSocketContainer.defaultConfiguratorFail" ), ( Throwable ) e );
            }
        }
        final ClientEndpointConfig.Builder builder = ClientEndpointConfig.Builder.create();
        if ( configurator != null ) {
            builder.configurator ( configurator );
        }
        final ClientEndpointConfig config = builder.decoders ( ( List ) Arrays.asList ( ( Class[] ) annotation.decoders() ) ).encoders ( ( List ) Arrays.asList ( ( Class[] ) annotation.encoders() ) ).preferredSubprotocols ( ( List ) Arrays.asList ( annotation.subprotocols() ) ).build();
        return this.connectToServer ( ep, config, path );
    }
    public Session connectToServer ( final Class<?> annotatedEndpointClass, final URI path ) throws DeploymentException {
        Object pojo;
        try {
            pojo = annotatedEndpointClass.newInstance();
        } catch ( InstantiationException | IllegalAccessException e ) {
            throw new DeploymentException ( WsWebSocketContainer.sm.getString ( "wsWebSocketContainer.endpointCreateFail", annotatedEndpointClass.getName() ), ( Throwable ) e );
        }
        return this.connectToServer ( pojo, path );
    }
    public Session connectToServer ( final Class<? extends Endpoint> clazz, final ClientEndpointConfig clientEndpointConfiguration, final URI path ) throws DeploymentException {
        Endpoint endpoint;
        try {
            endpoint = ( Endpoint ) clazz.newInstance();
        } catch ( InstantiationException | IllegalAccessException e ) {
            throw new DeploymentException ( WsWebSocketContainer.sm.getString ( "wsWebSocketContainer.endpointCreateFail", clazz.getName() ), ( Throwable ) e );
        }
        return this.connectToServer ( endpoint, clientEndpointConfiguration, path );
    }
    public Session connectToServer ( final Endpoint endpoint, final ClientEndpointConfig clientEndpointConfiguration, final URI path ) throws DeploymentException {
        boolean secure = false;
        ByteBuffer proxyConnect = null;
        final String scheme = path.getScheme();
        URI proxyPath;
        if ( "ws".equalsIgnoreCase ( scheme ) ) {
            proxyPath = URI.create ( "http" + path.toString().substring ( 2 ) );
        } else {
            if ( !"wss".equalsIgnoreCase ( scheme ) ) {
                throw new DeploymentException ( WsWebSocketContainer.sm.getString ( "wsWebSocketContainer.pathWrongScheme", scheme ) );
            }
            proxyPath = URI.create ( "https" + path.toString().substring ( 3 ) );
            secure = true;
        }
        final String host = path.getHost();
        if ( host == null ) {
            throw new DeploymentException ( WsWebSocketContainer.sm.getString ( "wsWebSocketContainer.pathNoHost" ) );
        }
        final int port = path.getPort();
        SocketAddress sa = null;
        final List<Proxy> proxies = ProxySelector.getDefault().select ( proxyPath );
        Proxy selectedProxy = null;
        for ( final Proxy proxy : proxies ) {
            if ( proxy.type().equals ( Proxy.Type.HTTP ) ) {
                sa = proxy.address();
                if ( sa instanceof InetSocketAddress ) {
                    final InetSocketAddress inet = ( InetSocketAddress ) sa;
                    if ( inet.isUnresolved() ) {
                        sa = new InetSocketAddress ( inet.getHostName(), inet.getPort() );
                    }
                }
                selectedProxy = proxy;
                break;
            }
        }
        if ( sa == null ) {
            if ( port == -1 ) {
                if ( "ws".equalsIgnoreCase ( scheme ) ) {
                    sa = new InetSocketAddress ( host, 80 );
                } else {
                    sa = new InetSocketAddress ( host, 443 );
                }
            } else {
                sa = new InetSocketAddress ( host, port );
            }
        } else {
            proxyConnect = createProxyRequest ( host, port );
        }
        final Map<String, List<String>> reqHeaders = createRequestHeaders ( host, port, clientEndpointConfiguration.getPreferredSubprotocols(), clientEndpointConfiguration.getExtensions() );
        clientEndpointConfiguration.getConfigurator().beforeRequest ( ( Map ) reqHeaders );
        if ( Constants.DEFAULT_ORIGIN_HEADER_VALUE != null && !reqHeaders.containsKey ( "Origin" ) ) {
            final List<String> originValues = new ArrayList<String> ( 1 );
            originValues.add ( Constants.DEFAULT_ORIGIN_HEADER_VALUE );
            reqHeaders.put ( "Origin", originValues );
        }
        final ByteBuffer request = createRequest ( path, reqHeaders );
        AsynchronousSocketChannel socketChannel;
        try {
            socketChannel = AsynchronousSocketChannel.open ( this.getAsynchronousChannelGroup() );
        } catch ( IOException ioe ) {
            throw new DeploymentException ( WsWebSocketContainer.sm.getString ( "wsWebSocketContainer.asynchronousSocketChannelFail" ), ( Throwable ) ioe );
        }
        long timeout = 5000L;
        final String timeoutValue = clientEndpointConfiguration.getUserProperties().get ( "org.apache.tomcat.websocket.IO_TIMEOUT_MS" );
        if ( timeoutValue != null ) {
            timeout = ( int ) ( Object ) Long.valueOf ( timeoutValue );
        }
        final ByteBuffer response = ByteBuffer.allocate ( this.maxBinaryMessageBufferSize );
        boolean success = false;
        final List<Extension> extensionsAgreed = new ArrayList<Extension>();
        Transformation transformation = null;
        final Future<Void> fConnect = socketChannel.connect ( sa );
        AsyncChannelWrapper channel = null;
        if ( proxyConnect != null ) {
            try {
                fConnect.get ( timeout, TimeUnit.MILLISECONDS );
                channel = new AsyncChannelWrapperNonSecure ( socketChannel );
                writeRequest ( channel, proxyConnect, timeout );
                final HttpResponse httpResponse = this.processResponse ( response, channel, timeout );
                if ( httpResponse.getStatus() != 200 ) {
                    throw new DeploymentException ( WsWebSocketContainer.sm.getString ( "wsWebSocketContainer.proxyConnectFail", selectedProxy, Integer.toString ( httpResponse.getStatus() ) ) );
                }
            } catch ( TimeoutException | InterruptedException | ExecutionException | EOFException e ) {
                if ( channel != null ) {
                    channel.close();
                }
                throw new DeploymentException ( WsWebSocketContainer.sm.getString ( "wsWebSocketContainer.httpRequestFailed" ), ( Throwable ) e );
            }
        }
        if ( secure ) {
            final SSLEngine sslEngine = this.createSSLEngine ( clientEndpointConfiguration.getUserProperties() );
            channel = new AsyncChannelWrapperSecure ( socketChannel, sslEngine );
        } else if ( channel == null ) {
            channel = new AsyncChannelWrapperNonSecure ( socketChannel );
        }
        String subProtocol = null;
        try {
            fConnect.get ( timeout, TimeUnit.MILLISECONDS );
            final Future<Void> fHandshake = channel.handshake();
            fHandshake.get ( timeout, TimeUnit.MILLISECONDS );
            writeRequest ( channel, request, timeout );
            final HttpResponse httpResponse2 = this.processResponse ( response, channel, timeout );
            if ( httpResponse2.status != 101 ) {
                throw new DeploymentException ( WsWebSocketContainer.sm.getString ( "wsWebSocketContainer.invalidStatus", Integer.toString ( httpResponse2.status ) ) );
            }
            final HandshakeResponse handshakeResponse = httpResponse2.getHandshakeResponse();
            clientEndpointConfiguration.getConfigurator().afterResponse ( handshakeResponse );
            final List<String> protocolHeaders = handshakeResponse.getHeaders().get ( "Sec-WebSocket-Protocol" );
            if ( protocolHeaders == null || protocolHeaders.size() == 0 ) {
                subProtocol = null;
            } else {
                if ( protocolHeaders.size() != 1 ) {
                    throw new DeploymentException ( WsWebSocketContainer.sm.getString ( "wsWebSocketContainer.invalidSubProtocol" ) );
                }
                subProtocol = protocolHeaders.get ( 0 );
            }
            final List<String> extHeaders = handshakeResponse.getHeaders().get ( "Sec-WebSocket-Extensions" );
            if ( extHeaders != null ) {
                for ( final String extHeader : extHeaders ) {
                    Util.parseExtensionHeader ( extensionsAgreed, extHeader );
                }
            }
            final TransformationFactory factory = TransformationFactory.getInstance();
            for ( final Extension extension : extensionsAgreed ) {
                final List<List<Extension.Parameter>> wrapper = new ArrayList<List<Extension.Parameter>> ( 1 );
                wrapper.add ( extension.getParameters() );
                final Transformation t = factory.create ( extension.getName(), wrapper, false );
                if ( t == null ) {
                    throw new DeploymentException ( WsWebSocketContainer.sm.getString ( "wsWebSocketContainer.invalidExtensionParameters" ) );
                }
                if ( transformation == null ) {
                    transformation = t;
                } else {
                    transformation.setNext ( t );
                }
            }
            success = true;
        } catch ( ExecutionException ) {}
        catch ( InterruptedException ) {}
        catch ( SSLException ) {}
        catch ( EOFException ) {}
        catch ( TimeoutException e ) {
            throw new DeploymentException ( WsWebSocketContainer.sm.getString ( "wsWebSocketContainer.httpRequestFailed" ), ( Throwable ) e );
        } finally {
            if ( !success ) {
                channel.close();
            }
        }
        final WsRemoteEndpointImplClient wsRemoteEndpointClient = new WsRemoteEndpointImplClient ( channel );
        final WsSession wsSession = new WsSession ( endpoint, wsRemoteEndpointClient, this, null, null, null, null, null, extensionsAgreed, subProtocol, Collections.emptyMap(), secure, ( EndpointConfig ) clientEndpointConfiguration );
        final WsFrameClient wsFrameClient = new WsFrameClient ( response, channel, wsSession, transformation );
        wsRemoteEndpointClient.setTransformation ( wsFrameClient.getTransformation() );
        endpoint.onOpen ( ( Session ) wsSession, ( EndpointConfig ) clientEndpointConfiguration );
        this.registerSession ( endpoint, wsSession );
        wsFrameClient.startInputProcessing();
        return ( Session ) wsSession;
    }
    private static void writeRequest ( final AsyncChannelWrapper channel, final ByteBuffer request, final long timeout ) throws TimeoutException, InterruptedException, ExecutionException {
        int toWrite = request.limit();
        Future<Integer> fWrite = channel.write ( request );
        Integer thisWrite;
        for ( thisWrite = fWrite.get ( timeout, TimeUnit.MILLISECONDS ), toWrite -= thisWrite; toWrite > 0; toWrite -= thisWrite ) {
            fWrite = channel.write ( request );
            thisWrite = fWrite.get ( timeout, TimeUnit.MILLISECONDS );
        }
    }
    private static ByteBuffer createProxyRequest ( final String host, final int port ) {
        final StringBuilder request = new StringBuilder();
        request.append ( "CONNECT " );
        request.append ( host );
        if ( port != -1 ) {
            request.append ( ':' );
            request.append ( port );
        }
        request.append ( " HTTP/1.1\r\nProxy-Connection: keep-alive\r\nConnection: keepalive\r\nHost: " );
        request.append ( host );
        if ( port != -1 ) {
            request.append ( ':' );
            request.append ( port );
        }
        request.append ( "\r\n\r\n" );
        final byte[] bytes = request.toString().getBytes ( StandardCharsets.ISO_8859_1 );
        return ByteBuffer.wrap ( bytes );
    }
    protected void registerSession ( final Endpoint endpoint, final WsSession wsSession ) {
        if ( !wsSession.isOpen() ) {
            return;
        }
        synchronized ( this.endPointSessionMapLock ) {
            if ( this.endpointSessionMap.size() == 0 ) {
                BackgroundProcessManager.getInstance().register ( this );
            }
            Set<WsSession> wsSessions = this.endpointSessionMap.get ( endpoint );
            if ( wsSessions == null ) {
                wsSessions = new HashSet<WsSession>();
                this.endpointSessionMap.put ( endpoint, wsSessions );
            }
            wsSessions.add ( wsSession );
        }
        this.sessions.put ( wsSession, wsSession );
    }
    protected void unregisterSession ( final Endpoint endpoint, final WsSession wsSession ) {
        synchronized ( this.endPointSessionMapLock ) {
            final Set<WsSession> wsSessions = this.endpointSessionMap.get ( endpoint );
            if ( wsSessions != null ) {
                wsSessions.remove ( wsSession );
                if ( wsSessions.size() == 0 ) {
                    this.endpointSessionMap.remove ( endpoint );
                }
            }
            if ( this.endpointSessionMap.size() == 0 ) {
                BackgroundProcessManager.getInstance().unregister ( this );
            }
        }
        this.sessions.remove ( wsSession );
    }
    Set<Session> getOpenSessions ( final Endpoint endpoint ) {
        final HashSet<Session> result = new HashSet<Session>();
        synchronized ( this.endPointSessionMapLock ) {
            final Set<WsSession> sessions = this.endpointSessionMap.get ( endpoint );
            if ( sessions != null ) {
                result.addAll ( ( Collection<?> ) sessions );
            }
        }
        return result;
    }
    private static Map<String, List<String>> createRequestHeaders ( final String host, final int port, final List<String> subProtocols, final List<Extension> extensions ) {
        final Map<String, List<String>> headers = new HashMap<String, List<String>>();
        final List<String> hostValues = new ArrayList<String> ( 1 );
        if ( port == -1 ) {
            hostValues.add ( host );
        } else {
            hostValues.add ( host + ':' + port );
        }
        headers.put ( "Host", hostValues );
        final List<String> upgradeValues = new ArrayList<String> ( 1 );
        upgradeValues.add ( "websocket" );
        headers.put ( "Upgrade", upgradeValues );
        final List<String> connectionValues = new ArrayList<String> ( 1 );
        connectionValues.add ( "upgrade" );
        headers.put ( "Connection", connectionValues );
        final List<String> wsVersionValues = new ArrayList<String> ( 1 );
        wsVersionValues.add ( "13" );
        headers.put ( "Sec-WebSocket-Version", wsVersionValues );
        final List<String> wsKeyValues = new ArrayList<String> ( 1 );
        wsKeyValues.add ( generateWsKeyValue() );
        headers.put ( "Sec-WebSocket-Key", wsKeyValues );
        if ( subProtocols != null && subProtocols.size() > 0 ) {
            headers.put ( "Sec-WebSocket-Protocol", subProtocols );
        }
        if ( extensions != null && extensions.size() > 0 ) {
            headers.put ( "Sec-WebSocket-Extensions", generateExtensionHeaders ( extensions ) );
        }
        return headers;
    }
    private static List<String> generateExtensionHeaders ( final List<Extension> extensions ) {
        final List<String> result = new ArrayList<String> ( extensions.size() );
        for ( final Extension extension : extensions ) {
            final StringBuilder header = new StringBuilder();
            header.append ( extension.getName() );
            for ( final Extension.Parameter param : extension.getParameters() ) {
                header.append ( ';' );
                header.append ( param.getName() );
                final String value = param.getValue();
                if ( value != null && value.length() > 0 ) {
                    header.append ( '=' );
                    header.append ( value );
                }
            }
            result.add ( header.toString() );
        }
        return result;
    }
    private static String generateWsKeyValue() {
        final byte[] keyBytes = new byte[16];
        WsWebSocketContainer.random.nextBytes ( keyBytes );
        return Base64.encodeBase64String ( keyBytes );
    }
    private static ByteBuffer createRequest ( final URI uri, final Map<String, List<String>> reqHeaders ) {
        final ByteBuffer result = ByteBuffer.allocate ( 4096 );
        result.put ( WsWebSocketContainer.GET_BYTES );
        if ( null == uri.getPath() || "".equals ( uri.getPath() ) ) {
            result.put ( WsWebSocketContainer.ROOT_URI_BYTES );
        } else {
            result.put ( uri.getRawPath().getBytes ( StandardCharsets.ISO_8859_1 ) );
        }
        final String query = uri.getRawQuery();
        if ( query != null ) {
            result.put ( ( byte ) 63 );
            result.put ( query.getBytes ( StandardCharsets.ISO_8859_1 ) );
        }
        result.put ( WsWebSocketContainer.HTTP_VERSION_BYTES );
        for ( final Map.Entry<String, List<String>> entry : reqHeaders.entrySet() ) {
            addHeader ( result, entry.getKey(), entry.getValue() );
        }
        result.put ( WsWebSocketContainer.crlf );
        result.flip();
        return result;
    }
    private static void addHeader ( final ByteBuffer result, final String key, final List<String> values ) {
        final StringBuilder sb = new StringBuilder();
        final Iterator<String> iter = values.iterator();
        if ( !iter.hasNext() ) {
            return;
        }
        sb.append ( iter.next() );
        while ( iter.hasNext() ) {
            sb.append ( ',' );
            sb.append ( iter.next() );
        }
        result.put ( key.getBytes ( StandardCharsets.ISO_8859_1 ) );
        result.put ( ": ".getBytes ( StandardCharsets.ISO_8859_1 ) );
        result.put ( sb.toString().getBytes ( StandardCharsets.ISO_8859_1 ) );
        result.put ( WsWebSocketContainer.crlf );
    }
    private HttpResponse processResponse ( final ByteBuffer response, final AsyncChannelWrapper channel, final long timeout ) throws InterruptedException, ExecutionException, DeploymentException, EOFException, TimeoutException {
        final Map<String, List<String>> headers = ( Map<String, List<String>> ) new CaseInsensitiveKeyMap();
        int status = 0;
        boolean readStatus = false;
        boolean readHeaders = false;
        String line = null;
        while ( !readHeaders ) {
            response.clear();
            final Future<Integer> read = channel.read ( response );
            final Integer bytesRead = read.get ( timeout, TimeUnit.MILLISECONDS );
            if ( bytesRead == -1 ) {
                throw new EOFException();
            }
            response.flip();
            while ( response.hasRemaining() && !readHeaders ) {
                if ( line == null ) {
                    line = this.readLine ( response );
                } else {
                    line += this.readLine ( response );
                }
                if ( "\r\n".equals ( line ) ) {
                    readHeaders = true;
                } else {
                    if ( !line.endsWith ( "\r\n" ) ) {
                        continue;
                    }
                    if ( readStatus ) {
                        this.parseHeaders ( line, headers );
                    } else {
                        status = this.parseStatus ( line );
                        readStatus = true;
                    }
                    line = null;
                }
            }
        }
        return new HttpResponse ( status, ( HandshakeResponse ) new WsHandshakeResponse ( headers ) );
    }
    private int parseStatus ( final String line ) throws DeploymentException {
        final String[] parts = line.trim().split ( " " );
        if ( parts.length < 2 || ( !"HTTP/1.0".equals ( parts[0] ) && !"HTTP/1.1".equals ( parts[0] ) ) ) {
            throw new DeploymentException ( WsWebSocketContainer.sm.getString ( "wsWebSocketContainer.invalidStatus", line ) );
        }
        try {
            return Integer.parseInt ( parts[1] );
        } catch ( NumberFormatException nfe ) {
            throw new DeploymentException ( WsWebSocketContainer.sm.getString ( "wsWebSocketContainer.invalidStatus", line ) );
        }
    }
    private void parseHeaders ( final String line, final Map<String, List<String>> headers ) {
        final int index = line.indexOf ( 58 );
        if ( index == -1 ) {
            this.log.warn ( WsWebSocketContainer.sm.getString ( "wsWebSocketContainer.invalidHeader", line ) );
            return;
        }
        final String headerName = line.substring ( 0, index ).trim().toLowerCase ( Locale.ENGLISH );
        final String headerValue = line.substring ( index + 1 ).trim();
        List<String> values = headers.get ( headerName );
        if ( values == null ) {
            values = new ArrayList<String> ( 1 );
            headers.put ( headerName, values );
        }
        values.add ( headerValue );
    }
    private String readLine ( final ByteBuffer response ) {
        final StringBuilder sb = new StringBuilder();
        char c = '\0';
        while ( response.hasRemaining() ) {
            c = ( char ) response.get();
            sb.append ( c );
            if ( c == '\n' ) {
                break;
            }
        }
        return sb.toString();
    }
    private SSLEngine createSSLEngine ( final Map<String, Object> userProperties ) throws DeploymentException {
        try {
            SSLContext sslContext = userProperties.get ( "org.apache.tomcat.websocket.SSL_CONTEXT" );
            if ( sslContext == null ) {
                sslContext = SSLContext.getInstance ( "TLS" );
                final String sslTrustStoreValue = userProperties.get ( "org.apache.tomcat.websocket.SSL_TRUSTSTORE" );
                if ( sslTrustStoreValue != null ) {
                    String sslTrustStorePwdValue = userProperties.get ( "org.apache.tomcat.websocket.SSL_TRUSTSTORE_PWD" );
                    if ( sslTrustStorePwdValue == null ) {
                        sslTrustStorePwdValue = "changeit";
                    }
                    final File keyStoreFile = new File ( sslTrustStoreValue );
                    final KeyStore ks = KeyStore.getInstance ( "JKS" );
                    try ( final InputStream is = new FileInputStream ( keyStoreFile ) ) {
                        ks.load ( is, sslTrustStorePwdValue.toCharArray() );
                    }
                    final TrustManagerFactory tmf = TrustManagerFactory.getInstance ( TrustManagerFactory.getDefaultAlgorithm() );
                    tmf.init ( ks );
                    sslContext.init ( null, tmf.getTrustManagers(), null );
                } else {
                    sslContext.init ( null, null, null );
                }
            }
            final SSLEngine engine = sslContext.createSSLEngine();
            final String sslProtocolsValue = userProperties.get ( "org.apache.tomcat.websocket.SSL_PROTOCOLS" );
            if ( sslProtocolsValue != null ) {
                engine.setEnabledProtocols ( sslProtocolsValue.split ( "," ) );
            }
            engine.setUseClientMode ( true );
            return engine;
        } catch ( Exception e ) {
            throw new DeploymentException ( WsWebSocketContainer.sm.getString ( "wsWebSocketContainer.sslEngineFail" ), ( Throwable ) e );
        }
    }
    public long getDefaultMaxSessionIdleTimeout() {
        return this.defaultMaxSessionIdleTimeout;
    }
    public void setDefaultMaxSessionIdleTimeout ( final long timeout ) {
        this.defaultMaxSessionIdleTimeout = timeout;
    }
    public int getDefaultMaxBinaryMessageBufferSize() {
        return this.maxBinaryMessageBufferSize;
    }
    public void setDefaultMaxBinaryMessageBufferSize ( final int max ) {
        this.maxBinaryMessageBufferSize = max;
    }
    public int getDefaultMaxTextMessageBufferSize() {
        return this.maxTextMessageBufferSize;
    }
    public void setDefaultMaxTextMessageBufferSize ( final int max ) {
        this.maxTextMessageBufferSize = max;
    }
    public Set<Extension> getInstalledExtensions() {
        return Collections.emptySet();
    }
    public long getDefaultAsyncSendTimeout() {
        return this.defaultAsyncTimeout;
    }
    public void setAsyncSendTimeout ( final long timeout ) {
        this.defaultAsyncTimeout = timeout;
    }
    public void destroy() {
        final CloseReason cr = new CloseReason ( ( CloseReason.CloseCode ) CloseReason.CloseCodes.GOING_AWAY, WsWebSocketContainer.sm.getString ( "wsWebSocketContainer.shutdown" ) );
        for ( final WsSession session : this.sessions.keySet() ) {
            try {
                session.close ( cr );
            } catch ( IOException ioe ) {
                this.log.debug ( WsWebSocketContainer.sm.getString ( "wsWebSocketContainer.sessionCloseFail", session.getId() ), ioe );
            }
        }
        if ( this.asynchronousChannelGroup != null ) {
            synchronized ( this.asynchronousChannelGroupLock ) {
                if ( this.asynchronousChannelGroup != null ) {
                    AsyncChannelGroupUtil.unregister();
                    this.asynchronousChannelGroup = null;
                }
            }
        }
    }
    private AsynchronousChannelGroup getAsynchronousChannelGroup() {
        AsynchronousChannelGroup result = this.asynchronousChannelGroup;
        if ( result == null ) {
            synchronized ( this.asynchronousChannelGroupLock ) {
                if ( this.asynchronousChannelGroup == null ) {
                    this.asynchronousChannelGroup = AsyncChannelGroupUtil.register();
                }
                result = this.asynchronousChannelGroup;
            }
        }
        return result;
    }
    public void backgroundProcess() {
        ++this.backgroundProcessCount;
        if ( this.backgroundProcessCount >= this.processPeriod ) {
            this.backgroundProcessCount = 0;
            for ( final WsSession wsSession : this.sessions.keySet() ) {
                wsSession.checkExpiration();
            }
        }
    }
    public void setProcessPeriod ( final int period ) {
        this.processPeriod = period;
    }
    public int getProcessPeriod() {
        return this.processPeriod;
    }
    static {
        sm = StringManager.getManager ( WsWebSocketContainer.class );
        random = new Random();
        crlf = new byte[] { 13, 10 };
        GET_BYTES = "GET ".getBytes ( StandardCharsets.ISO_8859_1 );
        ROOT_URI_BYTES = "/".getBytes ( StandardCharsets.ISO_8859_1 );
        HTTP_VERSION_BYTES = " HTTP/1.1\r\n".getBytes ( StandardCharsets.ISO_8859_1 );
    }
    private static class HttpResponse {
        private final int status;
        private final HandshakeResponse handshakeResponse;
        public HttpResponse ( final int status, final HandshakeResponse handshakeResponse ) {
            this.status = status;
            this.handshakeResponse = handshakeResponse;
        }
        public int getStatus() {
            return this.status;
        }
        public HandshakeResponse getHandshakeResponse() {
            return this.handshakeResponse;
        }
    }
}
