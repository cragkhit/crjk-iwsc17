package org.apache.tomcat.websocket;
import javax.websocket.SendResult;
import javax.websocket.SendHandler;
private static class StateUpdateSendHandler implements SendHandler {
    private final SendHandler handler;
    private final StateMachine stateMachine;
    public StateUpdateSendHandler ( final SendHandler handler, final StateMachine stateMachine ) {
        this.handler = handler;
        this.stateMachine = stateMachine;
    }
    public void onResult ( final SendResult result ) {
        if ( result.isOK() ) {
            this.stateMachine.complete ( true );
        }
        this.handler.onResult ( result );
    }
}
