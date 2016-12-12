package javax.websocket.server;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;
public interface ServerContainer extends WebSocketContainer {
    public abstract void addEndpoint ( Class<?> clazz ) throws DeploymentException;
    public abstract void addEndpoint ( ServerEndpointConfig sec )
    throws DeploymentException;
}
