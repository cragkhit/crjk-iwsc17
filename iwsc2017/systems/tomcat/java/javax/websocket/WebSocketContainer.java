package javax.websocket;
import java.io.IOException;
import java.net.URI;
import java.util.Set;
public interface WebSocketContainer {
    long getDefaultAsyncSendTimeout();
    void setAsyncSendTimeout ( long timeout );
    Session connectToServer ( Object endpoint, URI path )
    throws DeploymentException, IOException;
    Session connectToServer ( Class<?> annotatedEndpointClass, URI path )
    throws DeploymentException, IOException;
    Session connectToServer ( Endpoint endpoint,
                              ClientEndpointConfig clientEndpointConfiguration, URI path )
    throws DeploymentException, IOException;
    Session connectToServer ( Class<? extends Endpoint> endpoint,
                              ClientEndpointConfig clientEndpointConfiguration, URI path )
    throws DeploymentException, IOException;
    long getDefaultMaxSessionIdleTimeout();
    void setDefaultMaxSessionIdleTimeout ( long timeout );
    int getDefaultMaxBinaryMessageBufferSize();
    void setDefaultMaxBinaryMessageBufferSize ( int max );
    int getDefaultMaxTextMessageBufferSize();
    void setDefaultMaxTextMessageBufferSize ( int max );
    Set<Extension> getInstalledExtensions();
}
