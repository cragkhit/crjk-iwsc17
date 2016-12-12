package javax.websocket;
import java.util.Iterator;
import java.util.ServiceLoader;
public abstract class ContainerProvider {
    private static final String DEFAULT_PROVIDER_CLASS_NAME = "org.apache.tomcat.websocket.WsWebSocketContainer";
    public static WebSocketContainer getWebSocketContainer() {
        WebSocketContainer result = null;
        final ServiceLoader<ContainerProvider> serviceLoader = ServiceLoader.load ( ContainerProvider.class );
        for ( Iterator<ContainerProvider> iter = serviceLoader.iterator(); result == null && iter.hasNext(); result = iter.next().getContainer() ) {}
        if ( result == null ) {
            try {
                final Class<WebSocketContainer> clazz = ( Class<WebSocketContainer> ) Class.forName ( "org.apache.tomcat.websocket.WsWebSocketContainer" );
                result = clazz.newInstance();
            } catch ( ClassNotFoundException ) {}
            catch ( InstantiationException ) {}
            catch ( IllegalAccessException ex ) {}
        }
        return result;
    }
    protected abstract WebSocketContainer getContainer();
}
