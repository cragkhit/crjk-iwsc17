package javax.websocket.server;
import javax.websocket.Endpoint;
import java.util.Set;
public interface ServerApplicationConfig {
    Set<ServerEndpointConfig> getEndpointConfigs ( Set<Class<? extends Endpoint>> p0 );
    Set<Class<?>> getAnnotatedEndpointClasses ( Set<Class<?>> p0 );
}
