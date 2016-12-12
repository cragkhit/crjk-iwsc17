package javax.websocket;
import java.util.Iterator;
import java.util.ServiceLoader;
public abstract class ContainerProvider {
    private static final String DEFAULT_PROVIDER_CLASS_NAME =
        "org.apache.tomcat.websocket.WsWebSocketContainer";
    public static WebSocketContainer getWebSocketContainer() {
        WebSocketContainer result = null;
        ServiceLoader<ContainerProvider> serviceLoader =
            ServiceLoader.load ( ContainerProvider.class );
        Iterator<ContainerProvider> iter = serviceLoader.iterator();
        while ( result == null && iter.hasNext() ) {
            result = iter.next().getContainer();
        }
        if ( result == null ) {
            try {
                @SuppressWarnings ( "unchecked" )
                Class<WebSocketContainer> clazz =
                    ( Class<WebSocketContainer> ) Class.forName (
                        DEFAULT_PROVIDER_CLASS_NAME );
                result = clazz.newInstance();
            } catch ( ClassNotFoundException | InstantiationException |
                          IllegalAccessException e ) {
            }
        }
        return result;
    }
    protected abstract WebSocketContainer getContainer();
}
