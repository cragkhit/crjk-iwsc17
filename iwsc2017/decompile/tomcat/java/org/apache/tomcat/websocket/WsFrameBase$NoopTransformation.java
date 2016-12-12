package org.apache.tomcat.websocket;
import java.util.List;
import java.nio.ByteBuffer;
private final class NoopTransformation extends TerminalTransformation {
    @Override
    public TransformationResult getMoreData ( final byte opCode, final boolean fin, final int rsv, final ByteBuffer dest ) {
        long toWrite = Math.min ( WsFrameBase.access$300 ( WsFrameBase.this ) - WsFrameBase.access$400 ( WsFrameBase.this ), WsFrameBase.this.inputBuffer.remaining() );
        toWrite = Math.min ( toWrite, dest.remaining() );
        final int orgLimit = WsFrameBase.this.inputBuffer.limit();
        WsFrameBase.this.inputBuffer.limit ( WsFrameBase.this.inputBuffer.position() + ( int ) toWrite );
        dest.put ( WsFrameBase.this.inputBuffer );
        WsFrameBase.this.inputBuffer.limit ( orgLimit );
        WsFrameBase.access$402 ( WsFrameBase.this, WsFrameBase.access$400 ( WsFrameBase.this ) + toWrite );
        if ( WsFrameBase.access$400 ( WsFrameBase.this ) == WsFrameBase.access$300 ( WsFrameBase.this ) ) {
            return TransformationResult.END_OF_FRAME;
        }
        if ( WsFrameBase.this.inputBuffer.remaining() == 0 ) {
            return TransformationResult.UNDERFLOW;
        }
        return TransformationResult.OVERFLOW;
    }
    @Override
    public List<MessagePart> sendMessagePart ( final List<MessagePart> messageParts ) {
        return messageParts;
    }
}
