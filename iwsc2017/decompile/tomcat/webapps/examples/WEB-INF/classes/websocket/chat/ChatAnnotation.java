// 
// Decompiled by Procyon v0.5.29
// 

package websocket.chat;

import java.util.concurrent.CopyOnWriteArraySet;
import org.apache.juli.logging.LogFactory;
import java.util.Iterator;
import java.io.IOException;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import util.HTMLFilter;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.juli.logging.Log;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/websocket/chat")
public class ChatAnnotation
{
    private static final Log log;
    private static final String GUEST_PREFIX = "Guest";
    private static final AtomicInteger connectionIds;
    private static final Set<ChatAnnotation> connections;
    private final String nickname;
    private Session session;
    
    public ChatAnnotation() {
        this.nickname = "Guest" + ChatAnnotation.connectionIds.getAndIncrement();
    }
    
    @OnOpen
    public void start(final Session session) {
        this.session = session;
        ChatAnnotation.connections.add(this);
        final String message = String.format("* %s %s", this.nickname, "has joined.");
        broadcast(message);
    }
    
    @OnClose
    public void end() {
        ChatAnnotation.connections.remove(this);
        final String message = String.format("* %s %s", this.nickname, "has disconnected.");
        broadcast(message);
    }
    
    @OnMessage
    public void incoming(final String message) {
        final String filteredMessage = String.format("%s: %s", this.nickname, HTMLFilter.filter(message.toString()));
        broadcast(filteredMessage);
    }
    
    @OnError
    public void onError(final Throwable t) throws Throwable {
        ChatAnnotation.log.error((Object)("Chat Error: " + t.toString()), t);
    }
    
    private static void broadcast(final String msg) {
        for (final ChatAnnotation client : ChatAnnotation.connections) {
            try {
                synchronized (client) {
                    client.session.getBasicRemote().sendText(msg);
                }
            }
            catch (IOException e) {
                ChatAnnotation.log.debug((Object)"Chat Error: Failed to send message to client", (Throwable)e);
                ChatAnnotation.connections.remove(client);
                try {
                    client.session.close();
                }
                catch (IOException ex) {}
                final String message = String.format("* %s %s", client.nickname, "has been disconnected.");
                broadcast(message);
            }
        }
    }
    
    static {
        log = LogFactory.getLog((Class)ChatAnnotation.class);
        connectionIds = new AtomicInteger(0);
        connections = new CopyOnWriteArraySet<ChatAnnotation>();
    }
}
