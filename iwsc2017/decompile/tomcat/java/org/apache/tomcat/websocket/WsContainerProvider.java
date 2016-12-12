package org.apache.tomcat.websocket;
import javax.websocket.WebSocketContainer;
import javax.websocket.ContainerProvider;
public class WsContainerProvider extends ContainerProvider {
    protected WebSocketContainer getContainer() {
        return ( WebSocketContainer ) new WsWebSocketContainer();
    }
}
