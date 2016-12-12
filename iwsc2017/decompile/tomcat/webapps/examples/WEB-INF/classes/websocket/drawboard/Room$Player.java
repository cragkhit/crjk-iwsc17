// 
// Decompiled by Procyon v0.5.29
// 

package websocket.drawboard;

import websocket.drawboard.wsmessages.AbstractWebsocketMessage;
import websocket.drawboard.wsmessages.StringWebsocketMessage;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

public final class Player
{
    private Room room;
    private long lastReceivedMessageId;
    private final Client client;
    private final List<DrawMessage> bufferedDrawMessages;
    
    private List<DrawMessage> getBufferedDrawMessages() {
        return this.bufferedDrawMessages;
    }
    
    private Player(final Room room, final Client client) {
        this.lastReceivedMessageId = 0L;
        this.bufferedDrawMessages = new ArrayList<DrawMessage>();
        this.room = room;
        this.client = client;
    }
    
    public Room getRoom() {
        return this.room;
    }
    
    public Client getClient() {
        return this.client;
    }
    
    public void removeFromRoom() {
        if (this.room != null) {
            Room.access$900(this.room, this);
            this.room = null;
        }
    }
    
    private long getLastReceivedMessageId() {
        return this.lastReceivedMessageId;
    }
    
    private void setLastReceivedMessageId(final long value) {
        this.lastReceivedMessageId = value;
    }
    
    public void handleDrawMessage(final DrawMessage msg, final long msgId) {
        Room.access$1000(this.room, this, msg, msgId);
    }
    
    private void sendRoomMessage(final MessageType type, final String content) {
        Objects.requireNonNull(content);
        Objects.requireNonNull(type);
        final String completeMsg = String.valueOf(type.flag) + content;
        this.client.sendMessage(new StringWebsocketMessage(completeMsg));
    }
}
