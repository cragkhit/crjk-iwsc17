// 
// Decompiled by Procyon v0.5.29
// 

package websocket.drawboard;

import org.apache.juli.logging.LogFactory;
import java.io.IOException;
import java.io.EOFException;
import javax.websocket.CloseReason;
import websocket.drawboard.wsmessages.AbstractWebsocketMessage;
import websocket.drawboard.wsmessages.StringWebsocketMessage;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.MessageHandler;
import org.apache.juli.logging.Log;
import javax.websocket.Endpoint;

public final class DrawboardEndpoint extends Endpoint
{
    private static final Log log;
    private static volatile Room room;
    private static final Object roomLock;
    private Room.Player player;
    private final MessageHandler.Whole<String> stringHandler;
    
    public DrawboardEndpoint() {
        this.stringHandler = (MessageHandler.Whole<String>)new MessageHandler.Whole<String>() {
            public void onMessage(final String message) {
                DrawboardEndpoint.room.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean dontSwallowException = false;
                            try {
                                final char messageType = message.charAt(0);
                                final String messageContent = message.substring(1);
                                switch (messageType) {
                                    case '1': {
                                        final int indexOfChar = messageContent.indexOf(124);
                                        final long msgId = Long.parseLong(messageContent.substring(0, indexOfChar));
                                        final DrawMessage msg = DrawMessage.parseFromString(messageContent.substring(indexOfChar + 1));
                                        dontSwallowException = true;
                                        if (DrawboardEndpoint.this.player != null) {
                                            DrawboardEndpoint.this.player.handleDrawMessage(msg, msgId);
                                        }
                                        dontSwallowException = false;
                                        break;
                                    }
                                }
                            }
                            catch (DrawMessage.ParseException ex2) {}
                            catch (RuntimeException e) {
                                if (dontSwallowException) {
                                    throw e;
                                }
                            }
                        }
                        catch (RuntimeException ex) {
                            DrawboardEndpoint.log.error((Object)("Unexpected exception: " + ex.toString()), (Throwable)ex);
                        }
                    }
                });
            }
        };
    }
    
    public static Room getRoom(final boolean create) {
        if (create) {
            if (DrawboardEndpoint.room == null) {
                synchronized (DrawboardEndpoint.roomLock) {
                    if (DrawboardEndpoint.room == null) {
                        DrawboardEndpoint.room = new Room();
                    }
                }
            }
            return DrawboardEndpoint.room;
        }
        return DrawboardEndpoint.room;
    }
    
    public void onOpen(final Session session, final EndpointConfig config) {
        session.setMaxTextMessageBufferSize(10000);
        session.addMessageHandler((MessageHandler)this.stringHandler);
        final Client client = new Client(session);
        final Room room = getRoom(true);
        room.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    try {
                        DrawboardEndpoint.this.player = room.createAndAddPlayer(client);
                    }
                    catch (IllegalStateException ex) {
                        client.sendMessage(new StringWebsocketMessage("0" + ex.getLocalizedMessage()));
                        client.close();
                    }
                }
                catch (RuntimeException ex2) {
                    DrawboardEndpoint.log.error((Object)("Unexpected exception: " + ex2.toString()), (Throwable)ex2);
                }
            }
        });
    }
    
    public void onClose(final Session session, final CloseReason closeReason) {
        final Room room = getRoom(false);
        if (room != null) {
            room.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (DrawboardEndpoint.this.player != null) {
                            DrawboardEndpoint.this.player.removeFromRoom();
                            DrawboardEndpoint.this.player = null;
                        }
                    }
                    catch (RuntimeException ex) {
                        DrawboardEndpoint.log.error((Object)("Unexpected exception: " + ex.toString()), (Throwable)ex);
                    }
                }
            });
        }
    }
    
    public void onError(final Session session, final Throwable t) {
        int count;
        Throwable root;
        for (count = 0, root = t; root.getCause() != null && count < 20; root = root.getCause(), ++count) {}
        if (!(root instanceof EOFException)) {
            if (session.isOpen() || !(root instanceof IOException)) {
                DrawboardEndpoint.log.error((Object)("onError: " + t.toString()), t);
            }
        }
    }
    
    static {
        log = LogFactory.getLog((Class)DrawboardEndpoint.class);
        DrawboardEndpoint.room = null;
        roomLock = new Object();
    }
}
