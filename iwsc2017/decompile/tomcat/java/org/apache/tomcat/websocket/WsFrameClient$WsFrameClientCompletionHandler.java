package org.apache.tomcat.websocket;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.EOFException;
import java.nio.channels.CompletionHandler;
private class WsFrameClientCompletionHandler implements CompletionHandler<Integer, Void> {
    @Override
    public void completed ( final Integer result, final Void attachment ) {
        if ( result == -1 ) {
            if ( WsFrameClient.this.isOpen() ) {
                WsFrameClient.access$100 ( WsFrameClient.this, new EOFException() );
            }
            return;
        }
        WsFrameClient.access$200 ( WsFrameClient.this ).flip();
        try {
            WsFrameClient.access$300 ( WsFrameClient.this );
        } catch ( IOException e ) {
            if ( WsFrameClient.this.isOpen() ) {
                WsFrameClient.access$500 ( WsFrameClient.this ).debug ( WsFrameClient.access$400().getString ( "wsFrameClient.ioe" ), e );
                WsFrameClient.access$100 ( WsFrameClient.this, e );
            }
        }
    }
    @Override
    public void failed ( final Throwable exc, final Void attachment ) {
        if ( exc instanceof ReadBufferOverflowException ) {
            WsFrameClient.access$202 ( WsFrameClient.this, ByteBuffer.allocate ( ( ( ReadBufferOverflowException ) exc ).getMinBufferSize() ) );
            WsFrameClient.access$200 ( WsFrameClient.this ).flip();
            try {
                WsFrameClient.access$300 ( WsFrameClient.this );
            } catch ( IOException e ) {
                WsFrameClient.access$100 ( WsFrameClient.this, e );
            }
        } else {
            WsFrameClient.access$100 ( WsFrameClient.this, exc );
        }
    }
}
