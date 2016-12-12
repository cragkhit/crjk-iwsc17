// 
// Decompiled by Procyon v0.5.29
// 

package websocket.echo;

import javax.websocket.PongMessage;
import java.nio.ByteBuffer;
import javax.websocket.OnMessage;
import java.io.IOException;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/websocket/echoAnnotation")
public class EchoAnnotation
{
    @OnMessage
    public void echoTextMessage(final Session session, final String msg, final boolean last) {
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(msg, last);
            }
        }
        catch (IOException e) {
            try {
                session.close();
            }
            catch (IOException ex) {}
        }
    }
    
    @OnMessage
    public void echoBinaryMessage(final Session session, final ByteBuffer bb, final boolean last) {
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendBinary(bb, last);
            }
        }
        catch (IOException e) {
            try {
                session.close();
            }
            catch (IOException ex) {}
        }
    }
    
    @OnMessage
    public void echoPongMessage(final PongMessage pm) {
    }
}
