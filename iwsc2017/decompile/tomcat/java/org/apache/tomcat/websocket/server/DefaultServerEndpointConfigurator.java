package org.apache.tomcat.websocket.server;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import javax.websocket.Extension;
import java.util.Iterator;
import java.util.List;
import javax.websocket.server.ServerEndpointConfig;
public class DefaultServerEndpointConfigurator extends ServerEndpointConfig.Configurator {
    public <T> T getEndpointInstance ( final Class<T> clazz ) throws InstantiationException {
        try {
            return clazz.newInstance();
        } catch ( IllegalAccessException e ) {
            final InstantiationException ie = new InstantiationException();
            ie.initCause ( e );
            throw ie;
        }
    }
    public String getNegotiatedSubprotocol ( final List<String> supported, final List<String> requested ) {
        for ( final String request : requested ) {
            if ( supported.contains ( request ) ) {
                return request;
            }
        }
        return "";
    }
    public List<Extension> getNegotiatedExtensions ( final List<Extension> installed, final List<Extension> requested ) {
        final Set<String> installedNames = new HashSet<String>();
        for ( final Extension e : installed ) {
            installedNames.add ( e.getName() );
        }
        final List<Extension> result = new ArrayList<Extension>();
        for ( final Extension request : requested ) {
            if ( installedNames.contains ( request.getName() ) ) {
                result.add ( request );
            }
        }
        return result;
    }
    public boolean checkOrigin ( final String originHeaderValue ) {
        return true;
    }
    public void modifyHandshake ( final ServerEndpointConfig sec, final HandshakeRequest request, final HandshakeResponse response ) {
    }
}
