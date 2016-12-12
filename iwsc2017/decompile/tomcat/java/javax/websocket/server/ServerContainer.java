package javax.websocket.server;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;
public interface ServerContainer extends WebSocketContainer {
    void addEndpoint ( Class<?> p0 ) throws DeploymentException;
    void addEndpoint ( ServerEndpointConfig p0 ) throws DeploymentException;
}
