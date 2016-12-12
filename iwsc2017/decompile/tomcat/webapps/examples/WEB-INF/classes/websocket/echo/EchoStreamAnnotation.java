// 
// Decompiled by Procyon v0.5.29
// 

package websocket.echo;

import javax.websocket.PongMessage;
import javax.websocket.OnMessage;
import java.io.IOException;
import javax.websocket.Session;
import java.io.OutputStream;
import java.io.Writer;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/websocket/echoStreamAnnotation")
public class EchoStreamAnnotation
{
    Writer writer;
    OutputStream stream;
    
    @OnMessage
    public void echoTextMessage(final Session session, final String msg, final boolean last) throws IOException {
        if (this.writer == null) {
            this.writer = session.getBasicRemote().getSendWriter();
        }
        this.writer.write(msg);
        if (last) {
            this.writer.close();
            this.writer = null;
        }
    }
    
    @OnMessage
    public void echoBinaryMessage(final byte[] msg, final Session session, final boolean last) throws IOException {
        if (this.stream == null) {
            this.stream = session.getBasicRemote().getSendStream();
        }
        this.stream.write(msg);
        this.stream.flush();
        if (last) {
            this.stream.close();
            this.stream = null;
        }
    }
    
    @OnMessage
    public void echoPongMessage(final PongMessage pm) {
    }
}
