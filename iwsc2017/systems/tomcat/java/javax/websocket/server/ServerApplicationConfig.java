package javax.websocket.server;
import java.util.Set;
import javax.websocket.Endpoint;
public interface ServerApplicationConfig {
    Set<ServerEndpointConfig> getEndpointConfigs (
        Set<Class<? extends Endpoint>> scanned );
    Set<Class<?>> getAnnotatedEndpointClasses ( Set<Class<?>> scanned );
}
