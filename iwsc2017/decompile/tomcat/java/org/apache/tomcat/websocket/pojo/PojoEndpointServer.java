package org.apache.tomcat.websocket.pojo;
import java.util.Map;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import org.apache.tomcat.util.res.StringManager;
public class PojoEndpointServer extends PojoEndpointBase {
    private static final StringManager sm;
    public void onOpen ( final Session session, final EndpointConfig endpointConfig ) {
        final ServerEndpointConfig sec = ( ServerEndpointConfig ) endpointConfig;
        Object pojo;
        try {
            pojo = sec.getConfigurator().getEndpointInstance ( sec.getEndpointClass() );
        } catch ( InstantiationException e ) {
            throw new IllegalArgumentException ( PojoEndpointServer.sm.getString ( "pojoEndpointServer.getPojoInstanceFail", sec.getEndpointClass().getName() ), e );
        }
        this.setPojo ( pojo );
        final Map<String, String> pathParameters = sec.getUserProperties().get ( "org.apache.tomcat.websocket.pojo.PojoEndpoint.pathParams" );
        this.setPathParameters ( pathParameters );
        final PojoMethodMapping methodMapping = sec.getUserProperties().get ( "org.apache.tomcat.websocket.pojo.PojoEndpoint.methodMapping" );
        this.setMethodMapping ( methodMapping );
        this.doOnOpen ( session, endpointConfig );
    }
    static {
        sm = StringManager.getManager ( PojoEndpointServer.class );
    }
}
