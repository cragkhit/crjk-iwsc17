package org.apache.tomcat.websocket;
import javax.websocket.SendResult;
import java.nio.ByteBuffer;
import javax.websocket.SendHandler;
private static class OutputBufferSendHandler implements SendHandler {
    private final SendHandler handler;
    private final long blockingWriteTimeoutExpiry;
    private final ByteBuffer headerBuffer;
    private final ByteBuffer payload;
    private final byte[] mask;
    private final ByteBuffer outputBuffer;
    private final boolean flushRequired;
    private final WsRemoteEndpointImplBase endpoint;
    private int maskIndex;
    public OutputBufferSendHandler ( final SendHandler completion, final long blockingWriteTimeoutExpiry, final ByteBuffer headerBuffer, final ByteBuffer payload, final byte[] mask, final ByteBuffer outputBuffer, final boolean flushRequired, final WsRemoteEndpointImplBase endpoint ) {
        this.maskIndex = 0;
        this.blockingWriteTimeoutExpiry = blockingWriteTimeoutExpiry;
        this.handler = completion;
        this.headerBuffer = headerBuffer;
        this.payload = payload;
        this.mask = mask;
        this.outputBuffer = outputBuffer;
        this.flushRequired = flushRequired;
        this.endpoint = endpoint;
    }
    public void write() {
        while ( this.headerBuffer.hasRemaining() && this.outputBuffer.hasRemaining() ) {
            this.outputBuffer.put ( this.headerBuffer.get() );
        }
        if ( this.headerBuffer.hasRemaining() ) {
            this.outputBuffer.flip();
            this.endpoint.doWrite ( ( SendHandler ) this, this.blockingWriteTimeoutExpiry, this.outputBuffer );
            return;
        }
        final int payloadLeft = this.payload.remaining();
        final int payloadLimit = this.payload.limit();
        final int outputSpace = this.outputBuffer.remaining();
        int toWrite;
        if ( ( toWrite = payloadLeft ) > outputSpace ) {
            toWrite = outputSpace;
            this.payload.limit ( this.payload.position() + toWrite );
        }
        if ( this.mask == null ) {
            this.outputBuffer.put ( this.payload );
        } else {
            for ( int i = 0; i < toWrite; ++i ) {
                this.outputBuffer.put ( ( byte ) ( this.payload.get() ^ ( this.mask[this.maskIndex++] & 0xFF ) ) );
                if ( this.maskIndex > 3 ) {
                    this.maskIndex = 0;
                }
            }
        }
        if ( payloadLeft > outputSpace ) {
            this.payload.limit ( payloadLimit );
            this.outputBuffer.flip();
            this.endpoint.doWrite ( ( SendHandler ) this, this.blockingWriteTimeoutExpiry, this.outputBuffer );
            return;
        }
        if ( this.flushRequired ) {
            this.outputBuffer.flip();
            if ( this.outputBuffer.remaining() == 0 ) {
                this.handler.onResult ( WsRemoteEndpointImplBase.SENDRESULT_OK );
            } else {
                this.endpoint.doWrite ( ( SendHandler ) this, this.blockingWriteTimeoutExpiry, this.outputBuffer );
            }
        } else {
            this.handler.onResult ( WsRemoteEndpointImplBase.SENDRESULT_OK );
        }
    }
    public void onResult ( final SendResult result ) {
        if ( result.isOK() ) {
            if ( this.outputBuffer.hasRemaining() ) {
                this.endpoint.doWrite ( ( SendHandler ) this, this.blockingWriteTimeoutExpiry, this.outputBuffer );
            } else {
                this.outputBuffer.clear();
                this.write();
            }
        } else {
            this.handler.onResult ( result );
        }
    }
}
