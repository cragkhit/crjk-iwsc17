package org.apache.tomcat.websocket;
import java.io.IOException;
import javax.websocket.SendResult;
import java.nio.charset.CoderResult;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.CharBuffer;
import javax.websocket.SendHandler;
private class TextMessageSendHandler implements SendHandler {
    private final SendHandler handler;
    private final CharBuffer message;
    private final boolean isLast;
    private final CharsetEncoder encoder;
    private final ByteBuffer buffer;
    private final WsRemoteEndpointImplBase endpoint;
    private volatile boolean isDone;
    public TextMessageSendHandler ( final SendHandler handler, final CharBuffer message, final boolean isLast, final CharsetEncoder encoder, final ByteBuffer encoderBuffer, final WsRemoteEndpointImplBase endpoint ) {
        this.isDone = false;
        this.handler = handler;
        this.message = message;
        this.isLast = isLast;
        this.encoder = encoder.reset();
        this.buffer = encoderBuffer;
        this.endpoint = endpoint;
    }
    public void write() {
        this.buffer.clear();
        final CoderResult cr = this.encoder.encode ( this.message, this.buffer, true );
        if ( cr.isError() ) {
            throw new IllegalArgumentException ( cr.toString() );
        }
        this.isDone = !cr.isOverflow();
        this.buffer.flip();
        this.endpoint.startMessage ( ( byte ) 1, this.buffer, this.isDone && this.isLast, ( SendHandler ) this );
    }
    public void onResult ( final SendResult result ) {
        if ( this.isDone ) {
            WsRemoteEndpointImplBase.access$200 ( this.endpoint ).complete ( this.isLast );
            this.handler.onResult ( result );
        } else if ( !result.isOK() ) {
            this.handler.onResult ( result );
        } else if ( WsRemoteEndpointImplBase.access$300 ( WsRemoteEndpointImplBase.this ) ) {
            final SendResult sr = new SendResult ( ( Throwable ) new IOException ( WsRemoteEndpointImplBase.access$400().getString ( "wsRemoteEndpoint.closedDuringMessage" ) ) );
            this.handler.onResult ( sr );
        } else {
            this.write();
        }
    }
}
