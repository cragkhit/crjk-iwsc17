package org.apache.tomcat.websocket;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.WritePendingException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCode;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Extension;
import javax.websocket.MessageHandler;
import javax.websocket.MessageHandler.Partial;
import javax.websocket.MessageHandler.Whole;
import javax.websocket.PongMessage;
import javax.websocket.RemoteEndpoint;
import javax.websocket.SendResult;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.InstanceManagerBindings;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.res.StringManager;
public class WsSession implements Session {
    private static final byte[] ELLIPSIS_BYTES =
        "\u2026".getBytes ( StandardCharsets.UTF_8 );
    private static final int ELLIPSIS_BYTES_LEN = ELLIPSIS_BYTES.length;
    private static final StringManager sm = StringManager.getManager ( WsSession.class );
    private static AtomicLong ids = new AtomicLong ( 0 );
    private final Log log = LogFactory.getLog ( WsSession.class );
    private final Endpoint localEndpoint;
    private final WsRemoteEndpointImplBase wsRemoteEndpoint;
    private final RemoteEndpoint.Async remoteEndpointAsync;
    private final RemoteEndpoint.Basic remoteEndpointBasic;
    private final ClassLoader applicationClassLoader;
    private final WsWebSocketContainer webSocketContainer;
    private final URI requestUri;
    private final Map<String, List<String>> requestParameterMap;
    private final String queryString;
    private final Principal userPrincipal;
    private final EndpointConfig endpointConfig;
    private final List<Extension> negotiatedExtensions;
    private final String subProtocol;
    private final Map<String, String> pathParameters;
    private final boolean secure;
    private final String httpSessionId;
    private final String id;
    private volatile MessageHandler textMessageHandler = null;
    private volatile MessageHandler binaryMessageHandler = null;
    private volatile MessageHandler.Whole<PongMessage> pongMessageHandler = null;
    private volatile State state = State.OPEN;
    private final Object stateLock = new Object();
    private final Map<String, Object> userProperties = new ConcurrentHashMap<>();
    private volatile int maxBinaryMessageBufferSize =
        Constants.DEFAULT_BUFFER_SIZE;
    private volatile int maxTextMessageBufferSize =
        Constants.DEFAULT_BUFFER_SIZE;
    private volatile long maxIdleTimeout = 0;
    private volatile long lastActive = System.currentTimeMillis();
    private Map<FutureToSendHandler, FutureToSendHandler> futures = new ConcurrentHashMap<>();
    public WsSession ( Endpoint localEndpoint,
                       WsRemoteEndpointImplBase wsRemoteEndpoint,
                       WsWebSocketContainer wsWebSocketContainer,
                       URI requestUri, Map<String, List<String>> requestParameterMap,
                       String queryString, Principal userPrincipal, String httpSessionId,
                       List<Extension> negotiatedExtensions, String subProtocol, Map<String, String> pathParameters,
                       boolean secure, EndpointConfig endpointConfig ) throws DeploymentException {
        this.localEndpoint = localEndpoint;
        this.wsRemoteEndpoint = wsRemoteEndpoint;
        this.wsRemoteEndpoint.setSession ( this );
        this.remoteEndpointAsync = new WsRemoteEndpointAsync ( wsRemoteEndpoint );
        this.remoteEndpointBasic = new WsRemoteEndpointBasic ( wsRemoteEndpoint );
        this.webSocketContainer = wsWebSocketContainer;
        applicationClassLoader = Thread.currentThread().getContextClassLoader();
        wsRemoteEndpoint.setSendTimeout (
            wsWebSocketContainer.getDefaultAsyncSendTimeout() );
        this.maxBinaryMessageBufferSize =
            webSocketContainer.getDefaultMaxBinaryMessageBufferSize();
        this.maxTextMessageBufferSize =
            webSocketContainer.getDefaultMaxTextMessageBufferSize();
        this.maxIdleTimeout =
            webSocketContainer.getDefaultMaxSessionIdleTimeout();
        this.requestUri = requestUri;
        if ( requestParameterMap == null ) {
            this.requestParameterMap = Collections.emptyMap();
        } else {
            this.requestParameterMap = requestParameterMap;
        }
        this.queryString = queryString;
        this.userPrincipal = userPrincipal;
        this.httpSessionId = httpSessionId;
        this.negotiatedExtensions = negotiatedExtensions;
        if ( subProtocol == null ) {
            this.subProtocol = "";
        } else {
            this.subProtocol = subProtocol;
        }
        this.pathParameters = pathParameters;
        this.secure = secure;
        this.wsRemoteEndpoint.setEncoders ( endpointConfig );
        this.endpointConfig = endpointConfig;
        this.userProperties.putAll ( endpointConfig.getUserProperties() );
        this.id = Long.toHexString ( ids.getAndIncrement() );
        InstanceManager instanceManager = webSocketContainer.getInstanceManager();
        if ( instanceManager == null ) {
            instanceManager = InstanceManagerBindings.get ( applicationClassLoader );
        }
        if ( instanceManager != null ) {
            try {
                instanceManager.newInstance ( localEndpoint );
            } catch ( Exception e ) {
                throw new DeploymentException ( sm.getString ( "wsSession.instanceNew" ), e );
            }
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "wsSession.created", id ) );
        }
    }
    @Override
    public WebSocketContainer getContainer() {
        checkState();
        return webSocketContainer;
    }
    @Override
    public void addMessageHandler ( MessageHandler listener ) {
        Class<?> target = Util.getMessageType ( listener );
        doAddMessageHandler ( target, listener );
    }
    @Override
    public <T> void addMessageHandler ( Class<T> clazz, Partial<T> handler )
    throws IllegalStateException {
        doAddMessageHandler ( clazz, handler );
    }
    @Override
    public <T> void addMessageHandler ( Class<T> clazz, Whole<T> handler )
    throws IllegalStateException {
        doAddMessageHandler ( clazz, handler );
    }
    @SuppressWarnings ( "unchecked" )
    private void doAddMessageHandler ( Class<?> target, MessageHandler listener ) {
        checkState();
        Set<MessageHandlerResult> mhResults =
            Util.getMessageHandlers ( target, listener, endpointConfig, this );
        for ( MessageHandlerResult mhResult : mhResults ) {
            switch ( mhResult.getType() ) {
            case TEXT: {
                if ( textMessageHandler != null ) {
                    throw new IllegalStateException (
                        sm.getString ( "wsSession.duplicateHandlerText" ) );
                }
                textMessageHandler = mhResult.getHandler();
                break;
            }
            case BINARY: {
                if ( binaryMessageHandler != null ) {
                    throw new IllegalStateException (
                        sm.getString ( "wsSession.duplicateHandlerBinary" ) );
                }
                binaryMessageHandler = mhResult.getHandler();
                break;
            }
            case PONG: {
                if ( pongMessageHandler != null ) {
                    throw new IllegalStateException (
                        sm.getString ( "wsSession.duplicateHandlerPong" ) );
                }
                MessageHandler handler = mhResult.getHandler();
                if ( handler instanceof MessageHandler.Whole<?> ) {
                    pongMessageHandler =
                        ( MessageHandler.Whole<PongMessage> ) handler;
                } else {
                    throw new IllegalStateException (
                        sm.getString ( "wsSession.invalidHandlerTypePong" ) );
                }
                break;
            }
            default: {
                throw new IllegalArgumentException ( sm.getString (
                        "wsSession.unknownHandlerType", listener,
                        mhResult.getType() ) );
            }
            }
        }
    }
    @Override
    public Set<MessageHandler> getMessageHandlers() {
        checkState();
        Set<MessageHandler> result = new HashSet<>();
        if ( binaryMessageHandler != null ) {
            result.add ( binaryMessageHandler );
        }
        if ( textMessageHandler != null ) {
            result.add ( textMessageHandler );
        }
        if ( pongMessageHandler != null ) {
            result.add ( pongMessageHandler );
        }
        return result;
    }
    @Override
    public void removeMessageHandler ( MessageHandler listener ) {
        checkState();
        if ( listener == null ) {
            return;
        }
        MessageHandler wrapped = null;
        if ( listener instanceof WrappedMessageHandler ) {
            wrapped = ( ( WrappedMessageHandler ) listener ).getWrappedHandler();
        }
        if ( wrapped == null ) {
            wrapped = listener;
        }
        boolean removed = false;
        if ( wrapped.equals ( textMessageHandler ) ||
                listener.equals ( textMessageHandler ) ) {
            textMessageHandler = null;
            removed = true;
        }
        if ( wrapped.equals ( binaryMessageHandler ) ||
                listener.equals ( binaryMessageHandler ) ) {
            binaryMessageHandler = null;
            removed = true;
        }
        if ( wrapped.equals ( pongMessageHandler ) ||
                listener.equals ( pongMessageHandler ) ) {
            pongMessageHandler = null;
            removed = true;
        }
        if ( !removed ) {
            throw new IllegalStateException (
                sm.getString ( "wsSession.removeHandlerFailed", listener ) );
        }
    }
    @Override
    public String getProtocolVersion() {
        checkState();
        return Constants.WS_VERSION_HEADER_VALUE;
    }
    @Override
    public String getNegotiatedSubprotocol() {
        checkState();
        return subProtocol;
    }
    @Override
    public List<Extension> getNegotiatedExtensions() {
        checkState();
        return negotiatedExtensions;
    }
    @Override
    public boolean isSecure() {
        checkState();
        return secure;
    }
    @Override
    public boolean isOpen() {
        return state == State.OPEN;
    }
    @Override
    public long getMaxIdleTimeout() {
        checkState();
        return maxIdleTimeout;
    }
    @Override
    public void setMaxIdleTimeout ( long timeout ) {
        checkState();
        this.maxIdleTimeout = timeout;
    }
    @Override
    public void setMaxBinaryMessageBufferSize ( int max ) {
        checkState();
        this.maxBinaryMessageBufferSize = max;
    }
    @Override
    public int getMaxBinaryMessageBufferSize() {
        checkState();
        return maxBinaryMessageBufferSize;
    }
    @Override
    public void setMaxTextMessageBufferSize ( int max ) {
        checkState();
        this.maxTextMessageBufferSize = max;
    }
    @Override
    public int getMaxTextMessageBufferSize() {
        checkState();
        return maxTextMessageBufferSize;
    }
    @Override
    public Set<Session> getOpenSessions() {
        checkState();
        return webSocketContainer.getOpenSessions ( localEndpoint );
    }
    @Override
    public RemoteEndpoint.Async getAsyncRemote() {
        checkState();
        return remoteEndpointAsync;
    }
    @Override
    public RemoteEndpoint.Basic getBasicRemote() {
        checkState();
        return remoteEndpointBasic;
    }
    @Override
    public void close() throws IOException {
        close ( new CloseReason ( CloseCodes.NORMAL_CLOSURE, "" ) );
    }
    @Override
    public void close ( CloseReason closeReason ) throws IOException {
        doClose ( closeReason, closeReason );
    }
    public void doClose ( CloseReason closeReasonMessage, CloseReason closeReasonLocal ) {
        if ( state != State.OPEN ) {
            return;
        }
        synchronized ( stateLock ) {
            if ( state != State.OPEN ) {
                return;
            }
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "wsSession.doClose", id ) );
            }
            try {
                wsRemoteEndpoint.setBatchingAllowed ( false );
            } catch ( IOException e ) {
                log.warn ( sm.getString ( "wsSession.flushFailOnClose" ), e );
                fireEndpointOnError ( e );
            }
            state = State.OUTPUT_CLOSED;
            sendCloseMessage ( closeReasonMessage );
            fireEndpointOnClose ( closeReasonLocal );
        }
        IOException ioe = new IOException ( sm.getString ( "wsSession.messageFailed" ) );
        SendResult sr = new SendResult ( ioe );
        for ( FutureToSendHandler f2sh : futures.keySet() ) {
            f2sh.onResult ( sr );
        }
    }
    public void onClose ( CloseReason closeReason ) {
        synchronized ( stateLock ) {
            if ( state != State.CLOSED ) {
                try {
                    wsRemoteEndpoint.setBatchingAllowed ( false );
                } catch ( IOException e ) {
                    log.warn ( sm.getString ( "wsSession.flushFailOnClose" ), e );
                    fireEndpointOnError ( e );
                }
                if ( state == State.OPEN ) {
                    state = State.OUTPUT_CLOSED;
                    sendCloseMessage ( closeReason );
                    fireEndpointOnClose ( closeReason );
                }
                state = State.CLOSED;
                wsRemoteEndpoint.close();
            }
        }
    }
    private void fireEndpointOnClose ( CloseReason closeReason ) {
        Throwable throwable = null;
        InstanceManager instanceManager = webSocketContainer.getInstanceManager();
        if ( instanceManager == null ) {
            instanceManager = InstanceManagerBindings.get ( applicationClassLoader );
        }
        Thread t = Thread.currentThread();
        ClassLoader cl = t.getContextClassLoader();
        t.setContextClassLoader ( applicationClassLoader );
        try {
            localEndpoint.onClose ( this, closeReason );
        } catch ( Throwable t1 ) {
            ExceptionUtils.handleThrowable ( t1 );
            throwable = t1;
        } finally {
            if ( instanceManager != null ) {
                try {
                    instanceManager.destroyInstance ( localEndpoint );
                } catch ( Throwable t2 ) {
                    ExceptionUtils.handleThrowable ( t2 );
                    if ( throwable == null ) {
                        throwable = t2;
                    }
                }
            }
            t.setContextClassLoader ( cl );
        }
        if ( throwable != null ) {
            fireEndpointOnError ( throwable );
        }
    }
    private void fireEndpointOnError ( Throwable throwable ) {
        Thread t = Thread.currentThread();
        ClassLoader cl = t.getContextClassLoader();
        t.setContextClassLoader ( applicationClassLoader );
        try {
            localEndpoint.onError ( this, throwable );
        } finally {
            t.setContextClassLoader ( cl );
        }
    }
    private void sendCloseMessage ( CloseReason closeReason ) {
        ByteBuffer msg = ByteBuffer.allocate ( 125 );
        CloseCode closeCode = closeReason.getCloseCode();
        if ( closeCode == CloseCodes.CLOSED_ABNORMALLY ) {
            msg.putShort ( ( short ) CloseCodes.PROTOCOL_ERROR.getCode() );
        } else {
            msg.putShort ( ( short ) closeCode.getCode() );
        }
        String reason = closeReason.getReasonPhrase();
        if ( reason != null && reason.length() > 0 ) {
            appendCloseReasonWithTruncation ( msg, reason );
        }
        msg.flip();
        try {
            wsRemoteEndpoint.sendMessageBlock ( Constants.OPCODE_CLOSE, msg, true );
        } catch ( IOException | WritePendingException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "wsSession.sendCloseFail", id ), e );
            }
            wsRemoteEndpoint.close();
            if ( closeCode != CloseCodes.CLOSED_ABNORMALLY ) {
                localEndpoint.onError ( this, e );
            }
        } finally {
            webSocketContainer.unregisterSession ( localEndpoint, this );
        }
    }
    protected static void appendCloseReasonWithTruncation ( ByteBuffer msg,
            String reason ) {
        byte[] reasonBytes = reason.getBytes ( StandardCharsets.UTF_8 );
        if ( reasonBytes.length  <= 123 ) {
            msg.put ( reasonBytes );
        } else {
            int remaining = 123 - ELLIPSIS_BYTES_LEN;
            int pos = 0;
            byte[] bytesNext = reason.substring ( pos, pos + 1 ).getBytes (
                                   StandardCharsets.UTF_8 );
            while ( remaining >= bytesNext.length ) {
                msg.put ( bytesNext );
                remaining -= bytesNext.length;
                pos++;
                bytesNext = reason.substring ( pos, pos + 1 ).getBytes (
                                StandardCharsets.UTF_8 );
            }
            msg.put ( ELLIPSIS_BYTES );
        }
    }
    protected void registerFuture ( FutureToSendHandler f2sh ) {
        boolean fail = false;
        synchronized ( stateLock ) {
            if ( state == State.OPEN ) {
                futures.put ( f2sh, f2sh );
            } else if ( f2sh.isDone() ) {
            } else {
                fail = true;
            }
        }
        if ( fail ) {
            IOException ioe = new IOException ( sm.getString ( "wsSession.messageFailed" ) );
            SendResult sr = new SendResult ( ioe );
            f2sh.onResult ( sr );
        }
    }
    protected void unregisterFuture ( FutureToSendHandler f2sh ) {
        futures.remove ( f2sh );
    }
    @Override
    public URI getRequestURI() {
        checkState();
        return requestUri;
    }
    @Override
    public Map<String, List<String>> getRequestParameterMap() {
        checkState();
        return requestParameterMap;
    }
    @Override
    public String getQueryString() {
        checkState();
        return queryString;
    }
    @Override
    public Principal getUserPrincipal() {
        checkState();
        return userPrincipal;
    }
    @Override
    public Map<String, String> getPathParameters() {
        checkState();
        return pathParameters;
    }
    @Override
    public String getId() {
        return id;
    }
    @Override
    public Map<String, Object> getUserProperties() {
        checkState();
        return userProperties;
    }
    public Endpoint getLocal() {
        return localEndpoint;
    }
    public String getHttpSessionId() {
        return httpSessionId;
    }
    protected MessageHandler getTextMessageHandler() {
        return textMessageHandler;
    }
    protected MessageHandler getBinaryMessageHandler() {
        return binaryMessageHandler;
    }
    protected MessageHandler.Whole<PongMessage> getPongMessageHandler() {
        return pongMessageHandler;
    }
    protected void updateLastActive() {
        lastActive = System.currentTimeMillis();
    }
    protected void checkExpiration() {
        long timeout = maxIdleTimeout;
        if ( timeout < 1 ) {
            return;
        }
        if ( System.currentTimeMillis() - lastActive > timeout ) {
            String msg = sm.getString ( "wsSession.timeout", getId() );
            if ( log.isDebugEnabled() ) {
                log.debug ( msg );
            }
            doClose ( new CloseReason ( CloseCodes.GOING_AWAY, msg ),
                      new CloseReason ( CloseCodes.CLOSED_ABNORMALLY, msg ) );
        }
    }
    private void checkState() {
        if ( state == State.CLOSED ) {
            throw new IllegalStateException ( sm.getString ( "wsSession.closed", id ) );
        }
    }
    private static enum State {
        OPEN,
        OUTPUT_CLOSED,
        CLOSED
    }
}
