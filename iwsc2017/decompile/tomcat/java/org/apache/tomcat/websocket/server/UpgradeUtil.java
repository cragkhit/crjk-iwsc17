package org.apache.tomcat.websocket.server;
import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.security.ConcurrentMessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import org.apache.tomcat.websocket.TransformationFactory;
import java.io.IOException;
import java.util.Iterator;
import java.util.Enumeration;
import javax.websocket.EndpointConfig;
import org.apache.tomcat.websocket.pojo.PojoEndpointServer;
import javax.websocket.Endpoint;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import org.apache.tomcat.websocket.WsHandshakeResponse;
import javax.servlet.ServletException;
import org.apache.tomcat.websocket.Transformation;
import java.util.Collections;
import java.util.Collection;
import org.apache.tomcat.websocket.Constants;
import org.apache.tomcat.websocket.Util;
import javax.websocket.Extension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.websocket.server.ServerEndpointConfig;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import org.apache.tomcat.util.res.StringManager;
public class UpgradeUtil {
    private static final StringManager sm;
    private static final byte[] WS_ACCEPT;
    public static boolean isWebSocketUpgradeRequest ( final ServletRequest request, final ServletResponse response ) {
        return request instanceof HttpServletRequest && response instanceof HttpServletResponse && headerContainsToken ( ( HttpServletRequest ) request, "Upgrade", "websocket" ) && "GET".equals ( ( ( HttpServletRequest ) request ).getMethod() );
    }
    public static void doUpgrade ( final WsServerContainer sc, final HttpServletRequest req, final HttpServletResponse resp, final ServerEndpointConfig sec, final Map<String, String> pathParams ) throws ServletException, IOException {
        String subProtocol = null;
        if ( !headerContainsToken ( req, "Connection", "upgrade" ) ) {
            resp.sendError ( 400 );
            return;
        }
        if ( !headerContainsToken ( req, "Sec-WebSocket-Version", "13" ) ) {
            resp.setStatus ( 426 );
            resp.setHeader ( "Sec-WebSocket-Version", "13" );
            return;
        }
        final String key = req.getHeader ( "Sec-WebSocket-Key" );
        if ( key == null ) {
            resp.sendError ( 400 );
            return;
        }
        final String origin = req.getHeader ( "Origin" );
        if ( !sec.getConfigurator().checkOrigin ( origin ) ) {
            resp.sendError ( 403 );
            return;
        }
        final List<String> subProtocols = getTokensFromHeader ( req, "Sec-WebSocket-Protocol" );
        subProtocol = sec.getConfigurator().getNegotiatedSubprotocol ( sec.getSubprotocols(), ( List ) subProtocols );
        final List<Extension> extensionsRequested = new ArrayList<Extension>();
        final Enumeration<String> extHeaders = ( Enumeration<String> ) req.getHeaders ( "Sec-WebSocket-Extensions" );
        while ( extHeaders.hasMoreElements() ) {
            Util.parseExtensionHeader ( extensionsRequested, extHeaders.nextElement() );
        }
        List<Extension> installedExtensions = null;
        if ( sec.getExtensions().size() == 0 ) {
            installedExtensions = Constants.INSTALLED_EXTENSIONS;
        } else {
            installedExtensions = new ArrayList<Extension>();
            installedExtensions.addAll ( sec.getExtensions() );
            installedExtensions.addAll ( Constants.INSTALLED_EXTENSIONS );
        }
        final List<Extension> negotiatedExtensionsPhase1 = ( List<Extension> ) sec.getConfigurator().getNegotiatedExtensions ( ( List ) installedExtensions, ( List ) extensionsRequested );
        final List<Transformation> transformations = createTransformations ( negotiatedExtensionsPhase1 );
        List<Extension> negotiatedExtensionsPhase2;
        if ( transformations.isEmpty() ) {
            negotiatedExtensionsPhase2 = Collections.emptyList();
        } else {
            negotiatedExtensionsPhase2 = new ArrayList<Extension> ( transformations.size() );
            for ( final Transformation t : transformations ) {
                negotiatedExtensionsPhase2.add ( t.getExtensionResponse() );
            }
        }
        Transformation transformation = null;
        final StringBuilder responseHeaderExtensions = new StringBuilder();
        boolean first = true;
        for ( final Transformation t2 : transformations ) {
            if ( first ) {
                first = false;
            } else {
                responseHeaderExtensions.append ( ',' );
            }
            append ( responseHeaderExtensions, t2.getExtensionResponse() );
            if ( transformation == null ) {
                transformation = t2;
            } else {
                transformation.setNext ( t2 );
            }
        }
        if ( transformation != null && !transformation.validateRsvBits ( 0 ) ) {
            throw new ServletException ( UpgradeUtil.sm.getString ( "upgradeUtil.incompatibleRsv" ) );
        }
        resp.setHeader ( "Upgrade", "websocket" );
        resp.setHeader ( "Connection", "upgrade" );
        resp.setHeader ( "Sec-WebSocket-Accept", getWebSocketAccept ( key ) );
        if ( subProtocol != null && subProtocol.length() > 0 ) {
            resp.setHeader ( "Sec-WebSocket-Protocol", subProtocol );
        }
        if ( !transformations.isEmpty() ) {
            resp.setHeader ( "Sec-WebSocket-Extensions", responseHeaderExtensions.toString() );
        }
        final WsHandshakeRequest wsRequest = new WsHandshakeRequest ( req, pathParams );
        final WsHandshakeResponse wsResponse = new WsHandshakeResponse();
        final WsPerSessionServerEndpointConfig perSessionServerEndpointConfig = new WsPerSessionServerEndpointConfig ( sec );
        sec.getConfigurator().modifyHandshake ( ( ServerEndpointConfig ) perSessionServerEndpointConfig, ( HandshakeRequest ) wsRequest, ( HandshakeResponse ) wsResponse );
        wsRequest.finished();
        for ( final Map.Entry<String, List<String>> entry : wsResponse.getHeaders().entrySet() ) {
            for ( final String headerValue : entry.getValue() ) {
                resp.addHeader ( ( String ) entry.getKey(), headerValue );
            }
        }
        Endpoint ep;
        try {
            final Class<?> clazz = ( Class<?> ) sec.getEndpointClass();
            if ( Endpoint.class.isAssignableFrom ( clazz ) ) {
                ep = ( Endpoint ) sec.getConfigurator().getEndpointInstance ( ( Class ) clazz );
            } else {
                ep = new PojoEndpointServer();
                perSessionServerEndpointConfig.getUserProperties().put ( "org.apache.tomcat.websocket.pojo.PojoEndpoint.pathParams", pathParams );
            }
        } catch ( InstantiationException e ) {
            throw new ServletException ( ( Throwable ) e );
        }
        final WsHttpUpgradeHandler wsHandler = ( WsHttpUpgradeHandler ) req.upgrade ( ( Class ) WsHttpUpgradeHandler.class );
        wsHandler.preInit ( ep, ( EndpointConfig ) perSessionServerEndpointConfig, sc, wsRequest, negotiatedExtensionsPhase2, subProtocol, transformation, pathParams, req.isSecure() );
    }
    private static List<Transformation> createTransformations ( final List<Extension> negotiatedExtensions ) {
        final TransformationFactory factory = TransformationFactory.getInstance();
        final LinkedHashMap<String, List<List<Extension.Parameter>>> extensionPreferences = new LinkedHashMap<String, List<List<Extension.Parameter>>>();
        final List<Transformation> result = new ArrayList<Transformation> ( negotiatedExtensions.size() );
        for ( final Extension extension : negotiatedExtensions ) {
            List<List<Extension.Parameter>> preferences = extensionPreferences.get ( extension.getName() );
            if ( preferences == null ) {
                preferences = new ArrayList<List<Extension.Parameter>>();
                extensionPreferences.put ( extension.getName(), preferences );
            }
            preferences.add ( extension.getParameters() );
        }
        for ( final Map.Entry<String, List<List<Extension.Parameter>>> entry : extensionPreferences.entrySet() ) {
            final Transformation transformation = factory.create ( entry.getKey(), entry.getValue(), true );
            if ( transformation != null ) {
                result.add ( transformation );
            }
        }
        return result;
    }
    private static void append ( final StringBuilder sb, final Extension extension ) {
        if ( extension == null || extension.getName() == null || extension.getName().length() == 0 ) {
            return;
        }
        sb.append ( extension.getName() );
        for ( final Extension.Parameter p : extension.getParameters() ) {
            sb.append ( ';' );
            sb.append ( p.getName() );
            if ( p.getValue() != null ) {
                sb.append ( '=' );
                sb.append ( p.getValue() );
            }
        }
    }
    private static boolean headerContainsToken ( final HttpServletRequest req, final String headerName, final String target ) {
        final Enumeration<String> headers = ( Enumeration<String> ) req.getHeaders ( headerName );
        while ( headers.hasMoreElements() ) {
            final String header = headers.nextElement();
            final String[] split;
            final String[] tokens = split = header.split ( "," );
            for ( final String token : split ) {
                if ( target.equalsIgnoreCase ( token.trim() ) ) {
                    return true;
                }
            }
        }
        return false;
    }
    private static List<String> getTokensFromHeader ( final HttpServletRequest req, final String headerName ) {
        final List<String> result = new ArrayList<String>();
        final Enumeration<String> headers = ( Enumeration<String> ) req.getHeaders ( headerName );
        while ( headers.hasMoreElements() ) {
            final String header = headers.nextElement();
            final String[] split;
            final String[] tokens = split = header.split ( "," );
            for ( final String token : split ) {
                result.add ( token.trim() );
            }
        }
        return result;
    }
    private static String getWebSocketAccept ( final String key ) {
        final byte[] digest = ConcurrentMessageDigest.digestSHA1 ( new byte[][] { key.getBytes ( StandardCharsets.ISO_8859_1 ), UpgradeUtil.WS_ACCEPT } );
        return Base64.encodeBase64String ( digest );
    }
    static {
        sm = StringManager.getManager ( UpgradeUtil.class.getPackage().getName() );
        WS_ACCEPT = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11".getBytes ( StandardCharsets.ISO_8859_1 );
    }
}
