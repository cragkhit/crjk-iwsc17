// 
// Decompiled by Procyon v0.5.29
// 

package websocket.echo;

import java.io.IOException;
import javax.websocket.RemoteEndpoint;
import javax.websocket.MessageHandler;

private static class EchoMessageHandlerText implements MessageHandler.Partial<String>
{
    private final RemoteEndpoint.Basic remoteEndpointBasic;
    
    private EchoMessageHandlerText(final RemoteEndpoint.Basic remoteEndpointBasic) {
        this.remoteEndpointBasic = remoteEndpointBasic;
    }
    
    public void onMessage(final String message, final boolean last) {
        try {
            if (this.remoteEndpointBasic != null) {
                this.remoteEndpointBasic.sendText(message, last);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
