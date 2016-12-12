package org.apache.tomcat.websocket.server;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.SSLSupport;
import java.io.IOException;
import javax.websocket.CloseReason;
import org.apache.tomcat.websocket.WsIOException;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketEvent;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import org.apache.tomcat.websocket.WsWebSocketContainer;
import org.apache.tomcat.websocket.WsRemoteEndpointImplBase;
import javax.servlet.http.HttpSession;
import org.apache.tomcat.websocket.WsSession;
import javax.servlet.http.WebConnection;
import java.util.Map;
import org.apache.tomcat.websocket.Transformation;
import javax.websocket.Extension;
import java.util.List;
import javax.websocket.EndpointConfig;
import javax.websocket.Endpoint;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.coyote.http11.upgrade.InternalHttpUpgradeHandler;
public class WsHttpUpgradeHandler implements InternalHttpUpgradeHandler {
    private static final Log log;
    private static final StringManager sm;
    private final ClassLoader applicationClassLoader;
    private SocketWrapperBase<?> socketWrapper;
    private Endpoint ep;
    private EndpointConfig endpointConfig;
    private WsServerContainer webSocketContainer;
    private WsHandshakeRequest handshakeRequest;
    private List<Extension> negotiatedExtensions;
    private String subProtocol;
    private Transformation transformation;
    private Map<String, String> pathParameters;
    private boolean secure;
    private WebConnection connection;
    private WsRemoteEndpointImplServer wsRemoteEndpointServer;
    private WsFrameServer wsFrame;
    private WsSession wsSession;
    public WsHttpUpgradeHandler() {
        this.applicationClassLoader = Thread.currentThread().getContextClassLoader();
    }
    @Override
    public void setSocketWrapper ( final SocketWrapperBase<?> socketWrapper ) {
        this.socketWrapper = socketWrapper;
    }
    public void preInit ( final Endpoint ep, final EndpointConfig endpointConfig, final WsServerContainer wsc, final WsHandshakeRequest handshakeRequest, final List<Extension> negotiatedExtensionsPhase2, final String subProtocol, final Transformation transformation, final Map<String, String> pathParameters, final boolean secure ) {
        this.ep = ep;
        this.endpointConfig = endpointConfig;
        this.webSocketContainer = wsc;
        this.handshakeRequest = handshakeRequest;
        this.negotiatedExtensions = negotiatedExtensionsPhase2;
        this.subProtocol = subProtocol;
        this.transformation = transformation;
        this.pathParameters = pathParameters;
        this.secure = secure;
    }
    public void init ( final WebConnection connection ) {
        if ( this.ep == null ) {
            throw new IllegalStateException ( WsHttpUpgradeHandler.sm.getString ( "wsHttpUpgradeHandler.noPreInit" ) );
        }
        String httpSessionId = null;
        final Object session = this.handshakeRequest.getHttpSession();
        if ( session != null ) {
            httpSessionId = ( ( HttpSession ) session ).getId();
        }
        final Thread t = Thread.currentThread();
        final ClassLoader cl = t.getContextClassLoader();
        t.setContextClassLoader ( this.applicationClassLoader );
        try {
            this.wsRemoteEndpointServer = new WsRemoteEndpointImplServer ( this.socketWrapper, this.webSocketContainer );
            this.wsSession = new WsSession ( this.ep, this.wsRemoteEndpointServer, this.webSocketContainer, this.handshakeRequest.getRequestURI(), this.handshakeRequest.getParameterMap(), this.handshakeRequest.getQueryString(), this.handshakeRequest.getUserPrincipal(), httpSessionId, this.negotiatedExtensions, this.subProtocol, this.pathParameters, this.secure, this.endpointConfig );
            this.wsFrame = new WsFrameServer ( this.socketWrapper, this.wsSession, this.transformation, this.applicationClassLoader );
            this.wsRemoteEndpointServer.setTransformation ( this.wsFrame.getTransformation() );
            this.ep.onOpen ( ( Session ) this.wsSession, this.endpointConfig );
            this.webSocketContainer.registerSession ( this.ep, this.wsSession );
        } catch ( DeploymentException e ) {
            throw new IllegalArgumentException ( ( Throwable ) e );
        } finally {
            t.setContextClassLoader ( cl );
        }
    }
    @Override
    public AbstractEndpoint.Handler.SocketState upgradeDispatch ( final SocketEvent status ) {
        switch ( status ) {
        case OPEN_READ: {
            try {
                this.wsFrame.onDataAvailable();
                break;
            } catch ( WsIOException ws ) {
                this.close ( ws.getCloseReason() );
                break;
            } catch ( IOException ioe ) {
                this.onError ( ioe );
                final CloseReason cr = new CloseReason ( ( CloseReason.CloseCode ) CloseReason.CloseCodes.CLOSED_ABNORMALLY, ioe.getMessage() );
                this.close ( cr );
                return AbstractEndpoint.Handler.SocketState.CLOSED;
            }
        }
        case OPEN_WRITE: {
            this.wsRemoteEndpointServer.onWritePossible ( false );
            break;
        }
        case STOP: {
            CloseReason cr2 = new CloseReason ( ( CloseReason.CloseCode ) CloseReason.CloseCodes.GOING_AWAY, WsHttpUpgradeHandler.sm.getString ( "wsHttpUpgradeHandler.serverStop" ) );
            try {
                this.wsSession.close ( cr2 );
                break;
            } catch ( IOException ioe2 ) {
                this.onError ( ioe2 );
                cr2 = new CloseReason ( ( CloseReason.CloseCode ) CloseReason.CloseCodes.CLOSED_ABNORMALLY, ioe2.getMessage() );
                this.close ( cr2 );
                return AbstractEndpoint.Handler.SocketState.CLOSED;
            }
        }
        case ERROR: {
            final String msg = WsHttpUpgradeHandler.sm.getString ( "wsHttpUpgradeHandler.closeOnError" );
            this.wsSession.doClose ( new CloseReason ( ( CloseReason.CloseCode ) CloseReason.CloseCodes.GOING_AWAY, msg ), new CloseReason ( ( CloseReason.CloseCode ) CloseReason.CloseCodes.CLOSED_ABNORMALLY, msg ) );
        }
        case DISCONNECT:
        case TIMEOUT: {
            return AbstractEndpoint.Handler.SocketState.CLOSED;
        }
        }
        if ( this.wsFrame.isOpen() ) {
            return AbstractEndpoint.Handler.SocketState.UPGRADED;
        }
        return AbstractEndpoint.Handler.SocketState.CLOSED;
    }
    @Override
    public void pause() {
    }
    public void destroy() {
        if ( this.connection != null ) {
            try {
                this.connection.close();
            } catch ( Exception e ) {
                WsHttpUpgradeHandler.log.error ( WsHttpUpgradeHandler.sm.getString ( "wsHttpUpgradeHandler.destroyFailed" ), e );
            }
        }
    }
    private void onError ( final Throwable throwable ) {
        final Thread t = Thread.currentThread();
        final ClassLoader cl = t.getContextClassLoader();
        t.setContextClassLoader ( this.applicationClassLoader );
        try {
            this.ep.onError ( ( Session ) this.wsSession, throwable );
        } finally {
            t.setContextClassLoader ( cl );
        }
    }
    private void close ( final CloseReason cr ) {
        this.wsSession.onClose ( cr );
    }
    @Override
    public void setSslSupport ( final SSLSupport sslSupport ) {
    }
    static {
        log = LogFactory.getLog ( WsHttpUpgradeHandler.class );
        sm = StringManager.getManager ( WsHttpUpgradeHandler.class );
    }
}
