package org.apache.tomcat.websocket;
import javax.websocket.SendResult;
import java.nio.ByteBuffer;
import javax.websocket.SendHandler;
private static class OutputBufferFlushSendHandler implements SendHandler {
    private final ByteBuffer outputBuffer;
    private final SendHandler handler;
    public OutputBufferFlushSendHandler ( final ByteBuffer outputBuffer, final SendHandler handler ) {
        this.outputBuffer = outputBuffer;
        this.handler = handler;
    }
    public void onResult ( final SendResult result ) {
        if ( result.isOK() ) {
            this.outputBuffer.clear();
        }
        this.handler.onResult ( result );
    }
}
