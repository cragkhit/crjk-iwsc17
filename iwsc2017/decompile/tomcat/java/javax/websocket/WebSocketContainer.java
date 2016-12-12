package javax.websocket;
import java.util.Set;
import java.io.IOException;
import java.net.URI;
public interface WebSocketContainer {
    long getDefaultAsyncSendTimeout();
    void setAsyncSendTimeout ( long p0 );
    Session connectToServer ( Object p0, URI p1 ) throws DeploymentException, IOException;
    Session connectToServer ( Class<?> p0, URI p1 ) throws DeploymentException, IOException;
    Session connectToServer ( Endpoint p0, ClientEndpointConfig p1, URI p2 ) throws DeploymentException, IOException;
    Session connectToServer ( Class<? extends Endpoint> p0, ClientEndpointConfig p1, URI p2 ) throws DeploymentException, IOException;
    long getDefaultMaxSessionIdleTimeout();
    void setDefaultMaxSessionIdleTimeout ( long p0 );
    int getDefaultMaxBinaryMessageBufferSize();
    void setDefaultMaxBinaryMessageBufferSize ( int p0 );
    int getDefaultMaxTextMessageBufferSize();
    void setDefaultMaxTextMessageBufferSize ( int p0 );
    Set<Extension> getInstalledExtensions();
}
