package org.apache.tomcat.websocket.pojo;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.DeploymentException;
import java.util.Collections;
import javax.websocket.Decoder;
import java.util.List;
public class PojoEndpointClient extends PojoEndpointBase {
    public PojoEndpointClient ( final Object pojo, final List<Class<? extends Decoder>> decoders ) throws DeploymentException {
        this.setPojo ( pojo );
        this.setMethodMapping ( new PojoMethodMapping ( pojo.getClass(), decoders, null ) );
        this.setPathParameters ( Collections.emptyMap() );
    }
    public void onOpen ( final Session session, final EndpointConfig config ) {
        this.doOnOpen ( session, config );
    }
}
