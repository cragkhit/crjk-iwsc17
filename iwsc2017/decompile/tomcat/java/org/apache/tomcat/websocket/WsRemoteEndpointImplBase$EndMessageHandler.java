package org.apache.tomcat.websocket;
import javax.websocket.SendResult;
import javax.websocket.SendHandler;
private static class EndMessageHandler implements SendHandler {
    private final WsRemoteEndpointImplBase endpoint;
    private final SendHandler handler;
    public EndMessageHandler ( final WsRemoteEndpointImplBase endpoint, final SendHandler handler ) {
        this.endpoint = endpoint;
        this.handler = handler;
    }
    public void onResult ( final SendResult result ) {
        this.endpoint.endMessage ( this.handler, result );
    }
}
