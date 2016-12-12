package org.apache.tomcat.websocket;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import javax.websocket.ClientEndpoint;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.Extension;
import javax.websocket.HandshakeResponse;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.websocket.pojo.PojoEndpointClient;
public class WsWebSocketContainer implements WebSocketContainer, BackgroundProcess {
    private static final StringManager sm = StringManager.getManager ( WsWebSocketContainer.class );
    private static final Random random = new Random();
    private static final byte[] crlf = new byte[] {13, 10};
    private static final byte[] GET_BYTES = "GET ".getBytes ( StandardCharsets.ISO_8859_1 );
    private static final byte[] ROOT_URI_BYTES = "/".getBytes ( StandardCharsets.ISO_8859_1 );
    private static final byte[] HTTP_VERSION_BYTES =
        " HTTP/1.1\r\n".getBytes ( StandardCharsets.ISO_8859_1 );
    private volatile AsynchronousChannelGroup asynchronousChannelGroup = null;
    private final Object asynchronousChannelGroupLock = new Object();
    private final Log log = LogFactory.getLog ( WsWebSocketContainer.class );
    private final Map<Endpoint, Set<WsSession>> endpointSessionMap =
        new HashMap<>();
    private final Map<WsSession, WsSession> sessions = new ConcurrentHashMap<>();
    private final Object endPointSessionMapLock = new Object();
    private long defaultAsyncTimeout = -1;
    private int maxBinaryMessageBufferSize = Constants.DEFAULT_BUFFER_SIZE;
    private int maxTextMessageBufferSize = Constants.DEFAULT_BUFFER_SIZE;
    private volatile long defaultMaxSessionIdleTimeout = 0;
    private int backgroundProcessCount = 0;
    private int processPeriod = Constants.DEFAULT_PROCESS_PERIOD;
    private InstanceManager instanceManager;
    InstanceManager getInstanceManager() {
        return instanceManager;
    }
    protected void setInstanceManager ( InstanceManager instanceManager ) {
        this.instanceManager = instanceManager;
    }
    @Override
    public Session connectToServer ( Object pojo, URI path )
    throws DeploymentException {
        ClientEndpoint annotation =
            pojo.getClass().getAnnotation ( ClientEndpoint.class );
        if ( annotation == null ) {
            throw new DeploymentException (
                sm.getString ( "wsWebSocketContainer.missingAnnotation",
                               pojo.getClass().getName() ) );
        }
        Endpoint ep = new PojoEndpointClient ( pojo, Arrays.asList ( annotation.decoders() ) );
        Class<? extends ClientEndpointConfig.Configurator> configuratorClazz =
            annotation.configurator();
        ClientEndpointConfig.Configurator configurator = null;
        if ( !ClientEndpointConfig.Configurator.class.equals (
                    configuratorClazz ) ) {
            try {
                configurator = configuratorClazz.newInstance();
            } catch ( InstantiationException | IllegalAccessException e ) {
                throw new DeploymentException ( sm.getString (
                                                    "wsWebSocketContainer.defaultConfiguratorFail" ), e );
            }
        }
        ClientEndpointConfig.Builder builder = ClientEndpointConfig.Builder.create();
        if ( configurator != null ) {
            builder.configurator ( configurator );
        }
        ClientEndpointConfig config = builder.
                                      decoders ( Arrays.asList ( annotation.decoders() ) ).
                                      encoders ( Arrays.asList ( annotation.encoders() ) ).
                                      preferredSubprotocols ( Arrays.asList ( annotation.subprotocols() ) ).
                                      build();
        return connectToServer ( ep, config, path );
    }
    @Override
    public Session connectToServer ( Class<?> annotatedEndpointClass, URI path )
    throws DeploymentException {
        Object pojo;
        try {
            pojo = annotatedEndpointClass.newInstance();
        } catch ( InstantiationException | IllegalAccessException e ) {
            throw new DeploymentException ( sm.getString (
                                                "wsWebSocketContainer.endpointCreateFail",
                                                annotatedEndpointClass.getName() ), e );
        }
        return connectToServer ( pojo, path );
    }
    @Override
    public Session connectToServer ( Class<? extends Endpoint> clazz,
                                     ClientEndpointConfig clientEndpointConfiguration, URI path )
    throws DeploymentException {
        Endpoint endpoint;
        try {
            endpoint = clazz.newInstance();
        } catch ( InstantiationException | IllegalAccessException e ) {
            throw new DeploymentException ( sm.getString (
                                                "wsWebSocketContainer.endpointCreateFail", clazz.getName() ),
                                            e );
        }
        return connectToServer ( endpoint, clientEndpointConfiguration, path );
    }
    @SuppressWarnings ( "resource" )
    @Override
    public Session connectToServer ( Endpoint endpoint,
                                     ClientEndpointConfig clientEndpointConfiguration, URI path )
    throws DeploymentException {
        boolean secure = false;
        ByteBuffer proxyConnect = null;
        URI proxyPath;
        String scheme = path.getScheme();
        if ( "ws".equalsIgnoreCase ( scheme ) ) {
            proxyPath = URI.create ( "http" + path.toString().substring ( 2 ) );
        } else if ( "wss".equalsIgnoreCase ( scheme ) ) {
            proxyPath = URI.create ( "https" + path.toString().substring ( 3 ) );
            secure = true;
        } else {
            throw new DeploymentException ( sm.getString (
                                                "wsWebSocketContainer.pathWrongScheme", scheme ) );
        }
        String host = path.getHost();
        if ( host == null ) {
            throw new DeploymentException (
                sm.getString ( "wsWebSocketContainer.pathNoHost" ) );
        }
        int port = path.getPort();
        SocketAddress sa = null;
        List<Proxy> proxies = ProxySelector.getDefault().select ( proxyPath );
        Proxy selectedProxy = null;
        for ( Proxy proxy : proxies ) {
            if ( proxy.type().equals ( Proxy.Type.HTTP ) ) {
                sa = proxy.address();
                if ( sa instanceof InetSocketAddress ) {
                    InetSocketAddress inet = ( InetSocketAddress ) sa;
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
        Map<String, List<String>> reqHeaders = createRequestHeaders ( host, port,
                                               clientEndpointConfiguration.getPreferredSubprotocols(),
                                               clientEndpointConfiguration.getExtensions() );
        clientEndpointConfiguration.getConfigurator().
        beforeRequest ( reqHeaders );
        if ( Constants.DEFAULT_ORIGIN_HEADER_VALUE != null &&
                !reqHeaders.containsKey ( Constants.ORIGIN_HEADER_NAME ) ) {
            List<String> originValues = new ArrayList<> ( 1 );
            originValues.add ( Constants.DEFAULT_ORIGIN_HEADER_VALUE );
            reqHeaders.put ( Constants.ORIGIN_HEADER_NAME, originValues );
        }
        ByteBuffer request = createRequest ( path, reqHeaders );
        AsynchronousSocketChannel socketChannel;
        try {
            socketChannel = AsynchronousSocketChannel.open ( getAsynchronousChannelGroup() );
        } catch ( IOException ioe ) {
            throw new DeploymentException ( sm.getString (
                                                "wsWebSocketContainer.asynchronousSocketChannelFail" ), ioe );
        }
        long timeout = Constants.IO_TIMEOUT_MS_DEFAULT;
        String timeoutValue = ( String ) clientEndpointConfiguration.getUserProperties().get (
                                  Constants.IO_TIMEOUT_MS_PROPERTY );
        if ( timeoutValue != null ) {
            timeout = Long.valueOf ( timeoutValue ).intValue();
        }
        ByteBuffer response = ByteBuffer.allocate ( maxBinaryMessageBufferSize );
        String subProtocol;
        boolean success = false;
        List<Extension> extensionsAgreed = new ArrayList<>();
        Transformation transformation = null;
        Future<Void> fConnect = socketChannel.connect ( sa );
        AsyncChannelWrapper channel = null;
        if ( proxyConnect != null ) {
            try {
                fConnect.get ( timeout, TimeUnit.MILLISECONDS );
                channel = new AsyncChannelWrapperNonSecure ( socketChannel );
                writeRequest ( channel, proxyConnect, timeout );
                HttpResponse httpResponse = processResponse ( response, channel, timeout );
                if ( httpResponse.getStatus() != 200 ) {
                    throw new DeploymentException ( sm.getString (
                                                        "wsWebSocketContainer.proxyConnectFail", selectedProxy,
                                                        Integer.toString ( httpResponse.getStatus() ) ) );
                }
            } catch ( TimeoutException | InterruptedException | ExecutionException |
                          EOFException e ) {
                if ( channel != null ) {
                    channel.close();
                }
                throw new DeploymentException (
                    sm.getString ( "wsWebSocketContainer.httpRequestFailed" ), e );
            }
        }
        if ( secure ) {
            SSLEngine sslEngine = createSSLEngine (
                                      clientEndpointConfiguration.getUserProperties() );
            channel = new AsyncChannelWrapperSecure ( socketChannel, sslEngine );
        } else if ( channel == null ) {
            channel = new AsyncChannelWrapperNonSecure ( socketChannel );
        }
        try {
            fConnect.get ( timeout, TimeUnit.MILLISECONDS );
            Future<Void> fHandshake = channel.handshake();
            fHandshake.get ( timeout, TimeUnit.MILLISECONDS );
            writeRequest ( channel, request, timeout );
            HttpResponse httpResponse = processResponse ( response, channel, timeout );
            if ( httpResponse.status != 101 ) {
                throw new DeploymentException ( sm.getString ( "wsWebSocketContainer.invalidStatus",
                                                Integer.toString ( httpResponse.status ) ) );
            }
            HandshakeResponse handshakeResponse = httpResponse.getHandshakeResponse();
            clientEndpointConfiguration.getConfigurator().afterResponse ( handshakeResponse );
            List<String> protocolHeaders = handshakeResponse.getHeaders().get (
                                               Constants.WS_PROTOCOL_HEADER_NAME );
            if ( protocolHeaders == null || protocolHeaders.size() == 0 ) {
                subProtocol = null;
            } else if ( protocolHeaders.size() == 1 ) {
                subProtocol = protocolHeaders.get ( 0 );
            } else {
                throw new DeploymentException (
                    sm.getString ( "wsWebSocketContainer.invalidSubProtocol" ) );
            }
            List<String> extHeaders = handshakeResponse.getHeaders().get (
                                          Constants.WS_EXTENSIONS_HEADER_NAME );
            if ( extHeaders != null ) {
                for ( String extHeader : extHeaders ) {
                    Util.parseExtensionHeader ( extensionsAgreed, extHeader );
                }
            }
            TransformationFactory factory = TransformationFactory.getInstance();
            for ( Extension extension : extensionsAgreed ) {
                List<List<Extension.Parameter>> wrapper = new ArrayList<> ( 1 );
                wrapper.add ( extension.getParameters() );
                Transformation t = factory.create ( extension.getName(), wrapper, false );
                if ( t == null ) {
                    throw new DeploymentException ( sm.getString (
                                                        "wsWebSocketContainer.invalidExtensionParameters" ) );
                }
                if ( transformation == null ) {
                    transformation = t;
                } else {
                    transformation.setNext ( t );
                }
            }
            success = true;
        } catch ( ExecutionException | InterruptedException | SSLException |
                      EOFException | TimeoutException e ) {
            throw new DeploymentException (
                sm.getString ( "wsWebSocketContainer.httpRequestFailed" ), e );
        } finally {
            if ( !success ) {
                channel.close();
            }
        }
        WsRemoteEndpointImplClient wsRemoteEndpointClient = new WsRemoteEndpointImplClient ( channel );
        WsSession wsSession = new WsSession ( endpoint, wsRemoteEndpointClient,
                                              this, null, null, null, null, null, extensionsAgreed,
                                              subProtocol, Collections.<String, String>emptyMap(), secure,
                                              clientEndpointConfiguration );
        WsFrameClient wsFrameClient = new WsFrameClient ( response, channel,
                wsSession, transformation );
        wsRemoteEndpointClient.setTransformation ( wsFrameClient.getTransformation() );
        endpoint.onOpen ( wsSession, clientEndpointConfiguration );
        registerSession ( endpoint, wsSession );
        wsFrameClient.startInputProcessing();
        return wsSession;
    }
    private static void writeRequest ( AsyncChannelWrapper channel, ByteBuffer request,
                                       long timeout ) throws TimeoutException, InterruptedException, ExecutionException {
        int toWrite = request.limit();
        Future<Integer> fWrite = channel.write ( request );
        Integer thisWrite = fWrite.get ( timeout, TimeUnit.MILLISECONDS );
        toWrite -= thisWrite.intValue();
        while ( toWrite > 0 ) {
            fWrite = channel.write ( request );
            thisWrite = fWrite.get ( timeout, TimeUnit.MILLISECONDS );
            toWrite -= thisWrite.intValue();
        }
    }
    private static ByteBuffer createProxyRequest ( String host, int port ) {
        StringBuilder request = new StringBuilder();
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
        byte[] bytes = request.toString().getBytes ( StandardCharsets.ISO_8859_1 );
        return ByteBuffer.wrap ( bytes );
    }
    protected void registerSession ( Endpoint endpoint, WsSession wsSession ) {
        if ( !wsSession.isOpen() ) {
            return;
        }
        synchronized ( endPointSessionMapLock ) {
            if ( endpointSessionMap.size() == 0 ) {
                BackgroundProcessManager.getInstance().register ( this );
            }
            Set<WsSession> wsSessions = endpointSessionMap.get ( endpoint );
            if ( wsSessions == null ) {
                wsSessions = new HashSet<>();
                endpointSessionMap.put ( endpoint, wsSessions );
            }
            wsSessions.add ( wsSession );
        }
        sessions.put ( wsSession, wsSession );
    }
    protected void unregisterSession ( Endpoint endpoint, WsSession wsSession ) {
        synchronized ( endPointSessionMapLock ) {
            Set<WsSession> wsSessions = endpointSessionMap.get ( endpoint );
            if ( wsSessions != null ) {
                wsSessions.remove ( wsSession );
                if ( wsSessions.size() == 0 ) {
                    endpointSessionMap.remove ( endpoint );
                }
            }
            if ( endpointSessionMap.size() == 0 ) {
                BackgroundProcessManager.getInstance().unregister ( this );
            }
        }
        sessions.remove ( wsSession );
    }
    Set<Session> getOpenSessions ( Endpoint endpoint ) {
        HashSet<Session> result = new HashSet<>();
        synchronized ( endPointSessionMapLock ) {
            Set<WsSession> sessions = endpointSessionMap.get ( endpoint );
            if ( sessions != null ) {
                result.addAll ( sessions );
            }
        }
        return result;
    }
    private static Map<String, List<String>> createRequestHeaders ( String host,
            int port, List<String> subProtocols, List<Extension> extensions ) {
        Map<String, List<String>> headers = new HashMap<>();
        List<String> hostValues = new ArrayList<> ( 1 );
        if ( port == -1 ) {
            hostValues.add ( host );
        } else {
            hostValues.add ( host + ':' + port );
        }
        headers.put ( Constants.HOST_HEADER_NAME, hostValues );
        List<String> upgradeValues = new ArrayList<> ( 1 );
        upgradeValues.add ( Constants.UPGRADE_HEADER_VALUE );
        headers.put ( Constants.UPGRADE_HEADER_NAME, upgradeValues );
        List<String> connectionValues = new ArrayList<> ( 1 );
        connectionValues.add ( Constants.CONNECTION_HEADER_VALUE );
        headers.put ( Constants.CONNECTION_HEADER_NAME, connectionValues );
        List<String> wsVersionValues = new ArrayList<> ( 1 );
        wsVersionValues.add ( Constants.WS_VERSION_HEADER_VALUE );
        headers.put ( Constants.WS_VERSION_HEADER_NAME, wsVersionValues );
        List<String> wsKeyValues = new ArrayList<> ( 1 );
        wsKeyValues.add ( generateWsKeyValue() );
        headers.put ( Constants.WS_KEY_HEADER_NAME, wsKeyValues );
        if ( subProtocols != null && subProtocols.size() > 0 ) {
            headers.put ( Constants.WS_PROTOCOL_HEADER_NAME, subProtocols );
        }
        if ( extensions != null && extensions.size() > 0 ) {
            headers.put ( Constants.WS_EXTENSIONS_HEADER_NAME,
                          generateExtensionHeaders ( extensions ) );
        }
        return headers;
    }
    private static List<String> generateExtensionHeaders ( List<Extension> extensions ) {
        List<String> result = new ArrayList<> ( extensions.size() );
        for ( Extension extension : extensions ) {
            StringBuilder header = new StringBuilder();
            header.append ( extension.getName() );
            for ( Extension.Parameter param : extension.getParameters() ) {
                header.append ( ';' );
                header.append ( param.getName() );
                String value = param.getValue();
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
        byte[] keyBytes = new byte[16];
        random.nextBytes ( keyBytes );
        return Base64.encodeBase64String ( keyBytes );
    }
    private static ByteBuffer createRequest ( URI uri, Map<String, List<String>> reqHeaders ) {
        ByteBuffer result = ByteBuffer.allocate ( 4 * 1024 );
        result.put ( GET_BYTES );
        if ( null == uri.getPath() || "".equals ( uri.getPath() ) ) {
            result.put ( ROOT_URI_BYTES );
        } else {
            result.put ( uri.getRawPath().getBytes ( StandardCharsets.ISO_8859_1 ) );
        }
        String query = uri.getRawQuery();
        if ( query != null ) {
            result.put ( ( byte ) '?' );
            result.put ( query.getBytes ( StandardCharsets.ISO_8859_1 ) );
        }
        result.put ( HTTP_VERSION_BYTES );
        Iterator<Entry<String, List<String>>> iter = reqHeaders.entrySet().iterator();
        while ( iter.hasNext() ) {
            Entry<String, List<String>> entry = iter.next();
            addHeader ( result, entry.getKey(), entry.getValue() );
        }
        result.put ( crlf );
        result.flip();
        return result;
    }
    private static void addHeader ( ByteBuffer result, String key, List<String> values ) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = values.iterator();
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
        result.put ( crlf );
    }
    private HttpResponse processResponse ( ByteBuffer response,
                                           AsyncChannelWrapper channel, long timeout ) throws InterruptedException,
                                                                   ExecutionException, DeploymentException, EOFException,
        TimeoutException {
        Map<String, List<String>> headers = new CaseInsensitiveKeyMap<>();
        int status = 0;
        boolean readStatus = false;
        boolean readHeaders = false;
        String line = null;
        while ( !readHeaders ) {
            response.clear();
            Future<Integer> read = channel.read ( response );
            Integer bytesRead = read.get ( timeout, TimeUnit.MILLISECONDS );
            if ( bytesRead.intValue() == -1 ) {
                throw new EOFException();
            }
            response.flip();
            while ( response.hasRemaining() && !readHeaders ) {
                if ( line == null ) {
                    line = readLine ( response );
                } else {
                    line += readLine ( response );
                }
                if ( "\r\n".equals ( line ) ) {
                    readHeaders = true;
                } else if ( line.endsWith ( "\r\n" ) ) {
                    if ( readStatus ) {
                        parseHeaders ( line, headers );
                    } else {
                        status = parseStatus ( line );
                        readStatus = true;
                    }
                    line = null;
                }
            }
        }
        return new HttpResponse ( status, new WsHandshakeResponse ( headers ) );
    }
    private int parseStatus ( String line ) throws DeploymentException {
        String[] parts = line.trim().split ( " " );
        if ( parts.length < 2 || ! ( "HTTP/1.0".equals ( parts[0] ) || "HTTP/1.1".equals ( parts[0] ) ) ) {
            throw new DeploymentException ( sm.getString (
                                                "wsWebSocketContainer.invalidStatus", line ) );
        }
        try {
            return Integer.parseInt ( parts[1] );
        } catch ( NumberFormatException nfe ) {
            throw new DeploymentException ( sm.getString (
                                                "wsWebSocketContainer.invalidStatus", line ) );
        }
    }
    private void parseHeaders ( String line, Map<String, List<String>> headers ) {
        int index = line.indexOf ( ':' );
        if ( index == -1 ) {
            log.warn ( sm.getString ( "wsWebSocketContainer.invalidHeader", line ) );
            return;
        }
        String headerName = line.substring ( 0, index ).trim().toLowerCase ( Locale.ENGLISH );
        String headerValue = line.substring ( index + 1 ).trim();
        List<String> values = headers.get ( headerName );
        if ( values == null ) {
            values = new ArrayList<> ( 1 );
            headers.put ( headerName, values );
        }
        values.add ( headerValue );
    }
    private String readLine ( ByteBuffer response ) {
        StringBuilder sb = new StringBuilder();
        char c = 0;
        while ( response.hasRemaining() ) {
            c = ( char ) response.get();
            sb.append ( c );
            if ( c == 10 ) {
                break;
            }
        }
        return sb.toString();
    }
    private SSLEngine createSSLEngine ( Map<String, Object> userProperties )
    throws DeploymentException {
        try {
            SSLContext sslContext =
                ( SSLContext ) userProperties.get ( Constants.SSL_CONTEXT_PROPERTY );
            if ( sslContext == null ) {
                sslContext = SSLContext.getInstance ( "TLS" );
                String sslTrustStoreValue =
                    ( String ) userProperties.get ( Constants.SSL_TRUSTSTORE_PROPERTY );
                if ( sslTrustStoreValue != null ) {
                    String sslTrustStorePwdValue = ( String ) userProperties.get (
                                                       Constants.SSL_TRUSTSTORE_PWD_PROPERTY );
                    if ( sslTrustStorePwdValue == null ) {
                        sslTrustStorePwdValue = Constants.SSL_TRUSTSTORE_PWD_DEFAULT;
                    }
                    File keyStoreFile = new File ( sslTrustStoreValue );
                    KeyStore ks = KeyStore.getInstance ( "JKS" );
                    try ( InputStream is = new FileInputStream ( keyStoreFile ) ) {
                        ks.load ( is, sslTrustStorePwdValue.toCharArray() );
                    }
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance (
                                                  TrustManagerFactory.getDefaultAlgorithm() );
                    tmf.init ( ks );
                    sslContext.init ( null, tmf.getTrustManagers(), null );
                } else {
                    sslContext.init ( null, null, null );
                }
            }
            SSLEngine engine = sslContext.createSSLEngine();
            String sslProtocolsValue =
                ( String ) userProperties.get ( Constants.SSL_PROTOCOLS_PROPERTY );
            if ( sslProtocolsValue != null ) {
                engine.setEnabledProtocols ( sslProtocolsValue.split ( "," ) );
            }
            engine.setUseClientMode ( true );
            return engine;
        } catch ( Exception e ) {
            throw new DeploymentException ( sm.getString (
                                                "wsWebSocketContainer.sslEngineFail" ), e );
        }
    }
    @Override
    public long getDefaultMaxSessionIdleTimeout() {
        return defaultMaxSessionIdleTimeout;
    }
    @Override
    public void setDefaultMaxSessionIdleTimeout ( long timeout ) {
        this.defaultMaxSessionIdleTimeout = timeout;
    }
    @Override
    public int getDefaultMaxBinaryMessageBufferSize() {
        return maxBinaryMessageBufferSize;
    }
    @Override
    public void setDefaultMaxBinaryMessageBufferSize ( int max ) {
        maxBinaryMessageBufferSize = max;
    }
    @Override
    public int getDefaultMaxTextMessageBufferSize() {
        return maxTextMessageBufferSize;
    }
    @Override
    public void setDefaultMaxTextMessageBufferSize ( int max ) {
        maxTextMessageBufferSize = max;
    }
    @Override
    public Set<Extension> getInstalledExtensions() {
        return Collections.emptySet();
    }
    @Override
    public long getDefaultAsyncSendTimeout() {
        return defaultAsyncTimeout;
    }
    @Override
    public void setAsyncSendTimeout ( long timeout ) {
        this.defaultAsyncTimeout = timeout;
    }
    public void destroy() {
        CloseReason cr = new CloseReason (
            CloseCodes.GOING_AWAY, sm.getString ( "wsWebSocketContainer.shutdown" ) );
        for ( WsSession session : sessions.keySet() ) {
            try {
                session.close ( cr );
            } catch ( IOException ioe ) {
                log.debug ( sm.getString (
                                "wsWebSocketContainer.sessionCloseFail", session.getId() ), ioe );
            }
        }
        if ( asynchronousChannelGroup != null ) {
            synchronized ( asynchronousChannelGroupLock ) {
                if ( asynchronousChannelGroup != null ) {
                    AsyncChannelGroupUtil.unregister();
                    asynchronousChannelGroup = null;
                }
            }
        }
    }
    private AsynchronousChannelGroup getAsynchronousChannelGroup() {
        AsynchronousChannelGroup result = asynchronousChannelGroup;
        if ( result == null ) {
            synchronized ( asynchronousChannelGroupLock ) {
                if ( asynchronousChannelGroup == null ) {
                    asynchronousChannelGroup = AsyncChannelGroupUtil.register();
                }
                result = asynchronousChannelGroup;
            }
        }
        return result;
    }
    @Override
    public void backgroundProcess() {
        backgroundProcessCount ++;
        if ( backgroundProcessCount >= processPeriod ) {
            backgroundProcessCount = 0;
            for ( WsSession wsSession : sessions.keySet() ) {
                wsSession.checkExpiration();
            }
        }
    }
    @Override
    public void setProcessPeriod ( int period ) {
        this.processPeriod = period;
    }
    @Override
    public int getProcessPeriod() {
        return processPeriod;
    }
    private static class HttpResponse {
        private final int status;
        private final HandshakeResponse handshakeResponse;
        public HttpResponse ( int status, HandshakeResponse handshakeResponse ) {
            this.status = status;
            this.handshakeResponse = handshakeResponse;
        }
        public int getStatus() {
            return status;
        }
        public HandshakeResponse getHandshakeResponse() {
            return handshakeResponse;
        }
    }
}
