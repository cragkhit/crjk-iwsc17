package org.apache.tomcat.websocket;
import java.util.List;
import java.nio.ByteBuffer;
private final class UnmaskTransformation extends TerminalTransformation {
    @Override
    public TransformationResult getMoreData ( final byte opCode, final boolean fin, final int rsv, final ByteBuffer dest ) {
        while ( WsFrameBase.access$400 ( WsFrameBase.this ) < WsFrameBase.access$300 ( WsFrameBase.this ) && WsFrameBase.this.inputBuffer.remaining() > 0 && dest.hasRemaining() ) {
            final byte b = ( byte ) ( ( WsFrameBase.this.inputBuffer.get() ^ WsFrameBase.access$500 ( WsFrameBase.this ) [WsFrameBase.access$600 ( WsFrameBase.this )] ) & 0xFF );
            WsFrameBase.access$608 ( WsFrameBase.this );
            if ( WsFrameBase.access$600 ( WsFrameBase.this ) == 4 ) {
                WsFrameBase.access$602 ( WsFrameBase.this, 0 );
            }
            WsFrameBase.access$408 ( WsFrameBase.this );
            dest.put ( b );
        }
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
