package org.apache.tomcat.websocket;
import java.nio.charset.StandardCharsets;
import java.nio.channels.WritePendingException;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.ExceptionUtils;
import javax.websocket.SendResult;
import java.io.IOException;
import javax.websocket.CloseReason;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.websocket.WebSocketContainer;
import org.apache.tomcat.InstanceManager;
import javax.websocket.DeploymentException;
import org.apache.tomcat.InstanceManagerBindings;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.juli.logging.LogFactory;
import javax.websocket.PongMessage;
import javax.websocket.MessageHandler;
import javax.websocket.Extension;
import javax.websocket.EndpointConfig;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.net.URI;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Endpoint;
import org.apache.juli.logging.Log;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.tomcat.util.res.StringManager;
import javax.websocket.Session;
public class WsSession implements Session {
    private static final byte[] ELLIPSIS_BYTES;
    private static final int ELLIPSIS_BYTES_LEN;
    private static final StringManager sm;
    private static AtomicLong ids;
    private final Log log;
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
    private volatile MessageHandler textMessageHandler;
    private volatile MessageHandler binaryMessageHandler;
    private volatile MessageHandler.Whole<PongMessage> pongMessageHandler;
    private volatile State state;
    private final Object stateLock;
    private final Map<String, Object> userProperties;
    private volatile int maxBinaryMessageBufferSize;
    private volatile int maxTextMessageBufferSize;
    private volatile long maxIdleTimeout;
    private volatile long lastActive;
    private Map<FutureToSendHandler, FutureToSendHandler> futures;
    public WsSession ( final Endpoint localEndpoint, final WsRemoteEndpointImplBase wsRemoteEndpoint, final WsWebSocketContainer wsWebSocketContainer, final URI requestUri, final Map<String, List<String>> requestParameterMap, final String queryString, final Principal userPrincipal, final String httpSessionId, final List<Extension> negotiatedExtensions, final String subProtocol, final Map<String, String> pathParameters, final boolean secure, final EndpointConfig endpointConfig ) throws DeploymentException {
        this.log = LogFactory.getLog ( WsSession.class );
        this.textMessageHandler = null;
        this.binaryMessageHandler = null;
        this.pongMessageHandler = null;
        this.state = State.OPEN;
        this.stateLock = new Object();
        this.userProperties = new ConcurrentHashMap<String, Object>();
        this.maxBinaryMessageBufferSize = Constants.DEFAULT_BUFFER_SIZE;
        this.maxTextMessageBufferSize = Constants.DEFAULT_BUFFER_SIZE;
        this.maxIdleTimeout = 0L;
        this.lastActive = System.currentTimeMillis();
        this.futures = new ConcurrentHashMap<FutureToSendHandler, FutureToSendHandler>();
        this.localEndpoint = localEndpoint;
        ( this.wsRemoteEndpoint = wsRemoteEndpoint ).setSession ( this );
        this.remoteEndpointAsync = ( RemoteEndpoint.Async ) new WsRemoteEndpointAsync ( wsRemoteEndpoint );
        this.remoteEndpointBasic = ( RemoteEndpoint.Basic ) new WsRemoteEndpointBasic ( wsRemoteEndpoint );
        this.webSocketContainer = wsWebSocketContainer;
        this.applicationClassLoader = Thread.currentThread().getContextClassLoader();
        wsRemoteEndpoint.setSendTimeout ( wsWebSocketContainer.getDefaultAsyncSendTimeout() );
        this.maxBinaryMessageBufferSize = this.webSocketContainer.getDefaultMaxBinaryMessageBufferSize();
        this.maxTextMessageBufferSize = this.webSocketContainer.getDefaultMaxTextMessageBufferSize();
        this.maxIdleTimeout = this.webSocketContainer.getDefaultMaxSessionIdleTimeout();
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
        this.id = Long.toHexString ( WsSession.ids.getAndIncrement() );
        InstanceManager instanceManager = this.webSocketContainer.getInstanceManager();
        if ( instanceManager == null ) {
            instanceManager = InstanceManagerBindings.get ( this.applicationClassLoader );
        }
        if ( instanceManager != null ) {
            try {
                instanceManager.newInstance ( localEndpoint );
            } catch ( Exception e ) {
                throw new DeploymentException ( WsSession.sm.getString ( "wsSession.instanceNew" ), ( Throwable ) e );
            }
        }
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( WsSession.sm.getString ( "wsSession.created", this.id ) );
        }
    }
    public WebSocketContainer getContainer() {
        this.checkState();
        return ( WebSocketContainer ) this.webSocketContainer;
    }
    public void addMessageHandler ( final MessageHandler listener ) {
        final Class<?> target = Util.getMessageType ( listener );
        this.doAddMessageHandler ( target, listener );
    }
    public <T> void addMessageHandler ( final Class<T> clazz, final MessageHandler.Partial<T> handler ) throws IllegalStateException {
        this.doAddMessageHandler ( clazz, ( MessageHandler ) handler );
    }
    public <T> void addMessageHandler ( final Class<T> clazz, final MessageHandler.Whole<T> handler ) throws IllegalStateException {
        this.doAddMessageHandler ( clazz, ( MessageHandler ) handler );
    }
    private void doAddMessageHandler ( final Class<?> target, final MessageHandler listener ) {
        this.checkState();
        final Set<MessageHandlerResult> mhResults = Util.getMessageHandlers ( target, listener, this.endpointConfig, ( Session ) this );
        for ( final MessageHandlerResult mhResult : mhResults ) {
            switch ( mhResult.getType() ) {
            case TEXT: {
                if ( this.textMessageHandler != null ) {
                    throw new IllegalStateException ( WsSession.sm.getString ( "wsSession.duplicateHandlerText" ) );
                }
                this.textMessageHandler = mhResult.getHandler();
                continue;
            }
            case BINARY: {
                if ( this.binaryMessageHandler != null ) {
                    throw new IllegalStateException ( WsSession.sm.getString ( "wsSession.duplicateHandlerBinary" ) );
                }
                this.binaryMessageHandler = mhResult.getHandler();
                continue;
            }
            case PONG: {
                if ( this.pongMessageHandler != null ) {
                    throw new IllegalStateException ( WsSession.sm.getString ( "wsSession.duplicateHandlerPong" ) );
                }
                final MessageHandler handler = mhResult.getHandler();
                if ( handler instanceof MessageHandler.Whole ) {
                    this.pongMessageHandler = ( MessageHandler.Whole<PongMessage> ) handler;
                    continue;
                }
                throw new IllegalStateException ( WsSession.sm.getString ( "wsSession.invalidHandlerTypePong" ) );
            }
            default: {
                throw new IllegalArgumentException ( WsSession.sm.getString ( "wsSession.unknownHandlerType", listener, mhResult.getType() ) );
            }
            }
        }
    }
    public Set<MessageHandler> getMessageHandlers() {
        this.checkState();
        final Set<MessageHandler> result = new HashSet<MessageHandler>();
        if ( this.binaryMessageHandler != null ) {
            result.add ( this.binaryMessageHandler );
        }
        if ( this.textMessageHandler != null ) {
            result.add ( this.textMessageHandler );
        }
        if ( this.pongMessageHandler != null ) {
            result.add ( ( MessageHandler ) this.pongMessageHandler );
        }
        return result;
    }
    public void removeMessageHandler ( final MessageHandler listener ) {
        this.checkState();
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
        if ( wrapped.equals ( this.textMessageHandler ) || listener.equals ( this.textMessageHandler ) ) {
            this.textMessageHandler = null;
            removed = true;
        }
        if ( wrapped.equals ( this.binaryMessageHandler ) || listener.equals ( this.binaryMessageHandler ) ) {
            this.binaryMessageHandler = null;
            removed = true;
        }
        if ( wrapped.equals ( this.pongMessageHandler ) || listener.equals ( this.pongMessageHandler ) ) {
            this.pongMessageHandler = null;
            removed = true;
        }
        if ( !removed ) {
            throw new IllegalStateException ( WsSession.sm.getString ( "wsSession.removeHandlerFailed", listener ) );
        }
    }
    public String getProtocolVersion() {
        this.checkState();
        return "13";
    }
    public String getNegotiatedSubprotocol() {
        this.checkState();
        return this.subProtocol;
    }
    public List<Extension> getNegotiatedExtensions() {
        this.checkState();
        return this.negotiatedExtensions;
    }
    public boolean isSecure() {
        this.checkState();
        return this.secure;
    }
    public boolean isOpen() {
        return this.state == State.OPEN;
    }
    public long getMaxIdleTimeout() {
        this.checkState();
        return this.maxIdleTimeout;
    }
    public void setMaxIdleTimeout ( final long timeout ) {
        this.checkState();
        this.maxIdleTimeout = timeout;
    }
    public void setMaxBinaryMessageBufferSize ( final int max ) {
        this.checkState();
        this.maxBinaryMessageBufferSize = max;
    }
    public int getMaxBinaryMessageBufferSize() {
        this.checkState();
        return this.maxBinaryMessageBufferSize;
    }
    public void setMaxTextMessageBufferSize ( final int max ) {
        this.checkState();
        this.maxTextMessageBufferSize = max;
    }
    public int getMaxTextMessageBufferSize() {
        this.checkState();
        return this.maxTextMessageBufferSize;
    }
    public Set<Session> getOpenSessions() {
        this.checkState();
        return this.webSocketContainer.getOpenSessions ( this.localEndpoint );
    }
    public RemoteEndpoint.Async getAsyncRemote() {
        this.checkState();
        return this.remoteEndpointAsync;
    }
    public RemoteEndpoint.Basic getBasicRemote() {
        this.checkState();
        return this.remoteEndpointBasic;
    }
    public void close() throws IOException {
        this.close ( new CloseReason ( ( CloseReason.CloseCode ) CloseReason.CloseCodes.NORMAL_CLOSURE, "" ) );
    }
    public void close ( final CloseReason closeReason ) throws IOException {
        this.doClose ( closeReason, closeReason );
    }
    public void doClose ( final CloseReason closeReasonMessage, final CloseReason closeReasonLocal ) {
        if ( this.state != State.OPEN ) {
            return;
        }
        synchronized ( this.stateLock ) {
            if ( this.state != State.OPEN ) {
                return;
            }
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( WsSession.sm.getString ( "wsSession.doClose", this.id ) );
            }
            try {
                this.wsRemoteEndpoint.setBatchingAllowed ( false );
            } catch ( IOException e ) {
                this.log.warn ( WsSession.sm.getString ( "wsSession.flushFailOnClose" ), e );
                this.fireEndpointOnError ( e );
            }
            this.state = State.OUTPUT_CLOSED;
            this.sendCloseMessage ( closeReasonMessage );
            this.fireEndpointOnClose ( closeReasonLocal );
        }
        final IOException ioe = new IOException ( WsSession.sm.getString ( "wsSession.messageFailed" ) );
        final SendResult sr = new SendResult ( ( Throwable ) ioe );
        for ( final FutureToSendHandler f2sh : this.futures.keySet() ) {
            f2sh.onResult ( sr );
        }
    }
    public void onClose ( final CloseReason closeReason ) {
        synchronized ( this.stateLock ) {
            if ( this.state != State.CLOSED ) {
                try {
                    this.wsRemoteEndpoint.setBatchingAllowed ( false );
                } catch ( IOException e ) {
                    this.log.warn ( WsSession.sm.getString ( "wsSession.flushFailOnClose" ), e );
                    this.fireEndpointOnError ( e );
                }
                if ( this.state == State.OPEN ) {
                    this.state = State.OUTPUT_CLOSED;
                    this.sendCloseMessage ( closeReason );
                    this.fireEndpointOnClose ( closeReason );
                }
                this.state = State.CLOSED;
                this.wsRemoteEndpoint.close();
            }
        }
    }
    private void fireEndpointOnClose ( final CloseReason closeReason ) {
        Throwable throwable = null;
        InstanceManager instanceManager = this.webSocketContainer.getInstanceManager();
        if ( instanceManager == null ) {
            instanceManager = InstanceManagerBindings.get ( this.applicationClassLoader );
        }
        final Thread t = Thread.currentThread();
        final ClassLoader cl = t.getContextClassLoader();
        t.setContextClassLoader ( this.applicationClassLoader );
        try {
            this.localEndpoint.onClose ( ( Session ) this, closeReason );
        } catch ( Throwable t2 ) {
            ExceptionUtils.handleThrowable ( t2 );
            throwable = t2;
        } finally {
            if ( instanceManager != null ) {
                try {
                    instanceManager.destroyInstance ( this.localEndpoint );
                } catch ( Throwable t3 ) {
                    ExceptionUtils.handleThrowable ( t3 );
                    if ( throwable == null ) {
                        throwable = t3;
                    }
                }
            }
            t.setContextClassLoader ( cl );
        }
        if ( throwable != null ) {
            this.fireEndpointOnError ( throwable );
        }
    }
    private void fireEndpointOnError ( final Throwable throwable ) {
        final Thread t = Thread.currentThread();
        final ClassLoader cl = t.getContextClassLoader();
        t.setContextClassLoader ( this.applicationClassLoader );
        try {
            this.localEndpoint.onError ( ( Session ) this, throwable );
        } finally {
            t.setContextClassLoader ( cl );
        }
    }
    private void sendCloseMessage ( final CloseReason closeReason ) {
        final ByteBuffer msg = ByteBuffer.allocate ( 125 );
        final CloseReason.CloseCode closeCode = closeReason.getCloseCode();
        if ( closeCode == CloseReason.CloseCodes.CLOSED_ABNORMALLY ) {
            msg.putShort ( ( short ) CloseReason.CloseCodes.PROTOCOL_ERROR.getCode() );
        } else {
            msg.putShort ( ( short ) closeCode.getCode() );
        }
        final String reason = closeReason.getReasonPhrase();
        if ( reason != null && reason.length() > 0 ) {
            appendCloseReasonWithTruncation ( msg, reason );
        }
        msg.flip();
        try {
            this.wsRemoteEndpoint.sendMessageBlock ( ( byte ) 8, msg, true );
        } catch ( IOException ) {}
        catch ( WritePendingException e ) {
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( WsSession.sm.getString ( "wsSession.sendCloseFail", this.id ), e );
            }
            this.wsRemoteEndpoint.close();
            if ( closeCode != CloseReason.CloseCodes.CLOSED_ABNORMALLY ) {
                this.localEndpoint.onError ( ( Session ) this, ( Throwable ) e );
            }
        } finally {
            this.webSocketContainer.unregisterSession ( this.localEndpoint, this );
        }
    }
    protected static void appendCloseReasonWithTruncation ( final ByteBuffer msg, final String reason ) {
        final byte[] reasonBytes = reason.getBytes ( StandardCharsets.UTF_8 );
        if ( reasonBytes.length <= 123 ) {
            msg.put ( reasonBytes );
        } else {
            int remaining = 123 - WsSession.ELLIPSIS_BYTES_LEN;
            int pos = 0;
            for ( byte[] bytesNext = reason.substring ( pos, pos + 1 ).getBytes ( StandardCharsets.UTF_8 ); remaining >= bytesNext.length; remaining -= bytesNext.length, ++pos, bytesNext = reason.substring ( pos, pos + 1 ).getBytes ( StandardCharsets.UTF_8 ) ) {
                msg.put ( bytesNext );
            }
            msg.put ( WsSession.ELLIPSIS_BYTES );
        }
    }
    protected void registerFuture ( final FutureToSendHandler f2sh ) {
        boolean fail = false;
        synchronized ( this.stateLock ) {
            if ( this.state == State.OPEN ) {
                this.futures.put ( f2sh, f2sh );
            } else if ( !f2sh.isDone() ) {
                fail = true;
            }
        }
        if ( fail ) {
            final IOException ioe = new IOException ( WsSession.sm.getString ( "wsSession.messageFailed" ) );
            final SendResult sr = new SendResult ( ( Throwable ) ioe );
            f2sh.onResult ( sr );
        }
    }
    protected void unregisterFuture ( final FutureToSendHandler f2sh ) {
        this.futures.remove ( f2sh );
    }
    public URI getRequestURI() {
        this.checkState();
        return this.requestUri;
    }
    public Map<String, List<String>> getRequestParameterMap() {
        this.checkState();
        return this.requestParameterMap;
    }
    public String getQueryString() {
        this.checkState();
        return this.queryString;
    }
    public Principal getUserPrincipal() {
        this.checkState();
        return this.userPrincipal;
    }
    public Map<String, String> getPathParameters() {
        this.checkState();
        return this.pathParameters;
    }
    public String getId() {
        return this.id;
    }
    public Map<String, Object> getUserProperties() {
        this.checkState();
        return this.userProperties;
    }
    public Endpoint getLocal() {
        return this.localEndpoint;
    }
    public String getHttpSessionId() {
        return this.httpSessionId;
    }
    protected MessageHandler getTextMessageHandler() {
        return this.textMessageHandler;
    }
    protected MessageHandler getBinaryMessageHandler() {
        return this.binaryMessageHandler;
    }
    protected MessageHandler.Whole<PongMessage> getPongMessageHandler() {
        return this.pongMessageHandler;
    }
    protected void updateLastActive() {
        this.lastActive = System.currentTimeMillis();
    }
    protected void checkExpiration() {
        final long timeout = this.maxIdleTimeout;
        if ( timeout < 1L ) {
            return;
        }
        if ( System.currentTimeMillis() - this.lastActive > timeout ) {
            final String msg = WsSession.sm.getString ( "wsSession.timeout", this.getId() );
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( msg );
            }
            this.doClose ( new CloseReason ( ( CloseReason.CloseCode ) CloseReason.CloseCodes.GOING_AWAY, msg ), new CloseReason ( ( CloseReason.CloseCode ) CloseReason.CloseCodes.CLOSED_ABNORMALLY, msg ) );
        }
    }
    private void checkState() {
        if ( this.state == State.CLOSED ) {
            throw new IllegalStateException ( WsSession.sm.getString ( "wsSession.closed", this.id ) );
        }
    }
    static {
        ELLIPSIS_BYTES = "\u2026".getBytes ( StandardCharsets.UTF_8 );
        ELLIPSIS_BYTES_LEN = WsSession.ELLIPSIS_BYTES.length;
        sm = StringManager.getManager ( WsSession.class );
        WsSession.ids = new AtomicLong ( 0L );
    }
    private enum State {
        OPEN,
        OUTPUT_CLOSED,
        CLOSED;
    }
}
