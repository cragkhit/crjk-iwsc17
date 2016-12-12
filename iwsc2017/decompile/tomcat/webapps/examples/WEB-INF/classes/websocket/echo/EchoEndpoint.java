// 
// Decompiled by Procyon v0.5.29
// 

package websocket.echo;

import java.nio.ByteBuffer;
import java.io.IOException;
import javax.websocket.RemoteEndpoint;
import javax.websocket.MessageHandler;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.Endpoint;

public class EchoEndpoint extends Endpoint
{
    public void onOpen(final Session session, final EndpointConfig endpointConfig) {
        final RemoteEndpoint.Basic remoteEndpointBasic = session.getBasicRemote();
        session.addMessageHandler((MessageHandler)new EchoMessageHandlerText(remoteEndpointBasic));
        session.addMessageHandler((MessageHandler)new EchoMessageHandlerBinary(remoteEndpointBasic));
    }
    
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
}
