package org.apache.tomcat.websocket;
import javax.websocket.SendResult;
import javax.websocket.SendHandler;
private static class IntermediateMessageHandler implements SendHandler {
    private final WsRemoteEndpointImplBase endpoint;
    public IntermediateMessageHandler ( final WsRemoteEndpointImplBase endpoint ) {
        this.endpoint = endpoint;
    }
    public void onResult ( final SendResult result ) {
        this.endpoint.endMessage ( null, result );
    }
}
