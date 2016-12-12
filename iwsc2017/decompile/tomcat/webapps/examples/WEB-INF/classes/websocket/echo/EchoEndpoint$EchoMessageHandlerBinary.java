// 
// Decompiled by Procyon v0.5.29
// 

package websocket.echo;

import java.io.IOException;
import javax.websocket.RemoteEndpoint;
import java.nio.ByteBuffer;
import javax.websocket.MessageHandler;

private static class EchoMessageHandlerBinary implements MessageHandler.Partial<ByteBuffer>
{
    private final RemoteEndpoint.Basic remoteEndpointBasic;
    
    private EchoMessageHandlerBinary(final RemoteEndpoint.Basic remoteEndpointBasic) {
        this.remoteEndpointBasic = remoteEndpointBasic;
    }
    
    public void onMessage(final ByteBuffer message, final boolean last) {
        try {
            if (this.remoteEndpointBasic != null) {
                this.remoteEndpointBasic.sendBinary(message, last);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
