// 
// Decompiled by Procyon v0.5.29
// 

package websocket.drawboard.wsmessages;

import java.nio.ByteBuffer;

public final class BinaryWebsocketMessage extends AbstractWebsocketMessage
{
    private final ByteBuffer bytes;
    
    public BinaryWebsocketMessage(final ByteBuffer bytes) {
        this.bytes = bytes;
    }
    
    public ByteBuffer getBytes() {
        return this.bytes;
    }
}
