package org.apache.tomcat.websocket;
import javax.websocket.SendResult;
import javax.websocket.SendHandler;
private static class BlockingSendHandler implements SendHandler {
    private SendResult sendResult;
    private BlockingSendHandler() {
        this.sendResult = null;
    }
    public void onResult ( final SendResult result ) {
        this.sendResult = result;
    }
    public SendResult getSendResult() {
        return this.sendResult;
    }
}
